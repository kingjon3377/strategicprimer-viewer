import java.awt {
    Component,
    Dimension,
    Frame
}
import java.awt.event {
    ActionEvent
}
import java.text {
    NumberFormat
}

import javax.swing {
    JButton,
    JTextField,
    JPanel,
    JLabel
}

import lovelace.util.jvm {
    platform,
    BoxAxis,
    listenedButton,
    boxPanel,
    BoxPanel
}

import model.map {
    MapDimensions
}

import strategicprimer.viewer.drivers {
    SPDialog
}
import strategicprimer.viewer.model.map {
    pointFactory
}
class NumberState of valid|nonNumeric|negative|overflow {
    shared new valid { }
    shared new nonNumeric { }
    shared new negative { }
    shared new overflow { }
}
"A dialog to let the user select a tile by coordinates."
SPDialog selectTileDialog(Frame? parentFrame, IViewerModel model) {
    object retval extends SPDialog(parentFrame, "Go To ...") {}
    JLabel mainLabel = JLabel("Coordinates of tile to select:");
    mainLabel.alignmentX = Component.centerAlignment;
    mainLabel.alignmentY = Component.topAlignment;
    NumberFormat numParser = NumberFormat.integerInstance;
    JTextField rowField = JTextField("-1", 4);
    JTextField columnField = JTextField("-1", 4);
    JLabel errorLabel = JLabel(
        "This text should vanish from this label before it appears.");
    NumberState checkNumber(String text, Integer bound) {
        try {
            Integer number = numParser.parse(text).intValue();
            if (number < 0) {
                return NumberState.negative;
            } else if (number > bound) {
                return NumberState.overflow;
            } else {
                return NumberState.valid;
            }
        } catch (ParseException except) {
            log.debug("Non-numeric input", except);
            return NumberState.nonNumeric;
        }
    }
    String getErrorMessage(NumberState state, Integer bound) {
        switch (state)
        case (NumberState.valid) { return ""; }
        case (NumberState.nonNumeric) { return "must be a whole number. "; }
        case (NumberState.negative) { return "must be positive. "; }
        case (NumberState.overflow) { return "must be less than ``bound``."; }
    }
    void handleOK(ActionEvent ignored) {
        String rowText = rowField.text;
        String columnText = columnField.text;
        errorLabel.text = "";
        MapDimensions dimensions = model.mapDimensions;
        NumberState columnState = checkNumber(columnText, dimensions.columns - 1);
        if (columnState != NumberState.valid) {
            errorLabel.text += "Column ``getErrorMessage(columnState,
                dimensions.columns)``";
            columnField.text = "-1";
            columnField.selectAll();
        }
        NumberState rowState = checkNumber(rowText, dimensions.rows - 1);
        if (rowState != NumberState.valid) {
            errorLabel.text += "Row ``getErrorMessage(rowState, dimensions.rows)``";
            rowField.text = "-1";
            rowField.selectAll();
        }
        if (rowState == NumberState.valid, columnState == NumberState.valid) {
            try {
                model.selection = pointFactory(numParser.parse(rowText).intValue(),
                    numParser.parse(columnText).intValue());
            } catch (ParseException except) {
                log.error("Parse failure after we checked input was numeric", except);
            }
            retval.setVisible(false);
            retval.dispose();
        } else {
            retval.pack();
        }
    }
    JPanel contentPane = boxPanel(BoxAxis.pageAxis);
    contentPane.add(mainLabel);
    JPanel&BoxPanel boxPanelObj = boxPanel(BoxAxis.lineAxis);
    boxPanelObj.add(JLabel("Row: "));
    boxPanelObj.add(rowField);
    rowField.setActionCommand("OK");
    rowField.addActionListener(handleOK);
    boxPanelObj.addGlue();
    boxPanelObj.add(JLabel("Column:"));
    boxPanelObj.add(columnField);
    columnField.setActionCommand("OK");
    columnField.addActionListener(handleOK);
    boxPanelObj.addGlue();
    contentPane.add(boxPanelObj);
    contentPane.add(errorLabel);
    errorLabel.text = "";
    errorLabel.minimumSize = Dimension(200, 15);
    errorLabel.alignmentX = Component.centerAlignment;
    errorLabel.alignmentY = Component.topAlignment;
    JPanel&BoxPanel buttonPanel = boxPanel(BoxAxis.lineAxis);
    buttonPanel.addGlue();
    JButton okButton = listenedButton("OK", handleOK);
    JButton cancelButton = listenedButton("Cancel", (ActionEvent event) {
        retval.setVisible(false);
        rowField.text = "-1";
        columnField.text = "-1";
        retval.dispose();
    });
    platform.makeButtonsSegmented(okButton, cancelButton);
    buttonPanel.add(okButton);
    if (!platform.systemIsMac) {
        buttonPanel.addGlue();
    }
    buttonPanel.add(cancelButton);
    buttonPanel.addGlue();
    contentPane.add(buttonPanel);
    retval.contentPane = contentPane;
    retval.pack();
    return retval;
}
