package net.milanaleksic.mcs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ClosingForm {

	private Shell sShell = null;

    public ClosingForm() {
		createSShell();
        Rectangle monitorBounds = Display.getCurrent().getPrimaryMonitor().getBounds();
		sShell.setLocation(
				new Point(
						Math.abs(monitorBounds.width-sShell.getSize().x) / 2,
						Math.abs(monitorBounds.height-sShell.getSize().y) / 2 ));
		sShell.open();
	}

	private void createSShell() {
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		sShell = new Shell(SWT.NO_TRIM | SWT.BORDER);
		sShell.setText("Снимам restore SQL...");
		sShell.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
		sShell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		sShell.setSize(new Point(180, 30));
		sShell.setLayout(new GridLayout());
        Label label = new Label(sShell, SWT.NONE);
		label.setText("Снимам restore SQL...");
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		label.setLayoutData(gridData);
		label.setFont(new Font(Display.getDefault(), "Arial", 10, SWT.BOLD));
	}

}
