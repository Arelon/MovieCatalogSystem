package net.milanaleksic.mcs.restore;

import net.milanaleksic.mcs.util.StreamUtil;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipOutputStream;

public class RestorePointCreator extends AbstractRestorePointService {

    private static final String STATEMENT_DELIMITER = ";";

    private static final String SCRIPT_KATALOG_RESTORE_WITH_TIMESTAMP = "KATALOG_RESTORE_%s.sql";

    private static final String RESTORE_SCRIPT_HEADER =
            MCS_VERSION_TAG+"%d\n*/\n\nset schema DB2ADMIN;\n\n";

    private static final RestoreSource[] restoreSources = new RestoreSource[] {
            new TableRestoreSource("DB2ADMIN.TIPMEDIJA"),
            new TableRestoreSource("DB2ADMIN.POZICIJA"),
            new TableRestoreSource("DB2ADMIN.ZANR"),
            new TableRestoreSource("DB2ADMIN.MEDIJ"),
            new TableRestoreSource("DB2ADMIN.FILM"),
            new TableRestoreSource("DB2ADMIN.ZAUZIMA"),
            new ExactSqlRestoreSource("SELECT * FROM DB2ADMIN.PARAM WHERE NAME <> 'VERSION'")
    };

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

    private String createTimestampString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
        return sdf.format(date);
    }

    public void createRestorePoint() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        File renamedOldRestoreFile = null;
        try {
            conn = prepareDriverAndFetchConnection();
            createRestoreDir();

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

    private void printOutTableContentsToRestoreFile(Connection conn, File restoreFile) throws IOException, SQLException {
        FileOutputStream fos = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream outputStream = new PrintStream(buffer, true, "UTF-8");
            outputStream.print(getScriptHeader());

            appendRestartCountersScript(outputStream, conn);

            for(RestoreSource source : restoreSources) {
                printInsertStatementsForRestoreSource(conn, outputStream, source);
            }
            fos = new FileOutputStream(restoreFile);
            StreamUtil.copyStream(new ByteArrayInputStream(buffer.toByteArray()), fos);
        } finally {
            if (fos != null) {
                close(fos);
            }
        }
    }

    private void printInsertStatementsForRestoreSource(Connection conn, PrintStream outputStream, RestoreSource source) throws SQLException {
        log.debug("Working on restore script "+source.getScript());
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(source.getScript());
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            String tableName = metaData.getTableName(1);

            while (resultSet.next()) {
                outputStatement(outputStream, generateInsertSqlForResultSet(resultSet, metaData, tableName, getListOfResultSetColumns(metaData)));
            }
        } finally {
            close(preparedStatement);
            close(resultSet);
        }

    }

    private String getListOfResultSetColumns(ResultSetMetaData metaData) throws SQLException {
        StringBuilder colNames = new StringBuilder();
        for (int colIter=1; colIter<=metaData.getColumnCount(); colIter++) {
            colNames.append(metaData.getColumnName(colIter));
            if (colIter != metaData.getColumnCount())
                colNames.append(",");
        }
        return colNames.toString();
    }

    private String generateInsertSqlForResultSet(ResultSet resultSet, ResultSetMetaData metaData, String tableName, String colNames) throws SQLException {
        StringBuilder currentRowContents = new StringBuilder();
        for (int colIter=1; colIter<=metaData.getColumnCount(); colIter++) {
            int columnType = metaData.getColumnType(colIter);
            if (columnType == Types.INTEGER)
                currentRowContents.append(resultSet.getInt(colIter));
            else if (columnType == Types.VARCHAR)
                currentRowContents.append(getSQLString(resultSet.getString(colIter)));
            else if (columnType == Types.DECIMAL)
                currentRowContents.append(resultSet.getDouble(colIter));
            else
                throw new IllegalArgumentException("SQL type not supported: "+ columnType + " for column "+metaData.getColumnName(colIter));
            if (colIter != metaData.getColumnCount())
                currentRowContents.append(",");
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, colNames, currentRowContents);
    }

    private void eraseOldBackupIfIdenticalToCurrent(File renamedOldRestoreFile, File restoreFile) {
        if (StreamUtil.returnMD5ForFile(renamedOldRestoreFile).equals(StreamUtil.returnMD5ForFile(restoreFile))) {
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

            StreamUtil.writeFileToZipStream(zos, renamedOldRestoreFile.getName(), SCRIPT_KATALOG_RESTORE);

        } catch (Throwable t) {
            log.error("Failure while zipping restore script", t);
        } finally {
            if (zos != null) try {
                zos.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private void appendRestartCountersScript(PrintStream outputStream, Connection conn) throws UnsupportedEncodingException, FileNotFoundException, SQLException {
        log.debug("Writing new Restart Counters script fragment...");
        outputStream.print(createRestartWithForTable("Param", "IdParam", conn));
        outputStream.print(createRestartWithForTable("Film", "IdFilm", conn));
        outputStream.print(createRestartWithForTable("Medij", "IdMedij", conn));
        outputStream.print(createRestartWithForTable("Pozicija", "IdPozicija", conn));
        outputStream.print(createRestartWithForTable("TipMedija", "IdTip", conn));
        outputStream.print(createRestartWithForTable("Zanr", "IdZanr", conn));
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

    private String getSQLString(String input) {
        if (input == null)
            return "NULL";
        if (dbUrl.contains("db2")) {
            //log.debug("DB2 Unicode konvertor vratio: "+input+" -> "+tmp);
            return DB2CyrillicToUnicodeConvertor.obradiTekst('\'' + input + '\'');
        } else
            return '\'' + input.replaceAll("'", "''") + '\'';
    }

    private void outputStatement(PrintStream pos, String string) {
        pos.println(string + STATEMENT_DELIMITER);
        pos.println();
    }

    public static void main(String[] args) {
        if (new File("log4j.xml").exists())
            DOMConfigurator.configure("log4j.xml");
        RestorePointCreator restorePointCreator = new RestorePointCreator();
        try {
            restorePointCreator.afterPropertiesSet();
            restorePointCreator.createRestorePoint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        databaseConfiguration = applicationManager.getApplicationConfiguration().getDatabaseConfiguration();
    }


}