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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IMakeExtraDescription;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class ExtraPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private List<Extra> extras = new ArrayList<Extra>();

    private Composite extraItemComposite;
    private Composite extraParamComposite;

    private ScrolledComposite base;
    private Composite extraComposite;

    private CustomizationControls customizing;
    
    public ExtraPage( boolean showCustomizing ){
        super( "Extras" );
        if( showCustomizing ){
        	customizing = new CustomizationControls();
        	customizing.setPage( this );
        }
        setImage( NesCIcons.icons().get( NesCIcons.ICON_EXTRAS ) );
    }
    
    public void setCustomEnabled( boolean enabled ){
	    for( Extra extra : extras ){	
	    	extra.setCustomEnabled( enabled );
	    }
	    contentChanged();
    }

    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        for( Extra extra : extras )
            extra.dispose();

        extras.clear();

        IPlatform platform = information.getSelectedPlatform();

        if( platform != null ){
        	IMakeExtraDescription[] extras = platform.getExtras();
        	if( extras != null ){
        		for( IMakeExtraDescription makeExtra : extras ){
        			Extra extra = new Extra( makeExtra );
        			extra.create( extraItemComposite );
        			extra.createInput( extraParamComposite );
        			this.extras.add( extra );
        		}

        		MakeExtra[] selectedExtras = maketarget.getCustomMakeExtras();
        		if( selectedExtras != null ){
        			Map<String, Extra> map = new HashMap<String, Extra>();
        			for( Extra extra : this.extras )
        				map.put( extra.getDescription().getName(), extra );

        			for( MakeExtra selected : selectedExtras ){
        				Extra extra = map.get( selected.getName() );
        				if( extra != null ){
        					extra.select( selected );
        				}
        			}
        		}
        	}
        }

        layoutExtraComposite();
        
        if( customizing != null ){
        	customizing.setSelection(
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.MAKE_EXTRAS ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.MAKE_EXTRAS ));
        	
        	MakeExtra[] defaults = maketarget.getBackupProperty( MakeTargetPropertyKey.MAKE_EXTRAS );
        	StringBuilder builder = new StringBuilder();
        	if( defaults != null ){
        		for( int i = 0; i < defaults.length; i++ ){
        			if( i > 0 )
        				builder.append( ", " );
        			builder.append( defaults[i].getName() );
        		}
        	}
        	customizing.setDefaultValue( builder.toString() );
        }
    }

    private Button createExtrabutton( Composite c, String text ) {
        Button extra = new Button( c,SWT.CHECK | SWT.LEFT);
        GridData extra1LData = new GridData();
        extra1LData.grabExcessHorizontalSpace = true;
        extra.setLayoutData(extra1LData);
        extra.setText(text);
        return extra;
    }

    public void store( MakeTargetSkeleton maketarget ){
        List<MakeExtra> result = new ArrayList<MakeExtra>();
        for( Extra extra : extras ){
            if( extra.isSelected() ){
                extra.store();
                result.add( extra.getExtra() );
            }
        }
        maketarget.setCustomMakeExtras( result.toArray( new MakeExtra[ result.size() ] ) );
        
        if( customizing != null ){
        	Selection selection = customizing.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.MAKE_EXTRAS, selection.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.MAKE_EXTRAS, selection.isDefaults() );
        }
    }

    public void createControl(Composite parent) {
        base = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
        base.setAlwaysShowScrollBars( false );
        base.setExpandHorizontal( true );
        base.setExpandVertical( true );
        
        extraComposite = new Composite( base, SWT.NONE );
        base.setContent( extraComposite );
        
        GridLayout extraCompositeLayout = new GridLayout();
        extraCompositeLayout.makeColumnsEqualWidth = true;
        extraCompositeLayout.marginHeight = 0;
        extraCompositeLayout.horizontalSpacing = 0;
        extraCompositeLayout.marginWidth = 0;
        extraCompositeLayout.marginLeft = 5;
        extraCompositeLayout.marginRight = 5;
        extraComposite.setLayout(extraCompositeLayout);

        setControl( base );
        
        if( customizing != null ){
        	customizing.createControl( extraComposite, true );
        	customizing.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        	
        	Label separator = new Label( extraComposite, SWT.SEPARATOR | SWT.HORIZONTAL );
        	separator.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
        {
            extraItemComposite = new Composite(extraComposite,	SWT.NONE);
            extraItemComposite.setLayout( new GridLayout( 3, true ) );
            
            GridData extraItemCompositeLData = new GridData();
            extraItemCompositeLData.grabExcessHorizontalSpace = true;
            extraItemCompositeLData.horizontalAlignment = GridData.FILL;
            extraItemComposite.setLayoutData(extraItemCompositeLData);
        }
        {
            Label label4 = new Label(extraComposite, SWT.SEPARATOR
                    | SWT.HORIZONTAL);
//          label4.setText("label1");
            GridData label4LData = new GridData();
            label4LData.verticalAlignment = GridData.END;
            label4LData.horizontalAlignment = GridData.FILL;
            label4LData.heightHint = 30;
            label4LData.grabExcessHorizontalSpace = true;
            label4.setLayoutData(label4LData);
            label4.setBounds(0, 209, 60, 30);
        }
        {
            extraParamComposite = new Composite(extraComposite,	SWT.NONE);
            GridLayout makeExtraParamCompositeLayout = new GridLayout();
            makeExtraParamCompositeLayout.makeColumnsEqualWidth = true;
            makeExtraParamCompositeLayout.numColumns = 3;
            GridData makeExtraParamCompositeLData = new GridData();
            makeExtraParamCompositeLData.grabExcessHorizontalSpace = true;
            makeExtraParamCompositeLData.verticalAlignment = GridData.END;
            makeExtraParamCompositeLData.horizontalAlignment = GridData.FILL;
            extraParamComposite.setLayoutData(makeExtraParamCompositeLData);
            extraParamComposite.setLayout(makeExtraParamCompositeLayout);
        }
        
        layoutExtraComposite();
    }

    private Text createExtraText( Composite c, String text ){
        Text textWidget = new Text(c,SWT.BORDER);
        
        if( text == null ){
            textWidget.setText("");
        }
        else{
            textWidget.setText( text );
        }
        
        textWidget.addModifyListener( new ModifyListener(){
        	public void modifyText( ModifyEvent e ){
	        	contentChanged();	
        	}
        });
        
        return textWidget;
    }

    private Label createExtraLabel( Composite c, String text ){
        Label label5 = new Label(c,SWT.NONE);
        label5.setText(text);
        return label5;
    }

    private void layoutExtraComposite(){
    	extraItemComposite.layout();
    	extraParamComposite.layout();
        extraComposite.layout();
        base.setMinSize( extraComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    }
    
    private class Extra implements ICustomizeablePage{
        private Button buttonSelected;
        private Button buttonAskCompiling;
        private Label label;
        private Text parameter;

        private IMakeExtraDescription description;
        private MakeExtra extra;
        
        public Extra( IMakeExtraDescription description ){
            this.description = description;
        }
        
        public void setCustomEnabled( boolean enabled ){
	        if( buttonSelected != null )
	        	buttonSelected.setEnabled( enabled );
	        
	        if( buttonAskCompiling != null )
	        	buttonAskCompiling.setEnabled( enabled );
	        
	        if( label != null )
	        	label.setEnabled( enabled );
	        
	        if( parameter != null )
	        	parameter.setEnabled( enabled );
        }
        
        public IMakeExtraDescription getDescription(){
			return description;
		}

        public MakeExtra getExtra(){
			return extra;
		}
        
        public boolean isSelected(){
            return buttonSelected.getSelection();
        }

        public void dispose(){
            if( buttonSelected != null ){
                buttonSelected.dispose();
                buttonSelected = null;
            }
            disposeInput();
        }

        public void disposeInput(){
            if( buttonAskCompiling != null ){
                buttonAskCompiling.dispose();
                buttonAskCompiling = null;
            }
            if( label != null ){
                label.dispose();
                label = null;
            }
            if( parameter != null ){
                parameter.dispose();
                parameter = null;
            }
        }

        public void create( Composite parent ){
            buttonSelected = createExtrabutton( parent, description.getName() );
            buttonSelected.setToolTipText( description.getDescription() );
            buttonSelected.addSelectionListener( new SelectionListener(){
                public void widgetDefaultSelected( SelectionEvent e ) {
                    // ignore
                }

                public void widgetSelected( SelectionEvent e ) {
                    setExtraInputVisible( buttonSelected.getSelection() );
                    contentChanged();
                }
            });
        }

        public void select( MakeExtra original ){
        	this.extra = original;
        	
            buttonSelected.setSelection( true );
            setExtraInputVisible( true );
            
            if( parameter != null ){
            	String value = original.getParameterValue();
                parameter.setText( value == null ? "" : value );
            }
            
            if( buttonAskCompiling != null )
                buttonAskCompiling.setSelection( original.askParameterAtCompileTime() );
        }

        private void setExtraInputVisible( boolean visible ){
        	if( extra == null )
        		extra = new MakeExtra( description );
        	
            if( label != null ){
                GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
                data.exclude = !visible;
                label.setVisible( visible );
                label.setLayoutData( data );
            }

            if( parameter != null ){
                GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
                data.exclude = !visible;
                parameter.setVisible( visible );
                parameter.setLayoutData( data );
            }

            if( buttonAskCompiling != null ){
                GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
                data.exclude = !visible;
                buttonAskCompiling.setVisible( visible );
                buttonAskCompiling.setLayoutData( data );
            }

            layoutExtraComposite();
        }

        public void createInput( Composite parent ){
            if( description.hasParameter() ){
            	if( extra == null ){
            		extra = new MakeExtra( description );
            	}
            	
                label = createExtraLabel( parent, description.getName() + " - " + description.getParameterName() );
                label.setToolTipText( description.getParameterDescription() );

                parameter = createExtraText( parent, extra.getParameterValue() );

                buttonAskCompiling = new Button( parent, SWT.CHECK );
                buttonAskCompiling.setText("Ask");
                buttonAskCompiling.setToolTipText("Ask for parameter before compiling");
                buttonAskCompiling.setSelection( extra.askParameterAtCompileTime() );
                buttonAskCompiling.addSelectionListener( new SelectionAdapter(){
                	@Override
                	public void widgetSelected( SelectionEvent e ){
                		contentChanged();
                	}
                });

                setExtraInputVisible( false );
            }
        }
        
        public void store(){
            if( extra.hasParameter() ){
                extra.setAskParameterAtCompileTime( buttonAskCompiling.getSelection() );
                extra.setParameterValue( parameter.getText() );
            }
        }
    }
}
