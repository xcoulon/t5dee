/**
 * 
 */
package org.bytesparadise.t5dee.editor;

import org.bytesparadise.t5dee.editor.contentassist.ComponentContentAssistProcessor;
import org.bytesparadise.t5dee.editor.contentassist.ComponentParameterContentAssistProcessor;
import org.bytesparadise.t5dee.utils.Logger;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.html.core.text.IHTMLPartitions;
import org.eclipse.wst.html.ui.StructuredTextViewerConfigurationHTML;
import org.eclipse.wst.sse.core.text.IStructuredPartitions;
import org.eclipse.wst.xml.core.text.IXMLPartitions;

/**
 * @author Xi
 * 
 */
@SuppressWarnings("restriction")
public class TemplateEditorConfiguration extends StructuredTextViewerConfigurationHTML {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML#
	 * getContentAssistProcessors(org.eclipse.jface.text.source.ISourceViewer,
	 * java.lang.String)
	 */
	@Override
	public IContentAssistProcessor[] getContentAssistProcessors(ISourceViewer sourceViewer, String partitionType) {
		IContentAssistProcessor[] processors;
		Logger.info("Partition type:" + partitionType);
		if (partitionType == IStructuredPartitions.DEFAULT_PARTITION || partitionType == IXMLPartitions.XML_DEFAULT
				|| partitionType == IHTMLPartitions.HTML_DEFAULT) {
			processors = new IContentAssistProcessor[] { new ComponentContentAssistProcessor(), new ComponentParameterContentAssistProcessor()};
		} else {
			processors = super.getContentAssistProcessors(sourceViewer, partitionType);
		}
		return processors;
	}

}
