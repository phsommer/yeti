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
package tinyos.yeti.nesc12.ep.nodes;

import java.io.IOException;
import java.util.Map;

import tinyos.yeti.ep.figures.LabelContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.FieldUtility;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.utility.Icon;

/**
 * Represents a function, a field or an enumeration constant.
 * @author Benjamin Sigg
 */
public class FieldModelNode extends ModelNode implements Field, Inspectable{
    public static final IGenericFactory<FieldModelNode> FACTORY = new ReferenceFactory<FieldModelNode>( ModelNode.FACTORY ){
        public FieldModelNode create(){
            return new FieldModelNode();
        }

        @Override
        public void write( FieldModelNode value, IStorage storage ) throws IOException{
            super.write( value, storage );
            FieldUtility.write( value.delegate, storage );
        }

        @Override
        public FieldModelNode read( FieldModelNode value, IStorage storage ) throws IOException{
            super.read( value, storage );
            value.delegate = FieldUtility.read( storage );
            return value;
        }
    };

    private SimpleField delegate;

    protected FieldModelNode(){
        // nothing
    }

    public static FieldModelNode toNode( Field field ){
        if( field == null )
            return null;

        if( field.asNode() != null )
            return field.asNode();

        if( field.asSimple() != null )
            return new FieldModelNode( field.asSimple() );

        return null;
    }

    public FieldModelNode( SimpleField field ){
        super( fieldId( field.getName(), field.getType() ), true );
        delegate = field;
        init( field.getName(), field.getAttributes(), field.getModifiers(), field.getType(), field.getInitialValue(), false );
    }

    public FieldModelNode( Name identifier, ModelAttribute[] attributes, Modifiers modifiers, Type type, Value initialValue ){
        this( identifier, attributes, modifiers, type, initialValue, false );
    }

    public FieldModelNode( Name identifier, ModelAttribute[] attributes, Modifiers modifiers, Type type, Value initialValue, boolean enumeration ){
        super( fieldId( identifier, type ), true );
        init( identifier, attributes, modifiers, type, initialValue, enumeration );
    }

    public static final String fieldId( Name name, Type type ){
        if( name == null && type == null )
            throw new NullPointerException( "either name or type must not be null" );

        if( name == null )
            return "-" + type.id( false );

        if( type == null )
            return name.toIdentifier();

        return name.toIdentifier() + "-" + type.id( false );
    }

    private void init( Name identifier, ModelAttribute[] attributes, Modifiers modifiers, Type type, Value initialValue, boolean enumeration ){
        if( delegate == null )
            delegate = new SimpleField( modifiers, type, identifier, attributes, initialValue, null, getPath() );

        if( enumeration ){
            if( initialValue == null )
                setLabel( identifier.toIdentifier() );
            else
                setLabel( identifier.toIdentifier() + " : " + initialValue.toLabel() );
        }
        else{
            if( type != null && type.asFunctionType() != null ){
                setLabel( type.toLabel( identifier.toIdentifier(), Type.Label.SMALL ) );
            }
            else{
                if( type != null )
                    setLabel( identifier.toIdentifier() + " : " + type.toLabel( null, Type.Label.SMALL ));
                else
                    setLabel( identifier.toIdentifier() );
            }
        }

        setAttributes( attributes );
        setTags( getFieldTags( modifiers, type, enumeration ) );
        getTags().add( Tag.IDENTIFIABLE );
        setContent( new LabelContent( getLabel(), new Icon( getTags(), attributes ), getPath() ) );
    }

    /**
     * Gets the {@link Tag}s which describe a field with <code>type</code>.
     * @param modifiers the modifiers applied, can be <code>null</code>
     * @param type the type whose tags are required
     * @param enumeration whether the field is specified within an enumeration 
     * @return the tags
     */
    public static TagSet getFieldTags( Modifiers modifiers, Type type, boolean enumeration ){
        TagSet tags = new TagSet();
        tags.add( Tag.NO_BASE_EXPANSION );
        
        if( modifiers != null ){
            if( modifiers.isCommand() )
                tags.add( Tag.COMMAND );
            if( modifiers.isEvent() )
                tags.add( Tag.EVENT );
            if( modifiers.isAsync() )
                tags.add( Tag.ASYNC );
            if( modifiers.isTask() )
                tags.add( Tag.TASK );
        }

        if( enumeration ){
            tags.add( NesC12ASTModel.ENUMERATION_CONSTANT );
        }
        else{
            if( type != null && type.asFunctionType() != null ){
                tags.add( Tag.FUNCTION );
            }
            else{
                tags.add( NesC12ASTModel.FIELD );
            }
        }
        return tags;
    }
    
    @Override
    public void setPath( ASTModelPath path ){
        super.setPath( path );
        IASTFigureContent content = getContent();
        if( content instanceof LabelContent ){
            ((LabelContent)content).setPath( path );
        }
        delegate.setPath( path );
    }
    
    @Override
    public void resolveNameRanges(){
        delegate.resolveNameRanges();
    }

    public RangeDescription getRange(){
        return delegate.getRange();
    }

    public SimpleField asSimple() {
        return delegate == null ? null : delegate.asSimple();
    }

    public FieldModelNode asNode() {
        return this;
    }
    
    public IASTModelNodeConnection asConnection(){
	    return null;
    }
    
    public INesCNode inspect( BindingResolver resolver ){
    	return this;
    }
    
    public int getReferenceKindCount(){
    	return delegate.getReferenceKindCount();
    }
    
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
		return delegate.getReferences( key, inspector );
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	return delegate.getReferenceKind( index );
    }

    public Value getInitialValue() {
        return delegate.getInitialValue();
    }

    public Modifiers getModifiers() {
        return delegate.getModifiers();
    }

    public String getNodeName(){
    	Name name = getName();
    	if( name == null )
    		return null;
    	return name.toIdentifier();
    }
    
    public Name getName() {
        return delegate.getName();
    }
    
    public String getFieldName(){
	    return delegate.getFieldName();
    }
    
    public String getFunctionName(){
    	return delegate.getFunctionName();
    }
    
    public Type getType() {
        return delegate.getType();
    }
    
    public String getFieldType(){
	    return delegate.getFieldType();
    }
    
    public String getFunctionResultType(){
    	return delegate.getFunctionResultType();
    }

    public Field replace( Field[] indices, Map<GenericType, Type> generic ){
        return delegate.replace( indices, generic );
    }

    public void setIndices( Field[] fields ){
        delegate.setIndices( fields );
    }

    public Field[] getIndices(){
        return delegate.getIndices();
    }

    public void setInitialValue( Value initialValue ) {
        delegate.setInitialValue( initialValue );
    }

    public void setModifiers( Modifiers modifiers ) {
        delegate.setModifiers( modifiers );
    }

    public void setType( Type type ) {
        delegate.setType( type );
    }

    public void setArgumentNames( Name[] names ){
        delegate.setArgumentNames( names );
    }

    public Name[] getArgumentNames(){
        return delegate.getArgumentNames();
    }

    public String getDeclaration( String name ){
        return delegate.getDeclaration( name );
    }

    public String getBindingType() {
        return delegate.getBindingType();
    }

    public String getBindingValue() {
        return delegate.getBindingValue();
    }

    public String getNesCDescription(){
    	return delegate.getNesCDescription();
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        return delegate.getSegmentChild( segment, index );
    }

    public int getSegmentCount() {
        return delegate.getSegmentCount();
    }

    public String getSegmentName( int segment ) {
        return delegate.getSegmentName( segment );
    }

    public int getSegmentSize( int segment ) {
        return delegate.getSegmentSize( segment );
    }
    
    public boolean isField(){
	    return delegate.isField();
    }
    
    public boolean isFunction(){
    	return delegate.isFunction();
    }
}
