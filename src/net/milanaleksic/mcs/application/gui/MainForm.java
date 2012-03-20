package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionEvent;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionListener;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.export.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.List;
import java.util.*;

// do not allow java.awt.* to be added to import list because SWT's FileDialog
// will not work in some cases(https://bugs.eclipse.org/bugs/show_bug.cgi?id=349387)

public class MainForm extends Observable {

    private static final Logger log = Logger.getLogger(MainForm.class);

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

    private ResourceBundle bundle = null;

    private final static String titleConst = "Movie Catalog System (C) by Milan.Aleksic@gmail.com"; //NON-NLS

    private Shell sShell = null;
    private Combo comboZanr = null;
    private Combo comboTipMedija = null;
    private Combo comboPozicija = null;
    private Composite panCombos = null;
    private Label labelFilter = null;
    private Label labelCurrent = null;
    private Canvas toolTicker = null;
    private Composite wrapperDataInfo = null;
    private Menu settingsPopupMenu = null;

    private CurrentViewState currentViewState = new CurrentViewState();

    private CoolMovieComposite mainTable;

    private MovieDetailsPanel movieDetailsPanel;

    // private classes

    private static class CurrentViewState {

        private volatile Long activePage = 0L;
        private volatile Long showableCount = 0L;
        private volatile String filterText = null;
        private volatile int maxItemsPerPage;

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
    }

    private class MainTableKeyAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
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
                        sShell.dispose();
                    else
                        clearAllFilters();
                    return;
                case SWT.BS:
                    if (filterText != null && filterText.length() > 0) {
                        currentViewState.setFilterText(filterText.substring(0, filterText.length() - 1));
                        doFillMainTable();
                    }
                    return;
            }
            if (!Character.isLetterOrDigit(e.character) && e.keyCode != java.awt.event.KeyEvent.VK_SPACE)
                return;
            if (filterText == null)
                currentViewState.setFilterText("" + e.character);
            else
                currentViewState.setFilterText(filterText + e.character);
            doFillMainTable();
        }

        private void clearAllFilters() {
            comboPozicija.select(0);
            comboTipMedija.select(0);
            comboZanr.select(0);
            currentViewState.setFilterText("");
            doFillMainTable();
        }

        private boolean allFiltersAreCleared() {
            String filterText = currentViewState.getFilterText();
            return (filterText == null || filterText.length() == 0)
                    && comboPozicija.getSelectionIndex() == 0
                    && comboTipMedija.getSelectionIndex() == 0
                    && comboZanr.getSelectionIndex() == 0;
        }

    }

    private class NextPageSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            nextPage();
        }

    }

    private class PreviousPageSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            previousPage();
        }

    }

    private class MainFormShellListener extends ShellAdapter {

        @Override
        public void shellActivated(ShellEvent e) {
            sShell.removeShellListener(this);
            if (programArgsService.getProgramArgs().isNoInitialMovieListLoading())
                return;
            doFillMainTable();
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
            FileDialog dlg = new FileDialog(sShell, SWT.SAVE);
            dlg.setFilterNames(new String[]{bundle.getString("main.export.html")});
            dlg.setFilterExtensions(new String[]{"*.htm"}); //NON-NLS
            final String targetFileForExport = dlg.open();
            if (targetFileForExport == null)
                return;
            String ext = targetFileForExport.substring(targetFileForExport.lastIndexOf('.') + 1);
            if (log.isDebugEnabled())
                log.debug("Exporting to file \"" + targetFileForExport + "\""); //NON-NLS
            Exporter exporter = ExporterFactory.getInstance().getExporter(ext);
            if (exporter == null) {
                log.error("Exporting to the selected format is not supported"); //NON-NLS
                return;
            }
            List<Film> filmList = getAllFilms(0);
            final Film[] allFilms = filmList.toArray(new Film[filmList.size()]);
            exporter.export(new ExporterSource() {

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
        }

    }

    private class ToolEraseSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Film selectedMovie = mainTable.getSelectedItem();
            if (selectedMovie == null)
                return;
            deleteMovieDialogForm.open(sShell, selectedMovie,
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
                settingsDialogForm.open(sShell, new Runnable() {
                    @Override
                    public void run() {
                        resetPozicije();
                        resetZanrovi();
                        resetMedija();
                        doFillMainTable();
                    }
                });
            }
        }

    }

    private class ToolExitSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            sShell.dispose();
        }

    }

    private class ToolAboutSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            aboutDialogForm.open(sShell);
        }

    }

    private class ToolNewSelectionAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            newOrEditMovieDialogForm.open(sShell, null, new Runnable() {
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
            unusedMediumsDialogForm.open(sShell, new Runnable() {

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
            unmatchedMoviesDialogForm.open(sShell, new Runnable() {

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
                String filter = currentViewState.getFilterText();
                labelFilter.setText(filter == null ? "" : filter);
                wrapperDataInfo.pack();
            }

        });
    }

    public void open() {
        bundle = applicationManager.getMessagesBundle();
        checkCreated();
        sShell.open();
        mainTable.setFocus();
    }

    private void checkCreated() {
        if (sShell != null)
            return;
        createSShell();
        SWTUtil.setImageOnTarget(sShell, "/net/milanaleksic/mcs/application/res/database-64.png"); //NON-NLS
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public boolean isDisposed() {
        return sShell.isDisposed();
    }

    private void createSShell() {
        sShell = new Shell();
        sShell.setText(titleConst);
        sShell.setMaximized(false);
        sShell.setBounds(20, 20, 800, Display.getCurrent().getPrimaryMonitor().getBounds().height - 80);
        createToolTicker();
        createPanCombos();
        createToolBar();
        sShell.setLayout(new GridLayout(3, false));
        createCenterComposite();
        createStatusBar();
        createSettingsPopupMenu();
        sShell.addShellListener(new MainFormShellListener());
    }

    private void createCenterComposite() {
        Composite centerComposite = new Composite(sShell, SWT.NONE);
        centerComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1));
        centerComposite.setLayout(new GridLayout(1, false));

        ScrolledComposite scrolledComposite = new ScrolledComposite(centerComposite, SWT.V_SCROLL | SWT.NO);
        mainTable = new CoolMovieComposite(scrolledComposite, SWT.NONE, thumbnailManager);
        mainTable.addMovieSelectionListener(new MovieSelectionListener() {

            @Override
            public void movieSelected(MovieSelectionEvent e) {
                if (e.film == null)
                    movieDetailsPanel.clearData();
                else
                    movieDetailsPanel.showDataForMovie(e.film);
            }

            @Override
            public void movieDetailsSelected(MovieSelectionEvent e) {
                newOrEditMovieDialogForm.open(sShell, filmRepository.getCompleteFilm(e.film),
                        new Runnable() {
                            @Override
                            public void run() {
                                doFillMainTable();
                            }
                        });
            }

        });
        mainTable.addKeyListener(new MainTableKeyAdapter());
        scrolledComposite.setContent(mainTable);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.getVerticalBar().setIncrement(10);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolledComposite.setBackground(sShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        movieDetailsPanel = new MovieDetailsPanel(centerComposite, SWT.BORDER, bundle, thumbnailManager);
        GridData layoutData = new GridData(SWT.FILL, SWT.END, true, false);
        layoutData.heightHint = 150;
        movieDetailsPanel.setLayoutData(layoutData);
    }

    private void createPanCombos() {
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.END;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 1;
        panCombos = new Composite(sShell, SWT.NONE);
        panCombos.setLayoutData(gridData2);
        panCombos.setLayout(gridLayout1);
        createComboTipMedija();
        createComboPozicija();
        createComboZanr();
    }

    private void createComboZanr() {
        GridData gridData5 = new GridData();
        gridData5.widthHint = 80;
        comboZanr = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboZanr.setLayoutData(gridData5);
        comboZanr.setVisibleItemCount(16);
        comboZanr.addSelectionListener(new ComboRefreshAdapter());
        resetZanrovi();
    }

    private void createComboTipMedija() {
        GridData gridData1 = new GridData();
        gridData1.widthHint = 80;
        comboTipMedija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboTipMedija.setLayoutData(gridData1);
        comboTipMedija.setVisibleItemCount(8);
        comboTipMedija.addSelectionListener(new ComboRefreshAdapter());
        resetMedija();
    }

    private void createComboPozicija() {
        GridData gridData3 = new GridData();
        gridData3.widthHint = 80;
        comboPozicija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboPozicija.setLayoutData(gridData3);
        comboPozicija.setVisibleItemCount(8);
        comboPozicija.addSelectionListener(new ComboRefreshAdapter());
        resetPozicije();
    }

    private void resetZanrovi() {
        comboZanr.setItems(new String[]{});
        comboZanr.add(bundle.getString("main.allGenres"));
        comboZanr.add("-----------");
        int iter = 2;
        for (Zanr zanr : zanrRepository.getZanrs()) {
            comboZanr.setData(Integer.toString(iter++), zanr);
            comboZanr.add(zanr.toString());
        }
        comboZanr.select(0);
    }

    private void resetPozicije() {
        comboPozicija.setItems(new String[]{});
        comboPozicija.add(bundle.getString("main.anyLocation"));
        comboPozicija.add("-----------");
        for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
            comboPozicija.setData(Integer.toString(comboPozicija.getItemCount()), pozicija);
            comboPozicija.add(pozicija.toString());
        }
        comboPozicija.select(0);
    }

    private void resetMedija() {
        comboTipMedija.setItems(new String[]{});
        comboTipMedija.add(bundle.getString("main.allMediums"));
        comboTipMedija.add("-----------");
        for (TipMedija tip : tipMedijaRepository.getTipMedijas()) {
            comboTipMedija.setData(Integer.toString(comboTipMedija.getItemCount()), tip);
            comboTipMedija.add(tip.toString());
        }
        comboTipMedija.select(0);
    }

    private void createToolBar() {
        final ToolBar toolBar = new ToolBar(sShell, SWT.FLAT);
        toolBar.setBounds(new Rectangle(11, 50, 4, 50));
        toolBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
        ToolItem toolNew = new ToolItem(toolBar, SWT.PUSH);
        toolNew.setText(bundle.getString("global.newMovie"));
        SWTUtil.setImageOnTarget(toolNew, "/net/milanaleksic/mcs/application/res/media.png"); //NON-NLS
        ToolItem toolErase = new ToolItem(toolBar, SWT.PUSH);
        toolErase.setText(bundle.getString("global.deleteMovie"));
        SWTUtil.setImageOnTarget(toolErase, "/net/milanaleksic/mcs/application/res/alert.png"); //NON-NLS
        ToolItem toolExport = new ToolItem(toolBar, SWT.PUSH);
        SWTUtil.setImageOnTarget(toolExport, "/net/milanaleksic/mcs/application/res/folder_outbox.png"); //NON-NLS
        toolExport.setText(bundle.getString("main.export"));
        final ToolItem toolSettings = new ToolItem(toolBar, SWT.DROP_DOWN);
        SWTUtil.setImageOnTarget(toolSettings, "/net/milanaleksic/mcs/application/res/advancedsettings.png"); //NON-NLS
        toolSettings.setWidth(90);
        toolSettings.setText(bundle.getString("main.settings"));
        new ToolItem(toolBar, SWT.SEPARATOR);
        ToolItem toolAbout = new ToolItem(toolBar, SWT.PUSH);
        toolAbout.setText(bundle.getString("global.aboutProgram"));
        SWTUtil.setImageOnTarget(toolAbout, "/net/milanaleksic/mcs/application/res/jabber_protocol.png"); //NON-NLS
        new ToolItem(toolBar, SWT.SEPARATOR);
        ToolItem toolExit = new ToolItem(toolBar, SWT.PUSH);
        toolExit.setText(bundle.getString("main.exit"));
        SWTUtil.setImageOnTarget(toolExit, "/net/milanaleksic/mcs/application/res/shutdown.png"); //NON-NLS

        toolNew.addSelectionListener(new ToolNewSelectionAdapter());
        toolErase.addSelectionListener(new ToolEraseSelectionAdapter());
        toolExport.addSelectionListener(new ToolExportSelectionAdapter());
        toolSettings.addSelectionListener(new ToolSettingsSelectionAdapter());
        toolAbout.addSelectionListener(new ToolAboutSelectionAdapter());
        toolExit.addSelectionListener(new ToolExitSelectionAdapter());
    }

    private void createStatusBar() {
        ToolBar statusBar = new ToolBar(sShell, SWT.NONE);
        statusBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false, 1, 1));
        ToolItem toolPrevPage = new ToolItem(statusBar, SWT.PUSH);
        toolPrevPage.setText("<<");
        toolPrevPage.addSelectionListener(new PreviousPageSelectionAdapter());
        ToolItem toolNextPage = new ToolItem(statusBar, SWT.PUSH);
        toolNextPage.setText(">>");
        toolNextPage.addSelectionListener(new NextPageSelectionAdapter());

        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.horizontalSpan = 2;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 4;
        wrapperDataInfo = new Composite(sShell, SWT.NONE);
        wrapperDataInfo.setLayoutData(gridData2);
        wrapperDataInfo.setLayout(gridLayout1);

        Label labelCurrentDesc = new Label(wrapperDataInfo, SWT.NONE);
        labelCurrentDesc.setText(bundle.getString("main.filterExtracted"));
        labelCurrent = new Label(wrapperDataInfo, SWT.NONE);
        labelCurrent.setText("0");
        Label labelFilterDesc = new Label(wrapperDataInfo, SWT.NONE);
        labelFilterDesc.setText(bundle.getString("main.activeFilter"));
        labelFilter = new Label(wrapperDataInfo, SWT.NONE);
        labelFilter.setText("");
    }

    private void createToolTicker() {
        GridData tickerGridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
        tickerGridData.widthHint = 24;
        tickerGridData.heightHint = 24;
        toolTicker = new Canvas(sShell, SWT.NONE);
        toolTicker.setLayoutData(tickerGridData);
        SWTUtil.addImagePaintListener(toolTicker, "/net/milanaleksic/mcs/application/res/db_find.png"); //NON-NLS
    }

    private void createSettingsPopupMenu() {
        settingsPopupMenu = new Menu(sShell, SWT.POP_UP);
        MenuItem settingsMenuItem = new MenuItem(settingsPopupMenu, SWT.PUSH);
        settingsMenuItem.setText(bundle.getString("main.settings"));
        settingsMenuItem.addSelectionListener(new ToolSettingsSelectionAdapter());
        settingsPopupMenu.setDefaultItem(settingsMenuItem);
        MenuItem findUnusedMediums = new MenuItem(settingsPopupMenu, SWT.PUSH);
        findUnusedMediums.setText(bundle.getString("main.findUnusedMediums"));
        findUnusedMediums.addSelectionListener(new ShowUnusedMediumsForm());
        MenuItem findUmatchedImdbMovies = new MenuItem(settingsPopupMenu, SWT.PUSH);
        findUmatchedImdbMovies.setText(bundle.getString("main.findUmatchedImdbMovies"));
        findUmatchedImdbMovies.addSelectionListener(new ShowUnmatchedMoviesForm());
    }


    // LOGIC


    @MethodTiming
    public void doFillMainTable() {
        if (toolTicker != null) {
            toolTicker.setVisible(true);
            toolTicker.update();
        }
        mainTable.setMovies(getAllFilms(applicationManager.getUserConfiguration().getElementsPerPage()));

        setChanged();
        super.notifyObservers();

        if (toolTicker != null)
            toolTicker.setVisible(false);
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

    public List<Film> getAllFilms(int maxItems) {
        Zanr zanrFilter = (Zanr) comboZanr.getData(Integer.toString(comboZanr.getSelectionIndex()));
        TipMedija tipMedijaFilter = (TipMedija) comboTipMedija.getData(Integer.toString(comboTipMedija.getSelectionIndex()));
        Pozicija pozicijaFilter = (Pozicija) comboPozicija.getData(Integer.toString(comboPozicija.getSelectionIndex()));
        String filterText = currentViewState.getFilterText() == null ?
                null :
                '%' + currentViewState.getFilterText() + '%';
        int startFrom = currentViewState.getActivePage().intValue() * maxItems;
        currentViewState.setMaxItemsPerPage(maxItems);

        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(startFrom, maxItems,
                zanrFilter, tipMedijaFilter, pozicijaFilter, filterText);
        currentViewState.setShowableCount(filmsWithCount.count);

        return filmsWithCount.films;
    }

}