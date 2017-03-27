import ceylon.interop.java {
    CeylonIterable
}
import ceylon.language.meta {
    classDeclaration
}

import java.awt {
    Image,
    Graphics,
    GridLayout,
    BorderLayout
}
import java.io {
    IOException
}

import javax.swing {
    JPanel,
    JLabel,
    SwingConstants,
    JComponent,
    BorderFactory
}

import lovelace.util.jvm {
    FunctionalGroupLayout,
    verticalSplit,
    horizontalSplit
}

import model.listeners {
    UnitMemberListener
}
import model.map {
    HasPortrait
}
import model.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Worker,
    ProxyFor
}
import model.map.fixtures.mobile {
    Animal
}
import model.map.fixtures.mobile.worker {
    ISkill
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    WorkerStats
}

import strategicprimer.viewer.drivers.map_viewer {
    loadImage
}
"A panel to show the details of the currently selected unit-member."
JPanel&UnitMemberListener memberDetailPanel(JPanel resultsPanel) {
    JPanel statPanel = JPanel();
    FunctionalGroupLayout statLayout = FunctionalGroupLayout(statPanel);
    statPanel.layout = statLayout;
    statPanel.border = BorderFactory.createEmptyBorder();
    statLayout.autoCreateGaps = true;
    statLayout.autoCreateContainerGaps = true;
    class StatLabel(Integer(WorkerStats ) stat) extends JLabel("+NaN") {
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
    StatLabel[6] statLabels = [strLabel, dexLabel, conLabel, intLabel, wisLabel, chaLabel];
    JLabel caption(String string) {
        return JLabel("<html><b>``string``:</b></html>");
    }
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
    JLabel jobsCaption = JLabel("Job Levels");
    JPanel jobsPanel = JPanel(GridLayout(0, 1));
    statLayout.setVerticalGroup(statLayout.createSequentialGroupOf(
        statLayout.createParallelGroupOf(typeCaption, typeLabel),
        statLayout.createParallelGroupOf(nameCaption, nameLabel),
        statLayout.createParallelGroupOf(kindCaption, kindLabel),
        statLayout.createParallelGroupOf(strCaption, strLabel, intCaption, intLabel),
        statLayout.createParallelGroupOf(dexCaption, dexLabel, wisCaption, wisLabel),
        statLayout.createParallelGroupOf(conCaption, conLabel, chaCaption, chaLabel),
        statLayout.createParallelGroupOf(jobsCaption, jobsPanel)));
    statLayout.setHorizontalGroup(statLayout.createParallelGroupOf(
        statLayout.createSequentialGroupOf(
            statLayout.createParallelGroupOf(typeCaption, nameCaption, kindCaption,
                statLayout.createSequentialGroupOf(strCaption, strLabel),
                statLayout.createSequentialGroupOf(dexCaption, dexLabel),
                statLayout.createSequentialGroupOf(conCaption, conLabel), jobsCaption),
            statLayout.createParallelGroupOf(typeLabel, nameLabel, kindLabel,
                statLayout.createSequentialGroupOf(intCaption, intLabel),
                statLayout.createSequentialGroupOf(wisCaption, wisLabel),
                statLayout.createSequentialGroupOf(chaCaption, chaLabel), jobsPanel))));
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
    JComponent split = verticalSplit(0.5, 0.5, horizontalSplit(0.6, 0.6, statPanel,
        portraitComponent), resultsPanel);
    split.border = BorderFactory.createEmptyBorder();
    variable UnitMember? current = null;
    void recache() {
        UnitMember? local = current;
        if (is Worker local) { // TODO: IWorker interface?
            typeLabel.text = "Worker";
            nameLabel.text = local.name;
            kindLabel.text = local.kind;
            WorkerStats? stats = local.stats;
            for (label in statLabels) {
                label.recache(stats);
            }
            jobsPanel.removeAll();
            for (job in local) {
                if (!job.empty) {
                    JLabel label = JLabel("``job.name`` ``job.level``");
                    {ISkill*} skills = CeylonIterable(job);
                    if (exists firstSkill = skills.first) {
                        StringBuilder skillsBuilder = StringBuilder();
                        skillsBuilder.append("Skills: ``firstSkill.name`` ``firstSkill
                            .level``");
                        for (skill in skills.rest) {
                            skillsBuilder.append(", ``skill.name`` ``skill.level``");
                        }
                        label.toolTipText = skillsBuilder.string;
                    }
                    jobsPanel.add(label);
                }
            }
        } else if (is Animal local) {
            typeLabel.text = "Animal";
            nameLabel.text = "";
            kindLabel.text = local.kind;
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        } else if (exists local) {
            typeLabel.text = "Unknown";
            nameLabel.text = "";
            kindLabel.text = classDeclaration(local).name;
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        } else {
            typeLabel.text = "";
            nameLabel.text = "";
            kindLabel.text = "";
            for (label in statLabels) {
                label.recache(null);
            }
            jobsPanel.removeAll();
        }
        portraitComponent.portrait = null;
        if (is HasPortrait local) {
            String portraitName = local.portrait;
            if (!portraitName.empty) {
                try {
                    portraitComponent.portrait = loadImage(portraitName);
                } catch (IOException except) {
                    log.warn("Failed to load portrait", except);
                }
            }
        }
        portraitComponent.repaint();
    }
    object retval extends JPanel(BorderLayout()) satisfies UnitMemberListener {
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
    retval.add(JLabel("<html><h2>Unit Member Details:</h2></html>"),
        BorderLayout.pageStart);
    retval.add(split);
    recache();
    return retval;
}
