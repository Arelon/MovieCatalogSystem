package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.List;

public class NewMediumDialogForm extends AbstractDialogForm {

    @Inject private TipMedijaRepository tipMedijaRepository;

    @Inject private MedijRepository medijRepository;

    @Inject private MedijService medijService;

    private Text textID = null;
    private TipMedija selectedMediumType = null;
    private Group group = null;

	@Override protected void onShellCreated() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData2.grabExcessHorizontalSpace = true;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		shell.setText(bundle.getString("newMedium.addNewMedium"));
		shell.setSize(new Point(288, 189));
		shell.setLayout(gridLayout2);
        Label label2 = new Label(shell, SWT.LEFT);
		label2.setText(bundle.getString("newMedium.mediumType"));
		label2.setLayoutData(gridData1);
		createGroup();
        Label label3 = new Label(shell, SWT.NONE);
		label3.setText(bundle.getString("newMedium.mediumWillHaveFollowingId"));
		label3.setLayoutData(gridData2);
		textID = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		createComposite();
	}

    @Override
    protected void onShellReady() {
       List<TipMedija> tipMedijas = tipMedijaRepository.getTipMedijas();
        for (TipMedija tipMedija : tipMedijas) {
            Button mediumTypeBtn = new Button(group, SWT.RADIO);
            mediumTypeBtn.setText(tipMedija.getNaziv());
            mediumTypeBtn.setData(tipMedija);
            mediumTypeBtn.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                    obradaIzbora(((Button) e.getSource()).getData());
                }
            });
        }
    }

    private void createGroup() {
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 1;
        group = new Group(shell, SWT.NONE);
		group.setLayout(gridLayout1);
 	}

	public void obradaIzbora(Object mediumTypeAsObject) {
        TipMedija tipMedija = (TipMedija) mediumTypeAsObject;
        Integer indeks = medijService.getNextMedijIndeks(tipMedija.getNaziv());
		textID.setText(indeks.toString());
        selectedMediumType = tipMedija;
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
        Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
        Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setText(bundle.getString("global.save"));
		btnOk.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (selectedMediumType == null) {
                    MessageBox box = new MessageBox(parent, SWT.ICON_ERROR);
                    box.setMessage(bundle.getString("global.youHaveToSelectMediumType"));
                    box.setText("Error");
                    box.open();
                    return;
                }
                medijRepository.saveMedij(Integer.parseInt(textID.getText()), selectedMediumType);
                runnerWhenClosingShouldRun = true;
                shell.close();
            }
        });
        Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText(bundle.getString("global.cancel"));
		btnCancel.setLayoutData(gridData12);
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
	}

}
