package tinyos.yeti.model.standard;

import tinyos.yeti.model.ProjectModel;

public class LinkedProjectCache extends StandardProjectCache{
	@Override
	protected IStreamProvider createStreamProvider( ProjectModel model ){
		return new LinkedStreamProvider( model );
	}
	
	@Override
	public String getTypeIdentifier(){
		return "tinyos.yeti.model.LinkedProjectCache";
	}
}
