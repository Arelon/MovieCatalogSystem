package net.milanaleksic.mcs.infrastructure.export.impl;

import com.google.common.base.*;
import com.google.common.io.Files;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.export.*;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 4/23/12
 * Time: 2:04 PM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class HTMLExporterTest {

    @Test
    public void export_simple_data() {
        try {
            final File tempFile = File.createTempFile("export", ".htm");
            final Optional<Exporter> exporter = ExporterFactory.getInstance().getExporter(Files.getFileExtension(tempFile.getAbsolutePath()));
            Film test1 = new Film();
            test1.setNazivfilma("naziv 1");
            test1.setPrevodnazivafilma("prevod naziva 1");
            test1.setGodina(1981);
            test1.setZanr(new Zanr("test zanr"));
            test1.addMedij(new Medij(123, new TipMedija("CD"), new Pozicija("test pozicija")));
            final Film[] allFilms = {
                    test1
            };
            exporter.get().export(new ExporterSource() {

                @Override
                public String getTargetFile() {
                    return tempFile.getAbsolutePath();
                }

                @Override
                public int getItemCount() {
                    return allFilms.length;
                }

                @Override
                public int getColumnCount() {
                    return 5;
                }

                @Override
                public String getData(int row, int column) {
                    if (row == -1)
                        return "column " + column;
                    switch (column) {
                        case 0:
                            return allFilms[row].getMedijListAsString();
                        case 1:
                            return allFilms[row].getNazivfilma();
                        case 2:
                            return allFilms[row].getPrevodnazivafilma();
                        case 3:
                            return allFilms[row].getZanr().getZanr();
                        case 4:
                            return allFilms[row].getPozicija();
                        default:
                            return "";
                    }
                }

            });
            System.out.println(Files.toString(tempFile, Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
