package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.model.FilmRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class DeleteMovieDialogForm extends AbstractDialogForm {

    private Label labFilmNaziv = null;
    private Film film = null;

    @Inject private FilmRepository filmRepository;

    private final static class AlertImagePainter implements PaintListener {

        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            Image buffer = new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/application/res/alert.png"));
            gc.drawImage(buffer, 0, 0);
            buffer.dispose();
        }
    }

    public void open(Shell parent, Film film, Runnable runnable) {
        this.film = film;
        super.open(parent, runnable);
	}

	protected void reReadData() {
		// preuzimanje podataka za film koji se azurira
        if (film != null) {
            labFilmNaziv.setText(film.getNazivfilma() + "\n(" + film.getPrevodnazivafilma() + ")");
        }
	}

	@Override protected void onShellCreated() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData2.grabExcessVerticalSpace = true;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		shell.setText(bundle.getString("delete.deleteMovie"));
		shell.setLayout(gridLayout1);
		shell.setSize(new Point(431, 154));
        createCanvas();
        Label labUpozorenje = new Label(shell, SWT.NONE);
		labUpozorenje.setText(bundle.getString("delete.doYouReallyWishToDeleteMovie"));
        labUpozorenje.setLayoutData(gridData);
        labUpozorenje.setForeground(new Color(Display.getCurrent(), 255, 0, 0));
        labUpozorenje.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
        labFilmNaziv = new Label(shell, SWT.WRAP | SWT.SHADOW_OUT | SWT.HORIZONTAL | SWT.CENTER);
        labFilmNaziv.setLayoutData(gridData2);
		labFilmNaziv.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
		new Label(shell, SWT.NONE);
		createComposite();
    }

    @Override protected void onShellReady() {
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
            @Override public void handledSelected(SelectionEvent event) throws ApplicationException {
                filmRepository.deleteFilm(film);
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
