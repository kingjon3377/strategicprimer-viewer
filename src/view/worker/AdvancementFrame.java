package view.worker;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.text.View;

import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import view.util.AddRemovePanel;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;
import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;

/**
 * A GUI to let a user manage workers.
 *
 * @author Jonathan Lovelace
 *
 */
public class AdvancementFrame extends JFrame {
	/**
	 * The text of the relevant button. Can't be private without causing warnings, since it's used in an inner class.
	 */
	static final String NEW_WORKER_ACTION = "Add worker to selected unit ..."; // NOPMD
	/**
	 * Dividers start at half-way.
	 */
	private static final double HALF_WAY = .5;
	/**
	 * Constructor.
	 *
	 * @param source the model containing the data to work from
	 * @param ioHandler the I/O handler so the menu 'open' item, etc., will work
	 */
	public AdvancementFrame(final IWorkerModel source, final IOHandler ioHandler) {
		super("Strategic Primer worker advancement");
		setMinimumSize(new Dimension(640, 480));

		final PlayerChooserHandler pch = new PlayerChooserHandler(this, source);

		final PlayerLabel plabel = new PlayerLabel("", source.getMap()
				.getPlayers().getCurrentPlayer(), "'s Units:");
		pch.addPlayerChangeListener(plabel);
		final WorkerTree tree = new WorkerTree(source.getMap().getPlayers()
				.getCurrentPlayer(), source, pch);
		@Nullable final IWorkerTreeModel wtmodel = (IWorkerTreeModel) tree.getModel();
		assert wtmodel != null;
		final WorkerCreationListener nwl = new WorkerCreationListener(wtmodel,
				IDFactoryFiller.createFactory(source.getMap()));
		tree.addCompletionListener(nwl);
		final AddRemovePanel jarp = new AddRemovePanel(false, "job");
		final AddRemovePanel sarp = new AddRemovePanel(false, "skill");
		final JobsTree jobsTree = new JobsTree(new AddRemovePanel[] {jarp, sarp}, tree);
		final LevelListener llist = new LevelListener();
		jobsTree.addCompletionListener(llist);
		setContentPane(new SplitWithWeights(
				JSplitPane.HORIZONTAL_SPLIT,
				HALF_WAY,
				HALF_WAY,
				new BorderedPanel(new JScrollPane(tree), plabel,
						new ListenedButton(NEW_WORKER_ACTION, nwl), null, null),
				new SplitWithWeights(
						JSplitPane.VERTICAL_SPLIT,
						HALF_WAY,
						.3,
						new BorderedPanel(new JScrollPane(jobsTree),
								htmlize("Worker's Jobs and Skills:"), null,
								null, null),
						new BorderedPanel(
								new BorderedPanel(
										null,
										new BorderedPanel(
												null,
												htmlize("Add a job to the Worker:"),
												jarp, null, null),
										new BorderedPanel(
												null,
												htmlize("Add a Skill to the selected Job:"),
												sarp, null, null), null, null),
								null, new SkillAdvancementPanel(llist, jobsTree),
								null, null))));

		pch.notifyListeners();

		setJMenuBar(new WorkerMenu(ioHandler, this, pch));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Turn a string into left-aligned HTML.
	 *
	 * @param string a string
	 * @return a label, with its text that string wrapped in HTML code that should make it left-aligned.
	 */
	private static JLabel htmlize(final String string) {
		return new JLabel("<html><p align=\"left\">" + string + "</p></html>");
	}

	/**
	 * Get a label's size given a fixed width. Adapted from
	 * http://blog.nobel-joergensen
	 * .com/2009/01/18/changing-preferred-size-of-a-html-jlabel/
	 *
	 * @param component the component we're laying out
	 * @param width the width we're working within
	 * @return the "ideal" dimensions for the component
	 */
	public static Dimension getComponentPreferredSize(
			final JComponent component, final int width) {
		final View view = (View) component
				.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
		final Dimension defaultDim = component.getPreferredSize();
		if (view == null) {
			return defaultDim == null ? new Dimension(width, width) : defaultDim; // NOPMD
		} else {
			view.setSize(width, 0);
			final int wid = (int) Math.ceil(view.getPreferredSpan(View.X_AXIS));
			final int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
			return new Dimension(wid, height);
		}
	}
}
