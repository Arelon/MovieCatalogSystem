package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.hamcrest.Matchers;
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
        Transformer transformer = new Transformer(ResourceBundle.getBundle("messages"));
        Shell form = transformer
                .createFormFromResource("/net/milanaleksic/mcs/infrastructure/gui/transformer/TestForm.gui");
        assertThat(form, not(nullValue()));
        assertThat(form.getText(), equalTo("Delete movie"));
        assertThat(form.getSize(), equalTo(new Point(431,154)));
        assertThat(form.getLayout(), not(nullValue()));
        assertThat(form.getLayout(), instanceOf(GridLayout.class));
        GridLayout layout = (GridLayout) form.getLayout();
        assertThat(layout.numColumns, equalTo(2));

        Control[] children = form.getChildren();
        assertThat(children, not(nullValue()));
        assertThat(children.length, equalTo(3));

        assertThat(children[0], Matchers.instanceOf(Label.class));
        assertThat(((Label)children[0]).getText(), equalTo("Do you really wish to delete movie??"));

        assertThat(children[1], Matchers.<Object>instanceOf(Label.class));
        assertThat(((Label)children[1]).getText(), equalTo(""));
        assertThat((Control)transformer.getMappedObject("labFilmNaziv").get(), equalTo(children[1]));

        assertThat(children[2], Matchers.instanceOf(Label.class));
        assertThat(((Label)children[2]).getText(), equalTo(""));
    }

}
