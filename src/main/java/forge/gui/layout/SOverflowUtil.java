package forge.gui.layout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Package-private utilities for generic overflow behavior
 * in title bar for any cell in layout.
 * 
 * <br><br><i>(S at beginning of class name denotes a static factory.)</i>
 */
final class SOverflowUtil {
    private static final MouseListener MAD_OVERFLOW = new MouseAdapter() {
        @Override
        public void mouseReleased(final MouseEvent e) {
            final JLabel src = ((JLabel) e.getSource());
            final DragCell pnlParent = ((DragCell) src.getParent().getParent());
            final JPanel pnlOverflow = FViewNew.SINGLETON_INSTANCE.getPnlTabOverflow();
            final String constraints = "w 150px!, h 20px!, gap 5px 5px 2px 2px";
            final int w = 160;
            int h = 0;

            pnlOverflow.removeAll();
            for (final IVDoc t : pnlParent.getDocs()) {
                if (t.getTabLabel().isVisible()) { continue; }
                pnlOverflow.add(new OverflowLabel(t, pnlParent), constraints);
                h += 24;
            }

            pnlOverflow.revalidate();
            pnlOverflow.setVisible(true);

            int x = src.getParent().getParent().getX() + src.getX() + FViewNew.BORDER_T;
            final int y = src.getParent().getParent().getY() + src.getY() + FViewNew.BORDER_T + src.getHeight() + 3;

            // If overflow will appear offscreen, offset.
            if (x + w > FViewNew.SINGLETON_INSTANCE.getPnlContent().getWidth()) {
                x += src.getWidth() - w;
            }

            pnlOverflow.setBounds(x, y, w, h);
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            ((JLabel) e.getSource()).setBackground(Color.cyan);
            ((JLabel) e.getSource()).repaint();
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            ((JLabel) e.getSource()).setBackground(Color.black);
            ((JLabel) e.getSource()).repaint();
        }
    };

    /** @return {@link java.awt.event.MouseListener} */
    private static final MouseListener MAD_HIDE_OVERFLOW = new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent e) {
            final JPanel pnl = FViewNew.SINGLETON_INSTANCE.getPnlTabOverflow();
            if (pnl != null) {
                pnl.setVisible(pnl.isVisible() ? false : true);
            }
        }
    };

    /** @return {@link java.awt.event.MouseListener} */
    public static MouseListener getOverflowListener() {
        return MAD_OVERFLOW;
    }

    /** @return {@link java.awt.event.MouseListener} */
    public static MouseListener getHideOverflowListener() {
        return MAD_HIDE_OVERFLOW;
    }

    @SuppressWarnings("serial")
    private static class OverflowLabel extends JLabel implements ILocalRepaint {
        public OverflowLabel(final IVDoc tab0, final DragCell parent0) {
            super(tab0.getTabLabel().getText());
            setOpaque(true);
            setBackground(Color.LIGHT_GRAY);

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(final MouseEvent e) {
                    setBackground(Color.ORANGE);
                    repaintThis();
                }

                @Override
                public void mouseExited(final MouseEvent e) {
                    setBackground(Color.LIGHT_GRAY);
                    repaintThis();
                }

                @Override
                public void mousePressed(final MouseEvent e) {
                    FViewNew.SINGLETON_INSTANCE.getPnlTabOverflow().setVisible(false);
                    parent0.setSelected(tab0);
                    parent0.rebuild();
                }
            });
        }

        public void repaintThis() {
            final Dimension d = OverflowLabel.this.getSize();
            repaint(0, 0, d.width, d.height);
        }
    }
}
