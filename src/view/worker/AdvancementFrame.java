package view.worker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.text.View;

import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Skill;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import util.PropertyChangeAdapter;
import util.PropertyChangeSource;
import view.util.AddRemovePanel;
import view.util.BorderedPanel;
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
public class AdvancementFrame extends JFrame implements PropertyChangeListener,
		PropertyChangeSource {
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
		source.addPropertyChangeListener(this);
		setMinimumSize(new Dimension(640, 480));

		final PlayerChooserHandler pch = new PlayerChooserHandler(this, source);

		final PlayerLabel plabel = new PlayerLabel("", source.getMap()
				.getPlayers().getCurrentPlayer(), "'s Units:");
		pch.addPropertyChangeListener(plabel);
		final WorkerTree tree = new WorkerTree(source.getMap().getPlayers()
				.getCurrentPlayer(), source, this, pch, source);
		final IDFactory idf = IDFactoryFiller.createFactory(source.getMap());
		final JButton addWorkerButton = new JButton("Add worker to selected unit ...");
		final NewWorkerListener nwl = new NewWorkerListener((IWorkerTreeModel) tree.getModel(), LOGGER);
		tree.addPropertyChangeListener(nwl);
		addWorkerButton.addActionListener(new ActionListener() {
			// TODO: Add this functionality to NewWorkerListener
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final WorkerConstructionFrame frame = new WorkerConstructionFrame(idf);
				frame.addPropertyChangeListener(nwl);
				frame.setVisible(true);
			}
		});
		final AddRemovePanel jarp = new AddRemovePanel(false);
		final AddRemovePanel sarp = new AddRemovePanel(false);
		final JTree jobsTree = new JobsTree(this, new PropertyChangeAdapter(
				jarp, "add", "add_job"), new PropertyChangeAdapter(sarp, "add",
				"add_skill"), tree);
		jobsTree.addPropertyChangeListener(this);
		final JSplitPane panelThree = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				true,
				new BorderedPanel().setNorth(
						new JLabel(htmlize("Worker's Jobs and Skills:")))
						.setCenter(new JScrollPane(jobsTree)),
				new BorderedPanel()
						.setCenter(
								new BorderedPanel()
										.setNorth(
												new BorderedPanel()
														.setNorth(
																new JLabel(
																		htmlize("Add a job to the Worker:")))
														.setSouth(jarp))
										.setSouth(
												new BorderedPanel()
														.setNorth(
																new JLabel(
																		htmlize("Add a Skill to the selected Job:")))
														.setSouth(sarp)))
						.setSouth(new SkillAdvancementPanel(this, this)));
		panelThree.setResizeWeight(.3);
		panelThree.setDividerLocation(HALF_WAY);

		final JSplitPane jspThree = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, new BorderedPanel().setNorth(plabel)
						.setCenter(new JScrollPane(tree))
						.setSouth(addWorkerButton), panelThree);
		jspThree.setResizeWeight(HALF_WAY);
		jspThree.setDividerLocation(HALF_WAY);
		setContentPane(jspThree);

		addPropertyChangeListener(this);
		firePropertyChange("map", null, null);
		firePropertyChange("player", null, source.getMap().getPlayers().getCurrentPlayer());
		removePropertyChangeListener(this);

		addPropertyChangeListener(new LevelListener());

		setJMenuBar(new WorkerMenu(ioHandler, this, pch));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Handle a property change.
	 *
	 * @param evt the property-change event to handle
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (!equals(evt.getSource())) {
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
					evt.getNewValue());
		}
	}

	/**
	 * Turn a string into left-aligned HTML.
	 *
	 * @param string a string
	 * @return it wrapped in HTML code that should make it left-aligned.
	 */
	private static String htmlize(final String string) {
		return "<html><p align=\"left\">" + string + "</p></html>";
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
		if (view == null) {
			return component.getPreferredSize(); // NOPMD
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
	private static final class LevelListener implements PropertyChangeListener {
		/**
		 * Constructor.
		 */
		LevelListener() {
			// Needed to give access ...
		}

		/**
		 * The current worker.
		 */
		private UnitMember worker = null;
		/**
		 * The current skill.
		 */
		private Skill skill = null;

		/**
		 * @param evt the property-change event to handle
		 */
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if ("member".equals(evt.getPropertyName())
					&& (evt.getNewValue() instanceof UnitMember || evt
							.getNewValue() == null)) {
				worker = (UnitMember) evt.getNewValue();
			} else if ("skill".equals(evt.getPropertyName())
					&& (evt.getNewValue() instanceof Skill || evt.getNewValue() == null)) {
				skill = (Skill) evt.getNewValue();
			} else if ("level".equals(evt.getPropertyName())) {
				final StringBuilder builder = new StringBuilder();
				builder.append(getName(worker));
				builder.append(" gained a level in ");
				builder.append(getName(skill));
				SystemOut.SYS_OUT.println(builder.toString());
			}
		}

		/**
		 * @param named something that may have a name
		 * @return its name if it has one, "null" if null, or its toString
		 *         otherwise.
		 */
		private static String getName(final Object named) {
			if (named instanceof HasName) {
				return ((HasName) named).getName(); // NOPMD
			} else if (named == null) {
				return "null"; // NOPMD
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
	private static class NewWorkerListener implements PropertyChangeListener {
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
		private Unit selUnit;
		/**
		 * Constructor.
		 * @param treeModel the tree model
		 * @param logger the logger to use for logging
		 */
		NewWorkerListener(final IWorkerTreeModel treeModel, final Logger logger) {
			tmodel = treeModel;
			lgr = logger;
		}
		/**
		 * Handle a property change event.
		 * @param evt the event to handle
		 */
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			if ("selUnit".equalsIgnoreCase(evt.getPropertyName())
					&& (evt.getNewValue() == null || evt.getNewValue() instanceof Unit)) {
				selUnit = (Unit) evt.getNewValue();
			} else if ("worker".equalsIgnoreCase(evt.getPropertyName())
					&& evt.getNewValue() instanceof Worker) {
				if (selUnit == null) {
					lgr.warning("New worker created when no unit selected");
					// FIXME: Warn the user of this, using a dialog or something.
				} else {
					tmodel.addUnitMember(selUnit, (UnitMember) evt.getNewValue());
				}
			}
		}
	}
}
