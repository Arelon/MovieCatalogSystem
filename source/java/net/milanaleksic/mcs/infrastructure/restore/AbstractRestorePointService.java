package net.milanaleksic.mcs.infrastructure.restore;

import com.google.common.base.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:38 PM
 */
public abstract class AbstractRestorePointService {

    protected static final String MCS_VERSION_TAG = "/*MCS-VERSION: "; //NON-NLS

    protected static final String SCRIPT_KATALOG_RESTORE = "KATALOG_RESTORE.sql";   //NON-NLS

    public static final String SCRIPT_KATALOG_RESTORE_LOCATION = "restore" + File.separator + SCRIPT_KATALOG_RESTORE; //NON-NLS

    private int dbVersion;

    @Inject protected DataSource dataSource;

    protected final Log log = LogFactory.getLog(this.getClass());

    protected boolean useDB2StyleStringInScripts = false;

    public void setUseDB2StyleStringInScripts(boolean useDB2StyleStringInScripts) {
        this.useDB2StyleStringInScripts = useDB2StyleStringInScripts;
    }

    protected synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void close(Optional<? extends Closeable> pos) {
        if (pos.isPresent()) {
            try { pos.get().close(); } catch(IOException ignored) {}
        }
    }

    protected int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}
