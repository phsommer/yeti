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
package tinyos.yeti.editors.nesc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.text.IInformationControlExtension5;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import tinyos.yeti.editors.nesc.information.AnnotationInformationControl;
import tinyos.yeti.editors.nesc.information.BrowserInformationControl;
import tinyos.yeti.editors.nesc.information.INesCInformationControl;
import tinyos.yeti.editors.nesc.information.INesCInformationControlFactory;
import tinyos.yeti.editors.nesc.information.INesCInformationControlOwner;
import tinyos.yeti.editors.nesc.information.QuickfixInformationControl;
import tinyos.yeti.editors.nesc.information.TextInformationControl;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IHoverInformation;

/**
 * This {@link IInformationControl} is able to show {@link String}s, 
 * {@link Annotation}s, {@link IQuickFixInformation}s, {@link IHoverInformation} and {@link IASTModelNode}s.
 */
public class NesCInformationControl extends AbstractInformationControl implements IInformationControl, IInformationControlExtension2, IInformationControlExtension3, IInformationControlExtension5 {
	private Control focusControl;
	private Object input;
	private Composite controlParent;
	private IInformationControlCreator creator;
	
	private Map<Class<?>, INesCInformationControlFactory<?>> factories = new HashMap<Class<?>, INesCInformationControlFactory<?>>();
	private INesCInformationControl informationControl;
	
	public NesCInformationControl( Shell parentShell, IInformationControlCreator creator ) {
		super( parentShell, true );
		this.creator = creator;
		
		factories.put( Annotation.class, new AnnotationInformationControl.Factory() );
		factories.put( IQuickFixInformation.class, new QuickfixInformationControl.Factory() );
		factories.put( String.class, new TextInformationControl.StringFactory() );
		
		if( BrowserInformationControl.isAvailable( parentShell )){
			factories.put( IHoverInformation.class, new BrowserInformationControl.HoverFactory() );
			factories.put( IASTModelNode.class, new BrowserInformationControl.NodeFactory() );
		}
		else{
			factories.put( IHoverInformation.class, new TextInformationControl.HoverFactory() );
			factories.put( IASTModelNode.class, new TextInformationControl.NodeFactory() );
		}
		
		create();
	}
	
	@Override
	public void setSize( int width, int height ){
		super.setSize( width, height );
		getShell().layout( true, true );
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator(){
		return creator;
	}

	@Override
	public void setInformation(String information) {
		//replaced by IInformationControlExtension2#setInput
	}

	public void setInput( Object input ){
		this.input = input;
		disposeDeferredCreatedContent();
		deferredCreateContent();
	}


	public boolean hasContents() {
		return input != null;
	}

	@Override
	public void setFocus() {
		if (focusControl != null)
			focusControl.setFocus();
	}

	@Override
	public void setVisible(boolean visible) {
		if( !visible )
			disposeDeferredCreatedContent();
		super.setVisible( visible );
	}

	protected void disposeDeferredCreatedContent() {
		if( informationControl != null ){
			informationControl.dispose();
			informationControl = null;
		}
		
		Control[] children= controlParent.getChildren();
		for (int i= 0; i < children.length; i++) {
			children[i].dispose();
		}
	}

	@Override
	protected void createContent( Composite parent ){
		controlParent = parent;
		GridLayout layout= new GridLayout(1, false);
		layout.verticalSpacing= 0;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		controlParent.setLayout(layout);
	}

	@Override
	public Point computeSizeHint() {
		if( informationControl != null )
			return informationControl.computeSizeHint();
		
		Point preferedSize= getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		Point constrains= getSizeConstraints();
		if (constrains == null)
			return preferedSize;

		Point constrainedSize= getShell().computeSize(constrains.x, SWT.DEFAULT, true);

		int width= Math.min(preferedSize.x, constrainedSize.x);
		int height= Math.max(preferedSize.y, constrainedSize.y);

		return new Point(width, height);
	}

	@SuppressWarnings( "unchecked" )
	protected <E> INesCInformationControlFactory<? super E> getFactoryFor( Class<E> type ){
		Set<Class<?>> checked = new HashSet<Class<?>>();
		INesCInformationControlFactory<?> result = getFactoryFor( type, checked );
		return (INesCInformationControlFactory<? super E>)result;
	}
	
	protected INesCInformationControlFactory<?> getFactoryFor( Class<?> type, Set<Class<?>> checked ){
		if( checked.add( type )){
			INesCInformationControlFactory<?> factory = factories.get( type );
			if( factory != null )
				return factory;
			
		    Class<?>[] interfazes = type.getInterfaces();
		    if( interfazes != null ){
		    	for( Class<?> interfaze : interfazes ){
		    		factory = getFactoryFor( interfaze, checked );
		    		if( factory != null )
		    			return factory;
		    	}
		    }
		    
		    type = type.getSuperclass();
		    if( type != null ){
		    	return getFactoryFor( type, checked );
		    }
		}
		return null;
	}
	
	/**
	 * Create content of the hover. This is called after
	 * the input has been set.
	 */
	@SuppressWarnings( "unchecked" )
	protected void deferredCreateContent() {
		if( input == null )
			return;
		
	    INesCInformationControlFactory<Object> factory = (INesCInformationControlFactory<Object>)getFactoryFor( input.getClass() );
	    if( factory == null )
	    	return;
	    
	    informationControl = factory.create( controlParent, input, new INesCInformationControlOwner(){
			public Point getSizeConstraints(){
				return NesCInformationControl.this.getSizeConstraints();
			}
			public void setFocus( Control control ){
				focusControl = control;
			}
			public Shell getShell(){
				return NesCInformationControl.this.getShell();
			}
		});
		controlParent.layout(true);
	}
}
