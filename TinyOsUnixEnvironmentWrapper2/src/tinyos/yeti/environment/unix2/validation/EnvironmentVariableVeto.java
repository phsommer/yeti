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
package tinyos.yeti.environment.unix2.validation;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import tinyos.yeti.environment.basic.helper.EnvCommand;
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.environment.unix2.TinyOSUnixEnvironmentPlugin2;
import tinyos.yeti.environment.unix2.preference.PreferenceInitializer;
import tinyos.yeti.launch.ILaunchVeto;

public class EnvironmentVariableVeto implements ILaunchVeto{
	private boolean doNotAskAgainSession = false;
		
	public void setAskAgain( boolean doNotAskAgainEver, boolean doNotAskAgainSession ){
		this.doNotAskAgainSession = doNotAskAgainSession;
		IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
		store.setValue( PreferenceInitializer.CHECK_ENV_EVER, !doNotAskAgainEver );
	}
	
	private boolean doNotAskAgainEver(){
		IPreferenceStore store = TinyOSUnixEnvironmentPlugin2.getDefault().getPreferenceStore();
		return !store.getBoolean( PreferenceInitializer.CHECK_ENV_EVER );
	}
	
	public boolean veto( IProgressMonitor monitor ){
		if( doNotAskAgainEver() || doNotAskAgainSession )
			return false;
		
		try{
			final Map<String,String> envp = System.getenv();
			final Map<String,String> shell = listBashEnvVariables();
			final boolean[] result = new boolean[]{ false };
			
			final String[] problems = listDifferences( envp, shell );
			if( problems.length == 0 )
				return false;
			
			Display display = PlatformUI.getWorkbench().getDisplay();
			display.syncExec( new Runnable(){
				public void run(){
					DifferenceListDialog dialog = new DifferenceListDialog();
					result[0] = !dialog.open( problems, envp, shell );
					setAskAgain( dialog.isDoNotAskAgainEver(), dialog.isDoNotAskAgainSession() );
				}
			});
			
			return result[0];
		}
		catch( IOException ex ){
			// ignore
			return false;
		}
	}

	public static Map<String,String> listBashEnvVariables() throws IOException{
		try{
			return Environment.getEnvironment().getCommandExecuter().execute( new EnvCommand() );
		}
		catch( InterruptedException ex ){
			return new HashMap<String, String>();
		}
	}
	
	public static String[] listDifferences( Map<String,String> a, Map<String,String> b ){
		List<String> result = new ArrayList<String>();
		
		Set<String> allKeys = new HashSet<String>();
		allKeys.addAll( a.keySet() );
		allKeys.addAll( b.keySet() );
		
		for( String key : allKeys ){
			String valueA = a.get( key );
			String valueB = b.get( key );
			
			if( valueA == null && valueB != null )
				result.add( key );
			else if( valueA != null && !valueA.equals( valueB ))
				result.add( key );
		}
		
		result.remove( "_" );
		result.remove( "SHLVL" );
		result.remove( "PWD" );
		result.remove( "OLDPWD" );
		
		String[] array = result.toArray( new String[ result.size() ] );
		Arrays.sort( array, Collator.getInstance() );
		return array;
	}
}
