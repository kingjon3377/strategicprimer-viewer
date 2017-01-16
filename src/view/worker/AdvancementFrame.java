package view.worker;

import controller.map.misc.MenuBroker;
import java.awt.Dimension;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import model.listeners.PlayerChangeListener;
import model.map.IMapNG;
import model.map.Player;
import model.workermgmt.IWorkerModel;
import model.workermgmt.IWorkerTreeModel;
import model.workermgmt.JobTreeModel;
import model.workermgmt.WorkerTreeModelAlt;
import org.eclipse.jdt.annotation.Nullable;
import view.util.BorderedPanel;
import view.util.FormattedLabel;
import view.util.ItemAdditionPanel;
import view.util.ListenedButton;
import view.util.SPFrame;
import view.util.TreeExpansionOrderListener;

import static controller.map.misc.IDFactoryFiller.createFactory;
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
public final class AdvancementFrame extends SPFrame implements PlayerChangeListener {
	/**
	 * Dividers start at half-way.
	 */
	private static final double HALF_WAY = 0.5;
	/**
	 * The resize weight for the main division.
	 */
	private static final double RES_WEIGHT = 0.3;
	/**
	 * The label telling whose units are being listed.
	 */
	private final FormattedLabel playerLabel = new FormattedLabel("%s's Units:", "");
	/**
	 * The model underlying the worker tree.
	 */
	private final IWorkerTreeModel treeModel;
	/**
	 * Constructor.
	 *
	 * @param source    the model containing the data to work from
	 * @param menuHandler the handler of menu items
	 */
	public AdvancementFrame(final IWorkerModel source, final MenuBroker menuHandler) {
		super("Worker Advancement", source.getMapFile(), new Dimension(640, 480));
		final IMapNG map = source.getMap();
		treeModel = new WorkerTreeModelAlt(map.getCurrentPlayer(), source);
		final WorkerTree tree =
				WorkerTree.factory(treeModel, map.players(),
						() -> source.getMap().getCurrentTurn(), false);
		final WorkerCreationListener nwl =
				new WorkerCreationListener(treeModel, createFactory(source.getMap()));
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
		setContentPane(horizontalSplit(HALF_WAY, HALF_WAY,
				BorderedPanel.verticalPanel(playerLabel,
						new JScrollPane(tree),
						new ListenedButton("Add worker to selected unit ...", nwl)),
				verticalSplit(HALF_WAY, RES_WEIGHT, BorderedPanel.verticalPanel(
						htmlWrapped("Worker's Jobs and Skills:"),
						new JScrollPane(jobsTree),
						null),
						BorderedPanel.verticalPanel(null, BorderedPanel.verticalPanel(
								BorderedPanel.verticalPanel(
										htmlWrapped("Add a job to the Worker:"), null,
										jobAdditionPanel), null,
								BorderedPanel.verticalPanel(
										htmlWrapped("Add a Skill to the selected Job:"),
										null, skillAdditionPanel)),
								skillAdvancementPanel))));
		playerChanged(null, map.getCurrentPlayer());

		final TreeExpansionOrderListener expander = new TreeExpansionHandler(tree);
		menuHandler.register(evt -> expander.expandAll(), "expand all");
		menuHandler.register(evt -> expander.collapseAll(), "collapse all");
		menuHandler.register(evt -> expander.expandSome(2), "expand unit kinds");
		expander.expandAll();
		setJMenuBar(new WorkerMenu(menuHandler, this, source));
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
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Get the name of this app.
	 * @return the title of this app
	 */
	@Override
	public String getWindowName() {
		return "Worker Advancement";
	}

	/**
	 * Called when the current player changes.
	 *
	 * @param old       the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		playerLabel.setArgs(newPlayer.getName());
		treeModel.playerChanged(old, newPlayer);
	}
}
