package model.report;

/**
 * A node to replace usages of null.
 *
 * @author Jonathan Lovelace
 *
 */
public final class EmptyReportNode extends AbstractReportNode {
	/**
	 * Let's make this singleton, to reduce object allocations further.
	 */
	public static final EmptyReportNode NULL_NODE = new EmptyReportNode();

	/**
	 * Constructor.
	 */
	private EmptyReportNode() {
		super(null, "");
	}

	/**
	 * @return the empty string
	 */
	@Override
	public String produce() {
		return "";
	}

	/**
	 * @param builder the string-builder used to build the report
	 * @return it, unmodified
	 */
	@Override
	public StringBuilder produce(final StringBuilder builder) {
		return builder;
	}

	/**
	 * @return the number of characters we'll add to the report, namely zero.
	 */
	@Override
	public int size() {
		return 0;
	}

	/**
	 * @param obj an object
	 * @return whether it equals this; all EmptyReportNodes are equal.
	 */
	@Override
	protected boolean equalsImpl(final IReportNode obj) {
		return this == obj || obj instanceof EmptyReportNode;
	}

	/**
	 * @return a constant hash code
	 */
	@Override
	protected int hashCodeImpl() {
		return 0;
	}

}
