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
package forge.deck;

import java.util.ArrayList;
import java.util.Random;

import forge.CardList;
import forge.Constant;
import forge.PlayerType;
import forge.deck.generate.Generate2ColorDeck;
import forge.deck.generate.Generate3ColorDeck;
import forge.deck.generate.Generate5ColorDeck;
import forge.deck.generate.GenerateConstructedDeck;
import forge.deck.generate.GenerateConstructedMultiColorDeck;
import forge.deck.generate.GenerateThemeDeck;
import forge.game.GameType;
import forge.gui.GuiUtils;
import forge.util.MyRandom;

/**
 * Utility class to hold add deck generation routines, methods moved from
 * OldGuiNewGame.
 * 
 */
public abstract class DeckGeneration {

    /**
     * <p>
     * genDecks.
     * </p>
     * 
     * @param playerType
     *            the player type {@link java.lang.String} object.
     */
    public static void genDecks(final PlayerType playerType) {
        // TODO jendave to refactor deck generation
        Deck d = null;

        final ArrayList<String> decks = new ArrayList<String>();
        decks.add("2-Color Deck");
        decks.add("3-Color Deck");
        decks.add("5-Color Deck");
        decks.add("2-Color Deck (original)");
        decks.add("3-Color Deck (original)");
        decks.add("5-Color Deck (original)");
        decks.add("Semi-Random Theme Deck");

        final String playerName = playerType.equals(PlayerType.HUMAN) ? "Human" : "Computer";
        final String prompt = String.format("Generate %s Deck", playerName);

        final Object o = GuiUtils.getChoice(prompt, decks.toArray());

        if (o.toString().equals(decks.get(0))) {
            d = DeckGeneration.generate2ColorDeck(playerType);
        } else if (o.toString().equals(decks.get(1))) {
            d = DeckGeneration.generate3ColorDeck(playerType);
        } else if (o.toString().equals(decks.get(2))) {
            d = DeckGeneration.generate5ColorDeck(playerType);
        } else if (o.toString().equals(decks.get(3))) {
            d = DeckGeneration.generateConstructedDeck();
        } else if (o.toString().equals(decks.get(4))) {
            d = DeckGeneration.generateConstructed3ColorDeck();
        } else if (o.toString().equals(decks.get(5))) {
            d = DeckGeneration.generateConstructed5ColorDeck();
        } else if (o.toString().equals(decks.get(6))) {
            d = DeckGeneration.generateConstructedThemeDeck();
        }

        if (playerType.equals(PlayerType.HUMAN)) {
            Constant.Runtime.HUMAN_DECK[0] = d;
        } else if (playerType.equals(PlayerType.COMPUTER)) {
            Constant.Runtime.COMPUTER_DECK[0] = d;
        }
    }

    /**
     * <p>
     * generateConstructedDeck.
     * </p>
     * 
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generateConstructedDeck() {
        final GenerateConstructedDeck gen = new GenerateConstructedDeck();
        final CardList name = gen.generateDeck();
        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructed3ColorDeck.
     * </p>
     * 
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generateConstructed3ColorDeck() {
        final GenerateConstructedMultiColorDeck gen = new GenerateConstructedMultiColorDeck();
        final CardList name = gen.generate3ColorDeck();
        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructed5ColorDeck.
     * </p>
     * 
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generateConstructed5ColorDeck() {
        final GenerateConstructedMultiColorDeck gen = new GenerateConstructedMultiColorDeck();
        final CardList name = gen.generate5ColorDeck();
        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < 60; i++) {
            deck.addMain(name.get(i).getName());
        }
        return deck;
    }

    /**
     * <p>
     * generateConstructedThemeDeck.
     * </p>
     * 
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generateConstructedThemeDeck() {
        final GenerateThemeDeck gen = new GenerateThemeDeck();
        final ArrayList<String> tNames = GenerateThemeDeck.getThemeNames();
        tNames.add(0, "Random");
        final Object o = GuiUtils.getChoice("Select a theme.", tNames.toArray());

        String stDeck;
        if (o.toString().equals("Random")) {
            final Random r = MyRandom.getRandom();
            stDeck = tNames.get(r.nextInt(tNames.size() - 1) + 1);
        } else {
            stDeck = o.toString();
        }

        final CardList td = gen.getThemeDeck(stDeck, 60);
        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < td.size(); i++) {
            deck.addMain(td.get(i).getName());
        }

        return deck;
    }

    /**
     * <p>
     * generate2ColorDeck.
     * </p>
     * 
     * @param p
     *            a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generate2ColorDeck(final PlayerType p) {
        final Random r = MyRandom.getRandom();

        final ArrayList<String> colors = new ArrayList<String>();
        colors.add("Random");
        colors.add("white");
        colors.add("blue");
        colors.add("black");
        colors.add("red");
        colors.add("green");

        String c1;
        String c2;
        if (p.equals(PlayerType.HUMAN)) {
            c1 = GuiUtils.getChoice("Select first color.", colors.toArray()).toString();

            if (c1.equals("Random")) {
                c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c1);

            c2 = GuiUtils.getChoice("Select second color.", colors.toArray()).toString();

            if (c2.equals("Random")) {
                c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }
        } else {
            // if (p.equals("C"))
            c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c1);
            c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
        }
        final Generate2ColorDeck gen = new Generate2ColorDeck(c1, c2);
        final CardList d = gen.get2ColorDeck(60, p);

        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < d.size(); i++) {
            deck.addMain(d.get(i).getName());
        }

        return deck;
    }

    /**
     * <p>
     * generate3ColorDeck.
     * </p>
     * 
     * @param p
     *            a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generate3ColorDeck(final PlayerType p) {
        final Random r = MyRandom.getRandom();

        final ArrayList<String> colors = new ArrayList<String>();
        colors.add("Random");
        colors.add("white");
        colors.add("blue");
        colors.add("black");
        colors.add("red");
        colors.add("green");

        String c1;
        String c2;
        String c3;
        if (p.equals(PlayerType.HUMAN)) {
            c1 = GuiUtils.getChoice("Select first color.", colors.toArray()).toString();

            if (c1.equals("Random")) {
                c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c1);

            c2 = GuiUtils.getChoice("Select second color.", colors.toArray()).toString();

            if (c2.equals("Random")) {
                c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

            colors.remove(c2);

            c3 = GuiUtils.getChoice("Select third color.", colors.toArray()).toString();
            if (c3.equals("Random")) {
                c3 = colors.get(r.nextInt(colors.size() - 1) + 1);
            }

        } else {
            // if (p.equals("C"))
            c1 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c1);
            c2 = colors.get(r.nextInt(colors.size() - 1) + 1);
            colors.remove(c2);
            c3 = colors.get(r.nextInt(colors.size() - 1) + 1);
        }
        final Generate3ColorDeck gen = new Generate3ColorDeck(c1, c2, c3);
        final CardList d = gen.get3ColorDeck(60, p);

        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < d.size(); i++) {
            deck.addMain(d.get(i).getName());
        }

        return deck;

    }

    /**
     * <p>
     * generate5ColorDeck.
     * </p>
     * 
     * @param p
     *            a {@link java.lang.String} object.
     * @return a {@link forge.deck.Deck} object.
     */
    private static Deck generate5ColorDeck(final PlayerType p) {
        // Random r = MyRandom.random;

        // ArrayList<String> colors = new ArrayList<String>();
        // colors.add("Random");
        // colors.add("white");
        // colors.add("blue");
        // colors.add("black");
        // colors.add("red");
        // colors.add("green");

        final Generate5ColorDeck gen = new Generate5ColorDeck("white", "blue", "black", "red", "green");
        final CardList d = gen.get5ColorDeck(60, p);

        final Deck deck = new Deck(GameType.Constructed);

        for (int i = 0; i < d.size(); i++) {
            deck.addMain(d.get(i).getName());
        }

        return deck;

    }

}
