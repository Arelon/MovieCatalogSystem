package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
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

/**
 * Posebna klasa za kreaciju kompletnog SQL skripta za ocuvanje
 * informacija o trenutnom stanju baze podataka...
 *
 * @author Milan Aleksic
 *         06.04.2008.
 */
public class RestorePointService implements InitializingBean {

    private static final Log log = LogFactory.getLog(RestorePointService.class);

    private static final String STATEMENT_DELIMITER = ";";

    private static final String SCRIPT_RESOURCE_RECREATE = "recreate_script.sql";
    private static final String SCRIPT_KATALOG_RESTART_COUNTERS = "KATALOG_RESTART_COUNTERS.sql";
    private static final String SCRIPT_KATALOG_RESTORE = "KATALOG_RESTORE.sql";

    private static final String CREATE_SCRIPT_DB2_ONLY_HEADER =
            "CREATE DATABASE KATALOG ON 'D:' USING CODESET UTF-8 TERRITORY RU COLLATE USING UCA400_NO;\r\n" +
                    "\r\n" +
                    "connect to KATALOG user db2admin using db2admin;\r\n" +
                    "\r\n";

    private static final String RESTORE_SCRIPT_HEADER =
            "set schema DB2ADMIN;\n\n";

    private static final String RESTORE_SCRIPT_DB2_ONLY_HEADER =
            "CONNECT TO KATALOG USER DB2ADMIN USING db2admin";

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

            log.info("Renaming old restore script if there is one...");
            File restoreFile = new File("restore" + File.separatorChar + SCRIPT_KATALOG_RESTORE);
            if (restoreFile.exists()) {
                renamedOldRestoreFile = renameAndCompressOldRestoreFiles(restoreFile);
            }

            pos2 = new PrintStream(new FileOutputStream(restoreFile), true, "UTF-8");

            if (databaseConfiguration.getDatabaseType().equals(ApplicationConfiguration.DatabaseType.DB2)) {
                pos2.print(RESTORE_SCRIPT_DB2_ONLY_HEADER);
            }

            pos2.print(RESTORE_SCRIPT_HEADER);

            log.info("Working on TIPMEDIJA (1/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.TIPMEDIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO TIPMEDIJA VALUES(" + rs.getInt("IDTIP") + ", " + getSQLString(rs.getString("NAZIV")) + ")");

            close(st);
            close(rs);

            log.info("Working on POZICIJA (2/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.POZICIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO POZICIJA VALUES(" + rs.getInt("IDPOZICIJA") + ", " + getSQLString(rs.getString("POZICIJA")) + ")");

            close(st);
            close(rs);

            log.info("Working on ZANR (3/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.ZANR");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO ZANR VALUES(" + rs.getInt("IDZANR") + ", " + getSQLString(rs.getString("ZANR")) + ")");

            close(st);
            close(rs);

            log.info("Working on MEDIJ (4/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.MEDIJ");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO MEDIJ VALUES(" + rs.getInt("IDMEDIJ") + ", " + rs.getInt("INDEKS") + ", " + rs.getInt("IDTIP") + ", " + rs.getInt("IDPOZICIJA") + ")");

            close(st);
            close(rs);

            log.info("Working on FILM (5/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.FILM");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO FILM VALUES("
                        + rs.getInt("IDFILM") + ", "
                        + getSQLString(rs.getString("NAZIVFILMA")) + ", "
                        + getSQLString(rs.getString("PREVODNAZIVAFILMA")) + ", "
                        + rs.getInt("GODINA") + ", "
                        + rs.getInt("IDZANR") + ", "
                        + getSQLString(rs.getString("KOMENTAR")) + ", "
                        + rs.getDouble("IMDBREJTING")
                        + ")");

            close(st);
            close(rs);

            log.info("Working on ZAUZIMA (6/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.ZAUZIMA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO ZAUZIMA VALUES(" + rs.getInt("IDMEDIJ") + ", " + rs.getString("IDFILM") + ")");

            close(st);
            close(rs);

            log.info("Working on LOGERAKCIJA (7/9)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.LOGERAKCIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO LOGERAKCIJA VALUES(" + rs.getInt("IDLOGERAKCIJA") + ", " + getSQLString(rs.getString("NAZIV")) + ")");

            close(st);
            close(rs);

            log.info("Working on LOGER (8/9)...");
            st = conn.prepareStatement("SELECT IDLOGER, IDLOGERAKCIJA, KONTEKST, VREME VREME FROM DB2ADMIN.LOGER");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO LOGER VALUES("
                        + rs.getInt("IDLOGER") + ", "
                        + rs.getInt("IDLOGERAKCIJA") + ", "
                        + getSQLString(rs.getString("KONTEKST")) + ", "
                        + getSQLString(rs.getString("VREME"))
                        + ")");

            log.info("Working on PARAM (9/9)...");
            st = conn.prepareStatement("SELECT IDPARAM, NAME, VALUE FROM DB2ADMIN.PARAM WHERE NAME <> 'VERSION'");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO PARAM VALUES("
                        + getSQLString(rs.getString("IDPARAM")) + ", "
                        + getSQLString(rs.getString("NAME")) + ", "
                        + getSQLString(rs.getString("VALUE"))
                        + ")");

            close(st);
            close(rs);

            close(pos2);

            if (renamedOldRestoreFile != null) {
                eraseOldBackupIfIdenticalToCurrent(renamedOldRestoreFile, restoreFile);
            }

            log.info("Restoring process finished successfully!");

        } catch (Exception e) {
            log.error("Failure during restore creation ", e);
        } finally {
            close(pos2);
            close(rs);
            close(st);
            close(conn);
        }
    }

    private void eraseOldBackupIfIdenticalToCurrent(File renamedOldRestoreFile, File restoreFile) {
        if (returnMD5ForFile(renamedOldRestoreFile).equals(returnMD5ForFile(restoreFile))) {
            log.info("Deleting current backup because it is identical to previous");
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
        renamedOldRestoreFile = new File("restore" + File.separatorChar + "KATALOG_RESTORE_" + createTimestampString(lastModDate) + ".sql");
        if (!restoreFile.renameTo(renamedOldRestoreFile))
            throw new IllegalStateException("Old restore file could not be renamed " + restoreFile + "->" + renamedOldRestoreFile);

        compressPreviousRestoreFiles(renamedOldRestoreFile);
        return renamedOldRestoreFile;
    }

    private void compressPreviousRestoreFiles(File renamedOldRestoreFile) {
        ZipOutputStream zos = null;
        try {
            log.info("Creating ZIP file " + renamedOldRestoreFile.getAbsolutePath() + ".zip");
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
        log.info("Writing new CREATE script...");
        File createFile = new File("restore" + File.separatorChar + SCRIPT_KATALOG_RESTART_COUNTERS);
        createScriptStream = new PrintStream(new FileOutputStream(createFile), true, "UTF-8");

        if (databaseConfiguration.getDatabaseType().equals(ApplicationConfiguration.DatabaseType.DB2)) {
            createScriptStream.print(CREATE_SCRIPT_DB2_ONLY_HEADER);
        }

        createScriptStream.print(RESTORE_SCRIPT_HEADER);

        createScriptStream.print(createRestartWithForTable("Param", "IdParam", conn));
        createScriptStream.print(createRestartWithForTable("Loger", "IdLoger", conn));
        createScriptStream.print(createRestartWithForTable("LogerAkcija", "IdLogerAkcija", conn));
        createScriptStream.print(createRestartWithForTable("Film", "IdFilm", conn));
        createScriptStream.print(createRestartWithForTable("Medij", "IdMedij", conn));
        createScriptStream.print(createRestartWithForTable("Pozicija", "IdPozicija", conn));
        createScriptStream.print(createRestartWithForTable("TipMedija", "IdTip", conn));
        createScriptStream.print(createRestartWithForTable("Zanr", "IdZanr", conn));

        close(createScriptStream);
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
            log.info("Registering database driver...");
            Class.forName(databaseConfiguration.getDBDialect()).newInstance();
        }

        log.info("Getting connection...");
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
        new RestorePointService().createRestorePoint();
    }

    public void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public boolean validateDatabase() {
        boolean valid = false;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            conn = prepareDriverAndFetchConnection();

            log.info("Validating database...");
            st = conn.prepareStatement("SELECT Value FROM DB2ADMIN.Param WHERE Name='VERSION'");
            rs = st.executeQuery();
            rs.next();

            String dbVersion = rs.getString(1);
            log.info("Expected DB version = "+databaseConfiguration.getDBVersion()+", DB version = "+dbVersion);

            valid = dbVersion.equals(Integer.toString(databaseConfiguration.getDBVersion()));

        } catch (Exception e) {
            log.error("Validation failed - " + e.getMessage());
        } finally {
            close(rs);
            close(st);
            close(conn);
        }
        return valid;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseConfiguration = ApplicationManager.getApplicationConfiguration().getDatabaseConfiguration();
    }

    public void restoreDatabase() {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = prepareDriverAndFetchConnection();

            log.info("Restoring DDL");
            executeScriptOnConnection(getClass().getResourceAsStream(SCRIPT_RESOURCE_RECREATE), conn);

            executeScriptOnConnection(conn, new File("restore//"+ SCRIPT_KATALOG_RESTART_COUNTERS));
            executeScriptOnConnection(conn, new File("restore//"+ SCRIPT_KATALOG_RESTORE));

            log.info("Restoration finished");
        } catch (Exception e) {
            log.error("Restoration failed", e);
        } finally {
            close(st);
            close(conn);
        }
    }

    private void executeScriptOnConnection(Connection conn, File restartCountersScript) throws IOException, SQLException {
        log.info("Restoring file: " + restartCountersScript.getName());
        if (restartCountersScript.exists()) {
            FileInputStream fis = new FileInputStream(restartCountersScript);
            try {
                executeScriptOnConnection(new FileInputStream(restartCountersScript), conn);
            } finally {
                fis.close();
            }
            conn.commit();
        }
    }

    private void executeScriptOnConnection(InputStream stream, Connection conn) throws IOException, SQLException {
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
        StringBuilder script = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            script.append(line);
        }

        PreparedStatement st = conn.prepareStatement(script.toString());
        st.execute();

        close(st);
        conn.commit();
    }
}