package net.milanaleksic.mcs.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.File;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:18 PM
 */
@XmlRootElement
public class ApplicationConfiguration {

    @XmlEnum
    public enum DatabaseType {
        @XmlEnumValue("derby") DERBY,
        @XmlEnumValue("derby-jar") DERBY_JAR,
        @XmlEnumValue("hsql") HSQL,
        @XmlEnumValue("h2") H2,
        @XmlEnumValue("db2") DB2
    }

    public static class DatabaseConfiguration {

        private DatabaseType databaseType = DatabaseType.H2;

        private boolean databaseCreateRestore = true;
        private int dbVersion = 1;

        public DatabaseType getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(DatabaseType databaseType) {
            this.databaseType = databaseType;
        }

        public boolean isDatabaseCreateRestore() {
            return databaseCreateRestore;
        }

        public void setDatabaseCreateRestore(boolean databaseCreateRestore) {
            this.databaseCreateRestore = databaseCreateRestore;
        }

        public String getDBDialect() {
            switch (databaseType) {
                case DERBY:
                case DERBY_JAR:
                    return "org.hibernate.dialect.DerbyDialect";
                case HSQL:
                    return "org.hibernate.dialect.HSQLDialect";
                case H2:
                    return "org.hibernate.dialect.H2Dialect";
                case DB2:
                    return "org.hibernate.dialect.DB2Dialect";
            }
            throw new IllegalStateException("Unknown database type");
        }

        public String getDriverClass() {
            switch (databaseType) {
                case DERBY:
                case DERBY_JAR:
                    return "org.apache.derby.jdbc.EmbeddedDriver";
                case HSQL:
                    return "org.hsqldb.jdbcDriver";
                case H2:
                    return "org.h2.Driver";
                case DB2:
                    return "com.ibm.db2.jcc.DB2Driver";
            }
            throw new IllegalStateException("Unknown database type");
        }

        public String getDBUrl() {
            switch (databaseType) {
                case DERBY:
                    return "jdbc:derby:../katalogDB";
                case DERBY_JAR:
                    return "jdbc:derby:jar:(katalogDB.jar)katalogDB";
                case HSQL:
                    return "jdbc:hsqldb:hsql://localhost";
                case H2:
                    return "jdbc:h2:db/katalog;TRACE_LEVEL_FILE=0";
                case DB2:
                    return "jdbc:db2://localhost:50000/KATALOG";
            }
            throw new IllegalStateException("Unknown database type");
        }

        public String getDBUsername() {
            switch (databaseType) {
                case DERBY:
                case DERBY_JAR:
                    return "DB2ADMIN";
                case HSQL:
                    return "db";
                case H2:
                    return "katalog";
                case DB2:
                    return "DB2ADMIN";
            }
            throw new IllegalStateException("Unknown database type");
        }

        public String getDBPassword() {
            switch (databaseType) {
                case DERBY:
                case DERBY_JAR:
                    return "db2admin";
                case HSQL:
                    return "";
                case H2:
                    return "katalog";
                case DB2:
                    return "db2admin";
            }
            throw new IllegalStateException("Unknown database type");
        }

        public boolean getConvertSQLUnicodeCharacters() {
            return databaseType.equals(DatabaseType.DB2);
        }

        @Override
        public String toString() {
            return "DatabaseConfiguration{" +
                    "databaseType=" + databaseType +
                    ", databaseCreateRestore=" + databaseCreateRestore +
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
