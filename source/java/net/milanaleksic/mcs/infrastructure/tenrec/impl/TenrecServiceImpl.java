package net.milanaleksic.mcs.infrastructure.tenrec.impl;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.tenrec.TenrecService;
import net.milanaleksic.tenrec.client.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/8/12
 * Time: 3:01 PM
 */
public class TenrecServiceImpl implements TenrecService {

    private static final Logger log = Logger.getLogger(TenrecServiceImpl.class);

    @Inject
    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    private Tenrec tenrecWS;

    private String applicationIdentifier;

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;
    }

    @Override
    public Optional<VersionInformation> findNewerVersion() {
        try {
            if (log.isDebugEnabled())
                log.debug("Checking Tenrec for newest version of the application "+applicationIdentifier); //NON-NLS
            final Optional<VersionInformation> ofTheJedi = Optional.fromNullable(tenrecWS.getLatestVersionInformation(applicationIdentifier));
            if (!ofTheJedi.isPresent())
                return ofTheJedi;

            final VersionInformation remoteVersionInformation = ofTheJedi.get();
            final String version = net.milanaleksic.mcs.infrastructure.util.VersionInformation.getVersion();
            final String[] versionSegments = version.split("\\.");
            final int localMajor = Integer.parseInt(versionSegments[0], 10);
            final int localMinor = Integer.parseInt(versionSegments[1], 10);
            final int localBuildNo = Integer.parseInt(versionSegments[2], 10);

            if (localMajor > remoteVersionInformation.getMajorVersion())
                return Optional.absent();
            if (localMinor > remoteVersionInformation.getMinorVersion())
                return Optional.absent();
            if (localBuildNo >= remoteVersionInformation.getBuildNo())
                return Optional.absent();

            return ofTheJedi;
        } catch (Throwable t) {
            log.error("Failure while checking newest version of MCS: "+t.getMessage(), t); //NON-NLS
        }
        return Optional.absent();
    }
}
