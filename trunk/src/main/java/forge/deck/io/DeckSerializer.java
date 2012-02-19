/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.deck.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.filechooser.FileFilter;

import forge.Card;
import forge.deck.Deck;
import forge.item.CardPrinted;
import forge.util.FileSection;
import forge.util.FileUtil;
import forge.util.IItemSerializer;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public class DeckSerializer extends DeckReaderBase<Deck> implements IItemSerializer<Deck> {

    public DeckSerializer(File deckDir0) {
        super(deckDir0);
    }


    /** Constant <code>DCKFileFilter</code>. */
    public static final FilenameFilter DCK_FILE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".dck");
        }
    };
    /** The Constant DCK_FILTER. */
    public static final FileFilter DCK_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.getName().endsWith(".dck") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Simple Deck File .dck";
        }
    };
    /** The Constant HTML_FILTER. */
    public static final FileFilter HTML_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.getName().endsWith(".html") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Simple Deck File .html";
        }
    };


    /**
     * <p>
     * writeDeck.
     * </p>
     * 
     * @param d
     *            a {@link forge.deck.Deck} object.
     * @param out
     *            a {@link java.io.BufferedWriter} object.
     * @throws java.io.IOException
     *             if any.
     */
    private static void writeDeckHtml(final Deck d, final BufferedWriter out) throws IOException {
        Template temp = null;
        final int cardBorder = 0;
        final int height = 319;
        final int width = 222;

        /* Create and adjust the configuration */
        final Configuration cfg = new Configuration();
        try {
            cfg.setClassForTemplateLoading(d.getClass(), "/");
            cfg.setObjectWrapper(new DefaultObjectWrapper());

            /*
             * ------------------------------------------------------------------
             * -
             */
            /*
             * You usually do these for many times in the application
             * life-cycle:
             */

            /* Get or create a template */
            temp = cfg.getTemplate("proxy-template.ftl");

            /* Create a data-model */
            final Map<String, Object> root = new HashMap<String, Object>();
            root.put("title", d.getName());
            final List<String> list = new ArrayList<String>();
            for (final Card card : d.getMain().toForgeCardList().toArray()) {
                // System.out.println(card.getSets().get(card.getSets().size() -
                // 1).URL);
                list.add(card.getSets().get(card.getSets().size() - 1).getUrl());
            }
            /*
             * List<String> nameList = new ArrayList<String>(); for (Card card :
             * d.getMain().toForgeCardList().toArray()) {
             * //System.out.println(card.getSets().get(card.getSets().size() -
             * 1).URL); nameList.add(card.getName()); }
             */

            final TreeMap<String, Integer> map = new TreeMap<String, Integer>();
            for (final Entry<CardPrinted, Integer> entry : d.getMain().getOrderedList()) {
                map.put(entry.getKey().getName(), entry.getValue());
                // System.out.println(entry.getValue() + " " +
                // entry.getKey().getName());
            }

            root.put("urls", list);
            root.put("cardBorder", cardBorder);
            root.put("height", height);
            root.put("width", width);
            root.put("cardlistWidth", width - 11);
            // root.put("nameList", nameList);
            root.put("cardList", map);

            /* Merge data-model with template */
            // StringWriter sw = new StringWriter();
            temp.process(root, out);
            out.flush();
        } catch (final IOException e) {
            System.out.println(e.toString());
        } catch (final TemplateException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * <p>
     * writeDeck.
     * </p>
     * 
     * @param d
     *            a {@link forge.deck.Deck} object.
     * @param f
     *            a {@link java.io.File} object.
     */
    public static void writeDeck(final Deck d, final File f) {
        FileUtil.writeFile(f, d.save());
    }

    /**
     * <p>
     * Write deck to HTML.
     * </p>
     * 
     * @param d
     *            a {@link forge.deck.Deck} object.
     * @param f
     *            a {@link java.io.File} object.
     */
    public static void writeDeckHtml(final Deck d, final File f) {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            DeckSerializer.writeDeckHtml(d, writer);
            writer.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


    /* (non-Javadoc)
     * @see forge.deck.IDeckSerializer#save(forge.item.CardCollectionBase, java.io.File)
     */
    @Override
    public void save(Deck unit) {
        FileUtil.writeFile(makeFileFor(unit), unit.save());
    }

    /* (non-Javadoc)
     * @see forge.deck.IDeckSerializer#erase(forge.item.CardCollectionBase, java.io.File)
     */
    @Override
    public void erase(Deck unit) {
        makeFileFor(unit).delete();
    }

    /**
     * 
     * Make file name.
     * 
     * @param deckName
     *            a String
     * @param deckType
     *            a GameType
     * @return a File
     */
    public File makeFileFor(final Deck deck) {
        return new File(getDirectory(), deriveFileName(cleanDeckName(deck.getName())) + ".dck");
    }

    /* (non-Javadoc)
     * @see forge.deck.io.DeckSerializerBase#read(java.io.File)
     */
    @Override
    protected Deck read(File file) {
        return Deck.fromFile(file);
    }

    /* (non-Javadoc)
     * @see forge.deck.io.DeckSerializerBase#getFileFilter()
     */
    @Override
    protected FilenameFilter getFileFilter() {
        return DCK_FILE_FILTER;
    }

    public static DeckFileHeader readDeckMetadata(final Map<String, List<String>> map) {
        if (map == null) { return null; }
        List<String> lines = map.get("metadata");
        if (lines == null) { return null; }
        DeckFileHeader d = new DeckFileHeader(FileSection.parse(lines, "="));


        return d;
    }

}
