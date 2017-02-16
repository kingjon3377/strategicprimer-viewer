import controller.map.drivers {
    DriverFailedException
}
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
    PlayerChangeMenuListener,
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
    Player
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
    JString=String
}
import javax.swing {
    SwingUtilities
}
import view.resources {
    ResourceAddingFrame
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
                throw DriverFailedException("I/O error interacting with user", except);
            }
        } else {
            startDriverOnModel(cli, options, ResourceManagementDriver(model));
        }
    }
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
                ResourceAddingFrame frame = ResourceAddingFrame(model, menuHandler);
                pcml.addPlayerChangeListener(frame);
                frame.setVisible(true);
            });
        } else {
            startDriverOnModel(cli, options, ResourceManagementDriver(model));
        }
    }
}