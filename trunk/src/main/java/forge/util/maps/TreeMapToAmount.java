package forge.util.maps;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class TreeMapToAmount<T> extends TreeMap<T, Integer> implements MapToAmount<T> {
    private static final long serialVersionUID = 5001205073403776022L;

    public TreeMapToAmount() {
        super();
    }

    public TreeMapToAmount(Comparator<? super T> comparator) {
        super(comparator);
    }

    public TreeMapToAmount(Map<? extends T, ? extends Integer> m) {
        super(m);
    }

    public TreeMapToAmount(SortedMap<T, ? extends Integer> m) {
        super(m);
    }


    @Override
    public void add(T item) {
        add(item, 1);
    }

    @Override
    public void add(T item, int amount) {
        if (amount <= 0) return; // throw an exception maybe?
        Integer cur = get(item);
        int newVal = cur == null ? amount : amount + cur.intValue();
        put(item, Integer.valueOf(newVal));
    }

    @Override
    public void addAll(Iterable<T> item) {
        for(T i : item) add(i, 1);
    }

    @Override
    public boolean substract(T item) {
        return substract(item, 1);
    }


    @Override
    public boolean substract(T item, int amount) {
        Integer cur = get(item);
        if( cur == null ) return false;
        int newVal = cur.intValue() - amount;
        if(newVal > 0)
            put(item, Integer.valueOf(newVal));
        else
            remove(item);
        return true;
    }


    @Override
    public void substractAll(Iterable<T> item) {
        for(T i : item) substract(i);
    }

    @Override
    public int countAll() {
        int c = 0;
        for(java.util.Map.Entry<T, Integer> kv : this.entrySet()) {
            c += kv.getValue().intValue();
        }
        return c;
    }


    @Override
    public int count(T item) {
        Integer cur = get(item);
        return cur == null ? 0 : cur.intValue();
    }
}
