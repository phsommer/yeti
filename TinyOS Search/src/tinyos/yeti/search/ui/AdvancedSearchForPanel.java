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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeFilter;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

/**
 * A panel showing some controls that allow the user to create an 
 * {@link IASTModelNodeFilter} based on {@link Tag}s.
 * @author besigg
 */
public class AdvancedSearchForPanel{
	private static enum Rule{
		NULL( "", null ),
		MUST( "Must be", "must be" ),
		MUST_NOT( "Must not be", "must not be" ),
		SUFFICIENT( "Sufficient to be", "suf. to be" ),
		SUFFICIENT_NOT( "Sufficient not to be", "suf. not to be" );
		
		private String name;
		private String small;
		
		private Rule( String name, String small ){
			this.name = name;
			this.small = small;
		}
		
		public String getName(){
			return name;
		}
		
		public String getSmall(){
			return small;
		}
	}
	
	private ScrolledComposite control;
	private Composite panel;
	
	private Tag[] tags;
	
	private List<RowControl> rows = new ArrayList<RowControl>();
	
	public AdvancedSearchForPanel( Tag[] tags ){
		this.tags = tags;
	}
	
	public void createControl( Composite parent ){
		control = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
		
		panel = new Composite( control, SWT.NONE );
		panel.setLayout( new GridLayout( 2, true ) );
		
		control.setExpandHorizontal( true );
		control.setExpandVertical( false );
		
		control.setContent( panel );
		
		check();
	}
	
	public Control getControl(){
		return control;
	}

	private void check(){
		// remove empty rows (except the last one)
		Iterator<RowControl> iterator = rows.iterator();
		int remaining = rows.size()-1;
		
		while( remaining-- > 0 ){
			RowControl next = iterator.next();
			if( next.isEmpty() ){
				iterator.remove();
				next.dispose();
			}
		}
		
		if( rows.size() == 0 ){
			rows.add( new RowControl() );
		}
		else{
			RowControl last = rows.get( rows.size()-1 );
			if( !last.isEmpty() ){
				rows.add( new RowControl() );
			}
		}
		
		Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
		control.setMinSize( size );
		panel.setSize( size );
	}
	
	public Selection getSelection(){
		Selection selection = new Selection();
		selection.read( this );
		return selection;
	}
	
	public void setSelection( Selection selection ){
		if( selection instanceof Selection ){
			((Selection)selection).write( this );
		}
	}
	
	public static class Selection{
		private Rule[] rules;
		private Tag[] tags;
		
		public IASTModelNodeFilter getFilter(){
			final TagSet sufficient = getTags( Rule.SUFFICIENT );
			final TagSet sufficientNot = getTags( Rule.SUFFICIENT_NOT );
			final TagSet must = getTags( Rule.MUST );
			final TagSet mustNot = getTags( Rule.MUST_NOT );
			
			return new IASTModelNodeFilter(){
				public boolean include( IASTModelNode node ){
					TagSet set = node.getTags();
					if( set == null )
						set = TagSet.EMPTY;
					
					if( set.containsOneOf( mustNot ))
						return false;
					
					if( set.containsOneOf( sufficient ))
						return true;
					
					if( sufficientNot.size() > 0 && set.contains( sufficientNot ))
						return false;
					
					if( set.contains( must ))
						return true;
					
					return false;
				}
			};
		}
		
		private TagSet getTags( Rule rule ){
			TagSet tags = new TagSet();
			for( int i = 0; i < rules.length; i++ ){
				if( rules[i] == rule ){
					Tag tag = this.tags[i];
					if( tag != null ){
						tags.add( tag );
					}
				}
			}
			return tags;
		}
		
		public String getDescription(){
			if( rules == null )
				return "";
			
			StringBuilder builder = new StringBuilder();
			for( int i = 0; i < rules.length; i++ ){
				Rule rule = rules[i];
				Tag tag = tags[i];
				if( tag != null && rule != Rule.NULL ){
					if( builder.length() > 0 )
						builder.append( ", " );
					
					builder.append( rule.getSmall() );
					builder.append( " " );
					builder.append( tag.getDescription().getName() );
				}
			}
			
			return builder.toString();
		}
		
		public void read( Tag[] advancedTags, IDialogSettings settings ){
			if( settings.get( "count" ) == null ){
				rules = new Rule[]{};
				tags = new Tag[]{};
			}
			else{
				int count = settings.getInt( "count" );

				rules = new Rule[ count ];
				tags = new Tag[ count ];

				for( int i = 0; i < count; i++ ){
					rules[i] = Rule.valueOf( settings.get( "rule." + i ) );
					String tag = settings.get( "tag." + i );
					if( tag != null ){
						for( Tag check : advancedTags ){
							if( check.getId().equals( tag )){
								tags[i] = check;
								break;
							}
						}
					}
				}
			}
		}
		
		public void read( AdvancedSearchForPanel panel ){
			int size = panel.rows.size();
			rules = new Rule[ size ];
			tags = new Tag[ size ];
			
			int index = 0;
			for( RowControl row : panel.rows ){
				rules[ index ] = row.getRule();
				tags[ index ] = row.getTag();
				index++;
			}
		}
		
		public void write( IDialogSettings settings ){
			settings.put( "count", rules.length );
			for( int i = 0; i < rules.length; i++ ){
				settings.put( "rule." + i, rules[i].toString() );
				if( tags[i] != null ){
					settings.put( "tag." + i, tags[i].getId() );
				}
			}
		}
		
		public void write( AdvancedSearchForPanel panel ){
			while( panel.rows.size() < rules.length )
				panel.rows.add( panel.new RowControl() );
			while( panel.rows.size() > rules.length ){
				panel.rows.remove( panel.rows.size()-1 ).dispose();
			}
			
			int index = 0;
			for( RowControl row : panel.rows ){
				row.setContent( rules[ index ], tags[ index ]);
				index++;
			}
			
			panel.check();
		}
	}
	
	/**
	 * Let's the use choose a {@link Rule} and a {@link Tag}.
	 * @author besigg
	 */
	private class RowControl implements SelectionListener{
		private Combo ruleCombo;
		private Combo tagCombo;
		
		public RowControl(){
			ruleCombo = new Combo( panel, SWT.READ_ONLY );
			tagCombo = new Combo( panel, SWT.READ_ONLY );
			
			ruleCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			tagCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
			
			ruleCombo.setVisibleItemCount( Rule.values().length );
			tagCombo.setVisibleItemCount( 12 );
			
			for( Rule rule : Rule.values() ){
				ruleCombo.add( rule.getName() );
			}
			ruleCombo.select( 0 );
			
			tagCombo.add( "" );
			for( Tag tag : tags ){
				tagCombo.add( tag.getDescription().getName() );
			}
			
			tagCombo.select( 0 );
			
			ruleCombo.addSelectionListener( this );
			tagCombo.addSelectionListener( this );
		}
		
		public void dispose(){
			ruleCombo.dispose();
			tagCombo.dispose();
		}
		
		public boolean isEmpty(){
			return getTag() == null && getRule() == Rule.NULL;
		}
		
		public void setContent( Rule rule, Tag tag ){
			ruleCombo.select( rule.ordinal() );
			
			if( tag == null )
				tagCombo.select( 0 );
			else{
				for( int i = 0; i < tags.length; i++ ){
					if( tags[i].equals( tag )){
						tagCombo.select( i+1 );
						break;
					}
				}
			}
		}
		
		public Rule getRule(){
			return Rule.values()[ ruleCombo.getSelectionIndex() ];
		}
		
		public Tag getTag(){
			int index = tagCombo.getSelectionIndex();
			if( index == 0 )
				return null;
			else
				return tags[ index-1 ];
		}
		
		public void widgetDefaultSelected( SelectionEvent e ){
			check();
		}
		public void widgetSelected( SelectionEvent e ){
			check();
		}
	}
}
