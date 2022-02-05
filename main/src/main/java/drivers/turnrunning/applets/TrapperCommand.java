package drivers.turnrunning.applets;

import common.map.HasName;

/**
 * Possible actions a trapper can take. TODO: move into the class that uses
 * this; it was top-level in Ceylon because otherwise code couldn't {@code
 * switch} on the values.
 */
/* package */ enum TrapperCommand implements HasName {
	SetTrap("Set or reset a trap"),
	Check("Check a trap"),
	Move("Move to another trap"),
	EasyReset("Reset a foothold trap, e.g."),
	Quit("Quit");

	private TrapperCommand(final String name) {
		this.name = name;
	}

	private final String name;

	@Override
	public String getName() {
		return name;
	}
}
