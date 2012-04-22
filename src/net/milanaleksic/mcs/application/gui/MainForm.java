package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.application.gui.helper.CoolMovieComposite;
import net.milanaleksic.mcs.application.gui.helper.MovieDetailsPanel;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionEvent;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionListener;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.export.*;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.metamodel.SingularAttribute;
import javax.swing.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

// do not allow java.awt.* to be added to import list because SWT's FileDialog
// will not work in some cases(https://bugs.eclipse.org/bugs/show_bug.cgi?id=349387)

public class MainForm extends Observable {

    private static final Logger log = Logger.getLogger(MainForm.class);

    // some designer constants

    private static final int GUI_FORM_DEFAULT_X = 20;
    private static final int GUI_FORM_DEFAULT_Y = 20;
    private static final int GUI_FORM_DEFAULT_WIDTH = 900;

    public static final int GUI_MOVIE_DETAILS_HEIGHT = 150;
    public static final int GUI_SEARCH_FILTER_HEIGHT = 25;

    @Inject
    private NewOrEditMovieDialogForm newOrEditMovieDialogForm;

    @Inject
    private SettingsDialogForm settingsDialogForm;

    @Inject
    private AboutDialogForm aboutDialogForm;

    @Inject
    private ApplicationManager applicationManager;

    @Inject
    private ZanrRepository zanrRepository;

    @Inject
    private TagRepository tagRepository;

    @Inject
    private TipMedijaRepository tipMedijaRepository;

    @Inject
    private PozicijaRepository pozicijaRepository;

    @Inject
    private DeleteMovieDialogForm deleteMovieDialogForm;

    @Inject
    private FilmRepository filmRepository;

    @Inject
    private ProgramArgsService programArgsService;

    @Inject
    private UnusedMediumsDialogForm unusedMediumsDialogForm;

    @Inject
    private UnmatchedMoviesDialogForm unmatchedMoviesDialogForm;

    @Inject
    private ThumbnailManager thumbnailManager;

    @Inject
    private WorkerManager workerManager;

    @Inject
    private Transformer transformer;

    private Shell shell;

    @EmbeddedComponent
    private Combo comboZanr = null;

    @EmbeddedComponent
    private Combo comboTipMedija = null;

    @EmbeddedComponent
    private Combo comboPozicija = null;

    @EmbeddedComponent
    private Combo comboTag = null;

    @EmbeddedComponent
    private Label labelCurrent = null;

    @EmbeddedComponent
    private Label labelFilter = null;

    @EmbeddedComponent
    private Canvas toolTicker = null;

    @EmbeddedComponent
    private Menu settingsPopupMenu = null;

    @EmbeddedComponent
    private CoolMovieComposite mainTable = null;

    @EmbeddedComponent
    private MovieDetailsPanel movieDetailsPanel = null;

    private ResourceBundle bundle;

    private CurrentViewState currentViewState = new CurrentViewState();

    @EmbeddedComponent
    private Composite searchFilterLineComposite = null;

    // private classes

    private static class CurrentViewState {

        private volatile Long activePage = 0L;
        private volatile Long showableCount = 0L;
        private volatile String filterText = "";
        private volatile int maxItemsPerPage;
        private SingularAttribute<Film, String> singularAttribute = Film_.medijListAsString;
        private boolean ascending = true;

        public boolean isAscending() {
            return ascending;
        }

        public void setAscending(boolean ascending) {
            this.ascending = ascending;
        }

        public String getFilterText() {
            return filterText;
        }

        public void setFilterText(String filterText) {
            this.filterText = filterText;
            activePage = 0L;
        }

        public Long getActivePage() {
            return activePage;
        }

        public void setActivePage(Long activePage) {
            this.activePage = activePage;
        }

        public Long getShowableCount() {
            return showableCount;
        }

        public void setShowableCount(Long showableCount) {
            this.showableCount = showableCount;
        }

        public void setMaxItemsPerPage(int maxItemsPerPage) {
            this.maxItemsPerPage = maxItemsPerPage;
        }

        public int getMaxItemsPerPage() {
            return maxItemsPerPage;
        }

        public void setCurrentSortOn(SingularAttribute<Film, String> singularAttribute) {
            this.singularAttribute = singularAttribute;
        }

        public SingularAttribute<Film, String> getSingularAttribute() {
            return singularAttribute;
        }
    }

    private class MainTableKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                String filterText = currentViewState.getFilterText();
                switch (e.keyCode) {
                    case SWT.PAGE_UP:
                        previousPage();
                        return;
                    case SWT.PAGE_DOWN:
                        nextPage();
                        return;
                    case SWT.ESC:
                        if (allFiltersAreCleared())
                            shell.close();
                        else
                            clearAllFilters();
                        return;
                    case SWT.BS:
                        if (!filterText.isEmpty()) {
                            currentViewState.setFilterText(filterText.substring(0, filterText.length() - 1));
                            doFillMainTable();
                        }
                        return;
                }
                if (!Character.isLetterOrDigit(e.character) && e.keyCode != java.awt.event.KeyEvent.VK_SPACE)
                    return;
                currentViewState.setFilterText(filterText + e.character);
                doFillMainTable();
            } finally {
                if (!shell.isDisposed()) {
                    setChanged();
                    MainForm.super.notifyObservers();
                }
            }
        }

        private void clearAllFilters() {
            comboPozicija.select(0);
            comboTipMedija.select(0);
            comboZanr.select(0);
            comboTag.select(0);
            currentViewState.setFilterText("");
            doFillMainTable();
        }

        private boolean allFiltersAreCleared() {
            String filterText = currentViewState.getFilterText();
            return filterText.isEmpty()
                    && comboPozicija.getSelectionIndex() == 0
                    && comboTipMedija.getSelectionIndex() == 0
                    && comboTag.getSelectionIndex() == 0
                    && comboZanr.getSelectionIndex() == 0;
        }

    }

    private class NextPageSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            nextPage();
            mainTable.setFocus();
        }

    }

    private class PreviousPageSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            previousPage();
            mainTable.setFocus();
        }

    }

    private class MainFormShellListener extends ShellAdapter {

        private boolean activated;

        @Override
        public void shellClosed(ShellEvent e) {
            ApplicationConfiguration.InterfaceConfiguration interfaceConfiguration = applicationManager.getApplicationConfiguration().getInterfaceConfiguration();
            interfaceConfiguration.setLastApplicationLocation(new net.milanaleksic.mcs.infrastructure.config.Rectangle(shell.getBounds()));
            interfaceConfiguration.setMaximized(shell.getMaximized());
        }

        @Override
        public void shellActivated(ShellEvent e) {
            if (activated)
                return;
            activated = true;
            ApplicationConfiguration.InterfaceConfiguration interfaceConfiguration = applicationManager.getApplicationConfiguration().getInterfaceConfiguration();
            net.milanaleksic.mcs.infrastructure.config.Rectangle lastApplicationLocation = interfaceConfiguration.getLastApplicationLocation();
            if (lastApplicationLocation == null)
                shell.setBounds(GUI_FORM_DEFAULT_X, GUI_FORM_DEFAULT_Y,
                        GUI_FORM_DEFAULT_WIDTH, Display.getCurrent().getPrimaryMonitor().getBounds().height - 80);
            else
                shell.setBounds(lastApplicationLocation.toSWTRectangle());
            shell.setMaximized(interfaceConfiguration.isMaximized());
            if (programArgsService.getProgramArgs().isNoInitialMovieListLoading())
                return;
            doFillMainTable();
            executeAdditionalLowPriorityPreparation();
        }
    }

    private class ComboRefreshAdapter extends SelectionAdapter {

        public void widgetSelected(SelectionEvent e) {
            Combo combo = (Combo) e.widget;
            if (combo.getSelectionIndex() == 1)
                combo.select(0);
            currentViewState.setActivePage(0L);
            doFillMainTable();
            mainTable.setFocus();
        }

    }

    private class SortingComboSelectionListener extends SelectionAdapter {

        @SuppressWarnings({"unchecked"})
        public void widgetSelected(SelectionEvent e) {
            Combo combo = (Combo) e.widget;
            int selectionIndex = combo.getSelectionIndex();
            SingularAttribute singularAttribute = ((SingularAttribute[]) combo.getData())[selectionIndex];
            currentViewState.setCurrentSortOn((SingularAttribute<Film, String>) singularAttribute);
            doFillMainTable();
            mainTable.setFocus();
        }

    }

    private class SortingCheckBoxSelectionListener extends SelectionAdapter {

        public void widgetSelected(SelectionEvent e) {
            Button ascending = (Button) e.widget;
            currentViewState.setAscending(ascending.getSelection());
            doFillMainTable();
            mainTable.setFocus();
        }

    }

    private class ToolExportSelectionAdapter extends SelectionAdapter {

        private String[] columnNames;

        public ToolExportSelectionAdapter() {
            columnNames = new String[]{
                    bundle.getString("main.medium"),
                    bundle.getString("main.movieTitle"),
                    bundle.getString("main.movieTitleTranslation"),
                    bundle.getString("main.genre"),
                    bundle.getString("main.location"),
                    bundle.getString("main.comment"),
            };
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            FileDialog dlg = new FileDialog(shell, SWT.SAVE);
            dlg.setFilterNames(new String[]{bundle.getString("main.export.html")});
            dlg.setFilterExtensions(new String[]{"*.htm"}); //NON-NLS
            final String targetFileForExport = dlg.open();
            if (targetFileForExport == null)
                return;
            if (log.isDebugEnabled())
                log.debug("Exporting to file \"" + targetFileForExport + "\""); //NON-NLS
            final Optional<Exporter> exporter = ExporterFactory.getInstance().getExporter(Files.getFileExtension(targetFileForExport));
            if (!exporter.isPresent()) {
                log.error("Exporting to the selected format is not supported"); //NON-NLS
                return;
            }
            getAllFilms(0, new Function<List<Film>, Void>() {
                @Override
                public Void apply(List<Film> filmList) {
                    checkNotNull(filmList);
                    final Film[] allFilms = filmList.toArray(new Film[filmList.size()]);
                    exporter.get().export(new ExporterSource() {

                        @Override
                        public String getTargetFile() {
                            return targetFileForExport;
                        }

                        @Override
                        public int getItemCount() {
                            return allFilms.length;
                        }

                        @Override
                        public int getColumnCount() {
                            return 5;
                        }

                        @Override
                        public String getData(int row, int column) {
                            if (row == -1)
                                return columnNames[column];
                            switch (column) {
                                case 0:
                                    return allFilms[row].getMedijListAsString();
                                case 1:
                                    return allFilms[row].getNazivfilma();
                                case 2:
                                    return allFilms[row].getPrevodnazivafilma();
                                case 3:
                                    return allFilms[row].getZanr().getZanr();
                                case 4:
                                    return allFilms[row].getPozicija();
                                default:
                                    return "";
                            }
                        }

                    });
                    return null;
                }
            });
        }

    }

    private class ToolEraseSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Optional<Film> selectedMovie = mainTable.getSelectedItem();
            if (!selectedMovie.isPresent())
                return;
            deleteMovieDialogForm.open(shell, selectedMovie.get(),
                    new Runnable() {

                        @Override
                        public void run() {
                            doFillMainTable();
                        }

                    });
        }

    }

    private class ToolSettingsSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (e.detail == SWT.ARROW) {
                ToolItem toolItem = (ToolItem) e.widget;
                ToolBar toolBar = toolItem.getParent();
                Rectangle rect = toolItem.getBounds();
                Point pt = new Point(rect.x, rect.y + rect.height);
                pt = toolBar.toDisplay(pt);
                settingsPopupMenu.setLocation(pt.x, pt.y);
                settingsPopupMenu.setVisible(true);
            } else {
                settingsDialogForm.open(shell, new Runnable() {
                    @Override
                    public void run() {
                        resetPozicije();
                        resetZanrovi();
                        resetMedija();
                        resetTagova();
                        doFillMainTable();
                    }
                });
            }
        }

    }

    private class ToolExitSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            shell.close();
        }

    }

    private class ToolAboutSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            aboutDialogForm.open(shell);
        }

    }

    private class ToolNewSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            newOrEditMovieDialogForm.open(shell, Optional.<Film>absent(), new Runnable() {
                @Override
                public void run() {
                    doFillMainTable();
                }
            });
        }

    }

    private class ShowUnusedMediumsForm extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
            unusedMediumsDialogForm.open(shell, new Runnable() {

                @Override
                public void run() {
                    doFillMainTable();
                }

            });
        }
    }

    private class ShowUnmatchedMoviesForm extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent selectionEvent) {
            unmatchedMoviesDialogForm.open(shell, new Runnable() {

                @Override
                public void run() {
                    doFillMainTable();
                }

            });
        }
    }


    // DESIGN

    public MainForm() {
        this.currentViewState = new CurrentViewState();
        this.addObserver(new Observer() {

            @Override
            public void update(Observable obs, Object arg) {
                if (currentViewState.getMaxItemsPerPage() > 0) {
                    long lowerBound = currentViewState.getActivePage() * currentViewState.getMaxItemsPerPage() + 1;
                    if (currentViewState.getShowableCount() == 0)
                        lowerBound = 0;
                    long upperBound = (currentViewState.getActivePage() + 1) * currentViewState.getMaxItemsPerPage();
                    if (upperBound > currentViewState.getShowableCount())
                        upperBound = currentViewState.getShowableCount();
                    labelCurrent.setText(lowerBound + "-" + upperBound + " (" + currentViewState.getShowableCount().toString() + ")");
                } else
                    labelCurrent.setText(currentViewState.getShowableCount().toString());
                refreshSearchFilterText();
            }

            private void refreshSearchFilterText() {
                labelFilter.setText(currentViewState.getFilterText());
                GridData layoutData = (GridData) searchFilterLineComposite.getLayoutData();
                int oldHeight = layoutData.heightHint;
                int newHeight = currentViewState.getFilterText().isEmpty() ? 0 : GUI_SEARCH_FILTER_HEIGHT;
                if (oldHeight != newHeight) {
                    layoutData.heightHint = newHeight;
                    searchFilterLineComposite.getParent().layout();
                }
            }

        });
    }

    public void open() {
        bundle = applicationManager.getMessagesBundle();
        checkCreated();
        shell.open();
        mainTable.setFocus();
    }

    private void checkCreated() {
        if (shell != null)
            return;
        createShell();
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public boolean isDisposed() {
        return shell.isDisposed();
    }

    private void createShell() {
        try {
            TransformationContext transformationContext = transformer.fillManagedForm(this);
            shell = transformationContext.getShell();
            setupHeader(transformationContext);
            setupCenterComposite(transformationContext);
            setupStatusBar(transformationContext);
            shell.addShellListener(new MainFormShellListener());
        } catch (TransformerException e) {
            log.error("Main form creation failure!", e);
            javax.swing.JOptionPane.showMessageDialog(null,
                    String.format(bundle.getString("global.applicationErrorTemplate"), e.getClass().getCanonicalName(), e.getMessage()),
                    bundle.getString("global.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupHeader(TransformationContext transformationContext) {
        transformationContext.<ToolItem>getMappedObject("toolNew").get().addSelectionListener(
                new ToolNewSelectionAdapter());
        transformationContext.<ToolItem>getMappedObject("toolErase").get().addSelectionListener(
                new ToolEraseSelectionAdapter());
        transformationContext.<ToolItem>getMappedObject("toolExport").get().addSelectionListener(
                new ToolExportSelectionAdapter());
        transformationContext.<ToolItem>getMappedObject("toolSettings").get().addSelectionListener(
                new ToolSettingsSelectionAdapter());
        transformationContext.<ToolItem>getMappedObject("toolAbout").get().addSelectionListener(
                new ToolAboutSelectionAdapter());
        transformationContext.<ToolItem>getMappedObject("toolExit").get().addSelectionListener(
                new ToolExitSelectionAdapter());

        transformationContext.<MenuItem>getMappedObject("settingsMenuItem").get().addSelectionListener(
                new ToolSettingsSelectionAdapter());
        transformationContext.<MenuItem>getMappedObject("findUnusedMediums").get().addSelectionListener(
                new ShowUnusedMediumsForm());
        transformationContext.<MenuItem>getMappedObject("findUmatchedImdbMovies").get().addSelectionListener(
                new ShowUnmatchedMoviesForm());

        SWTUtil.addImagePaintListener(toolTicker, "/net/milanaleksic/mcs/application/res/db_find.png"); //NON-NLS
    }

    private void setupCenterComposite(TransformationContext transformationContext) {
        mainTable.addKeyListener(new MainTableKeyAdapter());
        mainTable.addMovieSelectionListener(new MovieSelectionListener() {

            @Override
            public void movieSelected(MovieSelectionEvent e) {
                if (e.film.isPresent())
                    movieDetailsPanel.showDataForMovie(Optional.of(filmRepository.getCompleteFilm(e.film.get())));
                GridData layoutData = (GridData) movieDetailsPanel.getLayoutData();
                int currentHeight = layoutData.heightHint;
                int newHeight = e.film.isPresent() ? GUI_MOVIE_DETAILS_HEIGHT : 0;
                if (currentHeight != newHeight) {
                    layoutData.heightHint = newHeight;
                    mainTable.getParent().getParent().layout();
                }
            }

            @Override
            public void movieDetailsSelected(MovieSelectionEvent e) {
                newOrEditMovieDialogForm.open(shell, Optional.of(filmRepository.getCompleteFilm(e.film.get())),
                        new Runnable() {
                            @Override
                            public void run() {
                                doFillMainTable();
                            }
                        });
            }

        });
        transformationContext.<ScrolledComposite>getMappedObject("mainTableWrapper")
                .get().getVerticalBar().setIncrement(10);
        movieDetailsPanel.prepareLayout();
    }

    private void setupStatusBar(TransformationContext transformationContext) {
        transformationContext.<Button>getMappedObject("btnPrevPage").get().addSelectionListener(
                new PreviousPageSelectionAdapter());
        transformationContext.<Button>getMappedObject("btnNextPage").get().addSelectionListener(
                new NextPageSelectionAdapter());
        Combo comboSort = transformationContext.<Combo>getMappedObject("comboSort").get();
        comboSort.setItems(new String[]{
                bundle.getString("main.medium"),
                bundle.getString("main.movieTitle"),
                bundle.getString("main.movieTitleTranslation"),
        });
        comboSort.setData(new SingularAttribute[]{
                Film_.medijListAsString,
                Film_.nazivfilma,
                Film_.prevodnazivafilma
        });
        comboSort.select(0);
        comboSort.addSelectionListener(new SortingComboSelectionListener());
        transformationContext.<Button>getMappedObject("cbAscending").get().addSelectionListener(
                new SortingCheckBoxSelectionListener());
        comboTipMedija.addSelectionListener(new ComboRefreshAdapter());
        resetMedija();
        comboPozicija.addSelectionListener(new ComboRefreshAdapter());
        resetPozicije();
        comboZanr.addSelectionListener(new ComboRefreshAdapter());
        resetZanrovi();
        comboTag.addSelectionListener(new ComboRefreshAdapter());
        resetTagova();
    }

    private void resetZanrovi() {
        comboZanr.setItems(new String[]{});
        comboZanr.add(bundle.getString("main.allGenres"));
        comboZanr.add("-----------");
        workerManager.submitLongTaskWithResultProcessingInSWTThread(
                new Callable<List<Zanr>>() {
                    @Override
                    public List<Zanr> call() throws Exception {
                        return zanrRepository.getZanrs();
                    }
                },
                new Function<List<Zanr>, Void>() {
                    @Override
                    public Void apply(List<Zanr> zanrs) {
                        checkNotNull(zanrs);
                        int iter = 2; // each item except first 2 will have Zanr object as data
                        for (Zanr zanr : zanrs) {
                            comboZanr.setData(Integer.toString(iter++), zanr);
                            comboZanr.add(zanr.toString());
                        }
                        comboZanr.select(0);
                        return null;
                    }
                }
        );
    }

    private void resetPozicije() {
        comboPozicija.setItems(new String[]{});
        comboPozicija.add(bundle.getString("main.anyLocation"));
        comboPozicija.add("-----------");
        workerManager.submitLongTaskWithResultProcessingInSWTThread(new Callable<List<Pozicija>>() {
                    @Override
                    public List<Pozicija> call() throws Exception {
                        return pozicijaRepository.getPozicijas();
                    }
                },
                new Function<List<Pozicija>, Void>() {
                    @Override
                    public Void apply(List<Pozicija> pozicijas) {
                        checkNotNull(pozicijas);
                        for (Pozicija pozicija : pozicijas) {
                            comboPozicija.setData(Integer.toString(comboPozicija.getItemCount()), pozicija);
                            comboPozicija.add(pozicija.toString());
                        }
                        comboPozicija.select(0);
                        return null;
                    }
                }
        );
    }

    private void resetMedija() {
        comboTipMedija.setItems(new String[]{});
        comboTipMedija.add(bundle.getString("main.allMediums"));
        comboTipMedija.add("-----------");
        workerManager.submitLongTaskWithResultProcessingInSWTThread(new Callable<List<TipMedija>>() {
                    @Override
                    public List<TipMedija> call() throws Exception {
                        return tipMedijaRepository.getTipMedijas();
                    }
                },
                new Function<List<TipMedija>, Void>() {
                    @Override
                    public Void apply(List<TipMedija> tipMedijas) {
                        checkNotNull(tipMedijas);
                        for (TipMedija tip : tipMedijas) {
                            comboTipMedija.setData(Integer.toString(comboTipMedija.getItemCount()), tip);
                            comboTipMedija.add(tip.toString());
                        }
                        comboTipMedija.select(0);
                        return null;
                    }
                }
        );
    }

    private void resetTagova() {
        comboTag.setItems(new String[]{});
        comboTag.add(bundle.getString("main.anyTags"));
        comboTag.add("-----------");
        workerManager.submitLongTaskWithResultProcessingInSWTThread(new Callable<List<Tag>>() {
                    @Override
                    public List<Tag> call() throws Exception {
                        return tagRepository.getTags();
                    }
                },
                new Function<List<Tag>, Void>() {
                    @Override
                    public Void apply(List<Tag> tags) {
                        checkNotNull(tags);
                        for (Tag tag : tags) {
                            comboTag.setData(Integer.toString(comboTag.getItemCount()), tag);
                            comboTag.add(tag.toString());
                        }
                        comboTag.select(0);
                        return null;
                    }
                }
        );
    }


    // LOGIC


    private void doFillMainTable() {
        toolTicker.setVisible(true);
        toolTicker.update();
        getAllFilms(applicationManager.getUserConfiguration().getElementsPerPage(), new Function<List<Film>, Void>() {
            @Override
            public Void apply(@Nullable List<Film> films) {
                mainTable.setMovies(Optional.fromNullable(films));

                MainForm.this.setChanged();
                MainForm.super.notifyObservers();

                toolTicker.setVisible(false);

                return null;
            }
        });
    }

    private void nextPage() {
        if (currentViewState.getMaxItemsPerPage() > 0)
            if (currentViewState.getMaxItemsPerPage() * (currentViewState.getActivePage() + 1) > currentViewState.getShowableCount())
                return;
        currentViewState.setActivePage(currentViewState.getActivePage() + 1);
        doFillMainTable();
    }

    private void previousPage() {
        if (currentViewState.getActivePage() == 0)
            return;
        currentViewState.setActivePage(currentViewState.getActivePage() - 1);
        doFillMainTable();
    }

    public void getAllFilms(final int maxItems, Function<List<Film>, Void> whatToDoWithFilms) {
        final Zanr zanrFilter = (Zanr) comboZanr.getData(Integer.toString(comboZanr.getSelectionIndex()));
        final TipMedija tipMedijaFilter = (TipMedija) comboTipMedija.getData(Integer.toString(comboTipMedija.getSelectionIndex()));
        final Pozicija pozicijaFilter = (Pozicija) comboPozicija.getData(Integer.toString(comboPozicija.getSelectionIndex()));
        final Tag tagFilter = (Tag) comboTag.getData(Integer.toString(comboTag.getSelectionIndex()));
        final String filterText = currentViewState.getFilterText().isEmpty() ?
                null :
                '%' + currentViewState.getFilterText() + '%';
        final int startFrom = currentViewState.getActivePage().intValue() * maxItems;

        workerManager.submitLongTaskWithResultProcessingInSWTThread(
                new Callable<List<Film>>() {
                    @Override
                    @MethodTiming(name = "getAllFilms")
                    public List<Film> call() throws Exception {
                        currentViewState.setMaxItemsPerPage(maxItems);
                        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(startFrom, maxItems,
                                Optional.fromNullable(zanrFilter),
                                Optional.fromNullable(tipMedijaFilter),
                                Optional.fromNullable(pozicijaFilter),
                                Optional.fromNullable(tagFilter),
                                Optional.fromNullable(filterText),
                                currentViewState.getSingularAttribute(),
                                currentViewState.isAscending());
                        currentViewState.setShowableCount(filmsWithCount.count);
                        return filmsWithCount.films;
                    }
                }, whatToDoWithFilms);
    }

    private void executeAdditionalLowPriorityPreparation() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                doExecuteAdditionalLowPriorityPreparation();
            }
        }.start();
    }

    @MethodTiming
    private void doExecuteAdditionalLowPriorityPreparation() {
        thumbnailManager.preCacheThumbnails();
    }

}