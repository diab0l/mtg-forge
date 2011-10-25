/**
 * FileType.java
 *
 * Created on 19.08.2009
 */

package tree.properties.types;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tree.properties.PropertyType;
import tree.properties.TreeProperties;

/**
 * The class FileType.
 * 
 * @author Clemens Koza
 * @version V0.0 19.08.2009
 */
public class FileType implements PropertyType<File> {
    /** Constant <code>suffix="file"</code>. */
    public static final String SUFFIX = "file";
    /** Constant <code>type</code>. */
    public static final Class<File> TYPE = File.class;

    /**
     * <p>
     * Getter for the field <code>suffix</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getSuffix() {
        return SUFFIX;
    }

    /**
     * <p>
     * Getter for the field <code>type</code>.
     * </p>
     * 
     * @return a {@link java.lang.Class} object.
     */
    public final Class<File> getType() {
        return TYPE;
    }

    /** {@inheritDoc} */
    public final File toObject(final TreeProperties p, final String s) {
        String path = getPath(s);
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        } else {
            return new File(p.getPath(), path);
        }
    }

    /**
     * Returns a path path from a property value. Three substitutions are
     * applied:
     * <ul>
     * <li>A "~/" or "~\" at the beginning is replaced with the user's home
     * directory</li>
     * <li>A "$$" anywhere is replaced with a single "$"</li>
     * <li>A "${*}", where * is any string without "}", is replaced by
     * {@link System#getProperty(String)}</li>
     * </ul>
     * 
     * @param s
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getPath(String s) {
        if (s.startsWith("~/")) {
            s = System.getProperty("user.home") + "/" + s.substring(2);
        } else if (s.startsWith("~\\")) {
            s = System.getProperty("user.home") + "\\" + s.substring(2);
        }
        Matcher m = Pattern.compile("\\$\\$|\\$\\{([^\\}]*)\\}").matcher(s);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            if (m.group().equals("$$")) {
                m.appendReplacement(result, Matcher.quoteReplacement("$"));
            } else {
                m.appendReplacement(result, Matcher.quoteReplacement(System.getProperty(m.group(1))));
            }
        }
        m.appendTail(result);
        return result.toString();
    }
}
