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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import tinyos.yeti.preferences.PreferenceConstants;

public class NesCSourceViewerDecorationSupport extends SourceViewerDecorationSupport{

    private BracketBackroundPainter fBracketBackroundPainter;

    private IPreferenceStore store;

    /** The shared color manager */
    private ISharedTextColors fSharedTextColors;

    /** The viewer */
    private ISourceViewer fSourceViewer;

    public NesCSourceViewerDecorationSupport( ISourceViewer sourceViewer,
            IOverviewRuler overviewRuler, IAnnotationAccess annotationAccess,
            ISharedTextColors sharedTextColors ){
        super( sourceViewer, overviewRuler, annotationAccess, sharedTextColors );

        fSharedTextColors = sharedTextColors;
        fSourceViewer = sourceViewer;
        /*fBracketBackroundPainterEnableKey = PreferenceConstants.bracketBGColorer;
        fBracketBackroundPainterColorKey = PreferenceConstants.bracketBGStartColor;
        fBracketBackroundPainterIncKey = PreferenceConstants.bracketBGIncrement;
        fBracketBackroundPainterErrorColorKey = PreferenceConstants.bracketBGErrorColor;*/

    }

    /**
     * in this implementation, the cursorlinepainter is disabled..
     */
    @Override
    public void setCursorLinePainterPreferenceKeys( String enableKey, String colorKey ){
        super.setCursorLinePainterPreferenceKeys( "null", "null" );
    }

    @Override
    public void dispose(){
        super.dispose();
        fBracketBackroundPainter = null;
    }

    @Override
    public void install( IPreferenceStore store ){
        this.store = store;
        super.install( store );

        updateAddedTextDecorations();

    }

    private void updateAddedTextDecorations(){
        StyledText widget = fSourceViewer.getTextWidget();
        if( widget == null || widget.isDisposed() )
            return;

        if( isBracketBackroundPainterShown() )
            enableBracketBackgroundPainter();
        else
            disableBracketBackgroundPainter();

    }

    @Override
    protected void handlePreferenceStoreChanged( PropertyChangeEvent event ){
        // super.handlePreferenceStoreChanged(event);
        super.handlePreferenceStoreChanged( event );

        String p = event.getProperty();

        if( PreferenceConstants.BRACKET_BG_COLORER.equals( p )){
            if( isBracketBackroundPainterShown() ){
                enableBracketBackgroundPainter();
            }
            else{
                disableBracketBackgroundPainter();
            }
        }
        
        if( isBracketBackroundPainterShown() ){
            if( PreferenceConstants.BRACKET_BG_START_COLOR.equals( p ) ||
                    PreferenceConstants.BRACKET_BG_INCREMENT.equals( p ) ||
                    PreferenceConstants.BRACKET_BG_ERROR_COLOR.equals( p )){
                
                disableBracketBackgroundPainter();
                enableBracketBackgroundPainter();
            }
        }
    }

    private void disableBracketBackgroundPainter(){
        if( fBracketBackroundPainter != null ){
            if( fSourceViewer instanceof ITextViewerExtension2 ){
                ITextViewerExtension2 extension = ( ITextViewerExtension2 )fSourceViewer;
                extension.removePainter( fBracketBackroundPainter );
                
                StyledText widget = fSourceViewer.getTextWidget();
                if( widget != null && !widget.isDisposed() ){
                	widget.redraw();
                }
                
                fBracketBackroundPainter.deactivate( true );
                fBracketBackroundPainter.dispose();
                fBracketBackroundPainter = null;
            }
        }
    }

    private void enableBracketBackgroundPainter(){
        if( fBracketBackroundPainter == null ){
            if( fSourceViewer instanceof ITextViewerExtension2 ){
                fBracketBackroundPainter = new BracketBackroundPainter( fSourceViewer, fSharedTextColors );
                fBracketBackroundPainter.setColor(
                        getColor( PreferenceConstants.BRACKET_BG_START_COLOR ),
                        getColor( PreferenceConstants.BRACKET_BG_INCREMENT ),
                        getColor( PreferenceConstants.BRACKET_BG_ERROR_COLOR ) );
                ITextViewerExtension2 extension = ( ITextViewerExtension2 )fSourceViewer;
                extension.addPainter( fBracketBackroundPainter );
            }
        }
    }

    /**
     * Tells whether the bracketpainter is enabled.
     * 
     * @return <code>true</code> if the cursor line is shown
     */
    private boolean isBracketBackroundPainterShown(){
        if( store != null )
            return store.getBoolean( PreferenceConstants.BRACKET_BG_COLORER );
        return false;
    }

    /**
     * Returns the shared color for the given key.
     * 
     * @param key
     *                the color key string
     * @return the shared color for the given key
     */
    private Color getColor( String key ){
        if( store != null ){
            RGB rgb = PreferenceConverter.getColor( store, key );
            return getColor( rgb );
        }
        return null;
    }

    /**
     * Returns the shared color for the given RGB.
     * 
     * @param rgb
     *                the RGB
     * @return the shared color for the given RGB
     */
    private Color getColor( RGB rgb ){
        return fSharedTextColors.getColor( rgb );
    }

}
