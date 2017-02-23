import ceylon.collection {
    HashMap,
    MutableSet,
    MutableMap,
    ArrayList,
    HashSet
}
import controller.map.misc {
    ICLIHelper,
    IDFactoryFiller,
    IDRegistrar,
    MenuBroker
}
import model.misc {
    IDriverModel
}
import model.resources {
    ResourceManagementDriver
}
import java.util {
    JList = List
}
import ceylon.interop.java {
    JavaList,
    CeylonIterable,
    javaString
}
import model.map {
    Player,
    PlayerImpl
}
import java.io {
    IOException
}
import model.map.fixtures {
    ResourcePile,
    Implement
}
import util {
    Quantity
}
import java.lang {
    JString=String, JInteger=Integer
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
import view.util {
    SPFrame,
    BoxPanel,
    FormattedLabel,
    StreamingLabel,
    ImprovedComboBox,
    SplitWithWeights
}
import model.listeners {
    PlayerChangeListener
}
import java.awt {
    Component,
    Dimension
}
import java.awt.event {
    ActionEvent
}
import view.worker {
    WorkerMenu
}
import ceylon.language.meta {
    classDeclaration
}
import lovelace.util.jvm {
    listenedButton
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
        List<JString> list = ArrayList(resourceKinds.size, 1.0,
            resourceKinds.map(javaString));
        JList<JString> temp = JavaList(list);
        Integer num = cli.chooseStringFromList(temp, "Possible kinds of resources:",
            "No resource kinds entered yet", "Chosen kind: ", false);
        if (exists retval = list.get(num)) {
            // TODO: drop .string once CLIHelper is ported
            return retval.string;
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
        List<JString> list = ArrayList(set.size, 1.0, set.map(javaString));
        JList<JString> temp = JavaList(list);
        Integer num = cli.chooseStringFromList(temp,
            "Possible resources in the ``kind`` category`", "No resources entered yet",
            "Choose resource: ", false);
        if (exists retval = list.get(num)) {
            // TODO: drop .string once CLIHelper is ported
            return retval.string;
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
    void enterResource(IDRegistrar idf, ResourceManagementDriver model, ICLIHelper cli,
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
    void enterImplement(IDRegistrar idf, ResourceManagementDriver model,
            ICLIHelper cli, Player player) {
        model.addResource(Implement(cli.inputString("Kind of equipment: "),
            idf.createID()), player);
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ResourceManagementDriver model) {
            JList<Player> players =
                    JavaList(ArrayList(0, 1.0, CeylonIterable(model.players)));
            IDRegistrar idf = IDFactoryFiller.createFactory(model);
            try {
                cli.loopOnList(players,
                    (clh) => clh.chooseFromList(players, "Players in the maps:",
                        "No players found.", "Player to add resources for: ", false),
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
            startDriverOnModel(cli, options, ResourceManagementDriver(model));
        }
    }
}
"A window to let the user enter resources etc. Note that this is not a dialog to enter one resource and close."
SPFrame&PlayerChangeListener resourceAddingFrame(ResourceManagementDriver model,
        MenuBroker menuHandler) {
    IDRegistrar idf = IDFactoryFiller.createFactory(model);
    variable Player currentPlayer = PlayerImpl(-1, "");
    BoxPanel mainPanel = BoxPanel(false);
    FormattedLabel resourceLabel = FormattedLabel("Add resource for %s:",
        currentPlayer.name);
    mainPanel.add(resourceLabel);
    class PairPanel(Component first, Component second) extends BoxPanel(false) {
        addGlue();
        add(first);
        addGlue();
        add(second);
        addGlue();
    }
    JPanel resourcePanel = BoxPanel(true);
    StreamingLabel logLabel = StreamingLabel();
    void logAddition(String addend) {
        try (writer = logLabel.writer) {
            writer.println(
                "<p style=\"color:white; margin-bottom: 0.5em; margin-top: 0.5em;\">Added ``
                addend`` for ``currentPlayer.name``");
        }
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
                // logLabel.writer is a PrintWriter whose close() is a no-op.
                try (logger = logLabel.writer) {
                    logger.println("Editor wasn't a text field, but a ``
                        classDeclaration(inner)``");
                }
            }
        }
    }
    UpdatedComboBox resourceKindBox = UpdatedComboBox();
    resourcePanel.add(PairPanel(JLabel("General Category"), resourceKindBox));
    // If we set the maximum high at this point, the fields would try to be unneccessarily
    // large. I'm not sure that setting it low at first helps, though.
    SpinnerNumberModel resourceCreatedModel = SpinnerNumberModel(-1, -1, 2000, 1);
    resourcePanel.add(PairPanel(JLabel("Turn created"), JSpinner(resourceCreatedModel)));
    UpdatedComboBox resourceBox = UpdatedComboBox();
    resourcePanel.add(PairPanel(JLabel("Specific Resource"), resourceBox));
    SpinnerNumberModel resourceQuantityModel = SpinnerNumberModel(0, 0, 2000, 1);
    resourcePanel.add(PairPanel(JLabel("Quantity"), JSpinner(resourceQuantityModel)));
    UpdatedComboBox resourceUnitsBox = UpdatedComboBox();
    resourcePanel.add(PairPanel(JLabel("Units"), resourceUnitsBox));
    variable Boolean playerIsDefault = true;
    void confirmPlayer() {
        if (playerIsDefault, currentPlayer.name.trimmed.empty) {
            menuHandler.actionPerformed(ActionEvent(mainPanel, 1, "change current player"));
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
        ResourcePile pile = ResourcePile(idf.createID(), kind, resource,
            Quantity(resourceQuantityModel.number, units));
        pile.created = resourceCreatedModel.number.intValue();
        model.addResource(pile, currentPlayer);
        logAddition(pile.string);
        for (box in { resourceKindBox, resourceBox, resourceUnitsBox }) {
            box.checkAndClear();
        }
        resourceCreatedModel.\ivalue = -1;
        resourceQuantityModel.\ivalue = 0;
    };
    resourcePanel.add(PairPanel(JLabel(""),
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
    mainPanel.add(BoxPanel.centeredHorizBox(implementQuantityField,
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
    retval.add(SplitWithWeights.verticalSplit(0.2, 0.1, mainPanel, scrolledLog));
    retval.jMenuBar = WorkerMenu(menuHandler, retval, model);
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
        if (is ResourceManagementDriver model) {
            PlayerChangeMenuListener pcml = PlayerChangeMenuListener(model);
            MenuBroker menuHandler = MenuBroker();
            menuHandler.register(pcml, "change current player");
            SwingUtilities.invokeLater(() {
                value frame = resourceAddingFrame(model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, ResourceManagementDriver(model));
        }
    }
}