package net.milanaleksic.mcs.util;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 7/31/11
 * Time: 7:49 PM
 */
public class ProgramArgs {

    private boolean guiOnly;

    @Option(name="-g", usage="start GUI only (no application logic)")
    public void setGuiOnly(boolean guiOnly) {
        this.guiOnly = guiOnly;
    }

    public boolean isGuiOnly() {
        return guiOnly;
    }

}
