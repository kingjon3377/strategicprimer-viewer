import javax.swing {
    JPopupMenu,
    JMenuItem
}
"A [[JPopupMenu]] that takes its menu items as initializer parameters."
shared class FunctionalPopupMenu extends JPopupMenu {
    shared new (JMenuItem* items) extends JPopupMenu() {
        for (item in items) {
            add(item);
        }
    }
}
