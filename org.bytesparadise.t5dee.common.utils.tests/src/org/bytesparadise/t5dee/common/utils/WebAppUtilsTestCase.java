/**
 * 
 */
package org.bytesparadise.t5dee.common.utils;

import java.io.IOException;

import junit.framework.Assert;

import org.bytesparadise.t5dee.utils.WebAppUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Xi
 * 
 */
@SuppressWarnings("restriction")
public class WebAppUtilsTestCase extends CommonUtilsBaseTestCase {

	@Before
	public void setupEditor() throws Exception {
		Assert.assertNotNull("JavaProject not found", super.javaProject);
		Assert.assertNotNull("Project not found", super.javaProject.getProject());
		Assert.assertTrue("Project is not a JavaProject", JavaProject.hasJavaNature(super.javaProject.getProject()));
	}

	@Test
	public void testGetTapestry5AppPackage_2_3() throws Exception {
		replaceDeploymentDescriptorWith(javaProject, "web-2_3.xml");
		Assert.assertEquals("App package not as expected", "org.bytesparadise23", WebAppUtils
				.getTapestry5AppPackage(javaProject));
		restoreDeploymentDescriptor(javaProject);
	}

	@Test
	public void testGetTapestry5AppPackage_2_4() throws Exception {
		replaceDeploymentDescriptorWith(javaProject, "web-2_4.xml");
		Assert.assertEquals("App package not as expected", "org.bytesparadise24", WebAppUtils
				.getTapestry5AppPackage(javaProject));
		restoreDeploymentDescriptor(javaProject);
	}

	@Test
	public void testGetTapestry5AppPackage_noT5Config() throws Exception {
		replaceDeploymentDescriptorWith(javaProject, "web-noT5Config.xml");
		Assert.assertNull("No result expected", WebAppUtils.getTapestry5AppPackage(javaProject));
		restoreDeploymentDescriptor(javaProject);
	}

	@Test
	public void testGetTapestry5AppPackage_noWebxml() throws Exception {
		replaceDeploymentDescriptorWith(javaProject, (String) null);
		Assert.assertNull("No result expected", WebAppUtils.getTapestry5AppPackage(javaProject));
		restoreDeploymentDescriptor(javaProject);
	}

	/**
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */
	public static void replaceDeploymentDescriptorWith(IJavaProject javaProject, String webxmlReplacementName)
			throws CoreException, IOException {
		IFolder webInfFolder = WebAppUtils.getWebInfFolder(javaProject.getProject());
		IResource webxmlResource = webInfFolder.findMember("web.xml");

		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(javaProject.getProject());
		webArtifactEdit.getDeploymentDescriptorRoot().eResource().unload();
		webArtifactEdit.dispose();

		IPath webxmlBackupPath = new Path("web.xml.backup");

		// remove previous backup if exists
		IResource webxmlBackupResource = webInfFolder.findMember(webxmlBackupPath);
		if (webxmlBackupResource != null && webxmlBackupResource.exists()) {
			webxmlBackupResource.delete(true, new NullProgressMonitor());
		}

		if (webxmlResource != null && webxmlResource.exists()) {
			webxmlResource.move(webxmlBackupPath, true, new NullProgressMonitor());
		}
		if (webxmlReplacementName != null) {
			IResource webxmlReplacementResource = webInfFolder.findMember(webxmlReplacementName);
			webxmlReplacementResource.copy(webxmlResource.getFullPath(), true, new NullProgressMonitor());
		}
		WorkbenchTasks.buildWorkspace(new NullProgressMonitor());
	}

	public static void restoreDeploymentDescriptor(IJavaProject javaProject) throws CoreException, IOException {
		IFolder webInfFolder = WebAppUtils.getWebInfFolder(javaProject.getProject());
		IResource webxmlResource = webInfFolder.findMember("web.xml");
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(javaProject.getProject());
		if (webxmlResource != null) {
			webArtifactEdit.getDeploymentDescriptorRoot().eResource().unload();
			webArtifactEdit.dispose();
		}

		IPath webxmlBackupPath = new Path("web.xml.backup");
		// remove previous backup if exists
		IResource webxmlBackupResource = webInfFolder.findMember(webxmlBackupPath);
		Assert.assertNotNull("Backup of web.xml does not exist. Restore called twice ?", webxmlBackupResource);
		Assert.assertTrue("Backup of web.xml does not exist. Restore called twice ?", webxmlBackupResource.exists());
		if (webxmlResource != null && webxmlResource.exists()) {
			webxmlResource.delete(true, new NullProgressMonitor());
		}
		webxmlBackupResource.move(webInfFolder.getFile("web.xml").getFullPath(), true, new NullProgressMonitor());
		WorkbenchTasks.buildWorkspace(new NullProgressMonitor());
	}

}
