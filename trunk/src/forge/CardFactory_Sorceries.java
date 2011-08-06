package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import com.esotericsoftware.minlog.Log;

public class CardFactory_Sorceries {

	private static final int hasKeyword(Card c, String k) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith(k)) return i;
        
        return -1;
    }
    
    public static Card getCard(final Card card, final String cardName, String owner) 
    {
    
    	 //*************** START *********** START **************************
        if(cardName.equals("Timetwister") || cardName.equals("Time Reversal")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 505983020365091226L;
                
                @Override
                public void resolve() {
                    discardDraw7(Constant.Player.Human);
                    discardDraw7(Constant.Player.Computer);
                    
                    if (cardName.equals("Time Reversal"))
                    	AllZone.GameAction.removeFromGame(card);
                }//resolve()
                
                void discardDraw7(String player) {
                    // Discard hand into graveyard
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);
                    Card[] c = hand.getCards();
                    for(int i = 0; i < c.length; i++) {
                        //AllZone.GameAction.discard(c[i]);
                    	hand.remove(c[i]);
                    	library.add(c[i], 0);
                    }
                    
                    // Move graveyard into library
                   
                    Card[] g = grave.getCards();
                    for(int i = 0; i < g.length; i++) {
                        grave.remove(g[i]);
                        library.add(g[i], 0);
                    }
                    
                    // Shuffle library
                    AllZone.GameAction.shuffle(player);
                    
                    // Draw seven cards
                    for(int i = 0; i < 7; i++)
                        AllZone.GameAction.drawCard(player);
                    
                    if(card.getController().equals(player)) {
                        library.remove(card);
                        grave.add(card);
                    }
                }
                
                // Simple, If computer has two or less playable cards remaining in hand play Timetwister
                @Override
                public boolean canPlayAI() {
                    Card[] c = removeLand(AllZone.Computer_Hand.getCards());
                    return 2 >= c.length;
                }
                
                Card[] removeLand(Card[] in) {
                    CardList c = new CardList(in);
                    c = c.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isLand();
                        }
                    });
                    return c.toArray();
                }//removeLand()
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Infest")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4970294125917784048L;
                
                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    CardList computer = new CardList(AllZone.Computer_Play.getCards());
                    
                    human = human.getType("Creature");
                    computer = computer.getType("Creature");
                    
                    human = CardListUtil.filterToughness(human, 2);
                    computer = CardListUtil.filterToughness(computer, 2);
                    
                    //the computer will at least destroy 2 more human creatures
                    return computer.size() < human.size() - 1;
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    //get all creatures
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Play.getCards());
                    list.addAll(AllZone.Computer_Play.getCards());
                    list = list.getType("Creature");
                    
                    for(int i = 0; i < list.size(); i++) {
                        final Card[] target = new Card[1];
                        target[0] = list.get(i);
                        
                        final Command untilEOT = new Command() {
							private static final long serialVersionUID = 38760668661487826L;

							public void execute() {
                                if(AllZone.GameAction.isCardInPlay(target[0])) {
                                    target[0].addTempAttackBoost(2);
                                    target[0].addTempDefenseBoost(2);
                                }
                            }
                        };//Command
                        
                        if(AllZone.GameAction.isCardInPlay(target[0])) {
                            target[0].addTempAttackBoost(-2);
                            target[0].addTempDefenseBoost(-2);
                            
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }//if
                    }//for
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Molten Rain")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8855786097956610090L;
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        if(!c.getType().contains("Basic")) AllZone.GameAction.getPlayerLife(c.getController()).subtractLife(
                                2,card);
                        AllZone.GameAction.destroy(c);
                    }
                    
                }// resolve()
                
            };// Spell
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("Land", AllZone.Human_Play));
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Land"));
        }// *************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Violent Ultimatum")) {
            final Card[] target = new Card[3];
            final int[] index = new int[1];
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -1880229743741157304L;
                
                @Override
                public boolean canPlayAI() {
                    CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                    
                    CardListUtil.sortAttack(human);
                    CardListUtil.sortFlying(human);
                    
                    if(3 <= human.size()) {
                        for(int i = 0; i < 3; i++)
                            //should check to make sure none of these creatures have protection or cannot be the target of spells.
                            target[i] = human.get(i);
                    }
                    
                    return 3 <= human.size();
                }
                
                @Override
                public void resolve() {
                    for(int i = 0; i < target.length; i++)
                        if(AllZone.GameAction.isCardInPlay(target[i])
                                && CardFactoryUtil.canTarget(card, target[i])) AllZone.GameAction.destroy(target[i]);
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = 5792813689927185739L;
                
                @Override
                public void showMessage() {
                    int count = 3 - index[0];
                    AllZone.Display.showMessage("Select target " + count + " permanents to destroy");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    for(int i = 0; i < index[0]; i++) {
                        if(c.equals(target[i])) {
                            AllZone.Display.showMessage("You have already selected this target. You must select unique targets for each of the 3 permanents to destroy.");
                            return; //cannot target the same permanent twice.
                        }
                    }
                    
                    if(c.isPermanent() && zone.is(Constant.Zone.Play)) {
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            if(this.isFree()) {
                                this.setFree(false);
                                AllZone.Stack.add(spell);
                                stop();
                            } else stopSetNext(new Input_PayManaCost(spell));
                        }
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                private static final long serialVersionUID = 3522833806455511494L;
                
                @Override
                public void showMessage() {
                    index[0] = 0;
                    stopSetNext(input);
                }
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Hex")) {
            final Card[] target = new Card[6];
            final int[] index = new int[1];
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -1880229743741157304L;
                
                @Override
                public boolean canPlayAI() {
                    CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                    
                    CardListUtil.sortAttack(human);
                    CardListUtil.sortFlying(human);
                    
                    if(6 <= human.size()) {
                        for(int i = 0; i < 6; i++)
                            //should check to make sure none of these creatures have protection or cannot be the target of spells.
                            target[i] = human.get(i);
                    }
                    
                    return 6 <= human.size();
                }
                
                @Override
                public void resolve() {
                    for(int i = 0; i < target.length; i++)
                        if(AllZone.GameAction.isCardInPlay(target[i])
                                && CardFactoryUtil.canTarget(card, target[i])) AllZone.GameAction.destroy(target[i]);
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = 5792813689927185739L;
                
                @Override
                public void showMessage() {
                    int count = 6 - index[0];
                    AllZone.Display.showMessage("Select target " + count + " creatures to destroy");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    for(int i = 0; i < index[0]; i++) {
                        if(c.equals(target[i])) {
                            AllZone.Display.showMessage("You have already selected this target. You must select unique targets for each of the 6 creatures to destroy.");
                            return; //cannot target the same creature twice.
                        }
                    }
                    
                    if(c.isCreature() && zone.is(Constant.Zone.Play)) {
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            if(this.isFree()) {
                                this.setFree(false);
                                AllZone.Stack.add(spell);
                                stop();
                            } else stopSetNext(new Input_PayManaCost(spell));
                        }
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                private static final long serialVersionUID = 3522833806455511494L;
                
                @Override
                public void showMessage() {
                    index[0] = 0;
                    stopSetNext(input);
                }
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Political Trickery")) {
            final Card[] target = new Card[2];
            final int[] index = new int[1];
            
            final SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = -3075569295823682336L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    
                    Card crd0 = target[0];
                    Card crd1 = target[1];
                    
                    if(crd0 != null && crd1 != null) {
                        ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                        ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                        
                        PlayerZone from0 = AllZone.getZone(crd0);
                        from0.remove(crd0);
                        PlayerZone from1 = AllZone.getZone(crd1);
                        from1.remove(crd1);
                        
                        crd0.setController(AllZone.GameAction.getOpponent(card.getController()));
                        crd1.setController(card.getController());
                        
                        PlayerZone to0 = AllZone.getZone(Constant.Zone.Play,
                                AllZone.GameAction.getOpponent(card.getController()));
                        to0.add(crd0);
                        PlayerZone to1 = AllZone.getZone(Constant.Zone.Play, card.getController());
                        to1.add(crd1);
                        
                        ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                        ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                    }
                    
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                
                private static final long serialVersionUID = -1017253686774265770L;
                
                @Override
                public void showMessage() {
                    if(index[0] == 0) AllZone.Display.showMessage("Select target land you control.");
                    else AllZone.Display.showMessage("Select target land opponent controls.");
                    
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    //must target creature you control
                    if(index[0] == 0 && !c.getController().equals(card.getController())) return;
                    
                    //must target creature you don't control
                    if(index[0] == 1 && c.getController().equals(card.getController())) return;
                    

                    if(c.isLand() && zone.is(Constant.Zone.Play) && CardFactoryUtil.canTarget(card, c)) {
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            if(this.isFree()) {
                                this.setFree(false);
                                AllZone.Stack.add(spell);
                                stop();
                            } else stopSetNext(new Input_PayManaCost(spell));
                        }
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                
                private static final long serialVersionUID = 4003351872990899418L;
                
                @Override
                public void showMessage() {
                    index[0] = 0;
                    stopSetNext(input);
                }
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        //should REALLY be an aura:
        else if(cardName.equals("Lignify")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5323770119451400755L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = CardFactoryUtil.AI_getHumanCreature(card, true);
                    CardListUtil.sortAttack(c);
                    CardListUtil.sortFlying(c);
                    
                    if(c.isEmpty()) return false;
                    
                    if(2 <= c.get(0).getNetAttack() && c.get(0).getKeyword().contains("Flying")) {
                        setTargetCard(c.get(0));
                        return true;
                    }
                    
                    CardListUtil.sortAttack(c);
                    if(4 <= c.get(0).getNetAttack()) {
                        setTargetCard(c.get(0));
                        return true;
                    }
                    
                    return false;
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        c.setBaseAttack(0);
                        c.setBaseDefense(4);
                        
                        c.setType(new ArrayList<String>());
                        c.addType("Creature");
                        c.addType("Treefolk");
                        
                        c.setIntrinsicKeyword(new ArrayList<String>());
                        
                        c.clearSpellAbility();
                    }
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        
      //*************** START *********** START **************************
        else if(cardName.equals("Demonic Tutor") || cardName.equals("Diabolic Tutor")
                || cardName.equals("Grim Tutor")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1481169060428051519L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    Object check = AllZone.Display.getChoiceOptional("Select card",
                            AllZone.Human_Library.getCards());
                    if(check != null) {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                        AllZone.GameAction.moveTo(hand, (Card) check);
                    }
                    AllZone.GameAction.shuffle(Constant.Player.Human);
                    
                    //lose 3 life
                    if(cardName.equals("Grim Tutor")) {
                        String player = Constant.Player.Human;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(3,card);
                    }
                }
                
                public void computerResolve() {
                    Card[] library = AllZone.Computer_Library.getCards();
                    CardList list = new CardList(library);
                    
                    //pick best creature
                    Card c = CardFactoryUtil.AI_getBestCreature(list);
                    if(c == null) c = library[0];
                    //System.out.println("comptuer picked - " +c);
                    AllZone.Computer_Library.remove(c);
                    AllZone.Computer_Hand.add(c);
                    
                    //lose 3 life
                    if(cardName.equals("Grim Tutor")) {
                        String player = Constant.Player.Computer;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(3,card);
                    }
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    return library.getCards().length != 0
                            && AllZone.Phase.getActivePlayer().equals(card.getController())
                            && !AllZone.Phase.getPhase().equals("End of Turn") && super.canPlay();
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList creature = new CardList();
                    creature.addAll(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    return creature.size() != 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        

        //*************** START *********** START **************************
        else if(cardName.equals("Do or Die")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8241241003478388362L;
                
                @Override
                public boolean canPlayAI() {
                    return 4 <= CardFactoryUtil.AI_getHumanCreature(card, true).size()
                            && 4 < AllZone.Phase.getTurn();
                }
                
                @Override
                public void resolve() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, getTargetPlayer());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Creature");
                    
                    list.shuffle();
                    
                    for(int i = 0; i < list.size() / 2; i++)
                        AllZone.GameAction.destroyNoRegeneration(list.get(i));
                }
            };//SpellAbility
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Lab Rats")) {
            final SpellAbility spell_one = new Spell(card) {
                private static final long serialVersionUID = -8112024383172056976L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Rat", "B 1 1 Rat", card, "B", new String[] {"Creature", "Rat"}, 1,
                            1, new String[] {""});
                }//resolve()
            };//SpellAbility
            
            final SpellAbility spell_two = new Spell(card) {
                private static final long serialVersionUID = -7503701530510847636L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Rat", "B 1 1 Rat", card, "B", new String[] {"Creature", "Rat"}, 1,
                            1, new String[] {""});
                    
                    //return card to the hand
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    AllZone.GameAction.moveTo(hand, card);
                }
                
                @Override
                public boolean canPlayAI() {
                    String phase = AllZone.Phase.getPhase();
                    return phase.equals(Constant.Phase.Main2);
                }
            };//SpellAbility
            
            spell_one.setManaCost("B");
            spell_two.setManaCost("4 B");
            spell_two.setAdditionalManaCost("4");
            
            spell_one.setDescription("Put a 1/1 black Rat token into play.");
            spell_two.setDescription("Buyback 4 (You may pay an additional 4 as you cast this spell. If you do, put this card into your hand as it resolves.)");
            // spell_two.setDescription("Buyback 4 - Pay 4B, put this card into your hand as it resolves.");
            
            spell_one.setStackDescription("Lab Rats - Put a 1/1 black Rat token into play");
            spell_two.setStackDescription("Lab Rats - Buyback, Put a 1/1 black Rat token into play");
            
            spell_two.setIsBuyBackAbility(true);
            
            card.clearSpellAbility();
            card.addSpellAbility(spell_one);
            card.addSpellAbility(spell_two);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Threaten") || cardName.equals("Act of Treason") || cardName.equals("Slave of Bolas")) {
            final PlayerZone[] orig = new PlayerZone[1];
            final PlayerZone[] temp = new PlayerZone[1];
            final String[] controllerEOT = new String[1];
            final Card[] target = new Card[1];
            
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = 5310901886760561889L;
                
                public void execute() {
                    //if card isn't in play, do nothing
                    if(!AllZone.GameAction.isCardInPlay(target[0])) return;
                    
                    target[0].setController(controllerEOT[0]);
                    
                    ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                    ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                    
                    //moveTo() makes a new card, so you don't have to remove "Haste"
                    //AllZone.GameAction.moveTo(playEOT[0], target[0]);
                    
                    temp[0].remove(target[0]);
                    orig[0].add(target[0]);
                    target[0].untap();
                    target[0].removeExtrinsicKeyword("Haste");
                    
                    ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                    ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                }//execute()
            };//Command
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -3447822168516135816L;
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        orig[0] = AllZone.getZone(getTargetCard());
                        controllerEOT[0] = getTargetCard().getController();
                        target[0] = getTargetCard();
                        
                        //set the controller
                        getTargetCard().setController(card.getController());
                        
                        ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                        ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                        
                        PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                        play.add(getTargetCard());
                        temp[0] = play;
                        orig[0].remove(getTargetCard());
                        
                        ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                        ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                        
                        getTargetCard().untap();
                        getTargetCard().addExtrinsicKeyword("Haste");
                        if (cardName.equals("Slave of Bolas")) getTargetCard().addExtrinsicKeyword("At the beginning of the end step, sacrifice CARDNAME.");
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }//is card in play?
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    //only use this card if creatures power is greater than 2
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    for(int i = 0; i < list.size(); i++)
                        if(2 < list.get(i).getNetAttack() && AllZone.Phase.getPhase().equals(Constant.Phase.Main1) &&
                           CardFactoryUtil.canTarget(card, list.get(i))) 
                        	return true;
                    
                    return false;
                }//canPlayAI()
                
                @Override
                public void chooseTargetAI() {
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    setTargetCard(CardFactoryUtil.AI_getBestCreature(list));
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Insurrection")) {
        	/*
        	 * Untap all creatures and gain control of them until end of
        	 * turn. They gain haste until end of turn.
        	 */
            final ArrayList<PlayerZone> orig = new ArrayList<PlayerZone>();
            final PlayerZone[] newZone = new PlayerZone[1];
            final ArrayList<String> controllerEOT = new ArrayList<String>();
            final ArrayList<Card> targets = new ArrayList<Card>();
            
            final Command untilEOT = new Command() {
				private static final long serialVersionUID = -5809548350739536763L;

				public void execute() {
                	int i = 0;
                	for(Card target:targets) {
                		//if card isn't in play, do nothing
                		if(!AllZone.GameAction.isCardInPlay(target)) break;

                		target.setController(controllerEOT.get(i));

                		((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                		((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);

                		newZone[0].remove(target);
                		orig.get(i).add(target);
                		target.untap();
                		target.removeExtrinsicKeyword("Haste");

                		((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                		((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                		i++;
                	}
                }//execute()
            };//Command
            
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -532862769235091780L;

				@Override
                public void resolve() {
                	CardList creatures = AllZoneUtil.getCreaturesInPlay();
                	newZone[0] = AllZone.getZone(Constant.Zone.Play, card.getController());;
                	int i = 0;
                	for(Card target:creatures) {
                		if(AllZone.GameAction.isCardInPlay(target)) {
                			orig.add(i, AllZone.getZone(target));
                			controllerEOT.add(i, target.getController());
                			targets.add(i, target);

                			//set the controller
                			target.setController(card.getController());

                			((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                			((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                			
                			newZone[0].add(target);
                			orig.get(i).remove(target);

                			((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                			((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);

                			target.untap();
                			target.addExtrinsicKeyword("Haste");
                		}//is card in play?
                	}//end for
                	AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	CardList creatures = AllZoneUtil.getCreaturesInPlay(Constant.Player.Human);
                    return creatures.size() > 0 && AllZone.Phase.getPhase().equals(Constant.Phase.Main1);
                }//canPlayAI()
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Beacon of Unrest")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7614131436905786565L;
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    PlayerZone grave = AllZone.getZone(c);
                    
                    if(AllZone.GameAction.isCardInZone(c, grave) && (c.isArtifact() || c.isCreature())) {
                        //set the correct controller if needed
                        c.setController(card.getController());
                        
                        //card changes zones
                        AllZone.getZone(c).remove(c);
                        PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                        play.add(c);
                        
                        //shuffle card back into the library
                        PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                        library.add(card);
                        AllZone.GameAction.shuffle(card.getController());
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return getCreaturesAndArtifacts().length != 0;
                }
                
                public Card[] getCreaturesAndArtifacts() {
                    CardList graveyardCards = new CardList();
                    graveyardCards.addAll(AllZone.Human_Graveyard.getCards());
                    graveyardCards.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    CardList graveyardCreaturesAndArtifacts = graveyardCards.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() || c.isCreature();
                        }
                    });
                    
                    return graveyardCreaturesAndArtifacts.toArray();
                }
                
                @Override
                public void chooseTargetAI() {
                    Card c[] = getCreaturesAndArtifacts();
                    Card biggest = c[0];
                    for(int i = 0; i < c.length; i++)
                        if(biggest.getNetAttack() < c[i].getNetAttack()) biggest = c[i];
                    
                    setTargetCard(biggest);
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            Input target = new Input() {
                private static final long serialVersionUID = -83460850846474327L;
                
                @Override
                public void showMessage() {
                    Object check = AllZone.Display.getChoiceOptional("Select creature", getCreaturesAndArtifacts());
                    if(check != null) {
                        spell.setTargetCard((Card) check);
                        stopSetNext(new Input_PayManaCost(spell));
                    } else stop();
                }//showMessage()
                
                //duplicated from SpellAbility above ^^^^^^^^
                public Card[] getCreaturesAndArtifacts() {
                    CardList graveyardCards = new CardList();
                    graveyardCards.addAll(AllZone.Human_Graveyard.getCards());
                    graveyardCards.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    CardList graveyardCreaturesAndArtifacts = graveyardCards.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() || c.isCreature();
                        }
                    });
                    
                    return graveyardCreaturesAndArtifacts.toArray();
                }
            };//Input
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Blinding Light")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -631672055247954361L;
                
                @Override
                public void resolve() {
                	CardList nonwhite = AllZoneUtil.getCreaturesInPlay();
                    nonwhite = nonwhite.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isWhite();
                        }
                    });
                    for(int i = 0; i < nonwhite.size(); i++)
                        nonwhite.get(i).tap();
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    //the computer seems to play this card at stupid times
                    return false;
                }
            };
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Grapeshot")) {
           final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 74155521291969L;
                
                @Override                           
                public boolean canPlayAI() {
                    return AllZone.Phase.getPhase().equals(Constant.Phase.Main2);
                }      
                @Override
                public void resolve() {
                    String player = card.getController();
                    
                    if(player == "Human"){
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) getTargetCard().addDamage(1,card);
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(1,card);
                    } else AllZone.GameAction.getPlayerLife(AllZone.GameAction.getOpponent(card.getController())).subtractLife(1,card);
            };
           };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        	spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
        }//*************** END ************ END ************************** 
        
        //*************** START *********** START **************************
        else if(cardName.equals("Ignite Memories")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 143904782338241969L;
                @Override                           
                public boolean canPlayAI() {
                    return AllZone.Phase.getPhase().equals(Constant.Phase.Main2);
                }                  
                @Override
                public void resolve() {
                    Card choice = null;
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, opponent);
                    Card[] handChoices = hand.getCards();
                    if (handChoices.length > 0)
                    {
                        choice = CardUtil.getRandom(handChoices);
                        handChoices[0] = choice;
                        for(int i = 1; i < handChoices.length; i++) {
                            handChoices[i] = null;
                        }
                        AllZone.Display.getChoice("Random card", handChoices);
                        AllZone.GameAction.getPlayerLife(opponent).subtractLife(
                                CardUtil.getConvertedManaCost(choice.getManaCost()),card);
                    }                                   
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END ************************** 
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cruel Ultimatum")) {
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598023699468746L;

				@Override
                public void resolve() {
					// Opponent Sacrifices Creature
                    String player = card.getController();
                    AllZone.Display.showMessage("Sacrifice a Creature: ");
                    ButtonUtil.enableOnlyCancel();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(card.getController()));                   
                    CardList creature2 = new CardList();
                    creature2.addAll(play.getCards());
                    creature2 = creature2.getType("Creature");                   
                    if(player != "Human"){
                		if(creature2.size() > 0) {
                        Card[] Target = new Card[creature2.size()];
                        for(int i = 0; i < creature2.size(); i++) {
            				Card crd = creature2.get(i);
            				Target[i] = crd;
                        }
                        Object check = AllZone.Display.getChoice("Select creature", Target);
                        if(check != null) {
                            setTargetCard((Card) check);
                        }
                		}
                    } else {
                    		if(creature2.size() > 0) {
                            Card smallest = creature2.get(0);
                            for(int i = 0; i < creature2.size(); i++)
                                if(smallest.getNetAttack() < creature2.get(i).getNetAttack()) smallest = creature2.get(i);                         
                            		setTargetCard(smallest);
                    				}
                    }
                    Card c = getTargetCard();
                    AllZone.GameAction.sacrifice(c);
                    
                                 
					// Opponent Discards 3 Cards
                    PlayerZone Ohand = AllZone.getZone(Constant.Zone.Hand, AllZone.GameAction.getOpponent(card.getController()));
                    Card h[] = Ohand.getCards();
                    Card[] handChoices = Ohand.getCards();
                    int Handsize = 3;
                    if(h.length <= 3) Handsize = h.length;
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    Card choice = null; 

                    for(int i = 0; i < Handsize; i++) {
                            AllZone.Display.showMessage("Select a card to discard " + (3 - i) + " more to discard");
                            ButtonUtil.enableOnlyCancel();
                        handChoices = Ohand.getCards();
                        //human chooses
                        if(opponent.equals(Constant.Player.Human)) {
                            choice = AllZone.Display.getChoice("Choose", handChoices);
                        } else//computer chooses
                        {
                            choice = CardUtil.getRandom(handChoices);
                        }
                        
                        AllZone.GameAction.discard(choice, this);
                    }
                    
					// Opponent Loses 5 Life
                    PlayerLife target = AllZone.GameAction.getPlayerLife(opponent);
			        target.subtractLife(5,card);

					// Player Returns Creature Card from Graveyard to Hand
                    if(player == "Human") {                 	
                        AllZone.Display.showMessage("Return a creature from your graveyard to your hand: ");
                        ButtonUtil.enableOnlyCancel();
                        }
                    
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    if(zone != null) {
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature"); 

                    if(player == "Human"){
                        Card[] Target = new Card[creature.size()];
                        for(int i = 0; i < creature.size(); i++) {
            				Card crd = creature.get(i);
            				Target[i] = crd;
                        }
                        Object check = AllZone.Display.getChoiceOptional("Select creature", Target);
                        if(check != null) {
                            setTargetCard((Card) check);
                        } 
                    } else {
                    		if(creature.size() > 0) {
                            Card biggest = creature.get(0);
                            for(int i = 0; i < creature.size(); i++)
                                if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                            		setTargetCard(biggest);
                    				}
                    }
                    Card c2 = getTargetCard();
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController()); 	                        
                    if(AllZone.GameAction.isCardInZone(c2, grave)) {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                        AllZone.GameAction.moveTo(hand, c2);
                    }
                    }
					// Player Draws 3 Cards
                    for(int i = 0; i < 3; i++) {
                        AllZone.GameAction.drawCard(card.getController());
                    }
					// Player Gains 5 Life
                    AllZone.GameAction.gainLife(card.getController(), 5);
			     
				} // Resolve

				public boolean canPlayAI() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerLife target = AllZone.GameAction.getPlayerLife(opponent);	
                    PlayerZone Lib = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList Deck = new CardList();
                    Deck.addAll(Lib.getCards()); 
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(card.getController()));                   
                    CardList creature = new CardList();
                    creature.addAll(play.getCards());
                    creature = creature.getType("Creature"); 
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    CardList creature2 = new CardList();
                    creature2.addAll(zone.getCards());
                    creature2 = creature2.getType("Creature");
                return (Deck.size() > 2 && (target.getLife() <= 5 || (creature.size() > 0 && creature2.size() > 0)));
        }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Tendrils of Agony")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598023699468746L;

				@Override
                public void resolve() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerLife target = AllZone.GameAction.getPlayerLife(opponent);
			        target.subtractLife(2,card);
			        AllZone.GameAction.gainLife(card.getController(), 2);
				}
                
                public boolean canPlayAI() {
                return 1 < Phase.StormCount;
        }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Roiling Terrain")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -65124658746L;

				@Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        AllZone.GameAction.destroy(getTargetCard());
                        CardList Grave = new CardList(AllZone.getZone(Constant.Zone.Graveyard, getTargetCard().getController()).getCards());
                        int Damage = (Grave.getType("Land")).size();
                        AllZone.GameAction.getPlayerLife(getTargetCard().getController()).subtractLife(Damage,card);
				}
				}
                @Override
                public void chooseTargetAI() {
                    //target basic land that Human only has 1 or 2 in play
                    CardList land = new CardList(AllZone.Human_Play.getCards());
                    land = land.getType("Land");
                    
                    Card target = null;
                    
                    String[] name = {"Forest", "Swamp", "Plains", "Mountain", "Island"};
                    for(int i = 0; i < name.length; i++)
                        if(land.getName(name[i]).size() == 1) {
                            target = land.getName(name[i]).get(0);
                            break;
                        }
                    
                    //see if there are only 2 lands of the same type
                    if(target == null) {
                        for(int i = 0; i < name.length; i++)
                            if(land.getName(name[i]).size() == 2) {
                                target = land.getName(name[i]).get(0);
                                break;
                            }
                    }//if
                    if(target == null) {
                        land.shuffle();
                        target = land.get(0);
                    }
                    setTargetCard(target);
                }//chooseTargetAI()
                
                @Override
                public boolean canPlayAI() {
                    CardList land = new CardList(AllZone.Human_Play.getCards());
                    land = land.getType("Land");
                    return land.size() != 0;
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Land"));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Volcanic Awakening")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -650147710658746L;

				@Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        AllZone.GameAction.destroy(getTargetCard());
				}
				}
                @Override
                public void chooseTargetAI() {
                    //target basic land that Human only has 1 or 2 in play
                    CardList land = new CardList(AllZone.Human_Play.getCards());
                    land = land.getType("Land");
                    
                    Card target = null;
                    
                    String[] name = {"Forest", "Swamp", "Plains", "Mountain", "Island"};
                    for(int i = 0; i < name.length; i++)
                        if(land.getName(name[i]).size() == 1) {
                            target = land.getName(name[i]).get(0);
                            break;
                        }
                    
                    //see if there are only 2 lands of the same type
                    if(target == null) {
                        for(int i = 0; i < name.length; i++)
                            if(land.getName(name[i]).size() == 2) {
                                target = land.getName(name[i]).get(0);
                                break;
                            }
                    }//if
                    if(target == null) {
                        land.shuffle();
                        target = land.get(0);
                    }
                    setTargetCard(target);
                }//chooseTargetAI()
                
                @Override
                public boolean canPlayAI() {
                    CardList land = new CardList(AllZone.Human_Play.getCards());
                    land = land.getType("Land");
                    return land.size() != 0;
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Land"));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Mind's Desire"))
        {
            final Spell PlayCreature = new Spell(card) {
                private static final long serialVersionUID = 53838791023456795L;                   
                @Override
                public void resolve() {
                    String player = card.getController();
					PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
			        PlayerZone RFG = AllZone.getZone(Constant.Zone.Removed_From_Play, player);
					Card[] Attached = card.getAttachedCards(); 
					RFG.remove(Attached[0]);
	                play.add(Attached[0]);
	                card.unattachCard(Attached[0]);
                }//resolve()
            };//SpellAbility
            
      	  final Ability freeCast = new Ability(card, "0")
            {
  			private static final long serialVersionUID = 4455819149429678456L;

  			@Override
  			public void resolve() {
            	Card target = null;
            	Card c = null;
                String player = card.getController();
                if(player == "Human"){
                	Card[] Attached = getSourceCard().getAttachedCards(); 
                	Card [] Choices = new Card[Attached.length];
                	boolean SystemsGo = true;
                	if(AllZone.Stack.size() > 0) {
                        CardList Config = new CardList();            		
                        for(int i = 0; i < Attached.length; i++) {	                      	
                        if(Attached[i].isInstant() == true || Attached[i].hasKeyword("Flash") == true) Config.add(Attached[i]);	
                	}                       
                    for(int i = 0; i < Config.size(); i++) {
        				Card crd = Config.get(i);
        				Choices[i] = crd;
                    }
                    if(Config.size() == 0) SystemsGo = false;
                	} else {
                        for(int i = 0; i < Attached.length; i++) {	
                        	Choices[i] =  Attached[i];               		
                	}
            	}
                Object check = null;
                if(SystemsGo == true) {
                	check = AllZone.Display.getChoiceOptional("Select Card to play for free", Choices);                   	
	                if(check != null) {
	                   target = ((Card) check);
	                }
	                if(target != null) c = AllZone.CardFactory.copyCard(target);
	                
					if(c != null) {
						if(c.isLand() == true) {
		   					if(CardFactoryUtil.canHumanPlayLand()) {
		   					// todo(sol): would prefer this in GameAction.playLand somehow
		    					PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
				                play.add(c);
				                card.unattachCard(c);
				                CardFactoryUtil.playLandEffects(c);
		  		                AllZone.GameInfo.incrementHumanPlayedLands();
		   					} else {
		   					JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "", JOptionPane.INFORMATION_MESSAGE);
		   					}
						} else if(c.isPermanent() == true && c.isAura() == false) {
							c.removeIntrinsicKeyword("Flash"); // Stops the player from re-casting the flash spell.
							PlayCreature.setStackDescription(c.getName() + " - Copied from Mind's Desire");
		                	Card [] ReAttach = new Card[Attached.length]; 
		                	ReAttach[0] = c;
		                	int ReAttach_Count = 0;
		                    for(int i = 0; i < Attached.length; i++) {	                      	
		                    	if(Attached[i] != target) {
		                    		ReAttach_Count = ReAttach_Count + 1;
		                    		ReAttach[ReAttach_Count] = Attached[i];
		                    	}
		                	}
		                    // Clear Attached List
		                    for(int i = 0; i < Attached.length; i++) {	                      	
		                    	card.unattachCard(Attached[i]);
		                    }
		                    // Re-add
		                    for(int i = 0; i < ReAttach.length; i++) {	                      	
		                    	if(ReAttach[i] != null) card.attachCard(ReAttach[i]);
		                    }	
							target.addSpellAbility(PlayCreature);
		                    AllZone.Stack.add(PlayCreature);
		  				} else {
		  	  						AllZone.GameAction.playCardNoCost(c);
			  						card.unattachCard(c); 
		  				}
	  				} else JOptionPane.showMessageDialog(null, "Player cancelled or there is no more cards available on Mind's Desire.", "", JOptionPane.INFORMATION_MESSAGE);
  				} else JOptionPane.showMessageDialog(null, "You can only play an instant at this point in time, but none are attached to Mind's Desire.", "", JOptionPane.INFORMATION_MESSAGE);
  			}
  			}
  			public boolean canPlayAI() {
            	return false;
  			}
  			
            };
            freeCast.setStackDescription("Mind's Desire - play card without paying its mana cost.");

            Command intoPlay = new Command() {
                private static final long serialVersionUID = 920148510259054021L;
                
                public void execute() {
					String player = AllZone.Phase.getActivePlayer();
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, player);
					Card Minds_D = card;
					if(player == "Human") AllZone.GameAction.shuffle(card.getController());
                		CardList MindsList = new CardList(Play.getCards());
                		MindsList = MindsList.getName("Mind's Desire");
                		MindsList.remove(card);
                		if(MindsList.size() > 0) {
                			Play.remove(card);   
                			Minds_D = MindsList.get(0);
                		} else JOptionPane.showMessageDialog(null, "Click Mind's Desire to see the available cards to play without paying its mana cost.", "", JOptionPane.INFORMATION_MESSAGE);			
        			        PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
        			        CardList libList = new CardList(lib.getCards());
        			        Card c = null;
        			        if(libList.size() > 0) {
        			        	c = libList.get(0);
            			        PlayerZone RFG = AllZone.getZone(Constant.Zone.Removed_From_Play, player);
                                AllZone.GameAction.moveTo(RFG, c);
                                Minds_D.attachCard(c); 
        			        }
                                final Card Minds = card;  
            	            //	AllZone.GameAction.exile(Minds);   
                                Minds.setImmutable(true);
                                Command untilEOT = new Command() {
                                    private static final long serialVersionUID = -28032591440730370L;
                                    
                                    public void execute() {
                                    	String player = AllZone.Phase.getActivePlayer();
                    					PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                    			        	play.remove(Minds);
                                            }
                                };
                                AllZone.EndOfTurn.addUntil(untilEOT);
                }
                
            };
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -2940969025405788931L;
                
                @Override
                public boolean canPlayAI() {
                	return false;
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(freeCast);
            spell.setDescription("");
        }
        //*************** END ************ END **************************  
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Doomsday")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1481112451519L;
                
                @Override
                public void resolve() {
                	CardList GraveandLibrary = new CardList();
                	String Player = card.getController();
                	GraveandLibrary.add(new CardList(AllZone.getZone(Constant.Zone.Library, Player).getCards()));
                	GraveandLibrary.add(new CardList(AllZone.getZone(Constant.Zone.Graveyard, Player).getCards()));
                	CardList NewLibrary = new CardList();
                	int Count = 5;
                	if(GraveandLibrary.size() < 5) Count = GraveandLibrary.size();
                	
                	for(int i = 0; i < Count; i++) {   
                	Card[] Search = GraveandLibrary.toArray();
                    AllZone.Display.showMessage("Select a card to put " + i + " from the top of the new library: "  + (Count - i) + " Choices to go.");
                    ButtonUtil.enableOnlyCancel();
                    Object check = AllZone.Display.getChoice("Select a card: ", Search);   
                    NewLibrary.add((Card) check);
                    GraveandLibrary.remove((Card) check);
                    
                	}
                	
			        PlayerZone RFG = AllZone.getZone(Constant.Zone.Removed_From_Play, Player);   
			        PlayerZone Library = AllZone.getZone(Constant.Zone.Library, Player);  
                    for(int i = 0; i < GraveandLibrary.size(); i++) AllZone.GameAction.moveTo(RFG,GraveandLibrary.get(i));
                    AllZone.GameAction.moveTo(RFG,card); // Not sure if Doomsday is supposed to be exiled
                    for(int i = 0; i < NewLibrary.size(); i++) AllZone.GameAction.moveTo(Library,NewLibrary.get(i));
                	
                    //lose half life
                        String player = Constant.Player.Human;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(life.getLife() / 2,card);
                }
                        
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Brilliant Ultimatum")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1481112451519L;
                
                @Override
                public void resolve() {
                    
                    Card choice = null;
                    
                    //check for no cards in hand on resolve
                    PlayerZone Library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList Lib = new CardList(Library.getCards());
                    PlayerZone Exile = AllZone.getZone(Constant.Zone.Removed_From_Play, card.getController());
                    CardList cards = new CardList();
                    CardList Exiled = new CardList();
                    if(Lib.size() == 0) {
                    	JOptionPane.showMessageDialog(null, "No more cards in library.", "", JOptionPane.INFORMATION_MESSAGE);
                    	return;
                    }
                    int Count = 5;
                    if(Lib.size() < 5) Count = Lib.size();
                    for(int i = 0; i < Count; i++) cards.add(Lib.get(i));                  	
                    for(int i = 0; i < Count; i++) {
                    	Exiled.add(Lib.get(i));
                    	AllZone.GameAction.moveTo(Exile, Lib.get(i));                  	
                    }
                    CardList Pile1 = new CardList();
                    CardList Pile2 = new CardList();
                    boolean stop = false;
                    int  Pile1CMC = 0;
                    int  Pile2CMC = 0;
                   

                        AllZone.Display.getChoice("Revealing top " + Count + " cards of library: ", cards.toArray());
                        //Human chooses
                        if(card.getController().equals(Constant.Player.Computer)) {
                        for(int i = 0; i < Count; i++) {
                        	if(stop == false) {
                        choice = AllZone.Display.getChoiceOptional("Choose cards to put into the first pile: ", cards.toArray());
                        if(choice != null) {
                        	Pile1.add(choice);
                        	cards.remove(choice);
                        	Pile1CMC = Pile1CMC + CardUtil.getConvertedManaCost(choice);
                        }
                        else stop = true;	
                        }
                        }
                        for(int i = 0; i < Count; i++) {
                        	if(!Pile1.contains(Exiled.get(i))) {
                        		Pile2.add(Exiled.get(i));
                        		Pile2CMC = Pile2CMC + CardUtil.getConvertedManaCost(Exiled.get(i));
                        	}
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("You have spilt the cards into the following piles" + "\r\n" + "\r\n");
                        sb.append("Pile 1: " + "\r\n");
                        for(int i = 0; i < Pile1.size(); i++) sb.append(Pile1.get(i).getName() + "\r\n");
                        sb.append("\r\n" + "Pile 2: " + "\r\n");
                        for(int i = 0; i < Pile2.size(); i++) sb.append(Pile2.get(i).getName() + "\r\n");
                        JOptionPane.showMessageDialog(null, sb, "", JOptionPane.INFORMATION_MESSAGE);
                        if(Pile1CMC >= Pile2CMC) {
                        	JOptionPane.showMessageDialog(null, "Computer chooses the Pile 1", "", JOptionPane.INFORMATION_MESSAGE);
	                    	  for(int i = 0; i < Pile1.size(); i++) {
									ArrayList<SpellAbility> choices = Pile1.get(i).getBasicSpells();

									for(SpellAbility sa:choices) {
										if(sa.canPlayAI()) {
											ComputerUtil.playStackFree(sa);
											if(Pile1.get(i).isPermanent()) Exiled.remove(Pile1.get(i));
											break;
										}
									}
	                    	  }
                        } else {
                        	JOptionPane.showMessageDialog(null, "Computer chooses the Pile 2", "", JOptionPane.INFORMATION_MESSAGE);
	                    	  for(int i = 0; i < Pile2.size(); i++) {
									ArrayList<SpellAbility> choices = Pile2.get(i).getBasicSpells();

									for(SpellAbility sa:choices) {
										if(sa.canPlayAI()) {
											ComputerUtil.playStackFree(sa);
											if(Pile2.get(i).isPermanent())  Exiled.remove(Pile2.get(i));
											break;
										}
									}
	                    	  }	
		    		}
                        
                    } else//Computer chooses (It picks the highest converted mana cost card and 1 random card.)
                    {
                        Card biggest = null;
                        biggest = Exiled.get(0);
                        
                        for(int i = 0; i < Count; i++) {
                            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) >= CardUtil.getConvertedManaCost(biggest.getManaCost())) {
                                biggest = cards.get(i);
                            }
                        }
                        Pile1.add(biggest);
                        cards.remove(biggest);
                        if(cards.size() > 0) { 
                        Card Random = CardUtil.getRandom(cards.toArray());
                        Pile1.add(Random);
                        }
                        for(int i = 0; i < Count; i++) if(!Pile1.contains(Exiled.get(i))) Pile2.add(Exiled.get(i));
                        StringBuilder sb = new StringBuilder();
                        sb.append("Choose a pile to add to your hand: " + "\r\n" + "\r\n");
                        sb.append("Pile 1: " + "\r\n");
                        for(int i = 0; i < Pile1.size(); i++) sb.append(Pile1.get(i).getName() + "\r\n");
                        sb.append("\r\n" + "Pile 2: " + "\r\n");
                        for(int i = 0; i < Pile2.size(); i++) sb.append(Pile2.get(i).getName() + "\r\n");
			        	Object[] possibleValues = {"Pile 1", "Pile 2"};
			        	Object q = JOptionPane.showOptionDialog(null, sb, "Brilliant Ultimatum", 
			        			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
			        			null, possibleValues, possibleValues[0]);
			        	boolean stop2 = false;
	                      if(q.equals(0)) {	  
	                    	  int Spells = Pile1.size();
	                    	  for( int i = 0; i < Spells; i++) {
	                          	if(stop2 == false) {
	                          Object check = AllZone.Display.getChoiceOptional("Select spells to play in reserve order: ", Pile1.toArray());
	                          if(check != null) {
	              	  					if(((Card) check).isLand() == true) {
	              	  	   					if(CardFactoryUtil.canHumanPlayLand()) {
		              	    					PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
		              	    					GameAction.playLand((Card)check, play);
	              	  	   					} else {
	              	  	   					JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "", JOptionPane.INFORMATION_MESSAGE);
	              	  	   					}
	              	  					} else if(((Card) check).isPermanent() == true && ((Card) check).isAura() == false) {	
	              	  	                    AllZone.Stack.add(((Card) check).getSpellAbility()[0]);
	              	  				} else {
	              	  	  						AllZone.GameAction.playCardNoCost(((Card) check));
	              	  				}
	                        	  Pile1.remove((Card) check);
	                          } 
	                    	  }  else stop2 = true;
	                    	  }
			    		} else {
	                    	  int Spells = Pile2.size();
	                    	  for( int i = 0; i < Spells; i++) {
	                          	if(stop2 == false) {
	                          Object check = AllZone.Display.getChoiceOptional("Select spells to play in reserve order: ", Pile2.toArray());
	                          if(check != null) {
            	  					if(((Card) check).isLand() == true) {
            	  	   					if(CardFactoryUtil.canHumanPlayLand()) {
            	  	   						PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
            	  	   						GameAction.playLand((Card)check, play);
            	  	   					} else {
            	  	   					JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "", JOptionPane.INFORMATION_MESSAGE);
            	  	   					}
            	  					} else if(((Card) check).isPermanent() == true && ((Card) check).isAura() == false) {	
            	  	                    AllZone.Stack.add(((Card) check).getSpellAbility()[0]);
            	  				} else {
            	  	  						AllZone.GameAction.playCardNoCost(((Card) check));
            	  				}
	                        	  Pile2.remove((Card) check);
	                          } 
	                    	  }  else stop2 = true;
	                    	  }
			    		}
                    }
                   Pile1.clear();
                   Pile2.clear();
                }//resolve()

                   			
                @Override
                public boolean canPlayAI() {
                	PlayerZone Library = AllZone.getZone(Constant.Zone.Library, card.getController());
                	CardList cards = new CardList(Library.getCards());
                    return cards.size() >= 8;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dragonstorm")) {
            SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 52740159316058876L;

				@Override
                public boolean canPlayAI() {
                    CardList list = AllZoneUtil.getPlayerCardsInLibrary(Constant.Player.Computer);
                    CardList dragons = new CardList();
                    
                    for(int i = 0; i < list.size(); i++) {
                        if(list.get(i).getType().contains("Dragon")
                                || list.get(i).getKeyword().contains("Changeling")) {
                            dragons.add(list.get(i));
                        }
                    }
                    return (0 < dragons.size() && (AllZone.Phase.getPhase().equals(Constant.Phase.Main2)));
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player == "Human"){
                        CardList list = AllZoneUtil.getPlayerCardsInLibrary(Constant.Player.Human);
                        CardList dragons = new CardList();
                        
                        for(int i = 0; i < list.size(); i++) {
                            if(list.get(i).getType().contains("Dragon")
                                    || list.get(i).getKeyword().contains("Changeling")) {
                                dragons.add(list.get(i));
                            }
                        }
                        
                        if(dragons.size() != 0) {
                            Object o = AllZone.Display.getChoiceOptional("Select an Dragon to put onto the battlefield", dragons.toArray());
                            
                            AllZone.GameAction.shuffle(card.getController());
                            if(o != null) {
                                //put card in hand
                                AllZone.Human_Library.remove(o);
                                AllZone.Human_Play.add((Card) o);
                            }
                        }//if
                    	
                    } else {
                        CardList list = AllZoneUtil.getPlayerCardsInLibrary(Constant.Player.Computer);
                        CardList dragons = new CardList();
                        
                        for(int i = 0; i < list.size(); i++) {
                            if(list.get(i).getType().contains("Dragon")
                                    || list.get(i).getKeyword().contains("Changeling")) {
                                dragons.add(list.get(i));
                            }
                        }
                                            
                        if(dragons.size() != 0) {
                            CardListUtil.sortAttack(dragons);
                            Card c = dragons.get(0);
                            AllZone.GameAction.shuffle(card.getController());
                            //move to hand
                            AllZone.Computer_Library.remove(c);
                            AllZone.Computer_Play.add(c);
                            
                            CardList l = new CardList();
                            l.add(c);
                            AllZone.Display.getChoiceOptional("Computer picked:", l.toArray());
                        }                    	
                    }
                    
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Feudkiller's Verdict")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5532477141899236266L;
                
                @Override
                public void resolve() {
                    PlayerLife life = AllZone.GameAction.getPlayerLife(card.getController());
                    AllZone.GameAction.gainLife(card.getController(), 10);
                    
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerLife oppLife = AllZone.GameAction.getPlayerLife(opponent);
                    
                    if(oppLife.getLife() < life.getLife()) makeToken();
                }//resolve()
                
                void makeToken() {
                    CardFactoryUtil.makeToken("Giant Warrior", "W 5 5 Giant Warrior", card, "W", new String[] {
                            "Creature", "Giant", "Warrior"}, 5, 5, new String[] {""});
                }//makeToken()
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        else if(cardName.equals("Flamebreak")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4224693616606508949L;
                
                @Override
                public boolean canPlayAI() {
                    if (AllZone.Computer_Life.getLife() <= 3)
                    	return false;
                    
                    if (AllZone.Human_Life.getLife() <= 3)
                    	return true;
                    
                    CardListFilter filter = new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isCreature() && CardFactoryUtil.canDamage(card, c) && (c.getNetDefense() - c.getDamage())< 4;
                    	}
                    };
                    
                    CardList humCreats = new CardList(AllZone.Human_Play.getCards());
                    humCreats = humCreats.filter(filter);
                    
                    CardList compCreats = new CardList(AllZone.Computer_Play.getCards());
                    compCreats = compCreats.filter(filter);
                    
                    return humCreats.size() > compCreats.size();
                    
                }
                
                @Override
                public void resolve() {
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                    all = all.getType("Creature");
                    
                    for(int i = 0; i < all.size(); i++)
                        if(!all.get(i).getKeyword().contains("Flying")) {
                            if(CardFactoryUtil.canDamage(card, all.get(i))) {
                                all.get(i).setShield(0);
                                all.get(i).addDamage(3, card);
                            }
                        }
                    
                    AllZone.Human_Life.subtractLife(3,card);
                    AllZone.Computer_Life.subtractLife(3,card);
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        

        
        //*************** START *********** START **************************
        else if(cardName.equals("Cranial Extraction")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8127696608769903507L;
                
                @Override
                @SuppressWarnings("unchecked")
                // Comparator
                public void resolve() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    Card choice = null;
                    
                    //check for no cards in library
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, opponent);
                    
                    if(library.size() == 0) //this is not right, but leaving it in here for now.
                    return;
                    
                    //human chooses
                    if(opponent.equals(Constant.Player.Computer)) {
                        CardList all = AllZone.CardFactory.getAllCards();
                        all.sort(new Comparator() {
                            public int compare(Object a1, Object b1) {
                                Card a = (Card) a1;
                                Card b = (Card) b1;
                                
                                return a.getName().compareTo(b.getName());
                            }
                        });
                        choice = AllZone.Display.getChoice("Choose", removeLand(all.toArray()));
                        
                        Card[] showLibrary = library.getCards();
                        Comparator<Card> com = new TableSorter(new CardList(showLibrary), 2, true);
                        Arrays.sort(showLibrary, com);
                        
                        AllZone.Display.getChoiceOptional("Opponent's Library", showLibrary);
                        AllZone.GameAction.shuffle(opponent);
                    }//if
                    else//computer chooses
                    {
                        //the computer cheats by choosing a creature in the human players library or hand
                        CardList all = new CardList();
                        all.addAll(AllZone.Human_Hand.getCards());
                        all.addAll(AllZone.Human_Library.getCards());
                        
                        CardList four = all.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                if(c.isLand()) return false;
                                
                                return 3 < CardUtil.getConvertedManaCost(c.getManaCost());
                            }
                        });
                        if(!four.isEmpty()) choice = CardUtil.getRandom(four.toArray());
                        else choice = CardUtil.getRandom(all.toArray());
                        
                    }//else
                    remove(choice, opponent);
                    AllZone.GameAction.shuffle(opponent);
                }//resolve()
                
                void remove(Card c, String player) {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);
                    
                    CardList all = new CardList();
                    all.addAll(hand.getCards());
                    all.addAll(grave.getCards());
                    all.addAll(library.getCards());
                    
                    for(int i = 0; i < all.size(); i++)
                        if(all.get(i).getName().equals(c.getName())) {
                            if(player.equals(Constant.Player.Human)) {
                                AllZone.GameAction.moveTo(AllZone.Human_Removed, all.get(i));
                            } else {
                                AllZone.GameAction.moveTo(AllZone.Computer_Removed, all.get(i));
                            }
                        }
                }//remove()
                
                @Override
                public boolean canPlayAI() {
                    Card[] c = removeLand(AllZone.Human_Library.getCards());
                    return 0 < c.length;
                }
                
                Card[] removeLand(Card[] in) {
                    CardList c = new CardList(in);
                    c = c.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isLand();
                        }
                    });
                    return c.toArray();
                }//removeLand()
            };//SpellAbility spell
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setBeforePayMana(new Input_PayManaCost(spell));
            spell.setStackDescription(card.getName() + " - targeting opponent");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Maelstrom Pulse")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4050843868789582138L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = getCreature();
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        return true;
                    }
                }//canPlayAI()
                
                CardList getCreature() {
                    CardList out = new CardList();
                    CardList list = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    list.shuffle();
                    
                    for(int i = 0; i < list.size(); i++)
                        if((list.get(i).getNetAttack() >= 2) && (list.get(i).getNetDefense() <= 2)) out.add(list.get(i));
                    
                    //in case human player only has a few creatures in play, target anything
                    if(out.isEmpty() && 0 < CardFactoryUtil.AI_getHumanCreature(2, card, true).size()
                            && 3 > CardFactoryUtil.AI_getHumanCreature(card, true).size()) {
                        out.addAll(CardFactoryUtil.AI_getHumanCreature(2, card, true).toArray());
                        CardListUtil.sortFlying(out);
                    }
                    return out;
                }//getCreature()
                

                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        
                        AllZone.GameAction.destroy(getTargetCard());
                        
                        if(!getTargetCard().isFaceDown()) {
                            //get all creatures
                            CardList list = new CardList();
                            list.addAll(AllZone.Human_Play.getCards());
                            list.addAll(AllZone.Computer_Play.getCards());
                            
                            list = list.getName(getTargetCard().getName());
                            list.remove(getTargetCard());
                            
                            if(!getTargetCard().isFaceDown()) for(int i = 0; i < list.size(); i++)
                                AllZone.GameAction.destroy(list.get(i));
                        }//is token?
                    }//in play?
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            Input target = new Input() {
                private static final long serialVersionUID = -4947592326270275532L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target non-land permanent for " + spell.getSourceCard());
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(zone.is(Constant.Zone.Play) && !card.isLand()) {
                        spell.setTargetCard(card);
                        if(this.isFree()) {
                            this.setFree(false);
                            AllZone.Stack.add(spell);
                            stop();
                        } else stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//Input
            
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Sunlance")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -8327380630150660908L;
                
                int                       damage           = 3;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage
                                && (!(flying.get(i)).isWhite())) {
                            return flying.get(i);
                        }
                    return null;
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        Card c = getTargetCard();
                        c.addDamage(damage, card);
                    }
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            //target
            Input target = new Input() {
                private static final long serialVersionUID = -579427555773493417L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target non-white creature for " + spell.getSourceCard());
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if((!card.isWhite()) && card.isCreature()
                            && zone.is(Constant.Zone.Play)) {
                        spell.setTargetCard(card);
                        if (this.isFree())
                        {
                        	this.setFree(false);
                            AllZone.Stack.add(spell);
                            stop();
                        }
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//SpellAbility - target
            
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Firebolt")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4100322462753117988L;
                
                int                       damage           = 2;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                    CardList hand = new CardList(compHand.getCards());
                    

                    if(AllZone.Human_Life.getLife() <= damage) return AllZone.GameAction.isCardInZone(card,
                            compHand);
                    
                    if(hand.size() >= 8) return true && AllZone.GameAction.isCardInZone(card, compHand);
                    
                    check = getFlying();
                    return check != null && AllZone.GameAction.isCardInZone(card, compHand);
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) {
                    	c = getAnyCreature();
                    	if (c == null) {
                    		setTargetPlayer(Constant.Player.Human);
                    		return;
                    	}
                    }
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                Card getAnyCreature() {
                    CardList creatures = CardFactoryUtil.AI_getHumanCreature(card, true);
                    for(int i = 0; i < creatures.size(); i++)
                        if(creatures.get(i).getNetDefense() <= damage) return creatures.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage,card);
                }
            };//SpellAbility
            
            final SpellAbility flashback = new Spell(card) {
                
                private static final long serialVersionUID = -4811352682106571233L;
                int                       damage           = 2;
                Card                      check;
                
                @Override
                public boolean canPlay() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    String phase = AllZone.Phase.getPhase();
                    String activePlayer = AllZone.Phase.getActivePlayer();
                    
                    return AllZone.GameAction.isCardInZone(card, grave)
                            && ((phase.equals(Constant.Phase.Main1) || phase.equals(Constant.Phase.Main2))
                                    && card.getController().equals(activePlayer) && AllZone.Stack.size() == 0);
                }
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    PlayerZone removed = AllZone.getZone(Constant.Zone.Removed_From_Play, card.getController());
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage,card);
                    
                    grave.remove(card);
                    removed.add(card);
                    
                }
            };//flashback
            flashback.setFlashBackAbility(true);
            flashback.setManaCost("4 R");
            flashback.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(flashback, true, false));
            flashback.setDescription("Flashback: 4R");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(flashback);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
            card.setFlashback(true);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        




        //*************** START *********** START **************************
        else if(cardName.equals("Erratic Explosion")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6003403347798646257L;
                
                int                       damage           = 3;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                    int damage = getDamage();
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            javax.swing.JOptionPane.showMessageDialog(null, "Erratic Explosion causes " + damage
                                    + " to " + getTargetCard());
                            
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(null, "Erratic Explosion causes " + damage
                                + " to " + getTargetPlayer());
                        AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage,card);
                    }
                }
                
                //randomly choose a nonland card
                int getDamage() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList notLand = new CardList(library.getCards());
                    notLand = notLand.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isLand();
                        }
                    });
                    notLand.shuffle();
                    
                    if(notLand.isEmpty()) return 0;
                    
                    Card card = notLand.get(0);
                    return CardUtil.getConvertedManaCost(card.getSpellAbility()[0]);
                }
            };//SpellAbility
            card.clearSpellAbility();
            
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("March of Souls")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -1468254925312413359L;
                
                @Override
                public void resolve() {
                    change(AllZone.Human_Play, card.getController());
                    change(AllZone.Computer_Play, card.getController());
                }
                
                public void change(PlayerZone play, String owner) {
                    Card[] c = play.getCards();
                    for(int i = 0; i < c.length; i++) {
                        if(c[i].isCreature()) {
                            AllZone.GameAction.destroyNoRegeneration(c[i]);
                            CardFactoryUtil.makeToken("Spirit", "W 1 1 Spirit", c[i], "W", new String[] {
                                    "Creature", "Spirit"}, 1, 1, new String[] {"Flying"});
                        }
                    }
                }//change()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Martial Coup")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = -29101524966207L;
                
                @Override
                public void resolve() {
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                	int Soldiers = card.getXManaCostPaid();
                	for(int i = 0; i < Soldiers; i++) {
                    CardFactoryUtil.makeToken("Soldier", "W 1 1 Soldier", card, "W", new String[] {
                            "Creature", "Soldier"}, 1, 1, new String[] {""}); 
                	}
                	if(Soldiers >= 5) {
                    for(int i = 0; i < all.size(); i++) {
                        Card c = all.get(i);
                        if(c.isCreature()) AllZone.GameAction.destroy(c);
                    }
                	}
                }// resolve()
                
                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    CardList computer = new CardList(AllZone.Computer_Play.getCards());
                    
                    human = human.getType("Creature");
                    computer = computer.getType("Creature");
                    
                    // the computer will at least destroy 2 more human creatures
                    return (computer.size() < human.size() - 1
                            || (AllZone.Computer_Life.getLife() < 7 && !human.isEmpty())) && ComputerUtil.getAvailableMana().size() >= 7;
                }
            };// SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Incendiary Command")) {
            //not sure what to call variables, so I just made up something
            final String[] m_player = new String[1];
            final Card[] m_land = new Card[1];
            
            final ArrayList<String> userChoice = new ArrayList<String>();
            
            final String[] cardChoice = {
                    "Incendiary Command deals 4 damage to target player",
                    "Incendiary Command deals 2 damage to each creature", "Destroy target nonbasic land",
                    "Each player discards all cards in his or her hand, then draws that many cards"};
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 9178547049760990376L;
                
                @Override
                public void resolve() {
//          System.out.println(userChoice);
//          System.out.println(m_land[0]);
//          System.out.println(m_player[0]);
                    //"Incendiary Command deals 4 damage to target player",
        			for(int i = 0; i <card.getChoices().size(); i++) {
        				if(card.getChoice(i).equals(cardChoice[0])) {
        					setTargetPlayer(card.getChoiceTarget(0)); 
        					AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(4,card);
        				}
        			}

                    //"Incendiary Command deals 2 damage to each creature",
                    if(userChoice.contains(cardChoice[1]) || card.getChoices().contains(cardChoice[1])) {
                        //get all creatures
                        CardList list = new CardList();
                        list.addAll(AllZone.Human_Play.getCards());
                        list.addAll(AllZone.Computer_Play.getCards());
                        list = list.getType("Creature");
                        

                        for(int i = 0; i < list.size(); i++) {
                            if(CardFactoryUtil.canDamage(card, list.get(i))) list.get(i).addDamage(2, card);
                        }
                    }
                    
                    //"Destroy target nonbasic land",
        			for(int i = 0; i <card.getChoices().size(); i++) {
        				if(card.getChoice(i).equals(cardChoice[2])) {
        			        PlayerZone Hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
        			        PlayerZone Cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
        			     //   CardList all = AllZone.CardFactory.getAllCards();
        			        CardList all = new CardList(Hplay.getCards());
        			        all.add(new CardList(Cplay.getCards()));
        			        for(int i2 = 0; i2 < all.size(); i2++) {
        			        if(String.valueOf(all.get(i2).getUniqueNumber()).equals(card.getChoiceTarget(card.getChoices().size() - 1))) {
        			        	setTargetCard(all.get(i2));	
            					AllZone.GameAction.destroy(getTargetCard());
        			        }
        			        }
        				}
        			}

                    //"Each player discards all cards in his or her hand, then draws that many cards"
                    if(userChoice.contains(cardChoice[3]) || card.getChoices().contains(cardChoice[3])) {
                        discardDraw(Constant.Player.Computer);
                        discardDraw(Constant.Player.Human);
                    }
                }//resolve()
                
                void discardDraw(String player) {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    int n = hand.size();
                    
                    //technically should let the user discard one card at a time
                    //in case graveyard order matters
                    for(int i = 0; i < n; i++)
                        AllZone.GameAction.discardRandom(player, this);
                    
                    for(int i = 0; i < n; i++)
                        AllZone.GameAction.drawCard(player);
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility
            
            final Command setStackDescription = new Command() {
                
                private static final long serialVersionUID = -4833850318955216009L;
                
                public void execute() {
                    ArrayList<String> a = new ArrayList<String>();
                    if(userChoice.contains(cardChoice[0]) || card.getChoices().contains(cardChoice[0])) a.add("deals 4 damage to " + m_player[0]);
                    
                    if(userChoice.contains(cardChoice[1]) || card.getChoices().contains(cardChoice[1])) a.add("deals 2 damage to each creature");
                    
                    if(userChoice.contains(cardChoice[2]) || card.getChoices().contains(cardChoice[2])) a.add("destroy " + m_land[0]);
                    
                    if(userChoice.contains(cardChoice[3]) || card.getChoices().contains(cardChoice[3])) a.add("each player discards all cards in his or her hand, then draws that many cards");
                    
                    String s = a.get(0) + ", " + a.get(1);
                    spell.setStackDescription(card.getName() + " - " + s);
                }
            };//Command
            

            final Input targetLand = new Input() {
                private static final long serialVersionUID = 1485276539154359495L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target nonbasic land");
                    ButtonUtil.enableOnlyCancel();
                    
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.isLand() && zone.is(Constant.Zone.Play) && !c.getType().contains("Basic")) {
                    	if(card.isCopiedSpell()) card.getChoiceTargets().remove(0);
                        m_land[0] = c;
                        spell.setTargetCard(c);
                        card.setSpellChoiceTarget(String.valueOf(c.getUniqueNumber()));
                        setStackDescription.execute();                        
                        stopSetNext(new Input_PayManaCost(spell));
                    }//if
                }//selectCard()
            };//Input targetLand
            
            final Input targetPlayer = new Input() {
                private static final long serialVersionUID = -2636869617248434242L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target player");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectPlayer(String player) {
                	if(card.isCopiedSpell()) card.getChoiceTargets().remove(0);
                    m_player[0] = player;
                    spell.setTargetPlayer(player);
                    card.setSpellChoiceTarget(player);
                    setStackDescription.execute();
                    //if user needs to target nonbasic land
                    if(userChoice.contains(cardChoice[2]) || card.getChoices().contains(cardChoice[2])) stopSetNext(targetLand);
                    else {
                        stopSetNext(new Input_PayManaCost(spell));
                    }
                }//selectPlayer()
            };//Input targetPlayer
            

            Input chooseTwoInput = new Input() {
                private static final long serialVersionUID = 5625588008756700226L;
                
                @Override
                public void showMessage() {
                	if(card.isCopiedSpell()) {
                        if(card.getChoices().contains(cardChoice[0])) stopSetNext(targetPlayer);
                        else if(card.getChoices().contains(cardChoice[2])) stopSetNext(targetLand);
                        else {
                            setStackDescription.execute();
                            
                            stopSetNext(new Input_PayManaCost(spell));
                        }
                	}
                	else {
                    //reset variables
                    m_player[0] = null;
                    m_land[0] = null;
                    card.getChoices().clear();
                    card.getChoiceTargets().clear();
                    userChoice.clear();
                    
                    ArrayList<String> display = new ArrayList<String>();
                    
                    //get all
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Play.getCards());
                    list.addAll(AllZone.Computer_Play.getCards());
                    
                    CardList land = list.getType("Land");
                    CardList basicLand = list.getType("Basic");
                    
                    display.add("Incendiary Command deals 4 damage to target player");
                    display.add("Incendiary Command deals 2 damage to each creature");
                    if(land.size() != basicLand.size()) display.add("Destroy target nonbasic land");
                    display.add("Each player discards all cards in his or her hand, then draws that many cards");
                    
                    ArrayList<String> a = chooseTwo(display);
                    //everything stops here if user cancelled
                    if(a == null) {
                        stop();
                        return;
                    }
                    
                    userChoice.addAll(a);
                    
                    if(userChoice.contains(cardChoice[0])) stopSetNext(targetPlayer);
                    else if(userChoice.contains(cardChoice[2])) stopSetNext(targetLand);
                    else {
                        setStackDescription.execute();
                        
                        stopSetNext(new Input_PayManaCost(spell));
                    }
                	}
                }//showMessage()
                
                ArrayList<String> chooseTwo(ArrayList<String> choices) {
                    ArrayList<String> out = new ArrayList<String>();
                    Object o = AllZone.Display.getChoiceOptional("Choose Two", choices.toArray());
                    if(o == null) return null;
                    
                    out.add((String) o);
                    card.addSpellChoice((String) o);
                    choices.remove(out.get(0));
                    o = AllZone.Display.getChoiceOptional("Choose Two", choices.toArray());
                    if(o == null) return null;
                    
                    out.add((String) o);
                    card.addSpellChoice((String) o);
                    return out;
                }//chooseTwo()
            };//Input chooseTwoInput
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.setSpellWithChoices(true);
            spell.setBeforePayMana(chooseTwoInput);
        }//*************** END ************ END **************************
        
        


        //*************** START *********** START **************************
        else if(cardName.equals("Plague Wind")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 6008660207658995400L;
                
                @Override
                public void resolve() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, opponent);
                    
                    CardList all = new CardList(play.getCards());
                    all = all.getType("Creature");
                    
                    for(int i = 0; i < all.size(); i++) {
                        Card c = all.get(i);
                        if(c.isCreature()) AllZone.GameAction.destroyNoRegeneration(c);
                    }
                }//resolve()

                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    
                    human = human.getType("Creature");
                    human = human.getNotKeyword("Indestructible");                    
                    
                    // the computer will at least destroy 1 creature
                    return !human.isEmpty();
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Overwhelming Forces")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7165356050118574287L;
                
                @Override
                public void resolve() {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, opponent);
                    
                    CardList all = new CardList(play.getCards());
                    all = all.getType("Creature");
                    
                    for(int i = 0; i < all.size(); i++) {
                        Card c = all.get(i);
                        if(c.isCreature()) AllZone.GameAction.destroy(c);
                        AllZone.GameAction.drawCard(card.getController());
                    }
                }//resolve()

                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Play.getCards());
                    
                    human = human.getType("Creature");
                    human = human.getNotKeyword("Indestructible");                    
                    
                    // the computer will at least destroy 1 creature
                    return !human.isEmpty();
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Wheel of Fortune")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7707418370887790709L;
                
                @Override
                public void resolve() {
                    discardDraw7(Constant.Player.Human);
                    discardDraw7(Constant.Player.Computer);
                }//resolve()
                
                void discardDraw7(String player) {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    Card[] c = hand.getCards();
                    for(int i = 0; i < c.length; i++)
                        AllZone.GameAction.discard(c[i], this);
                    
                    for(int i = 0; i < 7; i++)
                        AllZone.GameAction.drawCard(player);
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Armageddon") || cardName.equals("Ravages of War")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 432601263297207029L;
                
                @Override
                public void resolve() {
                    CardList all = new CardList();
                    all.addAll(AllZone.Human_Play.getCards());
                    all.addAll(AllZone.Computer_Play.getCards());
                    
                    for(int i = 0; i < all.size(); i++) {
                        Card c = all.get(i);
                        if(c.isLand()) AllZone.GameAction.destroy(c);
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    int human = countPower(AllZone.Human_Play);
                    int computer = countPower(AllZone.Computer_Play);
                    
                    return human < computer || MyRandom.percentTrue(10);
                }
                
                public int countPower(PlayerZone play) {
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Creature");
                    int power = 0;
                    for(int i = 0; i < list.size(); i++)
                        power += list.get(i).getNetAttack();
                    
                    return power;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************


        
        //*************** START *********** START **************************
        else if(cardName.equals("Bribery")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4267653042039058744L;
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                    
                    AllZone.GameAction.checkStateEffects();
                }
                
                public void humanResolve() {
                    //choose creature from opponents library to put into play
                    //shuffle opponent's library
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, opponent);
                    CardList choices = new CardList(library.getCards());
                    
                    choices = choices.getType("Creature");
                    Object o = AllZone.Display.getChoiceOptional("Choose a creature", choices.toArray());
                    if(o != null) resolve((Card) o);
                }
                
                public void computerResolve() {
                    CardList all = new CardList(AllZone.Human_Library.getCards());
                    all = all.filter(new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isCreature() && !c.getName().equals("Ball Lightning") && !c.getName().equals("Groundbreaker");
                    	}
                    });
                    
                    CardList flying = all.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getKeyword().contains("Flying");
                        }
                    });
                    //get biggest flying creature
                    Card biggest = null;
                    if(flying.size() != 0) {
                        biggest = flying.get(0);
                        
                        for(int i = 0; i < flying.size(); i++)
                            if(biggest.getNetAttack() < flying.get(i).getNetAttack()) biggest = flying.get(i);
                    }
                    
                    //if flying creature is small, get biggest non-flying creature
                    if(all.size() != 0 && (biggest == null || biggest.getNetAttack() < 3)) {
                        biggest = all.get(0);
                        
                        for(int i = 0; i < all.size(); i++)
                            if(biggest.getNetAttack() < all.get(i).getNetAttack()) biggest = all.get(i);
                    }
                    if(biggest != null) resolve(biggest);
                }//computerResolve()
                
                public void resolve(Card selectedCard) {
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, opponent);
                    
                    Card c = selectedCard;
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    
                    //need to set controller before adding it to "play"
                    c.setController(card.getController());
                    c.setSickness(true);
                    
                    library.remove(c);
                    play.add(c);
                    

                    AllZone.GameAction.shuffle(opponent);
                }//resolve()
            };
            
            spell.setBeforePayMana(new Input_PayManaCost(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Amnesia")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5456164079438881319L;
                
                @Override
                public void resolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, getTargetPlayer());
                    Card[] c = hand.getCards();
                    
                    for(int i = 0; i < c.length; i++)
                        if(!c[i].isLand()) AllZone.GameAction.discard(c[i], this);
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Deep Analysis")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 6317660847906461825L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.drawCard(card.getController());
                    AllZone.GameAction.drawCard(card.getController());
                }
                
                @Override
                public boolean canPlayAI() {
                    return AllZone.Computer_Hand.getCards().length <= 6;
                }
            };
            spell.setDescription("Target player draws two cards.");
            spell.setStackDescription(card.getName() + " - " + card.getController() + " draws two cards.");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "1 U", "3"));
            card.setFlashback(true);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Allied Strategies")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2730790148899002194L;
                
                @Override
                public void resolve() {
                    int n = countLandTypes();
                    
                    for(int i = 0; i < n; i++)
                        AllZone.GameAction.drawCard(getTargetPlayer());
                }
                
                int countLandTypes() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, getTargetPlayer());
                    CardList land = new CardList(play.getCards());
                    
                    String basic[] = {"Forest", "Plains", "Mountain", "Island", "Swamp"};
                    int count = 0;
                    
                    for(int i = 0; i < basic.length; i++) {
                        CardList c = land.getType(basic[i]);
                        if(!c.isEmpty()) count++;
                    }
                    
                    return count;
                }//countLandTypes()
                
                @Override
                public boolean canPlayAI() {
                    return AllZone.Computer_Hand.getCards().length <= 5;
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetComputer());
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
          

        //*************** START *********** START **************************
        else if(cardName.equals("Sylvan Tutor")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = 1873791994168389407L;

				@Override
                public boolean canPlayAI() {
                    return 6 < AllZone.Phase.getTurn();
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    CardList creature = new CardList(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    if(creature.size() != 0) {
                        Card c = creature.get(0);
                        AllZone.GameAction.shuffle(card.getController());
                        
                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        CardList list = new CardList();
                        list.add(c);
                        AllZone.Display.getChoiceOptional("Computer picked:", list.toArray());
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    list = list.getType("Creature");
                    
                    if(list.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select a creature", list.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put creature on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                    }//if
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Pulse of the Tangle")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 523613120207836692L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Beast", "G 3 3 Beast", card, "G",
                            new String[] {"Creature", "Beast"}, 3, 3, new String[] {""});
                    
                    //return card to hand if necessary
                    String opponent = AllZone.GameAction.getOpponent(card.getController());
                    PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, opponent);
                    PlayerZone myPlay = AllZone.getZone(Constant.Zone.Play, card.getController());
                    
                    CardList oppList = new CardList(oppPlay.getCards());
                    CardList myList = new CardList(myPlay.getCards());
                    
                    oppList = oppList.getType("Creature");
                    myList = myList.getType("Creature");
                    
                    //if true, return card to hand
                    if(myList.size() < oppList.size()) {
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                        hand.add(card);
                    } else AllZone.GameAction.moveToGraveyard(card);
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
       
        
        //*************** START *********** START **************************
        else if(cardName.equals("Call of the Herd")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 1959302998030377554L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Elephant", "G 3 3 Elephant", card, "G", new String[] {
                            "Creature", "Elephant"}, 3, 3, new String[] {""});
                }//resolve()
            };
            
            spell.setDescription("Put a 3/3 green Elephant creature token into play.");
            spell.setStackDescription(card.getController()
                    + " puts a 3/3 green Elephant creature token into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "3 G", "0"));
            
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Chatter of the Squirrel")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 3787460988525779623L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Squirrel", "G 1 1 Squirrel", card, "G", new String[] {
                            "Creature", "Squirrel"}, 1, 1, new String[] {""});
                }
            };
            
            spell.setDescription("Put a 1/1 green Squirrel creature token into play.");
            spell.setStackDescription(card.getController()
                    + " puts a 1/1 green Squirrel creature token into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "1 G", "0"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Acorn Harvest")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4779507778950336252L;
                
                @Override
                public void resolve() {
                    makeToken();
                    makeToken();
                }
                
                public void makeToken() {
                    CardFactoryUtil.makeToken("Squirrel", "G 1 1 Squirrel", card, "G", new String[] {
                            "Creature", "Squirrel"}, 1, 1, new String[] {""});
                }//resolve()
            };
            
            spell.setDescription("Put two 1/1 green Squirrel creature tokens into play.");
            spell.setStackDescription(card.getController()
                    + " puts two 1/1 green Squirrel creature tokens into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "1 G", "3"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Parallel Evolution")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3456160935845779623L;
                
                @Override
                public boolean canPlayAI() {
                    CardList humTokenCreats = new CardList();
                    humTokenCreats.addAll(AllZone.Human_Play.getCards());
                    humTokenCreats = getTokenCreats(humTokenCreats);
                    
                    CardList compTokenCreats = new CardList();
                    compTokenCreats.addAll(AllZone.Computer_Play.getCards());
                    compTokenCreats = getTokenCreats(compTokenCreats);
                    
                    return compTokenCreats.size() > humTokenCreats.size();
                }//canPlayAI()
                
                CardList getTokenCreats(CardList list) {
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && c.isToken();
                        }
                    });
                    return list;
                }//getTokenCreats()
                
                @Override
                public void resolve() {
                
                    // for each play zone add a copy of each creature token card
                    CardList AllTokenCreatures = new CardList();
                    AllTokenCreatures.addAll(AllZone.Human_Play.getCards());
                    AllTokenCreatures.addAll(AllZone.Computer_Play.getCards());
                    
                    AllTokenCreatures = getTokenCreats(AllTokenCreatures);
                    
                    CardFactoryUtil.copyTokens(AllTokenCreatures);
                
                }//resolve()
            };//SpellAbility
            
            spell.setDescription("For each creature token on the battlefield, its controller puts a token that's a copy of that creature onto the battlefield.");
            spell.setStackDescription("Parallel Evolution - For each creature token on the battlefield, its controller puts a token that's a copy of that creature onto the battlefield.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "4 G G G", "0"));
        }//*************** END ************ END **************************
        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Roar of the Wurm")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = -7861877439125080643L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Wurm", "G 6 6 Wurm", card, "G", new String[] {"Creature", "Wurm"},
                            6, 6, new String[] {""});
                }
            };
            
            spell.setDescription("Put a 6/6 green Wurm creature token into play.");
            spell.setStackDescription(card.getController() + " put a 6/6 green Wurm creature token into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "3 G", "0"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Crush of Wurms")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 3917531146741977318L;
                
                @Override
                public void resolve() {
                    makeToken();
                    makeToken();
                    makeToken();
                }
                
                public void makeToken() {
                    CardFactoryUtil.makeToken("Wurm", "G 6 6 Wurm", card, "G", new String[] {"Creature", "Wurm"},
                            6, 6, new String[] {""});
                }//resolve()
            };
            
            spell.setDescription("Put three 6/6 green Wurm creature tokens into play.");
            spell.setStackDescription(card.getController()
                    + " Put three 6/6 green Wurm creature tokens into play.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "9 G G G", "0"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Grizzly Fate")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 731860438110589738L;
                
                @Override
                public void resolve() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    CardList list = new CardList(grave.getCards());
                    makeToken();
                    makeToken();
                    if(list.size() >= 7) {
                        makeToken();
                        makeToken();
                    }
                }
                
                public void makeToken() {
                    CardFactoryUtil.makeToken("Bear", "G 2 2 Bear", card, "G", new String[] {"Creature", "Bear"},
                            2, 2, new String[] {""});
                }//resolve()
            };//SpellAbility
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getController()).append(" Puts 2/2 green Bear tokens into play.");
            spell.setStackDescription(sb.toString());
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "5 G G", "0"));
            
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Delirium Skeins")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 7901561313373975648L;
                
                @Override
                public void resolve() {
                    for(int i = 0; i < 3; i++)
                        AllZone.GameAction.discardRandom(Constant.Player.Computer, this);
                    
                    AllZone.InputControl.setInput(CardFactoryUtil.input_discard(3, this));
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Regrowth") || cardName.equals("Elven Cache")) {
        	// added cousin Reclaim since 90% of the code is shared
            final SpellAbility spell = new Spell(card) {
                
				private static final long serialVersionUID = -2069974600304157248L;

				@Override
                public void resolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    if(AllZone.GameAction.isCardInZone(getTargetCard(), graveyard)) {
                        graveyard.remove(getTargetCard());
                        hand.add(getTargetCard());
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    return graveyard.getCards().length != 0 && super.canPlay();
                }
            };
            Input runtime = new Input() {

				private static final long serialVersionUID = 3706956894704891104L;

				@Override
                public void showMessage() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    Object o = AllZone.Display.getChoiceOptional("Select target card", graveyard.getCards());
                    if(o == null) stop();
                    else {
                    	String location = "owner's hand";
                    	
                        spell.setStackDescription("Return " + o + " to " + location);
                        spell.setTargetCard((Card) o);
                        if(this.isFree()) 
                        {
                        	// WARNING: Read this before copying!
                        	// When we have an 'if (this.isFree())' in most input objects,
                        	// it's inside 'selectCard' not 'showMessage', and the usual 
                        	// order is 
                        	//   AllZone.Stack.add(spell);
                        	//   stop();
                        	// Here, we had to reverse the order of those two lines, or 
                        	// else the dialog for Regrowth would get put up twice 
                        	// when the card was played from Cascade. I think this 
                        	// has to do with when showMessage() is called versus 
                        	// selectCard().
                        	// This appears to be the only place we use this pattern. Be
                        	// careful when copying this code, and test your card with
                        	// Cascade or Isochron Scepter.
                        	this.setFree(false);
                        	stop();
                        	AllZone.Stack.add(spell);
                    	} 
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }//showMessage()
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("All", AllZone.Computer_Graveyard));
            spell.setBeforePayMana(runtime);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************       
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Ichor Slick")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -273970706213674570L;
                
                @Override
                public boolean canPlayAI() {
                    CardList c = CardFactoryUtil.AI_getHumanCreature(3, card, true);
                    CardListUtil.sortAttack(c);
                    CardListUtil.sortFlying(c);
                    
                    if(c.isEmpty()) return false;
                    else {
                        setTargetCard(c.get(0));
                        return true;
                    }
                }//canPlayAI()
                
                @Override
                public void resolve() {
                    final Card[] target = new Card[1];
                    final Command untilEOT = new Command() {
                        private static final long serialVersionUID = -1615047325868708734L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(target[0])) {
                                target[0].addTempAttackBoost(3);
                                target[0].addTempDefenseBoost(3);
                            }
                        }
                    };
                    
                    target[0] = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0])) {
                        target[0].addTempAttackBoost(-3);
                        target[0].addTempDefenseBoost(-3);
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }//resolve()
            };//SpellAbility
            
            Input target = new Input() {
                private static final long serialVersionUID = -7381927922574152604L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target creature for " + card.getName());
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(!CardFactoryUtil.canTarget(spell, card)) {
                        AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                    } else if(card.isCreature() && zone.is(Constant.Zone.Play)) {
                        spell.setTargetCard(card);
                        if(this.isFree()) 
                        {
                        	this.setFree(false);
                        	AllZone.Stack.add(spell);
                        	stop();
                    	} 
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }
            };//Input
            spell.setDescription("Target creature gets -3/-3 until end of turn");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_cycle(card, "2"));
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        

       

      
        //*************** START *********** START **************************
        else if(cardName.equals("Commune with Nature")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7652317332073733242L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    //get top 5 cards of library
                    CardList top = new CardList();
                    int limit = AllZone.Computer_Library.getCards().length;
                    
                    for(int i = 0; i < 5 && i < limit; i++) {
                        top.add(AllZone.Computer_Library.get(0));
                        AllZone.Computer_Library.remove(0);
                    }
                    
                    //put creature card in hand, if there is one
                    CardList creature = top.getType("Creature");
                    if(creature.size() != 0) {
                        AllZone.Computer_Hand.add(creature.get(0));
                        top.remove(creature.get(0));
                    }
                    
                    //put cards on bottom of library
                    for(int i = 0; i < top.size(); i++)
                        AllZone.Computer_Library.add(top.get(i));
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    
                    CardList list = new CardList();
                    for(int i = 0; i < 5 && i < library.getCards().length; i++)
                        list.add(library.get(i));
                    
                    //optional, select a creature
                    Object o = AllZone.Display.getChoiceOptional("Select a creature", list.toArray());
                    if(o != null && ((Card) o).isCreature()) {
                        AllZone.GameAction.moveTo(hand, (Card) o);
                        list.remove((Card) o);
                    }
                    
                    //put remaining cards on the bottom of the library
                    for(int i = 0; i < list.size(); i++) {
                        library.remove(list.get(i));
                        library.add(list.get(i));
                    }
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Kodama's Reach") || cardName.equals("Cultivate"))  {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -3361422153566629825L;
                
                @Override
                public void resolve() {
                	// Look for two basic lands: one goes into play tapped, one
                	// goes into your hand
                	AllZone.GameAction.searchLibraryTwoBasicLand(card.getController(),
                			Constant.Zone.Play, true, 
                			Constant.Zone.Hand, false);
                }
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
         
        
        //*************** START *********** START **************************
        else if(cardName.equals("Global Ruin")) {
            final CardList target = new CardList();
            final CardList saveList = new CardList();
            //need to use arrays so we can declare them final and still set the values in the input and runtime classes. This is a hack.
            final int[] index = new int[1];
            final int[] countBase = new int[1];
            final Vector<String> humanBasic = new Vector<String>();
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5739127258598357186L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                    //should check if computer has land in hand, or if computer has more basic land types than human.
                }
                
                @Override
                public void resolve() {
                    //add computer's lands to target
                    
                    //int computerCountBase = 0;
                    //Vector<?> computerBasic = new Vector();
                    
                    //figure out which basic land types the computer has
                    CardList land = new CardList(AllZone.Computer_Play.getCards()).getType("Land");
                    String basic[] = {"Forest", "Plains", "Mountain", "Island", "Swamp"};
                    
                    for(int i = 0; i < basic.length; i++) {
                        CardList cl = land.getType(basic[i]);
                        if(!cl.isEmpty()) {
                            //remove one land of this basic type from this list
                            //the computer AI should really jump in here and select the land which is the best.
                            //to determine the best look at which lands have enchantments, which lands are tapped
                            cl.remove(cl.get(0));
                            //add the rest of the lands of this basic type to the target list, this is the list which will be sacrificed.
                            target.addAll(cl.toArray());
                        }
                    }
                    
                    //need to sacrifice the other non-basic land types
                    land = land.filter(new CardListFilter() {
                        public boolean addCard(Card c){
                            if (c.getName().contains("Dryad Arbor")) return true;
                            else if (!(c.getType().contains("Forest") 
                                    || c.getType().contains("Plains") 
                                    || c.getType().contains("Mountain") 
                                    || c.getType().contains("Island") 
                                    || c.getType().contains("Swamp"))) return true;
                            else return false;
                        }
                    });
                    target.addAll(land.toArray());
                    
                    //when this spell resolves all basic lands which were not selected are sacrificed.
                    for(int i = 0; i < target.size(); i++)
                        if(AllZone.GameAction.isCardInPlay(target.get(i)) && !saveList.contains(target.get(i))) 
                            AllZone.GameAction.sacrifice(target.get(i));
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = 1739423591445361917L;
                private int               count;
                
                @Override
                public void showMessage() { //count is the current index we are on.
                    //countBase[0] is the total number of basic land types the human has
                    //index[0] is the number to offset the index by
                    count = countBase[0] - index[0] - 1; //subtract by one since humanBasic is 0 indexed.
                    if(count < 0) {
                        //need to reset the variables in case they cancel this spell and it stays in hand.
                        humanBasic.clear();
                        countBase[0] = 0;
                        index[0] = 0;
                        stop();
                    } else {
                        AllZone.Display.showMessage("Select target " + humanBasic.get(count)
                                + " land to not sacrifice");
                        ButtonUtil.enableOnlyCancel();
                    }
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.isLand() && zone.is(Constant.Zone.Play)
                            && c.getController().equals(Constant.Player.Human)
                            /*&& c.getName().equals(humanBasic.get(count))*/
                            && c.getType().contains(humanBasic.get(count)) 
                            /*&& !saveList.contains(c) */) {
                        //get all other basic[count] lands human player controls and add them to target
                        PlayerZone humanPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                        CardList land = new CardList(humanPlay.getCards()).getType("Land");
                        CardList cl = land.getType(humanBasic.get(count));
                        cl = cl.filter(new CardListFilter()
                        {
                            public boolean addCard(Card crd)
                            {
                                return !saveList.contains(crd);
                            }
                        });
                        
                        if (!c.getName().contains("Dryad Arbor")) {
                            cl.remove(c);
                            saveList.add(c);
                        }
                        target.addAll(cl.toArray());
                        
                        index[0]++;
                        showMessage();
                        
                        if(index[0] >= humanBasic.size()) stopSetNext(new Input_PayManaCost(spell));
                        
                        //need to sacrifice the other non-basic land types
                        land = land.filter(new CardListFilter() {
                            public boolean addCard(Card c){
                                if (c.getName().contains("Dryad Arbor")) return true;
                                else if (!(c.getType().contains("Forest") 
                                        || c.getType().contains("Plains") 
                                        || c.getType().contains("Mountain") 
                                        || c.getType().contains("Island") 
                                        || c.getType().contains("Swamp"))) return true;
                                else return false;
                            }
                        });
                        target.addAll(land.toArray());
                        
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                private static final long serialVersionUID = -122635387376995855L;
                
                @Override
                public void showMessage() {
                    countBase[0] = 0;
                    //figure out which basic land types the human has
                    //put those in an set to use later
                    CardList land = new CardList(AllZone.Human_Play.getCards());
                    String basic[] = {"Forest", "Plains", "Mountain", "Island", "Swamp"};
                    
                    for(int i = 0; i < basic.length; i++) {
                        CardList c = land.getType(basic[i]);
                        if(!c.isEmpty()) {
                            humanBasic.add(basic[i]);
                            countBase[0]++;
                        }
                    }
                    if(countBase[0] == 0) {
                        //human has no basic land, so don't prompt to select one.
                        stop();
                    } else {
                        index[0] = 0;
                        target.clear();
                        stopSetNext(input);
                    }
                }
            };//Input
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Gerrard's Verdict")) {
            SpellAbility spell = new Spell(card) {
                
                private static final long serialVersionUID = 4734024742326763385L;
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone humanHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human);
                    if(humanHand.size() >= 2) return true;
                    else return false;
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                    CardList list = new CardList(hand.getCards());
                    list.shuffle();
                    
                    if(list.size() == 0) return;
                    
                    Card c1 = list.get(0);
                    list.remove(c1);
                    /*
                    AllZone.Computer_Graveyard.add(c1);
                    AllZone.Computer_Hand.remove(c1);          
                    */
                    AllZone.GameAction.discard(c1, null);
                    
                    if(list.size() == 0) return;
                    
                    Card c2 = list.get(0);
                    list.remove(c2);
                    
                    /*
                    AllZone.Computer_Graveyard.add(c2);
                    AllZone.Computer_Hand.remove(c2);    
                    */
                    AllZone.GameAction.discard(c2, null);
                    
                    if(c1.getType().contains("Land")) {
                    	AllZone.GameAction.gainLife(Constant.Player.Human, 3);
                    }
                    
                    if(c2.getType().contains("Land")) {
                    	AllZone.GameAction.gainLife(Constant.Player.Human, 3);
                    }
                    

                }//resolve()
                
                public void computerResolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human);
                    CardList list = new CardList(hand.getCards());
                    
                    if(list.size() > 0) {
                        
                        Object o = AllZone.Display.getChoiceOptional("First card to discard", list.toArray());
                        
                        Card c = (Card) o;
                        list.remove(c);
                        
                        /*
                        hand.remove(c);
                        grave.add(c);
                        */
                        AllZone.GameAction.discard(c, null);
                        
                        if(c.getType().contains("Land")) {
                        	AllZone.GameAction.gainLife(Constant.Player.Computer, 3);
                        }
                        
                        if(list.size() > 0) {
                            Object o2 = AllZone.Display.getChoiceOptional("Second card to discard", list.toArray());
                            
                            Card c2 = (Card) o2;
                            list.remove(c2);
                            
                            /*
                            hand.remove(c2);
                            grave.add(c2);
                            */
                            AllZone.GameAction.discard(c2, null);
                            
                            if(c2.getType().contains("Land")) {
                            	AllZone.GameAction.gainLife(Constant.Player.Computer, 3);
                            }
                        }
                    }
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
   
        
        //*************** START *********** START **************************
        else if(cardName.equals("Reminisce")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 505983020365091226L;
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    // Move graveyard into library
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);
                    Card[] g = grave.getCards();
                    for(int i = 0; i < g.length; i++) {
                        grave.remove(g[i]);
                        library.add(g[i], 0);
                    }
                    // Shuffle library
                    AllZone.GameAction.shuffle(player);;
                }
                
                @Override
                public boolean canPlayAI()//97% of the time shuffling your grave into your library is a good thing
                {
                    setTargetPlayer(Constant.Player.Computer);
                    return true;
                }
                
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Sleight of Hand")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5608200094037045828L;
                
                @Override
                public boolean canPlay() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    if(library.size() >= 1 && super.canPlay()) return true;
                    else return false;
                    
                }
                
                @Override
                public void resolve() {
                    
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    CardList lib = new CardList(library.getCards());
                    
                    CardList topTwo = new CardList();
                    
                    if(lib.size() == 1) {
                        AllZone.GameAction.drawCard(card.getController());
                    } else {
                        if(card.getController().equals(Constant.Player.Human)) {
                            topTwo.add(lib.get(0));
                            topTwo.add(lib.get(1));
                            
                            Object o = AllZone.Display.getChoiceOptional("Select card to put in hand: ",
                                    topTwo.toArray());
                            
                            Card c1 = (Card) o;
                            topTwo.remove(c1);
                            library.remove(c1);
                            hand.add(c1);
                            
                            Card c2 = topTwo.get(0);
                            library.remove(c2);
                            library.add(c2);
                        } else //computer
                        {
                            Card c1 = lib.get(0);
                            library.remove(c1);
                            lib.remove(c1);
                            hand.add(c1);
                            
                            Card c2 = lib.get(0);
                            library.remove(c2);
                            lib.remove(c2);
                            library.add(c2); //put on bottom
                            
                        }
                        
                    }
                    
                }
            };
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************
      

        //*************** START *********** START **************************
        else if(cardName.equals("Cruel Tutor") || cardName.equals("Imperial Seal")) {
            SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -948983382014193129L;

				@Override
                public boolean canPlayAI() {
                    PlayerLife compLife = AllZone.GameAction.getPlayerLife("Computer");
                    int life = compLife.getLife();
                    if(4 < AllZone.Phase.getTurn() && AllZone.Computer_Library.size() > 0 && life >= 4) return true;
                    else return false;
                }
                
                @Override
                public void resolve() {
                    String player = card.getController();
                    if(player.equals(Constant.Player.Human)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    //TODO: somehow select a good non-creature card for AI
                    CardList creature = new CardList(AllZone.Computer_Library.getCards());
                    creature = creature.getType("Creature");
                    if(creature.size() != 0) {
                        Card c = CardFactoryUtil.AI_getBestCreature(creature);
                        
                        if(c == null) {
                            creature.shuffle();
                            c = creature.get(0);
                        }
                        
                        AllZone.GameAction.shuffle(card.getController());
                        
                        //move to top of library
                        AllZone.Computer_Library.remove(c);
                        AllZone.Computer_Library.add(c, 0);
                        
                        //lose 2 life
                        String player = Constant.Player.Computer;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(2,card);
                    }
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList(library.getCards());
                    
                    if(list.size() != 0) {
                        Object o = AllZone.Display.getChoiceOptional("Select a card", list.toArray());
                        
                        AllZone.GameAction.shuffle(card.getController());
                        if(o != null) {
                            //put card on top of library
                            library.remove(o);
                            library.add((Card) o, 0);
                        }
                        //lose 2 life
                        String player = Constant.Player.Human;
                        PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                        life.subtractLife(2,card);
                    }//if
                    

                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Invincible Hymn")) {
            final String player = card.getController();
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -827136493013927725L;
                
                @Override
                public void resolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList libCards = new CardList(library.getCards());
                    int lifeGain = libCards.size();
                    
                    Log.debug("Invincible Hymn", "lifeGain: " + lifeGain);
                    
                    PlayerLife life = AllZone.GameAction.getPlayerLife(player);
                    life.setLife(lifeGain);
                    
                    Log.debug("Invincible Hymn", "life.getLife(): " + life.getLife());
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList libCards = new CardList(library.getCards());
                    int lifeGain = libCards.size();
                    
                    PlayerLife compLife = AllZone.GameAction.getPlayerLife(Constant.Player.Computer);
                    if(lifeGain > compLife.getLife()) return true;
                    else return false;
                }
            };//spell
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
       
        //*************** START *********** START **************************
        else if(cardName.equals("Gift of Estates")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -4997834790204261916L;
                
                @Override
                public boolean canPlay() {
                    String oppPlayer = AllZone.GameAction.getOpponent(card.getController());
                    
                    PlayerZone selfZone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    PlayerZone oppZone = AllZone.getZone(Constant.Zone.Play, oppPlayer);
                    
                    CardList self = new CardList(selfZone.getCards());
                    CardList opp = new CardList(oppZone.getCards());
                    
                    self = self.getType("Land");
                    opp = opp.getType("Land");
                    
                    return (self.size() < opp.size()) && super.canPlay();
                }//canPlay()
                
                @Override
                public void resolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    
                    CardList plains = new CardList(library.getCards());
                    plains = plains.getType("Plains");
                    
                    for(int i = 0; i < 3 && i < plains.size(); i++)
                        AllZone.GameAction.moveTo(hand, plains.get(i));
                }//resolve()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        /* converted to keyword
        //*************** START *********** START **************************
        else if(cardName.equals("Feral Lightning")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -1841642966580694848L;
                
                @Override
                public boolean canPlayAI() {
                    return AllZone.Phase.getPhase().equals(Constant.Phase.Main1);
                }
                
                @Override
                public void resolve() {
                    int multiplier = 1;
                    int doublingSeasons = CardFactoryUtil.getCards("Doubling Season", card.getController()).size();
                    if(doublingSeasons > 0) multiplier = (int) Math.pow(2, doublingSeasons);
                    
                    final Card[] token = new Card[3 * multiplier];
                    final Command atEOT = new Command() {
                        private static final long serialVersionUID = -1928884889370422828L;
                        
                        public void execute() {
                            //destroy tokens at end of turn
                            for(int i = 0; i < token.length; i++)
                                if(AllZone.GameAction.isCardInPlay(token[i])) AllZone.GameAction.destroy(token[i]);
                        }
                    };
                    AllZone.EndOfTurn.addAt(atEOT);
                    
                    for(int i = 0; i < token.length; i++)
                        token[i] = makeToken();
                }//resolve()
                
                Card makeToken() {
                    Card c = new Card();
                    
                    c.setOwner(card.getController());
                    c.setController(card.getController());
                    
                    c.setName("Elemental");
                    c.setImageName("R 3 1 Elemental");
                    c.setManaCost("R");
                    c.setToken(true);
                    
                    c.addType("Creature");
                    c.addType("Elemental");
                    c.setBaseAttack(3);
                    c.setBaseDefense(1);
                    c.addIntrinsicKeyword("Haste");
                    c.setSacrificeAtEOT(true);
                    
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    play.add(c);
                    
                    return c;
                }//makeToken()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        */
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Hammer of Bogardan")) {
            final Ability ability2 = new Ability(card, "2 R R R") {
                
    			private static final long serialVersionUID = -5633123448009L;

    			@Override
                public void resolve() {
           //         PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
           //         grave.remove(card);          
                   card.addReplaceMoveToGraveyardCommand(new Command() {
                       private static final long serialVersionUID = -25594893330418L;
                       
                       public void execute() {
                           PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                           AllZone.GameAction.moveTo(hand, card);
                       }
                   });
    			}
                
                
                @Override
                public boolean canPlay() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    return AllZone.GameAction.isCardInZone(card, grave) && AllZone.GameAction.isPlayerTurn(card.getController());                   
                }
                
            };
            card.addSpellAbility(ability2);
            ability2.setFlashBackAbility(true);
            card.setUnearth(true);
            ability2.setDescription("2 R R R: Return Hammer of Bogardan from your graveyard to your hand. Activate this ability only during your upkeep.");
            ability2.setStackDescription(card.getName() + " returns from the graveyard to hand");
        }//*************** END ************ END **************************
        

        //*************** START ************ START **************************
        else if(cardName.equals("Goblin Grenade")) {
            final SpellAbility DamageCP = new Spell(card) {
                private static final long serialVersionUID = -4289150611689144985L;
                Card                      check;
                
                @Override
                public boolean canPlay() {
                    CardList gobs = new CardList(AllZone.getZone(Constant.Zone.Play, card.getController()).getCards());
                    gobs = gobs.getType("Goblin");
                    
                    return super.canPlay() && gobs.size() > 0;
                }
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= 5) return true;
                    
                    PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                    CardList hand = new CardList(compHand.getCards());
                    
                    if(hand.size() >= 8) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= 5) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
                    CardList hand = new CardList(compHand.getCards());
                    
                    if(getFlying() == null && hand.size() >= 7) //not 8, since it becomes 7 when getting cast
                    {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    
                    if(check == null && c != null) Log.debug("Goblin Grenade", "Check equals null");
                    else if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= 5) {
                        	Log.debug("Goblin Grenade", "getFlying() returns " + flying.get(i).getName());
                            return flying.get(i);
                        }
                    
                    Log.debug("Goblin Grenade", "getFlying() returned null");
                    return null;
                }
                
                @Override
                public void resolve() {
                    if(card.getController().equals(Constant.Player.Computer)) {
                        CardList gobs = new CardList(AllZone.getZone(Constant.Zone.Play, card.getController()).getCards());
                        gobs = gobs.getType("Goblin");
                        
                        if(gobs.size() > 0) {
                            CardListUtil.sortAttackLowFirst(gobs);
                            AllZone.GameAction.sacrifice(gobs.get(0));
                        }
                        //TODO, if AI can't sack, break out of this
                    }
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            //c.addDamage(damage);
                            AllZone.GameAction.addDamage(c, card, 5);
                        }
                    } else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(5,card);
                    //resolve()
                }
            }; //spellAbility
            DamageCP.setDescription(card.getName() + " deals 5 damage to target creature or player.");
            //DamageCP.setStackDescription(card.getName() +" deals 5 damage.");
            
            Input target = new Input() {
                private static final long serialVersionUID = 1843037500197925110L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target Creature, Player, or Planeswalker");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card crd, PlayerZone zone) {
                    if((crd.isCreature() || crd.isPlaneswalker()) && zone.is(Constant.Zone.Play)
                            && CardFactoryUtil.canTarget(DamageCP, crd)) {
                        DamageCP.setTargetCard(crd);
                        done();
                    }
                }//selectCard()
                
                @Override
                public void selectPlayer(String player) {
                    DamageCP.setTargetPlayer(player);
                    done();
                }
                
                void done() {
                    AllZone.Stack.add(DamageCP);
                    stop();
                }
            };
            
            Input targetSac = new Input() {
                
                private static final long serialVersionUID = -6102143961778874295L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a Goblin to sacrifice.");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card crd, PlayerZone zone) {
                    CardList choices = new CardList(
                            AllZone.getZone(Constant.Zone.Play, card.getController()).getCards());
                    choices = choices.getType("Goblin");
                    
                    if(choices.contains(crd)) {
                        AllZone.GameAction.sacrifice(crd);
                        //DamageCP.setTargetCard(crd);
                        if(DamageCP instanceof Ability_Tap && DamageCP.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                                (Ability_Tap) DamageCP));
                        else if(DamageCP.getManaCost().equals("0")) {
                            //AllZone.Stack.add(DamageCP);
                            stop();
                        } else stopSetNext(new Input_PayManaCost(DamageCP));
                    }
                }//selectCard()
            };
            
            DamageCP.setBeforePayMana(targetSac);
            DamageCP.setAfterPayMana(target);
            card.clearSpellAbility();
            card.addSpellAbility(DamageCP);
        }//*************** END ************ END **************************
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Innocent Blood")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3915880400376059369L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.sacrificeCreature(Constant.Player.Human, this);
                    AllZone.GameAction.sacrificeCreature(Constant.Player.Computer, this);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone cPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    
                    CardList hList = new CardList(hPlay.getCards());
                    CardList cList = new CardList(cPlay.getCards());
                    CardList smallCreats = cList.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && c.getNetAttack() < 2 && c.getNetDefense() < 3;
                        }
                    });
                    
                    hList = hList.getType("Creature");
                    
                    if(hList.size() == 0) return false;
                    
                    return smallCreats.size() > 0;
                }
            };
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Chainer's Edict")) {
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = 1139979866902867554L;

				@Override
                public void resolve() {
                    AllZone.GameAction.sacrificeCreature(getTargetPlayer(), this);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    CardList hList = new CardList(hPlay.getCards());
                    hList = hList.getType("Creature");
                    return hList.size() > 0;
                }
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            
            final SpellAbility flashback = new Spell(card) {
                private static final long serialVersionUID = -4889392369463499074L;
                
                @Override
                public boolean canPlay() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    String phase = AllZone.Phase.getPhase();
                    String activePlayer = AllZone.Phase.getActivePlayer();
                    
                    return AllZone.GameAction.isCardInZone(card, grave)
                            && ((phase.equals(Constant.Phase.Main1) || phase.equals(Constant.Phase.Main2))
                                    && card.getController().equals(activePlayer) && AllZone.Stack.size() == 0);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    CardList hList = new CardList(hPlay.getCards());
                    hList = hList.getType("Creature");
                    return hList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    PlayerZone removed = AllZone.getZone(Constant.Zone.Removed_From_Play, card.getController());
                    
                    AllZone.GameAction.sacrificeCreature(getTargetPlayer(), this);
                    
                    grave.remove(card);
                    removed.add(card);
                }
            };
            
            flashback.setManaCost("5 B B");
            flashback.setBeforePayMana(CardFactoryUtil.input_targetPlayer(flashback));
            flashback.setDescription("Flashback: 5 B B");
            flashback.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            
            card.addSpellAbility(flashback);
            card.setFlashback(true);

        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cruel Edict") || cardName.equals("Imperial Edict")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 4782606423085170723L;
                
                @Override
                public void resolve() {
                    AllZone.GameAction.sacrificeCreature(AllZone.GameAction.getOpponent(card.getController()),
                            this);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    CardList hList = new CardList(hPlay.getCards());
                    hList = hList.getType("Creature");
                    return hList.size() > 0;
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
      //*************** START *********** START **************************
        else if(cardName.equals("Rite of Replication")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -2902112019334177L;
                @Override
                public boolean canPlayAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    Card biggest = null;
                    if(zone != null) {
                    CardList creature = new CardList();
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature"); 
                    creature = creature.filter(new CardListFilter() {
						public boolean addCard(Card card) {
							return (!card.getType().contains("Legendary"));
						}
					});
                    if(creature.size() == 0) return false;
                    biggest = creature.get(0);
                    for(int i = 0; i < creature.size(); i++)
                        if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                    		setTargetCard(biggest);
                    }
                    return biggest.getNetAttack() > 4;
                }
                
                @Override
                public void chooseTargetAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    if(zone != null) {
                    CardList creature = new CardList();
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature"); 
                    creature = creature.filter(new CardListFilter() {
						public boolean addCard(Card card) {
							return (!card.getType().contains("Legendary"));
						}
					});
                    if(creature.size() > 0) {
                    Card biggest = creature.get(0);
                    for(int i = 0; i < creature.size(); i++)
                        if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                    		setTargetCard(biggest);
                    }
                    }
                }
                @Override
                public void resolve() {

                if(AllZone.GameAction.isCardInPlay(getTargetCard())
                        && CardFactoryUtil.canTarget(card, getTargetCard())) {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList DoublingSeasons = new CardList(play.getCards());
                    DoublingSeasons = DoublingSeasons.getName("Doubling Season");
                    PlayerZone_ComesIntoPlay.SimultaneousEntry = true;      
                    double Count = DoublingSeasons.size();
                    Count = Math.pow(2,Count);
                    for(int i = 0; i < Count; i++) {
                    	if(i + 1== Count) PlayerZone_ComesIntoPlay.SimultaneousEntry = false;                 
                    Card Copy = AllZone.CardFactory.copyCardintoNew(getTargetCard());
                    Copy.setToken(true);
                    Copy.setController(card.getController());
                    play.add(Copy);
                    }
                }             
                }//resolve()
            };
            
            spell.setDescription("Put a token onto the battlefield that's a copy of target creature.");
            spell.setStackDescription(card.getName() + " - " + card.getController()
                    + " puts a token onto the battlefield that's a copy of target creature.");
            
            SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = 13762512058673590L;
                @Override
                public boolean canPlayAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    Card biggest = null;
                    if(zone != null) {
                    CardList creature = new CardList();
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature"); 
                    creature = creature.filter(new CardListFilter() {
						public boolean addCard(Card card) {
							return (!card.getType().contains("Legendary"));
						}
					});
                    if(creature.size() == 0) return false;
                    biggest = creature.get(0);
                    for(int i = 0; i < creature.size(); i++)
                        if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                    		setTargetCard(biggest);
                    }
                    return biggest.getNetAttack() > 3;
                }
                
                @Override
                public void chooseTargetAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Play, card.getController());
                    if(zone != null) {
                    CardList creature = new CardList();
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature"); 
                    creature = creature.filter(new CardListFilter() {
						public boolean addCard(Card card) {
							return (!card.getType().contains("Legendary"));
						}
					});
                    if(creature.size() > 0) {
                    Card biggest = creature.get(0);
                    for(int i = 0; i < creature.size(); i++)
                        if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                    		setTargetCard(biggest);
                    }
                    }
                }
                @Override
                public void resolve() {
                	card.setKicked(true);
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                        CardList DoublingSeasons = new CardList(play.getCards());
                        DoublingSeasons = DoublingSeasons.getName("Doubling Season");
                        PlayerZone_ComesIntoPlay.SimultaneousEntry = true;      
                        int Count = DoublingSeasons.size();
                        Count = 5 * (int)Math.pow(2,Count);
                        for(int i = 0; i < Count; i++) {
                        	if(i + 1 == Count) PlayerZone_ComesIntoPlay.SimultaneousEntry = false;                 
                        Card Copy = AllZone.CardFactory.copyCardintoNew(getTargetCard());
                        Copy.setToken(true);
                        Copy.setController(card.getController());
                        play.add(Copy);
                        }
                    }            
                }//resolve()
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("7 U U");
            kicker.setAdditionalManaCost("5");
            kicker.setDescription("Kicker 5: If Rite of Replication was kicked, put five of those tokens onto the battlefield instead.");
            kicker.setStackDescription(card.getName() + " - " + card.getController()
                    + " puts five tokens onto the battlefield that's a copy of target creature.");
            kicker.setBeforePayMana(CardFactoryUtil.input_targetCreature(kicker));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(kicker);
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Conqueror's Pledge")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -2902179434079334177L;
                
                @Override
                public void resolve() {
                    for(int i = 0; i < 6; i++) {
                        CardFactoryUtil.makeToken("Kor Soldier", "W 1 1 Kor Soldier", card, "W", new String[] {
                                "Creature", "Kor", "Soldier"}, 1, 1, new String[] {""});
                    }//for
                }//resolve()
            };
            
            spell.setDescription("Put six 1/1 white Kor Soldier creature tokens onto the battlefield.");
            spell.setStackDescription(card.getName() + " - " + card.getController()
                    + " puts six 1/1 white Kor Soldier creature tokens onto the battlefield.");
            
            SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = 1376255732058673590L;
                
                @Override
                public void resolve() {
                    card.setKicked(true);
                    for(int i = 0; i < 12; i++) {
                        CardFactoryUtil.makeToken("Kor Soldier", "W 1 1 Kor Soldier", card, "W", new String[] {
                                "Creature", "Kor", "Soldier"}, 1, 1, new String[] {""});
                    }//for
                }//resolve()
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("8 W W W");
            kicker.setAdditionalManaCost("6");
            kicker.setDescription("Kicker 6: If Conqueror's Pledge was kicked, put twelve of those tokens onto the battlefield instead.");
            kicker.setStackDescription(card.getName() + " - " + card.getController()
                    + " puts twelve 1/1 white Kor Soldier creature tokens onto the battlefield.");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(kicker);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        if(cardName.equals("Tinker")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5878957726445248334L;
                
                @Override
                public boolean canPlay() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Artifact");
                    
                    return list.size() > 0;
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                    
                    CardList playList = new CardList(play.getCards());
                    playList = playList.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() && CardUtil.getConvertedManaCost(c.getManaCost()) <= 2;
                        }
                    });
                    
                    CardList libList = new CardList(lib.getCards());
                    libList = libList.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() && CardUtil.getConvertedManaCost(c.getManaCost()) > 5;
                        }
                    });
                    
                    if(libList.size() > 0 && playList.size() > 0) {
                        playList.shuffle();
                        setTargetCard(playList.get(0));
                        return true;
                    }
                    return false;
                    
                }
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    if(AllZone.GameAction.isCardInPlay(c)) {
                        
                        AllZone.GameAction.sacrifice(c);
                        
                        if(card.getController().equals(Constant.Player.Computer)) {
                            
                            CardList list = new CardList(lib.getCards());
                            list = list.filter(new CardListFilter() {
                                public boolean addCard(Card c) {
                                    return c.isArtifact() && CardUtil.getConvertedManaCost(c.getManaCost()) > 5;
                                }
                            });
                            
                            if(list.size() > 0) {
                                Card crd = CardFactoryUtil.AI_getBestArtifact(list);
                                lib.remove(crd);
                                play.add(crd);
                                AllZone.GameAction.shuffle(Constant.Player.Computer);
                            }
                        } else //human
                        {
                            CardList list = new CardList(lib.getCards());
                            list = list.filter(new CardListFilter() {
                                public boolean addCard(Card c) {
                                    return c.isArtifact();
                                }
                            });
                            if(list.size() > 0) {
                                Object o = AllZone.Display.getChoiceOptional("Select artifact", list.toArray());
                                
                                if(o != null) {
                                    Card crd = (Card) o;
                                    lib.remove(crd);
                                    play.add(crd);
                                    
                                }
                                AllZone.GameAction.shuffle(Constant.Player.Human);
                            }
                        }
                    }//if isCardInPlay
                }
            };
            /*
            final Command sac = new Command()
            {
            	private static final long serialVersionUID = -8925816099640324876L;

            	public void execute() {
            		AllZone.GameAction.sacrifice(spell.getTargetCard());
            	}
            };
            */

            Input runtime = new Input() {
                private static final long serialVersionUID = -4653972223582155502L;
                
                @Override
                public void showMessage() {
                    CardList choice = new CardList();
                    choice.addAll(AllZone.Human_Play.getCards());
                    choice = choice.getType("Artifact");
                    
                    boolean free = false;
                    if(this.isFree()) free = true;
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(spell, choice,
                            "Select artifact to sacrifice.", false, free));
                }
            };
            spell.setBeforePayMana(runtime);
            

            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Time Stretch")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -76579316599195788L;
                
                @Override
                public void resolve() {
                    AllZone.Phase.addExtraTurn(getTargetPlayer());
                    AllZone.Phase.addExtraTurn(getTargetPlayer());
                }
            };
            card.clearSpellAbility();
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetComputer());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
      //*************** START *********** START **************************
        else if(cardName.equals("Time Warp")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -76579316599195788L;
                
                @Override
                public void resolve() {
                    AllZone.Phase.addExtraTurn(getTargetPlayer());
                }
            };
            card.clearSpellAbility();
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetComputer());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Time Walk") || cardName.equals("Temporal Manipulation")
                || cardName.equals("Capture of Jingzhou")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 35300742940184315L;
                
                @Override
                public void resolve() {
                    //System.out.println("Turn: " + AllZone.Phase.getTurn());
                    AllZone.Phase.addExtraTurn(card.getController());
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************        
        

        //*************** START *********** START **************************
        if(cardName.equals("Glimpse the Unthinkable") || cardName.equals("Tome Scour")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    String player = getTargetPlayer();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    AllZone.GameAction.mill(getTargetPlayer(),
                    		(cardName.equals("Glimpse the Unthinkable")) ? 10 : 5);
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        if(cardName.equals("Traumatize")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    String player = getTargetPlayer();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    CardList libList = new CardList(lib.getCards());
                    
                    int max = libList.size() / 2;
                    
                    for(int i = 0; i < max; i++) {
                        Card c = libList.get(i);
                        lib.remove(c);
                        grave.add(c);
                    }
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        if(cardName.equals("Mind Funeral")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    String player = getTargetPlayer();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    CardList libList = new CardList(lib.getCards());
                    
                    int max = libList.size();
                    int count = 0;
                    int total = 0;
                    

                    for(int i = 0; i < max; i++) {
                        Card c = libList.get(i);
                        total = i;
                        if(c.getType().contains("Land")) {
                            count++;
                            if(count == 4) break;                          
                        }
                    }
                    
                    for(int i = 0; i <= total; i++) {
                        Card c = libList.get(i);
                        lib.remove(c);
                        grave.add(c);
                    }
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        if(cardName.equals("Haunting Echoes")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                	// Haunting Echoes shouldn't be cast if only basic land in graveyard or library is empty
                	CardList graveyard = AllZoneUtil.getPlayerGraveyard("Human");
                	CardList library = AllZoneUtil.getPlayerCardsInLibrary("Human");
                	int graveCount =  graveyard.size();
            		graveyard = graveyard.filter(new CardListFilter() {
        				public boolean addCard(Card c) {
        					return c.isBasicLand();
        				}
        			});
            		
                    return ((graveCount - graveyard.size() > 0) && library.size() > 0);
                }
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, player);
                    CardList libList = new CardList(lib.getCards());
                    CardList grvList = new CardList(grave.getCards());
                    
                    int max = libList.size();
                    int grv = grvList.size();
                    
                    for(int j = 0; j < grv; j++) {
                        Card g = grvList.get(j);
                        if(!g.getType().contains("Basic")) {
	                        for(int i = 0; i < max; i++) {
	                            Card c = libList.get(i);
	                            if(c.getName().equals(g.getName())) {
	                                lib.remove(c);
	                                exiled.add(c);
	                            }
	                        }
                            grave.remove(g);
                            exiled.add(g);
                        }
                    }
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Lobotomy")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 5338238621454661783L;
                
                @Override
                public void resolve() {
                    Card choice = null;
                    
                    //check for no cards in hand on resolve
                    String player = getTargetPlayer();
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, player);
                    CardList libList = new CardList(lib.getCards());
                    CardList grvList = new CardList(grave.getCards());
                    CardList fullHand = new CardList(hand.getCards());
                    Card[] handChoices = removeLand(hand.getCards());
                    
                    if(fullHand.size() > 0 && card.getController().equals(Constant.Player.Human)) AllZone.Display.getChoice(
                            "Revealing hand", fullHand.toArray());
                    
                    if (handChoices.length == 0)
                    	return;
                    
                    if(card.getController().equals(Constant.Player.Human)) {
                        choice = AllZone.Display.getChoice("Choose", handChoices);
                    } else //computer chooses
                    {
                        choice = CardUtil.getRandom(handChoices);
                    }
                    
                    String chosen = choice.getName();
                    
                    int max = libList.size();
                    for(int i = 0; i < max; i++) {
                        Card c = libList.get(i);
                        if(c.getName().equals(chosen)) {
                            lib.remove(c);
                            exiled.add(c);
                        }
                    }
                    int grv = grvList.size();
                    for(int i = 0; i < grv; i++) {
                        Card c = grvList.get(i);
                        if(c.getName().equals(chosen)) {
                            grave.remove(c);
                            exiled.add(c);
                        }
                    }
                    int hnd = fullHand.size();
                    for(int i = 0; i < hnd; i++) {
                        Card c = fullHand.get(i);
                        if(c.getName().equals(chosen)) {
                            hand.remove(c);
                            exiled.add(c);
                        }
                    }
                    
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    Card[] c = removeLand(AllZone.Human_Hand.getCards());
                    return 0 < c.length;
                }
                
                Card[] removeLand(Card[] in) {
                    CardList c = new CardList(in);
                    c = c.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.getType().contains("Basic");
                        }
                    });
                    return c.toArray();
                }//removeLand()
            };//SpellAbility spell
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************          
        

        //*************** START *********** START **************************
        if(cardName.equals("Identity Crisis")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    String player = getTargetPlayer();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    String player = getTargetPlayer();
                    
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
                    PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, player);
                    CardList handList = new CardList(hand.getCards());
                    CardList graveList = new CardList(grave.getCards());
                    
                    int max = handList.size();
                    for(int i = 0; i < max; i++) {
                        Card c = handList.get(i);
                        hand.remove(c);
                        exiled.add(c);
                    }
                    int grv = graveList.size();
                    for(int i = 0; i < grv; i++) {
                        Card c = graveList.get(i);
                        grave.remove(c);
                        exiled.add(c);
                    }
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        if(cardName.equals("Flame Rift")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6008296722680155321L;
                
                @Override
                public void resolve() {
                    AllZone.Human_Life.subtractLife(4,card);
                    AllZone.Computer_Life.subtractLife(4,card);
                }
                
                @Override
                public boolean canPlayAI() {
                    return AllZone.Computer_Life.getLife() > 7 && AllZone.Human_Life.getLife() < 7;
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
                
        //*************** START *********** START **************************
        else if(cardName.equals("Donate")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 782912579034503349L;
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    
                    if(c != null && AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        if(!c.isAura()) {
                            ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                            ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                            
                            PlayerZone from = AllZone.getZone(c);
                            from.remove(c);
                            
                            c.setController(AllZone.GameAction.getOpponent(card.getController()));
                            
                            PlayerZone to = AllZone.getZone(Constant.Zone.Play,
                                    AllZone.GameAction.getOpponent(card.getController()));
                            to.add(c);
                            
                            ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                            ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                        } else //Aura
                        {
                            c.setController(AllZone.GameAction.getOpponent(card.getController()));
                        }
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(AllZone.Computer_Play.getCards());
                    list = list.getName("Illusions of Grandeur");
                    
                    if(list.size() > 0) {
                        setTargetCard(list.get(0));
                        return true;
                    }
                    return false;
                }
            };
            
            Input runtime = new Input() {
                private static final long serialVersionUID = -7823269301012427007L;
                
                @Override
                public void showMessage() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
                    
                    CardList perms = new CardList();
                    perms.addAll(play.getCards());
                    perms = perms.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isPermanent() && !c.getName().equals("Mana Pool");
                        }
                    });
                    
                    boolean free = false;
                    if(this.isFree()) free = true;
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(spell, perms,
                            "Select a permanent you control", true, free));
                    
                }//showMessage()
            };//Input
            
            spell.setBeforePayMana(runtime);
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
                
        //*************** START *********** START **************************
        else if(cardName.equals("Bestial Menace")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 523613120207836692L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Snake", "G 1 1 Snake", card, "G",
                            new String[] {"Creature", "Snake"}, 1, 1, new String[] {""});
                    CardFactoryUtil.makeToken("Wolf", "G 2 2 Wolf", card, "G", new String[] {"Creature", "Wolf"},
                            2, 2, new String[] {""});
                    CardFactoryUtil.makeToken("Elephant", "G 3 3 Elephant", card, "G", new String[] {
                            "Creature", "Elephant"}, 3, 3, new String[] {""});
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //********************Start********Start***********************
        else if(cardName.equals("Living Death"))
        {
           final SpellAbility spell = new Spell(card)
           {
              private static final long serialVersionUID = -7657135492744579098L;
              
              public void resolve()
              {   //grab make 4 creature lists: human_play, human_graveyard, computer_play, computer_graveyard
                 CardList human_play = new CardList();
                 human_play.addAll(AllZone.Human_Play.getCards());
                 human_play = human_play.filter(new CardListFilter()
                 {
                    public boolean addCard(Card c) { return c.isCreature(); }
                 });
                 CardList human_graveyard = new CardList();
                 human_graveyard.addAll(AllZone.Human_Graveyard.getCards());
                 human_graveyard = human_graveyard.filter(new CardListFilter()
                 {
                    public boolean addCard(Card c) { return c.isCreature(); }
                 });
                 CardList computer_play = new CardList();
                 computer_play.addAll(AllZone.Computer_Play.getCards());
                 computer_play = computer_play.filter(new CardListFilter()
                 {
                    public boolean addCard(Card c) { return c.isCreature(); }
                 });
                 CardList computer_graveyard = new CardList();
                 computer_graveyard.addAll(AllZone.Computer_Graveyard.getCards());
                 computer_graveyard = computer_graveyard.filter(new CardListFilter()
                 {
                    public boolean addCard(Card c) { return c.isCreature(); }
                 });
                           
                 Card c = new Card();
                 Iterator<Card> it = human_play.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Human_Play,c);
                    AllZone.GameAction.moveTo(AllZone.Human_Graveyard,c);
                 }
                 
                 it = human_graveyard.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Human_Graveyard,c);
                    AllZone.GameAction.moveTo(AllZone.Human_Play,c);
                 }
                 
                 it = computer_play.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Computer_Play,c);
                    AllZone.GameAction.moveTo(AllZone.Computer_Graveyard,c);
                 }
                 
                 it = computer_graveyard.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Computer_Graveyard,c);
                    AllZone.GameAction.moveTo(AllZone.Computer_Play,c);
                 }
                 
              }//resolve
           };//spellability
           card.clearSpellAbility();
            card.addSpellAbility(spell);
         }//*********************END**********END***********************
        
      //*************** START *********** START **************************
      else if(cardName.equals("Exhume"))
      {
    	  final SpellAbility spell = new Spell(card)
          {
			private static final long serialVersionUID = 8073863864604364654L;

			public void resolve()
    		{
				
				  PlayerZone humanPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
  			      PlayerZone computerPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
				
    			  PlayerZone humanGrave = AllZone.getZone(Constant.Zone.Graveyard, Constant.Player.Human);
    			  PlayerZone computerGrave = AllZone.getZone(Constant.Zone.Graveyard, Constant.Player.Computer);
    			  
    			  CardList humanList = new CardList(humanGrave.getCards());
    			  humanList = humanList.getType("Creature");
    			  CardList computerList = new CardList(computerGrave.getCards());
    			  computerList = computerList.getType("Creature");
    			  
    			  Card c;
    			  if (humanList.size() > 0)
    			  {
    				  Object check = AllZone.Display.getChoiceOptional("Select creature to Exhume", humanList.toArray());
    				  if (check!=null)
    				  {
    					  c = (Card)check;
    					  humanGrave.remove(c);
    					  humanPlay.add(c);
    				  }
    				  
    			  }
    			  
    			  if (computerList.size() > 0)
    			  {
    				  c = CardFactoryUtil.AI_getBestCreature(computerList);
    				  if (c != null)
    				  {
    					  computerGrave.remove(c);
    					  computerPlay.add(c);
    				  }
    				  else
    				  {
    					  computerGrave.remove(computerList.get(0));
    					  computerPlay.add(computerList.get(0));
    				  }
    			  }
    			  
    		  }
    		  
    		  public boolean canPlayAI()
    		  {   
  			      PlayerZone humanGrave = AllZone.getZone(Constant.Zone.Graveyard, Constant.Player.Human);
  			      PlayerZone computerGrave = AllZone.getZone(Constant.Zone.Graveyard, Constant.Player.Computer);
				
    			  CardList humanList = new CardList(humanGrave.getCards());
    			  humanList = humanList.getType("Creature");
    			  CardList computerList = new CardList(computerGrave.getCards());
    			  computerList = computerList.getType("Creature");
    			  
    			  if (computerList.size() > 0)
    			  {
    				  if (humanList.size() == 0)
    					  return true;
    				  
    				  return CardFactoryUtil.AI_getBestCreature(computerList).getNetAttack() > 
    				  		 CardFactoryUtil.AI_getBestCreature(humanList).getNetAttack();
    			  }
    			  return false;
    		  }
          };
          card.clearSpellAbility();
          card.addSpellAbility(spell);
        
      }
      //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Prosperity"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = -4885933011194027735L;

  			public void resolve()
      		{
      			  for (int i=0;i<card.getXManaCostPaid();i++)
      			  {
      				  AllZone.GameAction.drawCard(Constant.Player.Human);
      				  AllZone.GameAction.drawCard(Constant.Player.Computer);
      			  }
      			  card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				return AllZone.Computer_Hand.size() < 5 && ComputerUtil.canPayCost("3 U");
  			}
      	  };
      	  spell.setDescription("Each player draws X cards.");
      	  spell.setStackDescription(card + " - Each player draws X cards.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Hurricane"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
			private static final long serialVersionUID = -7747687152948157277L;
			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && c.getKeyword().contains("Flying") &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
                  AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && c.getKeyword().contains("Flying") &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanFliers = new CardList(AllZone.Human_Play.getCards());
  			    humanFliers = humanFliers.filter(filter);
  			    
  			    CardList compFliers = new CardList(AllZone.Computer_Play.getCards());
  			    compFliers = compFliers.filter(filter);
  			    
  			    return humanFliers.size() > (compFliers.size() + 2) && AllZone.Computer_Life.getLife() > maxX + 3;
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature with flying and each player.");
      	  spell.setStackDescription(card + " - deals X damage to each creature with flying and each player.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Earthquake") || cardName.equals("Rolling Earthquake"))
        {
      	  final String[] keyword = new String[1];
      	  
      	  if (cardName.equals("Earthquake"))
      		  keyword[0] = "Flying";
      	  else
      		  keyword[0] = "Horsemanship";
      	  
      	  final SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = 2208504534888870597L;
  			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && !c.getKeyword().contains(keyword[0]) &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
                  AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - CardUtil.getConvertedManaCost(card);
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && !c.getKeyword().contains(keyword) &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList human = new CardList(AllZone.Human_Play.getCards());
  			    human = human.filter(filter);
  			    
  			    CardList comp = new CardList(AllZone.Computer_Play.getCards());
  			    comp = comp.filter(filter);
  			    
  			    return human.size() > (comp.size() + 2) && AllZone.Computer_Life.getLife() > maxX + 3;
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature without "+ keyword[0]+" and each player.");
      	  spell.setStackDescription(card + " - deals X damage to each creature without " +keyword[0] +" and each player.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
        
                
        //*************** START *********** START **************************
        else if(cardName.equals("Braingeyser"))
        {
      	  final SpellAbility spell = new Spell(card){
  			private static final long serialVersionUID = -7141472916367953810L;

  			public void resolve()
      		  {
      			  String player = getTargetPlayer();
      			  for(int i=0;i<card.getXManaCostPaid();i++)
      			  {
      				  AllZone.GameAction.drawCard(player);
      			  }
      			  card.setXManaCostPaid(0);
      		  }
      		  
      		  public boolean canPlayAI()
      		  {
      			  final int maxX = ComputerUtil.getAvailableMana().size() - 1;
      			  return maxX > 3 && AllZone.Computer_Hand.size() <= 3;
      		  }
      	  };
      	  spell.setDescription("Target player draws X cards.");
      	  spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
      	  spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        

        //*************** START *********** START **************************
      else if (cardName.equals("Beacon of Creation"))
      {
         SpellAbility spell = new Spell(card)
         {
			private static final long serialVersionUID = -2510951665205047650L;

    		public void resolve()
            {
             PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
              CardList land = new CardList(play.getCards());
              land = land.getType("Forest");
              makeToken();
                for(int i = 1; i < land.size(); i++)
                     makeToken();
            
              // shuffle back into library
            PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
            library.add(card);
            AllZone.GameAction.shuffle(card.getController());
            
            }//resolve()
          
          public void makeToken()
          {
             CardFactoryUtil.makeToken("Insect", "G 1 1 Insect", card, "G", new String[]{"Creature", "Insect"}, 1, 1, new String[] {""});
          }
          };
          card.clearSpellAbility();
          card.addSpellAbility(spell);
       }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Borrowing the East Wind"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = 3317055866601782361L;
  			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && c.getKeyword().contains("Horsemanship") &&
                  			   CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.GameAction.addDamage(Constant.Player.Human, card, damage);
                  AllZone.GameAction.addDamage(Constant.Player.Computer, card, damage);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - CardUtil.getConvertedManaCost(card);
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && c.getKeyword().contains("Horsemanship") &&
  							   CardFactoryUtil.canDamage(card, c) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList human = new CardList(AllZone.Human_Play.getCards());
  			    human = human.filter(filter);
  			    
  			    CardList comp = new CardList(AllZone.Computer_Play.getCards());
  			    comp = comp.filter(filter);
  			    
  			    return human.size() > (comp.size() + 2) && AllZone.Computer_Life.getLife() > maxX + 3;
  			}
      	  };
      	  spell.setDescription("Borrowing the East Wind deals X damage to each creature with horsemanship and each player.");
      	  spell.setStackDescription("Borrowing the East Wind - deals X damage to each creature with horsemanship and each player.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
          
        
        //*************** START *********** START **************************
        else if (cardName.equals("Savage Twister"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
			private static final long serialVersionUID = -2579177525478645067L;
			public void resolve()
      		{
  				CardList all = new CardList();
                  all.addAll(AllZone.Human_Play.getCards());
                  all.addAll(AllZone.Computer_Play.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  								
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && CardFactoryUtil.canDamage(card, c) && 
  							   maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanAll = new CardList(AllZone.Human_Play.getCards());
  			    humanAll = humanAll.filter(filter);
  			    
  			    CardList compAll = new CardList(AllZone.Computer_Play.getCards());
  			    compAll = compAll.filter(filter);
  			    
  			    return humanAll.size() > (compAll.size() + 2);
  			}
      	  };
      	  spell.setDescription(cardName + " deals X damage to each creature.");
      	  spell.setStackDescription(cardName + " - deals X damage to each creature.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
          
        
        //*************** START *********** START **************************
        else if(cardName.equals("Stream of Life"))
        {
      	  final SpellAbility spell = new Spell(card){
  			  private static final long serialVersionUID = 851280814064291421L;

  			  public void resolve()
      		  {
  				  AllZone.GameAction.gainLife(getTargetPlayer(), card.getXManaCostPaid());
      		      card.setXManaCostPaid(0);
      		  }
      		  
      		  public boolean canPlayAI()
      		  {
      			  int humanLife = AllZone.Human_Life.getLife();
      			  int computerLife = AllZone.Computer_Life.getLife();
      			  
      			  final int maxX = ComputerUtil.getAvailableMana().size() - 1;
      			  return maxX > 3 && (humanLife >= computerLife);
      		  }
      	  };
      	  spell.setDescription("Target player gains X life.");
      	  spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
      	  spell.setChooseTargetAI(CardFactoryUtil.AI_targetComputer());
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
          
        /*
        //*************** START *********** START **************************
        else if(cardName.equals("Goblin Offensive"))
        {
      	  final SpellAbility spell = new Spell(card){
  			  private static final long serialVersionUID = -8830760963758230870L;

  			  public void resolve()
      		  {
  				  for (int i = 0; i < card.getXManaCostPaid(); i ++)
  				  {
  					  makeToken();
  				  }
      			  card.setXManaCostPaid(0);
      		  }
  			  
  			  public void makeToken()
  	          {
  	             CardFactoryUtil.makeToken("Goblin", "R 1 1 Goblin", card, "R", new String[]{"Creature", "Goblin"}, 1, 1, new String[] {""});
  	          }
      		  
      		  public boolean canPlayAI()
      		  {
      			  final int maxX = ComputerUtil.getAvailableMana().size() - 3;
      			  return maxX > 2;
      		  }
      	  };
      	  spell.setDescription("Put X 1/1 red Goblin creature tokens onto the battlefield.");
      	  spell.setStackDescription("Goblin Offensive - put X 1/1 red Goblin creature tokens onto the battlefield.");
      	  
      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        */
          
                
        //*************** START *********** START **************************
        else if (cardName.equals("Lavalanche"))
        {
      	  final SpellAbility spell = new Spell(card)
      	  {
  			private static final long serialVersionUID = 3571646571415945308L;
  			public void resolve()
      		{
  				int damage = card.getXManaCostPaid();
  				
  				String player = getTargetPlayer();
  				PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                  CardList list = new CardList(play.getCards());
                  
                  list = list.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && CardFactoryUtil.canDamage(card, c);
                  	}
                  });
                  
                  for(int i = 0; i < list.size(); i++) {
                      	list.get(i).addDamage(card.getXManaCostPaid(), card);
                  }
                  
                  AllZone.GameAction.addDamage(player, card, damage);
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 3;
  				
  				if (AllZone.Human_Life.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && CardFactoryUtil.canDamage(card, c) && 
  							   maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList killableCreatures = new CardList(AllZone.Human_Play.getCards());
  				killableCreatures = killableCreatures.filter(filter);
  				
  				return (killableCreatures.size() >= 2);    // kill at least two of the human's creatures
  			}
      	  };
      	  spell.setDescription("Lavalanche deals X damage to target player and each creature he or she controls.");
      	  spell.setStackDescription("Lavalanche - deals X damage to target player and each creature he or she controls.");
      	  spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
      	  spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));

      	  card.clearSpellAbility();
      	  card.addSpellAbility(spell);
        } 
        //*************** END ************ END **************************
                 
        
        //*************** START *********** START **************************
        else if (cardName.equals("Psychic Drain"))
        {
        	final SpellAbility spell = new Spell(card){
        		private static final long serialVersionUID = -5739635875246083152L;

        		public void resolve()
        		{
        			AllZone.GameAction.mill(getTargetPlayer(),card.getXManaCostPaid());
        			
        			AllZone.GameAction.gainLife(card.getController(), card.getXManaCostPaid());
        			
        			card.setXManaCostPaid(0);
        		}
      		  
        		public boolean canPlayAI()
        		{
        			String player = getTargetPlayer();
        			PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
        			CardList libList = new CardList(lib.getCards());
      			  
        			int humanLife = AllZone.Human_Life.getLife();
        			int computerLife = AllZone.Computer_Life.getLife();
      			  
        			final int maxX = ComputerUtil.getAvailableMana().size() - 2;
        			return (maxX >= 3) && (humanLife >= computerLife) && (libList.size() > 0);
        		}
        	};
        	spell.setDescription("Target player puts the top X cards of his or her library into his or her graveyard and you gain X life.");
        	spell.setStackDescription("Psychic Drain - Target player puts the top X cards of his or her library into his or her graveyard and you gain X life.");
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Balance"))
        {
        	final SpellAbility spell = new Spell(card)
        	{
				private static final long serialVersionUID = -5941893280103164961L;

				public void resolve()
        		{
					//Lands:
					CardList humLand = new CardList(AllZone.Human_Play.getCards());
        			humLand = humLand.getType("Land");
        			CardList compLand = new CardList(AllZone.Computer_Play.getCards());
        			compLand = compLand.getType("Land");
        			
        			if (compLand.size() > humLand.size())
        			{
        				compLand.shuffle();
        				for (int i=0; i < compLand.size()-humLand.size();i++)
        					AllZone.GameAction.sacrifice(compLand.get(i));
        			}
        			else if (humLand.size() > compLand.size())
        			{
        				int diff = humLand.size() - compLand.size();
        				/*
        				List<Card> selection = AllZone.Display.getChoicesOptional("Select " + diff + " lands to sacrifice", humLand.toArray());
        				while(selection.size() != diff)
        					selection = AllZone.Display.getChoicesOptional("Select " + diff + " lands to sacrifice", humLand.toArray());
        				
	                    for(int m = 0; m < diff; m++)
	                    	AllZone.GameAction.sacrifice(selection.get(m));
	                    */
        				AllZone.InputControl.setInput(CardFactoryUtil.input_sacrificePermanents(diff, "Land"));
        			}
        			
        			//Hand
        			CardList humHand = new CardList(AllZone.Human_Hand.getCards());
        			CardList compHand = new CardList(AllZone.Computer_Hand.getCards());
        			
        			if (compHand.size() > humHand.size())
        			{
        				for (int i=0; i < compHand.size()-humHand.size();i++)
        					AllZone.GameAction.discardRandom(Constant.Player.Computer, this);
        			}
        			else if (humHand.size() > compHand.size())
        			{
        				int diff = humHand.size() - compHand.size();
        				AllZone.GameAction.discard(Constant.Player.Human, diff, this);
        			}
        			
        			//Creatures:
        			CardList humCreats = new CardList(AllZone.Human_Play.getCards());
        			humCreats = humCreats.getType("Creature");
        			CardList compCreats = new CardList(AllZone.Computer_Play.getCards());
        			compCreats = compCreats.getType("Creature");
        				
        			if (compCreats.size() > humCreats.size())
        			{
        				CardListUtil.sortAttackLowFirst(compCreats);
        				CardListUtil.sortCMC(compCreats);
        				compCreats.reverse();
        				for (int i=0; i < compCreats.size()-humCreats.size();i++)
        					AllZone.GameAction.sacrifice(compCreats.get(i));
        			}
        			else if (humCreats.size() > compCreats.size())
        			{
        				int diff = humCreats.size() - compCreats.size();
        				/*
        				List<Card> selection = AllZone.Display.getChoicesOptional("Select " + diff + " creatures to sacrifice", humCreats.toArray());
        				while(selection.size() != diff)
        					selection = AllZone.Display.getChoicesOptional("Select " + diff + " creatures to sacrifice", humCreats.toArray());
        				
	                    for(int m = 0; m < diff; m++)
	                    	AllZone.GameAction.sacrifice(selection.get(m));
	                    */
        				AllZone.InputControl.setInput(CardFactoryUtil.input_sacrificePermanents(diff, "Creature"));
        			}
        		}
        		
        		public boolean canPlayAI()
        		{
        			int diff = 0;
        			CardList humLand = new CardList(AllZone.Human_Play.getCards());
        			humLand = humLand.getType("Land");
        			CardList compLand = new CardList(AllZone.Computer_Play.getCards());
        			compLand = compLand.getType("Land");
        			diff += humLand.size() - compLand.size();
        			
        			CardList humCreats = new CardList(AllZone.Human_Play.getCards());
        			humCreats = humCreats.getType("Creature");
        			CardList compCreats = new CardList(AllZone.Computer_Play.getCards());
        			compCreats = compCreats.getType("Creature");
        			diff += 1.5 * (humCreats.size() - compCreats.size());
        			
        			CardList humHand = new CardList(AllZone.Human_Hand.getCards());
        			CardList compHand = new CardList(AllZone.Computer_Hand.getCards());
        			diff += 0.5 * (humHand.size() - compHand.size());
        			
        			return diff > 2;
        		}
        	};
        	
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Channel the Suns")) {
            final SpellAbility spell = new Spell(card) {
               
                private static final long serialVersionUID = -8509187529151755266L;
               
                @Override
                public void resolve() {
                    Card mp = AllZone.ManaPool;
                    mp.addExtrinsicKeyword("ManaPool:W");
                    mp.addExtrinsicKeyword("ManaPool:U");
                    mp.addExtrinsicKeyword("ManaPool:B");
                    mp.addExtrinsicKeyword("ManaPool:R");
                    mp.addExtrinsicKeyword("ManaPool:G");
                }
               
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
           
            spell.setStackDescription(cardName + " adds W U B R G to your mana pool");
            card.clearSpellAbility();
            card.addSpellAbility(spell);
           
            return card;
        }//*************** END ************ END **************************
        
        
      //*************** START *********** START **************************
        else if(cardName.equals("Riding the Dilu Horse"))
        {
        	SpellAbility spell = new Spell(card)
        	{
        		private static final long serialVersionUID = -620930445462994580L;


        		public boolean canPlayAI()
        		{
        			PlayerZone play = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

        			CardList list = new CardList(play.getCards());
        			list = list.filter(new CardListFilter()
        			{
        				public boolean addCard(Card c)
        				{
        					return c.isCreature() && !c.getKeyword().contains("Horsemanship") && !c.getKeyword().contains("Defender");
        				}
        			});
        			if (list.size() > 0) {
        				Card c = CardFactoryUtil.AI_getBestCreature(list, card);
        				setTargetCard(c);
        				return true;
        			}
        			return false;
        		}

        		public void resolve()
        		{
        			final Card[] target = new Card[1];


        			target[0] = getTargetCard();
        			if(AllZone.GameAction.isCardInPlay(target[0]) && CardFactoryUtil.canTarget(card, target[0]))
        			{
        				target[0].addTempAttackBoost(2);
        				target[0].addTempDefenseBoost(2);
        				target[0].addExtrinsicKeyword("Horsemanship");
      		  
        				//String s = target[0].getText();
        				target[0].setText("(+2/+2 and Horsemanship from " +card+")");
        			}
        		}//resolve()
        	};
        	spell.setDescription("Target creature gets +2/+2 and gains horsemanship. (This effect lasts indefinitely.)");
	        spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
	        card.clearSpellAbility();
	        card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Summer Bloom"))
        {
       	final SpellAbility spell = new Spell(card) {
			private static final long serialVersionUID = 5559004016728325736L;

			public boolean canPlayAI() {
   				// The computer should only play this card if it has at least 
   				// one land in its hand. Because of the way the computer turn
   				// is structured, it will already have played land to it's limit
   				PlayerZone hand = AllZone.getZone(Constant.Zone.Hand,
   						Constant.Player.Computer);

   				CardList list = new CardList(hand.getCards());

   				list = list.getType("Land");
   				if (list.size() > 0)
   					return true;
   				else
   					return false;
   			}
   			
   			public void resolve() {
   				final String thePlayer = card.getController();
   				if (thePlayer.equals(Constant.Player.Human))
   					AllZone.GameInfo.addHumanMaxPlayNumberOfLands(3);
   				else
   					AllZone.GameInfo.addComputerMaxPlayNumberOfLands(3);
   				
   				Command untilEOT = new Command()
   				{
					private static final long serialVersionUID = 1665720009691293263L;

					public void execute(){
   						if (thePlayer.equals(Constant.Player.Human))
   							AllZone.GameInfo.addHumanMaxPlayNumberOfLands(-3);
   						else
   							AllZone.GameInfo.addComputerMaxPlayNumberOfLands(-3);
 	            	}
   	          	};
       	          AllZone.EndOfTurn.addUntil(untilEOT);
       		}
       	};
       	card.clearSpellAbility();
       	card.addSpellAbility(spell);
       	
       	card.setSVar("PlayMain1", "TRUE");
       } //*************** END ************ END **************************
        
           
        //*************** START *********** START **************************
        else if(cardName.equals("Explore"))
        {
       	final SpellAbility spell = new Spell(card) {
   			private static final long serialVersionUID = 8377957584738695517L;

   			public boolean canPlayAI() {
   				// The computer should only play this card if it has at least 
   				// one land in its hand. Because of the way the computer turn
   				// is structured, it will already have played its first land.
   				PlayerZone hand = AllZone.getZone(Constant.Zone.Hand,
   						Constant.Player.Computer);

   				CardList list = new CardList(hand.getCards());

   				list = list.getType("Land");
   				if (list.size() > 0)
   					return true;
   				else
   					return false;
   			}
   			
   			public void resolve() {
   				final String thePlayer = card.getController();
   				if (thePlayer.equals(Constant.Player.Human))
   					AllZone.GameInfo.addHumanMaxPlayNumberOfLands(1);
   				else
   					AllZone.GameInfo.addComputerMaxPlayNumberOfLands(1);
   				
   				Command untilEOT = new Command()
   				{
   					
   					private static final long serialVersionUID = -2618916698575607634L;

   					public void execute(){
   						if (thePlayer.equals(Constant.Player.Human))
   							AllZone.GameInfo.addHumanMaxPlayNumberOfLands(-1);
   						else
   							AllZone.GameInfo.addComputerMaxPlayNumberOfLands(-1);
 	            	}
       	        };
       	        AllZone.EndOfTurn.addUntil(untilEOT);
       		}
       	};
       	card.clearSpellAbility();
       	card.addSpellAbility(spell);
       	
       	card.setSVar("PlayMain1", "TRUE");
       } //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Rampant Growth")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598323179507468746L;

				@Override
                public void resolve() {
					AllZone.GameAction.searchLibraryBasicLand(card.getController(), 
							Constant.Zone.Play, true);
				}
                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Basic");
                	return list.size() > 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Nature's Lore") || cardName.equals("Three Visits")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598323179507468746L;

				@Override
                public void resolve() {
					AllZone.GameAction.searchLibraryLand("Forest", card.getController(), 
							Constant.Zone.Play, false);
				}
                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Forest");
                	return list.size() > 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            String desc = "Search your library for a Forest card and put that card onto the battlefield. Then shuffle your library.";
            spell.setStackDescription(cardName + " - " + desc);
            spell.setDescription(desc);
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
                
        //*************** START *********** START **************************
        else if(cardName.equals("Hellion Eruption")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 5820870438419741058L;

				@Override
				public boolean canPlayAI() {
            		return getCreatures(Constant.Player.Computer).size() > 0;
            	}
				
                @Override
                public void resolve() {
                	Card[] c = getCreatures(card.getController()).toArray();
                	for(int i = 0; i < c.length; i++) {
                        if(c[i].isCreature()) {
                            AllZone.GameAction.sacrifice(c[i]);
                            CardFactoryUtil.makeToken("Hellion", "R 4 4 hellion", c[i], "R", new String[] {
                                    "Creature", "Hellion"}, 4, 4, new String[] {""});
                        }
                    }
                }
                
                private CardList getCreatures(String player) {
                	PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                	CardList creatures = new CardList();
                	creatures.addAll(play.getCards());
                	creatures = creatures.filter(new CardListFilter() {
                		public boolean addCard(Card c) {
                			return c.isCreature();
                		}
                	});
                	return creatures;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Repay in Kind")) {
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -4587825292642224776L;

				@Override
        		public boolean canPlayAI() {
        			return AllZone.Human_Life.getLife() > AllZone.Computer_Life.getLife();
        		}

        		@Override
        		public void resolve() {
        			int humanLife = AllZone.Human_Life.getLife();
        			int compLife = AllZone.Computer_Life.getLife();
        			if( humanLife > compLife ) {
        				AllZone.Human_Life.setLife(compLife);
        			}
        			else if( compLife > humanLife ) {
        				AllZone.Computer_Life.setLife(humanLife);
        			}
        			else {
        				//they must be equal, so nothing to do
        			}
        		}
        	};//SpellAbility
        	
        	spell.setStackDescription(card.getName() + " - Each player's life total becomes the lowest life total among all players..");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if (cardName.equals("Haunting Misery"))
        {
        	final SpellAbility spell = new Spell(card){
				private static final long serialVersionUID = 6867051257656060195L;

				@Override
				public void resolve() {
					String player = card.getController();
					String tPlayer = getTargetPlayer();
					PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
					CardList graveList = new CardList(grave.getCards());
					
					graveList = graveList.getType("Creature");
					
					int size = graveList.size();
					int damage = 0;
					
					if( player.equals(Constant.Player.Human)) {
						for(int i = 0; i < size; i++) {
							Object o = AllZone.Display.getChoice("Remove from game", graveList.toArray());
							if(o == null) break;
							damage++;	// tally up how many cards removed
							Card c_1 = (Card) o;
							graveList.remove(c_1); //remove from the display list
							AllZone.GameAction.removeFromGame(c_1);
						}
					}
					else { //Computer
						// it would be nice if the computer chose vanilla creatures over 
						for(int j = 0; j < size; j++) {
							AllZone.GameAction.removeFromGame(graveList.get(j));
						}
					}
					AllZone.GameAction.addDamage(tPlayer, card, damage);
				}
				
				@Override
        		public void chooseTargetAI() {
        			setTargetPlayer(Constant.Player.Human);
        		}//chooseTargetAI()
				
				@Override
        		public boolean canPlayAI() {
        			String player = getTargetPlayer();
        			PlayerZone grave = AllZone.getZone(Constant.Zone.Library, player);
        			CardList graveList = new CardList(grave.getCards());
        			graveList = graveList.getType("Creature");
        			int humanLife = AllZone.Human_Life.getLife();

        			return (graveList.size() > 5 || graveList.size() > humanLife);
        		}
        	};
        	
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Perish the Thought")) {
        	final SpellAbility spell = new Spell(card){
        		private static final long serialVersionUID = -3317966427398220444L;

        		@Override
        		public void resolve() {
        			String player = card.getController();
        			String target = AllZone.GameAction.getOpponent(player);

        			PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, target);
        			PlayerZone lib = AllZone.getZone(Constant.Zone.Library, target);

        			CardList handList = new CardList(hand.getCards());

        			//choose one card from it
        			if(player.equals(Constant.Player.Human)){ 
        				Object o = AllZone.Display.getChoice("Put into library", handList.toArray());
        				//if(o == null) break;
        				Card c_1 = (Card) o;
        				if( c_1 != null ) {
        					hand.remove(c_1);
        					lib.add(c_1);
        				}
        			}
        			else { //computer
        				Card[] c = AllZone.getZone(Constant.Zone.Hand, target).getCards();
        				if(c.length != 0) {
        					Card toLib = CardUtil.getRandom(c);
        					hand.remove(toLib);
        					lib.add(toLib);
        				}
        			}
        			AllZone.GameAction.shuffle(target);
        		}

        		@Override
        		public boolean canPlayAI() {
        			return AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human).size() > 0;
        		}
        	};

        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Burning Inquiry")) {
        	final SpellAbility spell = new Spell(card){
				private static final long serialVersionUID = 7133052973770045886L;

				@Override
        		public void resolve() {
        			//each player draws three cards
        			AllZone.GameAction.drawCards(Constant.Player.Computer, 3);
        			AllZone.GameAction.drawCards(Constant.Player.Human, 3);
        			
        			//now, each player discards 3 cards at random
        			AllZone.GameAction.discardRandom(Constant.Player.Computer, 3, this);
        			AllZone.GameAction.discardRandom(Constant.Player.Human, 3, this);
        		}

        		@Override
        		public boolean canPlayAI() {
        			return AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer).size() > 0;
        		}
        	};
        	
        	spell.setStackDescription(cardName+" - each player draws 3 cards, then discards 3 cards at random.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Brood Birthing")) {
        	final SpellAbility spell = new Spell(card)
        	{
				private static final long serialVersionUID = -8303724057068847270L;

				public void resolve()
        		{
        			int times = 1;
        			CardList cl;
        			if (CardFactoryUtil.getCards("Eldrazi Spawn", card.getController()).size() > 0)
        				times = 3;
        			for (int i=0;i<times;i++)
        			{
	        			cl = CardFactoryUtil.makeToken("Eldrazi Spawn", "C 0 1 Eldrazi Spawn", card, "", new String[] {
								"Creature", "Eldrazi", "Spawn"}, 0, 1, new String[] {"Sacrifice CARDNAME: Add 1 to your mana pool."});
	        			for (Card crd:cl)
	        				crd.addSpellAbility(CardFactoryUtil.getEldraziSpawnAbility(crd));
        			}
        		}
        	};
        	spell.setStackDescription(cardName+" - " + card.getController() + " puts one or three 0/1 Eldrazi Spawn creature tokens onto the battlefield.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Growth Spasm")) {
            SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = 9074719023825939855L;

				@Override
                public void resolve() {
					AllZone.GameAction.searchLibraryBasicLand(card.getController(), 
							Constant.Zone.Play, true);
					
					CardList cl = CardFactoryUtil.makeToken("Eldrazi Spawn", "C 0 1 Eldrazi Spawn", card, "", new String[] {
							"Creature", "Eldrazi", "Spawn"}, 0, 1, new String[] {"Sacrifice CARDNAME: Add 1 to your mana pool."});
        			for (Card crd:cl)
        				crd.addSpellAbility(CardFactoryUtil.getEldraziSpawnAbility(crd));
				}
                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Basic");
                	return list.size() > 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        /* converted to spMakeToken keyword
        //*************** START *********** START **************************
        else if(cardName.equals("Skittering Invasion")) {
        	final SpellAbility spell = new Spell(card)
        	{
				private static final long serialVersionUID = -8303724057068847270L;

				public void resolve()
        		{
        			CardList cl;

        			for (int i=0;i<5;i++)
        			{
	        			cl = CardFactoryUtil.makeToken("Eldrazi Spawn", "C 0 1 Eldrazi Spawn", card, "", new String[] {
								"Creature", "Eldrazi", "Spawn"}, 0, 1, new String[] {"Sacrifice CARDNAME: Add 1 to your mana pool."});
	        			for (Card crd:cl)
	        				crd.addSpellAbility(CardFactoryUtil.getEldraziSpawnAbility(crd));
        			}
        		}
        	};
        	spell.setStackDescription(cardName+" - " + card.getController() + " puts one or three 0/1 Eldrazi Spawn creature tokens onto the battlefield.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        */
        
        //*************** START *********** START **************************
        else if(cardName.equals("Buried Alive")) {
        	final SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = 5676345736475L;

        		@Override
        		public void resolve() {
        			String player = card.getController();
        			if(player.equals(Constant.Player.Human)) humanResolve();
        			else computerResolve();
        		}

        		public void humanResolve() {
        			for (int i=0;i<3;i++) {
        				PlayerZone deck = AllZone.getZone(Constant.Zone.Library, card.getController());
        				CardList list = new CardList(deck.getCards());
        				list = list.getType("Creature");
        				Object check = AllZone.Display.getChoiceOptional("Select a creature card", list.toArray());
        				if(check != null) {
        					PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
        					AllZone.GameAction.moveTo(grave, (Card) check);
        				}
        				AllZone.GameAction.shuffle(Constant.Player.Human);
        			}
        		} // humanResolve
        		
        		public void computerResolve() {
        			for (int i=0;i<3;i++) {
        				Card[] library = AllZone.Computer_Library.getCards();
        				CardList list = new CardList(library);
        				list = list.getType("Creature");

        				//pick best creature
        				Card c = CardFactoryUtil.AI_getBestCreature(list);
        				if(c == null) c = library[0];
        				//System.out.println("computer picked - " +c);
        				AllZone.Computer_Library.remove(c);
        				AllZone.Computer_Graveyard.add(c);
        			}
        		} // computerResolve
        		
        		@Override
        		public boolean canPlay() {
        			PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
        			return library.getCards().length != 0 && super.canPlay();
        		}

        		@Override
        		public boolean canPlayAI() {
        			CardList creature = new CardList();
        			creature.addAll(AllZone.Computer_Library.getCards());
        			creature = creature.getType("Creature");
        			return creature.size() > 2;
        		}
        	};//SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("All is Dust")) {
        	/*
        	 * Each player sacrifices all colored permanents he or she controls.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -8228522411909468245L;

				@Override
        		public void resolve() {
        			CardList all = AllZoneUtil.getCardsInPlay();
        			all = all.filter(colorless);

        			CardListUtil.sortByIndestructible(all);
        			CardListUtil.sortByDestroyEffect(all);

        			for(Card c: all) {
        				AllZone.GameAction.sacrifice(c);
        			}
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			//same basic AI as Wrath of God, Damnation, Consume the Meek, etc.
        			CardList human = AllZoneUtil.getPlayerCardsInPlay(Constant.Player.Human);
        			human = human.filter(colorless);
        			human = human.getNotKeyword("Indestructible");
        			CardList computer = AllZoneUtil.getPlayerCardsInPlay(Constant.Player.Computer);
        			computer = computer.filter(colorless);
        			computer = computer.getNotKeyword("Indestructible");

        			Log.debug("All is Dust", "Current phase:" + AllZone.Phase.getPhase());
        			// the computer will at least destroy 2 more human permanents
        			return  AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && 
        				(computer.size() < human.size() - 1
        				|| (AllZone.Computer_Life.getLife() < 7 && !human.isEmpty()));
        		}
        		
        		private CardListFilter colorless = new CardListFilter() {
        			public boolean addCard(Card c) {
    					return !CardUtil.getColors(c).contains(Constant.Color.Colorless) && !c.getName().equals("Mana Pool") &&
    					       !c.getName().equals("Mind's Desire");
    				}
        		};
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Acidic Soil")) {
        	/*
        	 * Acidic Soil deals damage to each player equal to the number of
        	 * lands he or she controls.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8555498267738686288L;

				@Override
        		public void resolve() {
        			CardList humanLands = AllZoneUtil.getPlayerLandsInPlay(Constant.Player.Human);
        			CardList compLands = AllZoneUtil.getPlayerLandsInPlay(Constant.Player.Computer);
        			
        			AllZone.GameAction.addDamage(Constant.Player.Computer, card, compLands.size());
        			AllZone.GameAction.addDamage(Constant.Player.Human, card, humanLands.size());
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			PlayerLife compLife = AllZone.GameAction.getPlayerLife(Constant.Player.Computer);
        			PlayerLife humanLife = AllZone.GameAction.getPlayerLife(Constant.Player.Human);
        			CardList human = AllZoneUtil.getPlayerLandsInPlay(Constant.Player.Human);
        			CardList comp = AllZoneUtil.getPlayerLandsInPlay(Constant.Player.Computer);
        			
        			if(humanLife.getLife() <= human.size() ) {
        				return true;
        			}
        			
        			if( compLife.getLife() >= comp.size() && human.size() > comp.size()+2 ) {
        				return true;
        			}
        			return false;
        		}
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Explosive Revelation")) {
        	/*
        	 * Choose target creature or player. Reveal cards from the top of
        	 * your library until you reveal a nonland card. Explosive Revelation
        	 * deals damage equal to that card's converted mana cost to that
        	 * creature or player. Put the nonland card into your hand and the
        	 * rest on the bottom of your library in any order.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -3234630801871872940L;
				
				int damage = 3;
                Card check;
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.Human_Life.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.Human_Life.getLife() <= damage) {
                        setTargetPlayer(Constant.Player.Human);
                        return;
                    }
                    
                    Card c = getFlying();
                    if((c == null) || (!check.equals(c))) throw new RuntimeException(card
                            + " error in chooseTargetAI() - Card c is " + c + ",  Card check is " + check);
                    
                    setTargetCard(c);
                }//chooseTargetAI()
                
                //uses "damage" variable
                Card getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    for(int i = 0; i < flying.size(); i++)
                        if(flying.get(i).getNetDefense() <= damage) return flying.get(i);
                    
                    return null;
                }
                
                @Override
                public void resolve() {
                	
                    int damage = getDamage();
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            javax.swing.JOptionPane.showMessageDialog(null, cardName+" causes " + damage
                                    + " to " + getTargetCard());
                            
                            Card c = getTargetCard();
                            c.addDamage(damage, card);
                        }
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(null, cardName+" causes " + damage
                                + " to " + getTargetPlayer());
                        AllZone.GameAction.addDamage(getTargetPlayer(), card, damage);
                    }
                    //System.out.println("Library after: "+AllZoneUtil.getPlayerCardsInLibrary(card.getController()));
                }
                
                int getDamage() {
                	/*
                	 * Reveal cards from the top of
                	 * your library until you reveal a nonland card.
                	 */
                    CardList lib = AllZoneUtil.getPlayerCardsInLibrary(card.getController());
                    Log.debug("Explosive Revelation", "Library before: "+lib);
                    CardList revealed = new CardList();
                    if( lib.size() > 0 ) {
                    	int index = 0;
                    	Card top;
                    	do {
                    		top = lib.get(index);
                    		//System.out.println("Got from top of library:"+top);
                    		index+= 1;
                    		revealed.add(top);
                    	} while( index < lib.size() && top.isLand() );
                    	//Display the revealed cards
                    	AllZone.Display.getChoice("Revealed cards:", revealed.toArray());
                    	//non-land card into hand
                    	AllZone.GameAction.moveToHand(revealed.get(revealed.size()-1));
                    	//put the rest of the cards on the bottom of library
                    	for(int j = 0; j < revealed.size()-1; j++ ) {
                    		AllZone.GameAction.moveToBottomOfLibrary(revealed.get(j));
                    	}
                    	//return the damage
                    	
                    	//System.out.println("Explosive Revelation does "+CardUtil.getConvertedManaCost(top)+" from: "+top);
                    	return CardUtil.getConvertedManaCost(top);
                    }
                    return 0;
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(spell, true, false));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Ponder") || cardName.equals("Omen")) {
        	/* 
        	 * Look at the top three cards of your library, then put them back
        	 * in any order. You may shuffle your library.  Draw a card. 
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 484615053209732769L;
				
				@Override
        		public void resolve() {
        			String player = card.getController();
        			AllZoneUtil.rearrangeTopOfLibrary(player, 3, false);
        			AllZone.GameAction.promptForShuffle(player);
        		}
				
        		@Override
        		public boolean canPlayAI() {
        			//basically the same reason as Sensei's Diving Top
        			return false;
        		}
        	};//spell
        	spell.setStackDescription(cardName+" - Rearrange the top 3 cards in your library in any order.  You may shuffle you library.  Draw a card.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
               
        
        //*************** START *********** START **************************
        else if(cardName.equals("Index")) {
        	/* 
        	 * Look at the top five cards of your library, then put them back
        	 * in any order.
        	 */
        	final SpellAbility spell = new Spell(card) {
        		private static final long serialVersionUID = -3175286661458692699L;

        		@Override
        		public void resolve() {
        			String player = card.getController();
        			AllZoneUtil.rearrangeTopOfLibrary(player, 5, false);
        		}

        		@Override
        		public boolean canPlayAI() {
        			//basically the same reason as Sensei's Diving Top
        			return false;
        		}
        	};//spell
        	spell.setStackDescription(cardName+" - Rearrange the top 5 cards in your library in any order.  You may shuffle you library.  Draw a card.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Oust")) {
            /*
             * Put target creature into its owner's library second from the
             * top. Its controller gains 3 life.
             */
            final SpellAbility spell = new Spell(card){
                private static final long serialVersionUID = 4305992990847699048L;

                @Override
                public void resolve() {
                    if (AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                    
                        Card tgt = getTargetCard();
                        if (null != tgt) {
                            if (tgt.isToken()) {
                                AllZone.GameAction.removeFromGame(tgt);
                            } else {
                                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, tgt.getOwner());
                                PlayerZone play = AllZone.getZone(Constant.Zone.Play, tgt.getController());
                                play.remove(tgt);
                                if (lib.size() > 0) {
                                    lib.add(tgt, 1); //add second from top if lib not empty
                                } else {
                                    lib.add(tgt); //add to top if lib is empty
                                }
                            }//else
                            AllZone.GameAction.gainLife(tgt.getController(), 3);
                        }
                    }//if isCardInPlay() && canTarget()
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return getHumanCreatures().size() != 0;
                }//canPlayAI()
                
                @Override
                public void chooseTargetAI() {
                    Card best = CardFactoryUtil.AI_getBestCreature(getHumanCreatures());
                    setTargetCard(best);
                }//chooseTargetAI()
                
                CardList getHumanCreatures() {
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() 
                                    && c.getNetAttack() > 2 
                                    && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    return list;
                }//getHumanCreature()
            };//SpellAbility
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
         
        
        //*************** START *********** START **************************
        else if(cardName.equals("Fireball")) {
        	/*
        	 * Fireball deals X damage divided evenly, rounded down, among
        	 * any number of target creatures and/or players.
        	 * Fireball costs 1 more to cast for each target beyond the first.
        	 */
        	//no reason this should never be enough targets
        	final Card[] target = new Card[100];
        	final int[] index = new int[1];
        	//it can target up to two players also
        	final String[] targetPlayers = new String[2];
        	final int[] index2 = new int[1];

        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -6293612568525319357L;

				@Override
        		public boolean canPlayAI() {
					final int maxX = ComputerUtil.getAvailableMana().size() - 1;
					int humanLife = AllZone.Human_Life.getLife();
					if(maxX >= humanLife) {
						index2[0] = 0;
						targetPlayers[index2[0]] = Constant.Player.Human;
						//index2[0] = 1;
						return true;
					}
        			return false;
        		}

        		@Override
        		public void resolve() {
        			int damage = (card.getXManaCostPaid() - getNumTargets() + 1) / getNumTargets();
        			//add that much damage to each creature
        			//DEBUG
        			Log.debug("Fireball", "Fireball - damage to each target: "+damage);
        			Log.debug("Fireball", "Fireball - card targets: ");
        			printCardTargets();
        			Log.debug("Fireball", "Fireball - player targets: ");
        			printPlayerTargets();
        			if(card.getController().equals(Constant.Player.Computer)) {
        				StringBuilder sb = new StringBuilder();
        				sb.append(cardName+" - Computer causes "+damage+" to:\n\n");
        				for(int i = 0; i < target.length; i++) {
            				if(AllZone.GameAction.isCardInPlay(target[i])
            						&& CardFactoryUtil.canTarget(card, target[i])
            						&& null != target[i]) {
            					sb.append(target[i]+"\n");
            				}
            			}
            			for(int i = 0; i < targetPlayers.length; i++) {
            				if( null != targetPlayers[i] ) {
            					sb.append(targetPlayers[i]+"\n");
            				}
            			}
        				javax.swing.JOptionPane.showMessageDialog(null, sb.toString());
        			}
        			for(int i = 0; i < target.length; i++) {
        				if(AllZone.GameAction.isCardInPlay(target[i])
        						&& CardFactoryUtil.canTarget(card, target[i])
        						&& null != target[i]) {
        					//DEBUG
        					Log.debug("Fireball", "Fireball does "+damage+" to: "+target[i]);
        					AllZone.GameAction.addDamage(target[i], card, damage);
        				}
        			}
        			for(int i = 0; i < targetPlayers.length; i++) {
        				if( null != targetPlayers[i] ) {
        					//DEBUG
        					Log.debug("Fireball", "Fireball does "+damage+" to: "+targetPlayers[i]);
        					AllZone.GameAction.addDamage(targetPlayers[i], card, damage);
        				}
        			}
        		}//resolve()
        		
        		//DEBUG
        		private void printCardTargets() {
        			StringBuilder sb = new StringBuilder("[");
        			for(int i = 0; i < target.length; i++) {
        				sb.append(target[i]).append(",");
        			}
        			sb.append("]");
        			Log.debug("Fireball", sb.toString());
        		}
        		//DEBUG
        		private void printPlayerTargets() {
        			StringBuilder sb = new StringBuilder("[");
        			for(int i = 0; i < targetPlayers.length; i++) {
        				sb.append(targetPlayers[i]).append(",");
        			}
        			sb.append("]");
        			Log.debug("Fireball", sb.toString());
        		}
        		
        		private int getNumTargets() {
        			int numTargets = 0;
        			for( int j = 0; j < target.length; j++ ) {
        				if( null != target[j] ) {
        					numTargets++;
        				}
        			}
        			for( int k = 0; k < targetPlayers.length; k++ ) {
        				if( null != targetPlayers[k] ) {
        					numTargets++;
        				}
        			}
        			return numTargets;
        		}
        		
        	};//SpellAbility

        	final Input input = new Input() {
				private static final long serialVersionUID = 1099272655273322957L;

				@Override
        		public void showMessage() {
        			AllZone.Display.showMessage("Select target creatures and/or players.  Currently, "+getNumTargets()+" targets.  Click OK when done.");
        		}
				
				private int getNumTargets() {
        			int numTargets = 0;
        			for( int j = 0; j < target.length; j++ ) {
        				if( null != target[j] ) {
        					numTargets++;
        				}
        			}
        			for( int k = 0; k < targetPlayers.length; k++ ) {
        				if( null != targetPlayers[k] ) {
        					numTargets++;
        				}
        			}
        			//DEBUG
        			Log.debug("Fireball", "Fireball - numTargets = "+numTargets);
        			return numTargets;
        		}

        		@Override
        		public void selectButtonCancel() { stop(); }
        		
        		@Override
        		public void selectButtonOK() {
        			if(this.isFree()) {
						this.setFree(false);
						AllZone.Stack.add(spell);
						stop();
					} else stopSetNext(new Input_PayManaCost(spell));
        		}

        		@Override
        		public void selectCard(Card c, PlayerZone zone) {
        			if( !CardFactoryUtil.canTarget(card, c)) {
        				AllZone.Display.showMessage("Cannot target this card.");
    					return; //cannot target
        			}
        			for(int i = 0; i < index[0]; i++) {
        				if(c.equals(target[i])) {
        					AllZone.Display.showMessage("You have already selected this target.");
        					return; //cannot target the same creature twice.
        				}
        			}

        			if(c.isCreature() && zone.is(Constant.Zone.Play)) {
        				target[index[0]] = c;
        				index[0]++;
        				showMessage();

        				/*if(index[0] == target.length) {
        					if(this.isFree()) {
        						this.setFree(false);
        						AllZone.Stack.add(spell);
        						stop();
        					} else stopSetNext(new Input_PayManaCost(spell));
        				}*/
        			}
        		}//selectCard()
        		
        		@Override
                public void selectPlayer(String player) {
        			for(int i = 0; i < index2[0]; i++) {
        				if(player.equals(targetPlayers[i])) {
        					AllZone.Display.showMessage("You have already selected this player.");
        					return; //cannot target the same player twice.
        				}
        			}
                    //spell.setTargetPlayer(player);
                    targetPlayers[index2[0]] = player;
                    index2[0]++;
                    showMessage();
                }
        	};//Input

        	Input runtime = new Input() {
        		private static final long serialVersionUID = 3522833806455511494L;

        		@Override
        		public void showMessage() {
        			index[0] = 0;
        			index2[0] = 0;
        			stopSetNext(input);
        		}
        	};//Input

        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        	spell.setBeforePayMana(runtime);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Energy Tap")) {
        	/*
        	 * Tap target untapped creature you control. If you do, add X to
        	 * your mana pool, where X is that creature's converted mana cost.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8883585452278041848L;

				@Override
        		public void resolve() {
        			Card target = getTargetCard();
        			if(null != target && target.isUntapped()) {
        				int cost = CardUtil.getConvertedManaCost(target);
        				Card mp = AllZone.ManaPool;
        				mp.addExtrinsicKeyword("ManaPool:"+cost);
        				target.tap();
        			}
        		}

        		@Override
        		public boolean canPlayAI() {
        			return false;
        		}
        	};
        	Input runtime = new Input() {
				private static final long serialVersionUID = -757364902159389697L;

				@Override
                public void showMessage() {
                    CardList choices = AllZoneUtil.getCreaturesInPlay(card.getController());
                	choices = choices.filter(AllZoneUtil.untapped);
                    stopSetNext(CardFactoryUtil.input_targetSpecific(spell, choices,
                    		"Select target untapped creature", true, false));
                }//showMessage()
            };//Input
        	spell.setBeforePayMana(runtime);
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        /* converted to spMakeToken keyword
        //*************** START *********** START **************************
        else if(cardName.equals("Sound the Call")) {
            SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -2359398136467055521L;

				@Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Wolf", "G 1 1 Wolf", card, "G", new String[] {"Creature", "Wolf"},
                            1, 1, new String[] {"This creature gets +1/+1 for each card named Sound the Call in each graveyard."});
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        */
        
        //*************** START *********** START **************************
        else if(cardName.equals("Reanimate")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 2940635076296411568L;

                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    int cmc = CardUtil.getConvertedManaCost(c.getManaCost());
                    PlayerZone grave = AllZone.getZone(c);
                    
                    if(AllZone.GameAction.isCardInZone(c, grave) && c.isCreature()) {
                        PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                        AllZone.GameAction.moveTo(play, c);
                        c.setController(card.getController());
                    }
                    AllZone.GameAction.getPlayerLife(c.getController()).subtractLife(cmc,card);
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && getCreatures().size() > 0;
                }
                
                public CardList getCreatures() {
                    CardList creatures = AllZoneUtil.getCardsInGraveyard();
                    creatures = creatures.getType("Creature");
                    if (card.getController().equals(Constant.Player.Computer)) {
                        creatures = creatures.getNotKeyword("At the beginning of the end step, sacrifice CARDNAME.");
                    }
                    return creatures;
                }
                
                @Override
                public void chooseTargetAI() {
                    setTargetCard(CardFactoryUtil.AI_getBestCreature(getCreatures()));
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            Input target = new Input() {
                private static final long serialVersionUID = -5293899159488141547L;

                @Override
                public void showMessage() {
                    Object check = AllZone.Display.getChoiceOptional("Select creature", getCreatures());
                    if(check != null) {
                        spell.setTargetCard((Card) check);
                        stopSetNext(new Input_PayManaCost(spell));
                    } else stop();
                }//showMessage()
                
                public Card[] getCreatures() {
                    CardList creatures = AllZoneUtil.getCardsInGraveyard();
                    return creatures.filter(AllZoneUtil.creatures).toArray();
                }
            };//Input
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Recall")) {
        	/*
        	 * Discard X cards, then return a card from your graveyard to your
        	 * hand for each card discarded this way. Exile Recall.
        	 */
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -3935814273439962834L;

				@Override
        		public boolean canPlayAI() {
        			//for compy to play this wisely, it should check hand, and if there
        			//are no spells that canPlayAI(), then use recall.  maybe.
        			return false;
        		}

        		@Override
        		public void resolve() {       			
        			int numCards = card.getXManaCostPaid();
        			final String player = card.getController();
        			int maxCards = AllZoneUtil.getPlayerHand(player).size();
        			if(numCards != 0) {
        				numCards = Math.min(numCards, maxCards);
        			if(player.equals(Constant.Player.Human)) {
        				AllZone.InputControl.setInput(CardFactoryUtil.input_discardRecall(numCards, card, this));
        			}
           			}
        			/*else { //computer
        				AllZone.GameAction.discardRandom(Constant.Player.Computer, numCards);
        				AllZone.GameAction.removeFromGame(card);
        				CardList grave = AllZoneUtil.getPlayerGraveyard(card.getController());
        				for(int i = 1; i <= numCards; i ++) {
        					Card t1 = CardFactoryUtil.AI_getBestCreature(grave);
        					if(null != t1) {
        						t1 = grave.get(0);
        						grave.remove(t1);
        						AllZone.GameAction.moveToHand(t1);
        					}
        				}
        			}*/
        		}//resolve()
        	};//SpellAbility

        	spell.setStackDescription(card.getName()+" - discard X cards and return X cards to your hand.");
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Windfall")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7707012960887790709L;
            
                @Override
                public boolean canPlayAI() {
                    /*
                     *  We want compy to have less cards in hand than the human
                     */
                    CardList Hhand = new CardList(AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human).getCards());
                    CardList Chand = new CardList(AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer).getCards());
                    return Chand.size() < Hhand.size();
                }
                
            @Override
            public void resolve() {
                discardDraw(Constant.Player.Human);
                discardDraw(Constant.Player.Computer);
            }//resolve()
            
            void discardDraw(String player) {
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                CardList Hhand = new CardList(AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human).getCards());
                CardList Chand = new CardList(AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer).getCards());
                int draw;
                if(Hhand.size() >= Chand.size()) {
                    draw = Hhand.size();
                } else {
                    draw = Chand.size();
                }
                Card[] c = hand.getCards();
                for(int i = 0; i < c.length; i++)
                    AllZone.GameAction.discard(c[i], null);
            
                for(int i = 0; i < draw; i++)
                    AllZone.GameAction.drawCard(player);
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Stitch Together")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -57996914115026814L;

                @Override
                public void resolve() {
                    CardList threshold = new CardList();
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    threshold.addAll(grave.getCards());
                    Card c = getTargetCard();
                    
                    if(threshold.size() >= 7) {
                        if(AllZone.GameAction.isCardInZone(c, grave)) {
                            PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
                            AllZone.GameAction.moveTo(play, c);
                        }
                    }
                    
                    else {
                        if(AllZone.GameAction.isCardInZone(c, grave)) {
                            PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                            AllZone.GameAction.moveTo(hand, c); 
                        }
                    }
                }//resolve()

                @Override
                public boolean canPlay() {
                    return getCreatures().length != 0;
                }
                
                public boolean canPlayAI() {
                    CardList check = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    check.addAll(zone.getCards());
                    return getCreaturesAI().length != 0 || check.size() >= 7;
                }
                
                public Card[] getCreatures() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature");
                    return creature.toArray();
                }
                
                public Card[] getCreaturesAI() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature");
                    creature = creature.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getNetAttack() > 4;
                        }
                    });
                    return creature.toArray();
                }
                
                @Override
                public void chooseTargetAI() {
                    Card c[] = getCreatures();
                    Card biggest = c[0];
                    for(int i = 0; i < c.length; i++)
                        if(biggest.getNetAttack() < c[i].getNetAttack()) biggest = c[i];

                    setTargetCard(biggest);
                }
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);

            Input target = new Input() {
                private static final long serialVersionUID = -3717723884199321767L;

                @Override
                public void showMessage() {
                    Object check = AllZone.Display.getChoiceOptional("Select creature", getCreatures());
                    if(check != null) {
                        spell.setTargetCard((Card) check);
                        stopSetNext(new Input_PayManaCost(spell));
                    } else stop();
                }//showMessage()

                public Card[] getCreatures() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Creature");
                    return creature.toArray();
                }
            };//Input
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        if(cardName.equals("Life from the Loam")) {
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = 9071771496065272936L;

				@Override
                public void resolve() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());

                    CardList cards = new CardList(grave.getCards());
                    CardList lands = new CardList();
                    
                    for(int i = 0; i < cards.size(); i++) {
                        if(cards.get(i).getType().contains("Land")) {
                        	lands.add(cards.get(i));
                        }
                    }
                    
                    String controller = card.getController();
                    
                    if(lands.size() == 0) return;
                    
                    if(controller.equals(Constant.Player.Human)) {
                        Object o = AllZone.Display.getChoiceOptional("Select First Land", lands.toArray());
                        if(o != null) {
                            //ability.setTargetCard((Card)o);
                            //AllZone.Stack.add(ability);
                            Card c1 = (Card) o;
                            grave.remove(c1);
                            hand.add(c1);
                            lands.remove(c1);
                            
                            if(lands.size() == 0) return;
                            
                            o = AllZone.Display.getChoiceOptional("Select Second Land", lands.toArray());
                            
                            if(o != null) {
                                Card c2 = (Card) o;
                                grave.remove(c2);
                                hand.add(c2);
                                lands.remove(c2);
                                
                                if(lands.size() == 0) return;
                                
                                o = AllZone.Display.getChoiceOptional("Select Third Land", lands.toArray());
                                
                                if(o != null) {
                                    Card c3 = (Card) o;
                                    grave.remove(c3);
                                    hand.add(c3);
                                    lands.remove(c3);
                                }
                            }
                        }
                        AllZone.GameAction.shuffle(controller);
                    } else //computer
                    {
                        lands.shuffle();
                        if(lands.size() >= 1) {
                            Card c1 = lands.getCard(0);
                            grave.remove(c1);
                            hand.add(c1);
                            lands.remove(c1);
                            
                            if(lands.size() >= 1) {
                                Card c2 = lands.getCard(0);
                                grave.remove(c2);
                                hand.add(c2);
                                lands.remove(c2);
                                
                                if(lands.size() >= 1) {
                                    Card c3 = lands.getCard(0);
                                    grave.remove(c3);
                                    hand.add(c3);
                                    lands.remove(c3);
                                    
                                }
                            }
                            
                        }
                        //ability.setTargetCard(powerTwoCreatures.get(0));
                        //AllZone.Stack.add(ability);
                        AllZone.GameAction.shuffle(controller);
                    }
                    

                    //...
                    
                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Natural Order")){
            final SpellAbility spell = new Spell(card) {

				private static final long serialVersionUID = -6598323179507468746L;

				@Override
                public void resolve() {
					String controller = card.getController();
					
					PlayerZone battlezone = AllZone.getZone(Constant.Zone.Play, controller);
			        PlayerZone library = AllZone.getZone(Constant.Zone.Library, controller);
			        
					CardList list = new CardList(library.getCards());
			        list = list.getType("Creature").getColor("G");
			        
			        if(list.size() == 0) return;
			        
					if(controller.equals(Constant.Player.Human)) {
				        
				        Card c = AllZone.Display.getChoiceOptional("Choose a green creature", list.toArray());
				        if(c != null) {
				            list.remove(c);
				            library.remove(c);
				            battlezone.add(c);
				            
				        }//if
			        } else {
			        	CardList greenlist = new CardList(library.getCards());
			        	greenlist = greenlist.getType("Creature").getColor("G");
			        	Card c = CardFactoryUtil.AI_getBestCreature(greenlist);
			        	if(c != null) {
				            list.remove(c);
				            library.remove(c);
				            battlezone.add(c);
				            
				        }//if
			        }
                } // resolve
               
        		public void chooseTargetAI() {
        			Card target = null;
        			target = CardFactoryUtil.AI_getWorstCreature(new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer).getCards()));
        			setTargetCard(target);
        			AllZone.GameAction.sacrifice(getTargetCard());
        		}//chooseTargetAI()

                
                public boolean canPlayAI()
                {
                	PlayerZone library = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
                	CardList list = new CardList(library.getCards());
                	list = list.getType("Creature").getColor("G");
                	PlayerZone inPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                	CardList listInPlay = new CardList(inPlay.getCards());
                	listInPlay = listInPlay.getType("Creature").getColor("G");
                	Card inPlayCreature = CardFactoryUtil.AI_getWorstCreature(new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer).getCards()));
                	Card inLibraryCreature = CardFactoryUtil.AI_getBestCreature(new CardList(AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer).getCards()));
                	return (list.size() > 0) && (listInPlay.size() > 0) && (inPlayCreature.getNetAttack() < inLibraryCreature.getNetAttack());
                }
            };//SpellAbility
            Input runtime = new Input() {
                
				private static final long serialVersionUID = -7551607354431165941L;

				@Override
                public void showMessage() {
                    String player = card.getController();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                    CardList choice = new CardList(play.getCards());
                    choice = choice.getType("Creature").getColor("G");   
                    
                    boolean free = false;
                    if (this.isFree())
                    	free = true;
                    
                    if (player.equals(Constant.Player.Human)) {
                    	stopSetNext(CardFactoryUtil.input_sacrifice(spell, choice, "Select a green creature to sacrifice.", free));
                    }
                }
            };

            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);

        } //*************** END ************ END **************************
        

        //*************** START ********** START *************************
        if (cardName.equals("Destructive Force") || cardName.equals("Wildfire"))
        {
        	SpellAbility spDFWf = new Spell(card)
        	{
        		private static final long serialVersionUID = 976372017492853L;
        		
        		private int getNum()
        		{
        			if (cardName.equals("Wildfire"))
        				return 4;
        			else if (cardName.equals("Destructive Force"))
        				return 5;
        			
        			return 0;
        		}
        		
        		public boolean canPlayAI()
        		{
        			int number = getNum();
        			
        			CardList cPlay = new CardList(AllZone.Computer_Play.getCards());
        			CardList cLands = cPlay.getType("Land");
        			CardList cCreatures = cPlay.getType("Creature");
        			
        			CardList hPlay = new CardList(AllZone.Human_Play.getCards());
        			CardList hLands = hPlay.getType("Land");
        			CardList hCreatures = hPlay.getType("Creature");
        			
        			if (hLands.size() < number)
        				return false;
        			
        			if (cLands.size() < (number + 2))
        				return false;
        			
        			int nCDie = 0;
        			for (int i=0; i<cCreatures.size(); i++)
        			{
        				if ((cCreatures.get(i).getNetDefense() - number) < 1)
        					nCDie++;
        			}
        			
        			int nHDie = 0;
        			for (int i=0; i<hCreatures.size(); i++)
        			{
        				if ((hCreatures.get(i).getNetDefense() - number) < 1)
        					nHDie++;
        			}
        			
        			if (nCDie > nHDie)
        				return false;
        			
        			return true;
        		}
        		
        		public void resolve()
        		{
        			int number = getNum();

        			String actPlayer = card.getController();
        			String oppPlayer = AllZone.GameAction.getOpponent(actPlayer);
        			
        			CardList aPlay = new CardList(AllZone.getZone(Constant.Zone.Play, actPlayer).getCards());
        			CardList aLands = null;
        			CardList aCreatures = aPlay.getType("Creature");
        			
        			CardList oPlay = new CardList(AllZone.getZone(Constant.Zone.Play, oppPlayer).getCards());
        			CardList oLands = null;
        			CardList oCreatures = oPlay.getType("Creature");

        			for (int i=0; i<number; i++)
        			{
        				aPlay = new CardList(AllZone.getZone(Constant.Zone.Play, actPlayer).getCards());
        				aLands = aPlay.getType("Land");

        				AllZone.GameAction.sacrificePermanent(actPlayer, "Select a Land to sacrifice", aLands);
        			}
        			
        			for (int i=0; i<number; i++)
        			{
        				oPlay = new CardList(AllZone.getZone(Constant.Zone.Play, oppPlayer).getCards());
        				oLands = oPlay.getType("Land");
        				
        				AllZone.GameAction.sacrificePermanent(oppPlayer, "Select a land to sacrifice", oLands);
        			}
        				
        			for (int i=0; i<aCreatures.size(); i++)
        				AllZone.GameAction.addDamage(aCreatures.get(i), card, number);
        			
        			for (int i=0; i<oCreatures.size(); i++)
        				AllZone.GameAction.addDamage(oCreatures.get(i), card, number);
        			
        			return;
        		}
        	};
        	
        	card.clearSpellAbility();
        	spDFWf.setStackDescription(cardName + " - Each player sacrifices lands and damage is dealt to each creature");
        	card.addSpellAbility(spDFWf);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Patriarch's Bidding")) {
            final String[] input = new String[2];
            
            // final SpellAbility ability = new Ability(card, "0") {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -2182173662547136798L;

                @Override
                public void resolve() {

                        input[0] = JOptionPane.showInputDialog(null, "Which creature type?", "Pick type",
                                JOptionPane.QUESTION_MESSAGE);
                        
                        if(!CardUtil.isCreatureType(input[0])) input[0] = "";
                        //TODO: some more input validation, case-sensitivity, etc.
                        
                        input[0] = input[0].trim(); //this is to prevent "cheating", and selecting multiple creature types,eg "Goblin Soldier"

                        PlayerZone aiGrave = AllZone.getZone(Constant.Zone.Graveyard, Constant.Player.Computer);
                        HashMap<String,Integer> countInGraveyard = new HashMap<String,Integer>();
                        CardList allGrave = new CardList(aiGrave.getCards());
                        allGrave.getType("Creature");
                        for(Card c:allGrave)
                        {
                            for(String type:c.getType())
                            {
                                if(CardUtil.isCreatureType(type))
                                {
                                    if(countInGraveyard.containsKey(type))
                                    {
                                        countInGraveyard.put(type, countInGraveyard.get(type)+1);
                                    }
                                    else
                                    {
                                        countInGraveyard.put(type, 1);
                                    }
                                }
                            }
                        }
                        String maxKey = "";
                        int maxCount = -1;
                        for(String type:countInGraveyard.keySet())
                        {
                            if(countInGraveyard.get(type) > maxCount)
                            {
                                maxKey = type;
                                maxCount = countInGraveyard.get(type);
                            }
                        }
                        if(!maxKey.equals("")) input[1] = maxKey;
                        else input[1] = "Sliver";

                        //Actually put everything  on the battlefield 
                        PlayerZone humanGrave = AllZone.getZone(Constant.Zone.Graveyard,Constant.Player.Human);
                        PlayerZone humanBattlefield = AllZone.getZone(Constant.Zone.Play,Constant.Player.Human);
                        for(Card c:humanGrave.getCards())
                        {
                            if(c.isType(input[0]))
                            {
                                humanGrave.remove(c);
                                humanBattlefield.add(c);
                            }
                        }
                        PlayerZone computerGrave = AllZone.getZone(Constant.Zone.Graveyard,Constant.Player.Computer);
                        PlayerZone computerBattlefield = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                        for(Card c:computerGrave.getCards())
                        {
                            if(c.isType(input[1]))
                            {
                                computerGrave.remove(c);
                                computerBattlefield.add(c);
                            }
                        }
                }//resolve()
            };//SpellAbility
/* No longer needed.
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 5634360316643996274L;
                
                public void execute() {
                    ability.setStackDescription("When " + card.getName()
                            + " comes into play, choose a creature type.");
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
*/
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - choose a creature type.");
            spell.setStackDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Ashes to Ashes")) {
            final Card[] target = new Card[2];
            final int[] index = new int[1];
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6509598408022853029L;
                
                @Override
                public boolean canPlayAI() {
                    return 2 <= getNonArtifact().size() && 5 < AllZone.Computer_Life.getLife();
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList human = getNonArtifact();
                    CardListUtil.sortAttack(human);
                    
                    target[0] = human.get(0);
                    target[1] = human.get(1);
                }
                
                CardList getNonArtifact() {
                    CardList list = CardFactoryUtil.AI_getHumanCreature(card, true);
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isArtifact();
                        }
                    });
                    return list;
                }//getNonArtifact()
                
                @Override
                public void resolve() {
                    for(int i = 0; i < target.length; i++) {
                        Card c = target[i];
                        if (AllZone.GameAction.isCardInPlay(c))
                        	AllZone.GameAction.removeFromGame(c);
                    }
                    
                    PlayerLife life = AllZone.GameAction.getPlayerLife(card.getController());
                    life.subtractLife(5,card);
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = -4114782677700487264L;
                
                @Override
                public void showMessage() {
                    if(index[0] == 0) AllZone.Display.showMessage("Select 1st target non-artifact creature to remove from the game");
                    else AllZone.Display.showMessage("Select 2nd target non-artifact creature to remove from the game");
                    
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(!c.isArtifact() && c.isCreature() && zone.is(Constant.Zone.Play)) {
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            if(this.isFree()) {
                                this.setFree(false);
                                AllZone.Stack.add(spell);
                                stop();
                            } else stopSetNext(new Input_PayManaCost(spell));
                        }
                    }
                }//selectCard()
            };//Input
            
            Input runtime = new Input() {
                private static final long serialVersionUID = -3162536306318797516L;
                
                @Override
                public void showMessage() {
                    index[0] = 0;
                    stopSetNext(input);
                }
            };//Input
            
            card.setSVar("PlayMain1", "TRUE");
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(runtime);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Take Possession")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7359291736123492910L;
                
                @Override
                public boolean canPlayAI() {
                    return 0 < CardFactoryUtil.AI_getHumanCreature(card, true).size();
                }
                
                @Override
                public void chooseTargetAI() {
                    Card best = CardFactoryUtil.AI_getBestCreature(CardFactoryUtil.AI_getHumanCreature(card, true));
                    setTargetCard(best);
                }
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    c.setController(card.getController());
                    
                    ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(false);
                    ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(false);
                    
                    PlayerZone from = AllZone.getZone(c);
                    PlayerZone to = AllZone.getZone(Constant.Zone.Play, card.getController());
                    
                    from.remove(c);
                    to.add(c);
                    
                    ((PlayerZone_ComesIntoPlay) AllZone.Human_Play).setTriggers(true);
                    ((PlayerZone_ComesIntoPlay) AllZone.Computer_Play).setTriggers(true);
                    
                }//resolve()
            };
            
            card.clearSpellAbility();
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "All"));
            card.addSpellAbility(spell);
            
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Leeches")) {
        	/*
        	 * Target player loses all poison counters.
        	 * Leeches deals that much damage to that player.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8555498267738686288L;

				@Override
        		public void resolve() {
        			int counters = AllZone.GameAction.getPoison(getTargetPlayer());
        			AllZone.GameAction.addDamage(getTargetPlayer(), card, counters);
        			AllZone.GameAction.addPoison(getTargetPlayer(), -counters);
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			PlayerLife compLife = AllZone.GameAction.getPlayerLife(Constant.Player.Computer);
        			PlayerLife humanLife = AllZone.GameAction.getPlayerLife(Constant.Player.Human);
        			int humanPoison = AllZone.GameAction.getPoison(Constant.Player.Human);
        			int compPoison = AllZone.GameAction.getPoison(Constant.Player.Computer);
        			
        			if(humanLife.getLife() <= humanPoison ) {
        				setTargetPlayer(Constant.Player.Human);
        				return true;
        			}
        			
        			if( (2*(11 - compPoison) < compLife.getLife() || compPoison > 7) && compPoison < compLife.getLife() - 2) {
        				setTargetPlayer(Constant.Player.Computer);
        				return true;
        			}
        			
        			return false;
        		}
        	};// SpellAbility
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cerebral Eruption")) {
        	/*
        	 * Target opponent reveals the top card of his or her library.
        	 * Cerebral Eruption deals damage equal to the revealed card's
        	 * converted mana cost to that player and each creature he or
        	 * she controls. If a land card is revealed this way, return
        	 * Cerebral Eruption to its owner's hand.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -1365692178841929046L;

				@Override
				public void resolve() {
					String player = card.getController();
					String opponent = AllZone.GameAction.getOpponent(player);
					CardList lib = AllZoneUtil.getPlayerCardsInLibrary(opponent);
					if(lib.size() > 0) {
						final Card topCard = lib.get(0);
						int damage = CardUtil.getConvertedManaCost(topCard);
						
						AllZone.Display.getChoiceOptional(card+" - Revealed card", new Card[] {topCard});

						//deal damage to player
						AllZone.GameAction.addDamage(opponent, card, damage);

						//deal damage to all opponent's creatures
						CardList creatures = AllZoneUtil.getCreaturesInPlay(opponent);
						for(Card creature:creatures) {
							AllZone.GameAction.addDamage(creature, card, damage);
						}

						card.addReplaceMoveToGraveyardCommand(new Command() {
							private static final long serialVersionUID = -5912663572746146726L;

							public void execute() {
								if(null != topCard && topCard.isLand()) {
									AllZone.GameAction.moveToHand(card);
								}
								else AllZone.GameAction.moveToGraveyard(card);
							}
						});
					}
				}// resolve()
				
				@Override
				public boolean canPlayAI() {
					return AllZoneUtil.getPlayerCardsInLibrary(Constant.Player.Human).size() > 0;
				}
				
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("False Mourning") || cardName.equals("Salvage")) {
            final SpellAbility spell = new Spell(card) {
                
				private static final long serialVersionUID = -6183756156784063761L;

				@Override
                public void resolve() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    if(AllZone.GameAction.isCardInZone(getTargetCard(), graveyard)) {
                        graveyard.remove(getTargetCard());
                        AllZone.GameAction.moveToTopOfLibrary(getTargetCard());
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    return graveyard.getCards().length != 0 && super.canPlay();
                }
            };
            Input runtime = new Input() {
				private static final long serialVersionUID = -1438178075940689742L;

				@Override
                public void showMessage() {
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    Object o = AllZone.Display.getChoiceOptional("Select target card", graveyard.getCards());
                    if(o == null) stop();
                    else {
                    	String location = "top of owner's library";
                    	
                        spell.setStackDescription("Return " + o + " to " + location);
                        spell.setTargetCard((Card) o);
                        if(this.isFree()) 
                        {
                        	this.setFree(false);
                        	stop();
                        	AllZone.Stack.add(spell);
                    	} 
                        else
                        	stopSetNext(new Input_PayManaCost(spell));
                    }
                }//showMessage()
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("All", AllZone.Computer_Graveyard));
            spell.setBeforePayMana(runtime);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      //*************** START *********** START **************************
        else if(cardName.equals("Sanity Grinding")) {
        	/*
        	 * Chroma � Reveal the top ten cards of your library. For each blue
        	 * mana symbol in the mana costs of the revealed cards, target opponent
        	 * puts the top card of his or her library into his or her graveyard.
        	 * Then put the cards you revealed this way on the bottom of your
        	 * library in any order.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 4475834103787262421L;

				@Override
				public void resolve() {
					String player = card.getController();
					String opp = AllZone.GameAction.getOpponent(player);
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
					int maxCards = lib.size();
					maxCards = Math.min(maxCards, 10);
					if(maxCards == 0) return;
					CardList topCards =   new CardList();
					//show top n cards:
					for(int j = 0; j < maxCards; j++ ) {
						topCards.add(lib.get(j));
					}
					final int num = CardFactoryUtil.getNumberOfManaSymbolsByColor("U", topCards);
					AllZone.Display.getChoiceOptional("Revealed cards - "+num+" U mana symbols", topCards.toArray());
					
					//opponent moves this many cards to graveyard
					PlayerZone oppLib = AllZone.getZone(Constant.Zone.Library, opp);
					for(int i = 0; i < num; i++) {
						AllZone.GameAction.moveToGraveyard(oppLib.get(i));
					}
					
					//then, move revealed cards to bottom of library
					for(Card c:topCards) {
						AllZone.GameAction.moveToBottomOfLibrary(c);
					}
				}// resolve()
				
				@Override
				public boolean canPlayAI() {
					return AllZoneUtil.getPlayerCardsInLibrary(Constant.Player.Computer).size() > 0;
				}
				
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Last Stand")) {
        	/*
        	 * Target opponent loses 2 life for each Swamp you control.
        	 * Last Stand deals damage equal to the number of Mountains
        	 * you control to target creature.
        	 * Put a 1/1 green Saproling creature token onto the battlefield
        	 * for each Forest you control.
        	 * You gain 2 life for each Plains you control.
        	 * Draw a card for each Island you control, then discard that many cards.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 4475834103787262421L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                	String player = card.getController();
                	String opp = AllZone.GameAction.getOpponent(player);
                    int numSwamps = AllZoneUtil.getPlayerTypeInPlay(player, "Swamp").size();
                    int numMountains = AllZoneUtil.getPlayerTypeInPlay(player, "Mountain").size();
                    int numForests = AllZoneUtil.getPlayerTypeInPlay(player, "Forest").size();
                    int numPlains = AllZoneUtil.getPlayerTypeInPlay(player, "Plains").size();
                    int numIslands = AllZoneUtil.getPlayerTypeInPlay(player, "Island").size();
                    
                    //swamps
                    AllZone.GameAction.loseLife(opp, 2*numSwamps);
                    
                    //mountain
                    AllZone.GameAction.addDamage(getTargetCard(), card, numMountains);
                    
                    //forest
                    for(int i = 0; i < numForests; i++)
                    	CardFactoryUtil.makeTokenSaproling(player);
                    
                    //plains
                    AllZone.GameAction.gainLife(player, 2*numPlains);
                    
                    //islands
                    int max = Math.min(numIslands, AllZoneUtil.getPlayerCardsInLibrary(player).size());
                    if(max > 0) {
                    	AllZone.GameAction.drawCards(player, max);
                    	if(player.equals(Constant.Player.Human)) {
                    		AllZone.InputControl.setInput(CardFactoryUtil.input_discard(max, this));
                    	}
                    	else {
                    		AllZone.GameAction.discardRandom(Constant.Player.Computer, max, this);
                    	}
                    }
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        // -1 means keyword "Cycling" not found
        if(hasKeyword(card, "Cycling") != -1) {
            int n = hasKeyword(card, "Cycling");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String manacost = k[1];
                
                card.addSpellAbility(CardFactoryUtil.ability_cycle(card, manacost));
            }
        }//Cycling

        while(hasKeyword(card, "TypeCycling") != -1) {
            int n = hasKeyword(card, "TypeCycling");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String type = k[1];
                final String manacost = k[2];
                
                card.addSpellAbility(CardFactoryUtil.ability_typecycle(card, manacost, type));
            }
        }//TypeCycling
        
        if(hasKeyword(card, "Transmute") != -1) {
            int n = hasKeyword(card, "Transmute");
            if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                card.removeIntrinsicKeyword(parse);
                
                String k[] = parse.split(":");
                final String manacost = k[1];
                
                card.addSpellAbility(CardFactoryUtil.ability_transmute(card, manacost));
            }
        }//transmute
            
      
        if (card.getManaCost().contains("X"))
        {
        	SpellAbility sa = card.getSpellAbility()[0];
    		sa.setIsXCost(true);
    		
        	if (card.getManaCost().startsWith("X X"))
        		sa.setXManaCost("2");
        	else if (card.getManaCost().startsWith("X"))
        		sa.setXManaCost("1");
        }//X
          

        
    	return card;
    }//getCard
}
