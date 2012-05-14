package net.milanaleksic.mcs.infrastructure.tenrec.impl;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.tenrec.TenrecService;
import net.milanaleksic.tenrec.client.*;
import org.apache.log4j.Logger;
import org.springframework.remoting.RemoteAccessException;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 5/8/12
 * Time: 3:01 PM
 */
public class TenrecServiceImpl implements TenrecService, LifeCycleListener {

    private static final Logger log = Logger.getLogger(TenrecServiceImpl.class);

    @Inject
    @SuppressWarnings({"SpringJavaAutowiringInspection"})
    private Tenrec tenrecWS;

    private String applicationIdentifier;

    private String sessionId = null;

    private Optional<UserConfiguration> userConfiguration = Optional.absent();

    public void setApplicationIdentifier(String applicationIdentifier) {
        this.applicationIdentifier = applicationIdentifier;
    }

    @Override
    public Optional<VersionInformation> findNewerVersionIfAllowed() {
        try {
            if (!userConfiguration.get().getTenrecConfiguration().isCheckForNewVersionsOnStartup())
                return Optional.absent();
            if (log.isDebugEnabled())
                log.debug("Checking Tenrec for newest version of the application " + applicationIdentifier); //NON-NLS
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
        } catch (RemoteAccessException e) {
            log.warn("Tenrec server is unavailable - " + e.getMessage()); //NON-NLS
        } catch (Throwable t) {
            log.error("Failure while checking newest version of MCS: " + t.getMessage(), t); //NON-NLS
        }
        return Optional.absent();
    }

    @Override
    public boolean login() {
        if (sessionId != null)
            return true;
        try {
            final UserConfiguration.TenrecConfiguration tenrecConfiguration = userConfiguration.get().getTenrecConfiguration();
            final SessionAndUser sessionAndUser = tenrecWS.login(tenrecConfiguration.getUsername(), tenrecConfiguration.getPassword());
            if (sessionAndUser == null) {
                log.error("Error while logging in to Tenrec - no session bean received"); //NON-NLS
                return false;
            }
            sessionId = sessionAndUser.getSessionId();
            if (sessionId == null)
                log.error("Error while logging in to Tenrec - no session identifier received"); //NON-NLS
            return sessionId != null;
        } catch (RemoteAccessException e) {
            log.warn("Tenrec server is unavailable - " + e.getMessage()); //NON-NLS
        } catch (Exception e) {
            log.error("Error while logging in to Tenrec", e); //NON-NLS
        }
        return false;
    }

    @Override
    public synchronized boolean saveDatabase(byte[] bytes) {
        if (sessionId == null) {
            final boolean loginSuccess = login();
            if (!loginSuccess)
                return false;
        }
        try {
            tenrecWS.storeDatabase(bytes, sessionId);
            return true;
        } catch (RemoteAccessException e) {
            log.warn("Tenrec server is unavailable - " + e.getMessage()); //NON-NLS
        } catch (TenrecException_Exception e) {
            log.error("Database could not have been saved to Tenrec!", e); //NON-NLS
        }
        return false;
    }

    @Override
    public void logOut() {
        sessionId = null;
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
        this.userConfiguration = Optional.of(userConfiguration);
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
    }
}
