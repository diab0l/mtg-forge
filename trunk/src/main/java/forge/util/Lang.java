package forge.util;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class Lang {

    /**
     * TODO: Write javadoc for this method.
     * @param position
     * @return
     */
    public static String getOrdinal(int position) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (position % 100) {
        case 11:
        case 12:
        case 13:
            return position + "th";
        default:
            return position + sufixes[position % 10];
        }
    }
    
    public static <T> String joinHomogenous(String s1, String s2) { 
        boolean has1 = StringUtils.isNotBlank(s1);
        boolean has2 = StringUtils.isNotBlank(s2);
        return has1 ? (has2 ? s1 + " and " + s2 : s1) : (has2 ? s2 : "");
    }
    
    public static <T> String joinHomogenous(Collection<T> objects) { return joinHomogenous(objects, null, "and"); }
    public static <T> String joinHomogenous(Collection<T> objects, Function<T, String> accessor) {
        return joinHomogenous(objects, accessor, "and");
    }
    public static <T> String joinHomogenous(Collection<T> objects, Function<T, String> accessor, String lastUnion) {
        int remaining = objects.size();
        StringBuilder sb = new StringBuilder();
        for(T obj : objects) {
            remaining--;
            if( accessor != null )
                sb.append(accessor.apply(obj));
            else 
                sb.append(obj);
            if( remaining > 1 ) sb.append(", ");
            if( remaining == 1 ) sb.append(" ").append(lastUnion).append(" ");
        }
        return sb.toString();
    }
    
    
    public static <T> String joinVerb(List<T> subjects, String verb) {
        // English is simple - just add (s) for multiple objects. 
        return subjects.size() > 1 ? verb : verb + "s";
    }

    public static String getPlural(String noun) {
        return noun + ( noun.endsWith("s") || noun.endsWith("x") ? "es" : "s");
    }
    
    public static <T> String nounWithAmount(int cnt, String noun) {
        String countedForm = cnt <= 1 ? noun : getPlural(noun);
        final String strCount;
        if( cnt == 1 )
            strCount = startsWithVowel(noun) ? "an " : "a ";
        else 
            strCount = String.valueOf(cnt) + " "; 
        return strCount + countedForm;
    }    
    
    /**
     * TODO: Write javadoc for this method.
     * @param name
     * @return
     */
    public static String getPossesive(String name) {
        return name.endsWith("s") ? name + "'" : name + "'s";
    }
    
    public static boolean startsWithVowel(String word) {
        return isVowel(word.trim().charAt(0));
    }
    
    private static final char[] vowels = { 'a', 'i', 'e', 'o', 'u' }; 
    public static boolean isVowel(char letter) {
        char l = Character.toLowerCase(letter);
        for(char c : vowels)
            if ( c == l ) return true;
        return false;
        
    }
}
