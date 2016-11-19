package view.worker;

import controller.map.misc.IDFactoryFiller;
import controller.map.misc.IOHandler;
import java.awt.Dimension;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import model.map.IMapNG;
import model.map.Player;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.JobTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import view.util.BorderedPanel;
import view.util.ISPWindow;
import view.util.ItemAdditionPanel;
import view.util.ListenedButton;

import static view.util.SplitWithWeights.horizontalSplit;
import static view.util.SplitWithWeights.verticalSplit;

/**
 * A GUI to let a user manage workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class AdvancementFrame extends JFrame implements ISPWindow {
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
		final Optional<Path> file = source.getMapFile();
		if (file.isPresent()) {
			setTitle(file.get() + " | Worker Advancement");
			getRootPane().putClientProperty("Window.documentFile",
					file.get().toFile());
		}
		setMinimumSize(new Dimension(640, 480));
		final IMapNG map = source.getMap();
		final Player player = map.getCurrentPlayer();
		final PlayerLabel playerLabel = new PlayerLabel("", player, "'s Units:");
		ioHandler.addPlayerChangeListener(playerLabel);
		final IWorkerTreeModel treeModel = new WorkerTreeModelAlt(player, source);
		final WorkerTree tree =
				WorkerTree.factory(treeModel, map.players(),
						() -> source.getMap().getCurrentTurn(), false);
		ioHandler.addPlayerChangeListener(treeModel);
		final WorkerCreationListener nwl = new WorkerCreationListener(treeModel,
																			IDFactoryFiller
																					.createFactory(
																							source.getMap()));
		tree.addUnitSelectionListener(nwl);
		final JobTreeModel jobsTreeModel = new JobTreeModel();
		tree.addUnitMemberListener(jobsTreeModel);
		final ItemAdditionPanel jobAdditionPanel = new ItemAdditionPanel("job");
		jobAdditionPanel.addAddRemoveListener(jobsTreeModel);
		final ItemAdditionPanel skillAdditionPanel = new ItemAdditionPanel("skill");
		skillAdditionPanel.addAddRemoveListener(jobsTreeModel);
		final LevelListener levelListener = new LevelListener();
		final JobsTree jobsTree = new JobsTree(jobsTreeModel);
		jobsTree.addSkillSelectionListener(levelListener);
		final SkillAdvancementPanel skillAdvancementPanel = new SkillAdvancementPanel();
		jobsTree.addSkillSelectionListener(skillAdvancementPanel);
		skillAdvancementPanel.addLevelGainListener(levelListener);
		final JLabel newJobText = htmlWrapped("Add a job to the Worker:");
		final JLabel newSkillText = htmlWrapped("Add a Skill to the selected Job:");
		setContentPane(horizontalSplit(HALF_WAY, HALF_WAY, BorderedPanel.verticalPanel(playerLabel,
				new JScrollPane(tree),
				new ListenedButton("Add worker to selected unit ...", nwl)),
				verticalSplit(HALF_WAY, RES_WEIGHT, BorderedPanel.verticalPanel(
						htmlWrapped("Worker's Jobs and Skills:"), new JScrollPane(jobsTree),
						null), BorderedPanel.verticalPanel(null, BorderedPanel.verticalPanel(
						BorderedPanel.verticalPanel(newJobText, null, jobAdditionPanel), null,
						BorderedPanel.verticalPanel(newSkillText, null, skillAdditionPanel)),
						skillAdvancementPanel))));

		ioHandler.notifyListeners();

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		ioHandler.addTreeExpansionListener(new TreeExpansionHandler(tree));
		setJMenuBar(new WorkerMenu(ioHandler, this, source));
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
	private static JLabel htmlWrapped(final String paragraph) {
		return new JLabel("<html><p align=\"left\">" + paragraph + "</p></html>");
	}
	/**
	 * Prevent serialization.
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}
	/**
	 * Prevent serialization
	 * @param in ignored
	 * @throws IOException always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings({ "unused", "static-method" })
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Worker Advancement";
	}
}
