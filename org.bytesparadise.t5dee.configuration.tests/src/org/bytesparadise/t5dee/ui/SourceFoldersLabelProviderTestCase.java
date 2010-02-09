package org.bytesparadise.t5dee.ui;

import java.util.List;

import junit.framework.Assert;

import org.bytesparadise.t5dee.common.utils.CommonUtilsBaseTestCase;
import org.bytesparadise.t5dee.ui.properties.SourceFoldersLabelProvider;
import org.bytesparadise.t5dee.utils.JdtUtils;
import org.bytesparadise.t5dee.utils.WebAppUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jst.j2ee.common.internal.util.CommonUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

@SuppressWarnings("restriction")
public class SourceFoldersLabelProviderTestCase extends CommonUtilsBaseTestCase {

	private SourceFoldersLabelProvider labelProvider = new SourceFoldersLabelProvider();

	@Test
	public void testGetTextForPackageFragmentRootElement() throws JavaModelException {
		List<IPackageFragmentRoot> roots = JdtUtils.getSourceFolders(javaProject);
		Assert.assertEquals("Wrong result", "/tapestry5-sample/src/main/java", labelProvider.getText(roots.get(0)));
	}

	@Test
	public void testGetTextForFolderElement() {
		IFolder folder = WebAppUtils.getWebContentDir(javaProject.getProject());
		Assert.assertEquals("Wrong result", "/tapestry5-sample/src/main/webapp", labelProvider.getText(folder));
	}

	@Test
	public void testGetTextForNullElement() {
		Assert.assertNull("Wrong result", labelProvider.getText(null));
	}

	@Test
	public void testGetTextForUnknownElementType() {
		Assert.assertNull("Wrong result", labelProvider.getText(new Path("/tapestry5-sample")));
	}

	@Test
	public void testImageForPackageFragmentRootElement() throws JavaModelException {
		List<IPackageFragmentRoot> roots = JdtUtils.getSourceFolders(javaProject);
		Image test = labelProvider.getImage(roots.get(0));
		Image control = JavaPluginImages.DESC_OBJS_PACKFRAG_ROOT.createImage();
		byte[] controlData = control.getImageData().data;
		byte[] testData = test.getImageData().data;
		for(int i = 0; i < testData.length; i++) {
			Assert.assertEquals("Wrong result", controlData[i], testData[i]);
		}
		test.dispose();
		control.dispose();
	
	}

	@Test
	public void testImageForFolderElement() {
		IFolder folder = WebAppUtils.getWebContentDir(javaProject.getProject());
		Image test = labelProvider.getImage(folder);
		Image control = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		Assert.assertEquals("Wrong result", control, test);
		test.dispose();
	}

	@Test
	public void testImageForNullElement() {
		Assert.assertNull("Wrong result", labelProvider.getImage(null));
		
	}

	@Test
	public void testImageForUnknownElementType() {
		Assert.assertNull("Wrong result", labelProvider.getImage(new Path("/tapestry5-sample")));
		
	}

}
