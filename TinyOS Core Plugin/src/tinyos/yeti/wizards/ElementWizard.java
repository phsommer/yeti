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
package tinyos.yeti.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import tinyos.yeti.TinyOSPlugin;

/**
 * Wizard creating new *.nc files.
 * @author Benjamin Sigg
 */
public abstract class ElementWizard extends Wizard implements INewWizard{
    private ElementPage page;
    private String elementName;
    
    public ElementWizard( String elementName ){
        this.elementName = elementName;
        page = new ElementPage();
        
        addPage( page );
        setWindowTitle( "Create " + elementName );
    }
    
    @Override
    public boolean performFinish(){
        try{
            page.createFile();
        }
        catch( CoreException ex ){
            TinyOSPlugin.getDefault().getLog().log( ex.getStatus() );
            return false;
        }
        return true;
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ){
        page.init( workbench, selection );
    }
    
    protected abstract String content( String elementName );
    
    private class ElementPage extends SimpleNewFileWizardPage implements IWizardPage{
        protected ElementPage(){
            super( "Element" );
            setMessage( "Create a new " + elementName + " file for TinyOS applications" );
            setExtensions( "nc" );
            setFileDescription( "Name of new " + elementName + ":" );
        }
        
        private boolean checkNameValidity(){
            String text = getFilename().trim();
            text = withoutExtension( text ).trim();
            
            if( text.length() == 0 ){
                setErrorMessage( "Name must not be empty" );
                return false;
            }
            
            if( !Character.isJavaIdentifierStart( text.charAt( 0 ) )){
                setErrorMessage( "Not a valid name: '" + text + "'" );
                return false;
            }
            
            for( int i = 1, n = text.length(); i<n; i++ ){
                if( !Character.isJavaIdentifierPart( text.charAt( i ) )){
                    setErrorMessage( "Not a valid name: '" + text + "'" );
                    return false;    
                }
            }
            
            return true;
        }
        
        @Override
        protected boolean checkValidity(){
            if( !checkNameValidity() )
                return false;
            
            return super.checkValidity();
        }

        @Override
        protected String getContent( String filename ){
            String text = getFilename().trim();
            text = withoutExtension( text ).trim();
            return content( text );
        }
    }
}
