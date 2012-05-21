package net.milanaleksic.mcs.application.tenrec;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.messages.ResourceBundleSource;
import net.milanaleksic.mcs.infrastructure.restore.AbstractRestorePointService;
import net.milanaleksic.mcs.infrastructure.tenrec.TenrecService;
import net.milanaleksic.tenrec.client.VersionInformation;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.springframework.util.FileCopyUtils;

import javax.inject.Inject;
import java.io.*;
import java.net.URI;

/**
 * User: Milan Aleksic
 * Date: 5/11/12
 * Time: 2:09 PM
 */
public class TenrecManager implements LifeCycleListener {

    private static final Logger log = Logger.getLogger(TenrecManager.class);

    @Inject
    private TenrecService tenrecService;

    @Inject
    private ResourceBundleSource resourceBundleSource;

    public void offerNewerVersionDownloadIfOneExists(final Shell owner) {
        Optional<VersionInformation> information = tenrecService.findNewerVersionIfAllowed();
        if (!information.isPresent()) {
            if (log.isDebugEnabled())
                log.debug("No new version found"); //NON-NLS
            return;
        }
        final VersionInformation versionInformation = information.get();
        owner.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                final MessageBox messageBox = new MessageBox(owner, SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
                messageBox.setText(resourceBundleSource.getMessagesBundle().getString("main.newVersionAvailable"));
                messageBox.setText(String.format(resourceBundleSource.getMessagesBundle().getString("main.newVersionInformation"),
                        versionInformation.getMajorVersion(),
                        versionInformation.getMinorVersion(),
                        versionInformation.getBuildNo(),
                        versionInformation.getDateBuilt()));
                if (messageBox.open() != SWT.YES)
                    return;
                if (java.awt.Desktop.isDesktopSupported()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                java.awt.Desktop.getDesktop().browse(new URI(versionInformation.getDirectUri()));
                            } catch (Exception e) {
                                log.error("Unexpected error: " + e.getMessage(), e); //NON-NLS
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        final File restoreFile = new File(AbstractRestorePointService.SCRIPT_KATALOG_RESTORE_LOCATION);
        if (!restoreFile.exists())
            return;
        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(restoreFile);
            if (log.isDebugEnabled())
                log.debug("Sending DB to server"); //NON-NLS
            final boolean success = tenrecService.saveDatabase(bytes);
            if (log.isDebugEnabled())
                log.debug("Sending DB to server finished with " + (success ? "success" : "failure")); //NON-NLS
        } catch (IOException e) {
            log.error("Failed to send the DB to server: " + e.getMessage(), e); //NON-NLS
        }
    }
}
