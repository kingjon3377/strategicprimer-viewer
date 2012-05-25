package controller.map.readerng;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import util.EqualsAny;
import util.Pair;

/**
 * An intermediate representation between SP objects and XML; this lets us add
 * in the 'row' and 'include' tags, which will then get handled properly when
 * writing the XML out.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SPIntermediateRepresentation {
	/**
	 * The attribute of an include tag specifying what file its
	 * included-by-reference contents are written to.
	 */
	private static final String FILE_ATTR = "file";
	/**
	 * Constructor. 
	 * @param name the tag this represents.
	 */
	public SPIntermediateRepresentation(final String name) {
		tag = name;
	}
	/**
	 * Secondary constructor.
	 * @param name the tag this represents
	 * @param attributes a list of attributes to add.
	 */
	public SPIntermediateRepresentation(final String name, final Pair<String, String>... attributes) {
		this(name);
		for (Pair<String, String> attr : attributes) {
			addAttribute(attr.first(), attr.second());
		}
	}
	/**
	 * The tag this represents.
	 */
	private final String tag;
	/**
	 * The map of attributes.
	 */
	private final Map<String, String> attrs = new LinkedHashMap<String, String>();
	/**
	 * Add an attribute.
	 * @param name the name of the attribute
	 * @param value its value
	 */
	public final void addAttribute(final String name, final String value) {
		attrs.put(name, value);
	}
	/**
	 * The set of child tags.
	 */
	private final Set<SPIntermediateRepresentation> children = new LinkedHashSet<SPIntermediateRepresentation>();
	
	/**
	 * Add a child tag. If the child's tag is the empty string, we do nothing
	 * instead---this is so we can handle empty tiles more easily.
	 * 
	 * @param child
	 *            the child to add.
	 */
	public void addChild(final SPIntermediateRepresentation child) {
		if (!"".equals(child.tag)) {
			children.add(child);
		}
	}
	
	/**
	 * Write to a Writer, or (if this is an 'include' tag) to its own.
	 * 
	 * @param writer
	 *            the writer to write to
	 * @param inclusion
	 *            whether we should use 'include' tags (or just skip them,
	 *            leaving everything in its own file)
	 * @param indentationLevel
	 *            how many tabs to indent---normally callers set to 0, and we
	 *            increment in recursive calls (except for inclusion, where we
	 *            reset it to 0 again)
	 * @throws IOException
	 *             on I/O error while writing
	 */
	public void write(final Writer writer, final boolean inclusion, final int indentationLevel) throws IOException {
		for (int i = 0; i < indentationLevel; i++) {
			writeIfTagNotEmpty(writer, "\t");
		}
		if ("include".equals(tag)) {
			writeInclude(writer, inclusion, indentationLevel);
		} else {
			writeIfTagNotEmpty(writer, "<");
			writeIfTagNotEmpty(writer, tag);
			final String text = "text".equals(tag) ? attrs
					.remove("text-contents") : "";
			for (String attr : attrs.keySet()) {
				writeIfTagNotEmpty(writer, " ");
				writeIfTagNotEmpty(writer, attr);
				writeIfTagNotEmpty(writer, "=\"");
				writeIfTagNotEmpty(writer, attrs.get(attr));
				writeIfTagNotEmpty(writer, "\"");
			}
			if (children.isEmpty()) {
				if (separateClosingTag(tag)) {
					writeIfTagNotEmpty(writer, ">");
					writeIfTagNotEmpty(writer, text);
					writeIfTagNotEmpty(writer, "</");
					writeIfTagNotEmpty(writer, tag);
					writeIfTagNotEmpty(writer, ">");
				} else {
					writeIfTagNotEmpty(writer, " />\n");
				}
			} else {
				writeIfTagNotEmpty(writer, ">\n");
				for (SPIntermediateRepresentation child : children) {
					child.write(writer, inclusion, indentationLevel + 1);
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
	 * Write an 'include' tag to the Writer, and if we're doing inclusion its contents to its own.
	 * @param writer the writer
	 * @param inclusion whether we're doing inclusion
	 * @param indentationLevel how many tabs to indent if inclusion is disabled
	 * @throws IOException on I/O error while writing
	 */
	private void writeInclude(final Writer writer, final boolean inclusion, final int indentationLevel)
			throws IOException {
		if (attrs.containsKey(FILE_ATTR) && inclusion) {
			if ("string:".equals(attrs.get(FILE_ATTR))) {
				final StringWriter swriter = new StringWriter();
				for (SPIntermediateRepresentation child : children) {
					child.write(swriter, inclusion, 0);
				}
				attrs.put(FILE_ATTR, xmlEncode(swriter.toString()));
			} else {
				final FileWriter fwriter = new FileWriter(attrs.get(FILE_ATTR));
				try {
				for (SPIntermediateRepresentation child : children) {
					child.write(fwriter, inclusion, 0);
				}
				} finally {
					fwriter.close();
				}
			}
			writeIfTagNotEmpty(writer, "<include file=\"");
			writeIfTagNotEmpty(writer, attrs.get(FILE_ATTR));
			writeIfTagNotEmpty(writer, "\" />");
		} else {
			for (SPIntermediateRepresentation child : children) {
				child.write(writer, inclusion, indentationLevel);
			}
		}
	}
	/**
	 * Escape a string for embedding in XML.
	 * @param string the string to escape
	 * @return it with XML-sensitive characters escaped
	 */
	private static String xmlEncode(final String string) {
		return string.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;");
	}
	/**
	 * @param tag a tag
	 * @return whether it should always have a separate closing tag, even if it has no children
	 */
	private static boolean separateClosingTag(final String tag) {
		return EqualsAny.equalsAny(tag, "tile", "fortress", "text");
	}
	/**
	 * Write only if the tag isn't the empty string.
	 * @param writer the Writer to write to
	 * @param string the string to write
	 * @throws IOException if I/O error in writing
	 */
	private void writeIfTagNotEmpty(final Writer writer, final String string) throws IOException {
		if (!tag.isEmpty()) {
			writer.write(string);
		}
	}
}
