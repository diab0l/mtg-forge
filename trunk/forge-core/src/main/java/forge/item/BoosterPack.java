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

import com.google.common.base.Function;

import forge.StaticData;
import forge.card.CardEdition;
import forge.util.MyRandom;

public class BoosterPack extends SealedProduct {
    private final int artIndex;
    private final int hash;

    public static final Function<CardEdition, BoosterPack> FN_FROM_SET = new Function<CardEdition, BoosterPack>() {
        @Override
        public BoosterPack apply(final CardEdition arg1) {
            Template d = StaticData.instance().getBoosters().get(arg1.getCode());
            return new BoosterPack(arg1.getName(), d);
        }
    };

    public BoosterPack(final String name0, final Template boosterData) {
        super(name0, boosterData);
        int maxIdx = StaticData.instance().getEditions().get(boosterData.getEdition()).getCntBoosterPictures();
        artIndex = MyRandom.getRandom().nextInt(maxIdx) + 1;
        hash = super.hashCode() ^  artIndex;
    }

    public final int getArtIndex() {
        return artIndex;
    }

    @Override
    public final String getItemType() {
        return "Booster Pack";
    }

    @Override
    public final Object clone() {
        return new BoosterPack(name, contents);
    }
    
    public Template getBoosterData() {
        return contents;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        BoosterPack other = (BoosterPack)obj;
        return artIndex == other.artIndex;
    }

    @Override
    public final int hashCode() {
        return hash;
    }
}
