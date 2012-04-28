package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.base.Optional;
import net.milanaleksic.mcs.application.gui.Form;
import net.milanaleksic.mcs.application.util.ApplicationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

/**
 * User: Milan Aleksic
 * Date: 2/27/12
 * Time: 3:22 PM
 */
public class EditableSingleColumnTableSelectionListener implements Listener {

    private static final Logger log = Logger.getLogger(EditableSingleColumnTableSelectionListener.class);

    private Form form;

    public interface ContentEditingFinishedListener {
        void contentEditingFinished(String finalContent, Object data);
    }

    private static final int EDITING_COLUMN = 0;

    private Optional<TableEditor> editor = Optional.absent();

    private final Optional<ContentEditingFinishedListener> contentEditingFinishedListener;

    public EditableSingleColumnTableSelectionListener(Form form, ContentEditingFinishedListener contentEditingFinishedListener) {
        this.form = form;
        this.contentEditingFinishedListener = Optional.fromNullable(contentEditingFinishedListener);
    }

    public void prepare(Table sourceTable) {
        TableEditor editor = new TableEditor(sourceTable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50; // The editor must have the same size as the cell and must not be any smaller than 50 pixels.
        this.editor = Optional.of(editor);
    }

    @Override
    public void handleEvent(Event e) {
        if (!editor.isPresent()) {
            log.warn("Editor is not prepared for form "+form.getClass().getName()); //NON-NLS
            return;
        }
        Control oldEditor = editor.get().getEditor(); // Clean up any previous editor control
        if (oldEditor != null)
            oldEditor.dispose();
        final TableItem item = (TableItem) e.item;
        if (item == null)
            return;
        // The control that will be the editor must be a child of the Table
        Text newEditor = new Text((Table) e.widget, SWT.BORDER);
        newEditor.setText(item.getText(EDITING_COLUMN));
        newEditor.addTraverseListener(new HandledTraverseListener(form.getShell(), form.getResourceBundle()) {
            @Override
            public void handledKeyTraversed(TraverseEvent traverseEvent) throws ApplicationException {
                switch (traverseEvent.detail) {
                    case SWT.TRAVERSE_RETURN:
                        Text textEditor = (Text) editor.get().getEditor();
                        String text = textEditor.getText();
                        if (text.isEmpty())
                            throw new ApplicationException("Empty text is not allowed");
                        fireContentChanged(text, item.getData());
                        editor.get().getEditor().dispose();
                        break;
                    case SWT.TRAVERSE_ESCAPE:
                        editor.get().getEditor().dispose();
                        break;
                    default:
                        break;
                }
            }
        });
        newEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                editor.get().getEditor().dispose();
            }
        });
        newEditor.selectAll();
        newEditor.setFocus();
        editor.get().setEditor(newEditor, item, EDITING_COLUMN);
    }

    private void fireContentChanged(String text, Object data) {
        editor.get().getItem().setText(EDITING_COLUMN, text);
        if (contentEditingFinishedListener.isPresent())
            contentEditingFinishedListener.get().contentEditingFinished(text, data);
    }
}
