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
package tinyos.yeti.editors;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.utility.Icons;
import tinyos.yeti.utility.NesCImageDescriptor;

/**
 * Bundle of most images used by the TinyOSPlugin plug-in.
 */
public class NesCIcons extends Icons{
    /*
     * Available cached Images in the TinyOS plug-in image registry.
     */	
    
    public static final String ICON_PLAIN_PAGE = "PLAIN_PAGE";
    
    static public final String ICON_CONFIGURATION = "ICON_CONFIGURATION";
    static public final String ICON_MODULE = "ICON_MODULE";
    static public final String ICON_BINARY_COMPONENT = "ICON_BINARY_COMPONENT";
    static public final String ICON_INCLUDES_LIST = "ICON_INCLUDES_LIST"; 
    static public final String ICON_INCLUDE = "ICON_INCLUDE";
    public static final String ICON_EXCLUDE_LIST = "excludes";
    static public final String ICON_IMPLEMENTATION_CONFIGURATION = "ICON_IMPLEMENTATION_config";
    static public final String ICON_IMPLEMENTATION_MODULE = "ICON_IMPLEMENTATION_module";
    static public final String ICON_SPECIFICATION = "ICON_specification";
    static public final String ICON_CONNECTION = "ICON_CONNECTION";
    static public final String ICON_EQUATE_WIRES ="ICON_EQUATE_WIRES";
    static public final String ICON_LINK_WIRES = "ICON_LINK_WIRES";
    static public final String ICON_LINK_WIRES_INVERSE = "ICON_LINK_WIRES_INVERSE ";
    static public final String ICON_COMPONENT = "ICON_COMPONENT";
    static public final String ICON_COMPONENT_RENAMED = "ICON_COMPONENT_RENAMED"; 

    static public final String ICON_USES_INTERFACE = "ICON_USES_INTERFACE"; 
    static public final String ICON_PROVIDES_INTERFACE = "ICON_PROVIDES_INTERFACE"; 

    static public final String ICON_INTERFACE = "ICON_INTERFACE";

    static public final String ICON_COMMAND = "ICON_COMMAND";
    static public final String ICON_COMMAND_USES = "ICON_COMMAND_USES";
    static public final String ICON_COMMAND_PROVIDES = "ICON_COMMAND_PROVIDES";
    static public final String ICON_COMMAND_ASYNC = "ICON_COMMAND_ASYNC";
    static public final String ICON_COMMAND_ASYNC_USES = "ICON_COMMAND_ASYNC_USES";
    static public final String ICON_COMMAND_ASYNC_PROVIDES = "ICON_COMMAND_ASYNC_PROVIDES";

    static public final String ICON_EVENT = "ICON_EVENT";
    static public final String ICON_EVENT_USES = "ICON_EVENT_USES";
    static public final String ICON_EVENT_PROVIDES = "ICON_EVENT_PROVIDES";
    static public final String ICON_EVENT_ASYNC = "ICON_EVENT_ASYNC";
    static public final String ICON_EVENT_ASYNC_PROVIDES = "ICON_EVENT_ASYNC_PROVIDES";
    static public final String ICON_EVENT_ASYNC_USES = "ICON_EVENT_ASYNC_USES";

    static public final String ICON_MACRO = "icon_macro";

    static public final String ICON_TASK = "ICON_TASK";
    static public final String ICON_TASK_ASYNC ="ICON_TASK_ASYNC";

    static public final String ICON_CFUNCTION = "ICON_CFUNCTION";

    static public final String ICON_FIELD = "icon_field";
    static public final String ICON_PARAMETERS = "icon_parameters";

    // Make View Icons
    static public final String ICON_MAKE_BUILD = "ICON_MAKE_BUILD";
    static public final String ICON_MAKE_BUILD_DISABLED = "ICON_MAKE_BUILD_DISABLED";
    static public final String ICON_MAKE_FILTER = "ICON_MAKE_FILTER";

    // Make - Target Icons
    static public final String ICON_MAKE_TARGET = "ICON_MAKE_TARGET";
    static public final String ICON_MAKE_TARGET_DEFAULT = "ICON_MAKE_TARGET_DEFAULT";

    // Template - Icons
    static public final String ICON_TEMPLATE_DOCS = "ICON_TEMPLATE_DOCS";

    // Struct
    static public final String ICON_ATTRIBUTE = "icon_attribute";
    static public final String ICON_SMALL_ATTRIBUTE = "icon_small_attribute";
    static public final String ICON_STRUCT = "ICON_STRUCT";
    static public final String ICON_UNION = "icon_union";
    static public final String ICON_TYPE = "icon_type";
    static public final String ICON_TYPEDEF = "icon_typedef";
    
    static public final String ICON_ENUMERATION = "icon_enumeration";
    static public final String ICON_ENUM_CONSTANT = "icon_enum_constant";
    
    static public final String ICON_ERROR = "ICON_ERROR";
    static public final String ICON_CLEAR = "ICON_CLEAR";


    static public final String ICON_PLUS =  "ICON_PLUS";
    static public final String ICON_MINUS = "ICON_MINUS";
    static public final String ICON_OPEN = "icon_open";

    static public final String ICON_PRINTER = "ICON_PRINTER";

    public static final String ICON_SORT = "ICON_SORT";
    public static final String ICON_FILTER = "icon_filter";

    static public final String ICON_TEMPLATE = "ICON_TEMPLATE";

    public static final String ICON_DECORATION_ERROR = "icon_decoration_error";
    public static final String ICON_DECORATION_WARNING = "icon_decoration_warning";
    
    public static final String ICON_CONVERT_TO_INFO = "icon_convert_to_info";
    
    public static final String ICON_EXTRAS = "icon_extras";
    public static final String ICON_SENSOR = "icon_sensor";
    public static final String ICON_APPLICATION = "icon_application";
    public static final String ICON_PLATFORM = "icon_platform";
    public static final String ICON_NESC = "icon_nesc";
    public static final String ICON_NESC_FILE = "icon_nesc_file";
    public static final String ICON_NESC_DECORATION = "icon_nesc_decoration";
    public static final String ICON_ENVIRONMENT_VARIABLE = "icon_environment_variable";
    
    public static final String ICON_ATLEASTONCE = "icon_nesc_atleastonce";
    public static final String ICON_ATMOSTONCE = "icon_nesc_atmostonce";
    public static final String ICON_EXACTLYONCE = "icon_nesc_exactlyonce";
    public static final String ICON_SAFE = "icon_nesc_safe";
    public static final String ICON_UNSAFE = "icon_nesc_unsafe";
    public static final String ICON_ATOMIC_HWEVENT = "icon_nesc_atomic_hwevent";
    public static final String ICON_COMBINE = "icon_nesc_combine";
    public static final String ICON_C = "icon_nesc_c";
    public static final String ICON_HWEVENT = "icon_nesc_hwevent";
    public static final String ICON_INTEGER = "icon_nesc_integer";
    public static final String ICON_NUMBER = "icon_nesc_number";
    public static final String ICON_SPONTANEOUS = "icon_nesc_spontaneous";
    
    public static final String DECORATION_ERROR = "decoration_error";
    public static final String DECORATION_WARNING = "decoration_warning";
    
    private static final NesCIcons ICONS = new NesCIcons();
    
    private Map<String,String> attributeIcons = new HashMap<String, String>();
    
    public NesCIcons(){
    	super( getImageURL() );
    	declareImages();
    }
    
    private static URL getImageURL(){
    	String pathSuffix = "icons/"; //$NON-NLS-1$
        return TinyOSPlugin.getDefault().getBundle().getEntry( pathSuffix );
    }
    
    public static NesCIcons icons(){
    	return ICONS;
    }

    public void loadAttributes( TinyOSPlugin plugin ){
    	Map<String, URL> attributes = plugin.loadMetaAttributes();
    	
    	for( Map.Entry<String, URL> entry : attributes.entrySet() ){
    		String name = entry.getKey();
    		String key = "attribute_" + name;
    		URL path = entry.getValue();
    		
    		declareRegistryImage( key, path );
    		declareAttribute( name, key );
    	}
    	
    }
    
    protected final void declareAttribute( String name, String icon ){
    	attributeIcons.put( name, icon );
    }
    
    private void declareImages(){
    	declareRegistryImage( ICON_PLAIN_PAGE, "empty_page.png" );
    	
        declareRegistryImage( ICON_CONFIGURATION, "configuration.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MODULE, "module.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_BINARY_COMPONENT, "binary_component.gif" );
        declareRegistryImage( ICON_INCLUDES_LIST, "includes.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_INCLUDE, "dot_h_file.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EXCLUDE_LIST, "excludes.gif" );
        declareRegistryImage( ICON_IMPLEMENTATION_CONFIGURATION, "implementation_configuration.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_IMPLEMENTATION_MODULE, "implementation_module.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_SPECIFICATION, "specification.png" );
        declareRegistryImage( ICON_CONNECTION, "connection.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EQUATE_WIRES, "equatewires.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_LINK_WIRES, "linkwires.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_LINK_WIRES_INVERSE, "linkwires_inv.gif" ); //$NON-NLS-1$

        declareRegistryImage( ICON_COMPONENT, "component.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMPONENT_RENAMED, "renamed_component.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_USES_INTERFACE, "uses_interface.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_PROVIDES_INTERFACE, "provides_interface.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_INTERFACE, "interface.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMMAND, "command.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMMAND_USES, "command_uses.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMMAND_PROVIDES, "command_provides.gif" ); //$NON-NLS-1$

        declareRegistryImage( ICON_COMMAND_ASYNC, "command_async.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMMAND_ASYNC_USES, "command_async_uses.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_COMMAND_ASYNC_PROVIDES, "command_async_provides.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT, "event.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT_USES, "event_uses.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT_PROVIDES, "event_provides.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT_ASYNC, "event_async.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT_ASYNC_PROVIDES, "event_async_provides.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_EVENT_ASYNC_USES, "event_async_uses.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_TASK, "task.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_TASK_ASYNC, "task_async.gif" ); //$NON-NLS-1$

        declareRegistryImage( ICON_CFUNCTION, "c_function.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MAKE_BUILD, "make_build.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MAKE_BUILD_DISABLED, "make_build_d.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MAKE_FILTER, "make_filter.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MAKE_TARGET, "make_target.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_MAKE_TARGET_DEFAULT, "make_target_default.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_TEMPLATE_DOCS, "at.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_ATTRIBUTE, "attribute.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_SMALL_ATTRIBUTE, "small_attribute.png" );
        declareRegistryImage( ICON_STRUCT, "struct.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_UNION, "union.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_TYPE, "type.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_ERROR, "error.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_CLEAR, "clear.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_PLUS, "plus.gif" ); //$NON-NLS-1$

        declareRegistryImage( ICON_MACRO, "macro.png" );
        
        declareRegistryImage( ICON_OPEN, "open.png" );
        declareRegistryImage( ICON_MINUS, "minus.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_PRINTER, "printer.gif" ); //$NON-NLS-1$
        declareRegistryImage( ICON_SORT, "sort.png" ); //$NON-NLS-1$
        declareRegistryImage( ICON_FILTER, "filter.png" );

        declareRegistryImage( ICON_TEMPLATE, "template.gif" );
        declareRegistryImage( ICON_PLATFORM, "platform.png" );

        declareRegistryImage( ICON_DECORATION_ERROR, "small_error.png" );
        declareRegistryImage( ICON_DECORATION_WARNING, "small_warning.png" );
        
        declareRegistryImage( ICON_PARAMETERS, "parameters.png" );
        declareRegistryImage( ICON_FIELD, "field.png" );
        declareRegistryImage( ICON_TYPEDEF, "typedef.png" );
        declareRegistryImage( ICON_ENUMERATION,  "enumeration.png" );
        declareRegistryImage( ICON_ENUM_CONSTANT, "enum_constant.png" );
        
        declareRegistryImage( ICON_CONVERT_TO_INFO, "convertToInfo.png" );
        
        declareRegistryImage( ICON_SENSOR, "sensor.png" );
        declareRegistryImage( ICON_ENVIRONMENT_VARIABLE, "environment_variable.png" );
        declareRegistryImage( ICON_EXTRAS, "extras.png" );
        declareRegistryImage( ICON_APPLICATION, "application.png" );
        declareRegistryImage( ICON_NESC, "nesc.png" );
        declareRegistryImage( ICON_NESC_FILE, "nesc_file.png" );
        declareRegistryImage( ICON_NESC_DECORATION, "nesc_nature.png" );


        declareRegistryImage( ICON_ATLEASTONCE, "small_atleastonce.png" );
        declareRegistryImage( ICON_ATMOSTONCE, "small_atmostonce.png" );
        declareRegistryImage( ICON_EXACTLYONCE, "small_exactlyonce.png" );
        declareRegistryImage( ICON_SAFE, "small_safe.png" );
        declareRegistryImage( ICON_UNSAFE, "small_unsafe.png" );
        
        declareRegistryImage( ICON_C, "small_c.png" );
        declareRegistryImage( ICON_ATOMIC_HWEVENT, "small_atomic_hwevent.png" );
        declareRegistryImage( ICON_COMBINE, "small_combine.png" );
        declareRegistryImage( ICON_HWEVENT, "small_hwevent.png" );
        declareRegistryImage( ICON_INTEGER, "small_integer.png" );
        declareRegistryImage( ICON_NUMBER, "small_number.png" );
    	declareRegistryImage( ICON_SPONTANEOUS, "small_spontaneous.png" );
    }
    
    public Image get( ImageDescriptor image, IASTModelAttribute[] attributes ){
    	if( image == null )
    		return null;
    	
    	image = decorate( image, attributes );
    	return get( image );
    }
    
    public Image get( TagSet tags, IASTModelAttribute[] attributes ){
    	ImageDescriptor image = getImageDescriptor( tags, attributes );
    	if( image == null )
    		return null;
    	return get( image );
    }
    
    public ImageDescriptor getImageDescriptor( TagSet tags, IASTModelAttribute[] attributes ){
    	ImageDescriptor image = getImageDescriptor( tags );
    	if( image == null )
    		return null;
    	return decorate( image, attributes );    
    }

    public ImageDescriptor decorate( ImageDescriptor image, IASTModelAttribute[] attributes ){
    	image = decoratable( image );
    	
    	if( attributes == null || attributes.length == 0 )
    		return image;
    	
    	List<String> named = new ArrayList<String>();
    	
    	for( IASTModelAttribute attribute : attributes ){
    		String name = attribute.getName();
    		if( attributeIcons.containsKey( name ) ){
    			named.add( name );
    		}
    	}
    	
    	if( named.size() != attributes.length ){
    		named.add( "//attribute" );
    	}
    	
    	return decorate( image, named.toArray( new String[ named.size() ] ), DECORATION_TOPRIGHT );
    }

    @Override
    protected ImageDescriptor getDecoration( String decoration ){
        if( decoration.equals( DECORATION_ERROR ))
            return getImageDescriptor( ICON_DECORATION_ERROR );
        
        if( decoration.equals( DECORATION_WARNING ))
            return getImageDescriptor( ICON_DECORATION_WARNING );
        
        if( decoration.equals( "//attribute" ))
        	return getImageDescriptor( ICON_SMALL_ATTRIBUTE );
        
        String icon = attributeIcons.get( decoration );
        if( icon != null ){
        	return getImageDescriptor( icon );
        }
        
        return null;
    }
    
    public ImageDescriptor decorateError( ImageDescriptor image ){
        return decorate( image, new String[]{ DECORATION_ERROR }, DECORATION_BOTTOMLEFT );
    }
    
    public ImageDescriptor decorateWarning( ImageDescriptor image ){
        return decorate( image, new String[]{ DECORATION_WARNING }, DECORATION_BOTTOMLEFT );
    }
    
    @Override
    protected void setupDecoratable( NesCImageDescriptor image ){
        int width = image.getWidth();
	    int height = image.getHeight();
	    image.setSize( width+9+4, height );
	    image.setBaseLocation( 4, 0 );
    }
}
