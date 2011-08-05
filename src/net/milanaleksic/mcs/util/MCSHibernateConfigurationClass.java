package net.milanaleksic.mcs.util;

import java.util.Properties;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
import org.dom4j.Document;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

public class MCSHibernateConfigurationClass extends Configuration {

	private static final long serialVersionUID = 8084165076731454388L;
	
	@Override
	protected Configuration doConfigure(Document doc) throws HibernateException {
		Configuration config = super.doConfigure(doc);

        ApplicationConfiguration.DatabaseConfiguration databaseConfiguration = ApplicationManager.getApplicationConfiguration().getDatabaseConfiguration();

		Properties additionalProps = new Properties();
		additionalProps.put("hibernate.dialect", databaseConfiguration.getDBDialect());
		additionalProps.put("hibernate.connection.driver_class", databaseConfiguration.getDriverClass());
		additionalProps.put("hibernate.connection.url", databaseConfiguration.getDBUrl());
		additionalProps.put("hibernate.connection.username", databaseConfiguration.getDBUsername());
		additionalProps.put("hibernate.connection.password", databaseConfiguration.getDBPassword());
		config.addProperties(additionalProps);
		return config;
	}

}
