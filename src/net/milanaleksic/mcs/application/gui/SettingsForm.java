package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkElementIndex;

public class SettingsForm extends AbstractTransformedForm {

    @Inject
    private PozicijaRepository pozicijaRepository;

    @Inject
    private ZanrRepository zanrRepository;

    @Inject
    private TipMedijaRepository tipMedijaRepository;

    @Inject
    private TagRepository tagRepository;

    private SelectionAdapter pozicijaDefaultButtonSelected = new HandledSelectionAdapter(shell, bundle) {
        @Override
        public void handledSelected(SelectionEvent event) throws ApplicationException {
            Pozicija pozicija = (Pozicija) event.widget.getData();
            pozicija.setDefault(true);
            pozicijaRepository.updatePozicija(pozicija);
            runnerWhenClosingShouldRun = true;
        }
    };

    @EmbeddedComponent
    private Table listMediumTypes = null;

    @EmbeddedComponent
    private Table listLokacije = null;

    @EmbeddedComponent
    private Table listZanrovi = null;

    @EmbeddedComponent
    private Table listTagovi = null;

    @EmbeddedComponent
    private Text textElementsPerPage = null;

    @EmbeddedComponent
    private Combo comboLanguage = null;

    @EmbeddedComponent
    private Text textProxyServer = null;

    @EmbeddedComponent
    private Text textProxyServerPort = null;

    @EmbeddedComponent
    private Text textProxyServerUsername = null;

    @EmbeddedComponent
    private Text textProxyServerPassword = null;

    @EmbeddedComponent
    private Button chkProxyServerUsesNtlm = null;

    private Optional<UserConfiguration> userConfiguration = Optional.absent();

    @EmbeddedEventListener(component = "btnCancel", event = SWT.Selection)
    private final HandledListener btnCancelSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
        }
    };

    @EmbeddedEventListener(component = "comboLanguage", event = SWT.Modify)
    private final HandledListener comboLanguageModifyListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (!isFormTransformationComplete())
                return;
            int index = comboLanguage.getSelectionIndex();
            checkElementIndex(index, Language.values().length);
            userConfiguration.get().setLocaleLanguage(Language.values()[index].getName());
            runnerWhenClosingShouldRun = true;
        }
    };

    @EmbeddedEventListener(component = "textElementsPerPage", event = SWT.Modify)
    private final HandledListener textElementsPerPageModifyListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (!isFormTransformationComplete())
                return;
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
    };

    @EmbeddedEventListener(component = "chkProxyServerUsesNtlm", event = SWT.Selection)
    private final HandledListener chkProxyServerUsesNtlmSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
            proxyConfiguration.setNtlm(chkProxyServerUsesNtlm.getSelection());
        }
    };

    @EmbeddedEventListeners({
            @EmbeddedEventListener(component = "textProxyServer", event = SWT.Modify),
            @EmbeddedEventListener(component = "textProxyServerPort", event = SWT.Modify),
            @EmbeddedEventListener(component = "textProxyServerUsername", event = SWT.Modify),
            @EmbeddedEventListener(component = "textProxyServerPassword", event = SWT.Modify)
    })
    private final HandledListener proxySettingsModifyListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (!isFormTransformationComplete())
                return;
            UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
            proxyConfiguration.setServer(textProxyServer.getText());
            if (!textProxyServerPort.getText().isEmpty())
                proxyConfiguration.setPort(Integer.parseInt(textProxyServerPort.getText()));
            proxyConfiguration.setUsername(textProxyServerUsername.getText());
            proxyConfiguration.setPassword(textProxyServerPassword.getText());
        }
    };

    @EmbeddedEventListener(component = "btnAddMediumType", event = SWT.Selection)
    private final HandledListener btnAddMediumTypeSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            TableItem tableItem = new TableItem(listMediumTypes, SWT.NONE);
            String newMediumTypeName = getNewEntityText(listMediumTypes.getItems(), bundle.getString("settings.newMediumType"));
            tableItem.setText(newMediumTypeName);
            tipMedijaRepository.addTipMedija(newMediumTypeName);
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnDeleteMediumType", event = SWT.Selection)
    private final HandledListener btnDeleteMediumTypeSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (listMediumTypes.getSelectionIndex() < 0)
                return;
            tipMedijaRepository.deleteMediumTypeByName(listMediumTypes.getItem(listMediumTypes.getSelectionIndex()).getText());
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnAddLocation", event = SWT.Selection)
    private final HandledListener btnAddLocationSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            TableItem tableItem = new TableItem(listLokacije, SWT.NONE);
            String newLocation = getNewEntityText(listLokacije.getItems(), bundle.getString("settings.newLocation"));
            tableItem.setText(newLocation);
            pozicijaRepository.addPozicija(new Pozicija(newLocation, false));
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnDeleteLocation", event = SWT.Selection)
    private final HandledListener btnDeleteLocationSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (listLokacije.getSelectionIndex() < 0)
                return;
            pozicijaRepository.deletePozicijaByName(listLokacije.getItem(listLokacije.getSelectionIndex()).getText());
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnAddGenre", event = SWT.Selection)
    private final HandledListener btnAddGenreSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            TableItem tableItem = new TableItem(listZanrovi, SWT.NONE);
            String newGenre = getNewEntityText(listZanrovi.getItems(), bundle.getString("settings.newGenre"));
            tableItem.setText(newGenre);
            zanrRepository.addZanr(newGenre);
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnDeleteGenre", event = SWT.Selection)
    private final HandledListener btnDeleteGenreSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (listZanrovi.getSelectionIndex() < 0)
                return;
            zanrRepository.deleteZanrByName(listZanrovi.getItem(listZanrovi.getSelectionIndex()).getText());
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnAddTag", event = SWT.Selection)
    private final HandledListener btnAddTagSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            TableItem tableItem = new TableItem(listTagovi, SWT.NONE);
            String newTag = getNewEntityText(listTagovi.getItems(), bundle.getString("settings.newTag"));
            tableItem.setText(newTag);
            tagRepository.addTag(newTag);
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "btnDeleteTag", event = SWT.Selection)
    private final HandledListener btnDeleteTagSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (listTagovi.getSelectionIndex() < 0)
                return;
            tagRepository.deleteTagByName(listTagovi.getItem(listTagovi.getSelectionIndex()).getText());
            runnerWhenClosingShouldRun = true;
            reReadData();
        }
    };

    @EmbeddedEventListener(component = "listMediumTypes", event = SWT.Selection)
    private final EditableSingleColumnTableSelectionListener listMediumTypesSelectionListener = new EditableSingleColumnTableSelectionListener(
            this, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
        @Override
        public void contentEditingFinished(String finalContent, Object data) {
            runnerWhenClosingShouldRun = true;
            TipMedija tipMedija = (TipMedija) data;
            tipMedija.setNaziv(finalContent);
            tipMedijaRepository.updateTipMedija(tipMedija);
        }
    });

    @EmbeddedEventListener(component = "listLokacije", event = SWT.Selection)
    private final EditableSingleColumnTableSelectionListener listLokacijeSelectionListener = new EditableSingleColumnTableSelectionListener(
            this, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
        @Override
        public void contentEditingFinished(String finalContent, Object data) {
            runnerWhenClosingShouldRun = true;
            Pozicija pozicija = (Pozicija) data;
            pozicija.setPozicija(finalContent);
            pozicijaRepository.updatePozicija(pozicija);
        }
    });

    @EmbeddedEventListener(component = "listZanrovi", event = SWT.Selection)
    private final EditableSingleColumnTableSelectionListener listZanroviSelectionListener = new EditableSingleColumnTableSelectionListener(
            this, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
        @Override
        public void contentEditingFinished(String finalContent, Object data) {
            runnerWhenClosingShouldRun = true;
            Zanr zanr = (Zanr) data;
            zanr.setZanr(finalContent);
            zanrRepository.updateZanr(zanr);
        }
    });

    @EmbeddedEventListener(component = "listTagovi", event = SWT.Selection)
    private final EditableSingleColumnTableSelectionListener listTagoviSelectionListener = new EditableSingleColumnTableSelectionListener(
            this, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
        @Override
        public void contentEditingFinished(String finalContent, Object data) {
            runnerWhenClosingShouldRun = true;
            Tag tag = (Tag) data;
            tag.setNaziv(finalContent);
            tagRepository.updateTag(tag);
        }
    });

    @Override
    public void open(Shell parent, Runnable callback) {
        this.userConfiguration = Optional.of(applicationManager.getUserConfiguration());
        super.open(parent, callback);
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
            createRadioButtonForLocation(pozicija, tableItem);
        }

        java.util.List<Zanr> zanrs = zanrRepository.getZanrs();
        listZanrovi.removeAll();
        for (Zanr zanr : zanrs) {
            TableItem tableItem = new TableItem(listZanrovi, SWT.NONE);
            tableItem.setText(zanr.getZanr());
            tableItem.setData(zanr);
        }

        java.util.List<Tag> tags = tagRepository.getTags();
        listTagovi.removeAll();
        for (Tag tag : tags) {
            TableItem tableItem = new TableItem(listTagovi, SWT.NONE);
            tableItem.setText(tag.getNaziv());
            tableItem.setData(tag);
        }
    }

    private void createRadioButtonForLocation(Pozicija pozicija, TableItem tableItem) {
        TableEditor editor = new TableEditor(listLokacije);
        final Button button = new Button(listLokacije, SWT.RADIO);
        button.setSelection(pozicija.isDefault());
        button.addSelectionListener(pozicijaDefaultButtonSelected);
        button.setData(pozicija);
        button.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        button.pack();
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(button, tableItem, 1);
        tableItem.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent disposeEvent) {
                button.dispose();
            }
        });
    }

    @Override
    protected void onTransformationComplete(TransformationContext transformationContext) {
        for (Language language : Language.values())
            comboLanguage.add(bundle.getString("language.name." + language.getName())); //NON-NLS
        if (userConfiguration.isPresent())
            comboLanguage.select(Language.ordinalForName(userConfiguration.get().getLocaleLanguage()));
        if (userConfiguration.isPresent())
            textElementsPerPage.setText(Integer.toString(userConfiguration.get().getElementsPerPage()));
        if (userConfiguration.isPresent()) {
            UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
            textProxyServer.setText(Strings.nullToEmpty(proxyConfiguration.getServer()));
            textProxyServerPassword.setText(Strings.nullToEmpty(proxyConfiguration.getPassword()));
            textProxyServerPort.setText(StringUtil.emptyIfNullOtherwiseConvert(proxyConfiguration.getPort()));
            textProxyServerUsername.setText(Strings.nullToEmpty(proxyConfiguration.getUsername()));
            chkProxyServerUsesNtlm.setSelection(proxyConfiguration.isNtlm());
        }
        listMediumTypesSelectionListener.prepare(listMediumTypes);
        listLokacijeSelectionListener.prepare(listLokacije);
        listZanroviSelectionListener.prepare(listZanrovi);
        listTagoviSelectionListener.prepare(listTagovi);
    }

    @Override
    protected void onShellReady() {
        reReadData();
    }

    private String getNewEntityText(TableItem[] items, String nameTemplate) {
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
