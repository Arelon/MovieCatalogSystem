package net.milanaleksic.mcs.infrastructure.util;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.Properties;

/**
 * User: Milan Aleksic
 * Date: 3/7/12
 * Time: 9:18 AM
 */
public class VersionInformation {

    private static Optional<String> version = Optional.absent();

    public static synchronized String getVersion() {
        if (!version.isPresent()) {
            Properties properties;
            try {
                properties = StreamUtil.fetchPropertiesFromClasspath("/net/milanaleksic/mcs/version.properties"); //NON-NLS
            } catch (IOException e) {
                throw new RuntimeException("Version properties files not found in resources");
            }
            version = Optional.of(properties.getProperty("src.version") + '.' + properties.getProperty("build.number"));
        }
        return version.get();
    }

}
