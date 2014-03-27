package view.exploration;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import model.exploration.ExplorationModel;
import model.listeners.CompletionListener;
import controller.map.misc.MultiIOHandler;

/**
 * The main window for the exploration GUI.
 *
 * @author Jonathan Lovelace
 */
public class ExplorationFrame extends JFrame {
	/**
	 * The exploration model.
	 */
	protected final ExplorationModel model;
	/**
	 * A listener to swap the panels when 'completion' is signalled.
	 * @author Jonathan Lovelace
	 */
	private static class SwapCompletionListener implements CompletionListener {
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
		private final List<Component> compList = new ArrayList<>();
		/**
		 * Whether we're *on* the first panel. If we are, we go 'next'; if not,
		 * we go 'first'.
		 */
		private boolean first = true;
		/**
		 * Constructor.
		 * @param clayout the layout to tell to swap panels
		 * @param parentComp the component it's laying out
		 * @param components things to tell to validate their layout before swapping
		 */
		protected SwapCompletionListener(final CardLayout clayout,
				final Container parentComp, final Component... components) {
			layout = clayout;
			parent = parentComp;
			for (final Component component : components) {
				if (component != null) {
					compList.add(component);
				}
			}
		}
		/**
		 * @param end ignored
		 */
		@Override
		public void stopWaitingOn(final boolean end) {
			for (final Component component : compList) {
				component.validate();
			}
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
		@Override
		public String toString() {
			return "SwapCompletionListener";
		}
	}
	/**
	 * @param emodel the exploration model
	 * @param ioHandler Passed to menu constructor
	 */
	public ExplorationFrame(final ExplorationModel emodel,
			final MultiIOHandler ioHandler) {
		super("Strategic Primer Exploration");
		model = emodel;
		setMinimumSize(new Dimension(768, 480));
		setPreferredSize(new Dimension(1024, 640));
		final Container outer = getContentPane();
		assert outer != null;
		final CardLayout layout = new CardLayout();
		setLayout(layout);
		final ExplorerSelectingPanel esp = new ExplorerSelectingPanel(emodel);
		final ExplorationPanel explorationPanel = new ExplorationPanel(emodel,
				esp.getMPDocument());
		emodel.addMovementCostListener(explorationPanel);
		emodel.addSelectionChangeListener(explorationPanel);
		final SwapCompletionListener swapper = new SwapCompletionListener(
				layout, outer, explorationPanel, esp);
		esp.addCompletionListener(swapper);
		explorationPanel.addCompletionListener(swapper);
		add(esp);
		add(explorationPanel);

		setJMenuBar(new ExplorationMenu(ioHandler, emodel, this));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}
}
