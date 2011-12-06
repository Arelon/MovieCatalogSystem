package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.domain.model.MedijRepository;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class NewMediumForm {

    @Inject private MedijRepository medijRepository;

	private Shell sShell = null;
    private Button rbCD = null;
    private Text textID = null;
    private Shell parent = null;
	private Runnable parentRunner = null;

    public void open(Shell parent, Runnable runnable) {
        this.parent = parent;
        this.parentRunner = runnable;
        createSShell();
		sShell.setLocation(
				new Point(
						parent.getLocation().x+Math.abs(parent.getSize().x-sShell.getSize().x) / 2,
						parent.getLocation().y+Math.abs(parent.getSize().y-sShell.getSize().y) / 2 ));
        obradaIzbora();
		sShell.open();
    }

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
		Integer indeks = medijRepository.getNextMedijIndeks(rbCD.getSelection() ? "CD" : "DVD");
		textID.setText(indeks.toString());
	}

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
		btnOk.addSelectionListener(new HandledSelectionAdapter(sShell) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                medijRepository.saveMedij(
                        Integer.parseInt(textID.getText()),
                        rbCD.getSelection() ? "CD" : "DVD");
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
