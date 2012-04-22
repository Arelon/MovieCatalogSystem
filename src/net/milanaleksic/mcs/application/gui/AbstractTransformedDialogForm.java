package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.infrastructure.gui.transformer.*;

import javax.inject.Inject;
import java.lang.annotation.*;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 11:35 AM
 */
public abstract class AbstractTransformedDialogForm extends AbstractDialogForm {

    @Inject
    private Transformer transformer;

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    @Override
    protected final void onShellCreated() {
        try {
            TransformationContext transformationContext = transformer.fillManagedForm(this, shell);
            onTransformationComplete(transformationContext);
        } catch (TransformerException e) {
            logger.error("Transformation failed", e); //NON-NLS
        }
    }

    protected void onTransformationComplete(TransformationContext transformationContext) {
    }

}
