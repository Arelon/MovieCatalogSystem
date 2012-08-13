package net.milanaleksic.mcs.infrastructure.restore;

import java.util.Formatter;

/**
 * User: Milan Aleksic
 * Date: 8/13/12
 * Time: 9:26 AM
 */
public class SelectAllFromTableRestoreSource implements RestoreSource {

    private static final String STATEMENT_DELIMITER = ";";

    private final String tableName;

    private String tableIdColumn;

    public SelectAllFromTableRestoreSource(String tableName) {
        this.tableName = tableName;
        this.tableIdColumn = null;
    }

    public SelectAllFromTableRestoreSource(String tableName, String tableIdColumn) {
        this.tableName = tableName;
        this.tableIdColumn = tableIdColumn;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getTableIdColumn() {
        return tableIdColumn;
    }

    @Override
    public String getScriptForData() {
        return String.format("SELECT * FROM %s order by 1", getTableName());  //NON-NLS
    }

    @Override
    public String getScriptForMaxId() {
        return new Formatter().format("SELECT COALESCE(MAX(%1s)+1,1) FROM %2s", //NON-NLS
                getTableIdColumn(), getTableName()).toString();
    }

    @Override
    public String getScriptForMaxIdTableAlter(int maxIndex) {
        return String.format("ALTER TABLE %s ALTER COLUMN %s RESTART WITH %d %s%n%n", //NON-NLS
                            getTableName(), getTableIdColumn(), maxIndex, STATEMENT_DELIMITER);
    }

    @Override
    public boolean hasTableId() {
        return getTableIdColumn() != null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", tableName=" + getTableName() + ", tableIdColumn=" + getTableIdColumn();
    }
}
