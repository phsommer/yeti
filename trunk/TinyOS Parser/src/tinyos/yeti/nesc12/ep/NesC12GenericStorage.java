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
package tinyos.yeti.nesc12.ep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.storage.GenericStorage;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.nesc12.CancellationException;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.declarations.EnumConstantDeclaration;
import tinyos.yeti.nesc12.ep.declarations.FieldDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.ep.nodes.BinaryComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ConfigurationModelNode;
import tinyos.yeti.nesc12.ep.nodes.ConnectionModelNode;
import tinyos.yeti.nesc12.ep.nodes.DataObjectTypeModelConnection;
import tinyos.yeti.nesc12.ep.nodes.DataObjectTypeModelNode;
import tinyos.yeti.nesc12.ep.nodes.EndpointModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.GenericComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.GenericTypeModelConnection;
import tinyos.yeti.nesc12.ep.nodes.GenericTypeModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.ep.nodes.ModuleModelNode;
import tinyos.yeti.nesc12.ep.nodes.NesC12TagSetFactory;
import tinyos.yeti.nesc12.ep.nodes.TypedefModelConnection;
import tinyos.yeti.nesc12.ep.nodes.TypedefModelNode;
import tinyos.yeti.nesc12.ep.nodes.UnitModelNode;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.NesC12FileInfo;
import tinyos.yeti.nesc12.parser.ast.elements.CombinedName;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.ConstType;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.EnumType;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;
import tinyos.yeti.nesc12.parser.ast.elements.values.ArrayValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.DataObject;
import tinyos.yeti.nesc12.parser.ast.elements.values.FloatingValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.IntegerValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.UnknownValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.ValueFactory;
import tinyos.yeti.preprocessor.RangeDescription;

public class NesC12GenericStorage extends GenericStorage {
    public NesC12GenericStorage( ProjectTOS project, DataInputStream in, IProgressMonitor monitor ) throws IOException{
        super( project, in, monitor );
    }

    public NesC12GenericStorage( ProjectTOS project, DataOutputStream out, IProgressMonitor monitor ) throws IOException{
        super( project, out, monitor );
    }
    

    @Override
    protected void createFactories(){
        super.createFactories();

        put( "tinyOS.nesc12.ep.ModelNode", ModelNode.class, ModelNode.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.ComponentModelNode", ComponentModelNode.class, ComponentModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.BinaryComponentModelNode", BinaryComponentModelNode.class, BinaryComponentModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.GenericComponentModelNode", GenericComponentModelNode.class, GenericComponentModelNode.FACTORY );
                    put( "tinyOS.nesc12.ep.nodes.ConfigurationModelNode", ConfigurationModelNode.class, ConfigurationModelNode.FACTORY );
                    put( "tinyOS.nesc12.ep.nodes.ModuleModelNode", ModuleModelNode.class, ModuleModelNode.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.FieldModelNode", FieldModelNode.class, FieldModelNode.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.InterfaceModelNode", InterfaceModelNode.class, InterfaceModelNode.FACTORY );
            put( "tinyOS.nesc12.ep.StandardModelNode", StandardModelNode.class, StandardModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.ConnectionModelNode", ConnectionModelNode.class, ConnectionModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.DataObjectTypeModelNode", DataObjectTypeModelNode.class, DataObjectTypeModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.GenericTypeModelNode", GenericTypeModelNode.class, GenericTypeModelNode.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.UnitModelNode", UnitModelNode.class, UnitModelNode.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.TypedefModelNode", TypedefModelNode.class, TypedefModelNode.FACTORY );
            
        put( "tinyOS.nesc12.ep.ModelConnection", ModelConnection.class, ModelConnection.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.ComponentReferenceModelConnection", ComponentReferenceModelConnection.class, ComponentReferenceModelConnection.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.EndpointModelConnection", EndpointModelConnection.class, EndpointModelConnection.FACTORY );
            put( "tinyOS.nesc12.ep.nodes.InterfaceReferenceModelConnection", InterfaceReferenceModelConnection.class, InterfaceReferenceModelConnection.FACTORY );
            put( "tinyOS.nesc12.ep.StandardModelConnection", StandardModelConnection.class, StandardModelConnection.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.DataObjectTypeModelConnection", DataObjectTypeModelConnection.class, DataObjectTypeModelConnection.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.FieldModelConnection", FieldModelConnection.class, FieldModelConnection.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.GenericTypeModelConnection", GenericTypeModelConnection.class, GenericTypeModelConnection.FACTORY );
                put( "tinyOS.nesc12.ep.nodes.TypedefModelConnection", TypedefModelConnection.class, TypedefModelConnection.FACTORY );
                
        
        put( "tinyos.yeti.nesc12.ep.nodes.ModelAttribute", ModelAttribute.class, ModelAttribute.FACTORY );
        put( "tinyos.yeti.nesc12.ep.nodes.ModelAttribute[]", ModelAttribute[].class, ModelAttribute.ARRAY_FACTORY );
        
        put( "tinyOS.nesc12.parser.FileRegion", FileRegion.class, FileRegion.FACTORY );
        
        put( "tinyOS.nesc12.parser.NesC12FileInfo", NesC12FileInfo.class, NesC12FileInfo.FACTORY );
        
        put( "tinyOS.nesc12.parser.ast.elements.LazyRangeDescription", LazyRangeDescription.class, LazyRangeDescription.FACTORY );
        put( "tinyOS.preprocessor.RangeDescription", RangeDescription.class, LazyRangeDescription.RANGE_DESCRIPTION_FACTORY );
        
        put( "tinyOS.nesc12.parser.ast.elements.Name", Name.class, Name.FACTORY );
            put( "tinyOS.nesc12.parser.ast.elements.SimpleName", SimpleName.class, SimpleName.FACTORY );
            put( "tinyOS.nesc12.parser.ast.elements.CombinedName", CombinedName.class, CombinedName.FACTORY );
            put( "tinyOS.nesc12.parser.ast.elements.Name[]", Name[].class, Name.ARRAY_FACTORY );
            
        put( "tinyOS.nesc12.parser.ast.elements.Field[]", Field[].class, Field.ARRAY_FACTORY );
        put( "tinyOS.nesc12.parser.ast.elements.SimpleField", SimpleField.class, SimpleField.FACTORY );
        
        put( "tinyOS.nesc12.parser.ast.elements.Modifiers", Modifiers.class, Modifiers.FACTORY );
        
        put( "tinyOS.nesc12.parser.ast.elements.types.ArrayType", ArrayType.class, TypeFactory.<ArrayType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.ConstType", ConstType.class, TypeFactory.<ConstType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.DataObjectType", DataObjectType.class, TypeFactory.<DataObjectType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.EnumType", EnumType.class, TypeFactory.<EnumType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.FunctionType", FunctionType.class, TypeFactory.<FunctionType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.GenericType", GenericType.class, TypeFactory.<GenericType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.PointerType", PointerType.class, TypeFactory.<PointerType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.BaseType", BaseType.class, TypeFactory.<BaseType>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.types.TypedefType", TypedefType.class, TypeFactory.<TypedefType>factory() );
        
        put( "tinyOS.nesc12.parser.ast.elements.values.ArrayValue", ArrayValue.class, ValueFactory.<ArrayValue>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.values.DataObject", DataObject.class, ValueFactory.<DataObject>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.values.IntegerValue", IntegerValue.class, ValueFactory.<IntegerValue>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.values.FloatingValue", FloatingValue.class, ValueFactory.<FloatingValue>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.values.StringValue", StringValue.class, ValueFactory.<StringValue>factory() );
        put( "tinyOS.nesc12.parser.ast.elements.values.UnknownValue", UnknownValue.class, ValueFactory.<UnknownValue>factory() );
        
        put( "tinyOS.nesc12.ep.declarations.BaseDeclaration", BaseDeclaration.class, BaseDeclaration.FACTORY );
            put( "tinyOS.nesc12.ep.declarations.EnumConstantDeclaration", EnumConstantDeclaration.class, EnumConstantDeclaration.FACTORY );
            put( "tinyOS.nesc12.ep.declarations.TypedDeclaration", TypedDeclaration.class, TypedDeclaration.FACTORY );
                put( "tinyOS.nesc12.ep.declarations.FieldDeclaration", FieldDeclaration.class, FieldDeclaration.FACTORY );
    }

    @Override
    protected IGenericFactory<TagSet> createTagsetFactory(){
        return new NesC12TagSetFactory();
    }
    
    public void checkCancel() throws CancellationException{
        IProgressMonitor monitor = getMonitor();
        if( monitor != null && monitor.isCanceled() )
            throw new CancellationException();
    }

    @Override
    public <V> void write( V value ) throws IOException{
        checkCancel();
        super.write( value );
    }
    
    @Override
    public <V> V read() throws IOException{
        checkCancel();
        return super.read();
    }
}
