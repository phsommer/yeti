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
package tinyos.yeti.editors.nesc.information;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.editors.nesc.NesCDocPresenter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.INesCDocComment;
import tinyos.yeti.utility.Icon;

public class TextInformationControl extends AbstractInformationControl{
	public static class StringFactory implements INesCInformationControlFactory<String>{
		public INesCInformationControl create( Composite parent, String input, INesCInformationControlOwner owner ){
			return new TextInformationControl( parent, null, null, input, null, owner );
		}
	}
	
	public static class HoverFactory implements INesCInformationControlFactory<IHoverInformation>{
		public INesCInformationControl create( Composite parent, IHoverInformation input, INesCInformationControlOwner owner ){
			return new TextInformationControl( parent, input.getIcon(), input.getTitle(), input.getContent(), input.getContentPresentation(), owner );
		}
	}
	
	public static class NodeFactory implements INesCInformationControlFactory<IASTModelNode>{
		public INesCInformationControl create( Composite parent, IASTModelNode input, INesCInformationControlOwner owner ){
			INesCDocComment comment = input.getDocumentation();
			
			String text = null;
			TextPresentation presentation = null;
			
			if( comment != null ){
				NesCDocPresenter presenter = new NesCDocPresenter();
				presentation = new TextPresentation();
				text = presenter.updatePresentation( comment.getComment(), presentation );
			}
			
			return new TextInformationControl( parent, new Icon( input ), input.getLabel(), text, presentation, owner );
		}
	}
	
	public TextInformationControl( Composite parent, Icon icon, String title, String text, TextPresentation presentation, INesCInformationControlOwner owner ){
		super( owner );
		createStringInformation( parent, icon, title, text, presentation );
		setColorAndFont( parent );
	}
	
	public void dispose(){
		// ignore	
	}
	
	private void createStringInformation( Composite parent, Icon icon, String title, String text, TextPresentation presentation ){
		Composite textParent = parent;
		Image image = null;
		if( icon != null ){
			image = icon.getImage( true, true );
		}
		
		if( image != null || title != null ){
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			GridLayout layout= new GridLayout( 1, false );
			layout.marginBottom = 2;
			layout.marginRight = 2;
			layout.horizontalSpacing = 0;
			
			composite.setLayout( layout );
			
			Composite titleComposite;
			if( image != null && title != null ){
				titleComposite = new Composite( composite, SWT.NONE );
				GridLayout titleLayout = new GridLayout( 2, false );
				titleLayout.marginHeight = 1;
				titleLayout.marginWidth = 0;
				titleComposite.setLayout( titleLayout );
			}
			else{
				titleComposite = composite;
			}
			
			Label iconLabel = null;
			if( image != null ){
				iconLabel = new Label( titleComposite, SWT.NO_FOCUS );
				iconLabel.setImage( image );
				iconLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.TOP, false, false ) );
			}
			
			StyledText titleLabel = null;
			if( title != null ){
				titleLabel = new StyledText( titleComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY );
				titleLabel.setText( title );
				titleLabel.setStyleRange( new StyleRange( 0, title.length(), null, null, SWT.BOLD ) );
				titleLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.BEGINNING, false, true ) );
			}
			
			textParent = composite;
		}
		
		if( text != null ){
			StyledText stext= new StyledText( textParent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY );
			GridData data= new GridData(SWT.FILL, SWT.FILL, true, true);
			stext.setLayoutData(data);
			stext.setText( text );
			if( presentation != null ){
				TextPresentation.applyTextPresentation( presentation, stext );
			}
		}
	}
}
