import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import java.awt.event {
    ActionEvent
}
import ceylon.language.meta {
    classDeclaration
}
import java.lang {
    JInteger=Integer
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import java.awt {
    Dimension,
    Component
}
import strategicprimer.model.common.map {
    IMutableMapNG,
    Player,
    PlayerImpl
}
import strategicprimer.viewer.drivers {
    PlayerChangeMenuListener,
    IOHandler
}
import strategicprimer.model.common.map.fixtures {
    IMutableResourcePile,
    Quantity,
    ResourcePileImpl,
    Implement
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import strategicprimer.viewer.drivers.worker_mgmt {
    workerMenu
}
import strategicprimer.drivers.common {
    PlayerChangeListener,
    SPOptions,
    MultiMapGUIDriver,
    DriverFailedException
}

import strategicprimer.drivers.gui.common {
    MenuBroker,
    SPFrame,
    SPFileChooser,
    WindowCloseListener
}
import lovelace.util.common {
    defer,
    PathWrapper
}
import javax.swing {
    SpinnerNumberModel,
    JTextField,
    JPanel,
    JScrollPane,
    JComponent,
    JLabel,
    JSpinner,
    SwingUtilities
}
import lovelace.util.jvm {
    centeredHorizontalBox,
    ListenedButton,
    StreamingLabel,
    BoxPanel,
    FileChooser,
    InterpolatedLabel,
    BoxAxis,
    boxPanel,
    BorderedPanel,
    verticalSplit,
    MemoizedComboBox
}
import ceylon.decimal {
    parseDecimal
}

class ResourceAddingGUI satisfies MultiMapGUIDriver {
    static JPanel pairPanel(Component first, Component second) =>
        BorderedPanel.verticalPanel(first, null, second);

    static String resourceLabelText(Player player) => "Add resource for ``player.name``:";
    static String equipmentLabelText(Player player) =>
        "Add equipment for ``player.name``:";

    static String css = """color:black; margin-bottom: 0.5em; margin-top: 0.5em;""";
    static void logAddition(StreamingLabel logLabel, Player currentPlayer, String addend)
        => logLabel.append(
        "<p style=\"``css``\">Added ``addend`` for ``currentPlayer.name``</p>");
    static String errorCSS = """color:red; margin-bottom: 0.5em; margin-top: 0.5em;""";
    static void logError(StreamingLabel logLabel)(String message) =>
        logLabel.append("<p style=\"``errorCSS``\">``message``</p>");

    static void addListenerToField(JSpinner field, Anything(ActionEvent) listener, StreamingLabel logLabel) {
        if (is JTextField editor = field.editor) {
            editor.addActionListener(listener);
        } else if (is JSpinner.DefaultEditor editor = field.editor) {
            editor.textField.addActionListener(listener);
        } else {
            logLabel.append("Spinner's editor wasn't a text field, but a ");
            logLabel.appendLine(classDeclaration(field.editor).string);
        }
    }


    shared actual ResourceManagementDriverModel model;
    ICLIHelper cli;
    shared actual SPOptions options;
    shared new (ICLIHelper cli, SPOptions options, ResourceManagementDriverModel model) {
        this.cli = cli;
        this.options = options;
        this.model = model;
    }

    "A window to let the user enter resources etc. Note that this is not a dialog to enter
     one resource and close."
    class ResourceAddingFrame(Anything(ActionEvent) menuHandler)
            extends SPFrame("Resource Entry", outer, null, true) satisfies PlayerChangeListener {
        IDRegistrar idf = createIDFactory(model.allMaps);
        variable Player currentPlayer = PlayerImpl(-1, "");
        JPanel&BoxPanel mainPanel = boxPanel(BoxAxis.pageAxis);
        InterpolatedLabel<[Player]> resourceLabel =
            InterpolatedLabel<[Player]>(resourceLabelText, [currentPlayer]);

        mainPanel.add(resourceLabel);
        JPanel resourcePanel = boxPanel(BoxAxis.lineAxis);
        StreamingLabel logLabel = StreamingLabel();

        MemoizedComboBox resourceKindBox = MemoizedComboBox(logError(logLabel));
        resourcePanel.add(pairPanel(JLabel("General Category"), resourceKindBox));

        // If we set the maximum high at this point, the fields would try to be
        // unneccessarily large. I'm not sure that setting it low at first helps, though.
        SpinnerNumberModel resourceCreatedModel = SpinnerNumberModel(-1, -1, 2000, 1);
        JSpinner creationSpinner = JSpinner(resourceCreatedModel);
        resourcePanel.add(pairPanel(JLabel("Turn created"), creationSpinner));

        MemoizedComboBox resourceBox = MemoizedComboBox(logError(logLabel));
        resourcePanel.add(pairPanel(JLabel("Specific Resource"), resourceBox));

        SpinnerNumberModel resourceQuantityModel = SpinnerNumberModel(0, 0, 2000, 1);
        JSpinner resourceQuantitySpinner = JSpinner(resourceQuantityModel);
        resourcePanel.add(pairPanel(JLabel("Quantity"), resourceQuantitySpinner));

        MemoizedComboBox resourceUnitsBox = MemoizedComboBox(logError(logLabel));
        resourcePanel.add(pairPanel(JLabel("Units"), resourceUnitsBox));

        variable Boolean playerIsDefault = true;
        void confirmPlayer() {
            if (playerIsDefault, currentPlayer.name.trimmed.empty) {
                menuHandler(ActionEvent(mainPanel, 1, "change current player"));
            }
            playerIsDefault = false;
        }

        void resourceListener(ActionEvent ignored) { // Param required for fields, below
            confirmPlayer();
            String kind = resourceKindBox.selectedString;
            String resource = resourceBox.selectedString;
            String units = resourceUnitsBox.selectedString;
            if (kind.empty) {
                resourceKindBox.requestFocusInWindow();
                return;
            } else if (resource.empty) {
                resourceBox.requestFocusInWindow();
                return;
            } else if (units.empty) {
                resourceUnitsBox.requestFocusInWindow();
                return;
            }

            if (exists qty = parseDecimal(resourceQuantityModel.number.string)) {
                IMutableResourcePile pile = ResourcePileImpl(idf.createID(), kind, resource,
                    Quantity(qty, units));
                pile.created = resourceCreatedModel.number.intValue();
                model.addResource(pile, currentPlayer);
                logAddition(logLabel, currentPlayer, pile.string);
                for (box in [ resourceKindBox, resourceBox, resourceUnitsBox ]) {
                    box.checkAndClear();
                }
                resourceCreatedModel.\ivalue = JInteger.valueOf(-1);
                resourceQuantityModel.\ivalue = JInteger.valueOf(0);
            } else {
                logLabel.appendLine("Failed to convert quantity into the form we need.");
            }
        }

        resourcePanel.add(pairPanel(JLabel(""),
            ListenedButton("Add Resource", resourceListener)));
        resourceUnitsBox.addSubmitListener(resourceListener);

        addListenerToField(creationSpinner, resourceListener, logLabel);
        addListenerToField(resourceQuantitySpinner, resourceListener, logLabel);

        resourceBox.addSubmitListener(resourceListener);
        resourceKindBox.addSubmitListener(resourceListener);
        mainPanel.add(resourcePanel);

        mainPanel.addGlue();
        InterpolatedLabel<[Player]>
        implementLabel = InterpolatedLabel<[Player]>(equipmentLabelText, [currentPlayer]);
        mainPanel.add(implementLabel);

        SpinnerNumberModel implementQuantityModel = SpinnerNumberModel(1, 1, 2000, 1);
        JSpinner implementQuantityField = JSpinner(implementQuantityModel);
        MemoizedComboBox implementKindBox = MemoizedComboBox(logLabel.append);
        void implementListener(ActionEvent ignored) { // Param required for fields, below
            confirmPlayer();
            String kind = implementKindBox.selectedString;
            if (kind.empty) {
                implementKindBox.requestFocusInWindow();
                return;
            }
            Integer quantity = implementQuantityModel.number.intValue();
            model.addResource(Implement(kind, idf.createID(), quantity), currentPlayer);
            logAddition(logLabel, currentPlayer, "``quantity`` x ``kind``");
            implementQuantityModel.\ivalue = JInteger.valueOf(1);
            implementKindBox.checkAndClear();
            implementQuantityField.requestFocusInWindow();
        }

        implementKindBox.addSubmitListener(implementListener);
        addListenerToField(implementQuantityField, implementListener, logLabel);

        mainPanel.add(centeredHorizontalBox(implementQuantityField,
            implementKindBox, ListenedButton("Add Equipment", implementListener)));
        mainPanel.addGlue();
        JScrollPane scrolledLog = JScrollPane(logLabel);
        scrolledLog.minimumSize = logLabel.minimumSize;

        shared actual void playerChanged(Player? old, Player newPlayer) {
            currentPlayer = newPlayer;
            resourceLabel.arguments = [currentPlayer];
            implementLabel.arguments = [currentPlayer];
        }

        shared actual void acceptDroppedFile(PathWrapper file) =>
            model.addSubordinateMap(mapIOHelper.readMap(file));

        add(verticalSplit(mainPanel, scrolledLog, 0.2, 0.1));
        jMenuBar = workerMenu(menuHandler, mainPanel, outer);
        pack();
        logLabel.minimumSize = Dimension(width - 20, 50);
        JComponent temp = logLabel;
        temp.preferredSize = Dimension(width, 100);
        JInteger maximum = JInteger.valueOf(runtime.maxArraySize);
        resourceCreatedModel.maximum = maximum;
        resourceQuantityModel.maximum = maximum;
        implementQuantityModel.maximum = maximum;
    }

    void startDriverImpl(PlayerChangeMenuListener pcml, MenuBroker menuHandler) {
        value frame = ResourceAddingFrame(menuHandler.actionPerformed);
        frame.addWindowListener(WindowCloseListener(menuHandler.actionPerformed));
        menuHandler.registerWindowShower(
            aboutDialog(frame, frame.windowName), "about");
        pcml.addPlayerChangeListener(frame);
        frame.showWindow();
    }

    shared actual void startDriver() {
        PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(this), "load", "save", "save as", "new",
            "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer", "close", "quit");
        menuHandler.register(pcml, "change current player");
        SwingUtilities.invokeLater(defer(startDriverImpl, [pcml, menuHandler]));
    }

    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }

    shared actual void open(IMutableMapNG map) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(ResourceAddingGUI.startDriver,
                ResourceAddingGUI), [cli, options,
                ResourceManagementDriverModel.fromMap(map)]));
        } else {
            model.setMap(map);
        }
    }
}
