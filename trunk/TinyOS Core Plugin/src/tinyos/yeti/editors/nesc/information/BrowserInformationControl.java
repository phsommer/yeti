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

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.editors.nesc.NesCDocPresenter;
import tinyos.yeti.editors.nesc.information.html.HTML2TextReader;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IHoverInformation;
import tinyos.yeti.ep.parser.INesCDocComment;
import tinyos.yeti.utility.Icon;

/**
 * An information control displaying HTML text.
 * @author Benjamin Sigg
 * @deprecated for now this control is not in use as it requires some additional attention.
 */
public class BrowserInformationControl extends AbstractInformationControl{
	public static class HoverFactory implements INesCInformationControlFactory<IHoverInformation>{
		public INesCInformationControl create( Composite parent, IHoverInformation input, INesCInformationControlOwner owner ){
			String html = input.getHTML();
			if( html == null ){
				return new TextInformationControl( parent, input.getIcon(), input.getTitle(), input.getContent(), input.getContentPresentation(), owner );
			}
			else{
				return new BrowserInformationControl( parent, input.getIcon(), input.getTitle(), html, owner );
			}
		}
	}
	
	public static class NodeFactory implements INesCInformationControlFactory<IASTModelNode>{
		public INesCInformationControl create( Composite parent,
				IASTModelNode input, INesCInformationControlOwner owner ){
			
			INesCDocComment comment = input.getDocumentation();
			
			String text = null;
			
			if( comment != null ){
				NesCDocPresenter presenter = new NesCDocPresenter();
				text = presenter.toHTML( comment.getComment() );
			}
			
			return new BrowserInformationControl( parent, new Icon( input ), input.getLabel(), text, owner );
		}
	}
	
	/**
	 * Tells whether the SWT Browser widget and hence this information
	 * control is available.
	 *
	 * @param parent the parent component used for checking or <code>null</code> if none
	 * @return <code>true</code> if this control is available
	 */
	public static boolean isAvailable(Composite parent) {
		if (!fgAvailabilityChecked) {
			try {
				Browser browser= new Browser(parent, SWT.NONE);
				browser.dispose();
				fgIsAvailable= true;
			} catch (SWTError er) {
				fgIsAvailable= false;
			} finally {
				fgAvailabilityChecked= true;
			}
		}

		return fgIsAvailable;
	}
	
	// disable browser for now
	private static boolean fgIsAvailable= false;
	// private static boolean fgAvailabilityChecked = false;
	private static boolean fgAvailabilityChecked = true;
	
	private Composite headComposite;
	private Composite titleComposite;
	
	private TextLayout textLayout;
	private TextStyle boldStyle;
	private Browser browser;
	private String input;
	private Point sizeHint = null;
	
	public BrowserInformationControl( Composite parent, Icon icon, String title, String html, INesCInformationControlOwner owner ){
		super( owner );
		this.input = html;
		createMarkupInformation( parent, icon, title, html != null );
		setColorAndFont( parent );
		
		if( html != null ){
			browser.setText( finalize( html, parent.getBackground(), JFaceResources.getDialogFont() ) );
		}
		
		computeSizeHint();
	}
	
	public void dispose(){
		if( textLayout != null ){
			textLayout.dispose();
			textLayout = null;
		}
	}
	/**
	 * Gets the {@link TextLayout} used for calculating the size hint of this
	 * control.
	 */
	private TextLayout getTextLayout() {
		if( textLayout == null ){
			textLayout = new TextLayout(browser.getDisplay());

			// Initialize fonts
			Font font = browser.getFont();
			
			textLayout.setFont(font);
			textLayout.setWidth(-1);
			FontData[] fontData = font.getFontData();
			for (int i = 0; i < fontData.length; i++)
				fontData[i].setStyle(SWT.BOLD);
			font = new Font( owner.getShell().getDisplay(), fontData );
			boldStyle = new TextStyle(font, null, null);

			// Compute and set tab width
			textLayout.setText("    ");
			int tabWidth = textLayout.getBounds().width;
			textLayout.setTabs(new int[] {tabWidth});

			textLayout.setText("");
		}
		return textLayout;
	}
	
	
	
	@Override
	public Point computeSizeHint() {
		if( input == null )
			return super.computeSizeHint();
		
		if( sizeHint == null ){
			Point constraints = owner.getSizeConstraints();
			int maxWidth = constraints.x;
			int maxHeight = constraints.y;
			
			Point text = computeMinimalTextSize();
			
			int width = text.x;
			int height = text.y;
			
			int deltaWidth = 15;
			int deltaHeight = 25;
			
			if( titleComposite != null ){
			    Point size = titleComposite.computeSize( SWT.DEFAULT, SWT.DEFAULT );
			    deltaWidth = size.x + 5;
			    deltaHeight = size.y + 5;
			}
			
			width += deltaWidth;
			height += deltaHeight;
			
			// Apply size constraints
			if( maxWidth != SWT.DEFAULT )
				width = Math.min( maxWidth, width );
			if( maxHeight != SWT.DEFAULT )
				height = Math.min( maxHeight, height );
	
			// Ensure minimal size
			width = Math.max( 80, width);
			height = Math.max( 80, height);
	
			GridData data = new GridData( SWT.FILL, SWT.FILL, true, true );
			data.minimumWidth = width - deltaWidth;
			data.minimumHeight = height - deltaHeight;
			browser.setLayoutData( data );
			
			sizeHint = new Point( width, height );
		}
		return sizeHint;
	}
	
	private Point computeMinimalTextSize(){
		TextLayout textLayout = getTextLayout();

		Point constraints = owner.getSizeConstraints();
		int maxWidth = constraints.x;
		
		TextPresentation presentation = new TextPresentation();
		HTML2TextReader reader = new HTML2TextReader( new StringReader( input ), presentation );
		String text;
		try {
			text = reader.getString();
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		}

		textLayout.setText(text);
		if( maxWidth != SWT.DEFAULT )
			textLayout.setWidth( maxWidth );
		else
			textLayout.setWidth( -1 );
		
		Iterator<?> iter = presentation.getAllStyleRangeIterator();
		while (iter.hasNext()) {
			StyleRange sr = (StyleRange) iter.next();
			if (sr.fontStyle == SWT.BOLD)
				textLayout.setStyle(boldStyle, sr.start, sr.start + sr.length - 1);
		}

		int lineCount= textLayout.getLineCount();
		int textWidth= 0;
		int textHeight = 0;
		
		for (int i= 0; i < lineCount; i++) {
			Rectangle rect = textLayout.getLineBounds(i);
			int lineWidth = rect.x + rect.width;
			textWidth = Math.max( textWidth, lineWidth );
			textHeight = Math.max( textHeight, rect.y + rect.height );
		}
		
		return new Point( textWidth, textHeight );
	}
	
	private void createMarkupInformation( Composite parent, Icon icon, String title, boolean html ){
		Composite textParent = parent;
		Image image = null;
		if( icon != null ){
			image = icon.getImage( true, true );
		}
		
		if( image != null || title != null ){
			headComposite= new Composite(parent, SWT.NONE);
			headComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			GridLayout layout= new GridLayout( 1, false );
			layout.marginBottom = 2;
			layout.marginRight = 2;
			layout.horizontalSpacing = 0;
			
			headComposite.setLayout( layout );
			
			if( image != null && title != null ){
				titleComposite = new Composite( headComposite, SWT.NONE );
				GridLayout titleLayout = new GridLayout( 2, false );
				titleLayout.marginHeight = 1;
				titleLayout.marginWidth = 0;
				titleComposite.setLayout( titleLayout );
			}
			else{
				titleComposite = headComposite;
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
			
			textParent = headComposite;
		}
		
		if( html ){
			browser = new Browser( textParent, SWT.NONE );
			GridData data= new GridData( SWT.FILL, SWT.FILL, true, true );
			//browser.setJavascriptEnabled( false );
			browser.setLayoutData(data);
		}
	}
	
	private String finalize( String html, Color background, Font font ){
		if( font == null )
			return html;
		
		FontData[] data = font.getFontData();
		if( data == null || data.length == 0 )
			return html;
		
		StringBuilder builder = new StringBuilder();
		builder.append( "<html><body>" );
		builder.append( "<head>" );
		builder.append( "<style type=\"text/css\"> \n");
		builder.append( "body\n");
		builder.append( "{\n");
		builder.append( "background-color:#" );
		int rgb = (background.getRed() << 16) | (background.getGreen() << 8) | background.getBlue();
		builder.append( Integer.toHexString( rgb ) );
		builder.append( ";\n");
		builder.append( "font-family:\"" );
		builder.append( data[0].getName() );
		builder.append( "\";\n");
		builder.append( "font-size:" );
		builder.append( data[0].getHeight() );
		builder.append( "px;\n");
		builder.append( "}\n");
		builder.append( "</style>\n");
		builder.append( "</head>\n");
		
		builder.append( html );
		
		builder.append( "</html></body>" );
		
		return builder.toString();
	}
}
