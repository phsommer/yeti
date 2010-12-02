package tinyos.yeti.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import tinyos.yeti.TinyOSPlugin;

/**
 * Contains all the important informations about an {@link IProjectCache}.
 * @author Benjamin Sigg
 */
public class ProjectCacheFactory{
	private IConfigurationElement element;
	
	public ProjectCacheFactory( IConfigurationElement element ){
		this.element = element;
	}
	
	public IProjectCache create(){
		try{
			return (IProjectCache)element.createExecutableExtension( "class" );
		}
		catch( CoreException e ){
			TinyOSPlugin.log( e );
			throw new IllegalStateException( e );
		}
	}
	
	public String getId(){
		return element.getAttribute( "id" );
	}
	
	public String getName(){
		return element.getAttribute( "name" );
	}
}
