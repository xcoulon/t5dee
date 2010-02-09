package org.bytesparadise.t5dee.editor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bytesparadise.t5dee.common.utils.CommonUtilsBaseTestCase;
import org.bytesparadise.t5dee.common.utils.WorkbenchTasks;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.exceptions.ResourceAlreadyExists;
import org.eclipse.wst.sse.core.internal.provisional.exceptions.ResourceInUse;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.text.rules.StructuredTextPartitionerForXML;
import org.junit.After;
import org.junit.Assert;

/**
 * Made abstract, so won't be automatically picked up as test (since intended to
 * be subclassed).
 * 
 * Based on
 * http://dev.eclipse.org/viewcvs/index.cgi/incubator/sourceediting/tests
 * /org.eclipse
 * .wst.xsl.ui.tests/src/org/eclipse/wst/xsl/ui/tests/AbstractXSLUITest
 * .java?revision=1.2&root=WebTools_Project&view=markup
 * 
 */
@SuppressWarnings("restriction")
public class EditorBaseTestCase extends CommonUtilsBaseTestCase {

	private static final Logger LOGGER = LogManager.getLogger(EditorBaseTestCase.class);
	public static final String SAMPLE_PROJECT_NAME = "tapestry5-sample";
	
	protected IStructuredDocument document = null;
	protected ISourceViewer sourceViewer = null;
	protected IFile templateFile = null;
	protected DocumentListener documentListener = null;


	@After
	public void refreshDocument() {
		if(documentListener != null && documentListener.isDocumentChanged()) {
			document.set(documentListener.getOriginalDocumentContent());
		}
	}

	
	/**
	 * Called by subclasses to setup the source editor
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	protected void setupEditorConfiguration(String fileName) throws Exception {
		LOGGER.debug("Setting up document " + fileName);
		Long start = new Date().getTime();
		templateFile = WorkbenchTasks.getFile(javaProject.getProject().getName() + File.separator + "src"
				+ File.separator + "main" + File.separator + "resources" + File.separator + fileName);
		document = openFileForEdit(templateFile);
		sourceViewer = initializeSourceViewer(document);
		documentListener = new DocumentListener(document.get());
		document.addDocumentListener(documentListener);
		// WorkbenchTasks.buildWorkspace(monitor);
		LOGGER.debug("Setting up document done in " + (new Date().getTime() - start) + " millis");
	}

	protected void tearDownProject() throws Exception {
		if (document != null) {
			IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForEdit(document);
			model.releaseFromEdit();

		}
	}

	/**
	 * @return
	 * @throws BadLocationException
	 */
	protected int getDocumentOffset(int lineNumber, int columnNumber, String expectedNodeName)
			throws BadLocationException {
		int offset = sourceViewer.getDocument().getLineOffset(lineNumber - 1) + columnNumber - 1;
		IDOMNode node = (IDOMNode) ContentAssistUtils.getNodeAt(sourceViewer, offset);
		assertEquals("Wrong node name returned:", expectedNodeName, node.getNodeName());
		return offset;
	}

	/**
	 * @return
	 * @throws BadLocationException
	 */
	protected int getDocumentOffset(int lineNumber, int columnNumber) throws BadLocationException {
		return sourceViewer.getDocument().getLineOffset(lineNumber - 1) + columnNumber - 1;
	}

	/**
	 * @param region
	 * @return
	 */
	protected Region getDirtyRegion(int offset) {
		IStructuredDocumentRegion region = ContentAssistUtils.getStructuredDocumentRegion(sourceViewer, offset);
		return new Region(region.getStartOffset(), region.getLength());
	}


	protected ISourceViewer initializeSourceViewer(IDocument document) {
		// some test environments might not have a "real" display
		if (Display.getCurrent() != null) {
			Shell shell = null;
			Composite parent = null;
			if (PlatformUI.isWorkbenchRunning()) {
				shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			} else {
				shell = new Shell(Display.getCurrent());
			}
			parent = new Composite(shell, SWT.NONE);
	
			// dummy viewer
			StructuredTextViewer sourceViewer = new StructuredTextViewer(parent, null, null, false, SWT.NONE);
			// configure SourceViewer
			sourceViewer.configure(new TemplateEditorConfiguration());
			sourceViewer.setDocument(document);
			return sourceViewer;
		} else {
			Assert.fail("Unable to run the test as a display must be available.");
			return null;
		}
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected IStructuredDocument openFileForEdit(IFile file) throws ResourceAlreadyExists, ResourceInUse,
			IOException, CoreException {
		IStructuredModel model = StructuredModelManager.getModelManager().getModelForEdit(file);
		model.reload(file.getContents());
	
		IStructuredDocument document = model.getStructuredDocument();
		IDocumentPartitioner partitioner = new StructuredTextPartitionerForXML();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		return document;
	}

}
