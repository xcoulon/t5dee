/**
 * 
 */
package org.bytesparadise.t5dee.editor.contentassist;

import java.util.List;
import java.util.StringTokenizer;

import org.bytesparadise.t5dee.editor.utils.ContentAssistRequestUtils;
import org.bytesparadise.t5dee.utils.JdtUtils;
import org.bytesparadise.t5dee.utils.Logger;
import org.bytesparadise.t5dee.utils.WebAppUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

/**
 * @author Xi
 * 
 */
@SuppressWarnings("restriction")
public class ComponentContentAssistProcessor extends HTMLContentAssistProcessor {

/**
	 * Adds content assist proposals with Tapestry5 components found in the classpath
	 * This method is called when the user invokes content assist for a tag that needs to be completed  
	 * (i.e.: there is a begin/open tag at the current location).
	 * Note : contentAssistRequest.matchstring does not include the open tag character '<'
	 * 
	 * 
	 * @see
	 * org.eclipse.wst.xml.ui.internal.contentassist.AbstractContentAssistProcessor
	 * #addTagNameProposals(org.eclipse.wst.xml.ui.internal.contentassist.
	 * ContentAssistRequest, int)
	 */
	@Override
	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
		this.addTagInsertionOrTagNameProposals(contentAssistRequest);
	}

	/**
	 * Adds content assist proposals with Tapestry5 components found in the
	 * classpath This method is called when the user invokes content assist for
	 * a new tag (i.e.: there is no begin/open tag at the current location)
	 * 
	 * @see org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor
	 *      #addTagInsertionProposals(org.eclipse.wst.xml.ui.internal.contentassist.
	 *      ContentAssistRequest, int)
	 */
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
		this.addTagInsertionOrTagNameProposals(contentAssistRequest);
	}

	protected void addTagInsertionOrTagNameProposals(ContentAssistRequest contentAssistRequest) {
		String t5ns = ContentAssistRequestUtils.extractTapestry5xNameSpace(contentAssistRequest);
		if (contentAssistRequest.getMatchString().length() == 0
				|| contentAssistRequest.getMatchString().startsWith(t5ns)) {
			try {
				IJavaProject javaProject = ContentAssistRequestUtils.getJavaProject(contentAssistRequest);
				String appPackage = WebAppUtils.getTapestry5AppPackage(javaProject);
				if(appPackage == null) {
					return;
				}
				NullProgressMonitor progressMonitor = new NullProgressMonitor();
				List<IType> components = JdtUtils.getAllComponents(javaProject, progressMonitor);
				if (components.isEmpty()) {
					Logger.warn("Could not find any Tapestry5 component in the project's classpath");
					return;
				}
				for (IType component: components) {
					// TODO : provide the standard <html> image with a decorator
					// for T5 components. Manage with resource (dispose) once
					// for all.
					Image t5ComponentIcon = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
					this.addCompletionProposal(contentAssistRequest, t5ComponentIcon, t5ns, component);
				}

			} catch (CoreException e) {
				Logger.error("Failed to add tagname proposals", e);
			}
		}
	}

	protected void addCompletionProposal(ContentAssistRequest contentAssistRequest, Image t5ComponentIcon, String t5ns,
			IType component) throws JavaModelException {
		String qComponentName = new StringBuilder(t5ns).append(':').append(component.getElementName()).toString();
		if (qComponentName.toLowerCase().indexOf(contentAssistRequest.getMatchString().toLowerCase()) == 0) {
			int replacementOffset = -1;
			int replacementLength = -1;
			String replacementValue = getReplacementValue(component, qComponentName);
			if (contentAssistRequest.getRegion().getType().equals(DOMRegionContext.XML_CONTENT)) {
				replacementOffset = contentAssistRequest.getReplacementBeginPosition();
				replacementLength = contentAssistRequest.getReplacementLength(); // should
				// be
				// zero
				// here...
			} else if (contentAssistRequest.getRegion().getType().equals(DOMRegionContext.XML_TAG_OPEN)) {
				replacementOffset = contentAssistRequest.getDocumentRegion().getStartOffset();
				replacementLength = 0; // TAG_OPEN region length
				// if the previous character in the document is a '<',
				// we want to avoid a final result such as "<<tag_name>"
				// However, it doesn't matter if the next character is '<' too.
				if (contentAssistRequest.getDocumentRegion().getPrevious().getType().equals(
						DOMRegionContext.XML_TAG_OPEN)) {
					replacementOffset--;
					replacementLength++;
				}
			} else if (contentAssistRequest.getRegion().getType().equals(DOMRegionContext.XML_TAG_NAME)) {
				IStructuredDocumentRegion region = contentAssistRequest.getDocumentRegion();
				// move to the previous region in the document if necessary
				// (hint : replacementLength is negative)
				if (contentAssistRequest.getReplacementLength() < 0) {
					region = contentAssistRequest.getDocumentRegion().getPrevious();
				}
				replacementOffset = region.getStartOffset();
				replacementLength = contentAssistRequest.getReplacementLength() + 1;// TAG_NAME +
				// TAG_OPEN regions'
				// cumulated length
			} else {
				Logger.error("Unexpected region type: " + contentAssistRequest.getRegion().getType() + "at "
						+ Thread.currentThread().getStackTrace()[0].toString());
			}
			int cursorOffset = new StringTokenizer(replacementValue, ">\"").nextToken().length() + 1; // move 1 char on the right to arrive after the "=" or """ char
			// Assert.assertEquals("Wrong cursor position", insertionOffset +
			// token.length(), sourceViewer.getSelectedRange().x);
			// String token = tokenizer.nextToken();
			IJavaProject javaProject = ContentAssistRequestUtils.getJavaProject(contentAssistRequest);
			String javadoc = JdtUtils.getJavadoc(javaProject, component);
			ICompletionProposal proposal = new CompletionProposal(replacementValue, replacementOffset,
					replacementLength, cursorOffset, t5ComponentIcon, qComponentName, null, javadoc);
			contentAssistRequest.addProposal(proposal);
		}

	}

	/**
	 * @param component
	 * @param qComponentName
	 * @return
	 */
	protected String getReplacementValue(IType component, String qComponentName) {
		StringBuilder replacement = new StringBuilder();
		replacement.append('<');
		replacement.append(qComponentName);
		try {
			List<IField> requiredParameters = JdtUtils.getComponentRequiredParameters(component);
			for (IField parameter : requiredParameters) {
				replacement.append(' ').append(parameter.getElementName()).append("=\"\"");
			}
		} catch (JavaModelException e) {
			Logger.error("Failed to load required parameters for component " + qComponentName, e);
		}
		replacement.append("></").append(qComponentName).append('>');
		String replacementValue = replacement.toString();
		return replacementValue;
	}

}
