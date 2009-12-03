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
package tinyos.yeti.search.ui;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.search.util.PatternConstructor;

/**
 * Allows the user to select a search pattern.
 * @author Benjamin Sigg
 */
public class SearchPatternPanel {
	private Composite panel;

	private Combo pattern;
	private Button caseSensitive;
	private Button regularExpression;
	private Button wholeWord;

	private List<String> lastPatterns = new LinkedList<String>();

	public Pattern getPattern(){
		String pattern = this.pattern.getText();
		boolean caseSensitive = this.caseSensitive.getSelection();
		boolean regularExpression = this.regularExpression.getSelection();
		boolean wholeWord = this.wholeWord.getSelection();

		if( pattern.length() > 0 ){
			if( !lastPatterns.contains( pattern )){
				lastPatterns.add( 0, pattern );
				if( lastPatterns.size() > 25 ){
					lastPatterns.remove( lastPatterns.size()-1 );
				}
			}
		}
		
		return PatternConstructor.convert( pattern, regularExpression, caseSensitive, wholeWord );
	}

	public void writeConfiguration( IDialogSettings settings ){
		settings.put( "case", caseSensitive.getSelection() );
		settings.put( "regular", regularExpression.getSelection() );
		settings.put( "word", wholeWord.getSelection() );
		settings.put( "pattern", pattern.getText() );

		IDialogSettings last = settings.addNewSection( "last" );
		last.put( "count", lastPatterns.size() );
		int index = 0;
		for( String pattern : lastPatterns ){
			last.put( "pattern." + index, pattern );
			index++;
		}
	}

	public void readConfiguration( IDialogSettings settings ){
		if( settings == null )
			return;

		caseSensitive.setSelection( settings.getBoolean( "case" ) );
		regularExpression.setSelection( settings.getBoolean( "regular" ) );
		wholeWord.setSelection( settings.getBoolean( "word" ) );
		
		String pattern = settings.get( "pattern" );
		if( pattern != null ){
			this.pattern.setText( pattern );
			this.pattern.setSelection( new Point( 0, pattern.length() ) );
		}

		IDialogSettings last = settings.getSection( "last" );
		if( last != null && last.get( "count" ) != null ){
			int count = last.getInt( "count" );
			for( int i = 0; i < count; i++ ){
				lastPatterns.add( last.get( "pattern." + i ) );
			}
		}

		if( this.pattern != null ){
			updatePatternBox();
		}
	}

	public String getText(){
		return pattern.getText();
	}

	public IASTModelNodeFilter getFilter(){
		return new IASTModelNodeFilter(){
			private Pattern pattern = getPattern();

			public boolean include( IASTModelNode node ){
				String name = node.getNodeName();
				if( name == null )
					return false;

				return pattern.matcher( name ).find();
			}
		};
	}

	public Control getControl(){
		return panel;
	}

	private void updatePatternBox(){
		String text = pattern.getText();
		Point selection = pattern.getSelection();
		
		final String current = text.toLowerCase();
		pattern.removeAll();

		String[] available = lastPatterns.toArray( new String[ lastPatterns.size()+1 ] );
		available[ available.length-1 ] = "";

		if( current.length() > 0 ){
			Arrays.sort( available, new Comparator<String>(){
				private Collator collator = Collator.getInstance();

				public int compare( String a, String b ){
					int ca = category( a );
					int cb = category( b );
					if( ca < cb )
						return -1;
					if( ca > cb )
						return 1;
					return collator.compare( a, b );
				}

				private int category( String text ){
					if( text.equals( "" ))
						return 0;
					if( text.toLowerCase().startsWith( current ))
						return -1;
					return 1;
				}
			});
		}

		for( String insert : available ){
			pattern.add( insert );
		}
		
		pattern.setText( text );
		pattern.setSelection( selection );
		
	}

	public void createControl( Composite parent ){
		panel = new Composite( parent, SWT.NONE );

		panel.setLayout( new GridLayout( 2, false ) );

		// left
		Composite left = new Composite( panel, SWT.NONE );
		left.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		left.setLayout( new GridLayout( 1, true ) );

		pattern = new Combo( left, SWT.DROP_DOWN );
		pattern.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		pattern.addModifyListener( new ModifyListener(){
			private boolean onCall = false;
			public void modifyText( ModifyEvent e ){
				if( !onCall ){
					try{
						onCall = true;
						updatePatternBox();
					}
					finally{
						onCall = false;
					}
				}
			}
		});
		
		Label info = new Label( left, SWT.NONE );
		info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		info.setText( "(* = any string, ? = any character, \\ = escape for literals: * ? \\)" );

		// right
		Composite right = new Composite( panel, SWT.NONE );
		right.setLayoutData( new GridData( SWT.FILL, SWT.TOP, false, false ) );
		right.setLayout( new GridLayout( 1, true ) );

		caseSensitive = new Button( right, SWT.CHECK );
		caseSensitive.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

		regularExpression = new Button( right, SWT.CHECK );
		regularExpression.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

		wholeWord = new Button( right, SWT.CHECK );
		wholeWord.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

		caseSensitive.setText( "Case sensitive" );
		regularExpression.setText( "Regular expression" );
		wholeWord.setText( "Whole word" );
	}
}
