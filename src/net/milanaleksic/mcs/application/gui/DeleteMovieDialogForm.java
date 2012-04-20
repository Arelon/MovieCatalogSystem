package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.model.FilmRepository;
import net.milanaleksic.mcs.infrastructure.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class DeleteMovieDialogForm extends AbstractDialogForm {

    private Label labFilmNaziv;
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
    protected void onShellCreated() {
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        shell.setText(bundle.getString("delete.deleteMovie"));
        shell.setLayout(gridLayout1);
        shell.setSize(new Point(431, 154));
        createCanvas();

        GridData gridData = new GridData();
        gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        gridData.grabExcessHorizontalSpace = true;
        Label labUpozorenje = new Label(shell, SWT.NONE);
        labUpozorenje.setText(bundle.getString("delete.doYouReallyWishToDeleteMovie"));
        labUpozorenje.setLayoutData(gridData);
        Color color = new Color(Display.getCurrent(), 255, 0, 0);
        labUpozorenje.setForeground(color);
        color.dispose();
        labUpozorenje.setFont(new Font(Display.getDefault(), SWTUtil.getSystemFontData().getName(), 12, SWT.BOLD));

        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        gridData2.grabExcessVerticalSpace = true;
        labFilmNaziv = new Label(shell, SWT.WRAP | SWT.SHADOW_OUT | SWT.HORIZONTAL | SWT.CENTER);
        labFilmNaziv.setLayoutData(gridData2);
        labFilmNaziv.setFont(new Font(Display.getDefault(), SWTUtil.getSystemFontData().getName(), 12, SWT.BOLD));
        new Label(shell, SWT.NONE);
        createComposite();
    }

    @Override
    protected void onShellReady() {
        reReadData();
    }

    private void createComposite() {
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        gridData1.grabExcessHorizontalSpace = true;
        GridData gridData12 = new GridData();
        gridData12.horizontalAlignment = GridData.END;
        gridData12.grabExcessVerticalSpace = true;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 20;
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData1);
        Button btnOk = new Button(composite, SWT.NONE);
        btnOk.setText(bundle.getString("delete.confirmDeletion"));
        Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.setText(bundle.getString("global.cancel"));
        btnCancel.setLayoutData(gridData12);
        btnOk.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                filmRepository.deleteFilm(film.get());
                runnerWhenClosingShouldRun = true;
                shell.close();
            }
        });
        btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
    }

    private void createCanvas() {
        GridData gridData3 = new GridData();
        gridData3.verticalSpan = 2;
        gridData3.heightHint = 64;
        gridData3.widthHint = 64;
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setLayoutData(gridData3);

        Color bckg = canvas.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        canvas.setBackground(bckg);
        canvas.addPaintListener(new AlertImagePainter());
    }

}
