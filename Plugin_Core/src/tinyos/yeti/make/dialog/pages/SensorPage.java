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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.ISensorBoard;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class SensorPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private Composite sensorComposite;
    private Composite sensorboardItemComposite;
    private Group group2;
    private CLabel cLabel5;
    private Text text2;

    // holding all board buttons
    private HashMap<String,Button> boardButtons = new HashMap<String,Button>();

    private CustomizationControls customization;

    public SensorPage( boolean showCustomization ){
        super( "Sensorboard" );
        if( showCustomization ){
        	customization = new CustomizationControls();
        	customization.setPage( this );
        }
        setImage( NesCIcons.icons().get( NesCIcons.ICON_SENSOR ) );
    }
    
    public void setCustomEnabled( boolean enabled ){
	    for( Button button : boardButtons.values() ){	
	    	button.setEnabled( enabled );
	    }
	    contentChanged();
    }

    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ) {
        IPlatform platform = information.getSelectedPlatform();

        for( Button button : boardButtons.values() )
            button.dispose();

        boardButtons.clear();

        if( platform != null ){
            ISensorBoard[] boards = platform.getSensorboards();
            if( boards != null ){
                String[] selection = maketarget.getCustomBoards();
                Set<String> selectedBoards = new HashSet<String>();
                if( selection != null ){
                    for( String board : selection )
                        selectedBoards.add( board );
                }

                for( int i = 0; i < boards.length; i++ ){
                    Button button = createBoardButton( sensorboardItemComposite, boards[i].getName() );
                    button.setSelection( selectedBoards.contains( boards[i].getName() ));
                    boardButtons.put( boards[i].getName(), button );
                    button.addSelectionListener( new SelectionListener(){
                    	public void widgetDefaultSelected( SelectionEvent e ){
                    		contentChanged();
                    	}
                    	public void widgetSelected( SelectionEvent e ){
	                    	contentChanged();	
                    	}
                    });
                }
            }
        }
        
        sensorboardItemComposite.layout();
        sensorComposite.layout();
        
        if( customization != null ){
        	customization.setSelection( 
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.BOARDS ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.BOARDS ));
        	
        	StringBuilder builder = new StringBuilder();
        	String[] backup = maketarget.getBackupProperty( MakeTargetPropertyKey.BOARDS );
        	if( backup != null ){
        		for( int i = 0; i < backup.length; i++ ){
        			if( i > 0 )
        				builder.append( ", " );
        			builder.append( backup[i] );
        		}
        	}
        	customization.setDefaultValue( builder.toString() );
        }
    }

    private Button createBoardButton(Composite c, String text) {
        Button button1 = new Button(c,SWT.CHECK | SWT.LEFT);
        GridData button1LData = new GridData();
        button1LData.horizontalAlignment = SWT.FILL;
        button1LData.grabExcessHorizontalSpace = true;
        button1.setLayoutData(button1LData);
        button1.setText(text);

        return button1;
    }

    public void store( MakeTargetSkeleton maketarget ){
        List<String> selection = new ArrayList<String>();
        for( Map.Entry<String, Button> entry : boardButtons.entrySet() ){
            if( entry.getValue().getSelection() ){
                selection.add( entry.getKey() );
            }
        }
        maketarget.setCustomBoards( selection.toArray( new String[ selection.size() ]));
        
        if( customization != null ){
        	Selection custom = customization.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.BOARDS, custom.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.BOARDS, custom.isDefaults() );
        }
    }

    public void createControl(Composite parent) {
        sensorComposite = new Composite( parent, SWT.NONE);
        GridLayout sensorCompositeLayout = new GridLayout();
        sensorCompositeLayout.makeColumnsEqualWidth = true;
        sensorCompositeLayout.marginHeight = 0;
        sensorCompositeLayout.horizontalSpacing = 0;
        sensorCompositeLayout.marginWidth = 0;
        sensorCompositeLayout.marginLeft = 5;
        sensorCompositeLayout.marginRight = 5;
        sensorComposite.setLayout(sensorCompositeLayout);


        setControl( sensorComposite );

        if( customization != null ){
        	customization.createControl( sensorComposite, true );
        	customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        	
        	Label separator = new Label( sensorComposite, SWT.HORIZONTAL | SWT.SEPARATOR );
        	separator.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
        
        {
            sensorboardItemComposite = new Composite(
                    sensorComposite,
                    SWT.NONE);
            GridLayout composite2Layout = new GridLayout();
            composite2Layout.makeColumnsEqualWidth = true;
            sensorboardItemComposite.setLayout(composite2Layout);
        }
        {
            group2 = new Group(sensorComposite, SWT.NONE);
            group2.setVisible(false);
            GridLayout group2Layout = new GridLayout();
            group2Layout.numColumns = 2;
            group2.setLayout(group2Layout);
            group2.setText("Sensorboard Description");
            GridData group2LData = new GridData();
            group2LData.verticalAlignment = GridData.END;
            group2LData.horizontalAlignment = GridData.FILL;
            group2LData.grabExcessHorizontalSpace = true;
            group2LData.grabExcessVerticalSpace = true;
            group2.setLayoutData(group2LData);
            {
                cLabel5 = new CLabel(group2, SWT.SHADOW_OUT);
//              cLabel5.setImage(SWTResourceManager
//              .getImage("Image1.gif"));
                GridData cLabel5LData = new GridData();
                cLabel5LData.widthHint = 150;
                cLabel5LData.heightHint = 150;
                cLabel5.setLayoutData(cLabel5LData);
                cLabel5.setSize(150, 150);
            }
            {
                text2 = new Text(group2, SWT.MULTI| SWT.READ_ONLY| SWT.WRAP);
                // Text for sensorboard description
//              text2.setText("");
                GridData text2LData = new GridData();
                text2LData.verticalAlignment = GridData.FILL;
                text2LData.horizontalAlignment = GridData.FILL;
                text2LData.grabExcessHorizontalSpace = true;
                text2LData.grabExcessVerticalSpace = true;
                text2.setLayoutData(text2LData);
            }
        }
    }

}
