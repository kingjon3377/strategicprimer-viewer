import strategicprimer.drivers.exploration.common {
    IExplorationModel
}

import strategicprimer.drivers.common.cli {
    ICLIHelper,
    AppletChooser
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}

service(`interface TurnAppletFactory`)
shared class RepeatAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
        RepeatApplet(model, cli, idf);
}

class RepeatApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) satisfies TurnApplet {
    shared actual [String+] commands = ["repeat"];
    shared actual String description = "Run multiple commands for a single unit";
    Boolean isNotRepeat(TurnAppletFactory factory) => !factory is RepeatAppletFactory;
    TurnApplet getApplet(TurnAppletFactory factory) => factory.create(model, cli, idf);
    AppletChooser<TurnApplet> appletChooser = AppletChooser<TurnApplet>(cli, *`module strategicprimer.viewer`
        .findServiceProviders(`TurnAppletFactory`).filter(isNotRepeat).map(getApplet));
    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        while (true) {
            switch (command = appletChooser.chooseApplet())
            case (null|true) { continue; }
            case (false) { return null; }
            case (is TurnApplet) {
                if (exists results = command.run()) {
                    buffer.append(results);
                } else {
                    return null;
                }
            }
            switch (cont = cli.inputBoolean("Create more results for this unit?"))
            case (true) {}
            case (false) { break; }
            case (null) { return null; }
        }
        return buffer.string;
    }
}
