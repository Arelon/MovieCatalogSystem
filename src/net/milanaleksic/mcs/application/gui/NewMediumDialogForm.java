package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.helper.*;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.*;
import net.milanaleksic.mcs.domain.service.MedijService;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.List;

public class NewMediumDialogForm extends AbstractTransformedDialogForm {

    @Inject
    private TipMedijaRepository tipMedijaRepository;

    @Inject
    private MedijRepository medijRepository;

    @Inject
    private MedijService medijService;

    private Optional<TipMedija> selectedMediumType = Optional.absent();

    @EmbeddedComponent
    private Text textID = null;

    @EmbeddedComponent
    private Group group = null;

    @EmbeddedEventListener(component = "btnOk", event = SWT.Selection)
    private final Listener shellCloseListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
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
    };

    @EmbeddedEventListener(component = "btnCancel", event = SWT.Selection)
    private final Listener btnCloseSelectionListener = new HandledListener(this) {
        @Override
        public void safeHandleEvent(Event event) throws ApplicationException {
            shell.close();
        }
    };

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
