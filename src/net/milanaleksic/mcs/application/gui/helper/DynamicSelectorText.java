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
public class DynamicSelectorText extends Composite implements PaintListener {

    private static final int PADDING_IN_ITEM = 3;
    private static final int CLOSER_DIMENSION = 6;
    private static final int PADDING_BETWEEN_ITEMS = 5;
    private static final int BORDER_SUM_BETWEEN_ITEMS = 2;

    private List<String> selectedItems;

    private Map<String, Object> dataItems = Maps.newHashMap();

    private ControlEditor editor;
    private List<Rectangle> closingButtons;

    private Color selectedItemBackgroundColor;

    private Color selectedItemForegroundColor;

    private Color closerColor;

    public DynamicSelectorText(Composite parent, int style) {
        super(parent, style);
        prepareComponent();
        addListeners();
    }

    private void prepareComponent() {
        if (isModifiable())
            prepareComboEditor();
        closingButtons = new LinkedList<>();
        selectedItemBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        selectedItemForegroundColor = getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
        closerColor = getDisplay().getSystemColor(SWT.COLOR_GRAY);
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
                        else
                            chooser.select(0);
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
        if (isModifiable()) {
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
        this.addPaintListener(this);
    }

    @Override
    public void paintControl(PaintEvent e) {
        closingButtons.clear();
        int xIter = PADDING_IN_ITEM, yIter = PADDING_IN_ITEM;
        if (selectedItems != null)
            for (String itemToPaint : selectedItems) {
                final Point textExtent = e.gc.textExtent(itemToPaint);

                drawItemRectangle(e, xIter, yIter, itemToPaint, textExtent);
                xIter += textExtent.x + PADDING_IN_ITEM * 2;

                if (isModifiable()) {
                    drawItemCloser(e, xIter, yIter, textExtent);
                    xIter += CLOSER_DIMENSION + PADDING_IN_ITEM;
                }
                xIter += PADDING_BETWEEN_ITEMS + BORDER_SUM_BETWEEN_ITEMS;
                if ((isModifiable() && xIter + editor.minimumWidth > getBounds().width)
                        || (!isModifiable() && xIter > getBounds().width)) {
                    xIter = PADDING_IN_ITEM;
                    yIter += textExtent.y + 2 * PADDING_IN_ITEM + PADDING_BETWEEN_ITEMS;
                }
            }
        if (isModifiable())
            editor.getEditor().setLocation(xIter, yIter);
    }

    private void drawItemCloser(PaintEvent e, int xIter, int yIter, Point textExtent) {
        final Color prevForeground = e.gc.getForeground();
        e.gc.setForeground(closerColor);
        e.gc.setLineWidth(3);
        Rectangle closingButton = new Rectangle(xIter, yIter + textExtent.y / 2,
                CLOSER_DIMENSION, CLOSER_DIMENSION);
        closingButtons.add(closingButton);
        e.gc.drawLine(closingButton.x, closingButton.y, closingButton.x + closingButton.width, closingButton.y + closingButton.height);
        e.gc.drawLine(closingButton.x + closingButton.width, closingButton.y, closingButton.x, closingButton.y + closingButton.height);
        e.gc.setLineWidth(1);
        e.gc.setForeground(prevForeground);
    }

    private void drawItemRectangle(PaintEvent e, int xIter, int yIter, String itemToPaint, Point textExtent) {
        final Color previousBackground = e.gc.getBackground();
        final Color previousForeground = e.gc.getForeground();

        e.gc.setBackground(selectedItemBackgroundColor);
        e.gc.setForeground(selectedItemForegroundColor);
        int arrowSpace = isModifiable() ? CLOSER_DIMENSION + PADDING_IN_ITEM : 0;
        Rectangle itemRectangle = new Rectangle(xIter, yIter,
                textExtent.x + PADDING_IN_ITEM * 3 + arrowSpace, textExtent.y + PADDING_IN_ITEM * 2);
        e.gc.fillRoundRectangle(itemRectangle.x, itemRectangle.y, itemRectangle.width, itemRectangle.height, 5, 5);
        e.gc.drawRoundRectangle(itemRectangle.x, itemRectangle.y, itemRectangle.width, itemRectangle.height, 5, 5);
//        e.gc.fillRectangle(itemRectangle);
//        e.gc.drawRectangle(itemRectangle);

        e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        e.gc.drawText(itemToPaint, xIter + PADDING_IN_ITEM, 3 + yIter);

        e.gc.setBackground(previousBackground);
        e.gc.setForeground(previousForeground);
    }

    private boolean isModifiable() {
        return (getStyle() & SWT.READ_ONLY) == 0;
    }

    public void setItems(Iterable<String> items) {
        this.closingButtons.clear();
        this.selectedItems = Lists.newArrayList();
        if (isModifiable()) {
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
        if (!isModifiable()) {
            return 0;
        }
        return getComboEditor().getItemCount();
    }

    public Color getSelectedItemBackgroundColor() {
        return selectedItemBackgroundColor;
    }

    public void setSelectedItemBackgroundColor(Color selectedItemBackgroundColor) {
        this.selectedItemBackgroundColor = selectedItemBackgroundColor;
    }

    public Color getSelectedItemForegroundColor() {
        return selectedItemForegroundColor;
    }

    public void setSelectedItemForegroundColor(Color selectedItemForegroundColor) {
        this.selectedItemForegroundColor = selectedItemForegroundColor;
    }

    public Color getCloserColor() {
        return closerColor;
    }

    public void setCloserColor(Color closerColor) {
        this.closerColor = closerColor;
    }
}
