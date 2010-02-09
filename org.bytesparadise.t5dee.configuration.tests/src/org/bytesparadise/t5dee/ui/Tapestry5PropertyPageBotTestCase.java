package org.bytesparadise.t5dee.ui;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.bytesparadise.t5dee.common.utils.CommonUtilsBaseTestCase;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.ChildrenControlFinder;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class Tapestry5PropertyPageBotTestCase extends CommonUtilsBaseTestCase {

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		SWTBotPreferences.TIMEOUT = 15000;
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();

	}

	@Before
	public void setupEditor() throws Exception {
		super.importSampleProject();
	}

	@Test
	@Ignore
	public void addTapestry5Facet() throws Exception {
		// ensure the "Window" menu is available
		bot.waitUntil(new DefaultCondition() {
			public boolean test() throws Exception {
				return bot.menu("Window").widget != null;
			}

			public String getFailureMessage() {
				return "Widget is not ready!";
			}
		});

		bot.menu("Window").menu("Show View").menu("Project Explorer").click();
		bot.viewByTitle("Project Explorer").setFocus();
		// Select your project in tree view and expand it.
		SWTBotTree tree = bot.viewByTitle("Project Explorer").bot().tree();
		tree.select("tapestry5-sample").expandNode("tapestry5-sample");
		bot.menu("Properties").click();
		// moves to the properties dailog box
		SWTBotShell shell = bot.shell("Properties for tapestry5-sample");
		shell.activate();
		bot.tree().getTreeItem("Project Facets").select();
		bot.waitUntil(new DefaultCondition() {
			public boolean test() throws Exception {
				return bot.table().widget != null;
			}

			public String getFailureMessage() {
				return "Widget is not ready!";
			}
		});

		bot.table().setFocus();

		SWTBotTable table = new SWTBotTable(bot.widget(widgetOfType(Table.class), shell.widget));
		Assert.assertEquals("Wrong number of columns", 3, table.columnCount());

		// Get access to the underlying table
		final List<Table> controls = new ChildrenControlFinder(bot.activeShell().widget).findControls(WidgetOfType
				.widgetOfType(Table.class));
		Assert.assertEquals("1 and only 1 table was expected", 1, controls.size());
		// try with ugly delay...
		Thread.sleep(10000);
		// Using the underlying table, select an item
		Assert.assertEquals("Wrong number of columns", 3, bot.table().columnCount()); // FAILS
																						// HERE:
																						// columnCount()
																						// returns
																						// '0'

		Table facetsTable = controls.get(0);
		List<TableColumn> columns = Arrays.asList(facetsTable.getColumns());
		int index = columns.indexOf("Project Facet");
		Assert.assertTrue("'Project Facet' column not found", index != -1);
		facetsTable.getItem(0).setChecked(true);
		Assert.assertTrue(facetsTable.getItem(0).getChecked());
		/*
		 * // SWTBotTable facetsTable = bot.table(); index =
		 * facetsTable.indexOf("Tapestry 5", index);
		 * Assert.assertTrue("Facet not found", index != -1); SWTBotTableItem
		 * t5FacetActivationCheckBox = facetsTable.getTableItem(index);
		 * Assert.assertTrue("Did not expect this facet to be activated",
		 * !t5FacetActivationCheckBox.isChecked());
		 * t5FacetActivationCheckBox.check(); bot.button("Apply").click();
		 * Assert.assertTrue("Did expect this facet to be activated",
		 * t5FacetActivationCheckBox.isChecked()); bot.button("OK").click();//
		 * FIXME: assert that the project is // actually created, for later
		 */
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}

}