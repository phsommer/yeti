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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * A painter the draws the background of the caret line in a configured color.
 * <p>
 * Clients usually instantiate and configure object of this class.
 * </p>
 * <p This class is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 */
public class BracketBackroundPainter implements IPainter, LineBackgroundListener{

    /** Indicates whether this painter is active */
    private boolean fIsActive = false;
    /** The source viewer this painter is associated with */
    private ISourceViewer fSourceViewer;
    /** The viewer's widget */
    private StyledText fTextWidget;

    /** The color in which to highlight the peer character */
    private Color fBGColor;
    private Color fIncColor;

    private Position position = new Position( 0, 0 );

    /** The paint position manager */
    private IPaintPositionManager fPaintPositionManager;

    private HashMap<Integer, Integer> levelsLine = new HashMap<Integer, Integer>();
    //private HashMap<Integer, Integer> levelsOffset = new HashMap<Integer, Integer>();

    private ISharedTextColors fSharedTextColors;
    private int iModulo;

    private int openBrackets;
    private int closingBrackets;

    private Comparator<Integer> comparator;
    private Color fErrorColor;

    /**
     * Creates a new MatchingCharacterPainter for the given source viewer using
     * the given character pair matcher. The character matcher is not adopted by
     * this painter. Thus, it is not disposed. However, this painter requires
     * exclusive access to the given pair matcher.
     * 
     * @param sourceViewer
     * @param sharedTextColors
     */
    public BracketBackroundPainter( ISourceViewer sourceViewer,
            ISharedTextColors sharedTextColors ){
        fSourceViewer = sourceViewer;
        fTextWidget = sourceViewer.getTextWidget();
        fSharedTextColors = sharedTextColors;
        comparator = new IntegerComparator();
    }

    /*
     * @see org.eclipse.jface.text.IPainter#dispose()
     */
    public void dispose(){
        fBGColor = null;
        fIncColor = null;
        fTextWidget = null;
    }

    /*
     * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
     */
    public void deactivate( boolean redraw ){
        if( fIsActive ){
            fIsActive = false;
            fTextWidget.removeLineBackgroundListener( this );
            if( fPaintPositionManager != null )
                fPaintPositionManager.unmanagePosition( position );
            // if (redraw)
            // handleDrawRequest(null);
        }
    }

    /*
     * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
     */
    public void setPositionManager( IPaintPositionManager manager ){
        fPaintPositionManager = manager;
    }

    public void setColor( Color bgcolor, Color incColor, Color color ){
        fBGColor = bgcolor;
        fIncColor = incColor;
        fErrorColor = color;

        int max = Math.max( fBGColor.getRed(), Math.max( fBGColor.getBlue(),
                fBGColor.getGreen() ) );
        if( max < 50 )
            max = 255 - max;
        iModulo = Math.abs( ( max - 50 ) / 15 );
    }

    public void paint( int reason ){
        IDocument document = fSourceViewer.getDocument();
        if( document == null ){
            deactivate( false );
            return;
        }

        if( !fIsActive ){
            fIsActive = true;
            fTextWidget.addLineBackgroundListener( this );
            /* ((ITextViewerExtension4)fSourceViewer).addTextPresentationListener(this); */
            position = new Position( 0, document.getLength() );
            fPaintPositionManager.managePosition( position );
        }else{

        }
        try{
            calculatePositions();
        }catch ( BadLocationException e ){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        drawHighlightLine();
    }

    private void calculatePositions() throws BadLocationException{
        if( fSourceViewer == null )
            return;
        IDocument document = fSourceViewer.getDocument();
        if( document != null ){
            NesCCodeReader scanner = new NesCCodeReader();

            scanner.configureForwardReader( document, 0, document.getLength(), true, true );

            int lastlevel = 0;
            levelsLine.clear();
            //levelsOffset.clear();
            openBrackets = 0;
            closingBrackets = 0;

            int offset = 0;
            while( true ){
                int read = scanner.next();
                if( read == -1 )
                    offset = document.getLength();
                else
                    offset = scanner.getOffset();
                
                if( read == '{' || read == '}' ){
                    if( read == '{' ){
                        lastlevel++;
                        openBrackets++;
                    }
                    else{
                        lastlevel--;
                        closingBrackets++;
                    }
                    
                    Integer level = Integer.valueOf( lastlevel );
                    
                    levelsLine.put(
                            Integer.valueOf( document.getLineOfOffset( offset ) ),
                            level );
                    //levelsOffset.put( 
                    //        Integer.valueOf( offset ),
                    //        level );
                }

                if( read == -1 )
                    break;
            }

            fSourceViewer.getTextWidget().setBackground(
                    subtract( openBrackets - closingBrackets ) );
        }

        /*
            char search[] = new char[]{ '{', '}' };
            NesCHeuristicScanner scanner = new NesCHeuristicScanner( document,
                    INesCPartitions.NESC_PARTITIONING, INesCPartitions.DEFAULT );
            int lastlevel = 0;
            levelsLine = new HashMap();
            openBrackets = 0;
            closingBrackets = 0;
            for( int i = 0; i < document.getLength(); ){
                int pos = scanner.scanForward( i, document.getLength(), search );

                if( pos == NesCHeuristicScanner.NOT_FOUND ){
                    i = document.getLength();
                }else{
                    char c = document.getChar( pos );
                    if( c == '{' ){
                        lastlevel++;
                        openBrackets++;
                    }else{
                        lastlevel--;
                        closingBrackets++;
                    }
                    levelsLine.put(
                            new Integer( document.getLineOfOffset( pos ) ),
                            new Integer( lastlevel ) );
                    levelsOffset.put( new Integer( pos ), new Integer(
                            lastlevel ) );

                    i = pos + 1;
                }
            }
            fSourceViewer.getTextWidget().setBackground(
                    subtract( openBrackets - closingBrackets ) );*/
    }

    private void drawHighlightLine(){
        fSourceViewer.getTextWidget().redraw();

    }

    public void lineGetBackground( LineBackgroundEvent event ){
        Color c = calcLineColor( event.lineOffset );
        event.lineBackground = c;

    }

    private Color calcLineColor( int lineOffset ){
        if( levelsLine == null )
            return fBGColor;
        int level = 0;
        Set<Integer> keys = levelsLine.keySet();

        Integer key = new Integer( fTextWidget.getLineAtOffset( lineOffset ) );

        if( fSourceViewer instanceof ITextViewerExtension5 ){
            ITextViewerExtension5 extension = ( ITextViewerExtension5 )fSourceViewer;
            key = new Integer( extension.widgetLine2ModelLine( key.intValue() ) );
        }

        if( keys.contains( key ) ){
            level = levelsLine.get( key ).intValue();
        }else{
            Integer a[] = keys.toArray( new Integer[keys.size()] );
            Arrays.sort( a, comparator );

            int insertionPoint = -Arrays.binarySearch( a, key, comparator ) - 1;

            if( insertionPoint < a.length ){
                level = levelsLine.get( a[insertionPoint] ).intValue();
            }
        }
        return subtract( level );
    }
/*
    private int getLevelsOffset( int i ){
        return levelsOffset.get( Integer.valueOf( i ) ).intValue();
    }
*/
//  private void calcTextBackgroundColor(TextPresentation textPresentation,
//  StyleRange sr) {
//  if (sr == null) return;
//  int start = sr.start;

//  if (fSourceViewer instanceof ITextViewerExtension5) {
//  ITextViewerExtension5 extension= (ITextViewerExtension5) fSourceViewer;
//  start = extension.widgetOffset2ModelOffset(start);
//  }
//  int end = sr.length + start;

//  Set keys = levelsOffset.keySet();

//  Iterator iter = keys.iterator();

//  while(iter.hasNext()) {
//  int offset = ((Integer)iter.next()).intValue();

//  if ((start < offset)&&(end <= offset)) {
//  sr.background = subtract(getLevelsOffset(offset)-1);

//  } else if ((start < offset)&&(end > offset)){
//  StyleRange t = (StyleRange) sr.clone();
//  t.length = offset - start -1;
//  t.background = subtract(getLevelsOffset(offset)-1);
//  textPresentation.addStyleRange(t);

//  sr.start = offset;
//  sr.length -= t.length;
//  } else if ((start == offset)&&(end > offset)) {
//  StyleRange t = (StyleRange) sr.clone();
//  t.length = 1;
//  t.background = subtract(getLevelsOffset(offset));
//  textPresentation.addStyleRange(t);

//  sr.start = sr.start+1;
//  sr.length = sr.length-1;

//  } else if ((start == offset)&&(end == offset)) {
//  return;
//  }

//  }
//  return;
//  }

    private Color subtract( int i ){
        if( i < 0 ){
            return fErrorColor;
        }

        // red = 255 - ( 1 * 5 ) % 256
        int red = Math.abs( ( fBGColor.getRed() - ( i % iModulo )
                * fIncColor.getRed() ) % 256 );
        int blue = Math.abs( ( fBGColor.getBlue() - ( i % iModulo )
                * fIncColor.getBlue() ) % 256 );
        int green = Math.abs( ( fBGColor.getGreen() - ( i % iModulo )
                * fIncColor.getGreen() ) % 256 );

        return fSharedTextColors.getColor( new RGB( red, green, blue ) );

    }

    private static class IntegerComparator implements Comparator<Integer>{
        public int compare( Integer a, Integer b ){
            return b.intValue() - a.intValue();
        }
    }

//  public void applyTextPresentation(TextPresentation textPresentation) {
//  try {
//  calculatePositions();
//  } catch (BadLocationException e) {
////TODO Auto-generated catch block
//  e.printStackTrace();
//  }

//  Iterator iter = textPresentation.getAllStyleRangeIterator();
//  ArrayList newStyles = new ArrayList();
//  while(iter.hasNext()) {
//  calcTextBackgroundColor(textPresentation, (StyleRange) iter.next());
//  }

//  calcTextBackgroundColor(textPresentation, textPresentation.getDefaultStyleRange());

//  }

}
