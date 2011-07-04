package net.milanaleksic.mcs.restore;

import java.io.*;
import java.security.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.milanaleksic.mcs.util.MCSProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.intellij.lang.annotations.Language;

/**
 * Posebna klasa za kreaciju kompletnog SQL skripta za ocuvanje
 * informacija o trenutnom stanju baze podataka...
 *
 * @author Milan Aleksic
 *         06.04.2008.
 */
public class RestorePointCreator {

    private static Log log = LogFactory.getLog(RestorePointCreator.class);

    private static final String STATEMENT_DELIMITER = ";";

    @Language("SQL")
    private static final String CREATE_SCRIPT_DB2_ONLY_HEADER =
            "CREATE DATABASE KATALOG ON 'D:' USING CODESET UTF-8 TERRITORY RU COLLATE USING UCA400_NO;\r\n" +
                    "\r\n" +
                    "connect to KATALOG user db2admin using db2admin;\r\n" +
                    "\r\n";

    @Language("SQL")
    private static final String RESTORE_SCRIPT_DB2_ONLY_HEADER =
            "CONNECT TO KATALOG USER DB2ADMIN USING db2admin";

    @Language("SQL")
    private static final String RESTORE_SCRIPT_HEADER =
            "set schema DB2ADMIN;\r\n" +
                    "\r\n";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_1_LOGER =
            "create schema if not exists DB2ADMIN;\r\n" +
                    "\r\n" +
                    "set schema DB2ADMIN;\r\n" +
                    "\r\n" +
                    "drop table logerakcija;\r\n" +
                    "\r\n" +
                    "drop table loger;\r\n" +
                    "\r\n" +
                    "drop table zauzima;\r\n" +
                    "\r\n" +
                    "drop table film;\r\n" +
                    "\r\n" +
                    "drop table medij;\r\n" +
                    "\r\n" +
                    "drop table tipmedija;\r\n" +
                    "\r\n" +
                    "drop table zanr;\r\n" +
                    "\r\n" +
                    "drop table pozicija;\r\n" +
                    "\r\n" +
                    "CREATE TABLE Loger(\r\n" +
                    "    IdLoger INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_2_LOGER_AKCIJA =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    IdLogerAkcija int NOT NULL,\r\n" +
                    "    Kontekst varchar(100) NOT NULL,\r\n" +
                    "    Vreme TIMESTAMP NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE LogerAkcija (\r\n" +
                    "    IdLogerAkcija INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_3_FILM =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    Naziv varchar(100) NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE Film(\r\n" +
                    "    IdFilm INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_4_MEDIJ =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    NazivFilma varchar(100) NOT NULL,\r\n" +
                    "    PrevodNazivaFilma varchar(100) NOT NULL,\r\n" +
                    "    Godina int NOT NULL,\r\n" +
                    "    IdZanr int NOT NULL,\r\n" +
                    "    Komentar varchar(1000),\r\n" +
                    "    IMDBRejting numeric(3, 1) NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE Medij(\r\n" +
                    "    IdMedij INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_5_POZICIJA =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    Indeks int NOT NULL,\r\n" +
                    "    idTip int NOT NULL,\r\n" +
                    "    IdPozicija int NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE Pozicija(\r\n" +
                    "    IdPozicija INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";


    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_6_TIP_MEDIJA =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    Pozicija varchar(100) NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE TipMedija(\r\n" +
                    "    IdTip INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_PRE_7_ZANR =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    Naziv varchar(100) NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE Zanr(\r\n" +
                    "    IdZanr INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH ";

    @Language("SQL")
    private static final String CREATE_SCRIPT_ENDING =
            ", INCREMENT BY 1) PRIMARY KEY,\r\n" +
                    "    Zanr varchar(100) NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "CREATE TABLE Zauzima(\r\n" +
                    "    idMedij int NOT NULL,\r\n" +
                    "    idFilm int NOT NULL\r\n" +
                    ")\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Film  ADD  FOREIGN KEY(IdZanr)\r\n" +
                    "REFERENCES Zanr (IdZanr)\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Medij  ADD  FOREIGN KEY(IdPozicija)\r\n" +
                    "REFERENCES Pozicija (IdPozicija)\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Medij  ADD  FOREIGN KEY(idTip)\r\n" +
                    "REFERENCES TipMedija (IdTip)\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Zauzima  ADD  FOREIGN KEY(idFilm)\r\n" +
                    "REFERENCES Film (IdFilm)\r\n" +
                    "ON DELETE CASCADE\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Zauzima  ADD  FOREIGN KEY(idMedij)\r\n" +
                    "REFERENCES Medij (IdMedij)\r\n" +
                    "ON DELETE CASCADE\r\n" +
                    ";\r\n" +
                    "\r\n" +
                    "ALTER TABLE Loger  ADD  FOREIGN KEY(idLogerAkcija)\r\n" +
                    "REFERENCES LogerAkcija (IdLogerAkcija)\r\n" +
                    "ON DELETE CASCADE\r\n" +
                    ";";

    @Language("SQL")
    public static final String INDEX_CREATION = "DROP INDEX DB2ADMIN.IDX_FILM_NAZIVFILMA;\r\n" +
            "CREATE INDEX IDX_FILM_NAZIVFILMA on DB2ADMIN.FILM(\"NAZIVFILMA\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_FILM_PREVODNAZIVAFILMA;\r\n" +
            "CREATE INDEX IDX_FILM_PREVODNAZIVAFILMA on DB2ADMIN.FILM(\"PREVODNAZIVAFILMA\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_FILM_IDZANR;\r\n" +
            "CREATE INDEX IDX_FILM_IDZANR on DB2ADMIN.FILM(\"IDZANR\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_MEDIJ_INDEKS;\r\n" +
            "CREATE INDEX IDX_MEDIJ_INDEKS on DB2ADMIN.MEDIJ(\"INDEKS\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_MEDIJ_IDPOZICIJA;\r\n" +
            "CREATE INDEX IDX_MEDIJ_IDPOZICIJA on DB2ADMIN.MEDIJ(\"IDPOZICIJA\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_ZAUZIMA_IDMEDIJ;\r\n" +
            "CREATE INDEX IDX_ZAUZIMA_IDMEDIJ on DB2ADMIN.ZAUZIMA(\"IDMEDIJ\");\r\n\r\n" +
            "DROP INDEX DB2ADMIN.IDX_ZAUZIMA_IDFILM;\r\n" +
            "CREATE INDEX IDX_ZAUZIMA_IDFILM on DB2ADMIN.ZAUZIMA(\"IDFILM\");";

    private int getNextIdForTable(String tableName, String idName, Connection conn) throws SQLException {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(new Formatter().format("SELECT COALESCE(MAX(%1s)+1,1) FROM DB2ADMIN.%2s", idName, tableName).toString());
            rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else
                throw new IllegalStateException("I could not fetch next ID for table " + tableName);
        } finally {
            close(st);
            close(rs);
        }
    }

    private void appendInsertStatements(Connection conn, PrintStream output) throws SQLException {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            log.info("Obradjujem TIPMEDIJA (1/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.TIPMEDIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(output, "INSERT INTO TIPMEDIJA VALUES(" + rs.getInt("IDTIP") + ", " + getSQLString(rs.getString("NAZIV")) + ")");
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
            conn = prepareProcess();
            createRestoreDir();
            createCreateRestoreScript(conn);

            log.info("Preimenujem stari Restore skript ako postoji...");
            File restoreFile = new File("restore" + File.separatorChar + "KATALOG_RESTORE.sql");
            if (restoreFile.exists()) {
                renamedOldRestoreFile = renameAndCompressOldRestoreFiles(restoreFile);
            }

            log.info("Otvaram fajl za restore SQL skript...");
            pos2 = new PrintStream(new FileOutputStream(restoreFile), true, "UTF-8");

            if (MCSProperties.getDatabaseType().equals("DB2")) {
                pos2.print(RESTORE_SCRIPT_DB2_ONLY_HEADER);
            }

            pos2.print(RESTORE_SCRIPT_HEADER);

            log.info("Obradjujem TIPMEDIJA (1/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.TIPMEDIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO TIPMEDIJA VALUES(" + rs.getInt("IDTIP") + ", " + getSQLString(rs.getString("NAZIV")) + ")");

            close(st);
            close(rs);

            log.info("Obradjujem POZICIJA (2/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.POZICIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO POZICIJA VALUES(" + rs.getInt("IDPOZICIJA") + ", " + getSQLString(rs.getString("POZICIJA")) + ")");

            close(st);
            close(rs);

            log.info("Obradjujem ZANR (3/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.ZANR");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO ZANR VALUES(" + rs.getInt("IDZANR") + ", " + getSQLString(rs.getString("ZANR")) + ")");

            close(st);
            close(rs);

            log.info("Obradjujem MEDIJ (4/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.MEDIJ");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO MEDIJ VALUES(" + rs.getInt("IDMEDIJ") + ", " + rs.getInt("INDEKS") + ", " + rs.getInt("IDTIP") + ", " + rs.getInt("IDPOZICIJA") + ")");

            close(st);
            close(rs);

            log.info("Obradjujem FILM (5/8)...");
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

            log.info("Obradjujem ZAUZIMA (6/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.ZAUZIMA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO ZAUZIMA VALUES(" + rs.getInt("IDMEDIJ") + ", " + rs.getString("IDFILM") + ")");

            close(st);
            close(rs);

            log.info("Obradjujem LOGERAKCIJA (7/8)...");
            st = conn.prepareStatement("SELECT * FROM DB2ADMIN.LOGERAKCIJA");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO LOGERAKCIJA VALUES(" + rs.getInt("IDLOGERAKCIJA") + ", " + getSQLString(rs.getString("NAZIV")) + ")");

            close(st);
            close(rs);

            log.info("Obradjujem LOGER (8/8)...");
            st = conn.prepareStatement("SELECT IDLOGER, IDLOGERAKCIJA, KONTEKST, VREME VREME FROM DB2ADMIN.LOGER");
            rs = st.executeQuery();
            while (rs.next())
                outputStatement(pos2, "INSERT INTO LOGER VALUES("
                        + rs.getInt("IDLOGER") + ", "
                        + rs.getInt("IDLOGERAKCIJA") + ", "
                        + getSQLString(rs.getString("KONTEKST")) + ", "
                        + getSQLString(rs.getString("VREME"))
                        + ")");

            close(st);
            close(rs);

            close(pos2);

            if (renamedOldRestoreFile != null) {
                eraseOldBackupIfIdenticalToCurrent(renamedOldRestoreFile, restoreFile);
            }

            log.info("Proces kreacije backup-a uspesno zavrsen!");

        } catch (InstantiationException e) {
            log.error("Greska prilikom podizanja DB drajvera", e);
        } catch (IllegalAccessException e) {
            log.error("Greska prilikom podizanja DB drajvera", e);
        } catch (ClassNotFoundException e) {
            log.error("Greska prilikom podizanja DB drajvera", e);
        } catch (SQLException e) {
            log.error("SQL Greska prilikom kreacije backup-a", e);
        } catch (FileNotFoundException e) {
            log.error("Greska prilikom rada sa datotekama u toku kreacije backup-a", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Greska prilikom rada sa datotekama u toku kreacije backup-a", e);
        } finally {
            close(pos2);
            close(rs);
            close(st);
            close(conn);
        }
    }

    private void eraseOldBackupIfIdenticalToCurrent(File renamedOldRestoreFile, File restoreFile) {
        if (returnMD5ForFile(renamedOldRestoreFile).equals(returnMD5ForFile(restoreFile))) {
            log.info("Brisem trenutni bekap jer je identican prethodnom");
            File redundantZipFile = new File(renamedOldRestoreFile.getAbsolutePath() + ".zip");
            if (redundantZipFile.exists())
                redundantZipFile.delete();
        }
        renamedOldRestoreFile.delete();
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
        FileInputStream fis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            log.info("Kreiram ZIP fajl " + renamedOldRestoreFile.getAbsolutePath() + ".zip");
            fis = new FileInputStream(renamedOldRestoreFile);
            fos = new FileOutputStream(renamedOldRestoreFile.getAbsolutePath() + ".zip");

            zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry(renamedOldRestoreFile.getName()));

            copyStream(fis, zos);

            zos.closeEntry();

        } catch (Throwable t) {
            log.error("Greska prilikom zipovanja starog loga", t);
        } finally {
            if (zos != null) try {
                zos.close();
            } catch (Throwable ignored) {
            }
            if (fos != null) try {
                fos.close();
            } catch (Throwable ignored) {
            }
            if (fis != null) try {
                fis.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private void createCreateRestoreScript(Connection conn) throws UnsupportedEncodingException, FileNotFoundException, SQLException {
        PrintStream createScriptStream;
        log.info("Kreiram nov CREATE skript...");
        File createFile = new File("restore" + File.separatorChar + "KATALOG_CREATE.sql");
        createScriptStream = new PrintStream(new FileOutputStream(createFile), true, "UTF-8");

        if (MCSProperties.getDatabaseType().equals("DB2")) {
            createScriptStream.print(CREATE_SCRIPT_DB2_ONLY_HEADER);
        }

        createScriptStream.print(CREATE_SCRIPT_PRE_1_LOGER);
        createScriptStream.print(getNextIdForTable("Loger", "IdLoger", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_2_LOGER_AKCIJA);
        createScriptStream.print(getNextIdForTable("LogerAkcija", "IdLogerAkcija", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_3_FILM);
        createScriptStream.print(getNextIdForTable("Film", "IdFilm", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_4_MEDIJ);
        createScriptStream.print(getNextIdForTable("Medij", "IdMedij", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_5_POZICIJA);
        createScriptStream.print(getNextIdForTable("Pozicija", "IdPozicija", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_6_TIP_MEDIJA);
        createScriptStream.print(getNextIdForTable("TipMedija", "IdTip", conn));

        createScriptStream.print(CREATE_SCRIPT_PRE_7_ZANR);
        createScriptStream.print(getNextIdForTable("Zanr", "IdZanr", conn));

        createScriptStream.print(CREATE_SCRIPT_ENDING);

        createScriptStream.print(INDEX_CREATION);
        close(createScriptStream);
    }

    private void createRestoreDir() {
        File restoreDir = new File("restore");
        if (!restoreDir.exists())
            restoreDir.mkdir();
    }

    private Connection prepareProcess() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        log.info("Registrujem drajver...");
        Class.forName(MCSProperties.getDBDialect()).newInstance();

        log.info("Dohvatam konekciju...");
        return DriverManager.getConnection(MCSProperties.getDBUrl(), MCSProperties.getDBUsername(), MCSProperties.getDBPassword());
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

            log.debug("Proracunat hash za fajl " + input.getAbsolutePath() + " je " + hash.toString());

        } catch (NoSuchAlgorithmException e) {
            log.error("Greska prilikom proracuna MD5", e);
        } catch (FileNotFoundException e) {
            log.error("Greska prilikom proracuna MD5", e);
        } catch (IOException e) {
            log.error("Greska prilikom proracuna MD5", e);
        }
        return hash.toString();
    }

    private String getSQLString(String input) {
        if (MCSProperties.getConvertSQLUnicodeCharacters()) {
            //log.debug("DB2 Unicode konvertor vratio: "+input+" -> "+tmp);
            return DB2CyrillicToUnicodeConvertor.obradiTekst('\'' + input + '\'', false);
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
                log.error("Greska prilikom zatvaranja ResultSet ka bazi", e);
            }
    }

    protected void close(PreparedStatement ps) {
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
                log.error("Greska prilikom zatvaranja PreparedStatement ka bazi", e);
            }
    }

    protected void close(Connection conn) {
        if (conn != null)
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Greska prilikom zatvaranja Connection ka bazi", e);
            }
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("log4j.properties");
        new RestorePointCreator().createRestorePoint();
    }

    public void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }


}