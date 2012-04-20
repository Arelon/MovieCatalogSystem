package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;

import java.lang.annotation.*;
import java.lang.reflect.Field;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 11:35 AM
 */
public abstract class AbstractTransformedDialogForm extends AbstractDialogForm {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface EmbeddedComponent {
        String name() default "";
    }

    @Override
    @MethodTiming
    protected void onShellCreated() {
        Transformer transformer = new Transformer(bundle);
        try {
            String thisClassNameAsResourceLocation = this.getClass().getCanonicalName().replaceAll("\\.", "/");
            String formName = "/"+thisClassNameAsResourceLocation+".gui"; //NON-NLS
            transformer.fillForm(formName, shell);
            onTransformationComplete(transformer);
        } catch (TransformerException e) {
            logger.error("Transformation failed", e); //NON-NLS
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unexpected problem while generating form", e);
        }
    }

    protected void onTransformationComplete(Transformer transformer) throws IllegalAccessException {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            field.set(this, transformer.getMappedObject(name).get());
        }
    }

}
