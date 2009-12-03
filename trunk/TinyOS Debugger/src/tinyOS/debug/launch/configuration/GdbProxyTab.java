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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import tinyOS.debug.NesCDebugIcons;
import tinyOS.debug.TinyOSDebugPlugin;
import tinyos.yeti.TinyOSPlugin;

/**
 * A tab that lets the user choose which command to run before kicking off CDT.
 * @author Silvan Nellen
 */
public class GdbProxyTab extends AbstractTinyOSDebuggerTab {
	protected Label proxyConfigurationLabel;
	protected Combo proxyConfigurationCombo;
	protected Text startupDelay;
	protected Label startupDelayLabel;

	protected Composite proxyConfigurationHolder;
	private IGdbProxyConfigurationTab[] availableProxyCombos;
	private ILaunchConfiguration launchConfiguration;


	public GdbProxyTab() {
		super();
		// List of all known gdb proxy configuration types.
		availableProxyCombos = loadTabs();
	}
	
	private IGdbProxyConfigurationTab[] loadTabs(){
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extPoint = reg.getExtensionPoint( "tinyos.yeti.debugger.proxyTab" );

		class Tab implements Comparable<Tab>{
			double priority;
			IGdbProxyConfigurationTab tab;
			
			public int compareTo( Tab o ){
				if( o.priority < priority )
					return 1;
				if( o.priority > priority )
					return -1;
				return 0;
			}
		}
		
		List<Tab> tabs = new ArrayList<Tab>();
		
        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "tab" ) ){
                	try{
                		Tab tab = new Tab();
                		
                		tab.priority = Double.valueOf( element.getAttribute( "order" ) );
                		tab.tab = (IGdbProxyConfigurationTab) element.createExecutableExtension( "class" );
                		
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
        
        IGdbProxyConfigurationTab[] result = new IGdbProxyConfigurationTab[ tabs.size() ];
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
	public String getErrorMessage() {
		IGdbProxyConfigurationTab gpc = getConfigForCurrentProxy();
		if(gpc != null && !gpc.isValid(getLaunchConfiguration())){
			return gpc.getErrorCondition();
		}
		return getErrorCondition();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createGdbProxyCommandTab(comp);

		createVerticalSpacer(comp, 1);
	}

	private void createGdbProxyCommandTab(Composite parent) {
		createProxyCombo(parent);
		createProxyGroup(parent);
		createStartupDelay(parent);
	}

	private void createStartupDelay(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		startupDelayLabel = new Label(comboComp, SWT.NONE);
		startupDelayLabel.setText("Startup delay [ms]:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 1;
		startupDelayLabel.setLayoutData(gd);

		startupDelay = new Text(comboComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.widthHint = 40;
		startupDelay.setLayoutData(gd);
		startupDelay.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				String error = "Invalid startup delay";
				if(!proxyDelayIsValid()) {
					setErrorCondition(error);
				} else {
					removeErrorCondition(error);
				}
				if(!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
	}

	private void createProxyGroup(Composite parent) {
		Group proxyGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		proxyGroup.setText("GDB Proxy Configuration"); //$NON-NLS-1$
		proxyConfigurationHolder = proxyGroup;
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		proxyConfigurationHolder.setLayout(tabHolderLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		proxyConfigurationHolder.setLayoutData(gd);
	}

	private void createProxyCombo(Composite parent) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comboComp.setLayoutData(gd);

		proxyConfigurationLabel = new Label(comboComp, SWT.NONE);
		proxyConfigurationLabel.setText("GDB Proxy:"); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 2;
		proxyConfigurationLabel.setLayoutData(gd);

		proxyConfigurationCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		proxyConfigurationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		proxyConfigurationCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (!isInitializing()) {
					handleProxyChanged();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	protected void loadProxyCombo(IGdbProxyConfigurationTab[] proxyConfigs, String current) {
		proxyConfigurationCombo.removeAll();
		int select = 0;
		for (int i = 0; i < proxyConfigs.length; i++) {
			proxyConfigurationCombo.add(proxyConfigs[i].getName());
			proxyConfigurationCombo.setData(Integer.toString(i), proxyConfigs[i]);
			if (proxyConfigs[i].getID().equalsIgnoreCase(current)) {
				select = i;
			}
		}
		proxyConfigurationCombo.select(select);
		handleProxyChanged(); 
	}

	/**
	 * Return the class that implements <code>GdbProxyConfiguration</code>
	 * that is registered against the name of the currently selected proxy.
	 */
	protected IGdbProxyConfigurationTab getConfigForCurrentProxy() {
		if(proxyConfigurationCombo != null) {
			int selectedIndex = proxyConfigurationCombo.getSelectionIndex();
			return (IGdbProxyConfigurationTab)proxyConfigurationCombo.getData(Integer.toString(selectedIndex));
		}
		return null;
	}


	/**
	 * Notification that the user changed the selection of the gdb proxy.
	 */
	protected void handleProxyChanged() {
		loadDynamicArea();
		if(!isInitializing()) {
			updateLaunchConfigurationDialog();
		}
	}

	protected void loadDynamicArea() {
		// Dispose of any current child widgets in the proxy holder area
		Control[] children = proxyConfigurationHolder.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		// Ask the dynamic UI to create its Control
		IGdbProxyConfigurationTab gpc = getConfigForCurrentProxy();
		if(gpc != null) {		
			gpc.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
			gpc.createControl(proxyConfigurationHolder);
			gpc.initializeFrom(getLaunchConfiguration());
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if(super.isValid(launchConfig)){
			if(!proxyDelayIsValid()) {
				return false;
			}
			IGdbProxyConfigurationTab gpc = getConfigForCurrentProxy();
			if(gpc != null && !gpc.isValid(launchConfig)){
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean proxyDelayIsValid() {
		try {
			String number = startupDelay.getText();
			number = number.trim();
			if(number.length() == 0) {
				return false;
			}
			int port = Integer.parseInt( number );
			return ( port > 0 && port <= Integer.MAX_VALUE );
		}
		catch( NumberFormatException e ) {
			return false;
		}
	}

	@Override
	public String getName() {
		return "GDB Proxy";
	}

	@Override
	public Image getImage() {
		return NesCDebugIcons.get(NesCDebugIcons.ICON_GDB_PROXY);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);

		setLaunchConfiguration(configuration);
		String proxyConfig = "";
		String proxyStartupDelay = "";
		try {
			proxyConfig = configuration.getAttribute(ITinyOSDebugLaunchConstants.ATTR_CURRENT_GDB_PROXY, "");
			loadProxyCombo(availableProxyCombos, proxyConfig);
			proxyStartupDelay = configuration.getAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_STARTUP_DELAY, "1500");
		} catch (CoreException ce) {
			TinyOSPlugin.getDefault().log("Exception while initializing GdbProxyTab", ce);
		}
		startupDelay.setText(proxyStartupDelay);

		setInitializing(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		IGdbProxyConfigurationTab gpc = getConfigForCurrentProxy();
		if(gpc != null) {
			boolean dirty = gpc.isDirty();
			gpc.performApply(configuration);
			if(dirty || isDirty()) {
				configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_COMMAND, gpc.getCommand().trim());
				configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_CURRENT_GDB_PROXY, gpc.getID());
			}
		} else {
			configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_COMMAND, "");
		}
		if(isDirty()) {
			configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_STARTUP_DELAY, startupDelay.getText().trim());
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_CURRENT_GDB_PROXY, "");
		IGdbProxyConfigurationTab gpc = getConfigForCurrentProxy();
		if(gpc != null) {
			gpc.setDefaults(configuration);
		}
		configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_COMMAND, "");
		configuration.setAttribute(ITinyOSDebugLaunchConstants.ATTR_GDB_PROXY_STARTUP_DELAY, "1500");
	}

}
