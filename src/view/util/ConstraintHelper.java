package view.util;

import java.awt.GridBagConstraints;

/**
 * A helper class so we can specify arguments inline rather than having to build
 * each object manually.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ConstraintHelper extends GridBagConstraints {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -1563269880596989283L;

	/**
	 * Constructor taking just position.
	 * @param col the column
	 * @param row the row
	 */
	public ConstraintHelper(final int col, final int row) {
		super();
		gridx = col;
		gridy = row;
	}
	/**
	 * Constructor taking position and extent
	 * @param col the column
	 * @param row the row
	 * @param width how many columns
	 * @param height how many rows
	 */
	public ConstraintHelper(final int col, final int row, final int width, final int height) {
		this(col, row);
		gridwidth = width;
		gridheight = height;
	}
}
