package net.milanaleksic.mcs.restore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:26 PM
 */
public class RestorePointRestorer extends AbstractRestorePointService {

    protected final Log log = LogFactory.getLog(this.getClass());

    private static final String SCRIPT_RESOURCE_ALTER = "alter_script_%d.sql";

    public void restoreDatabaseIfNeeded() {
        Connection conn = null;
        try {
            conn = prepareDriverAndFetchConnection();
            safeRestoreDatabaseIfNeeded(conn);
        } catch (Exception e) {
            log.error("Failure while creating restore point", e);
        } finally {
            close(conn);
        }
    }

    public int getDatabaseVersionFromDatabase(Connection conn) {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            log.info("Validating database");
            st = conn.prepareStatement("SELECT Value FROM DB2ADMIN.Param WHERE Name='VERSION'");
            rs = st.executeQuery();
            rs.next();

            String dbVersion = rs.getString(1);
            log.info("Expected DB version = " + databaseConfiguration.getDBVersion() + ", DB version = " + dbVersion);

            return Integer.valueOf(dbVersion);
        } catch (Exception e) {
            log.error("Validation failed - " + e.getMessage());
        } finally {
            close(rs);
            close(st);
        }
        return 0;
    }

    private void safeRestoreDatabaseIfNeeded(Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        int activeMCSDBVersion = databaseConfiguration.getDBVersion();
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
            reader = new LineNumberReader(new FileReader(restoreFile));
            String firstLine = reader.readLine();
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

    private void runDatabaseRecreation(int dbVersionFromDatabase, int dbVersionFromRestorePoint, Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        log.debug(String.format("RunDatabaseRecreation: dbVersionFromDatabase=%d, dbVersionFromRestorePoint=%d", dbVersionFromDatabase, dbVersionFromRestorePoint));
        int startAlterVersion = dbVersionFromDatabase+1;
        if (dbVersionFromDatabase == 0) {
            int ending = dbVersionFromRestorePoint == 0 ? 1 : dbVersionFromRestorePoint;
            for (int i=startAlterVersion; i<=ending; i++) {
                log.info("Running alter "+i);
                executeScriptOnConnection(getClass().getResourceAsStream(
                        String.format(SCRIPT_RESOURCE_ALTER, i)), conn);
            }
            startAlterVersion = ending+1;

            if (dbVersionFromRestorePoint != 0) {
                log.info("Restoring content");
                executeScriptOnConnection("restore//" + SCRIPT_KATALOG_RESTORE, conn);
            }
        }
        for (int i=startAlterVersion; i<=databaseConfiguration.getDBVersion(); i++) {
            log.info("Running alter "+i);
            executeScriptOnConnection(getClass().getResourceAsStream(String.format(SCRIPT_RESOURCE_ALTER, i)), conn);
        }
        log.info("Restoration finished");
    }

    private void executeScriptOnConnection(String restartCountersScript, Connection conn) throws IOException, SQLException {
        File fileRestartCountersScript = new File(restartCountersScript);
        log.info("Restoring file: " + fileRestartCountersScript.getName());
        if (fileRestartCountersScript.exists()) {
            FileInputStream fis = new FileInputStream(fileRestartCountersScript);
            try {
                executeScriptOnConnection(fis, conn);
            } finally {
                close(fis);
            }
        }
    }

    private void executeScriptOnConnection(InputStream stream, Connection conn) throws IOException, SQLException {
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            script.append(line).append("\r\n");
        }

        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(script.toString());
            st.execute();
            conn.commit();
        } finally {
            close(st);
        }
    }

}
