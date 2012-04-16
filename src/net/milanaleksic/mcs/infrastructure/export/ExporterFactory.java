package net.milanaleksic.mcs.infrastructure.export;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.export.impl.HTMLExporter;

import java.util.Locale;

/**
 * @author Milan Aleksic
 * 09.03.2008.
 */
public class ExporterFactory {

	private final static ExporterFactory instance = new ExporterFactory() ;

    public static final String FORMAT_HTM = "htm"; //NON-NLS
    public static final String FORMAT_HTML = "html"; //NON-NLS

    private ExporterFactory() {
	}
	
	public static ExporterFactory getInstance() {
		return instance;
	}
	
	public Optional<Exporter> getExporter(String ext) {
		ext = ext.toLowerCase(Locale.ENGLISH);
		if (ext.equals(FORMAT_HTM) || ext.equals(FORMAT_HTML))
			return Optional.<Exporter>of(new HTMLExporter());
		else
			return Optional.absent();
	}
	
}
