package drivers.common.cli;

import java.util.List;

/**
 * An interface for applets, subcommands that the user chooses between in
 * certqin CLI apps.
 *
 * In Ceylon this interface took a generic type argument Arguments, to allow
 * {@link #invoke} to take arguments while preserving type-safety, but Java does
 * not provide any way to abstract over functions of different arity, and as
 * far as I can tell no implementation actually takes a non-empty arguments
 * list.
 */
public interface Applet {
	/**
	 * Ways that this applet may be invoked.
	 *
	 * In Ceylon the type-system required that this not be empty.
	 */
	List<String> getCommands();

	/**
	 * What this applet does: a description presented to the user.
	 */
	String getDescription();

	/**
	 * What should happen when the user calls for this applet.
	 */
	void invoke();
}
