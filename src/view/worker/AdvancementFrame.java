package view.worker;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import model.map.Player;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.JobTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import view.util.AddRemovePanel;
import view.util.BorderedPanel;
import view.util.ListenedButton;
import view.util.SplitWithWeights;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

/**
 * A GUI to let a user manage workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class AdvancementFrame extends JFrame {
	/**
	 * The text of the relevant button. Can't be private without causing warnings, since
	 * it's used in an inner class.
	 */
	protected static final String NEW_WORKER = "Add worker to selected unit ...";
	/**
	 * Dividers start at half-way.
	 */
	private static final double HALF_WAY = 0.5;
	/**
	 * The resize weight for the main division.
	 */
	private static final double RES_WEIGHT = 0.3;

	/**
	 * Constructor.
	 *
	 * @param source    the model containing the data to work from
	 * @param ioHandler the I/O handler so the menu 'open' item, etc., will work
	 */
	public AdvancementFrame(final IWorkerModel source, final IOHandler ioHandler) {
		super("Worker Advancement");
		if (source.getMapFile().exists()) {
			setTitle(source.getMapFile().getName() + " | Worker Advancement");
			getRootPane().putClientProperty("Window.documentFile",
					source.getMapFile());
		}
		setMinimumSize(new Dimension(640, 480));

		final PlayerChooserHandler pch = new PlayerChooserHandler(this, source);

		final Player player = source.getMap().getCurrentPlayer();
		final PlayerLabel plabel = new PlayerLabel("", player, "'s Units:");
		pch.addPlayerChangeListener(plabel);
		final IWorkerTreeModel wtmodel = new WorkerTreeModelAlt(player, source);
		final WorkerTree tree =
				new WorkerTree(wtmodel, source.getMap().players(), false);
		pch.addPlayerChangeListener(wtmodel);
		final WorkerCreationListener nwl = new WorkerCreationListener(wtmodel,
																			 IDFactoryFiller
																					 .createFactory(
																							 source.getMap()));
		tree.addUnitSelectionListener(nwl);
		final JobTreeModel jtmodel = new JobTreeModel();
		final JobsTree jobsTree = new JobsTree(jtmodel);
		tree.addUnitMemberListener(jtmodel);
		final AddRemovePanel jarp = new AddRemovePanel(false, "job");
		jarp.addAddRemoveListener(jtmodel);
		final AddRemovePanel sarp = new AddRemovePanel(false, "skill");
		sarp.addAddRemoveListener(jtmodel);
		final LevelListener llist = new LevelListener();
		jobsTree.addSkillSelectionListener(llist);
		final SkillAdvancementPanel sapanel = new SkillAdvancementPanel();
		jobsTree.addSkillSelectionListener(sapanel);
		sapanel.addLevelGainListener(llist);
		final JLabel newJobText = htmlize("Add a job to the Worker:");
		final JLabel newSkillText = htmlize("Add a Skill to the selected Job:");
		setContentPane(new SplitWithWeights(HORIZONTAL_SPLIT, HALF_WAY,
												   HALF_WAY,
												   new BorderedPanel(new JScrollPane
																			 (tree),
																			plabel,
																			new
																					ListenedButton(NEW_WORKER,
																									  nwl),

																			null, null),
												   new SplitWithWeights(VERTICAL_SPLIT,
																			   HALF_WAY,
																			   RES_WEIGHT,
																			   new
																					   BorderedPanel(new JScrollPane(jobsTree),
																										htmlize("Worker's Jobs and Skills:"),
																										null,
																										null,
																										null),
																			   new
																					   BorderedPanel(
																										new BorderedPanel(null,
																																 new BorderedPanel(null,
																																						  newJobText,
																																						  jarp,
																																						  null,
																																						  null),
																																 new BorderedPanel(null,
																																						  newSkillText,
																																						  sarp,
																																						  null,
																																						  null),
																																 null,
																																 null),
																										null,
																										sapanel,
																										null,
																										null))));

		pch.notifyListeners();

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		setJMenuBar(new WorkerMenu(ioHandler, this, source,
										  new TreeExpansionHandler(tree)));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Turn a string into left-aligned HTML.
	 *
	 * @param paragraph a string
	 * @return a label, with its text that string wrapped in HTML code that should
	 * make it
	 * left-aligned.
	 */
	private static JLabel htmlize(final String paragraph) {
		return new JLabel("<html><p align=\"left\">" + paragraph + "</p></html>");
	}
}
