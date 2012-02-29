package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbException;
import net.milanaleksic.mcs.infrastructure.tmdb.TmdbService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import net.milanaleksic.mcs.infrastructure.worker.WorkerManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class UnmatchedMoviesDialogForm extends AbstractDialogForm {

    @Inject private FilmService filmService;

    @Inject private WorkerManager workerManager;

    @Inject private TmdbService tmdbService;

    private Table unmatchedMoviesTable;

    private LinkedList<Future<?>> listOfQueuedWorkers;

    private Map<Film, Movie[]> movieMatchesMap;
    private Table possibleMatchesTable;

    @Override protected void onShellCreated() {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        shell.setText(bundle.getString("global.unusedMediums"));
        shell.setLayout(gridLayout);
        createContent();
    }

    @Override protected void onShellReady() {
        readData();
    }

    private void readData() {
        java.util.List<Film> filmovi = filmService.getListOfUnmatchedMovies();
        unmatchedMoviesTable.removeAll();
        for (Film film : filmovi) {
            TableItem tableItem = new TableItem(unmatchedMoviesTable, SWT.NONE);
            tableItem.setText(new String[] { film.toString(), bundle.getString("unmatchedMoviesTable.status.awaiting")});
            tableItem.setData(film);
        }
        movieMatchesMap = new ConcurrentHashMap<>();
        listOfQueuedWorkers = new LinkedList<>();
    }

    private void createContent() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        // first row
        createUnmatchedMoviesTable(composite);
        final Button btnStartMatching = new Button(composite, SWT.NONE);
        btnStartMatching.setText(bundle.getString("unmatchedMoviesTable.start"));
        btnStartMatching.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnStartMatching.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                btnStartMatching.setEnabled(false);
                startProcess();
            }
        });
        createPossibleMatchesTable(composite);

        //second row
        Composite compositeFooter = new Composite(composite, SWT.NONE);
        compositeFooter.setLayout(new GridLayout(1, false));
        compositeFooter.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1));
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

    private void createPossibleMatchesTable(Composite composite) {
        possibleMatchesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        possibleMatchesTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
        gridData.heightHint = 200;
        possibleMatchesTable.setLayoutData(gridData);
        TableColumn firstColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        firstColumn.setText(bundle.getString("unmatchedMoviesTable.matches.nameColumn"));
        firstColumn.setWidth(300);
        TableColumn yearColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        yearColumn.setText(bundle.getString("unmatchedMoviesTable.matches.yearColumn"));
        yearColumn.setWidth(50);
        TableColumn linkColumn = new TableColumn(possibleMatchesTable, SWT.LEFT | SWT.FLAT);
        linkColumn.setText(bundle.getString("unmatchedMoviesTable.matches.imdbLink"));
        linkColumn.setWidth(50);
    }

    private void createUnmatchedMoviesTable(Composite parent) {
        unmatchedMoviesTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        unmatchedMoviesTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.heightHint = 300;
        unmatchedMoviesTable.setLayoutData(gridData);
        TableColumn firstColumn = new TableColumn(unmatchedMoviesTable, SWT.LEFT | SWT.FLAT);
        firstColumn.setText(bundle.getString("unmatchedMoviesTable.firstColumnName"));
        firstColumn.setWidth(300);
        TableColumn processingColumn = new TableColumn(unmatchedMoviesTable, SWT.LEFT | SWT.FLAT);
        processingColumn.setText(bundle.getString("unmatchedMoviesTable.processingColumnName"));
        processingColumn.setWidth(120);
        unmatchedMoviesTable.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                possibleMatchesTable.removeAll();
                int selectionIndex = unmatchedMoviesTable.getSelectionIndex();
                if (selectionIndex<0)
                    return;
                TableItem item = unmatchedMoviesTable.getItem(selectionIndex);
                Film film = (Film) item.getData();
                if (!movieMatchesMap.containsKey(film))
                    return;
                Movie[] movies = movieMatchesMap.get(film);
                for (final Movie movie:movies) {
                    TableItem matchItem = new TableItem(possibleMatchesTable, SWT.NONE);
                    matchItem.setText(new String[] {
                            movie.getName(),
                            movie.getReleasedYear(),
                            ""}
                    );

                    TableEditor editor = new TableEditor(possibleMatchesTable);
                    Link link = new Link(possibleMatchesTable, SWT.NONE);
                    link.setText("<A>"+bundle.getString("unmatchedMoviesTable.matches.url")+"</A>");
                    link.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    link.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
                        @Override
                        public void handledSelected(SelectionEvent event) throws ApplicationException {
                            try {
                                Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(movie.getImdbId()));
                            } catch (IOException e) {
                                throw new ApplicationException("Unexpected IO exception when trying to open URL based on received IMDB link");
                            }
                        }
                    });
                    link.pack();
                    editor.minimumWidth = link.getSize().x;
                    editor.horizontalAlignment = SWT.LEFT;
                    editor.setEditor(link, matchItem, 2);
                }
            }
        });
    }

    private synchronized void startProcess() {
        for (int i=0; i<unmatchedMoviesTable.getItemCount(); i++) {
            final TableItem item = unmatchedMoviesTable.getItem(i);
            final Film data = (Film) item.getData();
            item.setText(1, bundle.getString("unmatchedMoviesTable.status.enqueued"));
            listOfQueuedWorkers.add(workerManager.submitWorker(new Runnable() {
                @Override public void run() {
                    try {
                        setStatusOnItem(item, bundle.getString("unmatchedMoviesTable.status.processing"));
                        Movie[] movies = tmdbService.searchForMovies(data.getNazivfilma());
                        if (movieMatchesMap == null)
                            return;
                        movieMatchesMap.put(data, movies);
                        setStatusOnItem(item, bundle.getString("unmatchedMoviesTable.status.processed")
                                +" ("+(movies==null?0:movies.length)+")");
                    } catch (TmdbException e) {
                        logger.error("Application error while processing movie: "+data.getNazivfilma(), e);
                    } catch (Exception e) {
                        logger.error("Unexpected error while processing movie: "+data.getNazivfilma(), e);
                    }
                }
            }));
        }
    }

    private void setStatusOnItem(final TableItem item, final String status) {
        shell.getDisplay().asyncExec(new Runnable() {
            @Override public void run() {
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

    private void clearProcessingData() {
        if (listOfQueuedWorkers != null) {
            listOfQueuedWorkers.clear();
            listOfQueuedWorkers = null;
        }
        if (movieMatchesMap != null) {
            movieMatchesMap.clear();
            movieMatchesMap = null;
        }
    }

    private void killAllUnfinishedWorkers() {
        for (Future<?> future : listOfQueuedWorkers) {
            if (future != null && !future.isCancelled() && !future.isDone())
                future.cancel(true);
        }
    }

    private boolean thereAreUnfinishedWorkers() {
        if (listOfQueuedWorkers == null || listOfQueuedWorkers.size()==0)
            return false;
        for (Future<?> future : listOfQueuedWorkers) {
            if (future != null && !future.isCancelled() && !future.isDone())
                return true;
        }
        return false;
    }
}
