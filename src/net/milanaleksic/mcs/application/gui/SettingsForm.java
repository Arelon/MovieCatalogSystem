package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.UserConfiguration;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class SettingsForm {

    @Inject private PozicijaRepository pozicijaRepository;

    @Inject private ZanrRepository zanrRepository;

    @Inject private ApplicationManager applicationManager;

	private Shell sShell = null;
	private Runnable parentRunner = null;
	private Composite compositeLocations = null;
	private Composite compositeGenres = null;
    private Composite compositeSettings = null;
	private List listLokacije = null;
    private List listZanrovi = null;
    private Text textElementsPerPage = null;

    private boolean changed=false;
    private UserConfiguration userConfiguration = null;
    private ResourceBundle bundle = null;
    private Combo comboLanguage;

    public void open(Shell parent, Runnable runnable, UserConfiguration userConfiguration) {
        this.userConfiguration = userConfiguration;
        this.parentRunner = runnable;
        bundle = applicationManager.getMessagesBundle();
		createSShell(parent);
		sShell.setLocation(new Point(parent.getLocation().x + Math.abs(parent.getSize().x - sShell.getSize().x) / 2,
                parent.getLocation().y + Math.abs(parent.getSize().y - sShell.getSize().y) / 2));
		reReadData();
		sShell.open();
	}

	private void reReadData() {
        listLokacije.setItems(new String[] {});
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            listLokacije.add(pozicija.toString());
        }

        listZanrovi.setItems(new String[] {});
        for (Zanr zanr : zanrRepository.getZanrs()) {
            listZanrovi.add(zanr.toString());
        }
        textElementsPerPage.setText(Integer.toString(userConfiguration.getElementsPerPage()));
        comboLanguage.select(Language.ordinalForName(userConfiguration.getLocaleLanguage()));
	}

	private void createSShell(Shell parent) {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 1;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText(bundle.getString("settings.programSettings"));
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
		gridData.verticalAlignment = GridData.END;
		gridData.horizontalAlignment = GridData.CENTER;
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
		btnCancel.setText(bundle.getString("settings.close"));
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
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = GridData.FILL;
		TabFolder tabFolder = new TabFolder(sShell, SWT.NONE);
        tabFolder.setLayoutData(gridData1);
        createSettingsTabContents(tabFolder);
        createLocationTabContents(tabFolder);
        createGenresTabContents(tabFolder);
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(bundle.getString("settings.basicSettings"));
        tabItem.setControl(compositeSettings);
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(bundle.getString("settings.locations"));
        tabItem1.setControl(compositeLocations);
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(bundle.getString("settings.genres"));
        tabItem2.setControl(compositeGenres);
	}

    private void createSettingsTabContents(TabFolder tabFolder) {
        compositeSettings = new Composite(tabFolder, SWT.NONE);
		compositeSettings.setLayout(new GridLayout(1, false));
		compositeSettings.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, true));
        
        Group groupGlobal = new Group(compositeSettings, SWT.NONE);
        groupGlobal.setText(bundle.getString("settings.globalSettings"));
        groupGlobal.setLayout(new GridLayout(2, true));
        groupGlobal.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

        // language
        Label labelLang = new Label(groupGlobal, SWT.NONE);
        labelLang.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
        labelLang.setText(bundle.getString("settings.language"));
        comboLanguage = new Combo(groupGlobal, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboLanguage.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        for (Language language : Language.values())
            comboLanguage.add(bundle.getString("language.name."+language.getName()));
		comboLanguage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                int index = comboLanguage.getSelectionIndex();
                if (index > -1 && index < Language.values().length) {
                    userConfiguration.setLocaleLanguage(Language.values()[index].getName());
                    changed = true;
                }
            }
        });

        //elementsPerPage
        Label label = new Label(groupGlobal, SWT.NONE);
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
        label.setText(bundle.getString("settings.numberOfElementsPerPage"));
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

        UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.getProxyConfiguration();
        Group groupProxyServer = new Group(compositeSettings, SWT.NONE);
        groupProxyServer.setText(bundle.getString("settings.proxyServer"));
        groupProxyServer.setLayout(new GridLayout(2, false));
        groupProxyServer.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelServer = new Label(groupProxyServer, SWT.NONE);
        labelServer.setText(bundle.getString("settings.proxyServerAddress"));
        labelServer.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServer = new Text(groupProxyServer, SWT.BORDER);
        textProxyServer.setText(StringUtil.emptyIfNull(proxyConfiguration.getUsername()));
        textProxyServer.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelPort = new Label(groupProxyServer, SWT.NONE);
        labelPort.setText(bundle.getString("settings.proxyServerPort"));
        labelPort.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServerPort = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerPort.setText(StringUtil.emptyIfNullOtherwiseConvert(proxyConfiguration.getPort()));
        textProxyServerPort.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelUsername = new Label(groupProxyServer, SWT.NONE);
        labelUsername.setText(bundle.getString("settings.username"));
        labelUsername.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServerUsername = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerUsername.setText(StringUtil.emptyIfNull(proxyConfiguration.getUsername()));
        textProxyServerUsername.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelPassword = new Label(groupProxyServer, SWT.NONE);
        labelPassword.setText(bundle.getString("settings.password"));
        labelPassword.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServerPassword = new Text(groupProxyServer, SWT.BORDER);
        textProxyServerPassword.setText(StringUtil.emptyIfNull(proxyConfiguration.getPassword()));
        textProxyServerPassword.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        ModifyListener proxySettingsModifyListener = new HandledModifyListener(sShell, bundle) {
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
        textProxyServer.addModifyListener(proxySettingsModifyListener);
        textProxyServerPort.addModifyListener(proxySettingsModifyListener);
        textProxyServerUsername.addModifyListener(proxySettingsModifyListener);
        textProxyServerPassword.addModifyListener(proxySettingsModifyListener);
    }

    private void createLocationTabContents(TabFolder tabFolder) {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.FILL;
		gridData2.widthHint = 150;
		gridData2.grabExcessVerticalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		compositeLocations = new Composite(tabFolder, SWT.NONE);
		compositeLocations.setLayout(gridLayout1);
        Label label = new Label(compositeLocations, SWT.NONE);
		label.setText(bundle.getString("settings.availableLocations"));
		createAddLocationPanel();
		listLokacije = new List(compositeLocations, SWT.BORDER | SWT.V_SCROLL);
		listLokacije.setLayoutData(gridData2);
        Button btnIzbrisiLokaciju = new Button(compositeLocations, SWT.NONE);
		btnIzbrisiLokaciju.setText(bundle.getString("settings.delete"));
		btnIzbrisiLokaciju.setLayoutData(gridData5);
		btnIzbrisiLokaciju.addSelectionListener(new HandledSelectionAdapter(sShell, bundle) {
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

	private void createGenresTabContents(TabFolder tabFolder) {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = GridData.FILL;
		gridData7.widthHint = 150;
		gridData7.grabExcessVerticalSpace = true;
		gridData7.verticalAlignment = GridData.FILL;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		compositeGenres = new Composite(tabFolder, SWT.NONE);
		compositeGenres.setLayout(gridLayout2);
        Label label1 = new Label(compositeGenres, SWT.NONE);
		label1.setText(bundle.getString("settings.availableGenres"));
		createAddGenrePanel();
		listZanrovi = new List(compositeGenres, SWT.BORDER | SWT.V_SCROLL);
		listZanrovi.setLayoutData(gridData7);
        Button btnIzbrisiZanr = new Button(compositeGenres, SWT.NONE);
		btnIzbrisiZanr.setText(bundle.getString("settings.delete"));
		btnIzbrisiZanr.setLayoutData(gridData8);
		btnIzbrisiZanr.addSelectionListener(new HandledSelectionAdapter(sShell, bundle) {
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
		gridData4.horizontalAlignment = GridData.FILL;
		GridData gridData3 = new GridData();
		gridData3.verticalSpan = 3;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.horizontalAlignment = GridData.BEGINNING;
        Composite composite3 = new Composite(compositeLocations, SWT.NONE);
		composite3.setLayout(new GridLayout());
		composite3.setLayoutData(gridData3);
		final Text textNovaLokacija = new Text(composite3, SWT.BORDER);
		textNovaLokacija.setLayoutData(gridData4);
        final Button btnThisIsDefaultLocation = new Button(composite3, SWT.CHECK);
        btnThisIsDefaultLocation.setText(bundle.getString("settings.thisIsDefaultLocation"));
        Button btnDodajLokaciju = new Button(composite3, SWT.NONE);
		btnDodajLokaciju.setText(bundle.getString("settings.addThisLocation"));
		btnDodajLokaciju.addSelectionListener(new HandledSelectionAdapter(sShell, bundle) {
            @Override
            public void handledSelected() throws ApplicationException {
                String newLokacija = textNovaLokacija.getText();
                if (newLokacija == null || newLokacija.isEmpty())
                    throw new ApplicationException("Empty string not allowed");
                Pozicija pozicija = new Pozicija();
                pozicija.setPozicija(newLokacija);
                pozicija.setDefault(btnThisIsDefaultLocation.getSelection());
                pozicijaRepository.addPozicija(pozicija);
                changed = true;
                reReadData();
                btnThisIsDefaultLocation.setSelection(false);
                textNovaLokacija.setText("");
            }
        });
	}

	private void createAddGenrePanel() {
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = GridData.FILL;
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = GridData.CENTER;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 1;
		GridData gridData6 = new GridData();
		gridData6.verticalSpan = 3;
		gridData6.grabExcessVerticalSpace = true;
		gridData6.grabExcessHorizontalSpace = true;
		gridData6.horizontalAlignment = GridData.BEGINNING;
        Composite composite4 = new Composite(compositeGenres, SWT.NONE);
		composite4.setLayoutData(gridData6);
		composite4.setLayout(gridLayout4);
		final Text textNovZanr = new Text(composite4, SWT.BORDER);
		textNovZanr.setLayoutData(gridData10);
        Button btnDodajZanr = new Button(composite4, SWT.NONE);
		btnDodajZanr.setText(bundle.getString("settings.addThisGenre"));
		btnDodajZanr.setLayoutData(gridData9);
		btnDodajZanr.addSelectionListener(new HandledSelectionAdapter(sShell, bundle) {
            @Override
            public void handledSelected() throws ApplicationException {
                String newZanr = textNovZanr.getText();
                if (newZanr == null || newZanr.isEmpty())
                    throw new ApplicationException("Empty string not allowed");
                zanrRepository.addZanr(newZanr);
                changed = true;
                reReadData();
                textNovZanr.setText("");
            }
        });
	}

}
