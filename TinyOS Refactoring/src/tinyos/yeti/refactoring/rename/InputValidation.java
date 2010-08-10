package tinyos.yeti.refactoring.rename;

/**
 * This class can be used to validate the user input for a new c name.
 * @author Max Urech
 *
 */
public class InputValidation {
	
	/**
	 * This pattern matches iff it is given a valid c name.
	 */
	private final static String cNamePattern="[a-zA-Z_][a-zA-Z0-9_]*";
	
	/**
	 * This enum can be used to identify a error type in a cName.
	 *
	 */
	public enum CNameDifference{
		VALID(cNamePattern,""),
		EMPTY("","An empty string is no C name."),
		FIRST_CHAR("[0-9].*","A C name must not start with a digit."),
		ILLEGAL_CHARACTERS(".*[^a-zA-Z0-9_]+.*","A C name must contain only letters, digits and underscores.");
		
		private String pattern;
		private String userMessage;
		
		private CNameDifference(String pattern, String userMessage) {
			this.pattern = pattern;
			this.userMessage = userMessage;
		}
		
		/**
		 * Returns a regex pattern which can be used to identify this kind of difference to a valid CName.
		 * @return
		 */
		public String getPattern(){
			return pattern;
		}
		
		/**
		 * Returns a message which is inteded to be shown to the user, for informing him about what was wrong with the given cName.
		 * @return
		 */
		public String getUserMessage(){
			return userMessage;
		}

		public static String getLongestUserMessage(){
			CNameDifference longest=null;
			int max=-1;
			for(CNameDifference difference:values()){
				int length=difference.getUserMessage().length();
				if(length>max){
					max=length;
					longest=difference;
				}
			}
			return longest.getUserMessage();
		}
		
	}
	
	/**
	 * Checks if the given name is a valid CName.
	 * @param name
	 * @return
	 */
	public boolean isCName(String name){
		return name.matches(cNamePattern);
	}
	
	/**
	 * Tries to determine why the given name is no c name.
	 * @param name
	 * @return null if couldn't determine reason.
	 */
	public CNameDifference decideCNameDifference(String name){
		for(CNameDifference difference:CNameDifference.values()){
			if(name.matches(difference.getPattern())){
				return difference;
			}
		}
		return null;
	}
}
