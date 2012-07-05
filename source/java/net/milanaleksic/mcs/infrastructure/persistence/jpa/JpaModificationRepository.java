package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;

import javax.persistence.*;

/**
 * User: Milan Aleksic
 * Date: 7/3/12
 * Time: 10:05 AM
 */
@Repository
@Transactional
@SuppressWarnings({"HardCodedStringLiteral"})
public class JpaModificationRepository extends AbstractRepository implements ModificationRepository {

    @Override
    public void addDeleteModificationLog(String entityName, int id, int currentDatabaseVersion) {
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setModificationType(ModificationType.DELETE);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        modification.setClock(getNextClockForEntity(entityName, id));
        entityManager.persist(modification);
    }

    @Override
    public void addModificationLog(ModificationType modificationType, String entityName, int id, String fieldName, Object fieldValue, int currentDatabaseVersion, long clock) {
        final String newValue = fieldValue == null ? null : fieldValue.toString();
        try {
            final String previousValue = getPreviousValue(entityName, id, fieldName);
            if (previousValue != null && previousValue.equals(newValue))
                return;
        } catch(NoResultException ignored) {
        }
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        modification.setClock(clock);
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

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public long getNextClockForEntity(String entityName, int entityId) {
        final TypedQuery<Long> $ = entityManager.createNamedQuery("getNextClockForEntity", Long.class);
        $.setParameter("entityName", entityName);
        $.setParameter("entityId", entityId);
        try {
            final Long singleResult = $.getSingleResult();
            if (singleResult != null)
                return singleResult;
            return 1;
        } catch (NoResultException e) {
            return 1;
        }
    }

}
