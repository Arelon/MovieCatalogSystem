package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:37 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class TransformerTest {

    @Test
    public void form_creation() throws TransformerException {
        Shell form = new Transformer(ResourceBundle.getBundle("messages"))
                .createFormFromResource("/net/milanaleksic/mcs/infrastructure/gui/transformer/TestForm.gui");
        assertThat(form, not(nullValue()));
        assertThat(form.getText(), equalTo("Delete movie"));
        assertThat(form.getSize(), equalTo(new Point(431,154)));
        assertThat(form.getLayout(), not(nullValue()));
        assertThat(form.getLayout(), instanceOf(GridLayout.class));
        GridLayout layout = (GridLayout) form.getLayout();
        assertThat(layout.numColumns, equalTo(2));
    }

}
