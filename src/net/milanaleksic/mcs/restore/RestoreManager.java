package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
import net.milanaleksic.mcs.config.ApplicationConfigurationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import net.milanaleksic.mcs.gui.ClosingForm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 10:11 PM
 */
public class RestoreManager implements LifecycleListener {

    @Autowired private RestorePointCreator restorePointCreator;

    @Autowired private RestorePointRestorer restorePointRestorer;

    @Override public void applicationStarted() {
         restorePointRestorer.restoreDatabaseIfNeeded();
    }

    @Override public void applicationShutdown() {
        if (ApplicationManager.getApplicationConfiguration().getDatabaseConfiguration().isDatabaseCreateRestore()) {
            new ClosingForm();
            restorePointCreator.createRestorePoint();
        }
    }

}
