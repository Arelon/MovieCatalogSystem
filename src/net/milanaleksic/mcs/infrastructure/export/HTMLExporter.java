package net.milanaleksic.mcs.infrastructure.export;

import java.awt.Desktop;
import java.io.*;
import java.util.Calendar;

import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.apache.log4j.Logger;

public class HTMLExporter implements Exporter {
	
	private static final Logger logger = Logger.getLogger(HTMLExporter.class);

	@Override
	public void export(ExporterSource source) {
		File ekspFajl = new File(source.getTargetFile());
        try (PrintWriter writer = new PrintWriter(ekspFajl, "UTF-8")) {
            writeHtmlHead(writer);

            writer.println("<body onload=\"javascript:init()\">");
            writer.println("<h3>Списак филмова у MCS v" + VersionInformation.getVersion() + " бази</h3>");
            writer.println("<small style=\"text-align:right\"><em>timestamp:</em> " + Calendar.getInstance().getTime().toString() + "</small>");

            writeTableHeader(source, writer);
            writeTableContents(source, writer);
            writeFooter(writer);

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
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

    private void writeHtmlHead(PrintWriter writer) {
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
        writer.println(dohvatiCss());
        writer.println("</style><script language=\"javascript\"><!--");
        writer.println(dohvatiJs());
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

    private String dohvatiCss() {
        StringBuilder rez = new StringBuilder();
		File css = new File("export/stilovi.css");
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(css), "UTF-8"));
			String tmp;
			while ((tmp = reader.readLine()) != null)
				rez.append(tmp).append('\r').append('\n');
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		} finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
        }
		return rez.toString().trim();
	}

	private String dohvatiJs() {
        StringBuilder rez = new StringBuilder();
		File js = new File("export/prog.js");
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(js), "UTF-8"));
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				tmp = tmp.replace('\t', ' ');
				tmp = tmp.replaceAll("( ( ))+", "");
				if (tmp.length()>0 && tmp.charAt(tmp.length()-1)==';')
					rez.append(tmp);
				else
					rez.append(tmp).append(' ');
			}
				
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		} finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
        }
		return rez.toString().trim();
	}


}
