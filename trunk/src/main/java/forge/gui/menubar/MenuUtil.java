package forge.gui.menubar;

import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import forge.Singletons;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinProp;
import forge.gui.toolbox.imaging.ImageUtil;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;

public final class MenuUtil {
    private MenuUtil() { }

    private static ForgePreferences prefs = Singletons.getModel().getPreferences();

    // Get appropriate OS standard accelerator key for menu shortcuts.
    private static final int DEFAULT_MenuShortcutKeyMask = 
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    public static boolean isMenuBarVisible() {
        return !prefs.getPrefBoolean(FPref.UI_HIDE_MENUBAR);
    }
    
    public static void openUrlInBrowser(String url) { 
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            // Auto-generated catch block ignores the exception, but sends it to System.err and probably forge.log.
            e.printStackTrace();
        }
    }
        
    public static FSkin.SkinIcon getMenuIcon(SkinProp ico) {
        return ImageUtil.getMenuIcon(FSkin.getIcon(ico));       
    }
    
    public static KeyStroke getAcceleratorKey(int key) {
        return KeyStroke.getKeyStroke(key, DEFAULT_MenuShortcutKeyMask);
    }
            
    public static boolean getUserConfirmation(String prompt, String dialogTitle) {
        Object[] options = {"Yes", "No"};                    
        int reply = JOptionPane.showOptionDialog(
                null, 
                prompt, 
                dialogTitle, 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        return (reply == JOptionPane.YES_OPTION);                      
    }    

    public static void setupMenuBar(IMenuProvider provider) {
        Singletons.getControl().getMenuBar().setupMenuBar(provider);
    }
    public static void setMenuHint(final JMenuItem menu, final String hint) {
        menu.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                if (item.isArmed() || (item.isSelected() && e.getSource() instanceof JMenu)) {
                    Singletons.getControl().getMenuBar().setStatusText(hint);
                } else {
                    Singletons.getControl().getMenuBar().setStatusText("");
                }
            }
        });
    }
}
