package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.infrastructure.util.VersionInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.*;

import java.awt.*;
import java.net.URI;

public class AboutDialogForm extends AbstractTransformedForm {

    @EmbeddedEventListener(component="linkEmail", event= SWT.Selection)
    private static final Listener emailSender = new Listener() {
        @Override
        public void handleEvent(Event event) {
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

    @EmbeddedEventListener(component="linkSite", event= SWT.Selection)
    private static final Listener webSiteVisitor  = new Listener() {
        @Override
        public void handleEvent(Event event) {
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

    @EmbeddedComponent
    private Text textArea = null;

    @Override
    protected void onTransformationComplete(TransformationContext transformer) {
        textArea.setText("Copyright 2007-2012 by Milan Aleksic\n\n" + //NON-NLS
                bundle.getString("about.programVersion") + " " + VersionInformation.getVersion() + "\n\n" +
                bundle.getString("about.technologyDetails"));
        textArea.setFocus();
    }

}
