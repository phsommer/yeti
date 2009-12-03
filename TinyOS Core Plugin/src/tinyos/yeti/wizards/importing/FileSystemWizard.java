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
package tinyos.yeti.wizards.importing;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import tinyos.yeti.jobs.ImportProjectFromSourceJob;

/**
 * Allows to create new projects by copying their source files.
 * @author Benjamin Sigg
 */
public class FileSystemWizard extends Wizard implements IImportWizard {
    private FileSystemWizardPage page;

    public FileSystemWizard() {
        super();
    }

    @Override
    public boolean performFinish() {
        if( !page.isPageComplete() )
            return false;
        
        String directory = page.getImportDirectory();
        String project = page.getProjectName();
        
        ImportProjectFromSourceJob job = new ImportProjectFromSourceJob( directory, project, page.getEnvironment(), page.getPlatform() );
        job.setPriority( Job.LONG );
        job.schedule();
        
        return true;
    }


    public void init( IWorkbench workbench, IStructuredSelection selection ){
        setWindowTitle( "TinyOS Import Wizard" );
        setNeedsProgressMonitor(true);
        page = new FileSystemWizardPage();
    }

    @Override
    public void addPages() {
        super.addPages(); 
        addPage(page);        
    }
}
