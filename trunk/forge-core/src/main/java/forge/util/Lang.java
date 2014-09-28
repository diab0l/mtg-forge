package forge.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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

    public static <T> String joinHomogenous(Iterable<T> objects) { return joinHomogenous(Lists.newArrayList(objects)); }
    public static <T> String joinHomogenous(Collection<T> objects) { return joinHomogenous(objects, null, "and"); }
    public static <T> String joinHomogenous(Collection<T> objects, Function<T, String> accessor) {
        return joinHomogenous(objects, accessor, "and");
    }
    public static <T> String joinHomogenous(Collection<T> objects, Function<T, String> accessor, String lastUnion) {
        int remaining = objects.size();
        StringBuilder sb = new StringBuilder();
        for (T obj : objects) {
            remaining--;
            if (accessor != null) {
                sb.append(accessor.apply(obj));
            }
            else {
                sb.append(obj);
            }
            if (remaining > 1) {
                sb.append(", ");
            }
            else if (remaining == 1) {
                sb.append(" ").append(lastUnion).append(" ");
            }
        }
        return sb.toString();
    }

    public static <T> String joinVerb(List<T> subjects, String verb) {
        return subjects.size() > 1 || !subjectIsSingle3rdPerson(Iterables.getFirst(subjects, "it").toString()) ? verb : verbs3rdPersonSingular(verb);
    }

    public static String joinVerb(String subject, String verb) {
        return !Lang.subjectIsSingle3rdPerson(subject) ? verb : verbs3rdPersonSingular(verb);
    }

    public static boolean subjectIsSingle3rdPerson(String subject) {
        // Will be most simple
        return !"You".equalsIgnoreCase(subject);
    }

    public static String verbs3rdPersonSingular(String verb) {
        // English is simple - just add (s) for multiple objects.
        return verb + "s";
    }

    public static String getPlural(String noun) {
        return noun + (noun.endsWith("s") || noun.endsWith("x") || noun.endsWith("ch") ? "es" : "s");
    }

    public static <T> String nounWithAmount(int cnt, String noun) {
        String countedForm = cnt == 1 ? noun : getPlural(noun);
        final String strCount;
        if (cnt == 1) {
            strCount = startsWithVowel(noun) ? "an " : "a ";
        }
        else {
            strCount = String.valueOf(cnt) + " ";
        }
        return strCount + countedForm;
    }

    public static <T> String nounWithNumeral(int cnt, String noun) {
        String countedForm = cnt == 1 ? noun : getPlural(noun);
        return getNumeral(cnt) + " " + countedForm;
    }

    public static String getPossesive(String name) {
        if ("You".equalsIgnoreCase(name)) return name + "r"; // to get "your"
        return name.endsWith("s") ? name + "'" : name + "'s";
    }

    public static String getPossessedObject(String owner, String object) {
        return getPossesive(owner) + " " + object;
    }
    
    public static boolean startsWithVowel(String word) {
        return isVowel(word.trim().charAt(0));
    }

    private static final Pattern VOWEL_PATTERN = Pattern.compile("[aeiou]", Pattern.CASE_INSENSITIVE);
    public static boolean isVowel(char letter) {
        return VOWEL_PATTERN.matcher(String.valueOf(letter)).find();
    }

    public final static String[] numbers0 = new String[] {
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eightteen", "nineteen" };
    public final static String[] numbers20 = new String[] {"twenty", "thirty", "fourty", "fifty", "sixty", "seventy", "eighty", "ninety" };

    public static String getNumeral(int n) {
        String prefix = n < 0 ? "minus " : "";
        n = Math.abs(n);
        if (n >= 0 && n < 20) {
            return prefix + numbers0[n];
        }
        if (n < 100) {
            int n1 = n % 10;
            String ones = n1 == 0 ? "" : numbers0[n1];
            return prefix + numbers20[(n / 10) - 2] + " " + ones;
        }
        return Integer.toString(n);
    }
}
