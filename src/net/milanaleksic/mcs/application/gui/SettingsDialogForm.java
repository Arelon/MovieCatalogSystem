package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class SettingsDialogForm extends AbstractDialogForm {

    @Inject
    private PozicijaRepository pozicijaRepository;

    @Inject
    private ZanrRepository zanrRepository;

    @Inject
    private TipMedijaRepository tipMedijaRepository;

    private SelectionAdapter pozicijaDefaultButtonSelected;

    private Table listMediumTypes;
    private Table listLokacije;
    private Table listZanrovi;
    private Text textElementsPerPage;
    private Combo comboLanguage;

    private Optional<UserConfiguration> userConfiguration = Optional.absent();

    @Override public void open(Shell parent, Runnable callback) {
        this.userConfiguration = Optional.of(applicationManager.getUserConfiguration());
        super.open(parent, callback);
    }

    private void createDefaultButtonSelectionListener() {
        pozicijaDefaultButtonSelected = new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                Pozicija pozicija = (Pozicija) event.widget.getData();
                pozicija.setDefault(true);
                pozicijaRepository.updatePozicija(pozicija);
                runnerWhenClosingShouldRun = true;
            }
        };
    }

    private void reReadData() {
        java.util.List<TipMedija> tipMedijas = tipMedijaRepository.getTipMedijas();
        listMediumTypes.removeAll();
        for (TipMedija tipMedija : tipMedijas) {
            TableItem tableItem = new TableItem(listMediumTypes, SWT.NONE);
            tableItem.setText(tipMedija.getNaziv());
            tableItem.setData(tipMedija);
        }

        java.util.List<Pozicija> pozicijas = pozicijaRepository.getPozicijas();
        listLokacije.removeAll();
        for (Pozicija pozicija : pozicijas) {
            TableItem tableItem = new TableItem(listLokacije, SWT.NONE);
            tableItem.setText(pozicija.getPozicija());
            tableItem.setData(pozicija);
            TableEditor editor = new TableEditor(listLokacije);
            Button button = new Button(listLokacije, SWT.RADIO);
            button.setSelection(pozicija.isDefault());
            button.addSelectionListener(pozicijaDefaultButtonSelected);
            button.setData(pozicija);
            button.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            button.pack();
            editor.minimumWidth = button.getSize().x;
            editor.horizontalAlignment = SWT.LEFT;
            editor.setEditor(button, tableItem, 1);
        }

        java.util.List<Zanr> zanrs = zanrRepository.getZanrs();
        listZanrovi.removeAll();
        for (Zanr zanr : zanrs) {
            TableItem tableItem = new TableItem(listZanrovi, SWT.NONE);
            tableItem.setText(zanr.getZanr());
            tableItem.setData(zanr);
        }
    }

    @Override protected void onShellCreated() {
        shell.setText(bundle.getString("settings.programSettings"));
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(new Point(500, 350));
        createTabFolder();
        createComposite();
        createDefaultButtonSelectionListener();
    }

    @Override protected void onShellReady() {
        reReadData();
    }

    private void createComposite() {
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.horizontalSpacing = 20;
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.CENTER, GridData.END, true, false));
        Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.setText(bundle.getString("global.close"));
        btnCancel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, true));
        btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
    }

    private void createTabFolder() {
        TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, true));

        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setText(bundle.getString("settings.basicSettings"));
        tabItem.setControl(createSettingsTabContents(tabFolder));
        TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
        tabItem1.setText(bundle.getString("settings.mediumTypes"));
        tabItem1.setControl(createMediumTypeTabContents(tabFolder));
        TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
        tabItem2.setText(bundle.getString("settings.locations"));
        tabItem2.setControl(createLocationTabContents(tabFolder));
        TabItem tabItem3 = new TabItem(tabFolder, SWT.NONE);
        tabItem3.setText(bundle.getString("settings.genres"));
        tabItem3.setControl(createGenresTabContents(tabFolder));
    }

    private Composite createSettingsTabContents(TabFolder tabFolder) {
        Composite compositeSettings = new Composite(tabFolder, SWT.NONE);
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
            comboLanguage.add(bundle.getString("language.name." + language.getName()));
        comboLanguage.select(Language.ordinalForName(userConfiguration.get().getLocaleLanguage()));
        comboLanguage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                int index = comboLanguage.getSelectionIndex();
                if (index > -1 && index < Language.values().length) {
                    userConfiguration.get().setLocaleLanguage(Language.values()[index].getName());
                    runnerWhenClosingShouldRun = true;
                }
            }
        });

        //elementsPerPage
        Label label = new Label(groupGlobal, SWT.NONE);
        label.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, true, false));
        label.setText(bundle.getString("settings.numberOfElementsPerPage"));
        textElementsPerPage = new Text(groupGlobal, SWT.BORDER);
        textElementsPerPage.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textElementsPerPage.setText(Integer.toString(userConfiguration.get().getElementsPerPage()));
        textElementsPerPage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                String data = textElementsPerPage.getText();
                if (data.isEmpty()) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.get().getElementsPerPage()));
                    return;
                }
                int elementsPerPage;
                try {
                    elementsPerPage = Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.get().getElementsPerPage()));
                    return;
                }
                if (elementsPerPage < 0) {
                    textElementsPerPage.setText(Integer.toString(userConfiguration.get().getElementsPerPage()));
                    return;
                }
                userConfiguration.get().setElementsPerPage(elementsPerPage);
                runnerWhenClosingShouldRun = true;
            }
        });

        UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
        Group groupProxyServer = new Group(compositeSettings, SWT.NONE);
        groupProxyServer.setText(bundle.getString("settings.proxyServer"));
        groupProxyServer.setLayout(new GridLayout(2, false));
        groupProxyServer.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelServer = new Label(groupProxyServer, SWT.NONE);
        labelServer.setText(bundle.getString("settings.proxyServerAddress"));
        labelServer.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServer = new Text(groupProxyServer, SWT.BORDER);
        textProxyServer.setText(Strings.nullToEmpty(proxyConfiguration.getServer()));
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
        textProxyServerUsername.setText(Strings.nullToEmpty(proxyConfiguration.getUsername()));
        textProxyServerUsername.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labelPassword = new Label(groupProxyServer, SWT.NONE);
        labelPassword.setText(bundle.getString("settings.password"));
        labelPassword.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Text textProxyServerPassword = new Text(groupProxyServer, SWT.BORDER | SWT.PASSWORD);
        textProxyServerPassword.setText(Strings.nullToEmpty(proxyConfiguration.getPassword()));
        textProxyServerPassword.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        ModifyListener proxySettingsModifyListener = new HandledModifyListener(shell, bundle) {
            @Override
            public void handledModifyText() {
                UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
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

        Label labelSpacer = new Label(groupProxyServer, SWT.NONE);
        labelSpacer.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, true, false));
        final Button chkProxyServerUsesNtlm = new Button(groupProxyServer, SWT.CHECK);
        chkProxyServerUsesNtlm.setText(bundle.getString("settings.proxyUsesNtlm"));
        chkProxyServerUsesNtlm.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false));
        chkProxyServerUsesNtlm.setSelection(proxyConfiguration.isNtlm());
        chkProxyServerUsesNtlm.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
                proxyConfiguration.setNtlm(chkProxyServerUsesNtlm.getSelection());
            }
        });
        return compositeSettings;
    }

    private Composite createMediumTypeTabContents(TabFolder tabFolder) {
        Composite compositeGenres = new Composite(tabFolder, SWT.NONE);
        compositeGenres.setLayout(new GridLayout(2, false));
        listMediumTypes = new Table(compositeGenres, SWT.BORDER | SWT.FULL_SELECTION);
        listMediumTypes.setHeaderVisible(true);
        GridData listGridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 3);
        listGridData.heightHint = 180;
        listMediumTypes.setLayoutData(listGridData);
        TableColumn tableColumn = new TableColumn(listMediumTypes, SWT.LEFT | SWT.FLAT);
        tableColumn.setText(bundle.getString("settings.mediumTypeName"));
        tableColumn.setWidth(370);

        listMediumTypes.addSelectionListener(new EditableSingleColumnTableSelectionListener(
                listMediumTypes, shell, bundle, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
            @Override
            public void contentEditingFinished(String finalContent, Object data) {
                runnerWhenClosingShouldRun = true;
                TipMedija tipMedija = (TipMedija) data;
                tipMedija.setNaziv(finalContent);
                tipMedijaRepository.updateTipMedija(tipMedija);
            }
        }));

        Button btnAddMediumType = new Button(compositeGenres, SWT.NONE);
        btnAddMediumType.setText(bundle.getString("settings.add"));
        btnAddMediumType.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        btnAddMediumType.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listMediumTypes, SWT.NONE);
                String newMediumTypeName = getNewEntityTemplateName(listMediumTypes.getItems(), bundle.getString("settings.newMediumType"));
                tableItem.setText(newMediumTypeName);
                tipMedijaRepository.addTipMedija(newMediumTypeName);
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        Button btnDeleteMediumType = new Button(compositeGenres, SWT.NONE);
        btnDeleteMediumType.setText(bundle.getString("global.delete"));
        btnDeleteMediumType.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        btnDeleteMediumType.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listMediumTypes.getSelectionIndex() < 0)
                    return;
                tipMedijaRepository.deleteMediumTypeByName(listMediumTypes.getItem(listMediumTypes.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        return compositeGenres;
    }

    private Composite createLocationTabContents(TabFolder tabFolder) {
        Composite compositeLocations = new Composite(tabFolder, SWT.NONE);
        compositeLocations.setLayout(new GridLayout(2, false));
        listLokacije = new Table(compositeLocations, SWT.BORDER | SWT.FULL_SELECTION);
        listLokacije.setHeaderVisible(true);
        GridData listGridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 3);
        listGridData.heightHint = 180;
        listLokacije.setLayoutData(listGridData);
        TableColumn tableColumn = new TableColumn(listLokacije, SWT.LEFT);
        tableColumn.setText(bundle.getString("settings.locationName"));
        tableColumn.setWidth(320);
        TableColumn tableColumn1 = new TableColumn(listLokacije, SWT.LEFT | SWT.FLAT);
        tableColumn1.setText(bundle.getString("settings.locationDefault"));
        tableColumn1.setWidth(50);

        listLokacije.addSelectionListener(new EditableSingleColumnTableSelectionListener(
                listLokacije, shell, bundle, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
            @Override
            public void contentEditingFinished(String finalContent, Object data) {
                runnerWhenClosingShouldRun = true;
                Pozicija pozicija = (Pozicija) data;
                pozicija.setPozicija(finalContent);
                pozicijaRepository.updatePozicija(pozicija);
            }
        }));

        Button btnAddLocation = new Button(compositeLocations, SWT.NONE);
        btnAddLocation.setText(bundle.getString("settings.add"));
        btnAddLocation.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        btnAddLocation.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listLokacije, SWT.NONE);
                String newLocation = getNewEntityTemplateName(listLokacije.getItems(), bundle.getString("settings.newLocation"));
                tableItem.setText(newLocation);
                pozicijaRepository.addPozicija(new Pozicija(newLocation, false));
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        Button btnDeleteLocation = new Button(compositeLocations, SWT.NONE);
        btnDeleteLocation.setText(bundle.getString("global.delete"));
        btnDeleteLocation.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        btnDeleteLocation.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listLokacije.getSelectionIndex() < 0)
                    return;
                pozicijaRepository.deletePozicijaByName(listLokacije.getItem(listLokacije.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        return compositeLocations;
    }

    private Composite createGenresTabContents(TabFolder tabFolder) {
        Composite compositeGenres = new Composite(tabFolder, SWT.NONE);
        compositeGenres.setLayout(new GridLayout(2, false));
        listZanrovi = new Table(compositeGenres, SWT.BORDER | SWT.FULL_SELECTION);
        listZanrovi.setHeaderVisible(true);
        GridData listGridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 3);
        listGridData.heightHint = 180;
        listZanrovi.setLayoutData(listGridData);
        TableColumn tableColumn = new TableColumn(listZanrovi, SWT.LEFT | SWT.FLAT);
        tableColumn.setText(bundle.getString("settings.genreName"));
        tableColumn.setWidth(370);

        listZanrovi.addSelectionListener(new EditableSingleColumnTableSelectionListener(
                listZanrovi, shell, bundle, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
            @Override
            public void contentEditingFinished(String finalContent, Object data) {
                runnerWhenClosingShouldRun = true;
                Zanr zanr = (Zanr) data;
                zanr.setZanr(finalContent);
                zanrRepository.updateZanr(zanr);
            }
        }));

        Button btnAddGenre = new Button(compositeGenres, SWT.NONE);
        btnAddGenre.setText(bundle.getString("settings.add"));
        btnAddGenre.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        btnAddGenre.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listZanrovi, SWT.NONE);
                String newGenre = getNewEntityTemplateName(listZanrovi.getItems(), bundle.getString("settings.newGenre"));
                tableItem.setText(newGenre);
                zanrRepository.addZanr(newGenre);
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        Button btnDeleteGenre = new Button(compositeGenres, SWT.NONE);
        btnDeleteGenre.setText(bundle.getString("global.delete"));
        btnDeleteGenre.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        btnDeleteGenre.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listZanrovi.getSelectionIndex() < 0)
                    return;
                zanrRepository.deleteZanrByName(listZanrovi.getItem(listZanrovi.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        return compositeGenres;
    }

    private String getNewEntityTemplateName(TableItem[] items, String nameTemplate) {
        int iter = 0;
        boolean found;
        String title;
        while (true) {
            title = nameTemplate + (iter == 0 ? "" : iter);
            found = false;
            for (TableItem item : items) {
                if (item.getText().equals(title))
                    found = true;
            }
            if (!found)
                break;
            iter++;
        }
        return title;
    }

}
