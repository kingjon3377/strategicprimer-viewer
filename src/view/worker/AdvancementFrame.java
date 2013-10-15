package view.worker;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.text.View;

import model.listeners.CompletionListener;
import model.listeners.LevelGainListener;
import model.listeners.NewWorkerListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;

import org.eclipse.jdt.annotation.Nullable;

import view.util.AddRemovePanel;
import view.util.BorderedPanel;
import view.util.ErrorShower;
import view.util.ListenedButton;
import view.util.SplitWithWeights;
import view.util.SystemOut;
import controller.map.misc.IDFactory;
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
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(AdvancementFrame.class.getName());
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
		final NewWorkerListenerImpl nwl = new NewWorkerListenerImpl(
				(IWorkerTreeModel) tree.getModel(),
				IDFactoryFiller.createFactory(source.getMap()), LOGGER);
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

	/**
	 * A listener to print a line when a worker gains a level.
	 */
	private static final class LevelListener implements LevelGainListener, UnitMemberListener, CompletionListener {
		/**
		 * Constructor.
		 */
		LevelListener() {
			// Needed to give access ...
		}

		/**
		 * The current worker.
		 */
		@Nullable private UnitMember worker = null;
		/**
		 * The current skill.
		 */
		@Nullable private Skill skill = null;
		/**
		 * @param result maybe the newly selected skill
		 */
		@Override
		public void stopWaitingOn(final Object result) {
			if ("null_skill".equals(result)) {
				skill = null;
			} else if (result instanceof Skill) {
				skill = (Skill) result;
			}
		}
		/**
		 * Handle level gain notification.
		 */
		@Override
		public void level() {
			if (worker != null && skill != null) {
				final UnitMember wkr = worker;
				final Skill skl = skill;
				assert skl != null;
				final StringBuilder builder = new StringBuilder();
				builder.append(getName(wkr));
				builder.append(" gained a level in ");
				builder.append(getName(skl));
				SystemOut.SYS_OUT.println(builder.toString());
			}
		}
		/**
		 * @param old the previously selected member
		 * @param selected the newly selected unit member
		 */
		@Override
		public void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected) {
			worker = selected;
		}
		/**
		 * @param named something that may have a name
		 * @return its name if it has one, "null" if null, or its toString
		 *         otherwise.
		 */
		private static String getName(final Object named) {
			if (named instanceof HasName) {
				return ((HasName) named).getName(); // NOPMD
			} else {
				return named.toString();
			}
		}
	}
	/**
	 * A listener to keep track of the currently selected unit and listen for
	 * new-worker notifications, then pass this information on to the tree
	 * model.
	 */
	private static class NewWorkerListenerImpl implements ActionListener,
			CompletionListener, NewWorkerListener {
		/**
		 * The tree model.
		 */
		private final IWorkerTreeModel tmodel;
		/**
		 * The logger to use for logging.
		 */
		private final Logger lgr; // NOPMD
		/**
		 * The current unit. May be null, if nothing is selected.
		 */
		@Nullable private Unit selUnit;
		/**
		 * The ID factory to pass to the worker-creation window.
		 */
		private final IDFactory idf;
		/**
		 * Constructor.
		 * @param treeModel the tree model
		 * @param idFac the ID factory to pass to the worker-creation window.
		 * @param logger the logger to use for logging
		 */
		NewWorkerListenerImpl(final IWorkerTreeModel treeModel, final IDFactory idFac, final Logger logger) {
			tmodel = treeModel;
			idf = idFac;
			lgr = logger;
		}
		/**
		 * @param result the new value to stop waiting on (the newly selected unit, or the newly created worker)
		 */
		@Override
		public void stopWaitingOn(final Object result) {
			if ("null_unit".equals(result)) {
				selUnit = null;
			} else if (result instanceof Unit) {
				selUnit = (Unit) result;
			}
		}
		/**
		 * Handle button press.
		 * @param evt the event to handle.
		 */
		@Override
		public void actionPerformed(@Nullable final ActionEvent evt) {
			if (evt != null && NEW_WORKER_ACTION.equalsIgnoreCase(evt.getActionCommand())) {
				final WorkerConstructionFrame frame = new WorkerConstructionFrame(
						idf);
				frame.addNewWorkerListener(this);
				frame.setVisible(true);
			}
		}
		/**
		 * Handle a new user-created worker.
		 * @param worker the worker to handle
		 */
		@Override
		public void addNewWorker(final Worker worker) {
			final Unit locSelUnit = selUnit;
			if (locSelUnit == null) {
				lgr.warning("New worker created when no unit selected");
				ErrorShower.showErrorDialog(null, "The new worker was not added to a unit because no unit was selected.");
			} else {
				tmodel.addUnitMember(locSelUnit, worker);
			}
		}

	}
}
