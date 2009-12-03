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
package tinyos.yeti.make.dialog.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeInclude.Include;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.widgets.helper.TableContentProvider;

/**
 * The include page allows to specify a set of {@link MakeInclude}s. This class
 * can be used outside the core plugin.
 * @author Benjamin Sigg
 */
public class IncludePage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
	private DirectoryDialog directoryDialog;
	private FileDialog fileDialog;

	private boolean showIncludeButtons;
	private boolean showNoSTDinc;
	private Button nostdinc;
	private Button includeDefaults;
	private Button includeBuild;

	private Composite baseComposite;

	private TableViewer includeList;
	private TableContentProvider<TableEntry> contentProvider;

	private Button buttonAddFile;
	private Button buttonAddDirectory;
	private Button buttonEdit;
	private Button buttonDelete;
	private Button buttonUp;
	private Button buttonDown;
	
	private IMakeTargetInformation information;

	private CustomizationControls customization;

	public IncludePage( boolean showIncludeButtons, boolean showNoSTDinc, boolean showCustomization ){
		super( "Includes" );

		this.showIncludeButtons = showIncludeButtons;
		this.showNoSTDinc = showNoSTDinc;
		setDefaultMessage( "An additional set of directories to search for files." );

		if( showCustomization ){
			customization = new CustomizationControls();
			customization.setPage( this );
		}

		setImage( NesCIcons.icons().get( NesCIcons.ICON_INCLUDES_LIST ) );
	}

	public void setCustomEnabled( boolean enabled ){
		nostdinc.setEnabled( enabled );

		if( includeBuild != null )
			includeBuild.setEnabled( enabled );
		if( includeDefaults != null )
			includeDefaults.setEnabled( enabled );

		includeList.getControl().setEnabled( enabled );

		if( buttonAddFile != null )
			buttonAddFile.setEnabled( enabled );

		buttonAddDirectory.setEnabled( enabled );
		buttonEdit.setEnabled( enabled );
		buttonDelete.setEnabled( enabled );
		buttonUp.setEnabled( enabled );
		buttonDown.setEnabled( enabled );
		
		contentChanged();
	}

	public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
		this.information = information;

		contentProvider.clear();

		MakeInclude[] includes = maketarget.getCustomIncludes();
		if( includes != null ){
			show( includes );
		}
		else{
			baseComposite.layout();
		}
		
		if( showIncludeButtons ){
			includeDefaults.setSelection( maketarget.isUsingPlatformIncludes() );
			includeBuild.setSelection( maketarget.isUsingLastBuildIncludes() );
		}

		if( showNoSTDinc ){
			nostdinc.setSelection( maketarget.isCustomNostdinc() );
		}

		if( customization != null ){
			boolean local = maketarget.isUseLocalProperty( MakeTargetPropertyKey.INCLUDES );
			boolean defaults = maketarget.isUseDefaultProperty( MakeTargetPropertyKey.INCLUDES );

			customization.setSelection( local, defaults );
		}
	}
	
	private void show( MakeInclude[] includes ){
		contentProvider.clear();

		if( includes != null ){
			for( MakeInclude include : includes ){
				contentProvider.add( new TableEntry( include ) );
			}
		}

		baseComposite.layout();
	}

	public void show( MakeInclude[] includes, IMakeTargetInformation information ){
		this.information = information;
		show( includes );
	}

	public void store( MakeTargetSkeleton maketarget ){
		List<MakeInclude> includes = listIncludes();

		maketarget.setCustomIncludes( includes.toArray( new MakeInclude[ includes.size() ] ) );

		if( showNoSTDinc ){
			maketarget.setCustomNostdinc( nostdinc.getSelection() );
		}

		if( customization != null ){
			Selection selection = customization.getSelection();
			maketarget.setUseLocalProperty( MakeTargetPropertyKey.INCLUDES, selection.isLocal() );
			maketarget.setUseDefaultProperty( MakeTargetPropertyKey.INCLUDES, selection.isDefaults() );

			boolean local = selection.isLocal();

			maketarget.setUseLocalProperty( MakeTargetPropertyKey.NO_STD_INCLUDE, local );
			maketarget.setUseDefaultProperty( MakeTargetPropertyKey.NO_STD_INCLUDE, !local );

			maketarget.setUseLocalProperty( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS, local );
			maketarget.setUseDefaultProperty( MakeTargetPropertyKey.INCLUDE_ENVIRONMENT_DEFAULT_PATHS, !local );

			maketarget.setUseLocalProperty( MakeTargetPropertyKey.INCLUDE_LAST_BUILD, local );
			maketarget.setUseDefaultProperty( MakeTargetPropertyKey.INCLUDE_LAST_BUILD, !local );
		}

		if( showIncludeButtons ){
			maketarget.setUsingPlatformIncludes( includeDefaults.getSelection() );
			maketarget.setUsingLastBuildIncludes( includeBuild.getSelection() );
		}

	}

	public List<MakeInclude> listIncludes(){
		List<MakeInclude> result = new ArrayList<MakeInclude>();
		store( result );
		return result;   
	}

	@Override
	public void dispose(){
		super.dispose();
		directoryDialog = null;
		fileDialog = null;
	}


	protected DirectoryDialog getDirectoryDialog(){
		if( directoryDialog == null )
			directoryDialog = new DirectoryDialog( getControl().getShell() );

		return directoryDialog;
	}

	protected FileDialog getFileDialog(){
		if( fileDialog == null ){
			fileDialog = new FileDialog( getControl().getShell(), SWT.OPEN );
		}

		return fileDialog;
	}

	public void createControl( Composite parent ){
		Composite base = new Composite( parent, SWT.NONE );
		base.setLayout( new GridLayout( 1, false ) );

		if( customization != null ){
			customization.createControl( base, true );
			customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		}

		if( showNoSTDinc ){
			nostdinc = new Button( base, SWT.CHECK );
			nostdinc.setText( "No standard includes" );
			nostdinc.setToolTipText( "'-nostdinc': if checked then the default paths of 'ncc' are ignored" );
			nostdinc.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			nostdinc.addSelectionListener( new SelectionAdapter(){
				@Override
				public void widgetSelected( SelectionEvent e ){
					contentChanged();
				}
			});
		}

		if( showIncludeButtons ){
			includeDefaults = new Button( base, SWT.CHECK );
			includeDefaults.setText( "Search platform dependend paths specified by user." );
			includeDefaults.setToolTipText( "Whether to search the paths that depend on the current platform and were manually specified by a user.\n" +
			"Note: if this option is not checked then the automatically detected paths are still searched." );
			includeDefaults.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			includeDefaults.addSelectionListener( new SelectionAdapter(){
				@Override
				public void widgetSelected( SelectionEvent e ){
					contentChanged();
				}
			});

			includeBuild = new Button( base, SWT.CHECK );
			includeBuild.setText( "Search output of platform specific build, if present" );
			includeBuild.setToolTipText( "If checked, then the output of 'ncc' is searched for files as well" );
			includeBuild.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			includeBuild.addSelectionListener( new SelectionAdapter(){
				@Override
				public void widgetSelected( SelectionEvent e ){
					contentChanged();
				}
			});
		}

		/* Label info1 = new Label( base, SWT.NONE );
        info1.setText( "Checked directories are included recursively." );
        info1.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) ); */

		createInfoControl( base );
		
		Control table = createTableControl( base );
		table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		setControl( base );
	}
	
	private void createInfoControl( Composite parent ){
		Label infoPath = new Label( parent, SWT.NONE );
		Label infoNcc = new Label( parent, SWT.NONE );
		Label infoGlobal = new Label( parent, SWT.NONE );
		Label infoInclude = new Label( parent, SWT.NONE );
		Label infoSearch = new Label( parent, SWT.NONE );
		
		infoPath.setText( "Path: path to a file or directory." );
		infoNcc.setText( "Ncc: whether ncc should include the directory in the build path." );
		infoGlobal.setText( "Global: if yes, the file/directory is included in all source files (not when building the application)." );
		infoInclude.setText( "Include: if never, impossible. If source, allows #include \"file.h\". If system, allows #include \"file.h\" and #include <file.h>." );
		infoSearch.setText( "Search: how to traverse the directory when searching an included file." );
		
		infoPath.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		infoNcc.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		infoGlobal.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		infoInclude.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		infoSearch.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
	}

	private Control createTableControl( Composite parent ){
		baseComposite = new Composite( parent, SWT.NONE );
		baseComposite.setLayout( new GridLayout( 1, false ) );

		Composite contentComposite = new Composite( baseComposite, SWT.NONE );
		contentComposite.setLayout( new GridLayout( 2, false ) );
		contentComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

		// left list
		// includeList = new Table( contentComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK );
		// includeList.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
		createTable( contentComposite );

		// right buttons
		Composite buttons = new Composite( contentComposite, SWT.NONE );
		buttons.setLayoutData( new GridData( GridData.CENTER, GridData.CENTER, false, false ) );
		buttons.setLayout( new GridLayout( 1, false ) );

		buttonAddDirectory = new Button( buttons, SWT.PUSH );
		buttonAddDirectory.setText( "Add Directory" );
		buttonAddDirectory.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonAddDirectory.addSelectionListener( new AddDirectoryAction() );

		buttonAddFile = new Button( buttons, SWT.PUSH );
		buttonAddFile.setText( "Add File" );
		buttonAddFile.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonAddFile.addSelectionListener( new AddFileAction() );

		buttonEdit = new Button( buttons, SWT.PUSH );
		buttonEdit.setText( "Edit" );
		buttonEdit.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonEdit.addSelectionListener( new EditAction() );

		buttonDelete = new Button( buttons, SWT.PUSH );
		buttonDelete.setText( "Delete" );
		buttonDelete.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonDelete.addSelectionListener( new DeleteAction() );

		buttonUp = new Button( buttons, SWT.PUSH );
		buttonUp.setText( "Up" );
		buttonUp.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonUp.addSelectionListener( new UpAction() );

		buttonDown = new Button( buttons, SWT.PUSH );
		buttonDown.setText( "Down" );
		buttonDown.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
		buttonDown.addSelectionListener( new DownAction() );

		baseComposite.layout();
		return baseComposite;
	}

	private void createTable( Composite contentComposite ){
		includeList = new TableViewer( contentComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		includeList.getControl().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
		contentProvider = new TableContentProvider<TableEntry>( includeList );
		includeList.setContentProvider( contentProvider );

		includeList.getTable().setHeaderVisible( true );

		createColumnPath();
		createColumnRecursive();
		createColumnInclude();
		createColumnGlobal();
		createColumnNCC();
	}
	
	private TableViewerColumn createColumnPath(){
		TableViewerColumn column = new TableViewerColumn( includeList, SWT.LEFT, 0 );
		column.getColumn().setMoveable( false );
		column.getColumn().setResizable( true );
		column.getColumn().setText( "Path" );
		column.getColumn().setWidth( 200 );
		column.setEditingSupport( new EditingSupport( includeList ){
			@Override
			protected boolean canEdit( Object element ){
				return false;
			}

			@Override
			protected CellEditor getCellEditor( Object element ){
				return null;
			}

			@Override
			protected Object getValue( Object element ){
				return ((TableEntry)element).path;
			}

			@Override
			protected void setValue( Object element, Object value ){
				((TableEntry)element).path = String.valueOf( value );
				contentProvider.refresh( (TableEntry)element );
			}
		});
		column.setLabelProvider( new CellLabelProvider(){
			@Override
			public void update( ViewerCell cell ){
				TableEntry entry = (TableEntry)cell.getElement();
				cell.setText( entry.path );
			}                
		});
		return column;
	}
	
	private TableViewerColumn createColumnRecursive(){
		TableViewerColumn column = new TableViewerColumn( includeList, SWT.CENTER, 1 );
		column.getColumn().setMoveable( false );
		column.getColumn().setResizable( true );
		column.getColumn().setWidth( 100 );
		column.getColumn().setText( "Search" );
		column.setEditingSupport( new EditingSupport( includeList ){
			private CheckboxCellEditor editor = new CheckboxCellEditor( includeList.getTable() );

			@Override
			protected boolean canEdit( Object element ){
				return !((TableEntry)element).isFile();
			}

			@Override
			protected CellEditor getCellEditor( Object element ){
				return editor;
			}

			@Override
			protected Object getValue( Object element ){
				return ((TableEntry)element).recursive;
			}

			@Override
			protected void setValue( Object element, Object value ){
				((TableEntry)element).recursive = (Boolean)value;
				contentProvider.refresh( (TableEntry)element );
			}

		});
		column.setLabelProvider( new CellLabelProvider(){
			@Override
			public void update( ViewerCell cell ){
				TableEntry entry = (TableEntry)cell.getElement();
				if( entry.isFile()){
					cell.setText( "n/a" );
				}
				else{
					cell.setText( entry.recursive ? "recursive" : "flat" );
				}
			}
		});
		return column;
	}
	
	private TableViewerColumn createColumnNCC(){
		TableViewerColumn column = new TableViewerColumn( includeList, SWT.CENTER, 1 );
		column.getColumn().setMoveable( false );
		column.getColumn().setResizable( true );
		column.getColumn().setWidth( 100 );
		column.getColumn().setText( "ncc" );
		column.setEditingSupport( new EditingSupport( includeList ){
			private CheckboxCellEditor editor = new CheckboxCellEditor( includeList.getTable() );

			@Override
			protected boolean canEdit( Object element ){
				return !((TableEntry)element).isFile();
			}

			@Override
			protected CellEditor getCellEditor( Object element ){
				return editor;
			}

			@Override
			protected Object getValue( Object element ){
				return ((TableEntry)element).ncc;
			}

			@Override
			protected void setValue( Object element, Object value ){
				((TableEntry)element).ncc = (Boolean)value;
				contentProvider.refresh( (TableEntry)element );
			}

		});
		column.setLabelProvider( new CellLabelProvider(){
			@Override
			public void update( ViewerCell cell ){
				TableEntry entry = (TableEntry)cell.getElement();
				if( entry.isFile()){
					cell.setText( "n/a" );
				}
				else{
					cell.setText( entry.ncc ? "provide" : "ignore" );
				}
			}
		});
		return column;
	}
	
	private TableViewerColumn createColumnGlobal(){
		TableViewerColumn column = new TableViewerColumn( includeList, SWT.CENTER, 1 );
		column.getColumn().setMoveable( false );
		column.getColumn().setResizable( true );
		column.getColumn().setWidth( 100 );
		column.getColumn().setText( "Global" );
		column.setEditingSupport( new EditingSupport( includeList ){
			private CheckboxCellEditor editor = new CheckboxCellEditor( includeList.getTable() );

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
				return ((TableEntry)element).global;
			}

			@Override
			protected void setValue( Object element, Object value ){
				((TableEntry)element).global = (Boolean)value;
				contentProvider.refresh( (TableEntry)element );
			}

		});
		column.setLabelProvider( new CellLabelProvider(){
			@Override
			public void update( ViewerCell cell ){
				TableEntry entry = (TableEntry)cell.getElement();
				cell.setText( entry.global ? "yes" : "no" );
			}
		});
		return column;
	}
	
	private TableViewerColumn createColumnInclude(){
		TableViewerColumn column = new TableViewerColumn( includeList, SWT.CENTER, 1 );
		column.getColumn().setMoveable( false );
		column.getColumn().setResizable( true );
		column.getColumn().setWidth( 100 );
		column.getColumn().setText( "Include" );
		column.setEditingSupport( new EditingSupport( includeList ){
			private CellEditor editor = new CellEditor(){
				private int value = 0;
				
				@Override
				protected Control createControl( Composite parent ){
					return null;
				}

				@Override
				protected Object doGetValue(){
					return Integer.valueOf( value );
				}

				@Override
				protected void doSetFocus(){
					// ignore	
				}

				@Override
				protected void doSetValue( Object value ){
					this.value = (Integer)value;
				}
				
				@Override
				public void activate(){
					value = (value+1) % 3;
					fireApplyEditorValue();
				}
			};

			@Override
			protected boolean canEdit( Object element ){
				return !((TableEntry)element).isFile();
			}

			@Override
			protected CellEditor getCellEditor( Object element ){
				return editor;
			}

			@Override
			protected Object getValue( Object element ){
				switch( ((TableEntry)element).include ){
					case NONE: return 0;
					case SOURCE: return 1;
					case SYSTEM: return 2;
					default: return 0;
				}
			}

			@Override
			protected void setValue( Object element, Object value ){
				Include include = Include.NONE;
				switch( (Integer)value ){
					case 0: include = Include.NONE; break;
					case 1: include = Include.SOURCE; break;
					case 2: include = Include.SYSTEM; break;
					default: throw new IllegalArgumentException();
				}
				
				((TableEntry)element).include = include;
				contentProvider.refresh( (TableEntry)element );
			}

		});
		column.setLabelProvider( new CellLabelProvider(){
			@Override
			public void update( ViewerCell cell ){
				TableEntry entry = (TableEntry)cell.getElement();
				if( entry.isFile()){
					cell.setText( "n/a" );
				}
				else{
					switch( entry.include ){
						case NONE: cell.setText( "never" ); break;
						case SOURCE: cell.setText( "source" ); break;
						case SYSTEM: cell.setText( "system" ); break;
					}
				}
			}
		});
		return column;
	}

	public void store( List<MakeInclude> list ){
		for( int i = 0, n = contentProvider.getSize(); i<n; i++ ){
			TableEntry entry = contentProvider.getEntry( i );
			list.add( entry.toMakeInclude() );
		}
	}

	private void performeAddDirectory(){
		DirectoryDialog dialog = getDirectoryDialog();

		TableEntry selection = getSelectedEntry();
		int count = contentProvider.getSize();

		if( selection == null && count > 0 )
			selection = contentProvider.getEntry( count-1 );

		if( selection == null )
			dialog.setFilterPath( null );
		else
			dialog.setFilterPath( selection.path );

		while( true ){
			String directory = dialog.open();
			if( directory != null ){
				File directoryFile = new File( directory );
				directory = information.getEnvironment().systemToModel( directoryFile );
				if( directory == null ){
					MessageDialog errorDialog = new MessageDialog( getControl().getShell(), "Add Directory", 
							null, "The selected directory cannot be accessed by the tinyos tool chain.",
							MessageDialog.ERROR, new String[]{ "Browse", "Cancel" }, 0 );
					int result = errorDialog.open();
					if( result != 0 )
						break;
				}
				else{
					contentProvider.add( new TableEntry( directory ) );
					contentChanged();
					break;
				}
			}
			else{
				break;
			}
		}
	}

	private void performeAddFile(){
		FileDialog dialog = getFileDialog();
		TableEntry selection = getSelectedEntry();
		int count = contentProvider.getSize();

		if( selection == null && count > 0 )
			selection = contentProvider.getEntry( count-1 );

		if( selection == null )
			dialog.setFilterPath( null );
		else
			dialog.setFilterPath( selection.path );

		while( true ){
			String file = dialog.open();
			if( file == null )
				break;

			file = information.getEnvironment().systemToModel( new File( file ) );
			if( file == null ){
				MessageDialog errorDialog = new MessageDialog( getControl().getShell(), "Add File", 
						null, "The selected file cannot be accessed by the tinyos tool chain.",
						MessageDialog.ERROR, new String[]{ "Browse", "Cancel" }, 0 );
				int result = errorDialog.open();
				if( result != 0 )
					break;
			}
			else{
				contentProvider.add( new TableEntry( file ) );
				contentChanged();
				break;
			}
		}
	}

	private void performeEdit(){
		TableEntry selected = getSelectedEntry();
		if( selected != null ){
			String path = selected.path;
			File file = information.getEnvironment().modelToSystem( path );

			if( file.isFile() ){
				FileDialog dialog = getFileDialog();
				dialog.setFilterPath( file.getParent() );
				dialog.setFileName( file.getName() );

				while( true ){
					String selection = dialog.open();
					if( selection == null )
						break;

					selection = information.getEnvironment().systemToModel( new File( selection ) );
					if( selection == null ){
						MessageDialog errorDialog = new MessageDialog( getControl().getShell(), "Edit File", 
								null, "The selected file cannot be accessed by the tinyos tool chain.",
								MessageDialog.ERROR, new String[]{ "Browse", "Cancel" }, 0 );
						int result = errorDialog.open();
						if( result != 0 )
							break;
					}
					else{
						selected.path = selection;
						contentProvider.refresh( selected );
						contentChanged();
						break;
					}
				}
			}
			else{
				DirectoryDialog dialog = getDirectoryDialog();
				dialog.setFilterPath( file.getAbsolutePath() );
				while( true ){
					String directory = dialog.open();
					if( directory == null )
						break;

					directory = information.getEnvironment().systemToModel( new File( directory ) );
					if( directory == null ){
						MessageDialog errorDialog = new MessageDialog( getControl().getShell(), "Add Directory", 
								null, "The selected directory cannot be accessed by the tinyos tool chain.",
								MessageDialog.ERROR, new String[]{ "Browse", "Cancel" }, 0 );
						int result = errorDialog.open();
						if( result != 0 )
							break;
					}
					else{
						selected.path = directory;
						contentProvider.refresh( selected );
						contentChanged();
						break;
					}
				}
			}
		}
	}
	private void performeDelete(){
		StructuredSelection selection = (StructuredSelection)includeList.getSelection();
		if( selection != null ){
			Object[] array = selection.toArray();
			if( array != null ){
				for( Object entry : array ){
					contentProvider.remove( (TableEntry)entry );
				}
				contentChanged();
			}
		}
	}

	private void performeUp(){
		int selected = getSelectedIndex();
		int next = selected - 1;
		switchItems( selected, next );
	}

	private void performeDown(){
		int selected = getSelectedIndex();
		int next = selected + 1;
		switchItems( selected, next );
	}

	private void switchItems( int selected, int next ){
		if( selected >= 0 && next >= 0 && next < contentProvider.getSize() ){
			TableEntry alpha = contentProvider.getEntry( selected );
			TableEntry beta = contentProvider.getEntry( next );

			TableEntry temp = new TableEntry();
			temp.copy( alpha );
			alpha.copy( beta );
			beta.copy( temp );
			
			contentProvider.refresh( alpha );
			contentProvider.refresh( beta );

			includeList.setSelection( new StructuredSelection( beta ) );
			contentChanged();
		}            
	}

	private int getSelectedIndex(){
		return includeList.getTable().getSelectionIndex();
	}

	private TableEntry getSelectedEntry(){
		StructuredSelection selection = (StructuredSelection)includeList.getSelection();
		if( selection == null )
			return null;

		return (TableEntry)selection.getFirstElement();
	}

	private class TableEntry{
		public String path;
		public boolean recursive;
		public boolean ncc;
		public boolean global;
		public Include include;
		private Boolean file;

		public TableEntry(){
			// nothing
		}
		
		public TableEntry( MakeInclude include ){
			path = include.getPath();
			recursive = include.isRecursive();
			ncc = include.isNcc();
			global = include.isGlobal();
			this.include = include.getInclude();
		}
		
		public TableEntry( String path ){
			this.path = path;
			recursive = false;
			ncc = false;
			global = false;
			include = Include.NONE;
		}
		
		public boolean isFile(){
			if( file == null ){
				File check = new File( path );
				if( check.exists() ){
					file = check.isFile();
				}
				else{
					file = false;
				}
			}
			return file;
		}
		
		public void copy( TableEntry entry ){
			this.path = entry.path;
			this.recursive = entry.recursive;
			this.ncc = entry.ncc;
			this.global = entry.global;
			this.include = entry.include;
		}
		
		public MakeInclude toMakeInclude(){
			if( isFile() ){
				return new MakeInclude( path, Include.NONE, false, false, true );
			}
			else{
				return new MakeInclude( path, include, recursive, ncc, global );
			}
		}
	}

	private class AddDirectoryAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeAddDirectory();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeAddDirectory();
		}
	}

	private class AddFileAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeAddFile();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeAddFile();
		}
	}

	private class EditAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeEdit();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeEdit();
		}
	}

	private class DeleteAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeDelete();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeDelete();
		}
	}

	private class UpAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeUp();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeUp();
		}
	}

	private class DownAction implements SelectionListener{
		public void widgetSelected( SelectionEvent e ){
			performeDown();
		}
		public void widgetDefaultSelected( SelectionEvent e ){
			performeDown();
		}
	}
}
