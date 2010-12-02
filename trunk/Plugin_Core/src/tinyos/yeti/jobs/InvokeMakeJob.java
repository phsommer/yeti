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
package tinyos.yeti.jobs;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSConsole;
import tinyos.yeti.TinyOSCore;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.nature.MissingNatureException;

/**
 * This job not only calls <code>make</code>, but also shows a dialog
 * to set extra make options. This job can loop if the make target is
 * set to {@link IMakeTarget#getLoop()}.
 */
public class InvokeMakeJob extends Job{
    private boolean shouldContinue = true;
    private IMakeTarget target;
    
    private boolean result = false;

    // Keys for dialog/position in preference store
    private final static String posX = "BuildTargetAsk.x";
    private final static String posY = "BuildTargetAsk.y";
    
    public InvokeMakeJob( IMakeTarget target ){
        super( "Invoking Make" );
        this.target = target;
    }
    
    public void execShouldContinue(){
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                IStatus ready = target.ready();
                if( ready != null ){
                    int type = -1;
                    String title = null;
                    String message = null;

                    switch( ready.getSeverity() ){
                        case IStatus.ERROR:
                            title = "Error";
                            message = "an error";
                            type = MessageDialog.ERROR;
                            break;
                        case IStatus.WARNING:
                            title = "Warning";
                            message = "a warning";
                            type = MessageDialog.WARNING;
                            break;
                        case IStatus.INFO:
                            title = "Info";
                            message = "a message";
                            type = MessageDialog.INFORMATION;
                            break;
                    }

                    if( type >= 0 ){
                        message = "There is " + message + " associated with this make-option:\n\n"
                        + "'" + ready.getMessage() + "'\n\n"
                        + "Would you like to continue anyway?";


                        MessageDialog dialog = new MessageDialog( 
                                Display.getCurrent().getActiveShell(), 
                                title,
                                null,
                                message,
                                type,
                                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 
                                0 );

                        if( dialog.open() != 0 ){
                            shouldContinue = false;
                        }
                    }
                }
            }
        });
    }

    public void execShouldContinue(final boolean firstrun) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                shouldContinue = showDialogToAskForParameters( target, firstrun );
            }
        });
    }
    
    @Override
    public IStatus run(IProgressMonitor monitor) {
    	monitor.beginTask( "Make", IProgressMonitor.UNKNOWN );
    	
        execShouldContinue();
        if( !shouldContinue ){
        	monitor.done();
            return Status.CANCEL_STATUS;
        }
        execShouldContinue(true);
        if (!shouldContinue){
        	monitor.done();
            return Status.CANCEL_STATUS;
        }

        final TinyOSConsole console = TinyOSPlugin.getDefault().getConsole();

        try {
            final ProjectTOS project = TinyOSPlugin.getDefault().getProjectTOS( target.getProject() );

            console.setProject( project );

            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    while( shouldContinue ){
                        ProjectTOS project = target.getProjectTOS();
                        IEnvironment environment = project.getEnvironment();
                        
                        environment.executeMake(
                                console.info(),
                                console.out(),
                                console.err(),
                                target.getProject().getLocation().toFile(),
                                project,
                                target,
                                monitor );
                        // refresh project
                        project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
                        if( monitor.isCanceled() ){
                            return;
                        }

                        if( target.getLoop() ){
                            execShouldContinue( false );
                        } 
                        else {
                            shouldContinue = false;
                        }
                    }
                }
            };
            TinyOSPlugin.getWorkspace().run(runnable, new SubProgressMonitor( monitor, 1 ));

            console.out().flush();
            console.err().flush();
        }
        catch( CoreException e ){
            return e.getStatus();
        }
        catch( OperationCanceledException e ){
        }
        catch( MissingNatureException ex ){
        	TinyOSCore.inform( "build", ex );
        }
        catch( IOException e ){
            TinyOSPlugin.log( e );
        }
        finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

    private void setResult(boolean b) {
        this.result = b;
    }
    
    /**
     * 
     * @param target
     * @param firstrun 
     * @return
     * @return false if user hits cancel
     */
    private boolean showDialogToAskForParameters(final IMakeTarget target, boolean firstrun) {
        MakeExtra[] extras = target.getMakeExtras();

        // ------------
        final Shell dialogShell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setLayout(new GridLayout());

        Composite top = new Composite(dialogShell, SWT.NONE);
        GridLayout topLayout = new GridLayout();
        topLayout.numColumns = 2;
        GridData topLData = new GridData();
        topLData.grabExcessHorizontalSpace = true;
        topLData.grabExcessVerticalSpace = true;
        topLData.horizontalAlignment = GridData.FILL;
        topLData.verticalAlignment = GridData.FILL;
        top.setLayoutData(topLData);
        top.setLayout(topLayout);

        Composite bottom = new Composite(dialogShell, SWT.NONE);
        final Button okButton = new Button(bottom, SWT.PUSH | SWT.CENTER);
        Button cancelButton;
        Text text2;
        Label label1;

        position(dialogShell);

        // Insert MakeExtras
        int count = 0;

        if( extras != null ){
            for (int i = 0; i < extras.length; i++) {
                final MakeExtra extra = extras[i];

                if( extra.askParameterAtCompileTime() ){
                    count++;
                    label1 = new Label(top, SWT.NONE);
                    label1.setText(extra.getName()+" - "+extra.getParameterName() + ":");

                    text2 = new Text(top, SWT.BORDER);
                    GridData text2LData = new GridData();
                    text2LData.grabExcessHorizontalSpace = true;
                    text2LData.horizontalAlignment = GridData.FILL;
                    text2.setLayoutData(text2LData);
                    
                    String value = extra.getParameterValue();
                    
                    text2.setText( value == null ? "" : value );
                    text2.addModifyListener(new ModifyListener(){

                        public void modifyText(ModifyEvent e) {
                            extra.setParameterValue(((Text)e.widget).getText());
                        }});                            
                }
            }
        }

        GridLayout bottomLayout = new GridLayout();
        bottomLayout.makeColumnsEqualWidth = true;
        bottomLayout.numColumns = 2;
        GridData bottomLData = new GridData();
        bottomLData.grabExcessHorizontalSpace = true;
        bottomLData.horizontalAlignment = GridData.CENTER;
        bottom.setLayoutData(bottomLData);
        bottom.setLayout(bottomLayout);

        // show dialog with counter if no parameter is asked for
        if (count==0) {
            if (firstrun) return true;

            Runnable r = new Runnable(){
                public void run() {
                    if (target.getLoopTime() > 0) {
                        final long start = System.currentTimeMillis();
                        while((System.currentTimeMillis()-start)<(target.getLoopTime()*1000)) {
                            try {
                                Thread.sleep(300);
                                Display.getDefault().asyncExec(new Runnable(){

                                    public void run() {
                                        if (!okButton.isDisposed()) {
                                            okButton.setText("Waiting... "+ 
                                                    (target.getLoopTime() - (System.currentTimeMillis()-start)/1000) + " sec");
                                        }
                                    }});

                            } catch (InterruptedException e1) {
                                // TODO Auto-generated catch block
                                //e1.printStackTrace();
                            }

                        }
                        Display.getDefault().asyncExec(new Runnable(){

                            public void run() {
                                if (!dialogShell.isDisposed()) {
                                    savePosition(dialogShell);
                                    dialogShell.close();
                                    setResult(true);
                                }
                            }
                        });

                    }
                }
            };

            final Thread thread = new Thread(r);
            dialogShell.setText("Waiting for next run, click to cancel");       

            GridData button1LData = new GridData();
            button1LData.horizontalAlignment = GridData.CENTER;
            button1LData.widthHint = 200;
            button1LData.heightHint = 23;
            okButton.setLayoutData(button1LData);
            okButton.setText("Waiting... "+ target.getLoopTime() + " sec");
            okButton.setSize(200, 23);
            okButton.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {
                    savePosition(dialogShell);
                    dialogShell.close();

                    if  ((thread!=null)&&(thread.isAlive())) {
                        thread.interrupt();
                    }
                    setResult(false);
                }

                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });

            dialogShell.setDefaultButton(okButton);
            dialogShell.layout();
            dialogShell.pack();

            dialogShell.setSize(220, dialogShell.getBounds().height);
            dialogShell.open();
            Display display = dialogShell.getDisplay();

            thread.start();

            while (!dialogShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }

            return result;
        } 
        else {
            dialogShell.setText("Please specify the parameter(s)");

            GridData button1LData = new GridData();
            button1LData.horizontalAlignment = GridData.CENTER;
            button1LData.widthHint = 60;
            button1LData.heightHint = 23;
            okButton.setLayoutData(button1LData);
            okButton.setText("OK");
            okButton.setSize(60, 23);
            okButton.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {
                    savePosition(dialogShell);
                    dialogShell.close();
                    setResult(true);
                }

                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });

            cancelButton = new Button(bottom, SWT.PUSH | SWT.CENTER);
            GridData cancelButtonLData = new GridData();
            cancelButtonLData.widthHint = 60;
            cancelButton.setLayoutData(cancelButtonLData);
            cancelButton.setText("Cancel");
            cancelButton.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {
                    savePosition(dialogShell);
                    dialogShell.close();
                    setResult(false);
                }



                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });

            dialogShell.setDefaultButton(okButton);
            dialogShell.layout();
            dialogShell.pack();

            dialogShell.setSize(300, dialogShell.getBounds().height);
            dialogShell.open();
            Display display = dialogShell.getDisplay();
            while (!dialogShell.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }

            return result;
        }
    }

    protected void savePosition(Shell dialogShell) {
        // Save dialog position for next use
        try {
            TinyOSPlugin.getDefault().getDialogSettings().put(posY,dialogShell.getLocation().y);
            TinyOSPlugin.getDefault().getDialogSettings().put(posX,dialogShell.getLocation().x);
        } catch (Exception e) {

        }
    }

    private void position(Shell dialogShell) {
        // Set location based on saved values
        String posX_str = TinyOSPlugin.getDefault().getDialogSettings().get(posX);
        String posY_str = TinyOSPlugin.getDefault().getDialogSettings().get(posY);

        if ((posX_str!=null)&&(posY_str!=null)) {
            dialogShell.setLocation(Integer.parseInt(posX_str),Integer.parseInt(posY_str));
        }
    }

}
