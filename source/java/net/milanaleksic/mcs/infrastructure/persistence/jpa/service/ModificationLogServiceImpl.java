package net.milanaleksic.mcs.infrastructure.persistence.jpa.service;

import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.ModificationLogService;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.persistence.metamodel.*;
import java.lang.reflect.Field;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 2:46 PM
 */
@Service
public class ModificationLogServiceImpl implements ModificationLogService, ApplicationContextAware {

    @Autowired
    private ModificationRepository modificationRepository;

    @PersistenceContext(name = "MovieCatalogSystemDB")
    protected EntityManager entityManager;

    private Metamodel metamodel;

    @Value("${mcs.db.version}")
    private int currentDatabaseVersion;

    //TODO: what with many2many???

    @Override
    public void reportDelete(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        modificationRepository.addDeleteModificationLog(entity.getId(),
                metamodel.entity(entity.getClass()).getName(), currentDatabaseVersion);
    }

    @Override
    public void reportInsert(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        forAllActiveFields(entity, id, ModificationType.INSERT);
    }

    @SuppressWarnings({"unchecked"})
    @MethodTiming(name = "modificationLogForAllActiveFields")
    private void forAllActiveFields(ModificationsAwareEntity entity, int id, ModificationType modificationType) {
        EntityType entityType = metamodel.entity(entity.getClass());
        try {
            final Set<Attribute> attributes = entityType.getAttributes();
            for (javax.persistence.metamodel.Attribute attribute : attributes) {
                final String fieldName = attribute.getName();
                final Field field = entity.getClass().getField(fieldName);
                final boolean accessible = field.isAccessible();
                if (!accessible)
                    field.setAccessible(true);
                try {
                    final Object fieldValue = field.get(entity);
                    modificationRepository.addModificationLog(modificationType, id, currentDatabaseVersion,
                            entityType.getName(), fieldName, fieldValue);
                } finally {
                    if (!accessible)
                        field.setAccessible(false);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Runtime exception while creating modification log item", e);
        }
    }

    @Override
    public void reportUpdate(int id, ModificationsAwareEntity entity) {
        checkNotNull(entity);
        forAllActiveFields(entity, id, ModificationType.INSERT);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        metamodel = entityManager.getMetamodel();
    }
}
