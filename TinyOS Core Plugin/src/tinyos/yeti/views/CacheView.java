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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.ProjectManager;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.model.IProjectDefinitionCollector;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;

/**
 * A view giving information about the cache of a {@link ProjectTOS}.
 * @author Benjamin Sigg
 */
public class CacheView extends ViewPart implements ProjectManager.IProjectLastAccessedChangeListener{
    private Spinner projectModelCacheSize;
    private Label projectModelUsed;
    private TableViewer projectModelParseFiles;

    private Spinner wireCacheSize;
    private Label wireCacheUsed;
    private TableViewer wireCacheFiles;

    private ProjectModel model;

    private UpdateJob job;

    public CacheView(){
        TinyOSPlugin.getDefault().getProjectManager().addProjectChangeListener( this );
    }

    private void setProject( final IProject project ){
        Job ui = new UIJob( "Update" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                monitor.beginTask( "Update", 1 );
                if( project != null && !projectModelCacheSize.isDisposed() ){
                	try{
	                    ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	                    model = tos.getModel();
	
	                    projectModelCacheSize.setSelection( model.getCacheSize() );
	                    IProjectDefinitionCollector wire = model.getDefinitionCollection();
	                    wireCacheSize.setSelection( wire.getCacheSize() );
	
	                    if( job == null ){
	                        job = new UpdateJob();
	                        job.schedule();
	                    }
                	}
                	catch( MissingNatureException ex ){
                		// silent
                	}
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        ui.setSystem( true );
        ui.setPriority( Job.DECORATE );
        ui.schedule();
    }

    @Override
    public void dispose(){
        super.dispose();
        job = null;
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin != null ){
            plugin.getProjectManager().removeProjectLastChangeListener( this );
        }
    }

    @Override
    public void createPartControl( Composite parent ){
        TabFolder folder = new TabFolder( parent, SWT.BOTTOM );

        TabItem projectModel = new TabItem( folder, SWT.NONE );
        Composite projectModelParent = new Composite( folder, SWT.NONE );
        createProjectModel( projectModelParent );
        projectModel.setControl( projectModelParent );
        projectModel.setText( "AST Model" );

        TabItem wire = new TabItem( folder, SWT.NONE );
        Composite wireParent = new Composite( folder, SWT.NONE );
        createDefinitionCollector( wireParent );
        wire.setControl( wireParent );
        wire.setText( "Wiring and Includes" );

        setProject( TinyOSPlugin.getDefault().getProjectManager().getlastProject() );
    }

    private void createProjectModel( Composite parent ){
        parent.setLayout( new GridLayout( 2, false ) );

        Label cacheSize = new Label( parent, SWT.NONE );
        cacheSize.setText( "Cache size AST-Model" );
        cacheSize.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        projectModelCacheSize = new Spinner( parent, SWT.BORDER );
        projectModelCacheSize.setValues( 0, 0, 1000, 0, 1, 10 );
        projectModelCacheSize.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        projectModelUsed = new Label( parent, SWT.NONE );
        projectModelUsed.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        projectModelUsed.setText( "Used: -" );

        projectModelParseFiles = new TableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.FULL_SELECTION );
        projectModelParseFiles.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        projectModelParseFiles.setLabelProvider( new ProjectModelLabelProvider() );
        projectModelParseFiles.setContentProvider( new ArrayContentProvider() );

        projectModelParseFiles.setColumnProperties( new String[]{ "File", "Fully loaded" } );
        projectModelParseFiles.getTable().setHeaderVisible( true );

        TableColumn columnFile = new TableColumn( projectModelParseFiles.getTable(), SWT.LEFT );
        columnFile.setText( "File" );
        columnFile.setWidth( 200 );

        TableColumn columnLoad = new TableColumn( projectModelParseFiles.getTable(), SWT.CENTER );
        columnLoad.setText( "Fully loaded" );
        columnLoad.setWidth( 100 );

        projectModelCacheSize.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                if( model != null ){
                    model.setCacheSize( projectModelCacheSize.getSelection() );
                }
            }
        });
    }

    private void createDefinitionCollector( Composite parent ){
        parent.setLayout( new GridLayout( 2, false ) );

        Label cacheSize = new Label( parent, SWT.NONE );
        cacheSize.setText( "Cache size Wiring and Includes" );
        cacheSize.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        wireCacheSize = new Spinner( parent, SWT.BORDER );
        wireCacheSize.setValues( 0, 0, 1000, 0, 1, 10 );
        wireCacheSize.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        wireCacheUsed = new Label( parent, SWT.NONE );
        wireCacheUsed.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        wireCacheUsed.setText( "Used: -" );

        wireCacheFiles = new TableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE | SWT.FULL_SELECTION );
        wireCacheFiles.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
        wireCacheFiles.setLabelProvider( new WireLabelProvider() );
        wireCacheFiles.setContentProvider( new ArrayContentProvider() );

        wireCacheFiles.setColumnProperties( new String[]{ "File" } );
        wireCacheFiles.getTable().setHeaderVisible( true );

        TableColumn columnFile = new TableColumn( wireCacheFiles.getTable(), SWT.LEFT );
        columnFile.setText( "File" );
        columnFile.setWidth( 200 );

        wireCacheSize.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                if( model != null ){
                    IProjectDefinitionCollector wire = model.getDefinitionCollection();
                    wire.setCacheSize( wireCacheSize.getSelection() );
                }
            }
        });
    }

    @Override
    public void setFocus(){
    }

    public void projectChanged( IProject p ){
        setProject( p );
    }

    private class UpdateJob extends CancelingJob{
        public UpdateJob(){
            super( "Update Cache View" );
            setPriority( Job.INTERACTIVE );
        }
        
        @Override
        public IStatus run( IProgressMonitor monitor ){
            monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
            
            final List<ProjectModelContent> contents = new ArrayList<ProjectModelContent>();
            final List<WireContent> wireList = new ArrayList<WireContent>();
            
            PublicJob read = new PublicJob( "Read" ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    final ProjectModel model = CacheView.this.model;
                    if( model != null ){
                        IParseFile[] files = model.getCachedFiles();
                        boolean[] full = model.areCachedFilesFullLoaded();

                        for( int i = 0, n = files.length; i<n; i++ ){
                            ProjectModelContent content = new ProjectModelContent();
                            content.file = files[i];
                            content.loaded = full[i];
                            contents.add( content );
                        }

                        IProjectDefinitionCollector wire = model.getDefinitionCollection();

                        IParseFile[] wireFiles = wire.getCachedFiles();

                        for( int i = 0, n = wireFiles.length; i<n; i++ ){
                            WireContent content = new WireContent();
                            content.file = wireFiles[i];
                            wireList.add( content );
                        }
                    }

                    return Status.OK_STATUS;
                }
            };

            read.setPriority( getPriority() );
            read.setSystem( true );
            model.runJob( read, new SubProgressMonitor( monitor, 0 ) );
            
            Job uiUpdate = new UIJob( "Update" ){
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ){
                    monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
                    
                    if( !projectModelParseFiles.getControl().isDisposed() ){
                        projectModelParseFiles.setInput( contents.toArray() );
                        projectModelUsed.setText( "Used: " + contents.size() );
                        wireCacheFiles.setInput( wireList.toArray() );
                        wireCacheUsed.setText( "Used: " + wireList.size() );
                        
                        job = new UpdateJob();
                        job.schedule( 5000 );
                    }

                    return Status.OK_STATUS;
                }
            };
            uiUpdate.setSystem( true );
            uiUpdate.schedule();
            
            monitor.done();
            return Status.OK_STATUS;
        }
    }

    private class WireLabelProvider implements ITableLabelProvider{
        public Image getColumnImage( Object element, int columnIndex ){
            return null;
        }

        public String getColumnText( Object element, int columnIndex ){
            WireContent content = (WireContent)element;
            switch( columnIndex ){
                case 0: return content.file == null ? "null" : content.file.getName();
                default: return null;
            }
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
    }

    private class ProjectModelLabelProvider implements ITableLabelProvider{
        public Image getColumnImage( Object element, int columnIndex ){
            return null;
        }

        public String getColumnText( Object element, int columnIndex ){
            ProjectModelContent content = (ProjectModelContent)element;
            switch( columnIndex ){
                case 0: return content.file == null ? "null" : content.file.getName();
                case 1: return String.valueOf( content.loaded );
                default: return null;
            }
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

    }

    private class ArrayContentProvider implements IStructuredContentProvider{
        public Object[] getElements( Object inputElement ){
            return (Object[])inputElement;
        }

        public void dispose(){
            // ignore
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
            viewer.refresh();
        }
    }

    private class ProjectModelContent{
        public IParseFile file;
        public boolean loaded;
    }

    private class WireContent{
        public IParseFile file;
    }
}
