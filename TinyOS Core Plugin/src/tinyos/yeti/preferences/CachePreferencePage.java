package tinyos.yeti.preferences;

import java.util.Map;

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

public class CachePreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	private Combo cache;
	
	private String[] ids;
	private String[] names;
	
	public void init( IWorkbench workbench ){
		// ignore
	}
	
	@Override
	protected Control createContents( Composite parent ){
		Map<String, String> strategies = TinyOSPlugin.getDefault().loadProjectCacheNames();
		ids = new String[ strategies.size() ];
		names = new String[ strategies.size() ];
		int index = 0;
		for( Map.Entry<String, String> entry : strategies.entrySet() ){
			ids[index] = entry.getKey();
			names[index] = entry.getValue();
			index++;
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
			id = ids[ index ];
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
		int index = indexOf( ids, getPreferenceStore().getString( PreferenceConstants.PROJECT_CACHE ) );
		if( index >= 0 )
			cache.select( index );
		else if( cache.getItemCount() > 0 )
			cache.select( 0 );
	}
	
	private int indexOf( String[] ids, String id ){
		for( int i = 0; i < ids.length; i++ ){
			if( ids[i].equals( id )){
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
