package org.bytesparadise.t5dee.utils;

import java.io.IOException;
import java.io.Reader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bytesparadise.t5dee.utils.internal.SearchResultCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.wst.sse.ui.internal.derived.HTML2TextReader;

@SuppressWarnings("restriction")
public class JdtUtils {

	public static List<IType> getAllComponents(IJavaProject javaProject, IProgressMonitor progressMonitor)
			throws CoreException {
		// Create search pattern
		SearchPattern pattern = SearchPattern.createPattern("*", IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
		// search in javaProject's expected components packages
		List<IType> components = searchAndReturnAll(javaProject, pattern);
		// sort results
		Collections.sort(components, new Comparator<IType>() {
			public int compare(IType t1, IType t2) {
				return t1.getElementName().compareTo(t2.getElementName());
			}
		});
		return components;
	}

	public static IType getComponentByName(IJavaProject javaProject, String componentName,
			IProgressMonitor progressMonitor) throws CoreException {
		// Create search pattern
		SearchPattern pattern = SearchPattern.createPattern(componentName, IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
		// search in javaProject's expected components packages
		List<IType> components = searchAndReturnAll(javaProject, pattern);
		// filter results: return first item found
		if (components.size() > 0) {
			return components.get(0);
		}
		return null;
	}

	public static List<IType> searchAndReturnAll(IJavaProject javaProject, SearchPattern pattern) throws CoreException {
		// Get the custom components package
		String appPackage = WebAppUtils.getTapestry5AppPackage(javaProject);
		String customComponentsPackage = appPackage.replaceAll("\\.", "/").concat("/components");
		// Get the standard Tapestry5 components package
		String standardComponentsPackage = "org/apache/tapestry5/corelib/components";
		// loop on both packages and collect all results

		List<IType> results = new ArrayList<IType>();
		for (String pkgName : new String[] { customComponentsPackage, standardComponentsPackage }) {
			IJavaElement pkgElement = javaProject.findElement(new Path(pkgName));
			// Create search scope
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { pkgElement });
			// Get the search requestor
			SearchResultCollector requestor = new SearchResultCollector();
			// Search
			SearchEngine searchEngine = new SearchEngine();
			searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					requestor, null);
			results.addAll(requestor.getResults());
		}
		return results;
	}

	public static List<IPackageFragmentRoot> getSourceFolders(IJavaProject javaProject) throws JavaModelException {
		List<IPackageFragmentRoot> folders = new ArrayList<IPackageFragmentRoot>();
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot root : roots) {
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE && root.getJavaProject().equals(javaProject)
					&& !root.isArchive() && !root.isExternal()) {
				folders.add(root);
			}
		}
		return folders;
	}

	/**
	 * returns all fields annotated with @o.a.t5.annotations.Parameter
	 * 
	 * @return
	 * @throws JavaModelException
	 */
	public static Map<IField, IAnnotation> getComponentParameters(IType component) throws JavaModelException {
		Map<IField, IAnnotation> parameters = new HashMap<IField, IAnnotation>();
		IField[] fields = component.getFields();
		for (IField field : fields) {
			IAnnotation annotation = getParameterAnnotation(component, field);
			if (annotation != null) {
				parameters.put(field, annotation);
			}

		}

		return parameters;
	}

	private static IAnnotation getParameterAnnotation(IType component, IField field) throws JavaModelException {
		IAnnotation annotation = field.getAnnotation("org.apache.tapestry5.annotations.Parameter");
		if (annotation.exists()) {
			return annotation;
		}
		annotation = field.getAnnotation("Parameter");
		if (annotation.exists()) {
			String[][] r = component.resolveType(annotation.getElementName());
			if (r != null && r.length == 1 && r[0][0].equals("org.apache.tapestry5.annotations")) {
				return annotation;
			}
		}
		return null;
	}

	public static List<IField> getComponentRequiredParameters(IType component) throws JavaModelException {
		List<IField> parameters = new ArrayList<IField>();
		IField[] fields = component.getFields();
		for (IField field : fields) {
			IAnnotation annotation = getParameterAnnotation(component, field);
			if (annotation != null) {
				IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
				for (IMemberValuePair memberValuePair : memberValuePairs) {
					if (memberValuePair.getMemberName().equals("required")
							&& memberValuePair.getValue() == Boolean.TRUE) {
						parameters.add(field);
					}
				}
			}
		}

		return parameters;
	}

	public static boolean isRequiredParameter(IAnnotation annotation) throws JavaModelException {
		if (annotation != null) {
			IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
			for (IMemberValuePair memberValuePair : memberValuePairs) {
				if (memberValuePair.getMemberName().equals("required") && memberValuePair.getValue() == Boolean.TRUE) {
					return true;
				}
			}
		}

		return false;
	}

	//TODO: Manage HTML rendering in the Additional info display view. 
	//See https://bugs.eclipse.org/bugs/show_bug.cgi?id=218482
	//See https://bugs.eclipse.org/bugs/show_bug.cgi?id=241896
	public static String getJavadoc(IJavaProject javaProject, IMember member) {
		if (member == null) {
			return null;
		}
		try {
			Reader reader= JavadocContentAccess.getHTMLContentReader(member, true, false);
			if (reader != null) {
				reader= new HTML2TextReader(reader, null);
			}
			if (reader != null) {
				String str= getString(reader);
				BreakIterator breakIterator= BreakIterator.getSentenceInstance();
				breakIterator.setText(str);
				return str.substring(0, breakIterator.next());
			}
				//return getString(reader);
		} catch (JavaModelException e) {
			Logger.error("Failed to read component '" + member.getElementName() + "'source code", e);
		}

		return null;
	}

	/**
	 * Gets the reader content as a String
	 * 
	 * @param reader
	 *            the reader
	 * @return the reader content as string
	 */
	private static String getString(Reader reader) {
		if (reader == null) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		char[] buffer = new char[1024];
		int count;
		try {
			while ((count = reader.read(buffer)) != -1)
				buf.append(buffer, 0, count);
		} catch (IOException e) {
			return null;
		}
		return buf.toString().trim();
	}

}
