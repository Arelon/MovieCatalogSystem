package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.application.gui.helper.DynamicSelectorText;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.LifeCycleListener;
import net.milanaleksic.mcs.infrastructure.config.*;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.*;

import java.awt.*;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;

/**
 * User: Milan Aleksic
 * Date: 4/24/12
 * Time: 8:20 AM
 */
public class MovieDetailsForm extends AbstractTransformedForm implements LifeCycleListener {

    private ScheduledExecutorService hiderPool = Executors.newScheduledThreadPool(1);
    private Optional<ScheduledFuture<?>> hiderScheduledFuture = Optional.absent();

    private Runnable hideMovieDetailsForm = new Runnable() {
        @Override
        public void run() {
            if (!shell.getDisplay().getThread().equals(Thread.currentThread())) {
                shell.getDisplay().syncExec(this);
                return;
            }
            hide();
        }
    };

    @EmbeddedEventListener(component = "movieNameValue", event = SWT.MouseEnter)
    private final Listener movieNameValueMouseEnter = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (bundle.getString("global.selectAMovie").equals(movieNameValue.getText())) {
                movieNameValue.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
                movieNameValue.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
                return;
            }
            movieNameValue.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            movieNameValue.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        }
    };

    @EmbeddedEventListener(component = "movieNameValue", event = SWT.MouseExit)
    private final Listener movieNameValueMouseExit = new Listener() {
        @Override
        public void handleEvent(Event event) {
            movieNameValue.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        }
    };

    @EmbeddedEventListener(component = "movieNameValue", event = SWT.MouseDown)
    private final Listener movieNameValueMouseDown = new Listener() {
        @Override
        public void handleEvent(Event event) {
            if (!film.isPresent())
                return;
            if (!IMDBUtil.isValidImdbId(film.get().getImdbId())) {
                MessageBox box = new MessageBox(getShell(), SWT.ICON_INFORMATION);
                box.setText(bundle.getString("global.infoDialogTitle"));
                box.setMessage(bundle.getString("global.movieIsNotLinkedToImdb"));
                box.open();
                return;
            }
            try {
                Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(film.get().getImdbId()));
            } catch (IOException ignored) {
            }
        }
    };

    @EmbeddedComponent
    private Label movieNameValue;

    @EmbeddedComponent
    private DynamicSelectorText mediumListValue;

    @EmbeddedComponent
    private Label genreValue;

    @EmbeddedComponent
    private Label locationValue;

    @EmbeddedComponent
    private DynamicSelectorText tagsValue;

    @EmbeddedComponent
    private Label commentValue;

    private Optional<Film> film = Optional.absent();

    public void showDataForMovie(Shell parent, Optional<Film> film) {
        if (hiderScheduledFuture.isPresent())
            hiderScheduledFuture.get().cancel(true);

        boolean wasNull = false;
        if (shell == null) {
            prepareShell(parent);
            wasNull = true;
        }
        final Point cursorLocation = shell.getDisplay().getCursorLocation();
        shell.setLocation(cursorLocation.x + 1, cursorLocation.y + 1);
        this.film = film;
        if (!this.film.isPresent()) {
            return;
        }
        fillFormData(film);
        shell.pack();
        if (wasNull)
            shell.open();
        else
            shell.setVisible(true);

        hiderScheduledFuture = Optional.<ScheduledFuture<?>>of(hiderPool.schedule(hideMovieDetailsForm, 3000, TimeUnit.MILLISECONDS));
    }

    private void fillFormData(Optional<Film> film) {
        Film theFilm = film.get();
        movieNameValue.setText(theFilm.getNazivfilma());
        final Set<String> medijNames = Sets.newLinkedHashSet();
        for (Medij medij : theFilm.getMedijs()) {
            medijNames.add(medij.toString());
        }
        mediumListValue.setSelectedItems(medijNames);
        genreValue.setText(theFilm.getZanr().getZanr());
        locationValue.setText(theFilm.getPozicija());
        final Set<String> tagNames = Sets.newLinkedHashSet();
        for (Tag tag : theFilm.getTags()) {
            tagNames.add(tag.toString());
        }
        tagsValue.setSelectedItems(tagNames);
        commentValue.setText(theFilm.getKomentar());
    }

    public void hide() {
        if (shell == null)
            return;
        shell.setVisible(false);
    }

    @Override
    public void applicationStarted(ApplicationConfiguration configuration, UserConfiguration userConfiguration) {
    }

    @Override
    public void applicationShutdown(ApplicationConfiguration applicationConfiguration, UserConfiguration userConfiguration) {
        hiderPool.shutdownNow();
    }
}
