package net.milanaleksic.mcs.application.gui.helper;

import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.infrastructure.tmdb.bean.Movie;
import net.milanaleksic.mcs.infrastructure.util.IMDBUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 3/5/12
 * Time: 11:54 AM
 */
public class ImdbLinkColumnFactory {

    public static void create(Shell shell, int columnWhereEditorShouldBePlaced, final Movie movie,
                              ResourceBundle bundle, Table possibleMatchesTable, TableItem tableItemParent) {
        TableEditor editor = new TableEditor(possibleMatchesTable);
        final Link link = new Link(possibleMatchesTable, SWT.NONE);
        link.setText("<A>" + bundle.getString("unmatchedMoviesTable.matches.url") + "</A>"); //NON-NLS
        link.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        link.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                try {
                    Desktop.getDesktop().browse(IMDBUtil.createUriBasedOnId(movie.getImdbId()));
                } catch (IOException e) {
                    throw new ApplicationException("Unexpected IO exception when trying to open URL based on received IMDB link");
                }
            }
        });
        link.pack();
        editor.minimumWidth = link.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(link, tableItemParent, columnWhereEditorShouldBePlaced);
        tableItemParent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent disposeEvent) {
                link.dispose();
            }
        });
    }

}
