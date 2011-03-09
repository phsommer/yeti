package tinyOS.debug.simulation.ui.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

import tinyOS.debug.simulation.TinyOSDebugSimulationPlugin;
import tinyos.yeti.ep.IPlatform;

public class NesCStackFrameParser 
{
	private String m_separator = IPlatform.DEFAULT_NESC_SEPARATOR;
	
	private String componentName, interfaceName, eventName, sourceName = null;
	private IStackFrame frame;
	
	public NesCStackFrameParser(IStackFrame f)
	{
		frame = f;
		if(frame != null && isNesCStackFrame())
		{
			String name = null;
			try {
				name = frame.getName();
			} catch (DebugException e) {
				TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to get name of stack frame", e.getMessage());
				return;
			}
			
			Pattern p1 = Pattern.compile(getEscapedSeparator());
			String[] segments = p1.split(name);
			componentName = segments[0];
			interfaceName = segments[1];
			
			Pattern p2 = Pattern.compile(".*\\(\\)");
			Matcher m1 = p2.matcher(segments[segments.length-1]);
			m1.find();
			eventName = segments[segments.length-1].substring(m1.start(), m1.end());
			
			Pattern p3 = Pattern.compile("/");
			String[] pathSegments = p3.split(segments[segments.length-1]);
			sourceName = pathSegments[pathSegments.length-1];
		}
	}

	public String getEventName()
	{
		return eventName + " " + sourceName;
	}

	public String getInterfaceName()
	{
		return interfaceName;
	}
	
	public String getComponentName()
	{
		return componentName;	
	}
	
	public boolean isNesCStackFrame()
	{
		if(frame == null)
			return false;
		
		String separator = getEscapedSeparator();
		Pattern p = Pattern.compile(".+"+separator+".+"+separator+".+\\(\\).*");
		try {
			return p.matcher(frame.getName()).matches();
		} catch (DebugException e) {
			TinyOSDebugSimulationPlugin.getDefault().dialog(IStatus.ERROR, "Unable to get name of stack frame", e.getMessage());
			return false;
		}
	}
	
	private String getEscapedSeparator()
	{
		String separator = m_separator;
		if(separator.equals("(")
			|| separator.equals("[")
			|| separator.equals("{")
			|| separator.equals("\\")
			|| separator.equals("^")
			|| separator.equals("$")
			|| separator.equals("|")
			|| separator.equals(")")
			|| separator.equals(".")
			|| separator.equals("?")
			|| separator.equals("*")
			|| separator.equals("+")
			|| separator.equals("\""))
		{
			separator = "\\"+separator;
		}
		return separator;
	}
}
