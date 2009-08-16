package net.milanaleksic.mcs;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.milanaleksic.mcs.db.Film;
import net.milanaleksic.mcs.db.Pozicija;
import net.milanaleksic.mcs.db.TipMedija;
import net.milanaleksic.mcs.db.Zanr;
import net.milanaleksic.mcs.export.Exporter;
import net.milanaleksic.mcs.export.ExporterFactory;
import net.milanaleksic.mcs.export.ExporterSource;
import net.milanaleksic.mcs.util.FilmInfo;
import net.milanaleksic.mcs.util.MCSProperties;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;



public class MainForm {
	
	private static Logger log = Logger.getLogger(MainForm.class);  //  @jve:decl-index=0:
	
	private final String titleConst = "Movie Catalog System (C) by Milan.Aleksic@gmail.com";

	public Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="11,7"
	private Combo comboZanr = null;
	private Combo comboTipMedija = null;
	private Table mainTable = null;
	private Combo comboPozicija = null;
	private Composite panCombos = null;
	private ToolBar toolBar = null;
	private ArrayList<Integer> indeksi = new ArrayList<Integer>();  //  @jve:decl-index=0:
	protected String filterText = null;
	protected String targetFileForExport;

	private ToolBar toolTicker = null;

	public MainForm() {
		createSShell();
		sShell.open();
		mainTable.setFocus();
	}
	
	public boolean isDisposed() {
		return sShell.isDisposed();
	}

	private void createSShell() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		sShell = new Shell();
		sShell.setText(titleConst);
		sShell.setMaximized(false);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		sShell.setBounds(40, 40, 850, size.height-80);
		createToolTicker();
		createPanCombos();
		createToolBar();
		sShell.setLayout(gridLayout);
		mainTable = new Table(sShell, SWT.FULL_SELECTION);
		mainTable.setHeaderVisible(true);
		mainTable.setFont(new Font(Display.getDefault(), MCSProperties.getTableFont(), 12, SWT.NORMAL));
		mainTable.setLayoutData(gridData);
		mainTable.setLinesVisible(true);
		TableColumn tableColumn1 = new TableColumn(mainTable, SWT.RIGHT);
		mainTable.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
			public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
				parseCharPressed(e.character);
				if (!Character.isLetterOrDigit(e.character))
					return; 
				if (filterText == null)
					filterText = "" + e.character;
				else
					filterText = filterText + e.character;
				doFillMainTable();
			}
		});
		mainTable.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent mouseevent) {
				log.debug(mainTable.getSelection()[0].getText(6));
				if (mainTable.getSelectionIndex() != -1)
					new NewOrEditMovieForm(sShell, 
							Integer.valueOf(indeksi.get(mainTable.getSelectionIndex())), 
									new Runnable() {
	
										@Override
										public void run() {
											doFillMainTable();
										}
										
									});
			}			
		});
		tableColumn1.setWidth(100);
		tableColumn1.setText("Медиј");
		TableColumn tableColumn = new TableColumn(mainTable, SWT.NONE);
		tableColumn.setWidth(200);
		tableColumn.setText("Назив филма");
		TableColumn tableColumn2 = new TableColumn(mainTable, SWT.NONE);
		tableColumn2.setWidth(200);
		tableColumn2.setText("Превод назива");
		TableColumn tableColumn21 = new TableColumn(mainTable, SWT.NONE);
		tableColumn21.setWidth(95);
		tableColumn21.setText("Жанр");
		TableColumn tableColumn3 = new TableColumn(mainTable, SWT.NONE);
		tableColumn3.setWidth(95);
		tableColumn3.setText("Позиција");
		TableColumn tableColumn4 = new TableColumn(mainTable, SWT.NONE);
		tableColumn4.setWidth(120);
		tableColumn4.setText("Коментар");
		// dodajemo jedan listener za aktiviranje programa... 
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			
			@Override
			public void shellActivated(org.eclipse.swt.events.ShellEvent e) {
				sShell.removeShellListener(this);
				doActivated();
			}
			
		});
	}
	
	private void parseCharPressed(char character) {
		if (character == SWT.ESC) {
			if ((filterText == null || filterText.length()==0)
					&& comboPozicija.getSelectionIndex()==0
					&& comboTipMedija.getSelectionIndex()==0
					&& comboZanr.getSelectionIndex()==0) {
				// ako je pritisnut ESC, a pritom je svaki moguci filter vec anuliran
				// izlazimo iz programa !
				sShell.dispose();
				return;
			}
			else {
				// ponistavamo potpuno sve kombo boksove, kao i filter
				comboPozicija.select(0);
				comboTipMedija.select(0);
				comboZanr.select(0);
				filterText = "";
				doFillMainTable();
			}
		}
		else if (character == SWT.BS) {
			if (filterText != null && filterText.length() > 0)
				filterText = filterText.substring(0, filterText.length()-1);
			doFillMainTable();
		}
	}

	public void doFillMainTable() {
		if (toolTicker != null) {
			toolTicker.setVisible(true);
			toolTicker.update();
		}
		HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
		@SuppressWarnings("unchecked")
		List<FilmInfo> sviFilmovi = (List<FilmInfo>) template.execute(new HibernateCallback() {

			@Override
			@SuppressWarnings("unchecked")
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				final String ukljZanr = "f.zanr.zanr=:zanr";
				final String ukljTipMedija = "m.tipMedija.naziv=:tipmedija";
				final String ukljPozicija = "m.pozicija.pozicija=:pozicija";
				final String ukljFilter = "(lower(f.nazivfilma) like :filter or lower(f.prevodnazivafilma) like :filter or lower(f.komentar) like :filter)";
				
				StringBuffer buff = new StringBuffer("select f from Film f, Medij m where f.idfilm in elements(m.films)");

				if (comboZanr.getSelectionIndex()>1)
					buff.append(" and ").append(ukljZanr);
				if (comboTipMedija.getSelectionIndex()>1)
					buff.append(" and ").append(ukljTipMedija);
				if (comboPozicija.getSelectionIndex()>1)
					buff.append(" and ").append(ukljPozicija);
				if (filterText != null && filterText.length()>0)
					buff.append(" and ").append(ukljFilter);
				
				buff.append(" order by m.tipMedija.naziv, m.indeks, f.nazivfilma");
				
				
				log.info("Generisan upit: "+buff.toString());
				long start = new Date().getTime();
				Query query = session.createQuery(buff.toString());
				if (comboZanr.getSelectionIndex()>1)
					query.setString("zanr", comboZanr.getText());
				if (comboTipMedija.getSelectionIndex()>1)
					query.setString("tipmedija", comboTipMedija.getText());
				if (comboPozicija.getSelectionIndex()>1)
					query.setString("pozicija", comboPozicija.getText());
				if (filterText != null && filterText.length()>0)
					query.setString("filter", "%"+filterText.toLowerCase()+"%");
				
				
				List<Film> sviFilmovi = query.list();
				long end = new Date().getTime();
				log.info("Osnovni upit je uspesno zavrsen, vratio je "+sviFilmovi.size()+" redova za "+(end-start)+"ms");
				
				start = new Date().getTime();
				List<FilmInfo> rezLista = new LinkedList<FilmInfo> ();				
				for (Film film : sviFilmovi) {
					StringBuffer medijInfo = new StringBuffer();
					Object[] mediji = film.getMedijs().toArray();
					Arrays.sort(mediji);
					
					for (Object medij : mediji)
						medijInfo.append(medij.toString()).append(' ');
					String prisutan = film.getFilmLocation();
					
					rezLista.add(new FilmInfo(
								new Integer(film.getIdfilm()),
								medijInfo.toString().trim(),
								film.getNazivfilma(),
								film.getPrevodnazivafilma(),
								film.getZanr().getZanr(),
								prisutan,
								film.getKomentar()
							));
				}
				end = new Date().getTime();
				log.info("Dohvatanje vezanih informacija i priprema liste zavrseno za "+(end-start)+"ms");
				return rezLista;
			}
			
		});
		
		long start = new Date().getTime();
		indeksi.clear();
		int i=0;
		
		Object[] nizFilmova = (Object[]) sviFilmovi.toArray();
		Arrays.sort(nizFilmova);		
		
		if (nizFilmova.length < mainTable.getTopIndex())
			mainTable.setTopIndex(0);
		FilmInfo lastFilm = null;
		for (Object filmObj : nizFilmova) {
			FilmInfo film = (FilmInfo) filmObj;
			if (lastFilm !=null && film.getNazivFilma().equals(lastFilm.getNazivFilma()))
				continue;
			TableItem item = new TableItem(mainTable, SWT.NONE);
			if (i < mainTable.getItemCount())
				item = mainTable.getItem(i);
			else
				item = new TableItem(mainTable, SWT.NONE);
			i++;
			item.setText(new String[] {
					film.getMedij(), 
					film.getNazivFilma(),
					film.getPrevodFilma(),
					film.getZanr(),
					film.getPozicija(),
					film.getKomentar()
			});
			indeksi.add(film.getId());
			lastFilm = film;
		}
		// brisemo preostale (visak) elemente od poslednjeg u tabeli
		// pa sve do poslednjeg unetog 
		for (int j=mainTable.getItemCount()-1; j>=i ; j--)
			mainTable.remove(j);
		sShell.setText(titleConst+" ("+mainTable.getItemCount()+")" + (filterText != null && filterText.length()>0 ? ", филтер: <" + filterText + ">" : ""));
		long end = new Date().getTime();
		log.info("Lista je prikazana, umetanje zavrseno za "+(end-start)+"ms");
		if (toolTicker != null)
			toolTicker.setVisible(false);
	}
	
	public void doActivated() {
		doFillMainTable();
	}
	
	/**
	 * This method initializes comboZanr	
	 *
	 */
	private void createComboZanr() {
		GridData gridData5 = new GridData();
		gridData5.widthHint = 80;
		comboZanr = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboZanr.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboZanr.setLayoutData(gridData5);
		comboZanr.setVisibleItemCount(16);
		comboZanr.addSelectionListener(new ComboWrongIndexSelectionListener());
		comboZanr.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				doFillMainTable();
				mainTable.setFocus();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		@SuppressWarnings("unchecked")
		List<Zanr> zanrovi = (List<Zanr>) Startup.getKernel().getHibernateTemplate().find("from Zanr z order by LOWER(z.zanr) asc");
		comboZanr.add("Сви жанрови");
		comboZanr.add("-----------");
		for(Zanr zanr : zanrovi)
			comboZanr.add(zanr.toString());
		comboZanr.select(0);
	}
	
	private void resetZanrova() {
		comboZanr.setItems(new String [] {});
		@SuppressWarnings("unchecked")
		List<Zanr> zanrovi = (List<Zanr>) Startup.getKernel().getHibernateTemplate().find("from Zanr z order by LOWER(z.zanr) asc");
		comboZanr.add("Сви жанрови");
		comboZanr.add("-----------");
		for(Zanr zanr : zanrovi)
			comboZanr.add(zanr.toString());
		comboZanr.select(0);
	}

	/**
	 * This method initializes comboTipMedija	
	 *
	 */
	private void createComboTipMedija() {
		GridData gridData1 = new GridData();
		gridData1.widthHint = 80;
		comboTipMedija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboTipMedija.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboTipMedija.setLayoutData(gridData1);
		comboTipMedija.setVisibleItemCount(8);
		comboTipMedija.addSelectionListener(new ComboWrongIndexSelectionListener());
		comboTipMedija.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				doFillMainTable();
				mainTable.setFocus();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		@SuppressWarnings("unchecked")
		List<TipMedija> tipovi = (List<TipMedija>) Startup.getKernel().getHibernateTemplate().find("from TipMedija m order by LOWER(m.naziv) asc");
		comboTipMedija.add("Сви медији");
		comboTipMedija.add("-----------");
		for(TipMedija tip : tipovi)
			comboTipMedija.add(tip.toString());
		comboTipMedija.select(0);
	}

	/**
	 * This method initializes comboPozicija	
	 *
	 */
	private void createComboPozicija() {
		GridData gridData3 = new GridData();
		gridData3.widthHint = 80;
		comboPozicija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboPozicija.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboPozicija.setLayoutData(gridData3);
		comboPozicija.setVisibleItemCount(8);
		comboPozicija.addSelectionListener(new ComboWrongIndexSelectionListener());
		comboPozicija.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				doFillMainTable();
				mainTable.setFocus();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		resetPozicije();
	}

	private void resetPozicije() {
		comboPozicija.setItems(new String [] {});
		@SuppressWarnings("unchecked") 
		List<Pozicija> pozicije = (List<Pozicija>) Startup.getKernel().getHibernateTemplate().find("from Pozicija p order by LOWER(p.pozicija) asc");
		comboPozicija.add("Било где");
		comboPozicija.add("-----------");
		for(Pozicija pozicija : pozicije)
			comboPozicija.add(pozicija.toString());
		comboPozicija.select(0);
	}

	/**
	 * This method initializes panCombos	
	 *
	 */
	private void createPanCombos() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		panCombos = new Composite(sShell, SWT.NONE);
		panCombos.setLayoutData(gridData2);
		panCombos.setLayout(gridLayout1);
		createComboTipMedija();
		createComboPozicija();
		createComboZanr();
	}

	/**
	 * This method initializes toolBar	
	 *
	 */
	private void createToolBar() {
		toolBar = new ToolBar(sShell, SWT.FLAT);
		toolBar.setBounds(new Rectangle(11, 50, 4, 50));
		ToolItem toolNew = new ToolItem(toolBar, SWT.PUSH);
		toolNew.setText("Нов филм ...");
		toolNew.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/media.png")));
		ToolItem toolErase = new ToolItem(toolBar, SWT.PUSH);
		toolErase.setText("Обриши филм");
		toolErase.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/alert.png")));
		ToolItem toolExport = new ToolItem(toolBar, SWT.PUSH);
		toolExport.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/folder_outbox.png")));
		toolExport.setText("Експортовање");
		toolExport.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog dlg = new FileDialog(sShell, SWT.SAVE);
				dlg.setFilterNames(new String[] {"HTML files (*.htm)"});
				dlg.setFilterExtensions(new String[] {"*.htm"});
				targetFileForExport = dlg.open();
				if (targetFileForExport == null)
					return;
				String ext = targetFileForExport.substring(targetFileForExport.lastIndexOf('.')+1);
				if (targetFileForExport == null)
					return;
				log.debug("Odabrano eksportovanje u fajl \""+targetFileForExport+"\"");
				Exporter exporter = ExporterFactory.getInstance().getExporter(ext);
				if (exporter == null) {
					log.error("Eksportovanje u zeljeni format nije podrzano");
					return;
				}
				exporter.export(new ExporterSource() {
					
					@Override
					public String getTargetFile() {
						return targetFileForExport;
					}

					@Override
					public int getItemCount() {
						return mainTable.getItemCount();
					}
					
					@Override
					public int getColumnCount() {
						return mainTable.getColumnCount();
					}

					@Override
					public String getData(int row, int column) {
						if (row == -1)
							return mainTable.getColumn(column).getText();
						else
							return mainTable.getItem(row).getText(column);
					}
					
				});
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		toolErase.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (mainTable.getSelectionIndex() == -1)
					return;
				new DeleteMovieForm(sShell, Integer.valueOf(indeksi.get(mainTable.getSelectionIndex())), 
						new Runnable() {

							@Override
							public void run() {
								doFillMainTable();
							}
							
						});
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		ToolItem toolSettings = new ToolItem(toolBar, SWT.PUSH);
		toolSettings.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/advancedsettings.png")));
		toolSettings.setWidth(90);
		toolSettings.setText("Подешавања...");
		toolSettings.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				new SettingsForm(sShell, new Runnable() {

					@Override
					public void run() {
						resetPozicije();
						resetZanrova();
						doFillMainTable();
					}
					
				});
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem toolAbout = new ToolItem(toolBar, SWT.PUSH);
		toolAbout.setText("О програму ...");
		toolAbout.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/jabber_protocol.png")));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem toolExit = new ToolItem(toolBar, SWT.PUSH);
		toolExit.setText("Излаз");
		toolExit.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/shutdown.png")));
		toolExit.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				sShell.dispose();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		toolAbout.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				new AboutForm(sShell);
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		toolNew.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				new NewOrEditMovieForm(sShell, null, new Runnable() {

					@Override
					public void run() {
						doFillMainTable();
					}
					
				});
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
	}

	/**
	 * This method initializes toolTicker	
	 *
	 */
	private void createToolTicker() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData4.widthHint = -1;
		gridData4.heightHint = -1;
		gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		toolTicker = new ToolBar(sShell, SWT.NONE);
		toolTicker.setEnabled(true);
		toolTicker.setLayoutData(gridData4);
		ToolItem toolItem = new ToolItem(toolTicker, SWT.PUSH);
		toolItem.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/db_find.png")));
		toolItem.setDisabledImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/db_find.png")));
		toolItem.setEnabled(false);
		toolItem.setWidth(24);
		toolItem.setHotImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/db_find.png")));
	}

}