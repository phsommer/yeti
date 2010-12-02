package tinyos.yeti.editors.formatter;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.source.ISourceViewer;

import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.NesCSourceViewerConfiguration;
import tinyos.yeti.editors.format.INesCFormattingStrategyFactory;

/**
 * Factory creating a {@link NesCIndenterStrategy} or a {@link NesCFormattingStrategy}.
 * 
 * @author Benjamin Sigg
 * 
 */
public class NesCFormatterStrategyFactory implements
		INesCFormattingStrategyFactory {

	public IContentFormatter createFormatter(ISourceViewer viewer,
			NesCEditor editor) {
		NesCSourceViewerConfiguration configuration = editor.getConfiguration();

		MultiPassContentFormatter formatter = new MultiPassContentFormatter(
				configuration.getConfiguredDocumentPartitioning(viewer),
				IDocument.DEFAULT_CONTENT_TYPE);

		formatter.setMasterStrategy(new NesCFormatterStrategy());

		return formatter;
	}

	public boolean isFormatter() {
		return true;
	}

	public IContentFormatter createIndenter(ISourceViewer viewer,
			NesCEditor editor) {
		NesCSourceViewerConfiguration configuration = editor.getConfiguration();

		MultiPassContentFormatter formatter = new MultiPassContentFormatter(
				configuration.getConfiguredDocumentPartitioning(viewer),
				IDocument.DEFAULT_CONTENT_TYPE);

		formatter.setMasterStrategy(new NesCIndenterStrategy());

		return formatter;
	}

	public boolean isIndenter() {
		return true;
	}

	public String getName() {
		return "Jianyuan Li's formatting rules";
	}
	
	@Override
	public String getId(){
		return "tinyos.yeti.formatter";
	}
}
