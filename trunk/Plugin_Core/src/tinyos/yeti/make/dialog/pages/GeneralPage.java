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
package tinyos.yeti.make.dialog.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;

public class GeneralPage extends AbstractMakeTargetDialogPage<MakeTarget> {
    private Button loopTrue;
    private Button loopFalse;

    private Text targetName;
    private Text loopTime;

    public GeneralPage(){
        super( "General" );
    }

    public void show( MakeTarget maketarget, IMakeTargetInformation information ){
        loopTrue.setSelection( maketarget.getLoop() );
        loopFalse.setSelection( !maketarget.getLoop() );

        targetName.setText( maketarget.getName() );
        loopTime.setText( String.valueOf( maketarget.getLoopTime() ));
    }

    public void store( MakeTarget maketarget ){
        maketarget.setLoop( loopTrue.getSelection() );
        maketarget.setName( targetName.getText() );

        try{
            // TODO write this field in a way that an exception can't happen
            maketarget.setLoopTime( Double.parseDouble( loopTime.getText() ));
        }
        catch( NumberFormatException ex ){
            maketarget.setLoopTime( 0.0 );
        }
    }

    public void createControl( Composite parent ) {
        Composite generalComposite = new Composite( parent, SWT.NONE );
        GridLayout generalCompositeLayout = new GridLayout();
        generalCompositeLayout.makeColumnsEqualWidth = true;
        generalCompositeLayout.marginHeight = 0;
        generalCompositeLayout.horizontalSpacing = 0;
        generalCompositeLayout.marginLeft = 5;
        generalCompositeLayout.marginRight = 5;
        generalComposite.setLayout(generalCompositeLayout);

        setControl( generalComposite );

        {
            Composite generalItemComposite = new Composite(generalComposite,SWT.NONE);
            GridLayout generalItemCompositeLayout = new GridLayout();
            generalItemCompositeLayout.makeColumnsEqualWidth = true;
            generalItemCompositeLayout.numColumns = 2;
            GridData generalItemCompositeLData = new GridData();
            generalItemCompositeLData.grabExcessHorizontalSpace = true;
            generalItemCompositeLData.grabExcessVerticalSpace = true;
            generalItemCompositeLData.horizontalAlignment = GridData.FILL;
            generalItemCompositeLData.verticalAlignment = GridData.BEGINNING;
            generalItemComposite.setLayoutData(generalItemCompositeLData);
            generalItemComposite.setLayout(generalItemCompositeLayout);
            {
                Label nameLabel = new Label(
                        generalItemComposite,
                        SWT.NONE);
                nameLabel.setText("Target Name:");
            }
            {
                targetName = new Text(generalItemComposite,SWT.BORDER);
                GridData textNameLData = new GridData();
                textNameLData.grabExcessHorizontalSpace = true;
                textNameLData.horizontalAlignment = GridData.FILL;
                targetName.setLayoutData(textNameLData);
            }
            {
                GridLayout composite12Layout = new GridLayout();
                composite12Layout.makeColumnsEqualWidth = true;
                composite12Layout.numColumns = 2;
                GridData composite12LData = new GridData();
                composite12LData.grabExcessVerticalSpace = true;
                composite12LData.horizontalAlignment = GridData.FILL;
                composite12LData.grabExcessHorizontalSpace = true;
                composite12LData.verticalAlignment = GridData.FILL;
                {
                    // Spacaer
                    Label l0 = new Label(generalItemComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

                    GridData spacerHorLData = new GridData();
                    spacerHorLData.heightHint = 30;
                    spacerHorLData.grabExcessHorizontalSpace = true;
                    spacerHorLData.horizontalAlignment = GridData.FILL;
                    spacerHorLData.horizontalSpan=2;
                    //spacerHorLData.verticalAlignment = GridData.BEGINNING;
                    l0.setLayoutData(spacerHorLData);
                    //l0.setBounds(0, 209, 60, 30);

                    // Loop
                    Label l1 = new Label(generalItemComposite,SWT.NONE);
                    l1.setText("Loop execution:");
                    l1.setToolTipText("Execute option until user aborts it");

                    //--- true false
                    Composite c1 = new Composite(generalItemComposite,SWT.NONE);
                    GridLayout gl1 = new GridLayout();
                    gl1.makeColumnsEqualWidth = true;
                    gl1.numColumns = 2;
                    GridData gd1 = new GridData();
                    gd1.grabExcessVerticalSpace = true;
                    gd1.horizontalAlignment = GridData.FILL;
                    gd1.grabExcessHorizontalSpace = true;
                    gd1.verticalAlignment = GridData.FILL;
                    c1.setLayoutData(gd1);
                    c1.setLayout(gl1);

                    loopTrue = new Button(c1,SWT.RADIO | SWT.LEFT);
                    loopTrue.setText("true");

                    loopFalse = new Button(c1,SWT.RADIO | SWT.LEFT);
                    loopFalse.setText("false");
                    loopFalse.setSelection(true);

                    // Loop - Timer
                    Label timeLoopLabel = new Label(generalItemComposite,SWT.NONE);
                    timeLoopLabel.setText("Time between execs:");
                    timeLoopLabel.setToolTipText("Time (in sec) between two consecutive executions of make");


                    loopTime = new Text(generalItemComposite,SWT.BORDER);
                    GridData textNameLData = new GridData();
                    textNameLData.grabExcessHorizontalSpace = true;
                    textNameLData.horizontalAlignment = GridData.FILL;
                    loopTime.setLayoutData(textNameLData);
                }
            }
        }
    }

}
