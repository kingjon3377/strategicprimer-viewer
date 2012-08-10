package model.map;

/**
 * A class to centralize the previously-duplicated code managing XMLWritables'
 * files.
 *
 * FIXME: This is really a hack; since submaps are no longer on the roadmap, do
 * we need multi-file writing support? If not, the "file" parts of XMLWritable
 * can be entirely removed, and the serialization code significantly simplified.
 * But remember to add "implements XMLWritable" back to all this class's
 * descendants if we get rid of it ...
 *
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0011:
public abstract class XMLWritableImpl implements XMLWritable { // NOPMD
	/**
	 * Default constructor.
	 */
	protected XMLWritableImpl() {
		this("");
	}
	/**
	 * @param file the file we should be written to.
	 */
	protected XMLWritableImpl(final String file) {
		filename = file;
	}
	/**
	 * The filename this should be written to.
	 */
	private String filename;
	/**
	 * @return the filename this should be written to
	 */
	@Override
	public String getFile() {
		return filename;
	}
	/**
	 * @param file the filename this should be written to
	 */
	@Override
	public void setFile(final String file) {
		filename = file;
	}

}
