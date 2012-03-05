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

        private static final int dbVersion = 6;

        private boolean databaseCreateRestore = true;

        public boolean isDatabaseCreateRestore() {
            return databaseCreateRestore;
        }

        public void setDatabaseCreateRestore(boolean databaseCreateRestore) {
            this.databaseCreateRestore = databaseCreateRestore;
        }

        @Override
        @SuppressWarnings({"HardCodedStringLiteral"})
        public String toString() {
            return "DatabaseConfiguration{" +
                    "databaseCreateRestore=" + databaseCreateRestore +
                    '}';
        }

        public int getDbVersion() {
            return dbVersion;
        }
    }

    private DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    @Override
    @SuppressWarnings({"HardCodedStringLiteral"})
    public String toString() {
        return "ApplicationConfiguration{" +
                "databaseConfiguration=" + databaseConfiguration +
                '}';
    }

}
