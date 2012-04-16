package net.milanaleksic.mcs.application.gui;

import java.awt.Desktop;
import java.net.URI;

import net.milanaleksic.mcs.infrastructure.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class AboutDialogForm extends AbstractDialogForm {

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
    protected void onShellCreated() {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        shell.setText(bundle.getString("global.aboutProgram"));
        shell.setLayout(gridLayout);
        shell.setSize(new Point(412, 326));
        createHeader();
        createHeaderEmailPanel();
        createHeaderWebSitePanel();
        Text textArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY | SWT.CENTER | SWT.BORDER);
        textArea.setText("Copyright 2007-2012 by Milan Aleksic"); //NON-NLS
        textArea.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                true, true));
        textArea.setText(textArea.getText() + "\n\n" +
                bundle.getString("about.programVersion") + " " + VersionInformation.getVersion() + "\n\n" +
                bundle.getString("about.technologyDetails"));
    }

    private void createHeaderEmailPanel() {
        Composite composite = new Composite(shell, SWT.BORDER);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, false));
        Label labEmail = new Label(composite, SWT.NONE);
        Color color = new Color(Display.getCurrent(), 0, 0, 0);
        labEmail.setForeground(color);
        color.dispose();
        labEmail.setText("milan.aleksic@gmail.com"); //NON-NLS
        Button btnEmail = new Button(composite, SWT.NONE);
        btnEmail.setText(bundle.getString("about.sendEmail"));
        btnEmail.addSelectionListener(emailSender);
    }

    private void createHeaderWebSitePanel() {
        Composite composite1 = new Composite(shell, SWT.BORDER);
        composite1.setLayout(new GridLayout(2, false));
        composite1.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, false));
        Label labSite = new Label(composite1, SWT.NONE);
        Color color = new Color(Display.getCurrent(), 0, 0, 0);
        labSite.setForeground(color);
        color.dispose();
        labSite.setText("www.milanaleksic.net  "); //NON-NLS
        Button btnSite = new Button(composite1, SWT.NONE);
        btnSite.setText(bundle.getString("about.visit"));
        btnSite.addSelectionListener(webSiteVisitor);
    }

    private void createHeader() {
        Composite footerPanel = new Composite(shell, SWT.NONE);
        footerPanel.setLayout(new GridLayout(1, false));
        footerPanel.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, false));
        Label labTitle = new Label(footerPanel, SWT.CENTER);
        labTitle.setText("Movie Catalog System"); //NON-NLS
        labTitle.setFont(new Font(Display.getDefault(), SWTUtil.getSystemFontData().getName(), 12, SWT.BOLD));
        labTitle.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, false));
        Color color = new Color(Display.getCurrent(), 0, 0, 255);
        labTitle.setForeground(color);
        color.dispose();
        Label labAuthor = new Label(footerPanel, SWT.NONE);
        labAuthor.setText(bundle.getString("about.programAuthor"));
        labAuthor.setFont(new Font(Display.getDefault(), SWTUtil.getSystemFontData().getName(), 10, SWT.BOLD));
        labAuthor.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, false));
    }

}
