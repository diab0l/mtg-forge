package forge.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("serial")
public class UnsortedListModel<T> extends AbstractListModel<T> {
    List<T> model;

    public UnsortedListModel() {
        model = new ArrayList<T>();
    }

    @Override
    public int getSize() {
        return model.size();
    }

    @Override
    public T getElementAt(int index) {
        return model.get(index);
    }

    public void add(T element) {
        model.add(element);
        fireIntervalAdded(this, getSize() - 1, getSize() - 1);
        fireContentsChanged(this, 0, getSize() - 1);
    }

    public void addAll(T[] elements) {
        for (T e : elements) {
            model.add(e);
        }
        fireIntervalAdded(this, getSize() - elements.length, getSize() - 1);
        fireContentsChanged(this, 0, getSize() - 1);
    }

    public void addAll(Collection<T> elements) {
        if (elements.isEmpty()) {
            return;
        }
        model.addAll(elements);
        fireIntervalAdded(this, getSize() - elements.size(), getSize() - 1);
        fireContentsChanged(this, 0, getSize() - 1);
    }

    public void addAll(ListModel<T> otherModel) {
        Collection<T> elements = new ArrayList<T>();
        int size = otherModel.getSize();
        for (int i = 0; size > i; ++i) {
            elements.add((T)otherModel.getElementAt(i));
        }
        addAll(elements);
    }

    public void clear() {
        int prevSize = getSize();
        model.clear();
        fireIntervalRemoved(this, 0, prevSize - 1);
        fireContentsChanged(this, 0, prevSize - 1);
    }

    public boolean contains(Object element) {
        return model.contains(element);
    }

    public Iterator<T> iterator() {
        return model.iterator();
    }

    public void removeElement(int idx) {
        model.remove(idx);
        fireIntervalRemoved(this, idx, idx);
        fireContentsChanged(this, 0, getSize());
    }
}
