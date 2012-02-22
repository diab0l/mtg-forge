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
package forge.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

/**
 * This class treats every line of a given file as a source for a named object. 
 * 
 */
public abstract class StorageReaderFile<T extends IHasName> implements IItemReader<T> {

    private final File file;

    public StorageReaderFile(File file0) {
        file = file0;
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



    @Override
    public Map<String, T> readAll() {
        final Map<String, T> result = new TreeMap<String, T>();
        final ArrayList<String> fData = FileUtil.readFile(file);


        for (final String s : fData) {
            if (!lineContainsObject(s)) {
                continue;
            }

            T item = read(s);
            if (null == item) {
                String msg =  "An object stored in " + file.getPath() + " failed to load.\nPlease submit this as a bug with the mentioned file attached.";
                JOptionPane.showMessageDialog(null, msg);
                continue;
            }

            result.put(item.getName(), item);
        }

        return result;
    }


    /**
     * TODO: Write javadoc for this method.
     * @param file
     * @return
     */
    protected abstract T read(String line);


    protected boolean lineContainsObject(String line) {
        return !StringUtils.isBlank(line) && !line.trim().startsWith("#");
    }

}
