package tinyOS.debug.simulation.events;

public class SimulationEvent 
{
	public static final int CREATE = 0;
	public static final int TERMINATE = 1;
	public static final int CHANGE = 2;
	public static final int DEBUG_EVENT = 3;
	public static final int RESUME = 4;

	
	private Object source;
	private int type;
	
	public SimulationEvent(Object source, int type)
	{
		this.source = source;
		this.type = type;
	}
	
	public Object getSource()
	{
		return source;
	}
	
	public int getType()
	{
		return type;
	}
}
