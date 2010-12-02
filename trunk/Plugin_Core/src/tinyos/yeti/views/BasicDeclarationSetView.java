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
package tinyos.yeti.views;

import java.io.File;
import java.text.Collator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.model.BasicDeclarationSet;

/**
 * This view shows the contents of the {@link BasicDeclarationSet} of the
 * currently selected project.
 * @author Benjamin Sigg
 */
public class BasicDeclarationSetView extends ViewPart {
	private TableViewer basicTypes;
	private TableViewer basicDeclarations;
	private TableViewer globalInclusionFiles;
	private Set<File> globalInclusionFilesBase;
	private TableViewer globalDeclarations;
	private TableViewer globalMacros;
	private TableViewer allDeclarations;
	
	private Label currentProject;
	private Button refreshButton;
	
	@Override
	public void createPartControl( Composite parent ){
		Composite panel = new Composite( parent, SWT.NONE );
		panel.setLayout( new GridLayout( 1, false ) );
	
		currentProject = new Label( panel, SWT.NONE );
		currentProject.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		TabFolder notebook = new TabFolder( panel, SWT.FLAT | SWT.BORDER );
		notebook.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		basicTypes = create( notebook, "Basic Types", "These types are defined within the parser" );
		basicDeclarations = create( notebook, "Basic Declarations", "These declarations are defined within the parser" );
		globalInclusionFiles = create( notebook, "Global Files", "These files are, directly or indirectly, included in all the files of the project" );
		globalDeclarations = create( notebook, "Global Declarations", "These declarations are present in the global files and in the make-options (project properties)" );
		globalMacros = create( notebook, "Global Macros", "These macros are present in the global files and in the make-options (project properties)" );
		allDeclarations = create( notebook, "All declarations", "All declarations that are added to new parsers" );
		
		setupDeclarationTable( basicTypes );
		setupDeclarationTable( basicDeclarations );
		setupParseFileTable( globalInclusionFiles );
		setupDeclarationTable( globalDeclarations );
		setupMacroTable( globalMacros );
		setupDeclarationTable( allDeclarations );
		
		refreshButton = new Button( panel, SWT.PUSH );
		refreshButton.setText( "Refresh" );
		refreshButton.setLayoutData( new GridData( SWT.RIGHT, SWT.BOTTOM, false, false ) );
		
		refreshButton.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				refresh();
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				refresh();
			}
		});
	}
	
	private void setupTable( TableViewer table ){
		table.getTable().setHeaderVisible( true );
		table.setContentProvider( new IStructuredContentProvider(){
			public Object[] getElements( Object inputElement ){
				return (Object[])inputElement;
			}

			public void dispose(){
				// ignore
			}

			public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
				if( oldInput != null )
					((TableViewer)viewer).remove( (Object[])oldInput );
				if( newInput != null )
					((TableViewer)viewer).add( (Object[])newInput );
			}
		});
	}
	
	private void setupParseFileTable( TableViewer table ){
		setupTable( table );
		table.setComparator( new ViewerComparator(){
			private Collator collator = Collator.getInstance();
			
			@Override
			public int compare( Viewer viewer, Object e1, Object e2 ){
				return collator.compare( ((IParseFile)e1).getName(), ((IParseFile)e2).getName() );
			}
		});
		
		String[] titles = { "Name", "is in project", "directly included", "Full path" };
		
		for( String title : titles ){
			TableViewerColumn column = new TableViewerColumn( table, SWT.NONE );
			column.getColumn().setText( title );
			column.getColumn().setResizable( true );
			column.getColumn().setWidth( 300 );
		}
		
		ITableLabelProvider labelProvider = new ITableLabelProvider(){
			public Image getColumnImage( Object element, int columnIndex ){
				return null;
			}

			public String getColumnText( Object element, int columnIndex ){
				IParseFile file = (IParseFile)element;
				switch( columnIndex ){
					case 0: return file.getName();
					case 1: return String.valueOf( file.isProjectFile() );
					case 2: return String.valueOf( globalInclusionFilesBase.contains( file.toFile() ) );
					case 3: return file.getPath();
				}
				return null;
			}

			public void addListener( ILabelProviderListener listener ){
				// ignore	
			}

			public void dispose(){
				// ignore
			}

			public boolean isLabelProperty( Object element, String property ){
				return true;
			}

			public void removeListener( ILabelProviderListener listener ){
				// ignore
			}
		};
		
		table.setLabelProvider( labelProvider );
	}
	
	private void setupMacroTable( TableViewer table ){
		setupTable( table );
		table.setComparator( new ViewerComparator(){
			private Collator collator = Collator.getInstance();
			
			@Override
			public int compare( Viewer viewer, Object e1, Object e2 ){
				return collator.compare( ((IMacro)e1).getName(), ((IMacro)e2).getName() );
			}
		});
		
		String[] titles = { "Name", "is function" };
		
		for( String title : titles ){
			TableViewerColumn column = new TableViewerColumn( table, SWT.NONE );
			column.getColumn().setText( title );
			column.getColumn().setResizable( true );
			column.getColumn().setWidth( 300 );
		}
		
		ITableLabelProvider labelProvider = new ITableLabelProvider(){
			public Image getColumnImage( Object element, int columnIndex ){
				return null;
			}

			public String getColumnText( Object element, int columnIndex ){
				IMacro macro = (IMacro)element;
				switch( columnIndex ){
					case 0: return macro.getName();
					case 1: return String.valueOf( macro.isFunctionMacro() );
				}
				return null;
			}

			public void addListener( ILabelProviderListener listener ){
				// ignore	
			}

			public void dispose(){
				// ignore
			}

			public boolean isLabelProperty( Object element, String property ){
				return true;
			}

			public void removeListener( ILabelProviderListener listener ){
				// ignore
			}
		};
		
		table.setLabelProvider( labelProvider );
	}
	
	private void setupDeclarationTable( TableViewer table ){
		setupTable( table );
		table.setComparator( new ViewerComparator(){
			private Collator collator = Collator.getInstance();
			
			@Override
			public int compare( Viewer viewer, Object e1, Object e2 ){
				return collator.compare( ((IDeclaration)e1).getName(), ((IDeclaration)e2).getName() );
			}
		});
		
		String[] titles = { "Label", "Name", "Kind", "File" };
		
		for( String title : titles ){
			TableViewerColumn column = new TableViewerColumn( table, SWT.NONE );
			column.getColumn().setText( title );
			column.getColumn().setResizable( true );
			column.getColumn().setWidth( 300 );
		}
		
		ITableLabelProvider labelProvider = new ITableLabelProvider(){
			public Image getColumnImage( Object element, int columnIndex ){
				return null;
			}

			public String getColumnText( Object element, int columnIndex ){
				IDeclaration declaration = (IDeclaration)element;
				switch( columnIndex ){
					case 0: return declaration.getLabel();
					case 1: return declaration.getName();
					case 2: return String.valueOf( declaration.getKind() );
					case 3: 
						IParseFile file = declaration.getParseFile();
						if( file == null )
							return "-";
						return file.getName();
				}
				return null;
			}

			public void addListener( ILabelProviderListener listener ){
				// ignore	
			}

			public void dispose(){
				// ignore
			}

			public boolean isLabelProperty( Object element, String property ){
				return true;
			}

			public void removeListener( ILabelProviderListener listener ){
				// ignore
			}
		};
		
		table.setLabelProvider( labelProvider );
	}
	
	private TableViewer create( TabFolder notebook, String label, String description ){
		TabItem item = new TabItem( notebook, SWT.NONE );
		item.setText( label );
		
		Composite panel = new Composite( notebook, SWT.NONE );
		panel.setLayout( new GridLayout( 1, false ) );
		item.setControl( panel );
		
		Text info = new Text( panel, SWT.READ_ONLY | SWT.WRAP );
		info.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		info.setText( description );
		
		TableViewer table = new TableViewer( panel, SWT.V_SCROLL | SWT.H_SCROLL );
		table.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		
		return table;
	}

	@Override
	public void setFocus(){
		refreshButton.setFocus();
	}

	public void refresh(){
		RefreshJob job = new RefreshJob();
		job.schedule();
	}
	
	private class RefreshJob extends Job{
		public RefreshJob(){
			super( "refresh" );
		}
		
		@Override
		protected IStatus run( IProgressMonitor monitor ){
			ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS();
			BasicDeclarationSet set = project == null ? null : project.getModel().getBasicDeclarations();
			monitor.beginTask( "Refresh declarations", 10 );
			
			RefreshUIJob job = new RefreshUIJob();
			
			if( set == null ){
				job.basicTypesInput = new IDeclaration[]{};
				job.globalInclusionFilesBase = new File[]{};
				job.basicDeclarationsInput = new IDeclaration[]{};
				job.globalInclusionFilesInput = new IParseFile[]{};
				job.globalDeclarationsInput = new IDeclaration[]{};
				job.globalMacrosInput = new IMacro[]{};
				job.allDeclarationsInput = new IDeclaration[]{};
				job.info = "-";
			}
			else{
				job.globalInclusionFilesInput = set.listAllGlobalInclusionFiles( new SubProgressMonitor( monitor, 10 ) );
				job.globalInclusionFilesBase = set.listGlobalInclusionFiles();
				job.basicTypesInput = set.listBasicTypes();
				job.basicDeclarationsInput = set.listBasicDeclarations();
				job.globalDeclarationsInput = set.listGlobalDeclarations();
				job.globalMacrosInput = set.listGlobalMacros();
				job.allDeclarationsInput = set.listAllDeclarations();
				job.info = project.getProject().getName();
			}	
		
			job.schedule();
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	private class RefreshUIJob extends UIJob{
		public IDeclaration[] basicTypesInput;
		public File[] globalInclusionFilesBase;
		public IDeclaration[] basicDeclarationsInput;
		public IParseFile[] globalInclusionFilesInput;
		public IDeclaration[] globalDeclarationsInput;
		public IMacro[] globalMacrosInput;
		public IDeclaration[] allDeclarationsInput;
		public String info;
		
		public RefreshUIJob(){
			super( "Refresh declarations" );
			setSystem( true );
		}
		
		@Override
		public IStatus runInUIThread( IProgressMonitor monitor ){
			monitor.beginTask( "Set", 1 );
			
			BasicDeclarationSetView.this.globalInclusionFilesBase = new HashSet<File>();
			for( File file : globalInclusionFilesBase ){
				BasicDeclarationSetView.this.globalInclusionFilesBase.add( file );
			}
			
			basicTypes.setInput( basicTypesInput );
			basicDeclarations.setInput( basicDeclarationsInput );
			globalInclusionFiles.setInput( globalInclusionFilesInput );
			globalDeclarations.setInput( globalDeclarationsInput );
			globalMacros.setInput( globalMacrosInput );
			allDeclarations.setInput( allDeclarationsInput );
			currentProject.setText( info );
			
			monitor.done();
			return Status.OK_STATUS;
		}
	}
}
