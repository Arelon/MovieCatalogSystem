package net.milanaleksic.mcs.infrastructure.restore;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:12 PM
 */
public class TableRestoreSource implements RestoreSource {

    private final String script;

    public TableRestoreSource(String tableName) {
        script = String.format("SELECT * FROM %s", tableName);
    }

    public String getScript() {
        return script;
    }
}
