package net.milanaleksic.mcs.application.config;

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

    private JAXBContext jaxbContext = null;

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    public UserConfiguration loadUserConfiguration() {
        UserConfiguration ofTheJedi = null;
        File configurationFile = new File(CONFIGURATION_FILE);
        if (configurationFile.exists()) {
            try {
                jaxbContext = JAXBContext.newInstance(UserConfiguration.class);
                Unmarshaller u = jaxbContext.createUnmarshaller();
                ofTheJedi = (UserConfiguration) u.unmarshal(configurationFile);
                if (log.isInfoEnabled())
                    log.info("UserConfiguration read: " + ofTheJedi); //NON-NLS
            } catch (Throwable t) {
                log.error("UserConfiguration could not have been read. Using default settings", t); //NON-NLS
            }
        }
        return ofTheJedi;
    }


    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        try {
            if (jaxbContext == null)
                return;
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(userConfiguration, new File(CONFIGURATION_FILE));
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t); //NON-NLS
        }
    }

}
