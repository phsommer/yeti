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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.ep.nodes.ComponentModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.validators.ComponentAccessListValidator;

public class Access extends AbstractFixedASTNode{
    public static final Key<ComponentModelNode> COMPONENT = new Key<ComponentModelNode>( "access component" );
    
    public static final Flag ACCESS = new Flag( "in access clause" );
    public static final Flag ACCESS_USES = new Flag( "in uses clause" );
    public static final Flag ACCESS_PROVIDES = new Flag( "in provides clause" );
    
    public static enum Direction{ USES, PROVIDES };
    
    private Direction direction;
    
    public static final String INTERFACES = "interfaces";
    
    public Access(){
        super( "Access", INTERFACES );
    }
    
    public Access( Direction direction, ASTNode interfaces ){
        this();
        setDirection( direction );
        setField( INTERFACES, interfaces );
    }
    
    public Access( Direction direction, ParameterizedInterfaceList interfaces ){
        this();
        setDirection( direction );
        setInterfaces( interfaces );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.put( ACCESS );
        
        if( getDirection() == Direction.USES ){
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, new ComponentAccessListValidator() );
            stack.put( ACCESS_USES );
        }
        else if( getDirection() == Direction.PROVIDES ){
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, new ComponentAccessListValidator() );
            stack.put( ACCESS_PROVIDES );
        }
        
        super.resolve( stack );
        
        if( stack.isReportErrors() ){
            ParameterizedInterfaceList list = getInterfaces();
            if( list != null ){
                if( list.getChildrenCount() == 0 ){
                    stack.error( "Empty list of interfaces/functions", this );
                }
            }
        }
        
        stack.remove( ModifierValidator.MODIFIER_VALIDATOR );
        stack.remove( ACCESS );
        stack.remove( ACCESS_USES );
        stack.remove( ACCESS_PROVIDES );
    }
    
    public void setDirection( Direction direction ) {
        this.direction = direction;
    }
    public Direction getDirection() {
        return direction;
    }
    
    public void setInterfaces( ParameterizedInterfaceList interfaces ){
        setField( 0, interfaces );
    }
    public ParameterizedInterfaceList getInterfaces(){
        return (ParameterizedInterfaceList)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof ParameterizedInterfaceList ) )
            throw new ASTException( node, "Must be a ParameterizedInterfaceList" );
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
