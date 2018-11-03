import ceylon.language.meta {
    classDeclaration
}

import java.awt {
    Image,
    Graphics,
    GridLayout
}
import java.io {
    IOException
}

import javax.swing {
    JPanel,
    JLabel,
    SwingConstants,
    JComponent,
    BorderFactory,
    JScrollPane,
    ScrollPaneConstants
}

import lovelace.util.jvm {
    FunctionalGroupLayout,
    verticalSplit,
    horizontalSplit,
    BorderedPanel,
    platform
}

import strategicprimer.model.common.map {
    HasPortrait
}
import strategicprimer.model.common.map.fixtures {
    UnitMember,
    Implement,
    ResourcePile
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    ProxyFor,
    Animal,
    maturityModel,
    animalPlurals
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    WorkerStats,
    IJob
}
import strategicprimer.viewer.drivers.map_viewer {
    imageLoader
}

"A panel to show the details of the currently selected unit-member."
JPanel&UnitMemberListener memberDetailPanel(JPanel resultsPanel) {
    JPanel statPanel = JPanel();
    FunctionalGroupLayout statLayout = FunctionalGroupLayout(statPanel); // TODO: Add parameters to let us pass autoCreateGaps, autoCreateContainerGaps, to this method
    statPanel.layout = statLayout;
    statPanel.border = BorderFactory.createEmptyBorder();
    statLayout.autoCreateGaps = true;
    statLayout.autoCreateContainerGaps = true;

    class StatLabel(Integer(WorkerStats) stat) extends JLabel("+NaN") { // TODO: Replace with InterpolatedLabel
        shared void recache(WorkerStats? stats) {
            if (exists stats) {
                text = WorkerStats.getModifierString(stat(stats));
            } else {
                text = "";
            }
        }
    }
    StatLabel strLabel = StatLabel(WorkerStats.strength);
    StatLabel dexLabel = StatLabel(WorkerStats.dexterity);
    StatLabel conLabel = StatLabel(WorkerStats.constitution);
    StatLabel intLabel = StatLabel(WorkerStats.intelligence);
    StatLabel wisLabel = StatLabel(WorkerStats.wisdom);
    StatLabel chaLabel = StatLabel(WorkerStats.charisma);
    StatLabel[6] statLabels = [strLabel, dexLabel, conLabel, intLabel, wisLabel,
        chaLabel];

    JLabel caption(String string) =>
        JLabel("<html><b>``string``:</b></html>");
    JLabel typeCaption = caption("Member Type");
    JLabel typeLabel = JLabel("member type");
    JLabel nameCaption = caption("Name");
    JLabel nameLabel = JLabel("member name");
    JLabel kindCaption = caption("Race or Kind");
    JLabel kindLabel = JLabel("member kind");
    JLabel strCaption = caption("Str");
    JLabel dexCaption = caption("Dex");
    JLabel conCaption = caption("Con");
    JLabel intCaption = caption("Int");
    JLabel wisCaption = caption("Wis");
    JLabel chaCaption = caption("Cha");
    JLabel jobsCaption = caption("Job Levels");
    JPanel jobsPanel = JPanel(GridLayout(0, 1));

    statLayout.setVerticalGroup(statLayout.sequentialGroupOf(
        statLayout.parallelGroupOf(typeCaption, typeLabel),
        statLayout.parallelGroupOf(nameCaption, nameLabel),
        statLayout.parallelGroupOf(kindCaption, kindLabel),
        statLayout.parallelGroupOf(strCaption, strLabel, intCaption, intLabel),
        statLayout.parallelGroupOf(dexCaption, dexLabel, wisCaption, wisLabel),
        statLayout.parallelGroupOf(conCaption, conLabel, chaCaption, chaLabel),
        statLayout.parallelGroupOf(jobsCaption, jobsPanel)));

    statLayout.setHorizontalGroup(statLayout.parallelGroupOf(
        statLayout.sequentialGroupOf(
            statLayout.parallelGroupOf(typeCaption, nameCaption, kindCaption,
                statLayout.sequentialGroupOf(strCaption, strLabel),
                statLayout.sequentialGroupOf(dexCaption, dexLabel),
                statLayout.sequentialGroupOf(conCaption, conLabel), jobsCaption),
            statLayout.parallelGroupOf(typeLabel, nameLabel, kindLabel,
                statLayout.sequentialGroupOf(intCaption, intLabel),
                statLayout.sequentialGroupOf(wisCaption, wisLabel),
                statLayout.sequentialGroupOf(chaCaption, chaLabel), jobsPanel))));

    statLayout.linkSize(SwingConstants.horizontal, typeCaption, nameCaption, kindCaption,
        jobsCaption);
    statLayout.linkSize(SwingConstants.horizontal, typeLabel, nameLabel, kindLabel,
        jobsPanel);
    statLayout.linkSize(strCaption, dexCaption, conCaption, intCaption, wisCaption,
        chaCaption);
    statLayout.linkSize(*statLabels);
    statLayout.linkSize(SwingConstants.vertical, typeCaption, typeLabel);
    statLayout.linkSize(SwingConstants.vertical, nameCaption, nameLabel);
    statLayout.linkSize(SwingConstants.vertical, kindCaption, kindLabel);

    object portraitComponent extends JComponent() {
        shared variable Image? portrait = null;
        shared actual void paintComponent(Graphics pen) {
            super.paintComponent(pen);
            if (exists local = portrait) {
                pen.drawImage(local, 0, 0, width, height, this);
            }
        }
    }

    JScrollPane statPanelWrapped = JScrollPane(horizontalSplit(statPanel,
            portraitComponent, 0.6),
        (platform.systemIsMac) then ScrollPaneConstants.verticalScrollbarAlways
                else ScrollPaneConstants.verticalScrollbarAsNeeded,
        (platform.systemIsMac) then ScrollPaneConstants.horizontalScrollbarAlways
                else ScrollPaneConstants.horizontalScrollbarAsNeeded);
    JComponent split = verticalSplit(statPanelWrapped, resultsPanel);
    split.border = BorderFactory.createEmptyBorder();

    variable UnitMember? current = null;
    void recache() {
        UnitMember? local = current;
        jobsPanel.removeAll();
        if (is IWorker local) {
            typeLabel.text = "Worker";
            nameLabel.text = local.name;
            kindLabel.text = local.kind;
            WorkerStats? stats = local.stats;
            for (label in statLabels) {
                label.recache(stats);
            }
            for (job in local.filter(not(IJob.emptyJob))) {
                JLabel label = JLabel("``job.name`` ``job.level``");
                if (exists firstSkill = job.first) {
                    // TODO: Replace with label.toolTipText = "Skills: ``", ".join(job.map(skillString))``"; (defining skillString somewhere if there isn't a suitable String attribute in ISkill already)
                    StringBuilder skillsBuilder = StringBuilder();
                    skillsBuilder.append("Skills: ``firstSkill.name`` ``firstSkill
                        .level``");
                    for (skill in job.rest) {
                        skillsBuilder.append(", ``skill.name`` ``skill.level``");
                    }
                    label.toolTipText = skillsBuilder.string;
                }
                jobsPanel.add(label);
            }
        } else if (is Animal local) {
//            String plural = animalPlurals[local.kind]; // TODO: syntax sugar once compiler bug fixed
            String plural = animalPlurals.get(local.kind);
            if (local.born >= 0, maturityModel.currentTurn >= 0,
                    exists maturityAge = maturityModel.maturityAges[local.kind],
                    maturityModel.currentTurn - local.born < maturityAge) {
                if (local.population > 1) {
                    typeLabel.text = "Young Animals";
                    kindLabel.text =
                        "``local.population`` young ``plural``";
                } else {
                    typeLabel.text = "Young Animal";
                    kindLabel.text = "Young ``local.kind``";
                }
            } else {
                if (local.population > 1) {
                    typeLabel.text = "Animals";
                    kindLabel.text = "``local.population`` ``plural``";
                } else {
                    typeLabel.text = "Animal";
                    kindLabel.text = local.kind;
                }
            }
            nameLabel.text = "";
            for (label in statLabels) {
                label.recache(null);
            }
        } else if (is Implement local) {
            typeLabel.text = "Equipment";
            nameLabel.text = "";
            if (local.count > 1) {
                kindLabel.text = "``local.count`` x ``local.kind``";
            } else {
                kindLabel.text = local.kind;
            }
            for (label in statLabels) {
                label.recache(null);
            }
        } else if (is ResourcePile local) {
            typeLabel.text = "Resource";
            nameLabel.text = "";
            kindLabel.text = "``local.quantity`` ``local.contents`` (``local.kind``)";
            for (label in statLabels) {
                label.recache(null);
            }
        } else if (exists local) {
            typeLabel.text = "Unknown";
            nameLabel.text = "";
            kindLabel.text = classDeclaration(local).name;
            for (label in statLabels) {
                label.recache(null);
            }
        } else {
            typeLabel.text = "";
            nameLabel.text = "";
            kindLabel.text = "";
            for (label in statLabels) {
                label.recache(null);
            }
        }
        portraitComponent.portrait = null;
        if (is HasPortrait local) {
            String portraitName = local.portrait;
            if (!portraitName.empty) {
                try {
                    portraitComponent.portrait = imageLoader.loadImage(portraitName);
                } catch (IOException except) {
                    log.warn("Failed to load portrait", except);
                }
            }
        }
        portraitComponent.repaint();
    }

    object retval extends BorderedPanel() satisfies UnitMemberListener {
        shared actual void memberSelected(UnitMember? old, UnitMember? selected) {
            if (is ProxyFor<out UnitMember> selected) {
                if (selected.parallel) {
                    if (exists first = selected.proxied.first) {
                        memberSelected(old, first);
                        return;
                    }
                } else {
                    memberSelected(old, null);
                    return;
                }
            }
            if (exists selected) {
                if (exists temp = current, selected == temp) {
                } else {
                    current = selected;
                    recache();
                }
            } else if (current exists) {
                current = null;
                recache();
            }
        }
    }

    retval.pageStart = JLabel("<html><h2>Unit Member Details:</h2></html>");
    retval.center = split;
    recache();
    return retval;
}
