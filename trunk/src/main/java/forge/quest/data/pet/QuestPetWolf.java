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
package forge.quest.data.pet;

import javax.swing.ImageIcon;

import forge.AllZone;
import forge.Card;
import forge.Singletons;
import forge.view.toolbox.FSkin;

/**
 * <p>
 * QuestPetWolf class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestPetWolf extends QuestPetAbstract {
    /** {@inheritDoc} */
    @Override
    public final Card getPetCard() {
        final Card petCard = new Card();

        petCard.setName("Wolf Pet");
        petCard.addController(AllZone.getHumanPlayer());
        petCard.setOwner(AllZone.getHumanPlayer());

        petCard.addColor("G");
        petCard.setToken(true);

        petCard.addType("Creature");
        petCard.addType("Wolf");
        petCard.addType("Pet");

        if (this.getLevel() == 1) {
            petCard.setImageName("G 1 1 Wolf Pet");
            petCard.setBaseAttack(1);
            petCard.setBaseDefense(1);
        } else if (this.getLevel() == 2) {
            petCard.setImageName("G 1 2 Wolf Pet");
            petCard.setBaseAttack(1);
            petCard.setBaseDefense(2);
        } else if (this.getLevel() == 3) {
            petCard.setImageName("G 2 2 Wolf Pet");
            petCard.setBaseAttack(2);
            petCard.setBaseDefense(2);
        } else if (this.getLevel() == 4) {
            petCard.setImageName("G 2 2 Wolf Pet Flanking");
            petCard.setBaseAttack(2);
            petCard.setBaseDefense(2);
            petCard.addIntrinsicKeyword("Flanking");
        }

        return petCard;
    }

    /**
     * <p>
     * Constructor for QuestPetWolf.
     * </p>
     */
    public QuestPetWolf() {
        super("Wolf", "This ferocious animal may have been raised in captivity, but it has been trained to kill.", 4);
    }

    /** {@inheritDoc} */
    @Override
    public final int[] getAllUpgradePrices() {
        return new int[] { 250, 250, 500, 550 };
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getAllUpgradeDescriptions() {
        return new String[] { "Purchase Wolf", "Improve the attack power of your wolf.",
                "Improve the defense power of your wolf.", "Give Flanking to your wolf.",
                "You cannot train your wolf any further" };
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getAllStats() {
        return new String[] { "You do not own a wolf", "1/1, G", "1/2, G", "2/2, G", "2/2, G, Flanking" };
    }

    /** {@inheritDoc} */
    @Override
    public final ImageIcon[] getAllIcons() {
        final FSkin skin = Singletons.getView().getSkin();
        return new ImageIcon[] {
                skin.getIcon(FSkin.CreatureIcons.ICO_WOLF1),
                skin.getIcon(FSkin.CreatureIcons.ICO_WOLF2),
                skin.getIcon(FSkin.CreatureIcons.ICO_WOLF3),
                skin.getIcon(FSkin.CreatureIcons.ICO_WOLF4)
        };
    }
}
