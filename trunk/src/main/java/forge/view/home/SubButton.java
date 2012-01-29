package forge.view.home;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import forge.Singletons;
import forge.view.toolbox.FSkin;

/** 
 * Standard button used for for submenus on the home screen.
 *
 */
@SuppressWarnings("serial")
public class SubButton extends JButton {
    private FSkin skin;
    /** */
    public SubButton() {
        this("");
    }

    /**
     * 
     * Standard button used for for submenus on the home screen.
     *
     * @param txt0 &emsp; String
     */
    public SubButton(String txt0) {
        super(txt0);
        skin = Singletons.getView().getSkin();
        setBorder(new LineBorder(skin.getColor(FSkin.Colors.CLR_BORDERS), 1));
        setBackground(skin.getColor(FSkin.Colors.CLR_INACTIVE));
        setForeground(skin.getColor(FSkin.Colors.CLR_TEXT));
        setVerticalTextPosition(SwingConstants.CENTER);
        setFocusPainted(false);

        this.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) { setBackground(skin.getColor(FSkin.Colors.CLR_HOVER)); }
            }

            public void mouseExited(MouseEvent e) {
                if (isEnabled()) { setBackground(skin.getColor(FSkin.Colors.CLR_INACTIVE)); }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int px =  (int) (SubButton.this.getHeight() / 2);
                px = (px < 10 ? 10 : px);
                px = (px > 15 ? 15 : px);
                SubButton.this.setFont(Singletons.getView().getSkin().getFont(px));
            }
        });
    }

    @Override
    public void setEnabled(boolean b0) {
        super.setEnabled(b0);

        if (b0) { setBackground(skin.getColor(FSkin.Colors.CLR_INACTIVE)); }
        else { setBackground(new Color(220, 220, 220)); }
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.clearRect(0, 0, getWidth(), getHeight());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
