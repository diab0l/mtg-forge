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
package forge.error;

import forge.gui.WrapLayout;
import forge.toolbox.FHyperlink;
import forge.toolbox.FLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * The class ErrorViewer. Enables showing and saving error messages that
 * occurred in forge.
 * 
 * @author Clemens Koza
 * @version V1.0 02.08.2009
 */
public class BugReportDialog {
    private static boolean dialogShown;

    public static void show(String title, String text, boolean showExitAppBtn) {
        if (dialogShown) { return; }

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 10));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JPanel helpPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));
        for (String word : BugReporter.HELP_URL_LABEL.split(" ")) {
            helpPanel.add(new FLabel.Builder().text("<html>" + word + "</html>").useSkinColors(false).build());
        }
        helpPanel.add(new FHyperlink.Builder().url(BugReporter.HELP_URL).text("<html>this post</html>").useSkinColors(false).build());
        
        JPanel p = new JPanel(new MigLayout("wrap"));
        p.add(new FLabel.Builder().text(BugReporter.HELP_TEXT).useSkinColors(false).build(), "gap 5");
        p.add(helpPanel, "w 600");
        p.add(new JScrollPane(area), "w 100%, h 100%, gaptop 5");

        // Button is not modified, String gets the automatic listener to hide
        // the dialog
        ArrayList<Object> options = new ArrayList<Object>();
        options.add(new JButton(new _CopyAndGo(area)));
        options.add(new JButton(new _SaveAction(area)));
        options.add("Close");
        if (showExitAppBtn) {
            options.add(new JButton(new _ExitAction()));
        }
        
        JOptionPane pane = new JOptionPane(p, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options.toArray(), options.get(0));
        JDialog dlg = pane.createDialog(JOptionPane.getRootFrame(), title);
        dlg.setSize(showExitAppBtn ? 780 : 600, 400);
        dlg.setResizable(true);
        dialogShown = true;
        dlg.setVisible(true);
        dlg.dispose();
        dialogShown = false;
    }

    @SuppressWarnings("serial")
    private static class _CopyAndGo extends AbstractAction {
        private final JTextArea text;
        
        public _CopyAndGo(JTextArea text) {
            super("Copy and go to forum");
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            this.text = text;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            BugReporter.copyAndGoToForums(text.getText());
        }
    }

    @SuppressWarnings("serial")
    private static class _SaveAction extends AbstractAction {
        private final JTextArea area;

        public _SaveAction(final JTextArea areaParam) {
            super("Save to file");
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            this.area = areaParam;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            BugReporter.saveToFile(area.getText());
        }
    }

    @SuppressWarnings("serial")
    private static class _ExitAction extends AbstractAction {
        public _ExitAction() {
            super("Exit application");
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            System.exit(0);
        }
    }
    
    // disable instantiation
    private BugReportDialog() { }
}
