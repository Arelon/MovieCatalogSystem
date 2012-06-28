package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class DeleteMovieDialogForm extends AbstractTransformedForm {

    @EmbeddedComponent
    private Label labFilmNaziv = null;

    private Optional<Film> film = Optional.absent();

    public static final String RESOURCE_ALERT_IMAGE = "/net/milanaleksic/mcs/application/res/alert.png"; //NON-NLS

    @Inject
    private FilmRepository filmRepository;

    @EmbeddedEventListener(component = "canvas", event = SWT.Paint)
    private void paintListener(final Event e) {
        final Rectangle bounds = ((Canvas)e.widget).getBounds();
        SWTUtil.useImageAndThenDispose(RESOURCE_ALERT_IMAGE, new Function<Image, Void>() {
            @Override
            public Void apply(@Nullable Image image) {
                if (image == null)
                    return null;
                e.gc.drawImage(image,
                        (bounds.width - image.getBounds().width)/2,
                        (bounds.height - image.getBounds().height)/2);
                return null;
            }
        });
    }

    @EmbeddedEventListener(component = "btnOk", event = SWT.Selection)
    private void btnOkSelectionListener() {
        filmRepository.deleteFilm(film.get());
        runnerWhenClosingShouldRun = true;
        shell.close();
    }

    @EmbeddedEventListener(component = "btnCancel", event = SWT.Selection)
    private void btnCancelSelectionListener() {
        shell.close();
    }

    public void open(Shell parent, Film film, Runnable callback) {
        this.film = Optional.of(film);
        super.open(parent, callback);
    }

    protected void reReadData() {
        // preuzimanje podataka za film koji se azurira
        if (film.isPresent()) {
            labFilmNaziv.setText(film.get().getNazivfilma() + "\n(" + film.get().getPrevodnazivafilma() + ")");
        }
    }

    @Override
    protected void onShellReady() {
        reReadData();
    }

}
