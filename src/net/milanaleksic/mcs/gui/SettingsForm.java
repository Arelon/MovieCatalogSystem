package net.milanaleksic.mcs.gui;

import net.milanaleksic.mcs.config.UserConfiguration;

import net.milanaleksic.mcs.domain.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.springframework.beans.factory.annotation.Autowired;

public class SettingsForm {

    @Autowired private PozicijaRepository pozicijaRepository;

    @Autowired private ZanrRepository zanrRepository;

	private Shell sShell = null;
	private Shell parent = null;
	private Runnable parentRunner = null;
    private TabFolder tabFolder = null;
	private Composite composite1 = null;
	private Composite composite2 = null;
    private Composite composite3 = null;
	private List listLokacije = null;
    private Text textNovaLokacija = null;
    private List listZanrovi = null;
    private Text textNovZanr = null;
    private Text textElementsPerPage = null;

    private boolean changed=false;
    private UserConfiguration userConfiguration;

    public void open(Shell parent, Runnable runnable, UserConfiguration userConfiguration) {
		this.parent = parent;
        this.userConfiguration = userConfiguration;
        this.parentRunner = runnable;
		createSShell();
		sShell.setLocation(new Point(parent.getLocation().x + Math.abs(parent.getSize().x - sShell.getSize().x) / 2,
                parent.getLocation().y + Math.abs(parent.getSize().y - sShell.getSize().y) / 2));
		reReadData();
		sShell.open();
	}

	private void reReadData() {
		// preuzimanje svih pozicija
        listLokacije.setItems(new String[] {});
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            listLokacije.add(pozicija.toString());
        }

        // preuzimanje svih zanrova
        listZanrovi.setItems(new String[] {});
        for (Zanr zanr : zanrRepository.getZanrs()) {
            listZanrovi.add(zanr.toString());
        }
        textElementsPerPage.setText(Integer.toString(userConfiguration.getElementsPerPage()));
	}

	private void createSShell() {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 1;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("Подешавања програма");
		createTabFolder();
		sShell.setLayout(gridLayout3);
		createComposite();
		sShell.setSize(new Point(500, 350));
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
	}

	private void createComposite() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = GridData.END;
		gridData12.grabExcessVerticalSpace = true;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 20;
        Composite composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
        Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("Затвори");
		btnCancel.setLayoutData(gridData12);
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (changed)
                    parentRunner.run();
                sShell.close();
            }
        });
	}

	private void createTabFolder() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		tabFolder = new TabFolder(sShell, SWT.NONE);
        tabFolder.setLayoutData(gridData1);
        createSettingsTabContents();
        createLocationTabContents();
        createGenresTabContents();
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText("Основна подешавања");
        tabItem2.setControl(composite3);
        tabItem.setText("Локације");
        tabItem.setControl(composite1);
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText("Жанрови");
        tabItem1.setControl(composite2);
	}

    private void createSettingsTabContents() {
        GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData1 = new GridData();
		gridData1.verticalSpan = 3;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite3 = new Composite(tabFolder, SWT.BORDER);
		composite3.setLayout(gridLayout);
		composite3.setLayoutData(gridData1);
        Label label = new Label(composite3, SWT.NONE);
        label.setText("Број елемената по страници\n (0 за приказ свих филмова одједном)");
		textElementsPerPage = new Text(composite3, SWT.BORDER);
		textElementsPerPage.setLayoutData(gridData);
		textElementsPerPage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                String data = textElementsPerPage.getText();
                if (data == null || data.length()==0) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.getElementsPerPage()));
                    return;
                }
                int elementsPerPage;
                try {
                    elementsPerPage = Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.getElementsPerPage()));
                    return;
                }
                if (elementsPerPage<0) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.getElementsPerPage()));
                    return;
                }
                userConfiguration.setElementsPerPage(elementsPerPage);
                changed = true;
            }
        });
    }

    private void createLocationTabContents() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.widthHint = 150;
		gridData2.grabExcessVerticalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		composite1 = new Composite(tabFolder, SWT.NONE);
		composite1.setLayout(gridLayout1);
        Label label = new Label(composite1, SWT.NONE);
		label.setText("Тренутне локације:");
		createAddLocationPanel();
		listLokacije = new List(composite1, SWT.BORDER | SWT.V_SCROLL);
		listLokacije.setLayoutData(gridData2);
        Button btnIzbrisiLokaciju = new Button(composite1, SWT.NONE);
		btnIzbrisiLokaciju.setText("Избриши");
		btnIzbrisiLokaciju.setLayoutData(gridData5);
        //TODO:NYI
//		btnIzbrisiLokaciju.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
//            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
//                hibernateTemplate.execute(new HibernateCallback() {
//
//                    public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException, java.sql.SQLException {
//                        // preuzimanje podataka za film koji se azurira
//                        Transaction transaction = session.beginTransaction();
//                        Query query = session.createQuery("from Pozicija p where p.pozicija = :param");
//                        query.setString("param", listLokacije.getItem(listLokacije.getSelectionIndex()));
//                        Pozicija pozicija = (Pozicija) query.list().get(0);
//
//                        if (pozicija.getMedijs().size() > 0) {
//                            MessageBox box = new MessageBox(sShell, SWT.ICON_ERROR);
//                            box.setMessage("Забрањено је брисање, постоји " + pozicija.getMedijs().size() + " медијума који су на тој локацији!");
//                            box.setText("Грешка");
//                            box.open();
//                            return null;
//                        }
//
//                        session.delete(pozicija);
//                        transaction.commit();
//                        changed = true;
//                        reReadData();
//                        return null;
//                    }
//
//                });
//            }
//        });
	}

	private void createGenresTabContents() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData7.widthHint = 150;
		gridData7.grabExcessVerticalSpace = true;
		gridData7.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		composite2 = new Composite(tabFolder, SWT.NONE);
		composite2.setLayout(gridLayout2);
        Label label1 = new Label(composite2, SWT.NONE);
		label1.setText("Тренутни жанрови:");
		createAddGenrePanel();
		listZanrovi = new List(composite2, SWT.BORDER | SWT.V_SCROLL);
		listZanrovi.setLayoutData(gridData7);
        Button btnIzbrisiZanr = new Button(composite2, SWT.NONE);
		btnIzbrisiZanr.setText("Избриши");
		btnIzbrisiZanr.setLayoutData(gridData8);
        //TODO:NYI
//		btnIzbrisiZanr.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
//            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
//                hibernateTemplate.execute(new HibernateCallback() {
//
//                    public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException, java.sql.SQLException {
//                        // preuzimanje podataka za film koji se azurira
//                        Transaction transaction = session.beginTransaction();
//                        Query query = session.createQuery("from Zanr z where z.zanr = :param");
//                        query.setString("param", listZanrovi.getItem(listZanrovi.getSelectionIndex()));
//                        Zanr zanr = (Zanr) query.list().get(0);
//
//                        if (zanr.getFilms().size() > 0) {
//                            MessageBox box = new MessageBox(sShell, SWT.ICON_ERROR);
//                            box.setMessage("Забрањено је брисање, постоји " + zanr.getFilms().size() + " филмова који припадају овом жанру!");
//                            box.setText("Грешка");
//                            box.open();
//                            return null;
//                        }
//
//                        session.delete(zanr);
//                        transaction.commit();
//                        changed = true;
//                        reReadData();
//                        return null;
//                    }
//
//                });
//            }
//        });
	}

	private void createAddLocationPanel() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData3 = new GridData();
		gridData3.verticalSpan = 3;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        Composite composite3 = new Composite(composite1, SWT.BORDER);
		composite3.setLayout(new GridLayout());
		composite3.setLayoutData(gridData3);
		textNovaLokacija = new Text(composite3, SWT.BORDER);
		textNovaLokacija.setLayoutData(gridData4);
        Button btnDodajLokaciju = new Button(composite3, SWT.NONE);
		btnDodajLokaciju.setText("Додај ову локацију");
		btnDodajLokaciju.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                pozicijaRepository.addPozicija(textNovaLokacija.getText());
                changed = true;
                reReadData();
            }
        });
	}

	private void createAddGenrePanel() {
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 1;
		GridData gridData6 = new GridData();
		gridData6.verticalSpan = 3;
		gridData6.grabExcessVerticalSpace = true;
		gridData6.grabExcessHorizontalSpace = true;
		gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        Composite composite4 = new Composite(composite2, SWT.BORDER);
		composite4.setLayoutData(gridData6);
		composite4.setLayout(gridLayout4);
		textNovZanr = new Text(composite4, SWT.BORDER);
		textNovZanr.setLayoutData(gridData10);
        Button btnDodajZanr = new Button(composite4, SWT.NONE);
		btnDodajZanr.setText("Додај овај жанр");
		btnDodajZanr.setLayoutData(gridData9);
		btnDodajZanr.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                zanrRepository.addZanr(textNovZanr.getText());
                changed = true;
                reReadData();
            }
        });
	}

}
