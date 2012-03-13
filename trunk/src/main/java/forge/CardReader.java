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
package forge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.SwingUtilities;

import net.slightlymagic.braids.util.UtilFunctions;
import net.slightlymagic.braids.util.generator.FindNonDirectoriesSkipDotDirectoriesGenerator;
import net.slightlymagic.braids.util.generator.GeneratorFunctions;

import com.google.code.jyield.Generator;
import com.google.code.jyield.YieldUtils;

import forge.card.CardRules;
import forge.card.CardRulesReader;
import forge.card.EditionInfo;
import forge.card.replacement.ReplacementHandler;
import forge.card.trigger.TriggerHandler;
import forge.error.ErrorViewer;
import forge.gui.toolbox.FProgressBar;
import forge.util.FileUtil;
import forge.util.LineReader;
import forge.view.SplashFrame;

/**
 * <p>
 * CardReader class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardReader implements Runnable {

    // PM
    private static final String CARD_FILE_DOT_EXTENSION = ".txt";
    // Braids on
    // 8/18/11
    // 11:04 PM

    /** Default charset when loading from files. */
    public static final String DEFAULT_CHARSET_NAME = "US-ASCII";
    // Braids on
    // 8/18/11
    // 10:54 PM

    /** Regex that matches a single hyphen (-) or space. */
    public static final Pattern HYPHEN_OR_SPACE = Pattern.compile("[ -]");

    /** Regex for punctuation that we omit from card file names. */
    public static final Pattern PUNCTUATION_TO_ZAP = Pattern.compile("[,'\"]");
    // by
    // Braids
    // on
    // 8/18/11
    // 10:54
    // PM

    /** Regex that matches two or more underscores (_). */
    public static final Pattern MULTIPLE_UNDERSCORES = Pattern.compile("__+");
    // by
    // Braids
    // on
    // 8/18/11
    // 10:54
    // PM

    /** Special value for estimatedFilesRemaining. */
    protected static final int UNKNOWN_NUMBER_OF_FILES_REMAINING = -1;
    // by
    // Braids
    // on
    // 8/18/11
    // 10:54
    // PM

    private transient Map<String, Card> mapToFill;
    private transient List<CardRules> listRulesToFill;
    private transient File cardsfolder;

    private transient ZipFile zip;
    private transient Charset charset;
    private transient CardRulesReader rulesReader;

    private transient Enumeration<? extends ZipEntry> zipEnum;

    private transient long estimatedFilesRemaining = CardReader.// 8/18/11 10:56
                                                                // PM
    UNKNOWN_NUMBER_OF_FILES_REMAINING;

    private transient Iterable<File> findNonDirsIterable;

    // 8/18/11 10:56 PM

    /**
     * Instantiates a new card reader.
     * 
     * @param theCardsFolder
     *            the the cards folder
     * @param theMapToFill
     *            the the map to fill
     */
    public CardReader(final File theCardsFolder, final Map<String, Card> theMapToFill) {
        this(theCardsFolder, theMapToFill, null, true);
    }

    /**
     * This is a convenience for CardReader(cardsfolder, mapToFill, true); .
     * 
     * @param theCardsFolder
     *            indicates location of the cardsFolder
     * @param theMapToFill
     *            maps card names to Card instances; this is where we place the
     *            cards once read
     * @param listRules2Fill
     *            List<CardRules>
     */
    public CardReader(final File theCardsFolder, final Map<String, Card> theMapToFill,
            final List<CardRules> listRules2Fill) {
        this(theCardsFolder, theMapToFill, listRules2Fill, true);
    }

    /**
     * <p>
     * Constructor for CardReader.
     * </p>
     * 
     * @param theCardsFolder
     *            indicates location of the cardsFolder
     * 
     * @param theMapToFill
     *            maps card names to Card instances; this is where we place the
     *            cards once read
     * @param listRules2Fill
     *            List<CardRules>
     * @param useZip
     *            if true, attempts to load cards from a zip file, if one
     *            exists.
     */
    public CardReader(final File theCardsFolder, final Map<String, Card> theMapToFill,
            final List<CardRules> listRules2Fill, final boolean useZip) {
        if (theMapToFill == null) {
            throw new NullPointerException("theMapToFill must not be null.");
            // by
            // Braids
            // on
            // 8/18/11
            // 10:53
            // PM
        }
        this.mapToFill = theMapToFill;
        // These read data for lightweight classes.
        this.listRulesToFill = listRules2Fill == null ? new ArrayList<CardRules>() : listRules2Fill;
        this.rulesReader = new CardRulesReader();

        if (!theCardsFolder.exists()) {
            throw new RuntimeException("CardReader : constructor error -- file not found -- filename is "
                    + theCardsFolder.getAbsolutePath());
        }

        if (!theCardsFolder.isDirectory()) {
            throw new RuntimeException("CardReader : constructor error -- not a directory -- "
                    + theCardsFolder.getAbsolutePath());
        }

        this.cardsfolder = theCardsFolder;

        final File zipFile = new File(theCardsFolder, "cardsfolder.zip");

        // Prepare resources to read cards lazily.
        if (useZip && zipFile.exists()) {
            try {
                this.zip = new ZipFile(zipFile);
            } catch (final Exception exn) {
                System.err.println("Error reading zip file \""
                        // Braids on
                        // 8/18/11 10:53
                        // PM
                        + zipFile.getAbsolutePath() + "\": " + exn + ". " + "Defaulting to txt files in \""
                        + theCardsFolder.getAbsolutePath() + "\".");
            }

        }

        if (useZip && (this.zip != null)) {
            this.zipEnum = this.zip.entries();
            this.estimatedFilesRemaining = this.zip.size();
        }

        this.setEncoding(CardReader.DEFAULT_CHARSET_NAME);

    } // CardReader()

    /**
     * This finalizer helps assure there is no memory or thread leak with
     * findNonDirsIterable, which was created with YieldUtils.toIterable.
     * 
     * @throws Throwable
     *             indirectly
     */
    @Override
    protected final void finalize() throws Throwable {
        try {
            if (this.findNonDirsIterable != null) {
                for (@SuppressWarnings("unused")
                final// Do nothing; just exercising the Iterable.
                File ignored : this.findNonDirsIterable) {
                }
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Reads the rest of ALL the cards into memory. This is not lazy.
     */
    @Override
    public final void run() {
        this.loadCardsUntilYouFind(null);
    }

    /**
     * Starts reading cards into memory until the given card is found.
     * 
     * After that, we save our place in the list of cards (on disk) in case we
     * need to load more.
     * 
     * @param cardName
     *            the name to find; if null, load all cards.
     * 
     * @return the Card or null if it was not found.
     */
    protected final Card loadCardsUntilYouFind(final String cardName) {
        Card result = null;
        final FProgressBar barProgress = SplashFrame.PROGRESS_BAR;

        // Iterate through txt files or zip archive.
        // Report relevant numbers to progress monitor model.
        if (this.zip == null) {
            if (this.estimatedFilesRemaining == CardReader.UNKNOWN_NUMBER_OF_FILES_REMAINING) {
                final Generator<File> findNonDirsGen = new FindNonDirectoriesSkipDotDirectoriesGenerator(
                        this.cardsfolder);
                this.estimatedFilesRemaining = GeneratorFunctions.estimateSize(findNonDirsGen);
                this.findNonDirsIterable = YieldUtils.toIterable(findNonDirsGen);
            }

            if (barProgress != null) {
                barProgress.setMaximum((int) estimatedFilesRemaining);
                SwingUtilities.invokeLater(new Runnable() { @Override
                    public void run() {
                        barProgress.setDescription("Preloading card images: ");
                } });
            }

            for (final File cardTxtFile : this.findNonDirsIterable) {
                if (!cardTxtFile.getName().endsWith(CardReader.CARD_FILE_DOT_EXTENSION)) {
                    barProgress.increment();
                    continue;
                }

                result = this.loadCard(cardTxtFile);
                barProgress.increment();

                if ((cardName != null) && cardName.equals(result.getName())) {
                    break; // no thread leak here if entire card DB is loaded,
                           // or if this object is finalized.
                }

            } // endfor
        } else {
            barProgress.setMaximum((int) this.estimatedFilesRemaining);
            ZipEntry entry;

            // zipEnum was initialized in the constructor.
            while (this.zipEnum.hasMoreElements()) {
                entry = this.zipEnum.nextElement();

                if (entry.isDirectory() || !entry.getName().endsWith(CardReader.CARD_FILE_DOT_EXTENSION)) {
                    barProgress.increment();
                    continue;
                }

                result = this.loadCard(entry);
                barProgress.increment();

                if ((cardName != null) && cardName.equals(result.getName())) {
                    break;
                }
            }
        } // endif

        return result;
    } // loadCardsUntilYouFind(String)

    /**
     * <p>
     * addTypes to an existing card.
     * </p>
     * 
     * @param card
     *            a {@link forge.Card} object.
     * @param types
     *            a {@link java.lang.String} object.
     */
    public static void addTypes(final Card card, final String types) {
        final StringTokenizer tok = new StringTokenizer(types);
        while (tok.hasMoreTokens()) {
            card.addType(tok.nextToken());
        }
    }

    /**
     * <p>
     * load a card.
     * </p>
     * 
     * @param inputStream
     *            the stream from which to load the card's information
     * 
     * @return the card loaded from the stream
     */
    protected final Card loadCard(final InputStream inputStream) {
        this.rulesReader.reset();
        
        Card card = readCard( new LineReader(inputStream, this.charset), this.rulesReader, mapToFill );

        if (card.isInAlternateState()) {
            card.setState("Original");
        }

        this.listRulesToFill.add(this.rulesReader.getCard());
        this.mapToFill.put(card.getName(), card);
        return card;
    }
    
    public static Card readCard(Iterable<String> lines)
    {
        return readCard(lines, null, null);
    }
    
    /**
     * Returns the card read from input stream
     * @param lines are input lines
     * @param rulesReader is used to fill CardPrinted characteristics 
     * @param mapToFill is used to eliminate duplicates
     * @return the card
     */
    public static Card readCard(Iterable<String> lines, CardRulesReader rulesReader, Map<String, Card> mapToFill) {
        final Card card = new Card();
        boolean ignoreTheRest = false;
        
        for(String line : lines ) {
            line = line.trim();
            
            if("End".equals(line)) { ignoreTheRest = true; continue; }   
            if(ignoreTheRest) { continue; } // have to deplete the iterator
            // otherwise the underlying class would close its stream on finalize only 

            if (line.isEmpty() || line.charAt(0) == '#') 
                continue;

            if ( null != rulesReader )
                rulesReader.parseLine(line);

            if (line.startsWith("Name:")) {
                final String value = line.substring(5);
                // System.out.println(s);
                if (mapToFill != null && mapToFill.containsKey(value)) {
                    break; // this card has already been loaded.
                } else {
                    card.setName(value);
                }
            } else if (line.startsWith("ManaCost:")) {
                final String value = line.substring(9);
                // System.out.println(s);
                if (!"no cost".equals(value)) {
                    card.setManaCost(value);
                }
            } else if (line.startsWith("Types:")) {
                CardReader.addTypes(card, line.substring("Types:".length()));
            } else if (line.startsWith("Text:")) {
                String value = line.substring("Text:".length());
                // if (!t.equals("no text"));
                if ("no text".equals(value)) {
                    value = "";
                }
                card.setText(value);
            } else if (line.startsWith("PT:")) {
                final String value = line.substring("PT:".length());
                final String[] powTough = value.split("/");
                int att;
                if (powTough[0].contains("*")) {
                    att = 0;
                } else {
                    att = Integer.parseInt(powTough[0]);
                }

                int def;
                if (powTough[1].contains("*")) {
                    def = 0;
                } else {
                    def = Integer.parseInt(powTough[1]);
                }

                card.setBaseAttackString(powTough[0]);
                card.setBaseDefenseString(powTough[1]);
                card.setBaseAttack(att);
                card.setBaseDefense(def);
            } else if (line.startsWith("Loyalty:")) {
                final String[] splitStr = line.split(":");
                final int loyal = Integer.parseInt(splitStr[1]);
                card.setBaseLoyalty(loyal);
            } else if (line.startsWith("K:")) {
                final String value = line.substring(2);
                card.addIntrinsicKeyword(value);
            } else if (line.startsWith("SVar:")) {
                final String[] value = line.split(":", 3);
                card.setSVar(value[1], value[2]);
            } else if (line.startsWith("A:")) {
                final String value = line.substring(2);
                card.addIntrinsicAbility(value);
            } else if (line.startsWith("T:")) {
                final String value = line.substring(2);
                card.addTrigger(TriggerHandler.parseTrigger(value, card, true));
            } else if (line.startsWith("S:")) {
                final String value = line.substring(2);
                card.addStaticAbilityString(value);
            } else if (line.startsWith("R:")) {
                final String value = line.substring(2);
                card.addReplacementEffect(ReplacementHandler.parseReplacement(value, card));
            } else if (line.startsWith("SetInfo:")) {
                final String value = line.substring("SetInfo:".length());
                card.addSet(new EditionInfo(value));
                // 8/18/11 11:08 PM
            } else if (line.equals("ALTERNATE")) {
                String mode;
                if (card.isFlip()) {
                    mode = "Flipped";
                } else if (card.isDoubleFaced()) {
                    mode = "Transformed";
                } else {
                    mode = card.isTransformable();
                }
                card.addAlternateState(mode);
                card.setState(mode);
            } else if (line.startsWith("AlternateMode:")) {
                final String value = line.substring("AlternateMode:".length());
                if (value.equalsIgnoreCase("Flip")) {
                    card.setFlip(true);
                } else if (value.equalsIgnoreCase("DoubleFaced")) {
                    card.setDoubleFaced(true);
                } else {
                    card.setTransformable(value);
                }
            } else if (line.startsWith("Colors:")) {
                final String value = line.substring("Colors:".length());
                final ArrayList<CardColor> newCols = new ArrayList<CardColor>();
                for (final String col : value.split(",")) {
                    final CardColor newCol = new CardColor(card);
                    newCol.addToCardColor(col);
                    newCols.add(newCol);
                }

                card.setColor(newCols);
                card.setCardColorsOverridden(true);
            }
        } // while !End
        return card;
    }

    /**
     * Set the character encoding to use when loading cards.
     * 
     * @param charsetName
     *            the name of the charset, for example, "UTF-8"
     */
    public final void setEncoding(final String charsetName) {
        this.charset = Charset.forName(charsetName);
    }

    /**
     * Load a card from a txt file.
     * 
     * @param pathToTxtFile
     *            the full or relative path to the file to load
     * 
     * @return a new Card instance
     */
    protected final Card loadCard(final File pathToTxtFile) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(pathToTxtFile);
            return this.loadCard(fileInputStream);
        } catch (final FileNotFoundException ex) {
            ErrorViewer.showError(ex, "File \"%s\" exception", pathToTxtFile.getAbsolutePath());
            throw new RuntimeException("CardReader : run error -- file exception -- filename is "
                    + pathToTxtFile.getPath(), ex);
        } finally {
            try {
                fileInputStream.close();
            } catch (final IOException ignored) {
                // 11:08
                // PM
            }
        }
    }

    /**
     * Load a card from an entry in a zip file.
     * 
     * @param entry
     *            to load from
     * 
     * @return a new Card instance
     */
    protected final Card loadCard(final ZipEntry entry) {
        InputStream zipInputStream = null;
        try {
            zipInputStream = this.zip.getInputStream(entry);
            return this.loadCard(zipInputStream);

        } catch (final IOException exn) {
            throw new RuntimeException(exn);
            // PM
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (final IOException ignored) {
                // 11:08
                // PM
            }
        }
    }

    /**
     * Attempt to guess what the path to a given card's txt file would be.
     * 
     * @param asciiCardName
     *            the card name in canonicalized ASCII form
     * 
     * @return the likeliest path of the card's txt file, excluding cardsFolder
     *         but including the subdirectory of that and the ".txt" suffix. For
     *         example, "e/elvish_warrior.txt"
     * 
     * @see CardUtil#canonicalizeCardName
     */
    public final String toMostLikelyPath(final String asciiCardName) {
        String baseFileName = asciiCardName;

        /*
         * friarsol wrote: "hyphens and spaces are converted to underscores,
         * commas and apostrophes are removed (I'm not sure if there are any
         * other punctuation used)."
         * 
         * @see
         * http://www.slightlymagic.net/forum/viewtopic.php?f=52&t=4887#p63189
         */

        baseFileName = CardReader.HYPHEN_OR_SPACE.matcher(baseFileName).replaceAll("_");
        baseFileName = CardReader.MULTIPLE_UNDERSCORES.matcher(baseFileName).replaceAll("_");
        baseFileName = CardReader.PUNCTUATION_TO_ZAP.matcher(baseFileName).replaceAll("");

        // Place the file within a single-letter subdirectory.
        final StringBuffer buf = new StringBuffer(1 + 1 + baseFileName.length()
                + CardReader.CARD_FILE_DOT_EXTENSION.length());
        buf.append(Character.toLowerCase(baseFileName.charAt(0)));

        // Zip file is always created with unix-style path names.
        buf.append('/');

        buf.append(baseFileName.toLowerCase(Locale.ENGLISH));
        buf.append(CardReader.CARD_FILE_DOT_EXTENSION);

        return buf.toString();
    }

    /**
     * Attempt to load a card by its canonical ASCII name.
     * 
     * @param canonicalASCIIName
     *            the canonical ASCII name of the card
     * 
     * @return a new Card instance having that name, or null if not found
     */
    public final Card findCard(final String canonicalASCIIName) {
        // Braids on
        // 8/18/11
        // 11:08 PM
        UtilFunctions.checkNotNull("canonicalASCIIName", canonicalASCIIName);

        final String cardFilePath = this.toMostLikelyPath(canonicalASCIIName);

        Card result = null;

        if (this.zip != null) {
            final ZipEntry entry = this.zip.getEntry(cardFilePath);

            if (entry != null) {
                result = this.loadCard(entry);
            }
        }

        if (result == null) {
            result = this.loadCard(new File(this.cardsfolder, cardFilePath));
        }

        if ((result == null) || !(result.getName().equals(canonicalASCIIName))) {
            // System.err.println(":Could not find \"" + cardFilePath + "\".");
            result = this.loadCardsUntilYouFind(canonicalASCIIName);
        }

        return result;
    }
}
