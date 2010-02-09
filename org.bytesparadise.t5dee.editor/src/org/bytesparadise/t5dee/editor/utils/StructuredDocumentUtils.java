package org.bytesparadise.t5dee.editor.utils;

import java.util.Iterator;

import org.bytesparadise.t5dee.utils.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeNameRegion;
import org.eclipse.wst.xml.core.internal.parser.regions.AttributeValueRegion;
import org.eclipse.wst.xml.core.internal.parser.regions.TagNameRegion;

@SuppressWarnings("restriction")
public class StructuredDocumentUtils {

	/**
	 * Gets the file associated to the given document.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param document the document
	 * @return the file
	 */
	public static IFile getFile(final IStructuredDocument document) {
		IStructuredModel model = StructuredModelManager.getModelManager().getModelForEdit(document);
		IFile resource = null;
		try {
			String baselocation = model.getBaseLocation();
			if (baselocation != null) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IPath filePath = new Path(baselocation);
				if (filePath.segmentCount() > 0) {
					resource = root.getFile(filePath);
				}
			}
		} finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
		return resource;
	}

	/**
	 * @param documentRegion
	 * @param attributeValueRegion
	 * @return
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static String extractAttributeName(IStructuredDocumentRegion structuredDocumentRegion,
			AttributeValueRegion attributeValueRegion) {
		int index = structuredDocumentRegion.getRegions().indexOf(attributeValueRegion);
		ITextRegion region = structuredDocumentRegion.getRegions().get(index - 2);
		return structuredDocumentRegion.getText(region);
	}

	/**
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param region
	 * @param attributeName
	 * @return
	 */
	public static String extractAttributeValue(IStructuredDocumentRegion structuredDocumentRegion, String attributeName) {
		ITextRegionList regionList = structuredDocumentRegion.getRegions();
		for (int i = 0; i < regionList.size(); i++) {
			ITextRegion textRegion = regionList.get(i);
			if (textRegion instanceof AttributeNameRegion
					&& attributeName.equals(StringUtils.strip(structuredDocumentRegion.getText(textRegion)))
					&& regionList.get(i + 2) instanceof AttributeValueRegion) {
				return StringUtils.strip(structuredDocumentRegion.getText(regionList.get(i + 2)));
			}
		}
		Logger.info("Unable to locate attribute value for attribute name '" + attributeName + "'");
		return null;
	}

	/**
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param structuredDocumentRegion
	 * @param offset
	 * @return
	 */
	public static String extractAttributeValue(IStructuredDocumentRegion structuredDocumentRegion, int offset) {
		ITextRegion region = structuredDocumentRegion.getRegionAtCharacterOffset(offset);
		if (region != null) {
			return StringUtils.strip(structuredDocumentRegion.getText(region));
		}
		Logger.warn("No region was found at this (offset) location");
		return null;
	}

	/**
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param structuredDocumentRegion
	 * @param offset
	 * @return
	 */
	public static String extractTagName(IStructuredDocumentRegion structuredDocumentRegion) {
		for (Iterator<?> iterator = structuredDocumentRegion.getRegions().iterator(); iterator.hasNext();) {
			ITextRegion region = (ITextRegion) iterator.next();
			if (region instanceof TagNameRegion) {
				return StringUtils.strip(structuredDocumentRegion.getText(region));
			}
		}
		//Logger.warn("No tag name was found at this (offset) location");
		return null;
	}
	

}
