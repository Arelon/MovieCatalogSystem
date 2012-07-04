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
    public void addDeleteModificationLog(int id, String entityName, int currentDatabaseVersion) {
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setModificationType(ModificationType.DELETE);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        modification.setClock(getNextClockForEntity(entityName, id));
        entityManager.persist(modification);
    }

    @Override
    public void addModificationLog(ModificationType modificationType, int id, int currentDatabaseVersion,
                                   String entityName, String fieldName, Object fieldValue) {
        Modification modification = new Modification();
        modification.setEntityId(id);
        modification.setEntity(entityName);
        modification.setDbVersion(currentDatabaseVersion);
        modification.setClock(getNextClockForEntity(entityName, id));
        modification.setModificationType(modificationType);
        //TODO: find previous value. Don't commit if it's the same!
        entityManager.persist(modification);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    private long getNextClockForEntity(String entityName, int entityId) {
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
