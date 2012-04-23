package net.milanaleksic.mcs.infrastructure.export.impl;

import com.google.common.base.Function;
import net.milanaleksic.mcs.infrastructure.export.*;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.dom4j.Document;
import org.dom4j.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class HTMLExporter extends XmlSourceExporter {

    @Override
    public void exportDocument(final Document document, final ExporterSource exporterSource) {
        try {
            StreamUtil.useClasspathResource(this.getClass(), "HTMLExporter.xsl", new Function<InputStream, Void>() { //NON-NLS
                @Override
                public Void apply(InputStream inputStream) {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    try {
                        Transformer transformer = factory.newTransformer(new StreamSource(inputStream));

                        DocumentSource source = new DocumentSource(document);
                        DocumentResult result = new DocumentResult();
                        transformer.transform(source, result);

                        XMLWriter writer = new XMLWriter(new FileWriter(exporterSource.getTargetFile()), OutputFormat.createPrettyPrint());
                        writer.write(result.getDocument());
                        writer.close();
                    } catch (TransformerException | IOException e) {
                        logger.error("Eror while transforming exporting input", e); //NON-NLS
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            logger.error("Error while writing export output", e); //NON-NLS
        }
    }

}
