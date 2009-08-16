package net.milanaleksic.mcs;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @author Milan
 * 23 Sep 2007
 */
public class ComboWrongIndexSelectionListener implements SelectionListener {
	
	public ComboWrongIndexSelectionListener() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		Combo combo = (Combo)e.widget;
		if (combo.getSelectionIndex()==1)
			combo.select(0);
	}

}
