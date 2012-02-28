package net.milanaleksic.mcs.application.config;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.apache.log4j.Logger;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * User: Milan Aleksic
 * Date: 8/4/11
 * Time: 9:52 PM
 */
public class ApplicationConfigurationManager {

    private static final String CONFIGURATION_FILE = "/application-configuration.xml";

    private static final Logger log = Logger.getLogger(ApplicationConfigurationManager.class);

    private static class ApplicationConfigurationLoader implements Function<InputStream, ApplicationConfiguration> {
        @Override
        public ApplicationConfiguration apply(InputStream configurationFile) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationConfiguration.class);
                Unmarshaller u = jaxbContext.createUnmarshaller();
                StreamSource source = new StreamSource(configurationFile);
                ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) u.unmarshal(source);
                if (log.isInfoEnabled()) {
                    log.info("ApplicationConfiguration read: "+ applicationConfiguration);
                }
                return applicationConfiguration;
            } catch (Throwable t) {
                log.error("ApplicationConfiguration could not have been read. Using default settings", t);
            }
            return new ApplicationConfiguration();
        }
    }

    public ApplicationConfiguration loadApplicationConfiguration() {
        try {
            return StreamUtil.useClasspathResource(CONFIGURATION_FILE, new ApplicationConfigurationLoader());
        } catch (Throwable t) {
            log.error("ApplicationConfiguration could not have been read. Using default settings", t);
        }
        return new ApplicationConfiguration();
    }

    public static void main(String[] args) {
        try {
            System.out.println("Outputting default application configuration");
            JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationConfiguration.class);
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(new ApplicationConfiguration(), System.out);
        } catch (Throwable t) {
            log.error("Settings could not have been saved!", t);
        }
    }
}
