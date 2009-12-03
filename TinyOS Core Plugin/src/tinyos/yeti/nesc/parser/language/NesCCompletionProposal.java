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
package tinyos.yeti.nesc.parser.language;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import tinyos.yeti.ep.parser.INesCCompletionProposal;

public class NesCCompletionProposal implements INesCCompletionProposal{
    private ICompletionProposal base;
    
    public NesCCompletionProposal( ICompletionProposal base ){
        this.base = base;
    }
    
    public void apply( IDocument document ){
        base.apply( document );    
    }

    public String getAdditionalProposalInfo(){
        return base.getAdditionalProposalInfo();
    }

    public IContextInformation getContextInformation(){
        return base.getContextInformation();
    }

    public String getDisplayString(){
        return base.getDisplayString();
    }

    public Image getImage(){
        return base.getImage();
    }

    public Point getSelection( IDocument document ){
        return base.getSelection( document );
    }
    
    public boolean inFile(){
        return true;
    }

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals( Object obj ){
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		NesCCompletionProposal other = (NesCCompletionProposal)obj;
		if( base == null ){
			if( other.base != null )
				return false;
		}
		else if( !base.equals( other.base ) )
			return false;
		return true;
	}
}
