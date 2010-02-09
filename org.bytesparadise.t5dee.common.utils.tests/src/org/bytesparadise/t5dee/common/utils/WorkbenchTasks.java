package org.bytesparadise.t5dee.common.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class WorkbenchTasks {

	public static void addClasspathEntry(IJavaProject javaProject, IClasspathEntry classpathEntry,
			IProgressMonitor progressMonitor) throws CoreException {
		Assert.assertNotNull("Classpath entry is null", classpathEntry);
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		IClasspathEntry[] newEntries = entries.toArray(new IClasspathEntry[entries.size() + 1]);
		newEntries[newEntries.length - 1] = classpathEntry;
		javaProject.setRawClasspath(newEntries, progressMonitor);
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		buildWorkspace(progressMonitor);
	}

	public static IClasspathEntry removeClasspathEntryByName(IJavaProject javaProject, String name,
			IProgressMonitor progressMonitor) throws CoreException {
		IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
		int index = 0;
		for (IClasspathEntry entry : classpathEntries) {
			if (entry.getPath().toFile().getAbsolutePath().indexOf(name) != -1) {
				break;
			}
			index++;
		}
		Assert.assertTrue("Librairie not found in classpath entries", index < classpathEntries.length);
		IClasspathEntry[] newClasspathEntries = (IClasspathEntry[]) ArrayUtils.remove(classpathEntries, index);
		javaProject.setRawClasspath(newClasspathEntries, progressMonitor);
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		buildWorkspace(progressMonitor);
		return classpathEntries[index];
	}

	public static IClasspathEntry removeSourceFolder(IJavaProject javaProject, IPath path,
			IProgressMonitor progressMonitor) throws CoreException {
		IFolder srcFolder = javaProject.getProject().getFolder(path);
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcFolder.getFullPath());
		for (Iterator<IClasspathEntry> entryIterator = entries.iterator(); entryIterator.hasNext();) {
			IClasspathEntry entry = entryIterator.next();
			if (entry.getPath().equals(srcEntry.getPath())) {
				entryIterator.remove();
			}
		}
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), progressMonitor);
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
		buildWorkspace(progressMonitor);
		return srcEntry;
	}

	/**
	 * Removes the existing project from workspace
	 * 
	 * @param projectRootPath
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	public static void removeExistingProject(IPath path, IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException, CoreException {
		IPath dotProjectPath = path.addTrailingSeparator().append(".project");
		IProjectDescription description = IDEWorkbenchPlugin.getPluginWorkspace()
				.loadProjectDescription(dotProjectPath);
		String projectName = description.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if (project.exists()) {
			project.delete(true, monitor);
		}
	}

	/**
	 * Import the whole tapestry5-sample project from the dev-workspace into the
	 * junit-workspace.
	 * 
	 * @param projectRootPath
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static IProject importExistingProject(IPath path, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException, CoreException {
		IPath dotProjectPath = path.addTrailingSeparator().append(".project");
		IProjectDescription description = IDEWorkbenchPlugin.getPluginWorkspace()
				.loadProjectDescription(dotProjectPath);
		String projectName = description.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		if (!project.exists()) {
			IProjectDescription desc = workspace.newProjectDescription(projectName);
			desc.setBuildSpec(description.getBuildSpec());
			desc.setComment(description.getComment());
			desc.setDynamicReferences(description.getDynamicReferences());
			desc.setNatureIds(description.getNatureIds());
			desc.setReferencedProjects(description.getReferencedProjects());

			try {
				monitor.beginTask(DataTransferMessages.WizardProjectsImportPage_CreateProjectsTask, 100);
				project.create(desc, new SubProgressMonitor(monitor, 30));
				project.open(-1, new SubProgressMonitor(monitor, 70));
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			} finally {
				monitor.done();
			}
		} 
		// import project from location copying files - use default project
		// location for this workspace
		URI locationURI = description.getLocationURI();
		File importSource = new File(locationURI);
		// some error condition occured.
		List filesToImport = FileSystemStructureProvider.INSTANCE.getChildren(importSource);
		ImportOperation operation = new ImportOperation(project.getFullPath(), importSource,
				FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {

					public String queryOverwrite(String pathString) {
						return IOverwriteQuery.ALL;
					}
				}, filesToImport);
		operation.setContext(null);
		// need to overwrite .project, .classpath files
		operation.setOverwriteResources(true);
		operation.setCreateContainerStructure(false);
		operation.run(monitor);
		return project;
	}

	@Deprecated
	public static void refreshProject(IPath path, IProgressMonitor monitor) throws CoreException {
		IPath dotProjectPath = path.addTrailingSeparator().append(".project");
		IProjectDescription description = IDEWorkbenchPlugin.getPluginWorkspace()
				.loadProjectDescription(dotProjectPath);
		String projectName = description.getName();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		Assert.assertTrue("Project does not exist.", project.exists());

	}

	public static void buildWorkspace(IProgressMonitor progressMonitor) throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, progressMonitor);
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, progressMonitor);
	}

	public static void deleteAllMarkersOnFile(IFile file) throws CoreException {
		file.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
	}

	public static IFile getFile(String filePath) throws CoreException {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(filePath));
		if (file != null && !file.exists()) {
			Assert.fail("Unable to locate " + file.getFullPath() + " template file.");
		}
		file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		return file;
	}

}
