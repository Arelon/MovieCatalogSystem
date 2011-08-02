package net.milanaleksic.mcs.util;

import net.milanaleksic.mcs.config.Configuration;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * @author Milan 22 Sep 2007
 */
public class Kernel {
	
	private HibernateTemplate hibernateTemplate;
	private static final String version = "0.42";
    private ProgramArgs programArgs;
    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public static String getVersion() {
		return version;
	}

    public void setProgramArgs(ProgramArgs programArgs) {
        this.programArgs = programArgs;
    }

    public ProgramArgs getProgramArgs() {
        return programArgs;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}