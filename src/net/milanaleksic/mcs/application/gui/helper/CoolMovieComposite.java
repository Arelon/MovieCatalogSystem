package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.milanaleksic.mcs.application.gui.helper.event.CustomTypedListener;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionListener;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.thumbnail.impl.ImageTargetWidget;
import net.milanaleksic.mcs.infrastructure.util.StreamUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CoolMovieComposite extends Composite implements PaintListener {

    public static final int EventMovieSelected = SWT.Gesture + 1;
    public static final int EventMovieDetailsSelected = EventMovieSelected + 1;

    private final int PADDING_BETWEEN_COLUMNS = 10;
    private final int PADDING_BETWEEN_ROWS = 10;
    private final int PADDING_BETWEEN_ROWS_TEXT = 15;

    private List<MovieWrapper> movies = new LinkedList<>();

    private final ThumbnailManager thumbnailManager;
    private final int thumbnailHeight;
    private final int thumbnailWidth;

    private int selectedIndex = -1;

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
            redrawItem(this);
        }
    }

    public CoolMovieComposite(Composite parent, int style, ThumbnailManager thumbnailManager) {
        super(parent, style | SWT.NO_BACKGROUND);
        this.thumbnailManager = thumbnailManager;
        thumbnailWidth = thumbnailManager.getThumbnailWidth();
        thumbnailHeight = thumbnailManager.getThumbnailHeight();
        setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        addListeners();
    }

    private void fireMovieSelectionEvent() {
        Event event = new Event();
        event.data = getSelectedItem();
        super.notifyListeners(EventMovieSelected, event);
    }

    private void fireMovieDetailsSelectionEvent() {
        Event event = new Event();
        event.data = getSelectedItem();
        super.notifyListeners(EventMovieDetailsSelected, event);
    }

    public void addMovieSelectionListener(MovieSelectionListener listener) {
        checkWidget();
        if (listener == null)
            throw new SWTException(SWT.ERROR_NULL_ARGUMENT);
        CustomTypedListener typedListener = new CustomTypedListener(listener);
        addListener(EventMovieSelected, typedListener);
        addListener(EventMovieDetailsSelected, typedListener);
    }

    private void addListeners() {
        addPaintListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                calculateMovieSelection(e.x, e.y);
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                calculateMovieSelection(e.x, e.y);
                if (selectedIndex != -1)
                    fireMovieDetailsSelectionEvent();
            }
        });
        addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                switch (e.detail) {
                    case SWT.TRAVERSE_RETURN:
                        fireMovieDetailsSelectionEvent();
                        break;
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int targetIndex = selectedIndex;
                switch (e.keyCode) {
                    case SWT.ARROW_RIGHT:
                        targetIndex++;
                        break;
                    case SWT.ARROW_LEFT:
                        targetIndex--;
                        break;
                    case SWT.ARROW_DOWN:
                        targetIndex += getCellsPerRow();
                        break;
                    case SWT.ARROW_UP:
                        targetIndex -= getCellsPerRow();
                        break;
                }
                if (targetIndex != selectedIndex && targetIndex >= 0 && targetIndex < movies.size()) {
                    int previousSelectedIndex = selectedIndex;
                    selectedIndex = -1;
                    redrawItem(previousSelectedIndex);
                    setSelectedIndex(targetIndex);
                    redrawItem(targetIndex);
                }
            }
        });
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

    // standard operations on item collection

    public int size() {
        if (movies == null)
            return 0;
        return movies.size();
    }

    public void setSelectedIndex(int selectedItem) {
        this.selectedIndex = selectedItem;
        fireMovieSelectionEvent();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public Film getSelectedItem() {
        if (movies == null || movies.size() < selectedIndex || selectedIndex == -1)
            return null;
        return movies.get(selectedIndex).getFilm();
    }

    public Iterator<Film> iterator() {
        return Lists.transform(movies, new Function<MovieWrapper, Film>() {
            @Override
            public Film apply(@Nullable MovieWrapper movieWrapper) {
                if (movieWrapper == null)
                    return null;
                return movieWrapper.getFilm();
            }
        }).iterator();
    }

    public void setMovies(List<Film> sviFilmovi) {
        clearMovies();
        List<MovieWrapper> wrappers = new ArrayList<>();
        for (Film film : sviFilmovi) {
            wrappers.add(new MovieWrapper(film));
        }
        movies = wrappers;
        redraw();
        fireMovieSelectionEvent();
    }

    // Painting algorithms

    private void calculateMovieSelection(int x, int y) {
        long column = Math.round(Math.floor((x - PADDING_BETWEEN_COLUMNS / 2) / (thumbnailWidth + PADDING_BETWEEN_COLUMNS)));
        long row = Math.round(Math.floor((y - PADDING_BETWEEN_ROWS / 2) / (thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT)));
        long cellsPerRow = getCellsPerRow();
        long itemIndex = cellsPerRow * row + column;
        int prevSelectedItem = selectedIndex;
        selectedIndex = -1;
        redrawItem(prevSelectedItem);
        if (itemIndex < movies.size() && column < cellsPerRow) {
            setSelectedIndex((int) itemIndex);
        } else {
            setSelectedIndex(-1);
        }
        redrawItem(selectedIndex);
    }

    private long getCellsPerRow() {
        return Math.round(Math.floor(getClientArea().width / (thumbnailWidth + PADDING_BETWEEN_COLUMNS)));
    }

    private void redrawItem(int selectedItem) {
        if (selectedItem == -1 || selectedItem >= movies.size())
            return;
        MovieWrapper movieWrapper = movies.get(selectedItem);
        redrawItem(movieWrapper);
    }

    private void redrawItem(MovieWrapper movieWrapper) {
        Rectangle rectangleForItem = getRectangleForItem(movieWrapper.x, movieWrapper.y);
        redraw(rectangleForItem.x, rectangleForItem.y, rectangleForItem.width, rectangleForItem.height, false);
    }

    @Override
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        gc.setAdvanced(true);
        gc.setTextAntialias(SWT.ON);
        // we explicitly paint our background since SWT handling is turned off
        gc.fillRectangle(e.x, e.y, e.width, e.height);

        int x = PADDING_BETWEEN_COLUMNS, y = PADDING_BETWEEN_ROWS;
        for (int i = 0, moviesSize = movies.size(); i < moviesSize; i++) {
            // only draw items we have to
            Rectangle rectangleForItem = getRectangleForItem(x, y);
            if (rectangleForItem.intersects(e.x, e.y, e.width, e.height)) {
                MovieWrapper movieWrapper = movies.get(i);
                movieWrapper.setX(x);
                movieWrapper.setY(y);

                boolean isSelected = selectedIndex == i;
                if (isSelected)
                    subPaintSelectionRectangle(gc, x, y);
                subPaintMovieTitle(gc, x, y, movieWrapper, isSelected);
                subPaintMovieThumbnail(gc, x, y, movieWrapper);
            }

            x += thumbnailWidth + PADDING_BETWEEN_COLUMNS;
            if (x + thumbnailWidth > getBounds().width) {
                x = PADDING_BETWEEN_COLUMNS;
                y += thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT;
            }
        }
        setScrolledCompositeMinHeight(y);
    }

    private void setScrolledCompositeMinHeight(int y) {
        if (getParent() instanceof ScrolledComposite) {
            ScrolledComposite scrolledParent = (ScrolledComposite) getParent();
            if (getCellsPerRow() % movies.size() == 0)
                y += thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT;
            scrolledParent.setMinHeight(y);
        }
    }

    private void subPaintSelectionRectangle(GC gc, int x, int y) {
        Color previousBackground = gc.getBackground();
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
        Rectangle rectangleWithoutBorders = getRectangleForItem(x, y);
        rectangleWithoutBorders.width--;
        rectangleWithoutBorders.height--;
        gc.fillRectangle(rectangleWithoutBorders);
        gc.setBackground(previousBackground);

        int previousLineStyle = gc.getLineStyle();
        Color previousForeground = gc.getForeground();
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        rectangleWithoutBorders.width--;
        rectangleWithoutBorders.height--;
        gc.drawRectangle(rectangleWithoutBorders);
        gc.setForeground(previousForeground);
        gc.setLineStyle(previousLineStyle);
    }

    private void subPaintMovieThumbnail(GC gc, int x, int y, MovieWrapper movieWrapper) {
        if (movieWrapper.getImage() != null)
            gc.drawImage(movieWrapper.getImage(), x, y);
        else
            thumbnailManager.setThumbnailOnWidget(movieWrapper);
    }

    private void subPaintMovieTitle(GC gc, int x, int y, MovieWrapper movieWrapper, boolean selected) {
        Color previousTextColor = gc.getForeground();
        if (selected)
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
        else
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        Point textExtent = gc.stringExtent(movieWrapper.getFilm().getNazivfilma());
        if (textExtent.x < thumbnailWidth) {
            gc.drawText(movieWrapper.getFilm().getNazivfilma(),
                    x + (thumbnailWidth - textExtent.x) / 2,
                    thumbnailHeight + y + 2, true);
        } else {
            int charCount = thumbnailWidth / gc.getFontMetrics().getAverageCharWidth();
            String newText = movieWrapper.getFilm().getNazivfilma().substring(0, charCount) + "...";
            textExtent = gc.stringExtent(newText);
            gc.drawText(newText, x + (thumbnailWidth - textExtent.x) / 2, thumbnailHeight + y + 2, true);
        }
        gc.setForeground(previousTextColor);
    }

    private Rectangle getRectangleForItem(int x, int y) {
        return new Rectangle(x - PADDING_BETWEEN_COLUMNS / 2, y - PADDING_BETWEEN_ROWS / 2,
                thumbnailWidth + PADDING_BETWEEN_COLUMNS,
                thumbnailHeight + PADDING_BETWEEN_ROWS_TEXT * 3 / 2);
    }

}
