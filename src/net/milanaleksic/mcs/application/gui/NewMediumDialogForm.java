package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
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

    private Optional<TipMedija> selectedMediumType = Optional.absent();

    private Text textID;
    private Group group;

	@Override protected void onShellCreated() {
		shell.setText(bundle.getString("newMedium.addNewMedium"));
		shell.setSize(new Point(288, 189));
		shell.setLayout(new GridLayout(2, false));
        Label label2 = new Label(shell, SWT.LEFT);
		label2.setText(bundle.getString("newMedium.mediumType"));
		label2.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
		group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
        Label label3 = new Label(shell, SWT.NONE);
		label3.setText(bundle.getString("newMedium.mediumWillHaveFollowingId"));
		label3.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
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
                    TipMedija tipMedija = (TipMedija) e.widget.getData();
                    Integer indeks = medijService.getNextMedijIndeks(tipMedija.getNaziv());
                    textID.setText(indeks.toString());
                    selectedMediumType = Optional.of(tipMedija);
                }
            });
        }
    }

	private void createComposite() {
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 20;
        Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, true, true, 2, 1));
        Button btnOk = new Button(composite, SWT.NONE);
		btnOk.setText(bundle.getString("global.save"));
		btnOk.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (!selectedMediumType.isPresent()) {
                    MessageBox box = new MessageBox(parent.orNull(), SWT.ICON_ERROR);
                    box.setMessage(bundle.getString("global.youHaveToSelectMediumType"));
                    box.setText(bundle.getString("global.error"));
                    box.open();
                    return;
                }
                medijRepository.saveMedij(Integer.parseInt(textID.getText()), selectedMediumType.get());
                runnerWhenClosingShouldRun = true;
                shell.close();
            }
        });
        Button btnCancel = new Button(composite, SWT.NONE);
		btnCancel.setText(bundle.getString("global.cancel"));
		btnCancel.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, true));
		btnCancel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
	}

}
