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


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import tinyos.yeti.editors.nesc.util.IColorManager;

public class ColorProvider extends AbstractPreferenceProvider<Color> implements IColorManager, IPreferenceProvider<Color> {
    private IPreferenceStore store;

    //private Map<String, Color> fColorTable = new HashMap<String, Color>(11);
    private Map<Display, Map<RGB, Color>> displayTable = new HashMap<Display, Map<RGB, Color>>(2);
    /** 
     * Flag which tells if the colors are automatically disposed when the
     * current display gets disposed.
     */
    private boolean autoDisposeOnDisplayDispose;

    public ColorProvider( IPreferenceStore store ){
        this.store = store;
        store.addPropertyChangeListener( new IPropertyChangeListener(){
            public void propertyChange( PropertyChangeEvent event ){
                fireChanged( event.getProperty() );
            }
        });
    }
    
    /**
     * Return the color that is stored in the color table under the given RGB
     * value.
     * 
     * @param colorNameValue the RGB value
     * @return the color stored in the color table for the given RGB value
     * @see ColorConstants
     */
    public Color getColor(String[] colorNameValue) {
        RGB rgb = getColorPreference(colorNameValue[0]);
        return getColor( rgb );
    }


    private RGB getColorPreference(String categoryName)
    {
        String rgbString = store.getString(categoryName);

        if (rgbString.length() <= 0)
        {
            rgbString = store.getDefaultString(categoryName);
            if(rgbString.length() <= 0) 
            {
                rgbString = "0,0,0";
            }
        }
        return StringConverter.asRGB(rgbString);
    }

    public Color get( String key ){
        return getColor( key );
    }

    public Color getColor(String key) {
        RGB rgb = getColorPreference(key);
        return getColor( rgb );
    }

    public Color getColor(RGB rgb) {
        if (rgb == null) {
            return null;
        }
        final Display display = Display.getCurrent();
        Map<RGB, Color> colorTable = this.displayTable.get(display);
        if (colorTable == null) {
            colorTable = new HashMap<RGB, Color>(10);
            this.displayTable.put(display, colorTable);
            if (this.autoDisposeOnDisplayDispose) {
                display.disposeExec(new Runnable() {
                    public void run() {
                        dispose(display);
                    }
                });
            }
        }
        Color color = colorTable.get(rgb);
        if (color == null) {
            color = new Color(Display.getCurrent(), rgb);
            colorTable.put(rgb, color);
        }
        return color;
    }
    /*
     * @see org.eclipse.jface.text.source.ISharedTextColors#dispose()
     */
    public void dispose() {
        if (!this.autoDisposeOnDisplayDispose) {
            dispose(Display.getCurrent());
        }
    }

    private void dispose(Display display) {
        Map<RGB, Color> colorTable = this.displayTable.get(display);
        if (colorTable != null) {
            for ( Color color : colorTable.values() ) {
                if (!color.isDisposed()) {
                    color.dispose();
                }
            }
        }
    }
}
