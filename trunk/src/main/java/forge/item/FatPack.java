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

import java.util.ArrayList;
import java.util.List;

import net.slightlymagic.braids.util.lambda.Lambda1;
import forge.Singletons;
import forge.card.CardEdition;
import forge.card.FatPackData;

/**
 * TODO Write javadoc for this type.
 * 
 */
public class FatPack extends OpenablePack {

    /** The Constant fnFromSet. */
    public static final Lambda1<FatPack, CardEdition> FN_FROM_SET = new Lambda1<FatPack, CardEdition>() {
        @Override
        public FatPack apply(final CardEdition arg1) {
            FatPackData d = Singletons.getModel().getFatPacks().get(arg1.getCode());
            return new FatPack(arg1.getName(), d);
        }
    };

    private final FatPackData fpData;

    /**
     * Instantiates a new booster pack.
     * 
     * @param set
     *            the set
     */
    public FatPack(final String name0, final FatPackData fpData0) {
        super(name0, Singletons.getModel().getBoosters().get(fpData0.getEdition()));
        fpData = fpData0;
    }


    @Override
    public final String getImageFilename() {
        return "fatpacks/" + this.contents.getEdition();
    }


    @Override
    public final String getType() {
        return "Fat Pack";
    }

    protected List<CardPrinted> generate() {
        List<CardPrinted> result = new ArrayList<CardPrinted>();
        for (int i = 0; i < fpData.getCntBoosters(); i++) {
            result.addAll(super.generate());
        }
        CardEdition landEdition = Singletons.getModel().getEditions().get(fpData.getLandsEdition());
        result.addAll(getRandomBasicLands(landEdition, fpData.getCntLands()));
        return result;
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
        return new FatPack(name, fpData);
    }

    @Override
    public int getTotalCards() {
        return super.getTotalCards() * fpData.getCntBoosters() + fpData.getCntLands();
    }


}
