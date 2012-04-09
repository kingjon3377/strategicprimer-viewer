package controller.map;

import util.Warning;

/**
 * An interface for Nodes to implement if they need to do something before the
 * body of canonicalize() runs.
 * 
 * @author Jonathan Lovelace
 */
public interface NeedsExtraCanonicalization {
	/**
	 * Do the extra necessary work.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             on format errors uncovered in this process.
	 */
	void canonicalizeImpl(Warning warner) throws SPFormatException;
}
