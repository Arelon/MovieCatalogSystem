package net.milanaleksic.mcs;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.Color;

public class ClosingForm {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label label = null;
	
	public ClosingForm() {
		createSShell();
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		sShell.setLocation(
				new Point(
						Math.abs(size.width-sShell.getSize().x) / 2, 
						Math.abs(size.height-sShell.getSize().y) / 2 ));
		sShell.open();
	}

	/**
	 * This method initializes sShell
	 */
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
		sShell.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				// nista
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// nista
			}
			
		});
		label = new Label(sShell, SWT.NONE);
		label.setText("Снимам restore SQL...");
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		label.setLayoutData(gridData);
		label.setFont(new Font(Display.getDefault(), "Arial", 10, SWT.BOLD));
	}

}
