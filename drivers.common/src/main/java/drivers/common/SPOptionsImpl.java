package drivers.common;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The command-line options passed by the user. At this point we assume that if
 * any option is passed to an app more than once, the subsequent option
 * overrides the previous, and any option passed without argument has an
 * implied argument of "true".
 *
 * TODO: Make class final?
 */
public class SPOptionsImpl implements SPOptions {
	private final Map<String, String> options = new HashMap<>();

	// TODO: Take Map<String, String> instead and use options.putAll()?
	@SafeVarargs
	public SPOptionsImpl(final Map.Entry<String, String>... existing) {
		for (Map.Entry<String, String> entry : existing) {
			options.put(entry.getKey(), entry.getValue());
		}
	}

	public void addOption(final String option) {
		addOption(option, "true");
	}

	public void addOption(final String option, final String argument) {
		if ("false".equals(argument)) {
			options.remove(option);
		} else {
			options.put(option, argument);
		}
	}

	@Override
	public boolean hasOption(final String option) {
		return options.containsKey(option);
	}

	@Override
	public String getArgument(final String option) {
		return options.getOrDefault(option, "false");
	}

	@Override
	public SPOptionsImpl copy() {
		SPOptionsImpl retval = new SPOptionsImpl();
		retval.options.putAll(options);
		return retval;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : options.entrySet()) {
			builder.append(entry.getKey());
			if (!"true".equals(entry.getValue())) {
				builder.append('=').append(entry.getValue());
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return options.entrySet().iterator();
	}
}
