package drivers.common;

/**
 * Possible numbers of (non-option) parameters a driver might shared want.
 *
 * TODO: Helper methods should probably go into this class, right?
 */
public enum ParamCount {
	/**
	 * None at all.
	 */
	None,

	/**
	 * Exactly one.
	 */
	One,

	/**
	 * Exactly two.
	 */
	Two,

	/**
	 * One or more.
	 */
	AtLeastOne,

	/**
	 * Two or more.
	 */
	AtLeastTwo,

	/**
	 * Zero or more.
	 */
	AnyNumber
}
