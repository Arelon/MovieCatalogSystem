package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageInfo;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

public class FindMovieDialogForm extends AbstractDialogForm implements OfferMovieList.Receiver {

    private Text matchDescription;

    private Text movieName;

    private Table mainTable;

    private ShowImageComposite matchImage;

    private Movie selectedMovie;

    private PersistentHttpContext persistentHttpContext;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    @Inject
    @Named("offerMovieListForFindMovieDialogForm")
    private OfferMovieList offerMovieListForFindMovieDialogForm;

    private String initialText;

    private HandledSelectionAdapter matchSelectionHandler = new HandledSelectionAdapter(shell, bundle) {
        @Override
        public void handledSelected(SelectionEvent event) throws ApplicationException {
            int selectionIndex = mainTable.getSelectionIndex();
            if (selectionIndex < 0) {
                removeMatchDetails();
                return;
            }
            TableItem item = mainTable.getItem(selectionIndex);
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
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

        private String getAppropriateImageUrl(Movie movie) {
            for (ImageInfo imageInfo : movie.getPosters()) {
                if (imageInfo.getImage().getWidth() == 185)
                    return imageInfo.getImage().getUrl();
            }
            return null;
        }

        private void setStatusAndImage(@Nullable final String resourceId, @Nullable final Image image) {
            if (shell == null || shell.isDisposed())
                return;
            shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    matchImage.setImage(image);
                    if (resourceId != null) {
                        matchImage.setStatus(bundle.getString(resourceId));
                    }
                }
            });
        }
    };

    public void setInitialText(@Nullable String initialText) {
        this.initialText = initialText;
    }

    Movie getSelectedMovie() {
        return selectedMovie;
    }

    @Override
    protected void onShellCreated() {
        shell.setText(bundle.getString("global.findMovie"));
        shell.setLayout(new GridLayout(2, false));
        shell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
            public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
                if (offerMovieListForFindMovieDialogForm != null)
                    offerMovieListForFindMovieDialogForm.cleanup();
            }
        });
        createHeader();
        createCenter();
        createFooter();
    }

    @Override
    protected void onShellReady() {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        offerMovieListForFindMovieDialogForm.prepareFor(movieName);
        removeMatchDetails();
    }

    private void createFooter() {
        Composite footer = new Composite(shell, SWT.NONE);
        footer.setLayoutData(new GridData(SWT.CENTER, SWT.END, true, false, 2, 1));
        footer.setLayout(new GridLayout(2, true));

        Button btnAccept = new Button(footer, SWT.PUSH);
        btnAccept.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnAccept.setText(bundle.getString("global.save"));
        btnAccept.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int selectionIndex = mainTable.getSelectionIndex();
                if (selectionIndex < 0)
                    return;
                selectedMovie = (Movie) mainTable.getItem(selectionIndex).getData();
                FindMovieDialogForm.super.runnerWhenClosingShouldRun = true;
                shell.close();
            }
        });

        Button btnClose = new Button(footer, SWT.PUSH);
        btnClose.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        btnClose.setText(bundle.getString("global.cancel"));
        btnClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                shell.close();
            }
        });
    }

    private void createCenter() {
        mainTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        mainTable.setHeaderVisible(true);
        GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainTable.setLayoutData(tableLayoutData);
        mainTable.addSelectionListener(matchSelectionHandler);
        TableColumn firstColumn = new TableColumn(mainTable, SWT.LEFT | SWT.FLAT);
        firstColumn.setText(bundle.getString("global.columns.matchedMovieName"));
        firstColumn.setWidth(300);
        TableColumn yearColumn = new TableColumn(mainTable, SWT.LEFT | SWT.FLAT);
        yearColumn.setText(bundle.getString("global.columns.movieYear"));
        yearColumn.setWidth(50);
        TableColumn linkColumn = new TableColumn(mainTable, SWT.LEFT | SWT.FLAT);
        linkColumn.setText(bundle.getString("global.columns.imdbUrl"));
        linkColumn.setWidth(50);


        TabFolder folder = new TabFolder(shell, SWT.NONE);
        GridData folderGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
        folderGroupData.widthHint = 200;
        folderGroupData.heightHint = 300;
        folder.setLayoutData(folderGroupData);
        matchImage = new ShowImageComposite(bundle, folder, SWT.NONE);
        matchImage.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        matchDescription = new Text(folder, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        matchDescription.setText(bundle.getString("global.noImagePresent"));

        TabItem tabImage = new TabItem(folder, SWT.NONE);
        tabImage.setText(bundle.getString("global.tabs.poster"));
        tabImage.setControl(matchImage);
        TabItem tabDescription = new TabItem(folder, SWT.NONE);
        tabDescription.setText(bundle.getString("global.tabs.movieDescription"));
        tabDescription.setControl(matchDescription);
    }

    private void createHeader() {
        Composite header = new Composite(shell, SWT.NONE);
        header.setLayout(new GridLayout(2, false));
        header.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        Label labEmail = new Label(header, SWT.NONE);
        labEmail.setText(bundle.getString("findMovie.startTyping"));
        labEmail.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        movieName = new Text(header, SWT.BORDER);
        movieName.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void removeMatchDetails() {
        matchImage.setImage(null);
        matchImage.setStatus(null);
        selectedMovie = null;
        if (initialText != null) {
            movieName.setText(initialText);
            offerMovieListForFindMovieDialogForm.refreshRecommendations();
            initialText = null;
        }
    }

    @Override
    public void setCurrentQueryItems(final String currentQuery, final String message, @Nullable final Movie[] movies) {
        if (shell == null || shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                mainTable.removeAll();
                if (message != null) {
                    TableItem messageItem = new TableItem(mainTable, SWT.NONE);
                    messageItem.setText(new String[]{message, "", ""});
                }
                if (movies != null) {
                    for (Movie movie : movies) {
                        TableItem movieItem = new TableItem(mainTable, SWT.NONE);
                        movieItem.setText(new String[]{movie.getName(), movie.getReleasedYear(), ""});
                        movieItem.setData(movie);
                        ImdbLinkColumnFactory.create(shell, 2, movie, bundle, mainTable, movieItem);
                    }
                }
            }
        });
    }

}
