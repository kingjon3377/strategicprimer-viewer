import strategicprimer.model.map {
    TileFixture,
    HasImage
}
import java.awt.image {
    BufferedImage
}
import javax.swing.plaf.basic {
    BasicHTML
}
import javax.swing {
    JLabel,
    SwingList=JList,
    ImageIcon,
    DefaultListCellRenderer,
    ListCellRenderer,
    JComponent,
    Icon
}
import javax.swing.text {
    View
}
import java.awt {
    Graphics2D,
    Component,
    Color,
    Dimension
}
import java.io {
    FileNotFoundException,
    IOException
}
import ceylon.collection {
    MutableSet,
    HashSet
}
import java.nio.file {
    NoSuchFileException
}
import ceylon.numeric.float {
    halfEven,
    ceiling
}
import java.lang {
    Types
}
"A cell renderer for tile-details GUIs."
class FixtureCellRenderer satisfies ListCellRenderer<TileFixture> {
    static DefaultListCellRenderer defaultRenderer =
            DefaultListCellRenderer();
    static MutableSet<String> missingFilenames = HashSet<String>();
    static Icon createDefaultFixtureIcon() {
        Integer imageSize = 24;
        BufferedImage temp = BufferedImage(imageSize, imageSize,
            BufferedImage.typeIntArgb);
        Graphics2D pen = temp.createGraphics();
        Color saveColor = pen.color;
        pen.color = Color.\iRED;
        Float margin = 0.15;
        Float pixelMargin = halfEven(imageSize * margin);
        Float afterMargin = halfEven(imageSize * (1.0 - (margin * 2.0)));
        Float cornerRounding = halfEven((imageSize * margin) / 2.0);
        pen.fillRoundRect(pixelMargin.integer + 1, pixelMargin.integer + 1,
            afterMargin.integer, afterMargin.integer, cornerRounding.integer,
            cornerRounding.integer);
        pen.color = saveColor;
        Float newMargin = halfEven((imageSize / 2.0) - (imageSize * margin));
        Float newAfterMargin = halfEven(imageSize * margin * 2.0);
        Float newCorner = halfEven((imageSize * margin) / 2.0);
        pen.fillRoundRect(newMargin.integer + 1, newMargin.integer + 1,
            newAfterMargin.integer, newAfterMargin.integer, newCorner.integer,
            newCorner.integer);
        return ImageIcon(temp);
    }
    "Set a component's height given a fixed with."
    by("http://blog.nobel-joergensen.com/2009/01/18/changing-preferred-size-of-a-html-jlabel/")
    static void setComponentPreferredSize(JComponent component, Integer width) {
        assert (is View view = component.getClientProperty(
            Types.nativeString(BasicHTML.propertyKey)));
        view.setSize(width.float, 0.0);
        Integer wid = ceiling(view.getPreferredSpan(View.xAxis)).integer;
        Integer height = ceiling(view.getPreferredSpan(View.yAxis)).integer;
        component.preferredSize = Dimension(wid, height);
    }
    static Icon defaultFixtureIcon = createDefaultFixtureIcon();
    shared new () { }
    Icon getIcon(HasImage obj) {
        String image = obj.image;
        String actualImage;
        if (image.empty || missingFilenames.contains(image)) {
            actualImage = obj.defaultImage;
        } else {
            actualImage = image;
        }
        if (missingFilenames.contains(actualImage)) {
            return defaultFixtureIcon;
        }
        try {
            return imageLoader.loadIcon(actualImage);
        } catch (FileNotFoundException|NoSuchFileException except) {
            log.error("image file images/``actualImage`` not found");
            log.debug("With stack trace", except);
            missingFilenames.add(actualImage);
            return defaultFixtureIcon;
        } catch (IOException except) {
            log.error("I/O error reading image", except);
            return defaultFixtureIcon;
        }
    }
    shared actual Component getListCellRendererComponent(SwingList<out TileFixture> list,
            TileFixture val, Integer index, Boolean isSelected, Boolean cellHasFocus) {
        assert (is JLabel component = defaultRenderer.getListCellRendererComponent(list,
            val, index, isSelected, cellHasFocus));
        component.text = "<html><p>``val.shortDescription``</p></html>";
        if (is HasImage val) {
            component.icon = getIcon(val);
        } else {
            component.icon = defaultFixtureIcon;
        }
        component.maximumSize = Dimension(component.maximumSize.width.integer,
            (component.maximumSize.height * 2).integer);
        setComponentPreferredSize(component, list.width);
        return component;
    }
}
