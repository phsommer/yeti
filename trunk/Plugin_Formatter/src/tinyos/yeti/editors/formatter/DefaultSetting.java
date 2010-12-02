package tinyos.yeti.editors.formatter;

public class DefaultSetting implements IFormattingSettings{
	@Override
	public int getLineWrappingLength(){
		return 80;
	}
}
