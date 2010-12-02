package tinyos.yeti.preferences;

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

import tinyos.yeti.ProjectManager;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.model.ProjectCacheFactory;

public class CachePreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	private Combo cache;
	
	private ProjectCacheFactory[] factories;
	
	public void init( IWorkbench workbench ){
		// ignore
	}
	
	@Override
	protected Control createContents( Composite parent ){
		factories = TinyOSPlugin.getDefault().getProjectCaches();
		String[] names = new String[ factories.length ];
		for( int i = 0; i < names.length; i++ ){
			names[i] = factories[i].getName();
		}
		
		GridData data;
		
		Composite fields = new Composite( parent, SWT.NONE );
		fields.setLayout( new GridLayout( 2, false ) );
		fields.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		Label info = new Label( fields, SWT.NONE );
		info.setText( "How properties are stored persistent.\nNote: Changing these settings leads to a full rebuild of all TinyOS projects.");
		info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		
		Label indenterLabel = new Label( fields, SWT.NONE );
		indenterLabel.setText( "Strategy: " );
		indenterLabel.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, false, false ) );
		data.verticalIndent = 5;
		
		cache = new Combo( fields, SWT.DROP_DOWN | SWT.READ_ONLY );
		cache.setLayoutData( data = new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		cache.setItems( names );
		data.verticalIndent = 5;
		
		reset();
		
		return fields;
	}
	
	@Override
	public boolean performOk(){
		int index = cache.getSelectionIndex();
		String id = "";
		if( index >= 0 ){
			id = factories[ index ].getId();
		}
		IPreferenceStore store = getPreferenceStore();
		String old = store.getString( PreferenceConstants.PROJECT_CACHE );
		if( !id.equals( old )){
			store.setValue( PreferenceConstants.PROJECT_CACHE, id );
			
			ProjectManager manager = TinyOSPlugin.getDefault().getProjectManager();
			manager.replaceCaches();
		}
		
		return super.performOk();
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore(){
		return TinyOSPlugin.getDefault().getPreferenceStore();
	}
	
	private void reset(){
		int index = indexOf( getPreferenceStore().getString( PreferenceConstants.PROJECT_CACHE ) );
		if( index >= 0 )
			cache.select( index );
		else if( cache.getItemCount() > 0 )
			cache.select( 0 );
	}
	
	private int indexOf( String id ){
		for( int i = 0; i < factories.length; i++ ){
			if( factories[i].getId().equals( id )){
				return i;
			}
		}
		return -1;
	}
	
	
	@Override
	protected void performDefaults(){
		if( cache.getItemCount() > 0 )
			cache.select( 0 );
		super.performDefaults();
	}
}
