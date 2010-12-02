package tinyos.yeti.make.targets.factories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.targets.IMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.IStringMakeTargetPropertyFactory;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class EnvironmentVariableFactory implements IMakeTargetPropertyFactory<EnvironmentVariable[]>, IStringMakeTargetPropertyFactory<EnvironmentVariable>{
	public boolean supportsXML(){
		return true;
	}
	
	public void write( EnvironmentVariable[] value, XWriteStack out ){
		for( EnvironmentVariable variable : value ){	
			out.push( "variable" );
			out.setAttribute( "key", variable.getKey() );
			out.setText( variable.getValue() );
			out.pop();
		}
	}
	
	public EnvironmentVariable[] read( XReadStack in ){
		List<EnvironmentVariable> list = new ArrayList<EnvironmentVariable>();
		while( in.go( "variable" )){
			String key = in.getAttribute( "key" );
			String value = in.getText();
			in.pop();
			list.add( new EnvironmentVariable( key, value ) );
		}
		return list.toArray( new EnvironmentVariable[ list.size() ] );
	}
	
	public void write( EnvironmentVariable[] value,
			MakeTargetPropertyKey<EnvironmentVariable[]> key,
			ILaunchConfigurationWorkingCopy configuration ){
		
		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		
		if( value != null ){
			for( EnvironmentVariable variable : value ){
				keys.add( variable.getKey() );
				values.add( variable.getValue() );
			}
		}
		
		configuration.setAttribute( "tinyos." + key.getName() + ".keys", keys );
		configuration.setAttribute( "tinyos." + key.getName() + ".values", values );
	}
	
	@SuppressWarnings( "unchecked" )
	public EnvironmentVariable[] read(
			MakeTargetPropertyKey<EnvironmentVariable[]> key,
			ILaunchConfiguration configuration ){
		try{
			List<String> keys = configuration.getAttribute( "tinyos." + key.getName() + ".keys", Collections.EMPTY_LIST );
			List<String> values = configuration.getAttribute( "tinyos." + key.getName() + ".values", Collections.EMPTY_LIST );
		
			EnvironmentVariable[] result = new EnvironmentVariable[ Math.min( keys.size(), values.size() )];
			for( int i = 0; i < result.length; i++ ){
				result[i] = new EnvironmentVariable( values.get( i ), keys.get( i ));
			}
			return result;
		}
		catch( CoreException ex ){
			TinyOSPlugin.log( ex.getStatus() );
			return new EnvironmentVariable[]{};
		}
	}
	
	public String write( EnvironmentVariable value ){
		StringBuilder builder = new StringBuilder();
		String key = value.getKey();
		String content = value.getValue();
		
		builder.append( key.length() );
		builder.append( "." );
		builder.append( key );
		
		builder.append( content.length() );
		builder.append( "." );
		builder.append( content );
		
		return builder.toString();
	}
	
	public EnvironmentVariable read( String content ){
		int offset = content.indexOf( '.' );
		int length = Integer.parseInt( content.substring( 0, offset ) );
		offset++;
		String key = content.substring( offset, offset+length );
		offset += length;
		int end = content.indexOf( '.', offset );
		length = Integer.parseInt( content.substring( offset, end ) );
		offset = end+1;
		String value = content.substring( offset, offset+length );
		return new EnvironmentVariable( key, value );
	}
	
	public EnvironmentVariable[] array( int size ){
		return new EnvironmentVariable[ size ];
	}
}
