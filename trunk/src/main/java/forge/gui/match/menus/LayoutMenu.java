package forge.gui.match.menus;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import forge.Singletons;
import forge.gui.match.controllers.CDock;
import forge.gui.menubar.MenuUtil;
import forge.gui.toolbox.FSkin;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.view.FView;

/**
 * Returns a JMenu containing options associated with game screen layout.
 * <p>
 * Replicates options available in Dock tab.
 */
public final class LayoutMenu {
    private LayoutMenu() { }

    private static CDock controller =  CDock.SINGLETON_INSTANCE;
    private static ForgePreferences prefs = Singletons.getModel().getPreferences();
    private static boolean showIcons;

    public static JMenu getMenu(boolean showMenuIcons) {
        showIcons = showMenuIcons;
        JMenu menu = new JMenu("Layout");
        menu.setMnemonic(KeyEvent.VK_L);
        menu.add(getMenu_ViewOptions());
        menu.add(getMenu_FileOptions());
        menu.addSeparator();
        menu.add(getMenuItem_RevertLayout());
        return menu;
    }

    private static JMenu getMenu_ViewOptions() {
        JMenu menu = new JMenu("View");
        menu.add(getMenuItem_ShowTabs());
        menu.add(getMenuItem_ShowBackgroundImage());
        return menu;
    }

    private static JMenu getMenu_FileOptions() {
        JMenu menu = new JMenu("File");
        menu.add(getMenuItem_OpenLayout());
        menu.add(getMenuItem_SaveLayout());
        return menu;
    }


    private static JMenuItem getMenuItem_ShowBackgroundImage() {
        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Background Image");
        menuItem.setState(prefs.getPrefBoolean(FPref.UI_MATCH_IMAGE_VISIBLE));
        menuItem.addActionListener(getShowBackgroundImageAction(menuItem));
        return menuItem;
    }

    private static ActionListener getShowBackgroundImageAction(final JCheckBoxMenuItem menuItem) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = menuItem.getState();
                prefs.setPref(FPref.UI_MATCH_IMAGE_VISIBLE, isVisible);
                if (isVisible) {
                    FView.SINGLETON_INSTANCE.getPnlInsets().setForegroundImage(FSkin.getIcon(FSkin.Backgrounds.BG_MATCH));
                } else {
                    FView.SINGLETON_INSTANCE.getPnlInsets().setForegroundImage((Image)null);
                }
                FView.SINGLETON_INSTANCE.getPnlInsets().repaint();
            }
        };
    }

    private static JMenuItem getMenuItem_ShowTabs() {
        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Panel Tabs");
        menuItem.setAccelerator(MenuUtil.getAcceleratorKey(KeyEvent.VK_T));
        menuItem.setState(!prefs.getPrefBoolean(FPref.UI_HIDE_GAME_TABS));
        menuItem.addActionListener(getShowTabsAction(menuItem));
        return menuItem;
    }
    private static ActionListener getShowTabsAction(final JCheckBoxMenuItem menuItem) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setPref(FPref.UI_HIDE_GAME_TABS, !menuItem.getState());
                controller.revertLayout();
            }
        };
    }
    private static JMenuItem getMenuItem_SaveLayout() {
        JMenuItem menuItem = new JMenuItem("Save Current Layout");
        menuItem.setIcon((showIcons ? MenuUtil.getMenuIcon(FSkin.DockIcons.ICO_SAVELAYOUT) : null));
        menuItem.addActionListener(getSaveLayoutAction());
        return menuItem;
    }

    private static ActionListener getSaveLayoutAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.saveLayout();
            }
        };
    }

    private static JMenuItem getMenuItem_OpenLayout() {
        JMenuItem menuItem = new JMenuItem("Open...");
        menuItem.setIcon((showIcons ? MenuUtil.getMenuIcon(FSkin.DockIcons.ICO_OPENLAYOUT) : null));
        menuItem.addActionListener(getOpenLayoutAction());
        return menuItem;
    }

    private static ActionListener getOpenLayoutAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openLayout();
            }
        };
    }

    private static JMenuItem getMenuItem_RevertLayout() {
        JMenuItem menuItem = new JMenuItem("Refresh");
        menuItem.setIcon((showIcons ? MenuUtil.getMenuIcon(FSkin.DockIcons.ICO_REVERTLAYOUT) : null));
        menuItem.addActionListener(getRevertLayoutAction());
        return menuItem;
    }

    private static ActionListener getRevertLayoutAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.revertLayout();
            }
        };
    }

}
