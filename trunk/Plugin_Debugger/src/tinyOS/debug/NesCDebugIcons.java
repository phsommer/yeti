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
package tinyOS.debug;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;


public class NesCDebugIcons {
	 /* Declare Common paths */
    private static URL ICON_BASE_URL = null;

    static{
        String pathSuffix = "icons/"; //$NON-NLS-1$

        ICON_BASE_URL = TinyOSDebugPlugin.getDefault().getBundle().getEntry(
                pathSuffix );
    }

    // The plugin registry
    private static ImageRegistry fgImageRegistry = null;

    /** additional images */
    private static Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

    /*
     * Available cached Images in the TinyOS Debugger plug-in image registry.
     */	
    public static final String ICON_VAR_AGGR = "VAR_AGGR";
    public static final String ICON_VAR_SIMPLE = "VAR_SIMPLE";
    public static final String ICON_COMPONENT = "COMPONENT";
    public static final String ICON_CHANGE_VARIABLE = "CHANGE_VARIABLE";
    public static final String ICON_CHANGE_VARIABLE_DISABLED = "CHANGE_VARIABLE_DISABLED";
    
    public static final String ICON_GDB_PROXY = "GDB_PROXY";
    public static final String ICON_CDT_DEBBUGER_TAB = "CDT_DEBBUGER_TAB";
    public static final String ICON_CDT_MAIN_TAB = "CDT_MAIN_TAB";
    
    public static final String ICON_DECORATION_ERROR = "icon_decoration_error";
    public static final String ICON_DECORATION_WARNING = "icon_decoration_warning";
    public static final int DECORATION_ERROR = 1;
    public static final int DECORATION_WARNING = 2;

	
    
    /**
     * Returns the image managed under the given key in this registry.
     * 
     * @param key
     *                the image's key
     * @return the image managed under the given key
     */
    public static Image get( String key ){
        return getImageRegistry().get( "0.0.0.0.0." + key );
    }

    public static Image get( ImageDescriptor descriptor ){
        Image image = images.get( descriptor );
        if( image == null ){
            image = descriptor.createImage();
            images.put( descriptor, image );
        }
        return image;
    }

    protected static ImageDescriptor getDecoration( int decoration ){
        if( (decoration & DECORATION_ERROR) == DECORATION_ERROR )
            return getImageDescriptor( ICON_DECORATION_ERROR );
        
        if( (decoration & DECORATION_WARNING) == DECORATION_WARNING )
            return getImageDescriptor( ICON_DECORATION_WARNING );
        
        return null;
    }
    
    private static void dispose(){
        for( Image image : images.values() ){
            image.dispose();
        }
        images.clear();
    }

    /**
     * Returns the <code>ImageDescriptor</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static ImageDescriptor getImageDescriptor( String key ){
        return getImageRegistry().getDescriptor( "0.0.0.0.0." + key );
    }
    
    public static ImageDescriptor decorateError( ImageDescriptor image ){
        return decorate( image, new int[]{ 0, 0, 0, DECORATION_ERROR, 0 } );
    }
    
    public static ImageDescriptor decorateWarning( ImageDescriptor image ){
        return decorate( image, new int[]{ 0, 0, 0, DECORATION_WARNING, 0 } );
    }
    
    /**
     * Adds decorations to an icon. This method works only with {@link ImageDescriptor}s 
     * that were created by this class itself.
     * @param image some image that originates from this class
     * @param decorations the decorations where 0 = top left, 1 = top right, 2 = bottom left,
     * 3 = bottom right, 4 = underlay
     * @return a new image
     */
    public static ImageDescriptor decorate( ImageDescriptor image, int[] decorations ){
        if( image instanceof DecoratableImageDescriptor ){
            return ((DecoratableImageDescriptor)image).decorate( decorations );
        }
        return image;
    }
    
    /*
     * Helper method to access the image registry from the JDIDebugUIPlugin
     * class.
     */
    /* package */static ImageRegistry getImageRegistry(){
        if( fgImageRegistry == null ){
            initializeImageRegistry();
        }
        return fgImageRegistry;
    }

    private static void initializeImageRegistry(){
        fgImageRegistry = new ImageRegistry( TinyOSDebugPlugin.getStandardDisplay() ){
            @Override
            public void dispose(){
                super.dispose();
                NesCDebugIcons.dispose();
            }
        };
        declareImages();
    }

    private static void declareImages(){
    	declareRegistryImage( ICON_VAR_SIMPLE, "var_simple.gif" );
    	declareRegistryImage( ICON_VAR_AGGR, "var_global_aggr.gif" );
    	declareRegistryImage( ICON_COMPONENT, "component.gif" );
    	declareRegistryImage( ICON_CHANGE_VARIABLE, "changevariablevalue.gif" );
    	declareRegistryImage( ICON_CHANGE_VARIABLE_DISABLED, "changevariablevalue_d.gif" );
    	
    	declareRegistryImage( ICON_GDB_PROXY, "proxy.gif" );
    	declareRegistryImage( ICON_CDT_DEBBUGER_TAB, "debugger_tab.gif" );
    	declareRegistryImage( ICON_CDT_MAIN_TAB, "main_tab.gif" );
    	
        declareRegistryImage( ICON_DECORATION_ERROR, "small_error.png" );
        declareRegistryImage( ICON_DECORATION_WARNING, "small_warning.png" );
    }

    /**
     * Declare an Image in the registry table.
     * 
     * @param key
     *                The key to use when registering the image
     * @param path
     *                The path where the image can be found. This path is
     *                relative to where this plugin class is found (i.e.
     *                typically the packages directory)
     */
    private final static void declareRegistryImage( String key, String path ){
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        try{
            desc = ImageDescriptor.createFromURL( makeIconFileURL( path ) );
        }catch ( MalformedURLException me ){
        	TinyOSDebugPlugin.getDefault().log("Exception while creating image descriptor from url.",me);
        }
        desc = new DecoratableImageDescriptor( key, new int[]{ 0, 0, 0, 0, 0 }, desc );
        fgImageRegistry.put( "0.0.0.0.0." + key, desc );
    }

    private static URL makeIconFileURL( String iconPath ) throws MalformedURLException{
        if( ICON_BASE_URL == null ){
            throw new MalformedURLException();
        }
        return new URL( ICON_BASE_URL, iconPath );
    }

    private static class DecoratableImageDescriptor extends ImageDescriptor{
        private String key;
        private int[] decorations;
        private ImageDescriptor base;
        
        public DecoratableImageDescriptor( String key, int[] decorations, ImageDescriptor base ){
            this.key = key;
            this.decorations = decorations;
            this.base = base;
        }
        
        @Override
        public ImageData getImageData(){
            return base.getImageData();
        }
        
        public ImageDescriptor decorate( int[] decorations ){
            int[] used = new int[5];
            for( int i = 0; i < 5; i++ )
                used[i] = decorations[i] == 0 ? this.decorations[i] : decorations[i];
                
            StringBuilder key = new StringBuilder();
            for( int u : used ){
                key.append( u );
                key.append( "." );
            }
            key.append( this.key );
            
            ImageDescriptor decorated = fgImageRegistry.getDescriptor( key.toString() );
            
            if( decorated == null ){
                Image image = get( this.key );
                
                ImageDescriptor[] overlayArray = new ImageDescriptor[5];
                for( int i = 0; i < 5; i++ ){
                    overlayArray[i] = getDecoration( used[i] );
                }
                DecorationOverlayIcon icon = new DecorationOverlayIcon( image, overlayArray );
                decorated = new DecoratableImageDescriptor( this.key, used, icon );
                
                fgImageRegistry.put( key.toString(), decorated );
            }
            
            return decorated;
        }
    }
}
