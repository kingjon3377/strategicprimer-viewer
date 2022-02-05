package lovelace.util;

import javax.swing.DefaultListModel;

/**
 * An extension of the {@link DefaultListModel} class to add an implementation
 * of the {@link Reorderable} interface. For the convenience of callers, the
 * class also takes its initial elements as constructor parameters.
 */
public class ReorderableListModel<Element> extends DefaultListModel<Element> implements Reorderable {
	private static final long serialVersionUID = 1;
	@SafeVarargs
	public ReorderableListModel(final Element... initialElements) {
		for (Element element : initialElements) {
			addElement(element);
		}
	}

	@Override
	public void reorder(final int fromIndex, final int toIndex) {
		if (fromIndex != toIndex) {
			if (fromIndex > toIndex) {
				add(toIndex, remove(fromIndex));
			} else {
				add(toIndex - 1, remove(fromIndex));
			}
		}
	}
}
