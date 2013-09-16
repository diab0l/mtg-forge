package forge.gui.menubar;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import forge.gui.menus.ForgeMenu;
import forge.gui.menus.HelpMenu;
import forge.gui.menus.LayoutMenu;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.JLabelSkin;

@SuppressWarnings("serial")
public class FMenuBar extends JMenuBar {

    private String statusText;
    private JLabel lblStatus;
    private IMenuProvider provider;

    public FMenuBar(JFrame f) {
        f.setJMenuBar(this);
        setPreferredSize(new Dimension(f.getWidth(), 26));
        refresh();
        setStatusText(""); //set default status text
    }

    public void setupMenuBar(IMenuProvider provider0) {
        provider = provider0;
        refresh();
    }
    
    public void refresh() {
        removeAll();
        add(ForgeMenu.getMenu());
        addProviderMenus();
        add(LayoutMenu.getMenu());
        add(HelpMenu.getMenu());
        addStatusLabel();
        revalidate();
    }

    /**
     * Adds a label to the right-hand side of the MenuBar which can
     * be used to show hints or status information.
     */
    private void addStatusLabel() {
        add(Box.createHorizontalGlue()); // align right hack/patch.
        lblStatus = new JLabel(statusText);
        JLabelSkin<JLabel> labelSkin = FSkin.get(lblStatus);
        if (FSkin.isLookAndFeelSet()) {
            labelSkin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
        else { //ensure status is visible on default menu bar
            labelSkin.setForeground(getForeground());
        }
        labelSkin.setFont(FSkin.getItalicFont(11));
        lblStatus.setOpaque(false);
        add(lblStatus);
    }

    public void setStatusText(String text) {
        statusText = text.trim();
        if (statusText.isEmpty()) {
            statusText = "F1 : hide menu"; //show shortcut to hide menu if no other status to show
        }
        statusText += "  "; //add padding from right edge of menu bar
        lblStatus.setText(statusText);
    }

    private void addProviderMenus() {
        if (provider != null) {
            List<JMenu> menus = provider.getMenus();
            if (menus != null) {
                for (JMenu m : menus) {
                    m.setBorderPainted(false);
                    add(m);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }

}
