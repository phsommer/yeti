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

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class NesCImageDescriptor extends CompositeImageDescriptor{
	private Image base;
	
	private ImageDescriptor[] topLeft;
	private ImageDescriptor[] topRight;
	private ImageDescriptor[] bottomLeft;
	private ImageDescriptor[] bottomRight;
	
	private int width;
	private int height;
	
	private int baseX;
	private int baseY;
	
	public NesCImageDescriptor( Image base ){
		this.base = base;
		Rectangle bounds = base.getBounds();
		setSize( bounds.width, bounds.height );
	}
	
	public NesCImageDescriptor( NesCImageDescriptor original ){
		this.base = original.base;
		
		this.topLeft = original.topLeft;
		this.topRight = original.topRight;
		this.bottomLeft = original.bottomLeft;
		this.bottomRight = original.bottomRight;
		
		this.width = original.width;
		this.height = original.height;
		
		this.baseX = original.baseX;
		this.baseY = original.baseY;
	}
	
	public void setBase( Image base ){
		this.base = base;
	}
	
	public void setSize( int width, int height ){
		this.width = width;
		this.height = height;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public void setBaseLocation( int x, int y ){
		this.baseX = x;
		this.baseY = y;
	}
	
	public void setTopLeft( ImageDescriptor[] topLeft ){
		this.topLeft = topLeft;
	}
	
	public void setTopRight( ImageDescriptor[] topRight ){
		this.topRight = topRight;
	}
	
	public void setBottomLeft( ImageDescriptor[] bottomLeft ){
		this.bottomLeft = bottomLeft;
	}
	
	public void setBottomRight( ImageDescriptor[] bottomRight ){
		this.bottomRight = bottomRight;
	}
	
	@Override
	protected void drawCompositeImage( int width, int height ){
		if( base != null ){
			drawImage( base.getImageData(), baseX, baseY );
		}
		
		if( topLeft != null ){
			int x = 0;
			for( int i = 0; i < topLeft.length; i++ ){
				ImageData data = topLeft[i].getImageData();
				drawImage( data, x, 0 );
				x += data.width;
			}
		}
		
		if( topRight != null ){
			int x = width;
			for( int i = 0; i < topRight.length; i++ ){
				ImageData data = topRight[i].getImageData();
				x -= data.width;
				drawImage( data, x, 0 );
			}
		}
		
		if( bottomLeft != null ){
			int x = 0;
			for( int i = 0; i < bottomLeft.length; i++ ){
				ImageData data = bottomLeft[i].getImageData();
				drawImage( data, x, height-data.height );
				x += data.width;
			}
		}
		
		if( bottomRight != null ){
			int x = width;
			for( int i = 0; i < bottomRight.length; i++ ){
				ImageData data = bottomRight[i].getImageData();
				x -= data.width;
				drawImage( bottomRight[i].getImageData(), x, height-data.height );
			}
		}
	}

	@Override
	protected Point getSize(){
		return new Point( width, height );
	}
}
