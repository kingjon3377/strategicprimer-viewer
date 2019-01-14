import java.awt {
    CardLayout,
    Dimension,
    Component,
    GridLayout
}
import java.awt.event {
    ActionEvent
}
import java.lang {
    ObjectArray
}

import javax.swing {
    DefaultComboBoxModel,
    JTextField,
    JScrollPane,
    ComboBoxModel,
    JPanel,
    SwingList=JList,
    JLabel,
    JSpinner,
    SpinnerNumberModel
}

import lovelace.util.jvm {
    horizontalSplit,
    BorderedPanel,
    ListenedButton,
    ImprovedComboBox,
    FunctionalGroupLayout
}

import lovelace.util.common {
    silentListener,
    todo
}

import strategicprimer.model.common.map {
    Player
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

import strategicprimer.drivers.gui.common {
    SPFrame,
    SPMenu,
    MenuBroker
}
import strategicprimer.drivers.exploration.common {
    Speed
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}

"The main window for the exploration GUI."
todo("Merge parts of this back into ExplorationGUI?",
    "Do what we can to convert nested objects/classes to top-level, etc.")
SPFrame explorationFrame(ExplorationGUI driver,
        MenuBroker menuHandler) {

    SPFrame retval = SPFrame("Exploration", driver, Dimension(768, 48), true,
        (file) => driver.model.addSubordinateMap(mapIOHelper.readMap(file), file)); // TODO: Use driver-interface method once it's available

    CardLayout layoutObj = CardLayout();
    retval.setLayout(layoutObj);
    SpinnerNumberModel mpModel = SpinnerNumberModel(0, 0, 2000, 0);
    JSpinner mpField = JSpinner(mpModel);

    UnitListModel unitListModel = UnitListModel(driver.model);
    SwingList<IUnit> unitList = SwingList<IUnit>(unitListModel);

    PlayerListModel playerListModel = PlayerListModel(driver.model);
    SwingList<Player> playerList = SwingList<Player>(playerListModel);

    ComboBoxModel<Speed> speedModel = DefaultComboBoxModel<Speed>(
        ObjectArray<Speed>.with(sort(`Speed`.caseValues)));

    driver.model.addMapChangeListener(playerListModel);
    void handlePlayerChanged() {
        layoutObj.first(retval.contentPane);
        if (!playerList.selectionEmpty,
                exists newPlayer = playerList.selectedValue) {
            unitListModel.playerChanged(null, newPlayer);
        }
    }

    playerList.addListSelectionListener(silentListener(handlePlayerChanged));
    menuHandler.register(silentListener(handlePlayerChanged),
        "change current player");

    unitList.cellRenderer = UnitCellRenderer();

    speedModel.selectedItem = Speed.normal;

    BorderedPanel explorerSelectingPanel = BorderedPanel();

    JPanel tilesPanel = JPanel(GridLayout(3, 12, 2, 2));

    JPanel headerPanel = JPanel();
    FunctionalGroupLayout headerLayout = FunctionalGroupLayout(headerPanel);

    ExplorationPanel explorationPanel = ExplorationPanel(mpModel, speedModel, headerPanel,
        headerLayout, tilesPanel, driver.model);

    driver.model.addMovementCostListener(explorationPanel);
    driver.model.addSelectionChangeListener(explorationPanel);

    variable Boolean onFirstPanel = true;
    void swapPanels() {
        explorationPanel.validate();
        explorerSelectingPanel.validate();
        if (onFirstPanel) {
            layoutObj.next(retval.contentPane);
            onFirstPanel = false;
        } else {
            layoutObj.first(retval.contentPane);
            onFirstPanel = true;
        }
    }

    void buttonListener(ActionEvent event) {
        if (exists selectedValue = unitList.selectedValue,
            !unitList.selectionEmpty) {
            driver.model.selectedUnit = selectedValue;
            swapPanels();
        }
    }

    if (is JTextField mpEditor = mpField.editor) {
        mpEditor.addActionListener(buttonListener);
    }

    explorerSelectingPanel.center = horizontalSplit(
        BorderedPanel.verticalPanel(JLabel("Players in all maps:"), playerList,
            null),
        BorderedPanel.verticalPanel(JLabel(
            """<html><body><p>Units belonging to that player:</p>
               <p>(Selected unit will be used for exploration.)</p>
               </body></html>"""),
            JScrollPane(unitList), BorderedPanel.verticalPanel(
                BorderedPanel.horizontalPanel(JLabel("Unit's Movement Points"),
                    null, mpField),
                BorderedPanel.horizontalPanel(JLabel("Unit's Relative Speed"),
                    null, ImprovedComboBox<Speed>.withModel(speedModel)),
                ListenedButton("Start exploring!", buttonListener))));

    explorationPanel.addCompletionListener(swapPanels);
    retval.add(explorerSelectingPanel);
    retval.add(explorationPanel);

    (retval of Component).preferredSize = Dimension(1024, 640);

    retval.jMenuBar = SPMenu.forWindowContaining(explorationPanel,
        SPMenu.createFileMenu(menuHandler.actionPerformed, driver),
        SPMenu.disabledMenu(SPMenu.createMapMenu(menuHandler.actionPerformed, driver)),
        SPMenu.createViewMenu(menuHandler.actionPerformed, driver));
    retval.pack();
    return retval;
}
