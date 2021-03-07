import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

service(`interface TurnAppletFactory`)
shared class OtherAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) => OtherApplet();
}

class OtherApplet() satisfies TurnApplet {
    shared actual [String+] commands = ["other"];
    shared actual String description = "something no applet supports";
    shared actual String? run() => null;
}
