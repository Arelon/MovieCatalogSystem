package net.milanaleksic.mcs.util;

import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * @author Milan 22 Sep 2007
 */
public class Kernel {
	
	private HibernateTemplate hibernateTemplate;
	
	private static String version = "0.41";
    private ProgramArgs programArgs;

    public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public static String getVersion() {
		return version;
	}

	public static void setVersion(String version) {
		Kernel.version = version;
	}

    public void setProgramArgs(ProgramArgs programArgs) {
        this.programArgs = programArgs;
    }

    public ProgramArgs getProgramArgs() {
        return programArgs;
    }
}