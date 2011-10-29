package forge;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.esotericsoftware.minlog.Log;

import forge.error.ErrorViewer;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.properties.NewConstants.LANG.Gui_DownloadPictures;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.util.Random;
//import java.util.StringTokenizer;

/**
 * <p>
 * Gui_MigrateLocalMWSSetPictures_HQ class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class Gui_MigrateLocalMWSSetPictures_HQ extends DefaultBoundedRangeModel implements Runnable,
        NewConstants, NewConstants.LANG.Gui_DownloadPictures {

    /** Constant <code>serialVersionUID=-7890794857949935256L</code>. */
    private static final long serialVersionUID = -7890794857949935256L;

    /** Constant <code>types</code>. */
    public static final Proxy.Type[] TYPES = Proxy.Type.values();

    // proxy
    private int type;
    private final JTextField addr, port;

    // progress
    private final MCard[] cards;
    private int card;
    private boolean cancel;
    private final JProgressBar bar;

    private final JOptionPane dlg;
    private final JButton close;

    private final long[] times = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int tptr = 0;
    private long lTime = System.currentTimeMillis();

    /**
     * <p>
     * getAverageTimePerCard.
     * </p>
     * 
     * @return a int.
     */
    private int getAverageTimePerCard() {
        int aTime = 0;
        int nz = 10;

        if (this.tptr > 9) {
            this.tptr = 0;
        }

        this.times[this.tptr] = System.currentTimeMillis() - this.lTime;
        this.lTime = System.currentTimeMillis();

        int tTime = 0;
        for (int i = 0; i < 10; i++) {
            tTime += this.times[i];
            if (this.times[i] == 0) {
                nz--;
            }
        }
        aTime = tTime / nz;

        this.tptr++;

        return aTime;
    }

    /**
     * <p>
     * Constructor for Gui_MigrateLocalMWSSetPictures_HQ.
     * </p>
     * 
     * @param c
     *            an array of
     *            {@link forge.Gui_MigrateLocalMWSSetPictures_HQ.MCard} objects.
     */
    private Gui_MigrateLocalMWSSetPictures_HQ(final MCard[] c) {
        this.cards = c;
        this.addr = new JTextField(ForgeProps.getLocalized(Gui_DownloadPictures.PROXY_ADDRESS));
        this.port = new JTextField(ForgeProps.getLocalized(Gui_DownloadPictures.PROXY_PORT));
        this.bar = new JProgressBar(this);

        final JPanel p0 = new JPanel();
        p0.setLayout(new BoxLayout(p0, BoxLayout.Y_AXIS));

        // Proxy Choice
        final ButtonGroup bg = new ButtonGroup();
        final String[] labels = { ForgeProps.getLocalized(Gui_DownloadPictures.NO_PROXY),
                ForgeProps.getLocalized(Gui_DownloadPictures.HTTP_PROXY),
                ForgeProps.getLocalized(Gui_DownloadPictures.SOCKS_PROXY) };
        for (int i = 0; i < Gui_MigrateLocalMWSSetPictures_HQ.TYPES.length; i++) {
            final JRadioButton rb = new JRadioButton(labels[i]);
            rb.addChangeListener(new ProxyHandler(i));
            bg.add(rb);
            p0.add(rb);
            if (i == 0) {
                rb.setSelected(true);
            }
        }

        // Proxy config
        p0.add(this.addr);
        p0.add(this.port);
        // JTextField[] tfs = {addr, port};
        // String[] labels = {"Address", "Port"};
        // for(int i = 0; i < labels.length; i++) {
        // JPanel p1 = new JPanel(new BorderLayout());
        // p0.add(p1);
        // // p1.add(new JLabel(labels[i]), WEST);
        // p1.add(tfs[i]);
        // }

        // Start
        final JButton b = new JButton("Start copying");
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread(Gui_MigrateLocalMWSSetPictures_HQ.this).start();
                b.setEnabled(false);
            }
        });
        // p0.add(b);

        p0.add(Box.createVerticalStrut(5));

        // Progress
        p0.add(this.bar);
        this.bar.setStringPainted(true);
        // bar.setString(ForgeProps.getLocalized(BAR_BEFORE_START));
        this.bar.setString(this.card + "/" + this.cards.length);
        // bar.setString(String.format(ForgeProps.getLocalized(card ==
        // cards.length? BAR_CLOSE:BAR_WAIT), this.card, cards.length));
        final Dimension d = this.bar.getPreferredSize();
        d.width = 300;
        this.bar.setPreferredSize(d);

        // JOptionPane
        this.close = new JButton(ForgeProps.getLocalized(BUTTONS.CANCEL));
        final Object[] options = { b, this.close };
        this.dlg = new JOptionPane(p0, JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
    }

    /** {@inheritDoc} */
    @Override
    public int getMinimum() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getValue() {
        return this.card;
    }

    /** {@inheritDoc} */
    @Override
    public int getExtent() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximum() {
        return this.cards == null ? 0 : this.cards.length;
    }

    /**
     * <p>
     * update.
     * </p>
     * 
     * @param card
     *            a int.
     */
    private void update(final int card) {
        this.card = card;

        /**
         * 
         * TODO: Write javadoc for this type.
         * 
         */
        final class Worker implements Runnable {
            private final int card;

            /**
             * 
             * TODO: Write javadoc for Constructor.
             * 
             * @param card
             *            int
             */
            Worker(final int card) {
                this.card = card;
            }

            /**
             * 
             */
            @Override
            public void run() {
                Gui_MigrateLocalMWSSetPictures_HQ.this.fireStateChanged();

                final StringBuilder sb = new StringBuilder();

                final int a = Gui_MigrateLocalMWSSetPictures_HQ.this.getAverageTimePerCard();

                if (this.card != Gui_MigrateLocalMWSSetPictures_HQ.this.cards.length) {
                    sb.append(this.card + "/" + Gui_MigrateLocalMWSSetPictures_HQ.this.cards.length + " - ");

                    long t2Go = (Gui_MigrateLocalMWSSetPictures_HQ.this.cards.length - this.card) * a;

                    boolean secOnly = true;
                    if (t2Go > 3600000) {
                        sb.append(String.format("%02d:", t2Go / 3600000));
                        t2Go = t2Go % 3600000;
                        secOnly = false;
                    }
                    if (t2Go > 60000) {
                        sb.append(String.format("%02d:", t2Go / 60000));
                        t2Go = t2Go % 60000;
                        secOnly = false;
                    }
                    if (!secOnly) {
                        sb.append(String.format("%02d remaining.", t2Go / 1000));
                    } else {
                        sb.append(String.format("0:%02d remaining.", t2Go / 1000));
                    }
                } else {
                    sb.append(String.format(ForgeProps.getLocalized(Gui_DownloadPictures.BAR_CLOSE), this.card,
                            Gui_MigrateLocalMWSSetPictures_HQ.this.cards.length));
                }

                Gui_MigrateLocalMWSSetPictures_HQ.this.bar.setString(sb.toString());
                // bar.setString(String.format(ForgeProps.getLocalized(card ==
                // cards.length? BAR_CLOSE:BAR_WAIT), card,
                // cards.length));
                System.out.println(this.card + "/" + Gui_MigrateLocalMWSSetPictures_HQ.this.cards.length + " - " + a);
            }
        }
        EventQueue.invokeLater(new Worker(card));
    }

    /**
     * <p>
     * Getter for the field <code>dlg</code>.
     * </p>
     * 
     * @param frame
     *            a {@link javax.swing.JFrame} object.
     * @return a {@link javax.swing.JDialog} object.
     */
    public JDialog getDlg(final JFrame frame) {
        final JDialog dlg = this.dlg.createDialog(frame, ForgeProps.getLocalized(Gui_DownloadPictures.TITLE));
        this.close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dlg.setVisible(false);
            }
        });
        return dlg;
    }

    /**
     * <p>
     * Setter for the field <code>cancel</code>.
     * </p>
     * 
     * @param cancel
     *            a boolean.
     */
    public void setCancel(final boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * <p>
     * run.
     * </p>
     */
    @Override
    public void run() {
        BufferedInputStream in;
        BufferedOutputStream out;

        File base = ForgeProps.getFile(NewConstants.IMAGE_BASE);

        // Random r = MyRandom.random;

        Proxy p = null;
        if (this.type == 0) {
            p = Proxy.NO_PROXY;
        } else {
            try {
                p = new Proxy(Gui_MigrateLocalMWSSetPictures_HQ.TYPES[this.type], new InetSocketAddress(
                        this.addr.getText(), Integer.parseInt(this.port.getText())));
            } catch (final Exception ex) {
                ErrorViewer.showError(ex, ForgeProps.getLocalized(ERRORS.PROXY_CONNECT), this.addr.getText(),
                        this.port.getText());
                // throw new
                // RuntimeException("Gui_DownloadPictures : error 1 - " +ex);
                return;
            }
        }

        if (p != null) {
            final byte[] buf = new byte[1024];
            int len;
            System.out.println("basedir: " + base);
            for (this.update(0); (this.card < this.cards.length) && !this.cancel; this.update(this.card + 1)) {
                try {
                    final String url = this.cards[this.card].url;
                    String cName;
                    if (this.cards[this.card].name.substring(0, 3).equals("[T]")) {
                        base = ForgeProps.getFile(NewConstants.IMAGE_TOKEN);
                        cName = this.cards[this.card].name.substring(3, this.cards[this.card].name.length());
                    } else {
                        base = ForgeProps.getFile(NewConstants.IMAGE_BASE);
                        cName = this.cards[this.card].name;
                    }

                    final File f = new File(base, cName);

                    // test for folder existence
                    final File test = new File(base, this.cards[this.card].folder);
                    if (!test.exists()) {
                        // create folder
                        if (!test.mkdir()) {
                            System.out.println("Can't create folder" + this.cards[this.card].folder);
                        }
                    }

                    try {
                        // in = new BufferedInputStream(new
                        // URL(url).openConnection(p).getInputStream());

                        final File src = new File(url);
                        final InputStream in2 = new FileInputStream(src);

                        in = new BufferedInputStream(in2);
                        out = new BufferedOutputStream(new FileOutputStream(f));

                        while ((len = in.read(buf)) != -1) {
                            // user cancelled
                            if (this.cancel) {
                                in.close();
                                out.flush();
                                out.close();

                                // delete what was written so far
                                f.delete();

                                return;
                            } // if - cancel

                            out.write(buf, 0, len);
                        } // while - read and write file

                        in.close();
                        out.flush();
                        out.close();
                    } catch (final MalformedURLException mURLe) {
                        // System.out.println("Error - possibly missing URL for: "+cards[card].name);
                        // Log.error("LQ Pictures",
                        // "Malformed URL for: "+cards[card].name, mURLe);
                    }
                } catch (final FileNotFoundException fnfe) {
                    System.out.println("Error - the HQ picture for " + this.cards[this.card].name
                            + " could not be found. [" + this.cards[this.card].url + "] - " + fnfe.getMessage());
                } catch (final Exception ex) {
                    Log.error("HQ Pictures", "Error copying pictures", ex);
                }

                // pause

                try {
                    Thread.sleep(1);
                } catch (final InterruptedException e) {
                    Log.error("HQ Set Pictures", "Sleep Error", e);
                }
            } // for
        }
        this.close.setText(ForgeProps.getLocalized(BUTTONS.CLOSE));
    } // run

    /**
     * <p>
     * startDownload.
     * </p>
     * 
     * @param frame
     *            a {@link javax.swing.JFrame} object.
     */
    public static void startDownload(final JFrame frame) {
        final MCard[] card = Gui_MigrateLocalMWSSetPictures_HQ.getNeededCards();

        if (card.length == 0) {
            JOptionPane.showMessageDialog(frame, ForgeProps.getLocalized(Gui_DownloadPictures.NO_MORE));
            return;
        }

        final Gui_MigrateLocalMWSSetPictures_HQ download = new Gui_MigrateLocalMWSSetPictures_HQ(card);
        final JDialog dlg = download.getDlg(frame);
        dlg.setVisible(true);
        dlg.dispose();
        download.setCancel(true);
    } // startDownload()

    /**
     * <p>
     * getNeededCards.
     * </p>
     * 
     * @return an array of {@link forge.Gui_MigrateLocalMWSSetPictures_HQ.MCard}
     *         objects.
     */
    private static MCard[] getNeededCards() {
        // read all card names and urls
        // mCard[] cardPlay = readFile(CARD_PICTURES);
        // mCard[] cardTokenLQ = readFile(CARD_PICTURES_TOKEN_LQ);

        final ArrayList<MCard> cList = new ArrayList<MCard>();

        // File imgBase = ForgeProps.getFile(NewConstants.IMAGE_BASE);
        final String urlBase = "C:\\MTGForge\\HQPICS\\";
        String imgFN = "";

        for (final Card c : AllZone.getCardFactory()) {
            // String url = c.getSVar("Picture");
            // String[] URLs = url.split("\\\\");

            final ArrayList<SetInfo> cSetInfo = c.getSets();
            if (cSetInfo.size() > 0) {
                for (int j = 0; j < cSetInfo.size(); j++) {
                    c.setCurSetCode(cSetInfo.get(j).Code);
                    final String setCode3 = c.getCurSetCode();
                    final String setCode2 = SetUtils.getCode2ByCode(c.getCurSetCode());

                    int n = 0;
                    if (cSetInfo.get(j).PicCount > 0) {
                        n = cSetInfo.get(j).PicCount;

                        for (int k = 1; k <= n; k++) {
                            c.setRandomPicture(k);

                            imgFN = CardUtil.buildFilename(c);

                            if (imgFN.equals("none") || (!imgFN.contains(setCode3) && !imgFN.contains(setCode2))) {
                                imgFN += k + ".jpg";
                                final String fn = GuiDisplayUtil.cleanStringMWS(c.getName()) + k + ".full.jpg";
                                // CList.add(new mCard(SC3 + "/" + fn, URLBase +
                                // SC2 + "/" + Base64Coder.encodeString(fn,
                                // true), SC3));
                                cList.add(new MCard(setCode3 + "\\" + imgFN, urlBase + setCode2 + "\\" + fn, setCode3));
                            }
                        }
                    } else {
                        c.setRandomPicture(0);

                        imgFN = CardUtil.buildFilename(c);

                        if (imgFN.equals("none") || (!imgFN.contains(setCode3) && !imgFN.contains(setCode2))) {
                            // imgFN += ".jpg";

                            final String newFileName = GuiDisplayUtil.cleanString(c.getName()) + ".jpg";

                            final String fn = GuiDisplayUtil.cleanStringMWS(c.getName()) + ".full.jpg";
                            // fn = fn.replace(" ", "%20%");
                            // CList.add(new mCard(SC3 + "/" + fn, URLBase + SC2
                            // + "/" + Base64Coder.encodeString(fn, true),
                            // SC3));
                            cList.add(new MCard(setCode3 + "\\"
                            + newFileName, urlBase + setCode2 + "\\" + fn, setCode3));

                        }

                    }
                }

            }

            // Log.error(iName + ".jpg" + "\t" + URLs[0]);

        }

        // ArrayList<mCard> list = new ArrayList<mCard>();
        // File file;
        /*
         * File base = ForgeProps.getFile(IMAGE_TOKEN); for(int i = 0; i <
         * cardTokenLQ.length; i++) { file = new File(base,
         * cardTokenLQ[i].name.substring(3, cardTokenLQ[i].name.length()));
         * if(!file.exists()) CList.add(cardTokenLQ[i]); }
         */
        // return all card names and urls that are needed
        final MCard[] out = new MCard[cList.size()];
        cList.toArray(out);

        for (final MCard element : out) {
            System.out.println(element.name + " " + element.url);
        }
        return out;
    } // getNeededCards()

    /*
     * private static mCard[] readFile(String ABC) { try { FileReader zrc = new
     * FileReader(ForgeProps.getFile(ABC)); BufferedReader in = new
     * BufferedReader(zrc); String line; ArrayList<mCard> list = new
     * ArrayList<mCard>(); StringTokenizer tok;
     * 
     * line = in.readLine(); while(line != null && (!line.equals(""))) { tok =
     * new StringTokenizer(line); list.add(new mCard(tok.nextToken(),
     * tok.nextToken(), ""));
     * 
     * line = in.readLine(); }
     * 
     * mCard[] out = new mCard[list.size()]; list.toArray(out); return out;
     * 
     * } catch(Exception ex) { ErrorViewer.showError(ex,
     * "Gui_DownloadPictures: readFile() error"); throw new
     * RuntimeException("Gui_DownloadPictures : readFile() error"); }
     * }//readFile()
     */

    private class ProxyHandler implements ChangeListener {
        private final int type;

        public ProxyHandler(final int type) {
            this.type = type;
        }

        @Override
        public void stateChanged(final ChangeEvent e) {
            if (((AbstractButton) e.getSource()).isSelected()) {
                Gui_MigrateLocalMWSSetPictures_HQ.this.type = this.type;
                Gui_MigrateLocalMWSSetPictures_HQ.this.addr.setEnabled(this.type != 0);
                Gui_MigrateLocalMWSSetPictures_HQ.this.port.setEnabled(this.type != 0);
            }
        }
    }

    private static class MCard {
        private final String name;
        private final String url;
        private final String folder;

        MCard(final String cardName, final String cardURL, final String cardFolder) {
            this.name = cardName;
            this.url = cardURL;
            this.folder = cardFolder;
        }
    } // mCard
}
