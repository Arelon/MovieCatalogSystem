package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Medij;
import net.milanaleksic.mcs.domain.model.MedijRepository;
import net.milanaleksic.mcs.domain.service.MedijService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class UnusedMediumsDialogForm extends AbstractDialogForm {

    @Inject private MedijRepository medijRepository;

    @Inject private MedijService medijService;

    private Table unusedMediumTable;

    @Override protected void onShellCreated() {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        shell.setText(bundle.getString("global.unusedMediums"));
        shell.setLayout(gridLayout);
        createContent();
    }

    @Override protected void onShellReady() {
        readData();
    }

    private void readData() {
        java.util.List<Medij> mediji = medijService.getListOfUnusedMediums();
        unusedMediumTable.removeAll();
        for (Medij medij : mediji) {
            TableItem tableItem = new TableItem(unusedMediumTable, SWT.NONE);
            tableItem.setText(medij.toString());
            tableItem.setData(medij);
        }
    }

    private void createContent() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        unusedMediumTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        unusedMediumTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
        gridData.heightHint = 180;
        unusedMediumTable.setLayoutData(gridData);
        TableColumn tableColumn = new TableColumn(unusedMediumTable, SWT.LEFT | SWT.FLAT);
        tableColumn.setText(bundle.getString("unusedMediums.columnName"));
        tableColumn.setWidth(370);
        Button btnDeleteUnusedMedium = new Button(composite, SWT.NONE);
        btnDeleteUnusedMedium.setText(bundle.getString("global.delete"));
        btnDeleteUnusedMedium.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnDeleteUnusedMedium.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                if (unusedMediumTable.getSelectionIndex() < 0)
                    return;
                Medij medij = (Medij) unusedMediumTable.getItem(unusedMediumTable.getSelectionIndex()).getData();
                logger.warn("Deleting medium: " + medij); //NON-NLS
                medijRepository.deleteMediumType(medij);
                UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
                readData();
            }
        });
        Button btnDeleteAllUnusedMediums = new Button(composite, SWT.NONE);
        btnDeleteAllUnusedMediums.setText(bundle.getString("global.deleteAll"));
        btnDeleteAllUnusedMediums.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnDeleteAllUnusedMediums.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                for (int i = 0; i < unusedMediumTable.getItemCount(); i++) {
                    Medij medij = (Medij) unusedMediumTable.getItem(i).getData();
                    logger.warn("Deleting medium: " + medij); //NON-NLS
                    medijRepository.deleteMediumType(medij);
                    UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
                }
                readData();
            }
        });

        Composite compositeFooter = new Composite(composite, SWT.NONE);
        compositeFooter.setLayout(new GridLayout(1, false));
        compositeFooter.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        Button btnClose = new Button(compositeFooter, SWT.NONE);
        btnClose.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
        btnClose.setText(bundle.getString("global.close"));
        btnClose.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                shell.close();
            }
        });
    }

}
