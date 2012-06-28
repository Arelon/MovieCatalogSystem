package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nonnull;
import javax.inject.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

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
    FindMovieDialogForm findMovieDialogDialogForm;

    @EmbeddedComponent
    private Combo comboLokacija = null;

    @EmbeddedComponent
    private Combo comboZanr = null;

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
    private ShowImageComposite posterImage = null;

    @EmbeddedComponent
    private DynamicSelectorText tagSelector = null;

    @EmbeddedComponent
    private DynamicSelectorText diskSelector = null;

    private Optional<Film> activeFilm;

    @EmbeddedEventListener(event = SWT.Close)
    private void shellCloseListener() {
        if (offerMovieListForNewOrEditForm != null)
            offerMovieListForNewOrEditForm.cleanUp();
    }

    @EmbeddedEventListener(component = "btnSearchMovie", event = SWT.Selection)
    private void btnSearchMovieSelectionListener() {
        findMovieDialogDialogForm.setInitialText(comboNaziv.getText());
        findMovieDialogDialogForm.open(shell, new Runnable() {
            @Override
            public void run() {
                Optional<Movie> selectedMovie = findMovieDialogDialogForm.getSelectedMovie();
                if (selectedMovie.isPresent())
                    readFromMovie(selectedMovie.get());
            }
        });
    }

    @EmbeddedEventListener(component = "textPrevod", event = SWT.FocusIn)
    private void textPrevodFocusInListener() {
        if (textPrevod.getText().trim().equals(bundle.getString("newOrEdit.unknown")))
            textPrevod.setText("");
    }

    @EmbeddedEventListener(component = "textPrevod", event = SWT.FocusOut)
    private void textPrevodFocusOutListener() {
        if (textPrevod.getText().trim().equals(""))
            textPrevod.setText(bundle.getString("newOrEdit.unknown"));
    }

    @EmbeddedEventListener(component = "comboNaziv", event = SWT.Selection)
    private void comboNazivSelectionListener() {
        int index = comboNaziv.getSelectionIndex();
        if (index != -1) {
            readFromMovie((Movie) comboNaziv.getData(Integer.toString(index)));
        }
    }

    @EmbeddedEventListener(component = "btnNovMedij", event = SWT.Selection)
    private void btnNovMedijSelectionListener() {
        newMediumDialogDialogForm.open(shell, new Runnable() {
            @Override
            public void run() {
                refillCombos();
            }
        });
    }

    @EmbeddedEventListener(component = "btnPrihvati", event = SWT.Selection)
    private void btnPrihvatiSelectionListener() {
        //TODO: replace with Hibernate Validator (JSR 303 implementation)
        StringBuilder razlogOtkaza = new StringBuilder();
        if (diskSelector.getSelectedItemCount() == 0)
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

    @EmbeddedEventListener(component = "btnOdustani", event = SWT.Selection)
    private void btnOdustaniSelectionListener () {
        shell.close();
    }

    @EmbeddedEventListener(component = "imdbLink", event = SWT.Selection)
    private void imdbLinkSelectionListener() throws ApplicationException {
        try {
            Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(textImdbId.getText()));
        } catch (IOException e) {
            throw new ApplicationException("IO Exception when trying to browse to IMDB page", e);
        }
    }

    @EmbeddedEventListener(component = "textImdbId", event = SWT.Modify)
    private void textImdbIdSelectionListener() {
        if (!isFormTransformationComplete())
            return;
        imdbLink.setEnabled(IMDBUtil.isValidImdbId(textImdbId.getText()));
    }

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
            java.util.List<String> diskNames = Lists.newArrayList();
            for (Medij medij : film.getMedijs())
                diskNames.add(medij.toString());
            diskSelector.setSelectedItems(diskNames);
            java.util.List<String> tagNames = Lists.newArrayList();
            for (Tag tag : film.getTags())
                tagNames.add(tag.toString());
            tagSelector.setSelectedItems(tagNames);
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
            }
        }
    }

    protected void refillCombos() {
        String previousZanr = comboZanr.getText();
        String previousLokacija = comboLokacija.getText();
        comboZanr.removeAll();
        comboLokacija.removeAll();

        for (Zanr zanr : zanrRepository.getZanrs()) {
            comboZanr.add(zanr.getZanr());
            comboZanr.setData(zanr.getZanr(), zanr);
        }
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            comboLokacija.add(pozicija.getPozicija());
            comboLokacija.setData(pozicija.getPozicija(), pozicija);
        }
        List<String> allMediums = Lists.newArrayList();
        for (Medij medij : medijRepository.getMedijs()) {
            allMediums.add(medij.toString());
            diskSelector.setData(medij.toString(), medij);
        }
        diskSelector.setItems(allMediums);
        List<String> allTags = Lists.newArrayList();
        for (Tag tag : tagRepository.getTags()) {
            allTags.add(tag.toString());
            tagSelector.setData(tag.toString(), tag);
        }
        tagSelector.setItems(allTags);

        if (!previousZanr.isEmpty() && comboZanr.indexOf(previousZanr) != -1)
            comboZanr.select(comboZanr.indexOf(previousZanr));
        if (!previousLokacija.isEmpty() && comboLokacija.indexOf(previousLokacija) != -1)
            comboLokacija.select(comboLokacija.indexOf(previousLokacija));
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
        Set<Tag> tags = Sets.newHashSet();
        for (String tagName : tagSelector.getSelectedItems()) {
            Tag tag = (Tag) tagSelector.getData(tagName);
            tags.add(tag);
        }
        return tags;
    }

    private Set<Medij> getSelectedMediums() {
        Set<Medij> medijs = Sets.newHashSet();
        for (String medijName : diskSelector.getSelectedItems()) {
            Medij medij = (Medij) diskSelector.getData(medijName);
            medijs.add(medij);
        }
        return medijs;
    }

    @Override
    protected void onShellReady() {
        offerMovieListForNewOrEditForm.prepareFor(comboNaziv);
        reReadData();

        if (comboZanr.getItemCount() == 0 || diskSelector.getItemCount() == 0 || tipMedijaRepository.getTipMedijas().size() == 0) {
            MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
            box.setMessage(bundle.getString("newOrEdit.someBasicDomainElementsMissing"));
            box.setText(bundle.getString("global.information"));
            box.open();
            if (!shell.isDisposed())
                shell.close();
        }
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
