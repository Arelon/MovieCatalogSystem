import com.google.common.collect.Lists;
import net.milanaleksic.guitransformer.EmbeddedComponent;
import net.milanaleksic.mcs.application.gui.AbstractTransformedForm;
import net.milanaleksic.mcs.application.gui.helper.DynamicSelectorText;

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
        mediumListValue.setItems(Lists.asList("first", new String[] {"second", "third", "fourth", "fifth"})); //NON-NLS
        mediumListValue.setSelectedItems(Lists.asList("first", new String[] {"second", "third", "fourth", "fifth"})); //NON-NLS
    }
}
