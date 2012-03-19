package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Function;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.thumbnail.impl.ImageTargetWidget;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: Milan Aleksic
 * Date: 3/19/12
 * Time: 4:03 PM
 */
public class CoolMovieComposite extends Composite implements PaintListener {

    private final int PADDING_BETWEEN_COLUMNS = 10;
    private final int PADDING_BETWEEN_ROWS = 10;
    private final int PADDING_BETWEEN_ROWS_TEXT = 15;

    private List<MovieWrapper> movies = new LinkedList<>();

    private final ThumbnailManager thumbnailManager;
    private final int thumbnailHeight;
    private final int thumbnailWidth;
    private final Color shadeColor;

    private int selectedItem = -1;

    @Override
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        gc.setAdvanced(true);
        gc.setTextAntialias(SWT.ON);
        int x = PADDING_BETWEEN_COLUMNS;
        int y = PADDING_BETWEEN_ROWS;
        for (int i = 0, moviesSize = movies.size(); i < moviesSize; i++) {
            MovieWrapper movieWrapper = movies.get(i);
            // TODO: paint only if it intercepts drawing region
            gc.setBackground(shadeColor);
            gc.fillRectangle(x + 2, y + 2, thumbnailWidth + 2, thumbnailHeight + 2);

            gc.setBackground(getBackground());
            Point textExtent = gc.stringExtent(movieWrapper.getFilm().getNazivfilma());
            if (textExtent.x < thumbnailWidth) {
                gc.drawText(movieWrapper.getFilm().getNazivfilma(),
                        x + (thumbnailWidth - textExtent.x) / 2,
                        thumbnailHeight + y + 5);
            } else {
                int charCount = thumbnailWidth / gc.getFontMetrics().getAverageCharWidth();
                String newText = movieWrapper.getFilm().getNazivfilma().substring(0, charCount) + "...";
                textExtent = gc.stringExtent(newText);
                gc.drawText(newText, x + (thumbnailWidth - textExtent.x) / 2, thumbnailHeight + y + 5);
            }

            movieWrapper.setX(x);
            movieWrapper.setY(y);

            if (movieWrapper.getImage() != null)
                gc.drawImage(movieWrapper.getImage(), x, y);
            else
                thumbnailManager.setThumbnailOnWidget(movieWrapper);

            if (selectedItem==i) {
                Color previousForeground = gc.getForeground();
                gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                gc.drawRectangle(x-PADDING_BETWEEN_COLUMNS/2, y-PADDING_BETWEEN_ROWS/2,
                        thumbnailWidth + PADDING_BETWEEN_COLUMNS,
                        thumbnailHeight + PADDING_BETWEEN_ROWS_TEXT);
                gc.setForeground(previousForeground);
            }

            x += thumbnailWidth + PADDING_BETWEEN_COLUMNS;
            if (x + thumbnailWidth > getBounds().width) {
                x = PADDING_BETWEEN_COLUMNS;
                y += thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT;
            }
        }
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public class MovieWrapper implements ImageTargetWidget {

        private Film film;
        private Image image = null;

        private int x;
        private int y;

        public MovieWrapper(Film film) {
            this.film = film;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Film getFilm() {
            return film;
        }

        private void repaint() {
            redraw(x, y, getBounds().width, getBounds().height, true);
        }

        @Override
        public String getImdbId() {
            return film.getImdbId();
        }

        @Override
        public void setImageFromExternalFile(String absoluteFileLocation) {
            setImage(new Image(getDisplay(), absoluteFileLocation));
        }

        @Override
        public void setImageFromResource(String imageResource) {
            try {
                setImage(StreamUtil.useClasspathResource(imageResource, new Function<InputStream, Image>() {
                    @Override
                    public Image apply(@Nullable InputStream inputStream) {
                        return new Image(getDisplay(), inputStream);
                    }
                }));
            } catch (IOException e) {
                throw new IllegalArgumentException("Unexpected exception while working with the image", e);
            }
        }

        @Override
        public void safeSetImage(Image image, String imdbId) {
            if (this.image == null || this.image.isDisposed())
                return;
            if (getFilm() == null)
                return;
            setImage(image);
        }

        void dispose() {
            if (image != null && !image.isDisposed()) {
                try {
                    image.dispose();
                    image = null;
                } catch (Exception ignored) {
                }
            }
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
            this.repaint();
        }
    }

    public CoolMovieComposite(Composite parent, int style, ThumbnailManager thumbnailManager) {
        super(parent, style);
        this.thumbnailManager = thumbnailManager;
        thumbnailWidth = thumbnailManager.getThumbnailWidth();
        thumbnailHeight = thumbnailManager.getThumbnailHeight();
        setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
        shadeColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        addPaintListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                calculateMovieSelection(e.x, e.y);
            }
        });
    }

    private void calculateMovieSelection(int x, int y) {
        long column = Math.round(Math.floor((x - PADDING_BETWEEN_COLUMNS / 2) / (thumbnailWidth + PADDING_BETWEEN_COLUMNS)));
        long row = Math.round(Math.floor((y - PADDING_BETWEEN_ROWS - PADDING_BETWEEN_ROWS_TEXT) / (thumbnailHeight + PADDING_BETWEEN_ROWS)));
        long cellsPerRow = Math.round(Math.floor(getClientArea().width / (thumbnailWidth + PADDING_BETWEEN_COLUMNS)));
        long itemIndex = cellsPerRow * row + column;
        if (itemIndex < movies.size()) {
            selectedItem = (int) itemIndex;
        } else {
            selectedItem = -1;
        }
        this.redraw();
    }

    public void setMovies(List<Film> sviFilmovi) {
        clearMovies();
        List<MovieWrapper> wrappers = new ArrayList<>();
        for(Film film : sviFilmovi) {
            wrappers.add(new MovieWrapper(film));
        }
        movies = wrappers;
        redraw(0, 0, getBounds().width, getBounds().height, true);
    }

    private void clearMovies() {
        for (MovieWrapper movieWrapper : movies) {
            movieWrapper.dispose();
        }
    }

    @Override
    public void dispose() {
        clearMovies();
        super.dispose();
    }
}
