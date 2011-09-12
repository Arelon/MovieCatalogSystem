package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:38 PM
 */
public abstract class AbstractRestorePointService implements InitializingBean {

    protected static final String MCS_VERSION_TAG = "/*MCS-VERSION: ";

    protected static final String SCRIPT_KATALOG_RESTORE = "KATALOG_RESTORE.sql";

    @Inject protected ApplicationManager applicationManager;

    @Inject protected DataSource dataSource;

    protected final Log log = LogFactory.getLog(this.getClass());

    protected ApplicationConfiguration.DatabaseConfiguration databaseConfiguration;

    protected boolean useDB2StyleStringInScripts = false;

    public void setUseDB2StyleStringInScripts(boolean useDB2StyleStringInScripts) {
        this.useDB2StyleStringInScripts = useDB2StyleStringInScripts;
    }

    protected synchronized Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        return dataSource.getConnection();
    }

    protected void close(OutputStream pos) {
        if (pos != null) {
            try { pos.close(); } catch(IOException ignored) {}
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseConfiguration = applicationManager.getApplicationConfiguration().getDatabaseConfiguration();
    }

}
