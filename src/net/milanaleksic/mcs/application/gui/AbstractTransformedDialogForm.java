package net.milanaleksic.mcs.application.gui;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.infrastructure.gui.transformer.*;
import net.milanaleksic.mcs.infrastructure.util.MethodTiming;

import java.lang.annotation.*;
import java.lang.reflect.*;

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
    @MethodTiming(name = "GUI transformation")
    protected final void onShellCreated() {
        Transformer transformer = new Transformer(bundle);
        try {
            String thisClassNameAsResourceLocation = this.getClass().getCanonicalName().replaceAll("\\.", "/");
            String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS
            transformer.fillForm(formName, shell);
            embedComponents(transformer);
            onTransformationComplete(transformer);
        } catch (TransformerException e) {
            logger.error("Transformation failed", e); //NON-NLS
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unexpected problem while generating form", e);
        }
    }

    private void embedComponents(Transformer transformer) throws IllegalAccessException {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            EmbeddedComponent annotation = field.getAnnotation(EmbeddedComponent.class);
            if (annotation == null)
                continue;
            String name = annotation.name();
            if (name.isEmpty())
                name = field.getName();
            if ((field.getModifiers() & Modifier.PRIVATE) != 0)
                throw new IllegalStateException("Embedded components can not be private fields: "+this.getClass().getName()+"."+field.getName());
            Optional<Object> mappedObject = transformer.getMappedObject(name);
            if (!mappedObject.isPresent())
                throw new IllegalStateException("Field marked as embedded could not be found: "+this.getClass().getName()+"."+field.getName());
            field.set(this, mappedObject.get());
        }
    }

    protected void onTransformationComplete(Transformer transformer) {
    }

}
