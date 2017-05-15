import java.awt {
    Color,
    Dimension,
    Graphics
}

import javax.swing {
    JComponent
}
"The part of the key showing a tile's color."
class KeyElementComponent(Color color, Dimension minimum, Dimension preferred,
        Dimension maximum) extends JComponent() {
    minimumSize = minimum;
    preferredSize = preferred;
    maximumSize = maximum;
    shared actual void paint(Graphics pen) {
        Graphics context = pen.create();
        try {
            context.color = color;
            context.fillRect(0, 0, width, height);
        } finally {
            context.dispose();
        }
    }
}
