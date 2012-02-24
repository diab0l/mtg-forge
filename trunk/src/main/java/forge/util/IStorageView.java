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

import java.util.Collection;

/**
 * TODO: Write javadoc for this type.
 *
 * @param <T> the generic type
 */
public interface IStorageView<T> extends Iterable<T> {

    /**
     * <p>
     * getDeck.
     * </p>
     *
     * @param name the name
     * @return a {@link forge.deck.Deck} object.
     */
    T get(final String name);

    /**
     * Get names of decks.
     *
     * @return a ArrayList<String>
     */
    Collection<String> getNames();
    
    boolean contains(final String name);

}
