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
package tinyos.yeti.nesc12.ep.rules.hyperlink;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCName;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.preprocessor.RangeDescription;

public class NesCNameHyperlink implements IHyperlinkRule{
	public void search( NesC12AST ast, HyperlinkCollector collector ){
		ASTNode node = collector.getNode();

		if( !(node instanceof Identifier ))
			return;

		ASTNode nodeNesCName = node.getParent();

		// collect the possible interface references
		Identifier interfaze = null;
		ASTNode function = null;
		String functionName = null;

		if( nodeNesCName instanceof NesCNameDeclarator ){
			interfaze = ((NesCNameDeclarator)nodeNesCName).getInterface();
			function = ((NesCNameDeclarator)nodeNesCName).getFunctionName();
			if( function == null )
				return;
			functionName = ((Identifier)function).getName();
		}
		else{
			if( nodeNesCName instanceof ParameterizedIdentifier ){
				nodeNesCName = nodeNesCName.getParent();
			}

			if( nodeNesCName instanceof NesCName ){
				interfaze = ((NesCName)nodeNesCName).getInterface();
				ParameterizedIdentifier identifier = ((NesCName)nodeNesCName).getFunction();
				function = identifier;
				if( identifier == null )
					return;

				if( identifier.getIdentifier() == null )
					return;

				functionName = identifier.getIdentifier().getName();
			}
		}

		if( interfaze == null ){
			// might access a local field
			if( functionName != null ){
				IFileRegion source = collector.getSourceRegion();
				if( source != null ){
					List<Field> fields = ast.getRanges().getFields( collector.getOffset().getInputfileOffset() );
					for( Field field : fields ){
						Name name = field.getName();
						if( name != null && name.toIdentifier().equals( functionName )){
							RangeDescription target = name.getRange();
							IFileRegion targetRegion = RuleUtility.source( target );
			                if( targetRegion != null ){
								collector.add( new FileHyperlink( source, targetRegion ));
							}
						}
					}
				}
			}
		}
		else{
			// needs to jump to another file

			List<InterfaceReferenceModelConnection> references = ast.getRanges().getInterfaceReferences( collector.getOffset().getInputfileOffset() );
			List<InterfaceReferenceModelConnection> usefulReferences = new ArrayList<InterfaceReferenceModelConnection>();

			for( InterfaceReferenceModelConnection reference : references ){
				if( reference.getName().toIdentifier().equals( interfaze.getName() )){
					usefulReferences.add( reference );
				}
			}

			if( interfaze == node ){
				// jump to the interface reference
				IFileRegion sourceRegion = collector.getSourceRegion();

				for( InterfaceReferenceModelConnection reference : usefulReferences ){
					IFileRegion targetRegion = reference.getRegion();
					if( targetRegion != null ){
						collector.add( new FileHyperlink( sourceRegion, targetRegion ) );
					}
				}
			}
			else{
				// jump to a function within the interface
				for( InterfaceReferenceModelConnection reference : usefulReferences ){
					NesCInterfaceReference interfaceReference = reference.resolve( ast.getBindingResolver() );
					NesCInterface raw = interfaceReference.getRawReference();
					if( raw != null ){
						Field field = raw.getField( functionName );
						if( field != null ){
							RangeDescription range = field.getRange();
							
							IFileRegion targetRegion = RuleUtility.source( range );
			                if( targetRegion != null ){
								collector.add( new FileHyperlink( collector.getSourceRegion(), targetRegion ));
							}
						}
					}
				}
			}
		}
	}

}
