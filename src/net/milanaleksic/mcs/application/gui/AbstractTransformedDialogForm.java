package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;

import javax.inject.Inject;
import java.lang.annotation.*;
import java.lang.reflect.*;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 11:35 AM
 */
public abstract class AbstractTransformedDialogForm extends AbstractDialogForm {

    @Inject
    private Transformer transformer;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface EmbeddedComponent {
        String name() default "";
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    @Override
    @MethodTiming(name = "GUI transformation")
    protected final void onShellCreated() {
        try {
            String thisClassNameAsResourceLocation = this.getClass().getCanonicalName().replaceAll("\\.", "/");
            String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS
            TransformationContext transformationContext = transformer.fillForm(formName, shell);
            embedComponents(transformationContext);
            onTransformationComplete(transformationContext);
        } catch (TransformerException e) {
            logger.error("Transformation failed", e); //NON-NLS
        }
    }

    private void embedComponents(TransformationContext transformationContext) throws TransformerException {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            if ((field.getModifiers() & Modifier.PRIVATE) != 0)
                throw new IllegalStateException("Embedded components can not be private fields: " + this.getClass().getName() + "." + field.getName());
            Optional<Object> mappedObject = transformationContext.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: " + this.getClass().getName() + "." + field.getName());
            try {
                field.set(this, mappedObject.get());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TransformerException("Error while embedding component field named " + field.getName(), e);
            }
        }
    }

    protected void onTransformationComplete(TransformationContext transformationContext) {
    }

}
