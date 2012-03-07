package net.milanaleksic.mcs.infrastructure.util;

import java.io.IOException;
import java.util.Properties;

/**
 * User: Milan Aleksic
 * Date: 3/7/12
 * Time: 9:18 AM
 */
public class VersionInformation {

    private static String version = null;

    public static synchronized String getVersion() {
        if (version == null) {
            Properties properties;
            try {
                properties = StreamUtil.fetchPropertiesFromClasspath("/net/milanaleksic/mcs/version.properties"); //NON-NLS
            } catch (IOException e) {
                throw new RuntimeException("Version properties files not found in resources");
            }
            version = properties.getProperty("src.version") + '.' + properties.getProperty("build.number");
        }
        return version;
    }

}
