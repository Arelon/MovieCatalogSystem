package net.milanaleksic.mcs.infrastructure.persistence.jpa;

import net.milanaleksic.mcs.domain.model.*;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
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
        modification.setClock(getNextClock());
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
    public int getNextClock() {
        final Session session = (Session) entityManager.getDelegate();
        try {
            final SQLQuery sqlQuery = session.createSQLQuery("select next value for DB2ADMIN.MODIFICATION_CLOCK");
            final Object $ = sqlQuery.uniqueResult();
            return Integer.parseInt($.toString());
        } catch (HibernateException e) {
            return 1;
        } finally {
            session.flush();
            session.close();
        }
    }

}
