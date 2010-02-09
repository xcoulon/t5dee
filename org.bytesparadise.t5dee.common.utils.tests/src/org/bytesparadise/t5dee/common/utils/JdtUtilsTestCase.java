/**
 * 
 */
package org.bytesparadise.t5dee.common.utils;

import java.util.List;

import junit.framework.Assert;

import org.bytesparadise.t5dee.utils.JdtUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;

/**
 * @author Xi
 * 
 */
@SuppressWarnings("restriction")
public class JdtUtilsTestCase extends CommonUtilsBaseTestCase {

	@Test
	public void testAllComponents() throws Exception {
		List<IType> allComponents = JdtUtils.getAllComponents(javaProject, new NullProgressMonitor());
		Assert.assertNotNull("No components found", allComponents);
		Assert.assertEquals("Unexpected number of found components", 47, allComponents.size());
	}

	@Test
	public void testGetComponentParameters() throws CoreException {
		List<IType> allComponents = JdtUtils.getAllComponents(javaProject, new NullProgressMonitor());
		Assert.assertNotNull("No components found", allComponents);
		for (IType component : allComponents) {
			if ("BeanEditForm".equals(component.getElementName())) {
				Assert.assertEquals("Wrong number of parameters", 7, JdtUtils.getComponentParameters(component)
						.size());
			} else if ("PageLink".equals(component.getElementName())) {
				Assert.assertEquals("Wrong number of parameters", 2, JdtUtils.getComponentParameters(component)
						.size());
			}
		}
	}

	@Test
	public void testGetComponentRequiredParameters() throws CoreException {
		List<IType> allComponents = JdtUtils.getAllComponents(javaProject, new NullProgressMonitor());
				Assert.assertNotNull("No components found", allComponents);
		for (IType component : allComponents) {
			if ("BeanEditForm".equals(component.getElementName())) {
				Assert.assertEquals("Wrong number of parameters", 1, JdtUtils.getComponentRequiredParameters(
						component).size());
			} else if ("PageLink".equals(component.getElementName())) {
				Assert.assertEquals("Wrong number of parameters", 1, JdtUtils.getComponentRequiredParameters(
						component).size());
			}
		}
	}

	@Test
	public void testGetSourceFolders() throws Exception {
		List<IPackageFragmentRoot> sourceFolders = JdtUtils.getSourceFolders(javaProject);
		Assert.assertEquals("Wrong result", 4, sourceFolders.size());
		Assert.assertEquals("Wrong item", "/tapestry5-sample/src/main/java", sourceFolders.get(0).getPath().toString());
		Assert.assertEquals("Wrong item", "/tapestry5-sample/src/main/resources", sourceFolders.get(1).getPath()
				.toString());
		Assert.assertEquals("Wrong item", "/tapestry5-sample/src/test/java", sourceFolders.get(2).getPath().toString());
		Assert.assertEquals("Wrong item", "/tapestry5-sample/src/test/resources", sourceFolders.get(3).getPath()
				.toString());
	}
	
	@Test
	public void testSearchAndReturnSingleComponent() throws CoreException {
		IType component = JdtUtils.getComponentByName(javaProject, "pagelink", new NullProgressMonitor());
		Assert.assertNotNull("No component found", component);
		Assert.assertEquals("Bad component found", "PageLink", component.getElementName());
	}

	@Test
	public void testSearchAndReturnNoComponent() throws CoreException {
		IType component = JdtUtils.getComponentByName(javaProject, "unknown", new NullProgressMonitor());
		Assert.assertNull("No component found", component);
	}

	@Test
	public void testGetJavadocOnDocumentedSourceType() throws JavaModelException {
		IType component = javaProject.findType("org.bytesparadise.components.Layout");
		Assert.assertTrue(component.exists());
		String javadoc = JdtUtils.getJavadoc(javaProject, component);
		Assert.assertEquals("Wrong javadoc", "Layout component for pages of application tapestry5-sample.", javadoc);
	}

	@Test
	public void testGetJavadocOnUndocumentedSourceType() throws JavaModelException {
		IType component = javaProject.findType("org.bytesparadise.pages.About");
		Assert.assertTrue(component.exists());
		String javadoc = JdtUtils.getJavadoc(javaProject, component);
		Assert.assertNull("No expected javadoc", javadoc);	
	}

	@Test
	public void testGetJavadocOnBinaryTypeWithAttachedSource() throws JavaModelException {
		IType component = javaProject.findType("org.apache.tapestry5.corelib.components.PageLink");
		Assert.assertTrue(component.exists());
		String javadoc = JdtUtils.getJavadoc(javaProject, component);
		Assert.assertNotNull("Javadoc not found", javadoc);
		Assert.assertTrue("Wrong javadoc", javadoc.startsWith("Generates a render request link "));
		Assert.assertEquals("Wrong javadoc", -1, javadoc.indexOf("*"));
		
	}

	@Test
	public void testGetJavadocOnBinaryTypeWithoutAttachedSource() throws JavaModelException {
		IType component = javaProject.findType("org.apache.log4j.Logger");
		Assert.assertTrue(component.exists());
		String javadoc = JdtUtils.getJavadoc(javaProject, component);
		Assert.assertNull("No expected javadoc", javadoc);	
	}
	
	@Test
	public void testGetJavadocOnUnexistingType() throws JavaModelException {
		IType component = javaProject.findType("org.bytesparadise.components.Unknown");
		Assert.assertNull(component);
		String javadoc = JdtUtils.getJavadoc(javaProject, component);
		Assert.assertNull("No expected javadoc", javadoc);	
	}

}
