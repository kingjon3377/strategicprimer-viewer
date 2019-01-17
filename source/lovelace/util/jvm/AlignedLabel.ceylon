import javax.swing {
    JLabel
}

"A [[JLabel]] that takes its alignment configuration as initializer parameters."
shared class AlignedLabel(String text, Float alignmentX, Float alignmentY)
        extends JLabel(text) {
    super.alignmentX = alignmentX;
    super.alignmentY = alignmentY;
}
