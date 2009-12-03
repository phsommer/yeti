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
package tinyos.yeti.ep.parser;

import tinyos.yeti.views.NodeContentProvider;

/**
 * A tag describes a single role of an {@link IASTModelNode}. A tag can be linked to
 * an {@link IASTModelNode} itself, or to a {@link IASTModelNodeConnection}.<br>
 * A tag is either a key-tag or not. A key-tag is a tag that sticks to
 * the {@link IASTModelNode} itself, non key tags belong to the {@link IASTModelNodeConnection}
 * or are optional refinements of other keys.
 * @author Benjamin Sigg
 * @see TagSet
 */
public class Tag implements Comparable<Tag>{
	/** this tag marks nodes which describe an element with an identifier, e.g. an interface has an identifier while a file has not */
	public static final Tag IDENTIFIABLE = new Tag( "identifiable", false, null );
	
	/** tag indicating that an {@link IASTModelNode} represents an interface */
	public static final Tag INTERFACE = new Tag( "interface", true, new TagDescription( "Interface", null, true ) );

	/** tag indicating that an {@link IASTModelNode} represents a module */
	public static final Tag MODULE = new Tag( "module", false, new TagDescription( "Module", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents a configuration */
	public static final Tag CONFIGURATION = new Tag( "configuration", false, new TagDescription( "Configuration", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents a binary component */
	public static final Tag BINARY_COMPONENT = new Tag( "binary component", false, new TagDescription( "Binary Component", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents an interface, module, configuration or binary component */
	public static final Tag COMPONENT = new Tag( "component", true, new TagDescription( "Component", "A module, configuration or binary component", true ) );

	/** tag indicating that some object uses elements */
	public static final Tag USES = new Tag( "uses", false );

	/** tag indicating that some object provides elements */
	public static final Tag PROVIDES = new Tag( "provides", false );

	/** tag indicating that some object got renamed */
	public static final Tag RENAMED = new Tag( "renamed", false );

	/** tag indicating that an {@link IASTModelNode} represents a struct */
	public static final Tag STRUCT = new Tag( "struct", false, new TagDescription( "Struct", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents an union */
	public static final Tag UNION = new Tag( "union", false, new TagDescription( "Union", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents a struct or union */
	public static final Tag DATA_OBJECT = new Tag( "data object", true );

	/** tag indicating that an {@link IASTModelNode} represents an attribute */
	public static final Tag ATTRIBUTE = new Tag( "attribute", true, new TagDescription( "Attribute", "NesC specific attribute like '@safe'", false ) );

	/** tag indicating that an {@link IASTModelNode} represents a macro */
	public static final Tag MACRO = new Tag( "macro", true, new TagDescription( "Macro", "Preprocessor macro", true ));
	
	

	/** tag indicating that an {@link IASTModelNode} represents an function */
	public static final Tag FUNCTION = new Tag( "function", true, new TagDescription( "Function", "Also task, event or command", true ) );

	/** tag indicating that a function is async */
	public static final Tag ASYNC = new Tag( "async", false, new TagDescription( "Async", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents an event */
	public static final Tag EVENT = new Tag( "event", false, new TagDescription( "Event", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents a command */
	public static final Tag COMMAND = new Tag( "command", false, new TagDescription( "Command", null, false ) );

	/** tag indicating that an {@link IASTModelNode} represents a task */
	public static final Tag TASK = new Tag( "task", false, new TagDescription( "Task", null, false ) );

	/** marks a connection */
	public static final Tag CONNECTION = new Tag( "connection", true );

	/** marks a connection of the form "left &lt;- right" */
	public static final Tag CONNECTION_LEFT = new Tag( "connection left", false );

	/** marks a connection of the form "left -&gt; right"  */
	public static final Tag CONNECTION_RIGHT = new Tag( "connection right", false );

	/** marks a connection  of the form "left = right" */
	public static final Tag CONNECTION_BOTH = new Tag( "connection both", false );


	/** marks nodes that could be shown as roots in the outline view */
	public static final Tag OUTLINE = new Tag( "outline", false );

	/**
	 * marks nodes that can be shown as roots in the graph view. These nodes
	 * may provide a {@link IASTFigureContent} through {@link IASTModelNode#getContent()},
	 * but they are not forced to do that. 
	 */
	public static final Tag FIGURE = new Tag( "figure", false );

	/** tells that the icon for a connection should be searched by resolving the connection rather then the {@link IASTModelNode} to which the connection points */
	public static final Tag AST_CONNECTION_ICON_RESOLVE = new Tag( "connection.icon.resolve", false );

	/** tells that the icon for a connection in the graph view should be searched by resolving the connection rather then the {@link IASTModelNode} to which the connection points */
	public static final Tag AST_CONNECTION_GRAPH_ICON_RESOLVE = new Tag( "connection.graph.icon.resolve", false );

	/** tells that the label for a connection should be searched by resolving the connection rather then the {@link IASTModelNode} to which the connection points */
	public static final Tag AST_CONNECTION_LABEL_RESOLVE = new Tag( "connection.label.resolve", false );

	/** tells that the label for a connection in the graph view should be searched by resolving the connection rather then the {@link IASTModelNode} to which the connection points */
	public static final Tag AST_CONNECTION_GRAPH_LABEL_RESOLVE = new Tag( "connection.graph.label.resolve", false );

	/** marks elements that were included into a file */
	public static final Tag INCLUDED = new Tag( "included", false );

	/** nodes which have this tag set are not expanded by the {@link NodeContentProvider#expandBaseTree()} */
	public static final Tag NO_BASE_EXPANSION = new Tag( "no_base_expansion", false );

	private String id;
	private boolean key;
	private TagDescription description;

	public Tag( String uniqueId, boolean key ){
		this( uniqueId, key, null );
	}
	
	/**
	 * Creates a new tag.
	 * @param uniqueId the unique id used for this tag
	 * @param key whether this tag is a key tag. A key tag is a tag that 
	 * can be part of a larger key of a whole group of {@link IASTModelNode}s. Within
	 * that group, each {@link IASTModelNode} would have a unique name.
	 * @param description optional description of this tag 
	 */
	public Tag( String uniqueId, boolean key, TagDescription description ){
		if( uniqueId == null )
			throw new IllegalArgumentException( "unique id must not be null" );

		this.id = uniqueId;
		this.key = key;
		this.description = description;
	}

	@Override
	public String toString(){
		return getId();
	}

	public boolean isKey(){
		return key;
	}

	public String getId(){
		return id;
	}

	public TagDescription getDescription(){
		return description;
	}
	
	public int compareTo( Tag o ){
		return id.compareTo( o.id );
	}

	@Override
	public boolean equals( Object obj ){
		return obj != null && obj instanceof Tag && id.equals( ((Tag)obj).id ) && (((Tag)obj).key == key);
	}

	@Override
	public int hashCode(){
		return id.hashCode();
	}
}
