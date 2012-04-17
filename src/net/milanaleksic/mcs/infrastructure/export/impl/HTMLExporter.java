package net.milanaleksic.mcs.infrastructure.export.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.milanaleksic.mcs.infrastructure.export.*;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.util.Calendar;

public class HTMLExporter implements Exporter {
	
	private static final Logger logger = Logger.getLogger(HTMLExporter.class);

    //TODO: massively refactor this exporter to something more modern

	@Override
	public void export(ExporterSource source) {
		File ekspFajl = new File(source.getTargetFile());
        try (PrintWriter writer = new PrintWriter(ekspFajl, Charsets.UTF_8.name())) {
            writeHtmlHead(writer);

            writer.println("<body onload=\"javascript:init()\">");
            writer.println("<h3>Списак филмова у MCS v" + VersionInformation.getVersion() + " бази</h3>");
            writer.println("<small style=\"text-align:right\"><em>timestamp:</em> " + Calendar.getInstance().getTime().toString() + "</small>");

            writeTableHeader(source, writer);
            writeTableContents(source, writer);
            writeFooter(writer);

        } catch (IOException e) {
            logger.error("Unexpected exception occurred", e);
        }

        openInDefaultBrowser(ekspFajl);
    }

    private void writeFooter(PrintWriter writer) {
        writer.println("</table>");
        writer.println("<div id=\"selectionWrapper\" style=\"display:none\"><hr /><h3>Листа изабраних филмова</h3>");
        writer.println("<textarea cols=\"60\" rows=\"10\" id=\"selectionTarget\"></textarea>");
        writer.println("<input type=\"button\" value=\"Ресетуј листу\" onclick=\"javascript:resetujListu()\" /></div>");

        writer.println("</body>");
        writer.println("</html>");
    }

    private void writeTableContents(ExporterSource source, PrintWriter writer) {
        for (int i = 0; i< source.getItemCount(); i++) {
            String rest;
            if (i % 2 != 0)
                rest = " class=\"r1\"";
            else
                rest = " class=\"r2\"";
            rest += " id=\"f" + (i+1) + "\"";
            writer.print("<tr"+rest+">");
            for (int j = 0; j< source.getColumnCount()-1; j++) {
                writer.print("<td>" + source.getData(i, j) + "</td>");
            }
            writer.print("</tr>\r\n");
        }
    }

    private void writeTableHeader(ExporterSource source, PrintWriter writer) {
        writer.println("<table style=\"width: 100%\">");
        int[] widths = new int[source.getItemCount()-1];
        widths[0] = 80;
        widths[5] = 100;
        writer.println("<tr style=\"background-color: #CDD4FF; font-weight: bold\">");
        for (int i = 0; i< source.getColumnCount()-1; i++) {
            if (widths[i] != 0)
                writer.println("<td width=\"" + widths[i] + "\">" + source.getData(-1, i) + "</td>");
            else
                writer.println("<td>" + source.getData(-1, i) + "</td>");
        }
        writer.println("</tr>\r\n");
    }

    private void writeHtmlHead(PrintWriter writer) throws IOException {
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        writer.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\"><!--");
        writer.println("\r\n\tОва страница је аутоматски генерисана од стране MCS v"+
                VersionInformation.getVersion()+" (MovieCatalogSystem) софтвера");
        writer.println("\t(C) 2007-2008 by milan.aleksic@gmail.com\r\n");
        writer.println("--><head><title>Каталог филмова (креирано у MCS v"+
                VersionInformation.getVersion()+")</title>");
        writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
        writer.println("<meta name=\"author\" content=\"Milan Aleksić\" />");
        writer.println("<style>");
        writer.println(Files.toString(new File("export/stilovi.css"), Charsets.UTF_8));
        writer.println("</style><script language=\"javascript\"><!--");
        writer.println(Files.toString(new File("export/prog.js"), Charsets.UTF_8));
        writer.println("--></script></head>");
    }

    private void openInDefaultBrowser(File ekspFajl) {
        // otvaranje u podrazumevanom browser-u
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    desktop.open(ekspFajl);
                }
                catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

}
