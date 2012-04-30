import com.google.common.collect.Lists;
import net.milanaleksic.mcs.application.gui.AbstractTransformedForm;
import net.milanaleksic.mcs.application.gui.helper.DynamicSelectorText;
import net.milanaleksic.mcs.infrastructure.gui.transformer.EmbeddedComponent;

/**
 * User: Milan Aleksic
 * Date: 4/28/12
 * Time: 9:54 AM
 */
public class TestForm extends AbstractTransformedForm {

    @EmbeddedComponent
    private DynamicSelectorText mediumListValue;


    @Override
    protected void onShellCreated() {
        mediumListValue.setItems(Lists.asList("first", new String[] {"second", "third", "fourth", "fifth"}));
        mediumListValue.setSelectedItems(Lists.asList("first", new String[] {"second", "third", "fourth", "fifth"}));
    }
}
