package net.milanaleksic.mcs.application.config;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.LifecycleListener;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.xml.bind.*;
import java.io.File;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:52 PM
 */
public class UserConfigurationManager implements LifecycleListener {

    private static final String CONFIGURATION_FILE = "configuration.xml";

    private static final Logger log = Logger.getLogger(UserConfigurationManager.class);

    @Inject
    private ApplicationManager applicationManager;

    @Override
    public void applicationStarted() {
        UserConfiguration userConfiguration = new UserConfiguration();
        File configurationFile = new File(CONFIGURATION_FILE);
        if (configurationFile.exists()) {
            try {
                JAXBContext jc = JAXBContext.newInstance(UserConfiguration.class);
                Unmarshaller u = jc.createUnmarshaller();
                userConfiguration = (UserConfiguration) u.unmarshal(configurationFile);
                if (log.isInfoEnabled())
                    log.info("UserConfiguration read: "+ userConfiguration);
            } catch (Throwable t) {
                log.error("UserConfiguration could not have been read. Using default settings", t);
            }
        }
        applicationManager.setUserConfiguration(userConfiguration);
    }

    @Override
    public void applicationShutdown() {
        try {
            JAXBContext jc = JAXBContext.newInstance(UserConfiguration.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(applicationManager.getUserConfiguration(), new File(CONFIGURATION_FILE));
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t);
        }
    }
}
