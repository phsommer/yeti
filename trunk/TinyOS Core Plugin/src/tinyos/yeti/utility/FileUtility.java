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
package tinyos.yeti.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.TinyOSPlugin;

public class FileUtility{
    /**
     * Copies all files from <code>source</code> to <code>destination</code>, preserving
     * the internal structure of the files.
     * @param source the source of the files
     * @param destination the destination of the files
     * @param monitor to report progress
     * @throws CoreException if a file cannot be copied
     */
    public static void copyTree( File source, IFolder destination, IProgressMonitor monitor ) throws CoreException{
        copyTree( source, destination, null, monitor );
    }
    
    /**
     * Copies all files from <code>source</code> to <code>destination</code>, preserving
     * the internal structure of the files.
     * @param source the source of the files
     * @param destination the destination of the files
     * @param observer each file transfered gets reported in this observer, can be <code>null</code>
     * @param monitor to report progress
     * @throws CoreException if a file cannot be copied
     */
    public static void copyTree( File source, IFolder destination, TreeCopyListener observer, IProgressMonitor monitor ) throws CoreException{
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        int count = countFiles( source );
        monitor.beginTask( "Copy files", count );
        copyTreeRecursion( source, destination, observer, monitor );
        monitor.done();
    }
    
    public static interface TreeCopyListener extends FileUtility.FileCopyListener{
        public void enterDirectory( File sourceDirectory, IFolder destinationDirectory );
        public void leaveDirectory( File sourceDirectory, IFolder destinationDirectory );
    }
    
    private static void copyTreeRecursion( File source, IFolder destination, TreeCopyListener observer, IProgressMonitor monitor ) throws CoreException{
        File[] files = source.listFiles();
        if( files != null ){
            if( observer != null ){
                observer.enterDirectory( source, destination );
            }
            
            for( File file : files ){
                if( file.isFile() ){
                    copyFile( file, destination, observer, new SubProgressMonitor( monitor, 1 ) );
                }
                else if( file.isDirectory() ){
                    copyTreeRecursion( file, destination.getFolder( file.getName() ), observer, monitor );
                }
                
                if( monitor.isCanceled() ){
                    break;
                }
            }
            
            if( observer != null ){
                observer.leaveDirectory( source, destination );
            }
        }
    }
    
    /**
     * Copies the file <code>source</code> into <code>destination</code> without
     * changing the name of <code>source</code>.
     * @param source the file to copy
     * @param destination the destination of the file
     * @param monitor to report progress
     * @throws CoreException if the file cannot be copied
     */
    public static void copyFile( File source, IFolder destination, IProgressMonitor monitor ) throws CoreException{
        copyFile( source, destination, null, monitor );
    }
    
    /**
     * Copies the file <code>source</code> into <code>destination</code> without
     * changing the name of <code>source</code>.
     * @param source the file to copy
     * @param destination the destination of the file
     * @param observer gets informed about the content of the file that is transfered, can be <code>null</code>
     * @param monitor to report progress
     * @throws CoreException if the file cannot be copied
     */
    public static void copyFile( File source, IFolder destination, FileCopyListener observer, IProgressMonitor monitor ) throws CoreException{
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Copy file", 100 );
        createFolder( destination, new SubProgressMonitor( monitor, 50 ) );
        
        try{
            byte[] content = getBytesFromFile( source );
            IFile file = destination.getFile( source.getName() );
            
            if( observer != null ){
                observer.copyFile( source, file, content );
            }
            
            file.create( new ByteArrayInputStream( content ), true, new SubProgressMonitor( monitor, 50 ) );
            monitor.done();
        }
        catch( IOException ex ){
            throw new CoreException( new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, 0, ex.getMessage(), ex ));
        }
    }
    
    public static interface FileCopyListener{
        public void copyFile( File source, IFile destination, byte[] content );
    }
    
    /**
     * Counts the number of files (not directories) in the directory <code>base</code>
     * or only the file <code>base</code>.
     * @param base a file or directory
     * @return the number of files below <code>base</code> (including <code>base</code>)
     */
    public static int countFiles( File base ){
        if( base.isFile() )
            return 1;
        
        if( base.isDirectory() ){
            int sum = 0;
            File[] files = base.listFiles();
            if( files != null ){
                for( File file : files ){
                    sum += countFiles( file );
                }
            }
            return sum;
        }
        
        return 0;
    }
    
    /**
     * Reads the contents of <code>file</code>.
     * @param file the to read from
     * @return all bytes that are in the file
     * @throws IOException if the file can't be read
     */
    public static byte[] getBytesFromFile( File file ) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while( offset < bytes.length
                && ( numRead=is.read (bytes, offset, bytes.length-offset )) >= 0 ){
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }
    
    /**
     * Makes sure that <code>folder</code> exists.
     * @param folder the folder to create
     * @param monitor for user interaction
     * @throws CoreException if <code>folder</code> can't be created
     */
    public static void createFolder( IFolder folder, IProgressMonitor monitor ) throws CoreException{
        if( !folder.exists() ){
            IContainer parent = folder.getParent();
            if( parent instanceof IFolder ){
                createFolder( (IFolder)parent, new SubProgressMonitor( monitor, 0 ) );
            }
            
            folder.create( false, true, null );
        }
    }
    
    /**
     * Ensures that <code>file</code> exists and contains <code>content</code>.
     * @param file the file to create or to fill
     * @param content the new content of the file
     * @param monitor for user interaction
     * @throws CoreException forwarded from <code>file</code>
     */
    public static void createFile( IFile file, String content, IProgressMonitor monitor ) throws CoreException  {
        InputStream stream = new ByteArrayInputStream( content.getBytes() );
        if( file.exists() ){
            file.setContents(stream, true, true, monitor );
        }
        else {
            file.create(stream, true, monitor );
        }
    }
    
    public static boolean isAncestor( File parent, File child ){
        while( child != null ){
            if( parent.equals( child ))
                return true;
            
            child = child.getParentFile();
        }
        
        return false;
    }
    
    public static String putPathsTogether( String[] paths ){
        if( paths == null )
            return "";
        else{
            StringBuilder builder = new StringBuilder();
            for( String entry : paths ){
                if( builder.length() > 0 )
                    builder.append( File.pathSeparatorChar );
                builder.append( entry );
            }
            return builder.toString();
        }
    }
}
