package view.util;

import java.io.PrintStream;
/**
 * A class to get around FindBugs' insistence that System.out is always null.
 * @author Jonathan Lovelace
 *
 */
public final class SystemOut extends PrintStream {
	/**
	 * Constructor.
	 */
	private SystemOut() {
		super(System.out);
	}
	/**
	 * The singleton object.
	 */
	public static final SystemOut SYS_OUT = new SystemOut(); // $codepro.audit.disable closeWhereCreated
	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "SystemOut";
	}

}
