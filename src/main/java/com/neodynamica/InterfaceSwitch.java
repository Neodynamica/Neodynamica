package com.neodynamica;

import com.neodynamica.userinterface.cli.CLI;
import com.neodynamica.userinterface.gui.GUI;

/**
 * The class to run to initiate Neodynamica. The first argument should be the interface type to be executed
 */
public class InterfaceSwitch {
    public static void main(String[] args) {
        final String interfaceMode = args.length > 0 && args[0] == null ? "" : args[0].toLowerCase();
        switch (interfaceMode){
            case "gui":
                GUI.main(args);
                break;
            case "cli":
                CLI.main(args);
                break;
            default:
                CLI.main(args);
                break;
        }
    }
}
