import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * Wrapper around {@link System}.in to prevent it closing.
 */
public final class SystemIn extends FilterInputStream {
	/**
	 * Singleton.
	 */
	public static final InputStream STDIN = new SystemIn();

	/**
	 * Constructor.
	 */
	private SystemIn() {
		super(System.in);
	}

	/**
	 * Do *not* close.
	 */
	@Override
	public void close() {
		// Do nothing
	}
}
