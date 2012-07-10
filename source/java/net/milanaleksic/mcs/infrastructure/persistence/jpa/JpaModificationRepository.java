package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.ModificationLogService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import javax.inject.Inject;
import javax.persistence.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Milan Aleksic
 * Date: 7/3/12
 * Time: 10:05 AM
 */
@Repository
@Transactional
@SuppressWarnings({"HardCodedStringLiteral"})
public class JpaModificationRepository extends AbstractRepository implements ModificationRepository {

    @Inject
    private ModificationLogService modificationLogService;

    @Override
    public void addDeleteModificationLog(int clock, String entityName, int id, int currentDatabaseVersion) {
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setModificationType(ModificationType.DELETE);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        modification.setClock(clock);
        entityManager.persist(modification);
    }

    @Override
    public void addModificationLogWithLazyClock(AtomicReference<Integer> lazyClock, ModificationType modificationType, String entityName, int id, String fieldName, Object fieldValue, int currentDatabaseVersion) {
        final String newValue = fieldValue == null ? null : fieldValue.toString();
        try {
            final String previousValue = getPreviousValue(entityName, id, fieldName);
            if (previousValue != null && previousValue.equals(newValue))
                return;
        } catch(NoResultException ignored) {
        }
        if (log.isDebugEnabled())
            log.debug(String.format("Writing modification log for entity=%s, id=%d, fieldName=%s", entityName, id, fieldName));
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        if (lazyClock.get() == null)
            lazyClock.compareAndSet(null, modificationLogService.getNextClock());
        modification.setClock(lazyClock.get());
        modification.setModificationType(modificationType);
        modification.setField(fieldName);
        modification.setValue(newValue);
        entityManager.persist(modification);
    }

    private String getPreviousValue(String entityName, int id, String fieldName) {
        final TypedQuery<String> previousFieldValueQuery = entityManager.createNamedQuery("getPreviousFieldValue", String.class);
        previousFieldValueQuery.setParameter("entity", entityName);
        previousFieldValueQuery.setParameter("entityId", id);
        previousFieldValueQuery.setParameter("fieldName", fieldName);
        previousFieldValueQuery.setMaxResults(1);
        return previousFieldValueQuery.getSingleResult();
    }

}
