package net.milanaleksic.mcs.infrastructure.tenrec;

import com.google.common.base.Optional;
import net.milanaleksic.tenrec.client.VersionInformation;

/**
 * User: Milan Aleksic
 * Date: 5/8/12
 * Time: 3:01 PM
 */
public interface TenrecService {

    Optional<VersionInformation> findNewerVersionIfAllowed();

    boolean login();

    boolean saveDatabase(byte[] bytes);

    void logOut();
}
