package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.ShowImageComposite;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.Set;

/**
 * User: Milan Aleksic
 * Date: 4/24/12
 * Time: 8:20 AM
 */
public class MovieDetailsForm extends AbstractTransformedForm {

    @Inject
    private ThumbnailManager thumbnailManager;

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
    private ShowImageComposite movieDetailsImage;

    @EmbeddedComponent
    private Label movieNameValue;

    @EmbeddedComponent
    private Label mediumListLabel;

    @EmbeddedComponent
    private Label mediumListValue;

    @EmbeddedComponent
    private Label genreLabel;

    @EmbeddedComponent
    private Label genreValue;

    @EmbeddedComponent
    private Label locationLabel;

    @EmbeddedComponent
    private Label locationValue;

    @EmbeddedComponent
    private Label tagsLabel;

    @EmbeddedComponent
    private Label tagsValue;

    @EmbeddedComponent
    private Label commentValue;

    private Optional<Film> film = Optional.absent();

    public void clearData() {
        this.film = Optional.absent();
        movieDetailsImage.setStatus(Optional.of(bundle.getString("global.noImagePresent")));
        movieDetailsImage.setImage(Optional.<org.eclipse.swt.graphics.Image>absent());
        movieNameValue.setText(bundle.getString("global.selectAMovie"));
        mediumListLabel.setVisible(false);
        mediumListValue.setText("");
        genreLabel.setVisible(false);
        genreValue.setText("");
        locationLabel.setVisible(false);
        locationValue.setText("");
        tagsLabel.setVisible(false);
        tagsValue.setText("");
        commentValue.setVisible(false);
    }

    public void showDataForMovie(Shell parent, Optional<Film> film) {
        if (shell == null)
            open(parent);
        shell.setLocation(shell.getDisplay().getCursorLocation());
        this.film = film;
        if (!this.film.isPresent()) {
            clearData();
            return;
        }
        Film theFilm = film.get();
        thumbnailManager.setThumbnailForShowImageComposite(movieDetailsImage, theFilm.getImdbId());
        movieNameValue.setText(theFilm.getNazivfilma());
        mediumListLabel.setVisible(true);
        mediumListValue.setText(theFilm.getMedijListAsString());
        genreLabel.setVisible(true);
        genreValue.setText(theFilm.getZanr().getZanr());
        locationLabel.setVisible(true);
        locationValue.setText(theFilm.getPozicija());
        tagsLabel.setVisible(true);
        tagsValue.setText(getFilmTagsAsString(theFilm));
        commentValue.setVisible(true);
        commentValue.setText(theFilm.getKomentar());
    }

    public String getFilmTagsAsString(Film theFilm) {
        Set<Tag> tags = theFilm.getTags();
        if (null == tags)
            return "";
        StringBuilder builder = new StringBuilder();
        for (Tag tag : tags) {
            builder.append(tag.getNaziv()).append(", ");
        }
        if (builder.length() > 2)
            return builder.toString().substring(0, builder.length() - 2);
        return builder.toString();
    }
}
