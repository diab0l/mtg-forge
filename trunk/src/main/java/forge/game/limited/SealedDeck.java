package forge.game.limited;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import forge.Constant;
import forge.Constant.Color;
import forge.card.CardColor;
import forge.card.CardRules;
import forge.item.CardPrinted;
import forge.util.MyRandom;

/**
 * Deck builder for Sealed Deck Format.
 * 
 */
public class SealedDeck extends LimitedDeck {

    private static final long serialVersionUID = 8707786521610288811L;

    /**
     * Constructor.
     * 
     * @param list
     *            list of cards available
     */
    public SealedDeck(List<CardPrinted> list) {
        super(list);
        this.setColors(chooseColors());
        buildDeck();
    }

    /**
     * Choose colors for the Sealed Deck.
     * 
     * @return DeckColors
     */
    private CardColor chooseColors() {
        List<CardRankingBean> rankedCards = rankCards(getAiPlayables());

        // choose colors based on top 33% of cards
        final List<CardPrinted> colorChooserList = new ArrayList<CardPrinted>();
        double limit = rankedCards.size() * .33;
        for (int i = 0; i < limit; i++) {
            colorChooserList.add(rankedCards.get(i).getCardPrinted());
            System.out.println(rankedCards.get(i).getCardPrinted().getName() + " "
                    + rankedCards.get(i).getCardPrinted().getCard().getManaCost().toString());
        }

        int white = CardRules.Predicates.Presets.IS_WHITE.select(colorChooserList, CardPrinted.FN_GET_RULES).size();
        int blue = CardRules.Predicates.Presets.IS_BLUE.select(colorChooserList, CardPrinted.FN_GET_RULES).size();
        int black = CardRules.Predicates.Presets.IS_BLACK.select(colorChooserList, CardPrinted.FN_GET_RULES).size();
        int red = CardRules.Predicates.Presets.IS_RED.select(colorChooserList, CardPrinted.FN_GET_RULES).size();
        int green = CardRules.Predicates.Presets.IS_GREEN.select(colorChooserList, CardPrinted.FN_GET_RULES).size();
        final int[] colorCounts = { white, blue, black, red, green };
        final String[] colors = Constant.Color.ONLY_COLORS;
        int[] countsCopy = Arrays.copyOf(colorCounts, 5);
        Arrays.sort(countsCopy);

        List<String> maxColors = new ArrayList<String>();
        List<String> secondColors = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            if (countsCopy[4] == colorCounts[i]) {
                maxColors.add(colors[i]);
            } else if (countsCopy[3] == colorCounts[i]) {
                secondColors.add(colors[i]);
            }
        }

        String color1 = Color.GREEN;
        String color2 = Color.BLACK;
        final Random r = MyRandom.getRandom();
        if (maxColors.size() > 1) {
            int n = r.nextInt(maxColors.size());
            color1 = maxColors.get(n);
            maxColors.remove(n);
            n = r.nextInt(maxColors.size());
            color2 = maxColors.get(n);
        } else {
            color1 = maxColors.get(0);
            if (secondColors.size() > 1) {
                color2 = secondColors.get(r.nextInt(secondColors.size()));
            } else {
                color2 = secondColors.get(0);
            }
        }

        System.out.println("COLOR = " + color1);
        System.out.println("COLOR = " + color2);
        return CardColor.fromNames(color1, color2);
    }
}
