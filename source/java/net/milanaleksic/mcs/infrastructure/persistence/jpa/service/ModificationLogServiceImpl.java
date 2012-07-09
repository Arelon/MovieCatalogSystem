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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.metamodel.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 2:46 PM
 */
@Transactional(readOnly = false)
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
                fieldInformationMap.put(fieldName, new FieldInformation(attribute, field));
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("It was unexpected that an entity does not have a field identical to entity's attribute " + entityClass + ", details: ", e);
        }
        cachedEntityInformation.put(entityClass, entityInformation = new EntityInformation(entityType, fieldInformationMap.build()));
        return entityInformation;
    }

    @SuppressWarnings({"unchecked"})
    @MethodTiming(name = "modificationLogForAllActiveFields")
    private void forAllActiveFields(WorkItem workItem) {
        EntityInformation entityInformation = getOrPrepareEntityInformation(workItem.entity.getClass());
        final String entityName = entityInformation.entityType.getName();
        try {
            for (Map.Entry<String, FieldInformation> mapEntry : entityInformation.fieldDetailsMap.entrySet()) {
                final String fieldName = mapEntry.getKey();
                final Field field = mapEntry.getValue().field;
                final Attribute attribute = mapEntry.getValue().attribute;
                final Object fieldValue;
                if (attribute instanceof PluralAttribute) {
                    PluralAttribute plural = (PluralAttribute) attribute;
                    if (!(ModificationsAwareEntity.class.isAssignableFrom(plural.getElementType().getJavaType())))
                        throw new RuntimeException("The entity collection type does not contain ModificationsAwareEntity; entity=" + entityName + ", field=" + fieldName);

                    final Collection fromIterable = (Collection) field.get(workItem.entity);
                    if (fromIterable == null)
                        fieldValue = null;
                    else {
                        final Iterable<Integer> childIds = Iterables.transform(fromIterable, new Function<Object, Integer>() {
                            public Integer apply(Object o) {
                                return ((ModificationsAwareEntity) o).getId();
                            }
                        });
                        fieldValue = Joiner.on(',').join(childIds);
                    }
                } else {
                    fieldValue = field.get(workItem.entity);
                }
                if (log.isDebugEnabled())
                    log.debug(String.format("Writing modification log for entity=%s, id=%d, fieldName=%s", //NON-NLS
                            entityName, workItem.id, fieldName)); //NON-NLS
                modificationRepository.addModificationLog(workItem.modificationType, entityName,
                        workItem.id, fieldName, fieldValue, currentDatabaseVersion);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Runtime exception while creating modification log item on entity: " + entityName + ", id=" + workItem.id + ", type=" + workItem.modificationType.toString(), e);
        }
    }

    private void forDeleteAction(WorkItem workItem) {
        final ModificationsAwareEntity entity = workItem.entity;
        final EntityType entityType = metamodel.entity(entity.getClass());
        modificationRepository.addDeleteModificationLog(entityType.getName(), workItem.id, currentDatabaseVersion);
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
                    for (WorkItem workItem = null; ; workItem = queue.poll(100, TimeUnit.MILLISECONDS)) {
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
}
