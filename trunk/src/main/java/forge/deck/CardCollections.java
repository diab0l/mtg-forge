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
package forge.deck;

import java.io.File;

import forge.deck.io.DeckGroupSerializer;
import forge.deck.io.DeckSerializer;
import forge.deck.io.OldDeckParser;
import forge.util.StorageImmediatelySerialized;
import forge.util.IStorage;

/**
 * Holds editable maps of decks saved to disk. Adding or removing items to(from)
 * such map turns into immediate file update
 */
public class CardCollections {
    private final IStorage<Deck> constructed;
    private final IStorage<DeckGroup> draft;
    private final IStorage<DeckGroup> sealed;
    private final IStorage<Deck> cube;

    /**
     * TODO: Write javadoc for Constructor.
     *
     * @param file the file
     */
    public CardCollections(final File file) {
        this.constructed = new StorageImmediatelySerialized<Deck>(new DeckSerializer(new File(file, "constructed")));
        this.draft = new StorageImmediatelySerialized<DeckGroup>(new DeckGroupSerializer(new File(file, "draft")));
        this.sealed = new StorageImmediatelySerialized<DeckGroup>(new DeckGroupSerializer(new File(file, "sealed")));
        this.cube = new StorageImmediatelySerialized<Deck>(new DeckSerializer(new File(file, "cube")));

        System.out.printf("Read decks: %d constructed, %d sealed, %d draft, %d cubes.%n", constructed.getCount(), sealed.getCount(), draft.getCount(), cube.getCount());

        // remove this after most people have been switched to new layout
        final OldDeckParser oldParser = new OldDeckParser(file, this.constructed, this.draft, this.sealed, this.cube);
        oldParser.tryParse();
    }

    /**
     * Gets the constructed.
     *
     * @return the constructed
     */
    public final IStorage<Deck> getConstructed() {
        return this.constructed;
    }

    /**
     * Gets the draft.
     *
     * @return the draft
     */
    public final IStorage<DeckGroup> getDraft() {
        return this.draft;
    }

    /**
     * Gets the cubes.
     *
     * @return the cubes
     */
    public final IStorage<Deck> getCubes() {
        return this.cube;
    }

    /**
     * Gets the sealed.
     *
     * @return the sealed
     */
    public IStorage<DeckGroup> getSealed() {
        return this.sealed;
    }

}
