package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import com.google.common.collect.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.network.*;
import net.milanaleksic.mcs.infrastructure.tmdb.*;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.*;
import net.milanaleksic.mcs.infrastructure.util.*;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Table;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class UnmatchedMoviesDialogForm extends AbstractTransformedForm {

    @Inject
    private FilmService filmService;

    @Inject
    private WorkerManager workerManager;

    @Inject
    private TmdbService tmdbService;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    @EmbeddedComponent
    private Table unmatchedMoviesTable = null;

    @EmbeddedComponent
    private Button btnStartMatching = null;

    @EmbeddedComponent
    private Table possibleMatchesTable = null;

    @EmbeddedComponent
    private ShowImageComposite matchImage = null;

    @EmbeddedComponent
    private Text matchDescription = null;

    @EmbeddedComponent
    private Button btnAcceptThisMatch = null;

    private PersistentHttpContext persistentHttpContext;

    private LinkedList<Future<?>> listOfQueuedWorkers;
    private Map<Film, Integer> failureCountMap;
    private Map<Film, Movie[]> movieMatchesMap;

    private CountDownLatch processingCounterLatch;

    @EmbeddedEventListener(component = "unmatchedMoviesTable", event = SWT.Selection)
    private final Listener unmatchedMoviesTableSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
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

    @EmbeddedEventListener(component = "possibleMatchesTable", event = SWT.Selection)
    private final HandledListener possibleMatchesTableSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            int selectionIndex = possibleMatchesTable.getSelectionIndex();
            if (selectionIndex < 0) {
                removeMatchDetails();
                return;
            }
            TableItem item = possibleMatchesTable.getItem(selectionIndex);
            Movie movie = (Movie) item.getData();
            matchDescription.setText(Strings.nullToEmpty(movie.getOverview()));
            Optional<String> appropriateImageUrl = getAppropriateImageUrl(movie);
            if (appropriateImageUrl.isPresent())
                schedulePosterDownload(appropriateImageUrl.get());
            else
                setStatusAndImage(Optional.of("unmatchedMoviesTable.noImageFound"), Optional.<Image>absent());
        }

        private void schedulePosterDownload(final String appropriateImageUrl) {
            HandledRunnable task = new HandledRunnable(shell, bundle) {
                @Override
                public void handledRun() {
                    setStatusAndImage(Optional.of("unmatchedMoviesTable.downloadingImage"), Optional.<Image>absent());
                    SWTUtil.createImageFromUrl(URI.create(appropriateImageUrl),
                            persistentHttpContext,
                            new Function<Image, Void>() {
                                @Override
                                public Void apply(@Nullable final Image image) {
                                    setStatusAndImage(Optional.<String>absent(), Optional.fromNullable(image));
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

        private Optional<String> getAppropriateImageUrl(Movie movie) {
            for (ImageInfo imageInfo : movie.getPosters()) {
                if (imageInfo.getImage().getWidth() == 185)
                    return Optional.of(imageInfo.getImage().getUrl());
            }
            return Optional.absent();
        }

        private void setStatusAndImage(final Optional<String> resourceId, final Optional<Image> image) {
            shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    matchImage.setImage(image);
                    if (resourceId.isPresent()) {
                        matchImage.setStatus(Optional.of(bundle.getString(resourceId.get())));
                        btnAcceptThisMatch.setEnabled(true);
                    }
                }
            });
        }
    };

    @EmbeddedEventListener(component = "btnAcceptThisMatch", event = SWT.Selection)
    private final HandledListener btnAcceptThisMatchSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
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

    @EmbeddedEventListener(component = "btnStartMatching", event = SWT.Selection)
    private HandledListener btnStartMatchingSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            btnStartMatching.setEnabled(false);
            startProcess();
        }
    };

    @EmbeddedEventListener(component = "btnClose", event = SWT.Selection)
    private final HandledListener btnCloseSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
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
        movieMatchesMap = Maps.newConcurrentMap();
        listOfQueuedWorkers = Lists.newLinkedList();
        failureCountMap = Maps.newConcurrentMap();
    }

    private void removeMatchDetails() {
        matchImage.setImage(Optional.<Image>absent());
        matchImage.setStatus(Optional.<String>absent());
        matchDescription.setText("");
        btnAcceptThisMatch.setEnabled(false);
        possibleMatchesTable.removeAll();
    }

    private void startProcess() {
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
                    doRun();
                } catch (TmdbException e) {
                    logger.error("Application error while processing movie: " + film.getNazivfilma(), e); //NON-NLS
                    retryForMovie(item, film);
                    success = false;
                } finally {
                    if (success)
                        processingCounterLatch.countDown();
                }
            }

            private void doRun() throws TmdbException {
                Integer failureCount = failureCountMap.get(film);
                if (failureCount != null && failureCount >= 3) {
                    setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.gaveUp"));
                    return;
                }
                setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.processing"));
                Optional<Movie[]> moviesOptional = tmdbService.searchForMovies(film.getNazivfilma());
                if (movieMatchesMap == null)
                    return;
                if (moviesOptional.isPresent())
                    movieMatchesMap.put(film, moviesOptional.get());
                if (!moviesOptional.isPresent() || moviesOptional.get().length == 0) {
                    deleteItem(item);
                    return;
                }
                setStatusOnUnmatchedMoviesTableItem(item, bundle.getString("unmatchedMoviesTable.status.processed")
                        + " (" + (moviesOptional.get().length) + ")");
                if (logger.isDebugEnabled())
                    logger.debug("Movie match for movie " + film.getNazivfilma() + " returned " + (moviesOptional.get().length) + " items"); //NON-NLS
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

            private void retryForMovie(TableItem item, Film film) {
                synchronized (UnmatchedMoviesDialogForm.this) {
                    if (failureCountMap == null)
                        return;
                    Integer failureCount = failureCountMap.get(film);
                    failureCountMap.put(film, failureCount == null ? 1 : failureCount + 1);
                    addProcessingWorker(item, film);
                }
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
    protected boolean onShouldShellClose() {
        if (thereAreUnfinishedWorkers()) {
            MessageBox messageBox = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            messageBox.setMessage(bundle.getString("unmatchedMoviesTable.processNotFinishedConfirm"));
            if (messageBox.open() != SWT.YES)
                return false;
            logger.info("Killing all unfinished workers"); //NON-NLS
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
        synchronized (this) {
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
