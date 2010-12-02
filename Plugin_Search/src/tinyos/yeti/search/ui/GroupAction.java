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
package tinyos.yeti.search.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import tinyos.yeti.search.model.group.GroupGenerator;

/**
 * This action changes the {@link SearchDeclarationResultPage#setGroup(GroupGenerator) group property}
 * of a {@link SearchDeclarationResultPage}.
 * @author Benjamin Sigg
 */
public class GroupAction<N> extends Action{
	private String groupId;
	private SearchResultPage<N> page;
	private GroupGenerator<?, N> generator;
	
	public GroupAction( String groupId, String text, ImageDescriptor image, SearchResultPage<N> page, GroupGenerator<?, N> generator ){
		super( text, AS_RADIO_BUTTON );
		this.groupId = groupId;
		setImageDescriptor( image );
		this.generator = generator;
		this.page = page;
	}
	
	public GroupGenerator<?, N> getGenerator(){
		return generator;
	}
	
	@Override
	public void run(){
		if( isChecked() ){
			page.setGroup( generator, false );
		}
		else{
			page.setGroup( null, false );
		}
	}
	
	public boolean setState( GroupGenerator<?, ?> selection ){
		boolean selected = selection == generator;
		setChecked( selected );
		return selected;
	}
	
	public String getGroupId(){
		return groupId;
	}
}
