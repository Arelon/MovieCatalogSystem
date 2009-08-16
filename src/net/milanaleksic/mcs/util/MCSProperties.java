package net.milanaleksic.mcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MCSProperties {

	private static Log log = LogFactory.getLog(MCSProperties.class);
	private static Properties props;
	private static boolean logMode = false;
	private static final String MCS_PROPERTIES_FILE = "mcs.properties";
	
	/*
	 * Sledi kod za testiranje rada podesavanja
	 * 
	 */
	public static void main(String[] args) {
		logMode = true;
		MCSProperties sett = new MCSProperties();
		log.warn("Ukljucen LOGGING klase MRSSettings");
		Class<?> clSett = sett.getClass();
		Method[] metodi = clSett.getMethods();
		// sortiranje
		Method tmp = null;
		for (int i = 0; i < metodi.length - 1; i++)
			for (int j = i + 1; j < metodi.length; j++)
				if (metodi[i].getName().compareToIgnoreCase(metodi[j].getName()) > 0) {
					tmp = metodi[i];
					metodi[i] = metodi[j];
					metodi[j] = tmp;
				}
		//obrada
		for (int i = 0; i < metodi.length; i++) {
			if ((metodi[i].getName().indexOf("get") == 0) && !metodi[i].getName().equals("getClass")) {
				if (metodi[i].getName().equals("wait") || metodi[i].getName().equals("equals") || metodi[i].getName().equals("main"))
					continue;
				Class<?>[] tipoviParametra = metodi[i].getParameterTypes();
				if (tipoviParametra.length > 0) {
					String opisParametara = "";
					for (int j = 0; j < tipoviParametra.length; j++) {
						opisParametara = opisParametara + tipoviParametra[j].getName() + (j < tipoviParametra.length-1 ? ", " : "");
					}
					// u slucaju da funkcija ima samo jedan int parametar obradjujemo
					// tako sto krecemo od 1 pa sve dok ne dobijemo gresku - inace odbijamo obradu
					if (opisParametara.equals("int")) {
						try {
							int j = 1;
							while (true) {
								StringBuffer useCase = new StringBuffer();
								useCase.append("[" + metodi[i].getName() + ", intParam=" + j + "] ");
								Object result = metodi[i].invoke(sett, new Object[] { new Integer(j) });
								useCase.append(lastUsedName);
								useCase.append("=");
								useCase.append(lastUsedValue);
								if (lastUsedValue == null || result == null || (!lastUsedValue.toString().equals(result.toString())))
									useCase.append(" (").append(result).append(")");
								
								if (lastUsedValue==null)
									break;
								
								log.info(useCase);
								
								j++;
							}
						} catch (Throwable t) {
							log.error("Greska prilikom obrade metode", t);
						}
					}
					else
						log.info(">>>>>>>>>>>>PAZNJA, NISAM USPEO DA OBRADIM METOD [" + metodi[i].getName() + "] jer ima naredne parametre: " + opisParametara);
				} else {
					try {
						StringBuffer useCase = new StringBuffer();
						useCase.append("[" + metodi[i].getName() + "] ");
						Object result = metodi[i].invoke(sett, (Object[]) null);
						useCase.append(lastUsedName);
						useCase.append("=");
						useCase.append(lastUsedValue);
						if (lastUsedValue == null || result == null || (!lastUsedValue.toString().equals(result.toString())))
							useCase.append(" (").append(result).append(")");
						log.info(useCase);
					} catch (Throwable t) {
						log.error("Greska prilikom obrade metode", t);
					}
				}
			}
		}
	}

	private static String lastUsedName, lastUsedValue;

	private static String get(String key, String fallback) {
		String value = get(key);
		if (value == null)
			return fallback;
		else
			return value;
	}

	private static String get(String key) {
		if (logMode) {
			lastUsedName = key;
			String s = props.getProperty(key);
			lastUsedValue = s != null ? s.trim() : s;
			return lastUsedValue;
		} else {
			String s = props.getProperty(key);
			return s != null ? s.trim() : s;
		}
	}

	private static boolean getBool(String key, boolean fallback) {
		String value = get(key);
		if (value == null)
			return fallback;
		else
			return "true".equalsIgnoreCase(get(key));
	}

	private static void loadSettings() {
		File file = new File(MCS_PROPERTIES_FILE);
		InputStream is=null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			log.error(e1);
			System.exit(0);
		}
		props = new Properties();
		if (is != null) {
			try {
				props.load(is);
				int propertiesSize = props.size();
				if (propertiesSize == 0)
					throw new IllegalStateException("Properties file empty, propertiesSize: " + propertiesSize + ", filePath: " + MCS_PROPERTIES_FILE);
					
			} catch (IOException e) {
				log.error(e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	// OSNOVNA PODESAVANJA MCS-a

	/**
	 * Database type used for keeping track of the movies
	 */
	public static String getDatabaseType() {
		if (props == null) {
			loadSettings();
		}
		return get("DATABASE_TYPE", "DERBY").toUpperCase();
	}
	
	public static boolean getDatabaseCreateRestore() {
		if (props == null) {
			loadSettings();
		}
		return getBool("DATABASE_CREATE_RESTORE", true);
	}
	
	public static String getTableFont() {
		if (props == null) {
			loadSettings();
		}
		return get("MCS_TABLE_FONT", "Calibri");
	}
	
	// IZVEDENA PODESAVANJA
	
	public static String getDBDialect() {
		if (getDatabaseType().equals("DERBY") || getDatabaseType().equals("DERBY-JAR"))
			return "org.hibernate.dialect.DerbyDialect";
		else if (getDatabaseType().equals("HSQL"))
			return "org.hibernate.dialect.HSQLDialect";
		else
			return "org.hibernate.dialect.DB2Dialect";
	}
	
	public static String getDriverClass() {
		if (getDatabaseType().equals("DERBY") || getDatabaseType().equals("DERBY-JAR"))
			return "org.apache.derby.jdbc.EmbeddedDriver";
		else if (getDatabaseType().equals("HSQL"))
			return "org.hsqldb.jdbcDriver";
		else
			return "com.ibm.db2.jcc.DB2Driver";
	}
	
	public static String getDBUrl() {
		if (getDatabaseType().equals("DERBY"))
			return "jdbc:derby:../katalogDB";
		else if (getDatabaseType().equals("DERBY-JAR"))
			return "jdbc:derby:jar:(katalogDB.jar)katalogDB";
		else if (getDatabaseType().equals("HSQL"))
			return "jdbc:hsqldb:hsql://localhost";
		else
			return "jdbc:db2://localhost:50000/KATALOG";
	}
	
	public static String getDBUsername() {
		if (getDatabaseType().equals("DERBY") || getDatabaseType().equals("DERBY-JAR"))
			return "DB2ADMIN";
		else if (getDatabaseType().equals("HSQL"))
			return "sa";
		else
			return "DB2ADMIN";
	}
	
	public static String getDBPassword() {
		if (getDatabaseType().equals("DERBY") || getDatabaseType().equals("DERBY-JAR"))
			return "db2admin";
		else if (getDatabaseType().equals("HSQL"))
			return "";
		else
			return "db2admin";
	}
	
		public static boolean getConvertSQLUnicodeCharacters() {
		return getDatabaseType().equals("DB2");
	}
	
}
