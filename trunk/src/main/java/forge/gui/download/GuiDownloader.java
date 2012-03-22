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
package forge.gui.download;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

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

import org.apache.commons.lang3.StringUtils;

import com.esotericsoftware.minlog.Log;

import forge.error.ErrorViewer;
import forge.properties.ForgeProps;
import forge.properties.NewConstants;
import forge.util.FileUtil;
import forge.util.MyRandom;

/**
 * <p>
 * GuiDownloadQuestImages class.
 * </p>
 * 
 * @author Forge
 */
public abstract class GuiDownloader extends DefaultBoundedRangeModel implements Runnable {

    private static final long serialVersionUID = -8596808503046590349L;

    /** Constant <code>types</code>. */
    public static final Proxy.Type[] TYPES = Proxy.Type.values();

    // proxy
    /** The type. */
    private int type;

    /** The port. */
    private JTextField addr, port;

    // progress
    /** The cards. */
    private DownloadObject[] cards;

    /** The card. */
    private int card;

    /** The cancel. */
    private boolean cancel;

    /** The bar. */
    private JProgressBar bar;

    /** The dlg. */
    private JOptionPane dlg;

    /** The close. */
    private JButton close;

    /** The times. */
    private final long[] times = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    /** The tptr. */
    private int tptr = 0;

    /** The l time. */
    private long lTime = System.currentTimeMillis();

    /**
     * <p>
     * getAverageTimePerObject.
     * </p>
     * 
     * @return a int.
     */
    protected final int getAverageTimePerObject() {
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
     * Constructor for GuiDownloader.
     * </p>
     * 
     * @param frame
     *            a {@link javax.swing.JFrame} object.
     */
    protected GuiDownloader(final JFrame frame) {

        this.cards = this.getNeededImages();

        if (this.cards.length == 0) {
            JOptionPane
                    .showMessageDialog(frame, ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.NO_MORE));
            return;
        }

        this.addr = new JTextField(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.PROXY_ADDRESS));
        this.port = new JTextField(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.PROXY_PORT));
        this.bar = new JProgressBar(this);

        final JPanel p0 = new JPanel();
        p0.setLayout(new BoxLayout(p0, BoxLayout.Y_AXIS));

        // Proxy Choice
        final ButtonGroup bg = new ButtonGroup();
        final String[] labels = { ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.NO_PROXY),
                ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.HTTP_PROXY),
                ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.SOCKS_PROXY) };
        for (int i = 0; i < GuiDownloader.TYPES.length; i++) {
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

        // Start
        final JButton b = new JButton(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.Buttons.START));
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread(GuiDownloader.this).start();
                b.setEnabled(false);
            }
        });

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
        this.close = new JButton(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.Buttons.CANCEL));
        final Object[] options = { b, this.close };
        this.dlg = new JOptionPane(p0, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

        final JDialog jdlg = this.getDlg(frame);
        jdlg.setVisible(true);
        jdlg.dispose();
        this.setCancel(true);
    }

    /** {@inheritDoc} */
    @Override
    public final int getMinimum() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public final int getValue() {
        return this.card;
    }

    /** {@inheritDoc} */
    @Override
    public final int getExtent() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public final int getMaximum() {
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
                GuiDownloader.this.fireStateChanged();

                final StringBuilder sb = new StringBuilder();

                final int a = GuiDownloader.this.getAverageTimePerObject();

                if (this.card != GuiDownloader.this.cards.length) {
                    sb.append(this.card + "/" + GuiDownloader.this.cards.length + " - ");

                    long t2Go = (GuiDownloader.this.cards.length - this.card) * a;

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
                    sb.append(String.format(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.BAR_CLOSE),
                            this.card, GuiDownloader.this.cards.length));
                }

                GuiDownloader.this.bar.setString(sb.toString());
                System.out.println(this.card + "/" + GuiDownloader.this.cards.length + " - " + a);
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
    private JDialog getDlg(final JFrame frame) {
        final JDialog dlg = this.dlg.createDialog(frame,
                ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.TITLE));
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
    public final void setCancel(final boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * <p>
     * run.
     * </p>
     */
    @Override
    public final void run() {
        BufferedInputStream in;
        BufferedOutputStream out;

        final Random r = MyRandom.getRandom();

        Proxy p = null;
        if (this.type == 0) {
            p = Proxy.NO_PROXY;
        } else {
            try {
                p = new Proxy(GuiDownloader.TYPES[this.type], new InetSocketAddress(this.addr.getText(),
                        Integer.parseInt(this.port.getText())));
            } catch (final Exception ex) {
                ErrorViewer.showError(ex,
                        ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.Errors.PROXY_CONNECT),
                        this.addr.getText(), this.port.getText());
                return;
            }
        }

        if (p != null) {
            final byte[] buf = new byte[1024];
            int len;
            for (this.update(0); (this.card < this.cards.length) && !this.cancel; this.update(this.card + 1)) {

                final String url = this.cards[this.card].getSource();
                final File fileDest =  this.cards[this.card].getDestination();
                final File base = fileDest.getParentFile();

                try {
                    // test for folder existence
                    if (!base.exists() && !base.mkdir()) { // create folder if not found
                        System.out.println("Can't create folder" + base.getAbsolutePath());
                    }

                    in = new BufferedInputStream(new URL(url).openConnection(p).getInputStream());
                    out = new BufferedOutputStream(new FileOutputStream(fileDest));

                    while ((len = in.read(buf)) != -1) {
                        // user cancelled
                        if (this.cancel) {
                            in.close();
                            out.flush();
                            out.close();

                            // delete what was written so far
                            fileDest.delete();

                            return;
                        } // if - cancel

                        out.write(buf, 0, len);
                    } // while - read and write file

                    in.close();
                    out.flush();
                    out.close();
                } catch (final ConnectException ce) {
                    System.out.println("Connection refused for url: " + url);
                } catch (final MalformedURLException mURLe) {
                    System.out.println("Error - possibly missing URL for: " + fileDest.getName());
                } catch (final FileNotFoundException fnfe) {
                    String formatStr = "Error - the LQ picture %s could not be found on the server. [%s] - %s";
                    System.out.println(String.format(formatStr, fileDest.getName(), url, fnfe.getMessage()));
                } catch (final Exception ex) {
                    Log.error("LQ Pictures", "Error downloading pictures", ex);
                }

                // throttle -- why?
                try {
                    Thread.sleep(r.nextInt(750) + 420);
                } catch (final InterruptedException e) {
                    Log.error("GuiDownloader", "Sleep Error", e);
                }
            } // for
        }
        this.close.setText(ForgeProps.getLocalized(NewConstants.Lang.GuiDownloadPictures.Buttons.CLOSE));
    } // run

    /**
     * <p>
     * getNeededCards.
     * </p>
     * 
     * @return an array of {@link forge.gui.download.GuiDownloader.DownloadObject} objects.
     */
    protected abstract DownloadObject[] getNeededImages();

    /**
     * <p>
     * readFile.
     * </p>
     * 
     * @param urlsFile
     *            a {@link java.lang.String} object.
     * @param dir
     *            a {@link java.util.File} object.
     * @return an array of {@link forge.gui.download.GuiDownloader.DownloadObject} objects.
     */
    protected static List<DownloadObject> readFile(final String urlsFile, final File dir) {
        List<String> fileLines = FileUtil.readFile(ForgeProps.getFile(urlsFile));
        final ArrayList<DownloadObject> list = new ArrayList<DownloadObject>();
        final Pattern splitter = Pattern.compile(Pattern.quote("/"));
        final Pattern replacer = Pattern.compile(Pattern.quote("%20"));

        for (String line : fileLines) {

            if (line.equals("") || line.startsWith("#")) {
                continue;
            }

            String[] parts = splitter.split(line);

            // Maybe there's a better way to do this, but I just want the
            // filename from a URL
            String last = parts[parts.length - 1];
            list.add(new DownloadObject(line, new File(dir, replacer.matcher(last).replaceAll(" "))));
        }
        return list;
    } // readFile()

    /**
     * <p>
     * readFile.
     * </p>
     * 
     * @param urlNamesFile
     *            a {@link java.lang.String} object.
     * @param dir
     *            a {@link java.util.File} object.
     * @return an array of {@link forge.gui.download.GuiDownloader.DownloadObject} objects.
     */
    protected static ArrayList<DownloadObject> readFileWithNames(final String urlNamesFile, final File dir) {
        List<String> fileLines = FileUtil.readFile(ForgeProps.getFile(urlNamesFile));
        final ArrayList<DownloadObject> list = new ArrayList<DownloadObject>();
        final Pattern splitter = Pattern.compile(Pattern.quote(" "));
        final Pattern replacer = Pattern.compile(Pattern.quote("%20"));

        for (String line : fileLines) {

            if (StringUtils.isBlank(line) || line.startsWith("#")) {
                continue;
            }
            String[] parts = splitter.split(line, 2);
            String url = parts.length > 1 ? parts[1] : null;
            list.add(new DownloadObject(url, new File(dir, replacer.matcher(parts[0]).replaceAll(" "))));
        }

        return list;
    } // readFile()

    /**
     * The Class ProxyHandler.
     */
    protected class ProxyHandler implements ChangeListener {
        private final int type;

        /**
         * Instantiates a new proxy handler.
         * 
         * @param type
         *            the type
         */
        public ProxyHandler(final int type) {
            this.type = type;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.
         * ChangeEvent)
         */
        /**
         * State changed.
         * 
         * @param e
         *            ChangeEvent
         */
        @Override
        public final void stateChanged(final ChangeEvent e) {
            if (((AbstractButton) e.getSource()).isSelected()) {
                GuiDownloader.this.type = this.type;
                GuiDownloader.this.addr.setEnabled(this.type != 0);
                GuiDownloader.this.port.setEnabled(this.type != 0);
            }
        }
    }

    /**
     * The Class DownloadObject.
     */
    protected static class DownloadObject {

        private final String source;
        private final File destination;

        DownloadObject(final String srcUrl, final File destFile) {
            source = srcUrl;
            destination = destFile;
            // System.out.println("Created download object: "+name+" "+url+" "+dir);
        }

        public String getSource() {
            return source;
        }

        public File getDestination() {
            return destination;
        }
    } // DownloadObject
}
