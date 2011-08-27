package controller.map.simplexml;

/**
 * An interface for Nodes to implement if they need to do something
 * before the body of canonicalize() runs.
 * @author Jonathan Lovelace
 */
public interface NeedsExtraCanonicalization {
	/**
	 * Do the extra necessary work.
	 * @throws SPFormatException on format errors uncovered in this process.
	 */
	void canonicalizeImpl() throws SPFormatException;
}
