package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 2/27/12
 * Time: 3:22 PM
 */
public class EditableSingleColumnTableSelectionListener extends SelectionAdapter {

    public interface ContentEditingFinishedListener {
        void contentEditingFinished(String finalContent, Object data);
    }

    private static final int EDITING_COLUMN = 0;

    private final TableEditor editor;

    private final Table sourceTable;

    private final Shell parent;

    private final ResourceBundle bundle;

    private final Optional<ContentEditingFinishedListener> contentEditingFinishedListener;

    public EditableSingleColumnTableSelectionListener(Table sourceTable, Shell parent, ResourceBundle bundle,
                                                      ContentEditingFinishedListener contentEditingFinishedListener) {
        this.sourceTable = sourceTable;
        this.parent = parent;
        this.bundle = bundle;
        this.contentEditingFinishedListener = Optional.fromNullable(contentEditingFinishedListener);
        editor = new TableEditor(sourceTable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50; // The editor must have the same size as the cell and must not be any smaller than 50 pixels.
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        Control oldEditor = editor.getEditor(); // Clean up any previous editor control
        if (oldEditor != null)
            oldEditor.dispose();
        final TableItem item = (TableItem) e.item;
        if (item == null)
            return;
        // The control that will be the editor must be a child of the Table
        Text newEditor = new Text(sourceTable, SWT.BORDER);
        newEditor.setText(item.getText(EDITING_COLUMN));
        newEditor.addTraverseListener(new HandledTraverseListener(parent, bundle) {
            @Override
            public void handledKeyTraversed(TraverseEvent traverseEvent) throws ApplicationException {
                switch (traverseEvent.detail) {
                    case SWT.TRAVERSE_RETURN:
                        Text textEditor = (Text) editor.getEditor();
                        String text = textEditor.getText();
                        if (text.isEmpty())
                            throw new ApplicationException("Empty text is not allowed");
                        fireContentChanged(text, item.getData());
                        editor.getEditor().dispose();
                        break;
                    case SWT.TRAVERSE_ESCAPE:
                        editor.getEditor().dispose();
                        break;
                    default:
                        break;
                }
            }
        });
        newEditor.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent focusEvent) {
                editor.getEditor().dispose();
            }
        });
        newEditor.selectAll();
        newEditor.setFocus();
        editor.setEditor(newEditor, item, EDITING_COLUMN);
    }

    private void fireContentChanged(String text, Object data) {
        editor.getItem().setText(EDITING_COLUMN, text);
        if (contentEditingFinishedListener.isPresent())
            contentEditingFinishedListener.get().contentEditingFinished(text, data);
    }
}
