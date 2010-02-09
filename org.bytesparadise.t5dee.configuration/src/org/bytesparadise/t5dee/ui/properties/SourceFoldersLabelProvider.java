/**
 * 
 */
package org.bytesparadise.t5dee.ui.properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * @author Xi
 *
 */
@SuppressWarnings("restriction")
public class SourceFoldersLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if(element instanceof IPackageFragmentRoot) {
			JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
			return JavaPluginImages.DESC_OBJS_PACKFRAG_ROOT.createImage();
		} 
		if(element instanceof IFolder) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if(element instanceof IPackageFragmentRoot) {
			return ((IPackageFragmentRoot)element).getPath().toString();
		} 
		if(element instanceof IFolder) {
			return ((IFolder)element).getFullPath().toString();
		}
		return null;
	}

}
