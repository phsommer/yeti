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
package tinyos.yeti.model;

import java.util.Collection;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.ProjectModel.DeclarationFilter;

public class ArrayDeclarationCollection implements IDeclarationCollection{
    private IDeclaration[] declarations;

    public ArrayDeclarationCollection( IDeclaration[] declarations ){
        if( declarations == null )
            declarations = new IDeclaration[]{};
        
        this.declarations = declarations;
    }

    public void fillDeclarations(
            Collection<? super IDeclaration> declarations, String name,
            Kind... kinds ){

        for( IDeclaration declaration : this.declarations ){
            if( declaration.getName().equals( name )){
                Kind declarationKind = declaration.getKind();
                for( Kind kind : kinds ){
                    if( kind == declarationKind ){
                        declarations.add( declaration );
                        break;
                    }
                }
            }
        }
    }

    public void fillDeclarations(
            Collection<? super IDeclaration> declarations, Kind... kinds ){


        for( IDeclaration declaration : this.declarations ){
            Kind declarationKind = declaration.getKind();
            for( Kind kind : kinds ){
                if( kind == declarationKind ){
                    declarations.add( declaration );
                    break;
                }
            }
        }
    }

    public void fillDeclarations(
            Collection<? super IDeclaration> declarations,
            DeclarationFilter filter ){

        for( IDeclaration declaration : this.declarations ){
            if( filter.include( declaration )){
                declarations.add( declaration );
            }
        }
    }

    public IDeclaration[] toArray(){
        return declarations;
    }
}
