package controller.map.readerng;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import model.map.HasImage;

import org.eclipse.jdt.annotation.Nullable;

import util.EqualsAny;
import util.NullCleaner;
import util.Pair;

/**
 * An intermediate representation between SP objects and XML; this lets us add
 * in the 'row' and 'include' tags, which will then get handled properly when
 * writing the XML out.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class SPIntermediateRepresentation {
	/**
	 * Constructor.
	 *
	 * @param name the tag this represents.
	 */
	public SPIntermediateRepresentation(final String name) {
		tag = name;
		children = new LinkedHashSet<>();
	}

	/**
	 * Secondary constructor.
	 *
	 * @param name the tag this represents
	 * @param attributes a list of attributes to add.
	 */
	@SafeVarargs
	public SPIntermediateRepresentation(final String name,
			final Pair<String, String>... attributes) {
		this(name);
		for (final Pair<String, String> attr : attributes) {
			addAttribute(attr.first(), attr.second());
			// attrs.add(attr);
		}
	}

	/**
	 * The tag this represents.
	 */
	private final String tag;
	/**
	 * The list of attributes.
	 */
	// private final Map<String, String> attrs = new LinkedHashMap<>();
	// private final List<Pair<String, String>> attrs = new ArrayList<>();
	private final List<String> attrs = new ArrayList<>();
	/**
	 * The list of attribute values.
	 */
	private final List<String> vals = new ArrayList<>();

	/**
	 * Add an attribute.
	 *
	 * @param name the name of the attribute
	 * @param value its value
	 */
	public void addAttribute(final String name, final String value) {
		// attrs.put(name, value);
		// attrs.add(Pair.of(name, value));
		if (attrs.contains(name)) {
			vals.set(attrs.indexOf(name), value);
		} else {
			attrs.add(name);
			vals.add(value);
		}
	}

	/**
	 * The set of child tags.
	 */
	private final Set<SPIntermediateRepresentation> children;

	// private final List<SPIntermediateRepresentation> children = new
	// LinkedList<SPIntermediateRepresentation>();
	/**
	 * Add a child tag. If the child's tag is the empty string, we do nothing
	 * instead---this is so we can handle empty tiles more easily.
	 *
	 * @param child the child to add.
	 */
	public void addChild(final SPIntermediateRepresentation child) {
		if (!"".equals(child.tag)) {
			children.add(child);
		}
	}

	/**
	 * Remove an attribute and return its value.
	 *
	 * @param name an attribute name
	 * @return its value, or "" if it's not there
	 */
	private String removeAttribute(final String name) {
		if (attrs.contains(name)) {
			final int index = attrs.indexOf(name);
			attrs.remove(index);
			return NullCleaner.valueOrDefault(vals.remove(index), "");
		} else {
			return "";
		}
		// Pair<String, String> retval = Pair.of(name, "");
		// for (Pair<String, String> pair : attrs) {
		// if (pair.first().equals(name)) {
		// retval = pair;
		// break;
		// }
		// }
		// attrs.remove(retval);
		// return retval.second();
	}

	/**
	 * Write to a Writer, or (if this is an 'include' tag) to its own.
	 *
	 * @param writer the writer to write to
	 * @param indentationLevel how many tabs to indent---normally callers set to
	 *        0, and we increment in recursive calls (except for inclusion,
	 *        where we reset it to 0 again)
	 * @throws IOException on I/O error while writing
	 */
	public void write(final Writer writer, final int indentationLevel)
			throws IOException {
		for (int i = 0; i < indentationLevel; i++) {
			writeIfTagNotEmpty(writer, "\t");
		}
		if ("include".equals(tag)) {
			writeInclude(writer, indentationLevel);
		} else {
			writeIfTagNotEmpty(writer, "<");
			writeIfTagNotEmpty(writer, tag);
			// final String text = "text".equals(tag) ? attrs
			// .remove("text-contents") : "";
			final String text = removeAttribute("text-contents");
			// for (String attr : attrs.keySet()) {
			// for (Pair<String, String> attr : attrs) {
			for (int index = 0; index < attrs.size(); index++) {
				writeIfTagNotEmpty(writer, " ");
				// writeIfTagNotEmpty(writer, attr);
				// writeIfTagNotEmpty(writer, attr.first());
				writeIfTagNotEmpty(writer, attrs.get(index));
				writeIfTagNotEmpty(writer, "=\"");
				// writeIfTagNotEmpty(writer, attrs.get(attr));
				// writeIfTagNotEmpty(writer, attr.second());
				writeIfTagNotEmpty(writer, vals.get(index));
				writeIfTagNotEmpty(writer, "\"");
			}
			if (children.isEmpty()) {
				if (shouldSeparateClosingTag(tag) || !"".equals(text)) {
					writeIfTagNotEmpty(writer, ">");
					writeIfTagNotEmpty(writer, text);
					writeIfTagNotEmpty(writer, "</");
					writeIfTagNotEmpty(writer, tag);
					writeIfTagNotEmpty(writer, ">\n");
				} else {
					writeIfTagNotEmpty(writer, " />\n");
				}
			} else {
				writeIfTagNotEmpty(writer, ">\n");
				for (final SPIntermediateRepresentation child : children) {
					child.write(writer, indentationLevel + 1);
				}
				for (int i = 0; i < indentationLevel; i++) {
					writeIfTagNotEmpty(writer, "\t");
				}
				writeIfTagNotEmpty(writer, "</");
				writeIfTagNotEmpty(writer, tag);
				writeIfTagNotEmpty(writer, ">\n");
			}
		}
	}

	/**
	 * Write an 'include' tag to the Writer, and if we're doing inclusion its
	 * contents to its own.
	 *
	 * @param writer the writer
	 * @param indentationLevel how many tabs to indent if inclusion is disabled
	 * @throws IOException on I/O error while writing
	 */
	private void writeInclude(final Writer writer, final int indentationLevel)
			throws IOException {
		for (final SPIntermediateRepresentation child : children) {
			child.write(writer, indentationLevel);
		}
	}

	/**
	 * @param tag a tag
	 * @return whether it should always have a separate closing tag, even if it
	 *         has no children
	 */
	private static boolean shouldSeparateClosingTag(final String tag) {
		return EqualsAny.equalsAny(tag, "tile", "fortress", "text");
	}

	/**
	 * Write only if the tag isn't the empty string.
	 * 
	 * @param writer
	 *            the Writer to write to
	 * @param string
	 *            the string to write. May be null, in which case nothing is
	 *            written.
	 * @throws IOException
	 *             if I/O error in writing
	 */
	private void writeIfTagNotEmpty(final Writer writer,
			@Nullable final String string) throws IOException {
		if (!tag.isEmpty() && string != null) {
			writer.write(string);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPIntermediateRepresentation";
	}

	/**
	 * If the object's image attribute is the default, null, or empty, do
	 * nothing; if not, add the image attribute to this representation.
	 *
	 * @param obj the object to consider.
	 */
	public void addImageAttribute(final HasImage obj) {
		final String image = obj.getImage();
		if (!image.isEmpty() && !image.equals(obj.getDefaultImage())) {
			addAttribute("image", image);
		}
	}
	/**
	 * Add an ID attribute. This is so we only have to assert that
	 * Integer.toString doesn't return null in one place.
	 * @param id the ID to add
	 */
	public void addIdAttribute(final int id) {
		final String str = Integer.toString(id);
		assert str != null;
		addAttribute("id", str);
	}
}
