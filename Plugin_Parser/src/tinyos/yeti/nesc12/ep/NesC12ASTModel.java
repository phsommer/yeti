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
package tinyos.yeti.nesc12.ep;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagDescription;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModel;

public class NesC12ASTModel extends ASTModel{
    public static final Tag UNIT = new Tag( "unit", true, new TagDescription( "File", "Single *.nc or *.h file", false ) );
    public static final Tag TYPEDEF = new Tag( "typedef", true, new TagDescription( "Typedef", null, false ) );
    public static final Tag PARAMETERS = new Tag( "parameters", true );
    
    public static final Tag ENUMERATION = new Tag( "enumeration", false, new TagDescription( "Enumeration", null, false ) );
    public static final Tag ENUMERATION_CONSTANT = new Tag( "enum constant", true, new TagDescription( "Enumeration Constant", "Constant declared in an enumeration", true ) );
    
    public static final Tag FIELD = new Tag( "field", true, new TagDescription( "Field", null, true ) );
    
    public static final Tag TYPE = new Tag( "type", true, new TagDescription( "Type", "E.g. a struct, a union, a typedef", true ) );
    
    public static final Tag ERROR = new Tag( "error", false );
    public static final Tag WARNING = new Tag( "warning", false );
    
    public static final Tag GENERIC = new Tag( "generic", false, new TagDescription( "Generic", null, false ) );
    
    public static final Tag COMPLETE_FUNCTION = new Tag( "complete function", false, new TagDescription( "Complete Function", "A function with body", false ) );
    
    public static final Tag INCLUDES = new Tag( "includes", true );
    
    private static final TagSet DECORATING = TagSet.get( ERROR, WARNING );
    
    public static TagSet getSupportedTags(){
    	TagSet set = ASTModel.getSupportedTags();
    	
    	set.add( UNIT );
    	set.add( FIELD );
    	set.add( TYPEDEF );
    	set.add( PARAMETERS );
        set.add( ENUMERATION );
    	set.add( ENUMERATION_CONSTANT );
        set.add( TYPE );
        set.add( ERROR );
    	set.add( WARNING );
        set.add( GENERIC );
        set.add( COMPLETE_FUNCTION );
        set.add( Tag.MACRO );
        set.add( INCLUDES );
        
        return set;
    }
    
    public static TagSet getDecoratingTags(){
    	return DECORATING;
    }
    
    public static ImageDescriptor getImageFor( TagSet tags ){
        ImageDescriptor image = null;
        
        if( tags.contains( UNIT ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_NESC_FILE );
        
        else if( tags.contains( INCLUDES ))
        	image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_INCLUDES_LIST );
        
        else if( tags.contains( TYPEDEF ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_TYPEDEF );
        
        else if( tags.contains( Tag.UNION ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_UNION );
        
        else if( tags.contains( Tag.STRUCT ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_STRUCT );

        else if( tags.contains( ENUMERATION ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_ENUMERATION );

        else if( tags.contains( ENUMERATION_CONSTANT ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_ENUM_CONSTANT );

        else if( tags.contains( TYPE ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_TYPE );
        
        else if( tags.contains( FIELD ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_FIELD );
        
        else if( tags.contains( PARAMETERS ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_PARAMETERS );
        
        else if( tags.contains( Tag.ATTRIBUTE ))
            image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_ATTRIBUTE );
        
        else if( tags.contains( Tag.MACRO ))
        	image = NesCIcons.icons().getImageDescriptor( NesCIcons.ICON_MACRO );
        
        else
            image = ASTModel.getImageFor( tags );
        
        if( image != null ){
            if( tags.contains( ERROR )){
                image = NesCIcons.icons().decorateError( image );
            }
            else if( tags.contains( WARNING )){
                image = NesCIcons.icons().decorateWarning( image );
            }
        }
        
        return image;
    }
    
    private DeclarationResolver resolver;
    
    public NesC12ASTModel( IProject project, DeclarationResolver resolver ){
    	super( project );
        this.resolver = resolver;
    }
    
    public void setDeclarationResolver( DeclarationResolver resolver ){
        this.resolver = resolver;
    }
    
    @Override
    public boolean addNode( IASTModelNode node ){
    	return addNode( node, false );
    }
    
    @Override
    public boolean addNode( IASTModelNode node, boolean override ){
        if( super.addNode( node, override ) ){
            if( resolver != null ){
                ((ModelNode)node).setDeclarationResolver( resolver );
            }
            return true;
        }
        return false;
    }
}
