package net.milanaleksic.mcs.domain.model;

/**
 * User: Milan Aleksic
 * Date: 7/3/12
 * Time: 10:04 AM
 */
public interface ModificationRepository {

    void addDeleteModificationLog(int id, String entityName, int currentDatabaseVersion);

    void addModificationLog(ModificationType modificationType, int id, int currentDatabaseVersion, String entityName, String fieldName, Object fieldValue);
}
