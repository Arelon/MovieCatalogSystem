package net.milanaleksic.mcs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import net.milanaleksic.mcs.db.*;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.hibernate.*;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * @author Xp
 * 03.10.2007.
 */
public class NewOrEditMovieForm {
	
	private static Logger logger = Logger.getLogger(NewOrEditMovieForm.class);  //  @jve:decl-index=0:

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="29,0"
	private Composite composite = null;
    private Text textNaziv = null;
    private Text textPrevod = null;
    private Combo comboLokacija = null;
    private Combo comboZanr = null;
    private Text textIMDBOcena = null;
    private Composite composite2 = null;
	private List listDiskovi = null;
    private Combo comboDisk = null;
	
	private HashMap<String, Integer> sviZanrovi;  //  @jve:decl-index=0:
	private HashMap<String, Integer> sveLokacije;  //  @jve:decl-index=0:
	private HashMap<String, Integer> sviDiskovi;  //  @jve:decl-index=0:
	
	private Text textGodina = null;
    private Shell parent;
	private Runnable parentRunner = null;
    private Text textKomentar = null;
    private Integer filmId = null;
	
	public NewOrEditMovieForm(Shell parent, Integer filmId, Runnable runnable) {
		this.parent = parent;
		createSShell();
		logger.info("NewOrEditMovieForm: FILMID="+filmId);
		sShell.setLocation(
				new Point(
						parent.getLocation().x+Math.abs(parent.getSize().x-sShell.getSize().x) / 2, 
						parent.getLocation().y+Math.abs(parent.getSize().y-sShell.getSize().y) / 2 ));
		this.filmId  = filmId;
		resetControls();
		sShell.open();
		parentRunner = runnable;
	}
	
	private void resetControls() {
		textNaziv.setText("");
		textPrevod.setText("<непознат>");
		textIMDBOcena.setText("0.0");
		textGodina.setText("0");
		comboZanr.setItems(new String[] {});
		comboLokacija.setItems(new String[] {});
		comboDisk.setItems(new String[] {});
		
		reReadData();
	}

	/**
	 * Procitaj podatke iz baze podataka!
	 */
	protected void reReadData() {
		final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
		template.execute(new HibernateCallback() {
			
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
				
				// preuzimanje svih podataka od interesa i upis u kombo boksove
				refillCombos(session);
				
				// preuzimanje podataka za film koji se azurira
				if (filmId != null) {
					Query query = session.createQuery("from Film f where f.idfilm = :id");
					query.setInteger("id", filmId);
					@SuppressWarnings("unchecked")
					java.util.List list = query.list();
					if (list.size() != 1) {
						MessageBox box = new MessageBox(sShell, SWT.ICON_ERROR);
						box.setMessage("Нисам успео да јединствено идентификујем филм у бази, добио сам "+ list.size() + " одговора");
						box.setText("Грешка");
						box.open();
						logger.error("Nisam uspeo da jedinstveno identifikujem film u bazi, " +
								"na upit sam dobio " + list.size() + " odgovora");
						return null;
					}
					Film film = (Film) list.get(0);
					textNaziv.setText(film.getNazivfilma());
					textPrevod.setText(film.getPrevodnazivafilma());
					textGodina.setText(String.valueOf(film.getGodina()));
					textIMDBOcena.setText(String.valueOf(film.getImdbrejting()));
					textKomentar.setText(film.getKomentar());
					comboZanr.select(comboZanr.indexOf(film.getZanr().getZanr()));
					listDiskovi.removeAll();
					for (Medij medij : film.getMedijs())
						listDiskovi.add(medij.toString());
					comboLokacija.select( comboLokacija.indexOf( film.getFilmLocation() ) );
				}
				else {
					if (comboLokacija.indexOf("присутан") != -1)
						comboLokacija.select(comboLokacija.indexOf("присутан"));
					if (comboDisk.getItemCount() != 0)
						comboDisk.select(comboDisk.getItemCount()-1);
				}
				
				return null;
			}

		});
	}
	
	@SuppressWarnings("unchecked")
	protected void refillCombos(Session session) {
		String previousZanr = comboZanr.getText();
		String previousLokacija = comboLokacija.getText();
		String previousDisk = comboDisk.getText();
		if (comboZanr.getItemCount() != 0)
			comboZanr.removeAll();
		if (comboLokacija.getItemCount() != 0)
			comboLokacija.removeAll();
		if (comboDisk.getItemCount() != 0)
			comboDisk.removeAll();
		
		sviZanrovi = new HashMap<String, Integer>();
		sveLokacije = new HashMap<String, Integer>();
		sviDiskovi = new HashMap<String, Integer>();
		
		Query query = session.createQuery("from Zanr z order by lower(z.zanr)");
		java.util.List<Zanr> zanrovi = (java.util.List <Zanr>) query.list();
		for (Zanr zanr : zanrovi) {
			comboZanr.add(zanr.getZanr());
			sviZanrovi.put(zanr.getZanr(), zanr.getIdzanr());
		}
		query = session.createQuery("from Pozicija p order by lower(p.pozicija)");
		java.util.List<Pozicija> pozicije = (java.util.List <Pozicija>) query.list();
		for (Pozicija pozicija : pozicije) {
			comboLokacija.add(pozicija.getPozicija());
			sveLokacije.put(pozicija.getPozicija(), pozicija.getIdpozicija());
		}
		query = session.createQuery("from Medij m order by tipMedija.naziv, indeks");
		java.util.List<Medij> diskovi = (java.util.List <Medij>) query.list();
		for (Medij medij : diskovi) {
			comboDisk.add(medij.toString());
			sviDiskovi.put(medij.toString(), medij.getIdmedij());
		}
		if (previousZanr != null && previousZanr.length()>0 && comboZanr.indexOf(previousZanr)!=-1)
			comboZanr.select(comboZanr.indexOf(previousZanr));
		if (previousLokacija != null && previousLokacija.length()>0 && comboLokacija.indexOf(previousLokacija)!=-1)
			comboLokacija.select(comboLokacija.indexOf(previousLokacija));
		if (previousDisk != null && previousDisk.length()>0 && comboDisk.indexOf(previousDisk)!=-1)
			comboDisk.select(comboDisk.indexOf(previousDisk));
	}

	protected void dodajNoviFilm() {
		final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
		template.execute(new HibernateCallback() {
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
				//refillCombos(session);
				Transaction transaction = session.beginTransaction();
				Film novFilm = new Film();
				novFilm.setNazivfilma(textNaziv.getText().trim());
				novFilm.setPrevodnazivafilma(textPrevod.getText().trim());
				novFilm.setGodina(Integer.parseInt(textGodina.getText()));
				novFilm.setImdbrejting(BigDecimal.valueOf(Double.parseDouble(textIMDBOcena.getText().trim())));
				novFilm.setKomentar(textKomentar.getText());
				
				Zanr zanr = (Zanr) session.get(Zanr.class, sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex())));
				zanr.addFilm(novFilm);	
				
				if (listDiskovi.getItemCount() != 0) {
					for (String item : listDiskovi.getItems()) {
						Medij medij = (Medij) session.get(Medij.class, sviDiskovi.get(item));
						novFilm.addMedij(medij);
						Pozicija pozicija = (Pozicija) session.get(Pozicija.class, sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex())));
						pozicija.addMedij(medij);
					}
				}
				session.save(novFilm);
				
				transaction.commit();
				reReadData();
				return null;
			}
		});
	}
	
	private void izmeniFilm() {
		final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
		template.execute(new HibernateCallback() {
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
				Transaction transaction = session.beginTransaction();
				
				Query query = session.createQuery("from Film f where f.idfilm = :id");
				query.setInteger("id", filmId);
				@SuppressWarnings("unchecked")
				java.util.List<Film> list = (java.util.List<Film>) query.list();
				if (list.size() != 1) {
					logger.error("Nisam uspeo da jedinstveno identifikujem film u bazi, " +
							"na upit sam dobio " + list.size() + " odgovora");
					return null;
				}
				Film film = list.get(0);
				
				// za sledece nema problema - u pitanju je postavljanje nerefencijucih podataka
				film.setNazivfilma(textNaziv.getText().trim());
				film.setPrevodnazivafilma(textPrevod.getText().trim());
				film.setGodina(Integer.parseInt(textGodina.getText()));
				film.setImdbrejting(BigDecimal.valueOf(Double.parseDouble(textIMDBOcena.getText().trim())));
				film.setKomentar(textKomentar.getText());
				
				// promena zanra po potrebi !
				if (sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex())) != film.getZanr().getIdzanr()) {
					film.getZanr().getFilms().remove(film);
					Zanr zanr = (Zanr) session.get(Zanr.class, sviZanrovi.get(comboZanr.getItem(comboZanr.getSelectionIndex())));
					zanr.addFilm(film);					
				}

				// pocinjemo rad sa medijima - prvo da pokupimo stare
				ArrayList<String> raniji = new ArrayList<String>();
				for (Medij medij : film.getMedijs())
					raniji.add(medij.toString());
				
				// dodavanje i oduzimanje diskova po potrebi
				if (listDiskovi.getItemCount() != 0) {
					for (String item : listDiskovi.getItems()) {
						//dohvatanje medija i pozicije
						Medij medij = (Medij) session.get(Medij.class, sviDiskovi.get(item));
						Pozicija pozicija = (Pozicija) session.get(Pozicija.class, 
								sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex())));
						if (raniji.contains(medij.toString())) {
							if (medij.getPozicija().getIdpozicija() != 
									sveLokacije.get(comboLokacija.getItem(comboLokacija.getSelectionIndex()))) {
								Pozicija staraPozicija = (Pozicija) session.get(Pozicija.class, medij.getPozicija().getIdpozicija());
								staraPozicija.getMedijs().remove(medij);
								pozicija.addMedij(medij);
							}
							raniji.remove(medij.toString());// vec postoji, nema potrebe nista da se radi...
						}
						else {
							film.addMedij(medij);
							pozicija.addMedij(medij);
						}
					}
				}
				
				// mediji koji su ostali u kolekciji RANIJI su oni koji su za brisanje !
				// iz nekog cudnog razloga ovde mora da se ponovo ucita disk ili obrada nece raditi
				for (String medijOpis : raniji) {
					logger.info("Brisem: "+medijOpis);
					Medij medij = (Medij) session.get(Medij.class, sviDiskovi.get(medijOpis));
					medij.getFilms().remove(film);
					film.getMedijs().remove(medij);
				}
								
				transaction.commit();
				reReadData();
				return null;
			}
		});
	}
	
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("Додавање или измена филма");
		createComposite();
		createComposite1();
		sShell.setLayout(gridLayout);
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
		sShell.pack();
	}
	/**
	 * This method initializes composite	
	 *
	 */
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
		composite = new Composite(sShell, SWT.NONE);
		composite.setLayoutData(gridData1);
		composite.setLayout(gridLayout1);
        Label labNazivFilma = new Label(composite, SWT.RIGHT);
		labNazivFilma.setText("Назив филма:");
		labNazivFilma.setLayoutData(gridData5);
		textNaziv = new Text(composite, SWT.BORDER);
		textNaziv.setLayoutData(gridData2);
        Label labPrevod = new Label(composite, SWT.NONE);
		labPrevod.setText("Превод назива филма:");
		labPrevod.setLayoutData(gridData4);
		textPrevod = new Text(composite, SWT.BORDER);
		textPrevod.setLayoutData(gridData3);
		textPrevod.addFocusListener(new org.eclipse.swt.events.FocusListener() {
			
			public void focusLost(org.eclipse.swt.events.FocusEvent e) {    
				if (textPrevod.getText().trim().equals(""))
					textPrevod.setText("<непознат>");
			}
			public void focusGained(org.eclipse.swt.events.FocusEvent e) {
				if (textPrevod.getText().trim().equals("<непознат>"))
					textPrevod.setText("");
			}
		
		});
        Label labZanr = new Label(composite, SWT.NONE);
		labZanr.setText("Жанр:");
		labZanr.setLayoutData(gridData6);
		createComboZanr();
        Label labGodina = new Label(composite, SWT.NONE);
		labGodina.setText("Година производње:");
		labGodina.setLayoutData(gridData19);
		textGodina = new Text(composite, SWT.BORDER);
		textGodina.setLayoutData(gridData18);
        Label labLokacija = new Label(composite, SWT.NONE);
		labLokacija.setText("Локација:");
		labLokacija.setLayoutData(gridData7);
		createComboLokacija();
        Label labIMDB = new Label(composite, SWT.NONE);
		labIMDB.setText("IMDB оцена:");
		labIMDB.setLayoutData(gridData10);
		textIMDBOcena = new Text(composite, SWT.BORDER);
		textIMDBOcena.setLayoutData(gridData11);
        Label label = new Label(composite, SWT.NONE);
		label.setText("Дискови:");
		label.setLayoutData(gridData13);
		createComposite2();
        Label labKomentar = new Label(composite, SWT.NONE);
		labKomentar.setText("Коментар:");
		labKomentar.setLayoutData(gridData20);
		createComposite3();
	}
	
	/**
	 * This method initializes composite1	
	 */	 
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
        Composite composite1 = new Composite(sShell, SWT.NONE);
		composite1.setLayoutData(gridData);
		composite1.setLayout(gridLayout2);
        Button btnPrihvati = new Button(composite1, SWT.NONE);
		btnPrihvati.setText("Сними");
		btnPrihvati.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                StringBuilder razlogOtkaza = new StringBuilder();
                if (listDiskovi.getItemCount() == 0)
                    razlogOtkaza.append("\r\nМорате доделити барем један медијум");
                if (textNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\nМорате унети назив филма");
                if (textNaziv.getText().trim().equals(""))
                    razlogOtkaza.append("\r\nМорате унети превод назива филма");
                if (comboZanr.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\nМорате изабрати неки жанр за филм");
                try {
                    Integer.parseInt(textGodina.getText());
                } catch (Throwable t) {
                    razlogOtkaza.append("\r\nФормат уноса (целобројна вредност) за годину производње није исправан");
                }
                if (comboLokacija.getSelectionIndex() == -1)
                    razlogOtkaza.append("\r\nМорате изабрати неку локацију за диск");
                if (textIMDBOcena.getText().trim().equals(""))
                    razlogOtkaza.append("\r\nМорате унети неку вредност за оцену филма на IMDB-у");
                try {
                    Double.parseDouble(textIMDBOcena.getText());
                } catch (Throwable t) {
                    razlogOtkaza.append("\r\nФормат уноса (реални број) за оцену филма на IMDB-у није исправан");
                }
                if (razlogOtkaza.length() != 0) {
                    MessageBox messageBox = new MessageBox(sShell, SWT.OK | SWT.ICON_WARNING);
                    messageBox.setText("Додавање није могуће");
                    messageBox.setMessage("Разлог отказа:\r\n----------" + razlogOtkaza.toString());
                    messageBox.open();
                } else {
                    if (filmId != null)
                        izmeniFilm();
                    else
                        dodajNoviFilm();
                    parentRunner.run();
                    sShell.close();
                }
            }
        });
        Button btnOdustani = new Button(composite1, SWT.NONE);
		btnOdustani.setText("Одустани");
		btnOdustani.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                sShell.close();
            }
        });
	}

	/**
	 * This method initializes comboLokacija	
	 *
	 */
	private void createComboLokacija() {
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboLokacija = new Combo(composite, SWT.READ_ONLY);
		comboLokacija.setVisibleItemCount(10);
		comboLokacija.setLayoutData(gridData9);
	}

	/**
	 * This method initializes comboZanr	
	 *
	 */
	private void createComboZanr() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboZanr = new Combo(composite, SWT.READ_ONLY);
		comboZanr.setVisibleItemCount(10);
		comboZanr.setLayoutData(gridData8);
	}

	/**
	 * This method initializes composite2	
	 *
	 */
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
		btnDodajDisk.setText("Додај диск");
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
		btnOduzmi.setText("Одузми диск");
		btnOduzmi.setLayoutData(gridData16);
		btnOduzmi.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (listDiskovi.getSelectionIndex() != -1)
                    listDiskovi.remove(listDiskovi.getSelectionIndex());
            }
        });
        Label label1 = new Label(composite2, SWT.NONE);
		label1.setText("Додавање:");
		label1.setLayoutData(gridData22);
        Button btnNovMedij = new Button(composite2, SWT.NONE);
		btnNovMedij.setText("Нов диск ...");
		btnNovMedij.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                new NewMediumForm(sShell, new Runnable() {

                    @Override
                    public void run() {
                        final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
                        template.execute(new HibernateCallback() {

                            public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException, java.sql.SQLException {
                                // preuzimanje svih podataka od interesa i upis u kombo boksove
                                refillCombos(session);
                                if (comboDisk.getItemCount() > 0)
                                    comboDisk.select(comboDisk.getItemCount() - 1);
                                return null;
                            }

                        });
                    }

                });
            }
        });
	}

	/**
	 * This method initializes comboDiskovi	
	 *
	 */
	private void createComboDiskovi() {
		GridData gridData14 = new GridData();
		gridData14.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		comboDisk = new Combo(composite2, SWT.READ_ONLY);
		comboDisk.setVisibleItemCount(10);
		comboDisk.setLayoutData(gridData14);
	}

	/**
	 * This method initializes composite3	
	 *
	 */
	private void createComposite3() {
		GridData gridData21 = new GridData();
		gridData21.widthHint = 130;
		gridData21.verticalSpan = 2;
		gridData21.heightHint = 50;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 2;
        Composite composite3 = new Composite(composite, SWT.NONE);
		composite3.setLayout(gridLayout4);
		textKomentar = new Text(composite3, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textKomentar.setLayoutData(gridData21);
        Button btnNeodgledano = new Button(composite3, SWT.NONE);
		btnNeodgledano.setText("Нисам гледао");
		btnNeodgledano.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + "нисам гледао");
            }
        });
        Button btnLos = new Button(composite3, SWT.NONE);
		btnLos.setText("Лош (снимак)");
		btnLos.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                textKomentar.setText(textKomentar.getText() + (textKomentar.getText().length() > 0 ? " " : "") + "лош");
            }
        });
	}

}
