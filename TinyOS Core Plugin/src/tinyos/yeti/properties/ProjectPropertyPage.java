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
package tinyos.yeti.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import tinyos.yeti.EnvironmentManager;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;

public class ProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    public static final String ENV_PROPERTY = "TOSENV";

    private Label descr;
    private Combo combo;
    private IEnvironment envs[];

    public ProjectPropertyPage() {
        super();
    }

    private String getSaved() {
        try {
            String s = ((IResource) getElement()).getPersistentProperty( new QualifiedName("", ENV_PROPERTY));

            return s;
        } catch (CoreException e) {

        }
        return null;
    }

    @Override
    protected Control createContents(Composite parent) {
        String savedId = getSaved();

        Composite n = new Composite(parent,SWT.NONE);
        n.setLayout(new RowLayout(SWT.VERTICAL));
        Label label = new Label(n,SWT.NONE);
        label.setText("Select the TinyOS-Environment:");

        envs = TinyOSPlugin.getDefault().getEnvironments().getEnvironmentsArray();
        combo = new Combo(n,SWT.SINGLE | SWT.READ_ONLY);

        if (envs.length==0) {
            combo.add("no environment available");
        } else {
            for (int i = 0; i < envs.length; i++) {
                IEnvironment e = envs[i];
                combo.add(e.getEnvironmentName());
            }

            IEnvironment selected = null;
            if ((savedId!=null)&&(savedId!="")) {
                selected = EnvironmentManager.getDefault().getEnvironment( savedId );
            }
            if (selected !=null) {
                descr = new Label(n,SWT.NONE);
                descr.setText(selected.getEnvironmentDescription());

                String[] items = combo.getItems();
                for (int i = 0; i < items.length; i++) {
                    if (items[i].equals(selected.getEnvironmentName())) {
                        combo.select(i);
                        break;
                    }
                }
            }
            else {
                descr = new Label(n,SWT.NONE);
                IEnvironment defaultEnvironment = EnvironmentManager.getDefault().getDefaultEnvironment();
                for( int i = 0; i<envs.length; i++ ){
                    if( envs[i] == defaultEnvironment ){
                        descr.setText( defaultEnvironment.getEnvironmentDescription() );
                        combo.select( i );
                        break;
                    }
                }
            }
            combo.addSelectionListener(new SelectionListener(){

                public void widgetSelected(SelectionEvent e) {
                    descr.setText(envs[combo.getSelectionIndex()].getEnvironmentDescription());
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });


        }

        return n;
    }

    @Override
    public boolean performOk() {
    	try{
    		EnvironmentManager.getDefault().setEnvironment( ((IResource)getElement()).getProject(), envs[ combo.getSelectionIndex() ] );
    		return true;
    	}
    	catch( CoreException ex ){
    		TinyOSPlugin.log( ex );
    		return false;
    	}
    }
}
