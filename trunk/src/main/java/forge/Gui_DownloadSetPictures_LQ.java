package forge;

import com.esotericsoftware.minlog.Log;

import forge.card.CardSet;
import forge.error.ErrorViewer;
import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
//import java.util.StringTokenizer;

import static java.lang.Integer.parseInt;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;


/**
 * <p>Gui_DownloadSetPictures_LQ class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class Gui_DownloadSetPictures_LQ extends DefaultBoundedRangeModel implements Runnable, NewConstants, NewConstants.LANG.Gui_DownloadPictures {

    /** Constant <code>serialVersionUID=-7890794857949935256L</code> */
    private static final long serialVersionUID = -7890794857949935256L;

    /** Constant <code>types</code> */
    public static final Proxy.Type[] types = Proxy.Type.values();

    //proxy
    private int type;
    private JTextField addr, port;

    //progress
    private mCard[] cards;
    private int card;
    private boolean cancel;
    private JProgressBar bar;

    private JOptionPane dlg;
    private JButton close;

    private long times[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int tptr = 0;
    private long lTime = System.currentTimeMillis();

    /**
     * <p>getAverageTimePerCard.</p>
     *
     * @return a int.
     */
    private int getAverageTimePerCard() {
        int aTime = 0;
        int nz = 10;

        if (tptr > 9)
            tptr = 0;

        times[tptr] = System.currentTimeMillis() - lTime;
        lTime = System.currentTimeMillis();

        int tTime = 0;
        for (int i = 0; i < 10; i++) {
            tTime += times[i];
            if (times[i] == 0)
                nz--;
        }
        aTime = tTime / nz;

        tptr++;

        return aTime;
    }


    /**
     * <p>Constructor for Gui_DownloadSetPictures_LQ.</p>
     *
     * @param c an array of {@link forge.Gui_DownloadSetPictures_LQ.mCard} objects.
     */
    private Gui_DownloadSetPictures_LQ(mCard[] c) {
        this.cards = c;
        addr = new JTextField(ForgeProps.getLocalized(PROXY_ADDRESS));
        port = new JTextField(ForgeProps.getLocalized(PROXY_PORT));
        bar = new JProgressBar(this);

        JPanel p0 = new JPanel();
        p0.setLayout(new BoxLayout(p0, BoxLayout.Y_AXIS));

        //Proxy Choice
        ButtonGroup bg = new ButtonGroup();
        String[] labels = {
                ForgeProps.getLocalized(NO_PROXY), ForgeProps.getLocalized(HTTP_PROXY),
                ForgeProps.getLocalized(SOCKS_PROXY)};
        for (int i = 0; i < types.length; i++) {
            JRadioButton rb = new JRadioButton(labels[i]);
            rb.addChangeListener(new ProxyHandler(i));
            bg.add(rb);
            p0.add(rb);
            if (i == 0) rb.setSelected(true);
        }

        //Proxy config
        p0.add(addr);
        p0.add(port);
//        JTextField[] tfs = {addr, port};
//        String[] labels = {"Address", "Port"};
//        for(int i = 0; i < labels.length; i++) {
//            JPanel p1 = new JPanel(new BorderLayout());
//            p0.add(p1);
////            p1.add(new JLabel(labels[i]), WEST);
//            p1.add(tfs[i]);
//        }

        //Start
        final JButton b = new JButton(ForgeProps.getLocalized(BUTTONS.START));
        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new Thread(Gui_DownloadSetPictures_LQ.this).start();
                b.setEnabled(false);
            }
        });
//        p0.add(b);

        p0.add(Box.createVerticalStrut(5));

        //Progress
        p0.add(bar);
        bar.setStringPainted(true);
        //bar.setString(ForgeProps.getLocalized(BAR_BEFORE_START));
        bar.setString(card + "/" + cards.length);
        //bar.setString(String.format(ForgeProps.getLocalized(card == cards.length? BAR_CLOSE:BAR_WAIT), this.card, cards.length));
        Dimension d = bar.getPreferredSize();
        d.width = 300;
        bar.setPreferredSize(d);

        //JOptionPane
        Object[] options = {b, close = new JButton(ForgeProps.getLocalized(BUTTONS.CANCEL))};
        dlg = new JOptionPane(p0, DEFAULT_OPTION, PLAIN_MESSAGE, null, options, options[1]);
    }

    /** {@inheritDoc} */
    @Override
    public int getMinimum() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getValue() {
        return card;
    }

    /** {@inheritDoc} */
    @Override
    public int getExtent() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximum() {
        return cards == null ? 0 : cards.length;
    }

    /**
     * <p>update.</p>
     *
     * @param card a int.
     */
    private void update(int card) {
        this.card = card;

        final class Worker implements Runnable {
            private int card;

            Worker(int card) {
                this.card = card;
            }

            public void run() {
                fireStateChanged();

                StringBuilder sb = new StringBuilder();

                int a = getAverageTimePerCard();

                if (card != cards.length) {
                    sb.append(card + "/" + cards.length + " - ");

                    long t2Go = (cards.length - card) * a;

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
                    if (!secOnly)
                        sb.append(String.format("%02d remaining.", t2Go / 1000));
                    else
                        sb.append(String.format("0:%02d remaining.", t2Go / 1000));
                } else
                    sb.append(String.format(ForgeProps.getLocalized(BAR_CLOSE), card, cards.length));

                bar.setString(sb.toString());
                //bar.setString(String.format(ForgeProps.getLocalized(card == cards.length? BAR_CLOSE:BAR_WAIT), card,
                //        cards.length));
                System.out.println(card + "/" + cards.length + " - " + a);
            }
        }
        ;
        EventQueue.invokeLater(new Worker(card));
    }

    /**
     * <p>Getter for the field <code>dlg</code>.</p>
     *
     * @param frame a {@link javax.swing.JFrame} object.
     * @return a {@link javax.swing.JDialog} object.
     */
    public JDialog getDlg(JFrame frame) {
        final JDialog dlg = this.dlg.createDialog(frame, ForgeProps.getLocalized(TITLE));
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
            }
        });
        return dlg;
    }

    /**
     * <p>Setter for the field <code>cancel</code>.</p>
     *
     * @param cancel a boolean.
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }


    /**
     * <p>run.</p>
     */
    public void run() {
        BufferedInputStream in;
        BufferedOutputStream out;

        File base = ForgeProps.getFile(IMAGE_BASE);

        Random r = MyRandom.random;

        Proxy p = null;
        if (type == 0) p = Proxy.NO_PROXY;
        else try {
            p = new Proxy(types[type], new InetSocketAddress(addr.getText(), parseInt(port.getText())));
        } catch (Exception ex) {
            ErrorViewer.showError(ex, ForgeProps.getLocalized(ERRORS.PROXY_CONNECT), addr.getText(),
                    port.getText());
//            throw new RuntimeException("Gui_DownloadPictures : error 1 - " +ex);
            return;
        }

        if (p != null) {
            byte[] buf = new byte[1024];
            int len;
            System.out.println("basedir: " + base);
            for (update(0); card < cards.length && !cancel; update(card + 1)) {
                try {
                    String url = cards[card].url;
                    String cName;
                    if (cards[card].name.substring(0, 3).equals("[T]")) {
                        base = ForgeProps.getFile(IMAGE_TOKEN);
                        cName = cards[card].name.substring(3, cards[card].name.length());
                    } else {
                        base = ForgeProps.getFile(IMAGE_BASE);
                        cName = cards[card].name;
                    }
                    if (Constant.Runtime.DevMode[0])
                        System.out.println(cName + " - " + url);

                    File f = new File(base, cName);

                    //test for folder existence
                    File test = new File(base, cards[card].folder);
                    if (!test.exists()) {
                        // create folder
                        if (!test.mkdir())
                            System.out.println("Can't create folder" + cards[card].folder);
                    }

                    try {
                        in = new BufferedInputStream(new URL(url).openConnection(p).getInputStream());
                        out = new BufferedOutputStream(new FileOutputStream(f));

                        while ((len = in.read(buf)) != -1) {
                            //user cancelled
                            if (cancel) {
                                in.close();
                                out.flush();
                                out.close();

                                //delete what was written so far
                                f.delete();

                                return;
                            }//if - cancel

                            out.write(buf, 0, len);
                        }//while - read and write file

                        in.close();
                        out.flush();
                        out.close();
                    } catch (MalformedURLException mURLe) {
                        System.out.println("Error - possibly missing URL for: " + cards[card].name);
                        //Log.error("LQ Pictures", "Malformed URL for: "+cards[card].name, mURLe);
                    }
                } catch (FileNotFoundException fnfe) {
                    System.out.println("Error - the LQ picture for " + cards[card].name + " could not be found on the server. [" + cards[card].url + "] - " + fnfe.getMessage());
                } catch (Exception ex) {
                    Log.error("LQ Pictures", "Error downloading pictures", ex);
                }

                // pause

                try {
                    Thread.sleep(r.nextInt(750) + 420);
                } catch (InterruptedException e) {
                    Log.error("LQ Set Pictures", "Sleep Error", e);
                }
            }//for
        }
        close.setText(ForgeProps.getLocalized(BUTTONS.CLOSE));
    }//run

    /**
     * <p>startDownload.</p>
     *
     * @param frame a {@link javax.swing.JFrame} object.
     */
    public static void startDownload(JFrame frame) {
        final mCard[] card = getNeededCards();

        if (card.length == 0) {
            JOptionPane.showMessageDialog(frame, ForgeProps.getLocalized(NO_MORE));
            return;
        }

        Gui_DownloadSetPictures_LQ download = new Gui_DownloadSetPictures_LQ(card);
        JDialog dlg = download.getDlg(frame);
        dlg.setVisible(true);
        dlg.dispose();
        download.setCancel(true);
    }//startDownload()

    /**
     * <p>getNeededCards.</p>
     *
     * @return an array of {@link forge.Gui_DownloadSetPictures_LQ.mCard} objects.
     */
    private static mCard[] getNeededCards() {
        //read all card names and urls
        //mCard[] cardPlay = readFile(CARD_PICTURES);
        //mCard[] cardTokenLQ = readFile(CARD_PICTURES_TOKEN_LQ);

        ArrayList<mCard> CList = new ArrayList<mCard>();

        //File imgBase = ForgeProps.getFile(NewConstants.IMAGE_BASE);
        String URLBase = "http://cardforge.org/fpics/";

        for (CardPrinted c : CardDb.instance().getAllCards()) {
            String SC3 = c.getSet();
            if (StringUtils.isBlank(SC3) || "???".equals(SC3)) continue; // we don't want cards from unknown sets

            CardSet thisSet = SetUtils.getSetByCode(SC3);
            String SC2 = thisSet.getCode2(); 

            String imgFN = CardUtil.buildFilename(c);
            boolean foundSetImage = imgFN.contains(SC3) || imgFN.contains(SC2); 

            if (!foundSetImage) {
                int artsCnt = c.getCard().getSetInfo(SC3).getCopiesCount();
                String fn = CardUtil.buildIdealFilename(c.getName(), c.getArtIndex(), artsCnt); 
                //System.out.println(fn);
                CList.add(new mCard(SC3 + "/" + fn, URLBase + SC2 + "/" + Base64Coder.encodeString(fn, true), SC3));
            }
        }

        //return all card names and urls that are needed
        mCard[] out = new mCard[CList.size()];
        CList.toArray(out);

        for (int i = 0; i < out.length; i++) { System.out.println(out[i].name + " " + out[i].url); }
        
        return out;
    }//getNeededCards()

/*    *//**
     * <p>readFile.</p>
     *
     * @param ABC a {@link java.lang.String} object.
     * @return an array of {@link forge.Gui_DownloadSetPictures_LQ.mCard} objects.
     *//*
    private static mCard[] readFile(String ABC) {
        try {
            FileReader zrc = new FileReader(ForgeProps.getFile(ABC));
            BufferedReader in = new BufferedReader(zrc);
            String line;
            ArrayList<mCard> list = new ArrayList<mCard>();
            StringTokenizer tok;

            line = in.readLine();
            while (line != null && (!line.equals(""))) {
                tok = new StringTokenizer(line);
                list.add(new mCard(tok.nextToken(), tok.nextToken(), ""));

                line = in.readLine();
            }

            mCard[] out = new mCard[list.size()];
            list.toArray(out);
            return out;

        } catch (Exception ex) {
            ErrorViewer.showError(ex, "Gui_DownloadPictures: readFile() error");
            throw new RuntimeException("Gui_DownloadPictures : readFile() error");
        }
    }//readFile()
*/
    private class ProxyHandler implements ChangeListener {
        private int type;

        public ProxyHandler(int type) {
            this.type = type;
        }

        public void stateChanged(ChangeEvent e) {
            if (((AbstractButton) e.getSource()).isSelected()) {
                Gui_DownloadSetPictures_LQ.this.type = type;
                addr.setEnabled(type != 0);
                port.setEnabled(type != 0);
            }
        }
    }

    private static class mCard {
        final public String name;
        final public String url;
        final public String folder;

        mCard(String cardName, String cardURL, String cardFolder) {
            name = cardName;
            url = cardURL;
            folder = cardFolder;
        }
    }//mCard
} 
