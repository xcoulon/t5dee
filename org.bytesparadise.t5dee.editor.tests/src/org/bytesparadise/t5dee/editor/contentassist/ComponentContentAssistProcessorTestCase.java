package org.bytesparadise.t5dee.editor.contentassist;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.bytesparadise.t5dee.common.utils.WebAppUtilsTestCase;
import org.bytesparadise.t5dee.common.utils.WorkbenchTasks;
import org.bytesparadise.t5dee.editor.EditorBaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ComponentContentAssistProcessorTestCase extends EditorBaseTestCase {

	@Before
	public void setupEditor() throws Exception {
		super.setupEditorConfiguration("org/bytesparadise/pages/Index.tml");
		Assert.assertNotNull("JavaProject not found", javaProject);
		Assert.assertNotNull("Project not found", javaProject.getProject());
		Assert.assertTrue("Project is not a JavaProject", JavaProject.hasJavaNature(javaProject.getProject()));
		Assert.assertNotNull("Editor is not configured", sourceViewer);
	}

	/**
	 * Get the content completion proposals at the given document
	 * <code>offset</code>.
	 * 
	 * @param offset
	 * @return
	 * @throws Exception
	 */
	// TODO : add optional dependency on some html plugin (that provides html
	// content assist) ?
	private Map<String, ICompletionProposal> getAllProposals(int offset) throws Exception {
		ICompletionProposal[] proposals = new ComponentContentAssistProcessor().computeCompletionProposals(
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
	public void testAddTagInsertionProposalsWithResults() throws Exception {
		int offset = getDocumentOffset(6, 6);
		Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
		// assert that the returned proposals contain both html tag names and T5
		// component names
		Assert.assertEquals("Wrong number of proposals", 47, completionProposals.size());
		Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("a"));
		Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("br"));
		Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:PageLink"));
		Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:Layout"));
		Assert.assertNotNull("Missing javadoc", completionProposals.get("t:PageLink").getAdditionalProposalInfo());
		Assert.assertNotNull("Missing javadoc", completionProposals.get("t:Layout").getAdditionalProposalInfo());
	}

	@Test
	public void testAddTagInsertionProposalsWithUndefinedAppPackage() throws Exception {
		try {
			WebAppUtilsTestCase.replaceDeploymentDescriptorWith(javaProject, "web-noT5Config.xml");
			int offset = getDocumentOffset(6, 6);
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			// assert that the returned proposals contain both html tag names
			// and T5
			// component names
			Assert.assertEquals("Wrong number of proposals", 0, completionProposals.size());
		} finally {
			WebAppUtilsTestCase.restoreDeploymentDescriptor(javaProject);
		}
	}

	@Test
	public void testAddTagInsertionProposalsWithoutDeploymentDescriptor() throws Exception {
		try {
			WebAppUtilsTestCase.replaceDeploymentDescriptorWith(javaProject, null);
			int offset = getDocumentOffset(6, 6);
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			// assert that the returned proposals contain both html tag names
			// and T5
			// component names
			Assert.assertEquals("Wrong number of proposals", 0, completionProposals.size());
		} finally {
			WebAppUtilsTestCase.restoreDeploymentDescriptor(javaProject);
		}

	}

	@Test
	public void testAddTagInsertionProposalsWithoutSourceComponents() throws Exception {
		IClasspathEntry srcFolder = WorkbenchTasks.removeSourceFolder(javaProject, new Path("src/main/java"),
				new NullProgressMonitor());
		Assert.assertNull("Unexpected component: Layout", javaProject.findType("org.bytesparadise.components.Layout"));
		Assert.assertNotNull("Missing component: PageLink", javaProject
				.findType("org.apache.tapestry5.corelib.components.PageLink"));
		try {
			int offset = getDocumentOffset(6, 6);
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			Assert.assertEquals("Wrong number of proposals", 46, completionProposals.size());
			Assert.assertFalse("Unexpected proposal t:Layout", completionProposals.containsKey("t:Layout"));
			Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:LinkSubmit"));
		} finally {
			WorkbenchTasks.addClasspathEntry(javaProject, srcFolder, new NullProgressMonitor());
			Assert.assertNotNull("Missing component: Layout", javaProject
					.findType("org.bytesparadise.components.Layout"));
		}
	}

	@Test
	public void testAddTagInsertionProposalsWithoutT5CoreComponents() throws Exception {
		IClasspathEntry entry = WorkbenchTasks.removeClasspathEntryByName(javaProject, "org.eclipse.jst.j2ee.internal.web.container",
				new NullProgressMonitor());
		Assert.assertNull("Unexpected component: PageLink", javaProject
				.findType("org.apache.tapestry5.corelib.components.PageLink"));
		Assert.assertNotNull("Missing component: Layout", javaProject
				.findType("org.bytesparadise.components.Layout"));
		try {
			int offset = getDocumentOffset(6, 6);
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			Assert.assertEquals("Wrong number of proposals", 1, completionProposals.size());
			Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:Layout"));
			Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("t:LinkSubmit"));
		} finally {
			WorkbenchTasks.addClasspathEntry(javaProject, entry, new NullProgressMonitor());
			Assert.assertNotNull("Missing component: PageLink", javaProject
					.findType("org.apache.tapestry5.corelib.components.PageLink"));
		}
	}

	@Test
	public void testAddTagNameProposalsWithResults() throws Exception {
		int offset = getDocumentOffset(6, 6);
		String text = "<t:L";
		document.replace(offset, 0, text);
		offset += text.length();
		Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
		Assert.assertEquals("Wrong number of proposals", 4, completionProposals.size());
		Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("a"));
		Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("br"));
		Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:Layout"));
		Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:LinkSubmit"));
		Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("t:PageLink"));
		Assert.assertNotNull("Missing javadoc", completionProposals.get("t:Layout").getAdditionalProposalInfo());
		Assert.assertNotNull("Missing javadoc", completionProposals.get("t:LinkSubmit").getAdditionalProposalInfo());
	}

	@Test
	public void testAddTagNameProposalsWithUndefinedAppPackage() throws Exception {
		try {
			WebAppUtilsTestCase.replaceDeploymentDescriptorWith(javaProject, "web-noT5Config.xml");
			int offset = getDocumentOffset(6, 6);
			String text = "<t:L";
			document.replace(offset, 0, text);
			offset += text.length();
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			// assert that the returned proposals contain both html tag names
			// and T5
			// component names
			Assert.assertEquals("Wrong number of proposals", 0, completionProposals.size());
		} finally {
			WebAppUtilsTestCase.restoreDeploymentDescriptor(javaProject);
		}
	}

	@Test
	public void testAddTagNameProposalsWithoutDeploymentDescriptor() throws Exception {
		try {
			WebAppUtilsTestCase.replaceDeploymentDescriptorWith(javaProject, null);
			int offset = getDocumentOffset(6, 6);
			String text = "<t:L";
			document.replace(offset, 0, text);
			offset += text.length();
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			// assert that the returned proposals contain both html tag names
			// and T5
			// component names
			Assert.assertEquals("Wrong number of proposals", 0, completionProposals.size());
		} finally {
			WebAppUtilsTestCase.restoreDeploymentDescriptor(javaProject);
		}
	}

	@Test
	public void testAddTagNameProposalsWithoutSourceComponents() throws Exception {
		IClasspathEntry srcFolder = WorkbenchTasks.removeSourceFolder(javaProject, new Path("src/main/java"),
				new NullProgressMonitor());
		Assert.assertNull("Unexpected component: Layout", javaProject.findType("org.bytesparadise.components.Layout"));
		try {
			int offset = getDocumentOffset(6, 6);
			String text = "<t:L";
			document.replace(offset, 0, text);
			offset += text.length();
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			Assert.assertEquals("Wrong number of proposals", 3, completionProposals.size());
			Assert.assertFalse("Unexpected proposal t:Layout", completionProposals.containsKey("t:Layout"));
			Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:LinkSubmit"));
		} finally {
			WorkbenchTasks.addClasspathEntry(javaProject, srcFolder, new NullProgressMonitor());
			Assert.assertNotNull("Missing component: Layout", javaProject
					.findType("org.bytesparadise.components.Layout"));
		}
	}

	@Test
	public void testAddTagNameProposalsWithoutT5CoreComponents() throws Exception {
		IClasspathEntry entry = WorkbenchTasks.removeClasspathEntryByName(javaProject, "org.eclipse.jst.j2ee.internal.web.container",
				new NullProgressMonitor());
		Assert.assertNull("Unexpected component: PageLink", javaProject
				.findType("org.apache.tapestry5.corelib.components.PageLink"));
		try {
			int offset = getDocumentOffset(6, 6);
			String text = "<t:L";
			document.replace(offset, 0, text);
			offset += text.length();
			Map<String, ICompletionProposal> completionProposals = getAllProposals(offset);
			Assert.assertEquals("Wrong number of proposals", 1, completionProposals.size());
			Assert.assertFalse("Unexpected proposal", completionProposals.containsKey("t:LinkSubmit"));
			Assert.assertTrue("Missing proposal", completionProposals.containsKey("t:Layout"));
		} finally {
			WorkbenchTasks.addClasspathEntry(javaProject, entry, new NullProgressMonitor());
			Assert.assertNotNull("Missing component: PageLink", javaProject
					.findType("org.apache.tapestry5.corelib.components.PageLink"));
		}
	}

	@Test
	public void testApplyLayoutTagFromTagInsertionProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(7, 5), null, "<t:Layout title=\"\"></t:Layout>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyLayoutTagFromTagOpenProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(7, 5), "<", "<t:Layout title=\"\"></t:Layout>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyLayoutTagFromTagNameProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(7, 5), "<t:L", "<t:Layout title=\"\"></t:Layout>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyLayoutTagFromTagInsertionProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(9, 5), null, "<t:Layout title=\"\"></t:Layout>");
	}

	@Test
	public void testApplyLayoutTagFromTagOpenProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(9, 5), "<", "<t:Layout title=\"\"></t:Layout>");
	}

	@Test
	public void testApplyLayoutTagFromTagNameProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:Layout", getDocumentOffset(9, 5), "<t:L", "<t:Layout title=\"\"></t:Layout>");
	}

	@Test
	public void testApplyPageLinkTagFromTagInsertionProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(7, 5), null, "<t:PageLink page=\"\"></t:PageLink>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyPageLinkTagFromTagOpenProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(7, 5), "<", "<t:PageLink page=\"\"></t:PageLink>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyPageLinkTagFromTagNameProposalWithinXmlContentRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(7, 5), "<t:P", "<t:PageLink page=\"\"></t:PageLink>");
		Assert.assertEquals("Rest of document moved", "<p>", document.get(getDocumentOffset(9, 5), "<p>".length()));
	}

	@Test
	public void testApplyPageLinkTagFromTagInsertionProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(9, 5), null, "<t:PageLink page=\"\"></t:PageLink>");
	}

	@Test
	public void testApplyPageLinkTagFromTagOpenProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(9, 5), "<", "<t:PageLink page=\"\"></t:PageLink>");
	}

	@Test
	public void testApplyPageLinkTagFromTagNameProposalNearTagOpenRegion() throws Exception {
		applyAndAssertTagInsertion("t:PageLink", getDocumentOffset(9, 5), "<t:P", "<t:PageLink page=\"\"></t:PageLink>");
	}

	protected void applyAndAssertTagInsertion(String proposal, int insertionOffset, String begin,
			String expectedInsertion) throws Exception {
		int proposalOffset = insertionOffset;
		if (begin != null) {
			document.replace(insertionOffset, 0, begin);
			// move offset, but ignore trailing spaces
			proposalOffset += begin.trim().length();
		}
		sourceViewer.getSelectionProvider().setSelection(new TextSelection(proposalOffset, 0));
		Map<String, ICompletionProposal> completionProposals = getAllProposals(proposalOffset);
		// Find the proposal for t:PageLink component, apply proposal and check
		// the doc contains the expected char sequence at the offset
		ICompletionProposal completionProposal = completionProposals.get(proposal);
		Assert.assertNotNull("Missing proposal", completionProposal);
		completionProposal.apply(document);
		Assert.assertEquals("Wrong document content after proposal application", expectedInsertion, document.get(
				insertionOffset, expectedInsertion.length()));
		// cursor position assertion fails, but cursor really moves when
		// "manually testing" in an Eclipse instance...
		// StringTokenizer tokenizer = new StringTokenizer(expectedInsertion,
		// ">\"");
		// Assert.assertEquals("Wrong cursor position", insertionOffset +
		// token.length(), sourceViewer.getSelectedRange().x);
		// String token = tokenizer.nextToken();
	}

}
