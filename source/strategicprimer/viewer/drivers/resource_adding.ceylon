import ceylon.collection {
    HashMap,
    MutableSet,
    MutableMap,
    HashSet,
    MutableList,
    ArrayList
}
import ceylon.language.meta {
    classDeclaration
}
import ceylon.decimal {
    parseDecimal
}

import java.awt {
    Component,
    Dimension
}
import java.awt.event {
    ActionEvent
}
import java.lang {
    JInteger=Integer,
    JString=String
}

import javax.swing {
    SwingUtilities,
    SpinnerNumberModel,
    JSpinner,
    JLabel,
    JPanel,
    JTextField,
    JScrollPane,
    JComponent
}

import lovelace.util.jvm {
    listenedButton,
    ImprovedComboBox,
    StreamingLabel,
    centeredHorizontalBox,
    BoxAxis,
    BoxPanel,
    boxPanel,
    verticalSplit,
    FileChooser,
    InterpolatedLabel
}

import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel,
    SPOptions,
    IDriverUsage,
    DriverFailedException,
    ParamCount,
    DriverUsage,
    PlayerChangeListener,
    CLIDriver,
    GUIDriver,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver,
    GUIDriverFactory,
    MultiMapGUIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    Player,
    PlayerImpl,
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Implement,
    FortressMember,
    Quantity
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.viewer.drivers.worker_mgmt {
    workerMenu
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.drivers.gui.common {
    SPFrame,
    WindowCloseListener,
    MenuBroker,
    SPFileChooser
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap
}
import strategicprimer.drivers.gui.common.about {
    aboutDialog
}
import lovelace.util.common {
    PathWrapper,
    defer
}

"A driver model for resource-entering drivers."
class ResourceManagementDriverModel extends SimpleMultiMapModel {
    shared new fromMap(IMutableMapNG map, PathWrapper? file) extends
        SimpleMultiMapModel(map, file) { }
    shared new fromDriverModel(IDriverModel driverModel) extends
        SimpleMultiMapModel.copyConstructor(driverModel) { }

    "All the players in all the maps."
    shared {Player*} players =>
            allMaps.map(Entry.key).flatMap(IMapNG.players).distinct;

    "Add a resource to a player's HQ."
    shared void addResource(FortressMember resource, Player player) {
        for (map->_ in allMaps) {
            Player mapPlayer = map.currentPlayer;
            if (mapPlayer.independent || mapPlayer.playerId < 0 ||
                    mapPlayer.playerId == player.playerId) {
                addResourceToMap(resource.copy(false), map, player);
                setModifiedFlag(map, true);
            } // Else log why we're skipping the map
        }
    }

    "Add a resource to a player's HQ in a particular map."
    shared void addResourceToMap(FortressMember resource, IMapNG map, Player player) {
        for (fixture in map.fixtures.map(Entry.item).narrow<Fortress>()) {
            if ("HQ" == fixture.name, player.playerId == fixture.owner.playerId) {
                fixture.addMember(resource);
            }
        }
    }

    "Get the current player. If none is current, returns null."
    shared Player? currentPlayer => players.find(Player.current);
}

"A factory for a driver to let the user enter a player's resources and equipment."
service(`interface DriverFactory`)
shared class ResourceAddingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-d", "--add-resource"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            return ResourceAddingCLI(cli, options, model);
        } else {
            return createDriver(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ResourceManagementDriverModel.fromMap(map, path);
}

"A driver to let the user enter a player's resources and equipment."
class ResourceAddingCLI(ICLIHelper cli, SPOptions options, model) satisfies CLIDriver {
    shared actual ResourceManagementDriverModel model;

    MutableSet<String> resourceKinds = HashSet<String>();
    MutableMultimap<String, String> resourceContents =
            HashMultimap<String, String>();
    MutableMap<String, String> resourceUnits = HashMap<String, String>();

    "Ask the user to choose or enter a resource kind."
    String getResourceKind() {
        String[] list = resourceKinds.sequence();
        value choice = cli.chooseStringFromList(list, "Possible kinds of resources:",
            "No resource kinds entered yet", "Chosen kind: ", false);
        if (exists retval = choice.item) {
            return retval;
        } else {
            String retval = cli.inputString("Resource kind to use: ");
            resourceKinds.add(retval);
            return retval;
        }
    }

    "Ask the user to choose or enter a resource-content-type for a given resource kind."
    String getResourceContents(String kind) {
        String[] list = resourceContents.get(kind).sequence();
        value num->item = cli.chooseStringFromList(list,
            "Possible resources in the ``kind`` category`", "No resources entered yet",
            "Choose resource: ", false);
        if (exists item) {
            return item;
        } else {
            String retval = cli.inputString("Resource to use: ");
            resourceContents.put(kind, retval);
            return retval;
        }
    }

    "Ask the user to choose units for a type of resource."
    String getResourceUnits(String resource) {
        if (exists unit = resourceUnits[resource],
                cli.inputBooleanInSeries(
                    "Is ``unit`` the correct unit for ``resource``? ",
                    "correct;``unit``;``resource``")) {
            return unit;
        } else {
            String retval = cli.inputString("Unit to use for ``resource``: ");
            resourceUnits[resource] = retval;
            return retval;
        }
    }

    "Ask the user to enter a resource."
    void enterResource(IDRegistrar idf, Player player) {
        String kind = getResourceKind();
        String origContents = getResourceContents(kind);
        String units = getResourceUnits(origContents);
        String contents;
        if (cli.inputBooleanInSeries("Qualify the particular resource with a prefix? ",
                "prefix " + origContents)) {
            contents = "``cli.inputString("Prefix to use: ").trimmed`` ``origContents``";
        } else {
            contents = origContents;
        }
        if (exists quantity = cli.inputDecimal("Quantity in ``units``?")) {
            model.addResource(ResourcePile(idf.createID(), kind, contents, Quantity(
                quantity, units)), player);
        }
    }

    "Ask the user to enter an Implement (a piece of equipment)"
    void enterImplement(IDRegistrar idf, Player player) {
        String kind = cli.inputString("Kind of equipment: ");
        Integer count;
        if (cli.inputBooleanInSeries("Add more than one? ")) {
            count = cli.inputNumber("Number to add: ") else 0;
        } else {
            count = 1;
        }
        model.addResource(Implement(kind, idf.createID(), count), player);
    }

    shared actual void startDriver() {
        MutableList<Player> players = ArrayList { elements = model.players; };
        IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));
        while (!players.empty, exists chosen = cli.chooseFromList(players, // TODO: Add a loopOnPlayers() helper method to CLIDriver interface, since there are several disparate CLI drivers that do that.
                "Players in the maps:", "No players found.",
                "Player to add resources for: ", false).item) {
            players.remove(chosen);
            while (cli.inputBoolean("Keep going? ")) {
                if (cli.inputBooleanInSeries(
                    "Enter a (quantified) resource? ")) {
                    enterResource(idf, chosen);
                } else if (cli.inputBooleanInSeries(
                    "Enter equipment etc.? ")) {
                    enterImplement(idf, chosen);
                }
            }
            if (!cli.inputBoolean("Choose another player?")) {
                break;
            }
        }
    }
}

"A factory for the resource-adding GUI app."
service(`interface DriverFactory`)
shared class ResourceAddingGUIFactory() satisfies GUIDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        invocations = ["-d", "--add-resource"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps";
        includeInCLIList = false;
        includeInGUIList = true;
        supportedOptions = [ "--current-turn=NN" ];
    };

    "Ask the user to choose a file or files."
    shared actual {PathWrapper*} askUserForFiles() {
        try {
            return SPFileChooser.open(null).files;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }

    shared actual GUIDriver createDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            return ResourceAddingGUI(cli, options, model);
        } else {
            return createDriver(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ResourceManagementDriverModel.fromMap(map, path);
}

class ResourceAddingGUI satisfies MultiMapGUIDriver {
    "Extends [[ImprovedComboBox]] to keep a running collection of values."
    static class UpdatedComboBox(Anything(String) logger) extends ImprovedComboBox<String>() {
        "The values we've had in the past."
        MutableSet<String> values = HashSet<String>();
        "Clear the combo box, but if its value was one we haven't had previously, add it
         to the drop-down list."
        shared void checkAndClear() {
            assert (is String temp = selectedItem);
            String item = temp.trimmed;
            if (!values.contains(item)) {
                values.add(item);
                addItem(item);
            }
            selectedItem = null;
        }

        shared actual Object? selectedItem {
            Anything retval = super.selectedItem;
            if (is String retval) {
                return retval.trimmed;
            } else if (exists retval) {
                return retval.string.trimmed;
            } else {
                return "";
            }
        }

        assign selectedItem {
            if (is String|JString selectedItem) {
                super.selectedItem = selectedItem;
            } else {
                logger("Failed to set selectedItem: must be a String.");
            }
        }

        shared String selectedString {
            if (is JTextField inner = editor.editorComponent) {
                value text = inner.text;
                if (!text.empty) {
                    selectedItem = text;
                    return text;
                }
            }
            assert (is String retval = selectedItem);
            return retval;
        }

        shared void addSubmitListener(Anything(ActionEvent) listener) {
            value inner = editor.editorComponent;
            if (is JTextField inner) {
                inner.addActionListener(listener);
            } else {
                logger("Editor wasn't a text field, but a ``
                    classDeclaration(inner)``");
            }
        }
    }

    static JPanel pairPanel(Component first, Component second) { // TODO: Use a better layout than BoxLayout: try BorderedPanel.horizontalLine
        JPanel&BoxPanel panel = boxPanel(BoxAxis.pageAxis);
        panel.addGlue();
        panel.add(first);
        panel.addGlue();
        panel.add(second);
        panel.addGlue();
        return panel;
    }

    static String resourceLabelText(Player player) => "Add resource for ``player.name``:";
    static String equipmentLabelText(Player player) => "Add equipment for ``player.name``:";

    shared actual ResourceManagementDriverModel model;
    ICLIHelper cli;
    SPOptions options;
    shared new (ICLIHelper cli, SPOptions options, ResourceManagementDriverModel model) {
        this.cli = cli;
        this.options = options;
        this.model = model;
    }

    "A window to let the user enter resources etc. Note that this is not a dialog to enter
     one resource and close." // TODO: Move methods embedded in this method to top level of class
    SPFrame&PlayerChangeListener resourceAddingFrame(Anything(ActionEvent) menuHandler) {
        IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));
        variable Player currentPlayer = PlayerImpl(-1, "");
        JPanel&BoxPanel mainPanel = boxPanel(BoxAxis.pageAxis);
        InterpolatedLabel<[Player]> resourceLabel =
                InterpolatedLabel<[Player]>(resourceLabelText, [currentPlayer]);

        mainPanel.add(resourceLabel);
        JPanel resourcePanel = boxPanel(BoxAxis.lineAxis);
        StreamingLabel logLabel = StreamingLabel();
        String css = """color:black; margin-bottom: 0.5em; margin-top: 0.5em;""";
        void logAddition(String addend) => logLabel.append(
            "<p style=\"``css``\">Added ``addend`` for ``currentPlayer.name``</p>");

        String errorCSS = """color:red; margin-bottom: 0.5em; margin-top: 0.5em;""";
        void logError(String message) =>
                logLabel.append("<p style=\"``errorCSS``\">``message``</p>");

        UpdatedComboBox resourceKindBox = UpdatedComboBox(logError);
        resourcePanel.add(pairPanel(JLabel("General Category"), resourceKindBox));

        // If we set the maximum high at this point, the fields would try to be
        // unneccessarily large. I'm not sure that setting it low at first helps, though.
        SpinnerNumberModel resourceCreatedModel = SpinnerNumberModel(-1, -1, 2000, 1);
        JSpinner creationSpinner = JSpinner(resourceCreatedModel);
        resourcePanel.add(pairPanel(JLabel("Turn created"), creationSpinner));

        UpdatedComboBox resourceBox = UpdatedComboBox(logError);
        resourcePanel.add(pairPanel(JLabel("Specific Resource"), resourceBox));

        SpinnerNumberModel resourceQuantityModel = SpinnerNumberModel(0, 0, 2000, 1);
        JSpinner resourceQuantitySpinner = JSpinner(resourceQuantityModel);
        resourcePanel.add(pairPanel(JLabel("Quantity"), resourceQuantitySpinner));

        UpdatedComboBox resourceUnitsBox = UpdatedComboBox(logError);
        resourcePanel.add(pairPanel(JLabel("Units"), resourceUnitsBox));

        variable Boolean playerIsDefault = true;
        void confirmPlayer() {
            if (playerIsDefault, currentPlayer.name.trimmed.empty) {
                menuHandler(ActionEvent(mainPanel, 1, "change current player"));
            }
            playerIsDefault = false;
        }

        void resourceListener(ActionEvent event) {
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
                ResourcePile pile = ResourcePile(idf.createID(), kind, resource,
                    Quantity(qty, units));
                pile.created = resourceCreatedModel.number.intValue();
                model.addResource(pile, currentPlayer);
                logAddition(pile.string);
                for (box in [ resourceKindBox, resourceBox, resourceUnitsBox ]) {
                    box.checkAndClear();
                }
                resourceCreatedModel.\ivalue = JInteger.valueOf(-1);
                resourceQuantityModel.\ivalue = JInteger.valueOf(0);
            } else {
                logLabel.append("Failed to convert quantity into the form we need.
                                 ");
            }
        }

        resourcePanel.add(pairPanel(JLabel(""),
            listenedButton("Add Resource", resourceListener)));
        resourceUnitsBox.addSubmitListener(resourceListener);

        if (is JTextField editor = creationSpinner.editor) {
            editor.addActionListener(resourceListener);
        } else {
            logLabel.append("Turn-created spinner's editor wasn't a text field, but a ``
                                classDeclaration(creationSpinner.editor)``
                             ");
        }
        if (is JTextField editor = resourceQuantitySpinner.editor) {
            editor.addActionListener(resourceListener);
        } else {
            logLabel.append("Quantity spinner's editor wasn't a text field, but a ``
                                classDeclaration(resourceQuantitySpinner.editor)``
                             ");
        }

        resourceBox.addSubmitListener(resourceListener);
        resourceKindBox.addSubmitListener(resourceListener);
        mainPanel.add(resourcePanel);

        mainPanel.addGlue();
        InterpolatedLabel<[Player]>
        implementLabel = InterpolatedLabel<[Player]>(equipmentLabelText, [currentPlayer]);
        mainPanel.add(implementLabel);

        SpinnerNumberModel implementQuantityModel = SpinnerNumberModel(1, 1, 2000, 1);
        JSpinner implementQuantityField = JSpinner(implementQuantityModel);
        UpdatedComboBox implementKindBox = UpdatedComboBox(logLabel.append);
        void implementListener(ActionEvent event) {
            confirmPlayer();
            String kind = implementKindBox.selectedString;
            if (kind.empty) {
                implementKindBox.requestFocusInWindow();
                return;
            }
            Integer quantity = implementQuantityModel.number.intValue();
            for (i in 0:quantity) {
                model.addResource(Implement(kind, idf.createID()), currentPlayer);
            }
            logAddition("``quantity`` x ``kind``");
            implementQuantityModel.\ivalue = JInteger.valueOf(1);
            implementKindBox.checkAndClear();
            implementQuantityField.requestFocusInWindow();
        }

        implementKindBox.addSubmitListener(implementListener);
        if (is JTextField editor = implementQuantityField.editor) {
            editor.addActionListener(implementListener);
        } else {
            logLabel.append(
                "Implement quantity spinner's editor wasn't a text field, but a ``
                                classDeclaration(implementQuantityField.editor)``
                             ");
        }

        mainPanel.add(centeredHorizontalBox(implementQuantityField,
            implementKindBox, listenedButton("Add Equipment", implementListener)));
        mainPanel.addGlue();
        JScrollPane scrolledLog = JScrollPane(logLabel);
        scrolledLog.minimumSize = logLabel.minimumSize;

        object retval extends SPFrame("Resource Entry", outer, null, true,
                    (file) => model.addSubordinateMap(mapIOHelper.readMap(file), file))
                satisfies PlayerChangeListener {
            shared actual void playerChanged(Player? old, Player newPlayer) {
                currentPlayer = newPlayer;
                resourceLabel.arguments = [currentPlayer];
                implementLabel.arguments = [currentPlayer];
            }
        }

        retval.add(verticalSplit(mainPanel, scrolledLog, 0.2, 0.1));
        retval.jMenuBar = workerMenu(menuHandler, retval, this);
        retval.pack();

        logLabel.minimumSize = Dimension(retval.width - 20, 50);
        JComponent temp = logLabel;
        temp.preferredSize = Dimension(retval.width, 100);
        JInteger maximum = JInteger.valueOf(runtime.maxArraySize);
        resourceCreatedModel.maximum = maximum;
        resourceQuantityModel.maximum = maximum;
        implementQuantityModel.maximum = maximum;
        return retval;
    }

    void startDriverImpl(PlayerChangeMenuListener pcml, MenuBroker menuHandler) {
        value frame = resourceAddingFrame(menuHandler.actionPerformed);
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

    shared actual void open(IMutableMapNG map, PathWrapper? path) {
        if (model.mapModified) {
            SwingUtilities.invokeLater(defer(compose(ResourceAddingGUI.startDriver,
                ResourceAddingGUI), [cli, options,
                    ResourceManagementDriverModel.fromMap(map, path)]));
        } else {
            model.setMap(map, path);
        }
    }
}
