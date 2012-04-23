package net.milanaleksic.mcs.infrastructure.export;

import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.apache.log4j.Logger;
import org.dom4j.*;

import java.util.Calendar;

/**
 * User: Milan Aleksic
 * Date: 4/23/12
 * Time: 2:43 PM
 */
public abstract class XmlSourceExporter implements Exporter {

    protected final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public final void export(ExporterSource source) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("source"); //NON-NLS
        Element metaData = root.addElement("metaData"); //NON-NLS
        metaData.addElement("version").addText(VersionInformation.getVersion());
        metaData.addElement("date").addText(Calendar.getInstance().getTime().toString()); //NON-NLS
        final Element columns = metaData.addElement("columns"); //NON-NLS
        for (int j = 0; j < source.getColumnCount(); j++) {
            columns
                    .addElement("col") //NON-NLS
                    .addAttribute("index", String.valueOf(j)) //NON-NLS
                    .addText(source.getData(-1, j));
        }
        Element data = root.addElement("data"); //NON-NLS
        for (int i = 0; i < source.getItemCount(); i++) {
            Element row = data.addElement("row"); //NON-NLS
            for (int j = 0; j < source.getColumnCount(); j++) {
                row
                        .addElement("col") //NON-NLS
                        .addAttribute("index", String.valueOf(j)) //NON-NLS
                        .addText(source.getData(i, j));
            }
        }
        if (logger.isTraceEnabled())
            logger.trace("Document contents: " + document.asXML()); //NON-NLS
        exportDocument(document, source);
    }

    public abstract void exportDocument(Document document, ExporterSource source);

}
