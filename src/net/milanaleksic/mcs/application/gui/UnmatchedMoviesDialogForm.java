package net.milanaleksic.mcs.application.gui;

import net.milanaleksic.mcs.application.gui.helper.HandledSelectionAdapter;
import net.milanaleksic.mcs.application.util.ApplicationException;
import net.milanaleksic.mcs.domain.model.Film;
import net.milanaleksic.mcs.domain.service.FilmService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.inject.Inject;

public class UnmatchedMoviesDialogForm extends AbstractDialogForm {

    @Inject private FilmService filmService;

    private Table unmatchedMoviesTable;

    @Override protected void onShellCreated() {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.verticalSpacing = 10;
        shell.setText(bundle.getString("global.unusedMediums"));
        shell.setLayout(gridLayout);
        createContent();
    }

    @Override protected void onShellReady() {
        readData();
    }

    private void readData() {
        java.util.List<Film> filmovi = filmService.getListOfUnmatchedMovies();
        unmatchedMoviesTable.removeAll();
        for (Film film : filmovi) {
            TableItem tableItem = new TableItem(unmatchedMoviesTable, SWT.NONE);
            tableItem.setText(film.toString());
            tableItem.setData(film);
        }
    }

    private void createContent() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        unmatchedMoviesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        unmatchedMoviesTable.setHeaderVisible(true);
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        gridData.heightHint = 100;
        unmatchedMoviesTable.setLayoutData(gridData);
        TableColumn tableColumn = new TableColumn(unmatchedMoviesTable, SWT.LEFT | SWT.FLAT);
        tableColumn.setText(bundle.getString("unmatchedMoviesTable.columnName"));
        tableColumn.setWidth(370);
        Button btnStartMatching = new Button(composite, SWT.NONE);
        btnStartMatching.setText(bundle.getString("unmatchedMoviesTable.start"));
        btnStartMatching.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        btnStartMatching.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                startProcess();
                readData();
            }
        });

        Composite compositeFooter = new Composite(composite, SWT.NONE);
        compositeFooter.setLayout(new GridLayout(1, false));
        compositeFooter.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        Button btnClose = new Button(compositeFooter, SWT.NONE);
        btnClose.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
        btnClose.setText(bundle.getString("global.close"));
        btnClose.addSelectionListener(new HandledSelectionAdapter(shell, bundle) {
            @Override
            public void handledSelected(SelectionEvent event) throws ApplicationException {
                shell.close();
            }
        });
    }

    private void startProcess() {
        //TODO: implement matching process
    }

}
