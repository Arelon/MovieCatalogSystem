package net.milanaleksic.mcs.infrastructure.restore;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:13 PM
 */
public class FreeDataScriptRestoreSource extends SelectAllFromTableRestoreSource {

    private final String scriptForData;

    public FreeDataScriptRestoreSource(String scriptForData, String tableName) {
        super(tableName);
        this.scriptForData = scriptForData;
    }

    public FreeDataScriptRestoreSource(String scriptForData, String tableName, String tableIdColumn) {
        super(tableName, tableIdColumn);
        this.scriptForData = scriptForData;
    }

    @Override
    public String getScriptForData() {
        return scriptForData;
    }
}
