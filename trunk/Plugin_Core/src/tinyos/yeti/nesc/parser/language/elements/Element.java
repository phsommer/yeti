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
package tinyos.yeti.nesc.parser.language.elements;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.ui.model.IWorkbenchAdapter;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.ProposalLocation;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.parser.Declaration;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.ITokenInfo;


abstract public class Element implements IWorkbenchAdapter, IAdaptable, ITokenInfo
{
    final protected static String NEWLINE = System.getProperty("line.separator");

    protected String name = "";
    protected int offset = 0;
    protected int length = 0;
    protected int line = -1;

    protected Element parent;
    protected List children = new LinkedList();

    //protected NesCModel model;

    //protected HashMap childrenByName;
    protected ImageDescriptor image = null;


    public void setOffset(int i) {
        this.offset = i;
    }
    public void setLine(int i) {
        this.line = i;
    }
    public int getLine() {
        return line;
    }

    public void setLength(int i) {
        this.length = i;
    }

    public Element(ITokenInfo it) {
        this(it.getText(), it.getOffset(), it.getLength(), it.getLine());
    }


    /**
     * Creates a new Element and stores parent element and location in the text.
     * 
     * @param aName text corresponding to the func
     * @param offset  the offset into the Readme text
     * @param length  the length of the element
     * @param line 
     */
    public Element(String aName, int offset, int length, int line)
    {
        set(aName,offset,length,line);
    }

    private void set(String name, int offset, int length, int line) {
        this.name = name;
        this.offset = offset;
        this.length = length;
        this.line = line;
    }
    public Element(Element e) {
        if (e!= null) {
            set(e.name, e.offset, e.length, e.line);
        } 
    }

    public Element(String text, ITokenInfo e) {
        if (e != null) {
            set(text,e.getOffset(),e.getLength(), e.getLine());
        } else {
            set(text,0,0,-1);
        }
    }

    public Element(String text, ITokenInfo e1, ITokenInfo e2) {	
        if ((e1!=null)&&(e2!=null)) {
            set(text, 
                    e1.getOffset(),
                    e2.getOffset() - e1.getOffset() + e2.getLength(),
                    e1.getLine()
            );
        } else if (e1==null) {
            set(text,
                    e2.getOffset(),
                    e2.getLength(),
                    e2.getLine()
            );
        } else if (e2 == null) {
            set(text,
                    e1.getOffset(),
                    e1.getLength(),
                    e1.getLine()
            );
        } else {
            set (text,0,0,-1);
        }

    }


    public Element(String string, ArrayList l) {
        sortElementListByOffset(l);

        if ((l == null)||(l.size()==0)) return;


        // one object in array
        if (l.size() == 1) {
            ITokenInfo o = (ITokenInfo) l.get(0);

            set(string, o.getOffset(), o.getLength(), o.getLine());
            return;
        }

        // more then one..
        ITokenInfo o1 = (ITokenInfo) l.get(0);
        ITokenInfo o2 = (ITokenInfo) l.get(l.size()-1);

        set(string, 
                o1.getOffset(), 
                o2.getOffset()-o1.getOffset()+o2.getLength(),
                o1.getLine());
    }

    @SuppressWarnings("unchecked")
    public void sortElementListByOffset(List list){
        if ((list == null)||(list.size() == 0)) return;
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Element e1 = (Element) o1;
                Element e2 = (Element) o2;
                Integer d1 = (e1 ==null) ? new Integer(0) : new Integer(e1.offset);
                Integer d2 = (e2 ==null) ? new Integer(0) :new Integer(e2.offset);
                return d1.compareTo(d2);
            }
        });
    }


    public Element(String string, ArrayList list, ITokenInfo token) {
        sortElementListByOffset(list);

        if ((list == null) && (token == null)) {
            set(string,0,0,-1);
        } else if (list == null) {
            set(string, token.getOffset(),token.getLength(),token.getLine());
        } else if (token == null) {
            set(string, 
                    ((ITokenInfo)list.get(0)).getOffset(), 
                    ((ITokenInfo)list.get(0)).getLength(),
                    ((ITokenInfo)list.get(0)).getLine());
        } else {
            set(string,
                    ((ITokenInfo)list.get(0)).getOffset(),
                    token.getOffset() - ((ITokenInfo)list.get(0)).getOffset() + token.getLength(),
                    ((ITokenInfo)list.get(0)).getLine());
        }
    }


    public Element(String string, ITokenInfo token, ArrayList list) {
        sortElementListByOffset(list);

        if ((list == null) && (token == null)) {
            set(string,0,0,-1);
        } else if (list == null) {
            set(string, token.getOffset(),token.getLength(),token.getLine());
        } else if (token == null) {
            set(string, 
                    ((ITokenInfo)list.get(0)).getOffset(), 
                    ((ITokenInfo)list.get(0)).getLength(),
                    ((ITokenInfo)list.get(0)).getLine());
        } else {
            set(string,
                    token.getOffset(),
                    - token.getOffset() + ((ITokenInfo)list.get(0)).getOffset() + ((ITokenInfo)list.get(0)).getLength(),
                    token.getLine());
        }
    }
    public Element(ITokenInfo token, ITokenInfo token2) {
        if (token2 == null) {
            set(token.getText(),token.getOffset(),token.getLength(),token.getLine());
        } else if (token == null) {
            set(token2.getText(),token2.getOffset(),token2.getLength(),token2.getLine());
        } else {
            set(token.getText()+token2.getText(),
                    token.getOffset(), 
                    token2.getOffset()-token.getOffset()+token2.getLength(), 
                    token.getLine()
            );
        }
    }

    /**
     * Transforms this element into a new {@link IASTModelNode}.
     * @param parent the parent of the node this element returns, can be <code>null</code>
     * @param project project to search for additional information
     * @param model the model to write into
     * @param file the parse file
     */
    public void toNode( ASTModelNode parent, ProjectModel project, ASTModel model, IParseFile file ){
        for( Element child : (List<Element>)getChildren() ){
            child.toNode( parent, project, model, file );
        }
    }

    public String getText() {
        return this.name;
    }
    /**
     * Method declared on IAdaptable
     */
    public Object getAdapter(Class adapter)
    {
        if (adapter == IWorkbenchAdapter.class)
        {
            return this;
        }

        return null;
    }

    /**
     * Method declared on IWorkbenchAdapter
     */
    public String getLabel(Object o)
    {
        return name;
    }

    /**
     * Returns the number of characters in this section.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Returns the offset of this section in the file.
     */
    public int getStart()
    {
        return offset;
    }

    /**
     * Returns the offset of this section in the file
     */
    public int getEnd() {
        return offset + length;
    }

    public String toString() {
        String t = "Element - "+name+"\n"+ 
        "           Offset: "+offset+" Length: "+length+"\n"+
        "		   Line: "+line;

        if (parent != null) {
            t+= "           Parent: "+parent.name+"\n";
        }
        t+= "           Childrens: "+children.size();

        return t;

    }

    public List getChildren() {
        return children;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
     */
    @SuppressWarnings("unchecked")
    public Object[] getChildren(Object o)
    {
        Object[] result = new Object[children.size()];
        //Object[] result = new Object[((Element)o).children.size()];
        //return ((Element)o).children.toArray(result);
        return children.toArray(result);
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(Object)
     */
    public Object getParent(Object o)
    {
        return parent;
    }

    public String getName(){ 
        return name;
    }

    public void setName(String name){ 
        this.name = name;
    }
    public int getOffset(){	
        return offset;
    }
    public Element getParent(){
        return parent;
    }

    protected void setParent(Element element){
        parent = element;
    }

    public boolean sharesParentWith(Element anElement)
    {
        if(parent == null) {
            return anElement.getParent() == null;
        }

        return parent.equals(anElement.getParent());
    }

    public boolean equals(Element anElement)
    {
        return sharesParentWith(anElement) && name.equals(anElement.getName());
    }

    public boolean hasChildren() {
        return (children.size() > 0);
    }

    @SuppressWarnings("unchecked")
    public void addChildElement(Element anElement)	{
        if (anElement == null) return;

        //String elementName = anElement.getName();
        //if(!childrenByName.containsKey(elementName))
        {
            this.children.add(anElement);
            //this.childrenByName.put(elementName, anElement);
            anElement.setParent(this);
        }
    }

    public SemanticError[] getSemanticErrors( ProjectTOS project ) {
        return null;
    }

    public SemanticError[] getSemanticWarnings( ProjectTOS project ) {
        return null;
    }

    public Iterator iterator() {
        return new ElementIterator(this);
    }

    /*
     * Iterator for Elements-Tree
     */
    class ElementIterator implements Iterator {
        /** 
         * List of elements not yet returned. Childrens of these elements
         * also have not yet been returned 
         */
        public List notyet;

        @SuppressWarnings("unchecked")
        public ElementIterator(Element n) {
            notyet = new LinkedList();
            notyet.add(n);
        }	

        public boolean hasNext() {
            return (notyet.size()>0);
        }

        @SuppressWarnings("unchecked")
        public Object next() {
            Element next = (Element) notyet.remove(0);
            for (int i = 0; i < next.children.size(); ++i) {
                notyet.add(next.children.get(i));
            }
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() on ElementIterator not possible");

        }	
    }

    public void setImage(ImageDescriptor i) {
        this.image = i;
    }
    public ImageDescriptor getImageDescriptor(Object object)	{
        return image;
    }

    public void addChilds(ArrayList a) {
        sortElementListByOffset(a);

        for (int i = 0; i < a.size(); i++) {
            addChildElement((Element)a.get(i));
        }
    }

    public ArrayList<INesCCompletionProposal> getCompletionProposals( ProposalLocation location ){
        return null;
    }

    public boolean isFoldable() {
        return false;
    }
    public Position getPositionForOutline() {
        return new Position(getOffset(), length);

    }
    public void setDeclarations(Declaration[] declarations) {

    }
    public Declaration[] getDeclarations() {
        return null;
    }
//  public void setModel(NesCModel model) {
//  this.model = model;
//  }
//  protected NesCModel getModel() {
//  return model;
//  }


    /**
     * @param l List of Elements
     * @param c Class of desired Element
     * @return array of elements matching given class c
     */
    @SuppressWarnings("unchecked")
    protected Object[] extractClasses(List l, Class c) {
        ArrayList result = new ArrayList();
        Iterator iter = l.iterator();
        while(iter.hasNext()) {
            Element e = (Element) iter.next();
            if (c.equals(e.getClass())) {
                result.add(e);
            }
        }
        return result.toArray((Object[]) Array.newInstance(c,result.size()));
    }

    /**
     * 
     * @param children
     * @param name
     * @return first class matching the class given.
     */
    protected Object extractClass(List l, Class c) {
        Iterator iter = l.iterator();
        while(iter.hasNext()) {
            Element e = (Element) iter.next();
            if (c.equals(e.getClass())) {
                return e;
            }
        }
        // nothing found
        return null;
    }

    /**
     * sideeffect: if element is affected , set imagedescriptor to error
     * @param region
     */
    public void updatePosition(DirtyRegion region) {
        int end = offset + length;

        //TinyOSPlugin.getDefault().wirteToConsole(this.toString());
        /* end of dirtyregion*/
        int Roffset = region.getOffset();
        int Rlength = region.getLength();
        int Rend = Roffset + Rlength;

        if (region.getType() == DirtyRegion.INSERT) {
            // Primary criteria : where region offset relativ to element offset
            if (Roffset <= offset) {
                offset += Rlength;				
                //System.out.println("--------------------1");
            } else if (Roffset > end) {
                // dirty region after element
                //System.out.println("--------------------2");
            } else {
                // dirty region within element
                length += Rlength;
                setErrorImage();
                //System.out.println("--------------------3");
            }

        } else {
            // Region was removed

            if (Roffset < offset) {
                if (Rend < offset) {
                    offset -= Rlength;
                    length -= Rlength;
                    //System.out.println("--------------------4");
                } else if (Rend < end) {
                    offset = Roffset + Rlength;
                    length = end - Rend;
                    setErrorImage();
                    //System.out.println("--------------------5");
                } else {
                    // Element is deleted...
                    Element e = getParent();
                    if (e!=null) {
                        e.children.remove(this);
                    }
                    //System.out.println("--------------------6");
                }
            } else if (Roffset > end) {
                // dirty region after element
//              System.out.println("--------------------7");
            } else {
                // dirty region within element
                if (Rend  < end) {
                    length -= Rlength;
                    //System.out.println("--------------------8");
                } else {
                    length -= Roffset;
                    //System.out.println("--------------------9");
                }
                setErrorImage();
            }

        }
        //TinyOSPlugin.getDefault().wirteToConsole(this.toString());
    }

    public static Comparator getComparator() {
        return new Comparator() {

            public int compare(Object arg0, Object arg1) {
                Element e = (Element)arg0;
                Element f = (Element)arg1;
                return e.getLabel(null).compareTo(f.getLabel(null));
            }

        };
    }

    private void setErrorImage() {
        image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_ERROR);
    }  
}