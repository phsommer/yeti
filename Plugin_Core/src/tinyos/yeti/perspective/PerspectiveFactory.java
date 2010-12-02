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
package tinyos.yeti.perspective;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import tinyos.yeti.TinyOSPlugin;

public class PerspectiveFactory implements IPerspectiveFactory {

    /**
     * Initialize the TinyOS Perspective...
     * Set defaults for position and size of the different views
     * (Package Explorer N, Editor E, Console C, Problem P, Task T, Outline O,
     * MakeOption M, TinyOS Files TF, Component Graph CG)
     * 
     * 	 -----------------------------
     *   |   |             |         |
     *   |   |             |         |
     *   | N |    E / CG   |    O    |
     *   | TF|             |         |
     *   |   |-------------|---------|
     *   |   |  C / P / T  |  M      |
     *   -----------------------------
     * 
     */
    public void createInitialLayout(IPageLayout layout) {
        // Window > Show View
        layout.addShowViewShortcut( JavaUI.ID_PACKAGES );
        layout.addShowViewShortcut( IPageLayout.ID_OUTLINE );
        layout.addShowViewShortcut( IPageLayout.ID_PROBLEM_VIEW );
        layout.addShowViewShortcut( IConsoleConstants.ID_CONSOLE_VIEW );
        layout.addShowViewShortcut( TinyOSPlugin.ID_MAKE_OPTIONS_VIEW );
        layout.addShowViewShortcut( TinyOSPlugin.ID_THUMBNAIL_VIEW );
        //layout.addShowViewShortcut( TinyOSPlugin.ID_ALL_FILES_VIEW );
        
        // File > New
        layout.addNewWizardShortcut( TinyOSPlugin.ID_NEW_HEADER_WIZARD );
        layout.addNewWizardShortcut( TinyOSPlugin.ID_NEW_INTERFACE_WIZARD );
        layout.addNewWizardShortcut( TinyOSPlugin.ID_NEW_MODULE_WIZARD );
        layout.addNewWizardShortcut( TinyOSPlugin.ID_NEW_CONFIGURATION_WIZARD );
        layout.addNewWizardShortcut( "org.eclipse.ui.wizards.new.folder" );
        layout.addNewWizardShortcut( "org.eclipse.ui.wizards.new.file" );
        layout.addNewWizardShortcut( "org.eclipse.ui.editors.wizards.UntitledTextFileWizard" );

        // Menu
        layout.addActionSet( TinyOSPlugin.ID_MENU_ACTIONSET );
        layout.addActionSet( "org.eclipse.debug.ui.launchActionSet" );
        
        // The editor area
        String editorArea = layout.getEditorArea();

        // The folder the editor is in


        // The folder on the left side
        // ---------------------------------------------
        IFolderLayout left = layout.createFolder("left",
                IPageLayout.LEFT, 
                0.20f,
                editorArea);
        // Add view Resource-Navigator
        left.addView(JavaUI.ID_PACKAGES);
        left.addPlaceholder(IPageLayout.ID_RES_NAV);
        left.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        left.addPlaceholder( TinyOSPlugin.ID_ALL_FILES_VIEW );

        // The folder on the right side
        // ---------------------------------------------
        IFolderLayout right = layout.createFolder("right", 
                IPageLayout.RIGHT, 
                0.750f,
                editorArea);
        // Outline View
        right.addView(IPageLayout.ID_OUTLINE);

        // The folder below the right folder..
        // ---------------------------------------------
        IFolderLayout rightbott = layout.createFolder("rightb", 
                IPageLayout.BOTTOM, 
                0.7f,
                IPageLayout.ID_OUTLINE);
        // TinyOS MakeOption View
        rightbott.addView("TinyOS.view.makeOptions");


        // The folder under the editor area
        // ---------------------------------------------
        IFolderLayout bottom = layout.createFolder("bott",
                IPageLayout.BOTTOM,
                0.8f,
                editorArea);

        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        bottom.addView(IPageLayout.ID_TASK_LIST);
        bottom.addPlaceholder( IPageLayout.ID_PROGRESS_VIEW );
        bottom.addPlaceholder( "org.eclipse.pde.runtime.LogView" );
    }

}
