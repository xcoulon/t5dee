package org.bytesparadise.t5dee.utils.internal;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class SearchResultCollector extends SearchRequestor {

	private final List<IType> results = new LinkedList<IType>();
	
	/**
	 * @return the results
	 */
	public List<IType> getResults() {
		return results;
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		if(match.getElement() instanceof IType) {
			IType element = (IType)match.getElement();
			if(element.getDeclaringType() == null && element.getElementName().length()> 0) { // skip inner classes
				results.add(element);
			}
		}
	}

}
