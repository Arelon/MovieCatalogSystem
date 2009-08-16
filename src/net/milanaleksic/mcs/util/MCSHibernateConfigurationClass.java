package net.milanaleksic.mcs.util;

import java.util.Properties;

import org.dom4j.Document;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

public class MCSHibernateConfigurationClass extends Configuration {

	private static final long serialVersionUID = 8084165076731454388L;
	
	@Override
	protected Configuration doConfigure(Document doc) throws HibernateException {
		Configuration config = super.doConfigure(doc);
		Properties additionalProps = new Properties();
		additionalProps.put("hibernate.dialect", MCSProperties.getDBDialect());
		additionalProps.put("hibernate.connection.driver_class", MCSProperties.getDriverClass());
		additionalProps.put("hibernate.connection.url", MCSProperties.getDBUrl());
		additionalProps.put("hibernate.connection.username", MCSProperties.getDBUsername());
		additionalProps.put("hibernate.connection.password", MCSProperties.getDBPassword());
		config.addProperties(additionalProps);
		return config;
	}

}
