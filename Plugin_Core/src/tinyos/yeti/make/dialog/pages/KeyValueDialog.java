package tinyos.yeti.make.dialog.pages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class KeyValueDialog<T> extends Dialog{
    private Text key;
    private Text value;
    
    private String valueValue;
    private String keyValue;
    
    private String title = "";
    private KeyValuePage<T> page;
    
    public KeyValueDialog( Shell shell, KeyValuePage<T> page ){
        super( shell );
        setBlockOnOpen( true );
        this.page = page;
    }

	@Override
	protected boolean isResizable() {
    	return true;
    }
    
	protected abstract T create( String key, String value );
	
	public T get(){
		return create( keyValue, valueValue );
	}
	
    public boolean open( String key, String value, T item ){
        if( key == null || value == null )
            title = page.getNewDialogTitle();
        else
            title = page.getEditDialogTitle();
        
        Shell shell = getShell();
        if( shell != null )
            shell.setText( title );
        
        valueValue = value == null ? "" : value;
        keyValue = key == null ? "" : key;
        
        int state = super.open();
        
        if( state == OK ){
            return true;
        }
        
        return false;
    }
    
    @Override
    protected Control createDialogArea( Composite parent ){
        getShell().setText( title );
        
        Composite content = (Composite)super.createDialogArea( parent );
        
        Label info = new Label( content, SWT.NONE );
        info.setText( page.getDialogExample() );
        info.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Composite fields = new Composite( content, SWT.NONE );
        fields.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        fields.setLayout( new GridLayout( 2, false ) );

        createFields( fields );
        
        checkOkButton();
        
        return content;
    }
    
    protected void createFields( Composite parent ){
        Label keyLabel = new Label( parent, SWT.NONE );
        keyLabel.setText( page.getKeyName() + ": " );
        keyLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        key = new Text( parent, SWT.BORDER | SWT.SINGLE );
        key.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        key.setText( keyValue );
        key.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                contentChanged();
            }
        });
        
        Label valueLabel = new Label( parent, SWT.NONE );
        valueLabel.setText( page.getValueName() + ": " );
        valueLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

        value = new Text( parent, SWT.BORDER | SWT.SINGLE );
        value.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        value.setText( valueValue );
        value.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                contentChanged();
            }
        });    	
    }
    
    private void contentChanged(){
        keyValue = key.getText().trim();
        valueValue = value.getText().trim();
        
        checkOkButton();
    }
    
    protected abstract boolean checkOk( String key, String value );
    
    private void checkOkButton(){
        Button button = getButton( IDialogConstants.OK_ID );
        if( button != null )
            button.setEnabled( checkOk( keyValue, valueValue ) );
    }
}