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
package forge.item;

import java.util.List;

import net.slightlymagic.braids.util.lambda.Lambda1;
import forge.Singletons;
import forge.card.BoosterData;
import forge.card.BoosterGenerator;
import forge.card.CardEdition;

/**
 * TODO Write javadoc for this type.
 * 
 */
public class TournamentPack extends OpenablePack {

    /** The Constant fnFromSet. */
    public static final Lambda1<TournamentPack, CardEdition> FN_FROM_SET = new Lambda1<TournamentPack, CardEdition>() {
        @Override
        public TournamentPack apply(final CardEdition arg1) {
            BoosterData d = Singletons.getModel().getTournamentPacks().get(arg1.getCode());
            return new TournamentPack(arg1.getName(), d);
        }
    };

    /**
     * Instantiates a new booster pack.
     * 
     * @param set
     *            the set
     */
    public TournamentPack(final String name0, final BoosterData boosterData) {
        super(name0, boosterData);    
    }


    @Override
    public final String getImageFilename() {
        return "tournamentpacks/" + this.contents.getEdition() + ".png";
    }


    @Override
    public final String getType() {
        return "Tournament Pack";
    }

    protected List<CardPrinted> generate() {
        final BoosterGenerator gen = new BoosterGenerator(this.contents.getEditionFilter());
        return gen.getBoosterPack(this.contents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    /**
     * Clone.
     * 
     * @return Object
     */
    @Override
    public final Object clone() {
        return new TournamentPack(name, contents);
    }


}
