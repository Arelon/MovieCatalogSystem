package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.milanaleksic.mcs.application.gui.helper.event.CustomTypedListener;
import net.milanaleksic.mcs.application.gui.helper.event.MovieSelectionListener;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.infrastructure.image.ImageRepository;
import net.milanaleksic.mcs.infrastructure.thumbnail.ThumbnailManager;
import net.milanaleksic.mcs.infrastructure.thumbnail.impl.ImageTargetWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class CoolMovieComposite extends Composite implements PaintListener {

    public static final int EventMovieSelected = SWT.Gesture + 1;
    public static final int EventMovieDetailsSelected = EventMovieSelected + 1;

    private final int PADDING_BETWEEN_COLUMNS = 10;
    private final int PADDING_BETWEEN_ROWS = 10;
    private final int PADDING_BETWEEN_ROWS_TEXT = 15;

    private List<MovieWrapper> movies = new LinkedList<>();

    private final ThumbnailManager thumbnailManager;

    private final ImageRepository imageRepository;

    private final int thumbnailHeight;
    private final int thumbnailWidth;

    private int selectedIndex = -1;
    private boolean recalculateOfCellLocationsNeeded = false;

    public class MovieWrapper implements ImageTargetWidget {

        private Film film;

        private Image image = null;

        private int x;
        private int y;

        public MovieWrapper(Film film) {
            this.film = film;
        }

        public Film getFilm() {
            return film;
        }

        @Override
        public String getImdbId() {
            return film.getImdbId();
        }

        @Override
        public void setImageFromExternalFile(final String absoluteFileLocation) {
            setImage(imageRepository.getImage(absoluteFileLocation));
        }

        @Override
        public void setImageFromResource(final String imageResource) {
            setImage(imageRepository.getResourceImage(imageResource));
        }

        @Override
        public void safeSetImage(Image image, String imdbId) {
            if (image == null || image.isDisposed())
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

    public CoolMovieComposite(Composite parent, int style, ThumbnailManager thumbnailManager, ImageRepository imageRepository) {
        super(parent, style | SWT.NO_BACKGROUND);
        this.thumbnailManager = thumbnailManager;
        this.imageRepository = imageRepository;
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
        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event event) {
                recalculateOfCellLocationsNeeded = true;
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

    @SuppressWarnings({"UnusedDeclaration"})
    public int getSelectedIndex() {
        return selectedIndex;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Film getSelectedItem() {
        if (movies == null || movies.size() < selectedIndex || selectedIndex == -1)
            return null;
        return movies.get(selectedIndex).getFilm();
    }

    @SuppressWarnings({"UnusedDeclaration"})
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
            MovieWrapper movieWrapper = new MovieWrapper(film);
            wrappers.add(movieWrapper);
        }
        recalculateOfCellLocationsNeeded = true; // we can't do it now since SWT maybe can't give us dimensions
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
        Rectangle rectangleForItem = getRectangleForItem(movieWrapper);
        redraw(rectangleForItem.x, rectangleForItem.y, rectangleForItem.width, rectangleForItem.height, false);
    }

    private int getVerticalCellDistance() {
        return thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT;
    }

    private Rectangle getRectangleForItem(MovieWrapper movieWrapper) {
        return getRectangleForItemAtLocation(movieWrapper.x, movieWrapper.y);
    }

    private Rectangle getRectangleForItemAtLocation(int x, int y) {
        return new Rectangle(x - PADDING_BETWEEN_COLUMNS / 2, y - PADDING_BETWEEN_ROWS / 2,
                thumbnailWidth + PADDING_BETWEEN_COLUMNS,
                thumbnailHeight + PADDING_BETWEEN_ROWS_TEXT * 3 / 2);
    }

    @Override
    public void paintControl(PaintEvent e) {
        if (recalculateOfCellLocationsNeeded)
            recalculateCellLocations();
        GC gc = e.gc;
        gc.setAdvanced(true);
        gc.setTextAntialias(SWT.ON);

        Region backgroundRegion = new Region(getDisplay());
        backgroundRegion.add(new Rectangle(e.x, e.y, e.width, e.height));
        for (MovieWrapper wrapper : movies) {
            if ((wrapper.getImage() == null || wrapper.getImage().getImageData().alphaData == null))
                backgroundRegion.subtract(wrapper.x, wrapper.y, thumbnailWidth, thumbnailHeight);
            Point extent = gc.stringExtent(getVisibleMovieTitle(gc, wrapper));
            backgroundRegion.subtract(new Rectangle(
                    wrapper.x + (thumbnailWidth - extent.x) / 2,
                    thumbnailHeight + wrapper.y,
                    extent.x, extent.y));
        }
        gc.setClipping(backgroundRegion);
        gc.fillRectangle(e.x, e.y, e.width, e.height);

        gc.setClipping((Rectangle) null);
        backgroundRegion.dispose();

        for (int i = 0, moviesSize = movies.size(); i < moviesSize; i++) {
            MovieWrapper wrapper = movies.get(i);
            if (getRectangleForItem(wrapper).intersects(e.x, e.y, e.width, e.height)) {
                boolean isSelected = selectedIndex == i;
                if (isSelected)
                    subPaintSelectionRectangle(gc, wrapper);
                subPaintMovieTitle(gc, wrapper, isSelected);
                subPaintMovieThumbnail(gc, wrapper);
            }
        }
        setScrolledCompositeMinHeight();
    }

    private void recalculateCellLocations() {
        int x = PADDING_BETWEEN_COLUMNS, y = PADDING_BETWEEN_ROWS;
        for (MovieWrapper movieWrapper : movies) {
            movieWrapper.x = x;
            movieWrapper.y = y;
            x += thumbnailWidth + PADDING_BETWEEN_COLUMNS;
            if (x + thumbnailWidth > getParent().getBounds().width) {
                x = PADDING_BETWEEN_COLUMNS;
                y += thumbnailHeight + PADDING_BETWEEN_ROWS + PADDING_BETWEEN_ROWS_TEXT;
            }
        }
        recalculateOfCellLocationsNeeded = false;
    }

    private void setScrolledCompositeMinHeight() {
        if (getParent() instanceof ScrolledComposite) {
            if (movies.size() == 0)
                return;
            MovieWrapper movieWrapper = movies.get(movies.size() - 1);
            int y = movieWrapper.y + getVerticalCellDistance();
            ScrolledComposite scrolledParent = (ScrolledComposite) getParent();
            if (getCellsPerRow() % movies.size() == 0)
                y += getVerticalCellDistance();
            scrolledParent.setMinHeight(y);
        }
    }

    private void subPaintSelectionRectangle(GC gc, MovieWrapper movieWrapper) {
        Color previousBackground = gc.getBackground();
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
        Rectangle rectangleWithoutBorders = getRectangleForItem(movieWrapper);
        rectangleWithoutBorders.width--;
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

    private void subPaintMovieThumbnail(GC gc, MovieWrapper movieWrapper) {
        if (movieWrapper.getImage() != null)
            gc.drawImage(movieWrapper.getImage(), movieWrapper.x, movieWrapper.y);
        else
            thumbnailManager.setThumbnailOnWidget(movieWrapper);
    }

    private void subPaintMovieTitle(GC gc, MovieWrapper movieWrapper, boolean selected) {
        Color previousTextColor = gc.getForeground();
        if (selected) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
        } else {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }

        String visibleMovieTitle = getVisibleMovieTitle(gc, movieWrapper);
        Point textExtent = gc.stringExtent(visibleMovieTitle);
        gc.drawText(visibleMovieTitle,
                movieWrapper.x + (thumbnailWidth - textExtent.x) / 2,
                thumbnailHeight + movieWrapper.y, false);
        gc.setForeground(previousTextColor);
    }

    private String getVisibleMovieTitle(GC gc, MovieWrapper movieWrapper) {
        String textToShow = movieWrapper.getFilm().getNazivfilma();
        Point textExtent = gc.stringExtent(textToShow);
        if (textExtent.x > thumbnailWidth) {
            int len = movieWrapper.getFilm().getNazivfilma().length() - 1;
            textToShow = movieWrapper.getFilm().getNazivfilma().substring(0, len) + "...";
            while (textExtent.x > thumbnailWidth) {
                textExtent = gc.stringExtent(textToShow);
                textToShow = movieWrapper.getFilm().getNazivfilma().substring(0, --len) + "...";
            }
        }
        return textToShow;
    }

}
