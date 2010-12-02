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
package tinyos.yeti.utility.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * This standard implementation works on a {@link IPreferenceStore}.
 * @author Benjamin Sigg
 */
public class MultiPreferenceProvider implements IMultiPreferenceProvider{
    private IPreferenceStore store;
    
    private IPreferenceProvider<Color> colors;
    private IPreferenceProvider<TextAttribute> textAttributes;
    private IPreferenceProvider<Integer> fontStyles;
    
    public MultiPreferenceProvider( IPreferenceStore store ){
        this.store = store;
    }
    
    public IPreferenceProvider<Color> getColors(){
        if( colors == null ){
            colors = new ColorProvider( store );
        }
        
        return colors;
    }
    
    public IPreferenceProvider<TextAttribute> getTextAttributes(){
        if( textAttributes == null ){
            textAttributes = new TextAttributeProvider( this );
        }
        
        return textAttributes;
    }
    
    public IPreferenceProvider<Integer> getFontStyles(){
        if( fontStyles == null ){
            fontStyles = new StyleProvider( store );
        }
        return fontStyles;
    }
    
    public void dispose(){
        if( colors != null ){
            colors.dispose();
            colors = null;
        }
        
        if( textAttributes != null ){
            textAttributes.dispose();
            textAttributes = null;
        }
        
        if( fontStyles != null ){
            fontStyles.dispose();
            fontStyles = null;
        }
    }
}
