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
package tinyos.yeti.views.make;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;

public class NescFilePatternMatchListener implements IPatternMatchListener {
	private ProjectTOS project;

	public String getPattern() {
		return "/.*\\.nc(:[0-9]+)?+";
	}

	public void setProject( ProjectTOS project ){
		this.project = project;
	}

	public int getCompilerFlags() {
		return 0;
	}

	public String getLineQualifier() {
		return null;
	}

	public void connect( TextConsole console ){
		// ignore
	}

	public void disconnect() {
		// ignore
	}

	public void matchFound( PatternMatchEvent event ){
		try{
			if( project == null )
				return;

			int length = event.getLength();
			int offset = event.getOffset();
			MessageConsole c = (MessageConsole) event.getSource();
			
			if( c == null )
				return;
			
			String text = c.getDocument().get( offset, length );
			if( text == null )
				return;

			String[] s = text.split(":");
			if( s.length == 0 )
				return;

			String filetext = s[0];
			int lineNumber = -1;
			if ((s.length > 1)&&(s[1].length()>0)) {
				try{
					lineNumber = Integer.parseInt(s[1]);
				}
				catch( NumberFormatException ex ){
					// ignore
				}
			}

			IEnvironment environment = project.getEnvironment();
			if( environment == null )
				return;
			
			File file = environment.modelToSystem( filetext );
			if( file == null )
				return;
			
			if( file.isAbsolute() ){
				if( !file.exists() ){
					return;
				}
			}
			else{
				File result = null;
				IFolder[] folders = project.getSourceContainers();
				for( IFolder folder : folders ){
					IFile test = folder.getFile( filetext );
					if( test.exists() ){
						IPath path = test.getLocation();
						if( path != null ){
							result = path.toFile();
						}
					}
				}
				if( result == null )
					return;

				file = result;
			}

			// file exists

			c.addHyperlink( new Link( file, project, lineNumber ), offset, length );
		}
		catch( BadLocationException e ){
			TinyOSPlugin.log( e );
		}

		//              c.addHyperlink(new FileLink(file, null, 42, -1, lineNumber),
		//		c.addHyperlink(new FileLink(file, null, -1, -1, lineNumber),
		//				offset, length);
	}

	private static class Link implements IHyperlink{
		private File target;
		private ProjectTOS project;
		private int lineNumber;

		public Link( File target, ProjectTOS project, int lineNumber ){
			this.target = target;
			this.project = project;
			this.lineNumber = lineNumber;
		}

		public void linkActivated(){
			IPath path = new Path( target.getAbsolutePath() );
			try{
				ITextEditor editor = TinyOSPlugin.getDefault().openFileInTextEditor( project, path, true );
				if( lineNumber >= 0 && editor != null ){
					IEditorInput input = editor.getEditorInput();
					
					IDocumentProvider provider = editor.getDocumentProvider();
					try {
						provider.connect(input);
					
						IDocument document = provider.getDocument(input);
					
						IRegion region= document.getLineInformation( lineNumber-1 );
						editor.selectAndReveal( region.getOffset(), region.getLength() );
					}
					catch (BadLocationException e) {
						TinyOSPlugin.log( e );
					}
					catch (CoreException e) {
						// unable to link
						TinyOSPlugin.log( e );
						return;
					}
					finally{
						provider.disconnect( input );
					}
				}
			}
			catch( CoreException e ){
				TinyOSPlugin.log( e );
			}
		}

		public void linkEntered(){
			// ignore
		}

		public void linkExited(){
			// ignore	
		}
	}
}
