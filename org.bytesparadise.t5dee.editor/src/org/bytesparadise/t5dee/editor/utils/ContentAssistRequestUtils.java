package org.bytesparadise.t5dee.editor.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class ContentAssistRequestUtils {

	/**
	 * Returns the attribute name for which ContentAssist is going to be
	 * proposed to the user
	 * 
	 * @param contentAssistRequest
	 * @return
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractAttributeName(ContentAssistRequest contentAssistRequest) {
		ITextRegion region = contentAssistRequest.getRegion();
		ITextRegionList regions = contentAssistRequest.getDocumentRegion().getRegions();
		while (!(region instanceof AttributeNameRegion)) {
			region = regions.get(regions.indexOf(region) - 1);
		}

		return contentAssistRequest.getDocumentRegion().getFullText(region).trim();

	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractAttributeMatchValue(ContentAssistRequest contentAssistRequest) {
		String match = contentAssistRequest.getMatchString();
		return StringUtils.stripQuotes(match).trim();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractAttributeCurrentValue(ContentAssistRequest contentAssistRequest) {
		String value = contentAssistRequest.getText();
		// remove heading and trailing double quotes if present...
		return StringUtils.stripQuotes(value).trim();
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractTagName(ContentAssistRequest contentAssistRequest) {
		return contentAssistRequest.getNode().getNodeName();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractTapestry5xNameSpace(ContentAssistRequest contentAssistRequest) {
		Node node = contentAssistRequest.getNode();
		String t5ns = null;
		while(t5ns == null && node.getParentNode() != null) {
			node = node.getParentNode();
			NamedNodeMap attributes = node.getAttributes();
			for(int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				if("http://tapestry.apache.org/schema/tapestry_5_1_0.xsd".equals(attribute.getNodeValue())) {
					t5ns = attribute.getLocalName();
					break;
				}
			}
		}
		
		return t5ns;
	}

	/**
	 * Retrieve the JavaProject the current edited resource belongs to.
	 * 
	 * @param document the current document under edition
	 * @return instance of IJavaProject containing the edited resource
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IJavaProject getJavaProject(final ContentAssistRequest contentAssistRequest) {
		IFile resource = StructuredDocumentUtils.getFile(contentAssistRequest.getDocumentRegion().getParentDocument());
		IJavaProject project = JavaCore.create(resource.getProject());
		return project;
	}
}
