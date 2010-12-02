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

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * A {@link IToken} which can update its data-object automatically.
 * @author Benjamin Sigg
 * @param <D> the kind of data this token uses
 */
public class PreferenceToken<D> extends Token{
    private IPreferenceProvider<D> provider;
    private String key;

    private IPreferenceListener<D> listener = new IPreferenceListener<D>(){
        public void preferenceChanged( IPreferenceProvider<D> provider, String name) {
            if( key.equals( name )){
                setData( provider.get( name ));
            }
        }
    };

    public PreferenceToken( String key, IPreferenceProvider<D> provider ){
        super( provider.get( key ));
        
        this.provider = provider;
        this.key = key;

        provider.addPreferenceListener( listener );
    }
    
    public void dispose(){
        provider.removePreferenceListener( listener );
    }
}
