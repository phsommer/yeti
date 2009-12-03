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
package tinyos.yeti.nesc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.rules.FastPartitioner;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCPartitions;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.nesc.NesCDocumentPartitioner;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.ASTView;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNodeFactory;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclarationFactory;
import tinyos.yeti.ep.parser.IFoldingRegion;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.IMissingResourceRecorder;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCCompletionProposal;
import tinyos.yeti.ep.parser.INesCDefinitionCollector;
import tinyos.yeti.ep.parser.INesCInitializer;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.ep.parser.ProposalLocation;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.GenericStorage;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.parser.Declaration;
import tinyos.yeti.nesc.parser.ErrorMessages;
import tinyos.yeti.nesc.parser.HeaderFileParser;
import tinyos.yeti.nesc.parser.NameSpace;
import tinyos.yeti.nesc.parser.NesCparser;
import tinyos.yeti.nesc.parser.ParserError;
import tinyos.yeti.nesc.parser.SimpleDeclaration;
import tinyos.yeti.nesc.parser.yyException;
import tinyos.yeti.nesc.parser.language.NesCCompletionProposal;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.parser.language.elements.ConfigurationElement;
import tinyos.yeti.nesc.parser.language.elements.ConfigurationImplElement;
import tinyos.yeti.nesc.parser.language.elements.Element;
import tinyos.yeti.nesc.parser.language.elements.InterfaceElement;
import tinyos.yeti.nesc.parser.language.elements.ModuleElement;
import tinyos.yeti.nesc.parser.language.elements.RenamedIdentifierElement;
import tinyos.yeti.nesc.scanner.Scanner;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.DocumentUtility;

public class NesCParserFactory implements INesCParserFactory {
    public Comparator<TagSet> createComparator(){
        return new Comparator<TagSet>(){
            public int compare( TagSet a, TagSet b ){
                boolean aSpec = a.contains( ASTModel.SPECIFICATION );
                boolean bSpec = b.contains( ASTModel.SPECIFICATION );
                
                if( aSpec == bSpec )
                    return 0;
                
                if( aSpec )
                    return -1;
                
                return 1;
            }
        };
    }
    
    public IDeclarationFactory getDeclarationFactory(){
        // TODO write or read declarations
        return null;
    }
    
    public IASTModelNodeFactory getModelNodeFactory(){
        // TODO write or read nodes
        return null;
    }
    
    public IStorage createStorage( ProjectTOS project, DataInputStream in, IProgressMonitor monitor ) throws IOException{
        return new GenericStorage( project, in, monitor );
    }
    
    public IStorage createStorage( ProjectTOS project, DataOutputStream out, IProgressMonitor monitor ) throws IOException{
        return new GenericStorage( project, out, monitor );
    }
    
    public INesCParser createParser( IProject project ) {
        return new Parser( project, new LinkedList<String>() );
    }
    
    public INesCInitializer createInitializer( IProject project ) {
    	return new NesCInitializer();
    }
    
    public INesCDefinitionCollector createCollector( ProjectTOS project, IParseFile parseFile ){
        return null;
    }
    
    public IASTModel createModel( IProject project ){
        return new ASTModel( project );
    }
    
    public IASTModelPath createRoot( IProject project ) {
        return new ASTModelPath( null );
    }

    public ASTView[] getViews(){
        return new ASTView[]{
                new ASTView( "&Provides", NesCIcons.icons().get(NesCIcons.ICON_PROVIDES_INTERFACE ), ASTNodeFilterFactory.subset( Tag.COMPONENT, Tag.PROVIDES ), false ),
                new ASTView( "&Uses", NesCIcons.icons().get(NesCIcons.ICON_USES_INTERFACE ), ASTNodeFilterFactory.subset( Tag.COMPONENT, Tag.USES ), false ),
                new ASTView( "&Interfaces", NesCIcons.icons().get(NesCIcons.ICON_INTERFACE ), ASTNodeFilterFactory.subset( Tag.INTERFACE ), true ),
                new ASTView( "&Configurations", NesCIcons.icons().get(NesCIcons.ICON_CONFIGURATION), ASTNodeFilterFactory.subset( Tag.CONFIGURATION ), false ),
                new ASTView( "&Modules", NesCIcons.icons().get(NesCIcons.ICON_MODULE ), ASTNodeFilterFactory.subset( Tag.MODULE ), false ),
                //new ASTView( "&Structs", NesCIcons.get(NesCIcons.ICON_STRUCT ), ASTNodeFilterFactory.subset( Tag.DATA_OBJECT ), false )
        };
    }
    
    public ImageDescriptor getImageFor( TagSet tags ){
        return ASTModel.getImageFor( tags );
    }
    
    public TagSet getSupportedTags(){
    	return ASTModel.getSupportedTags();
    }
    
    public TagSet getDecoratingTags(){
	    return TagSet.EMPTY;
    }
    
    public IDeclaration toBasicType( String type, String name, IDeclaration[] typedefs ) {
        return new SimpleDeclaration( name, null, Kind.TYPEDEF, null );
    }
    
    public INesCCompletionProposal[] getProposals( ProposalLocation location ) {
        IDocument document = location.getDocument().getDocument();
        int offset = location.getOffset();
        String prefix = location.getPrefix();
        
        FastPartitioner partitioner = new NesCDocumentPartitioner();

        partitioner.connect(document,false);

        // components keyword should exist..
        ITypedRegion[] r = partitioner.computePartitioning(0,document.getLength());
        int offsetOfComponentsKey = -1;
        ITypedRegion inRegion = null;
        for (int i = 0; i < r.length; i++) {
            inRegion = r[i];
            if (inRegion.getType().equals(INesCPartitions.DEFAULT)) {
                try {
                    String temp = document.get(inRegion.getOffset(),inRegion.getLength());
                    if (temp.indexOf("components") != -1) {
                        offsetOfComponentsKey = temp.indexOf("components");
                        break;
                    }
                } catch (BadLocationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }   
        }

        if (offsetOfComponentsKey==-1) {
            return null;
        } 

        // get components till semikolon;
        boolean semikolonFound=false; 
        int compOff = inRegion.getOffset() + offsetOfComponentsKey + "components".length();

        String components = "";
        while(!semikolonFound&&(document.getLength()>=compOff)) {
            ITypedRegion[] irs = partitioner.computePartitioning(compOff,1);
            if (irs[0].getType().equals(INesCPartitions.DEFAULT)) {
                char c;
                try {
                    c = document.getChar(compOff++);
                    if (c == ';') {
                        semikolonFound = true;
                    } else {
                        components += c;
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            } else {
                compOff++;
            }
        }
        components = components.trim();

        if (components.equals("")) return null;

        // individual modules
        String[] comps = components.split(",");
        ArrayList<RenamedIdentifierElement> compElements = new ArrayList<RenamedIdentifierElement>();
        for (int i = 0; i < comps.length; i++) {
            String string = comps[i];

            if (string.indexOf("as")== -1) {
                compElements.add(new RenamedIdentifierElement(new Token(1,string.trim(),0,0,0)));
            } else {
                RenamedIdentifierElement t = new RenamedIdentifierElement(new Token(1,string.split("as")[0].trim(),0,0,0));
                t.setRenamedTo(string.split("as")[1].trim());
                compElements.add(t);
            }

        }       


        ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();

        // what is considered:
        // ---------------------------------------------------------
        //  Listing Modules:
        // ---------------------------------------------------------
        //  1.  [';'|'}'] WHITESPACES *
        //          should list all included components
        //  2.  [';'|'}'] WHITESPACES M*    
        //          should list all included components beginning with the letter m
        //  3.  [';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] *
        //          should list all included components
        //  4.  [';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai*
        //          should list all included components beginning with the letter Mai
        // ---------------------------------------------------------
        // Listing Interfaces:
        // ---------------------------------------------------------
        //  5.  [';'|'}'] WHITESPACES Main.*
        //          should list all interfaces from main
        //  6.  [';'|'}'] WHITESPACES Main.S*
        //          should list all interfaces from main that begins with S
        //          (case insensitiv)
        //  7.  [';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai.*
        //          should list all interfaces from Mai which are valid for the wiring
        //  8.  [';'|'}'] WHITESPACES Main[.sadfsd] [=,<-,->] Mai.S*
        //          should list all interfaces from Mai which are valid for the wiring beginnig with S      
        // ---------------------------------------------------------
        // Listing Wiring Statements
        // ---------------------------------------------------------
        //  9.      [';'|'}'] WHITESPACES Main[.sadfs] *
        //          should list wiring statements

        int start = inRegion.getOffset() + offsetOfComponentsKey + "components".length();
        int end = offset;
        String text;
        try {
            text = document.get(start,end-start);
        }
        catch( BadLocationException e ) {
            e.printStackTrace();
            text = "";
        }

        int lastIndexSemikolon = text.lastIndexOf(';');
        int lastIndexKlammer = text.lastIndexOf('}');

        int last = (lastIndexKlammer > lastIndexSemikolon) ? lastIndexKlammer : lastIndexSemikolon;

        text = text.substring(last+1);

        if (text.trim().equalsIgnoreCase("")) {
            // 1.
            result.addAll(listComponents(offset,prefix,compElements));

        } else {

            if ((text.trim().endsWith("->"))||(text.trim().endsWith("<-"))||(text.trim().endsWith("="))) {
                // 3. 
                result.addAll(listComponents(offset,prefix,compElements));
            } else if (text.endsWith(".")) {
                // Case 5 / 7

                // 7.
                if (((text.indexOf("->"))!=-1)||((text.indexOf("<-"))!=-1)||((text.indexOf("="))!=-1)){             
                    Collection<? extends INesCCompletionProposal> l = listInterfaces(
                            compElements, DocumentUtility.lastWord(document,offset-1), " ", location );
                    if (l != null) result.addAll(l);
                } else {
                    // 5.
                    Collection<? extends INesCCompletionProposal> l = listInterfaces(
                            compElements, DocumentUtility.lastWord(document,offset-1), " ", location );
                    if (l != null) result.addAll(l);
                }

            } else if (text.endsWith(" ")) {
//              // 9.
                result.addAll(ConfigurationImplElement.listWiringStatementsProposal(offset));
            } else {
//              // text ends with letter..

//              // in right side
                if (((text.indexOf("->"))!=-1)||((text.indexOf("<-"))!=-1)||((text.indexOf("="))!=-1)){
                    int s = -1;
                    int op = -1;
                    String[] ind = new String[]{"->","=","<-"};
                    for (int i = 0; i < ind.length; i++) {
                        if (text.indexOf(ind[i])!=-1) {
                            s = text.indexOf(ind[i]);
                            op = i;
                        }
                    }               

                    if (text.substring(s).indexOf(".")!=-1) {
//                      // 8. @todo give only correct wiring statements..
                        String rename =  text.substring(s+ind[op].length()).trim();
                        rename = rename.substring(0,rename.indexOf("."));
                        result.addAll(listInterfaces(
                                compElements, rename, ";", location ));
                    } else {
                        // 4.
                        result.addAll(listComponents(offset, prefix, compElements));
                    }

                } else {
//                  // left side
                    if (text.indexOf(".")!=-1) {
//                      // 6.
                        result.addAll(listInterfaces(
                                compElements, text.substring(0,text.indexOf(".")).trim(), " ", location ));
                    } else {
//                      // 2.
                        result.addAll(listComponents(offset, prefix, compElements));
                    }
                }
            }

        }

        partitioner.disconnect();
        return result.toArray(new INesCCompletionProposal[result.size()]);

    }
    

    protected Collection<? extends INesCCompletionProposal> listComponents(int offset,String prefix, ArrayList<RenamedIdentifierElement> list) {
        Iterator<RenamedIdentifierElement> iter = list.iterator();

        ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();

        while(iter.hasNext()){
            RenamedIdentifierElement ce = iter.next();
            if (ce.getRenamed().toLowerCase().startsWith(prefix.toLowerCase())) {

                CompletionProposal proposal = new CompletionProposal(
                        ce.getRenamed(),                    // replacement string
                        offset - prefix.length(),           // replacement offset
                        prefix.length(),                    // replacement length
                        ce.getRenamed().length(),           // cursor position              
                        NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_MODULE).createImage(), //image
                        ce.getLabel(null),                  //DisplayString
                        null,                               // IContextInformation
                        ConfigurationImplElement.getAdditionalProposalInfo(ce.getName())        // additional proposal info
                );

                result.add(new NesCCompletionProposal(proposal));
            }
        }
        return result;
    }

    private Collection<? extends INesCCompletionProposal> listInterfaces( 
            ArrayList<RenamedIdentifierElement> components, 
            String renamedComponent,
            String addOn,
            ProposalLocation location ) {
        
        ProjectTOS tos = location.getProject();
        if( tos == null )
            return Collections.emptyList();
        
        String prefix = location.getPrefix();
        int offset = location.getOffset();
        
        Iterator<RenamedIdentifierElement> iter = components.iterator();
        ArrayList<INesCCompletionProposal> result = new ArrayList<INesCCompletionProposal>();

        // get the original name of the component
        ProjectModel model = tos.getModel();
        while(iter.hasNext()) {
            RenamedIdentifierElement c = iter.next();
            if (c.getRenamed().equals(renamedComponent)) {
                // fetch component model and extract interfaces
                
                IDeclaration declaration = model.getDeclaration( c.getName(), Kind.MODULE, Kind.CONFIGURATION );
                if( declaration != null ){
                    ComponentASTModelNode node = (ComponentASTModelNode)model.getNode( declaration, null );
                    String lowerPrefix = prefix.toLowerCase();
                    
                    for( String interfaze : node.getUsesProvides() ){
                        if( interfaze.toLowerCase().startsWith( lowerPrefix )){
                            TagSet tags;
                            if( node.getUses( interfaze ) != null )
                                tags = TagSet.get( Tag.INTERFACE, Tag.USES );
                            else if( node.getProvides( interfaze ) != null )
                                tags = TagSet.get( Tag.INTERFACE, Tag.PROVIDES );
                            else
                                tags = TagSet.get( Tag.INTERFACE );
                            
                            String name = node.get( interfaze );
                            String label;
                            if( name != null && !name.equals( interfaze ))
                                label = name + " as " + interfaze;
                            else
                                label = interfaze;
                            
                            CompletionProposal proposal = new CompletionProposal(
                                    interfaze+addOn,             // replacement string
                                    offset - prefix.length(),           // replacement offset
                                    prefix.length(),                    // replacement length
                                    interfaze.length()+addOn.length(),       // cursor position
                                    NesCIcons.icons().get( ASTModel.getImageFor( tags ) ), //image
                                    label,                       //DisplayString
                                    null,                               // IContextInformation
                                    ConfigurationImplElement.getAdditionalProposalInfo(interfaze)   // additional proposal info
                            );
    
                            result.add(new NesCCompletionProposal( proposal ));
                        }
                    }
                }
                break;
            }
        }
        return result;
    }


    private class Parser implements INesCParser{
        private IProject project;
        
        private NesCparser ncParser;
        private HeaderFileParser hParser;
        
        private Element root;
        private List<IDeclaration> declarations;
        private ParserError error;
        private List<Token> multiLineCommentTokens;
        private boolean createMessages = false;
        private List<SemanticError> messages;
        private ASTModel model;
        private IParseFile file;
        private boolean createDeclarations = false;
        private IMissingResourceRecorder recorder;
        
        public Parser( IProject project, LinkedList<String> fileHistory ){
            this.project = project;
            ncParser = new NesCparser( new String[]{}, project, fileHistory );
            hParser = new HeaderFileParser( project, null, fileHistory );
        }

        public void setMissingResourceRecorder( IMissingResourceRecorder recorder ){
            this.recorder = recorder;
        }
        
        public IMissingResourceRecorder getMissingResourceRecorder(){
            return recorder;
        }
        
        public IMessage[] getMessages() {
            int length = 0;
            if( messages != null )
                length += messages.size();
            
            if( error != null )
                length++;
            
            IMessage[] result = new IMessage[ length ];
            
            if( messages != null )
                result = messages.toArray( result );
            
            if( error != null )
                result[ result.length-1 ] = error;
            
            return result;
        }
        
        public void addBasicType( IDeclaration declaration ) {
            ncParser.ns.addTypesToGlobalScope( new String[]{ declaration.getName() } );
        }
        
        public void setCreateDeclarations( boolean create ) {
            createDeclarations = create;
        }
        
        public IDeclaration[] getDeclarations() {
            if( declarations == null )
                return null;
            
            return declarations.toArray( new IDeclaration[ declarations.size() ] );
            
            /*if( namespace == null )
                return null;
            
            Declaration[] declarations = namespace.getDeclarations();
            IDeclaration[] decls = new IDeclaration[ declarations.length ];
            System.arraycopy( declarations, 0, decls, 0, decls.length );
            return decls;*/
        }
        
        public void addDeclarations( IDeclaration[] declarations ) {
            List<Declaration> forward = new ArrayList<Declaration>();
            for( IDeclaration decl : declarations ){
                if( decl instanceof Declaration )
                    forward.add( (Declaration)decl );
            }
            
            ncParser.ns.setTypeDefs( forward.toArray( new Declaration[ forward.size() ] ) );
        }
        
        public void addMacro( IMacro macro ){
            // ignore
        }
        
        public void setCreateAST( boolean create ) {
            // ignore, this ncParser creates an ast anyway
        }
        
        public void setCreateInspector( boolean create ){
        	// ignore, this parser does not supports inspectors
        }
        
        public void setResolveFullModel( boolean full ) {
            // ignore
        }
        
        public void setCreateReferences( boolean create ){
	        // ignore	
        }
        
        public IASTReference[] getReferences(){
        	return null;
        }
        
        public INesCAST getAST() {
            if( root == null )
                return null;
            
            return new NesCAST( file, root );
        }
        
        public INesCInspector getInspector(){
        	return null;
        }
        
        public boolean parse( IMultiReader reader, IProgressMonitor monitor ) throws IOException{
            Reader read = reader.open();
            boolean result = parse( read );
            read.close();
            return result;
        }
        
        public boolean parse( Reader reader ) throws IOException {
        	ProjectTOS tos = null;
        	
            if( project != null ){
            	try{
	                tos = TinyOSPlugin.getDefault().getProjectTOS( project );
	                if( tos != null ){
	                    tos.getModel().addDeclarations( file, this );
	                }
            	}
            	catch( MissingNatureException ex ){
            		// silent
            	}
            }
            
            multiLineCommentTokens = null;
            root = null;
            error = null;
            declarations = null;
            if( createMessages )
                messages = new ArrayList<SemanticError>();
            else
                messages = null;
            
            if( file != null && file.getPath().endsWith( ".nc" )){
                Scanner scanner = new Scanner(reader);
                scanner.setCallback(ncParser);
                ncParser.setScanner(scanner);

                ParserError pe = new ParserError();
                //jay.yydebug.yyDebug debug = new jay.yydebug.yyDebugAdapter();
                Object oRoot;
                try {
                    oRoot = ncParser.yyparse(scanner,pe);

                    multiLineCommentTokens = scanner.multiLineCommentTokens;
                    // could be also token.. arraylist..
                    if (oRoot instanceof Element) {
                        root = (Element) oRoot;
                    } else {
                        root = null;
                    }

                    if( root != null && model != null && tos != null ){
                        root.toNode( null, tos.getModel(), model, file );
                    }

                    if (pe.state != -1) {
                        error = ErrorMessages.getDetailedMessage(pe);
                    }

                    if( createDeclarations ){
                        if( root != null ){
                            declarations = new ArrayList<IDeclaration>();
                            // search for interfaces, modules and configurations
                            Iterator<Element> iterator = root.iterator();
                            while( iterator.hasNext() ){
                                Element next = iterator.next();
                                if( next instanceof InterfaceElement ){
                                    String name = ((InterfaceElement)next).getName();
                                    declarations.add( new SimpleDeclaration( name, file, IDeclaration.Kind.INTERFACE, TagSet.get( Tag.INTERFACE ) ));
                                }
                                if( next instanceof ModuleElement ){
                                    String name = ((ModuleElement)next).getName();
                                    declarations.add( new SimpleDeclaration( name, file, IDeclaration.Kind.MODULE, TagSet.get( Tag.MODULE, Tag.COMPONENT ) ));
                                }
                                if( next instanceof ConfigurationElement ){
                                    String name = ((ConfigurationElement)next).getName();
                                    declarations.add( new SimpleDeclaration( name, file, IDeclaration.Kind.CONFIGURATION, TagSet.get( Tag.CONFIGURATION, Tag.COMPONENT ) ));
                                }
                            }
                        }
                    }
                    
                    if( createMessages ){
                        ArrayList<SemanticError> warnings = ncParser.getWarnings();
                        if( warnings != null )
                            messages.addAll( warnings );
                        
                        if( root != null && tos != null ){
                            collectMessages( messages, root, tos );
                        }
                    }
                    return true;
                }
                catch( yyException e ) {
                    e.printStackTrace();
                    return false;
                }
                catch( ArrayIndexOutOfBoundsException e ){
                    e.printStackTrace();
                    return false;
                }
                catch( NullPointerException e ){
                    e.printStackTrace();
                    return false;
                }
            }
            else if( file == null || file.getPath().endsWith( ".h" )){
                try {
                    Scanner scanner = new Scanner(reader);
                    scanner.setCallback(hParser);
                    hParser.setScanner(scanner);
                    
                    ParserError pe = new ParserError();
                    
                    hParser.yyparse( scanner, pe );
                    
                    if (pe.state != -1) {
                        error = ErrorMessages.getDetailedMessage(pe);
                    }

                    if( createDeclarations ){
                        NameSpace namespace = ncParser.ns;
                        // TODO grab the new definitions here
                    }
                    
                    return true;
                }
                catch( yyException e ) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            return false;
        }
        
        @SuppressWarnings("unchecked")
        private void collectMessages( List<SemanticError> messages, Element root, ProjectTOS project ){
            SemanticError[] errors = root.getSemanticErrors( project );
            if( errors != null ){
                for( SemanticError error : errors )
                    messages.add( error );
            }
            
            SemanticError[] warnings = root.getSemanticWarnings( project );
            if( warnings != null ){
                for( SemanticError warning : warnings )
                    messages.add( warning );
            }
            
            List<Element> children = root.getChildren();
            if( children != null ){
                for( Element child : children )
                    collectMessages( messages, child, project );
            }
        }
        
        public void setCreateFoldingRegions( boolean create ){
            // ignore this feature
        }
        
        public IFoldingRegion[] getFoldingRegions(){
            // no implementation provided for this feature
            return null;
        }
        
        public List<Token> getMultiLineCommentTokens() {
            return multiLineCommentTokens;
        }
        
        public ParserError getParserError() {
            return error;
        }
        
        public Element getRoot() {
            return root;
        }

        public void setASTModel( IASTModel model ){
            this.model = (ASTModel)model;
        }
        
        public IASTModel getASTModel() {
            return model;
        }

        public void setParseFile( IParseFile name ) {
            this.file = name;
            ncParser.ns.setParseFile( name );
            hParser.getNameSpace().setParseFile( name );
        }
        
        public void setCreateMessages( boolean create ) {
            createMessages = create;
        }

        public void setFollowIncludes( boolean follow ) {
            ncParser.setFollowIncludes( follow );
        }
    }
}
