package drivers.exploration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import common.map.fixtures.mobile.IUnit;

import java.awt.Component;
import org.jetbrains.annotations.Nullable;

/* package */ class UnitCellRenderer implements ListCellRenderer<IUnit> {
	private static final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(@Nullable final JList<? extends IUnit> list, @Nullable final IUnit val,
	                                              final int index, final boolean isSelected, final boolean cellHasFocus) {
		Component retval = DEFAULT_RENDERER.getListCellRendererComponent(list, val, index,
			isSelected, cellHasFocus);
		if (val != null && retval instanceof JLabel) {
			((JLabel) retval).setText(String.format("% (%s)", val.getName(), val.getKind()));
		}
		return retval;
	}
}