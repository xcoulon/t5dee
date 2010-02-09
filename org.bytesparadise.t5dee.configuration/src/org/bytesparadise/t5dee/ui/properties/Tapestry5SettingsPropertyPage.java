/**
 * 
 */
package org.bytesparadise.t5dee.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.bytesparadise.t5dee.Activator;
import org.bytesparadise.t5dee.i18n.Messages;
import org.bytesparadise.t5dee.utils.JdtUtils;
import org.bytesparadise.t5dee.utils.Logger;
import org.bytesparadise.t5dee.utils.WebAppUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Xi
 * 
 */
public class Tapestry5SettingsPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private Text javaControllersFolderTextField;

	private Text templatesFolderTextField;

	private Button javaControllersFolderSelectButton;

	private Button templatesFolderSelectButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		// define the preferencesStore
		final IProject project = (IProject) getElement();
		final IEclipsePreferences preferences = new ProjectScope(project)
				.getNode(Activator.PLUGIN_ID);

		final Composite mainSection = new Composite(parent, SWT.NONE);
		mainSection.setLayout(new GridLayout(3, false));
		// Header message
		createLabel(mainSection, "FoldersSection.Label", 3);

		// Java controllers folder
		this.javaControllersFolderTextField = createText(mainSection,
				"JavaControllersFolder.Label", preferences.get(
						"javaControllersFolder", ""));
		this.javaControllersFolderSelectButton = createButton(mainSection,
				"JavaControllersFolder.Button");
		this.javaControllersFolderSelectButton
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						Tapestry5SettingsPropertyPage.this.widgetSelected(
								getFolders(project, false),
								javaControllersFolderTextField);
					}
				});
		// Templates folder
		this.templatesFolderTextField = createText(mainSection,
				"TemplatesFolder.Label", preferences.get("templatesFolder", ""));
		// this.templatesFolderTextField.setText(moduleName);
		this.templatesFolderSelectButton = createButton(mainSection,
				"TemplatesFolder.Button");
		this.templatesFolderSelectButton
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						Tapestry5SettingsPropertyPage.this.widgetSelected(
								getFolders(project, true),
								templatesFolderTextField);
					}
				});

		// Horizontal separator
		Label separator = new Label(mainSection, SWT.SEPARATOR | SWT.HORIZONTAL
				| SWT.LINE_SOLID);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		separator.setLayoutData(layoutData);

		return mainSection;
	}

	protected Object[] getFolders(IProject project, boolean includeWebContent) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			List<IPackageFragmentRoot> sourceFolders;
			sourceFolders = JdtUtils.getSourceFolders(javaProject);
			if (includeWebContent) {
				IFolder webContentDir = WebAppUtils
						.getWebContentDir(javaProject.getProject());
				List<Object> elements = new ArrayList<Object>(sourceFolders);
				elements.add(webContentDir);
				return elements.toArray();
			}
			return sourceFolders.toArray();
		} catch (JavaModelException e) {
			Logger.error(
					"Failed to retrieve source elements from java project", e);
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		savePreferences();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		return savePreferences();
	}

	protected boolean savePreferences() {
		try {
			IProject project = (IProject) getElement();
			IEclipsePreferences preferences = new ProjectScope(project)
					.getNode(Activator.PLUGIN_ID);
			preferences.put("javaControllersFolder",
					javaControllersFolderTextField.getText());
			preferences.put("templatesFolder", templatesFolderTextField
					.getText());
			preferences.flush();
			preferences.sync();
		} catch (BackingStoreException e) {
			Logger.error("Failed to store properties:", e);
			return false;
		}
		return true;
	}

	protected void widgetSelected(final Object[] objects, final Text textField) {
		ListDialog dialog = new ListDialog(getShell());
		dialog.setLabelProvider(new SourceFoldersLabelProvider());
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setInput(objects);
		// dialog.setInput(sourceFoldersStructuredContentProvider);
		dialog.setTitle(Messages.getString("FolderSelection.ListDialogTitle"));
		dialog.setMessage(Messages
				.getString("FolderSelection.ListDialogMessage"));
		if (dialog.open() == ElementTreeSelectionDialog.OK) {
			Object result = dialog.getResult()[0];
			if(result instanceof IPackageFragmentRoot) {
				textField.setText(((IPackageFragmentRoot) result).getPath().toString());
			} else if(result instanceof IFolder) {
				textField.setText(((IFolder) result).getFullPath().toString());
			}
		}
	}

	static Button createButton(Composite parent, String textKey) {
		Button button = new Button(parent, SWT.NONE);
		button.setText(Messages.getString(textKey));
		button.setLayoutData(new GridData());
		return button;
	}

	static Text createText(Composite parent, String labelKey, String value) {
		createLabel(parent, labelKey);
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (value != null) {
			text.setText(value);
		}
		return text;
	}

	static Label createLabel(Composite parent, String textKey) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.getString(textKey));
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		return label;
	}

	static Label createLabel(Composite parent, String textKey,
			int horizontalSpan) {
		Label label = new Label(parent, SWT.WRAP);
		label.setText(Messages.getString(textKey));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = horizontalSpan;
		label.setLayoutData(layoutData);
		return label;
	}

}
