package tinyOS.debug.simulation.launch.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.launch.configuration.AbstractTinyOSDebuggerTab;
import tinyos.yeti.TinyOSPlugin;

/**
 * A tab that lets the user choose which Simulator to use and how to connect it
 * @author Richard Huber
 */
public class SimulatorTab extends AbstractTinyOSDebuggerTab {
	
	protected Label simulatorConfigurationLabel;
	protected Combo simulatorConfigurationCombo;

	protected Composite simulatorConfigurationHolder;
	private ISimulatorConfigurationTab[] availableSimulatorCombos;
	private ILaunchConfiguration launchConfiguration;


	public SimulatorTab() {
		super();
		// List of all known simulator configuration types.
		availableSimulatorCombos = loadTabs();
	}
	
	private ISimulatorConfigurationTab[] loadTabs()
	{
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = reg.getExtensionPoint( "tinyos.yeti.debugger.simulation.simulatorTab" );

		class Tab implements Comparable<Tab>{
			double priority;
			ISimulatorConfigurationTab tab;
			@SuppressWarnings("unused")
			String simulationManager;
			
			public int compareTo( Tab o ){
				if( o.priority < priority )
					return 1;
				if( o.priority > priority )
					return -1;
				return 0;
			}
		}
		
		List<Tab> tabs = new ArrayList<Tab>();
		
        for( IExtension ext : extPoint.getExtensions() )
        {
        	for( IConfigurationElement element : ext.getConfigurationElements() )
            {
            	if( element.getName().equals( "tab" ) )
            	{
                	try{
                		Tab tab = new Tab();
                		
                		tab.priority = Double.valueOf( element.getAttribute( "order" ) );
                		tab.tab = (ISimulatorConfigurationTab) element.createExecutableExtension( "class" );
                		tab.simulationManager = element.getAttribute("simulation_manager");
                		tabs.add( tab );
                	} 
                	catch ( CoreException e ){
                		TinyOSDebugPlugin.getDefault().log( e.getMessage(), e );
                	}
                	catch( NumberFormatException ex ){
                		String className = element.getAttribute( "class" );
                		TinyOSDebugPlugin.getDefault().log( "cannot load '" + className + "'", ex );
                	}
                }
            }
        }

        Collections.sort( tabs );
        
        ISimulatorConfigurationTab[] result = new ISimulatorConfigurationTab[ tabs.size() ];
        for( int i = 0; i < result.length; i++ ){
        	result[i] = tabs.get( i ).tab;
        }
        
        return result;
	}

	protected void setLaunchConfiguration(ILaunchConfiguration lc) {
		launchConfiguration = lc;
	}

	protected ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	@Override
	public String getErrorMessage() 
	{
		ISimulatorConfigurationTab sc = getConfigForCurrentSimulator();
		if(sc != null && !sc.isValid(getLaunchConfiguration()))
		{
			return sc.getErrorCondition();
		}
		return getErrorCondition();
	}

	@Override
	public void createControl(Composite parent) 
	{
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createSimulatorCommandTab(comp);

		createVerticalSpacer(comp, 1);
	}

	private void createSimulatorCommandTab(Composite parent) 
	{
		createSimulatorCombo(parent);
		createSimulatorGroup(parent);
	}

	private void createSimulatorGroup(Composite parent) 
	{
		Group simulatorGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		simulatorGroup.setText("Simulator Configuration");
		simulatorConfigurationHolder = simulatorGroup;
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		simulatorConfigurationHolder.setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		simulatorConfigurationHolder.setLayoutData(gd);
	}

	private void createSimulatorCombo(Composite parent) 
	{
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		simulatorConfigurationLabel = new Label(comboComp, SWT.NONE);
		simulatorConfigurationLabel.setText("Simulator:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		simulatorConfigurationLabel.setLayoutData(gd);

		simulatorConfigurationCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		simulatorConfigurationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		simulatorConfigurationCombo.addSelectionListener(
			new SelectionListener() 
			{
				public void widgetSelected(SelectionEvent e) {
					if (!isInitializing()) {
						handleSimulatorChanged();
					}
				}
	
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
	}

	protected void loadSimulatorCombo(ISimulatorConfigurationTab[] simulatorConfigs, String current) 
	{
		simulatorConfigurationCombo.removeAll();
		int select = 0;
		for (int i = 0; i < simulatorConfigs.length; i++) 
		{
			simulatorConfigurationCombo.add(simulatorConfigs[i].getName());
			simulatorConfigurationCombo.setData(Integer.toString(i), simulatorConfigs[i]);
			if (simulatorConfigs[i].getID().equalsIgnoreCase(current)) 
			{
				select = i;
			}
		}
		simulatorConfigurationCombo.select(select);
		handleSimulatorChanged(); 
	}

	/**
	 * Return the class that implements <code>SimulatorConfiguration</code>
	 * that is registered against the name of the currently selected simulator.
	 */
	protected ISimulatorConfigurationTab getConfigForCurrentSimulator() 
	{
		if(simulatorConfigurationCombo != null) 
		{
			int selectedIndex = simulatorConfigurationCombo.getSelectionIndex();
			return (ISimulatorConfigurationTab)simulatorConfigurationCombo.getData(Integer.toString(selectedIndex));
		}
		return null;
	}


	/**
	 * Notification that the user changed the selection of the simulator.
	 */
	protected void handleSimulatorChanged() 
	{
		loadDynamicArea();
		if(!isInitializing()) {
			updateLaunchConfigurationDialog();
		}
	}

	protected void loadDynamicArea() 
	{
		// Dispose of any current child widgets in the simulator holder area
		Control[] children = simulatorConfigurationHolder.getChildren();
		for (int i = 0; i < children.length; i++) 
		{
			children[i].dispose();
		}

		// Ask the dynamic UI to create its Control
		ISimulatorConfigurationTab sc = getConfigForCurrentSimulator();
		if(sc != null) {		
			sc.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
			sc.createControl(simulatorConfigurationHolder);
			sc.initializeFrom(getLaunchConfiguration());
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) 
	{
		if(super.isValid(launchConfig))
		{
			ISimulatorConfigurationTab sc = getConfigForCurrentSimulator();
			if(sc != null && !sc.isValid(launchConfig))
			{
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String getName()
	{
		return "Simulator";
	}

	@Override
	public Image getImage() 
	{
		// TODO: Create icon for simulations
		return NesCDebugIcons.get(NesCDebugIcons.ICON_GDB_PROXY);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) 
	{
		setInitializing(true);

		setLaunchConfiguration(configuration);
		String simulatorConfig = "";
		try {
			simulatorConfig = configuration.getAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_CURRENT_SIMULATOR, "");
			loadSimulatorCombo(availableSimulatorCombos, simulatorConfig);
		} catch (CoreException ce) {
			TinyOSPlugin.getDefault().log("Exception while initializing SimulatorTab", ce);
		}

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) 
	{
		ISimulatorConfigurationTab sc = getConfigForCurrentSimulator();
		if(sc != null) 
		{
			boolean dirty = sc.isDirty();
			sc.performApply(configuration);
			if(dirty || isDirty()) 
			{
				configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_CURRENT_SIMULATOR, sc.getID());
				configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST, sc.getHost().trim());
				configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_PORT, sc.getPort());
			}
		} else {
			configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST, "localhost");
			configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_PORT, 4242);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) 
	{
		configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_HOST, "localhost");
		configuration.setAttribute(ITinyOSDebugSimulationLaunchConstants.ATTR_SIMULATOR_PORT, 4242);
		ISimulatorConfigurationTab sc = getConfigForCurrentSimulator();
		if(sc != null) 
		{
			sc.setDefaults(configuration);
		}
	}

}
