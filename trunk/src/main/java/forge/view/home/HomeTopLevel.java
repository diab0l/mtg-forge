/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.view.home;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import forge.AllZone;
import forge.view.toolbox.FButton;
import forge.view.toolbox.FPanel;
import forge.view.toolbox.FRoundedPanel;
import forge.view.toolbox.FSkin;

/**
 * Lays out battle, sidebar, user areas in locked % vals and repaints as
 * necessary.
 * 
 */

@SuppressWarnings("serial")
public class HomeTopLevel extends FPanel {
    private JPanel pnlMenu, pnlContent;
    private FButton btnDraft, btnConstructed, btnSealed, btnQuest, btnSettings, btnUtilities, btnEditor;
    private FSkin skin;
    private String constraints;
    private String imgDirAddress;

    private ViewConstructed constructed;
    private ViewSealed sealed;
    private ViewDraft draft;
    private ViewQuest quest;
    private ViewSettings settings;
    private ViewUtilities utilities;

    /**
     * Instantiates a new home top level.
     */
    public HomeTopLevel() {
        super();
        skin = AllZone.getSkin();
        imgDirAddress = "res/images/ui/HomeScreen/default_600/";

        constructed = new ViewConstructed(this);
        sealed = new ViewSealed(this);
        draft = new ViewDraft(this);
        quest = new ViewQuest(this);
        settings = new ViewSettings(this);
        utilities = new ViewUtilities(this);

        this.setOpaque(false);
        this.setBGTexture(new ImageIcon(skin.getImage("bg.texture")));
        this.setLayout(new MigLayout("insets 0, gap 0"));
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                revalidate();
            }
        });

        pnlMenu = new FRoundedPanel();
        pnlMenu.setLayout(new MigLayout("insets 0, gap 0, wrap"));
        pnlMenu.setBackground(skin.getColor("theme"));

        pnlContent = new FRoundedPanel();
        pnlContent.setBackground(skin.getColor("zebra"));
        pnlContent.setLayout(new MigLayout("insets 0, gap 0"));

        btnDraft = new FButton();
        btnDraft.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { draft(); }
        });
        btnDraft.setText("Draft (Hard)");

        btnConstructed = new FButton();
        btnConstructed.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { constructed(); }
        });
        btnConstructed.setText("Constructed (Easy)");

        btnSealed = new FButton();
        btnSealed.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { sealed(); }
        });
        btnSealed.setText("Sealed (Medium)");

        btnQuest = new FButton();
        btnQuest.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { quest(); }
        });
        btnQuest.setText("Quest");

        btnSettings = new FButton();
        btnSettings.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { settings(); }
        });
        btnSettings.setText("Settings");

        btnUtilities = new FButton();
        btnUtilities.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) { utilities(); }
        });
        btnUtilities.setText("Utilities");

        btnEditor = new FButton();
        btnEditor.setText("Deck Editor");

        add(pnlMenu, "w 36%!, h 96%!, gap 2% 2% 2% 2%");
        add(pnlContent, "w 58%!, h 96%!, gap 0% 2% 2% 2%");

        JLabel lblIcon = new JLabel(new ImageIcon(imgDirAddress + "Main_logo.png"));
        pnlMenu.add(lblIcon, "ax center");

        constraints = "w 80%!, gapleft 10%, gaptop 1%, gapbottom 1%, h 40px!";
        pnlMenu.add(btnConstructed, constraints);
        pnlMenu.add(btnSealed, constraints);
        pnlMenu.add(btnDraft, constraints);
        pnlMenu.add(btnQuest, constraints);
        pnlMenu.add(btnSettings, constraints);
        pnlMenu.add(btnUtilities, constraints);
        pnlMenu.add(btnEditor, constraints);

        // Open "constructed" screen on first run.
        constructed();
    }

    private void constructed() {
        clearToggles();
        btnConstructed.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(constructed, "w 100%!, h 100%!");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void draft() {
        clearToggles();
        btnDraft.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(draft, "w 100%!, h 100%!");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void sealed() {
        clearToggles();
        btnSealed.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(sealed, "w 100%!, h 100%!");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void quest() {
        clearToggles();
        btnQuest.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(quest, "w 99%!, h 95%!, gaptop 2.5%, gapleft 0.5%");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void settings() {
        clearToggles();
        btnSettings.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(settings, "w 99%!, h 95%!, gaptop 2.5%, gapleft 0.5%");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    private void utilities() {
        clearToggles();
        btnUtilities.setToggled(true);
        pnlContent.removeAll();
        pnlContent.add(utilities, "w 100%!, h 100%!");
        pnlContent.revalidate();
        pnlContent.repaint();
    }

    /** @return String */
    public String getImgDirAddress() {
        return imgDirAddress;
    }

    /** @return ImageIcon */
    public ImageIcon getStartButtonDown() {
        return new ImageIcon(imgDirAddress + "btnStart_Down.png");
    }

    /** @return ImageIcon */
    public ImageIcon getStartButtonOver() {
        return new ImageIcon(imgDirAddress + "btnStart_Over.png");
    }

    /** @return ImageIcon */
    public ImageIcon getStartButtonUp() {
        return new ImageIcon(imgDirAddress + "btnStart_Up.png");
    }

    private void clearToggles() {
        btnConstructed.setToggled(false);
        btnSealed.setToggled(false);
        btnDraft.setToggled(false);
        btnQuest.setToggled(false);
        btnSettings.setToggled(false);
        btnUtilities.setToggled(false);
    }

    /** */
    public void resetQuest() {
        quest = new ViewQuest(this);
        quest();
    }
}
