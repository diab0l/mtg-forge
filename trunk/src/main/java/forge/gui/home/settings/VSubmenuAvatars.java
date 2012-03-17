package forge.gui.home.settings;

import java.awt.Dimension;
import java.awt.Image;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import forge.Command;
import forge.Singletons;
import forge.gui.WrapLayout;
import forge.gui.home.EMenuGroup;
import forge.gui.home.EMenuItem;
import forge.gui.home.ICSubmenu;
import forge.gui.home.IVSubmenu;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.properties.ForgePreferences.FPref;

/** 
 * Singleton instance of "Draft" submenu in "Constructed" group.
 */
public enum VSubmenuAvatars implements IVSubmenu {
    /** */
    SINGLETON_INSTANCE;

    /** */
    private final JPanel pnl = new JPanel();
    private final JPanel pnlAvatars = new JPanel();

    private final FLabel lblAvatarHuman = new FLabel.Builder().hoverable(true).selectable(true)
            .iconScaleFactor(0.99f).iconInBackground(true).build();
    private final FLabel lblAvatarAI = new FLabel.Builder().hoverable(true).selectable(true)
            .iconScaleFactor(0.99f).iconInBackground(true).build();

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        populateAvatars();

        pnl.removeAll();
        pnl.setOpaque(false);
        pnl.setLayout(new MigLayout("insets 0, gap 0"));
        pnl.add(new FScrollPane(pnlAvatars,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), " w 100%!, h 100%!");
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.SETTINGS;
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getPanel()
     */
    @Override
    public JPanel getPanel() {
        return pnl;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Avatars";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuName()
     */
    @Override
    public String getItemEnum() {
        return EMenuItem.SETTINGS_AVATARS.toString();
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getControl()
     */
    @Override
    public ICSubmenu getControl() {
        return CSubmenuAvatars.SINGLETON_INSTANCE;
    }

    @SuppressWarnings("serial")
    private void populateAvatars() {
        final Map<Integer, Image> avatarMap = FSkin.getAvatars();
        final JPanel pnlAvatarPics = new JPanel(new WrapLayout());
        final JPanel pnlAvatarUsers = new JPanel(new MigLayout("insets 0, gap 0, align center"));

        pnlAvatars.setOpaque(false);
        pnlAvatarUsers.setOpaque(false);
        pnlAvatarPics.setOpaque(false);

        pnlAvatarUsers.add(new FLabel.Builder().fontSize(12).text("Human").build(),
                "w 100px!, h 20px!, gap 0 20px 0 0");
        pnlAvatarUsers.add(new FLabel.Builder().fontSize(12).text("AI").build(),
                "w 100px!, h 20px!, wrap");

        pnlAvatarUsers.add(lblAvatarHuman, "w 100px!, h 100px!, gap 0 20px 0 0");
        pnlAvatarUsers.add(lblAvatarAI, "w 100px!, h 100px!");

        for (final Integer i : avatarMap.keySet()) {
            pnlAvatarPics.add(makeAvatarLabel(avatarMap.get(i), i));
        }

        pnlAvatars.removeAll();
        pnlAvatars.setLayout(new MigLayout("insets 0, gap 0"));
        pnlAvatars.add(pnlAvatarUsers, "w 90%!, h 150px!, wrap");
        pnlAvatars.add(new FScrollPane(pnlAvatarPics,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
                "w 90%!, pushy, growy, gap 5% 0 0 0");

        final Command cmdHuman = new Command() { @Override
            public void execute() { lblAvatarAI.setSelected(false); } };

        final Command cmdAI = new Command() { @Override
            public void execute() { lblAvatarHuman.setSelected(false); } };

        lblAvatarHuman.setCommand(cmdHuman);
        lblAvatarAI.setCommand(cmdAI);

        lblAvatarHuman.setSelected(true);

        final String[] indexes = Singletons.getModel().getPreferences().getPref(FPref.UI_AVATARS).split(",");
        int aiIndex = Integer.parseInt(indexes[0]);
        int humanIndex = Integer.parseInt(indexes[1]);

        if (humanIndex >= FSkin.getAvatars().size()) { humanIndex = 0; }
        if (aiIndex >= FSkin.getAvatars().size()) { aiIndex = 0; }

        lblAvatarAI.setIcon(new ImageIcon(FSkin.getAvatars().get(aiIndex)));
        lblAvatarHuman.setIcon(new ImageIcon(FSkin.getAvatars().get(humanIndex)));

        Singletons.getModel().getPreferences().setPref(FPref.UI_AVATARS, aiIndex + "," + humanIndex);
        Singletons.getModel().getPreferences().save();
    }

    @SuppressWarnings("serial")
    private FLabel makeAvatarLabel(final Image img0, final int index0) {
        final FLabel lbl = new FLabel.Builder().icon(new ImageIcon(img0)).iconScaleFactor(1.0)
                .iconAlpha(0.7f).iconInBackground(true).hoverable(true).build();

        final Dimension size = new Dimension(100, 100);
        lbl.setPreferredSize(size);
        lbl.setMaximumSize(size);
        lbl.setMinimumSize(size);

        final Command cmd = new Command() {
            @Override
            public void execute() {
                String[] indices = Singletons.getModel().getPreferences()
                        .getPref(FPref.UI_AVATARS).split(",");

                if (lblAvatarAI.isSelected()) {
                    lblAvatarAI.setIcon(new ImageIcon(FSkin.getAvatars().get(index0)));
                    lblAvatarAI.repaintOnlyThisLabel();
                    indices[0] = String.valueOf(index0);
                }
                else {
                    lblAvatarHuman.setIcon(new ImageIcon(FSkin.getAvatars().get(index0)));
                    lblAvatarHuman.repaintOnlyThisLabel();
                    indices[1] = String.valueOf(index0);
                }

                Singletons.getModel().getPreferences().setPref(FPref.UI_AVATARS, indices[0] + "," + indices[1]);
                Singletons.getModel().getPreferences().save();
            }
        };

        lbl.setCommand(cmd);
        return lbl;
    }
}
