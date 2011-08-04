package net.milanaleksic.mcs.config;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.event.LifecycleListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.*;
import java.io.File;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:52 PM
 */
public class ConfigurationManager implements LifecycleListener {

    private static final String CONFIGURATION_FILE = "configuration.xml";

    private static final Logger log = Logger.getLogger(ConfigurationManager.class);

    @Autowired private ApplicationManager applicationManager;

    @Override
    public void applicationStarted() {
        UserConfiguration userConfiguration = new UserConfiguration();
        File configurationFile = new File(CONFIGURATION_FILE);
        if (configurationFile.exists()) {
            try {
                JAXBContext jc = JAXBContext.newInstance(UserConfiguration.class);
                Unmarshaller u = jc.createUnmarshaller();
                userConfiguration = (UserConfiguration) u.unmarshal(configurationFile);
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
