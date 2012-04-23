package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.config.UserConfiguration;
import net.milanaleksic.mcs.infrastructure.gui.transformer.EmbeddedComponent;
import net.milanaleksic.mcs.infrastructure.gui.transformer.TransformationContext;
import net.milanaleksic.mcs.infrastructure.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkElementIndex;

public class SettingsDialogForm extends AbstractTransformedDialogForm {

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
        setupMainTab(transformationContext);
        setupMediumTypeTab(transformationContext);
        setupLocationTab(transformationContext);
        setupGenreTab(transformationContext);
        setupTagsTab(transformationContext);
    }

    private void setupMainTab(TransformationContext transformationContext) {
        transformationContext.<Button>getMappedObject("btnCancel").get().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        for (Language language : Language.values())
            comboLanguage.add(bundle.getString("language.name." + language.getName())); //NON-NLS
        if (userConfiguration.isPresent())
            comboLanguage.select(Language.ordinalForName(userConfiguration.get().getLocaleLanguage()));
        comboLanguage.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                int index = comboLanguage.getSelectionIndex();
                checkElementIndex(index, Language.values().length);
                userConfiguration.get().setLocaleLanguage(Language.values()[index].getName());
                runnerWhenClosingShouldRun = true;
            }
        });
        if (userConfiguration.isPresent())
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
        chkProxyServerUsesNtlm.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
                proxyConfiguration.setNtlm(chkProxyServerUsesNtlm.getSelection());
            }
        });
        if (userConfiguration.isPresent()) {
            UserConfiguration.ProxyConfiguration proxyConfiguration = userConfiguration.get().getProxyConfiguration();
            textProxyServer.setText(Strings.nullToEmpty(proxyConfiguration.getServer()));
            textProxyServerPassword.setText(Strings.nullToEmpty(proxyConfiguration.getPassword()));
            textProxyServerPort.setText(StringUtil.emptyIfNullOtherwiseConvert(proxyConfiguration.getPort()));
            textProxyServerUsername.setText(Strings.nullToEmpty(proxyConfiguration.getUsername()));
            chkProxyServerUsesNtlm.setSelection(proxyConfiguration.isNtlm());
        }
    }

    private void setupMediumTypeTab(TransformationContext transformationContext) {
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
        transformationContext.<Button>getMappedObject("btnAddMediumType").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listMediumTypes, SWT.NONE);
                String newMediumTypeName = getNewEntityText(listMediumTypes.getItems(), bundle.getString("settings.newMediumType"));
                tableItem.setText(newMediumTypeName);
                tipMedijaRepository.addTipMedija(newMediumTypeName);
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        transformationContext.<Button>getMappedObject("btnDeleteMediumType").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listMediumTypes.getSelectionIndex() < 0)
                    return;
                tipMedijaRepository.deleteMediumTypeByName(listMediumTypes.getItem(listMediumTypes.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
    }

    private void setupLocationTab(TransformationContext transformationContext) {
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
        transformationContext.<Button>getMappedObject("btnAddLocation").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listLokacije, SWT.NONE);
                String newLocation = getNewEntityText(listLokacije.getItems(), bundle.getString("settings.newLocation"));
                tableItem.setText(newLocation);
                pozicijaRepository.addPozicija(new Pozicija(newLocation, false));
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        transformationContext.<Button>getMappedObject("btnDeleteLocation").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listLokacije.getSelectionIndex() < 0)
                    return;
                pozicijaRepository.deletePozicijaByName(listLokacije.getItem(listLokacije.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
    }

    private void setupGenreTab(TransformationContext transformationContext) {
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
        transformationContext.<Button>getMappedObject("btnAddGenre").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listZanrovi, SWT.NONE);
                String newGenre = getNewEntityText(listZanrovi.getItems(), bundle.getString("settings.newGenre"));
                tableItem.setText(newGenre);
                zanrRepository.addZanr(newGenre);
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        transformationContext.<Button>getMappedObject("btnDeleteGenre").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listZanrovi.getSelectionIndex() < 0)
                    return;
                zanrRepository.deleteZanrByName(listZanrovi.getItem(listZanrovi.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
    }

    private void setupTagsTab(TransformationContext transformationContext) {
        listTagovi.addSelectionListener(new EditableSingleColumnTableSelectionListener(
                listTagovi, shell, bundle, new EditableSingleColumnTableSelectionListener.ContentEditingFinishedListener() {
            @Override
            public void contentEditingFinished(String finalContent, Object data) {
                runnerWhenClosingShouldRun = true;
                Tag tag = (Tag) data;
                tag.setNaziv(finalContent);
                tagRepository.updateTag(tag);
            }
        }));
        transformationContext.<Button>getMappedObject("btnAddTag").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                TableItem tableItem = new TableItem(listTagovi, SWT.NONE);
                String newTag = getNewEntityText(listTagovi.getItems(), bundle.getString("settings.newTag"));
                tableItem.setText(newTag);
                tagRepository.addTag(newTag);
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
        transformationContext.<Button>getMappedObject("btnDeleteTag").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (listTagovi.getSelectionIndex() < 0)
                    return;
                tagRepository.deleteTagByName(listTagovi.getItem(listTagovi.getSelectionIndex()).getText());
                runnerWhenClosingShouldRun = true;
                reReadData();
            }
        });
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
