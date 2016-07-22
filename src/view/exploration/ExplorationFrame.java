package view.exploration;

import controller.map.misc.IOHandler;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import model.exploration.IExplorationModel;
import model.listeners.CompletionListener;
import util.NullCleaner;
import view.util.ISPWindow;

/**
 * The main window for the exploration GUI.
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
public final class ExplorationFrame extends JFrame implements ISPWindow {
	/**
	 * A listener to swap the panels when 'completion' is signalled.
	 *
	 * @author Jonathan Lovelace
	 */
	private static final class SwapCompletionListener implements CompletionListener {
		/**
		 * The layout to tell to swap panels.
		 */
		private final CardLayout layout;
		/**
		 * The component it's laying out.
		 */
		private final Container parent;
		/**
		 * Things to tell to validate their layout before swapping.
		 */
		private final Collection<Component> compList = new ArrayList<>();
		/**
		 * Whether we're *on* the first panel. If we are, we go 'next'; if not, we go
		 * 'first'.
		 */
		private boolean first = true;

		/**
		 * Constructor.
		 *
		 * @param cardLayout    the layout to tell to swap panels
		 * @param parentComponent the component it's laying out
		 * @param components things to tell to validate their layout before swapping
		 */
		protected SwapCompletionListener(final CardLayout cardLayout,
										final Container parentComponent,
										final Component... components) {
			layout = cardLayout;
			parent = parentComponent;
			Stream.of(components).forEach(compList::add);
		}

		/**
		 * Swap panels.
		 */
		@Override
		public void finished() {
			compList.forEach(Component::validate);
			if (first) {
				layout.next(parent);
				first = false;
			} else {
				layout.first(parent);
				first = true;
			}
		}

		/**
		 * @return a String representation of the object
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public String toString() {
			return "SwapCompletionListener";
		}
	}

	/**
	 * @param explorationModel    the exploration model
	 * @param ioHandler Passed to menu constructor
	 */
	public ExplorationFrame(final IExplorationModel explorationModel,
							final IOHandler ioHandler) {
		super("Exploration");
		if (explorationModel.getMapFile().isPresent()) {
			setTitle(explorationModel.getMapFile().get() + " | Exploration");
			getRootPane().putClientProperty("Window.documentFile",
					explorationModel.getMapFile().get().toFile());
		}
		setMinimumSize(new Dimension(768, 480));
		setPreferredSize(new Dimension(1024, 640));
		final CardLayout layout = new CardLayout();
		setLayout(layout);
		final ExplorerSelectingPanel esp = new ExplorerSelectingPanel(explorationModel);
		final ExplorationPanel explorationPanel =
				new ExplorationPanel(explorationModel, esp.getMPDocument());
		explorationModel.addMovementCostListener(explorationPanel);
		explorationModel.addSelectionChangeListener(explorationPanel);
		final CompletionListener swapper =
				new SwapCompletionListener(layout,
												NullCleaner.assertNotNull(
														getContentPane()),
												explorationPanel, esp);
		esp.addCompletionListener(swapper);
		explorationPanel.addCompletionListener(swapper);
		add(esp);
		add(explorationPanel);

		setJMenuBar(new ExplorationMenu(NullCleaner.assertNotNull(ioHandler),
				explorationModel, this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
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
		return "Exploration";
	}
}
