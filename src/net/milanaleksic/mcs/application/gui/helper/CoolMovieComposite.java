package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Function;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.thumbnail.impl.ImageTargetWidget;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 3/19/12
 * Time: 4:03 PM
 */
public class CoolMovieComposite extends Composite implements PaintListener {

    private List<MovieWrapper> movies = new LinkedList<>();

    private ThumbnailManager thumbnailManager;

    @Override
    public void paintControl(PaintEvent e) {
        final int PADDING = 10;
        GC gc = e.gc;
        int x = PADDING;
        int y = PADDING;
        int width = 92;
        int height = 138;
        for (MovieWrapper movieWrapper : movies) {
            // TODO: paint only if it intercepts drawing region
            gc.drawRectangle(x, y, width, height);
            gc.drawText(movieWrapper.getFilm().getNazivfilma(), x + 2, y + 2);
            movieWrapper.setX(x);
            movieWrapper.setY(y);

            if (movieWrapper.getImage() != null)
                gc.drawImage(movieWrapper.getImage(), x, y);
            else
                thumbnailManager.setThumbnailOnWidget(movieWrapper);

            x += width + PADDING;
            if (x + width > getBounds().width) {
                x = PADDING;
                y += height + PADDING;
            }
        }
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
        addPaintListener(this);
    }

    public void setMovies(List<Film> sviFilmovi) {
        clearMovies();
        List<MovieWrapper> wrappers = new LinkedList<>();
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
