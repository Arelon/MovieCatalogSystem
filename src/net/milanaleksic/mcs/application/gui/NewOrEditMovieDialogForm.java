package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import net.milanaleksic.mcs.infrastructure.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.io.IOException;
import java.util.*;

public class NewOrEditMovieDialogForm extends AbstractDialogForm implements OfferMovieList.Receiver {

    @Inject
    private NewMediumDialogForm newMediumDialogForm;

    @Inject
    private ThumbnailManager thumbnailManager;

    @Inject
    private FilmRepository filmRepository;

    @Inject
    private ZanrRepository zanrRepository;

    @Inject
    private MedijRepository medijRepository;

    @Inject
    private PozicijaRepository pozicijaRepository;

    @Inject
    @Named("offerMovieListForNewOrEditForm")
    private OfferMovieList offerMovieListForNewOrEditForm;

    @Inject
    private FilmService filmService;

    @Inject
    FindMovieDialogForm findMovieDialogForm;

    private Composite mainPanel = null;
    private Combo comboLokacija = null;
    private Combo comboZanr = null;
    private Combo comboDisk = null;
    private List listDiskovi = null;
    private Combo comboNaziv = null;
    private Text textPrevod = null;
    private Text textImdbId = null;
    private Text textGodina = null;
    private Text textKomentar = null;
    private ShowImageComposite posterImage;

    private Film activeFilm = null;

    private HashMap<String, Zanr> sviZanrovi;
    private HashMap<String, Pozicija> sveLokacije;
    private HashMap<String, Medij> sviDiskovi;

    public void open(Shell parent, @Nullable Film film, Runnable callback) {
        this.activeFilm = film;
        super.open(parent, callback);
    }

    protected void reReadData() {
        refillCombos();
        if (activeFilm != null) {
            comboNaziv.setText(activeFilm.getNazivfilma());
            textPrevod.setText(activeFilm.getPrevodnazivafilma());
            textGodina.setText(String.valueOf(activeFilm.getGodina()));
            textImdbId.setText(activeFilm.getImdbId());
            textKomentar.setText(activeFilm.getKomentar());
            comboZanr.select(comboZanr.indexOf(activeFilm.getZanr().getZanr()));
            listDiskovi.removeAll();
            for (Medij medij : activeFilm.getMedijs())
                listDiskovi.add(medij.toString());
            int indexOfPozicija = comboLokacija.indexOf(activeFilm.getPozicija());
            if (indexOfPozicija >= 0)
                comboLokacija.select(indexOfPozicija);
            thumbnailManager.setThumbnailForShowImageComposite(posterImage, activeFilm.getImdbId());
        } else {
            Pozicija defaultPozicija = pozicijaRepository.getDefaultPozicija();
            if (defaultPozicija != null) {
                String nameOfDefaultPosition = defaultPozicija.getPozicija();
                if (comboLokacija.indexOf(nameOfDefaultPosition) != -1)
                    comboLokacija.select(comboLokacija.indexOf(nameOfDefaultPosition));
                if (comboDisk.getItemCount() != 0)
                    comboDisk.select(comboDisk.getItemCount() - 1);
            }
        }
    }

    protected void refillCombos() {
        String previousZanr = comboZanr.getText();
        String previousLokacija = comboLokacija.getText();
        String previousDisk = comboDisk.getText();
        if (comboZanr.getItemCount() != 0)
            comboZanr.removeAll();
        if (comboLokacija.getItemCount() != 0)
            comboLokacija.removeAll();
        if (comboDisk.getItemCount() != 0)
            comboDisk.removeAll();

        sviZanrovi = new HashMap<>();
        sveLokacije = new HashMap<>();
        sviDiskovi = new HashMap<>();

        for (Zanr zanr : zanrRepository.getZanrs()) {
            comboZanr.add(zanr.getZanr());
            sviZanrovi.put(zanr.getZanr(), zanr);
        }
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            comboLokacija.add(pozicija.getPozicija());
            sveLokacije.put(pozicija.getPozicija(), pozicija);
        }

        for (Medij medij : medijRepository.getMedijs()) {
            comboDisk.add(medij.toString());
            sviDiskovi.put(medij.toString(), medij);
        }
        if (previousZanr != null && previousZanr.length() > 0 && comboZanr.indexOf(previousZanr) != -1)
            comboZanr.select(comboZanr.indexOf(previousZanr));
        if (previousLokacija != null && previousLokacija.length() > 0 && comboLokacija.indexOf(previousLokacija) != -1)
            comboLokacija.select(comboLokacija.indexOf(previousLokacija));
        if (previousDisk != null && previousDisk.length() > 0 && comboDisk.indexOf(previousDisk) != -1)
            comboDisk.select(comboDisk.indexOf(previousDisk));
    }

    protected void dodajNoviFilm() {
        refillCombos();
        Film novFilm = new Film();
        novFilm.setNazivfilma(comboNaziv.getText().trim());
        novFilm.setPrevodnazivafilma(textPrevod.getText().trim());
        novFilm.setGodina(Integer.parseInt(textGodina.getText()));
        novFilm.setImdbId(textImdbId.getText().trim());
        novFilm.setKomentar(textKomentar.getText());
        Zanr zanr = sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex()));
        Pozicija position = sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex()));
        java.util.List<Medij> medijs = new ArrayList<>();
        if (listDiskovi.getItemCount() != 0) {
            for (String medijName : listDiskovi.getItems()) {
                Medij medij = sviDiskovi.get(medijName);
                medijs.add(medij);
            }
        }
        filmRepository.saveFilm(novFilm, zanr, medijs, position);
        reReadData();
    }

    private void izmeniFilm() {
        activeFilm.setNazivfilma(comboNaziv.getText().trim());
        activeFilm.setPrevodnazivafilma(textPrevod.getText().trim());
        activeFilm.setGodina(Integer.parseInt(textGodina.getText()));
        activeFilm.setImdbId(textImdbId.getText().trim());
        activeFilm.setKomentar(textKomentar.getText());

        Set<Medij> selectedMediums = new HashSet<>();
        for (String item : listDiskovi.getItems()) {
            selectedMediums.add(sviDiskovi.get(item));
        }

        filmService.updateFilmWithChanges(activeFilm,
                sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex())),
                sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex())),
                selectedMediums);

        reReadData();
    }


    @Override
    protected void onShellCreated() {
        shell.setText(bundle.getString("newOrEdit.addingOrModifyingMovie"));
        shell.setLayout(new GridLayout(1, false));
        shell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
            public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
                if (offerMovieListForNewOrEditForm != null)
                    offerMovieListForNewOrEditForm.cleanup();
            }
        });
        createMainPanel();
        createFooterPanel();
    }

    @Override
    protected void onShellReady() {
        offerMovieListForNewOrEditForm.prepareFor(comboNaziv);
        reReadData();
    }

    private void createMainPanel() {
        GridLayout gridLayout1 = new GridLayout(2, false);
        gridLayout1.horizontalSpacing = 10;
        GridData gridData1 = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
        mainPanel = new Composite(shell, SWT.NONE);
        mainPanel.setLayoutData(gridData1);
        mainPanel.setLayout(gridLayout1);
        createMovieNamePanel();
        Label labPrevod = new Label(mainPanel, SWT.NONE);
        labPrevod.setText(bundle.getString("newOrEdit.movieNameTranslated"));
        labPrevod.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        textPrevod = new Text(mainPanel, SWT.BORDER);
        textPrevod.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        textPrevod.addFocusListener(new org.eclipse.swt.events.FocusListener() {

            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                if (textPrevod.getText().trim().equals(""))
                    textPrevod.setText(bundle.getString("newOrEdit.unknown"));
            }

            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                if (textPrevod.getText().trim().equals(bundle.getString("newOrEdit.unknown")))
                    textPrevod.setText("");
            }

        });
        Label labZanr = new Label(mainPanel, SWT.NONE);
        labZanr.setText(bundle.getString("newOrEdit.genre"));
        labZanr.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        createComboZanr();
        Label labGodina = new Label(mainPanel, SWT.NONE);
        labGodina.setText(bundle.getString("newOrEdit.yearPublished"));
        labGodina.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        textGodina = new Text(mainPanel, SWT.BORDER);
        textGodina.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        Label labLokacija = new Label(mainPanel, SWT.NONE);
        labLokacija.setText(bundle.getString("newOrEdit.location"));
        labLokacija.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        createComboLokacija();
        Label labIMDB = new Label(mainPanel, SWT.NONE);
        labIMDB.setText(bundle.getString("newOrEdit.imdbIdFormat"));
        labIMDB.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        createImdbIdPanel(mainPanel);
        createPostersPanel();
        createCommentaryGroup();
        createMediumsGroup();
    }

    private void createPostersPanel() {
        Composite detailsPane = new Composite(mainPanel, SWT.NONE);
        detailsPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
        detailsPane.setLayout(new GridLayout(1, false));
        posterImage = new ShowImageComposite(bundle, detailsPane, SWT.NONE);
        posterImage.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
    }

    private void createMovieNamePanel() {
        Label labNazivFilma = new Label(mainPanel, SWT.RIGHT);
        labNazivFilma.setText(bundle.getString("newOrEdit.movieName"));
        labNazivFilma.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
        Composite movieNameComposite = new Composite(mainPanel, SWT.NONE);
        movieNameComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        GridLayout movieNameGridLayout = new GridLayout(2, false);
        movieNameGridLayout.marginLeft = 0;
        movieNameGridLayout.marginWidth = 0;
        movieNameGridLayout.marginHeight = 0;
        movieNameComposite.setLayout(movieNameGridLayout);
        comboNaziv = new Combo(movieNameComposite, SWT.DROP_DOWN);
        comboNaziv.setVisibleItemCount(10);
        comboNaziv.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        comboNaziv.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = comboNaziv.getSelectionIndex();
                if (index != -1) {
                    readFromMovie((Movie) comboNaziv.getData(Integer.toString(index)));
                }
            }
        });
        Button btnSearchMovie = new Button(movieNameComposite, SWT.PUSH);
        btnSearchMovie.setText(bundle.getString("global.search"));
        btnSearchMovie.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                findMovieDialogForm.setInitialText(comboNaziv.getText());
                findMovieDialogForm.open(shell, new Runnable() {
                    @Override
                    public void run() {
                        readFromMovie(findMovieDialogForm.getSelectedMovie());
                    }
                });
                findMovieDialogForm.setInitialText(null);
            }
        });
    }

    private void readFromMovie(Movie movie) {
        if (movie == null)
            return;
        if ("?".equals(movie.getReleasedYear()))
            textGodina.setText("");
        else
            textGodina.setText(movie.getReleasedYear());
        comboNaziv.setText(movie.getName());
        textKomentar.setText(movie.getOverview());
        textImdbId.setText(StringUtil.emptyIfNull(movie.getImdbId()));
        thumbnailManager.setThumbnailForShowImageComposite(posterImage, movie.getImdbId());
    }

    private void createImdbIdPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        GridLayout imdbIdComposite = new GridLayout(2, false);
        imdbIdComposite.marginLeft = 0;
        imdbIdComposite.marginWidth = 0;
        imdbIdComposite.marginHeight = 0;
        panel.setLayout(imdbIdComposite);
        textImdbId = new Text(panel, SWT.BORDER);
        textImdbId.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        final Link link = new Link(panel, SWT.NONE);
        link.setText(String.format("<a>%s</a>", bundle.getString("newOrEdit.goToImdbPage"))); //NON-NLS
        link.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        link.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                try {
                    Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(textImdbId.getText()));
                } catch (IOException e) {
                    throw new ApplicationException("IO Exception when trying to browse to IMDB page", e);
                }
            }
        });
        link.setEnabled(false);
        textImdbId.addModifyListener(new HandledModifyListener(shell, bundle) {
            @Override
            public void handledModifyText() throws ApplicationException {
                link.setEnabled(IMDBUtil.isValidImdbId(textImdbId.getText()));
            }
        });
    }

    private void createFooterPanel() {
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.verticalSpacing = 5;
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 10;
        gridLayout.horizontalSpacing = 40;
        GridData gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
        gridData.widthHint = -1;
        gridData.horizontalIndent = 0;
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(gridData);
        composite.setLayout(gridLayout);
        Button btnPrihvati = new Button(composite, SWT.NONE);
        btnPrihvati.setText(bundle.getString("global.save"));
        btnPrihvati.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                //TODO: replace with Hibernate Validator (JSR 303 implementation)
                StringBuilder razlogOtkaza = new StringBuilder();
                if (listDiskovi.getItemCount() == 0)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.atLeastOneMediumNeeded"));
                if (comboNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.movieMustHaveName"));
                if (comboNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.movieMustHaveNameTranslation"));
                if (comboZanr.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.genreMustBeSelected"));
                try {
                    Integer.parseInt(textGodina.getText());
                } catch (Throwable t) {
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.yearNotANumber"));
                }
                if (comboLokacija.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.locationMustBeSelected"));
                if (!textImdbId.getText().isEmpty() && !IMDBUtil.isValidImdbId(textImdbId.getText()))
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.imdbFormatNotOk"));
                if (razlogOtkaza.length() != 0) {
                    MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
                    messageBox.setText(bundle.getString("newOrEdit.movieCouldNotBeAdded"));
                    messageBox.setMessage(bundle.getString("newOrEdit.cancellationCause") + "\r\n----------" + razlogOtkaza.toString());
                    messageBox.open();
                } else {
                    if (activeFilm != null)
                        izmeniFilm();
                    else
                        dodajNoviFilm();
                    runnerWhenClosingShouldRun = true;
                    shell.close();
                }
            }
        });
        Button btnOdustani = new Button(composite, SWT.NONE);
        btnOdustani.setText(bundle.getString("global.cancel"));
        btnOdustani.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
    }

    private void createComboLokacija() {
        GridData gridData9 = new GridData();
        gridData9.horizontalAlignment = GridData.FILL;
        comboLokacija = new Combo(mainPanel, SWT.READ_ONLY);
        comboLokacija.setVisibleItemCount(10);
        comboLokacija.setLayoutData(gridData9);
    }

    private void createComboZanr() {
        GridData gridData8 = new GridData();
        gridData8.horizontalAlignment = GridData.FILL;
        comboZanr = new Combo(mainPanel, SWT.READ_ONLY);
        comboZanr.setVisibleItemCount(10);
        comboZanr.setLayoutData(gridData8);
    }

    private void createMediumsGroup() {
        Group groupMediums = new Group(mainPanel, SWT.NONE);
        groupMediums.setText(bundle.getString("newOrEdit.mediums"));
        groupMediums.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        groupMediums.setLayout(new GridLayout(3, false));

        // 1st row
        comboDisk = new Combo(groupMediums, SWT.READ_ONLY);
        comboDisk.setVisibleItemCount(10);
        comboDisk.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        Button btnDodajDisk = new Button(groupMediums, SWT.NONE);
        btnDodajDisk.setText(bundle.getString("newOrEdit.addMedium"));
        btnDodajDisk.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
        btnDodajDisk.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                int index = comboDisk.getSelectionIndex();
                if (index != -1) {
                    if (listDiskovi.indexOf(comboDisk.getItem(index)) == -1) {
                        listDiskovi.add(comboDisk.getItem(index));
                    }
                }
            }
        });
        Button btnNovMedij = new Button(groupMediums, SWT.NONE);
        btnNovMedij.setText(bundle.getString("newOrEdit.newMedium"));
        btnNovMedij.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                newMediumDialogForm.open(shell, new Runnable() {
                    @Override
                    public void run() {
                        // preuzimanje svih podataka od interesa i upis u kombo boksove
                        refillCombos();
                        if (comboDisk.getItemCount() > 0)
                            comboDisk.select(comboDisk.getItemCount() - 1);
                    }
                });
            }
        });

        // 2nd row
        listDiskovi = new List(groupMediums, SWT.NONE);
        GridData mediumListGridData = new GridData();
        mediumListGridData.horizontalAlignment = GridData.FILL;
        mediumListGridData.heightHint = 50;
        listDiskovi.setLayoutData(mediumListGridData);
        Button btnOduzmi = new Button(groupMediums, SWT.NONE);
        btnOduzmi.setText(bundle.getString("newOrEdit.removeMedium"));
        btnOduzmi.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        btnOduzmi.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (listDiskovi.getSelectionIndex() != -1)
                    listDiskovi.remove(listDiskovi.getSelectionIndex());
            }
        });
    }

    private void createCommentaryGroup() {
        GridData gridData = new GridData();
        gridData.widthHint = 200;
        gridData.verticalSpan = 2;
        gridData.heightHint = 80;
        Group group = new Group(mainPanel, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setText(bundle.getString("newOrEdit.comment"));
        textKomentar = new Text(group, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        textKomentar.setLayoutData(gridData);
        Button btnNeodgledano = new Button(group, SWT.NONE);
        btnNeodgledano.setText(bundle.getString("newOrEdit.iDidNotWatch"));
        btnNeodgledano.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + bundle.getString("newOrEdit.iDidNotWatch"));
            }
        });
        Button btnLos = new Button(group, SWT.NONE);
        btnLos.setText(bundle.getString("newOrEdit.badRecording"));
        btnLos.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + bundle.getString("newOrEdit.badRecording"));
            }
        });
    }

    @Override
    public void setCurrentQueryItems(final String query, final String message, @Nullable final Movie[] movies) {
        if (shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (query.equals(comboNaziv.getText())) {
                    Point selection = comboNaziv.getSelection();
                    if (message != null) {
                        comboNaziv.setItems(new String[]{message});
                    } else if (movies != null) {
                        String[] newItems = new String[movies.length <= 10 ? movies.length : 10];
                        for (int i = 0; i < newItems.length; i++) {
                            newItems[i] = String.format("%s (%s)", movies[i].getName(), movies[i].getReleasedYear()); //NON-NLS
                        }
                        comboNaziv.setItems(newItems);
                        for (int i = 0; i < movies.length; i++)
                            comboNaziv.setData("" + i, movies[i]);
                    }
                    comboNaziv.setListVisible(false);
                    comboNaziv.setListVisible(true);
                    comboNaziv.setText(query);
                    comboNaziv.setSelection(selection);
                }
            }
        });
    }

}
