package net.milanaleksic.mcs.application.gui;

import com.google.common.base.*;
import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.infrastructure.gui.transformer.TransformationContext;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class DeleteMovieDialogForm extends AbstractTransformedDialogForm {

    @EmbeddedComponent
    Label labFilmNaziv = null;

    private Optional<Film> film = Optional.absent();

    public static final String RESOURCE_ALERT_IMAGE = "/net/milanaleksic/mcs/application/res/alert.png"; //NON-NLS

    @Inject
    private FilmRepository filmRepository;

    private final static class AlertImagePainter implements PaintListener {
        public void paintControl(final PaintEvent e) {
            SWTUtil.useImageAndThenDispose(RESOURCE_ALERT_IMAGE, new Function<Image, Void>() {
                @Override
                public Void apply(@Nullable Image image) {
                    e.gc.drawImage(image, 0, 0);
                    return null;
                }
            });
        }
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
    protected void onTransformationComplete(TransformationContext transformer) {
        transformer.<Canvas>getMappedObject("canvas").get().addPaintListener(new AlertImagePainter()); //NON-NLS
        transformer.<Button>getMappedObject("btnOk").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) { //NON-NLS
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                filmRepository.deleteFilm(film.get());
                runnerWhenClosingShouldRun = true;
                shell.close();
            }
        });
        transformer.<Button>getMappedObject("btnCancel").get().addSelectionListener(new SelectionAdapter() { //NON-NLS
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
    }

    @Override
    protected void onShellReady() {
        reReadData();
    }

}
