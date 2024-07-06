package drivers.exploration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import legacy.map.fixtures.mobile.IUnit;

import java.awt.Component;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/* package */ class UnitCellRenderer implements ListCellRenderer<IUnit> {
	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(final @Nullable JList<? extends IUnit> list,
	                                              final @Nullable IUnit val, final int index, final boolean isSelected,
	                                              final boolean cellHasFocus) {
		final Component retval = DEFAULT_RENDERER.getListCellRendererComponent(list, val, index,
				isSelected, cellHasFocus);
		if (!Objects.isNull(val) && retval instanceof final JLabel label) {
			label.setText("%s (%s)".formatted(val.getName(), val.getKind()));
		}
		return retval;
	}
}
