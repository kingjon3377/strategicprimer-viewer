package drivers.exploration;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import common.map.fixtures.mobile.IUnit;

import java.awt.Component;
import org.jetbrains.annotations.Nullable;

/* package */ class UnitCellRenderer implements ListCellRenderer<IUnit> {
	private static DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(@Nullable JList<? extends IUnit> list, @Nullable IUnit val,
	                                              int index, boolean isSelected, boolean cellHasFocus) {
		Component retval = DEFAULT_RENDERER.getListCellRendererComponent(list, val, index,
			isSelected, cellHasFocus);
		if (val != null && retval instanceof JLabel) {
			((JLabel) retval).setText(String.format("% (%s)", val.getName(), val.getKind()));
		}
		return retval;
	}
}
