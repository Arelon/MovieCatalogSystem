package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.model.FilmRepository;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class DeleteMovieForm {

	private static final Logger log = Logger.getLogger(DeleteMovieForm.class);

	private Shell sShell = null;
    private Shell parent = null;
	private Runnable parentRunner = null;
    private Label labFilmNaziv = null;
    private Film film = null;

    @Inject
    private FilmRepository filmRepository;

    public void open(Shell parent, Film film, Runnable runnable) {
		this.parent = parent;
        this.parentRunner = runnable;
		createSShell();
        if (log.isInfoEnabled())
    		log.info("DeleteMovieForm: FILMID=" + film.getIdfilm());
		sShell.setLocation(new Point(parent.getLocation().x + Math.abs(parent.getSize().x - sShell.getSize().x) / 2, parent.getLocation().y
				+ Math.abs(parent.getSize().y - sShell.getSize().y) / 2));
		this.film = film;
		reReadData();
		sShell.pack();
		sShell.open();
	}

	protected void reReadData() {
		// preuzimanje podataka za film koji se azurira
        if (film != null) {
            labFilmNaziv.setText(film.getNazivfilma() + "\n(" + film.getPrevodnazivafilma() + ")");
        }
	}

	private void createSShell() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData2.grabExcessVerticalSpace = true;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("Брисање филма");
		sShell.setLayout(gridLayout1);
		createCanvas();
		sShell.setSize(new Point(431, 154));
        Label labUpozorenje = new Label(sShell, SWT.NONE);
		labFilmNaziv = new Label(sShell, SWT.WRAP | SWT.SHADOW_OUT | SWT.HORIZONTAL | SWT.CENTER);
		labUpozorenje.setText("Да ли заиста желите да избришете филм??");
		labUpozorenje.setLayoutData(gridData);
		labUpozorenje.setForeground(new Color(Display.getCurrent(), 255, 0, 0));
		labUpozorenje.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
		labFilmNaziv.setText("[ назив филма ]");
		labFilmNaziv.setLayoutData(gridData2); 
		labFilmNaziv.setFont(new Font(Display.getDefault(), "Segoe UI", 12, SWT.BOLD));
		new Label(sShell, SWT.NONE);
		createComposite();
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
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
        Composite composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData1);
        Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setText("ОБРИШИ!");
        Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("\u041e\u0434\u0443\u0441\u0442\u0430\u043d\u0438");
		btnCancel.setLayoutData(gridData12);
		btnOk.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override public void handledSelected() throws ApplicationException {
                filmRepository.deleteFilm(film);
                parentRunner.run();
                sShell.close();
            }
        });
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                sShell.close();
            }
        });
	}

	private void createCanvas() {
		GridData gridData3 = new GridData();
		gridData3.verticalSpan = 2;
		gridData3.heightHint = 64;
		gridData3.widthHint = 64;
        Canvas canvas = new Canvas(sShell, SWT.NONE);
		canvas.setLayoutData(gridData3);

		Color bckg = canvas.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		canvas.setBackground(bckg);
		canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                Image buffer = new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/application/res/alert.png"));
                gc.drawImage(buffer, 0, 0);
                buffer.dispose();
            }
        });
	}

}
