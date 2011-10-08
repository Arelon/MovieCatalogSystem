package net.milanaleksic.mcs.application.config;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:18 PM
 */
@XmlRootElement
public class ApplicationConfiguration {

    public static class DatabaseConfiguration {

        private int dbVersion = 4;

        private boolean databaseCreateRestore = true;

        public boolean isDatabaseCreateRestore() {
            return databaseCreateRestore;
        }

        public void setDatabaseCreateRestore(boolean databaseCreateRestore) {
            this.databaseCreateRestore = databaseCreateRestore;
        }

        @Override
        public String toString() {
            return "DatabaseConfiguration{" +
                    "databaseCreateRestore=" + databaseCreateRestore +
                    '}';
        }

        public int getDBVersion() {
            return dbVersion;
        }
    }

    public static class InterfaceConfiguration {

        private String tableFont = "Calibri";

        public String getTableFont() {
            return tableFont;
        }

        public void setTableFont(String tableFont) {
            this.tableFont = tableFont;
        }

        @Override
        public String toString() {
            return "InterfaceConfiguration{" +
                    "tableFont='" + tableFont + '\'' +
                    '}';
        }
    }

    private DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    private InterfaceConfiguration interfaceConfiguration = new InterfaceConfiguration();

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    public InterfaceConfiguration getInterfaceConfiguration() {
        return interfaceConfiguration;
    }

    public void setInterfaceConfiguration(InterfaceConfiguration interfaceConfiguration) {
        this.interfaceConfiguration = interfaceConfiguration;
    }

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "databaseConfiguration=" + databaseConfiguration +
                ", interfaceConfiguration=" + interfaceConfiguration +
                '}';
    }

}
