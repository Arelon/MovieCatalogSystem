package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.config.UserConfiguration;
import net.milanaleksic.mcs.application.gui.helper.HandledModifyListener;
import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class SettingsForm {

    @Inject private PozicijaRepository pozicijaRepository;

    @Inject private ZanrRepository zanrRepository;

	private Shell sShell = null;
	private Shell parent = null;
	private Runnable parentRunner = null;
    private TabFolder tabFolder = null;
	private Composite compositeLocations = null;
	private Composite compositeGenres = null;
    private Composite compositeSettings = null;
	private List listLokacije = null;
    private Text textNovaLokacija = null;
    private List listZanrovi = null;
    private Text textNovZanr = null;
    private Text textElementsPerPage = null;
    private Text textProxyServer = null;
    private Text textProxyServerPort = null;
    private Text textProxyServerUsername = null;
    private Text textProxyServerPassword = null;

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
        tabItem.setText("Основна подешавања");
        tabItem.setControl(compositeSettings);
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText("Локације");
        tabItem1.setControl(compositeLocations);
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText("Жанрови");
        tabItem2.setControl(compositeGenres);
	}

    private void createSettingsTabContents() {
        compositeSettings = new Composite(tabFolder, SWT.BORDER);
		compositeSettings.setLayout(new GridLayout(1, false));
		compositeSettings.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, true));
        
        Group groupGlobal = new Group(compositeSettings, SWT.NONE);
        groupGlobal.setText("Глобална подешавања");
        groupGlobal.setLayout(new GridLayout(2, true));
        groupGlobal.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label label = new Label(groupGlobal, SWT.NONE);
        label.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        label.setText("Број елемената по страници\n (0 за приказ свих филмова одједном)");
		textElementsPerPage = new Text(groupGlobal, SWT.BORDER);
		textElementsPerPage.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
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

        ModifyListener proxySettingsModifyListener = new HandledModifyListener(sShell) {
            @Override
            public void handledModifyText() {
                UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.getProxyConfiguration();
                proxyConfiguration.setServer(textProxyServer.getText());
                if (!textProxyServerPort.getText().isEmpty())
                    proxyConfiguration.setPort(Integer.parseInt(textProxyServerPort.getText()));
                proxyConfiguration.setUsername(textProxyServerUsername.getText());
                proxyConfiguration.setPassword(textProxyServerPassword.getText());
            }
        };
        UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.getProxyConfiguration();
        Group groupProxyServer = new Group(compositeSettings, SWT.NONE);
        groupProxyServer.setText("Прокси сервер");
        groupProxyServer.setLayout(new GridLayout(2, false));
        groupProxyServer.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelServer = new Label(groupProxyServer, SWT.NONE);
        labelServer.setText("Адреса прокси сервера: ");
        labelServer.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        textProxyServer = new Text(groupProxyServer, SWT.BORDER);
        textProxyServer.setText(proxyConfiguration.getServer());
        textProxyServer.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textProxyServer.addModifyListener(proxySettingsModifyListener);
        Label labelPort = new Label(groupProxyServer, SWT.NONE);
        labelPort.setText("Порт прокси сервера: ");
        labelPort.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        textProxyServerPort = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerPort.setText(Integer.toString(proxyConfiguration.getPort()));
        textProxyServerPort.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textProxyServerPort.addModifyListener(proxySettingsModifyListener);
        Label labelUsername = new Label(groupProxyServer, SWT.NONE);
        labelUsername.setText("Корисничко име: ");
        labelUsername.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        textProxyServerUsername = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerUsername.setText(proxyConfiguration.getUsername());
        textProxyServerUsername.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textProxyServerUsername.addModifyListener(proxySettingsModifyListener);
        Label labelPassword = new Label(groupProxyServer, SWT.NONE);
        labelPassword.setText("Шифра: ");
        labelPassword.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        textProxyServerPassword = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerPassword.setText(proxyConfiguration.getPassword());
        textProxyServerPassword.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textProxyServerPassword.addModifyListener(proxySettingsModifyListener);
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
		compositeLocations = new Composite(tabFolder, SWT.NONE);
		compositeLocations.setLayout(gridLayout1);
        Label label = new Label(compositeLocations, SWT.NONE);
		label.setText("Тренутне локације:");
		createAddLocationPanel();
		listLokacije = new List(compositeLocations, SWT.BORDER | SWT.V_SCROLL);
		listLokacije.setLayoutData(gridData2);
        Button btnIzbrisiLokaciju = new Button(compositeLocations, SWT.NONE);
		btnIzbrisiLokaciju.setText("Избриши");
		btnIzbrisiLokaciju.setLayoutData(gridData5);
		btnIzbrisiLokaciju.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override
            public void handledSelected() throws ApplicationException {
                if (listLokacije.getSelectionIndex() < 0)
                    return;
                pozicijaRepository.deletePozicijaByName(listLokacije.getItem(listLokacije.getSelectionIndex()));
                changed = true;
                reReadData();
            }
        });
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
		compositeGenres = new Composite(tabFolder, SWT.NONE);
		compositeGenres.setLayout(gridLayout2);
        Label label1 = new Label(compositeGenres, SWT.NONE);
		label1.setText("Тренутни жанрови:");
		createAddGenrePanel();
		listZanrovi = new List(compositeGenres, SWT.BORDER | SWT.V_SCROLL);
		listZanrovi.setLayoutData(gridData7);
        Button btnIzbrisiZanr = new Button(compositeGenres, SWT.NONE);
		btnIzbrisiZanr.setText("Избриши");
		btnIzbrisiZanr.setLayoutData(gridData8);
		btnIzbrisiZanr.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override
            public void handledSelected() throws ApplicationException {
                if (listZanrovi.getSelectionIndex() < 0)
                    return;
                zanrRepository.deleteZanrByName(listZanrovi.getItem(listZanrovi.getSelectionIndex()));
                changed = true;
                reReadData();
            }
        });
	}

	private void createAddLocationPanel() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData3 = new GridData();
		gridData3.verticalSpan = 3;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        Composite composite3 = new Composite(compositeLocations, SWT.BORDER);
		composite3.setLayout(new GridLayout());
		composite3.setLayoutData(gridData3);
		textNovaLokacija = new Text(composite3, SWT.BORDER);
		textNovaLokacija.setLayoutData(gridData4);
        Button btnDodajLokaciju = new Button(composite3, SWT.NONE);
		btnDodajLokaciju.setText("Додај ову локацију");
		btnDodajLokaciju.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override
            public void handledSelected() throws ApplicationException {
                String newLokacija = textNovaLokacija.getText();
                if (newLokacija == null || newLokacija.isEmpty())
                    throw new ApplicationException("Empty string not allowed");
                pozicijaRepository.addPozicija(newLokacija);
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
        Composite composite4 = new Composite(compositeGenres, SWT.BORDER);
		composite4.setLayoutData(gridData6);
		composite4.setLayout(gridLayout4);
		textNovZanr = new Text(composite4, SWT.BORDER);
		textNovZanr.setLayoutData(gridData10);
        Button btnDodajZanr = new Button(composite4, SWT.NONE);
		btnDodajZanr.setText("Додај овај жанр");
		btnDodajZanr.setLayoutData(gridData9);
		btnDodajZanr.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override
            public void handledSelected() throws ApplicationException {
                String newZanr = textNovZanr.getText();
                if (newZanr == null || newZanr.isEmpty())
                    throw new ApplicationException("Empty string not allowed");
                zanrRepository.addZanr(newZanr);
                changed = true;
                reReadData();
            }
        });
	}

}
