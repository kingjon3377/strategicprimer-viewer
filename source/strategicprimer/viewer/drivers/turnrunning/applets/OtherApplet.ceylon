import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
service(`interface TurnAppletFactory`)
shared class OtherAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) => OtherApplet();
}

class OtherApplet() satisfies TurnApplet {
    shared actual [String+] commands = ["other"];
    shared actual String description = "something no applet supports";
    shared actual String? run() => null;
}
