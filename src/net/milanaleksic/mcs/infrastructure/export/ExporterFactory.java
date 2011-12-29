package net.milanaleksic.mcs.infrastructure.export;

import java.util.Locale;

/**
 * @author Milan Aleksic
 * 09.03.2008.
 */
public class ExporterFactory {

	private final static ExporterFactory instance = new ExporterFactory() ;
	
	private ExporterFactory() {
	}
	
	public static ExporterFactory getInstance() {
		return instance;
	}
	
	public Exporter getExporter(String ext) {
		ext = ext.toLowerCase(Locale.ENGLISH);
		if (ext.equals("htm") || ext.equals("html"))
			return new HTMLExporter();
		else
			return null;
	}
	
}
