package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.OfferMovieList;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.FilmService;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

public class NewOrEditMovieDialogForm extends AbstractDialogForm {
	
    @Inject private NewMediumDialogForm newMediumDialogForm;

    @Inject private FilmRepository filmRepository;

    @Inject private ZanrRepository zanrRepository;

    @Inject private MedijRepository medijRepository;

    @Inject private PozicijaRepository pozicijaRepository;

    @Inject private OfferMovieList offerMovieList;

    @Inject private FilmService filmService;

    private Composite composite = null;
    private Composite composite2 = null;
    private Combo comboLokacija = null;
    private Combo comboZanr = null;
    private Combo comboDisk = null;
    private List listDiskovi = null;
    private Combo comboNaziv = null;
    private Text textPrevod = null;
    private Text textImdbId = null;
    private Text textGodina = null;
    private Text textKomentar = null;

    private Film activeFilm = null;

    private HashMap<String, Zanr> sviZanrovi;
    private HashMap<String, Pozicija> sveLokacije;
    private HashMap<String, Medij> sviDiskovi;
    private static final Pattern PATTERN_IMDB_ID = Pattern.compile("tt\\d{7}");

    public void open(Shell parent, @Nullable Film film, Runnable runnable) {
		this.activeFilm  = film;
        super.open(parent, runnable);
	}
	
	protected void reReadData() {
        // preuzimanje svih podataka od interesa i upis u kombo boksove
        refillCombos();

        // preuzimanje podataka za film koji se azurira
        if (activeFilm != null) {
            comboNaziv.setText(activeFilm.getNazivfilma());
            textPrevod.setText(activeFilm.getPrevodnazivafilma());
            textGodina.setText(String.valueOf(activeFilm.getGodina()));
            textImdbId.setText(String.valueOf(activeFilm.getImdbId()));
            textKomentar.setText(activeFilm.getKomentar());
            comboZanr.select(comboZanr.indexOf(activeFilm.getZanr().getZanr()));
            listDiskovi.removeAll();
            for (Medij medij : activeFilm.getMedijs())
                listDiskovi.add(medij.toString());
            int indexOfPozicija = comboLokacija.indexOf(activeFilm.getPozicija());
            if (indexOfPozicija >= 0)
                comboLokacija.select(indexOfPozicija);
        }
        else {
            Pozicija defaultPozicija = pozicijaRepository.getDefaultPozicija();
            if (defaultPozicija != null) {
                String nameOfDefaultPosition = defaultPozicija.getPozicija();
                if (comboLokacija.indexOf(nameOfDefaultPosition) != -1)
                    comboLokacija.select(comboLokacija.indexOf(nameOfDefaultPosition));
                if (comboDisk.getItemCount() != 0)
                    comboDisk.select(comboDisk.getItemCount()-1);
            }
        }
	}
	
	@SuppressWarnings("unchecked")
	protected void refillCombos() {
		String previousZanr = comboZanr.getText();
		String previousLokacija = comboLokacija.getText();
		String previousDisk = comboDisk.getText();
		if (comboZanr.getItemCount() != 0)
			comboZanr.removeAll();
		if (comboLokacija.getItemCount() != 0)
			comboLokacija.removeAll();
		if (comboDisk.getItemCount() != 0)
			comboDisk.removeAll();

		sviZanrovi = new HashMap<>();
		sveLokacije = new HashMap<>();
		sviDiskovi = new HashMap<>();

		for (Zanr zanr : zanrRepository.getZanrs()) {
			comboZanr.add(zanr.getZanr());
			sviZanrovi.put(zanr.getZanr(), zanr);
		}
		for (Pozicija pozicija : pozicijaRepository.getPozicijas()) {
			comboLokacija.add(pozicija.getPozicija());
			sveLokacije.put(pozicija.getPozicija(), pozicija);
		}

		for (Medij medij : medijRepository.getMedijs()) {
			comboDisk.add(medij.toString());
			sviDiskovi.put(medij.toString(), medij);
		}
		if (previousZanr != null && previousZanr.length()>0 && comboZanr.indexOf(previousZanr)!=-1)
			comboZanr.select(comboZanr.indexOf(previousZanr));
		if (previousLokacija != null && previousLokacija.length()>0 && comboLokacija.indexOf(previousLokacija)!=-1)
			comboLokacija.select(comboLokacija.indexOf(previousLokacija));
		if (previousDisk != null && previousDisk.length()>0 && comboDisk.indexOf(previousDisk)!=-1)
			comboDisk.select(comboDisk.indexOf(previousDisk));
	}

	protected void dodajNoviFilm() {
        refillCombos();
        Film novFilm = new Film();
        novFilm.setNazivfilma(comboNaziv.getText().trim());
        novFilm.setPrevodnazivafilma(textPrevod.getText().trim());
        novFilm.setGodina(Integer.parseInt(textGodina.getText()));
        novFilm.setImdbId(textImdbId.getText().trim());
        novFilm.setKomentar(textKomentar.getText());
        Zanr zanr = sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex()));
        Pozicija position = sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex()));
        java.util.List<Medij> medijs = new ArrayList<>();
        if (listDiskovi.getItemCount() != 0) {
            for (String medijName : listDiskovi.getItems()) {
                Medij medij = sviDiskovi.get(medijName);
                medijs.add(medij);
            }
        }
        filmRepository.saveFilm(novFilm, zanr, medijs, position);
        reReadData();
	}
	
	private void izmeniFilm() {
        activeFilm.setNazivfilma(comboNaziv.getText().trim());
        activeFilm.setPrevodnazivafilma(textPrevod.getText().trim());
        activeFilm.setGodina(Integer.parseInt(textGodina.getText()));
        activeFilm.setImdbId(textImdbId.getText().trim());
        activeFilm.setKomentar(textKomentar.getText());

        Set<Medij> selectedMediums = new HashSet<>();
        for (String item : listDiskovi.getItems()) {
            selectedMediums.add(sviDiskovi.get(item));
        }

        filmService.updateFilmWithChanges(activeFilm,
                sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex())),
                sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex())),
                selectedMediums);

        reReadData();
	}
	
	
	@Override protected void onShellCreated() {
        shell.setText(bundle.getString("newOrEdit.addingOrModifyingMovie"));
        shell.setLayout(new GridLayout(1, false));
		shell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
                if (offerMovieList != null)
                    offerMovieList.cleanup();
			}
		});
        createComposite();
        createComposite1();
    }

    @Override protected void onShellReady() {
        comboNaziv.addKeyListener(prepareOfferMovieListForCombo(comboNaziv));
        reReadData();
	}

	private void createComposite() {
		GridData gridData20 = new GridData();
		gridData20.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData19 = new GridData();
		gridData19.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData18 = new GridData();
		gridData18.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData13 = new GridData();
		gridData13.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData3 = new GridData();
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		gridLayout1.horizontalSpacing = 10;
		gridLayout1.makeColumnsEqualWidth = false;
		GridData gridData1 = new GridData();
		gridData1.grabExcessVerticalSpace = true;
		gridData1.heightHint = -1;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gridData1.grabExcessHorizontalSpace = true;
		composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(gridData1);
		composite.setLayout(gridLayout1);
        Label labNazivFilma = new Label(composite, SWT.RIGHT);
		labNazivFilma.setText(bundle.getString("newOrEdit.movieName"));
		labNazivFilma.setLayoutData(gridData5);
		comboNaziv = new Combo(composite, SWT.DROP_DOWN);
        comboNaziv.setVisibleItemCount(10);
		comboNaziv.setLayoutData(gridData2);
        comboNaziv.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = comboNaziv.getSelectionIndex();
                if (index != -1) {
                    Movie movie = (Movie)comboNaziv.getData(Integer.toString(index));
                    if (movie == null)
                        return;
                    textGodina.setText(movie.getReleasedYear());
                    comboNaziv.setText(movie.getName());
                    textImdbId.setText(movie.getImdbId());
                    textKomentar.setText(movie.getOverview());
                }
            }
        });
        Label labPrevod = new Label(composite, SWT.NONE);
		labPrevod.setText(bundle.getString("newOrEdit.movieNameTranslated"));
		labPrevod.setLayoutData(gridData4);
		textPrevod = new Text(composite, SWT.BORDER);
		textPrevod.setLayoutData(gridData3);
		textPrevod.addFocusListener(new org.eclipse.swt.events.FocusListener() {

            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                if (textPrevod.getText().trim().equals(""))
                    textPrevod.setText(bundle.getString("newOrEdit.unknown"));
            }

            public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                if (textPrevod.getText().trim().equals(bundle.getString("newOrEdit.unknown")))
                    textPrevod.setText("");
            }

        });
        Label labZanr = new Label(composite, SWT.NONE);
		labZanr.setText(bundle.getString("newOrEdit.genre"));
		labZanr.setLayoutData(gridData6);
		createComboZanr();
        Label labGodina = new Label(composite, SWT.NONE);
		labGodina.setText(bundle.getString("newOrEdit.yearPublished"));
		labGodina.setLayoutData(gridData19);
		textGodina = new Text(composite, SWT.BORDER);
		textGodina.setLayoutData(gridData18);
        Label labLokacija = new Label(composite, SWT.NONE);
		labLokacija.setText(bundle.getString("newOrEdit.location"));
		labLokacija.setLayoutData(gridData7);
		createComboLokacija();
        Label labIMDB = new Label(composite, SWT.NONE);
		labIMDB.setText(bundle.getString("newOrEdit.imdbIdFormat"));
		labIMDB.setLayoutData(gridData10);
		textImdbId = new Text(composite, SWT.BORDER);
		textImdbId.setLayoutData(gridData11);
        Label label = new Label(composite, SWT.NONE);
		label.setText(bundle.getString("newOrEdit.mediums"));
		label.setLayoutData(gridData13);
		createComposite2();
        Label labKomentar = new Label(composite, SWT.NONE);
		labKomentar.setText(bundle.getString("newOrEdit.comment"));
		labKomentar.setLayoutData(gridData20);
		createComposite3();
	}

    private OfferMovieList prepareOfferMovieListForCombo(Combo queryCombo) {
        offerMovieList.startup();
        offerMovieList.setQueryField(queryCombo);
        return offerMovieList;
    }

    private void createComposite1() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		gridLayout2.verticalSpacing = 5;
		gridLayout2.marginWidth = 5;
		gridLayout2.marginHeight = 10;
		gridLayout2.horizontalSpacing = 40;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = -1;
		gridData.horizontalIndent = 0;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        Composite composite1 = new Composite(shell, SWT.NONE);
		composite1.setLayoutData(gridData);
		composite1.setLayout(gridLayout2);
        Button btnPrihvati = new Button(composite1, SWT.NONE);
		btnPrihvati.setText(bundle.getString("global.save"));
		btnPrihvati.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                //TODO: replace with Hibernate Validator (JSR 303 implementation)
                StringBuilder razlogOtkaza = new StringBuilder();
                if (listDiskovi.getItemCount() == 0)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.atLeastOneMediumNeeded"));
                if (comboNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.movieMustHaveName"));
                if (comboNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.movieMustHaveNameTranslation"));
                if (comboZanr.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.genreMustBeSelected"));
                try {
                    Integer.parseInt(textGodina.getText());
                } catch (Throwable t) {
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.yearNotANumber"));
                }
                if (comboLokacija.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.locationMustBeSelected"));
                if (!textImdbId.getText().isEmpty() &&
                        !PATTERN_IMDB_ID.matcher(textImdbId.getText()).matches())
                    razlogOtkaza.append("\r\n").append(bundle.getString("newOrEdit.imdbFormatNotOk"));
                if (razlogOtkaza.length() != 0) {
                    MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
                    messageBox.setText(bundle.getString("newOrEdit.movieCouldNotBeAdded"));
                    messageBox.setMessage(bundle.getString("newOrEdit.cancellationCause") + "\r\n----------" + razlogOtkaza.toString());
                    messageBox.open();
                } else {
                    if (activeFilm != null)
                        izmeniFilm();
                    else
                        dodajNoviFilm();
                    runnerWhenClosingShouldRun = true;
                    shell.close();
                }
            }
        });
        Button btnOdustani = new Button(composite1, SWT.NONE);
		btnOdustani.setText(bundle.getString("global.cancel"));
		btnOdustani.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
	}

	private void createComboLokacija() {
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboLokacija = new Combo(composite, SWT.READ_ONLY);
		comboLokacija.setVisibleItemCount(10);
		comboLokacija.setLayoutData(gridData9);
	}

	private void createComboZanr() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboZanr = new Combo(composite, SWT.READ_ONLY);
		comboZanr.setVisibleItemCount(10);
		comboZanr.setLayoutData(gridData8);
	}

	private void createComposite2() {
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridData gridData15 = new GridData();
		gridData15.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		GridData gridData17 = new GridData();
		gridData17.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData17.heightHint = 50;
		GridData gridData16 = new GridData();
		gridData16.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 2;
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		composite2 = new Composite(composite, SWT.NONE);
		composite2.setLayoutData(gridData12);
		composite2.setLayout(gridLayout3);
		createComboDiskovi();
        Button btnDodajDisk = new Button(composite2, SWT.NONE);
		btnDodajDisk.setText(bundle.getString("newOrEdit.addMedium"));
		btnDodajDisk.setLayoutData(gridData15);
		btnDodajDisk.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                int index = comboDisk.getSelectionIndex();
                if (index != -1) {
                    if (listDiskovi.indexOf(comboDisk.getItem(index)) == -1) {
                        listDiskovi.add(comboDisk.getItem(index));
                    }
                }
            }
        });
		listDiskovi = new List(composite2, SWT.NONE);
		listDiskovi.setLayoutData(gridData17);
        Button btnOduzmi = new Button(composite2, SWT.NONE);
		btnOduzmi.setText(bundle.getString("newOrEdit.removeMedium"));
		btnOduzmi.setLayoutData(gridData16);
		btnOduzmi.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (listDiskovi.getSelectionIndex() != -1)
                    listDiskovi.remove(listDiskovi.getSelectionIndex());
            }
        });
        Label label1 = new Label(composite2, SWT.NONE);
		label1.setText(bundle.getString("newOrEdit.add"));
		label1.setLayoutData(gridData22);
        Button btnNovMedij = new Button(composite2, SWT.NONE);
		btnNovMedij.setText(bundle.getString("newOrEdit.newMedium"));
		btnNovMedij.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                newMediumDialogForm.open(shell, new Runnable() {

                    @Override
                    public void run() {
                        // preuzimanje svih podataka od interesa i upis u kombo boksove
                        refillCombos();
                        if (comboDisk.getItemCount() > 0)
                            comboDisk.select(comboDisk.getItemCount() - 1);
                    }

                });
            }
        });
	}

	private void createComboDiskovi() {
		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboDisk = new Combo(composite2, SWT.READ_ONLY);
		comboDisk.setVisibleItemCount(10);
		comboDisk.setLayoutData(gridData14);
	}

	private void createComposite3() {
		GridData gridData21 = new GridData();
		gridData21.widthHint = 200;
		gridData21.verticalSpan = 2;
		gridData21.heightHint = 80;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 2;
        Composite composite3 = new Composite(composite, SWT.NONE);
		composite3.setLayout(gridLayout4);
		textKomentar = new Text(composite3, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textKomentar.setLayoutData(gridData21);
        Button btnNeodgledano = new Button(composite3, SWT.NONE);
		btnNeodgledano.setText(bundle.getString("newOrEdit.iDidNotWatch"));
		btnNeodgledano.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + bundle.getString("newOrEdit.iDidNotWatch"));
            }
        });
        Button btnLos = new Button(composite3, SWT.NONE);
		btnLos.setText(bundle.getString("newOrEdit.badRecording"));
		btnLos.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + bundle.getString("newOrEdit.badRecording"));
            }
        });
	}

    @MethodTiming
    public void setCurrentQueryItems(final String query, final String[] newItems, @Nullable final Movie[] newMovies) {
        if (shell.isDisposed())
            return;
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (query.equals(comboNaziv.getText())) {
                    Point selection = comboNaziv.getSelection();
                    comboNaziv.setItems(newItems);
                    comboNaziv.setListVisible(false);
                    comboNaziv.setListVisible(true);
                    comboNaziv.setText(query);
                    if (newMovies != null) {
                        for (int i = 0; i < newMovies.length; i++)
                            comboNaziv.setData("" + i, newMovies[i]);
                    }
                    comboNaziv.setSelection(selection);
                }
            }
        });
    }

}
