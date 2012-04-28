package net.milanaleksic.mcs.infrastructure.config;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:18 PM
 */
@XmlRootElement
public class ApplicationConfiguration {

    public static class InterfaceConfiguration {

        @Nullable
        private Rectangle lastApplicationLocation;

        private boolean maximized = false;

        @Nullable
        public Rectangle getLastApplicationLocation() {
            return lastApplicationLocation;
        }

        public boolean isMaximized() {
            return maximized;
        }

        public void setLastApplicationLocation(@Nullable Rectangle bounds) {
            this.lastApplicationLocation = bounds;
        }

        public void setMaximized(boolean maximized) {
            this.maximized = maximized;
        }

        @Override
        public String toString() {
            return "InterfaceConfiguration{" +
                    "lastApplicationLocation=" + lastApplicationLocation +
                    ", maximized=" + maximized +
                    '}';
        }
    }

    public static class DatabaseConfiguration {

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
    }

    public static class CacheConfiguration {

        private String location = "./cache"; //NON-NLS

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        @SuppressWarnings({"HardCodedStringLiteral"})
        public String toString() {
            return "CacheConfiguration{" +
                    "location='" + location +
                    '}';
        }

    }

    private DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    private CacheConfiguration cacheConfiguration = new CacheConfiguration();

    private InterfaceConfiguration interfaceConfiguration = new InterfaceConfiguration();

    public CacheConfiguration getCacheConfiguration() {
        return cacheConfiguration;
    }

    public void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

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
                ", cacheConfiguration=" + cacheConfiguration +
                ", interfaceConfiguration=" + interfaceConfiguration +
                '}';
    }

}
