package tinyos.yeti.refactoring.utilities;

/**
 * This class is just used for debugging purposes
 * @author Max Urech
 *
 */
public class DebugUtil {
	public static String endOutput="";
	public static void addOutput(String output){
		if(output.equals("")){
			output="output is EMPTY";
		}
		endOutput+="\n"+output;
	}
	public static void printOutput(){
		System.err.println(endOutput);
	}
	
	public static void clearOutput(){
		endOutput="";
	}
	
	public static void immediatePrint(String output){
		System.err.println(output);
	}
}
