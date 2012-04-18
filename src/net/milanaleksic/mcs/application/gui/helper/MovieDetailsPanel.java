package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 3/20/12
 * Time: 2:09 PM
 */
public class MovieDetailsPanel extends Composite {

    private ResourceBundle bundle;
    private ThumbnailManager thumbnailManager;
    private ShowImageComposite movieDetailsImage;

    private FontData systemFontData;

    private Label movieNameValue;
    private Label mediumListLabel;
    private Label mediumListValue;
    private Label genreLabel;
    private Label genreValue;
    private Label locationLabel;
    private Label locationValue;
    private Label tagsLabel;
    private Label tagsValue;
    private Text commentValue;
    private Optional<Film> film = Optional.absent();

    public MovieDetailsPanel(Composite parent, int style, ResourceBundle bundle, ThumbnailManager thumbnailManager) {
        super(parent, style);
        this.bundle = bundle;
        this.thumbnailManager = thumbnailManager;
        systemFontData = getDisplay().getSystemFont().getFontData()[0];
        createLayout();
    }

    private void createLayout() {
        // -----------------------------------------------------
        // moviedetailsimage |           secondColumn          |
        //                   |            movieName            |
        //                   | movieNameLabel | movieNameValue |

        setLayout(new GridLayout(2, false));

        movieDetailsImage = new ShowImageComposite(this, SWT.NONE, bundle);
        GridData firstColumnData = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        firstColumnData.widthHint = thumbnailManager.getThumbnailWidth();
        movieDetailsImage.setLayoutData(firstColumnData);

        Composite secondColumn = new Composite(this, SWT.NONE);
        secondColumn.setLayout(new GridLayout(2, false));
        secondColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createMovieNameRow(secondColumn);
        createMediumListRow(secondColumn);
        createGenreRow(secondColumn);
        createLocationRow(secondColumn);
        createTagsRow(secondColumn);
        createCommentsRow(secondColumn);
    }

    private void createMovieNameRow(Composite secondColumn) {
        movieNameValue = new Label(secondColumn, SWT.NONE);
        movieNameValue.setFont(new Font(getDisplay(), systemFontData.getName(), 12, SWT.BOLD));
        movieNameValue.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
        movieNameValue.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                if (bundle.getString("global.selectAMovie").equals(movieNameValue.getText())) {
                    movieNameValue.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
                    movieNameValue.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
                    return;
                }
                movieNameValue.setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
                movieNameValue.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
            }

            @Override
            public void mouseExit(MouseEvent e) {
                movieNameValue.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
            }
        });
        movieNameValue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (!film.isPresent())
                    return;
                if (!IMDBUtil.isValidImdbId(film.get().getImdbId())) {
                    MessageBox box = new MessageBox(getParent().getShell(), SWT.ICON_INFORMATION);
                    box.setText(bundle.getString("global.infoDialogTitle"));
                    box.setMessage(bundle.getString("global.movieIsNotLinkedToImdb"));
                    box.open();
                    return;
                }
                try {
                    Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(film.get().getImdbId()));
                } catch (IOException ignored) {}
            }
        });
    }

    private void createMediumListRow(Composite secondColumn) {
        mediumListLabel = new Label(secondColumn, SWT.NONE);
        mediumListLabel.setText(bundle.getString("main.medium"));
        mediumListLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        mediumListValue = new Label(secondColumn, SWT.NONE);
        mediumListValue.setFont(new Font(getDisplay(), systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD));
        mediumListValue.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void createGenreRow(Composite secondColumn) {
        genreLabel = new Label(secondColumn, SWT.NONE);
        genreLabel.setText(bundle.getString("main.genre"));
        genreLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        genreValue = new Label(secondColumn, SWT.NONE);
        genreValue.setFont(new Font(getDisplay(), systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD));
        genreValue.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void createLocationRow(Composite secondColumn) {
        locationLabel = new Label(secondColumn, SWT.NONE);
        locationLabel.setText(bundle.getString("main.location"));
        locationLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        locationValue = new Label(secondColumn, SWT.NONE);
        locationValue.setFont(new Font(getDisplay(), systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD));
        locationValue.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void createTagsRow(Composite secondColumn) {
        tagsLabel = new Label(secondColumn, SWT.NONE);
        tagsLabel.setText(bundle.getString("global.tags"));
        tagsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        tagsValue = new Label(secondColumn, SWT.NONE);
        tagsValue.setFont(new Font(getDisplay(), systemFontData.getName(), systemFontData.getHeight(), SWT.BOLD));
        tagsValue.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void createCommentsRow(Composite secondColumn) {
        commentValue = new Text(secondColumn, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        commentValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    }

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

    public void showDataForMovie(Optional<Film> film) {
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
        for(Tag tag : tags) {
            builder.append(tag.getNaziv()).append(", ");
        }
        if (builder.length()>2)
            return builder.toString().substring(0, builder.length()-2);
        return builder.toString();
    }
}
