package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.infrastructure.gui.transformer.Transformer;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import java.awt.*;
import java.net.URI;

public class AboutDialogForm extends AbstractTransformedDialogForm {

    private static final SelectionAdapter emailSender = new SelectionAdapter() {
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            new Thread(new Runnable() {

                public void run() {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.MAIL)) {
                            try {
                                desktop.mail(new URI("mailto:milan.aleksic@gmail.com")); //NON-NLS
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
                }

            }).start();
        }
    };

    private static final SelectionAdapter webSiteVisitor = new SelectionAdapter() {
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            new Thread(new Runnable() {

                public void run() {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(URI.create("http://www.milanaleksic.net")); //NON-NLS
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                    }
                }

            }).start();
        }
    };

    @Override
    protected void onTransformationComplete(Transformer transformer) {
        transformer.<Button>getMappedObject("btnEmail").get().addSelectionListener(emailSender);
        transformer.<Button>getMappedObject("btnSite").get().addSelectionListener(webSiteVisitor);
        transformer.<Text>getMappedObject("textArea").get().setText("Copyright 2007-2012 by Milan Aleksic\n\n" +
                bundle.getString("about.programVersion") + " " + VersionInformation.getVersion() + "\n\n" +
                bundle.getString("about.technologyDetails"));
    }

}
