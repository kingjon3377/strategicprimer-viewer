package view.exploration;

import model.exploration.IExplorationModel;

/**
 * A CLI to help running exploration. Now separated from the "driver" bits, to
 * simplify things.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationCLI {
	/**
	 * The exploration model we use.
	 */
	private final IExplorationModel model;
	/**
	 * @param emodel the exploration model to use
	 */
	public ExplorationCLI(final IExplorationModel emodel) {
		model = emodel;
	}
}
