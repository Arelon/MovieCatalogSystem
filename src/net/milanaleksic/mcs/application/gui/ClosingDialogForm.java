package net.milanaleksic.mcs.application.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ClosingDialogForm extends AbstractDialogForm {

    @Override protected void createShell(Shell parent) {
        shell = new Shell(SWT.NO_TRIM | SWT.BORDER);
    }

	@Override protected void onShellCreated() {
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		shell.setText(bundle.getString("closing.savingRestoreSQL"));
        Color color = new Color(Display.getCurrent(), 0, 0, 0);
        shell.setForeground(color);
        color.dispose();
		shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		shell.setSize(new Point(180, 30));
		shell.setLayout(new GridLayout());
        Label label = new Label(shell, SWT.NONE);
		label.setText(bundle.getString("closing.savingRestoreSQL"));
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		label.setLayoutData(gridData);
		label.setFont(new Font(Display.getDefault(), "Arial", 10, SWT.BOLD));
        Rectangle monitorBounds = Display.getCurrent().getPrimaryMonitor().getBounds();
		shell.setLocation(
				new Point(
						Math.abs(monitorBounds.width-shell.getSize().x) / 2,
						Math.abs(monitorBounds.height-shell.getSize().y) / 2 ));
	}
}
