/**
 * 
 */
package org.bytesparadise.t5dee.editor.contentassist;

import java.util.Map;
import java.util.Map.Entry;

import org.bytesparadise.t5dee.editor.Activator;
import org.bytesparadise.t5dee.editor.utils.ContentAssistRequestUtils;
import org.bytesparadise.t5dee.utils.JdtUtils;
import org.bytesparadise.t5dee.utils.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Xi
 * 
 */
@SuppressWarnings("restriction")
public class ComponentParameterContentAssistProcessor extends HTMLContentAssistProcessor {

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
		// check if the current element is a T5 component
		Node node = contentAssistRequest.getNode();
		String nodeName = node.getNodeName();
		try {
			IProgressMonitor progressMonitor = new NullProgressMonitor();
			String t5ns = ContentAssistRequestUtils.extractTapestry5xNameSpace(contentAssistRequest) + ":";
			if (!nodeName.startsWith(t5ns)) {
				return;
			}
			nodeName = nodeName.substring(t5ns.length()).toLowerCase();
			IJavaProject javaProject = ContentAssistRequestUtils.getJavaProject(contentAssistRequest);
			IType component = JdtUtils.getComponentByName(javaProject, nodeName, progressMonitor);
			// check if the current element is a T5 component (ie, exists)
			if (component != null && component.exists()) {
				Map<IField, IAnnotation> parameters = JdtUtils.getComponentParameters(component);
				final NamedNodeMap attributes = node.getAttributes();
				for (Entry<IField, IAnnotation> parameter : parameters.entrySet()) {
					final String paramName = parameter.getKey().getElementName();
					// skip parameter if it doesn't start with the "matchString"
					if(contentAssistRequest.getMatchString().length()>0 && !paramName.startsWith(contentAssistRequest.getMatchString())) {
						continue;
					}
					if (attributes.getNamedItem(paramName) == null) {
						Image image = null;
						if(JdtUtils.isRequiredParameter(parameter.getValue())) {
							image = Activator.getDefault().getImage("icons/attribute_required.gif");
						} else {
							image = Activator.getDefault().getImage("icons/attribute.gif");
						}
						String javadoc = JdtUtils.getJavadoc(javaProject, parameter.getKey());
						ICompletionProposal proposal = new CompletionProposal(paramName + "=\"\"", contentAssistRequest
								.getReplacementBeginPosition(), contentAssistRequest.getMatchString().length(), paramName.length() + 2, image,
								paramName, null, javadoc);
						contentAssistRequest.addProposal(proposal);
					}
				}
			} else {
				Logger.info("Current element is not a T5 component: " + nodeName
						+ ". Skipping content assist processing");
			}

		} catch (Exception e) {
			Logger.error("Failed to provide attributes content assist proposals on element " + nodeName, e);
		} finally {
			//super.addAttributeNameProposals(contentAssistRequest);
		}
	}

}
