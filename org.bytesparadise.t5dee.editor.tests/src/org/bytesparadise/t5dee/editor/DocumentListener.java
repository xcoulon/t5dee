/**
 * 
 */
package org.bytesparadise.t5dee.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * @author xcoulon
 *
 */
public class DocumentListener implements IDocumentListener {

	
	private boolean documentChanged = false;
	
	private final String originalDocumentContent;
	
	public DocumentListener(String originalDocumentContent) {
		this.originalDocumentContent = originalDocumentContent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		documentChanged = true;

	}

	public String getOriginalDocumentContent() {
		return originalDocumentContent;
	}

	public boolean isDocumentChanged() {
		return documentChanged;
	}

}
