package org.bytesparadise.t5dee.utils;

import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jst.j2ee.common.CommonFactory;
import org.eclipse.jst.j2ee.common.ParamValue;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.jst.j2ee.webapplication.ContextParam;
import org.eclipse.jst.j2ee.webapplication.WebApp;
import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;

@SuppressWarnings("restriction")
public class WebAppUtils {

	private static final String TAPESTRY_APP_PACKAGE = "tapestry.app-package";

	
	/*public static IPath getDeploymentDescriptorPath(IJavaProject javaProject) {
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(javaProject.getProject());
		return webArtifactEdit.getDeploymentDescriptorPath();
	}*/
	
	public static IFolder getWebInfLibFolder(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder contentFolder = component.getRootFolder();
		return (IFolder) contentFolder.getFolder(WebArtifactEdit.WEBLIB).getUnderlyingFolder();
	}

	public static IFolder getWebInfFolder(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder contentFolder = component.getRootFolder();
		return (IFolder) contentFolder.getFolder(WebArtifactEdit.WEB_INF).getUnderlyingFolder();
	}

	public static IFolder getWebContentFolder(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		IVirtualFolder contentFolder = component.getRootFolder();
		return (IFolder) contentFolder.getUnderlyingFolder();
	}

	@SuppressWarnings( { "unchecked" })
	public static String getTapestry5AppPackage(IJavaProject javaProject) {
		IFolder webInfFolder = getWebInfFolder(javaProject.getProject());
		IResource webxmlResource = webInfFolder.findMember("web.xml");
		if(webxmlResource == null || !webxmlResource.exists()) {
			Logger.warn("No deployment descriptor found in this web application");
			return null;
		}
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(javaProject.getProject());
		// webArtifactEdit.getDeploymentDescriptorRoot().eResource().unload();
		if(!webArtifactEdit.isValid()) {
			return null;
		}
		webArtifactEdit.getDeploymentDescriptorRoot();
		WebApp webApp = webArtifactEdit.getWebApp();
		// If J2EE 1.4, add the param value and description info instances to
		// the context params
		if (webApp.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID) {
			EList<ParamValue> contextParams = webApp.getContextParams();
			Iterator<ParamValue> iterator = contextParams.iterator();
			while (iterator.hasNext()) {
				ParamValue contextParam = iterator.next();
				if (TAPESTRY_APP_PACKAGE.equals(contextParam.getName())) {
					return contextParam.getValue();
				}
			}
		}
		// If J2EE 1.2 or 1.3, use the servlet specific context param instances
		else {
			EList<ContextParam> contextParams = webApp.getContexts();
			Iterator<ContextParam> iterator = contextParams.iterator();
			while (iterator.hasNext()) {
				ContextParam contextParam = iterator.next();
				if (TAPESTRY_APP_PACKAGE.equals(contextParam.getParamName())) {
					return contextParam.getParamValue();
				}
			}
		}
		Logger.warn("No 'tapestry.app-package' context-param declared in the application deployement descriptor");
		return null;
	}

	@SuppressWarnings( { "unchecked" })
	public void createOrUpdateTapestry5AppPackage(IJavaProject javaProject, String appModulePackage) {
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForWrite(javaProject.getProject());
		try {
			WebApp webApp = webArtifactEdit.getWebApp();
			// If J2EE 1.4, add the param value and description info instances
			// to
			// the context params
			if (webApp.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID) {
				// Create 1.4 common param value
				ParamValue param = CommonFactory.eINSTANCE.createParamValue();
				param.setName(TAPESTRY_APP_PACKAGE);
				param.setValue(appModulePackage);
				// Add the context param
				webApp.getContextParams().add(param);
			}
			// If J2EE 1.2 or 1.3, use the servlet specific context param
			// instances
			else {
				// Create the web init param
				ContextParam param = WebapplicationFactory.eINSTANCE.createContextParam();
				// Set the param name
				param.setParamName(TAPESTRY_APP_PACKAGE);
				// Set the param value
				param.setParamValue(appModulePackage);
				// Add the context param
				webApp.getContexts().add(param);
			}
		} finally {
			webArtifactEdit.dispose();
		}
	}

	public void createOrUpdateTapestry5Filter(IJavaProject javaProject, String filterName, String filterClassName) {
		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForWrite(javaProject.getProject());
		try {
			WebApp webApp = webArtifactEdit.getWebApp();
			// If J2EE 1.4, add the param value and description info instances
			// to
			// the context params
			if (webApp.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID) {
			}
			// If J2EE 1.2 or 1.3, use the servlet specific context param
			// instances
			else {
			}
		} finally {
			webArtifactEdit.dispose();
		}
	}

	/**
	 * Returns the location of the web project's WebContent/webapp directory.
	 * 
	 * @param pj
	 *            the web project
	 * @return the location of the web project's WebContent/webapp directory
	 */

	public static IFolder getWebContentDir(final IProject pj) {
		final IVirtualComponent vc = ComponentCore.createComponent(pj);
		final IVirtualFolder vf = vc.getRootFolder();
		return (IFolder) vf.getUnderlyingFolder();
	}

	/**
	 * Returns the location of the web project's WEB-INF/lib directory.
	 * 
	 * @param pj
	 *            the web project
	 * @return the location of the WEB-INF/lib directory
	 */
	public static IFolder getWebInfLibDir(final IProject pj) {
		final IVirtualComponent vc = ComponentCore.createComponent(pj);
		final IVirtualFolder vf = vc.getRootFolder().getFolder("WEB-INF/lib");
		return (IFolder) vf.getUnderlyingFolder();
	}

}
