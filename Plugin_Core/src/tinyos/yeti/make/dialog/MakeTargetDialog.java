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
package tinyos.yeti.make.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.SharedMakeTarget;
import tinyos.yeti.make.dialog.pages.ComponentPage;
import tinyos.yeti.make.dialog.pages.ConstantMacroPage;
import tinyos.yeti.make.dialog.pages.EnvironmentVariablesPage;
import tinyos.yeti.make.dialog.pages.ExcludePage;
import tinyos.yeti.make.dialog.pages.ExtraPage;
import tinyos.yeti.make.dialog.pages.GeneralPage;
import tinyos.yeti.make.dialog.pages.IncludePage;
import tinyos.yeti.make.dialog.pages.PlatformPage;
import tinyos.yeti.make.dialog.pages.SensorPage;
import tinyos.yeti.make.dialog.pages.TypedefPage;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.make.targets.StandardSharedMakeTarget;

public class MakeTargetDialog<M extends MakeTargetSkeleton> extends TitleAreaDialog implements IMakeTargetDialog {
	public static class StandardMakeTargetDialog extends MakeTargetDialog<MakeTarget>{
	    /**
	     * Create Dialog with new values.. on project project
	     * @param parent
	     * @param project
	     */
	    public StandardMakeTargetDialog( Shell parent, ProjectTOS project ) {
	        super( parent, createPages(), new StandardSharedMakeTarget( project.getMakeTargets(), null ) );
	    }

	    /**
	     * Populate dialog with existing values from target
	     * @param parent the parent shell of this dialog
	     * @param target the base target
	     */
	    public StandardMakeTargetDialog( Shell parent, MakeTarget target ){
	    	super( parent, createPages(), new StandardSharedMakeTarget( target.getTargets(), target ) );
	    }
	    
	    @SuppressWarnings("unchecked")
		private static IMakeTargetDialogPage<MakeTarget>[] createPages(){
	    	return new IMakeTargetDialogPage[]{
	                new GeneralPage(),
	                new ComponentPage( true ),
	                new IncludePage( true, true, true ),
	                new ExcludePage( true ),
	                new PlatformPage( true ),
	                new ExtraPage( true ),
	                new SensorPage( true ),
	                new ConstantMacroPage( true ),
	                new TypedefPage( true ),
	                new EnvironmentVariablesPage( true ),
	        };
	    }
	}
	
	public static class ProjectDefaultsMakeTargetDialog extends MakeTargetDialog<MakeTargetSkeleton>{

	    /**
	     * Populate dialog with existing values from target
	     * @param parent the parent shell of this dialog
	     * @param target the base target
	     */
	    public ProjectDefaultsMakeTargetDialog( Shell parent, ProjectTOS project ){
	    	super( parent, createPages(), project.getMakeTargets().openDefaults( false ) );
	    }
	    
	    @SuppressWarnings("unchecked")
		private static IMakeTargetDialogPage<MakeTargetSkeleton>[] createPages(){
	    	return new IMakeTargetDialogPage[]{
	                new ComponentPage( false ),
	                new IncludePage( true, true, false ),
	                new ExcludePage( false ),
	                new PlatformPage( false ),
	                new ExtraPage( false ),
	                new SensorPage( false ),
	                new ConstantMacroPage( false ),
	                new TypedefPage( false )
	        };
	    }
	}
	
    private Composite pageComposite = null;

    // Tree
    private Tree optionTree = null;

    private SharedMakeTarget<M> makeTarget;
    
    /** handle for label showing errors and warnings for the pages */
    private Info info = new Info();

    /** current selected page */
    private IMakeTargetDialogPage<M> page = null;

    private IMakeTargetDialogPage<M>[] pages;

	public MakeTargetDialog( Shell parent, IMakeTargetDialogPage<M>[] pages, SharedMakeTarget<M> makeTarget ) {
        super( parent );

        this.pages = pages;
        this.makeTarget = makeTarget;
        
        setShellStyle( SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE );
        setHelpAvailable( false );
    }

    @Override
    protected Control createContents( Composite parent ){
        Control result = super.createContents( parent );

        info.setPages( pages );
        info.setPage( page );

        return result;
    }
    
    @Override
    protected Control createDialogArea( Composite parent ){
    	makeTarget.open();
    	
        Composite panel = new Composite( parent, SWT.NONE );
        panel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        panel.setLayout( new GridLayout( 1, false ));
        
        {
            SashForm top = new SashForm( panel, SWT.HORIZONTAL );
            top.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
            {
                optionTree = new Tree( top, SWT.BORDER );
                optionTree.addSelectionListener(new SelectionListener(){
                    public void widgetSelected(SelectionEvent e) {
                        final TreeItem ti = optionTree.getSelection()[0];

                        if ((ti != null)&&(ti.getData() instanceof IMakeTargetDialogPage)) {
                            Runnable longJob = new Runnable() {

                                @SuppressWarnings("unchecked")
								public void run() {
                                    IMakeTargetDialogPage<M> page = (IMakeTargetDialogPage<M>)ti.getData();
                                    setPage( page );
                                };                  
                            };                                                                      
                            BusyIndicator.showWhile(TinyOSPlugin.getStandardDisplay(), longJob);
                        }
                    }

                    public void widgetDefaultSelected(SelectionEvent e) {

                    }
                });

                int index = 0;
                for( IMakeTargetDialogPage<M> page : pages ){
                    TreeItem item = new TreeItem( optionTree, SWT.NONE );
                    item.setText( (++index) + ". " + page.getName() );
                    item.setData( page );
                }
            }
            {
                pageComposite = new Composite( top, SWT.BORDER );
                StackLayout pageLayout = new StackLayout();
                pageComposite.setLayout(pageLayout);

                for( IMakeTargetDialogPage<M> page : pages ){
                    page.createControl( pageComposite );
                }
                pageLayout.topControl = pages[0].getControl();
            }
            top.setWeights( new int[]{ 1, 3 } );
        }
        {
            Label separator = new Label( panel, SWT.HORIZONTAL | SWT.SEPARATOR);
            GridData data = new GridData( SWT.FILL, SWT.CENTER, true, false );
            data.verticalIndent = 15;
            separator.setLayoutData( data );
        }
        
        for( IMakeTargetDialogPage<M> page : pages ){
            page.setDialog( this );
            page.check( makeTarget.getMakeTarget(), makeTarget );
        }

        getShell().setText( "Make Options" );

        return panel;
    }

    public void openMakeTargetDialog() {
        setBlockOnOpen( true );
        open();
    }

    private IMakeTargetDialogPage<M> getPage(){
        Control current = ((StackLayout)pageComposite.getLayout()).topControl;
        for( IMakeTargetDialogPage<M> page : pages ){
            if( page.getControl() == current )
                return page;
        }
        return null;
    }

    private void setPage( IMakeTargetDialogPage<M> page ){
        if( this.page != page ){
            if( this.page != null )
                this.page.store( makeTarget.getMakeTarget() );

            this.page = page;

            if( this.page != null ){
                this.page.show( makeTarget.getMakeTarget(), makeTarget );

                Control next = page.getControl();

                ((StackLayout)pageComposite.getLayout()).topControl = next;

                setTitle( page.getName() );
                pageComposite.layout();

                if( next instanceof Composite ){
                    ((Composite)next).layout();
                }
            }
            else{
                setTitle( "No page" );
            }

            for( IMakeTargetDialogPage<M> next : pages ){
                next.check( makeTarget.getMakeTarget(), makeTarget );
            }

            info.setPage( page );
        }
    }

    public void setMessage( IMakeTargetDialogPage<?> page, String message, Severity severity ){
        info.setMessage( page, message, severity );
        Button button = getButton( IDialogConstants.OK_ID );
        if( button != null )
            button.setEnabled( info.hasNoErrors() );
    }

    public void contentChanged(){
    	// ignore
    }
    
    @Override
    protected void okPressed(){
        IMakeTargetDialogPage<M> page = getPage();
        if( page != null ){
            page.store( makeTarget.getMakeTarget() );
        }

        for( IMakeTargetDialogPage<M> next : pages ){
            next.check( makeTarget.getMakeTarget(), makeTarget );
        }

        if( !info.hasNoErrors() ){
            getButton( IDialogConstants.OK_ID ).setEnabled( false );
            return;
        }

        makeTarget.close();
        super.okPressed();
    }

    private class Info extends AbstractMessageConverter{
        @Override
        protected void showMessage( Severity severity, String message, IMakeTargetDialogPage<?> page ){
            if( severity == null )
                MakeTargetDialog.this.setMessage( null, IMessageProvider.NONE );
            else{
                switch( severity ){
                    case ERROR:
                        MakeTargetDialog.this.setMessage( "(" + page.getName() + ") " + message, IMessageProvider.ERROR );
                        break;
                    case WARNING:
                        MakeTargetDialog.this.setMessage( "(" + page.getName() + ") " + message, IMessageProvider.WARNING );
                        break;
                    case INFO:
                        MakeTargetDialog.this.setMessage( "(" + page.getName() + ") " + message, IMessageProvider.INFORMATION );
                        break;
                    case DESCRIPTION:
                        MakeTargetDialog.this.setMessage( message, IMessageProvider.INFORMATION );
                        break;       
                }
            }
        }
    }
}