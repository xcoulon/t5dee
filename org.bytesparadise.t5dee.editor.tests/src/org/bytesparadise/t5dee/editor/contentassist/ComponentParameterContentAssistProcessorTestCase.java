package org.bytesparadise.t5dee.editor.contentassist;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.bytesparadise.t5dee.editor.EditorBaseTestCase;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ComponentParameterContentAssistProcessorTestCase extends EditorBaseTestCase {

	@Before
	public void setupEditor() throws Exception {
		super.setupEditorConfiguration("org/bytesparadise/pages/Index.tml");
	}

	private Map<String, ICompletionProposal> getAllProposals(int offset) throws Exception {
		ICompletionProposal[] proposals = new ComponentParameterContentAssistProcessor().computeCompletionProposals(
				sourceViewer, offset);
		Map<String, ICompletionProposal> completionProposals = new HashMap<String, ICompletionProposal>();
		if (proposals != null) {
			for (ICompletionProposal proposal : proposals) {
				completionProposals.put(proposal.getDisplayString(), proposal);
			}
		}
		return completionProposals;
	}

	@Test
	public void testAddAttributeNameProposalsOnPageLinkElement() throws BadLocationException, Exception {
		Map<String, ICompletionProposal> completionProposals = getAllProposals(getDocumentOffset(21, 39));
		Assert.assertEquals("Wrong number of proposals", 1, completionProposals.size());
		Assert.assertTrue("Proposal not found", completionProposals.containsKey("context"));
		Assert.assertNotNull("No javadoc for proposal", completionProposals.get("context").getAdditionalProposalInfo());
	}

	@Test
	public void testAddAttributeNameProposalsOnLayoutElement() throws Exception {
		int offset = getDocumentOffset(6, 6);
		String text = "<t:Layout />";
		document.replace(offset, 0, text);
		offset += "<t:Layout ".length();
		Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
		Assert.assertEquals("Wrong number of proposals", 3, completionProposals.size());
		Assert.assertTrue("Proposal not found", completionProposals.containsKey("title"));
		Assert.assertTrue("Proposal not found", completionProposals.containsKey("sidebarTitle"));
		Assert.assertTrue("Proposal not found", completionProposals.containsKey("sidebar"));
		Assert.assertNull("No javadoc was expected for proposal", completionProposals.get("sidebar")
				.getAdditionalProposalInfo());
		Assert.assertNotNull("Javadoc not found for proposal", completionProposals.get("title")
				.getAdditionalProposalInfo());
	}
	
	@Test
	public void testAddAttributeNameProposalsOnLayoutElementWithMatchString() throws Exception {
		int offset = getDocumentOffset(6, 6);
		String text = "<t:Layout t/>";
		document.replace(offset, 0, text);
		offset += "<t:Layout t".length();
		Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
		Assert.assertEquals("Wrong number of proposals", 1, completionProposals.size());
		Assert.assertTrue("Proposal not found", completionProposals.containsKey("title"));
		completionProposals.get("title").apply(document);
		String expectedInsertion = "<t:Layout title=\"\"/>";
		offset -= "<t:Layout t".length();
		Assert.assertEquals("Wrong document content after proposal application", expectedInsertion, document.get(offset, expectedInsertion.length()));
	}

	@Test
	public void testAddAttributeNameProposalsOnSomeHTMLElement() throws BadLocationException, Exception {
		Map<String, ICompletionProposal> completionProposals = getAllProposals(getDocumentOffset(13, 12));
		Assert.assertEquals("Wrong number of proposals", 0, completionProposals.size());

	}

}
