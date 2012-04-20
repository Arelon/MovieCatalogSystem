package net.milanaleksic.mcs.application.config;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.LifecycleListener;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import org.apache.log4j.Logger;

import javax.xml.bind.*;
import java.io.File;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:52 PM
 */
public class UserConfigurationManager implements LifecycleListener {

    private static final String CONFIGURATION_FILE = "configuration.xml"; //NON-NLS

    private static final Logger log = Logger.getLogger(UserConfigurationManager.class);

    private static Optional<JAXBContext> jaxbContext = Optional.absent();

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    public UserConfiguration loadUserConfiguration() {
        File configurationFile = new File(CONFIGURATION_FILE);
        try {
            jaxbContext = Optional.of(JAXBContext.newInstance(UserConfiguration.class));
            if (configurationFile.exists()) {
                Unmarshaller u = jaxbContext.get().createUnmarshaller();
                UserConfiguration ofTheJedi = (UserConfiguration) u.unmarshal(configurationFile);
                if (log.isInfoEnabled())
                    log.info("UserConfiguration read: " + ofTheJedi); //NON-NLS
                return ofTheJedi;
            }
        } catch (Throwable t) {
            log.error("UserConfiguration could not have been read. Using default settings", t); //NON-NLS
        }
        log.warn("User configuration file could not have been found! Using defaults..."); //NON-NLS
        return new UserConfiguration();
    }


    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        try {
            if (!jaxbContext.isPresent())
                throw new IllegalStateException("JAXB context not prepared for saving of app configuration");
            Marshaller m = jaxbContext.get().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(userConfiguration, new File(CONFIGURATION_FILE));
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t); //NON-NLS
        }
    }

}
