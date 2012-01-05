package net.milanaleksic.mcs.application.gui.helper;

public enum Language {

    ENGLISH {
        @Override public String getName() {
            return "en";
        }
    }, SERBIAN {
        @Override public String getName() {
            return "sr";
        }
    };

    public abstract String getName() ;

    public static int ordinalForName(String localeLanguage) {
        if (SERBIAN.getName().equals(localeLanguage))
            return SERBIAN.ordinal();
        else
            return ENGLISH.ordinal();
    }
}
