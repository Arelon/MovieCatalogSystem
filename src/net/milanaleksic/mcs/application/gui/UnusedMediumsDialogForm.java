package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
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
    private final Listener btnDeleteUnusedMediumSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            if (unusedMediumTable.getSelectionIndex() < 0)
                return;
            Medij medij = (Medij) unusedMediumTable.getItem(unusedMediumTable.getSelectionIndex()).getData();
            logger.warn("Deleting medium: " + medij); //NON-NLS
            medijRepository.deleteMediumType(medij);
            UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
            readData();
        }
    };

    @EmbeddedEventListener(component = "btnDeleteAllUnusedMediums", event = SWT.Selection)
    private final Listener btnDeleteAllUnusedMediumsSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            for (int i = 0; i < unusedMediumTable.getItemCount(); i++) {
                Medij medij = (Medij) unusedMediumTable.getItem(i).getData();
                logger.warn("Deleting medium: " + medij); //NON-NLS
                medijRepository.deleteMediumType(medij);
                UnusedMediumsDialogForm.super.runnerWhenClosingShouldRun = true;
            }
            readData();
        }
    };

    @EmbeddedEventListener(component = "btnClose", event = SWT.Selection)
    private final Listener btnCloseSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
        }
    };

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
