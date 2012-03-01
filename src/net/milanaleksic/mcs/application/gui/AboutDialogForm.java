package net.milanaleksic.mcs.application.gui;

import java.awt.Desktop;
import java.net.URI;

import net.milanaleksic.mcs.application.ApplicationManager;
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
                                desktop.mail(new URI("mailto:milan.aleksic@gmail.com"));
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
                                desktop.browse(URI.create("http://www.milanaleksic.net"));
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
        createComposite2();
        createComposite();
        createComposite1();
        Text textArea = new Text(shell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY | SWT.CENTER | SWT.BORDER);
        textArea.setText("Copyright 2007-2012 by Milan Aleksic");
        textArea.setLayoutData(new GridData(org.eclipse.swt.layout.GridData.FILL, org.eclipse.swt.layout.GridData.FILL,
                true, true));
        textArea.setText(textArea.getText() + "\n\n" +
                bundle.getString("about.programVersion") + " " + ApplicationManager.getVersion() + "\n\n" +
                bundle.getString("about.technologyDetails"));
    }

    private void createComposite() {
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        Composite composite = new Composite(shell, SWT.BORDER);
        composite.setLayout(gridLayout1);
        composite.setLayoutData(gridData3);
        Label labEmail = new Label(composite, SWT.NONE);
        Color color = new Color(Display.getCurrent(), 0, 0, 0);
        labEmail.setForeground(color);
        color.dispose();
        labEmail.setText("milan.aleksic@gmail.com");
        Button btnEmail = new Button(composite, SWT.NONE);
        btnEmail.setText(bundle.getString("about.sendEmail"));
        btnEmail.addSelectionListener(emailSender);
    }

    private void createComposite1() {
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.numColumns = 2;
        Composite composite1 = new Composite(shell, SWT.BORDER);
        composite1.setLayout(gridLayout2);
        composite1.setLayoutData(gridData2);
        Label labSite = new Label(composite1, SWT.NONE);
        Color color = new Color(Display.getCurrent(), 0, 0, 0);
        labSite.setForeground(color);
        color.dispose();
        labSite.setText("www.milanaleksic.net  ");
        Button btnSite = new Button(composite1, SWT.NONE);
        btnSite.setText(bundle.getString("about.visit"));
        btnSite.addSelectionListener(webSiteVisitor);
    }

    private void createComposite2() {
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridData gridData6 = new GridData();
        gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridData gridData5 = new GridData();
        gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        gridData5.grabExcessHorizontalSpace = true;
        GridData gridData4 = new GridData();
        gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridLayout gridLayout3 = new GridLayout();
        gridLayout3.numColumns = 1;
        Composite composite2 = new Composite(shell, SWT.NONE);
        composite2.setLayout(gridLayout3);
        composite2.setLayoutData(gridData5);
        Label label3 = new Label(composite2, SWT.CENTER);
        label3.setText("Movie Catalog System");
        label3.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
        label3.setLayoutData(gridData11);
        Color color = new Color(Display.getCurrent(), 0, 0, 255);
        label3.setForeground(color);
        color.dispose();
        Label label = new Label(composite2, SWT.NONE);
        label.setText(bundle.getString("about.programAuthor"));
        label.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.BOLD));
        label.setLayoutData(gridData);
    }

}
