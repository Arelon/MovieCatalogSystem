package net.milanaleksic.mcs.domain.model;

/**
 * User: Milan Aleksic
 * Date: 7/3/12
 * Time: 10:04 AM
 */
public interface ModificationRepository {

    void addDeleteModificationLog(String entityName, int id, int currentDatabaseVersion);

    void addModificationLog(ModificationType modificationType, String entityName, int id, String fieldName, Object fieldValue, int currentDatabaseVersion);

}
