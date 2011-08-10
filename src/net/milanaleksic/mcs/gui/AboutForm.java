package net.milanaleksic.mcs.gui;

import java.awt.Desktop;
import java.net.URI;

import net.milanaleksic.mcs.ApplicationManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class AboutForm {
	
	private static final String dodatniTekst = 
		"Верзија програма: " + ApplicationManager.getVersion() + "\n\n" +
		"У развоју су коришћене следеће бесплатне технологије:\n"+
		"Јава 6, Eclipse SWT 3.4.1, Spring 2.5, Hibernate 3.6, H2 1.3\n"+
		"Args4J, DOM4J, Log4J, EhCache, C3P0 итд.\n\n"+
		"Иконе су део \"Crystal Project\"-а, аутор је Евералдо Келхо.\n\n"+
		"Програм је још увек у развоју, све грешке молим пријавите аутору програма";

	private Shell sShell = null;
	private Shell parent = null;
	private Text textArea = null;

    public AboutForm(Shell parent) {
		this.parent = parent;
		createSShell();
		sShell.setLocation(
				new Point(
						parent.getLocation().x+Math.abs(parent.getSize().x-sShell.getSize().x) / 2, 
						parent.getLocation().y+Math.abs(parent.getSize().y-sShell.getSize().y) / 2 ));
		textArea.setText(textArea.getText() + "\n\n" + AboutForm.dodatniTekst);
		sShell.open();
	}
	
	private void createSShell() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.verticalSpacing = 10;
		gridLayout.makeColumnsEqualWidth = true;
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.heightHint = -1;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("О програму");
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(412, 326));
		createComposite2();
		createComposite();
		createComposite1();
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
		textArea = new Text(sShell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY | SWT.CENTER | SWT.BORDER);
		textArea.setText("Copyright(C)2007-2010 by Milan Aleksic");
		textArea.setLayoutData(gridData1);
	}

	private void createComposite() {
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
        Composite composite = new Composite(sShell, SWT.BORDER);
		composite.setLayout(gridLayout1);
		composite.setLayoutData(gridData3);
        Label labEmail = new Label(composite, SWT.NONE);
		labEmail.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
		labEmail.setText("milan.aleksic@gmail.com");
        Button btnEmail = new Button(composite, SWT.NONE);
		btnEmail.setText("пошаљи email");
		btnEmail.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
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
        });
	}

	private void createComposite1() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
        Composite composite1 = new Composite(sShell, SWT.BORDER);
		composite1.setLayout(gridLayout2);
		composite1.setLayoutData(gridData2);
        Label labSite = new Label(composite1, SWT.NONE);
		labSite.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
		labSite.setText("www.milanaleksic.net  ");
        Button btnSite = new Button(composite1, SWT.NONE);
		btnSite.setText("иди");
		btnSite.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
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
        });
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
        Composite composite2 = new Composite(sShell, SWT.NONE);
		composite2.setLayout(gridLayout3);
		composite2.setLayoutData(gridData5);
        Label label3 = new Label(composite2, SWT.CENTER);
		label3.setText("Movie Catalog System");
		label3.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
		label3.setLayoutData(gridData11);
		label3.setForeground(new Color(Display.getCurrent(), 0, 0, 255));
        Label label = new Label(composite2, SWT.NONE);
		label.setText("Аутор програма је Милан Алексић");
		label.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.BOLD));
		label.setLayoutData(gridData);
        Label label1 = new Label(composite2, SWT.NONE);
		label1.setText("дипл инг етх");
		label1.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.BOLD));
		label1.setLayoutData(gridData4);
        Label label2 = new Label(composite2, SWT.NONE);
		label2.setText("новембар 2007 - август 2011");
		label2.setLayoutData(gridData6);
	}

}
