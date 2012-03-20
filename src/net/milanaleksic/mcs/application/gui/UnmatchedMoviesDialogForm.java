package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageInfo;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.*;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Event;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

public class UnmatchedMoviesDialogForm extends AbstractDialogForm {

    @Inject
    private FilmService filmService;

    @Inject
    private WorkerManager workerManager;

    @Inject
    private TmdbService tmdbService;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    private Table unmatchedMoviesTable;

    private Table possibleMatchesTable;

    private PersistentHttpContext persistentHttpContext;

    private LinkedList<Future<?>> listOfQueuedWorkers;
    private Map<Film, Integer> failureCountMap;
    private Map<Film, Movie[]> movieMatchesMap;
    private CountDownLatch processingCounterLatch;

    private ShowImageComposite matchImage;
    private Text matchDescription;
    private Button btnAcceptThisMatch;
    private Button btnStartMatching;

    private HandledSelectionAdapter unmatchedMovieSelectedHandler = new HandledSelectionAdapter(shell, bundle) {
        @Override
        public void handledSelected(SelectionEvent event) throws ApplicationException {
            removeMatchDetails();
            int selectionIndex = unmatchedMoviesTable.getSelectionIndex();
            if (selectionIndex < 0)
                return;
            TableItem item = unmatchedMoviesTable.getItem(selectionIndex);
            Film film = (Film) item.getData();
            if (!movieMatchesMap.containsKey(film))
                return;
            Movie[] movies = movieMatchesMap.get(film);
            for (final Movie movie : movies) {
                createItemForMovieMatch(movie);
            }
            explicitlySelectItemInTable(possibleMatchesTable, 0);
        }

        private void createItemForMovieMatch(final Movie movie) {
            TableItem matchItem = new TableItem(possibleMatchesTable, SWT.NONE);
            matchItem.setText(new String[]{
                    movie.getName(),
                    movie.getReleasedYear(),
                    ""}
            );
            matchItem.setData(movie);
            ImdbLinkColumnFactory.create(shell, 2, movie, bundle, possibleMatchesTable, matchItem);
        }
    };

    private HandledSelectionAdapter possibleMatchesSelectedHandler = new HandledSelectionAdapter(shell, bundle) {
        @Override
        public void handledSelected(SelectionEvent event) throws ApplicationException {
            int selectionIndex = possibleMatchesTable.getSelectionIndex();
            if (selectionIndex < 0) {
                removeMatchDetails();
                return;
            }
            TableItem item = possibleMatchesTable.getItem(selectionIndex);
            Movie movie = (Movie) item.getData();
            String appropriateImageUrl = getAppropriateImageUrl(movie);
            matchDescription.setText(movie.getOverview() == null ? "" : movie.getOverview());
            if (appropriateImageUrl == null)
                setStatusAndImage("unmatchedMoviesTable.noImageFound", null);
            else
                schedulePosterDownload(appropriateImageUrl);
        }

        private void schedulePosterDownload(final String appropriateImageUrl) {
            HandledRunnable task = new HandledRunnable(shell, bundle) {
                @Override
                public void handledRun() {
                    setStatusAndImage("unmatchedMoviesTable.downloadingImage", null);
                    SWTUtil.createImageFromUrl(URI.create(appropriateImageUrl),
                            persistentHttpContext,
                            new Function<Image, Void>() {
                                @Override
                                public Void apply(@Nullable final Image image) {
                                    setStatusAndImage(null, image);
                                    return null;
                                }
                            }
                    );
                }
            };
            // in case we already have some tasks enqueued let's run download as independent task
            if (processingCounterLatch.getCount() > 0)
                new Thread(task).start();
            else
                listOfQueuedWorkers.add(workerManager.submitIoBoundWorker(task));
        }

        private String getAppropriateImageUrl(Movie movie) {
            for (ImageInfo imageInfo : movie.getPosters()) {
                if (imageInfo.getImage().getWidth() == 185)
                    return imageInfo.getImage().getUrl();
            }
            return null;
        }

        private void setStatusAndImage(@Nullable final String resourceId, @Nullable final Image image) {
            shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    matchImage.setImage(image);
                    if (resourceId != null) {
                        matchImage.setStatus(bundle.getString(resourceId));
                        btnAcceptThisMatch.setEnabled(true);
                    }
                }
            });
        }
    };

    private HandledSelectionAdapter acceptMatchHandler = new HandledSelectionAdapter(shell, bundle) {
        @Override
        public void handledSelected(SelectionEvent event) throws ApplicationException {
            int unmatchedMovieIndex = unmatchedMoviesTable.getSelectionIndex();
            if (unmatchedMovieIndex < 0)
                throw new ApplicationException("No item has been selected in unmatched movies table");
            int possibleMatchIndex = possibleMatchesTable.getSelectionIndex();
            if (possibleMatchIndex < 0)
                throw new ApplicationException("No item has been selected in possible matches table");

            Film film = (Film) unmatchedMoviesTable.getItem(unmatchedMovieIndex).getData();
            Movie match = (Movie) possibleMatchesTable.getItem(possibleMatchIndex).getData();
            film.copyFromMovie(match);
            filmService.updateFilmWithChanges(film);
            unmatchedMoviesTable.remove(unmatchedMovieIndex);
            UnmatchedMoviesDialogForm.super.runnerWhenClosingShouldRun = true;
            removeMatchDetails();
            selectNextMovieWithMatches(unmatchedMovieIndex);
        }
    };

    private void selectNextMovieWithMatches(int startFromIndex) {
        int i = startFromIndex;
        while (i < unmatchedMoviesTable.getItemCount()) {
            TableItem item = unmatchedMoviesTable.getItem(i);
            Film film = (Film) item.getData();
            Movie[] movies = movieMatchesMap.get(film);
            if (movies != null && movies.length > 0) {
                explicitlySelectItemInTable(unmatchedMoviesTable, i);
                return;
            }
            i++;
        }
    }

    @Override
    protected void onShellCreated() {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        shell.setText(bundle.getString("global.unmatchedMoviesTable"));
        shell.setLayout(gridLayout);
        createContent();
    }

    @Override
    protected void onShellReady() {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        readData();
    }

    private void readData() {
        java.util.List<Film> filmovi = filmService.getListOfUnmatchedMovies();
        removeMatchDetails();
        for (Film film : filmovi) {
            TableItem tableItem = new TableItem(unmatchedMoviesTable, SWT.NONE);
            tableItem.setText(new String[]{film.toString(), bundle.getString("unmatchedMoviesTable.status.awaiting")});
            tableItem.setData(film);
        }
        movieMatchesMap = new ConcurrentHashMap<>();
        listOfQueuedWorkers = new LinkedList<>();
        failureCountMap = new ConcurrentHashMap<>();
    }

    private void removeMatchDetails() {
        matchImage.setImage(null);
        matchImage.setStatus(null);
        matchDescription.setText("");
        btnAcceptThisMatch.setEnabled(false);
        possibleMatchesTable.removeAll();
    }

    private void createContent() {
        final int ELEMENTS_IN_FIRST_ROW = 4;
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(ELEMENTS_IN_FIRST_ROW, false));

        // first row
        createUnmatchedMoviesTable(composite);
        createBtnStartProcess(composite);
        createPossibleMatchesTable(composite);
        createMatchImageContainer(composite);

        //second row
        Composite compositeFooter = new Composite(composite, SWT.NONE);
        compositeFooter.setLayout(new GridLayout(1, false));
        compositeFooter.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, ELEMENTS_IN_FIRST_ROW, 1));
        Button btnClose = new Button(compositeFooter, SWT.NONE);
        GridData btnCloseGridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
        btnCloseGridData.widthHint = 150;
        btnClose.setLayoutData(btnCloseGridData);
        btnClose.setText(bundle.getString("global.close"));
        btnClose.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                shell.close();
            }
        });
    }

    private void createBtnStartProcess(Composite composite) {
        btnStartMatching = new Button(composite, SWT.NONE);
        btnStartMatching.setText(bundle.getString("unmatchedMoviesTable.start"));
        btnStartMatching.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnStartMatching.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                btnStartMatching.setEnabled(false);
                startProcess();
            }
        });
    }

    private void createMatchImageContainer(Composite composite) {
        Composite matcherPanel = new Composite(composite, SWT.NONE);
        matcherPanel.setLayout(new GridLayout(1, false));
        matcherPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));


        TabFolder folder = new TabFolder(matcherPanel, SWT.NONE);
        GridData folderGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
        folderGroupData.widthHint = 200;
        folder.setLayoutData(folderGroupData);
        matchImage = new ShowImageComposite(folder, SWT.NONE, bundle);
        matchImage.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        matchDescription = new Text(folder, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        matchDescription.setText(bundle.getString("global.noImagePresent"));

        TabItem tabImage = new TabItem(folder, SWT.NONE);
        tabImage.setText(bundle.getString("global.tabs.poster"));
        tabImage.setControl(matchImage);
        TabItem tabDescription = new TabItem(folder, SWT.NONE);
        tabDescription.setText(bundle.getString("global.tabs.movieDescription"));
        tabDescription.setControl(matchDescription);

        btnAcceptThisMatch = new Button(matcherPanel, SWT.NONE);
        btnAcceptThisMatch.setText(bundle.getString("unmatchedMoviesTable.acceptMatch"));
        btnAcceptThisMatch.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnAcceptThisMatch.addSelectionListener(acceptMatchHandler);
    }

    private void createPossibleMatchesTable(Composite composite) {
        possibleMatchesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        possibleMatchesTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
        gridData.heightHint = 200;
        possibleMatchesTable.setLayoutData(gridData);
        possibleMatchesTable.addSelectionListener(possibleMatchesSelectedHandler);
        TableColumn firstColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        firstColumn.setText(bundle.getString("global.columns.matchedMovieName"));
        firstColumn.setWidth(300);
        TableColumn yearColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        yearColumn.setText(bundle.getString("global.columns.movieYear"));
        yearColumn.setWidth(50);
        TableColumn linkColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        linkColumn.setText(bundle.getString("global.columns.imdbUrl"));
        linkColumn.setWidth(50);
    }

    private void createUnmatchedMoviesTable(Composite parent) {
        unmatchedMoviesTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        unmatchedMoviesTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.heightHint = 350;
        unmatchedMoviesTable.setLayoutData(gridData);
        TableColumn firstColumn = new TableColumn(unmatchedMoviesTable, SWT.LEFT | SWT.FLAT);
        firstColumn.setText(bundle.getString("unmatchedMoviesTable.firstColumnName"));
        firstColumn.setWidth(300);
        TableColumn processingColumn = new TableColumn(unmatchedMoviesTable, SWT.LEFT | SWT.FLAT);
        processingColumn.setText(bundle.getString("unmatchedMoviesTable.processingColumnName"));
        processingColumn.setWidth(120);
        unmatchedMoviesTable.addSelectionListener(unmatchedMovieSelectedHandler);
    }

    private synchronized void startProcess() {
        final long begin = System.currentTimeMillis();
        int maxCount = unmatchedMoviesTable.getItemCount();
        processingCounterLatch = new CountDownLatch(maxCount);
        for (int i = 0; i < maxCount; i++) {
            final TableItem item = unmatchedMoviesTable.getItem(i);
            setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.enqueued"));
            addProcessingWorker(item, (Film) item.getData());
        }
        Thread infoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (logger.isDebugEnabled())
                        logger.debug("Awaiting completion of all work items"); //NON-NLS
                    processingCounterLatch.await();
                    final String timeSpent = StringUtil.showMillisIntervalAsString(System.currentTimeMillis() - begin);
                    shell.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
                            messageBox.setMessage(String.format(bundle.getString("unmatchedMoviesTable.matchingCompleteIn"), timeSpent));
                            messageBox.setText(bundle.getString("global.infoDialogTitle"));
                            messageBox.open();
                            btnStartMatching.setEnabled(true);
                        }
                    });
                } catch (InterruptedException e) {
                    logger.warn("Waiting for all processes to be finished just got interrupted."); //NON-NLS
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    logger.error("Unexpected exception while waiting for matching process to complete", t); //NON-NLS
                }
            }
        }, "InfoWhenAllMoviesMatchedThread"); //NON-NLS
        infoThread.setDaemon(true);
        infoThread.start();
    }

    private void addProcessingWorker(final TableItem item, final Film film) {
        listOfQueuedWorkers.add(workerManager.submitIoBoundWorker(new HandledRunnable(shell, bundle) {
            @Override
            public void handledRun() {
                boolean success = true;
                try {
                    Integer failureCount = failureCountMap.get(film);
                    if (failureCount != null && failureCount >= 3) {
                        setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.gaveUp"));
                        return;
                    }
                    setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.processing"));
                    Movie[] movies = tmdbService.searchForMovies(film.getNazivfilma());
                    if (movieMatchesMap == null)
                        return;
                    movieMatchesMap.put(film, movies);
                    if (movies == null || movies.length == 0) {
                        deleteItem(item);
                        return;
                    }
                    setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.processed")
                            + " (" + (movies.length) + ")");
                    if (logger.isDebugEnabled())
                        logger.debug("Movie match for movie " + film.getNazivfilma() + " returned " + (movies == null ? "NULL" : movies.length) + " items"); //NON-NLS
                } catch (TmdbException e) {
                    logger.error("Application error while processing movie: " + film.getNazivfilma(), e); //NON-NLS
                    retryForMovie(item, film);
                    success = false;
                } finally {
                    if (success)
                        processingCounterLatch.countDown();
                }
            }

            private void deleteItem(final TableItem item) {
                shell.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (item.isDisposed())
                            return;
                        item.dispose();
                    }
                });
            }

            private synchronized void retryForMovie(TableItem item, Film film) {
                if (failureCountMap == null)
                    return;
                Integer failureCount = failureCountMap.get(film);
                failureCountMap.put(film, failureCount == null ? 1 : failureCount + 1);
                addProcessingWorker(item, film);
            }
        }));
    }

    private void setStatusOnUnmatchedMoviesTableItem(final TableItem item, final String status) {
        if (shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (item.isDisposed())
                    return;
                item.setText(1, status);
            }
        });
    }

    @Override
    protected synchronized boolean onShouldShellClose() {
        if (thereAreUnfinishedWorkers()) {
            MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            messageBox.setMessage(bundle.getString("unmatchedMoviesTable.processNotFinishedConfirm"));
            if (messageBox.open() != SWT.YES)
                return false;
            logger.info("Killing all unfinished workers");
            killAllUnfinishedWorkers();
        }
        clearProcessingData();
        return true;
    }

    private void explicitlySelectItemInTable(Table table, int itemIndex) {
        if (table.getItemCount() <= itemIndex || itemIndex < 0)
            return;
        table.select(itemIndex);
        Event e = new Event();
        e.item = table.getItem(itemIndex);
        table.notifyListeners(SWT.Selection, e);
    }

    private void clearProcessingData() {
        if (listOfQueuedWorkers != null) {
            listOfQueuedWorkers.clear();
            listOfQueuedWorkers = null;
        }
        if (movieMatchesMap != null) {
            movieMatchesMap.clear();
            movieMatchesMap = null;
        }
        if (failureCountMap != null) {
            failureCountMap.clear();
            failureCountMap = null;
        }
    }

    private void killAllUnfinishedWorkers() {
        for (Future<?> future : listOfQueuedWorkers) {
            if (future != null && !future.isCancelled() && !future.isDone())
                future.cancel(true);
        }
    }

    private boolean thereAreUnfinishedWorkers() {
        if (listOfQueuedWorkers == null || listOfQueuedWorkers.size() == 0)
            return false;
        for (Future<?> future : listOfQueuedWorkers) {
            if (future != null && !future.isCancelled() && !future.isDone())
                return true;
        }
        return false;
    }
}
