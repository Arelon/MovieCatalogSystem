package net.milanaleksic.mcs.restore;

/**
 * User: Milan Aleksic
 * Date: 8/10/11
 * Time: 5:13 PM
 */
public class ExactSqlRestoreSource implements RestoreSource {

    private String script;

    public ExactSqlRestoreSource(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }
}
