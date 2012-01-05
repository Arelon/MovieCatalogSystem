package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.ApplicationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class ClosingForm {

    @Inject private ApplicationManager applicationManager;

	private Shell sShell = null;
    private ResourceBundle bundle;

    public ClosingForm() {
    }

    public void open() {
        bundle = applicationManager.getMessagesBundle();
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
		sShell.setText(bundle.getString("closing.savingRestoreSQL"));
		sShell.setForeground(new Color(Display.getCurrent(), 0, 0, 0));
		sShell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		sShell.setSize(new Point(180, 30));
		sShell.setLayout(new GridLayout());
        Label label = new Label(sShell, SWT.NONE);
		label.setText(bundle.getString("closing.savingRestoreSQL"));
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		label.setLayoutData(gridData);
		label.setFont(new Font(Display.getDefault(), "Arial", 10, SWT.BOLD));
	}

}
