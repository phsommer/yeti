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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.util.Assert;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;
import tinyos.yeti.ep.parser.*;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.ComponentASTModelNode;
import tinyos.yeti.nesc.parser.language.NesCCompletionProposal;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.DocumentUtility;
import tinyos.yeti.utility.IOConversion;

/*
 * configuration_implementation
	: IMPLEMENTATION '{' component_list connection_list '}' 
			{ 
			  $$ = new ConfigurationImplElement($1, $5);
			  $<Element>$.addChildElement($3); 
			  $<Element>$.addChildElement($4); 																
			} 
	| IMPLEMENTATION '{' connection_list '}'
			{ 
			  $$ = new ConfigurationImplElement($1, $4);
			  $<Element>$.addChildElement($3); 
			}
	;
 */
public class ConfigurationImplElement extends Element {

	public ConfigurationImplElement(Token token, Token token2) {
		super("Implementation", token, token2);
		image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_IMPLEMENTATION_CONFIGURATION);
	}
	
	public ComponentListElement getComponentList() {
		return (ComponentListElement) extractClass(children, ComponentListElement.class);
	}
	
	public ConnectionListElement getConnectionList() {
		return (ConnectionListElement) extractClass(children, ConnectionListElement.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<INesCCompletionProposal> getCompletionProposals( ProposalLocation location ) {
		IDocument document = location.getDocument().getDocument();
		int offset = location.getOffset();
		ProjectTOS project = location.getProject();
		String prefix = location.getPrefix();
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		
		// what is considered:
		// ---------------------------------------------------------
		//	Listing Modules:
		// ---------------------------------------------------------
		//	1.	[';'|'}'] WHITESPACES *
		//			should list all included components
		//			+ provided / used interfaces
		//	2.	[';'|'}'] WHITESPACES M*	
		//			should list all included components beginning with the letter m
		//			+ provided / used interfaces
		//	3.	[';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] *
		//			should list all included components
		//	4.	[';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai*
		//			should list all included components beginning with the letter Mai
		// ---------------------------------------------------------
		// Listing Interfaces:
		// ---------------------------------------------------------
		// 	5.	[';'|'}'] WHITESPACES Main.*
		//			should list all interfaces from main
		//	6.	[';'|'}'] WHITESPACES Main.S*
		//			should list all interfaces from main that begins with S
		//			(case insensitiv)
		//	7.	[';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai.*
		//			should list all interfaces from Mai which are valid for the wiring
		//	8.	[';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai.S*
		//			should list all interfaces from Mai which are valid for the wiring beginnig with S		
		// ---------------------------------------------------------
		// Listing Wiring Statements
		// ---------------------------------------------------------
		//  9.		[';'|'}'] WHITESPACES Main[.sadfs] *
		//			should list wiring statements

		int start = this.getOffset();
		int end = offset;
		String text;
        try {
            text = document.get(start,end-start);
        }
        catch( BadLocationException e ) {
            text = "";
            e.printStackTrace();
        }

		int lastIndexSemikolon = text.lastIndexOf(';');
		int lastIndexKlammer = text.lastIndexOf('}');
		
		int last = (lastIndexKlammer > lastIndexSemikolon) ? lastIndexKlammer : lastIndexSemikolon;
		
		text = text.substring(last+1);
		
		if (text.trim().equalsIgnoreCase("")) {
			// 1.
			result.addAll(listComponents("",offset));
			result.addAll(listProvUsedInterfaces("",offset));
		} else {
			
			if ((text.trim().endsWith("->"))||(text.trim().endsWith("<-"))||(text.trim().endsWith("="))) {
				// 3. 
				// @todo check if a  sadfsd | asdfsadf.sadfsd | adsfs.asdfs[sadf] 
				//		exists
				result.addAll(listComponents("",offset));
			} else if (text.endsWith(".")) {
				// Case 5 / 7
				
				// 7.
				if (((text.indexOf("->"))!=-1)||((text.indexOf("<-"))!=-1)||((text.indexOf("="))!=-1)){
					// @todo check if correct statement
					
					int s = -1;
					int op = -1;
					String[] ind = new String[]{"->","=","<-"};
					for (int i = 0; i < ind.length; i++) {
						String string = ind[i];
						if (text.indexOf(ind[i])!=-1) {
							s = text.indexOf(ind[i]);
							op = i;
						}
					}	
					
					String temp = text.substring(s+ind[op].length(),text.indexOf(".",s)).trim();
//					Collection<? extends CompletionProposal> te = givePossibleInterfaces(
//									text.substring(0,s).trim(), // leftside
//									ind[op], // operator
//									temp,//renamed component
//									prefix); 
//					if (te!=null) result.addAll(te);
					
					Collection<? extends INesCCompletionProposal> l = listInterfaces("",DocumentUtility.lastWord(document,offset-1),offset,";", project);
					if (l != null) result.addAll(l);
				} else {
					// 5.
					Collection<? extends INesCCompletionProposal> l = listInterfaces("",DocumentUtility.lastWord(document,offset-1),offset," ", project);
					if (l != null) result.addAll(l);
				}
				
			} else if (text.endsWith(" ")) {
				// 9.
				result.addAll(listWiringStatementsProposal(offset));
			} else {
				// text ends with letter..
				
				// in right side
				if (((text.indexOf("->"))!=-1)||((text.indexOf("<-"))!=-1)||((text.indexOf("="))!=-1)){
					int s = -1;
					int op = -1;
					String[] ind = new String[]{"->","=","<-"};
					for (int i = 0; i < ind.length; i++) {
						String string = ind[i];
						if (text.indexOf(ind[i])!=-1) {
							s = text.indexOf(ind[i]);
							op = i;
						}
					}				

					if (text.substring(s).indexOf(".")!=-1) {
						// 8. @todo give only correct wiring statements..
//						String temp = text.substring(s+1,text.indexOf(".",s)).trim();
//						Collection<? extends CompletionProposal> te = givePossibleInterfaces(
//										text.substring(0,s).trim(),
//										ind[op], // operator
//										temp,//renamed component
//										prefix); 
//						if (te!=null) result.addAll(te);
						result.addAll(listInterfaces(prefix,text.substring(0,text.indexOf(".")).trim(),offset,";", project));
					} else {
						// 4.
						result.addAll(listComponents(prefix,offset));
					}
					
				} else {
					// left side
					if (text.indexOf(".")!=-1) {
						// 6.
						result.addAll(listInterfaces(prefix,text.substring(0,text.indexOf(".")).trim(),offset," ", project));
					} else {
						// 2.
						result.addAll(listComponents(prefix,offset));
						result.addAll(listProvUsedInterfaces(prefix,offset));
					}
				}
			}
			
		}
			
			
		return result;
		
//		
//		/* detect bla.ss or bla. */ 
//		int hasDotLeft = hasDotOnLeftSide(offset,document);
//		
//		/* detect that ';'|'{'blablabla[.bla] is written and now it
//		 * would be expected that a wiring is written
//		 */ 
//		boolean leftSideComplete = isLeftSideComplete(offset,document);
//		
//		if (hasDotLeft!=-1) {
//			 else {
//			if (leftSideComplete) {
//				
//			} else {
//				
//			}
//		}
//		 
//		return result;
	}
	
	public static Collection<? extends INesCCompletionProposal> listWiringStatementsProposal(int offset) {
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		
		result.add(new NesCCompletionProposal( new CompletionProposal(
				"-> ", 						// replacement string
				offset, 			// replacement offset
				0,						// replacement length
				3,				// cursor position				
				null, //image
				"->", 						 //DisplayString
				null,   									 // IContextInformation
				"endpoint1 = endpoint2 (equate wires):\n" +
				"Any connection involving an external specification\n"+
				"element. These effectively make two specification elements equivalent.\n"+
				"\nLet S1 be the specification element of endpoint1 and S2 that of endpoint2.\n" +
				"One of the following two conditions must hold or a compile-time error occurs: \n"+
				"\n - S1 is internal, S2 is external (or vice-versa) \n" +
				"  and S1 and S2 are both provided or both used,\n"+
				"\n - S1 and S2 are both external and one is provided " +
				"   and the other used."		
				// additional proposal info
			)));
		
		result.add(new NesCCompletionProposal( new CompletionProposal(
				"= ", 						// replacement string
				offset, 			// replacement offset
				0,						// replacement length
				2,				// cursor position				
				null, //image
				"=", 						 //DisplayString
				null,   									 // IContextInformation
				"endpoint1 -> endpoint2 (link wires): \n\n" +
				"A connection involving two internal specification elements.\n\n" +
				"Link wires always connect a used specification element\n " +
				"specified by endpoint1 to a provided one specified by endpoint2.\n " +
				"If these two conditions do not hold, a compile-time error occurs."	 // additional proposal info
			)));
		
		result.add(new NesCCompletionProposal( new CompletionProposal(
				"<- ", 						// replacement string
				offset, 			// replacement offset
				0,						// replacement length
				3,				// cursor position				
				null, //image
				"<-", 						 //DisplayString
				null,   									 // IContextInformation
				"endpoint1 <- endpoint2 \n\nis equivalent to \n\nendpoint2 -> endpoint1."			 // additional proposal info
			)));
		
		return result;
	}

	private Collection<? extends INesCCompletionProposal> listInterfaces(String prefix,String renamedComponent, int offset, String addOn, ProjectTOS project ) {
	    if( project == null )
	        return Collections.emptyList();
	    
		Assert.isNotNull(addOn);
		ComponentListElement cle = getComponentList();
		Iterator<ComponentElement> iter = cle.getChildren().iterator();
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		
		// get the original name of the component
		while(iter.hasNext()) {
			ComponentElement c = iter.next();
			if (c.getRenamed().equals(renamedComponent)) {
				// fetch component model and extract interfaces
			    
			    ProjectModel model = project.getModel();
			    IDeclaration declaration = model.getDeclaration( c.getName(), Kind.CONFIGURATION, Kind.MODULE );
			    
			    if( declaration == null ){
			        break;
			    }
			    
			    IASTModelNode node = model.getNode( declaration, null );

			    if( node instanceof ComponentASTModelNode ){
			        ComponentASTModelNode component = (ComponentASTModelNode)node;
			        String[] interfaces = component.getUsesProvides();

			        String lowerPrefix = prefix.toLowerCase();
			        
			        for( String interfaze : interfaces ){
			            if( interfaze.toLowerCase().startsWith( lowerPrefix )){
			                String originalName = component.get( interfaze );
			                String label;
			                if( originalName == null || originalName.equals( name ))
			                    label = name;
			                else
			                    label = originalName + " as " + name;
			                
			                TagSet tags;
			                
			                if( component.getUses( interfaze ) != null )
			                    tags = TagSet.get( Tag.INTERFACE, Tag.USES );
			                else if( component.getProvides( interfaze ) != null )
                                tags = TagSet.get( Tag.INTERFACE, Tag.PROVIDES );
			                else
                                tags = TagSet.get( Tag.INTERFACE );
			                
			                CompletionProposal proposal = new CompletionProposal(
	                                interfaze+addOn,             // replacement string
	                                offset - prefix.length(),           // replacement offset
	                                prefix.length(),                    // replacement length
	                                interfaze.length()+addOn.length(),      // cursor position
	                                NesCIcons.icons().get( ASTModel.getImageFor( tags )), //image
	                                label,                               //DisplayString
	                                null,                               // IContextInformation
	                                getAdditionalProposalInfo( name )  // additional proposal info
	                            );
	                    
	                            result.add(new NesCCompletionProposal( proposal ));
			            }
			        }
			    }
				break;
			}
		}
		return result;
	}

	protected Collection<? extends INesCCompletionProposal> listProvUsedInterfaces(String prefix ,int offset) {
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		
		ConfigurationElement e = (ConfigurationElement) this.getParent();
		SpecificationElement[] interfaces = e.getInterfaces();
		
		for (int i = 0; i < interfaces.length; i++) {
			SpecificationElement ce = interfaces[i];
			if (ce.getRenamed().toLowerCase().startsWith(prefix.toLowerCase())) {
				
				CompletionProposal proposal = new CompletionProposal(
						ce.getRenamed(), 					// replacement string
						offset - prefix.length(), 			// replacement offset
						prefix.length(),					// replacement length
						ce.getRenamed().length(),			// cursor position				
						ce.getImageDescriptor(null).createImage(), //image
						ce.getLabel(null), 					//DisplayString
						null,   							// IContextInformation
						getAdditionalProposalInfo(ce)		// additional proposal info
				);
			
				result.add(new NesCCompletionProposal( proposal ));		
			}	
		}
		return result;
		
	}

	protected Collection<? extends INesCCompletionProposal> listComponents(String prefix ,int offset) {
		ComponentListElement cle = getComponentList();
		Iterator<ComponentElement> iter = cle.getChildren().iterator();
		ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();
		
		while(iter.hasNext()){
			ComponentElement ce = iter.next();
			if (ce.getRenamed().toLowerCase().startsWith(prefix.toLowerCase())) {
				
				CompletionProposal proposal = new CompletionProposal(
						ce.getRenamed(), 					// replacement string
						offset - prefix.length(), 			// replacement offset
						prefix.length(),					// replacement length
						ce.getRenamed().length(),			// cursor position				
						ce.getImageDescriptor(null).createImage(), //image
						ce.getLabel(null), 					//DisplayString
						null,   							// IContextInformation
						getAdditionalProposalInfo(ce)		// additional proposal info
				);
			
				result.add( new NesCCompletionProposal( proposal )); 
			}
		}
		return result;
	}

	private String getAdditionalProposalInfo(SpecificationElement e) {
		return getAdditionalProposalInfo(e.getName());
	}
	
	public static String getAdditionalProposalInfo(String s) {
		final int maxDisplay = 400;
		File f = TinyOSPlugin.getDefault().locate(s+".nc",false,null);
		if ((f == null)||(!f.isFile())) return "";
		String result = null;
		
		FileReader fr = null;
		Document d;
		IDocumentPartitioner partitioner = null;
		try {
			fr = new FileReader(f);
			String content = IOConversion.getStringFromStream(fr);
			
			d = new Document(content);
			
			partitioner = new NesCDocumentPartitioner();
			
			partitioner.connect(d);
			
			ITypedRegion[] regions = partitioner.computePartitioning(0,d.getLength());
			
			// Display code from the first nescdoc comment...
			for (int i = 0; i < regions.length; i++) {
				ITypedRegion region = regions[i];
				
				if (region.getType().equals(INesCPartitions.NESC_DOC)) {
					if (d.getLength()-region.getOffset() > maxDisplay) {
						result = d.get(region.getOffset(),maxDisplay) + "...";
					} else {
						result = d.get(region.getOffset(),d.getLength()-region.getOffset());	
					}
					
					break;
				}
			}
			// No nescdoc comment is set -> display the code from first keyword
			if (result==null) {
				for (int i = 0; i < regions.length; i++) {
					ITypedRegion region = regions[i];
					
					// skip very small nesc regions.. (spaces)
					if ((region.getType().equals(INesCPartitions.DEFAULT))&&(region.getLength() > 200)) {
						if (d.getLength()-region.getOffset() > maxDisplay) {
							result = d.get(region.getOffset(),maxDisplay) + "...";
						} else {
							result = d.get(region.getOffset(),d.getLength()-region.getOffset());	
						}
						
						break;
					}
				}
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			partitioner.disconnect();
			if (result == null) result = "";
			try {
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result.trim();
	}
	
	private String getAdditionalProposalInfo(ComponentElement c) {
		return getAdditionalProposalInfo(c.getName());
	}

	/*
	 * todo...
	 * @param leftside  ( Component[.interface])
	 * @param wiringOperator ConnectionElement.WIRING_EQUALS, etc.
	 * @param renamedComponent component name
	 * @param prefix	(e.g prefix of interface name of component)
	 * @return
	 *
	private Collection<? extends CompletionProposal> givePossibleInterfaces(String leftside, String operator, String renamedComponent, String prefix)  {
		ArrayList<CompletionProposal> result = new ArrayList<CompletionProposal>();
		// fetch model of left component
		NesCModel leftModel = null;
		NesCModel rightModel = null;
		String renamedLeftComponent;
		if (leftside.indexOf(".")!=-1) {
			renamedLeftComponent = leftside.substring(0,leftside.indexOf(".")).trim();
		} else {
			renamedLeftComponent = leftside.trim();
		}
		
		// fetch the real name of the component
		Iterator iter = getComponentList().getChildren().iterator();
		String nameLeft = null;
		String nameRight = null;
		while(iter.hasNext()) {
			ComponentElement ce = (ComponentElement) iter.next();
			if (ce.getRenamed().equals(renamedLeftComponent)) {
				nameLeft = ce.getName();
			} if (ce.getRenamed().equals(renamedComponent)) {
				nameRight = ce.getName();
			}
		}
			
		leftModel = TinyOSPlugin.getDefault().getModel(nameLeft);
		rightModel = TinyOSPlugin.getDefault().getModel(nameRight);
		if ((leftModel == null)||(leftModel == null)) return null;
		
		
		// construct Endpoints 
		EndpointElement leftEndpoint = new EndpointElement("left",new Token(1,"", 0,1,1),new Token(1,"", 0,1,1));
		if (leftside.indexOf(".")!=-1) {
			leftEndpoint.setComponentElementName(nameLeft);
			leftEndpoint.setSpecificationElementName(leftside.substring(leftside.indexOf(".")).split("\\[")[0]);
		} else {
			leftEndpoint.setComponentOrExternalSpecificationName(nameLeft);
		}
		EndpointElement rightEndpoint = new EndpointElement("right",new Token(1,"", 0,1,1),new Token(1,"", 0,1,1));
		rightEndpoint.setComponentOrExternalSpecificationName(renamedComponent);
		
		ConnectionElement connection = new ConnectionElement("c",null,new Token(1,"", 0,1,1));
		connection.setLeft(leftEndpoint);
		connection.setRigth(rightEndpoint);
		connection.setOperator(operator);	
		
		// specificationelements can be renamed.. extract the element 
		ISpecificationElement left = null;
		if (leftModel.type == NesCModel.CONFIGURATION) {
			left = ((ConfigurationElement)leftModel.getTypeElement()).getSpecificationElement(leftEndpoint.getComponentElementName());
		} else if (leftModel.type == NesCModel.MODULE) {
			left = ((ModuleElement)leftModel.getTypeElement()).getSpecificationElement(leftEndpoint.getComponentElementName());
		} else {//if (leftModel.type == NesCModel.UNDEFINED) {
			// Error parsing the file
			// return as if the file had same interface name (assume that its is correct)
			// 	Example: TimerM.PowerManagement -> HPLPowerManagementM;
			//		return PowerManagement
			return null;		
		}
		
		// set function flag
		if (left instanceof DeclarationElement) {
			connection.setWiresFunctionEndpoints(true);
		}
		
		// extract specification list 
		SpecificationListElement right = null;
		if (rightModel.type == NesCModel.CONFIGURATION) {
			right = ((ConfigurationElement) rightModel.getTypeElement()).getSpecification();
		} else if (rightModel.type == NesCModel.MODULE) {
			right = ((ModuleElement) rightModel.getTypeElement()).getSpecification();
		} else if (rightModel.type == NesCModel.UNDEFINED) {
			// Error parsing the file
			// return as if the file had same interface name (assume that its is correct)
			// 	Example: TimerM.PowerManagement -> HPLPowerManagementM;
			//		return PowerManagement
			return null;			
		}
		
		// check if one specification matches.. 
		iter = right.getChildren().iterator();
		String interfaceName = null;
		
		while(iter.hasNext()) {
			ISpecificationElement se = (ISpecificationElement) iter.next();
			if (se.getName().equals(left.getName())) {
				if ((se.isProvides()) == (!left.isProvides())) {
					interfaceName = se.getName();	
					result.add(new CompletionProposal(
						se.getRenamed(),
						0,
						se.getRenamed().length(),
						0,
						null, // image
						se.getLabel(null), 
						null, //icontect
						""  // additional info
					));
				} else {
					//throw new IncompatibleWiringStatementException();
				}
			}
		}
		// reset counter
		
		
		// if there are function elements / if only one ist matching (the name doesn't bother)
		// .. then ok
		if (connection.wiresFunctions) {
			
			iter = right.getChildren().iterator();			
			while(iter.hasNext()) {
				ISpecificationElement se = (ISpecificationElement) iter.next();
				if (se instanceof DeclarationElement) {
					if ((se.isProvides()) == (!left.isProvides())) {
						if (((DeclarationElement)se).getType() ==  
							 ((DeclarationElement)left).getType()) {
					
							interfaceName = se.getName();	
							result.add(new CompletionProposal(
									se.getRenamed(),
									0,
									se.getRenamed().length(),
									0,
									null, // image
									se.getLabel(null), 
									null, //icontect
									""  // additional info
							));
						}
					}
				}
			}
		}
	
		
		return result;
	}
	*/
}
