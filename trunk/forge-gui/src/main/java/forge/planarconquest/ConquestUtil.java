package forge.planarconquest;

import java.util.ArrayList;
import java.util.List;

import forge.card.ColorSet;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.deck.generation.DeckGenerator2Color;
import forge.deck.generation.DeckGenerator3Color;
import forge.deck.generation.DeckGenerator5Color;
import forge.deck.generation.DeckGeneratorBase;
import forge.deck.generation.DeckGeneratorMonoColor;
import forge.deck.generation.IDeckGenPool;
import forge.item.PaperCard;
import forge.properties.ForgeConstants;
import forge.quest.QuestUtil;
import forge.util.Aggregates;
import forge.util.FileUtil;
import forge.util.gui.SOptionPane;

public class ConquestUtil {
    private ConquestUtil() {}

    public static Deck generateHumanDeck(PaperCard commander, IDeckGenPool pool) {
        return generateDeck(commander, pool, false);
    }
    public static Deck generateAiDeck(Iterable<PaperCard> commanderOptions, IDeckGenPool pool) {
        return generateDeck(Aggregates.random(commanderOptions), pool, true);
    }
    private static Deck generateDeck(PaperCard commander, IDeckGenPool pool, boolean forAi) {
        ColorSet colorID = commander.getRules().getColorIdentity();

        List<String> colors = new ArrayList<String>();
        if (colorID.hasWhite()) { colors.add("White"); }
        if (colorID.hasBlue())  { colors.add("Blue"); }
        if (colorID.hasBlack()) { colors.add("Black"); }
        if (colorID.hasRed())   { colors.add("Red"); }
        if (colorID.hasGreen()) { colors.add("Green"); }

        DeckGeneratorBase gen;
        switch (colors.size()) {
        case 0:
            gen = new DeckGeneratorMonoColor(pool, "");
            break;
        case 1:
            gen = new DeckGeneratorMonoColor(pool, colors.get(0));
            break;
        case 2:
            gen = new DeckGenerator2Color(pool, colors.get(0), colors.get(1));
            break;
        case 3:
            gen = new DeckGenerator3Color(pool, colors.get(0), colors.get(1), colors.get(2));
            break;
        case 5:
            gen = new DeckGenerator5Color(pool);
            break;
        default:
            return null; //shouldn't happen
        }

        gen.setSingleton(true);
        CardPool cards = gen.getDeck(60, forAi);

        Deck deck = new Deck(commander.getName());
        deck.getMain().addAll(cards);
        deck.getOrCreate(DeckSection.Commander).add(commander);

        return deck;
    }

    public static String promptForName() {
        String name;
        while (true) {
            name = SOptionPane.showInputDialog("Historians will recall your conquest as:", "Conquest Name");
            if (name == null) { return null; }
    
            name = QuestUtil.cleanString(name);
    
            if (name.isEmpty()) {
                SOptionPane.showMessageDialog("Please specify a conquest name.");
                continue;
            }
            if (FileUtil.doesFileExist(ForgeConstants.CONQUEST_SAVE_DIR + name + ".dat")) {
                SOptionPane.showMessageDialog("A conquest already exists with that name. Please pick another quest name.");
                continue;
            }
            break;
        }
        return name;
    }
}
