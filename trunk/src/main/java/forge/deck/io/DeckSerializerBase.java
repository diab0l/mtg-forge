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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import forge.error.ErrorViewer;
import forge.item.IHasName;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public abstract class DeckSerializerBase<T extends IHasName> implements IDeckSerializer<T> {

    private final File directory;
    
    protected final File getDirectory() {
        return directory;
    }


    public DeckSerializerBase(File deckDir0)
    {
        directory = deckDir0;
        
        if (directory == null) {
            throw new IllegalArgumentException("No deck directory specified");
        }
        try {
            if (directory.isFile()) {
                throw new IOException("Not a directory");
            } else {
                directory.mkdirs();
                if (!directory.isDirectory()) {
                    throw new IOException("Directory can't be created");
                }
            }
        } catch (final IOException ex) {
            ErrorViewer.showError(ex);
            throw new RuntimeException("DeckManager : writeDeck() error, " + ex.getMessage());
        }
    }
    
    
    // only accepts numbers, letters or dashes up to 20 characters in length
    /**
     * 
     * Clean deck name.
     * 
     * @param in
     *            a String
     * @return a String
     */
    protected String cleanDeckName(final String in) {
        final char[] c = in.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; (i < c.length) && (i < 20); i++) {
            if (Character.isLetterOrDigit(c[i]) || (c[i] == '-')) {
                sb.append(c[i]);
            }
        }
        return sb.toString();
    }


    
    @Override
    public Map<String, T> readAll() {    
        final Map<String, T> result = new TreeMap<String, T>();
        final List<String> decksThatFailedToLoad = new ArrayList<String>();
        final File[] files = directory.listFiles(getFileFilter());
        for (final File file : files) {
            try {
                final T newDeck = read(file);
                result.put(newDeck.getName(), newDeck);
            } catch (final NoSuchElementException ex) {
                final String message = String.format("%s failed to load because ---- %s", file.getName(),
                        ex.getMessage());
                decksThatFailedToLoad.add(message);
            }
        }

        if (!decksThatFailedToLoad.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    StringUtils.join(decksThatFailedToLoad, System.getProperty("line.separator")),
                    "Some of your decks were not loaded.", JOptionPane.WARNING_MESSAGE);
        }

        return result;
    }


    /**
     * TODO: Write javadoc for this method.
     * @param file
     * @return
     */
    protected abstract T read(File file);


    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    protected abstract FilenameFilter getFileFilter();

}
