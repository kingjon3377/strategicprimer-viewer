import javax.swing {
    JPopupMenu,
    JMenuItem
}
"A [[JPopupMenu]] that takes its menu items as initializer parameters."
shared class FunctionalPopupMenu extends JPopupMenu {
    shared new (JMenuItem* items) extends JPopupMenu() {
        for (item in items) {
	    // Can't use items.each(add) because add() is overloaded
            add(item);
        }
    }
}
