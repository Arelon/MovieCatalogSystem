package net.milanaleksic.mcs.application.gui.helper;

import com.google.common.collect.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

/**
 * User: Milan Aleksic
 * Date: 4/24/12
 * Time: 2:26 PM
 */
public class DynamicSelectorText extends Composite {

    public static final int PADDING_IN_ITEM = 3;
    public static final int PADDING_BETWEEN_ITEMS = 5;
    public static final int ARROW_DIMENSION = 6;

    private List<String> selectedItems;

    private Map<String, Object> dataItems = Maps.newHashMap();

    private ControlEditor editor;
    private List<Rectangle> closingButtons;

    public DynamicSelectorText(Composite parent, int style) {
        super(parent, style);
        prepareComponent();
        addListeners();
    }

    private void prepareComponent() {
        if ((getStyle() & SWT.READ_ONLY) == 0)
            prepareComboEditor();
        closingButtons = new LinkedList<>();
    }

    private void prepareComboEditor() {
        editor = new ControlEditor(this);
        final Combo chooser = new Combo(this, SWT.DROP_DOWN);
        chooser.add("<choose>", 0);
        chooser.setBackground(getBackground());
        editor.minimumHeight = 20;
        editor.minimumWidth = 100;
        editor.setEditor(chooser);
        chooser.addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(TraverseEvent e) {
                switch (e.detail) {
                    case SWT.TRAVERSE_RETURN:
                        final String itemText = chooser.getText();
                        if (Arrays.asList(chooser.getItems()).contains(itemText))
                            safeAddSelectItem(itemText);
                        break;
                    case SWT.TRAVERSE_ESCAPE:
                        chooser.select(0);
                        break;
                }
            }
        });
        chooser.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                addSelectedItem(chooser);
            }
        });
    }

    private void addSelectedItem(Combo chooser) {
        final int selectionIndex = chooser.getSelectionIndex();
        if (selectionIndex <= 0 || selectionIndex > chooser.getItemCount())
            return;
        final String item = chooser.getItem(selectionIndex);
        safeAddSelectItem(item);
    }

    private void safeAddSelectItem(String item) {
        if (selectedItems.contains(item))
            return;
        selectedItems.add(item);
        getComboEditor().select(0);
        redraw();
    }

    private Combo getComboEditor() {
        return ((Combo) editor.getEditor());
    }

    private void addListeners() {
        if ((getStyle() & SWT.READ_ONLY) == 0) {
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDown(MouseEvent e) {
                    for (int i = 0; i < closingButtons.size(); i++) {
                        final Rectangle rectangle = closingButtons.get(i);
                        if (e.x < rectangle.x || e.x > rectangle.x + rectangle.width)
                            continue;
                        if (e.y < rectangle.y || e.y > rectangle.y + rectangle.height)
                            continue;
                        selectedItems.remove(i);
                        redraw();
                        return;
                    }
                }
            });
        }
        this.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                closingButtons.clear();
                int xIter = PADDING_IN_ITEM;
                for (String itemToPaint : selectedItems) {
                    final Point textExtent = e.gc.textExtent(itemToPaint);
                    e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

                    e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
                    int arrowSpace = (getStyle() & SWT.READ_ONLY) == 0 ? ARROW_DIMENSION + PADDING_IN_ITEM : 0;
                    e.gc.fillRectangle(xIter, PADDING_IN_ITEM,
                            textExtent.x + PADDING_IN_ITEM * 2 + arrowSpace, textExtent.y + PADDING_IN_ITEM * 2);
                    e.gc.drawRectangle(xIter, PADDING_IN_ITEM,
                            textExtent.x + PADDING_IN_ITEM * 2 + arrowSpace, textExtent.y + PADDING_IN_ITEM * 2);
                    e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));

                    e.gc.drawText(itemToPaint, xIter + PADDING_IN_ITEM, 3 + PADDING_IN_ITEM);

                    e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));

                    xIter += textExtent.x + PADDING_IN_ITEM * 2;

                    if ((getStyle() & SWT.READ_ONLY) == 0) {
                        e.gc.setLineWidth(3);
                        Rectangle closingButton = new Rectangle(xIter, PADDING_IN_ITEM + textExtent.y / 2,
                                ARROW_DIMENSION, ARROW_DIMENSION);
                        closingButtons.add(closingButton);
                        e.gc.drawLine(closingButton.x, closingButton.y, closingButton.x + closingButton.width, closingButton.y + closingButton.height);
                        e.gc.drawLine(closingButton.x + closingButton.width, closingButton.y, closingButton.x, closingButton.y + closingButton.height);
                        e.gc.drawLine(xIter + ARROW_DIMENSION, PADDING_IN_ITEM + textExtent.y / 2,
                                xIter, PADDING_IN_ITEM + ARROW_DIMENSION + textExtent.y / 2);
                        e.gc.setLineWidth(1);

                        xIter += ARROW_DIMENSION + PADDING_IN_ITEM;
                    }
                    xIter += PADDING_BETWEEN_ITEMS;
                }
                if ((getStyle() & SWT.READ_ONLY) == 0)
                    editor.getEditor().setLocation(xIter, PADDING_IN_ITEM);
            }
        });
    }

    public void setItems(Iterable<String> items) {
        this.closingButtons.clear();
        this.selectedItems = Lists.newArrayList();
        if ((getStyle() & SWT.READ_ONLY) == 0) {
            final Combo combo = getComboEditor();
            combo.setItems(Iterables.toArray(items, String.class));
            combo.add("<choose>", 0);
            combo.select(0);
        }
        redraw();
    }

    public void setSelectedItems(Iterable<String> selectedItems) {
        this.selectedItems = Lists.newArrayList(selectedItems);
        redraw();
    }

    public Iterable<String> getSelectedItems() {
        return Iterables.unmodifiableIterable(selectedItems);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void setData(String key, Object data) {
        this.dataItems.put(key, data);
    }

    public Object getData(String key) {
        return this.dataItems.get(key);
    }

    public int getItemCount() {
        if ((getStyle() & SWT.READ_ONLY) != 0) {
            return 0;
        }
        return getComboEditor().getItemCount();
    }
}
