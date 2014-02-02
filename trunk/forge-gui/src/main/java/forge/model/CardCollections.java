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
package forge.model;

import java.io.File;

import org.apache.commons.lang3.time.StopWatch;

import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.io.DeckGroupSerializer;
import forge.deck.io.DeckStorage;
import forge.deck.io.OldDeckParser;
import forge.properties.NewConstants;
import forge.util.storage.IStorage;
import forge.util.storage.StorageImmediatelySerialized;

/**
 * Holds editable maps of decks saved to disk. Adding or removing items to(from)
 * such map turns into immediate file update
 */
public class CardCollections {
    private final IStorage<Deck> constructed;
    private final IStorage<DeckGroup> draft;
    private final IStorage<DeckGroup> sealed;
    private final IStorage<Deck> cube;
    private final IStorage<Deck> scheme;
    private final IStorage<Deck> plane;
    private final IStorage<Deck> commander;

    /**
     * TODO: Write javadoc for Constructor.
     *
     * @param file the file
     */
    public CardCollections() {
        StopWatch sw = new StopWatch();
        sw.start();
        this.constructed = new StorageImmediatelySerialized<Deck>("Constructed decks", new DeckStorage(new File(NewConstants.DECK_CONSTRUCTED_DIR), true), true);
        this.draft = new StorageImmediatelySerialized<DeckGroup>("Draft deck sets", new DeckGroupSerializer(new File(NewConstants.DECK_DRAFT_DIR)));
        this.sealed = new StorageImmediatelySerialized<DeckGroup>("Sealed deck sets", new DeckGroupSerializer(new File(NewConstants.DECK_SEALED_DIR)));
        this.cube = new StorageImmediatelySerialized<Deck>("Cubes", new DeckStorage(new File(NewConstants.DECK_CUBE_DIR)));
        this.scheme = new StorageImmediatelySerialized<Deck>("Archenemy decks", new DeckStorage(new File(NewConstants.DECK_SCHEME_DIR)));
        this.plane = new StorageImmediatelySerialized<Deck>("Planechase decks", new DeckStorage(new File(NewConstants.DECK_PLANE_DIR)));
        this.commander = new StorageImmediatelySerialized<Deck>("Commander decks", new DeckStorage(new File(NewConstants.DECK_COMMANDER_DIR)));
        
        sw.stop();
        System.out.printf("Read decks (%d ms): %d constructed, %d sealed, %d draft, %d cubes, %d scheme, %d planar, %d commander.%n", sw.getTime(), constructed.size(), sealed.size(), draft.size(), cube.size(), scheme.size(), plane.size(),commander.size());
//        int sum = constructed.size() + sealed.size() + draft.size() + cube.size() + scheme.size() + plane.size();
//        FSkin.setProgessBarMessage(String.format("Loaded %d decks in %f sec", sum, sw.getTime() / 1000f ));
        // remove this after most people have been switched to new layout
        final OldDeckParser oldParser = new OldDeckParser(this.constructed, this.draft, this.sealed, this.cube);
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

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public IStorage<Deck> getScheme() {
        return this.scheme;
    }

    /**
     * @return the plane
     */
    public IStorage<Deck> getPlane() {
        return plane;
    }
    
    /**
     * @return the plane
     */
    public IStorage<Deck> getCommander() {
        return commander;
    }

}
