package forge.ai.simulation;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import forge.GuiBase;
import forge.GuiDesktop;
import forge.ai.ComputerUtilAbility;
import forge.ai.LobbyPlayerAi;
import forge.card.CardStateName;
import forge.deck.Deck;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.GameStage;
import forge.game.GameType;
import forge.game.Match;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CounterType;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.RegisteredPlayer;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.item.IPaperCard;
import forge.model.FModel;
import junit.framework.TestCase;

public class GameSimulatorTest extends TestCase {
    private static boolean initialized = false;
    
    private Game initAndCreateGame() {
        List<RegisteredPlayer> players = Lists.newArrayList();
        Deck d1 = new Deck();
        players.add(new RegisteredPlayer(d1).setPlayer(new LobbyPlayerAi("p2", null)));
        players.add(new RegisteredPlayer(d1).setPlayer(new LobbyPlayerAi("p1", null)));
        GameRules rules = new GameRules(GameType.Constructed);
        Match match = new Match(rules, players, "Test");
        Game game = new Game(players, rules, match);
        game.setAge(GameStage.Play);

        if (!initialized) {
            GuiBase.setInterface(new GuiDesktop());
            FModel.initialize(null);
            initialized = true;
        }
        return game;
    }
 
    private Card findCardWithName(Game game, String name) {
        for (Card c : game.getCardsIn(ZoneType.Battlefield)) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    private SpellAbility findSAWithPrefix(Card c, String prefix) {
        return findSAWithPrefix(c.getSpellAbilities(), prefix);
    }
    
    private SpellAbility findSAWithPrefix(Iterable<SpellAbility> abilities, String prefix) {
        for (SpellAbility sa : abilities) {
            if (sa.toString().startsWith(prefix)) {
                return sa;
            }
        }
        return null;
    }

    private Card createCard(String name, Player p) {
        IPaperCard paperCard = FModel.getMagicDb().getCommonCards().getCard(name);
        return Card.fromPaperCard(paperCard, p);
    }

    private Card addCardToZone(String name, Player p, ZoneType zone) {
        Card c = createCard(name, p);
        p.getZone(zone).add(c);
        return c;
    }

    private Card addCard(String name, Player p) {
        return addCardToZone(name, p, ZoneType.Battlefield);
    }
    

    public void testActivateAbilityTriggers() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);

        addCard("Plains", p);
        addCard("Plains", p);
        addCard("Plains", p);
        String heraldCardName = "Herald of Anafenza";
        Card herald = addCard(heraldCardName, p);
        herald.setSickness(false);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);

        SpellAbility outlastSA = findSAWithPrefix(herald, "Outlast");
        assertNotNull(outlastSA);

        GameSimulator sim = new GameSimulator(game, p);
        int score = sim.simulateSpellAbility(outlastSA).value;
        assertTrue(score >  0);
        Game simGame = sim.getSimulatedGameState();

        Card heraldCopy = findCardWithName(simGame, heraldCardName);
        assertNotNull(heraldCopy);
        assertTrue(heraldCopy.isTapped());
        assertTrue(heraldCopy.hasCounters());
        assertEquals(1, heraldCopy.getToughnessBonusFromCounters());
        assertEquals(1, heraldCopy.getPowerBonusFromCounters());

        Card warriorToken = findCardWithName(simGame, "Warrior");
        assertNotNull(warriorToken);
        assertTrue(warriorToken.isSick());
        assertEquals(1, warriorToken.getCurrentPower());
        assertEquals(1, warriorToken.getCurrentToughness());
    }

    public void testStaticAbilities() {
        String sliverCardName = "Sidewinder Sliver";
        String heraldCardName = "Herald of Anafenza";
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card sliver = addCard(sliverCardName, p);
        sliver.setSickness(false);
        Card herald = addCard(heraldCardName, p);
        herald.setSickness(false);
        addCard("Plains", p);
        addCard("Plains", p);
        addCard("Plains", p);
        addCard("Spear of Heliod", p);
        
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);
        game.getAction().checkStateEffects(true);
        game.getAction().checkStateEffects(true);

        assertEquals(1, sliver.getAmountOfKeyword("Flanking"));
        assertEquals(2, sliver.getNetPower());
        assertEquals(2, sliver.getNetToughness());

        SpellAbility outlastSA = findSAWithPrefix(herald, "Outlast");
        assertNotNull(outlastSA);

        GameSimulator sim = new GameSimulator(game, p);
        int score = sim.simulateSpellAbility(outlastSA).value;
        assertTrue(score >  0);
        Game simGame = sim.getSimulatedGameState();
        Card sliverCopy = findCardWithName(simGame, sliverCardName);
        assertEquals(1, sliverCopy.getAmountOfKeyword("Flanking"));
        assertEquals(2, sliver.getNetPower());
        assertEquals(2, sliver.getNetToughness());
    }

    public void testStaticEffectsMonstrous() {
        String lionCardName = "Fleecemane Lion";
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card lion = addCard(lionCardName, p);
        lion.setSickness(false);
        lion.setMonstrous(true);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);
        game.getAction().checkStateEffects(true);
        assertTrue(lion.isMonstrous());
        assertEquals(1, lion.getAmountOfKeyword("Hexproof"));
        assertEquals(1, lion.getAmountOfKeyword("Indestructible"));

        GameSimulator sim = new GameSimulator(game, p);
        Game simGame = sim.getSimulatedGameState();
        Card lionCopy = findCardWithName(simGame, lionCardName);
        assertTrue(lionCopy.isMonstrous());
        assertEquals(1, lionCopy.getAmountOfKeyword("Hexproof"));
        assertEquals(1, lionCopy.getAmountOfKeyword("Indestructible"));
    }

    public void testEquippedAbilities() {
        String bearCardName = "Runeclaw Bear";
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card bear = addCard(bearCardName, p);
        bear.setSickness(false);
        Card cloak = addCard("Whispersilk Cloak", p);
        cloak.equipCard(bear);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);
        game.getAction().checkStateEffects(true);
        assertEquals(1, bear.getAmountOfKeyword("Unblockable"));

        GameSimulator sim = new GameSimulator(game, p);
        Game simGame = sim.getSimulatedGameState();
        Card bearCopy = findCardWithName(simGame, bearCardName);
        assertEquals(1, bearCopy.getAmountOfKeyword("Unblockable"));
    }

    public void testEnchantedAbilities() {
        String bearCardName = "Runeclaw Bear";
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card bear = addCard(bearCardName, p);
        bear.setSickness(false);
        Card lifelink = addCard("Lifelink", p);
        lifelink.enchantEntity(bear);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, p);
        game.getAction().checkStateEffects(true);
        assertEquals(1, bear.getAmountOfKeyword("Lifelink"));

        GameSimulator sim = new GameSimulator(game, p);
        Game simGame = sim.getSimulatedGameState();
        Card bearCopy = findCardWithName(simGame, bearCardName);
        assertEquals(1, bearCopy.getAmountOfKeyword("Lifelink"));
    }
    
    public void testEtbTriggers() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        addCard("Black Knight", p);
        for (int i = 0; i < 5; i++)
            addCard("Swamp", p);

        String merchantCardName = "Gray Merchant of Asphodel";
        Card c = addCardToZone(merchantCardName, p, ZoneType.Hand);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        SpellAbility playMerchantSa = c.getSpellAbilities().get(0);
        playMerchantSa.setActivatingPlayer(p);

        GameSimulator sim = new GameSimulator(game, p);
        int origScore = sim.getScoreForOrigGame().value;
        int score = sim.simulateSpellAbility(playMerchantSa).value;
        assertTrue(String.format("score=%d vs. origScore=%d",  score, origScore), score > origScore);
        Game simGame = sim.getSimulatedGameState();
        assertEquals(24, simGame.getPlayers().get(1).getLife());
        assertEquals(16, simGame.getPlayers().get(0).getLife());
    }
    
    public void testEchoCostState() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);

        String c1Name = "Acridian";
        String c2Name = "Goblin Patrol";
        Card c1 = addCard(c1Name, p);
        Card c2 = addCard(c2Name, p);
        
        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        assertTrue(c1.hasStartOfKeyword("(Echo unpaid)"));
        assertTrue(c2.hasStartOfKeyword("(Echo unpaid)"));
        c2.removeAllExtrinsicKeyword("(Echo unpaid)");

        GameSimulator sim = new GameSimulator(game, p);
        Game simGame = sim.getSimulatedGameState();
        Card c1Copy = findCardWithName(simGame, c1Name);
        assertTrue(c1Copy.hasStartOfKeyword("(Echo unpaid)"));
        Card c2Copy = findCardWithName(simGame, c2Name);
        assertFalse(c2Copy.hasStartOfKeyword("(Echo unpaid)"));
    }
    
    public void testSimulateUnmorph() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card ripper = createCard("Ruthless Ripper", p);
        ripper.setState(CardStateName.FaceDown, true);
        p.getZone(ZoneType.Battlefield).add(ripper);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        assertEquals(20, p.getOpponent().getLife());
        
        GameSimulator sim = new GameSimulator(game, p);
        Game simGame = sim.getSimulatedGameState();

        SpellAbility unmorphSA = findSAWithPrefix(ripper, "Morph - Reveal a black card");
        assertNotNull(unmorphSA);
        sim.simulateSpellAbility(unmorphSA);
        assertEquals(18, simGame.getPlayers().get(0).getLife());
    }
    
    public void testFindingOwnCard() {
        Game game = initAndCreateGame();
        Player p0 = game.getPlayers().get(0);
        Player p1 = game.getPlayers().get(1);
        addCardToZone("Skull Fracture", p0, ZoneType.Hand);
        addCardToZone("Runeclaw Bear", p0, ZoneType.Hand);
        Card fractureP1 = addCardToZone("Skull Fracture", p1, ZoneType.Hand);
        addCard("Swamp", p1);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p1);
        game.getAction().checkStateEffects(true);
        
        GameSimulator sim = new GameSimulator(game, p1);
        Game simGame = sim.getSimulatedGameState();

        SpellAbility fractureSa = fractureP1.getSpellAbilities().get(0);
        assertNotNull(fractureSa);
        fractureSa.getTargets().add(p0);
        sim.simulateSpellAbility(fractureSa);
        assertEquals(1, simGame.getPlayers().get(0).getCardsIn(ZoneType.Hand).size());
        assertEquals(0, simGame.getPlayers().get(1).getCardsIn(ZoneType.Hand).size());
    }
    
    public void testPlaneswalkerAbilities() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        Card sorin = addCard("Sorin, Solemn Visitor", p);        
        sorin.addCounter(CounterType.LOYALTY, 5, false);

        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        CardCollection cards = ComputerUtilAbility.getAvailableCards(game, p);
        ArrayList<SpellAbility> abilities = ComputerUtilAbility.getSpellAbilities(cards, p);
        SpellAbility minusTwo = findSAWithPrefix(abilities, "-2: Put a 2/2 black Vampire");
        assertNotNull(minusTwo);
        minusTwo.setActivatingPlayer(p);
        assertTrue(minusTwo.canPlay());

        GameSimulator sim = new GameSimulator(game, p);
        sim.simulateSpellAbility(minusTwo);
        Game simGame = sim.getSimulatedGameState();
        Card vampireToken = findCardWithName(simGame, "Vampire");
        assertNotNull(vampireToken);

        Player simP = simGame.getPlayers().get(1);
        cards = ComputerUtilAbility.getAvailableCards(simGame, simP);
        abilities = ComputerUtilAbility.getSpellAbilities(cards, simP);
        SpellAbility minusTwoSim = findSAWithPrefix(abilities, "-2: Put a 2/2 black Vampire");
        assertNotNull(minusTwoSim);
        minusTwo.setActivatingPlayer(simP);
        assertFalse(minusTwoSim.canPlay());
        assertEquals(1, minusTwoSim.getActivationsThisTurn());
        
        GameCopier copier = new GameCopier(simGame);
        Game copy = copier.makeCopy();
        Player copyP = copy.getPlayers().get(1);
        cards = ComputerUtilAbility.getAvailableCards(copy, copyP);
        abilities = ComputerUtilAbility.getSpellAbilities(cards, copyP);
        SpellAbility minusTwoCopy = findSAWithPrefix(abilities, "-2: Put a 2/2 black Vampire");
        minusTwoCopy.setActivatingPlayer(copyP);
        assertFalse(minusTwoCopy.canPlay());
        assertEquals(1, minusTwoCopy.getActivationsThisTurn());
    }
    
    public void testManifest() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        addCard("Plains", p);
        addCard("Plains", p);
        Card soulSummons = addCardToZone("Soul Summons", p, ZoneType.Hand);
        addCardToZone("Ornithopter", p, ZoneType.Library);
        
        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        SpellAbility manifestSA = soulSummons.getSpellAbilities().get(0);
        
        GameSimulator sim = new GameSimulator(game, p);
        sim.simulateSpellAbility(manifestSA);
        Game simGame = sim.getSimulatedGameState();
        Card manifestedCreature = findCardWithName(simGame, "");
        assertNotNull(manifestedCreature);

        SpellAbility unmanifestSA = findSAWithPrefix(manifestedCreature, "Unmanifest");
        assertNotNull(unmanifestSA);
        assertEquals(2, manifestedCreature.getNetPower());
        assertFalse(manifestedCreature.hasKeyword("Flying"));

        GameSimulator sim2 = new GameSimulator(simGame, simGame.getPlayers().get(1));
        sim2.simulateSpellAbility(unmanifestSA);
        Game simGame2 = sim2.getSimulatedGameState();
        Card ornithopter = findCardWithName(simGame2, "Ornithopter");
        assertEquals(0, ornithopter.getNetPower());
        assertTrue(ornithopter.hasKeyword("Flying"));
        assertNull(findSAWithPrefix(ornithopter, "Unmanifest"));

        GameCopier copier = new GameCopier(simGame2);
        Game copy = copier.makeCopy();
        Card ornithopterCopy = findCardWithName(copy, "Ornithopter");
        assertNull(findSAWithPrefix(ornithopterCopy, "Unmanifest"));
    }

    public void testManifest2() {
        Game game = initAndCreateGame();
        Player p = game.getPlayers().get(1);
        addCard("Plains", p);
        addCard("Plains", p);
        Card soulSummons = addCardToZone("Soul Summons", p, ZoneType.Hand);
        addCardToZone("Plains", p, ZoneType.Library);
        
        game.getPhaseHandler().devModeSet(PhaseType.MAIN2, p);
        game.getAction().checkStateEffects(true);

        SpellAbility manifestSA = soulSummons.getSpellAbilities().get(0);
        
        GameSimulator sim = new GameSimulator(game, p);
        sim.simulateSpellAbility(manifestSA);
        Game simGame = sim.getSimulatedGameState();
        Card manifestedCreature = findCardWithName(simGame, "");
        assertNotNull(manifestedCreature);
        assertNull(findSAWithPrefix(manifestedCreature, "Unmanifest"));

        GameCopier copier = new GameCopier(simGame);
        Game copy = copier.makeCopy();
        Card manifestedCreatureCopy = findCardWithName(copy, "");
        assertNull(findSAWithPrefix(manifestedCreatureCopy, "Unmanifest"));
   }
}