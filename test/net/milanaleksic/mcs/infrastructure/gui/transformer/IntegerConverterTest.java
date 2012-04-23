package net.milanaleksic.mcs.infrastructure.gui.transformer;

import org.codehaus.jackson.node.TextNode;
import org.eclipse.swt.layout.GridData;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * User: Milan Aleksic
 * Date: 4/20/12
 * Time: 9:44 AM
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class IntegerConverterTest {

    @Test
    public void convert_simple_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat((Integer) integerConverter.getValueFromJson(new TextNode("173")), equalTo(173));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat((Integer) integerConverter.getValueFromJson(new TextNode("{grid.center}")), equalTo(GridData.CENTER));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void convert_multiple_magic_value() {
        IntegerConverter integerConverter = new IntegerConverter();
        try {
            assertThat((Integer) integerConverter.getValueFromJson(new TextNode("1|2")), equalTo(1 | 2));
            assertThat((Integer) integerConverter.getValueFromJson(new TextNode("{grid.center}|{grid.begin}")), equalTo(GridData.CENTER | GridData.BEGINNING));
        } catch (TransformerException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
