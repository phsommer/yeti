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
package tinyos.yeti.wizards;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.EnvironmentManager;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;

public class TinyOSNewProjectCreationPage extends WizardPage {

    public TinyOSNewProjectCreationPage(String pageName) {
        super(pageName);
        setPageComplete(false);
        customLocationFieldValue = ""; //$NON-NLS-1$
    }

    boolean useDefaults = true;

    // initial value stores
    private String initialProjectFieldValue;

    private String initialLocationFieldValue;

    // the value the user has entered
    private String customLocationFieldValue;

    private Combo targetCombo;
    private Combo envCombo;
    private IEnvironment envs[];

    // widgets
    private Text projectNameField;
    private Text locationPathField;

    private Label locationLabel;

    private Button browseButton;

    private Listener nameModifyListener = new Listener() {
        public void handleEvent(Event e) {
            boolean valid = validatePage();
            setPageComplete(valid);
            if (valid)
                setLocationForSelection();
        }
    };

    private Listener envModifyListener = new Listener() {
        public void handleEvent(Event e) {
            targetCombo.removeAll();
            if (envs[envCombo.getSelectionIndex()] != null) {
                IPlatform[] ip = envs[envCombo.getSelectionIndex()].getPlatforms();
                if ((ip!=null)&&(ip.length>0)) {
                    for (int i = 0; i < ip.length; i++) {
                        targetCombo.add(ip[i].getName());
                    }
                }
                targetCombo.select(0);
            }

        }
    };

    private Listener locationModifyListener = new Listener() {
        public void handleEvent(Event e) {
            setPageComplete(validatePage());
        }
    };

    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    private static final String WizardNewProjectCreationPage_projectExistsMessage = 
        Messages.getString("TiynOSNewProjectCreationPage.projectExistsMessage"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_projectContentsLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.projectContentsLabel"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_nameLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.nameLabel"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_locationLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.locationLabel"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_browseLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.browseLabel"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_directoryLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.directoryLabel"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_projectNameEmpty = 
        Messages.getString("TiynOSNewProjectCreationPage.projectNameEmpty"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_projectLocationEmpty = 
        Messages.getString("TiynOSNewProjectCreationPage.projectLocationEmpty"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_locationError = 
        Messages.getString("TiynOSNewProjectCreationPage.locationError"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_defaultLocationError = 
        Messages.getString("TiynOSNewProjectCreationPage.defaultLocationError"); //$NON-NLS-1$

    private static final String WizardNewProjectCreationPage_useDefaultLabel = 
        Messages.getString("TiynOSNewProjectCreationPage.useDefaultLabel"); //$NON-NLS-1$

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());

        initializeDialogUnits(parent);

//        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
//                IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite upperGroup = new Composite( composite, SWT.NULL );
        upperGroup.setLayout( new GridLayout( 2, false ));
        upperGroup.setLayoutData(new GridData( SWT.FILL, SWT.TOP, true, false ));
        createProjectNameGroup(upperGroup);
        createProjectEnvironmentGroup(upperGroup);
        createProjectTargetGroup(upperGroup);
        
        createProjectLocationGroup(composite);
        
        setPageComplete(validatePage());
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    public String getTarget() {
    	int index = targetCombo.getSelectionIndex();
    	if( index < 0 )
    		return null;
    	if( index >= targetCombo.getItemCount() )
    		return null;
    	
    	return targetCombo.getItem( index );
    }

    private void createProjectEnvironmentGroup(Composite parent) {
        // new project label
        Label projectLabel = new Label( parent, SWT.NONE);
        projectLabel
        .setText(Messages.getString("TiynOSNewProjectCreationPage.selectEnvironment")); //$NON-NLS-1$
        projectLabel.setFont(parent.getFont());
        projectLabel.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, false, false ));

        // new project name entry field
        envs = EnvironmentManager.getDefault().getEnvironmentsArray();
        envCombo = new Combo( parent, SWT.BORDER | SWT.READ_ONLY);
        for (int i = 0; i < envs.length; i++) {
            IEnvironment e = envs[i];
            envCombo.add(e.getEnvironmentName());
        }
        if (envCombo.getItemCount()>0) {
            envCombo.select(0);
        }

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        envCombo.setLayoutData(data);
        envCombo.setFont(parent.getFont());

        envCombo.addListener(SWT.Modify, envModifyListener);
    }

    private void createProjectTargetGroup(Composite parent) {
        // new project label
        Label projectLabel = new Label( parent, SWT.NONE);
        projectLabel
        .setText(Messages.getString("TiynOSNewProjectCreationPage.selectTarget")); //$NON-NLS-1$
        projectLabel.setFont(parent.getFont());
        projectLabel.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, false, false ));

        // new project name entry field
        targetCombo = new Combo( parent, SWT.BORDER| SWT.READ_ONLY);
        if (envs.length > 0 && envs[0] != null) {
            IPlatform[] ip = envs[0].getPlatforms();
            if ((ip!=null)&&(ip.length>0)) {
                for (int i = 0; i < ip.length; i++) {
                    targetCombo.add(ip[i].getName());
                }
            }
            targetCombo.select(0);
        }


        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        targetCombo.setLayoutData(data);
        targetCombo.setFont(parent.getFont());

    }

    /**
     * Creates the project location specification controls.
     * 
     * @param parent
     *            the parent composite
     */
    private final void createProjectLocationGroup(Composite parent) {

        Font font = parent.getFont();
        // project specification group
        Group projectGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectGroup.setFont(font);
        //projectGroup.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectContentsGroupLabel);
        projectGroup.setText( WizardNewProjectCreationPage_projectContentsLabel );

        final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK
                | SWT.RIGHT);
        useDefaultsButton.setText( WizardNewProjectCreationPage_useDefaultLabel );
        useDefaultsButton.setSelection(useDefaults);
        useDefaultsButton.setFont(font);

        GridData buttonData = new GridData();
        buttonData.horizontalSpan = 3;
        useDefaultsButton.setLayoutData(buttonData);

        createUserSpecifiedProjectLocationGroup(projectGroup, !useDefaults);

        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDefaults = useDefaultsButton.getSelection();
                browseButton.setEnabled(!useDefaults);
                locationPathField.setEnabled(!useDefaults);
                locationLabel.setEnabled(!useDefaults);
                if (useDefaults) {
                    customLocationFieldValue = locationPathField.getText();
                    setLocationForSelection();
                } else {
                    locationPathField.setText(customLocationFieldValue);
                }
            }
        };
        useDefaultsButton.addSelectionListener(listener);
    }

    /**
     * Creates the project name specification controls.
     *
     * @param parent the parent composite
     */
    private final void createProjectNameGroup(Composite parent) {
        // new project label
        Label projectLabel = new Label(parent, SWT.NONE);
        projectLabel.setText( WizardNewProjectCreationPage_nameLabel );
        projectLabel.setFont(parent.getFont());
        projectLabel.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, false, false ));

        // new project name entry field
        projectNameField = new Text(parent, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        projectNameField.setLayoutData(data);
        projectNameField.setFont(parent.getFont());

        // Set the initial value first before listener
        // to avoid handling an event during the creation.
        if (initialProjectFieldValue != null)
            projectNameField.setText(initialProjectFieldValue);
        projectNameField.addListener(SWT.Modify, nameModifyListener);
    }

    /**
     * Creates the project location specification controls.
     *
     * @param projectGroup the parent composite
     * @param enabled the initial enabled state of the widgets created
     */
    private void createUserSpecifiedProjectLocationGroup(
            Composite projectGroup, boolean enabled) {

        Font font = projectGroup.getFont();

        // location label
        locationLabel = new Label(projectGroup, SWT.NONE);
        locationLabel.setText( WizardNewProjectCreationPage_locationLabel );
        locationLabel.setEnabled(enabled);
        locationLabel.setFont(font);

        // project location entry field
        locationPathField = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        locationPathField.setLayoutData(data);
        locationPathField.setEnabled(enabled);
        locationPathField.setFont(font);

        // browse button
        browseButton = new Button(projectGroup, SWT.PUSH);
        browseButton.setText(WizardNewProjectCreationPage_browseLabel);
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                handleLocationBrowseButtonPressed();
            }
        });

        browseButton.setEnabled(enabled);
        browseButton.setFont(font);
        setButtonLayoutData(browseButton);

        // Set the initial value first before listener
        // to avoid handling an event during the creation.
        if (initialLocationFieldValue == null)
            locationPathField.setText(Platform.getLocation().toOSString());
        else
            locationPathField.setText(initialLocationFieldValue);
        locationPathField.addListener(SWT.Modify, locationModifyListener);
    }

    /**
     * Returns the current project location path as entered by 
     * the user, or its anticipated initial value.
     * Note that if the default has been returned the path
     * in a project description used to create a project
     * should not be set.
     *
     * @return the project location path or its anticipated initial value.
     */
    public IPath getLocationPath() {
        if (useDefaults)
            return Platform.getLocation();

        return new Path(getProjectLocationFieldValue());
    }

    /**
     * Creates a project resource handle for the current project name field value.
     * <p>
     * This method does not create the project resource; this is the responsibility
     * of <code>IProject::create</code> invoked by the new project resource wizard.
     * </p>
     *
     * @return the new project resource handle
     */
    public IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(
                getProjectName());
    }

    /**
     * Returns the current project name as entered by the user, or its anticipated
     * initial value.
     *
     * @return the project name, its anticipated initial value, or <code>null</code>
     *   if no project name is known
     */
    public String getProjectName() {
        if (projectNameField == null)
            return initialProjectFieldValue;

        return getProjectNameFieldValue();
    }

    /**
     * Returns the value of the project name field
     * with leading and trailing spaces removed.
     * 
     * @return the project name in the field
     */
    private String getProjectNameFieldValue() {
        if (projectNameField == null)
            return ""; //$NON-NLS-1$

        return projectNameField.getText().trim();
    }

    /**
     * Returns the value of the project location field
     * with leading and trailing spaces removed.
     * 
     * @return the project location directory in the field
     */
    private String getProjectLocationFieldValue() {
        if (locationPathField == null)
            return ""; //$NON-NLS-1$

        return locationPathField.getText().trim();
    }

    /**
     *	Open an appropriate directory browser
     */
    void handleLocationBrowseButtonPressed() {
        DirectoryDialog dialog = new DirectoryDialog(locationPathField
                .getShell());
        dialog.setMessage(WizardNewProjectCreationPage_directoryLabel);

        String dirName = getProjectLocationFieldValue();
        if (!dirName.equals("")) { //$NON-NLS-1$
            File path = new File(dirName);
            if (path.exists())
                dialog.setFilterPath(new Path(dirName).toOSString());
        }

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            customLocationFieldValue = selectedDirectory;
            locationPathField.setText(customLocationFieldValue);
        }
    }

    /**
     * Sets the initial project name that this page will use when
     * created. The name is ignored if the createControl(Composite)
     * method has already been called. Leading and trailing spaces
     * in the name are ignored.
     * 
     * @param name initial project name for this page
     */
    public void setInitialProjectName(String name) {
        if (name == null)
            initialProjectFieldValue = null;
        else {
            initialProjectFieldValue = name.trim();
            initialLocationFieldValue = getDefaultLocationForName(initialProjectFieldValue);
        }
    }

    /**
     * Set the location to the default location if we are set to useDefaults.
     */
    void setLocationForSelection() {
        if (useDefaults)
            locationPathField
            .setText(getDefaultLocationForName(getProjectNameFieldValue()));
    }

    /**
     * Get the defualt location for the provided name.
     * 
     * @param nameValue the name
     * @return the location
     */
    private String getDefaultLocationForName(String nameValue) {
        IPath defaultPath = Platform.getLocation().append(nameValue);
        return defaultPath.toOSString();
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    protected boolean validatePage() {
        // IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        String projectFieldContents = getProjectNameFieldValue();
        if (projectFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            setMessage(WizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }

        IStatus nameStatus = workspace.validateName(projectFieldContents,
                IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }

        String locationFieldContents = getProjectLocationFieldValue();

        if (locationFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            setMessage(WizardNewProjectCreationPage_projectLocationEmpty);
            return false;
        }

        IPath path = new Path(""); //$NON-NLS-1$
        if (!path.isValidPath(locationFieldContents)) {
            setErrorMessage(WizardNewProjectCreationPage_locationError);
            return false;
        }

        IPath projectPath = new Path(locationFieldContents);
        if (!useDefaults && Platform.getLocation().isPrefixOf(projectPath)) {
            setErrorMessage(WizardNewProjectCreationPage_defaultLocationError);
            return false;
        }

        IProject handle = getProjectHandle();
        if (handle.exists()) {
            setErrorMessage(WizardNewProjectCreationPage_projectExistsMessage);
            return false;
        }

        /*
         * If not using the default value validate the location.
         */
        if (!useDefaults()) {
            IStatus locationStatus = workspace.validateProjectLocation(handle,
                    projectPath);
            if (!locationStatus.isOK()) {
                setErrorMessage(locationStatus.getMessage()); //$NON-NLS-1$
                return false;
            }
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    /*
     * see @DialogPage.setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible)
            projectNameField.setFocus();
    }

    /**
     * Returns the useDefaults.
     * @return boolean
     */
    public boolean useDefaults() {
        return useDefaults;
    }

    public IEnvironment getEnvironment() {
        return envs[envCombo.getSelectionIndex()];
    }

}

