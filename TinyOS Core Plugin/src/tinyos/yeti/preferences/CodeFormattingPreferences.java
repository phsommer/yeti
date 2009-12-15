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
package tinyos.yeti.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.format.INesCFormattingStrategyFactory;

public class CodeFormattingPreferences extends PreferencePage implements IWorkbenchPreferencePage{
	private Combo formatter;
	private Combo indenter;
	
	private INesCFormattingStrategyFactory[] formatterFactories;
	private INesCFormattingStrategyFactory[] indenterFactories;
	
	public void init( IWorkbench workbench ){
		// ignore
	}
	
	@Override
	protected Control createContents( Composite parent ){
		INesCFormattingStrategyFactory[] factories = TinyOSPlugin.getDefault().getFormattingFactories();
		GridData data;
		
		Composite fields = new Composite( parent, SWT.NONE );
		fields.setLayout( new GridLayout( 2, false ) );
		fields.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		Label info = new Label( fields, SWT.NONE );
		info.setText( "Algorithms used for indenting and formatting code.\nNote: these settings are not applied to editors that are currently open,\nclosing and re-opening the editors is necessary.");
		info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		
		Label indenterLabel = new Label( fields, SWT.NONE );
		indenterLabel.setText( "Code indentation: " );
		indenterLabel.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, false, false ) );
		data.verticalIndent = 5;
		
		indenter = new Combo( fields, SWT.DROP_DOWN | SWT.READ_ONLY );
		indenter.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		indenter.setItems( getNames( indentingLabels( factories ) ) );
		data.verticalIndent = 5;
		
		Label formatterLabel = new Label( fields, SWT.NONE );
		formatterLabel.setText( "Code formatting: " );
		formatterLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ));
		
		formatter = new Combo( fields, SWT.DROP_DOWN | SWT.READ_ONLY );
		formatter.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		formatter.setItems( getNames( formattingLabels( factories ) ) );
		
		reset();
		
		return fields;
	}
	
	private String[] getNames( INesCFormattingStrategyFactory[] factories ){
		String[] result = new String[ factories.length ];
		for( int i = 0; i < factories.length; i++ )
			result[i] = factories[i].getName();
		return result;
	}
	
	private int indexOf( INesCFormattingStrategyFactory[] factories, String id ){
		for( int i = 0; i < factories.length; i++ ){
			if( factories[i].getId().equals( id )){
				return i;
			}
		}
		return -1;
	}
	
	private INesCFormattingStrategyFactory[] formattingLabels( INesCFormattingStrategyFactory[] factories ){
		if( formatterFactories != null )
			return formatterFactories;
		
		List<INesCFormattingStrategyFactory> result = new ArrayList<INesCFormattingStrategyFactory>();
		for( INesCFormattingStrategyFactory factory : factories ){
			if( factory.isFormatter() ){
				result.add( factory );
			}
		}
		return formatterFactories = result.toArray( new INesCFormattingStrategyFactory[ result.size() ] );
	}
	
	private INesCFormattingStrategyFactory[] indentingLabels( INesCFormattingStrategyFactory[] factories ){
		if( indenterFactories != null )
			return indenterFactories;
		
		List<INesCFormattingStrategyFactory> result = new ArrayList<INesCFormattingStrategyFactory>();
		for( INesCFormattingStrategyFactory factory : factories ){
			if( factory.isIndenter() ){
				result.add( factory );
			}
		}
		return indenterFactories = result.toArray( new INesCFormattingStrategyFactory[ result.size() ] );		
	}
	
	@Override
	public boolean performOk(){
		int index = formatter.getSelectionIndex();
		String id = "";
		if( index >= 0 ){
			id = formatterFactories[index].getId();
		}
		getPreferenceStore().setValue( PreferenceConstants.CF_CODE_FORMATTING_STRATEGY, id );
		
		index = indenter.getSelectionIndex();
		id = "";
		if( index >= 0 ){
			id = indenterFactories[index].getId();
		}
		getPreferenceStore().setValue( PreferenceConstants.CF_INDENTATION_STRATEGY, id );
		
		return super.performOk();
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore(){
		return TinyOSPlugin.getDefault().getPreferenceStore();
	}
	
	private void reset(){
		int index = indexOf( indenterFactories, getPreferenceStore().getString( PreferenceConstants.CF_INDENTATION_STRATEGY ) );
		if( index >= 0 )
			indenter.select( index );
		else if( indenter.getItemCount() > 0 )
			indenter.select( 0 );
		
		index = indexOf( formatterFactories, getPreferenceStore().getString( PreferenceConstants.CF_CODE_FORMATTING_STRATEGY ) );
		if( index >= 0 )
			formatter.select( index );
		else if( formatter.getItemCount() > 0 )
			formatter.select( 0 );
	}
	
	@Override
	protected void performDefaults(){
		if( formatter.getItemCount() > 0 )
			formatter.select( 0 );
		if( indenter.getItemCount() > 0 )
			indenter.select( 0 );
		super.performDefaults();
	}
}
