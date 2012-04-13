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
public class ApplicationConfigurationManager implements LifecycleListener {

    private static final String CONFIGURATION_FILE = "application-configuration.xml"; //NON-NLS

    private static final Logger log = Logger.getLogger(ApplicationConfigurationManager.class);

    private static Optional<JAXBContext> jaxbContext = Optional.absent();

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        try {
            if (!jaxbContext.isPresent())
                throw new IllegalStateException("JAXB context not prepared for saving of app configuration");
            Marshaller m = jaxbContext.get().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(applicationConfiguration, new File(CONFIGURATION_FILE));
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t); //NON-NLS
        }
    }

    public ApplicationConfiguration loadApplicationConfiguration() {
        try {
            jaxbContext = Optional.of(JAXBContext.newInstance(ApplicationConfiguration.class));
            Unmarshaller u = jaxbContext.get().createUnmarshaller();
            ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) u.unmarshal(new File(CONFIGURATION_FILE));
            if (log.isInfoEnabled()) {
                log.info("ApplicationConfiguration read: " + applicationConfiguration); //NON-NLS
            }
            return applicationConfiguration;
        } catch (Throwable t) {
            log.error("ApplicationConfiguration could not have been read. Using default settings", t); //NON-NLS
        }
        return new ApplicationConfiguration();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Outputting default application configuration"); //NON-NLS
            JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationConfiguration.class);
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(new ApplicationConfiguration(), System.out);
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t); //NON-NLS
        }
    }
}
