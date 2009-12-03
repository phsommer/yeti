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
package tinyos.yeti.nesc.parser.language.elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.rules.FastPartitioner;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.nesc.CTemplateCompletionProcessor;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;
import tinyos.yeti.ep.parser.*;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.FunctionASTModelNode;
import tinyos.yeti.nesc.parser.language.NesCCompletionProposal;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.DocumentUtility;
import tinyos.yeti.utility.IOConversion;

public class ImplementationElement extends Element {

	public static final String SKELETON = NEWLINE +"implementation" + NEWLINE +
										  "{" + NEWLINE + NEWLINE +
										  "}"+NEWLINE;
	Token implement;

	public ImplementationElement(Token t1, Token t2) {
		super("Implementation",t1,t2);
		implement = t1;
		image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_PLAIN_PAGE);
	}
	
	@Override
	public void updatePosition(DirtyRegion region) {
		super.updatePosition(region);
		if (implement != null) {
			if (implement.updatePosition(region)) implement = null;
		}

	}
	
	public Position getPositionForOutline() {
		return new Position(implement.offset,implement.length());
	}
	
	@Override
    public boolean isFoldable() {
		return true;
	}
	
	@Override
	public ArrayList<INesCCompletionProposal> getCompletionProposals( ProposalLocation location ){
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		int offset = location.getOffset();
		
		// fetch all component names
		ModuleElement me = (ModuleElement) this.getParent();
		SpecificationListElement sle = me.getSpecification();
		Iterator iter = sle.getChildren().iterator();
		
		int start = this.getOffset();
		int end = offset;
		String text;
		try{
		    text = location.getDocument().getDocument().get(start,end-start);
		}
		catch( BadLocationException ex ){
		    ex.printStackTrace();
		    text = "";
		}
		
		String prefix = location.getPrefix();
		ProjectTOS project = location.getProject();
		
		while(iter.hasNext()) {
			SpecificationElement se = (SpecificationElement) iter.next();
			
			if (text.endsWith("."+prefix)) {
			    if( project != null ){
			        // fetch command events..
			        String interfaceRenamed = DocumentUtility.lastWord( location.getDocument().getDocument(), offset-prefix.length()-1);
			        if (se.getRenamed().equals(interfaceRenamed)) {
			            // fetch interfacemodel

			            ProjectModel model = project.getModel();
			            
			            IDeclaration declaration = model.getDeclaration( se.getName(), Kind.INTERFACE );
			            if( declaration != null ){
			                model.freeze( declaration.getParseFile() );
			                try{
			                    IASTModelNode node = model.getNode( declaration, null );
			                    if( node != null ){
			                        IASTModelNode[] nodes = model.getCacheModel().getNodes( node.getPath(), null, TagSet.get( Tag.FUNCTION ) );
			                        String lowerPrefix = prefix.toLowerCase();
			                        for( IASTModelNode function : nodes ){
			                            String fname = function.getIdentifier();
			                            if( fname.toLowerCase().startsWith( lowerPrefix )){
			                                if( function instanceof FunctionASTModelNode ){
			                                    int cp = ((FunctionASTModelNode)function).getParameterCount();
			                                    result.add(new NesCCompletionProposal(new CompletionProposal(
			                                            fname+"()", // Replacement
			                                            offset - prefix.length(),  // Replacement offset
			                                            prefix.length(),    // Replacement length
			                                            fname.length()+cp,  // Cursor Position
			                                            NesCIcons.icons().get( ASTModel.getImageFor( function.getTags() ) ),    // Image
			                                            function.getLabel(), // Display-string
			                                            null, // i-context info
			                                            getInterfaceFunctionInfo( se.getName(), function )))     // additional proposal info
			                                    );
			                                }
			                            }
			                        }
			                    }
			                }
			                finally{
			                    model.melt( declaration.getParseFile() );
			                }
			            }
			        }
			    }
			}
			else {
				if (se.getRenamed().toLowerCase().startsWith(prefix.toLowerCase())) {
					result.add(new NesCCompletionProposal(new CompletionProposal(
						se.getRenamed(), // Replacement
						offset - prefix.length(),  // Replacement offset
						prefix.length(),	// Replacement length
						se.getRenamed().length(),	// Cursor Position
						se.getImageDescriptor(null).createImage(),	// Image
						se.getLabel(null), // Display-string
						null,	// i-context info
						""		// additional proposal info
					)));
				}
			}
		}	
		
		// Typedefs
		if (!text.endsWith("."+prefix)) {
			// fetch all typedefs
			String name = this.getParent().getName();
			if( project != null ){
			    List<IDeclaration> declList = project.getModel().getDeclarations( IDeclaration.Kind.TYPEDEF );
			    IDeclaration[] decls = declList.toArray( new IDeclaration[ declList.size() ] );
			    
			    Arrays.sort(decls, new Comparator<IDeclaration>(){
			        public int compare(IDeclaration o1, IDeclaration o2) {
			            return o1.getName().compareToIgnoreCase(o2.getName());
			        }

			    });
			    
			    String lowerPrefix = prefix.toLowerCase();
			    for (int i = 0; i < decls.length; i++) {
			        IDeclaration declaration = decls[i];

			        if( declaration.getName().toLowerCase().startsWith( lowerPrefix )) {
			            
			            TagSet tags = declaration.getTags();
			            if( tags == null )
			                tags = TagSet.get();
			            
			            result.add(new NesCCompletionProposal( new CompletionProposal(
			                    declaration.getName(), // Replacement
			                    offset - prefix.length(),  // Replacement offset
			                    prefix.length(),	// Replacement length
			                    declaration.getName().length(),	// Cursor Position
			                    NesCIcons.icons().get( ASTModel.getImageFor( tags ) ),	// Image
			                    declaration.getName(), // Display-string
			                    null,	// i-context info
			                    ""		// additional proposal info
			            )));
			        } else {

			        }
			    }
			}
			
			// Add C Templates
			CTemplateCompletionProcessor template = new CTemplateCompletionProcessor();
			ICompletionProposal[] is = template.computeCompletionProposals(location.getViewer(),offset);
			if (is != null) {
			    for( ICompletionProposal proposal : is ){
			        result.add( new NesCCompletionProposal( proposal ) );
			    }
			}
		}
		return result;
	}

	private String getInterfaceFunctionInfo(String interfaceName, IASTModelNode function) {
		final int maxDisplay = 400;
		File f = TinyOSPlugin.getDefault().locate(interfaceName+".nc",false,null);
		String result = "";
		
		FileReader fr = null;
		Document d;
		IDocumentPartitioner partitioner = null;
		try {
			fr = new FileReader(f);
			String content = IOConversion.getStringFromStream(fr);
			
			d = new Document(content);
			
			partitioner = new NesCDocumentPartitioner();
			
			partitioner.connect(d);
			
//			ITypedRegion[] regions = partitioner.computePartitioning(0,d.getLength());
			IFileRegion region = function.getRegion();
			if( region == null )
			    return null;
			
			int line = d.getLineOfOffset( region.getOffset() );
			
			ITypedRegion r = partitioner.getPartition(d.getLineOffset(line-1));
			if (r.getType().equals(INesCPartitions.NESC_DOC)) {
				result = d.get(r.getOffset(),r.getLength());
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			partitioner.disconnect();
			try {
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result.trim();
	}

}
