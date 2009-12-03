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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.ui.IMarkerResolution;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.markerresolutions.InsertTextResolution;
import tinyos.yeti.editors.quickfixer.QuickFixer;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.figures.LabelContent;
import tinyos.yeti.ep.figures.ModuleContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.parser.standard.FileRegion;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.ComponentASTModelNode;
import tinyos.yeti.nesc.FunctionASTModelNode;
import tinyos.yeti.nesc.RenamedASTModelNodeConnection;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.Icon;

public class ModuleElement extends Element {

    Token module;

    FunctionDefinition[] toImplement;
    FunctionDefinition[] notImplemented;
    FunctionElement[] implemented;

    @Override
    public void updatePosition(DirtyRegion region) {
        super.updatePosition(region);
        if (module != null) {
            module.updatePosition(region);
            if (module.offset == 0 && module.end == 0 && module.line == 0) {
                module = null;
            }
        }
        if (implemented != null) {
            for (int i = 0; i < implemented.length; i++) {
                implemented[i].updatePosition(region);
            }
        }
    }

    public ModuleElement(String string, Token token, Element element) {
        super(string,token,element);
        module = token;
        image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_MODULE);
    }

    public Position getPositionForOutline() {
        return new Position(module.offset,module.length());
    }


    public boolean isFoldable() {
        return true;
    }

    /**
     * 
     */
    @Override
    public SemanticError[] getSemanticErrors( ProjectTOS project ) {

        extractDeclarationsToImplement( project );		
        extractDeclarationsImplemented();
        ArrayList<SemanticError> errors = checkForErrors();

        if (errors.size() > 0) {
            return (SemanticError[]) errors.toArray(new SemanticError[errors.size() - 1]);
        }
        return null;
    }

    /*
     * extract all functions not implemented and additionally
     * test if return type equals the type in the interface
     * and keyword, detects not defined interfacenames
     */
    private ArrayList<SemanticError> checkForErrors() {
        if (toImplement==null) return null;

        HashMap<String,String> renamedTo = new HashMap<String,String>();

        ArrayList<SemanticError> errors = new ArrayList<SemanticError>();
        ArrayList<FunctionDefinition> notImpl = new ArrayList<FunctionDefinition>();

        // Get renamed interface names
        SpecificationListElement sp = (SpecificationListElement) extractClass(children,SpecificationListElement.class);
        if (sp != null) {
            for (int i = 0; i < sp.getChildren(null).length; i++) {
                Object o =  sp.children.get(i);
                if (o instanceof SpecificationElement) {	
                    SpecificationElement se = (SpecificationElement)o;
                    renamedTo.put(se.getRenamed(),se.getName());					
                }
            }
        }	

        for (int i = 0; i < implemented.length; i++) {		
            String s[] = implemented[i].getDeclarator().getIdentifier().split("\\.");
            if (s.length < 2 ) continue;

            String ifacename = s[0];
            String ifacerenamed = (String)renamedTo.get(ifacename);
            if (ifacerenamed == null) {
                // unknown interface
                errors.add(new SemanticError("Unknown interface: "+ifacename,implemented[i].getDeclarator()));
            } 
        }


        for (int i = 0; i < toImplement.length; i++) {
            String functionName = toImplement[i].getFunctionName(); 

            //System.out.println(functionName);
            boolean found = false;
            for (int j = 0; j < implemented.length; j++) {
                //implemented[j].getDeclarator().getParameterCount()
                if (functionName.equals(implemented[j].getDeclarator().identifier)) {
                    if (toImplement[i].node != null) {
                        if (toImplement[i].node.getParameterCount() ==
                            implemented[j].getDeclarator().getParameterCount()) {
                            found = true; break;
                        }
                    }
                }
            }
            if (!found) {
                notImpl.add(toImplement[i]);
                SemanticError e = new SemanticError(
                        "Interface "+toImplement[i].getFunctionName()+" not implemented",
                        sp,
                        QuickFixer.MODULE_INTERFACEFUNCTION_NOT_IMPL);
                Map<String, Object> infos = new HashMap<String, Object>();
                infos.put( QuickFixer.MODULE, this );
                e.addQuickfixInfos( infos );
                errors.add(e);	
            }
        }
        notImplemented = (FunctionDefinition[]) notImpl.toArray(new FunctionDefinition[notImpl.size()]);
        return errors;
    }

    @Override
    public void toNode( ASTModelNode parent, ProjectModel project, ASTModel model, IParseFile file ){
        final Map<String, String> renaming = new HashMap<String, String>();
        
        ComponentASTModelNode node = new ComponentASTModelNode( parent, getName(), getName(), getLabel( this ), file,
                new IFileRegion[]{ new FileRegion( getPositionForOutline(), getLine(), file ) }, 
                Tag.MODULE, Tag.COMPONENT, Tag.OUTLINE, Tag.FIGURE );

        if( !model.addNode( node ) )
            return;

        // Specification: uses and provides interfaces
// TODO check also used/provided functions, AND SET THE FUNCTION-TAG
        ASTModelNode specification = new ASTModelNode( node, node.getIdentifier() + ".specification", null, "Specification", file, null, ASTModel.SPECIFICATION );
        if( model.addNode( specification ) )
            node.addChild( specification );
        
        TagSet usesInterface = TagSet.get( Tag.INTERFACE, Tag.USES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
        TagSet usesComponent = TagSet.get( Tag.COMPONENT, Tag.USES );
        TagSet providesInterface = TagSet.get( Tag.INTERFACE, Tag.PROVIDES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
        TagSet providesComponent = TagSet.get( Tag.COMPONENT, Tag.PROVIDES );
        
        SpecificationListElement sle = (SpecificationListElement)extractClass( children, SpecificationListElement.class );
        Iterator<Element> sleChildren = sle.getChildren().iterator();
        while( sleChildren.hasNext() ){
            Element sleChild = sleChildren.next();
            if( sleChild instanceof SpecificationElement ){
                SpecificationElement se = (SpecificationElement)sleChild;
                
                String name = se.getName();
                String rename = se.getRenamed();
                
                String label = name;
                
                if( rename != null && !rename.equals( name )){
                    label = rename + " (" + name + ")";
                    renaming.put( rename, name );
                }
                else{
                    renaming.put( name, name );
                }
                
                if( se.isProvides() ){
                    ASTModelNode module = (ASTModelNode)model.getNode( null, name, providesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, name, name, name, file, null, providesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    RenamedASTModelNodeConnection reference = new RenamedASTModelNodeConnection( specification, true, name, rename, label, null, providesInterface );
                    specification.addConnection( reference );
                    node.addProvides( name, rename == null ? name : rename );
                }
                else{ // uses
                    ASTModelNode module = (ASTModelNode)model.getNode( null, name, usesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, name, name, name, file, null, usesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    RenamedASTModelNodeConnection reference = new RenamedASTModelNodeConnection( specification, true, name, rename, label, null, usesInterface );
                    specification.addConnection( reference );
                    node.addUses( name, rename == null ? name : rename );
                }
            }
        }
        
        /*
        
        IFileRegion[] regions = new IFileRegion[]{ new FileRegion( getPositionForOutline(), file ) };
        ASTModelNode node = new ASTModelNode( parent, getName(), getLabel( null ), file, regions, Tag.MODULE, Tag.COMPONENT, Tag.OUTLINE, Tag.FIGURE );
        if( !model.addNode( node ) )
            return;

        String[] uses = getUsedInterfaces();
        String[] provides = getProvidedInterfaces();

        if( (uses != null && uses.length > 0) || (provides != null && provides.length > 0) ){
            ASTModelNode specification = new ASTModelNode( node, node.getIdentifier() + ".specification", "specification", 
        	    file, null, ASTModel.SPECIFICATION );
            
            if( model.addNode( specification ) )
                node.addChild( specification );

            if( uses != null ){
                TagSet usesInterface = TagSet.get( Tag.INTERFACE, Tag.USES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
                TagSet usesComponent = TagSet.get( Tag.COMPONENT, Tag.USES );
                for( String use : uses ){
                    ASTModelNode module = (ASTModelNode)model.getNode( null, use, usesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, use, use, file, null, usesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    specification.addReference( use, use, null, usesInterface );
                }
            }

            if( provides != null ){
                TagSet providesInterface = TagSet.get( Tag.INTERFACE, Tag.PROVIDES, Tag.AST_CONNECTION_ICON_RESOLVE, Tag.AST_CONNECTION_LABEL_RESOLVE );
                TagSet providesComponent = TagSet.get( Tag.COMPONENT, Tag.PROVIDES );

                for( String provide : provides ){
                    ASTModelNode module = (ASTModelNode)model.getNode( null, provide, providesComponent );
                    if( module == null ){
                        module = new ASTModelNode( null, provide, provide, file, null, providesComponent );
                        model.addNode( module );
                    }
                    module.addReference( node.getIdentifier(), node.getLabel(), null, node.getTags() );
                    specification.addReference( provide, provide, null, providesInterface );
                }
            }
        }
        */

        if( implemented == null )
            extractDeclarationsImplemented();

        FunctionElement[] functions = getImplementation();
        
        List<IASTFigureContent> contentCommands = new ArrayList<IASTFigureContent>();
        List<IASTFigureContent> contentEvents = new ArrayList<IASTFigureContent>();
        List<IASTFigureContent> contentTasks = new ArrayList<IASTFigureContent>();
        
        if( functions != null && functions.length >  0 ){
            ASTModelNode implementation = new ASTModelNode( node, node.getIdentifier() + ".implementation", null, "implementation", file, null, ASTModel.IMPLEMENTATION );
            if( model.addNode( implementation ) )
                node.addChild( implementation );

            for( FunctionElement function : functions ){
                TagSet set = null;
        	    
                List<IASTFigureContent> destination = null;
        	    
                switch( function.getType() ){
                    case FunctionElement.COMMAND:
                        set = TagSet.get( Tag.FUNCTION, Tag.COMMAND );
                        destination = contentCommands;
                        break;
                    case FunctionElement.EVENT:
                        set = TagSet.get( Tag.FUNCTION, Tag.EVENT );
                        destination = contentEvents;
                        break;
                    case FunctionElement.TASK:
                        set = TagSet.get( Tag.FUNCTION, Tag.TASK );
                        destination = contentTasks;
                        break;
                    default:
                        set = TagSet.get( Tag.FUNCTION );
                }

                String label = function.getLabel( function );
                IFileRegion[] regions = new IFileRegion[]{ new FileRegion( function.getPositionForOutline(), getLine(), file ) };
                implementation.addReference( function.getName(), label, regions, set );
                
                if( destination != null ){
                    destination.add( new LabelContent( label, new Icon( set ), null ) );
                }
            }
        }
        
        ModuleContent content = new ModuleContent(
        		null, null,
                contentCommands.toArray( new IASTFigureContent[ contentCommands.size() ] ),
                contentEvents.toArray( new IASTFigureContent[ contentEvents.size() ] ),
                contentTasks.toArray( new IASTFigureContent[ contentTasks.size() ] ));
        content.addPath( node.getPath().getChild( "implementation" ) );
        
        node.setContent( content );
    }

    private void extractDeclarationsImplemented() {
        if (implemented != null) return;
        ImplementationElement is[] = (ImplementationElement[]) extractClasses(children, ImplementationElement.class );
        if (is.length == 1) {
            implemented = (FunctionElement[]) extractClasses(is[0].children,FunctionElement.class);
//          for (int i = 0; i < implemented.length; i++) {
//          System.out.println("Implemented: "+implemented[i].getLabel(null));
//          }
        }

    }

    @SuppressWarnings("unchecked")
    public String[] getProvidedInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList provides = new ArrayList();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            if (se.isProvides()) {
                provides.add(se.getName());
            }
        }
        return (String[]) provides.toArray(new String[provides.size()]);
    }

    @SuppressWarnings("unchecked")
    public String[] getUsedInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList implemented = new ArrayList();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            if (!se.isProvides()) {
                implemented.add(se.getName());
            }
        }
        return (String[]) implemented.toArray(new String[implemented.size()]);
    }

    private void extractDeclarationsToImplement( ProjectTOS project ) {
        if (toImplement != null)
            return;
        
        if( project == null )
            return;

        ArrayList<FunctionDefinition> result = new ArrayList<FunctionDefinition>();

        SpecificationListElement sle = null;

        Iterator iter = children.iterator();
        while(iter.hasNext()) {
            Element e = (Element) iter.next();

            if (e instanceof SpecificationListElement) {
                sle = (SpecificationListElement) e;
                break;
            }
        }

        iter = sle.children.iterator();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)){ 
                continue;
            }
            SpecificationElement se = (SpecificationElement) element;
            

            ProjectModel model = project.getModel();
            IDeclaration declaration = model.getDeclaration( se.getName(), Kind.INTERFACE );
            if( declaration != null ){
                IASTModelNode node = model.getNode( declaration, null );
                if( node != null ){
                    TagSet tags;
                    if( se.isProvides() )
                        tags = TagSet.get( Tag.FUNCTION, Tag.PROVIDES );
                    else
                        tags = TagSet.get( Tag.FUNCTION, Tag.USES );

                    for( IASTModelNodeConnection connection : node.getChildren() ){
                        if( connection.getTags().contains( tags )){
                            IASTModelNode function = model.getNode( connection, null );
                            if( function != null ){
                                if( function instanceof FunctionASTModelNode ){
                                    FunctionASTModelNode functionNode = (FunctionASTModelNode)function;
                                    FunctionDefinition definition = new FunctionDefinition( se.getRenamed(), functionNode );
                                    result.add( definition );
                                }        
                            }
                        }
                    }
                }
            }
        }
        toImplement = result.toArray(new FunctionDefinition[result.size()]);
    }
    /*
    private NesCModel getModel(SpecificationElement se) {
        File f = TinyOSPlugin.getDefault().locate(se.getName()+".nc");
        NesCModel model = TinyOSPlugin.getDefault().getProjectTOS().getModel(f);

        if ((f != null)&&(model == null)) {
            IProject m =TinyOSPlugin.getDefault().getProjectManager().getlastProject();
            if (m == null) return null;
            model = new NesCModel(m,f,this.getDeclarations(), new LinkedList());

            TinyOSPlugin.getDefault().getProjectTOS().setModel(f,model);
        }
        return model;
    }
    */

    private static class FunctionDefinition {
        public String renamedInterface;
        private FunctionASTModelNode node;
        
        public FunctionDefinition(String renamedInterface, FunctionASTModelNode node) {
            this.renamedInterface = renamedInterface;
            this.node = node;
        }
        

        public String getFunctionName() {
            String name = node.getNodeName();
            
            if( name == null )
                return "";
            else
                return renamedInterface + "." + name;
        }

        public String getSkeleton() {
            return NEWLINE+node.getSkeleton(renamedInterface)+ NEWLINE;
        }
    }

    public IMarkerResolution[] getResolution() {
        ArrayList<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();

        ImplementationElement impl = (ImplementationElement) extractClass(children,ImplementationElement.class);
        if (impl == null) return new IMarkerResolution[0];

        int offset = impl.offset + impl.getLength()-1;

        for (int i = 0; i < notImplemented.length; i++) {
            InsertTextResolution itr = new InsertTextResolution("Add Function: "+notImplemented[i].getFunctionName(),offset,notImplemented[i].getSkeleton());
            resolutions.add(itr);
//          notImplemented[i].getSkeleton()
        }

        if (notImplemented.length > 1) {
            String skeleton="";
            for (int i = 0; i < notImplemented.length; i++) {
                skeleton+= notImplemented[i].getSkeleton();
            }
            InsertTextResolution itr = new InsertTextResolution("Add all unimplemented functions",offset,skeleton);
            resolutions.add(0,itr);
        }

        return (IMarkerResolution[]) resolutions.toArray(new IMarkerResolution[resolutions.size()]);
    }

    public SpecificationElement[] getInterfaces(boolean provides) {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList<SpecificationElement> implemented = new ArrayList<SpecificationElement>();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            if (provides == se.isProvides()) implemented.add(se);
        }
        return (SpecificationElement[])implemented.toArray(new SpecificationElement[implemented.size()]);
    }

    public SpecificationElement[] getInterfaces() {
        SpecificationListElement sle = (SpecificationListElement) extractClass(children,SpecificationListElement.class);

        Iterator iter = sle.children.iterator();
        ArrayList<SpecificationElement> implemented = new ArrayList<SpecificationElement>();
        while(iter.hasNext()) {
            Element element = (Element) iter.next();
            if (!(element instanceof SpecificationElement)) continue;
            SpecificationElement se = (SpecificationElement) element;
            implemented.add(se);
        }
        return (SpecificationElement[])implemented.toArray(new SpecificationElement[implemented.size()]);
    }

    public FunctionElement[] getImplementation() {
        return implemented;
    }

    /**
     * Returns all specific function.. 
     * possible types FunctionElement.TASK EVENT COMMAND OR C
     * @return
     */
    public FunctionElement[] getSpecificFunction(int type) {
        if (implemented == null) extractDeclarationsImplemented();
        ArrayList<FunctionElement> array = new ArrayList<FunctionElement>();
        for (int i = 0; i < implemented.length; i++) {
            FunctionElement elem = implemented[i];
            if (elem.getType() == type) {
                array.add(elem);
            }
        }
        return (FunctionElement[]) array.toArray(new FunctionElement[array.size()]);
    }

    public SpecificationListElement getSpecification() {
        return (SpecificationListElement) extractClass(children, SpecificationListElement.class);
    }

    /**
     * Returns a declaration element if a command or event was used, for normal
     * interfaces a specificationelement is returned
     * @param renamedInterfaceName
     * @return
     */
    ISpecificationElement getSpecificationElement(String renamedInterfaceName) {
        SpecificationListElement sle = getSpecification();
        Iterator iter = sle.getChildren().iterator();

        while(iter.hasNext()) {
            ISpecificationElement se = (ISpecificationElement) iter.next();
            System.out.println(se.getRenamed());
            if (renamedInterfaceName.equals(se.getRenamed())) {
                return se;
            }
        }
        return null;
    }


}
