package net.milanaleksic.mcs.infrastructure.restore;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import net.milanaleksic.mcs.infrastructure.util.*;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipOutputStream;

public class RestorePointCreator extends AbstractRestorePointService {

    private static final String STATEMENT_DELIMITER = ";";

    private static final String SCRIPT_KATALOG_RESTORE_WITH_TIMESTAMP = "KATALOG_RESTORE_%s.sql"; //NON-NLS

    private static final String RESTORE_SCRIPT_HEADER =
            MCS_VERSION_TAG + "%d%n*/%n%nset schema DB2ADMIN;%n%n"; //NON-NLS

    private static final String FILENAME_FORMAT_DATE = "yyyyMMddkkmmss"; //NON-NLS

    @SuppressWarnings({"HardCodedStringLiteral"})
    private List<RestoreSource> restoreSources;

    public void setRestoreSources(List<RestoreSource> restoreSources) {
        this.restoreSources = restoreSources;
    }

    private String createTimestampString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(FILENAME_FORMAT_DATE);
        return sdf.format(date);
    }

    public void createRestorePoint() {
        try (Connection conn = getConnection()) {
            createRestoreDir();

            File restoreFile = new File(SCRIPT_KATALOG_RESTORE_LOCATION);
            if (log.isDebugEnabled())
                log.debug("Renaming old restore script if there is one..."); //NON-NLS

            File renamedOldRestoreFile = null;
            if (restoreFile.exists())
                renamedOldRestoreFile = renameAndCompressOldRestoreFiles(restoreFile);
            printOutTableContentsToRestoreFile(conn, restoreFile);
            if (renamedOldRestoreFile != null && renamedOldRestoreFile.exists())
                eraseOldBackupIfIdenticalToCurrent(renamedOldRestoreFile, restoreFile);
            if (log.isInfoEnabled())
                log.info("Restore point creation process finished successfully!"); //NON-NLS
        } catch (SQLException | IOException e) {
            log.error("Failure during restore creation ", e); //NON-NLS
        }
    }

    private void printOutTableContentsToRestoreFile(Connection conn, File restoreFile) throws IOException, SQLException {
        if (log.isDebugEnabled())
            log.debug("Creating new restore script"); //NON-NLS
        Optional<FileOutputStream> fos = Optional.absent();
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream outputStream = new PrintStream(buffer, true, Charsets.UTF_8.name());
            outputStream.print(getScriptHeader());

            appendRestartCountersScript(outputStream, conn);

            for (RestoreSource source : restoreSources) {
                printInsertStatementsForRestoreSource(conn, outputStream, source);
            }
            fos = Optional.of(new FileOutputStream(restoreFile));
            ByteStreams.copy(new ByteArrayInputStream(buffer.toByteArray()), fos.get());
        } finally {
            close(fos);
        }
    }

    private void printInsertStatementsForRestoreSource(Connection conn, PrintStream outputStream, RestoreSource source) throws SQLException {
        try (PreparedStatement preparedStatement = conn.prepareStatement(source.getScriptForData())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<Integer> columnTypeMappings = Lists.newArrayList();
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int colIter = 1; colIter <= metaData.getColumnCount(); colIter++) {
                    columnTypeMappings.add(metaData.getColumnType(colIter));
                }
                final String listOfResultSetColumns = getListOfResultSetColumns(metaData);
                final String tableName = metaData.getTableName(1);

                while (resultSet.next()) {
                    outputStatement(outputStream,
                            generateInsertSqlForResultSet(resultSet, columnTypeMappings, tableName, listOfResultSetColumns));
                }
            }
        } catch (SQLException e) {
            log.error("SQL exception occurred while working on restore script " + source.getScriptForData()); //NON-NLS
            throw e;
        }
    }

    private String getListOfResultSetColumns(ResultSetMetaData metaData) throws SQLException {
        StringBuilder colNames = new StringBuilder();
        for (int colIter = 1; colIter <= metaData.getColumnCount(); colIter++) {
            colNames.append(metaData.getColumnName(colIter));
            if (colIter != metaData.getColumnCount())
                colNames.append(",");
        }
        return colNames.toString();
    }

    private String generateInsertSqlForResultSet(ResultSet resultSet, List<Integer> columnTypeMappings, String tableName, String colNames) throws SQLException {
        StringBuilder currentRowContents = new StringBuilder();
        final int columnCount = columnTypeMappings.size();
        for (int colIter = 1; colIter <= columnCount; colIter++) {
            int columnType = columnTypeMappings.get(colIter-1);
            if (columnType == Types.INTEGER)
                currentRowContents.append(resultSet.getInt(colIter));
            else if (columnType == Types.VARCHAR || columnType == Types.CHAR)
                currentRowContents.append(getSQLString(Optional.fromNullable(resultSet.getString(colIter))));
            else if (columnType == Types.DECIMAL)
                currentRowContents.append(resultSet.getDouble(colIter));
            else
                throw new IllegalArgumentException("SQL type not supported: " + columnType + " in table " + tableName);
            if (colIter != columnCount)
                currentRowContents.append(",");
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, colNames, currentRowContents); //NON-NLS
    }

    private String getSQLString(Optional<String> value) {
        if (useDB2StyleStringInScripts) {
            return DBUtil.getSQLStringForDB2(value);
        } else
            return DBUtil.getSQLString(value);
    }

    private void eraseOldBackupIfIdenticalToCurrent(File renamedOldRestoreFile, File restoreFile) {
        if (StreamUtil.returnMD5ForFile(renamedOldRestoreFile).equals(StreamUtil.returnMD5ForFile(restoreFile))) {
            if (log.isDebugEnabled())
                log.debug("Deleting current backup because it is identical to previous"); //NON-NLS
            File redundantZipFile = new File(renamedOldRestoreFile.getAbsolutePath() + ".zip"); //NON-NLS
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
        renamedOldRestoreFile = new File("restore" + File.separatorChar + //NON-NLS
                String.format(SCRIPT_KATALOG_RESTORE_WITH_TIMESTAMP, createTimestampString(lastModDate)));
        if (!restoreFile.renameTo(renamedOldRestoreFile))
            throw new IllegalStateException("Old restore file could not be renamed " + restoreFile + "->" + renamedOldRestoreFile);

        compressPreviousRestoreFiles(renamedOldRestoreFile);
        return renamedOldRestoreFile;
    }

    private void compressPreviousRestoreFiles(File renamedOldRestoreFile) {
        Optional<ZipOutputStream> zos = Optional.absent();
        try {
            if (log.isDebugEnabled())
                log.debug("Creating ZIP file of previous restore: " + renamedOldRestoreFile.getAbsolutePath() + ".zip"); //NON-NLS
            zos = Optional.of(new ZipOutputStream(new FileOutputStream(renamedOldRestoreFile.getAbsolutePath() + ".zip"))); //NON-NLS

            StreamUtil.writeFileToZipStream(zos.get(), "restore\\" + renamedOldRestoreFile.getName(), SCRIPT_KATALOG_RESTORE); //NON-NLS

        } catch (Throwable t) {
            log.error("Failure while zipping restore script", t); //NON-NLS
        } finally {
            close(zos);
        }
    }

    private void appendRestartCountersScript(PrintStream outputStream, Connection conn) throws SQLException {
        for (RestoreSource restoreSource : restoreSources) {
            outputStream.print(createRestartWithForTable(restoreSource, conn));
        }
    }

    private String createRestartWithForTable(RestoreSource restoreSource, Connection conn) throws SQLException {
        if (!restoreSource.hasTableId())
            return "";
        try (PreparedStatement st = conn.prepareStatement(restoreSource.getScriptForMaxId())) {
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return restoreSource.getScriptForMaxIdTableAlter(rs.getInt(1));
                } else
                    throw new IllegalStateException("I could not fetch next ID for table " + restoreSource);
            }
        }
    }

    private String getScriptHeader() {
        return String.format(RESTORE_SCRIPT_HEADER, getDbVersion());
    }

    private void createRestoreDir() {
        File restoreDir = new File("restore"); //NON-NLS
        if (!restoreDir.exists())
            if (!restoreDir.mkdir())
                throw new IllegalStateException("Could not create restore directory");
    }

    private void outputStatement(PrintStream pos, String string) {
        pos.println(string + STATEMENT_DELIMITER);
        pos.println();
    }

}