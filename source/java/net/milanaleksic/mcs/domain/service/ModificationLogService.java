package net.milanaleksic.mcs.domain.service;

import net.milanaleksic.mcs.domain.model.ModificationsAwareEntity;

/**
 * User: Milan Aleksic
 * Date: 7/2/12
 * Time: 2:44 PM
 */
public interface ModificationLogService {

    void reportDelete(int id, ModificationsAwareEntity serializable);

    void reportInsert(int id, ModificationsAwareEntity serializable);

    void reportUpdate(int id, ModificationsAwareEntity serializable);

    void pumpAllModificationLogItems(boolean waitForTermination);

    int getNextClock();

}
