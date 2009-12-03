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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.TagSet;

/**
 * Utility class to load and store icons.
 * @author Benjamin Sigg
 */
public abstract class Icons{
	public static final int DECORATION_TOPLEFT = 0;
	public static final int DECORATION_TOPRIGHT = 1;
	public static final int DECORATION_BOTTOMLEFT = 2;
	public static final int DECORATION_BOTTOMRIGHT = 3;
	
	private URL iconBaseURL;


    // The plugin registry
    private ImageRegistry imageRegistry = null;

    private int genKey = 0;
    
    /** additional images */
    private Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();
    
    public Icons( URL iconBaseURL ){
    	this.iconBaseURL = iconBaseURL;
    }
    
    /**
     * Returns the image managed under the given key in this registry.
     * 
     * @param key
     *                the image's key
     * @return the image managed under the given key
     */
    public Image get( String key ){
        return getImageRegistry().get( keyOf( key, false, null ) );
    }
    
    public Image get( String key, boolean decoratable ){
    	ImageDescriptor descriptor = getImageDescriptor( key );
    	return get( descriptor, decoratable );
    }
    
    /**
     * Returns the <code>ImageDescriptor</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public ImageDescriptor getImageDescriptor( String key ){
        return getImageRegistry().getDescriptor( keyOf( key, false, null ) );
    }
    
    private String keyOf( String key, boolean decoratable, String[][] decorations ){
    	if( decorations == null )
    		return key;

    	StringBuilder builder = new StringBuilder();
    	for( String[] decoration : decorations ){
    		if( decoration != null ){
    			for( String entry : decoration ){
    				builder.append( entry );
    				builder.append( "." );
    			}
    		}
    		builder.append( "-" );
    	}
    	builder.append( key );
    	builder.append( "." );
    	builder.append( decoratable );
    	
    	return builder.toString();
    }

    public Image get( ImageDescriptor descriptor, boolean decoratable ){
    	if( descriptor == null )
    		return null;
    	
    	if( decoratable ){
    		descriptor = decoratable( descriptor );
    	}
    	
    	return get( descriptor );
    }
    
    public Image get( ImageDescriptor descriptor ){
    	if( descriptor == null )
    		return null;
    	
        Image image = images.get( descriptor );
        if( image == null ){
            image = descriptor.createImage();
            images.put( descriptor, image );
        }
        return image;
    }

    public Image get( TagSet tags ){
    	ImageDescriptor descriptor = getImageDescriptor( tags );
    	if( descriptor == null )
    		return null;
    	return get( descriptor );
    }
    
    private void dispose(){
        for( Image image : images.values() ){
            image.dispose();
        }
        images.clear();
    }
    
    public static ImageDescriptor getImageDescriptor( TagSet tags ){
    	if( tags == null )
    		return null;
    	
    	TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    	if( plugin == null )
    		return null;
    	INesCParserFactory factory = plugin.getParserFactory();
    	if( factory == null )
    		return null;
    	return factory.getImageFor( tags );
    }
    
    /**
     * Adds decorations to an icon. This method works only with {@link ImageDescriptor}s 
     * that were created by this class itself. What meaning a number has depends
     * on the subclass, see {@link #getDecoration(String)}
     * @param image some image that originates from this class
     * @param decorations the decorations where 0 = top left, 1 = top right, 2 = bottom left,
     * 3 = bottom right
     * @return a new image
     */
    public ImageDescriptor decorate( ImageDescriptor image, String[][] decorations ){
    	image = decoratable( image );
    	
        if( image instanceof DecoratableImageDescriptor ){
            return ((DecoratableImageDescriptor)image).decorate( decorations );
        }
        return image;
    }
    
    public ImageDescriptor decorate( ImageDescriptor image, String[] decorations, int corner ){
    	String[][] array = new String[4][];
    	array[corner] = decorations;
    	return decorate( image, array );
    }
    
    /**
     * Gets the decoratable version of <code>image</code>. This version is intended
     * to be decorated.
     * @param descriptor some image
     * @return the decoratable version
     */
    public ImageDescriptor decoratable( ImageDescriptor descriptor ){
    	if( descriptor instanceof DecoratableImageDescriptor ){
    		return ((DecoratableImageDescriptor)descriptor).decoratable();
    	}
    	else{
    		Image image = get( descriptor );
    		NesCImageDescriptor base = new NesCImageDescriptor( image );
    		String key = "generic_key_" + genKey++;
    		return new DecoratableImageDescriptor( key, true, null, base );
    	}
    }
    
    /**
     * Gets an image that is used to decorate another image. The subclass
     * is free to define what exact meaning the various integers have.
     * @param decoration the decoration
     * @return an image or <code>null</code>
     */
    protected abstract ImageDescriptor getDecoration( String decoration );
    
    /*
     * Helper method to access the image registry from the JDIDebugUIPlugin
     * class.
     */
    private ImageRegistry getImageRegistry(){
        if( imageRegistry == null ){
            initializeImageRegistry();
        }
        return imageRegistry;
    }

    private void initializeImageRegistry(){
        imageRegistry = new ImageRegistry( TinyOSPlugin.getStandardDisplay() ){
            @Override
            public void dispose(){
                super.dispose();
                Icons.this.dispose();
            }
        };
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
    protected final void declareRegistryImage( String key, String path ){
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        try{
            desc = ImageDescriptor.createFromURL( makeIconFileURL( path ) );
        }
        catch ( MalformedURLException me ){
            TinyOSPlugin.log( me );
        }
        
        NesCImageDescriptor nesc = new NesCImageDescriptor( get( desc ));
        DecoratableImageDescriptor result = new DecoratableImageDescriptor( key, false, new String[][]{}, nesc );
        
        getImageRegistry().put( keyOf( key, false, null ), result );
    }
   
    private URL makeIconFileURL( String iconPath ) throws MalformedURLException{
        if( iconBaseURL == null ){
            throw new MalformedURLException();
        }

        return new URL( iconBaseURL, iconPath );
    }
    
    private String[][] merge( String[][] alpha, String[][] beta ){
    	sort( alpha );
    	sort( beta );
    	
    	if( alpha == null )
    		return beta;
    	if( beta == null )
    		return alpha;
    	
    	String[][] result = new String[ Math.max( alpha.length, beta.length )][];
    	for( int i = 0; i < result.length; i++ ){
    		if( i >= alpha.length )
    			result[i] = beta[i];
    		else if( i >= beta.length )
    			result[i] = alpha[i];
    		else if( alpha[i] == null || alpha[i].length == 0 )
    			result[i] = beta[i];
    		else if( beta[i] == null || beta[i].length == 0 )
    			result[i] = alpha[i];
    		else{
    			String[] temp = new String[ alpha[i].length + beta[i].length ];
    			System.arraycopy( alpha[i], 0, temp, 0, alpha[i].length );
    			System.arraycopy( beta[i], 0, temp, alpha[i].length, beta[i].length );
    			Arrays.sort( temp );
    			
    			int delta = 0;
    			for( int j = 1; j < temp.length-delta; j++ ){
    				if( temp[j-1].equals( temp[j+delta] )){
    					delta++;
    					j--;
    				}
    				temp[j] = temp[j+delta];
    			}
    			if( delta > 0 ){
    				result[i] = new String[ temp.length-delta ];
    				System.arraycopy( temp, 0, result[i], 0, result[i].length );
    			}
    			else{
    				result[i] = temp;
    			}
    		}
    	}
    	
    	return result;
    }
    
    private void sort( String[][] array ){
    	if( array != null ){
    		for( String[] check : array ){
	    		if( check != null ){
	    			Arrays.sort( check );
	    		}
    		}
    	}
    }
    
    protected void setupDecoratable( NesCImageDescriptor icon ){
    	// nothing
    }

    private class DecoratableImageDescriptor extends ImageDescriptor{
        private String key;
        private String[][] decorations;
        private NesCImageDescriptor base;
        private boolean decoratable;
        
        public DecoratableImageDescriptor( String key, boolean decoratable, String[][] decorations, NesCImageDescriptor base ){
            this.key = key;
            this.decorations = decorations;
            this.decoratable = decoratable;
            this.base = base;
            
            if( decoratable ){
            	setupDecoratable( base );
            }
        }
        
        @Override
        public ImageData getImageData(){
            return base.getImageData();
        }
        
        public ImageDescriptor decoratable(){
        	if( decoratable )
        		return this;
        	
        	String decoratableKey = keyOf( key, true, decorations );
        	ImageDescriptor result = imageRegistry.getDescriptor( decoratableKey );
        	if( result == null ){
        		NesCImageDescriptor image = new NesCImageDescriptor( base );
        		result = new DecoratableImageDescriptor( key, true, decorations, image );
        		imageRegistry.put( decoratableKey, result );
        	}
        	
        	return result;
        }
        
        public ImageDescriptor decorate( String[][] decorations ){
        	if( !decoratable ){
        		throw new IllegalStateException( "this image is not in the decoratable state" );
        	}
        	
        	decorations = merge( decorations, this.decorations );
        	String decoratedKey = keyOf( key, decoratable, decorations );
            ImageDescriptor decorated = imageRegistry.getDescriptor( decoratedKey );
            
            if( decorated == null ){
            	Image image = get( this.key );
            	NesCImageDescriptor icon = new NesCImageDescriptor( image );
            	
            	if( decorations != null ){
	                for( int i = 0; i < 4 && i < decorations.length; i++ ){
	                	if( decorations[i] != null && decorations[i].length > 0 ){
	                		ImageDescriptor[] overlay = new ImageDescriptor[ decorations[i].length ];
	                		for( int j = 0; j < decorations[i].length; j++ ){
	                			overlay[j] = getDecoration( decorations[i][j] );
	                		}
		                	switch( i ){
		                		case DECORATION_TOPLEFT:
		                			icon.setTopLeft( overlay );
		                			break;
		                		case DECORATION_BOTTOMLEFT:
		                			icon.setBottomLeft( overlay );
		                			break;
		                		case DECORATION_BOTTOMRIGHT:
		                			icon.setBottomRight( overlay );
		                			break;
		                		case DECORATION_TOPRIGHT:
		                			icon.setTopRight( overlay );
		                			break;
		                	}
	                	}
	                }
            	}
            	
                // Rectangle bounds = image.getBounds();
                // Point size = new Point( bounds.width+5, bounds.height );
                decorated = new DecoratableImageDescriptor( this.key, true, decorations, icon );
                imageRegistry.put( decoratedKey, decorated );
            }
            
            return decorated;
        }
    }
}
