package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.network.HttpClientFactoryService;
import net.milanaleksic.mcs.infrastructure.network.PersistentHttpContext;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.ImageInfo;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

public class FindMovieForm extends AbstractTransformedForm implements OfferMovieList.Receiver {

    @EmbeddedComponent
    private Text matchDescription = null;

    @EmbeddedComponent
    private Text movieName = null;

    @EmbeddedComponent
    private Table mainTable = null;

    @EmbeddedComponent
    private ShowImageComposite matchImage = null;

    private Optional<Movie> selectedMovie = Optional.absent();

    private PersistentHttpContext persistentHttpContext;

    @Inject
    private HttpClientFactoryService httpClientFactoryService;

    @Inject
    @Named("offerMovieListForFindMovieDialogForm")
    @Nullable
    private OfferMovieList offerMovieListForFindMovieDialogForm;

    @EmbeddedEventListener(event = SWT.Close)
    private final Listener shellCloseListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (offerMovieListForFindMovieDialogForm != null)
                offerMovieListForFindMovieDialogForm.cleanUp();
        }
    };

    @EmbeddedEventListener(component = "btnAccept", event = SWT.Selection)
    private final Listener btnAcceptSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            int selectionIndex = mainTable.getSelectionIndex();
            if (selectionIndex < 0)
                return;
            selectedMovie = Optional.of((Movie) mainTable.getItem(selectionIndex).getData());
            FindMovieForm.super.runnerWhenClosingShouldRun = true;
            shell.close();
        }
    };

    @EmbeddedEventListener(component = "btnClose", event = SWT.Selection)
    private final Listener btnCloseSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
        }
    };

    private String initialText = "";

    @EmbeddedEventListener(component = "mainTable", event = SWT.Selection)
    private final Listener matchSelectionHandler = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            int selectionIndex = mainTable.getSelectionIndex();
            if (selectionIndex < 0) {
                removeMatchDetails();
                return;
            }
            TableItem item = mainTable.getItem(selectionIndex);
            Movie movie = (Movie) item.getData();
            if (movie == null)
                return;
            Optional<String> appropriateImageUrl = getAppropriateImageUrl(movie);
            matchDescription.setText(Strings.nullToEmpty(movie.getOverview()));
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
                                    setStatusAndImage(Optional.<String>absent(), Optional.of(image));
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

        private Optional<String> getAppropriateImageUrl(Movie movie) {
            for (ImageInfo imageInfo : movie.getPosters()) {
                if (imageInfo.getImage().getWidth() == 185)
                    return Optional.of(imageInfo.getImage().getUrl());
            }
            return Optional.absent();
        }

        private void setStatusAndImage(final Optional<String> resourceId, final Optional<Image> image) {
            if (shell.isDisposed())
                return;
            shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    matchImage.setImage(image);
                    if (resourceId.isPresent()) {
                        matchImage.setStatus(Optional.of(bundle.getString(resourceId.get())));
                    }
                }
            });
        }
    };

    public void setInitialText(@Nonnull String initialText) {
        this.initialText = initialText;
    }

    Optional<Movie> getSelectedMovie() {
        return selectedMovie;
    }

    @Override
    protected void onShellReady() {
        persistentHttpContext = httpClientFactoryService.createPersistentHttpContext();
        if (offerMovieListForFindMovieDialogForm != null)
            offerMovieListForFindMovieDialogForm.prepareFor(movieName);
        removeMatchDetails();
    }

    private void removeMatchDetails() {
        matchImage.setImage(Optional.<Image>absent());
        matchImage.setStatus(Optional.<String>absent());
        selectedMovie = Optional.absent();
        movieName.setText(initialText);
        if (offerMovieListForFindMovieDialogForm != null)
            offerMovieListForFindMovieDialogForm.refreshRecommendations();
    }

    @Override
    public void setCurrentQueryItems(final String currentQuery, final Optional<String> message, final Optional<Movie[]> movies) {
        if (shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (mainTable.isDisposed())
                    return;
                mainTable.removeAll();
                if (message.isPresent()) {
                    TableItem messageItem = new TableItem(mainTable, SWT.NONE);
                    messageItem.setText(new String[]{message.get(), "", ""});
                }
                if (movies.isPresent()) {
                    for (Movie movie : movies.get()) {
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
