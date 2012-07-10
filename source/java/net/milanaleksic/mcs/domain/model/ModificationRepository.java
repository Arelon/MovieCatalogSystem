package net.milanaleksic.mcs.domain.model;

import java.util.concurrent.atomic.AtomicReference;

/**
 * User: Milan Aleksic
 * Date: 7/3/12
 * Time: 10:04 AM
 */
public interface ModificationRepository {

    void addDeleteModificationLog(int clock, String entityName, int id, int currentDatabaseVersion);

    void addModificationLogWithLazyClock(AtomicReference<Integer> lazyClock, ModificationType modificationType, String entityName, int id, String fieldName, Object fieldValue, int currentDatabaseVersion);
}
