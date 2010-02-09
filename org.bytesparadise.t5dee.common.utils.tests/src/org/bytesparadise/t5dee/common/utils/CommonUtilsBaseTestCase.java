package org.bytesparadise.t5dee.common.utils;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

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
public class CommonUtilsBaseTestCase {

	private static final Logger LOGGER = LogManager.getLogger(CommonUtilsBaseTestCase.class);
	public static final String SAMPLE_PROJECT_NAME = "tapestry5-sample";
	
	protected IJavaProject javaProject;
	

	@BeforeClass
	public static void setupWorkspace() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (!workspace.isAutoBuilding()) {
			IWorkspaceDescription description = workspace.getDescription();
			description.setAutoBuilding(true);
			workspace.setDescription(description);
		}
		removeSampleProject();
	}

	@Before
	public void refreshAndBindSampleProject() throws Exception {
		importSampleProject();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(SAMPLE_PROJECT_NAME);
		javaProject = JavaCore.create(project);
		//WorkbenchTasks.refreshProject(getSampleProjectPath(), new NullProgressMonitor());
	}
	
	
	/**
	 * Called by subclasses to setup the workspace with project and files (xml,
	 * java, etc.)
	 * 
	 * @throws Exception
	 */
	public static void importSampleProject() throws Exception {
		LOGGER.debug("Setting up sample project.");
		Long start = new Date().getTime();
		NullProgressMonitor monitor = new NullProgressMonitor();
		IProject project = WorkbenchTasks.importExistingProject(getSampleProjectPath(), monitor);
		IJavaProject javaProject = JavaCore.create(project);
		
		WorkbenchTasks.buildWorkspace(monitor);
		// attach java sources for Tapestry5 jars
		for(IPackageFragmentRoot packageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
			if(packageFragmentRoot.getElementName().indexOf("tapestry") != -1) {
				IPath sourcePath = packageFragmentRoot.getPath();
				sourcePath = new Path(sourcePath.removeFileExtension().toOSString() + "-sources.jar");
				if(!sourcePath.toFile().exists()) {
					Assert.fail("Missing source file (required for javadoc extraction): " + sourcePath.toString());
				}
				packageFragmentRoot.attachSource(sourcePath, null, new NullProgressMonitor());
			}
		}
		Assert.assertNotNull("Source not attached", javaProject.findType("org.apache.tapestry5.corelib.components.PageLink").getSource());
		
		LOGGER.debug("Setting up sample project done in " + (new Date().getTime() - start) + " millis");
	}

	/**
	 * Called by subclasses to setup the workspace with project and files (xml,
	 * java, etc.)
	 * 
	 * @throws Exception
	 */
	public static void removeSampleProject() throws Exception {
		IPath path = getSampleProjectPath();
		NullProgressMonitor monitor = new NullProgressMonitor();
		WorkbenchTasks.removeExistingProject(path, monitor);
	}

	/**
	 * @return
	 */
	private static IPath getSampleProjectPath() {
		IPath path = null;
		if (System.getProperty("user.dir") != null) {
			path = new Path(System.getProperty("user.dir")).append("..").append(SAMPLE_PROJECT_NAME).makeAbsolute();
		} else if (System.getProperty("tapestry5SampleProjectPath") == null) {
			path = new Path(System.getProperty("tapestry5SampleProjectPath"));
		} else {
			Assert
					.fail("The sample project was not found in the laucnher workspace under name 'tapestry5-sample'. You need to setup the T5 Sample project root path with a system parameter.\n"
							+ "e.g.: -Dtapestry5SampleProjectPath=\"/<workspace_loc>/tapestry5-sample\"");

		}
		return path;
	}

}
