import java.awt {
    Component
}
import javax.swing {
    JPanel
}
"A [[JPanel]] using the default [[java.awt::FlowLayout]] and taking children to add as
 initializer parameters."
shared class FlowPanel(Component* components) extends JPanel() {
    for (component in components) {
	// Can't use items.each(add) because add() is overloaded
        add(component);
    }
}
