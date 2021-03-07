import ceylon.logging {
    Logger,
    logger
}

Logger log = logger(`module strategicprimer.drivers.utility`);

// Left outside mapCheckerCLI because it's also used in the todoFixerCLI.
{String+} landRaces = [ "Danan", "dwarf", "elf", "half-elf", "gnome", "human" ];
