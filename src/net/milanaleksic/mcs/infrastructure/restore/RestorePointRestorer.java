package net.milanaleksic.mcs.infrastructure.restore;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.restore.alter.AlterScript;
import net.milanaleksic.mcs.infrastructure.util.DBUtil;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;

import java.io.*;
import java.sql.*;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:26 PM
 */
public class RestorePointRestorer extends AbstractRestorePointService {

    private String patternForSqlAlters;
    private String patternForCodeAlters;
    private String versionScript;

    public void restoreDatabaseIfNeeded() {
        try (Connection conn = getConnection()) {
            safeRestoreDatabaseIfNeeded(conn);
        } catch (SQLException | IOException e) {
            log.error("Failure while restoring database", e);
        }
    }

    public int getDatabaseVersionFromDatabase(Connection conn) {
        if (log.isInfoEnabled())
            log.info("Validating database");
        try (PreparedStatement st = conn.prepareStatement(versionScript)) {
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                String dbVersion = rs.getString(1);
                if (log.isInfoEnabled())
                    log.info("Expected DB version = " + getDbVersion() + ", DB version = " + dbVersion);

                return Integer.valueOf(dbVersion);
            }
        } catch (Exception e) {
            log.error("Validation failed - " + e.getMessage());
        }
        return 0;
    }

    private void safeRestoreDatabaseIfNeeded(Connection conn) throws SQLException, IOException {
        int activeMCSDBVersion = getDbVersion();
        int versionFromDatabase = getDatabaseVersionFromDatabase(conn);
        if (versionFromDatabase == activeMCSDBVersion)
            return;

        if (versionFromDatabase > activeMCSDBVersion)
            throw new IllegalStateException(
                    String.format("Database is not supported (MCS is of activeMCSDBVersion %d, but your database is of versionFromDatabase %d)",
                            activeMCSDBVersion, versionFromDatabase));

        int versionFromRestorePoint = getDatabaseVersionFromRestorePoint();
        if (versionFromRestorePoint > activeMCSDBVersion) {
            throw new IllegalStateException(
                    String.format("Database restore point is not supported (MCS is of activeMCSDBVersion %d, but your database is of versionFromRestorePoint %d)",
                            activeMCSDBVersion, versionFromRestorePoint));
        }
        runDatabaseRecreation(versionFromDatabase, versionFromRestorePoint, conn);
    }

    private int getDatabaseVersionFromRestorePoint() {
        File restoreFile = new File("restore//" + SCRIPT_KATALOG_RESTORE);
        if (!restoreFile.exists())
            return 0;
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(restoreFile), "UTF-8"));
            String firstLine = reader.readLine();
            if (firstLine != null)
                if (firstLine.contains(MCS_VERSION_TAG))
                    return Integer.parseInt((firstLine.indexOf(MCS_VERSION_TAG) + firstLine.substring(MCS_VERSION_TAG.length())).trim());
        } catch (IOException e) {
            log.error("IO Error while reading DB version from restore point", e);
        } finally {
            if (reader != null)
                try { reader.close(); } catch (IOException ignored) {}
        }
        return 1;
    }

    private void runDatabaseRecreation(int dbVersionFromDatabase, int dbVersionFromRestorePoint, Connection conn) throws SQLException, IOException {
        if (log.isDebugEnabled())
            log.debug(String.format("RunDatabaseRecreation: dbVersionFromDatabase=%d, dbVersionFromRestorePoint=%d", dbVersionFromDatabase, dbVersionFromRestorePoint));
        int startAlterVersion = dbVersionFromDatabase+1;
        if (dbVersionFromDatabase == 0) {
            int ending = dbVersionFromRestorePoint == 0 ? 1 : dbVersionFromRestorePoint;
            for (int i=startAlterVersion; i<=ending; i++) {
                runAltersForVersion(conn, i);
            }
            startAlterVersion = ending+1;

            if (dbVersionFromRestorePoint != 0) {
                if (log.isInfoEnabled())
                    log.info("Restoring content");
                DBUtil.executeScriptOnConnection("restore//" + SCRIPT_KATALOG_RESTORE, conn);
            }
        }
        for (int i=startAlterVersion; i<=getDbVersion(); i++)
            runAltersForVersion(conn, i);
        if (log.isInfoEnabled())
            log.info("Restoration finished");
    }

    private void runAltersForVersion(final Connection conn, final int alterVersion) throws IOException, SQLException {
        if (log.isInfoEnabled())
            log.info("Running SQL DB alter "+ String.format(patternForSqlAlters, alterVersion));
        try {
            StreamUtil.useClasspathResource(String.format(patternForSqlAlters, alterVersion), new Function<InputStream, Void>() {
                @Override
                public Void apply(InputStream inputStream) {
                    try {
                        if (inputStream != null)  {
                            // file has not been found, there is SQL alter... proceed
                            DBUtil.executeScriptOnConnection(inputStream, conn);
                        }
                    } catch (IOException e) {
                        log.error("Unexpected exception occurred while running SQL alter for version "+alterVersion, e);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            AlterScript alterScript = (AlterScript) Class.forName(
                    String.format(patternForCodeAlters, alterVersion)).newInstance();
            if (log.isInfoEnabled())
                log.info("Running application-driven DB alter "+ String.format(patternForCodeAlters, alterVersion));
            alterScript.executeAlterOnConnection(conn);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {
            // if class has not been found, there is no app-driven alter... proceed
        }
    }

    public void setPatternForSqlAlters(String patternForSqlAlters) {
        this.patternForSqlAlters = patternForSqlAlters;
    }

    public void setPatternForCodeAlters(String patternForCodeAlters) {
        this.patternForCodeAlters = patternForCodeAlters;
    }

    public void setVersionScript(String versionScript) {
        this.versionScript = versionScript;
    }
}
