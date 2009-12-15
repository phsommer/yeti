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
package tinyos.yeti.make;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.MakeExtra;
import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.MakeInclude.Include;
import tinyos.yeti.make.targets.DefaultSharedMakeTarget;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

/**
 * Used for one {@link IProject} this {@link ProjectTargets} is a list
 * of {@link IMakeTarget}s. It contains methods to persistently store and
 * load targets.
 */
public class ProjectTargets implements IProjectMakeTargets{
	public static final String MAKE_TARGET_KEY = ".targetsOptions"; 

	private static final String CLIENT_DATA = "client-data";
	private static final String CLIENT_DATA_ENTRY = "data";
	
	private static final String BUILD_TARGET_DEFAULT = "default";
	private static final String BUILD_TARGET_CUSTOM = "custom";
	private static final String BUILD_TARGET_ELEMENTS = "buildTargets"; 

	private static final String TARGET_ELEMENT = "target"; 
	private static final String TARGET_SELECTED = "selected";
	private static final String TARGET_DEFAULT = "default";

	private static final String INCLUDES_ELEMENTS = "includes";
	private static final String INCLUDES_ATTR_DEFAULTS = "defaults";
	private static final String INCLUDES_ATTR_BUILD = "build";
	private static final String INCLUDE_ELEMENT = "include";
	private static final String INCLUDE_ATTR_TYPE = "type";
	private static final String INCLUDE_ATTR_RECURSIVE = "recursive";
	
	private static final String EXCLUDE_ELEMENTS = "excludes";
	private static final String EXCLUDE_ELEMENT = "exclude";

	private static final String TYPEDEF_ELEMENTS = "typedefs";
	private static final String TYPEDEF_ELEMENT = "typedef";
	private static final String TYPEDEF_ATTR_TYPE = "specifier";
	private static final String TYPEDEF_ATTR_NAME = "declarator";

	private static final String BOARDS_ELEMENTS = "boards";
	private static final String TARGET_BOARD = "board";
	
	private static final String NOSTDINC = "nostdinc";

	private static final String TARGET_ATTR_VERSION = "version";
	private static final String TARGET_ATTR_NAME = "name";
	private static final String TARGET_ATTR_NOSTDINCT = "nostdinc";
	private static final String TARGET_ATTR_LOOP = "loop";
	private static final String TARGET_ATTR_LTIME = "time";

	private static final String TARGET_ATTR_USE_DEFAULT = "use-default";
	private static final String TARGET_ATTR_USE_LOCAL = "use-custom";
	
	private static final String EXTRA_ELEMENTS = "extras";
	private static final String EXTRA_ELEMENT = "extra";
	private static final String EXTRA_PARAM = "param";

	private static final String EXTRA_NAME = "name";
	private static final String EXTRA_OPTION_NAME = "name";
	private static final String EXTRA_ASK_BEFORE_COMPILE= "ask";
	
	private static final String MACRO_ELEMENTS = "macros";
	private static final String MACRO_ELEMENT = "macro";
	private static final String MACRO_ATTR_NAME = "name";
	private static final String MACRO_ATTR_CONTENT = "content";
	private static final String MACRO_ATTR_YETI = "yeti";
	private static final String MACRO_ATTR_NCC = "ncc";

	private static final String APPLICATION_NAME = "application";

	/** default properties for the associated project */
	private MakeTargetSkeleton defaults;
	
	private DefaultSharedMakeTarget sharedDefaults;
	
	/** customized properties */
	private List<MakeTarget> targets = new ArrayList<MakeTarget>();

	private IProject project = null;

	private MakeTargetManager manager;
	
	private IMakeTargetMorpheable selection;
	
	private Map<String, String> clientData = new HashMap<String, String>();
	
	public ProjectTargets( MakeTargetManager manager, IProject project ) {
		this.project = project;
		
		defaults = new MakeTargetSkeleton( project );
		defaults.setCustomExcludes( MakeExclude.DEFAULT_EXCLUDES );

		Document document = translateNCProjectToDocument();

		setSelectedTarget( defaults, false );
		
		if (document != null) {
			read( document );
		}
		
		this.manager = manager;
	}
	
	public void put( String key, String value ){
		clientData.put( key, value );
		saveTargets();
	}
	
	public String get( String key ){
		return clientData.get( key );
	}
	
	public MakeTargetSkeleton getDefaults(){
		return defaults;
	}
	
	public void setDefaults( MakeTargetSkeleton defaults ){
		if( defaults.getProject() != project )
			throw new IllegalArgumentException( "wrong project in target" );

		this.defaults = defaults;
		
		for( MakeTarget target : targets ){
			target.setDefaults( defaults );
		}
		
		informDefaultsChanged();
	}
	
	public SharedMakeTarget<MakeTargetSkeleton> openDefaults( boolean open ){
		if( sharedDefaults == null ){
			sharedDefaults = new DefaultSharedMakeTarget( this ){
				@Override
				protected MakeTargetSkeleton write( MakeTargetSkeleton copy, MakeTargetSkeleton original ){
					sharedDefaults = null;
					return super.write( copy, original );
				}
				
				@Override
				public void cancel(){
					super.cancel();
					sharedDefaults = null;
				}
			};
		}
		
		if( open ){
			sharedDefaults.open();
		}
		
		return sharedDefaults;
	}
	
	private Document translateNCProjectToDocument() {
		String xml = "";
		try {
			if (getProject().isAccessible()) {
				BufferedReader input = new BufferedReader(new FileReader(new File(getProject().getLocation().toFile(),MAKE_TARGET_KEY)));
				String line = null;
				while (( line = input.readLine()) != null){
					xml+=(line);
					xml+=System.getProperty("line.separator");
				}
				input.close();
			}
		} catch (FileNotFoundException e1) {
			// nothing serious..
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// String xml = TinyOSPlugin.getDefault().getPreferenceStore().getString(getProject().getName()+MAKE_TARGET_KEY);
		if (!xml.equals("")) {
			try {
				//System.out.println(xml);
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
				return document;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public boolean setSelectedTarget( IMakeTargetMorpheable target ){
		return setSelectedTarget( target, true );
	}
	
	protected boolean setSelectedTarget( IMakeTargetMorpheable target, boolean save ){
		IMakeTargetMorpheable im = getSelectedTarget();
        		
		if( im != target ){
			selection = target;
			if( save ){
				saveTargets();
			}
			if( manager != null ){
				manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.SELECTED_TARGET_CHANGED, project ) );
			}
		}
        
        return true;
	}
	
	public IMakeTargetMorpheable getSelectedTarget(){
		return selection;
    }

	public boolean addStandardTarget( MakeTarget target ){
		if( targets.contains( target ) ) {
			return false; 
		}
		return addStandardTarget( target, true );
	}

	private boolean addStandardTarget( MakeTarget target, boolean check){
		if (!"".equals( target.getName() )){
			//target.setDefaults( defaults );
			targets.add(target);
			target.setDefaults( defaults );
			
			if( check ){
				if( manager != null )
					manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.TARGET_ADD, target ) );
				
				if( getSelectedTarget() == null ){
					setSelectedTarget( target, false );
				}

				saveTargets();
			}
			return true;
		}
		
		return false;
	}

	public boolean containsStandardTarget(MakeTarget target) {
		return targets.contains( target );
	}


	public boolean removeStandardTarget(MakeTarget target) {
		if( targets.remove( target ) ){
	        if( manager != null )
	        	manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.TARGET_REMOVED, target ) );
	        
	        // make another target default
	        if( getSelectedTarget() == target ){
	            MakeTarget[] ims = getStandardTargets();
	            if( ( ims != null ) && ( ims.length > 0 ) ){
	                setSelectedTarget( ims[0], false );
	                informStandardTargetChanged( ims[0] );
	            }
	            else{
	            	setSelectedTarget( defaults, false );
	            }
	        }
	        
	        saveTargets();
	        
	        return true;
		}
		else
			return false;
	}


	public MakeTarget[] getStandardTargets() {
		return targets.toArray( new MakeTarget[ targets.size() ] );
	}
	
	public IMakeTargetMorpheable[] getSelectableTargets(){
		IMakeTargetMorpheable[] result = new IMakeTargetMorpheable[ targets.size()+1 ];
		result[0] = defaults;
		for( int i = 1; i < result.length; i++ ){
			result[i] = targets.get( i-1 );
		}
		return result;
	}
	
	public String getNameForSelectable( IMakeTargetMorpheable morph ){
		if( morph instanceof MakeTarget )
			return ((MakeTarget)morph).getName();
		
		if( morph == defaults )
			return project.getName();
		
		return morph.toMakeTarget().getName();
	}

	public MakeTarget findStandardTarget( String name ) {
		for( MakeTarget target : targets ){
			if( target.getName().equals( name )){
				return target;
			}
		}

		return null;
	}
	
	public void informStandardTargetChanged( MakeTarget target ){
		saveTargets();
		if( manager != null )
			manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.TARGET_CHANGED, target ) );
	}
	
	public void informDefaultsChanged(){
		saveTargets();
		if( manager != null ){
			if( defaults != null ){
				manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.DEFAULT_TARGET_CHANGED, project ) );
			}
			
			MakeTarget[] targets = getStandardTargets();
			if( targets != null ){
				for( MakeTarget target : targets ){
					manager.notifyListeners( new MakeTargetEvent( this, MakeTargetEvent.TARGET_CHANGED, target ) );
				}
			}	
		}
	}

	public void saveTargets() {
		Document doc = getAsXML();

		try {
			saveDocument(doc);
		} catch (IOException e) {
			TinyOSPlugin.getDefault().log("Problems Saving MakeOptions", e);
		}

	}

	protected void saveTargets(Document doc, OutputStream output) throws TransformerException {
		Debug.info( "Saving make options for: " + project.getName() );
		
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(4));

		Transformer transformer;
		transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");

		DOMSource source = new DOMSource(doc);
		StreamResult outputTarget = new StreamResult(output);

		transformer.transform(source, outputTarget);
	}

	protected Document getAsXML() {
		XWriteStack xml = new XWriteStack();
		xml.push( BUILD_TARGET_ELEMENTS );

		if( defaults != null ){
			xml.push( BUILD_TARGET_DEFAULT );
			xml.setAttribute( TARGET_ATTR_VERSION, "2" );
			writeContent( defaults, xml, selection );
			xml.pop();
		}
		
		if( targets.size() > 0 ){
			xml.push( BUILD_TARGET_CUSTOM );
			for( MakeTarget target : targets ){
				write( target, xml, selection );
			}
			xml.pop();
		}

		xml.push( CLIENT_DATA );
		for( Map.Entry<String, String> entry : clientData.entrySet() ){
			xml.push( CLIENT_DATA_ENTRY );
			xml.setAttribute( "key", entry.getKey() );
			xml.setText( entry.getValue() );
			xml.pop();
		}
		xml.pop();
		
		xml.pop();
		
		return xml.getDocument();
	}

	/**
	 * Creates the contents of an xml file that has exactly <code>defaults</code>
	 * as default entry.
	 * @param defaults the default entry
	 * @return the xml file for a {@link ProjectTargets} that is empty
	 * except of <code>defaults</code>
	 */
	public static String convert( MakeTargetSkeleton defaults ){
		try{
			XWriteStack xml = new XWriteStack();
			xml.push( BUILD_TARGET_ELEMENTS );
			
			xml.push( BUILD_TARGET_DEFAULT );
			xml.setAttribute( TARGET_ATTR_VERSION, "2" );
			writeContent( defaults, xml, defaults );
			xml.pop();
	
			xml.pop();
		
			return toString( xml.getDocument() );
		}
		
		catch( TransformerConfigurationException e ){
			// this should not happen...
			TinyOSPlugin.log( e );
			return "";
		}
		catch( TransformerException e ){
			// this should not happen...
			TinyOSPlugin.log( e );
			return "";
		}
	}
	
	/**
	 * Converts a collection of {@link MakeTarget}s into a {@link String} that can
	 * later be converted back into {@link MakeTarget}s.
	 * @param targets the collection of targets to convert
	 * @return the string, in xml format
	 */
	public static String convert( Collection<MakeTarget> targets ){
		try{
			XWriteStack xml = new XWriteStack();
			xml.push( BUILD_TARGET_ELEMENTS );

			for( MakeTarget target : targets ){
				write( target, xml, null );
			}

			return toString( xml.getDocument() );
		}
		
		catch( TransformerConfigurationException e ){
			// this should not happen...
			TinyOSPlugin.log( e );
			return "";
		}
		catch( TransformerException e ){
			// this should not happen...
			TinyOSPlugin.log( e );
			return "";
		}
	}
	
	private static String toString( Document doc ) throws TransformerConfigurationException, TransformerException{
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(4));

		Transformer transformer;
		transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");

		DOMSource source = new DOMSource( doc );
		StringWriter result = new StringWriter();
		StreamResult outputTarget = new StreamResult( result );

		transformer.transform( source, outputTarget );

		return result.toString();
	}

	private static void write( MakeTarget target, XWriteStack xml, IMakeTargetMorpheable selection ){
		// MakeTarget Name
		xml.push( TARGET_ELEMENT );

		xml.setAttribute(TARGET_ATTR_NAME, target.getName());
		xml.setAttribute(TARGET_ATTR_LOOP, Boolean.toString(target.getLoop()));
		xml.setAttribute(TARGET_ATTR_LTIME, Double.toString(target.getLoopTime()));

		// Default
		writeContent( target, xml, selection );
		
		xml.pop();
	}
	

	/**
	 * Writes the content of <code>target</code> into <code>xml</code>, also
	 * stores when to use custom or default properties.
	 * @param target some make target
	 * @param selection the make-target that is selected, may be <code>null</code>
	 * @param xml to write into
	 */
	private static void writeContent( MakeTargetSkeleton target, XWriteStack xml, IMakeTargetMorpheable selection ){
		// version
		xml.setAttribute( TARGET_ATTR_VERSION, "4" );
		
		// Default
		xml.push( TARGET_DEFAULT );
		xml.setText( Boolean.toString( target == selection ) );
		xml.pop();
		
		for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
			writeContent( target, key, xml );
		}
	}
	
	private static <T> void writeContent( MakeTargetSkeleton target, MakeTargetPropertyKey<T> key, XWriteStack xml ){
		IMakeTargetPropertyFactory<T> factory = key.getFactory();
		if( factory == null )
			return;
		
		xml.push( key.getName() );
		writeUsage( target, key, xml );
		T value = target.getLocalProperty( key );
		if( value != null ){
			factory.write( value, xml );
		}
		xml.pop();
	}
	
	/**
	 * Adds attributes {@link #TARGET_ATTR_USE_DEFAULT} and {@link #TARGET_ATTR_USE_LOCAL}
	 * to the current tag on <code>xml</code>.
	 * @param target the target that gets written
	 * @param key the key of the property to store
	 * @param xml to write into
	 */
	private static void writeUsage( MakeTargetSkeleton target, MakeTargetPropertyKey<?> key, XWriteStack xml ){
		if( target.getDefaults() != null ){
			if( key.isArray() ){
				xml.setAttribute( TARGET_ATTR_USE_LOCAL, Boolean.toString( target.isUseLocalProperty( key ) ) );
				xml.setAttribute( TARGET_ATTR_USE_DEFAULT, Boolean.toString( target.isUseDefaultProperty( key ) ) );
			}
			else{
				xml.setAttribute( TARGET_ATTR_USE_LOCAL, Boolean.toString( target.isUseLocalProperty( key ) ) );
			}
		}
	}
	
	public void read( Document document ){
		Debug.info( "Read make options for: " + project.getName() );
		
		XReadStack xml = new XReadStack( document );
		if( xml.search( BUILD_TARGET_ELEMENTS )){
			Default defaultTarget = new Default();
			
			if( xml.search( BUILD_TARGET_DEFAULT )){
				readContent( defaults, xml, xml.getInteger( TARGET_ATTR_VERSION, 2 ), defaultTarget );
				xml.pop();
			}
			
			if( xml.search( BUILD_TARGET_CUSTOM ) ){
				while( xml.go( TARGET_ELEMENT )){
					addStandardTarget( read( xml, defaultTarget ), false );
					xml.pop();
				}
				xml.pop();
			}
			else{
				while( xml.go( TARGET_ELEMENT )){
					addStandardTarget( read( xml, defaultTarget ), false );
					xml.pop();
				}
			}
			
			if( defaultTarget.target != null ){
				setSelectedTarget( defaultTarget.target, false );
			}
			
			if( xml.search( CLIENT_DATA )){
				while( xml.go( CLIENT_DATA_ENTRY )){
					String key = xml.getString( "key", null );
					if( key != null ){
						String value = xml.getText();
						if( value != null ){
							clientData.put( key, value );
						}
					}
					xml.pop();
				}
				
				xml.pop();
			}
			
			xml.pop();
		}
	}

	/**
	 * Reads some {@link MakeTarget}s from <code>content</code>. The new 
	 * targets are prepared for attaching to this project.
	 * @param content the contents to read
	 * @return the targets that were read, might be empty
	 */
	public Collection<MakeTarget> convert( String content ){
		try{
			if( content.equals( "" ))
				return new ArrayList<MakeTarget>();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse( new InputSource( new StringReader( content )));

			List<MakeTarget> result = new ArrayList<MakeTarget>();

			XReadStack xml = new XReadStack( document );
			if( xml.search( BUILD_TARGET_ELEMENTS )){
				while( xml.go( TARGET_ELEMENT )){
					result.add( read( xml, project, new Default() ) );
					xml.pop();
				}
			}

			return result;
		}
		catch( ParserConfigurationException ex ){
			TinyOSPlugin.log( ex );
			return new ArrayList<MakeTarget>();
		} 
		catch( SAXException e ){
			TinyOSPlugin.log( e );
			return new ArrayList<MakeTarget>();
		}
		catch( IOException e ){
			TinyOSPlugin.log( e );
			return new ArrayList<MakeTarget>();
		}
	}

	private MakeTarget read( XReadStack xml, Default defaultTarget ){
		return read( xml, project, defaultTarget );
	}
	
	private MakeTarget read( XReadStack xml, IProject project, Default defaultTarget ){
		// MakeTarget Name
		
		int version = xml.getInteger( TARGET_ATTR_VERSION, 1 );
		String name = xml.getString( TARGET_ATTR_NAME, "" );
		
		MakeTarget target = new MakeTarget( project, name, null );
		
		if( version == 1 ){
			target.setCustomNostdinc( xml.getBoolean( TARGET_ATTR_NOSTDINCT, false ));
		}
		
		target.setLoop( xml.getBoolean( TARGET_ATTR_LOOP, false ));
		target.setLoopTime( xml.getDouble( TARGET_ATTR_LTIME, 0.0 ));

		target.setDefaults( defaults );
		readContent( target, xml, version, defaultTarget );
		return target;
	}
	
	private void readContent( MakeTargetSkeleton target, XReadStack xml, int version, Default defaultTarget ){
		if( version <= 2 )
			readContent2( target, xml, version, defaultTarget );
		else if( version <= 3 )
			readContent3( target, xml, defaultTarget );
		else
			readContent4( target, xml, defaultTarget );
	}
	
	private void readContent4( MakeTargetSkeleton target, XReadStack xml, Default defaultTarget ){
		// Default
		if( xml.search( TARGET_DEFAULT )){
			if( Boolean.parseBoolean( xml.getText() ) ){
				defaultTarget.target = target;
			}
			xml.pop();
		}
		
		for( MakeTargetPropertyKey<?> key : MakeTargetPropertyKey.KEYS ){
			readContent4( target, key, xml );
		}
	}
	
	@SuppressWarnings("deprecation")
	private void readContent3( MakeTargetSkeleton target, XReadStack xml, Default defaultTarget ){
		readContent4( target, xml, defaultTarget );
		
		// replace COMPONENT with COMPONENT-FILE
		
		target.setUseLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, target.isUseLocalProperty( MakeTargetPropertyKey.COMPONENT ) );
		target.setUseDefaultProperty( MakeTargetPropertyKey.COMPONENT_FILE, target.isUseDefaultProperty( MakeTargetPropertyKey.COMPONENT ) );
		
		String component = target.getCustomComponent();
		if( component != null ){
			IPath path = new Path( "src/" + component + ".nc" );
			IFile file = project.getFile( path );
			target.putLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, file );
		}
	}
	
	private <T> void readContent4( MakeTargetSkeleton target, MakeTargetPropertyKey<T> key, XReadStack xml ){
		IMakeTargetPropertyFactory<T> factory = key.getFactory();
		if( factory == null || !factory.supportsXML() )
			return;
		
		if( xml.search( key.getName() )){
			readUsage( target, key, xml );
			T value = factory.read( xml );
			target.putLocalProperty( key, value );
			xml.pop();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void readContent2( MakeTargetSkeleton target, XReadStack xml, int version, Default defaultTarget ){
		if( version >= 2 ){
			if( xml.search( NOSTDINC )){
				target.setCustomNostdinc( Boolean.parseBoolean( xml.getText() ) );
				readUsage( target, MakeTargetPropertyKey.NO_STD_INCLUDE, xml );
				xml.pop();
			}
		}
		
		// Default
		if( xml.search( TARGET_DEFAULT )){
			if( Boolean.parseBoolean( xml.getText() ) ){
				defaultTarget.target = target;
			}
			xml.pop();
		}
		
		// Additional includes
		if( xml.search( INCLUDES_ELEMENTS )){
			target.setUsingPlatformIncludes( xml.getBoolean( INCLUDES_ATTR_DEFAULTS, false ));
			target.setUsingLastBuildIncludes( xml.getBoolean( INCLUDES_ATTR_BUILD, false ));
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.INCLUDES, xml );
			}

			List<MakeInclude> includes = new ArrayList<MakeInclude>();
			while( xml.go( INCLUDE_ELEMENT ) ){
				String type = xml.getString( INCLUDE_ATTR_TYPE, "source" );
				boolean ncc = false;
				MakeInclude.Include system = Include.NONE;
				boolean global = false;
				
				if( "source".equals( type )){
					ncc = true;
					system = Include.SOURCE;
				}
				else if( "system".equals( type )){
					system = Include.SYSTEM;
				}
				else if( "global".equals( type )){
					global = true;
				}
				
				includes.add( 
						new MakeInclude(
								xml.getText(), system,
								xml.getBoolean( INCLUDE_ATTR_RECURSIVE, false ),
								ncc, global ));
				xml.pop();
			}
			target.setCustomIncludes( includes.toArray( new MakeInclude[ includes.size() ] ));

			xml.pop();
		}

		// Excludes
		if( xml.search( EXCLUDE_ELEMENTS )){
			List<MakeExclude> excludes = new ArrayList<MakeExclude>();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.EXCLUDES, xml );
			}
			
			while( xml.go( EXCLUDE_ELEMENT )){
				excludes.add( new MakeExclude( xml.getText() ));
				xml.pop();
			}

			target.setCustomExcludes( excludes.toArray( new MakeExclude[ excludes.size()] ));
			xml.pop();
		}

		// Typedefs
		if( xml.search( TYPEDEF_ELEMENTS )){
			List<MakeTypedef> typedefs = new ArrayList<MakeTypedef>();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.TYPEDEFS, xml );
			}

			while( xml.go( TYPEDEF_ELEMENT )){
				typedefs.add(
						new MakeTypedef( 
								xml.getString( TYPEDEF_ATTR_TYPE, "type" ),
								xml.getString( TYPEDEF_ATTR_NAME, "name" ) ));
				xml.pop();
			}

			target.setCustomTypedefs( typedefs.toArray( new MakeTypedef[ typedefs.size() ] ));
			xml.pop();
		}

		// Macros
		if( xml.search( MACRO_ELEMENTS )){
			List<MakeMacro> macros = new ArrayList<MakeMacro>();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.MACROS, xml );
			}
			
			while( xml.go( MACRO_ELEMENT )){
				IMacro macro = new ConstantMacro(
								xml.getString( MACRO_ATTR_NAME, "name" ),
								xml.getString( MACRO_ATTR_CONTENT, "content" ));
				
				boolean yeti = xml.getBoolean( MACRO_ATTR_YETI, true );
				boolean ncc = xml.getBoolean( MACRO_ATTR_NCC, false );
				
				macros.add( new MakeMacro( macro, yeti, ncc ) );
				
				xml.pop();
			}

			target.setCustomMacros( macros.toArray( new MakeMacro[ macros.size() ] ));
			xml.pop();
		}

		// Target
		if( xml.search( TARGET_SELECTED )){
			String text = xml.getText();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.TARGET, xml );
			}
			if( text != null && text.length() > 0 ){
				target.setCustomTarget( text );
			}
			xml.pop();
		}

		// Application
		if( xml.search( APPLICATION_NAME )){
			String text = xml.getText();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.COMPONENT, xml );
			}
			if( text != null && text.length() > 0 ){
				target.setCustomComponent( text );
			}
			xml.pop();
		}
		
		// Board(s)
		if( xml.search( BOARDS_ELEMENTS )){
			List<String> boards = new ArrayList<String>();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.BOARDS, xml );
			}
			while( xml.go( TARGET_BOARD )){
				boards.add( xml.getText() );
				xml.pop();
			}
			target.setCustomBoards( boards.toArray( new String[ boards.size() ] ));
			xml.pop();
		}

		// Make Extra
		if( xml.search( EXTRA_ELEMENTS )){
			List<MakeExtra> extras = new ArrayList<MakeExtra>();
			if( version >= 2 ){
				readUsage( target, MakeTargetPropertyKey.MAKE_EXTRAS, xml );
			}
			
			while( xml.go( EXTRA_ELEMENT )){
				String name = xml.getString( EXTRA_NAME, "name" );
				MakeExtra extra = new MakeExtra( name );

				if( xml.search( EXTRA_PARAM )){
					extra.setAskParameterAtCompileTime( xml.getBoolean( EXTRA_ASK_BEFORE_COMPILE, false ) );
					extra.setParameterName( xml.getString( EXTRA_OPTION_NAME, "option" ) );
					extra.setParameterValue( xml.getText() );
					xml.pop();
				}

				extras.add( extra );
				xml.pop();
			}

			target.setCustomMakeExtras( extras.toArray( new MakeExtra[ extras.size() ] ));
			xml.pop();
		}
	}

	/**
	 * Reverse of {@link #writeUsage(MakeTargetSkeleton, MakeTargetPropertyKey, XWriteStack)},
	 * reads and sets the local and default usage flags.
	 * @param target the target to modify
	 * @param key property to read
	 * @param xml to read from
	 */
	private static void readUsage( MakeTargetSkeleton target, MakeTargetPropertyKey<?> key, XReadStack xml ){
		if( target.getDefaults() != null ){
			if( key.isArray() ){
				target.setUseLocalProperty( key, Boolean.parseBoolean( xml.getAttribute( TARGET_ATTR_USE_LOCAL ) ) );
				target.setUseDefaultProperty( key, Boolean.parseBoolean( xml.getAttribute( TARGET_ATTR_USE_DEFAULT ) ) );
			}
			else{
				boolean local = Boolean.parseBoolean( xml.getAttribute( TARGET_ATTR_USE_LOCAL ) );
				target.setUseLocalProperty( key, local );
				target.setUseDefaultProperty( key, !local );
			}
		}
	}
	
	private void saveDocument(Document doc) throws IOException {
		File projectDir = getProject().getLocation().toFile();

		FileOutputStream fos = new FileOutputStream(new File(projectDir,MAKE_TARGET_KEY));

		try {
			saveTargets(doc,fos);
			fos.flush();
			fos.close();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//	TinyOSPlugin.getDefault().getPreferenceStore().setValue(getProject().getName()+MAKE_TARGET_KEY,baos.toString());		
	}

	public IProject getProject() {
		return project;
	}

	private static class Default{
		public MakeTargetSkeleton target;
	}
}
