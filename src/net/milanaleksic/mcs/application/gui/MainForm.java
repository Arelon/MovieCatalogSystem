package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import net.milanaleksic.mcs.application.ApplicationManager;
import net.milanaleksic.mcs.application.config.ProgramArgsService;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionEvent;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionListener;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.config.ApplicationConfiguration;
import net.milanaleksic.mcs.infrastructure.export.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;

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

    @Inject
    private WorkerManager workerManager;

    private ResourceBundle bundle = null;

    private final static String titleConst = "Movie Catalog System (C) by Milan.Aleksic@gmail.com"; //NON-NLS

    private Shell sShell = null;
    private Combo comboZanr = null;
    private Combo comboTipMedija = null;
    private Combo comboPozicija = null;
    private Label labelFilter = null;
    private Label labelFilterDesc = null;
    private Label labelCurrent = null;
    private Canvas toolTicker = null;
    private Menu settingsPopupMenu = null;
    private Composite statusBar = null;
    private CurrentViewState currentViewState = new CurrentViewState();

    private CoolMovieComposite mainTable;

    private MovieDetailsPanel movieDetailsPanel;

    // private classes

    private static class CurrentViewState {

        private volatile Long activePage = 0L;
        private volatile Long showableCount = 0L;
        private volatile String filterText = null;
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
                            sShell.close();
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
            } finally {
                if (!sShell.isDisposed()) {
                    setChanged();
                    MainForm.super.notifyObservers();
                }
            }
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
            interfaceConfiguration.setLastApplicationLocation(new net.milanaleksic.mcs.infrastructure.config.Rectangle(sShell.getBounds()));
            interfaceConfiguration.setMaximized(sShell.getMaximized());
        }

        @Override
        public void shellActivated(ShellEvent e) {
            if (activated)
                return;
            activated = true;
            ApplicationConfiguration.InterfaceConfiguration interfaceConfiguration = applicationManager.getApplicationConfiguration().getInterfaceConfiguration();
            net.milanaleksic.mcs.infrastructure.config.Rectangle lastApplicationLocation = interfaceConfiguration.getLastApplicationLocation();
            if (lastApplicationLocation == null)
                sShell.setBounds(20, 20, 800, Display.getCurrent().getPrimaryMonitor().getBounds().height - 80);
            else
                sShell.setBounds(lastApplicationLocation.toSWTRectangle());
            sShell.setMaximized(interfaceConfiguration.isMaximized());
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
            FileDialog dlg = new FileDialog(sShell, SWT.SAVE);
            dlg.setFilterNames(new String[]{bundle.getString("main.export.html")});
            dlg.setFilterExtensions(new String[]{"*.htm"}); //NON-NLS
            final String targetFileForExport = dlg.open();
            if (targetFileForExport == null)
                return;
            String ext = targetFileForExport.substring(targetFileForExport.lastIndexOf('.') + 1);
            if (log.isDebugEnabled())
                log.debug("Exporting to file \"" + targetFileForExport + "\""); //NON-NLS
            final Exporter exporter = ExporterFactory.getInstance().getExporter(ext);
            if (exporter == null) {
                log.error("Exporting to the selected format is not supported"); //NON-NLS
                return;
            }
            getAllFilms(0, new Function<List<Film>, Void>() {
                @Override
                public Void apply(@Nullable List<Film> filmList) {
                    if (filmList == null)
                        return null;
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
                    return null;
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
            sShell.close();
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
                labelFilterDesc.setVisible(filter != null && !filter.isEmpty());
                statusBar.layout();
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
        sShell.setLayout(new GridLayout(1, false));

        createHeader();
        createCenterComposite();
        createStatusBar();

        createSettingsPopupMenu();
        sShell.addShellListener(new MainFormShellListener());
    }

    private void createHeader() {
        Composite header = new Composite(sShell, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        header.setLayout(layout);
        header.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        createToolTicker(header);
        createPanCombos(header);
        createToolBar(header);
    }

    private void createToolTicker(Composite header) {
        GridData tickerGridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
        tickerGridData.widthHint = 24;
        tickerGridData.heightHint = 24;
        toolTicker = new Canvas(header, SWT.NONE);
        toolTicker.setLayoutData(tickerGridData);
        SWTUtil.addImagePaintListener(toolTicker, "/net/milanaleksic/mcs/application/res/db_find.png"); //NON-NLS
    }

    private void createPanCombos(Composite header) {
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.END;
        Composite panCombos = new Composite(header, SWT.NONE);
        panCombos.setLayoutData(gridData2);
        panCombos.setLayout(new GridLayout(1, false));
        createComboTipMedija(panCombos);
        createComboPozicija(panCombos);
        createComboZanr(panCombos);
    }

    private void createToolBar(Composite header) {
        final ToolBar toolBar = new ToolBar(header, SWT.FLAT | SWT.WRAP);
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

    private void createCenterComposite() {
        Composite centerComposite = new Composite(sShell, SWT.NONE);
        centerComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
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

        movieDetailsPanel = new MovieDetailsPanel(centerComposite, SWT.NONE, bundle, thumbnailManager);
        GridData layoutData = new GridData(SWT.FILL, SWT.END, true, false);
        layoutData.heightHint = 150;
        movieDetailsPanel.setLayoutData(layoutData);
    }

    private void createComboZanr(Composite panCombos) {
        GridData gridData5 = new GridData();
        gridData5.widthHint = 80;
        comboZanr = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboZanr.setLayoutData(gridData5);
        comboZanr.setVisibleItemCount(16);
        comboZanr.addSelectionListener(new ComboRefreshAdapter());
        resetZanrovi();
    }

    private void createComboTipMedija(Composite panCombos) {
        GridData gridData1 = new GridData();
        gridData1.widthHint = 80;
        comboTipMedija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        comboTipMedija.setLayoutData(gridData1);
        comboTipMedija.setVisibleItemCount(8);
        comboTipMedija.addSelectionListener(new ComboRefreshAdapter());
        resetMedija();
    }

    private void createComboPozicija(Composite panCombos) {
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

    private void createStatusBar() {
        statusBar = new Composite(sShell, SWT.BORDER);
        statusBar.setLayoutData(new GridData(GridData.FILL, GridData.END, true, false));

        GridLayout layout = new GridLayout(8, false);
        statusBar.setLayout(layout);
        Button btnPrevPage = new Button(statusBar, SWT.PUSH);
        btnPrevPage.setText("<<");
        btnPrevPage.addSelectionListener(new PreviousPageSelectionAdapter());

        labelCurrent = new Label(statusBar, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        layoutData.widthHint = 90;
        labelCurrent.setLayoutData(layoutData);
        labelCurrent.setAlignment(SWT.CENTER);
        labelCurrent.setText("0");

        Button btnNextPage = new Button(statusBar, SWT.PUSH);
        btnNextPage.setText(">>");
        btnNextPage.addSelectionListener(new NextPageSelectionAdapter());

        labelFilterDesc = new Label(statusBar, SWT.NONE);
        labelFilterDesc.setText(bundle.getString("main.activeFilter"));
        labelFilterDesc.setVisible(false);
        labelFilter = new Label(statusBar, SWT.NONE);
        FontData systemFontData = SWTUtil.getSystemFontData();
        Font systemFont = new Font(sShell.getDisplay(), systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD);
        labelFilter.setFont(systemFont);
        labelFilter.setText("");

        Label sortLabel = new Label(statusBar, SWT.NONE);
        sortLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        sortLabel.setText(bundle.getString("main.sortOn"));
        sortLabel.setAlignment(SWT.RIGHT);
        Combo combo = new Combo(statusBar, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        combo.setItems(new String[]{
                bundle.getString("main.medium"),
                bundle.getString("main.movieTitle"),
                bundle.getString("main.movieTitleTranslation"),
        });
        combo.setData(new SingularAttribute[]{
                Film_.medijListAsString,
                Film_.nazivfilma,
                Film_.prevodnazivafilma
        });
        combo.select(0);
        combo.addSelectionListener(new SortingComboSelectionListener());

        Button cbAscending = new Button(statusBar, SWT.CHECK);
        cbAscending.setText(bundle.getString("main.ascending"));
        cbAscending.setSelection(true);
        cbAscending.addSelectionListener(new SortingCheckBoxSelectionListener());
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
        getAllFilms(applicationManager.getUserConfiguration().getElementsPerPage(), new Function<List<Film>, Void>() {
            @Override
            public Void apply(@Nullable List<Film> films) {
                mainTable.setMovies(films);

                MainForm.this.setChanged();
                MainForm.super.notifyObservers();

                if (toolTicker != null)
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
        final String filterText = currentViewState.getFilterText() == null ?
                null :
                '%' + currentViewState.getFilterText() + '%';
        final int startFrom = currentViewState.getActivePage().intValue() * maxItems;

        workerManager.submitLongTaskWithResultProcessingInSWTThread(
                new Callable<List<Film>>() {
                    @Override
                    public List<Film> call() throws Exception {
                        currentViewState.setMaxItemsPerPage(maxItems);
                        FilmRepository.FilmsWithCount filmsWithCount = filmRepository.getFilmByCriteria(startFrom, maxItems,
                                zanrFilter, tipMedijaFilter, pozicijaFilter, filterText, currentViewState.getSingularAttribute(),
                                currentViewState.isAscending());
                        currentViewState.setShowableCount(filmsWithCount.count);
                        return filmsWithCount.films;
                    }
                }, whatToDoWithFilms);
    }

}