package net.milanaleksic.mcs.gui;

import java.sql.SQLException;

import net.milanaleksic.mcs.Startup;
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


public class NewMediumForm {

	private static final Logger log = Logger.getLogger(NewMediumForm.class); // @jve:decl-index=0:

	private Shell sShell = null; // @jve:decl-index=0:visual-constraint="17,12"
    private Button rbCD = null;
    private Text textID = null;
    private Shell parent = null; // @jve:decl-index=0:
	private Runnable parentRunner = null; // @jve:decl-index=0:

	private HibernateTemplate template = null;

	public NewMediumForm(Shell parent, Runnable runnable) {
		this.parent = parent;
		createSShell();
		sShell.setLocation(
				new Point(
						parent.getLocation().x+Math.abs(parent.getSize().x-sShell.getSize().x) / 2, 
						parent.getLocation().y+Math.abs(parent.getSize().y-sShell.getSize().y) / 2 ));
		sShell.open();
		template = Startup.getKernel().getHibernateTemplate();
		obradaIzbora();
		parentRunner = runnable;
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		if (parent == null)
			sShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		else
			sShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		sShell.setText("Додавање новог медијума");
		sShell.setSize(new Point(288, 189));
		sShell.setLayout(gridLayout2);
        Label label2 = new Label(sShell, SWT.LEFT);
		label2.setText("Тип медијума који додајете: ");
		label2.setLayoutData(gridData1);
		createGroup();
        Label label3 = new Label(sShell, SWT.NONE);
		label3.setText("Медијум ће имати следећи ID:");
		label3.setLayoutData(gridData2);
		textID = new Text(sShell, SWT.BORDER | SWT.READ_ONLY);
		createComposite();
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				sShell.dispose();
			}
		});
	}

	/**
	 * This method initializes group
	 * 
	 */
	private void createGroup() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
        Group group = new Group(sShell, SWT.NONE);
		group.setLayout(gridLayout1);
		rbCD = new Button(group, SWT.RADIO);
		rbCD.setText("CD");
		rbCD.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				obradaIzbora();
			}
		});
        Button rbDVD = new Button(group, SWT.RADIO);
		rbDVD.setText("DVD");
		rbDVD.setSelection(true);
		rbDVD.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                obradaIzbora();
            }
        });
	}

	public void obradaIzbora() {
		Integer indeks = (Integer) template.execute(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery("select max(indeks)+1 from Medij m where m.tipMedija.naziv=:tipMedija");
				String selected = rbCD.getSelection() ? "CD" : "DVD";
				query.setString("tipMedija", selected);
				if (query.list().get(0)==null)
					return new Integer("1");
				else
					return new Integer(query.list().get(0).toString());
			}

		});

		textID.setText(indeks.toString());
	}

	/**
	 * This method initializes composite
	 * 
	 */
	private void createComposite() {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 20;
		GridData gridData12 = new GridData();
		gridData12.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData12.grabExcessVerticalSpace = true;
        Composite composite = new Composite(sShell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
        Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setText("Сними");
		btnOk.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {

                template.execute(new HibernateCallback() {

                    @Override
                    public Object doInHibernate(Session session) throws HibernateException, SQLException {

                        Transaction transaction = session.beginTransaction();

                        String selected = rbCD.getSelection() ? "CD" : "DVD";
                        Medij m = new Medij();
                        m.setFilms(null);
                        m.setIndeks(Integer.parseInt(textID.getText()));

                        Query query = session.createQuery("from Pozicija p where p.pozicija=:primljen");
                        query.setString("primljen", "присутан");
                        Pozicija pozicija = (Pozicija) query.list().get(0);
                        pozicija.addMedij(m);

                        query = session.createQuery("from TipMedija t where t.naziv=:tipMedija");
                        query.setString("tipMedija", selected);
                        TipMedija tipMedija = (TipMedija) query.list().get(0);
                        tipMedija.addMedij(m);

                        log.info("Dodajem nov medij: indeksID=" + m.getIndeks() +
                                ", pozicijaID=" + pozicija.getIdpozicija() +
                                ", tipMedijaID=" + tipMedija.getIdtip());

                        session.save(m);
                        transaction.commit();
                        return m;
                    }

                });

                parentRunner.run();

                sShell.close();
            }
        });
        Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText("Одустани");
		btnCancel.setLayoutData(gridData12);
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                sShell.close();
            }
        });
	}

}
