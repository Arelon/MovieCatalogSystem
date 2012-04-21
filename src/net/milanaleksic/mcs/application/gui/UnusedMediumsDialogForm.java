package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import net.milanaleksic.mcs.infrastructure.gui.transformer.TransformationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class UnusedMediumsDialogForm extends AbstractTransformedDialogForm {

    @Inject private MedijRepository medijRepository;

    @Inject private MedijService medijService;

    @EmbeddedComponent
    Table unusedMediumTable = null;

    @Override protected void onTransformationComplete(TransformationContext transformer) {
        transformer.<Button>getMappedObject("btnDeleteUnusedMedium").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) { //NON-NLS
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
        transformer.<Button>getMappedObject("btnDeleteAllUnusedMediums").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) { //NON-NLS
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
        transformer.<Button>getMappedObject("btnClose").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) { //NON-NLS
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                shell.close();
            }
        });
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

}
