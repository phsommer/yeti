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
package tinyos.yeti.editors.action;

import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatterExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCEditor;

public class IndentSourceAction extends TextEditorAction{
	private NesCEditor editor;
	private IContentFormatter indentor;

	public IndentSourceAction( ResourceBundle bundle, String prefix,
			NesCEditor editor ){
		super( bundle, prefix, editor );
		this.editor = editor;
		indentor = editor.getConfiguration().getIndentFormatter(
				editor.getEditorSourceViewer() );
	}

	@Override
	public void run(){
		if (!isEnabled() || !validateEditorInputState())
			return;

		ITextSelection selection= getSelection();
		final IDocument document= getDocument();

		if (document != null) {
			IRewriteTarget target= (IRewriteTarget)getTextEditor().getAdapter(IRewriteTarget.class);
			if (target != null)
				target.beginCompoundChange();
			
			try{
				int offset= selection.getOffset();
				int length= selection.getLength();
				Position start = new Position( offset );
				Position end = new Position( offset + length);
				
				document.addPosition( start );
				document.addPosition( end );
				
				IRegion region = getFormatRegion( document, offset, length );
				
				if( indentor instanceof IContentFormatterExtension ){
					IContentFormatterExtension extension = (IContentFormatterExtension)indentor;
					FormattingContext context = new FormattingContext();
					context.setProperty( FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.FALSE );
					context.setProperty( FormattingContextProperties.CONTEXT_MEDIUM, document );
					context.setProperty( FormattingContextProperties.CONTEXT_REGION, region );
					extension.format( document, context );
				}
				else{
					indentor.format( document, region );
				}
				
				document.removePosition( start );
				document.removePosition( end );
				
				int newOffset = Math.min( start.offset, end.offset );
				int newLength = Math.max( start.offset, end.offset ) - newOffset;
				if( newLength == 0 ){
					editor.setCaretPosition( newOffset );
				}
				else{
					editor.selectAndReveal( newOffset, newLength );
				}
			}
			catch( BadLocationException ex ){
				TinyOSPlugin.log( ex );
			}
			finally{
				if (target != null)
					target.endCompoundChange();
			}
		}
	}
	
	private IRegion getFormatRegion( IDocument document, int offset, int length ) throws BadLocationException{
		int firstLine = document.getLineOfOffset( offset );
		int lastLine = document.getLineOfOffset( offset+length );
		
		int start = document.getLineOffset( firstLine );
		int end = document.getLineOffset( lastLine ) + document.getLineLength( lastLine )-1;
		
		if( end < start )
			end = start;
		
		return new Region( start, end-start );
	}

	private ISelectionProvider getSelectionProvider(){
		ITextEditor editor = getTextEditor();
		if( editor != null ){
			return editor.getSelectionProvider();
		}
		return null;
	}

	private IDocument getDocument(){
		ITextEditor editor = getTextEditor();
		if( editor != null ){
			IDocumentProvider provider = editor.getDocumentProvider();
			IEditorInput input = editor.getEditorInput();
			if( provider != null && input != null ){
				return provider.getDocument( input );
			}
		}
		return null;
	}

	private ITextSelection getSelection(){
		ISelectionProvider provider = getSelectionProvider();
		if( provider != null ){
			ISelection selection = provider.getSelection();
			if( selection instanceof ITextSelection )
				return (ITextSelection)selection;
		}

		return TextSelection.emptySelection();
	}
}
