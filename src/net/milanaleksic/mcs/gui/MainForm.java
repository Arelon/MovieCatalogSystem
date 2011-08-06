package net.milanaleksic.mcs.gui;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.config.ApplicationConfiguration;
import net.milanaleksic.mcs.domain.*;
import net.milanaleksic.mcs.export.*;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class MainForm extends Observable {

	private static final Logger log = Logger.getLogger(MainForm.class);  //  @jve:decl-index=0:

	private final static String titleConst = "Movie Catalog System (C) by Milan.Aleksic@gmail.com";

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="11,7"
	private Combo comboZanr = null;
	private Combo comboTipMedija = null;
	private Table mainTable = null;
	private Combo comboPozicija = null;
	private Composite panCombos = null;
	private Label labelFilter = null;
    private Label labelCurrent = null;
    private ToolBar toolTicker = null;
    private Composite wrapperDataInfo = null;

    private CurrentViewState currentViewState = new CurrentViewState();
    private ApplicationConfiguration.InterfaceConfiguration interfaceConfiguration;

    @Autowired private ApplicationManager applicationManager;
    @Autowired private HibernateTemplate hibernateTemplate;

    // private classes

	private static class CurrentViewState {

		private volatile Long activePage = 0L;
		private volatile Long showableCount = 0L;
		private volatile String filterText = "";
        private volatile int maxItemsPerPage;

        public String getFilterText() {
			return filterText;
		}

		public void setFilterText(String filterText) {
			this.filterText = filterText;
			activePage = 0L;
		}

		public Long getActivePage() {
			return activePage;
		}

		public void setActivePage(Long activePage) {
			this.activePage = activePage;
		}

		public Long getShowableCount() {
			return showableCount;
		}

		public void setShowableCount(Long showableCount) {
			this.showableCount = showableCount;
		}

        public void setMaxItemsPerPage(int maxItemsPerPage) {
            this.maxItemsPerPage = maxItemsPerPage;
        }

        public int getMaxItemsPerPage() {
            return maxItemsPerPage;
        }
    }
	
	private class MainTableKeyAdapter extends KeyAdapter {
		
		@Override public void keyPressed(KeyEvent e) {
			String filterText = currentViewState.getFilterText();
			
			switch (e.keyCode) {
				case SWT.ARROW_LEFT:
					new PreviousPageSelectionAdapter().widgetSelected(null);
					return;
				case SWT.ARROW_RIGHT:
					new NextPageSelectionAdapter().widgetSelected(null);
					return;
				case SWT.ESC:
					if ((filterText == null || filterText.length() == 0)
							&& comboPozicija.getSelectionIndex() == 0
							&& comboTipMedija.getSelectionIndex() == 0
							&& comboZanr.getSelectionIndex() == 0) {
						// ako je pritisnut ESC, a pritom je svaki moguci filter vec
						// anuliran
						// izlazimo iz programa !
						sShell.dispose();
						return;
					} else {
						// ponistavamo potpuno sve kombo boksove, kao i filter
						comboPozicija.select(0);
						comboTipMedija.select(0);
						comboZanr.select(0);
						currentViewState.setFilterText("");
						doFillMainTable();
					}
					return;
				case SWT.BS:
					if (filterText != null && filterText.length() > 0) {
						currentViewState.setFilterText(filterText.substring(0, filterText.length() - 1));
					    doFillMainTable();
                    }
					return;
			}
			
			if (!Character.isLetterOrDigit(e.character))
				return;
			if (filterText.length() == 0)
				currentViewState.setFilterText("" + e.character);
			else
				currentViewState.setFilterText(filterText + e.character);
			doFillMainTable();
		}

	}
	
	private class NextPageSelectionAdapter extends SelectionAdapter {

		@Override public void widgetSelected(SelectionEvent e) {
            if (currentViewState.getMaxItemsPerPage()>0)
			    if (currentViewState.getMaxItemsPerPage() * (currentViewState.getActivePage()+1) > currentViewState.getShowableCount())
				    return;
			currentViewState.setActivePage(currentViewState.getActivePage()+1);
			doFillMainTable();
		}
		
	}
	
	private class PreviousPageSelectionAdapter extends SelectionAdapter {

		@Override public void widgetSelected(SelectionEvent e) {
			if (currentViewState.getActivePage()==0)
				return;
			currentViewState.setActivePage(currentViewState.getActivePage()-1);
			doFillMainTable();
		}
		
	}
	
	private class MainTableMouseListener extends MouseAdapter {
		
		@Override public void mouseDoubleClick(MouseEvent mouseevent) {
			if (mainTable.getSelectionIndex() != -1)
				new NewOrEditMovieForm(sShell, 
						(Integer)mainTable.getSelection()[0].getData(),
								new Runnable() {

									@Override public void run() {
										doFillMainTable();
									}
									
								}, hibernateTemplate);
		}			
	}
	
	private class MainFormShellListener extends ShellAdapter {
			
		@Override public void shellActivated(ShellEvent e) {
			sShell.removeShellListener(this);
			if (applicationManager.getProgramArgs().isNoInitialMovieListLoading())
                return;
            doFillMainTable();
		}
			
	}
	
	private class ComboRefreshAdapter extends SelectionAdapter {
	
		public void widgetSelected(SelectionEvent e) {
			Combo combo = (Combo)e.widget;
			if (combo.getSelectionIndex()==1)
				combo.select(0);
			currentViewState.setActivePage(0L);
			doFillMainTable();
			mainTable.setFocus();
		}
		
	}
	
	private class ToolExportSelectionAdapter extends SelectionAdapter {
		
		@Override public void widgetSelected(SelectionEvent e) {
			FileDialog dlg = new FileDialog(sShell, SWT.SAVE);
			dlg.setFilterNames(new String[] {"HTML files (*.htm)"});
			dlg.setFilterExtensions(new String[] {"*.htm"});
			final String targetFileForExport = dlg.open();
			if (targetFileForExport == null)
				return;
			String ext = targetFileForExport.substring(targetFileForExport.lastIndexOf('.')+1);
			log.debug("Odabrano eksportovanje u fajl \""+targetFileForExport+"\"");
			Exporter exporter = ExporterFactory.getInstance().getExporter(ext);
			if (exporter == null) {
				log.error("Eksportovanje u zeljeni format nije podrzano");
				return;
			}
            @SuppressWarnings("unchecked")
            final List<Film> allFilms = (List<Film>) hibernateTemplate.execute(new ListMoviesHibernateCallback());
			exporter.export(new ExporterSource() {
				
				@Override public String getTargetFile() {
					return targetFileForExport;
				}

				@Override public int getItemCount() {
					return allFilms.size();
				}
				
				@Override public int getColumnCount() {
					return 5;
				}

				@Override public String getData(int row, int column) {
                    if (row == -1)
						return mainTable.getColumn(column).getText();
                    switch(column) {
                        case 0:
                            return allFilms.get(row).getMedijListAsString();
                        case 1:
                            return allFilms.get(row).getNazivfilma();
                        case 2:
                            return allFilms.get(row).getPrevodnazivafilma();
                        case 3:
                            return allFilms.get(row).getZanr().getZanr();
                        case 4:
                            return allFilms.get(row).getFilmLocation();
                        default:
                            return "";
                    }
				}
				
			});
		}

	}
	
	private class ToolEraseSelectionAdapter extends SelectionAdapter {
		
		@Override public void widgetSelected(SelectionEvent e) {
			if (mainTable.getSelectionIndex() == -1)
				return;
			new DeleteMovieForm(sShell, (Integer) mainTable.getSelection()[0].getData(),
					new Runnable() {

						@Override public void run() {
							doFillMainTable();
						}
						
					}, hibernateTemplate);
		}
		
	}
	
	private class ToolSettingsSelectionAdapter extends SelectionAdapter {

		@Override public void widgetSelected(SelectionEvent e) {
			new SettingsForm(sShell, new Runnable() {

				@Override public void run() {
					resetPozicije();
					resetZanrova();
					doFillMainTable();
				}
				
			}, hibernateTemplate, applicationManager.getUserConfiguration());
		}
		
		private void resetZanrova() {
			comboZanr.setItems(new String [] {});
			@SuppressWarnings("unchecked")
			List<Zanr> zanrovi = (List<Zanr>) hibernateTemplate.find("from Zanr z order by LOWER(z.zanr) asc");
			comboZanr.add("Сви жанрови");
			comboZanr.add("-----------");
			for(Zanr zanr : zanrovi)
				comboZanr.add(zanr.toString());
			comboZanr.select(0);
		}

	}
	
	private class ToolExitSelectionAdapter extends SelectionAdapter {

		@Override public void widgetSelected(SelectionEvent e) {
			sShell.dispose();
		}
		
	}
	
	private class ToolAboutSelectionAdapter extends SelectionAdapter {
		
		@Override public void widgetSelected(SelectionEvent e) {
			new AboutForm(sShell);
		}
		
	}
	
	private class ToolNewSelectionAdapter extends SelectionAdapter {

		@Override public void widgetSelected(SelectionEvent e) {
			new NewOrEditMovieForm(sShell, null, new Runnable() {

				@Override
				public void run() {
					doFillMainTable();
				}
				
			}, hibernateTemplate);
		}

	}
	
	private class ListMoviesHibernateCallback implements HibernateCallback {

        private final int maxItems;

        public ListMoviesHibernateCallback() {
            this.maxItems = 0;
        }

        public ListMoviesHibernateCallback(int maxItems) {
            this.maxItems = maxItems;
        }

        @Override
		@SuppressWarnings("unchecked")
		public Object doInHibernate(Session session) throws HibernateException, SQLException {
			final String ukljZanr = "f.zanr.zanr=:zanr";
			final String ukljTipMedija = "m.tipMedija.naziv=:tipmedija";
			final String ukljPozicija = "m.pozicija.pozicija=:pozicija";
			final String ukljFilter = "(lower(f.nazivfilma) like :filter or lower(f.prevodnazivafilma) like :filter or lower(f.komentar) like :filter)";
			final String filterText = currentViewState.getFilterText();

			StringBuilder buff = new StringBuilder("select f from Film f where idfilm in (select f.idfilm from Film f, Medij m where f.idfilm in elements(m.films)");

            StringBuilder countBuff = new StringBuilder("select count(distinct idfilm) from Film f, Medij m where f.idfilm in elements(m.films)");
 
			if (comboZanr.getSelectionIndex()>1) {
				buff.append(" and ").append(ukljZanr);
				countBuff.append(" and ").append(ukljZanr);
			}
			if (comboTipMedija.getSelectionIndex()>1) {
				buff.append(" and ").append(ukljTipMedija);
				countBuff.append(" and ").append(ukljTipMedija);
			}
			if (comboPozicija.getSelectionIndex()>1) {
				buff.append(" and ").append(ukljPozicija);
				countBuff.append(" and ").append(ukljPozicija);
			}
			if (filterText != null && filterText.length()>0) {
				buff.append(" and ").append(ukljFilter);
				countBuff.append(" and ").append(ukljFilter);
			}
			buff.append(" order by m.tipMedija.naziv, m.indeks, f.nazivfilma)");
			
			String hsql = buff.toString();
			log.info("Generisan upit: "+hsql);
			
			long start = new Date().getTime();
			Query query = session.createQuery(hsql);
			Query countQuery = session.createQuery(countBuff.toString());
			if (comboZanr.getSelectionIndex()>1) {
				query.setString("zanr", comboZanr.getText());
				countQuery.setString("zanr", comboZanr.getText());
			}
			if (comboTipMedija.getSelectionIndex()>1) {
				query.setString("tipmedija", comboTipMedija.getText());
				countQuery.setString("tipmedija", comboTipMedija.getText());
			}
			if (comboPozicija.getSelectionIndex()>1) {
				query.setString("pozicija", comboPozicija.getText());
				countQuery.setString("pozicija", comboPozicija.getText());
			}
			if (filterText != null && filterText.length()>0) {
				query.setString("filter", "%"+filterText.toLowerCase()+"%");
				countQuery.setString("filter", "%"+filterText.toLowerCase()+"%");
			}

            if (maxItems >0) {
                query.setFirstResult(currentViewState.getActivePage().intValue()*maxItems);
                query.setMaxResults(maxItems);
            }

            currentViewState.setMaxItemsPerPage(maxItems);

			List<Film> sviFilmovi = query.list();
            long end = new Date().getTime();
            log.info("Upit dohvatanja jedne stranice filmova je izvukao "+sviFilmovi.size()+" redova za "+(end-start)+"ms");

            start = System.currentTimeMillis();
			currentViewState.setShowableCount((Long)countQuery.uniqueResult());
			end = new Date().getTime();
			log.info("Upit dohvatanja ukupnog broja filmova koji ispunjavaju kriterijum za "+(end-start)+"ms");
			
			return sviFilmovi;
		}
		
	}
	
	
	
	
	
	// DESIGN

    public MainForm() {
        this.currentViewState = new CurrentViewState();
        this.interfaceConfiguration = ApplicationManager.getApplicationConfiguration().getInterfaceConfiguration();
        this.addObserver(new Observer() {

			@Override public void update(Observable obs, Object arg) {
				log.info("Osvezavam prikaz stanja!");
                if (currentViewState.getMaxItemsPerPage()>0) {
                    long lowerBound = currentViewState.getActivePage()*currentViewState.getMaxItemsPerPage()+1;
                    if (currentViewState.getShowableCount()==0)
                        lowerBound = 0;
                    long upperBound = (currentViewState.getActivePage()+1)*currentViewState.getMaxItemsPerPage();
                    if (upperBound>currentViewState.getShowableCount())
                        upperBound = currentViewState.getShowableCount();
                    labelCurrent.setText(lowerBound + "-" + upperBound + " (" + currentViewState.getShowableCount().toString() + ")");
                }
                else
                    labelCurrent.setText(currentViewState.getShowableCount().toString());
				labelFilter.setText(currentViewState.getFilterText());
				wrapperDataInfo.pack();
			}

		});
    }

    public void showForm() {
        checkCreated();
		sShell.open();
		mainTable.setFocus();
    }

    private void checkCreated() {
        if (sShell != null)
            return;
        createSShell();
        sShell.setImage(new Image(sShell.getDisplay(), MainForm.class.getResourceAsStream("/net/milanaleksic/mcs/res/database-64.png")));
    }

	public boolean isDisposed() {
		return sShell.isDisposed();
	}
	
	private void createSShell() {
		sShell = new Shell();
		sShell.setText(titleConst);
		sShell.setMaximized(false);
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		sShell.setBounds(20, 20, 860, size.height-80);
		createToolTicker();
		createPanCombos();
		createToolBar();
		sShell.setLayout(new GridLayout(3, false));
		mainTable = new Table(sShell, SWT.FULL_SELECTION);
		mainTable.setHeaderVisible(true);
		mainTable.setFont(new Font(Display.getDefault(), interfaceConfiguration.getTableFont(), 12, SWT.NORMAL));
		mainTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 3, 1));
		mainTable.setLinesVisible(true);
		TableColumn tableColumn1 = new TableColumn(mainTable, SWT.RIGHT);
		mainTable.addKeyListener(new MainTableKeyAdapter());
		mainTable.addMouseListener(new MainTableMouseListener());
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
		
		createStatusBar();
		// dodajemo jedan listener za aktiviranje programa... 
		sShell.addShellListener(new MainFormShellListener());
	}
	
	private void createComboZanr() {
		GridData gridData5 = new GridData();
		gridData5.widthHint = 80;
		comboZanr = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboZanr.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboZanr.setLayoutData(gridData5);
		comboZanr.setVisibleItemCount(16);
		comboZanr.addSelectionListener(new ComboRefreshAdapter());
		@SuppressWarnings("unchecked")
		List<Zanr> zanrovi = (List<Zanr>) hibernateTemplate.find("from Zanr z order by LOWER(z.zanr) asc");
		comboZanr.add("Сви жанрови");
		comboZanr.add("-----------");
		for(Zanr zanr : zanrovi)
			comboZanr.add(zanr.toString());
		comboZanr.select(0);
	}
	
	private void createComboTipMedija() {
		GridData gridData1 = new GridData();
		gridData1.widthHint = 80;
		comboTipMedija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboTipMedija.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboTipMedija.setLayoutData(gridData1);
		comboTipMedija.setVisibleItemCount(8);
		comboTipMedija.addSelectionListener(new ComboRefreshAdapter());
		@SuppressWarnings("unchecked")
		List<TipMedija> tipovi = (List<TipMedija>) hibernateTemplate.find("from TipMedija m order by LOWER(m.naziv) asc");
		comboTipMedija.add("Сви медији");
		comboTipMedija.add("-----------");
		for(TipMedija tip : tipovi)
			comboTipMedija.add(tip.toString());
		comboTipMedija.select(0);
	}

	private void createComboPozicija() {
		GridData gridData3 = new GridData();
		gridData3.widthHint = 80;
		comboPozicija = new Combo(panCombos, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboPozicija.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL));
		comboPozicija.setLayoutData(gridData3);
		comboPozicija.setVisibleItemCount(8);
		comboPozicija.addSelectionListener(new ComboRefreshAdapter());
		resetPozicije();
	}

	private void resetPozicije() {
		comboPozicija.setItems(new String [] {});
		@SuppressWarnings("unchecked") 
		List<Pozicija> pozicije = (List<Pozicija>) hibernateTemplate.find("from Pozicija p order by LOWER(p.pozicija) asc");
		comboPozicija.add("Било где");
		comboPozicija.add("-----------");
		for(Pozicija pozicija : pozicije)
			comboPozicija.add(pozicija.toString());
		comboPozicija.select(0);
	}

	private void createPanCombos() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.END;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
		panCombos = new Composite(sShell, SWT.NONE);
		panCombos.setLayoutData(gridData2);
		panCombos.setLayout(gridLayout1);
		createComboTipMedija();
		createComboPozicija();
		createComboZanr();
	}
	
	private void createToolBar() {
		ToolBar toolBar = new ToolBar(sShell, SWT.FLAT);
		toolBar.setBounds(new Rectangle(11, 50, 4, 50));
		toolBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		ToolItem toolNew = new ToolItem(toolBar, SWT.PUSH);
		toolNew.setText("Нов филм ...");
		toolNew.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/media.png")));
		ToolItem toolErase = new ToolItem(toolBar, SWT.PUSH);
		toolErase.setText("Обриши филм");
		toolErase.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/alert.png")));
		ToolItem toolExport = new ToolItem(toolBar, SWT.PUSH);
		toolExport.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/folder_outbox.png")));
		toolExport.setText("Експортовање");
		toolExport.addSelectionListener(new ToolExportSelectionAdapter());
		toolErase.addSelectionListener(new ToolEraseSelectionAdapter());
		ToolItem toolSettings = new ToolItem(toolBar, SWT.PUSH);
		toolSettings.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/advancedsettings.png")));
		toolSettings.setWidth(90);
		toolSettings.setText("Подешавања...");
		toolSettings.addSelectionListener(new ToolSettingsSelectionAdapter());
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem toolAbout = new ToolItem(toolBar, SWT.PUSH);
		toolAbout.setText("О програму ...");
		toolAbout.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/jabber_protocol.png")));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem toolExit = new ToolItem(toolBar, SWT.PUSH);
		toolExit.setText("Излаз");
		toolExit.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/net/milanaleksic/mcs/res/shutdown.png")));
		toolExit.addSelectionListener(new ToolExitSelectionAdapter());
		toolAbout.addSelectionListener(new ToolAboutSelectionAdapter());
		toolNew.addSelectionListener(new ToolNewSelectionAdapter());
	}
	
	private void createStatusBar() {
		ToolBar statusBar = new ToolBar(sShell, SWT.NONE);
		statusBar.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false, 1, 1));
		ToolItem toolPrevPage = new ToolItem(statusBar, SWT.PUSH);
		toolPrevPage.setText("<<");
		toolPrevPage.addSelectionListener(new PreviousPageSelectionAdapter());
		ToolItem toolNextPage = new ToolItem(statusBar, SWT.PUSH);
		toolNextPage.setText(">>");
		toolNextPage.addSelectionListener(new NextPageSelectionAdapter());
		
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.horizontalSpan = 2;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 4;
		wrapperDataInfo = new Composite(sShell, SWT.NONE);
		wrapperDataInfo.setLayoutData(gridData2);
		wrapperDataInfo.setLayout(gridLayout1);
		
		Label labelCurrentDesc = new Label(wrapperDataInfo, SWT.NONE);
		labelCurrentDesc.setText("Филтер извукао: ");
		labelCurrent = new Label(wrapperDataInfo, SWT.NONE);
		labelCurrent.setText("0");
		labelCurrent.setFont(new Font(Display.getDefault(), interfaceConfiguration.getTableFont(), 10, SWT.BOLD));
		Label labelFilterDesc = new Label(wrapperDataInfo, SWT.NONE);
		labelFilterDesc.setText("Активан филтер: ");
		labelFilter = new Label(wrapperDataInfo, SWT.NONE);
		labelFilter.setText("");
		labelFilter.setFont(new Font(Display.getDefault(), interfaceConfiguration.getTableFont(), 10, SWT.BOLD));
	}

	private void createToolTicker() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.END;
		gridData4.widthHint = -1;
		gridData4.heightHint = -1;
		gridData4.verticalAlignment = GridData.CENTER;
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
	
	
	
	
	
	
	
	// LOGIC
	
	
	public void doFillMainTable() {
		if (toolTicker != null) {
			toolTicker.setVisible(true);
			toolTicker.update();
		}
		@SuppressWarnings("unchecked")
		List<Film> sviFilmovi = (List<Film>) hibernateTemplate.execute(
                new ListMoviesHibernateCallback(applicationManager.getUserConfiguration().getElementsPerPage()));
		long start = new Date().getTime();
		int i=0;
		
        Object[] nizFilmova = sviFilmovi.toArray();
		Arrays.sort(nizFilmova);		
		
		if (nizFilmova.length < mainTable.getTopIndex())
			mainTable.setTopIndex(0);
		for (Object filmObj : nizFilmova) {
			Film film = (Film) filmObj;
			TableItem item;
			if (i < mainTable.getItemCount())
				item = mainTable.getItem(i);
			else
				item = new TableItem(mainTable, SWT.NONE);
			i++;
			item.setText(new String[] {
					film.getMedijListAsString(),
					film.getNazivfilma(),
					film.getPrevodnazivafilma(),
					film.getZanr().getZanr(),
					film.getFilmLocation(),
					film.getKomentar()
			});
            item.setData(film.getIdfilm());
		}
		// brisemo preostale (visak) elemente od poslednjeg u tabeli
		// pa sve do poslednjeg unetog 
		for (int j=mainTable.getItemCount()-1; j>=i ; j--)
			mainTable.remove(j);
		
		setChanged();
		super.notifyObservers();
		
		long end = new Date().getTime();
		log.info("Lista je prikazana, umetanje zavrseno za "+(end-start)+"ms");
		if (toolTicker != null)
			toolTicker.setVisible(false);
	}
	
}