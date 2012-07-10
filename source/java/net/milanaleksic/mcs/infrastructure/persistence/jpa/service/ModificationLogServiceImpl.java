package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.ModificationLogService;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.transaction.annotation.*;

import javax.persistence.*;
import javax.persistence.Query;
import javax.persistence.metamodel.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 2:46 PM
 */
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class ModificationLogServiceImpl extends AbstractService
        implements ModificationLogService, ApplicationContextAware, LifeCycleListener {

    @Autowired
    private ModificationRepository modificationRepository;

    private Metamodel metamodel;

    @Value("${mcs.db.version}")
    private int currentDatabaseVersion;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BlockingQueue<WorkItem> queue = new LinkedBlockingQueue<>();

    private volatile boolean shutDown = false;
    private CountDownLatch shutdownProcessed = new CountDownLatch(1);

    private boolean enabled;

    private Map<Class<?>, EntityInformation> cachedEntityInformation = Maps.newHashMap();

    private class EntityInformation {

        private final EntityType<? extends ModificationsAwareEntity> entityType;

        private final ImmutableMap<String, FieldInformation> fieldDetailsMap;

        private EntityInformation(EntityType<? extends ModificationsAwareEntity> entityType, ImmutableMap<String, FieldInformation> fieldDetailsMap) {
            this.entityType = entityType;
            this.fieldDetailsMap = fieldDetailsMap;
        }
    }

    private class FieldInformation {

        private final Attribute attribute;

        private final Field field;

        private FieldInformation(Attribute attribute, Field field) {
            this.attribute = attribute;
            this.field = field;
        }
    }

    private class WorkItem {

        private final ModificationsAwareEntity entity;
        private final int id;
        private final ModificationType modificationType;

        public WorkItem(ModificationsAwareEntity entity, int id, ModificationType modificationType) {
            this.entity = entity;
            this.id = id;
            this.modificationType = modificationType;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        pumpAllModificationLogItems(false);
    }

    @Override
    public void reportDelete(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        if (!enabled)
            return;
        queue.offer(new WorkItem(entity, id, ModificationType.DELETE));
    }

    @Override
    public void reportInsert(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        if (!enabled)
            return;
        queue.offer(new WorkItem(entity, id, ModificationType.INSERT));
    }

    @Override
    public void reportUpdate(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        if (!enabled)
            return;
        queue.offer(new WorkItem(entity, id, ModificationType.UPDATE));
    }

    @Override
    public void pumpAllModificationLogItems(boolean waitForTermination) {
        if (!enabled)
            return;
        shutDown = true;
        executor.shutdown();
        if (waitForTermination) {
            try {
                shutdownProcessed.await();
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for the shutdown in modification log service", e); //NON-NLS
            }
        }
    }

    private EntityInformation getOrPrepareEntityInformation(Class<? extends ModificationsAwareEntity> entityClass) {
        EntityInformation entityInformation = cachedEntityInformation.get(entityClass);
        if (entityInformation != null)
            return entityInformation;

        final EntityType<? extends ModificationsAwareEntity> entityType = metamodel.entity(entityClass);
        ImmutableMap.Builder<String, FieldInformation> fieldInformationMap = ImmutableMap.builder();
        try {
            for (Attribute<?, ?> attribute : entityType.getAttributes()) {
                final String fieldName = attribute.getName();
                final Field field = entityClass.getDeclaredField(fieldName);
                if (!field.isAccessible())
                    field.setAccessible(true); // we are NOT going to make field un-accessible again, waste of time!

                boolean shouldAvoid = false;
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof OneToMany) {
                        // we will not record this part of relation
                        shouldAvoid = true;
                        break;
                    }
                    if (annotation instanceof ManyToMany) {
                        final ManyToMany m2mAnnotation = (ManyToMany) annotation;
                        if (!("".equals(m2mAnnotation.mappedBy()))) {
                            // we will record only "mappedBy" side of the relation
                            shouldAvoid = true;
                            break;
                        }
                    }
                }
                if (!shouldAvoid)
                    fieldInformationMap.put(fieldName, new FieldInformation(attribute, field));
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("It was unexpected that an entity does not have a field identical to entity's attribute " + entityClass + ", details: ", e);
        }
        cachedEntityInformation.put(entityClass, entityInformation = new EntityInformation(entityType, fieldInformationMap.build()));
        return entityInformation;
    }

    private void forAllActiveFields(WorkItem workItem) {
        try {
            recordFields(workItem, getOrPrepareEntityInformation(workItem.entity.getClass()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Runtime exception while creating modification log item on entity: " + workItem.getClass()
                    + ", id=" + workItem.id + ", type=" + workItem.modificationType.toString(), e);
        }
    }

    @MethodTiming(name = "modificationLogService.recordFields")
    private void recordFields(WorkItem workItem, EntityInformation entityInformation) throws IllegalAccessException {
        final AtomicReference<Integer> lazyClock = new AtomicReference<>(null);
        for (Map.Entry<String, FieldInformation> mapEntry : entityInformation.fieldDetailsMap.entrySet()) {
            final String fieldName = mapEntry.getKey();
            final Field field = mapEntry.getValue().field;
            final Attribute attribute = mapEntry.getValue().attribute;
            final Object fieldValue = attribute instanceof PluralAttribute
                    ? getPluralAttributeValue(workItem, entityInformation, mapEntry.getValue())
                    : field.get(workItem.entity);
            if (log.isDebugEnabled())
                log.debug(String.format("Writing modification log for entity=%s, id=%d, fieldName=%s", //NON-NLS
                        entityInformation.entityType.getName(), workItem.id, fieldName)); //NON-NLS
            modificationRepository.addModificationLogWithLazyClock(lazyClock, workItem.modificationType, entityInformation.entityType.getName(),
                    workItem.id, fieldName, fieldValue, currentDatabaseVersion);
        }
    }

    @SuppressWarnings({"unchecked"})
    private Object getPluralAttributeValue(WorkItem workItem, EntityInformation entityInformation, FieldInformation fieldInfo) throws IllegalAccessException {
        PluralAttribute pluralAttribute = (PluralAttribute) fieldInfo.attribute;
        if (!(ModificationsAwareEntity.class.isAssignableFrom(pluralAttribute.getElementType().getJavaType())))
            throw new RuntimeException("The entity collection type does not contain ModificationsAwareEntity; entity=" +
                    entityInformation.entityType.getName() + ", field=" + fieldInfo.field.getName());

        final Collection targetCollection = (Collection) fieldInfo.field.get(workItem.entity);
        if (targetCollection == null)
            return null;
        else {
            return Joiner.on(',').join(
                    Ordering.natural().sortedCopy(
                            Iterables.transform(targetCollection, new Function<Object, Integer>() {
                                public Integer apply(Object o) {
                                    return ((ModificationsAwareEntity) o).getId();
                                }
                            })
                    )
            );
        }
    }

    private void forDeleteAction(WorkItem workItem) {
        int clock = getNextClock();
        final ModificationsAwareEntity entity = workItem.entity;
        final EntityType entityType = metamodel.entity(entity.getClass());
        modificationRepository.addDeleteModificationLog(clock, entityType.getName(), workItem.id, currentDatabaseVersion);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!enabled)
            return;
        metamodel = entityManager.getMetamodel();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (WorkItem workItem = null; ; workItem = queue.poll(1000, TimeUnit.MILLISECONDS)) {
                        if (workItem == null) {
                            if (shutDown || Thread.currentThread().isInterrupted())
                                break;
                            continue;
                        }
                        if (workItem.modificationType == ModificationType.DELETE)
                            forDeleteAction(workItem);
                        else
                            forAllActiveFields(workItem);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Unexpected exception in the background modification log writer", e); //NON-NLS
                } finally {
                    shutdownProcessed.countDown();
                }
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public int getNextClock() {
        final Query query = entityManager.createNativeQuery("select next value for DB2ADMIN.MODIFICATION_CLOCK"); //NON-NLS
        final Object $ = query.getSingleResult();
        return Integer.parseInt($.toString());
    }
}
