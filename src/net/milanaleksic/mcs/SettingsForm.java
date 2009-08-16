package net.milanaleksic.mcs;

import net.milanaleksic.mcs.db.Pozicija;
import net.milanaleksic.mcs.db.Zanr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;


public class SettingsForm {
	
	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="22,11"
	private Shell parent = null;
	private Runnable parentRunner = null;
	private Composite composite = null;
	private Button btnCancel = null;
	private TabFolder tabFolder = null;
	private Composite composite1 = null;
	private Composite composite2 = null;
	private List listLokacije = null;
	private Label label = null;
	private Composite composite3 = null;
	private Button btnDodajLokaciju = null;
	private Text textNovaLokacija = null;
	private Button btnIzbrisiLokaciju = null;
	private Label label1 = null;
	private Composite composite4 = null;
	private List listZanrovi = null;
	private Button btnIzbrisiZanr = null;
	private Text textNovZanr = null;
	private Button btnDodajZanr = null;
	
	private boolean changed=false;

	public SettingsForm(Shell parent, Runnable runnable) {
		this.parent = parent;
		createSShell();
		sShell.setLocation(new Point(parent.getLocation().x + Math.abs(parent.getSize().x - sShell.getSize().x) / 2, parent.getLocation().y
				+ Math.abs(parent.getSize().y - sShell.getSize().y) / 2));
		reReadData();
		sShell.open();
		parentRunner = runnable;
	}

	private void reReadData() {
		final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
		template.execute(new HibernateCallback() {
			
			@SuppressWarnings("unchecked")
			public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
				
				// preuzimanje svih pozicija
				Query query = session.createQuery("from Pozicija p order by lower(p.pozicija)");
				java.util.List<Pozicija> svePozicije = (java.util.List<Pozicija>) query.list();
				listLokacije.setItems(new String[] {});
				for (Pozicija pozicija : svePozicije) {
					listLokacije.add(pozicija.toString());
				}
				
				// preuzimanje svih zanrova
				query = session.createQuery("from Zanr z order by lower(z.zanr)");
				java.util.List<Zanr> sviZanrovi = (java.util.List<Zanr>) query.list();
				listZanrovi.setItems(new String[] {});
				for (Zanr zanr : sviZanrovi) {
					listZanrovi.add(zanr.toString());
				}
				
				return null;
			};
			
		});
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 1;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("Подешавања програма");
		createTabFolder();
		sShell.setLayout(gridLayout3);
		createComposite();
		sShell.setSize(new Point(377, 312));
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
	}

	/**
	 * This method initializes composite	
	 *
	 */
	private void createComposite() {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = GridData.END;
		gridData12.grabExcessVerticalSpace = true;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.horizontalSpacing = 20;
		composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
		btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("Затвори");
		btnCancel.setLayoutData(gridData12);
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (changed)
					parentRunner.run();
				sShell.close();
			}
		});
	}

	/**
	 * This method initializes tabFolder	
	 *
	 */
	private void createTabFolder() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		tabFolder = new TabFolder(sShell, SWT.NONE);
		createComposite1();
		tabFolder.setLayoutData(gridData1);
		createComposite2();
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Локације");
		tabItem.setControl(composite1);
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
		tabItem1.setText("Жанрови");
		tabItem1.setControl(composite2);
	}

	/**
	 * This method initializes composite1	
	 *
	 */
	private void createComposite1() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.widthHint = 150;
		gridData2.grabExcessVerticalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		composite1 = new Composite(tabFolder, SWT.NONE);
		composite1.setLayout(gridLayout1);
		label = new Label(composite1, SWT.NONE);
		label.setText("Тренутне локације:");
		createComposite3();
		listLokacije = new List(composite1, SWT.BORDER | SWT.V_SCROLL);
		listLokacije.setLayoutData(gridData2);
		btnIzbrisiLokaciju = new Button(composite1, SWT.NONE);
		btnIzbrisiLokaciju.setText("Избриши");
		btnIzbrisiLokaciju.setLayoutData(gridData5);
		btnIzbrisiLokaciju.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
				template.execute(new HibernateCallback() {
					
					public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
						
						// preuzimanje podataka za film koji se azurira
						Transaction transaction = session.beginTransaction();
						Query query = session.createQuery("from Pozicija p where p.pozicija = :param");
						query.setString("param", listLokacije.getItem(listLokacije.getSelectionIndex()));
						Pozicija pozicija = (Pozicija) query.list().get(0);
						
						if (pozicija.getMedijs().size() > 0) {
							MessageBox box = new MessageBox(sShell, SWT.ICON_ERROR);
							box.setMessage("Забрањено је брисање, постоји " + pozicija.getMedijs().size() + " медијума који су на тој локацији!");
							box.setText("Грешка");
							box.open();
							return null;
						}
						
						session.delete(pozicija);
						transaction.commit();
						changed = true;
						reReadData();
						return null;
					};
					
				});
			}
		});
	}

	/**
	 * This method initializes composite2	
	 *
	 */
	private void createComposite2() {
		GridData gridData8 = new GridData();
		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridData gridData7 = new GridData();
		gridData7.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData7.widthHint = 150;
		gridData7.grabExcessVerticalSpace = true;
		gridData7.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		composite2 = new Composite(tabFolder, SWT.NONE);
		composite2.setLayout(gridLayout2);
		label1 = new Label(composite2, SWT.NONE);
		label1.setText("Тренутни жанрови:");
		createComposite4();
		listZanrovi = new List(composite2, SWT.BORDER | SWT.V_SCROLL);
		listZanrovi.setLayoutData(gridData7);
		btnIzbrisiZanr = new Button(composite2, SWT.NONE);
		btnIzbrisiZanr.setText("Избриши");
		btnIzbrisiZanr.setLayoutData(gridData8);
		btnIzbrisiZanr.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
				template.execute(new HibernateCallback() {
					
					public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
						
						// preuzimanje podataka za film koji se azurira
						Transaction transaction = session.beginTransaction();
						Query query = session.createQuery("from Zanr z where z.zanr = :param");
						query.setString("param", listZanrovi.getItem(listZanrovi.getSelectionIndex()));
						Zanr zanr = (Zanr) query.list().get(0);
						
						if (zanr.getFilms().size() > 0) {
							MessageBox box = new MessageBox(sShell, SWT.ICON_ERROR);
							box.setMessage("Забрањено је брисање, постоји " + zanr.getFilms().size() + " филмова који припадају овом жанру!");
							box.setText("Грешка");
							box.open();
							return null;
						}
						
						session.delete(zanr);
						transaction.commit();
						changed = true;
						reReadData();
						return null;
					};
					
				});
			}
		});
	}

	/**
	 * This method initializes composite3	
	 *
	 */
	private void createComposite3() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData3 = new GridData();
		gridData3.verticalSpan = 3;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		composite3 = new Composite(composite1, SWT.BORDER);
		composite3.setLayout(new GridLayout());
		composite3.setLayoutData(gridData3);
		textNovaLokacija = new Text(composite3, SWT.BORDER);
		textNovaLokacija.setLayoutData(gridData4);
		btnDodajLokaciju = new Button(composite3, SWT.NONE);
		btnDodajLokaciju.setText("Додај ову локацију");
		btnDodajLokaciju.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
				template.execute(new HibernateCallback() {
					
					public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
						
						// preuzimanje podataka za film koji se azurira
						Transaction transaction = session.beginTransaction();
						Pozicija pozicija = new Pozicija();
						pozicija.setPozicija(textNovaLokacija.getText());
						
						session.save(pozicija);
						transaction.commit();
						changed=true;
						reReadData();
						return null;
					};
					
				});
			}
		});
	}

	/**
	 * This method initializes composite4	
	 *
	 */
	private void createComposite4() {
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData9 = new GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 1;
		GridData gridData6 = new GridData();
		gridData6.verticalSpan = 3;
		gridData6.grabExcessVerticalSpace = true;
		gridData6.grabExcessHorizontalSpace = true;
		gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		composite4 = new Composite(composite2, SWT.BORDER);
		composite4.setLayoutData(gridData6);
		composite4.setLayout(gridLayout4);
		textNovZanr = new Text(composite4, SWT.BORDER);
		textNovZanr.setLayoutData(gridData10);
		btnDodajZanr = new Button(composite4, SWT.NONE);
		btnDodajZanr.setText("Додај овај жанр");
		btnDodajZanr.setLayoutData(gridData9);
		btnDodajZanr.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				final HibernateTemplate template = Startup.getKernel().getHibernateTemplate();
				template.execute(new HibernateCallback() {
					
					public Object doInHibernate(org.hibernate.Session session) throws org.hibernate.HibernateException ,java.sql.SQLException {
						
						// preuzimanje podataka za film koji se azurira
						Transaction transaction = session.beginTransaction();
						Zanr zanr = new Zanr();
						zanr.setZanr(textNovZanr.getText());
						
						session.save(zanr);
						transaction.commit();
						changed = true;
						reReadData();
						return null;
					};
					
				});
			}
		});
	}

}
