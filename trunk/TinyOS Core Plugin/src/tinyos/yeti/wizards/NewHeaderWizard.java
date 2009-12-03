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
 * Wizard creating new *.h files.
 * @author Benjamin Sigg
 */
public class NewHeaderWizard extends Wizard implements INewWizard{
    private HeaderPage page;
    
    public NewHeaderWizard(){
        page = new HeaderPage();
        
        addPage( page );
        setWindowTitle( "Create new header file" );
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
    
    public static String getSkeleton( String filename ){
        String constant = toConstant( filename );
        return "#ifndef " + constant + "\n" +
                "#define " + constant + "\n\n" +
                "#endif /* " + constant + " */\n";
    }
    
    private static String toConstant( String filename ){
        StringBuilder builder = new StringBuilder( filename.length() + 5 );
        
        boolean block = true;
        for( int i = 0, n = filename.length(); i<n; i++ ){
            char c = filename.charAt( i );
            if( Character.isUpperCase( c )){
                if( !block ){
                    block = true;
                    builder.append( "_" );
                }
                builder.append( c );
            }
            else if( Character.isLowerCase( c )){
                block = false;
                builder.append( Character.toUpperCase( c ) );
            }
            else if( c == '.' ){
                builder.append( "_" );
                block = true;
            }
            else{
                block = false;
                builder.append( c );
            }
        }
        
        return builder.toString();
    }
    private class HeaderPage extends SimpleNewFileWizardPage implements IWizardPage{
        protected HeaderPage(){
            super( "Filename" );
            setMessage( "Create a new header file for TinyOS applications" );
            setExtensions( "h" );
        }

        @Override
        protected String getContent( String filename ){
            return getSkeleton( filename );
        }
    }
}
