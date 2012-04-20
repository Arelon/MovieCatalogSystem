package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import net.milanaleksic.mcs.infrastructure.gui.transformer.Transformer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.List;

public class NewMediumDialogForm extends AbstractTransformedDialogForm {

    @Inject private TipMedijaRepository tipMedijaRepository;

    @Inject private MedijRepository medijRepository;

    @Inject private MedijService medijService;

    private Optional<TipMedija> selectedMediumType = Optional.absent();

    @EmbeddedComponent
    Text textID = null;

    @EmbeddedComponent
    Group group = null;

	@Override protected void onTransformationComplete(Transformer transformer) {
        transformer.<Button>getMappedObject("btnOk").get().addSelectionListener(new HandledSelectionAdapter(shell, bundle) { //NON-NLS
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
        transformer.<Button>getMappedObject("btnCancel").get().addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() { //NON-NLS
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                shell.close();
            }
        });
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

}
