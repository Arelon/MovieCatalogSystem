package net.milanaleksic.mcs.infrastructure.export;

import com.google.common.base.Optional;

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
	
	public Optional<Exporter> getExporter(String ext) {
		ext = ext.toLowerCase(Locale.ENGLISH);
		if (ext.equals("htm") || ext.equals("html"))
			return Optional.<Exporter>of(new HTMLExporter());
		else
			return Optional.absent();
	}
	
}
