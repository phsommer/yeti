package tinyos.yeti.environment.winXP.preference;

import tinyos.yeti.environment.basic.preferences.AbstractPlatformEnvironmentVariablesPreferencePage;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.platform.Platform;
import tinyos.yeti.environment.winXP.platform.PlatformManager;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.EnvironmentVariable;

public class PlatformEnvironmentVariablesPreferencePage extends
		AbstractPlatformEnvironmentVariablesPreferencePage{

	@Override
	protected EnvironmentVariable[] getDefaults( IPlatform platform ){
		return new EnvironmentVariable[]{};
	}

	@Override
	protected IEnvironment getEnvironment(){
		return Environment.getEnvironment();
	}

	@Override
	protected IPlatform[] getPlatforms(){
		return getEnvironment().getPlatforms();
	}

	@Override
	protected EnvironmentVariable[] getVariables( IPlatform platform ){
		if( platform == null ){
			return getManager().getDefaultEnvironmentVariables();
		}
		else{
			return ((Platform)platform).getEnvironmentVariables();
		}
	}

	@Override
	protected void setVariables( IPlatform platform, EnvironmentVariable[] variables ){
		if( platform == null ){
			getManager().setDefaultVariables( variables );
		}
		else{
			((Platform)platform).setEnvironmentVariables( variables );
		}
	}


    private PlatformManager getManager(){
    	return Environment.getEnvironment().getPlatformManager();
    }
}
