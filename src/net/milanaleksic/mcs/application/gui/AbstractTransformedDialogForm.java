package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.infrastructure.gui.transformer.*;

import javax.inject.Inject;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 11:35 AM
 */
public abstract class AbstractTransformedDialogForm extends AbstractDialogForm {

    @Inject
    private Transformer transformer;

    private boolean formTransformationComplete = false;

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    @Override final protected void createShell() {
        try {
            TransformationContext transformationContext = transformer.fillManagedForm(this.parent.orNull(), this);
            this.shell = transformationContext.getShell();
            onTransformationComplete(transformationContext);
        } catch (TransformerException e) {
            logger.error("Transformation failed", e); //NON-NLS
        } finally {
            formTransformationComplete = true;
        }
    }

    @Override
    protected void onShellCreated() {
    }

    protected boolean isFormTransformationComplete() {
        return formTransformationComplete;
    }

    protected void onTransformationComplete(TransformationContext transformationContext) {
    }

}
