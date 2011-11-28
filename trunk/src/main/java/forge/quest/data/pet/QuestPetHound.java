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

import forge.AllZone;
import forge.Card;
import forge.card.abilityfactory.AbilityFactory;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;

/**
 * <p>
 * QuestPetHound class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class QuestPetHound extends QuestPetAbstract {
    /**
     * <p>
     * Constructor for QuestPetHound.
     * </p>
     */
    public QuestPetHound() {
        super("Hound", "Dogs are said to be man's best friend. Definitely not this one.", 4);
    }

    /** {@inheritDoc} */
    @Override
    public final Card getPetCard() {
        final Card petCard = new Card();

        petCard.setName("Hound Pet");
        petCard.addController(AllZone.getHumanPlayer());
        petCard.setOwner(AllZone.getHumanPlayer());

        petCard.addColor("R");
        petCard.setToken(true);

        petCard.addType("Creature");
        petCard.addType("Hound");
        petCard.addType("Pet");

        if (this.getLevel() == 1) {
            petCard.setImageName("R 1 1 Hound Pet");
            petCard.setBaseAttack(1);
            petCard.setBaseDefense(1);
        } else if (this.getLevel() == 2) {
            petCard.setImageName("R 1 1 Hound Pet Haste");
            petCard.setBaseAttack(1);
            petCard.setBaseDefense(1);
            petCard.addIntrinsicKeyword("Haste");
        } else if (this.getLevel() == 3) {
            petCard.setImageName("R 2 1 Hound Pet");
            petCard.setBaseAttack(2);
            petCard.setBaseDefense(1);
            petCard.addIntrinsicKeyword("Haste");

        } else if (this.getLevel() == 4) {
            petCard.setImageName("R 2 1 Hound Pet Alone");
            petCard.setBaseAttack(2);
            petCard.setBaseDefense(1);
            petCard.addIntrinsicKeyword("Haste");

            final Trigger myTrigger = TriggerHandler
                    .parseTrigger(
                            "Mode$ Attacks | ValidCard$ Card.Self | Alone$ True | TriggerDescription$ Whenever CARDNAME attacks alone, it gets +2/+0 until end of turn.",
                            petCard, true);
            final AbilityFactory af = new AbilityFactory();
            myTrigger.setOverridingAbility(af.getAbility("AB$Pump | Cost$ 0 | Defined$ Self | NumAtt$ 2", petCard));
            petCard.addTrigger(myTrigger);
        }

        return petCard;
    }

    /** {@inheritDoc} */
    @Override
    public final int[] getAllUpgradePrices() {
        return new int[] { 200, 350, 450, 750 };
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getAllUpgradeDescriptions() {
        return new String[] { "Purchase hound", "Give Haste to your hound.", "Improve the attack power of your hound.",
                "Greatly improves your hound's attack power if it attacks alone.",
                "You cannot train your hound any further" };
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getAllStats() {
        return new String[] { "You do not own a hound", "1/1, R", "1/1, R, Haste", "2/1, R, Haste",
                "2/1, R, Haste, Whenever this creature attacks alone, it gets +2/+0 until end of turn." };
    }

    /** {@inheritDoc} */
    @Override
    public final String[] getAllImageNames() {
        return new String[] { "", "r_1_1_hound_pet_small.jpg", "r_1_1_hound_pet_haste_small.jpg",
                "r_2_1_hound_pet_small.jpg", "r_2_1_hound_pet_alone_small.jpg" };
    }
}
