/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.environment.basic.preferences.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A table that shows the different architectures (including the
 * default architecture), a checkbox whether to use the default or the
 * user set path, and the path itself.
 * @author Benjamin Sigg
 */
public class ArchitecturePathTable {
    private TableViewer table;
    private List<Row> rows = new ArrayList<Row>();

    public Control getControl(){
        return table.getControl();
    }

    public void createControl( Composite parent ){
        table = new TableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE );
        table.getTable().setHeaderVisible( true );
        table.getTable().setLinesVisible( true );

        createArchitectureColumn( table );
        createOverrideColumn( table );
        createPathColumn( table );
    }

    /**
     * Sets the values of <code>architecture</code>, adds a new row if
     * <code>architecture</code> does not yet exist.
     * @param architecture the name of the architecture
     * @param override whether the user or the default path is to be used
     * @param defaultPath the default path
     * @param userPath the path set by the user
     */
    public void set( String architecture, boolean override, String defaultPath, String userPath ){
        for( Row row : rows ){
            if( row.architecture.equals( architecture )){
                row.override = override;
                row.defaultPath = defaultPath;
                row.userPath = userPath;
                table.update( row, null );
                return;
            }
        }

        add( architecture, override, defaultPath, userPath) ;
    }

    /**
     * Adds a new row at the end of this table
     * @param architecture the name of the architecture
     * @param override whether the user or the default path is to be used
     * @param defaultPath the default path
     * @param userPath the path set by the user
     */
    public void add( String architecture, boolean override, String defaultPath, String userPath ){
        Row row = new Row( architecture, override, defaultPath, userPath );
        rows.add( row );
        table.add( row );
    }

    /**
     * Updates the default path of <code>architecture</code>.
     * @param architecture the name of an architecture
     * @param defaultPath the new default path
     */
    public void update( String architecture, String defaultPath ){
        for( Row row : rows ){
            if( row.architecture.equals( architecture )){
                row.defaultPath = defaultPath;
                table.update( row, null );
                return;
            }
        }
    }

    /**
     * Takes the current selection and sets the user path equal
     * to the default path.
     */
    @SuppressWarnings("unchecked")
    public void copyDefaultPaths(){
        IStructuredSelection selection = (IStructuredSelection)table.getSelection();
        if( selection != null ){
            Iterator<Object> iterator = selection.iterator();
            while( iterator.hasNext() ){
                Row row = (Row)iterator.next();
                row.userPath = row.defaultPath;
                table.update( row, null );
            }
        }
    }

    public boolean isShowing( String architecture ){
        for( Row row : rows ){
            if( row.architecture.equals( architecture ))
                return true;
        }
        return false;
    }

    public int getRowCount(){
        return rows.size();
    }

    public String getArchitecture( int row ){
        return rows.get( row ).architecture;
    }

    public boolean isOverridden( int row ){
        return rows.get( row ).override;
    }

    public String getUserPath( int row ){
        return rows.get( row ).userPath;
    }

    public String getDefaultPath( int row ){
        return rows.get( row ).defaultPath;
    }

    private void createArchitectureColumn( TableViewer table ){
        TableViewerColumn architectureColumn = new TableViewerColumn( table, SWT.LEFT );
        architectureColumn.getColumn().setWidth( 100 );
        architectureColumn.getColumn().setText( "Architecture" );
        architectureColumn.setLabelProvider( new CellLabelProvider(){
            @Override
            public void update( ViewerCell cell ){
                Row row = (Row)cell.getElement();
                cell.setText( row.architecture );
            }			
        });
    }

    private void createOverrideColumn( final TableViewer table ){
        TableViewerColumn overrideColumn = new TableViewerColumn( table, SWT.CENTER );
        overrideColumn.getColumn().setText( "Uses" );
        overrideColumn.getColumn().setWidth( 100 );
        overrideColumn.setLabelProvider( new CellLabelProvider(){
            @Override
            public void update(ViewerCell cell) {
                Row row = (Row)cell.getElement();
                cell.setText( row.override ? "user defined" : "default" );
            }
        });
        overrideColumn.setEditingSupport( new EditingSupport( table ){
            private CheckboxCellEditor editor = new CheckboxCellEditor();

            @Override
            protected boolean canEdit( Object element ){
                return true;
            }

            @Override
            protected CellEditor getCellEditor( Object element ){
                return editor;
            }

            @Override
            protected Object getValue( Object element ){
                return Boolean.valueOf(((Row)element).override);
            }

            @Override
            protected void setValue( Object element, Object value ){
                Row row = (Row)element;
                row.override = Boolean.TRUE.equals( value );
                table.update( row, null );
            }
        });
    }

    private void createPathColumn( final TableViewer table ){
        TableViewerColumn pathColumn = new TableViewerColumn( table, SWT.LEFT );
        pathColumn.getColumn().setText( "Path" );
        pathColumn.getColumn().setWidth( 300 );
        pathColumn.setLabelProvider( new CellLabelProvider(){
            @Override
            public void update(ViewerCell cell) {
                Row row = (Row)cell.getElement();
                cell.setText( row.override ? row.userPath : row.defaultPath );
            }
        });
        pathColumn.setEditingSupport( new EditingSupport( table ){
            private TextCellEditor editor = new TextCellEditor( table.getTable() );

            @Override
            protected boolean canEdit( Object element ){
                return ((Row)element).override;
            }

            @Override
            protected CellEditor getCellEditor( Object element ){
                return editor;
            }

            @Override
            protected Object getValue( Object element ){
                return ((Row)element).userPath;
            }

            @Override
            protected void setValue( Object element, Object value ){
                ((Row)element).userPath = String.valueOf( value );
                table.update( element, null );
            }
        });
    }

    private static class Row{
        public String architecture;
        public boolean override;
        public String defaultPath;
        public String userPath;

        public Row( String architecture, boolean override, String defaultPath, String userPath ){
            this.architecture = architecture;
            this.override = override;
            this.defaultPath = defaultPath;
            this.userPath = userPath;
        }


    }
}
