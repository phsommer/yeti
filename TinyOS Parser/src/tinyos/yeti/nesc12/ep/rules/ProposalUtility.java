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
package tinyos.yeti.nesc12.ep.rules;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.ProposalLocation;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.rules.proposals.NesC12CompletionProposal;
import tinyos.yeti.nesc12.ep.rules.proposals.TemplateCompletionProposal;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.preprocessor.RangeDescription;

public class ProposalUtility{

    /**
     * Creates a new proposal that proposes to insert <code>field</code> at
     * the given location
     * @param field the field to insert, <code>null</code> will be ignored
     * @param location the location where to insert the field
     * @param ast the ast into which the proposal will be inserted
     * @return the new proposal or <code>null</code> if <code>field</code> can't
     * be inserted
     */
    public static INesCCompletionProposal createProposal( Field field, ProposalLocation location, NesC12AST ast ){
        return createProposal( field, location, true, ast );
    }

    public static INesCCompletionProposal createProposal( Field field, ProposalLocation location, boolean functionArguments, NesC12AST ast ){
        if( field == null )
            return null;

        Name name = field.getName();
        if( name == null )
            return null;

        Type type = field.getType();
        String identifier = name.toIdentifier();
        
        if( location.getPrefix() != null && !RuleUtility.startsWithIgnoreCase( identifier, location.getPrefix() ))
            return null;
        
        String replacementString;
        int replacementOffset = location.getOffset() - location.getPrefix().length();
        int replacementLength = location.getPrefix().length();
        int curserPosition = identifier.length();
        String displayString;

        TagSet tags;
        FieldModelNode node = field.asNode();
        if( node != null )
            tags = node.getTags();
        else{
            if( type == null && type.asFunctionType() == null )
                tags = TagSet.get( NesC12ASTModel.FIELD );
            else{
                tags = TagSet.get( Tag.FUNCTION );
            }

            Modifiers modifiers = field.getModifiers();
            if( modifiers != null ){
                if( modifiers.isAsync() )
                    tags.add( Tag.ASYNC );
                if( modifiers.isTask() )
                    tags.add( Tag.TASK );
                if( modifiers.isCommand() )
                    tags.add( Tag.COMMAND );
                if( modifiers.isEvent() )
                    tags.add( Tag.EVENT );
            }
        }

        if( type == null )
            displayString = identifier;
        else
            displayString = identifier + " - " + type.toLabel( identifier, Type.Label.SMALL );

        if( type == null || type.asFunctionType() == null || !functionArguments ){
            replacementString = identifier;

            NesC12CompletionProposal proposal = new NesC12CompletionProposal(
                    replacementString, 
                    replacementOffset,
                    replacementLength, 
                    curserPosition,
                    NesCIcons.icons().get( NesC12ASTModel.getImageFor( tags ) ),
                    displayString,
                    null,
                    replacementString );

            proposal.setInFile( inFile( field, ast.getParseFile() ) );

            return proposal;
        }
        else{
            FunctionType function = type.asFunctionType();

            StringBuilder builder = new StringBuilder();
            StringBuilder infoBuilder = new StringBuilder();
            
            builder.append( identifier );
            infoBuilder.append( identifier );
            
            if( field.getIndices() != null ){
                builder.append( "[" );
                infoBuilder.append( "[" );
                Field[] indices = field.getIndices();
                for( int i = 0, n = indices.length; i<n; i++ ){
                    if( i > 0 ){
                        builder.append( ", " );
                        infoBuilder.append( ", " );
                    }
                    
                    if( indices[i] == null ){
                        argumentTemplate( null, null, "par" + i, builder, infoBuilder );
                    }
                    else{
                        argumentTemplate( indices[i].getName(), indices[i].getType(), "par"+i, builder, infoBuilder );
                    }
                }
                builder.append( "]" );
                infoBuilder.append( "]" );
            }
            builder.append( "(" );
            infoBuilder.append( "(" );
            Name[] argumentNames = field.getArgumentNames();

            for( int i = 0, n = function.getArgumentCount(); i<n; i++ ){
                if( i > 0 ){
                    builder.append( ", " );
                    infoBuilder.append( ", " );
                }
                
                Name argumentName;
                if( argumentNames != null && i < argumentNames.length )
                    argumentName = argumentNames[i];
                else
                    argumentName = null;
                
                argumentTemplate( argumentName, function.getArgument( i ), "arg" + i, builder, infoBuilder );
            }
            builder.append( ")" );
            infoBuilder.append( ")" );
            replacementString = builder.toString();
            
            TemplateCompletionProposal proposal =  new TemplateCompletionProposal(
                        identifier,
                        replacementString,
                        replacementOffset,
                        NesCIcons.icons().get( NesC12ASTModel.getImageFor( tags )),
                        displayString, 
                        null,
                        infoBuilder.toString() );


            proposal.setInFile( inFile( field, ast.getParseFile() ) );

            return proposal;
        }
        
    }
    
    private static void argumentTemplate( Name name, Type type, String var, StringBuilder builder, StringBuilder infoBuilder ){
        builder.append( "${" );
        builder.append( var );
        builder.append( "," );
        builder.append( "\"" );
        if( type == null && name == null ){
            builder.append( var );
            infoBuilder.append( "?" );
        }
        else if( type == null ){
            String text = name.toIdentifier();
            builder.append( text );
            infoBuilder.append( text );
        }
        else if( name == null ){
            String text = type.toLabel( null, Type.Label.DECLARATION );
            builder.append( text );
            infoBuilder.append( text );
        }
        else{
            String text = type.toLabel( name.toIdentifier(), Type.Label.DECLARATION );
            builder.append( text );
            infoBuilder.append( text );
        }
        builder.append( "\"" );
        builder.append( "}" );
    }

    /**
     * Creates a proposal that will insert something like "command interfaze.field( int x, int y ){}"
     * into the source.
     * @param interfaze the interface whose method is added
     * @param field the field to insert
     * @param begin the first character to replace
     * @param whitespaces a string that will be inserted at the beginning of any
     * newline.
     * @param location the current location of the cursor
     * @return the new proposal or <code>null</code>
     */
    public static INesCCompletionProposal createProposal( NesCInterfaceReference interfaze, Field field, int begin, String whitespaces, ProposalLocation location ){
        Name name = field.getName();
        if( name == null )
            return null;

        Type type = field.getType();
        if( type == null )
            return null;

        if( type.asFunctionType() == null )
            return null;

        String identifier = name.toIdentifier();
        if( location.getPrefix() != null && !RuleUtility.startsWithIgnoreCase( identifier, location.getPrefix() ))
            return null;

        int replacementOffset = begin;
        int replacementLength = location.getOffset() - begin;

        StringBuilder replacementString = new StringBuilder();
        StringBuilder infoString = new StringBuilder();
        
        appendTo( field.getDeclaration( interfaze.getName() + "." + identifier ), replacementString, infoString );
        appendTo( "{\n", replacementString, infoString );
        replacementString.append( whitespaces );
        replacementString.append( RuleUtility.getTab() );
        int curserPosition = replacementString.length();
        replacementString.append( "\n" );
        replacementString.append( whitespaces );
        appendTo( "}", replacementString, infoString );
        replacementString.append( "\n" );
        replacementString.append( whitespaces );

        String displayString = identifier;
        Image image = null;

        TagSet tags = field.asNode() == null ? null : field.asNode().getTags();
        if( tags == null ){
            tags = TagSet.get( Tag.FUNCTION );
            Modifiers modifiers = field.getModifiers();
            if( modifiers != null ){
                if( modifiers.isEvent() )
                    tags.add( Tag.EVENT );
                if( modifiers.isCommand() )
                    tags.add( Tag.COMMAND );
                if( modifiers.isAsync() )
                    tags.add( Tag.ASYNC );
            }
        }
        
        if( tags != null ){
            ImageDescriptor descriptor = NesC12ASTModel.getImageFor( tags );
            if( descriptor != null ){
                image = NesCIcons.icons().get( descriptor );
            }
        }

        NesC12CompletionProposal proposal = new NesC12CompletionProposal(
                replacementString.toString(), 
                replacementOffset,
                replacementLength, 
                curserPosition,
                image ,
                displayString,
                null,
                infoString.toString() );

        proposal.setInFile( true );

        return proposal;
    }

    /**
     * Creates a proposal to insert <code>declaration</code> at the given <code>location</code>.
     * @param declaration the declaration to insert
     * @param location where to insert
     * @param ast the ast into which the declaration will be inserted
     * @return the new proposal or <code>null</code> if the input is not valid
     */
    public static INesCCompletionProposal createProposal( IDeclaration declaration, ProposalLocation location, NesC12AST ast ){
        return createProposal( declaration, location, null, null, ast );
    }


    /**
     * Creates a proposal to insert <code>declaration</code> at the given <code>location</code>.
     * @param declaration the declaration to insert
     * @param location where to insert
     * @param prefix an optional string that will be put in front of the insert
     * @param postfix an optional string that will be put after the insert
     * @param ast the ast into which the declaration will be inserted
     * @return the new proposal or <code>null</code> if the input is not valid
     */
    public static INesCCompletionProposal createProposal( IDeclaration declaration, ProposalLocation location, String prefix, String postfix, NesC12AST ast ){
        BaseDeclaration base = (BaseDeclaration)declaration;

        if( base == null )
            return null;

        String identifier = base.getName();

        if( location.getPrefix() != null && !RuleUtility.startsWithIgnoreCase( identifier, location.getPrefix() ))
            return null;

        String replacementString;
        if( prefix == null && postfix == null )
            replacementString = identifier;
        else if( prefix == null )
            replacementString = identifier + postfix;
        else if( postfix == null )
            replacementString = prefix + identifier;
        else
            replacementString = prefix + identifier + postfix;

        int replacementOffset = location.getOffset() - location.getPrefix().length();
        int replacementLength = location.getPrefix().length();
        int curserPosition = replacementString.length();
        String displayString = base.getLabel();
        Image image = null;

        TagSet tags = base.getTags();
        if( tags != null ){
            ImageDescriptor descriptor = NesC12ASTModel.getImageFor( tags );
            if( descriptor != null ){
                image = NesCIcons.icons().get( descriptor );
            }
        }

        NesC12CompletionProposal proposal = new NesC12CompletionProposal(
                replacementString, 
                replacementOffset,
                replacementLength, 
                curserPosition,
                image ,
                displayString,
                null,
                null );

        proposal.setInFile( inFile( declaration, ast.getParseFile() ) );

        return proposal;
    }

    public static INesCCompletionProposal createProposal( ComponentReferenceModelConnection reference, ProposalLocation location ){
        if( reference == null )
            return null;

        String name = reference.getName();
        return createProposal( name, reference, location );
    }

    public static INesCCompletionProposal createProposal( InterfaceReferenceModelConnection reference, ProposalLocation location ){
        if( reference == null )
            return null;

        Name name = reference.getName();
        return createProposal( name, reference, location );
    }

    public static INesCCompletionProposal createProposal( Name name, ModelConnection reference, ProposalLocation location ){
        if( name == null )
            return null;

        String identifier = name.toIdentifier();
        return createProposal( identifier, reference, location );
    }

    public static INesCCompletionProposal createProposal( String identifier, ModelConnection reference, ProposalLocation location ){
        if( identifier == null )
            return null;

        if( location.getPrefix() != null && !RuleUtility.startsWithIgnoreCase( identifier, location.getPrefix() ))
            return null;

        String replacementString = identifier;
        int replacementOffset = location.getOffset() - location.getPrefix().length();
        int replacementLength = location.getPrefix().length();
        int curserPosition = identifier.length();

        TagSet tags = reference.getTags();

        NesC12CompletionProposal proposal = new NesC12CompletionProposal(
                replacementString, 
                replacementOffset,
                replacementLength, 
                curserPosition,
                NesCIcons.icons().get( NesC12ASTModel.getImageFor( tags ) ),
                replacementString,
                null,
                null );

        proposal.setInFile( true );

        return proposal;
    }

    /**
     * Creates a proposal for inserting <code>identifier</code> such that the
     * statement resolves to the type <code>type</code>.
     * @param name the name the type is supposed to have. This method
     * will always use <code>identifier</code> even if <code>type</code> 
     * itself contains enough information to use another name.
     * @param type the type to insert
     * @param writeKeywords whether to write keywords like "struct" or "enum"
     * @param location where to insert the type
     * @param ast the ast into which the type will be inserted
     * @return the new proposal or <code>null</code>
     */
    public static INesCCompletionProposal createProposal( Name name, Type type, boolean writeKeywords, ProposalLocation location, NesC12AST ast ){
        if( name == null || type == null )
            return null;

        String identifier = name.toIdentifier();

        if( location.getPrefix() != null && !RuleUtility.startsWithIgnoreCase( identifier, location.getPrefix() ))
            return null;

        String replacementString;

        if( writeKeywords ){
            if( type.asTypedefType() != null ){
                replacementString = identifier;

            }
            else if( type.asDataObjectType() != null ){
                switch( type.asDataObjectType().getKind() ){
                    case NX_STRUCT:
                        replacementString = "nx_struct " + identifier;
                        break;
                    case NX_UNION:
                        replacementString = "nx_union " + identifier;
                        break;
                    case STRUCT:
                        replacementString = "struct " + identifier;
                        break;
                    case UNION:
                        replacementString = "union " + identifier;
                        break;
                    default:
                        replacementString = identifier;
                }
            }
            else if( type.asEnumType() != null ){
                replacementString = "enum " + identifier;
            }
            else{
                replacementString = identifier;
            }
        }
        else{
            replacementString = identifier;
        }

        int replacementOffset = location.getOffset() - location.getPrefix().length();
        int replacementLength = location.getPrefix().length();
        int curserPosition = replacementString.length();
        String displayString = identifier + " - " + type.toLabel( null, Type.Label.EXTENDED );

        TagSet tags = TagSet.get( NesC12ASTModel.TYPE );
        if( type.asTypedefType() != null )
            tags.add( NesC12ASTModel.TYPEDEF );

        NesC12CompletionProposal proposal = new NesC12CompletionProposal(
                replacementString, 
                replacementOffset,
                replacementLength, 
                curserPosition,
                NesCIcons.icons().get( NesC12ASTModel.getImageFor( tags ) ),
                displayString,
                null,
                null );

        proposal.setInFile( inFile( name, ast.getParseFile() ) );

        return proposal;
    }

    private static void appendTo( String text, StringBuilder alpha, StringBuilder beta ){
        alpha.append( text );
        beta.append( text );
    }
    
    public static boolean inFile( IDeclaration declaration, IParseFile file ){
        if( file == null )
            return false;

        return file.equals( declaration.getParseFile() );
    }

    public static boolean inFile( Field field, IParseFile file ){
        return inFile( field.getRange(), file );
    }

    public static boolean inFile( Name name, IParseFile file ){
        return inFile( name.getRange(), file );
    }

    public static boolean inFile( RangeDescription range, IParseFile file ){
        if( range == null || file == null )
            return false;

        for( int i = 0, n = range.getRangeCount(); i<n; i++ ){
            NesC12FileInfo info = (NesC12FileInfo)range.getRange( i ).file();
            if( info != null ){
                if( file.equals( info.getParseFile() ))
                    return true;
            }
        }

        return false;
    }
}
