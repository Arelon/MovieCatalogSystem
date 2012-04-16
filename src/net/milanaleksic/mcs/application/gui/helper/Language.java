package net.milanaleksic.mcs.application.gui.helper;

public enum Language {

    ENGLISH {
        @Override public String getName() {
            return "en"; //NON-NLS
        }
    }, SERBIAN {
        @Override public String getName() {
            return "sr"; //NON-NLS
        }
    };

    public abstract String getName() ;

    public static int ordinalForName(String localeLanguage) {
        for (Language language : Language.values())
            if (language.getName().equals(localeLanguage))
                return language.ordinal();
        return ENGLISH.ordinal();
    }
}
