package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.awt.*;
import java.io.IOException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class NewOrEditMovieDialogForm extends AbstractTransformedForm implements OfferMovieList.Receiver {

    @Inject
    private NewMediumDialogForm newMediumDialogDialogForm;

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
    private TipMedijaRepository tipMedijaRepository;

    @Inject
    private TagRepository tagRepository;

    @Inject
    @Named("offerMovieListForNewOrEditForm")
    private OfferMovieList offerMovieListForNewOrEditForm;

    @Inject
    private FilmService filmService;

    @Inject
    FindMovieForm findMovieDialogForm;

    @EmbeddedComponent
    private Combo comboLokacija = null;

    @EmbeddedComponent
    private Combo comboZanr = null;

    @EmbeddedComponent
    private Combo comboDisk = null;

    @EmbeddedComponent
    private List listDiskovi = null;

    @EmbeddedComponent
    private Combo comboNaziv = null;

    @EmbeddedComponent
    private Text textPrevod = null;

    @EmbeddedComponent
    private Text textImdbId = null;

    @EmbeddedComponent
    private Link imdbLink = null;

    @EmbeddedComponent
    private Text textGodina = null;

    @EmbeddedComponent
    private Text textKomentar = null;

    @EmbeddedComponent
    private Table tableTags = null;

    @EmbeddedComponent
    private ShowImageComposite posterImage = null;

    private Optional<Film> activeFilm;

    @EmbeddedEventListener(event = SWT.Close)
    private final Listener shellCloseListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (offerMovieListForNewOrEditForm != null)
                offerMovieListForNewOrEditForm.cleanUp();
        }
    };

    @EmbeddedEventListener(component = "btnSearchMovie", event = SWT.Selection)
    private final Listener btnSearchMovieSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            findMovieDialogForm.setInitialText(comboNaziv.getText());
            findMovieDialogForm.open(shell, new Runnable() {
                @Override
                public void run() {
                    Optional<Movie> selectedMovie = findMovieDialogForm.getSelectedMovie();
                    if (selectedMovie.isPresent())
                        readFromMovie(selectedMovie.get());
                }
            });
        }
    };

    @EmbeddedEventListener(component = "textPrevod", event = SWT.FocusIn)
    private final Listener textPrevodFocusInListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (textPrevod.getText().trim().equals(bundle.getString("newOrEdit.unknown")))
                textPrevod.setText("");
        }
    };

    @EmbeddedEventListener(component = "textPrevod", event = SWT.FocusOut)
    private final Listener textPrevodFocusOutListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (textPrevod.getText().trim().equals(""))
                textPrevod.setText(bundle.getString("newOrEdit.unknown"));
        }
    };

    @EmbeddedEventListener(component = "comboNaziv", event = SWT.Selection)
    private final Listener comboNazivSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            int index = comboNaziv.getSelectionIndex();
            if (index != -1) {
                readFromMovie((Movie) comboNaziv.getData(Integer.toString(index)));
            }
        }
    };

    @EmbeddedEventListener(component = "btnNovMedij", event = SWT.Selection)
    private final Listener btnNovMedijSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            newMediumDialogDialogForm.open(shell, new Runnable() {
                @Override
                public void run() {
                    refillCombos();
                    if (comboDisk.getItemCount() > 0)
                        comboDisk.select(comboDisk.getItemCount() - 1);
                }
            });
        }
    };

    @EmbeddedEventListener(component = "btnOduzmi", event = SWT.Selection)
    private final Listener btnOduzmiSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (listDiskovi.getSelectionIndex() != -1)
                listDiskovi.remove(listDiskovi.getSelectionIndex());
        }
    };

    @EmbeddedEventListener(component = "btnDodajDisk", event = SWT.Selection)
    private final Listener btnDodajDiskSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            int index = comboDisk.getSelectionIndex();
            if (index != -1) {
                if (listDiskovi.indexOf(comboDisk.getItem(index)) == -1) {
                    listDiskovi.add(comboDisk.getItem(index));
                }
            }
        }
    };

    @EmbeddedEventListener(component = "btnPrihvati", event = SWT.Selection)
    private final Listener btnPrihvatiSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
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
                if (activeFilm.isPresent())
                    izmeniFilm();
                else
                    dodajNoviFilm();
                runnerWhenClosingShouldRun = true;
                shell.close();
            }
        }
    };

    @EmbeddedEventListener(component = "btnOdustani", event = SWT.Selection)
    private final Listener btnOdustaniSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
        }
    };

    @EmbeddedEventListener(component = "imdbLink", event = SWT.Selection)
    private final Listener imdbLinkSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            try {
                Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(textImdbId.getText()));
            } catch (IOException e) {
                throw new ApplicationException("IO Exception when trying to browse to IMDB page", e);
            }
        }
    };

    @EmbeddedEventListener(component = "textImdbId", event = SWT.Modify)
    private final Listener textImdbIdSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (!isFormTransformationComplete())
                return;
            imdbLink.setEnabled(IMDBUtil.isValidImdbId(textImdbId.getText()));
        }
    };

    private Function<TableItem, Tag> tableItemToTagFunction = new Function<TableItem, Tag>() {
        @Override
        public Tag apply(TableItem input) {
            checkNotNull(input);
            return (Tag) input.getData();
        }
    };

    private Predicate<TableItem> isCheckedTableItemFunction = new Predicate<TableItem>() {
        @Override
        public boolean apply(TableItem input) {
            checkNotNull(input);
            return input.getChecked();
        }
    };

    public void open(Shell parent, Optional<Film> film, Runnable callback) {
        this.activeFilm = film;
        super.open(parent, callback);
    }

    protected void reReadData() {
        refillCombos();
        if (activeFilm.isPresent()) {
            Film film = activeFilm.get();
            comboNaziv.setText(film.getNazivfilma());
            textPrevod.setText(film.getPrevodnazivafilma());
            textGodina.setText(String.valueOf(film.getGodina()));
            textImdbId.setText(film.getImdbId());
            textKomentar.setText(film.getKomentar());
            comboZanr.select(comboZanr.indexOf(film.getZanr().getZanr()));
            listDiskovi.removeAll();
            for (Medij medij : film.getMedijs())
                listDiskovi.add(medij.toString());
            for (Tag tag : film.getTags())
                findAndSelectTagInTable(tableTags, tag);
            int indexOfPozicija = comboLokacija.indexOf(film.getPozicija());
            if (indexOfPozicija >= 0)
                comboLokacija.select(indexOfPozicija);
            thumbnailManager.setThumbnailForShowImageComposite(posterImage, film.getImdbId());
        } else {
            Optional<Pozicija> defaultPozicija = pozicijaRepository.getDefaultPozicija();
            if (defaultPozicija.isPresent()) {
                String nameOfDefaultPosition = defaultPozicija.get().getPozicija();
                if (comboLokacija.indexOf(nameOfDefaultPosition) != -1)
                    comboLokacija.select(comboLokacija.indexOf(nameOfDefaultPosition));
                if (comboDisk.getItemCount() != 0)
                    comboDisk.select(comboDisk.getItemCount() - 1);
            }
        }
    }

    private void findAndSelectTagInTable(Table targetTable, Tag tag) {
        checkNotNull(tag);
        for (TableItem tableItem : targetTable.getItems()) {
            if (tag.equals(tableItem.getData()))
                tableItem.setChecked(true);
        }
    }

    protected void refillCombos() {
        String previousZanr = comboZanr.getText();
        String previousLokacija = comboLokacija.getText();
        String previousDisk = comboDisk.getText();
        Iterable<Tag> previousTags = getSelectedTags();
        comboZanr.removeAll();
        comboLokacija.removeAll();
        comboDisk.removeAll();
        tableTags.removeAll();

        for (Zanr zanr : zanrRepository.getZanrs()) {
            comboZanr.add(zanr.getZanr());
            comboZanr.setData(zanr.getZanr(), zanr);
        }
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            comboLokacija.add(pozicija.getPozicija());
            comboLokacija.setData(pozicija.getPozicija(), pozicija);
        }
        for (Medij medij : medijRepository.getMedijs()) {
            comboDisk.add(medij.toString());
            comboDisk.setData(medij.toString(), medij);
        }
        for (Tag tag : tagRepository.getTags()) {
            TableItem tableItem = new TableItem(tableTags, SWT.NONE);
            tableItem.setText(tag.getNaziv());
            tableItem.setData(tag);
        }

        if (comboZanr.getItemCount() == 0 || comboDisk.getItemCount() == 0 || tipMedijaRepository.getTipMedijas().size() == 0) {
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
            box.setMessage(bundle.getString("newOrEdit.someBasicDomainElementsMissing"));
            box.setText(bundle.getString("global.information"));
            box.open();
            shell.close();
            return;
        }

        if (!previousZanr.isEmpty() && comboZanr.indexOf(previousZanr) != -1)
            comboZanr.select(comboZanr.indexOf(previousZanr));
        if (!previousLokacija.isEmpty() && comboLokacija.indexOf(previousLokacija) != -1)
            comboLokacija.select(comboLokacija.indexOf(previousLokacija));
        if (!previousDisk.isEmpty() && comboDisk.indexOf(previousDisk) != -1)
            comboDisk.select(comboDisk.indexOf(previousDisk));
        for (Tag tag : previousTags)
            findAndSelectTagInTable(tableTags, tag);
    }

    protected void dodajNoviFilm() {
        refillCombos();
        Film novFilm = new Film();
        novFilm.setNazivfilma(comboNaziv.getText().trim());
        novFilm.setPrevodnazivafilma(textPrevod.getText().trim());
        novFilm.setGodina(Integer.parseInt(textGodina.getText()));
        novFilm.setImdbId(textImdbId.getText().trim());
        novFilm.setKomentar(textKomentar.getText());
        Zanr zanr = (Zanr) comboZanr.getData(comboZanr.getItem(comboZanr.getSelectionIndex()));
        Pozicija position = (Pozicija) comboLokacija.getData(comboLokacija.getItem(comboLokacija.getSelectionIndex()));
        filmRepository.saveFilm(novFilm, zanr, getSelectedMediums(), position, getSelectedTags());
        reReadData();
    }

    private void izmeniFilm() {
        Film film = activeFilm.get();
        film.setNazivfilma(comboNaziv.getText().trim());
        film.setPrevodnazivafilma(textPrevod.getText().trim());
        film.setGodina(Integer.parseInt(textGodina.getText()));
        film.setImdbId(textImdbId.getText().trim());
        film.setKomentar(textKomentar.getText());

        filmService.updateFilmWithChanges(film,
                (Zanr) comboZanr.getData(comboZanr.getItem(comboZanr.getSelectionIndex())),
                (Pozicija) comboLokacija.getData(comboLokacija.getItem(comboLokacija.getSelectionIndex())),
                getSelectedMediums(), getSelectedTags());
    }

    private Iterable<Tag> getSelectedTags() {
        return Iterables.transform(
                Iterables.filter(Lists.newArrayList(tableTags.getItems()), isCheckedTableItemFunction),
                tableItemToTagFunction);
    }

    private Set<Medij> getSelectedMediums() {
        Set<Medij> medijs = Sets.newHashSet();
        for (String medijName : listDiskovi.getItems()) {
            // only comboDisk is holding detached entities
            Medij medij = (Medij) comboDisk.getData(medijName);
            medijs.add(medij);
        }
        return medijs;
    }

    @Override
    protected void onShellReady() {
        offerMovieListForNewOrEditForm.prepareFor(comboNaziv);
        reReadData();
    }

    private void readFromMovie(@Nonnull Movie movie) {
        if ("?".equals(movie.getReleasedYear()))
            textGodina.setText("");
        else
            textGodina.setText(movie.getReleasedYear());
        comboNaziv.setText(movie.getName());
        textKomentar.setText(movie.getOverview());
        textImdbId.setText(Strings.nullToEmpty(movie.getImdbId()));
        thumbnailManager.setThumbnailForShowImageComposite(posterImage, movie.getImdbId());
    }

    @Override
    public void setCurrentQueryItems(final String query, final Optional<String> message, final Optional<Movie[]> moviesOptional) {
        if (shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (query.equals(comboNaziv.getText())) {
                    Point selection = comboNaziv.getSelection();
                    if (message.isPresent()) {
                        comboNaziv.setItems(new String[]{message.get()});
                    } else if (moviesOptional.isPresent()) {
                        Movie[] movies = moviesOptional.get();
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
