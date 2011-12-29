package net.milanaleksic.mcs.application.config;

import org.apache.log4j.Logger;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:52 PM
 */
public class ApplicationConfigurationManager {

    private static final String CONFIGURATION_FILE = "application-configuration.xml";

    private static final Logger log = Logger.getLogger(ApplicationConfigurationManager.class);

    public ApplicationConfiguration loadApplicationConfiguration() {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        InputStream configurationFile = ApplicationConfigurationManager.class.getResourceAsStream("/"+CONFIGURATION_FILE);
        try {
            JAXBContext jc = JAXBContext.newInstance(ApplicationConfiguration.class);
            Unmarshaller u = jc.createUnmarshaller();
            applicationConfiguration = (ApplicationConfiguration) u.unmarshal(configurationFile);
            if (log.isInfoEnabled())
                log.info("ApplicationConfiguration read: "+ applicationConfiguration);
        } catch (Throwable t) {
            log.error("ApplicationConfiguration could not have been read. Using default settings", t);
        } finally {
            if (configurationFile != null) {
                try {
                    configurationFile.close();
                } catch (IOException ignored) {
                }
            }
        }
        return applicationConfiguration;
    }

    public static void main(String[] args) {
        try {
            System.out.println("Outputting default application configuration");
            JAXBContext jc = JAXBContext.newInstance(ApplicationConfiguration.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(new ApplicationConfiguration(), System.out);
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t);
        }
    }

}
