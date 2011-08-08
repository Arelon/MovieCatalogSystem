package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
import net.milanaleksic.mcs.config.ApplicationConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RestorePointService implements InitializingBean {

    private static final Log log = LogFactory.getLog(RestorePointService.class);

    private static final String STATEMENT_DELIMITER = ";";

    private static final String SCRIPT_RESOURCE_ALTER = "alter_script_%d.sql";

    private static final String SCRIPT_KATALOG_RESTART_COUNTERS = "KATALOG_RESTART_COUNTERS.sql";
    private static final String SCRIPT_KATALOG_RESTORE = "KATALOG_RESTORE.sql";
    private static final String SCRIPT_KATALOG_RESTORE_WITH_TIMESTAMP = "KATALOG_RESTORE_%s.sql";

    private static final String CREATE_SCRIPT_DB2_ONLY_HEADER =
            "CREATE DATABASE KATALOG ON 'D:' USING CODESET UTF-8 TERRITORY RU COLLATE USING UCA400_NO;\r\n" +
                    "\r\n" +
                    "connect to KATALOG user db2admin using db2admin;\r\n" +
                    "\r\n";

    private static final String MCS_VERSION_TAG = "/*MCS-VERSION: ";

    private static final String RESTORE_SCRIPT_HEADER =
            MCS_VERSION_TAG+"%d\n*/\n\nset schema DB2ADMIN;\n\n";

    private static final String RESTORE_SCRIPT_DB2_ONLY_HEADER =
            "CONNECT TO KATALOG USER DB2ADMIN USING db2admin";

    private static final String[] OUTPUT_SQLS = {
            "SELECT * FROM DB2ADMIN.TIPMEDIJA",
            "SELECT * FROM DB2ADMIN.POZICIJA",
            "SELECT * FROM DB2ADMIN.ZANR",
            "SELECT * FROM DB2ADMIN.MEDIJ",
            "SELECT * FROM DB2ADMIN.FILM",
            "SELECT * FROM DB2ADMIN.ZAUZIMA",
            "SELECT * FROM DB2ADMIN.PARAM WHERE NAME <> 'VERSION'"
    };

    private ApplicationConfiguration.DatabaseConfiguration databaseConfiguration;

    private String createRestartWithForTable(String tableName, String idName, Connection conn) throws SQLException {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(new Formatter().format("SELECT COALESCE(MAX(%1s)+1,1) FROM DB2ADMIN.%2s", idName, tableName).toString());
            rs = st.executeQuery();
            if (rs.next()) {
                return String.format("alter table %s alter COLUMN %s RESTART WITH %d %s\n\n",
                        tableName, idName, rs.getInt(1), STATEMENT_DELIMITER);
            } else
                throw new IllegalStateException("I could not fetch next ID for table " + tableName);
        } finally {
            close(st);
            close(rs);
        }
    }

    public void createRestorePoint() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        PrintStream pos2 = null;
        File renamedOldRestoreFile = null;
        try {
            conn = prepareDriverAndFetchConnection();
            createRestoreDir();
            createRestartCountersScript(conn);

            File restoreFile = new File("restore" + File.separatorChar + SCRIPT_KATALOG_RESTORE);
            log.debug("Renaming old restore script if there is one...");
            if (restoreFile.exists()) {
                renamedOldRestoreFile = renameAndCompressOldRestoreFiles(restoreFile);
            }

            printOutTableContentsToRestoreFile(conn, restoreFile);

            if (renamedOldRestoreFile != null) {
                eraseOldBackupIfIdenticalToCurrent(renamedOldRestoreFile, restoreFile);
            }
            log.info("Restore point creation process finished successfully!");
        } catch (Exception e) {
            log.error("Failure during restore creation ", e);
        } finally {
            close(rs);
            close(st);
            close(conn);
        }
    }

    private void printOutTableContentsToRestoreFile(Connection conn, File restoreFile) throws UnsupportedEncodingException, FileNotFoundException, SQLException {
        PrintStream outputStream = null;
        try {
            outputStream = new PrintStream(new FileOutputStream(restoreFile), true, "UTF-8");
            if (databaseConfiguration.getDatabaseType().equals(ApplicationConfiguration.DatabaseType.DB2)) {
                outputStream.print(RESTORE_SCRIPT_DB2_ONLY_HEADER);
            }
            outputStream.print(getScriptHeader());
            for(String sql : OUTPUT_SQLS) {
                printInsertStatementsForResultOfSql(conn, outputStream, sql);
            }
        } finally {
            if (outputStream != null) {
                close(outputStream);
            }
        }
    }

    private void printInsertStatementsForResultOfSql(Connection conn, PrintStream outputStream, String sql) throws SQLException {
        log.debug("Working on restore script "+sql);
        PreparedStatement st2 = conn.prepareStatement(sql);
        ResultSet rs2 = st2.executeQuery();
        ResultSetMetaData metaData = rs2.getMetaData();
        String tableName = metaData.getTableName(1);

        StringBuilder colNames = new StringBuilder();
        for (int colIter=1; colIter<=metaData.getColumnCount(); colIter++) {
            colNames.append(metaData.getColumnName(colIter));
            if (colIter != metaData.getColumnCount())
                colNames.append(",");
        }
        while (rs2.next()) {
            StringBuilder currentRowContents = new StringBuilder();
            for (int colIter=1; colIter<=metaData.getColumnCount(); colIter++) {
                int columnType = metaData.getColumnType(colIter);
                if (columnType == Types.INTEGER)
                    currentRowContents.append(rs2.getInt(colIter));
                else if (columnType == Types.VARCHAR)
                    currentRowContents.append(getSQLString(rs2.getString(colIter)));
                else if (columnType == Types.DECIMAL)
                    currentRowContents.append(rs2.getDouble(colIter));
                else
                    throw new IllegalArgumentException("SQL type not supported: "+ columnType + " for column "+metaData.getColumnName(colIter));
                if (colIter != metaData.getColumnCount())
                    currentRowContents.append(",");
            }
            outputStatement(outputStream, String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, colNames, currentRowContents));
        }

        close(st2);
        close(rs2);
    }

    private void eraseOldBackupIfIdenticalToCurrent(File renamedOldRestoreFile, File restoreFile) {
        if (returnMD5ForFile(renamedOldRestoreFile).equals(returnMD5ForFile(restoreFile))) {
            log.debug("Deleting current backup because it is identical to previous");
            File redundantZipFile = new File(renamedOldRestoreFile.getAbsolutePath() + ".zip");
            if (redundantZipFile.exists())
                if (!redundantZipFile.delete())
                    throw new IllegalStateException("Failure when deleting previous backup");
        }
        if (!renamedOldRestoreFile.delete())
            throw new IllegalStateException("Failure when deleting previous backup");
    }

    private File renameAndCompressOldRestoreFiles(File restoreFile) {
        File renamedOldRestoreFile;
        long lastMod = restoreFile.lastModified();
        Date lastModDate = Calendar.getInstance().getTime();
        lastModDate.setTime(lastMod);
        renamedOldRestoreFile = new File("restore" + File.separatorChar +
                String.format(SCRIPT_KATALOG_RESTORE_WITH_TIMESTAMP, createTimestampString(lastModDate)));
        if (!restoreFile.renameTo(renamedOldRestoreFile))
            throw new IllegalStateException("Old restore file could not be renamed " + restoreFile + "->" + renamedOldRestoreFile);

        compressPreviousRestoreFiles(renamedOldRestoreFile);
        return renamedOldRestoreFile;
    }

    private void compressPreviousRestoreFiles(File renamedOldRestoreFile) {
        ZipOutputStream zos = null;
        try {
            log.debug("Creating ZIP file " + renamedOldRestoreFile.getAbsolutePath() + ".zip");
            zos = new ZipOutputStream(new FileOutputStream(renamedOldRestoreFile.getAbsolutePath() + ".zip"));

            writeFileToZipStream(zos, SCRIPT_KATALOG_RESTART_COUNTERS);

            writeFileToZipStream(zos, renamedOldRestoreFile.getName());

        } catch (Throwable t) {
            log.error("Failure while zipping restore script", t);
        } finally {
            if (zos != null) try {
                zos.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private void writeFileToZipStream(ZipOutputStream zos, String fileName) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("restore\\"+fileName);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            copyStream(fis, zos);
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private void createRestartCountersScript(Connection conn) throws UnsupportedEncodingException, FileNotFoundException, SQLException {
        PrintStream createScriptStream;
        log.debug("Writing new CREATE script...");
        File createFile = new File("restore" + File.separatorChar + SCRIPT_KATALOG_RESTART_COUNTERS);
        createScriptStream = new PrintStream(new FileOutputStream(createFile), true, "UTF-8");

        if (databaseConfiguration.getDatabaseType().equals(ApplicationConfiguration.DatabaseType.DB2)) {
            createScriptStream.print(CREATE_SCRIPT_DB2_ONLY_HEADER);
        }

        createScriptStream.print(getScriptHeader());

        createScriptStream.print(createRestartWithForTable("Param", "IdParam", conn));
        createScriptStream.print(createRestartWithForTable("Film", "IdFilm", conn));
        createScriptStream.print(createRestartWithForTable("Medij", "IdMedij", conn));
        createScriptStream.print(createRestartWithForTable("Pozicija", "IdPozicija", conn));
        createScriptStream.print(createRestartWithForTable("TipMedija", "IdTip", conn));
        createScriptStream.print(createRestartWithForTable("Zanr", "IdZanr", conn));

        close(createScriptStream);
    }

    private String getScriptHeader() {
        return String.format(RESTORE_SCRIPT_HEADER, databaseConfiguration.getDBVersion());
    }

    private void createRestoreDir() {
        File restoreDir = new File("restore");
        if (!restoreDir.exists())
            if (!restoreDir.mkdir())
                throw new IllegalStateException("Could not create restore directory");
    }

    private boolean driverRegistered = false;
    private synchronized Connection prepareDriverAndFetchConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        if (!driverRegistered) {
            driverRegistered = true;
            log.debug("Registering database driver");
            Class.forName(databaseConfiguration.getDBDialect()).newInstance();
        }

        log.debug("Getting connection");
        return DriverManager.getConnection(databaseConfiguration.getDBUrl(),
                databaseConfiguration.getDBUsername(), databaseConfiguration.getDBPassword());
    }

    private String returnMD5ForFile(File input) {
        StringBuilder hash = new StringBuilder("");
        FileInputStream stream;
        MessageDigest digestAlg;
        try {
            digestAlg = MessageDigest.getInstance("MD5");
            stream = new FileInputStream(input);
            DigestInputStream digest = new DigestInputStream(stream, digestAlg);
            while (digest.read() != -1) {
                // keep reading
            }
            digest.close();

            byte[] calcDigest = digestAlg.digest();
            for (byte aCalcDigest : calcDigest)
                hash.append(String.format("%1$02X", aCalcDigest));

            log.debug("Calculated hash for file " + input.getAbsolutePath() + " is " + hash.toString());

        } catch (Exception e) {
            log.error("Failure while calculating MD5", e);
        }
        return hash.toString();
    }

    private String getSQLString(String input) {
        if (databaseConfiguration.getConvertSQLUnicodeCharacters()) {
            //log.debug("DB2 Unicode konvertor vratio: "+input+" -> "+tmp);
            return DB2CyrillicToUnicodeConvertor.obradiTekst('\'' + input + '\'');
        } else
            return '\'' + input.replaceAll("'", "''") + '\'';
    }

    private void outputStatement(PrintStream pos, String string) {
        pos.println(string + STATEMENT_DELIMITER);
        pos.println();
    }

    private void close(PrintStream pos) {
        if (pos != null) {
            pos.close();
        }
    }

    private String createTimestampString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
        return sdf.format(date);
    }

    protected void close(ResultSet rs) {
        if (rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Failure while closing ResultSet", e);
            }
    }

    protected void close(PreparedStatement ps) {
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
                log.error("Failure while closing PreparedStatement", e);
            }
    }

    protected void close(Connection conn) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Failure while closing DB Connection", e);
            }
    }

    public static void main(String[] args) {
        if (new File("log4j.xml").exists())
            DOMConfigurator.configure("log4j.xml");
        ApplicationConfiguration applicationConfiguration = ApplicationConfigurationManager.loadApplicationConfiguration();
        ApplicationManager.setApplicationConfiguration(applicationConfiguration);
        RestorePointService restorePointService = new RestorePointService();
        try {
            restorePointService.afterPropertiesSet();
            restorePointService.createRestorePoint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, bytesRead);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseConfiguration = ApplicationManager.getApplicationConfiguration().getDatabaseConfiguration();
    }

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

    private void safeRestoreDatabaseIfNeeded(Connection conn) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
        int activeMCSDBVersion = databaseConfiguration.getDBVersion();
        int versionFromDatabase = getDatabaseVersionFromDatabase(conn);
        if (versionFromDatabase == activeMCSDBVersion)
            return;

        if (versionFromDatabase > activeMCSDBVersion)
            throw new IllegalStateException(
                    String.format("Database is not supported (MCS is of versionFromDatabase %d, but your database is of versionFromDatabase %d)",
                            activeMCSDBVersion, versionFromDatabase));

        int versionFromRestorePoint = getDatabaseVersionFromRestorePoint();
        if (versionFromRestorePoint > activeMCSDBVersion) {
            throw new IllegalStateException(
                    String.format("Database restore point is not supported (MCS is of versionFromRestorePoint %d, but your database is of versionFromDatabase %d)",
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
        log.debug(String.format("SafeRunDatabaseRecreation: dbVersionFromDatabase=%d, dbVersionFromRestorePoint=%d", dbVersionFromDatabase, dbVersionFromRestorePoint));
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
                executeScriptOnConnection("restore//" + SCRIPT_KATALOG_RESTART_COUNTERS, conn);
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
                fis.close();
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

        PreparedStatement st = conn.prepareStatement(script.toString());
        st.execute();

        close(st);
        conn.commit();
    }
}