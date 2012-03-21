package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;

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
    private Text commentValue;
    private Film film;

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
                if (film == null)
                    return;
                if (!IMDBUtil.isValidImdbId(film.getImdbId())) {
                    MessageBox box = new MessageBox(getParent().getShell(), SWT.ICON_INFORMATION);
                    box.setText(bundle.getString("global.infoDialogTitle"));
                    box.setMessage(bundle.getString("global.movieIsNotLinkedToImdb"));
                    box.open();
                    return;
                }
                try {
                    Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(film.getImdbId()));
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

    private void createCommentsRow(Composite secondColumn) {
        commentValue = new Text(secondColumn, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        commentValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    }

    public void clearData() {
        this.film = null;
        movieDetailsImage.setStatus(bundle.getString("global.noImagePresent"));
        movieDetailsImage.setImage(null);
        movieNameValue.setText(bundle.getString("global.selectAMovie"));
        mediumListLabel.setVisible(false);
        mediumListValue.setText("");
        genreLabel.setVisible(false);
        genreValue.setText("");
        locationLabel.setVisible(false);
        locationValue.setText("");
        commentValue.setVisible(false);
    }

    public void showDataForMovie(Film film) {
        if (film == null) {
            clearData();
            return;
        }
        this.film = film;
        thumbnailManager.setThumbnailForShowImageComposite(movieDetailsImage, film.getImdbId());
        movieNameValue.setText(film.getNazivfilma());
        mediumListLabel.setVisible(true);
        mediumListValue.setText(film.getMedijListAsString());
        genreLabel.setVisible(true);
        genreValue.setText(film.getZanr().getZanr());
        locationLabel.setVisible(true);
        locationValue.setText(film.getPozicija());
        commentValue.setVisible(true);
        commentValue.setText(film.getKomentar());
    }


}
