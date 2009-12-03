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
package tinyOS.debug.launch.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.CDTAbstractionLayer.CDTLaunchConfigConst;

public class MainConfigurationTab extends AbstractTinyOSDebuggerTab {

	/**
	 * The TinyOS nature
	 */
	public static final String TINYOS_NATURE = "tinyos.yeti.core.TinyOSProject";

	public class ProjectSelectionLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if(element instanceof IProject) {

				return ((IProject)element).getName();
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if(element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) element;
				IWorkbenchAdapter wbAdapter= (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
				if (wbAdapter == null) {
					return null;
				}
				ImageDescriptor descriptor= wbAdapter.getImageDescriptor(adaptable);
				if (descriptor == null) {
					return null;
				}
				return descriptor.createImage();
			}
			return super.getImage(element);
		}

	}
	// Project UI widgets
	protected Label projectLabel;
	protected Text projectText;
	protected Button projectButton;

	// Main class UI widgets
	protected Label programLabel;
	protected Text programText;
	protected Button programSearchButton;
	
	private Combo prerunTabsCombo;
	private Composite prerunTabsContent;
	
	private Tab[] prerunTabs;

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createProjectGroup(comp, 1);
		createExeFileGroup(comp, 1);
		createPrerunSelection( comp, 1 );
		createVerticalSpacer(comp, 1);
	}

	protected void createProjectGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		projectLabel = new Label(projComp, SWT.NONE);
		projectLabel.setText("Project:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		projectLabel.setLayoutData(gd);

		projectText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		projectText.setLayoutData(gd);
		projectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
				updatePrerunProject();
			}
		});

		projectButton = createPushButton(projComp, "Browse...", null); //$NON-NLS-1$
		projectButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createPrerunSelection( Composite parent, int colSpan ){
		prerunTabs = loadTabs();
		
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout contentLayout = new GridLayout();
		contentLayout.numColumns = 2;
		contentLayout.marginHeight = 0;
		contentLayout.marginWidth = 0;
		content.setLayout( contentLayout );
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		content.setLayoutData(gd);

		Label contentLabel = new Label( content, SWT.NONE);
		contentLabel.setText( "Before starting the debug session:" ); 
		gd = new GridData();
		gd.horizontalSpan = 2;
		contentLabel.setLayoutData(gd);
		
		prerunTabsCombo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
		
		prerunTabsContent = new Composite( parent, SWT.NONE );
		prerunTabsContent.setLayoutData(  new GridData( SWT.LEFT, SWT.CENTER, false, false, colSpan, 1 ) );
		prerunTabsContent.setLayout( new StackLayout() );
		prerunTabsContent.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, colSpan, 1 ) );
		
		for( Tab tab : prerunTabs ){
			prerunTabsCombo.add( tab.getTab().getName() );
			tab.create( prerunTabsContent );
		}
		
		prerunTabsCombo.addSelectionListener( new SelectionListener(){
			@Override
			public void widgetDefaultSelected( SelectionEvent e ){
				selectPrerunTab( prerunTabsCombo.getSelectionIndex() );
				if( !isInitializing() ){
					setDirty( true );
					updateLaunchConfigurationDialog();
				}
			}
			@Override
			public void widgetSelected( SelectionEvent e ){
				selectPrerunTab( prerunTabsCombo.getSelectionIndex() );
				if( !isInitializing() ){
					setDirty( true );
					updateLaunchConfigurationDialog();
				}
			}
		});
	}
	
	private int indexOfTab( String id ){
		for( int i = 0; i < prerunTabs.length; i++ ){
			if( prerunTabs[i].getId().equals( id )){
				return i;
			}
		}
		return -1;
	}
	
	private void selectPrerunTab( int index ){
		if( index < 0 )
			index = 0;
		
		if( prerunTabsCombo.getSelectionIndex() != index ){
			prerunTabsCombo.select( index );
		}
		
		((StackLayout)prerunTabsContent.getLayout()).topControl = prerunTabs[ index ].getControl();
		prerunTabsContent.layout();
	}
	
	private void selectPrerunTab( String id ){
		selectPrerunTab( indexOfTab( id ) );
	}
	
	private String selectedPrerunTab(){
		int index = prerunTabsCombo.getSelectionIndex();
		if( index < 0 )
			index = 0;
		return prerunTabs[index].getId();
	}
	
	public ILaunchPrerunTab getSelectedPrerunTab(){
		int index = prerunTabsCombo.getSelectionIndex();
		if( index < 0 )
			index = 0;
		
		if( index >= prerunTabs.length )
			return null;
		
		return prerunTabs[index].tab;
	}
	
	private void updatePrerunProject(){
		IProject project;
		try{
			project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectText.getText() );
		}
		catch( IllegalArgumentException ex ){
			project = null;
		}
		for( Tab tab : prerunTabs ){
			tab.getTab().setProject( project );
		}
	}
	
	private Tab[] loadTabs(){
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = reg.getExtensionPoint( "tinyos.yeti.debugger.launchPrerun" );

		List<Tab> tabs = new ArrayList<Tab>();
		
        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "prerun" ) ){
                	try{
                		double priority = Double.valueOf( element.getAttribute( "order" ) );
                		ILaunchPrerunTab tab = (ILaunchPrerunTab) element.createExecutableExtension( "tab-class" );
                		String id = element.getAttribute( "id" );
                		
                		tabs.add( new Tab( tab, id, priority ) );
                	} 
                	catch ( CoreException e ){
                		TinyOSDebugPlugin.getDefault().log( e.getMessage(), e );
                	}
                	catch( NumberFormatException ex ){
                		String className = element.getAttribute( "tab-class" );
                		TinyOSDebugPlugin.getDefault().log( "cannot load '" + className + "'", ex );
                	}
                }
            }
        }

        Tab[] result = tabs.toArray( new Tab[ tabs.size() ] );
        Arrays.sort( result );
        
        return result;
	}
	
	private void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null) {
			return;
		}

		String projectName = project.getName();
		projectText.setText(projectName);
	}

	/**
	 * Realize a TinyOS Project selection dialog and return the first selected project, or null if there
	 * was none.
	 */
	protected IProject chooseProject() {

		IProject[] projects = getTinyOSProjects();

		ILabelProvider labelProvider = new ProjectSelectionLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection"); //$NON-NLS-1$
		dialog.setMessage("Choose a project to constrain the search for a TinyOS Binary"); //$NON-NLS-1$
		dialog.setElements(projects);

		IProject project = getProject();
		if (project != null) {
			dialog.setInitialSelections(new Object[]{project});
		}
		if (dialog.open() == Window.OK) {
			return (IProject)dialog.getFirstResult();
		}

		return null;
	}

	/**
	 * Return an array a ProjectTOS containing all TinyOS projects.
	 */
	private IProject[] getTinyOSProjects() {
		Vector<IProject> res = new Vector<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(IProject p: projects) {
			try {
				if(p.isAccessible() && p.hasNature(TINYOS_NATURE)) {
					res.add(p);
				}
			} catch (CoreException e) {
				TinyOSDebugPlugin.getDefault().log("Could not check nature: "+p.getName(), e);
			}
		}
		return res.toArray(new IProject[res.size()]);
	}

	/**
	 * Return the ProjectTOS corresponding to the project name in the project name text field, or
	 * null if the text does not match a project name.
	 */
	private IProject getProject() {		
		String projectName = projectText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	protected void createExeFileGroup(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);
		programLabel = new Label(mainComp, SWT.NONE);
		programLabel.setText("TinyOS Binary:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		programLabel.setLayoutData(gd);
		programText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		programText.setLayoutData(gd);
		programText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if ( !isInitializing() ) {
					setDirty(true);
					updateLaunchConfigurationDialog();
				}
			}
		});

		programSearchButton = createPushButton(mainComp, "Search Project...", null); //$NON-NLS-1$
		programSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForBinaryButton;
		fBrowseForBinaryButton = createPushButton(mainComp, "Browse...", null); //$NON-NLS-1$
		fBrowseForBinaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleBinaryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}


	protected void handleSearchButtonSelected() {
		if (getProject() == null) {
			MessageDialog.openInformation(getShell(), "Project required", //$NON-NLS-1$
			"Project must first be entered before browsing for a program"); //$NON-NLS-1$
			return;
		}

		ILabelProvider programLabelProvider = new LabelProvider() {
			public Image getImage(Object element) {
				return DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN);
			}
		};

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), programLabelProvider);
		dialog.setElements(getBinaries());
		dialog.setMessage("Choose a TinyOS Binary:"); //$NON-NLS-1$
		dialog.setTitle("Binary Selection"); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			String binary = (String)dialog.getFirstResult();
			programText.setText(binary);
		}
	}

	protected String[] getBinaries() {
		Vector<String> result = new Vector<String>();
		IProject proj = getProject();
		if(proj != null) {
			IPath p = Path.fromOSString("build");
			IFolder folder = proj.getFolder(p);
			try {
				IResource[] children = folder.members();
				for(IResource child:children) {
					if(child instanceof IFolder) {
						String bin = getBinaryFromFolder((IFolder)child);
						if(bin != null) {
							bin = child.getProjectRelativePath() + "/" + bin;
							result.add(bin);
						}
					}
				}
			} catch (CoreException e) {
				TinyOSDebugPlugin.getDefault().log("Exception while searching for binaries", e);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private String getBinaryFromFolder(IFolder folder) throws CoreException {
		IResource[] children = folder.members();
		String extension = ".exe";
		for(IResource child:children) {
			String name = ((IFile)child).getName();
			int index = name.indexOf(extension);
			int length = index+extension.length();
			if(child instanceof IFile && index > 0 && name.length() == length) {
				return name;
			}
		}
		return null;
	}

	private void handleBinaryBrowseButtonSelected() {
		final IProject project = getProject();
		if (project == null) {
			MessageDialog.openInformation(getShell(), "Project required", //$NON-NLS-1$
			"Project must first be entered before browsing for a program"); //$NON-NLS-1$
			return;
		}
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(programText.getText());
		String text= fileDialog.open();
		if (text != null) {
			programText.setText(text);
		}
	}
	
	@Override
	public String getErrorMessage(){
		String superResult = super.getErrorMessage();
		if( superResult != null )
			return superResult;
		
		ILaunchPrerunTab tab = getSelectedPrerunTab();
		if( tab == null )
			return null;
		
		return tab.getErrorMessage();
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(super.isValid(launchConfig)) {
			String name = projectText.getText().trim();

			String error = "Project not specified";
			if (name.length() == 0) {
				setErrorCondition(error);
				return false;
			} else {
				removeErrorCondition(error);
			}
			error = "Project does not exist";
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorCondition(error); //$NON-NLS-1$
				return false;
			} else {
				removeErrorCondition(error);
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			error = "Project must be opened";
			if (!project.isOpen()) {
				setErrorCondition(error); //$NON-NLS-1$
				return false;
			} else {
				removeErrorCondition(error);
			}

			name = programText.getText().trim();
			error = "Program not specified";
			if (name.length() == 0) {
				setErrorCondition(error); //$NON-NLS-1$
				return false;
			} else {
				removeErrorCondition(error);
			}
			String prgNotExist = "Program does not exist";
			if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				setErrorCondition(prgNotExist); //$NON-NLS-1$
				return false;
			} else {
				removeErrorCondition(prgNotExist);
			}
			IPath exePath = new Path(name);
			if (!exePath.isAbsolute()) {
				IFile projFile = null;
				String invalidPath = "Program invalid project path";
				try {
					projFile = project.getFile(name);
				}
				catch (Exception exc) {
					// throws an exception if it's a relative path pointing outside project
					setErrorCondition(invalidPath); //$NON-NLS-1$
					return false;
				} 
				removeErrorCondition(invalidPath);
				if (projFile == null || !projFile.exists()) {
					setErrorCondition(prgNotExist);
					return false;
				} else {
					removeErrorCondition(prgNotExist);
				}
				exePath = projFile.getLocation();
			} else {
				if (!exePath.toFile().exists()) {
					setErrorCondition(prgNotExist);
					return false;
				} else {
					removeErrorCondition(prgNotExist);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Image getImage() {
		return NesCDebugIcons.get(NesCDebugIcons.ICON_CDT_MAIN_TAB);
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);
		try {
			projectText.setText(configuration.getAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, ""));
			programText.setText(configuration.getAttribute(CDTLaunchConfigConst.ATTR_PROGRAM_NAME, ""));
			selectPrerunTab( configuration.getAttribute( ITinyOSDebugLaunchConstants.ATTR_CURRENT_LAUNCH_PRERUN, "" ) );
			
			for( Tab tab : prerunTabs ){
				tab.getTab().read( configuration );
			}
			
		} catch (CoreException e) {
			TinyOSDebugPlugin.getDefault().log("Exception while initializing", e);
		}
		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if(isDirty()) {
			configuration.setAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, projectText.getText());
			configuration.setAttribute(CDTLaunchConfigConst.ATTR_PROGRAM_NAME, programText.getText());
			configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_CURRENT_LAUNCH_PRERUN, selectedPrerunTab() );

			setAdditionalAttributes(configuration);

			for( Tab tab : prerunTabs ){
				tab.getTab().apply( configuration );
			}
			
			setDirty(false);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, "");
		configuration.setAttribute(CDTLaunchConfigConst.ATTR_PROGRAM_NAME, "");
		configuration.setAttribute( ITinyOSDebugLaunchConstants.ATTR_CURRENT_LAUNCH_PRERUN, "" );

		setAdditionalAttributes(configuration);
	}

	private void setAdditionalAttributes(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(CDTLaunchConfigConst.ATTR_USE_TERMINAL, false);
	}
	
	private class Tab implements Comparable<Tab>, ILaunchPrerunTabHandle{
		private double priority;
		private ILaunchPrerunTab tab;
		private String id;
		private Control control;
		
		public Tab( ILaunchPrerunTab tab, String id, double priority ){
			this.tab = tab;
			this.id = id;
			this.priority = priority;
			
			tab.setHandle( this );
		}
		
		@Override
		public boolean isInitializing(){
			return MainConfigurationTab.this.isInitializing();
		}
		
		@Override
		public void setDirty(){
			MainConfigurationTab.this.setDirty( true );
			updateLaunchConfigurationDialog();
		}
	
		public void create( Composite composite ){
			control = tab.getControl( composite );
		}
		
		public Control getControl(){
			return control;
		}
		
		public int compareTo( Tab o ){
			if( o.priority < priority )
				return 1;
			if( o.priority > priority )
				return -1;
			return 0;
		}
		
		public double getPriority(){
			return priority;
		}
		
		public ILaunchPrerunTab getTab(){
			return tab;
		}
		
		public String getId(){
			return id;
		}
	}
}
