package net.milanaleksic.mcs.export;

/**
 * @author Milan Aleksic
 * 09.03.2008.
 */
public class ExporterFactory {

	private static ExporterFactory instance ;
	
	private ExporterFactory() {
	}
	
	public static ExporterFactory getInstance() {
		if (instance==null)
			instance = new ExporterFactory();
		return instance;
	}
	
	public Exporter getExporter(String ext) {
		ext = ext.toLowerCase();
		if (ext.equals("htm") || ext.equals("html"))
			return new HTMLExporter();
		else
			return null;
	}
	
}
