import ceylon.collection {
    HashMap,
    MutableSet,
    MutableMap,
    HashSet
}
import ceylon.language.meta {
    classDeclaration
}
import ceylon.math.decimal {
    parseDecimal
}

import java.awt {
    Component,
    Dimension
}
import java.awt.event {
    ActionEvent
}
import java.io {
    IOException
}
import java.lang {
    JInteger=Integer
}
import java.nio.file {
    JPath=Path
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
    FormattedLabel
}

import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel,
    SPOptions,
    IDriverUsage,
    DriverFailedException,
    SimpleDriver,
    ParamCount,
    DriverUsage,
    SimpleCLIDriver,
    ICLIHelper,
    PlayerChangeListener
}
import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.map {
    Player,
    IMutableMapNG,
    IMapNG,
    PlayerImpl
}
import strategicprimer.model.map.fixtures {
    ResourcePile,
    Implement,
    FortressMember,
    Quantity
}
import strategicprimer.model.map.fixtures.towns {
    Fortress
}
import strategicprimer.viewer.drivers.worker_mgmt {
    workerMenu
}
"A driver model for resource-entering drivers."
class ResourceManagementDriverModel extends SimpleMultiMapModel {
    shared new fromMap(IMutableMapNG map, JPath? file) extends
        SimpleMultiMapModel(map, file) { }
    shared new fromDriverModel(IDriverModel driverModel) extends
        SimpleMultiMapModel.copyConstructor(driverModel) { }
    "All the players in all the maps."
    shared {Player*} players => allMaps.map((pair) => pair.first)
        .flatMap((IMutableMapNG temp) => temp.players.distinct);
    "Add a resource to a player's HQ."
    shared void addResource(FortressMember resource, Player player) {
        for (pair in allMaps) {
            IMutableMapNG map = pair.first;
            Player mapPlayer = map.currentPlayer;
            if (mapPlayer.independent || mapPlayer.playerId < 0 ||
                    mapPlayer.playerId == player.playerId) {
                addResourceToMap(resource.copy(false), map, player);
            }
        }
    }
    "Add a resource to a player's HQ in a particular map."
    shared void addResourceToMap(FortressMember resource, IMapNG map, Player player) {
        for (location in map.locations) {
            for (fixture in map.getOtherFixtures(location)) {
                if (is Fortress fixture, "HQ" == fixture.name,
                        player.playerId == fixture.owner.playerId) {
                    fixture.addMember(resource);
                }
            }
        }
    }
    "Get the current player. If none is current, returns null."
    shared Player? currentPlayer => players.find(Player.current);
}
"A driver to le the user enter a player's resources and equipment."
object resourceAddingCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-d";
        longOption = "--add-resource";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    MutableSet<String> resourceKinds = HashSet<String>();
    MutableMap<String, MutableSet<String>> resourceContents =
            HashMap<String, MutableSet<String>>();
    MutableMap<String, String> resourceUnits = HashMap<String, String>();
    "Ask the user to choose or enter a resource kind."
    String getResourceKind(ICLIHelper cli) {
        String[] list = [*resourceKinds];
        Integer num = cli.chooseStringFromList(list, "Possible kinds of resources:",
            "No resource kinds entered yet", "Chosen kind: ", false);
        if (exists retval = list.get(num)) {
            return retval;
        } else {
            String retval = cli.inputString("Resource kind to use: ");
            resourceKinds.add(retval);
            return retval;
        }
    }
    "Ask the user to choose or enter a resource-content-type for a given resource kind."
    String getResourceContents(String kind, ICLIHelper cli) {
        MutableSet<String> set;
        if (exists temp = resourceContents.get(kind)) {
            set = temp;
        } else {
            set = HashSet<String>();
            resourceContents.put(kind, set);
        }
        String[] list = [*set];
        Integer num = cli.chooseStringFromList(list,
            "Possible resources in the ``kind`` category`", "No resources entered yet",
            "Choose resource: ", false);
        if (exists retval = list.get(num)) {
            return retval;
        } else {
            String retval = cli.inputString("Resource to use: ");
            set.add(retval);
            return retval;
        }
    }
    "Ask the user to choose units for a type of resource."
    String getResourceUnits(String resource, ICLIHelper cli) {
        if (exists unit = resourceUnits.get(resource),
            cli.inputBooleanInSeries("Is ``unit`` the correct unit for ``resource``? ",
                "correct;``unit``;``resource``")) {
            return unit;
        } else {
            String retval = cli.inputString("Unit to use for ``resource``: ");
            resourceUnits.put(resource, retval);
            return retval;
        }
    }
    "Ask the user to enter a resource."
    void enterResource(IDRegistrar idf, ResourceManagementDriverModel model, ICLIHelper cli,
            Player player) {
        String kind = getResourceKind(cli);
        String origContents = getResourceContents(kind, cli);
        String units = getResourceUnits(origContents, cli);
        String contents;
        if (cli.inputBooleanInSeries("Qualify the particular resource with a prefix? ",
                "prefix " + origContents)) {
            contents = "``cli.inputString("Prefix to use: ").trimmed`` ``origContents``";
        } else {
            contents = origContents;
        }
        model.addResource(ResourcePile(idf.createID(), kind, contents, Quantity(
            cli.inputDecimal("Quantity in ``units``?"), units)), player);
    }
    "Ask the user to enter an Implement (a piece of equipment)"
    void enterImplement(IDRegistrar idf, ResourceManagementDriverModel model,
            ICLIHelper cli, Player player) {
        model.addResource(Implement(cli.inputString("Kind of equipment: "),
            idf.createID()), player);
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            Player[] players = [*model.players];
            IDRegistrar idf = createIDFactory(model.allMaps.map((pair) => pair.first));
            try {
                cli.loopOnList(players,
                    (clh) => clh.chooseFromList(players,
                        "Players in the maps:", "No players found.",
                        "Player to add resources for: ", false),
                "Choose another player?", (Player player, clh) {
                        while (clh.inputBoolean("Keep going? ")) {
                            if (clh.inputBooleanInSeries(
                                    "Enter a (quantified) resource? ")) {
                                enterResource(idf, model, clh, player);
                            } else if (clh.inputBooleanInSeries(
                                    "Enter equipment etc.? ")) {
                                enterImplement(idf, model, clh, player);
                            }
                        }
                    });
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error interacting with user");
            }
        } else {
            startDriverOnModel(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }
}
"A window to let the user enter resources etc. Note that this is not a dialog to enter one resource and close."
SPFrame&PlayerChangeListener resourceAddingFrame(ResourceManagementDriverModel model,
        Anything(ActionEvent) menuHandler) {
    IDRegistrar idf = createIDFactory(model.allMaps.map((pair) => pair.first));
    variable Player currentPlayer = PlayerImpl(-1, "");
    JPanel&BoxPanel mainPanel = boxPanel(BoxAxis.pageAxis);
    FormattedLabel resourceLabel = FormattedLabel("Add resource for %s:",
        currentPlayer.name);
    mainPanel.add(resourceLabel);
    JPanel pairPanel(Component first, Component second) {
        JPanel&BoxPanel panel = boxPanel(BoxAxis.pageAxis);
        panel.addGlue();
        panel.add(first);
        panel.addGlue();
        panel.add(second);
        panel.addGlue();
        return panel;
    }
    JPanel resourcePanel = boxPanel(BoxAxis.lineAxis);
    StreamingLabel logLabel = StreamingLabel();
    void logAddition(String addend) {
        logLabel.append(
                "<p style=\"color:white; margin-bottom: 0.5em; margin-top: 0.5em;\">Added ``
                addend`` for ``currentPlayer.name``");
    }
    "Extends [[ImprovedComboBox]] to keep a running collection of values."
    class UpdatedComboBox() extends ImprovedComboBox<String>() {
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
            if (is String selectedItem) {
                super.selectedItem = selectedItem;
            }
        }
        shared String selectedString {
            assert (is String retval = selectedItem);
            return retval;
        }
        shared void addSubmitListener(Anything(ActionEvent) listener) {
            value inner = editor.editorComponent;
            if (is JTextField inner) {
                inner.addActionListener(listener);
            } else {
                logLabel.append("Editor wasn't a text field, but a ``
                        classDeclaration(inner)``");
            }
        }
    }
    UpdatedComboBox resourceKindBox = UpdatedComboBox();
    resourcePanel.add(pairPanel(JLabel("General Category"), resourceKindBox));
    // If we set the maximum high at this point, the fields would try to be unneccessarily
    // large. I'm not sure that setting it low at first helps, though.
    SpinnerNumberModel resourceCreatedModel = SpinnerNumberModel(-1, -1, 2000, 1);
    resourcePanel.add(pairPanel(JLabel("Turn created"), JSpinner(resourceCreatedModel)));
    UpdatedComboBox resourceBox = UpdatedComboBox();
    resourcePanel.add(pairPanel(JLabel("Specific Resource"), resourceBox));
    SpinnerNumberModel resourceQuantityModel = SpinnerNumberModel(0, 0, 2000, 1);
    resourcePanel.add(pairPanel(JLabel("Quantity"), JSpinner(resourceQuantityModel)));
    UpdatedComboBox resourceUnitsBox = UpdatedComboBox();
    resourcePanel.add(pairPanel(JLabel("Units"), resourceUnitsBox));
    variable Boolean playerIsDefault = true;
    void confirmPlayer() {
        if (playerIsDefault, currentPlayer.name.trimmed.empty) {
            menuHandler(ActionEvent(mainPanel, 1, "change current player"));
        }
        playerIsDefault = false;
    }
    Anything(ActionEvent) resourceListener = (ActionEvent event) {
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
            pile.created =resourceCreatedModel.number.intValue();
            model.addResource(pile, currentPlayer);
            logAddition(pile.string);
            for (box in { resourceKindBox, resourceBox, resourceUnitsBox }) {
                box.checkAndClear();
            }
            resourceCreatedModel.\ivalue = -1;
            resourceQuantityModel.\ivalue = 0;
        } else {
            logLabel.append("Failed to convert quantity into the form we need.
                             ");
        }
    };
    resourcePanel.add(pairPanel(JLabel(""),
        listenedButton("Add Resource", resourceListener)));
    resourceUnitsBox.addSubmitListener(resourceListener);
    mainPanel.add(resourcePanel);

    mainPanel.addGlue();
    FormattedLabel implementLabel = FormattedLabel("Add equipment for %s:",
        currentPlayer.name);
    mainPanel.add(implementLabel);
    SpinnerNumberModel implementQuantityModel = SpinnerNumberModel(1, 1, 2000, 1);
    JSpinner implementQuantityField = JSpinner(implementQuantityModel);
    UpdatedComboBox implementKindBox = UpdatedComboBox();
    Anything(ActionEvent) implementListener = (ActionEvent event) {
        confirmPlayer();
        String kind = implementKindBox.selectedString;
        if (kind.empty) {
            implementKindBox.requestFocusInWindow();
            return;
        }
        Integer quantity = implementQuantityModel.number.intValue();
        for (i in 0..quantity) {
            model.addResource(Implement(kind, idf.createID()), currentPlayer);
        }
        logAddition("``quantity`` x ``kind``");
        implementQuantityModel.\ivalue = 1;
        implementKindBox.checkAndClear();
        implementQuantityField.requestFocusInWindow();
    };
    implementKindBox.addSubmitListener(implementListener);
    mainPanel.add(centeredHorizontalBox(implementQuantityField,
        implementKindBox, listenedButton("Add Equipment", implementListener)));
    mainPanel.addGlue();
    JScrollPane scrolledLog = JScrollPane(logLabel);
    scrolledLog.minimumSize = logLabel.minimumSize;

    object retval extends SPFrame("Resource Entry", model.mapFile)
            satisfies PlayerChangeListener {
        shared actual String windowName = "Resource Entry";
        shared actual void playerChanged(Player? old, Player newPlayer) {
            currentPlayer = newPlayer;
            resourceLabel.setArgs(currentPlayer.name);
            implementLabel.setArgs(currentPlayer.name);
        }
    }
    retval.add(verticalSplit(0.2, 0.1, mainPanel, scrolledLog));
    retval.jMenuBar = workerMenu(menuHandler, retval, model);
    retval.pack();
    logLabel.minimumSize = Dimension(retval.width - 20, 50);
    JComponent temp = logLabel;
    temp.preferredSize = Dimension(retval.width, 100);
    JInteger maximum = JInteger(JInteger.maxValue);
    resourceCreatedModel.maximum = maximum;
    resourceQuantityModel.maximum = maximum;
    implementQuantityModel.maximum = maximum;
    return retval;
    
}
object resourceAddingGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-d";
        longOption = "--add-resource";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Add resources to maps";
        longDescription = "Add resources for players to maps";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ResourceManagementDriverModel model) {
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(pcml, "change current player");
            SwingUtilities.invokeLater(() {
                value frame = resourceAddingFrame(model, menuHandler.actionPerformed);
                pcml.addPlayerChangeListener(frame);
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options,
                ResourceManagementDriverModel.fromDriverModel(model));
        }
    }
    "Ask the user to choose a file."
    shared actual JPath askUserForFile() {
        try {
            return FileChooser.open(null).file;
        } catch (FileChooser.ChoiceInterruptedException except) {
            throw DriverFailedException(except,
                "Choice interrupted or user didn't choose");
        }
    }
}