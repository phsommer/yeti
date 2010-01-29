package tinyos.yeti.editors.formatter;

/**
 * Settings that are to be used to customize formatting.
 * @author Benjamin Sigg
 */
public interface IFormattingSettings{
	/**
	 * Gets the number of characters that are to be placed on one line. A value
	 * smaller than 0 indicates that this feature is disabled.
	 * @return the number of characters per line or a value below 0
	 */
	public int getLineWrappingLength();
}
