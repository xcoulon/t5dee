/**
 * 
 */
package org.bytesparadise.t5dee.ui.facet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

/**
 * @author Xi
 * 
 */
public class Tapestry5FacetInstallDelegate implements IDelegate {

	/**
	 * The method that's called to execute the delegate.
	 * 
	 * @param project
	 *            the workspace project
	 * @param fv
	 *            the project facet version that this delegate is handling; this
	 *            is useful when sharing the delegate among several versions of
	 *            the same project facet or even different project facets
	 * @param config
	 *            the configuration object, or null if defaults should be used
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if the delegate fails for any reason
	 */

	public void execute(IProject project, IProjectFacetVersion fv, Object config, IProgressMonitor monitor) {
		//TODO: prompt for app-package ('browse' button ?) and filter name
		
		//TODO: create/modify web.xml to add filter with filter-mapping and context-params
		
		//TODO: create base package and sub-packges (pages, mixins, ) AppModule class
	}

}
