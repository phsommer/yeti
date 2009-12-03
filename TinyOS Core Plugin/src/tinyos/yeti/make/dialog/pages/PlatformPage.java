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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class PlatformPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    // holding all platform buttons
    private HashMap<String,Button> platformButtons = new HashMap<String,Button>();

    private Composite platformItemContainer;

    private Group group1;
    private Text text1;
    private CLabel cLabel4;
    
    private CustomizationControls customization;

    private MakeTargetSkeleton maketarget;
    private IMakeTargetInformation information;
    
    public PlatformPage( boolean showCustomization ){
        super( "Platform" );
        if( showCustomization ){
        	customization = new CustomizationControls();
        	customization.setPage( this );
        }
        setImage( NesCIcons.icons().get( NesCIcons.ICON_PLATFORM ) );
    }
    
    public void setCustomEnabled( boolean enabled ){
	    for( Button button : platformButtons.values() ){
	    	button.setEnabled( enabled );
	    }
	    checkButtons();
	    contentChanged();
    }

    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
    	this.maketarget = maketarget;
    	this.information = information;
    	
        IPlatform[] platforms = information.getPlatforms();

        for( Button button : platformButtons.values() ){
            button.dispose();
        }

        platformButtons.clear();

        if( platforms != null ){
        	for (int i = 0; i < platforms.length; i++) {
        		IPlatform platform = platforms[i];
        		Button button = createRadioButton( platformItemContainer, platform.getName() );

        		button.setSelection( platform.getName().equals( maketarget.getCustomTarget() ) );

        		platformButtons.put( platform.getName(), button );

        		button.addSelectionListener( new SelectionListener(){
        			public void widgetDefaultSelected( SelectionEvent e ){
        				checkButtons();  
        				contentChanged();
        			}
        			public void widgetSelected( SelectionEvent e ){
        				checkButtons();
        				contentChanged();
        			}
        		});
        	}
        }

        platformItemContainer.layout();
        checkButtons();
        
        if( customization != null ){
        	String backup = maketarget.getBackupProperty( MakeTargetPropertyKey.TARGET );
        	if( backup == null )
        		customization.setDefaultValue( "<not specified>" );
        	else
        		customization.setDefaultValue( backup );
        	
        	customization.setSelection(
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.TARGET ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.TARGET ));
        }
    }
    
    @Override
    public void check( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        IPlatform[] platforms = information.getPlatforms();
        if( platforms == null || platforms.length == 0 ){
        	setError( "No platforms found. Perhaps due to invalid setup. Check the paths in 'Window > Preferences > TinyOS > Environments > ...'" );
        	return;
        }
        
        boolean found = false;
        String target = maketarget.getTarget();
        for( IPlatform platform : platforms ){
            if( platform.getName().equals( target )){
                found = true;
                break;
            }
        }
        
        if( found ){
            setDefaultMessage();
        }
        else{
            setError( "Must select a platform" );
        }
    }
    
    private void checkButtons(){
    	if( platformButtons.size() == 0 ){
        	setError( "No platforms found. Perhaps due to invalid setup. Check the paths in 'Window > Preferences > TinyOS > Environments > ...'" );
        	return;
        }
        
        if( customization != null && customization.getSelection() == Selection.DEFAULT ){
    		String target = maketarget.getBackupProperty( MakeTargetPropertyKey.TARGET );
    		if( target == null || target.equals( "" )){
    			setError( "Must select a platform (missing in default settings)" );
    			return;
    		}
    		else{
    			IPlatform[] platforms = information.getPlatforms();
    			boolean found = false;
    			for( IPlatform platform : platforms ){
    				if( platform.getName().equals( target )){
    					found = true;
    					break;
    				}
    			}
    			if( !found ){
    				setError( "Invalid platform selected (defaults settings are invalid)" );
    				return;
    			}
    			
    			setDefaultMessage();
    		}
        }
        else{
        	for( Button button : platformButtons.values() ){
        		if( button.getSelection() ){
        			setDefaultMessage();
        			return;
        		}
        	}
        
        	setError( "Must select a platform" );
        }
    }

    public void store( MakeTargetSkeleton maketarget ){
        for( Map.Entry<String, Button> selection : platformButtons.entrySet() ){
            if( selection.getValue().getSelection() ){
                maketarget.setCustomTarget( selection.getKey() );
                break;
            }
        }
        
        if( customization != null ){
        	Selection selection = customization.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.TARGET, selection.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.TARGET, selection.isDefaults() );
        }
    }

    private Button createRadioButton(Composite c, String text) {
        Button button1 = new Button(c,SWT.RADIO | SWT.LEFT);
        GridData button1LData = new GridData();
        button1LData.horizontalAlignment = SWT.FILL;
        button1LData.grabExcessHorizontalSpace = true;
        button1.setLayoutData(button1LData);
        button1.setText(text);

        return button1;
    }


    protected void setPlatformDescription(IPlatform p) {
        ImageDescriptor im = p.getImage();

        String descr = p.getDescription();

        if( descr==null ){
            group1.setVisible(false);
        } 
        else {
            group1.setVisible( true );
            text1.setText( descr );
        }

        if( im != null ){
            // Dispose of the allocated space of the image
            Image m = cLabel4.getImage();
            if( m !=null )
                m.dispose();

            m = im.createImage(true);

            m.setBackground(new Color(Display.getCurrent(),200,200,200));
            cLabel4.setImage(m);
            cLabel4.setSize(150,150);
            cLabel4.pack();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        Image m = cLabel4.getImage();
        if( m !=null )
            m.dispose();
    }

    public void createControl( Composite parent ){
        Composite platformComposite = new Composite( parent, SWT.NONE );
        GridLayout platformCompositeLayout = new GridLayout();
        platformCompositeLayout.makeColumnsEqualWidth = true;
        platformCompositeLayout.marginHeight = 0;
        platformCompositeLayout.horizontalSpacing = 0;
        platformCompositeLayout.marginLeft = 5;
        platformCompositeLayout.marginWidth = 0;
        platformCompositeLayout.marginRight = 5;

        platformComposite.setLayout(platformCompositeLayout);

        setControl( platformComposite );

        if( customization != null ){
        	customization.createControl( platformComposite, false );
        	customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ));
        	
        	Label separator = new Label( platformComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        	separator.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
        
        {
            platformItemContainer = new Composite(platformComposite,SWT.NONE);
            GridLayout platformItemContainerLayout = new GridLayout();
            platformItemContainerLayout.numColumns = 3;
            platformItemContainerLayout.horizontalSpacing = 20;
            GridData platformItemContainerLData = new GridData();
            platformItemContainerLData.grabExcessHorizontalSpace = true;
            platformItemContainerLData.horizontalAlignment = GridData.FILL;
            platformItemContainerLData.verticalAlignment = GridData.BEGINNING;
            platformItemContainer.setLayoutData(platformItemContainerLData);
            platformItemContainer.setLayout(platformItemContainerLayout);
        }
        {
            group1 = new Group(platformComposite, SWT.NONE);
            GridLayout group1Layout = new GridLayout();
            group1Layout.numColumns = 2;
            group1.setLayout(group1Layout);
            GridData group1LData = new GridData();
            group1LData.grabExcessVerticalSpace = true;
            group1LData.grabExcessHorizontalSpace = true;
            group1LData.horizontalAlignment = GridData.FILL;
            group1LData.verticalAlignment = GridData.END;
            group1LData.grabExcessHorizontalSpace = true;
            group1LData.horizontalAlignment = GridData.FILL;
            group1.setLayoutData(group1LData);
            group1.setVisible(false);
            group1.setText("Platform Description");
            {
                cLabel4 = new CLabel(group1, SWT.SHADOW_OUT | SWT.SHADOW_IN | SWT.SHADOW_ETCHED_IN | SWT.SHADOW_ETCHED_OUT);
                cLabel4.setBackground(new Color(Display.getCurrent(),255,255,255));		
//              cLabel4.setImage(SWTResourceManager.getImage("Image1.gif"));
                GridData cLabel4LData = new GridData();
                cLabel4LData.widthHint = 150;
                cLabel4LData.heightHint = 150;
                cLabel4.setLayoutData(cLabel4LData);
                cLabel4.setSize(150, 150);
            }
            {
                // Platformdescription text
                text1 = new Text(group1, SWT.MULTI| SWT.READ_ONLY| SWT.WRAP);
                GridData text1LData = new GridData();
                text1LData.grabExcessVerticalSpace = true;
                text1LData.grabExcessHorizontalSpace = true;
                text1LData.verticalAlignment = GridData.FILL;
                text1LData.horizontalAlignment = GridData.FILL;
                text1.setLayoutData(text1LData);
//              text1.setText("text1dfg sdfgs �dgj� sdflgjk s�dlfkgj sdlkfgj sdlfkgjsd�lfk gjdlfks gdf");
            }
        }
    }

}
