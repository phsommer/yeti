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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.make.targets.MakeTargetSkeleton;

/**
 * One page on the {@link MakeTargetDialog}.
 * @param <M> What kind of make-target this page can edit
 * @author Benjamin Sigg
 */
public interface IMakeTargetDialogPage<M extends MakeTargetSkeleton> {
    /**
     * Informs this page about the dialog for which it works
     * @param dialog the new dialog
     */
    public void setDialog( IMakeTargetDialog dialog );
    
    /**
     * Gets a description of this page.
     * @return a human readable description of what this page does
     */
    public String getDescription();
    
    /**
     * The title of the page.
     * @return gets the title shown in the list where the user can select the
     * current page
     */
    public String getName();
    
    /**
     * Gets a small icon for this page.
     * @return the image or <code>null</code>
     */
    public Image getImage();

    /**
     * Creates the contents of this page.
     * @param parent the parent of the new control
     */
    public void createControl( Composite parent );

    /**
     * Gets the control which represents this page.
     * @return the control, or <code>null</code> if not yet {@link #createControl(Composite) created} 
     */
    public Control getControl();

    /**
     * Called every time before this page is shown (even if the dialog was
     * not closed since the last time that <code>show</code> was called).
     * @param maketarget a target that contains the values that were
     * set by the other pages
     * @param information additional information about the current make target
     */
    public void show( M maketarget, IMakeTargetInformation information );

    /**
     * Makes a quick check whether the information in <code>maketarget</code>
     * is valid. Uses the {@link #setDialog(IMakeTargetDialog) dialog} to
     * report any errors or warnings.
     * @param maketarget the target to check
     * @param information information about the current make target
     */
    public void check( M maketarget, IMakeTargetInformation information );
    
    /**
     * Stores the values of this page in <code>maketarget</code>
     * @param maketarget the target to store values in
     */
    public void store( M maketarget );

    /**
     * Called when the dialog is no longer used, this page should release
     * all the resources it holds 
     */
    public void dispose();
}
