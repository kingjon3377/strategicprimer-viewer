package lovelace.util;

import java.util.function.Consumer;

/**
 * Wrap a no-argument method to pass it to a listener or other caller that wants to pass it a parameter.
 */
public class SilentListener<Type> implements Consumer<Type> {
	private final Runnable method;

	public SilentListener(final Runnable method) {
		this.method = method;
	}

	@Override
	public void accept(final Type item) {
		method.run();
	}

	@Override
	public String toString() {
		return "SilentListener{method=" + method + '}';
	}
}
