package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.guitransformer.*;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class UnusedMediumsDialogForm extends AbstractTransformedForm {

    @Inject
    private MedijRepository medijRepository;

    @Inject
    private MedijService medijService;

    @EmbeddedComponent
    private Table unusedMediumTable = null;

    @EmbeddedEventListener(component = "btnDeleteUnusedMedium", event = SWT.Selection)
    private void btnDeleteUnusedMediumSelectionListener() {
        if (unusedMediumTable.getSelectionIndex() < 0)
            return;
        Medij medij = (Medij) unusedMediumTable.getItem(unusedMediumTable.getSelectionIndex()).getData();
        logger.warn("Deleting medium: " + medij); //NON-NLS
        medijRepository.deleteMediumType(medij);
        UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
        readData();
    }

    @EmbeddedEventListener(component = "btnDeleteAllUnusedMediums", event = SWT.Selection)
    private void btnDeleteAllUnusedMediumsSelectionListener() {
        for (int i = 0; i < unusedMediumTable.getItemCount(); i++) {
            Medij medij = (Medij) unusedMediumTable.getItem(i).getData();
            logger.warn("Deleting medium: " + medij); //NON-NLS
            medijRepository.deleteMediumType(medij);
            UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
        }
        readData();
    }

    @EmbeddedEventListener(component = "btnClose", event = SWT.Selection)
    private void btnCloseSelectionListener() {
        shell.close();
    }

    @Override
    protected void onShellReady() {
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

}
