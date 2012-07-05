package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.ModificationLogService;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        executor.shutdown();
        shutDown = true;
    }

    private class WorkItem {

        final ModificationsAwareEntity entity;
        final int id;
        final ModificationType modificationType;

        public WorkItem(ModificationsAwareEntity entity, int id, ModificationType modificationType) {
            this.entity = entity;
            this.id = id;
            this.modificationType = modificationType;
        }
    }

    @Override
    public void reportDelete(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        queue.offer(new WorkItem(entity, id, ModificationType.DELETE));
    }

    @Override
    public void reportInsert(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        queue.offer(new WorkItem(entity, id, ModificationType.INSERT));
    }

    @Override
    public void reportUpdate(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        queue.offer(new WorkItem(entity, id, ModificationType.UPDATE));
    }

    @SuppressWarnings({"unchecked"})
    @MethodTiming(name = "modificationLogForAllActiveFields")
    private void forAllActiveFields(WorkItem workItem) {
        final ModificationsAwareEntity entity = workItem.entity;
        final EntityType entityType = metamodel.entity(entity.getClass());
        final String entityName = entityType.getName();
        try {
            final Set<Attribute> attributes = entityType.getAttributes();
            for (javax.persistence.metamodel.Attribute attribute : attributes) {
                final String fieldName = attribute.getName();
                final Field field = entity.getClass().getDeclaredField(fieldName);
                final boolean accessible = field.isAccessible();
                if (!accessible)
                    field.setAccessible(true);
                try {
                    final Object fieldValue;
                    if (attribute instanceof PluralAttribute) {
                        PluralAttribute plural = (PluralAttribute) attribute;
                        if (!(ModificationsAwareEntity.class.isAssignableFrom(plural.getElementType().getJavaType())))
                            throw new RuntimeException("The entity collection type does not contain ModificationsAwareEntity; entity="+entityType+", field="+fieldName);

                        final Iterable<Integer> childIds = Iterables.transform((Collection) field.get(entity), new Function<Object, Integer>() {
                            public Integer apply(@Nullable Object o) {
                                return ((ModificationsAwareEntity) o).getId();
                            }
                        });
                        fieldValue = Joiner.on(',').join(childIds);
                    } else {
                        fieldValue = field.get(entity);
                    }
                    if (log.isDebugEnabled())
                        log.debug(String.format("Writing modification log for entity=%s, id=%d, fieldName=%s", //NON-NLS
                                entityName, workItem.id, fieldName)); //NON-NLS
                    modificationRepository.addModificationLog(workItem.modificationType, entityName,
                            workItem.id, fieldName, fieldValue, currentDatabaseVersion);
                } finally {
                    if (!accessible)
                        field.setAccessible(false);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Runtime exception while creating modification log item on entity: " + entity.getClass() + ", id=" + workItem.id + ", type=" + workItem.modificationType.toString(), e);
        }
    }

    private void forDeleteAction(WorkItem workItem) {
        final ModificationsAwareEntity entity = workItem.entity;
        final EntityType entityType = metamodel.entity(entity.getClass());
        modificationRepository.addDeleteModificationLog(entityType.getName(), workItem.id, currentDatabaseVersion);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
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
                }
            }
        });
    }
}
