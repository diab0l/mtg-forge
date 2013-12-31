package forge.gui.toolbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import forge.gui.toolbox.FSkin.SkinImage;
import forge.view.FDialog;

/**
 * Class to replace JOptionPane using skinned dialogs
 *
 */
@SuppressWarnings("serial")
public class FOptionPane extends FDialog {
    public static final SkinImage QUESTION_ICON = FSkin.getIcon(FSkin.InterfaceIcons.ICO_QUESTION);
    public static final SkinImage INFORMATION_ICON = FSkin.getIcon(FSkin.InterfaceIcons.ICO_INFORMATION);
    public static final SkinImage WARNING_ICON = FSkin.getIcon(FSkin.InterfaceIcons.ICO_WARNING);
    public static final SkinImage ERROR_ICON = FSkin.getIcon(FSkin.InterfaceIcons.ICO_ERROR);

    public static void showMessageDialog(String message) {
        showMessageDialog(message, "Forge", INFORMATION_ICON);
    }

    public static void showMessageDialog(String message, String title) {
        showMessageDialog(message, title, INFORMATION_ICON);
    }

    public static void showErrorMessageDialog(String message) {
        showMessageDialog(message, "Forge", ERROR_ICON);
    }

    public static void showErrorMessageDialog(String message, String title) {
        showMessageDialog(message, title, ERROR_ICON);
    }

    public static void showMessageDialog(String message, String title, SkinImage icon) {
        showOptionDialog(message, title, icon, new String[] {"OK"}, 0);
    }

    public static boolean showConfirmDialog(String message) {
        return showConfirmDialog(message, "Forge");
    }

    public static boolean showConfirmDialog(String message, String title) {
        return showConfirmDialog(message, title, "Yes", "No", true);
    }

    public static boolean showConfirmDialog(String message, String title, boolean defaultYes) {
        return showConfirmDialog(message, title, "Yes", "No", defaultYes);
    }

    public static boolean showConfirmDialog(String message, String title, String yesButtonText, String noButtonText) {
        return showConfirmDialog(message, title, yesButtonText, noButtonText, true);
    }

    public static boolean showConfirmDialog(String message, String title, String yesButtonText, String noButtonText, boolean defaultYes) {
        String[] options = {yesButtonText, noButtonText};
        int reply = FOptionPane.showOptionDialog(message, title, QUESTION_ICON, options, defaultYes ? 0 : 1);
        return (reply == 0);
    }

    public static int showOptionDialog(String message, String title, SkinImage icon, String[] options, int defaultOption) {
        final FOptionPane optionPane = new FOptionPane(message, title, icon, null, options, defaultOption);
        optionPane.setVisible(true);
        int dialogResult = optionPane.result;
        optionPane.dispose();
        return dialogResult;
    }

    public static String showInputDialog(String message, String title) {
        return showInputDialog(message, title, null, "", null);
    }

    public static String showInputDialog(String message, String title, SkinImage icon) {
        return showInputDialog(message, title, icon, "", null);
    }

    public static String showInputDialog(String message, String title, SkinImage icon, String initialInput) {
        return showInputDialog(message, title, icon, initialInput, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T showInputDialog(String message, String title, SkinImage icon, T initialInput, T[] inputOptions) {
        final Component inputField;
        FTextField txtInput = null;
        FComboBox<T> cbInput = null;
        if (inputOptions == null) {
            txtInput = new FTextField.Builder().text(initialInput.toString()).build();
            inputField = txtInput;
        }
        else {
            cbInput = new FComboBox<T>(inputOptions);
            cbInput.setSelectedItem(initialInput);
            inputField = cbInput;
        }

        final FOptionPane optionPane = new FOptionPane(message, title, icon, inputField, new String[] {"OK", "Cancel"}, -1);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                inputField.requestFocusInWindow();
            }
        });
        inputField.addKeyListener(new KeyAdapter() { //hook so pressing Enter on field accepts dialog
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    SwingUtilities.invokeLater(new Runnable() { //delay so enter can confirm input choice first
                        @Override
                        public void run() {
                            optionPane.result = 0;
                            optionPane.setVisible(false);
                        }
                    });
                }
            }
        });
        optionPane.setVisible(true);
        int dialogResult = optionPane.result;
        optionPane.dispose();
        if (dialogResult == 0) {
            if (inputOptions == null) {
                return (T)txtInput.getText();
            }
            else {
                return (T)cbInput.getSelectedItem();
            }
        }
        return null;
    }

    private int result = -1; //default result to -1, indicating dialog closed without choosing option

    private FOptionPane(String message, String title, SkinImage icon, Component comp, String[] options, int defaultOption) {
        this.setTitle(title);

        int padding = 10;
        int x = padding;
        int gapAboveButtons = padding * 3 / 2;
        int gapBottom = comp == null ? gapAboveButtons: padding;

        if (icon != null) {
            FLabel lblIcon = new FLabel.Builder().icon(icon).build();
            int labelWidth = icon.getWidth();
            this.add(lblIcon, "x " + (x - 3) + ", ay top, w " + labelWidth + ", h " + icon.getHeight() + ", gapbottom " + gapBottom);
            x += labelWidth;
        }
        if (message != null) {
            FTextArea prompt = new FTextArea(message);
            FSkin.get(prompt).setFont(FSkin.getFont(14));
            prompt.setAutoSize(true);
            Dimension parentSize = JOptionPane.getRootFrame().getSize();
            prompt.setMaximumSize(new Dimension(parentSize.width / 2, parentSize.height - 100));
            this.add(prompt, "x " + x + ", ay top, wrap, gaptop " + (icon == null ? 0 : 7) + ", gapbottom " + gapBottom);
            x = padding;
        }
        if (comp != null) {
            this.add(comp, "x " + x + ", w 100%-" + (x + padding) + ", wrap, gapbottom " + gapAboveButtons);
        }

        //determine size of buttons
        int optionCount = options.length;
        FButton btnMeasure = new FButton(); //use blank button to aid in measurement
        FontMetrics metrics = JOptionPane.getRootFrame().getGraphics().getFontMetrics(btnMeasure.getFont());

        int maxTextWidth = 0;
        final FButton[] buttons = new FButton[optionCount];
        for (int i = 0; i < optionCount; i++) {
            int textWidth = metrics.stringWidth(options[i]);
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
            buttons[i] = new FButton(options[i]);
        }

        this.pack(); //resize dialog to fit component and title to help determine button layout

        int width = this.getWidth();
        int gapBetween = 3;
        int buttonHeight = 26;
        int buttonWidth = Math.max(maxTextWidth + btnMeasure.getMargin().left + btnMeasure.getMargin().right, 120); //account for margins and enfore minimum width
        int dx = buttonWidth + gapBetween;
        int totalButtonWidth = dx * optionCount - gapBetween;
        final int lastOption = optionCount - 1;

        //add buttons
        x = (width - totalButtonWidth) / 2;
        if (x < padding) {
            width = totalButtonWidth + 2 * padding; //increase width to make room for buttons
            x = padding;
        }
        for (int i = 0; i < optionCount; i++) {
            final int option = i;
            final FButton btn = buttons[i];
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    FOptionPane.this.result = option;
                    FOptionPane.this.setVisible(false);
                }
            });
            btn.addKeyListener(new KeyAdapter() { //hook certain keys to move focus between buttons
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (option > 0) {
                            buttons[option - 1].requestFocusInWindow();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (option < lastOption) {
                            buttons[option + 1].requestFocusInWindow();
                        }
                        break;
                    case KeyEvent.VK_HOME:
                        if (option > 0) {
                            buttons[0].requestFocusInWindow();
                        }
                        break;
                    case KeyEvent.VK_END:
                        if (option < lastOption) {
                            buttons[lastOption].requestFocusInWindow();
                        }
                        break;
                    }
                }
            });
            if (option == defaultOption) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        btn.requestFocusInWindow();
                    }
                });
            }
            this.add(btn, "x " + x + ", w " + buttonWidth + ", h " + buttonHeight);
            x += dx;
        }

        this.setSize(width, this.getHeight() + buttonHeight); //resize dialog again to account for buttons
    }
}
