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

import forge.card.CardRules;
import forge.util.FileUtil;
import forge.util.ThreadUtil;
import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <p>
 * CardReader class.
 * </p>
 *
 * @author Forge
 * @version $Id: CardStorageReader.java 23742 2013-11-22 16:32:56Z Max mtg $
 */

public class CardStorageReader {
    public interface Observer {
        public void cardLoaded(CardRules rules, List<String> lines, File fileOnDisk);
    }    
    
    public interface ProgressObserver{
        void setOperationName(String name, boolean usePercents);
        void report(int current, int total);
        
        // does nothing, used when they pass null instead of an instance 
        public final static ProgressObserver emptyObserver = new ProgressObserver() {
            @Override public void setOperationName(String name, boolean usePercents) {}
            @Override public void report(int current, int total) {}
        };
    }    
    
    private static final String CARD_FILE_DOT_EXTENSION = ".txt";

    /** Default charset when loading from files. */
    public static final String DEFAULT_CHARSET_NAME = "US-ASCII";

    private final boolean useThreadPool = ThreadUtil.isMultiCoreSystem();
    private final static int NUMBER_OF_PARTS = 25;

    private final ProgressObserver progressObserver;

    private transient File cardsfolder;

    private transient ZipFile zip;
    private final transient Charset charset;

    private final Observer observer;


    // 8/18/11 10:56 PM


    /**
     * <p>
     * Constructor for CardReader.
     * </p>
     *
     * @param theCardsFolder
     *            indicates location of the cardsFolder
     * @param useZip
     *            if true, attempts to load cards from a zip file, if one
     *            exists.
     */
    public CardStorageReader(String cardDataDir, CardStorageReader.ProgressObserver progressObserver, Observer observer) {
        this.progressObserver = progressObserver != null ? progressObserver : CardStorageReader.ProgressObserver.emptyObserver;
        this.cardsfolder = new File(cardDataDir);
        this.observer = observer;

        // These read data for lightweight classes.
        if (!cardsfolder.exists()) {
            throw new RuntimeException("CardReader : constructor error -- " + cardsfolder.getAbsolutePath() + " file/folder not found.");
        }

        if (!cardsfolder.isDirectory()) {
            throw new RuntimeException("CardReader : constructor error -- not a directory -- " + cardsfolder.getAbsolutePath());
        }

        final File zipFile = new File(cardsfolder, "cardsfolder.zip");

        if (zipFile.exists()) {
            try {
                this.zip = new ZipFile(zipFile);
            } catch (final Exception exn) {
                System.err.printf("Error reading zip file \"%s\": %s. Defaulting to txt files in \"%s\".%n", zipFile.getAbsolutePath(), exn, cardsfolder.getAbsolutePath());
            }
         }

        this.charset = Charset.forName(CardStorageReader.DEFAULT_CHARSET_NAME);

    } // CardReader()

    private final List<CardRules> loadCardsInRange(final List<File> files, int from, int to) {
        CardRules.Reader rulesReader = new CardRules.Reader();

        List<CardRules> result = new ArrayList<CardRules>();
        for(int i = from; i < to; i++) {
            File cardTxtFile = files.get(i);
            result.add(this.loadCard(rulesReader, cardTxtFile));
        }
        return result;
    }

    private final List<CardRules> loadCardsInRangeFromZip(final List<ZipEntry> files, int from, int to) {
        CardRules.Reader rulesReader = new CardRules.Reader();

        List<CardRules> result = new ArrayList<CardRules>();
        for(int i = from; i < to; i++) {
            ZipEntry ze = files.get(i);
            // if (ze.getName().endsWith(CardStorageReader.CARD_FILE_DOT_EXTENSION))  // already filtered!
            result.add(this.loadCard(rulesReader, ze));
        }
        return result;
    }

    /**
     * Starts reading cards into memory until the given card is found.
     *
     * After that, we save our place in the list of cards (on disk) in case we
     * need to load more.
     *
     * @return the Card or null if it was not found.
     */
    public final Iterable<CardRules> loadCards() {
        progressObserver.setOperationName("Loading cards, examining folder", true);

        // Iterate through txt files or zip archive.
        // Report relevant numbers to progress monitor model.


        Set<CardRules> result = new TreeSet<CardRules>(new Comparator<CardRules>() {
            @Override
            public int compare(CardRules o1, CardRules o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        final List<File> allFiles = collectCardFiles(new ArrayList<File>(), this.cardsfolder);
        if(!allFiles.isEmpty()) {
            int fileParts = zip == null ? NUMBER_OF_PARTS : 1 + NUMBER_OF_PARTS / 3;
            if( allFiles.size() < fileParts * 100)
                fileParts = allFiles.size() / 100; // to avoid creation of many threads for a dozen of files
            final CountDownLatch cdlFiles = new CountDownLatch(fileParts);
            List<Callable<List<CardRules>>> taskFiles = makeTaskListForFiles(allFiles, cdlFiles);
            progressObserver.setOperationName("Loading cards from folders", true);
            progressObserver.report(0, taskFiles.size());
            StopWatch sw = new StopWatch();
            sw.start();
            executeLoadTask(result, taskFiles, cdlFiles);
            sw.stop();
            final long timeOnParse = sw.getTime();
            System.out.printf("Read cards: %s files in %d ms (%d parts) %s%n", allFiles.size(), timeOnParse, taskFiles.size(), useThreadPool ? "using thread pool" : "in same thread");
        }

        if( this.zip != null ) {
            final CountDownLatch cdlZip = new CountDownLatch(NUMBER_OF_PARTS);
            List<Callable<List<CardRules>>> taskZip = new ArrayList<>();
            
            ZipEntry entry;
            List<ZipEntry> entries = new ArrayList<ZipEntry>();
            // zipEnum was initialized in the constructor.
            Enumeration<? extends ZipEntry> zipEnum = this.zip.entries();
            while (zipEnum.hasMoreElements()) {
                entry = zipEnum.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(CardStorageReader.CARD_FILE_DOT_EXTENSION))
                    continue;
                entries.add(entry);
                }

            taskZip = makeTaskListForZip(entries, cdlZip);
            progressObserver.setOperationName("Loading cards from archive", true);
            progressObserver.report(0, taskZip.size());
            StopWatch sw = new StopWatch();
            sw.start();
            executeLoadTask(result, taskZip, cdlZip);
            sw.stop();
            final long timeOnParse = sw.getTime();
            System.out.printf("Read cards: %s archived files in %d ms (%d parts) %s%n", this.zip.size(), timeOnParse, taskZip.size(), useThreadPool ? "using thread pool" : "in same thread");            
        }

        return result;
    } // loadCardsUntilYouFind(String)

    private void executeLoadTask(Collection<CardRules> result, final List<Callable<List<CardRules>>> tasks, CountDownLatch cdl) {
        try {
            if ( useThreadPool ) {
                final ExecutorService executor = ThreadUtil.getComputingPool(0.5f);
                final List<Future<List<CardRules>>> parts = executor.invokeAll(tasks);
                executor.shutdown();
                cdl.await();
                for(Future<List<CardRules>> pp : parts) {
                    result.addAll(pp.get());
                }
            } else {
                for(Callable<List<CardRules>> c : tasks) {
                    result.addAll(c.call());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) { // this clause comes from non-threaded branch
            throw new RuntimeException(e);
        }
    }

    private List<Callable<List<CardRules>>> makeTaskListForZip(final List<ZipEntry> entries, final CountDownLatch cdl) {
        int totalFiles = entries.size();
        final int maxParts = (int) cdl.getCount();
        int filesPerPart = totalFiles / maxParts;
        final List<Callable<List<CardRules>>> tasks = new ArrayList<Callable<List<CardRules>>>();
        for (int iPart = 0; iPart < maxParts; iPart++) {
            final int from = iPart * filesPerPart;
            final int till = iPart == maxParts - 1 ? totalFiles : from + filesPerPart;
            tasks.add(new Callable<List<CardRules>>() {
                @Override
                public List<CardRules> call() throws Exception{
                    List<CardRules> res = loadCardsInRangeFromZip(entries, from, till);
                    cdl.countDown();
                    progressObserver.report(maxParts - (int)cdl.getCount(), maxParts);
                    return res;
                }
            });
        }
        return tasks;
    }

    private List<Callable<List<CardRules>>> makeTaskListForFiles(final List<File> allFiles, final CountDownLatch cdl) {
        int totalFiles = allFiles.size();
        final int maxParts = (int) cdl.getCount();
        int filesPerPart = totalFiles / maxParts;
        final List<Callable<List<CardRules>>> tasks = new ArrayList<Callable<List<CardRules>>>();
        for (int iPart = 0; iPart < maxParts; iPart++) {
            final int from = iPart * filesPerPart;
            final int till = iPart == maxParts - 1 ? totalFiles : from + filesPerPart;
            tasks.add(new Callable<List<CardRules>>() {
                @Override
                public List<CardRules> call() throws Exception{
                    List<CardRules> res = loadCardsInRange(allFiles, from, till);
                    cdl.countDown();
                    progressObserver.report(maxParts - (int)cdl.getCount(), maxParts);
                    return res;
                }
            });
        }
        return tasks;
    }

    public static List<File> collectCardFiles(List<File> accumulator, File startDir) {
        String[] list = startDir.list();
        for (String filename : list) {
            File entry = new File(startDir, filename);

            if (!entry.isDirectory()) {
            if (entry.getName().endsWith(CardStorageReader.CARD_FILE_DOT_EXTENSION))
                accumulator.add(entry);
                continue;
            }
            if (filename.startsWith(".")) {
                continue;
            }

            collectCardFiles(accumulator, entry);
        }
        return accumulator;
    }


    private List<String> readScript(final InputStream inputStream) {
        return FileUtil.readAllLines(new InputStreamReader(inputStream, this.charset), true);
    }
    
    /**
     * Load a card from a txt file.
     *
     * @param pathToTxtFile
     *            the full or relative path to the file to load
     *
     * @return a new Card instance
     */
    protected final CardRules loadCard(final CardRules.Reader reader, final File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            reader.reset();
            List<String> lines = readScript(fileInputStream);
            CardRules rules = reader.readCard(lines);
            if ( null != observer )
                observer.cardLoaded(rules, lines, file);
            return rules;
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException("CardReader : run error -- file not found: " + file.getPath(), ex);
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
    protected final CardRules loadCard(final CardRules.Reader rulesReader, final ZipEntry entry) {
        InputStream zipInputStream = null;
        try {
            zipInputStream = this.zip.getInputStream(entry);
            rulesReader.reset();
            CardRules rules = rulesReader.readCard(readScript(zipInputStream));

            return rules;
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

}
