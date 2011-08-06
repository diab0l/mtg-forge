
package forge.card.cardFactory;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.esotericsoftware.minlog.Log;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.ButtonUtil;
import forge.Card;
import forge.CardList;
import forge.CardListFilter;
import forge.CardListUtil;
import forge.CardUtil;
import forge.Combat;
import forge.Command;
import forge.CommandReturn;
import forge.ComputerUtil;
import forge.Constant;
import forge.Counters;
import forge.GameActionUtil;
import forge.MyRandom;
import forge.Player;
import forge.PlayerZone;
import forge.PlayerZone_ComesIntoPlay;
import forge.Phase;
import forge.card.spellability.Ability;
import forge.card.spellability.Ability_Activated;
import forge.card.spellability.Ability_Mana;
import forge.card.spellability.Ability_Static;
import forge.card.spellability.Ability_Sub;
import forge.card.spellability.Cost;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Spell_Permanent;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.gui.GuiUtils;
import forge.gui.input.Input;
import forge.gui.input.Input_PayManaCost;
import forge.gui.input.Input_PayManaCost_Ability;

public class CardFactory_Creatures {
    
    private static final int hasKeyword(Card c, String k) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith(k)) return i;
        
        return -1;
    }
    
    public static int shouldCycle(Card c) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith("Cycling")) return i;
        
        return -1;
    }
    
    public static int shouldTypeCycle(Card c) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith("TypeCycling")) return i;
        
        return -1;
    }
    
    public static int shouldTransmute(Card c) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith("Transmute")) return i;
        
        return -1;
    }
    
    public static int shouldSoulshift(Card c) {
        ArrayList<String> a = c.getKeyword();
        for(int i = 0; i < a.size(); i++)
            if(a.get(i).toString().startsWith("Soulshift")) return i;
        
        return -1;
    }
    

    public static Card getCard(final Card card, final String cardName, Player owner, CardFactory cf) {
        
        //*************** START *********** START **************************
        if(cardName.equals("Lurking Informant")) {
            Target target = new Target(card,"Select target player", new String[] {"Player"});
        	Cost abCost = new Cost("2 T", cardName, true);
            final SpellAbility a1 = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = 1446529067071763245L;
                
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    if(player == null) player = AllZone.HumanPlayer;
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    Card c = lib.get(0);
                    
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                    	if(GameActionUtil.showYesNoDialog(card, "Mill "+c.getName()+"?")) {
                            AllZone.GameAction.moveToGraveyard(c);
                    	}
                    } else {
                        CardList landlist = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
                        //AI will just land starve or land feed human.  Perhaps this could use overall card ranking
                        if(landlist.size() >= 5 && !c.getType().contains("Land")) {
                        	AllZone.GameAction.moveToGraveyard(c);
                        }
                        else if(landlist.size() < 5 && c.getType().contains("Land")) {
                        	AllZone.GameAction.moveToGraveyard(c);
                        }
                        else if(lib.size() <= 5) {
                        	AllZone.GameAction.moveToGraveyard(c);
                        }
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList libList = AllZoneUtil.getPlayerCardsInLibrary(AllZone.HumanPlayer);
                    return libList.size() > 0;
                }
            };//SpellAbility
            card.addSpellAbility(a1);
            a1.setDescription(abCost+"Look at the top card of target player's library. You may put that card into that player's graveyard.");
            a1.setStackDescription(cardName+" - Look at the top card of target player's library. You may put that card into that player's graveyard.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Force of Savagery")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = 1603238129819160467L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                    
                    return list.containsName("Glorious Anthem") || list.containsName("Gaea's Anthem");
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }
        //*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Caller of the Claw")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int stop = countGraveyard();
                    for(int i = 0; i < stop; i++)
                        makeToken();
                }//resolve()
                
                int countGraveyard() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    CardList list = new CardList(grave.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && (c.getTurnInZone() == AllZone.Phase.getTurn());
                        }
                    });
                    return list.size();
                }//countGraveyard()
                
                void makeToken() {
                    CardFactoryUtil.makeToken("Bear", "G 2 2 Bear", card.getController(), "G", new String[] {"Creature", "Bear"},
                            2, 2, new String[] {""});
                }//makeToken()
            };//SpellAbility
            
            Command comesIntoPlay = new Command() {
                private static final long serialVersionUID = 8485080996453793968L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };//Command
            ability.setStackDescription("Caller of the Claw - Put a 2/2 green Bear creature token onto the battlefield for each nontoken creature put into your graveyard from play this turn.");
            card.addComesIntoPlayCommand(comesIntoPlay);
            
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = 6946020026681536710L;
                
                @Override
                public boolean canPlayAI() {
                    return super.canPlay();
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Kiki-Jiki, Mirror Breaker")) {
            final CardFactory cfact = cf;
            Cost abCost = new Cost("T", cardName, true);
            Target target = new Target(card,"Select target nonlegendary creature you control.", new String[] {"Creature.nonLegendary+YouCtrl"});
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = -943706942500499644L;
                
                @Override
                public boolean canPlayAI() {
                    return getCreature().size() != 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    setTargetCard(getCreature().get(0));
                }
                
                CardList getCreature() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(card.getController());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (!c.getType().contains("Legendary"));
                        }
                    });
                    CardListUtil.sortAttack(list);
                    return list;
                }//getCreature()
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && getTargetCard().getController().equals(card.getController())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        
                    	 int multiplier = AllZoneUtil.getDoublingSeasonMagnitude(card.getController());
                         Card[] crds = new Card[multiplier];
                         
                         for (int i=0;i<multiplier;i++)
                         {
	                        //TODO: Use central copy methods
	                        Card copy;
	                        if(!getTargetCard().isToken()) {
	                            //CardFactory cf = new CardFactory("cards.txt");
	                            
	
	                            //copy creature and put it onto the battlefield
	                            //copy = getCard(getTargetCard(), getTargetCard().getName(), card.getController());
	                            copy = cfact.getCard(getTargetCard().getName(), getTargetCard().getOwner());
	                            
	                            //when copying something stolen:
	                            copy.setController(getTargetCard().getController());
	                            
	                            copy.setToken(true);
	                            copy.setCopiedToken(true);
	                            
	                            copy.addIntrinsicKeyword("Haste");
	                        } else //isToken()
	                        {
	                            Card c = getTargetCard();
	                            
	                            copy = CardFactory.copyStats(c);
	                            
	                            copy.setName(c.getName());
	                            copy.setImageName(c.getImageName());
	                            
	                            copy.setOwner(c.getController());
	                            copy.setController(c.getController());
	                            
	                            copy.setManaCost(c.getManaCost());
	                            copy.setToken(true);
	                            
	                            copy.setType(c.getType());
	                            
	                            copy.setBaseAttack(c.getBaseAttack());
	                            copy.setBaseDefense(c.getBaseDefense());
	                            copy.addIntrinsicKeyword("Haste");
	                        }
	                        
	                        //Slight hack in case Kiki copies a creature with triggers.
                            for(Trigger t : copy.getTriggers())
                            {
                            	AllZone.TriggerHandler.registerTrigger(t);
                            }
	                        
	                        copy.setCurSetCode(getTargetCard().getCurSetCode());
	                        copy.setImageFilename(getTargetCard().getImageFilename());
	                        
                            if(getTargetCard().isFaceDown()) {
                                copy.setIsFaceDown(true);
                                copy.setManaCost("");
                                copy.setBaseAttack(2);
                                copy.setBaseDefense(2);
                                copy.setIntrinsicKeyword(new ArrayList<String>()); //remove all keywords
                                copy.setType(new ArrayList<String>()); //remove all types
                                copy.addType("Creature");
                                copy.clearSpellAbility(); //disallow "morph_up"
                                copy.setCurSetCode("");
                                copy.setImageFilename("morph.jpg");
                            }

	                        AllZone.GameAction.moveToPlay(copy);
	                        crds[i] = copy;
                    	}
                        

                        //have to do this since getTargetCard() might change
                        //if Kiki-Jiki somehow gets untapped again
                        final Card[] target = new Card[multiplier];
                        for (int i=0;i<multiplier;i++) {
                        	final int index = i;
	                        target[i] = crds[i];
	                        Command atEOT = new Command() {
	                            private static final long serialVersionUID = 7803915905490565557L;
	                            
	                            public void execute() {
	                                //technically your opponent could steal the token
	                                //and the token shouldn't be sacrificed
	                                if(AllZone.GameAction.isCardInPlay(target[index]))
	                                {
	                                	//Slight hack in case kiki copies a creature with triggers
	                                	AllZone.TriggerHandler.removeAllFromCard(target[index]);
	                                	AllZone.GameAction.sacrifice(target[index]); //maybe do a setSacrificeAtEOT, but probably not.
	                                }
	                            }
	                        };//Command
	                        AllZone.EndOfTurn.addAt(atEOT);
                        }
                    }//is card in play?
                }//resolve()
            };//SpellAbility
            
            ability.setStackDescription("Kiki-Jiki - copy card.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(abCost);
            sb.append("Put a token that's a copy of target nonlegendary creature you control onto the battlefield. ");
            sb.append("That token has haste. Sacrifice it at the beginning of the next end step.");
            ability.setDescription(sb.toString());
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Loxodon Hierarch")) {
        	Cost abCost = new Cost("G W Sac<1/CARDNAME>", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = 6606519504236074186L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    final Card[] c = AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards();
                    
                    for(int i = 0; i < c.length; i++)
                        if(c[i].isCreature()) c[i].addShield();
                    
                    AllZone.EndOfTurn.addUntil(new Command() {
                        private static final long serialVersionUID = 5853778391858472471L;
                        
                        public void execute() {
                            for(int i = 0; i < c.length; i++)
                                c[i].resetShield();
                        }
                    });
                }//resolve()
            };//SpellAbility
            
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Regenerate each creature you control.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" regenerate each of ").append(card.getController()).append("'s creatures.");
            ability.setStackDescription(sb.toString());
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Gilder Bairn")) {
        	Cost abCost = new Cost("2 GU Untap", cardName, true);
        	Target tgt = new Target(card,"Select target permanent.", new String[]{"Permanent"});
            final Ability_Activated a1 = new Ability_Activated(card, abCost, tgt) {
				private static final long serialVersionUID = -1847685865277129366L;

				@Override
                public void resolve() {
                    Card c = getTargetCard();
                    
                    if(c.sumAllCounters() == 0) return;
                    else if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        //zerker clean up:
                        for(Counters c_1:Counters.values())
                            if(c.getCounters(c_1) > 0) c.addCounter(c_1, c.getCounters(c_1));
                    }
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList perms = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    perms = perms.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.sumAllCounters() > 0 && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    perms.shuffle();
                    setTargetCard(perms.get(0)); //TODO: improve this.
                }
                
                @Override
                public boolean canPlayAI() {
                	CardList perms = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    perms = perms.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.sumAllCounters() > 0 && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    return perms.size() > 0;
                }
            };//SpellAbility
            
            card.addSpellAbility(a1);
            a1.setDescription(abCost+"For each counter on target permanent, put another of those counters on that permanent.");
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Rukh Egg")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Bird", "R 4 4 Bird", card.getController(), "R", new String[] {"Creature", "Bird"},
                            4, 4, new String[] {"Flying"});
                }
            }; //ability
            
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" - Put a 4/4 red Bird creature token with flying onto the battlefield.");
            ability.setStackDescription(sb.toString());
            
            final Command createBird = new Command() {
                private static final long serialVersionUID = 2856638426932227407L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };
            
            final Command destroy = new Command() {
                private static final long serialVersionUID = 2320128493809478823L;
                
                public void execute() {
                    AllZone.EndOfTurn.addAt(createBird);
                }
            };
            
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Primal Plasma") || cardName.equals("Primal Clay")) {
        	card.setBaseAttack(3);
        	card.setBaseDefense(3);
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    String choice = "";
                    String choices[] = {"3/3", "2/2 with flying", "1/6 with defender"};
                    
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        choice = GuiUtils.getChoice("Choose one", choices);
                    } else choice = choices[MyRandom.random.nextInt(3)];
                    
                    if(choice.equals("2/2 with flying")) {
                        card.setBaseAttack(2);
                        card.setBaseDefense(2);
                        card.addIntrinsicKeyword("Flying");
                    }
                    if(choice.equals("1/6 with defender")) {
                        card.setBaseAttack(1);
                        card.setBaseDefense(6);
                        card.addIntrinsicKeyword("Defender");
                        card.addType("Wall");
                    }
                    
                }//resolve()
            };//SpellAbility
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 8957338395786245312L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - choose: 3/3, 2/2 flying, 1/6 defender");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Brawn")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {}
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -3009968608543593584L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - ").append(card.getOwner()).append(" creatures have Trample.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Filth")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {}
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -3009968608543593584L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - ").append(card.getOwner());
                	sb.append(" creatures have Swampwalk.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
                

       //*************** START *********** START **************************
        else if(cardName.equals("Glory")) {
            final Ability ability = new Ability(card, "2 W") {
				private static final long serialVersionUID = -79984345642451L;
				
				@Override
                public boolean canPlayAI() {
                    return getAttacker() != null;
                }
				
				public Card getAttacker() {
                    // target creatures that is going to attack
                    Combat c = ComputerUtil.getAttackers();
                    Card[] att = c.getAttackers();

                    // Effect best used on at least a couple creatures
                    if (att.length > 1) {
                        return att[0];
                    } else return null;
                }//getAttacker()
				
				String getKeywordBoost() {
					String theColor = getChosenColor();
					return "Protection from " + theColor;
                }//getKeywordBoost()
				
				String getChosenColor() {
					// Choose color for protection in Brave the Elements
					String color = "";
					if (card.getController().equals(AllZone.HumanPlayer)) {

						String[] colors = Constant.Color.Colors;
						colors[colors.length-1] = null;

						Object o = GuiUtils.getChoice("Choose color", colors);
						color = (String)o;
					}
					else {
						PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.HumanPlayer);
						PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
						CardList list = new CardList();
						list.addAll(lib.getCards());
						list.addAll(hand.getCards());

						if (list.size() > 0) {  
							String mpcolor = CardFactoryUtil.getMostProminentColor(list);
							if (!mpcolor.equals(""))
								color = mpcolor;
							else
								color = "black";
						}
						else  {
							color = "black";
						}
					}
					return color;
				} // getChosenColor
				
				@Override
				public void resolve() {
					final String kboost = getKeywordBoost();
					
					CardList list = new CardList();
					PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    list.addAll(play.getCards());
                    
                    for (int i = 0; i < list.size(); i++) {
                        final Card[] target = new Card[1];
                        target[0] = list.get(i);
                        
                        final Command untilEOT = new Command() {
							private static final long serialVersionUID = 6308754740309909072L;

							public void execute() {
                                if (AllZone.GameAction.isCardInPlay(target[0])) {
                                	target[0].removeExtrinsicKeyword(kboost);
                                }
                            }
                        };//Command
                        
                        if (AllZone.GameAction.isCardInPlay(target[0]) && 
                        		!target[0].getKeyword().contains(kboost)) {
                            target[0].addExtrinsicKeyword(kboost);
                            
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }//if
                    }//for
				}//resolve
                
                @Override
                public boolean canPlay() {
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    
                    return AllZone.GameAction.isCardInZone(card, grave);                   
                }
            };//Ability

            card.addSpellAbility(ability);
            ability.setFlashBackAbility(true);
            card.setUnearth(true);
            
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append("2 W: Creatures you control gain protection from the color of your choice ");
            sbDesc.append("until end of turn. Activate this ability only if Glory is in your graveyard.");
            ability.setDescription(sbDesc.toString());
            
            StringBuilder sbStack = new StringBuilder();
            sbStack.append(card.getName()).append(" - Creatures ").append(card.getController());
            sbStack.append(" controls gain protection from the color of his/her choice until end of turn");
            ability.setStackDescription(sbStack.toString());
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Anger")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {}
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = 1707519783018941582L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - ").append(card.getOwner());
                	sb.append(" creatures have Haste.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Valor")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {}
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -3009968608543593584L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - ").append(card.getOwner());
                	sb.append(" creatures have First Strike.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Wonder")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {}
            };
            
            Command destroy = new Command() {
                private static final long serialVersionUID = 340877499423908818L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - ").append(card.getOwner());
                	sb.append(" creatures have Flying.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Oracle of Mul Daya")) {
            final SpellAbility ability = new Ability(card, "0") {
                private static final long serialVersionUID = 2902408812353813L;
                
                @Override
                public void resolve() {
				// todo: change to static ability?
                	CardList library = AllZoneUtil.getPlayerCardsInLibrary(card.getController());
                	if(library.size() == 0)
                		return;
                    
                    Card top = library.get(0);
                    if(top.isLand()) 
                    	card.getController().playLand(top);
                }//resolve()
                
                @Override
                public boolean canPlay() { 
                    CardList library = new CardList(AllZone.getZone(Constant.Zone.Library, card.getController()).getCards());
                    if(library.size() == 0) return false;
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());                                      
                    boolean canPlayLand = card.getController().canPlayLand();
                        
                    return (AllZone.GameAction.isCardInZone(card, play) && library.get(0).isLand() && canPlayLand);
                }
            };//SpellAbility
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getController()).append(" - plays land from top of library.");
            ability.setStackDescription(sb.toString());
            card.addSpellAbility(ability);           
        }//*************** END ************ END **************************


        //*************** START *********** START **************************
        else if(cardName.equals("Drekavac")) {
            final Input discard = new Input() {
                private static final long serialVersionUID = -6392468000100283596L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a noncreature card to discard");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(zone.is(Constant.Zone.Hand) && !c.isCreature()) {
                        c.getController().discard(c, null);
                        stop();
                    }
                }
                
                @Override
                public void selectButtonCancel() {
                    AllZone.GameAction.sacrifice(card);
                    stop();
                }
            };//Input
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        if(AllZone.Human_Hand.getCards().length == 0) AllZone.GameAction.sacrifice(card);
                        else AllZone.InputControl.setInput(discard);
                    } else {
                        CardList list = new CardList(AllZone.Computer_Hand.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return (!c.isCreature());
                            }
                        });
                        list.get(0).getController().discard(list.get(0), this);
                    }//else
                }//resolve()
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 9202753910259054021L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" sacrifices Drekavac unless he discards a noncreature card");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -2940969025405788931L;
                
                //could never get the AI to work correctly
                //it always played the same card 2 or 3 times
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    CardList list = new CardList(hand.getCards());
                    list.remove(card);
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (!c.isCreature());
                        }
                    });
                    return list.size() != 0;
                }//canPlay()
            };
            card.addComesIntoPlayCommand(intoPlay);
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Minotaur Explorer") || cardName.equals("Balduvian Horde") ||
        		cardName.equals("Pillaging Horde")) {

        	final SpellAbility creature = new Spell_Permanent(card){
				private static final long serialVersionUID = -7326018877172328480L;

				@Override
				public boolean canPlayAI(){
        			int reqHand = 1;
        			if (AllZone.getZone(card).is(Constant.Zone.Hand))
        				reqHand++;
        			
        			// Don't play if it would sacrifice as soon as it comes into play
        			return AllZoneUtil.getCardsInZone(Constant.Zone.Hand, AllZone.ComputerPlayer).size() > reqHand;
        		}
        	};
        	card.clearFirstSpellAbility();
        	card.addFirstSpellAbility(creature);
        	
            final SpellAbility ability = new Ability(card, "0") {
            	
                @Override
                public void resolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    if(hand.getCards().length == 0) 
                    	AllZone.GameAction.sacrifice(card);
                    else 
                    	card.getController().discardRandom(this);
                }
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 4986114285467649619L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" - discards at random or sacrifices ").append(cardName);
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sylvan Messenger") 
        		|| cardName.equals("Enlistment Officer") 
        		|| cardName.equals("Tidal Courier")
        		|| cardName.equals("Goblin Ringleader")
        		|| cardName.equals("Grave Defiler")) {
        	
        	final String[] typeToGet = {""};
            if(card.getName().equals("Sylvan Messenger"))
            {
            	typeToGet[0] = "Elf";
            }
            else if(card.getName().equals("Enlistment Officer"))
            {
            	typeToGet[0] = "Soldier";
            }
            else if(card.getName().equals("Tidal Courier"))
            {
            	typeToGet[0] = "Merfolk";
            }
            else if(card.getName().equals("Grave Defiler"))
            {
            	typeToGet[0] = "Zombie";
            }
            else if(card.getName().equals("Goblin Ringleader"))
            {
            	typeToGet[0] = "Goblin";
            }
        	
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    PlayerZone libraryZone = AllZone.getZone(Constant.Zone.Library, card.getController());

                    //get top 4 cards of the library
                    CardList top = new CardList();
                    Card[] library = libraryZone.getCards();
                    for(int i = 0; i < 4 && i < library.length; i++)
                        top.add(library[i]);
                    
                    if (card.getController().equals(AllZone.ComputerPlayer))
                    {
                    	StringBuilder sb = new StringBuilder();
                    	sb.append("<html><b>");
                    	for (Card c:top) {
                    		sb.append(c.getName());
                    		sb.append("<br>");
                    	}
                    	sb.append("</b></html>");
                    	JOptionPane.showMessageDialog(null, sb.toString(), "Computer reveals:", JOptionPane.INFORMATION_MESSAGE); 
                    }
                    
                    CardList typeLimitedTop = top.getType(typeToGet[0]);
                    
                    for(Card c : typeLimitedTop){
                    	AllZone.GameAction.moveToHand(c);
                    	top.remove(c);
                    }
                    
                    for(Card c : top){
                    	AllZone.GameAction.moveToBottomOfLibrary(c);
                    }

                }//resolve()
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 4757054648163014149L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" - reveal the top four cards of your library. Put all ").append(typeToGet[0]);
            sb.append(" cards revealed this way into your hand and the rest on the bottom of your library.");
            ability.setStackDescription(sb.toString());
            
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sleeper Agent")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                	// Todo: this need to be targeted
                	AllZone.GameAction.changeController(new CardList(card), card.getController(), card.getController().getOpponent());
                }
            };
            
            ability.setStackDescription("When Sleeper Agent enters the battlefield, target opponent gains control of it.");
            Command intoPlay = new Command() {
                private static final long serialVersionUID = -3934471871041458847L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                    
                }//execute()
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Phylactery Lich") ) {
            final CommandReturn getArt = new CommandReturn() {
                //get target card, may be null
                public Object execute() {
                    CardList art = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    art = art.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact();
                        }
                    });
                    
                    CardList list = new CardList(art.toArray());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getIntrinsicKeyword().contains("Indestructible");
                        }
                    });

                    Card target = null;
                    if(!list.isEmpty())
                    	target = list.get(0);
                    else if (!art.isEmpty())
                    	target = art.get(0);

                    return target;
                }//execute()
            };//CommandReturn
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    
                    if(AllZone.GameAction.isCardInPlay(c) && c.isArtifact()) {
                    	c.addCounter(Counters.PHYLACTERY, 1);
                    	card.setFinishedEnteringBF(true);
                    }
                }//resolve()
            };//SpellAbility
            Command intoPlay = new Command() {
				private static final long serialVersionUID = -1601957445498569156L;

				public void execute() {
                    Input target = new Input() {

						private static final long serialVersionUID = -806140334868210520L;

						@Override
                        public void showMessage() {
                            AllZone.Display.showMessage("Select target artifact you control");
                            ButtonUtil.disableAll();
                        }
                        
                        @Override
                        public void selectCard(Card card, PlayerZone zone) {
                            if(card.isArtifact() && zone.is(Constant.Zone.Battlefield) && card.getController().equals(AllZone.HumanPlayer)) {
                                ability.setTargetCard(card);
                                AllZone.Stack.add(ability);
                                stop();
                            }
                        }
                    };//Input target
                    

                    if(card.getController().equals(AllZone.HumanPlayer)) {
                    	CardList artifacts = AllZoneUtil.getPlayerTypeInPlay(AllZone.HumanPlayer, "Artifact");
                        
                        if(artifacts.size() != 0) AllZone.InputControl.setInput(target);

                    } 
                    else{ //computer
                        Object o = getArt.execute();
                        if(o != null)//should never happen, but just in case
                        {
                            ability.setTargetCard((Card) o);
                            AllZone.Stack.add(ability);
                        }
                    }//else
                }//execute()
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(new Spell_Permanent(card) {
                
				private static final long serialVersionUID = -1506199222879057809L;

				@Override
                public boolean canPlayAI() {
                    Object o = getArt.execute();
                    return (o != null) && AllZone.getZone(getSourceCard()).is(Constant.Zone.Hand);
                }
            });          
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Peregrine Drake")) {
            final Input untap = new Input() {
                private static final long serialVersionUID = 2287264826189281795L;
                
                int                       stop             = 5;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a land to untap");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(card.isLand() && zone.is(Constant.Zone.Battlefield)) {
                        card.untap();
                        count++;
                        if(count == stop) stop();
                    }
                }//selectCard()
            };
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(untap);
                    else {
                        CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return c.isLand() && c.isTapped();
                            }
                        });
                        for(int i = 0; i < 5 && i < list.size(); i++)
                            list.get(i).untap();
                    }//else
                }//resolve()
            };
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 3208277692165539396L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" untaps up to 5 lands.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Great Whale")) {
            final Input untap = new Input() {
                private static final long serialVersionUID = -2167059018040912025L;
                
                int                       stop             = 7;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a land to untap");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(card.isLand() && zone.is(Constant.Zone.Battlefield)) {
                        card.untap();
                        count++;
                        if(count == stop) stop();
                    }
                }//selectCard()
            };
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(untap);
                    else {
                        CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return c.isLand() && c.isTapped();
                            }
                        });
                        for(int i = 0; i < 7 && i < list.size(); i++) {
                            list.get(i).untap();
                        }
                    }//else
                }//resolve()
            };
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 7222997838266323277L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" untaps up to 7 lands.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END ***************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Palinchron")) {
            final Input untap = new Input() {
                private static final long serialVersionUID = -2167159918040912025L;
                
                int                       stop             = 7;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a land to untap");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(card.isLand() && zone.is(Constant.Zone.Battlefield)) {
                        card.untap();
                        count++;
                        if(count == stop) stop();
                    }
                }//selectCard()
            };
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(untap);
                    else {
                        CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return c.isLand() && c.isTapped();
                            }
                        });
                        for(int i = 0; i < 7 && i < list.size(); i++) {
                            list.get(i).untap();
                        }
                    }//else
                }//resolve()
            };
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 7222997848166323277L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" untaps up to 7 lands.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
            
            final SpellAbility a1 = new Ability(card, "2 U U") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    AllZone.GameAction.moveToHand(card);
                }
            };//a1
            
            card.addSpellAbility(a1);
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getController()).append(" returns Palinchron back to its owner's hand.");
            a1.setStackDescription(sb.toString());
            
            a1.setDescription("2 U U: Return Palinchron to its owner's hand.");
        }//*************** END ************ END ***************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cloud of Faeries")) {
            final Input untap = new Input() {
                private static final long serialVersionUID = -2167059918040912025L;
                
                int                       stop             = 2;
                int                       count            = 0;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select a land to untap");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(card.isLand() && zone.is(Constant.Zone.Battlefield)) {
                        card.untap();
                        count++;
                        if(count == stop) stop();
                    }
                }//selectCard()
            };
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(untap);
                    else {
                        CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return c.isLand() && c.isTapped();
                            }
                        });
                        for(int i = 0; i < 2 && i < list.size(); i++) {
                            list.get(i).untap();
                        }
                    }//else
                }//resolve()
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 7222997838166323277L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController()).append(" untaps up to 2 lands.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END ***************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Whirlpool Rider")) {
            final SpellAbility ability = new Ability(card, "0") {
            	
                @Override
                public void resolve() {
                    //shuffle hand into library, then shuffle library
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
                    Card c[] = hand.getCards();
                    for(int i = 0; i < c.length; i++)
                        AllZone.GameAction.moveTo(library, c[i]);
                    card.getController().shuffle();
                    
                    //draw same number of cards as before
                    for(int i = 0; i < c.length; i++)
                    	card.getController().drawCard();
                }
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = 6290392806910817877L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController());
                	sb.append(" shuffles the cards from his hand into his library, then draws that many cards.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Sky Swallower")) {
            final SpellAbility ability = new Ability(card, "0") {
            	
                @Override
                public void resolve() {
                	Player opp = card.getController().getOpponent();
                    
                    CardList list = AllZoneUtil.getCardsInPlay();
                    list = list.getValidCards("Card.Other+YouCtrl".split(","),card.getController(), card);

                    AllZone.GameAction.changeController(list, card.getController(), opp);
                }//resolve()
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = -453410206437839334L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getController().getOpponent());
                	sb.append(" gains control of all other permanents you control");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************


        //*************** START *********** START **************************
        else if(cardName.equals("Lightning Crafter")) {
            final CommandReturn getCreature = new CommandReturn() {
                public Object execute() {
                    //get all creatures
                    CardList list = new CardList();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    list.addAll(play.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getType().contains("Goblin") || 
                            c.getType().contains("Shaman") || c.getKeyword().contains("Changeling");
                        }
                    });
                    
                    return list;
                }
            };//CommandReturn
            
            final SpellAbility abilityComes = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(getTargetCard() == null || getTargetCard() == card) AllZone.GameAction.sacrifice(card);
                    
                    else if(AllZone.GameAction.isCardInPlay(getTargetCard())) {
                        AllZone.GameAction.exile(getTargetCard());
                    }
                }//resolve()
            };
            
            final Input inputComes = new Input() {
                private static final long serialVersionUID = -6066115143834426784L;
                
                @Override
                public void showMessage() {
                    CardList choice = (CardList) getCreature.execute();
                    
                    stopSetNext(CardFactoryUtil.input_targetChampionSac(card, abilityComes, choice,
                            "Select Goblin or Shaman to exile", false, false));
                    ButtonUtil.disableAll();
                }
                
            };
            Command commandComes = new Command() {
                private static final long serialVersionUID = -3498068347359658023L;
                
                public void execute() {
                    CardList creature = (CardList) getCreature.execute();
                    Player s = card.getController();
                    if(creature.size() == 0) {
                        AllZone.GameAction.sacrifice(card);
                        return;
                    } else if(s.equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(inputComes);
                    else //computer
                    {
                        Card target;
                        //must target computer creature
                        CardList computer = new CardList(AllZone.Computer_Battlefield.getCards());
                        computer = computer.getType("Goblin");
                        computer.remove(card);
                        
                        computer.shuffle();
                        if(computer.size() != 0) {
                            target = computer.get(0);
                            abilityComes.setTargetCard(target);
                            AllZone.Stack.add(abilityComes);
                        }
                        else
                        	AllZone.GameAction.sacrifice(card);
                    }//else
                }//execute()
            };//CommandComes
            Command commandLeavesPlay = new Command() {
                private static final long serialVersionUID = 4236503599117025393L;
                
                public void execute() {
                    //System.out.println(abilityComes.getTargetCard().getName());
                    Object o = abilityComes.getTargetCard();
                    
                    if(o == null || ((Card) o).isToken() || !AllZone.GameAction.isCardExiled((Card) o)) return;
                    
                    SpellAbility ability = new Ability(card, "0") {
                        @Override
                        public void resolve() {
                            //copy card to reset card attributes like attack and defense
                            Card c = abilityComes.getTargetCard();
                            if(!c.isToken()) {
                                c = AllZone.CardFactory.copyCard(c);
                                c.setController(c.getOwner());

                                AllZone.GameAction.moveToPlay(c);
                            }
                        }//resolve()
                    };//SpellAbility
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(card.getName()).append(" - returning creature to the battlefield");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }//execute()
            };//Command
            
            card.addComesIntoPlayCommand(commandComes);
            card.addLeavesPlayCommand(commandLeavesPlay);
            
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(new Spell_Permanent(card) {
                private static final long serialVersionUID = -62128538115338896L;
                
                @Override
                public boolean canPlayAI() {
                    Object o = getCreature.execute();
                    if(o == null) return false;
                    
                    CardList cl = (CardList) getCreature.execute();
                    return (o != null) && cl.size() > 0 && AllZone.getZone(getSourceCard()).is(Constant.Zone.Hand);
                }
            });
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Ember-Fist Zubera")) {
            //counts Zubera in all graveyards for this turn
            final CommandReturn countZubera = new CommandReturn() {
                public Object execute() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Graveyard.getCards());
                    list.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getTurnInZone() == AllZone.Phase.getTurn())
                                    && (c.getType().contains("Zubera") || c.getKeyword().contains("Changeling"));
                        }
                    });//CardListFilter()
                    
                    return Integer.valueOf(list.size());
                }
            };
            
            final Input[] input = new Input[1];
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    //human chooses target on resolve,
                    //computer chooses target in Command destroy
                    if(AllZone.HumanPlayer.equals(card.getController())) AllZone.InputControl.setInput(input[0]);
                    else {
                        int damage = ((Integer) countZubera.execute()).intValue();
                        
                        if(getTargetCard() != null) {
                            if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                    && CardFactoryUtil.canTarget(card, getTargetCard())) {
                                Card c = getTargetCard();
                                c.addDamage(damage, card);
                            }
                        } else getTargetPlayer().addDamage(damage, card);
                    }
                }//resolve()
            };//SpellAbility
            
            input[0] = new Input() {
                private static final long serialVersionUID = 1899925898843297992L;
                
                @Override
                public void showMessage() {
                    int damage = ((Integer) countZubera.execute()).intValue();
                    AllZone.Display.showMessage("Select target Creature, Planeswalker or Player - " + damage
                            + " damage ");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if((card.isCreature() || card.isPlaneswalker()) && zone.is(Constant.Zone.Battlefield)) {
                        int damage = ((Integer) countZubera.execute()).intValue();
                        card.addDamage(damage, card);
                        
                        //have to do this since state effects aren't checked
                        //after this "Input" class is done
                        //basically this makes everything work right
                        //Ember-Fist Zubera can destroy a 2/2 creature
                        AllZone.GameAction.checkStateEffects();
                        stop();
                    }
                }//selectCard()
                
                @Override
                public void selectPlayer(Player player) {
                    int damage = ((Integer) countZubera.execute()).intValue();
                    player.addDamage(damage, card);
                    stop();
                }//selectPlayer()
            };//Input
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -1889425992069348304L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card).append(" causes damage to creature or player");
                	ability.setStackDescription(sb.toString());
                    
                    //@SuppressWarnings("unused") // damage
                    //int damage = ((Integer)countZubera.execute()).intValue();
                    
                    Player con = card.getController();
                    
                    //human chooses target on resolve,
                    //computer chooses target in Command destroy
                    if(con.equals(AllZone.ComputerPlayer)) ability.setTargetPlayer(AllZone.HumanPlayer);
                    
                    AllZone.Stack.add(ability);
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Ashen-Skin Zubera")) {
            //counts Zubera in all graveyards for this turn
            final CommandReturn countZubera = new CommandReturn() {
                public Object execute() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Graveyard.getCards());
                    list.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getTurnInZone() == AllZone.Phase.getTurn())
                                    && (c.getType().contains("Zubera") || c.getKeyword().contains("Changeling"));
                        }
                    });//CardListFilter()
                    return Integer.valueOf(list.size());
                }
            };//CommandReturn
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int discard = ((Integer) countZubera.execute()).intValue();
                    getTargetPlayer().discard(discard, this, false);
                }//resolve()
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -7494691537986218546L;
                
                public void execute() {
                    Player opponent = card.getController().getOpponent();
                    ability.setTargetPlayer(opponent);
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(card).append(" - ").append(opponent).append(" discards cards");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Floating-Dream Zubera")) {
            //counts Zubera in all graveyards for this turn
            final CommandReturn countZubera = new CommandReturn() {
                public Object execute() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Graveyard.getCards());
                    list.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getTurnInZone() == AllZone.Phase.getTurn())
                                    && (c.getType().contains("Zubera") || c.getKeyword().contains("Changeling"));
                        }
                    });//CardListFilter()
                    return Integer.valueOf(list.size());
                }
            };//CommandReturn
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int number = ((Integer) countZubera.execute()).intValue();
                    
                    for(int i = 0; i < number; i++)
                        getTargetPlayer().drawCard();
                }//resolve()
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -5814070329854975419L;
                
                public void execute() {
                    ability.setTargetPlayer(card.getController());
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(card).append(" - ").append(card.getController()).append(" draws cards");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                    
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Silent-Chant Zubera")) {
            //counts Zubera in all graveyards for this turn
            final CommandReturn countZubera = new CommandReturn() {
                public Object execute() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Graveyard.getCards());
                    list.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getTurnInZone() == AllZone.Phase.getTurn())
                                    && (c.getType().contains("Zubera") || c.getKeyword().contains("Changeling"));
                        }
                    });//CardListFilter()
                    return Integer.valueOf(list.size());
                }
            };//CommandReturn
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int number = ((Integer) countZubera.execute()).intValue();
                    
                    getTargetPlayer().gainLife(number*2, card);
                }//resolve()
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -2327085948421343657L;
                
                public void execute() {
                    ability.setTargetPlayer(card.getController());
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(card).append(" - ").append(card.getController()).append(" gains life");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                    
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Dripping-Tongue Zubera")) {
            //counts Zubera in all graveyards for this turn
            final CommandReturn countZubera = new CommandReturn() {
                public Object execute() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Graveyard.getCards());
                    list.addAll(AllZone.Computer_Graveyard.getCards());
                    
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return (c.getTurnInZone() == AllZone.Phase.getTurn())
                                    && (c.getType().contains("Zubera") || c.getKeyword().contains("Changeling"));
                        }
                    });//CardListFilter()
                    return Integer.valueOf(list.size());
                }
            };//CommandReturn
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int count = ((Integer) countZubera.execute()).intValue();
                    for(int i = 0; i < count; i++)
                        CardFactoryUtil.makeToken("Spirit", "C 1 1 Spirit", card.getController(), "", new String[] {
                                "Creature", "Spirit"}, 1, 1, new String[] {""});
                }//resolve()
            };//SpellAbility
            
            Command destroy = new Command() {
                private static final long serialVersionUID = 8362692868619919330L;
                
                public void execute() {
                    ability.setTargetPlayer(card.getController());
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append(card).append(" - ").append(card.getController()).append(" puts tokens onto the battlefield");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Jhoira of the Ghitu")) {
            final Stack<Card> chosen= new Stack<Card>();
            final SpellAbility ability = new Ability(card, "2") {
                private static final long serialVersionUID = 4414609319033894302L;
                @Override
                public boolean canPlay() {
                    CardList possible = new CardList(AllZone.getZone(Constant.Zone.Hand, card.getController()).getCards());
                    possible.filter(new CardListFilter(){
                    	public boolean addCard(Card c){
                    		return !c.isLand();
                    	}                    	
                    });
                    return !possible.isEmpty() && super.canPlay();
                }
                
                public boolean canPlayAI(){return false;}
                
                @Override
                public void resolve() {
                    Card c = chosen.pop();
                    c.addCounter(Counters.TIME, 4);
                    c.setSuspend(true);
                }
            };
            
            ability.setAfterPayMana(new Input() {
                private static final long serialVersionUID = -1647181037510967127L;
                
                @Override
                public void showMessage()
                {
                	ButtonUtil.disableAll();
                    AllZone.Display.showMessage("Exile a nonland card from your hand.");
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone)
                {
                	if(zone.is(Constant.Zone.Hand) && !c.isLand())
                	{
                		AllZone.GameAction.exile(c);
                		chosen.push(c);
                		ability.setStackDescription(card.toString() + " - Suspending " + c.toString());
                		AllZone.Stack.add(ability);
                		stop();
                	}
                }
            });
            
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Disciple of Kangee")) {
        	Cost abCost = new Cost("U T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, new Target(card,"TgtC")) {
                private static final long serialVersionUID = -5169389637917649036L;
                
                @Override
                public boolean canPlayAI() {
                    if (CardFactoryUtil.AI_doesCreatureAttack(card)) return false;
                    
                    return CardFactoryUtil.AI_getHumanCreature("Flying", card, false).isEmpty()
                            && (getCreature().size() != 0);
                }
                
                @Override
                public void chooseTargetAI() {
                    card.tap();
                    Card target = CardFactoryUtil.AI_getBestCreature(getCreature());
                    setTargetCard(target);
                }
                
                CardList getCreature() {
                    CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && 
                            	   (!CardFactoryUtil.AI_doesCreatureAttack(c)) && 
                            	   (!c.getKeyword().contains("Flying")) && 
                            	   CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    list.remove(card);
                    return list;
                }//getCreature()
                
                @Override
                public void resolve() {
                    if (AllZone.GameAction.isCardInPlay(getTargetCard()) && 
                    		CardFactoryUtil.canTarget(card, getTargetCard())) {
                        final Card[] creature = new Card[1];
                        final long timestamp;
                        
                        creature[0] = getTargetCard();
                        creature[0].addExtrinsicKeyword("Flying");
                        timestamp = creature[0].addColor("U", card, false, true);
            
                        final Command EOT = new Command() {
                            private static final long serialVersionUID = -1899153704584793548L;
                            long stamp = timestamp;
                            public void execute() {
                                if (AllZone.GameAction.isCardInPlay(creature[0])) {
                                    creature[0].removeExtrinsicKeyword("Flying");
                                    creature[0].removeColor("U", card, false, stamp);
                                }
                            }
                        };
                        AllZone.EndOfTurn.addUntil(EOT);

                    }//if (card is in play)
                }//resolve()
            };//SpellAbility
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Target creature gains flying and becomes blue until end of turn.");
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Mirror Entity"))
        {
      	  final Ability ability = new Ability(card, "0")
      	  {
      		  public void resolve()
      		  {
        			final CardList list = new CardList(AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards()).getType("Creature");
                    final int[] originalAttack = new int[list.size()];
                    final int[] originalDefense = new int[list.size()];
          			for(int i = 0; i < list.size(); i++) {
                     originalAttack[i] = list.get(i).getBaseAttack();
                     originalDefense[i] = list.get(i).getBaseDefense();
                      
                      list.get(i).setBaseAttack(Integer.parseInt(getManaCost()));
                      list.get(i).setBaseDefense(Integer.parseInt(getManaCost()));
                      list.get(i).addExtrinsicKeyword("Changeling");
                      if(i + 1 == list.size()) {
                      final Command EOT = new Command() {
                          private static final long serialVersionUID = 6437463765161964445L;
                          
                          public void execute() {
                        	  
                        	  for(int x = 0; x < list.size(); x++) {
                              if(AllZone.GameAction.isCardInPlay(list.get(x))) {
                            	  list.get(x).setBaseAttack(originalAttack[x]);
                            	  list.get(x).setBaseDefense(originalDefense[x]);
                            	  list.get(x).removeExtrinsicKeyword("Changeling");
                              }
                        	  }
                        	  }
                          };
                          AllZone.EndOfTurn.addUntil(EOT);
                      };
      		  }
      		  }
      		  public boolean canPlayAI()
      		  {
      			 return false;
      			  /*
      			CardList Clist = new CardList(AllZone.getZone(Constant.Zone.Play, AllZone.ComputerPlayer).getCards()).getType("Creature"); 
      			CardList Hlist = new CardList(AllZone.getZone(Constant.Zone.Play, AllZone.HumanPlayer).getCards()).getType("Creature");
      			return((Clist.size() - Hlist.size() * ComputerUtil.getAvailableMana().size() > AllZone.HumanPlayer.getLife())
      					&& AllZone.Phase.getPhase().equals(Constant.Phase.Main1));
      					*/
      					
      		  }
      	  };
      	  ability.setBeforePayMana(new Input()
      	  {
      		private static final long serialVersionUID = 4378124586732L;

  			public void showMessage()
      		 {
      			 String s = JOptionPane.showInputDialog("What would you like X to be?");
      	  		 try {
      	  			     Integer.parseInt(s);
      	  				 ability.setManaCost(s);
      	  				 stopSetNext(new Input_PayManaCost(ability));
      	  			 }
      	  			 catch(NumberFormatException e){
      	  				 AllZone.Display.showMessage("\"" + s + "\" is not a number.");
      	  				 showMessage();
      	  			 }
      		 }
      	  });
      	  ability.setDescription("X: Creatures you control become X/X and gain changeling until end of turn.");
      	  
      	  StringBuilder sb = new StringBuilder();
      	  sb.append(card.getName()).append("X: Creatures you control become X/X and gain changeling until end of turn.");
      	  ability.setStackDescription(sb.toString());
      	  
      	  card.addSpellAbility(ability);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Gigantomancer")) {
        	final Ability ability = new Ability(card, "1") {
                private static final long serialVersionUID = -68531201448677L;
                
                @Override
                public boolean canPlayAI() {
                    Card c = getCreature();
                    if(c == null) return false;
                    else {
                        setTargetCard(c);
                        return true;
                    }
                }//canPlayAI()
                
                //may return null
                public Card getCreature() {
                    CardList untapped = new CardList(AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards()).getType("Creature");
                    untapped = untapped.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isUntapped() && 6 > c.getNetAttack();
                        }
                    });                   
                    if(untapped.isEmpty()) return null;
                    Card worst = untapped.get(0);
                    for(int i = 0; i < untapped.size(); i++)
                        if(worst.getNetAttack() > untapped.get(i).getNetAttack()) worst = untapped.get(i);
                    return worst;
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        final Card[] creature = new Card[1];
                        
                        creature[0] = getTargetCard();
                        final int[] originalAttack = {creature[0].getBaseAttack()};
                        final int[] originalDefense = {creature[0].getBaseDefense()};
                        
                        creature[0].setBaseAttack(7);
                        creature[0].setBaseDefense(7);
                        
                        final Command EOT = new Command() {
                            private static final long serialVersionUID = 6437463765161964445L;
                            
                            public void execute() {
                                if(AllZone.GameAction.isCardInPlay(creature[0])) {
                                    creature[0].setBaseAttack(originalAttack[0]);
                                    creature[0].setBaseDefense(originalDefense[0]);
                                }
                            }
                        };
                        AllZone.EndOfTurn.addUntil(EOT);
                    }//is card in play?
                }//resolve()
            };//SpellAbility
            card.addSpellAbility(ability);
            ability.setDescription("1: Target creature you control becomes 7/7 until end of turn.");
            //this ability can target "this card" when it shouldn't be able to
            ability.setBeforePayMana(new Input() {
                private static final long serialVersionUID = -7903295056497483023L;
                
                @Override
                public void showMessage() {
                	Player player = card.getController();
                    CardList targets = new CardList(AllZone.getZone(Constant.Zone.Battlefield, player).getCards()); 
                    targets = targets.getType("Creature");
                    stopSetNext(CardFactoryUtil.input_targetSpecific(ability, targets,
                            "Select a creature you control", true, false));
                }
            });
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Hermit Druid")) {
        	Cost abCost = new Cost("G T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = 5884624727757154056L;

				@Override
                public boolean canPlayAI() {
                    // todo: figure out when the AI would want to use the Druid
					return false;
                }                
                
                @Override
                public void resolve() {
                	CardList library = AllZoneUtil.getPlayerCardsInLibrary(card.getController());
                	if(library.size() == 0) return;	// maybe provide some notification that library is empty?
 
					CardList revealed = new CardList();

					Card basicGrab = null;

					int count = 0;
					// reveal top card until library runs out or hit a basic land
					while(basicGrab == null) {
						Card top = library.get(count);
						count++;
						revealed.add(top);

						if (top.isBasicLand())
							basicGrab = top;

						if(count == library.size()) 
							break;
					}//while
					GuiUtils.getChoiceOptional("Revealed cards:", revealed.toArray());
					
					if (basicGrab != null){
						// put basic in hand
						AllZone.GameAction.moveToHand(basicGrab);
						revealed.remove(basicGrab);
					}
					// place revealed cards in graveyard (todo: player should choose order)
					for(Card c : revealed){
						AllZone.GameAction.moveToGraveyard(c);
					}
                }
            };
            ability.setStackDescription(abCost+"Reveal cards until you reveal a basic land. Put that in your hand, and put the rest in your graveyard");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sorceress Queen") || cardName.equals("Serendib Sorcerer")) {
        	Cost abCost = new Cost("T", cardName, true);
        	Target target = new Target(card,"Select target creature other than "+cardName, new String[] {"Creature.Other"});
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = -6853184726011448677L;
                
                @Override
                public boolean canPlayAI() {
                    Card c = getCreature();
                    if(c == null) return false;
                    else {
                        setTargetCard(c);
                        return true;
                    }
                }//canPlayAI()
                
                //may return null
                public Card getCreature() {
                    CardList untapped = CardFactoryUtil.AI_getHumanCreature(card, true);
                    untapped = untapped.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isUntapped() && 2 < c.getNetDefense() && c != card;
                        }
                    });
                    if(untapped.isEmpty()) return null;
                    
                    Card big = CardFactoryUtil.AI_getBestCreature(untapped);
                    return big;
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        final Card[] creature = new Card[1];
                        
                        creature[0] = getTargetCard();
                        final int[] originalAttack = {creature[0].getBaseAttack()};
                        final int[] originalDefense = {creature[0].getBaseDefense()};
                        
                        creature[0].setBaseAttack(0);
                        creature[0].setBaseDefense(2);
                        
                        final Command EOT = new Command() {
                            private static final long serialVersionUID = 6437463765161964445L;
                            
                            public void execute() {
                                if(AllZone.GameAction.isCardInPlay(creature[0])) {
                                    creature[0].setBaseAttack(originalAttack[0]);
                                    creature[0].setBaseDefense(originalDefense[0]);
                                }
                            }
                        };
                        AllZone.EndOfTurn.addUntil(EOT);
                    }//is card in play?
                }//resolve()
            };//SpellAbility
            card.addSpellAbility(ability);
            
            StringBuilder sb = new StringBuilder();
            sb.append(abCost).append("Target creature other than ").append(cardName).append(" becomes 0/2 until end of turn.");
            ability.setDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Vedalken Plotter")) {
            final Card[] target = new Card[2];
            final int[] index = new int[1];
            
            final Ability ability = new Ability(card, "") {
                
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
                    	Player p0 = crd0.getController();
                    	Player p1 = crd1.getController();
                    	AllZone.GameAction.changeController(new CardList(crd0), p0, p1);
                    	AllZone.GameAction.changeController(new CardList(crd1), p1, p0);
                    }
                    
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                
                private static final long serialVersionUID = -7143706716256752987L;
                
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
                    

                    if(c.isLand() && zone.is(Constant.Zone.Battlefield) && CardFactoryUtil.canTarget(card, c)) {
                        //System.out.println("c is: " +c);
                        target[index[0]] = c;
                        index[0]++;
                        showMessage();
                        
                        if(index[0] == target.length) {
                            AllZone.Stack.add(ability);
                            stop();
                        }
                    }
                }//selectCard()
            };//Input
            
            Command comesIntoPlay = new Command() {
                private static final long serialVersionUID = 6513203926272187582L;
                
                public void execute() {
                    index[0] = 0;
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(input);
                }
            };
            
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" - Exchange control of target land you control and target land an opponent controls.");
            ability.setStackDescription(sb.toString());
            
            card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dauntless Escort")) {
        	final SpellAbility ability = new Ability(card, "0") {
        		@Override
        		public boolean canPlayAI() {
			        PlayerZone PlayerPlayZone = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
			        CardList PlayerCreatureList = new CardList(PlayerPlayZone.getCards());
			        PlayerCreatureList = PlayerCreatureList.getType("Creature");
					PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
			        CardList opponentCreatureList = new CardList(opponentPlayZone.getCards());
			        opponentCreatureList = opponentCreatureList.getType("Creature");
                    return ((PlayerCreatureList.size() + 1 > 2* opponentCreatureList.size() + 1) && (Phase.isSacDauntlessEscortAI() == false) && (AllZone.Phase.getPhase().equals(Constant.Phase.Main1))) ;
        		}
               
                final Command untilEOT = new Command() {
                    private static final long serialVersionUID = 2701248867610L;
                    
                    public void execute() {
                        if(card.getController() == AllZone.HumanPlayer) {
                        	Phase.setSacDauntlessEscort(false);
                        	} else {
                        	Phase.setSacDauntlessEscortAI(false);  
                        	}
    			        PlayerZone PlayerPlayZone = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
    			        CardList PlayerCreatureList = new CardList(PlayerPlayZone.getCards());
    			        PlayerCreatureList = PlayerCreatureList.getType("Creature");
        				if(PlayerCreatureList.size() != 0) {
        	                for(int i = 0; i < PlayerCreatureList.size(); i++) {
        	                	Card c = PlayerCreatureList.get(i);
        	                    c.removeExtrinsicKeyword("Indestructible");				
        				}
        					}
                    }
                };
        		@Override
        		public void resolve() {
                    AllZone.GameAction.sacrifice(card);
                    if(card.getController() == AllZone.HumanPlayer) {
                    	Phase.setSacDauntlessEscort(true);
                    }
                    else Phase.setSacDauntlessEscortAI(true);	

                    AllZone.EndOfTurn.addUntil(untilEOT);
        		}
        	};
        	card.addSpellAbility(ability);
            ability.setStackDescription("Sacrifice Dauntless Escort: Creatures you control are indestructible this turn.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Wojek Embermage")) {
        	Cost abCost = new Cost("T", cardName, true);
            Target tgt = new Target(card,"TgtC");
            final Ability_Activated ability = new Ability_Activated(card, abCost, tgt) {
                private static final long serialVersionUID = -1208482961653326721L;
                
                @Override
                public boolean canPlayAI() {
                    return (CardFactoryUtil.AI_getHumanCreature(1, card, true).size() != 0)
                            && (AllZone.Phase.getPhase().equals(Constant.Phase.Main2));
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList list = CardFactoryUtil.AI_getHumanCreature(1, card, true);
                    list.shuffle();
                    setTargetCard(list.get(0));
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        CardList list = getRadiance(getTargetCard());
                        for(int i = 0; i < list.size(); i++) {
                        	list.get(i).addDamage(1, card);
                        }
                    }
                }//resolve()
                
                //parameter Card c, is included in CardList
                //no multi-colored cards
                CardList getRadiance(Card c) {
                	if(CardUtil.getColors(c).contains(Constant.Color.Colorless)) {
                        CardList list = new CardList();
                        list.add(c);
                        return list;
                    }
                    
                    CardList sameColor = new CardList();
                    CardList list = AllZoneUtil.getCreaturesInPlay();
                    
                    for(int i = 0; i < list.size(); i++)
                        if(list.get(i).sharesColorWith(c)) sameColor.add(list.get(i));
                    
                    return sameColor;
                }
                
            };//SpellAbility
            card.addSpellAbility(ability);
            ability.setDescription("Radiance - "+abCost+cardName+" deals 1 damage to target creature and each other creature that shares a color with it.");
            
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Adarkar Valkyrie")) {
            //tap ability - no cost - target creature - EOT
            
            final Card[] target = new Card[1];
            
            final Command destroy = new Command() {
                private static final long serialVersionUID = -2433442359225521472L;
                
                public void execute() {
                    AllZone.Stack.add(new Ability(card, "0", "Adarkar Valkyrie - Return " + target[0] + " from graveyard to the battlefield") {
                        @Override
                        public void resolve() {
                            PlayerZone grave = AllZone.getZone(target[0]);
                            //checks to see if card is still in the graveyard
        
                            if(grave != null && AllZone.GameAction.isCardInZone(target[0], grave)) {
                                PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                                target[0].setController(card.getController());
                                AllZone.GameAction.moveTo(play, target[0]);
                            }
                        }
                    });
                }//execute()
            };
            
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = 2777978927867867610L;
                
                public void execute() {
                    //resets the Card destroy Command
                    target[0].removeDestroyCommand(destroy);
                }
            };
            
            Cost abCost = new Cost("T", cardName, true);
            Target tgt = new Target(card,"Target creature other than "+cardName, "Creature.Other".split(","));
            final Ability_Activated ability = new Ability_Activated(card, abCost, tgt){
                private static final long serialVersionUID = -8454685126878522607L;
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())) {
                        target[0] = getTargetCard();
                        
                        if (!target[0].isToken()){	// not necessary, but will help speed up stack resolution
	                        AllZone.EndOfTurn.addUntil(untilEOT);
	                        target[0].addDestroyCommand(destroy);
                        }
                    }//if
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility

            card.addSpellAbility(ability);
            
            StringBuilder sb = new StringBuilder();
            sb.append("tap: When target creature other than Adarkar Valkyrie is put into a ");
            sb.append("graveyard this turn, return that card to the battlefield under your control.");
            ability.setDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Mayael the Anima")) {
        	Cost abCost = new Cost("3 R G W T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, null) {
                
                private static final long serialVersionUID = -9076784333448226913L;
                
				@Override
                public void resolve() {
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());

                    CardList Library = new CardList(lib.getCards());
                    int Count = 5;
                    if(Library.size() < 5) Count = Library.size();
                    CardList TopCards = new CardList();
                    
                    for(int i = 0; i < Count; i++) TopCards.add(Library.get(i));
                    CardList TopCreatures = TopCards;
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        if(TopCards.size()  > 0) {
						GuiUtils.getChoice(
                                "Look at the top five cards: ", TopCards.toArray());
                        TopCreatures = TopCreatures.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                if(c.isCreature() && c.getNetAttack() >= 5) return true;
                                else return false;
                            }
                        });
                        if(TopCreatures.size() > 0) {
	                        Object o2 = GuiUtils.getChoiceOptional(
	                                "Put a creature with a power 5 or greater onto the battlefield: ", TopCreatures.toArray());
	                        if(o2 != null) {
	                            Card c = (Card) o2;
	                            AllZone.GameAction.moveToPlay(c);
	                            TopCards.remove(c);
	                        }
                        } else 
                        	JOptionPane.showMessageDialog(null, "No creatures in top 5 cards with a power greater than 5.", "", JOptionPane.INFORMATION_MESSAGE); 
                        
                        Count = TopCards.size();
                    	for(int i = 0; i < Count; i++) {   
                            AllZone.Display.showMessage("Select a card to put " + (Count - i) + " from the bottom of your library: "  + (Count - i) + " Choices to go.");
                            ButtonUtil.enableOnlyCancel();
                            Object check = GuiUtils.getChoice("Select a card: ", TopCards.toArray());   
                            AllZone.GameAction.moveTo(lib, (Card) check);
                            TopCards.remove((Card) check);
                        }
                        }  else JOptionPane.showMessageDialog(null, "No more cards in library.", "", JOptionPane.INFORMATION_MESSAGE);
                    }
                        else {
                        TopCreatures = TopCreatures.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                            	CardList Compplay = new CardList();
                            	Compplay.addAll(AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards());
                            	Compplay = Compplay.getName(c.getName());
                                if(c.isCreature() && c.getNetAttack() >= 5 && (Compplay.size() == 0 && c.getType().contains("Legendary"))) return true;
                                else return false;
                            }
                        });
                        if(TopCreatures.size() > 0) {
                        	Card c = CardFactoryUtil.AI_getBestCreature(TopCreatures);
                        	AllZone.GameAction.moveToPlay(c);
                            TopCards.remove(c);
                        Count = TopCards.size();
                    	for(int i = 0; i < Count; i++) {
                    		Card Remove_Card = TopCards.get(i);
                            AllZone.GameAction.moveTo(lib, Remove_Card);
                        	}
                    } 
                        
                    }
                }              
                @Override
                public boolean canPlayAI() {
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList Library = new CardList(lib.getCards());
                    return Library.size() > 0 && super.canPlay();
                }
            };
                     
            card.addSpellAbility(ability);
            
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(abCost+"Look at the top five cards of your library. ");
            sbDesc.append("You may put a creature card with power 5 or greater from among them onto the battlefield. ");
            sbDesc.append("Put the rest on the bottom of your library in any order.");
            ability.setDescription(sbDesc.toString());
            
            StringBuilder sbStack = new StringBuilder();
            sbStack.append(card).append(" - Looks at the top five cards of his/her library. ");
            sbStack.append("That player may put a creature card with power 5 or greater from among them onto the battlefield. ");
            sbStack.append("The player then puts the rest on the bottom of his/her library in any order.");
            ability.setStackDescription(sbStack.toString());
        }//*************** END ************ END ************************** 

        
        //*************** START *********** START **************************
        else if(cardName.equals("Helldozer")) {
            Cost abCost = new Cost("B B B T", cardName, true);
            Target target = new Target(card,"Select target land.", new String[]{"Land"});
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = 6426884086364885861L;
                
                @Override
                public boolean canPlayAI() {
                    if(CardFactoryUtil.AI_doesCreatureAttack(card)) return false;
                    
                    CardList land = new CardList(AllZone.Human_Battlefield.getCards());
                    land = land.getType("Land");
                    return land.size() != 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    //target basic land that Human only has 1 or 2 in play
                    CardList land = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
                    
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
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        AllZone.GameAction.destroy(getTargetCard());
                        
                        //if non-basic, untap Helldozer
                        if(!getTargetCard().isBasicLand()) card.untap();
                    }
                }//resolve()
            };//SpellAbility
            
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Destroy target land. If that land was nonbasic, untap Helldozer.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Obsidian Fireheart")) {
            final Ability ability = new Ability(card, "1 R R") {
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    
                    if(AllZone.GameAction.isCardInPlay(c) && c.isLand() && (c.getCounters(Counters.BLAZE) == 0)) c.addCounter(
                            Counters.BLAZE, 1);
                    
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
                    CardList land = new CardList(play.getCards());
                    land = land.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isLand() && c.getCounters(Counters.BLAZE) < 1;
                        }
                    });
                    
                    if(land.size() > 0) setTargetCard(land.get(0));
                    
                    return land.size() > 0;
                }
                
            };
            
            StringBuilder sb = new StringBuilder();
            sb.append("1 R R: Put a blaze counter on target land without a blaze counter on it. ");
            sb.append("For as long as that land has a blaze counter on it, it has \"At the beginning of your upkeep, ");
            sb.append("this land deals 1 damage to you.\" (The land continues to burn after Obsidian Fireheart has left the battlefield.)");
            ability.setDescription(sb.toString());
            
            ability.setBeforePayMana(CardFactoryUtil.input_targetType(ability, "Land"));
            card.addSpellAbility(ability);
        }// *************** END ************ END **************************        
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Wild Mongrel")) {
        	
        	final String[] color = new String[1];
        	final long[] timeStamp = new long[1];
            
        	final Cost abCost = new Cost("Discard<1/Card>", cardName, true);
        	
            //mana tap ability
            final SpellAbility ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = 5443609178720006665L;

				@Override
                public boolean canPlayAI() {
                    Card[] hand = AllZone.Computer_Hand.getCards();
                    return CardFactoryUtil.AI_doesCreatureAttack(card) && (hand.length > 3);
                }
                
                @Override
                public void chooseTargetAI() {
                    AllZone.ComputerPlayer.discardRandom(this);
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        card.addTempAttackBoost(1);
                        card.addTempDefenseBoost(1);
                        if(card.getController().equals(AllZone.HumanPlayer)) {
                            String[] colors = Constant.Color.onlyColors;
                            
                            Object o = GuiUtils.getChoice("Choose color", colors);
                            color[0] = (String) o;
                            card.setChosenColor(color[0]);
                        } else { 
                        	// wild mongrel will choose a color that appears the most, but that might not be right way to choose
                        	PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.ComputerPlayer);
                            PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer);
                            CardList list = new CardList();
                            list.addAll(lib.getCards());
                            list.addAll(hand.getCards());
                            list.addAll(AllZone.Computer_Battlefield.getCards());
                            
                            color[0] = Constant.Color.White;
                            int max = list.getKeywordsContain(color[0]).size();
                            
                            String[] colors = { Constant.Color.Blue, Constant.Color.Black, Constant.Color.Red, Constant.Color.Green };
                            for(String c : colors){
    	                        int cmp = list.getKeywordsContain(c).size();
    	                        if (cmp > max){
    	                        	max = cmp;
    	                        	color[0] = c;
    	                        }
                            }
                            card.setChosenColor(color[0]);
                        }
                        String s = CardUtil.getShortColor(color[0]);
                        timeStamp[0] = card.addColor(s, card, false, true);
                        
                        //sacrifice ability - targets itself - until EOT
                        final Command untilEOT = new Command() {
                            private static final long serialVersionUID = -5563743272875711445L;
                            long stamp = timeStamp[0];
                            String s = CardUtil.getShortColor(color[0]);
                            
                            public void execute() {
                                card.addTempAttackBoost(-1);
                                card.addTempDefenseBoost(-1);
                                card.removeColor(s, card, false, stamp);
                                card.setChosenColor("");
                            }
                        };
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }//resolve()
            };//SpellAbility

            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" gets +1/+1 and becomes the color of your choice until end of turn.");
            ability.setStackDescription(sb.toString());
            
            ability.setDescription("Discard a card: Wild Mongrel gets +1/+1 and becomes the color of your choice until end of turn.");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Spiritmonger")) {
        	
        	final String[] color = new String[1];
        	final long[] timeStamp = new long[1];
            
            //color change ability
        	Cost abCost = new Cost("G", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = -5362934962417382279L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        if(card.getController().equals(AllZone.HumanPlayer)) {
                            String[] colors = Constant.Color.onlyColors;
                            
                            Object o = GuiUtils.getChoice("Choose color", colors);
                            color[0] = (String) o;
                            card.setChosenColor(color[0]);
                            String s = CardUtil.getShortColor(color[0]);
                            timeStamp[0] = card.addColor(s, card, false, true);
                            
                            //until EOT
                            final Command untilEOT = new Command() {
                                private static final long serialVersionUID = -7093762180313802891L;
                                long stamp = timeStamp[0];
                                String s = CardUtil.getShortColor(color[0]);
                                public void execute() {
                                    card.removeColor(s, card, false, stamp);
                                    card.setChosenColor("");
                                }
                            };
                            
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }
                    }
                }//resolve()
            };//SpellAbility
            
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes the color of your choice until end of turn.");
            ability.setStackDescription(sb.toString());
            
            ability.setDescription(abCost+cardName+" becomes the color of your choice until end of turn.");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Jugan, the Rising Star")) {
            final SpellAbility ability = new Ability(card, "0") {
                //should the computer play this card?
                @Override
                public boolean canPlayAI() {
                    return (getComputerCreatures().size() != 0);
                }
                
                //set the target for the computer AI
                @Override
                public void chooseTargetAI() {
                    CardList list = getComputerCreatures();
                    
                    if(0 < list.size()) setTargetCard(list.get(0));
                    else
                    //the computer doesn't have any other creatures
                    setTargetCard(null);
                }
                
                CardList getComputerCreatures() {
                    CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
                    CardList out = list.getType("Creature");
                    return out;
                }//getCreatures
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(c != null && AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        c.addCounter(Counters.P1P1, 5);
                    }
                }//resolve()
            };
            ability.setBeforePayMana(CardFactoryUtil.input_targetCreature(ability));
            
            Command leavesPlay = new Command() {
                private static final long serialVersionUID = -2823505283781217181L;
                
                public void execute() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(CardFactoryUtil.input_targetCreature(ability));
                    else if(ability.canPlayAI()) {
                        ability.chooseTargetAI();
                        //need to add this to the stack
                        AllZone.Stack.add(ability);
                    }
                    
                }//execute()
            };//Command
            
            card.addDestroyCommand(leavesPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Painter's Servant")) {
        	final long[] timeStamp = new long[1];
        	final String[] color = new String[1];
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        String[] colors = Constant.Color.onlyColors;
                        
                        Object o = GuiUtils.getChoice("Choose color", colors);
                        color[0] = (String) o;
                        card.setChosenColor(color[0]);
                    } else { 
                    	// AI chooses the color that appears in the keywords of the most cards in its deck, hand and on battlefield
                    	PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.ComputerPlayer);
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer);
                        CardList list = new CardList();
                        list.addAll(lib.getCards());
                        list.addAll(hand.getCards());
                        list.addAll(AllZone.Computer_Battlefield.getCards());
                        
                        color[0] = Constant.Color.White;
                        int max =  list.getKeywordsContain(color[0]).size();
                        
                        String[] colors = { Constant.Color.Blue, Constant.Color.Black, Constant.Color.Red, Constant.Color.Green };
                        for(String c : colors){
	                        int cmp = list.getKeywordsContain(c).size();
	                        if (cmp > max){
	                        	max = cmp;
	                        	color[0] = c;
	                        }
                        }
                        card.setChosenColor(color[0]);
                    }
                    
                    String s = CardUtil.getShortColor(color[0]);
                    timeStamp[0] = AllZone.GameInfo.addColorChanges(s, card, true, true);
                }
            };
            
            Command comesIntoPlay = new Command() {
                private static final long serialVersionUID = 333134223161L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };//Command

            final Ability unpaint = new Ability(card, "0") {
            	public void resolve(){
            		String s = CardUtil.getShortColor(color[0]);
            		AllZone.GameInfo.removeColorChanges(s, card, true, timeStamp[0]);
            	}
            };
            
            Command leavesBattlefield = new Command() {
				private static final long serialVersionUID = 2559212590399132459L;

				public void execute(){
            		AllZone.Stack.add(unpaint);
            	}
            };
 
            ability.setStackDescription("As Painter's Servant enters the battlefield, choose a color.");
            unpaint.setStackDescription("Painter's Servant left the battlefield, resetting colors.");
            card.addComesIntoPlayCommand(comesIntoPlay);
            card.addLeavesPlayCommand(leavesBattlefield);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Goblin Skycutter")) {
            final Ability ability = new Ability(card, "0") {
                @Override
                public boolean canPlayAI() {
                    return getFlying().size() != 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    AllZone.GameAction.sacrifice(card);
                    
                    CardList flying = getFlying();
                    flying.shuffle();
                    setTargetCard(flying.get(0));
                }
                
                CardList getFlying() {
                    CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    flying = flying.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getNetDefense() == 2;
                        }
                    });
                    return flying;
                }//getFlying()
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        getTargetCard().addDamage(2, card);
                        getTargetCard().removeIntrinsicKeyword("Flying");
                        getTargetCard().removeExtrinsicKeyword("Flying");
                    }
                    
                    AllZone.EndOfTurn.addUntil(new Command() {
                        private static final long serialVersionUID = -8889549737746466810L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(getTargetCard())) getTargetCard().addIntrinsicKeyword(
                                    "Flying");
                        }
                    });
                }//resolve()
            };//SpellAbility
            
            Input runtime = new Input() {
                private static final long serialVersionUID = 8609211991425118222L;
                
                @Override
                public void showMessage() {
                    CardList list = AllZoneUtil.getCreaturesInPlay();
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.getKeyword().contains("Flying")
                                    && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(ability, list,
                            "Select a creature with flying to deal 2 damage to", new Command() {
                                private static final long serialVersionUID = -3287971244881855563L;
                                
                                public void execute() {
                                    AllZone.GameAction.sacrifice(card);
                                }
                            }, true, false));
                }//showMessage()
            };//Input
            
            card.addSpellAbility(ability);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Sacrifice Goblin Skycutter: Goblin Skycutter deals 2 damage to target ");
            sb.append("creature with flying. That creature loses flying until end of turn.");
            ability.setDescription(sb.toString());
            
            ability.setBeforePayMana(runtime);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Transluminant")) {
            final Command atEOT = new Command() {
                private static final long serialVersionUID = -5126793112740563180L;
                
                public void execute() {
                    CardFactoryUtil.makeToken("Spirit", "W 1 1 Spirit", card.getController(), "W", new String[] {
                            "Creature", "Spirit"}, 1, 1, new String[] {"Flying"});
                }//execute()
            };//Command
            
            final Ability ability = new Ability(card, "W") {
                @Override
                public boolean canPlayAI() { /*
                                             CardList list = new CardList(AllZone.Human_Play.getCards());
                                             list = list.getType("Creature");

                                             String phase = AllZone.Phase.getPhase();
                                             return phase.equals(Constant.Phase.Main2) && list.size() != 0;
                                             */
                    return false;
                }
                
                @Override
                public void chooseTargetAI() {
                    AllZone.GameAction.sacrifice(card);
                }
                
                @Override
                public void resolve() {
                    AllZone.EndOfTurn.addAt(atEOT);
                }//resolve()
            };//SpellAbility
            
            card.addSpellAbility(ability);
            ability.setDescription("W, Sacrifice Transluminant: Put a 1/1 white Spirit creature token with flying onto the battlefield at the beginning of the next end step.");
            ability.setStackDescription("Put a 1/1 white Spirit creature token with flying onto the battlefield at end of turn.");
            ability.setBeforePayMana(new Input_PayManaCost_Ability(ability.getManaCost(), new Command() {
                private static final long serialVersionUID = -6553009833190713980L;
                
                public void execute() {
                    AllZone.GameAction.sacrifice(card);
                    AllZone.Stack.add(ability);
                }
            }));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Memnarch")) {
            //has 2 non-tap abilities that affect itself
            final SpellAbility ability1 = new Ability(card, "1 U U") {
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        Card crd = getTargetCard();
                        ArrayList<String> types = crd.getType();
                        crd.setType(new ArrayList<String>()); //clear
                        getTargetCard().addType("Artifact"); //make sure artifact is at the beginning
                        for(String type:types)
                            crd.addType(type);
                        
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    CardList list = getCreature();
                    return list.size() != 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    Card target = CardFactoryUtil.AI_getBestCreature(getCreature());
                    setTargetCard(target);
                }//chooseTargetAI()
                
                CardList getCreature() {
                    CardList list = new CardList(AllZone.Human_Battlefield.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && (!c.isArtifact()) && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    return list;
                }//getCreature()
            };//SpellAbility
            
            card.addSpellAbility(ability1);
            ability1.setDescription("1 U U: Target permanent becomes an artifact in addition to its other types. (This effect doesn't end at end of turn.)");
            ability1.setBeforePayMana(CardFactoryUtil.input_targetType(ability1, "All"));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Stangg")) {
            
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    CardList cl = CardFactoryUtil.makeToken("Stangg Twin", "RG 3 4 Stangg Twin", card.getController(), "R G",
                            new String[] {"Legendary", "Creature", "Human", "Warrior"}, 3, 4, new String[] {""});
                    
                    cl.get(0).addLeavesPlayCommand(new Command() {
                        private static final long serialVersionUID = 3367390368512271319L;
                        
                        public void execute() {
                            if(AllZone.GameAction.isCardInPlay(card)) AllZone.GameAction.sacrifice(card);
                        }
                    });
                }
            };
            ability.setStackDescription("When Stangg enters the battlefield, if Stangg is on the battlefield, put a legendary 3/4 red and green Human Warrior creature token named Stangg Twin onto the battlefield.");
            
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 6667896040611028600L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            });
            
            card.addLeavesPlayCommand(new Command() {
                private static final long serialVersionUID = 1786900359843939456L;
                
                public void execute() {
                    CardList list = AllZoneUtil.getCardsInPlay("Stangg Twin");
                    
                    if(list.size() == 1) AllZone.GameAction.exile(list.get(0));
                }
            });            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Goldmeadow Lookout")) {
        	final Cost lookCost = new Cost("W T Discard<1/Card>", card.getName(), true);
        	final SpellAbility ability = new Ability_Activated(card, lookCost, null){
                private static final long serialVersionUID = -8413409735529340094L;
                
                @Override
                public void resolve() {
                    makeToken();
                }
                
                void makeToken() {
                    CardList cl = CardFactoryUtil.makeToken("Goldmeadow Harrier", "W 1 1 Goldmeadow Harrier",
                    		card.getController(), "W", new String[] {"Creature", "Kithkin", "Soldier"}, 1, 1, new String[] {""});
                    
                    for(final Card c:cl) {
                    	final Cost abCost = new Cost("W T", c.getName(), true);
                    	final Target tgt = new Target(card,"TgtC");
                    	final SpellAbility tokenAbility = new Ability_Activated(card, abCost, tgt){
                            private static final long serialVersionUID = -7327585136675896817L;
                            
                            @Override
                            public void resolve() {
                                Card c = getTargetCard();
                                c.tap();
                            }
                            
                            @Override
                            public boolean canPlayAI() {
                            	CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                            	human = human.filter(new CardListFilter() {
                            		public boolean addCard(Card c) {
                            			return c.isUntapped() && CardFactoryUtil.canTarget(card, c);
                            		}
                            	});
                            	
                                if (human.size() > 0) {
                                	CardListUtil.sortAttack(human);
                                    CardListUtil.sortFlying(human);
                                	setTargetCard(human.get(0));
                                }
                                
                                PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                                CardList assassins = new CardList();
                                assassins.addAll(play.getCards());
                                
                                assassins = assassins.filter(new CardListFilter() {
                                    public boolean addCard(Card c) {
                                        return c.isCreature() && (!c.hasSickness() || c.getKeyword().contains("Haste")) && c.isUntapped() && 
                                              (c.getName().equals("Rathi Assassin") || c.getName().equals("Royal Assassin") || 
                                               c.getName().equals("Tetsuo Umezawa") || c.getName().equals("Stalking Assassin"));
                                    }
                                });
                                
                                Combat attackers = ComputerUtil.getAttackers();
                                CardList list = new CardList(attackers.getAttackers());
                            	
                                return (AllZone.Phase.getPhase().equals(Constant.Phase.Main1) && AllZone.Phase.getPlayerTurn().equals(card.getController()) && 
                                		human.size() > 0 && (assassins.size() > 0 || !list.contains(card)));
                                
                            }//canPlayAI
                        };//SpellAbility
                        c.addSpellAbility(new Spell_Permanent(c));
                        c.addSpellAbility(tokenAbility);
                        tokenAbility.setDescription("W, tap: Tap target creature.");
                    }
                    
                }//makeToken()
                
                @Override
                public boolean canPlayAI() {
                    return super.canPlayAI() && AllZone.Phase.getPhase().equals(Constant.Phase.Main2);
                }
            };//SpellAbility
            
            card.addSpellAbility(ability);
            
            StringBuilder sb = new StringBuilder();
            sb.append("W, tap, Discard a card: Put a 1/1 white Kithkin Soldier creature token named ");
            sb.append("Goldmeadow Harrier onto the battlefield. It has \"W, tap : Tap target creature.\"");
            ability.setDescription(sb.toString());
            
            ability.setStackDescription(cardName+" - Put a 1/1 token onto the battlefield");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Horde of Notions")) {
            final Ability ability = new Ability(card, "W U B R G") {
                @Override
                public void resolve() {
                    Card c = null;
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        Object o = GuiUtils.getChoiceOptional("Select Elemental", getCreatures());
                        c = (Card) o;
                        
                    } else {
                        c = getAIElemental();
                    }
                    
                    PlayerZone grave = AllZone.getZone(c);
                    
                    if(AllZone.GameAction.isCardInZone(c, grave)) {
                        PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, c.getController());
                        AllZone.GameAction.moveTo(play, c);
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return getCreatures().length != 0 && AllZone.GameAction.isCardInPlay(card) && super.canPlay();
                }
                
                public Card[] getCreatures() {
                    CardList creature = new CardList();
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    creature.addAll(zone.getCards());
                    creature = creature.getType("Elemental");
                    return creature.toArray();
                }
                
                public Card getAIElemental() {
                    Card c[] = getCreatures();
                    Card biggest = c[0];
                    for(int i = 0; i < c.length; i++)
                        if(biggest.getNetAttack() < c[i].getNetAttack()) biggest = c[i];
                    
                    return biggest;
                }
            };//SpellAbility
            card.addSpellAbility(ability);
            
            ability.setDescription("W U B R G: You may play target Elemental card from your graveyard without paying its mana cost.");
            ability.setStackDescription("Horde of Notions - play Elemental card from graveyard without paying its mana cost.");
            ability.setBeforePayMana(new Input_PayManaCost(ability));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Rhys the Redeemed")) {
           
        	Cost abCost = new Cost("4 GW GW T", card.getName(), true);
            final Ability_Activated copyTokens1 = new Ability_Activated(card, abCost, null) {
                private static final long serialVersionUID = 6297992502069547478L;
                
                @Override
                public void resolve() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    CardList allTokens = new CardList();
                    allTokens.addAll(play.getCards());
                    allTokens = allTokens.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && c.isToken();
                        }
                    });
                    
                    int multiplier = AllZoneUtil.getDoublingSeasonMagnitude(card.getController());
                    
                    for(int i = 0; i < allTokens.size(); i++) {
                        Card c = allTokens.get(i);
                        for(int j = 0; j < multiplier; j++)
                            copyToken(c);
                    }
                }
                
                public void copyToken(Card token) {
                    Card copy = new Card();
                    copy.setName(token.getName());
                    copy.setImageName(token.getImageName());
                    
                    copy.setOwner(token.getController());
                    copy.setController(token.getController());
                    copy.setManaCost(token.getManaCost());
                    copy.setToken(true);
                    copy.setType(token.getType());
                    copy.setBaseAttack(token.getBaseAttack());
                    copy.setBaseDefense(token.getBaseDefense());
                    
                    AllZone.GameAction.moveToPlay(copy);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    CardList tokens = new CardList(play.getCards());
                    tokens = tokens.filter(new CardListFilter() {
                        
                        public boolean addCard(Card c) {
                            return c.isToken();
                        }
                        
                    });
                    return tokens.size() >= 2;
                }
            };
            
            card.addSpellAbility(copyTokens1);
            copyTokens1.setDescription(abCost+"For each creature token you control, put a token that's a copy of that creature onto the battlefield.");
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - For each creature token you control, put a token that's a copy of that creature onto the battlefield.");
            copyTokens1.setStackDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Mystic Snake")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(AllZone.Stack.size() > 0) {
                        SpellAbility sa = AllZone.Stack.peek();
                        if(sa.isSpell() && CardFactoryUtil.isCounterable(sa.getSourceCard())) {
                            sa = AllZone.Stack.pop();
                            AllZone.GameAction.moveToGraveyard(sa.getSourceCard());
                        }
                    }
                }//resolve()
            };//SpellAbility
            
            Command intoPlay = new Command() {
                private static final long serialVersionUID = -6564365394043612388L;
                
                public void execute() {
                    if(AllZone.Stack.size() > 0) {
                    	StringBuilder sb = new StringBuilder();
                    	sb.append("Mystic Snake counters ").append(AllZone.Stack.peek().getSourceCard().getName());
                    	ability.setStackDescription(sb.toString());
                        
                        AllZone.Stack.add(ability);
                    }
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
            
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(new Spell_Permanent(card) {
                private static final long serialVersionUID = 6440845807532409545L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            });            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Rootwater Thief")) {
            final Ability ability2 = new Ability(card, "2") {
                @Override
                public void resolve() {
                	Player opponent = card.getController().getOpponent();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, opponent);
                    CardList cards = new CardList(lib.getCards());
                    
                    if(cards.size() > 0) {
                        if(card.getController().equals(AllZone.HumanPlayer)) {
                            Object o = GuiUtils.getChoiceOptional("Select card to remove: ",
                                    cards.toArray());
                            Card c = (Card) o;
                            AllZone.GameAction.exile(c);
                            opponent.shuffle();
                        } else {
                            Card c = lib.get(0);
                            AllZone.GameAction.exile(c);
                            opponent.shuffle();
                        }
                    }
                    
                }
                
                @Override
                public boolean canPlay() {
                    //this is set to false, since it should only TRIGGER
                    return false;
                }
            };// ability2
            card.addSpellAbility(ability2);
            
            StringBuilder sb2 = new StringBuilder();
            sb2.append(card.getName()).append(" - search opponent's library and remove a card from game.");
            ability2.setStackDescription(sb2.toString());
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Treva, the Renewer")) {
            final Player player = card.getController();
            
            final Ability ability2 = new Ability(card, "2 W") {
                @Override
                public void resolve() {
                    int lifeGain = 0;
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        String choices[] = {"white", "blue", "black", "red", "green"};
                        Object o = GuiUtils.getChoiceOptional("Select Color: ", choices);
                        Log.debug("Treva, the Renewer", "Color:" + o);
                        lifeGain = CardFactoryUtil.getNumberOfPermanentsByColor((String) o);
                        
                    } else {
                        CardList list = AllZoneUtil.getCardsInPlay();
                        String color = CardFactoryUtil.getMostProminentColor(list);
                        lifeGain = CardFactoryUtil.getNumberOfPermanentsByColor(color);
                    }
                    
                    card.getController().gainLife(lifeGain, card);
                }
                
                @Override
                public boolean canPlay() {
                    //this is set to false, since it should only TRIGGER
                    return false;
                }
            };// ability2
            //card.clearSpellAbility();
            card.addSpellAbility(ability2);
            
            StringBuilder sb2 = new StringBuilder();
            sb2.append(card.getName()).append(" - ").append(player);
            sb2.append(" gains life equal to permanents of the chosen color.");
            ability2.setStackDescription(sb2.toString());
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Rith, the Awakener")) {
            final Player player = card.getController();
            
            final Ability ability2 = new Ability(card, "2 G") {
                @Override
                public void resolve() {
                    int numberTokens = 0;
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        String choices[] = {"white", "blue", "black", "red", "green"};
                        Object o = GuiUtils.getChoiceOptional("Select Color: ", choices);
                        //System.out.println("Color:" + o);
                        numberTokens = CardFactoryUtil.getNumberOfPermanentsByColor((String) o);
                    } else {
                        CardList list = AllZoneUtil.getCardsInPlay();
                        String color = CardFactoryUtil.getMostProminentColor(list);
                        numberTokens = CardFactoryUtil.getNumberOfPermanentsByColor(color);
                    }
                    
                    for(int i = 0; i < numberTokens; i++) {
                        CardFactoryUtil.makeTokenSaproling(card.getController());
                    }
                }
                
                @Override
                public boolean canPlay() {
                    //this is set to false, since it should only TRIGGER
                    return false;
                }
            };// ability2
            //card.clearSpellAbility();
            card.addSpellAbility(ability2);
            
            StringBuilder sb2 = new StringBuilder();
            sb2.append(card.getName()).append(" - ").append(player);
            sb2.append(" puts a 1/1 green Saproling creature token onto the battlefield for each permanent of the chosen color");
            ability2.setStackDescription(sb2.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cromat")) {
            //Kill ability
            
            final Ability a2 = new Ability(card, "W B") {
                @Override
                public boolean canPlay() {
                    return (AllZone.GameAction.isCardInPlay(card) && (AllZone.Combat.isBlocked(card) || AllZone.Combat.getAllBlockers().contains(
                            card)) && super.canPlay());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                    //TODO
                }
                
                @Override
                public void resolve() {
                    AllZone.GameAction.destroy(getTargetCard());
                }//resolve()
            };
            Input runtime2 = new Input() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void showMessage() {
                    CardList targets = new CardList();
                    if(AllZone.Combat.isBlocked(card) || AllZone.Combat.getAllBlockers().contains(card)) {
                        if(AllZone.Combat.isBlocked(card)) {
                            targets = AllZone.Combat.getBlockers(card);
                        } else {
                            targets = new CardList();
                            for(Card c:AllZone.Combat.getAttackers()) {
                                if(AllZone.Combat.isBlocked(c)) if(AllZone.Combat.getBlockers(c).contains(card)) targets.add(c);
                            }
                        }
                    }
                    stopSetNext(CardFactoryUtil.input_targetSpecific(a2, targets,
                            "Select target blocking or blocked by Cromat.", true, false));
                }
            };
            card.addSpellAbility(a2);
            a2.setBeforePayMana(runtime2);
            a2.setDescription("W B: Destroy target creature blocking or blocked by Cromat.");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Sphinx of Jwar Isle")) {
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                	Player player = card.getController();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    
                    if(lib.size() < 1) return;
                    
                    CardList cl = new CardList();
                    cl.add(lib.get(0));
                    
                    GuiUtils.getChoiceOptional("Top card", cl.toArray());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility
            
            StringBuilder sb1 = new StringBuilder();
            sb1.append(card.getName()).append(" - look at top card of library.");
            ability1.setStackDescription(sb1.toString());
            
            ability1.setDescription("You may look at the top card of your library.");
            card.addSpellAbility(ability1);
        }//*************** END ************ END **************************
         
        /* Converteded to AF Trigger
        //*************** START *********** START **************************
        else if(cardName.equals("Doomsday Specter")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c)) {
                        AllZone.GameAction.moveToHand(c);
                    }
                }
            };
            Command intoPlay = new Command() {
				private static final long serialVersionUID = -1944407110865125254L;

				public void execute() {
                    CardList creatures = AllZoneUtil.getCreaturesInPlay(card.getController());
                    creatures = creatures.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isBlue() || c.isBlack();
                    	}
                    });
                    
                    AllZone.InputControl.setInput(CardFactoryUtil.input_targetSpecific(ability, creatures,
                            "Select blue or black creature you control.", false, false));
                    ButtonUtil.disableAll();
                    
                }//execute()
            };//Command
            card.addComesIntoPlayCommand(intoPlay);
            
            card.clearSpellAbility();
            card.addSpellAbility(new Spell_Permanent(card) {
				private static final long serialVersionUID = -1885027663323697759L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
            });
        }//*************** END ************ END **************************
        */
        
        //*************** START *********** START **************************
        else if(cardName.equals("Master of the Wild Hunt")) {
        	
        	final Cost abCost = new Cost("T", cardName, true);
        	final Target abTgt = new Target(card,"Target a creature to Hunt", "Creature".split(","));
        	final Ability_Activated ability = new Ability_Activated(card, abCost, abTgt) {
                private static final long serialVersionUID = 35050145102566898L;
                
                @Override
                public boolean canPlayAI() {
                    CardList wolves = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    wolves = wolves.getType("Wolf");
                    
                    wolves = wolves.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isUntapped() && c.isCreature();
                        }
                    });
                    int power = 0;
                    for(int i = 0; i < wolves.size(); i++) 
                    	power += wolves.get(i).getNetAttack();
                    
                    if (power == 0) 
                    	return false;
                    
                    final int totalPower = power;
                    
                    CardList targetables = AllZoneUtil.getPlayerCardsInPlay(AllZone.HumanPlayer);
                    
                    targetables = targetables.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return CardFactoryUtil.canTarget(card, c) && c.isCreature() && c.getNetDefense() <= totalPower;
                        }
                    });
                    
                    if (targetables.size() == 0)
                    	return false;
                    
                    getTarget().resetTargets();
                    setTargetCard(CardFactoryUtil.AI_getBestCreature(targetables));
                    
                	return true;
                }

                @Override
                public void resolve() {
                    CardList wolves = AllZoneUtil.getPlayerCardsInPlay(card.getController());
                    wolves = wolves.getType("Wolf");
                    
                    wolves = wolves.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isUntapped() && c.isCreature();
                        }
                    });

                    final Card target = getTargetCard();   
                    
                    if (wolves.size() == 0)
                    	return;
                    
                    if (!(CardFactoryUtil.canTarget(card, target) && AllZone.GameAction.isCardInPlay(target)))
                    	return;
                    
                    for(Card c : wolves){
                    	c.tap();
                    	target.addDamage(c.getNetAttack(),c);
                    }

                    if (target.getController().isHuman()){	// Human choose spread damage
                         for(int x = 0; x < target.getNetAttack() ; x++) {
                        	 AllZone.InputControl.setInput(CardFactoryUtil.MasteroftheWildHunt_input_targetCreature(this, wolves, new Command() {
                                 private static final long serialVersionUID = -328305150127775L;
                                 
                                 public void execute() {
                                	 getTargetCard().addDamage(1,target);
                                	 AllZone.GameAction.checkStateEffects();
                                 }
                             }));
                        }               
                    }
                    else {		// AI Choose spread Damage
                    	CardList damageableWolves = wolves.filter(new CardListFilter() {
                    		public boolean addCard(Card c) {
                    			return (c.predictDamage(target.getNetAttack(), target, false) > 0);
                    		}
                    	});
                    	
                    	if (damageableWolves.size() == 0)	// don't bother if I can't damage anything
                    		return;
                    	
                    	CardList wolvesLeft = damageableWolves.filter(new CardListFilter() {
                    		public boolean addCard(Card c) {
                    			return !c.hasKeyword("Indestructible");
                    		}
                    	});
                    	
						for (int i = 0; i < target.getNetAttack(); i++) {
							wolvesLeft = wolvesLeft.filter(new CardListFilter() {
	                    		public boolean addCard(Card c) {
	                    			return c.getKillDamage() > 0 && (c.getKillDamage() <= target.getNetAttack() 
	                    					|| target.hasKeyword("Deathtouch"));
	                    		}
	                    	});
							
							// Kill Wolves that can be killed first
							if (wolvesLeft.size() > 0) {
								Card best = CardFactoryUtil.AI_getBestCreature(wolvesLeft);								
								best.addDamage(1, target);
								if (best.getKillDamage() <= 0 || target.hasKeyword("Deathtouch")){
									wolvesLeft.remove(best);
								}
							} 

							else{
								// Add -1/-1s to Random Indestructibles
								if (target.hasKeyword("Infect") || target.hasKeyword("Wither")){
									CardList indestructibles = damageableWolves.filter(new CardListFilter() {
										public boolean addCard(Card c) {
											return c.hasKeyword("Indestructible");
										}
									});
									indestructibles.shuffle();
									indestructibles.get(0).addDamage(1, target);
								}
 
								// Then just add Damage randomnly
	
								else {
									damageableWolves.shuffle();
									wolves.get(0).addDamage(1, target);
								}
							}
						}
					}
                }//resolve()
            };//SpellAbility
            
            StringBuilder sb = new StringBuilder();
            sb.append("Tap: Tap all untapped Wolf creatures you control. Each Wolf tapped ");
            sb.append("this way deals damage equal to its power to target creature. That creature deals ");
            sb.append("damage equal to its power divided as its controller chooses among any number of those Wolves.");
            ability.setDescription(sb.toString());
            
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Scarblade Elite")) {
        	Cost abCost = new Cost("T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, new Target(card,"TgtC")) {
                private static final long serialVersionUID = 3505019464802566898L;
                
                @Override
                public boolean canPlay() {
                	Player controller = card.getController();
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, controller);
                    
                    CardList sins = new CardList(graveyard.getCards());
                    sins = sins.getType("Assassin");
                    
                    if(sins.size() > 0 && AllZone.GameAction.isCardInPlay(card)
                            && CardFactoryUtil.canTarget(card, getTargetCard()) && super.canPlay()) return true;
                    else return false;
                    
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList human = CardFactoryUtil.AI_getHumanCreature(card, true);
                    human = human.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return AllZone.GameAction.isCardInPlay(c);
                        }
                    });
                    
                    CardListUtil.sortAttack(human);
                    CardListUtil.sortFlying(human);
                    
                    //if(0 < human.size())
                    //  setTargetCard(human.get(0));
                    
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);
                    
                    CardList grave = new CardList(graveyard.getCards());
                    grave = grave.getType("Assassin");
                    
                    if(human.size() > 0 && grave.size() > 0) setTargetCard(human.get(0));
                    
                    return 0 < human.size() && 0 < grave.size();
                }
                
                @Override
                public void resolve() {
                	Player controller = card.getController();
                    PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, controller);
                    
                    CardList sins = new CardList(graveyard.getCards());
                    sins = sins.getType("Assassin");
                    
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        Object o = GuiUtils.getChoiceOptional("Select an Assassin to exile", sins.toArray());
                        
                        if(o != null) {
                            Card crd = (Card) o;
                            AllZone.GameAction.exile(crd);
                            
                            Card c = getTargetCard();
                            if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                                AllZone.GameAction.destroy(c);
                            }
                        } //if o!= null
                    }//player.equals("human")
                    else {
                        Card crd = sins.get(0);
                        AllZone.GameAction.exile(crd);
                        
                        Card c = getTargetCard();
                        if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                            AllZone.GameAction.destroy(c);
                        }
                    }
                }//resolve()
            };//SpellAbility
            
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Exile an Assassin card from your graveyard: Destroy target creature.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Gilt-Leaf Archdruid")) {
        	Cost abCost = new Cost("tapXType<7/Druid>", cardName, true);
        	Target tgt = new Target(card,"Select a player to gain lands from", "Player".split(","));
        	final SpellAbility stealLands = new Ability_Activated(card, abCost, tgt){
				private static final long serialVersionUID = 636594487143500891L;

				@Override
        		public boolean canPlayAI(){
        			Player p = AllZone.HumanPlayer;
        			
        			if (!p.canTarget(card))
        				return false;
        			
        			setTargetPlayer(p);
        			
        			CardList lands = AllZoneUtil.getPlayerCardsInPlay(p);
        			lands = lands.getType("Land");
        			
        			// Don't steal lands if Human has less than 2
        			return lands.size() >= 2;
        		}
                
                @Override
                public void resolve() {
                    Player activator = this.getActivatingPlayer();
                    
                    CardList lands = AllZoneUtil.getPlayerCardsInPlay(getTargetPlayer());
                    lands = lands.getType("Land");
                    
                    for(int i = 0; i < lands.size(); i++) {
                        Card land = lands.get(i);
                        if(AllZone.GameAction.isCardInPlay(land)) {	// this really shouldn't fail in the middle of resolution
                            land.setController(activator);
                            
                            // i don't know how the code handles Sum Sickness so I'm leaving this
                            // but a card changing controllers should always gain this no matter if it has haste or not
                            if(land.getKeyword().contains("Haste")) {
                                land.setSickness(false);
                            } else {
                                land.setSickness(true);
                            }
                            
                            AllZone.GameAction.changeController(new CardList(land), land.getController(), card.getController());
                        }//if
                    }
                }
            };
            
            card.addSpellAbility(stealLands);
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.toString()).append(" - Gain control of all lands target player controls.");
            stealLands.setStackDescription(sb.toString());
            
            stealLands.setDescription("Tap seven untapped Druids you control: Gain control of all lands target player controls.");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Figure of Destiny")) {
            Ability ability1 = new Ability(card, "RW") {
                @Override
                public void resolve() {
                    boolean artifact = false;
                    card.setBaseAttack(2);
                    card.setBaseDefense(2);
                    
                    card.removeIntrinsicKeyword("Flying");
                    card.removeIntrinsicKeyword("First Strike");
                    
                    if(card.isArtifact()) artifact = true;
                    
                    card.setType(new ArrayList<String>());
                    if(artifact) card.addType("Artifact");
                    card.addType("Creature");
                    card.addType("Kithkin");
                    card.addType("Spirit");
                }
                
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Spirit") && super.canPlayAI();
                }
                
            };// ability1
            
            ability1.setDescription("RW: Figure of Destiny becomes a 2/2 Kithkin Spirit.");
            ability1.setStackDescription("Figure of Destiny becomes a 2/2 Kithkin Spirit.");
            card.addSpellAbility(ability1);
            

            Ability ability2 = new Ability(card, "RW RW RW") {
                @Override
                public void resolve() {
                    if(card.isType("Spirit")) {
                        boolean artifact = false;
                        card.setBaseAttack(4);
                        card.setBaseDefense(4);
                        
                        card.removeIntrinsicKeyword("Flying");
                        card.removeIntrinsicKeyword("First Strike");
                        
                        if(card.isArtifact()) artifact = true;
                        
                        card.setType(new ArrayList<String>());
                        if(artifact) card.addType("Artifact");
                        card.addType("Creature");
                        card.addType("Kithkin");
                        card.addType("Spirit");
                        card.addType("Warrior");
                    }
                }
                
                @Override
                public boolean canPlay() {
                    return card.isType("Spirit") && super.canPlay();
                }
                
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Warrior") && super.canPlayAI();
                }
                
            };// ability2
            
            ability2.setDescription("RW RW RW: If Figure of Destiny is a Spirit, it becomes a 4/4 Kithkin Spirit Warrior.");
            ability2.setStackDescription("Figure of Destiny becomes a 4/4 Kithkin Spirit Warrior.");
            card.addSpellAbility(ability2);
            

            Ability ability3 = new Ability(card, "RW RW RW RW RW RW") {
                @Override
                public void resolve() {
                    if(card.isType("Warrior")) {
                        boolean artifact = false;
                        card.setBaseAttack(8);
                        card.setBaseDefense(8);
                        
                        card.addIntrinsicKeyword("Flying");
                        card.addIntrinsicKeyword("First Strike");
                        
                        if(card.isArtifact()) artifact = true;
                        
                        card.setType(new ArrayList<String>());
                        if(artifact) card.addType("Artifact");
                        card.addType("Creature");
                        card.addType("Kithkin");
                        card.addType("Spirit");
                        card.addType("Warrior");
                        card.addType("Avatar");
                    }
                }
                
                @Override
                public boolean canPlay() {
                    return card.isType("Warrior") && super.canPlay();
                }
                
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Avatar") && super.canPlayAI();
                }
            };// ability3
            
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append("RW RW RW RW RW RW: If Figure of Destiny is a Warrior, it becomes ");
            sbDesc.append("an 8/8 Kithkin Spirit Warrior Avatar with flying and first strike.");
            ability3.setDescription(sbDesc.toString());
            
            ability3.setStackDescription("Figure of Destiny becomes an 8/8 Kithkin Spirit Warrior Avatar with flying and first strike.");
            card.addSpellAbility(ability3);           
        }//*************** END ************ END **************************
               
                        
        //*************** START *********** START **************************
        else if(cardName.equals("Covetous Dragon")) {
            SpellAbility spell = new Spell_Permanent(card) {
                
                private static final long serialVersionUID = -1446713295855849195L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer).getCards());
                    list = list.getType("Artifact");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Tethered Griffin")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -7872917651421012893L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer).getCards());
                    list = list.getType("Enchantment");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cantivore")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = 7254358703158629514L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer).getCards());
                    list.addAll(AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer).getCards());
                    list = list.getType("Enchantment");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Terravore")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = 7316190829288665283L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer).getCards());
                    list.addAll(AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer).getCards());
                    list = list.getType("Land");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Mortivore")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -7118801410173525870L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer).getCards());
                    list.addAll(AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer).getCards());
                    list = list.getType("Creature");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cognivore")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -2216181341715046786L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer).getCards());
                    list.addAll(AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer).getCards());
                    list = list.getType("Instant");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Magnivore")) {
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -2252263708643462897L;
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(
                            AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer).getCards());
                    list.addAll(AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer).getCards());
                    list = list.getType("Sorcery");
                    return super.canPlayAI() && list.size() > 0;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
               
        //*************** START *********** START **************************
        else if(cardName.equals("Shifting Wall") || cardName.equals("Maga, Traitor to Mortals") || cardName.equals("Feral Hydra")
        		|| cardName.equals("Krakilin") || cardName.equals("Ivy Elemental") || cardName.equals("Lightning Serpent")) { 
        	
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -11489323313L;
                
                @Override
                public boolean canPlayAI() {
                    return super.canPlay() && 4 <= ComputerUtil.getAvailableMana().size() - CardUtil.getConvertedManaCost(card.getManaCost());
                }
            };
            card.clearFirstSpellAbility();
            card.addFirstSpellAbility(spell);
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
        	        getTargetPlayer().loseLife(card.getCounters(Counters.P1P1),card);
                }//resolve()
            };
  
            Command intoPlay = new Command() {
                
                private static final long serialVersionUID = 2559021594L;
                
                public void execute() {
                	int XCounters = card.getXManaCostPaid();
                	if(card.getName().equals("Lightning Serpent")) card.addCounter(Counters.P1P0, XCounters);
                	else card.addCounter(Counters.P1P1, XCounters);
                	if(card.getName().equals("Maga, Traitor to Mortals")) {
                		StringBuilder sb = new StringBuilder();
                		sb.append(ability.getTargetPlayer()).append(" - loses life equal to the number of +1/+1 counters on ").append(card.getName());
                		ability.setStackDescription(sb.toString());
                        
                        if(card.getController() == AllZone.HumanPlayer) AllZone.InputControl.setInput(CardFactoryUtil.input_targetPlayer(ability));
                        else {
                        	ability.setTargetPlayer(AllZone.HumanPlayer);
                        	AllZone.Stack.add(ability);
                        }
                	} 

                }//execute()
            };//Command
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Apocalypse Hydra")) {      
            SpellAbility spell = new Spell_Permanent(card) {
                private static final long serialVersionUID = -11489323313L;
                
                @Override
                public boolean canPlayAI() {
                    return super.canPlay() && 5 <= ComputerUtil.getAvailableMana().size() - 2;
                }
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(spell);

            Command intoPlay = new Command() {
                
                private static final long serialVersionUID = 255901529244894L;
                
                public void execute() {
                	int XCounters = card.getXManaCostPaid();
                	if(XCounters >= 5) XCounters = 2 * XCounters;
                    card.addCounter(Counters.P1P1, XCounters);                   
                }//execute()
            };//Command
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Molten Hydra")) {
        	Target target = new Target(card,"TgtCP");
        	Cost abCost = new Cost("T", cardName, true);
            final Ability_Activated ability2 = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = 2626619319289064289L;
                
                @Override
                public boolean canPlay() {
                    return card.getCounters(Counters.P1P1) > 0 && super.canPlay();
                }
                
                @Override
                public boolean canPlayAI() {
                    return getCreature().size() != 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.HumanPlayer.getLife() < card.getCounters(Counters.P1P1)) setTargetPlayer(AllZone.HumanPlayer);
                    else {
                        CardList list = getCreature();
                        list.shuffle();
                        setTargetCard(list.get(0));
                    }
                }//chooseTargetAI()
                
                CardList getCreature() {

                    //toughness of 1
                    CardList list = CardFactoryUtil.AI_getHumanCreature(card.getCounters(Counters.P1P1), card, true);
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                        	int total = card.getCounters(Counters.P1P1);
                            return (total >= c.getKillDamage());
                        }
                    });
                    return list;
                }//getCreature()
                
                @Override
                public void resolve() {
                	int total = card.getCounters(Counters.P1P1);
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) getTargetCard().addDamage(total,
                                card);
                    } else getTargetPlayer().addDamage(total, card);
                   card.subtractCounter(Counters.P1P1,total);
                }//resolve()
            };//SpellAbility

            card.addSpellAbility(ability2);
            
            StringBuilder sb = new StringBuilder();
            sb.append(abCost+"Remove all +1/+1 counters from "+cardName+":  "+cardName);
            sb.append(" deals damage to target creature or player equal to the number of +1/+1 counters removed this way.");
            ability2.setDescription(sb.toString());
            
            ability2.setStackDescription("Molten Hydra deals damage to number of +1/+1 counters on it to target creature or player.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Academy Rector") || cardName.equals("Lost Auramancers")) {
            final SpellAbility ability = new Ability(card, "0") {
                
                @Override
                public void resolve() {
                    
                    if (card.getController().equals(AllZone.HumanPlayer)) {
                        StringBuilder question = new StringBuilder();
                        if (card.getName().equals("Academy Rector")) {
                            question.append("Exile ").append(card.getName()).append(" and place ");
                        } else {
                            question.append("Place ");
                        }
                        question.append("an enchantment from your library onto the battlefield?");
                        
                        if (GameActionUtil.showYesNoDialog(card, question.toString())) {
                            if (card.getName().equals("Academy Rector")) {
                                AllZone.GameAction.exile(card);
                            }
                            CardList list = AllZoneUtil.getPlayerCardsInLibrary(AllZone.HumanPlayer);
                            list = list.getType("Enchantment");
                            
                            if (list.size() > 0) {
                                Object objectSelected = GuiUtils.getChoiceOptional("Choose an enchantment", list.toArray());
                                
                                if (objectSelected != null) {

                                    final Card c = (Card) objectSelected;
                                    AllZone.GameAction.moveToPlay(c);
                                    
                                    if (c.isAura()) {
                                        
                                        String enchantThisType[] = {""};
                                        String message[] = {""};
                                        
                                        // The type following "Enchant" maybe upercase or lowercase, cardsfolder has both
                                        // Note that I am being overly cautious.
                                        
                                        if (c.getKeyword().contains("Enchant creature without flying") 
                                                || c.getKeyword().contains("Enchant Creature without flying")) {
                                            enchantThisType[0] = "Creature.withoutFlying";
                                            message[0] = "Select a creature without flying";
                                        } else if (c.getKeyword().contains("Enchant creature with converted mana cost 2 or less") 
                                                || c.getKeyword().contains("Enchant Creature with converted mana cost 2 or less")) {
                                            enchantThisType[0] = "Creature.cmcLE2";
                                            message[0] = "Select a creature with converted mana cost 2 or less";
                                        } else if (c.getKeyword().contains("Enchant red or green creature")) {
                                            enchantThisType[0] = "Creature.Red,Creature.Green";
                                            message[0] = "Select a red or green creature";
                                        } else if (c.getKeyword().contains("Enchant tapped creature")) {
                                            enchantThisType[0] = "Creature.tapped";
                                            message[0] = "Select a tapped creature";
                                        } else if (c.getKeyword().contains("Enchant creature") 
                                                || c.getKeyword().contains("Enchant Creature")) {
                                            enchantThisType[0] = "Creature";
                                            message[0] = "Select a creature";
                                        } else if (c.getKeyword().contains("Enchant wall") 
                                                || c.getKeyword().contains("Enchant Wall")) {
                                            enchantThisType[0] = "Wall";
                                            message[0] = "Select a Wall";
                                        } else if (c.getKeyword().contains("Enchant land you control") 
                                                || c.getKeyword().contains("Enchant Land you control")) {
                                            enchantThisType[0] = "Land.YouCtrl";
                                            message[0] = "Select a land you control";
                                        } else if (c.getKeyword().contains("Enchant land") 
                                                || c.getKeyword().contains("Enchant Land")) {
                                            enchantThisType[0] = "Land";
                                            message[0] = "Select a land";
                                        } else if (c.getKeyword().contains("Enchant artifact") 
                                                || c.getKeyword().contains("Enchant Artifact")) {
                                            enchantThisType[0] = "Artifact";
                                            message[0] = "Select an artifact";
                                        } else if (c.getKeyword().contains("Enchant enchantment") 
                                                || c.getKeyword().contains("Enchant Enchantment")) {
                                            enchantThisType[0] = "Enchantment";
                                            message[0] = "Select an enchantment";
                                        }
                                        
                                        CardList allCards = new CardList();
                                        allCards.addAll(AllZone.Human_Battlefield.getCards());
                                        allCards.addAll(AllZone.Computer_Battlefield.getCards());
                                        
                                        // Make sure that we were able to match the selected aura with our list of criteria
                                        
                                        if (enchantThisType[0] != "" && message[0] != "") {
                                        
                                            final CardList choices = allCards.getValidCards(enchantThisType[0], card.getController(), card);
                                            final String msg = message[0];
                                        
                                            AllZone.InputControl.setInput(new Input() {
                                                private static final long serialVersionUID = -6271957194091955059L;

                                                @Override
                                                public void showMessage() {
                                                    AllZone.Display.showMessage(msg);
                                                    ButtonUtil.enableOnlyOK();
                                                }
                                            
                                                @Override
                                                public void selectButtonOK() {
                                                    stop();
                                                }
                                            
                                                @Override
                                                public void selectCard(Card card, PlayerZone zone) {
                                                    if (choices.contains(card)) {
                                                    
                                                        if (AllZone.GameAction.isCardInPlay(card)) {
                                                            c.enchantCard(card);
                                                            stop();
                                                        }
                                                    }
                                                }//selectCard()
                                            });// Input()
                                            
                                        }// if we were able to match the selected aura with our list of criteria
                                    }// If enchantment selected is an aura
                                }// If an enchantment is selected
                            }// If there are enchantments in library
                            
                            card.getController().shuffle();
                        }// If answered yes to may exile
                    }// If player is human
                    
                    // player is the computer
                    else {
                        PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.ComputerPlayer);
                        CardList list = new CardList(lib.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return c.isEnchantment() && !c.isAura();
                            }
                        });
                        
                        if (list.size() > 0) {
                            Card c = CardFactoryUtil.AI_getBestEnchantment(list, card, false);

                            AllZone.GameAction.moveToPlay(c);
                            if (card.getName().equals("Academy Rector")) {
                                AllZone.GameAction.exile(card);
                            }
                            card.getController().shuffle();
                        }
                    }// player is the computer
                }// resolve()
            };// ability
            
            StringBuilder sb = new StringBuilder();
            if (card.getName().equals("Academy Rector")) {
                sb.append("Academy Rector - ").append(card.getController());
                sb.append(" may exile this card and place an enchantment from his library onto the battlefield.");
            } else {
                sb.append("Lost Auramancers - ").append(card.getController());
                sb.append(" may place an enchantment from his library onto the battlefield.");
            }
            ability.setStackDescription(sb.toString());
            
            final Command destroy = new Command() {
                private static final long serialVersionUID = -4352349741511065318L;
                public void execute() {
                    
                    if (card.getName().equals("Lost Auramancers") 
                            && card.getCounters(Counters.TIME) <= 0) {
                        AllZone.Stack.add(ability);
                    } else if (card.getName().equals("Academy Rector")) {
                        AllZone.Stack.add(ability);
                    }
                    
                }// execute()
            };// Command destroy
            
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Deadly Grub")) {
            final Command destroy = new Command() {
                private static final long serialVersionUID = -4352349741511065318L;
                
                public void execute() {
                    if(card.getCounters(Counters.TIME) <= 0) CardFactoryUtil.makeToken("Insect", "G 6 1 Insect",
                    		card.getController(), "G", new String[] {"Creature", "Insect"}, 6, 1, new String[] {"Shroud"});
                }
            };
            
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Vendilion Clique")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

                    CardList list = new CardList(hand.getCards());
                    CardList nonLandList = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return !c.isLand();
                        }
                    });
                    
                    if(list.size() > 0) {
                        if(card.getController().equals(AllZone.HumanPlayer)) {
                            GuiUtils.getChoiceOptional("Revealing hand", list.toArray());
                            if(nonLandList.size() > 0) {
                                Object o = GuiUtils.getChoiceOptional("Select nonland card",
                                        nonLandList.toArray());
                                if(o != null) {
                                    Card c = (Card) o;
                                	AllZone.GameAction.moveToBottomOfLibrary(c);

                                    player.drawCard();
                                }
                            }
                        } else //comp
                        {
                            if(AllZone.Phase.getTurn() >= 12 && nonLandList.size() > 0) {
                                Card c = CardFactoryUtil.AI_getMostExpensivePermanent(nonLandList, card, false);
                                AllZone.GameAction.moveToBottomOfLibrary(c);
                                AllZone.HumanPlayer.drawCard();
                            }
                        }
                    }//handsize > 0
                    
                }
            };
            Command intoPlay = new Command() {
                private static final long serialVersionUID = -5052568979553782714L;
                
                public void execute() {
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        AllZone.InputControl.setInput(CardFactoryUtil.input_targetPlayer(ability));
                        ButtonUtil.disableAll();
                    } else if(card.getController().equals(AllZone.ComputerPlayer)) {
                        ability.setTargetPlayer(AllZone.HumanPlayer);
                        AllZone.Stack.add(ability);
                    }
                }//execute()
            };//Command
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Sygg, River Guide")) {
            final HashMap<Card, String[]> creatureMap = new HashMap<Card, String[]>();
            
            final Ability ability = new Ability(card, "1 W") {
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    String color = "";
                    if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        
                        Object o = GuiUtils.getChoice("Choose mana color", Constant.Color.onlyColors);
                        color = (String) o;
                        c.addExtrinsicKeyword("Protection from " + color);
                        if(creatureMap.containsKey(c)) {
                            int size = creatureMap.get(c).length;
                            String[] newString = new String[size + 1];
                            
                            for(int i = 0; i < size; i++) {
                                newString[i] = creatureMap.get(c)[i];
                            }
                            newString[size] = color;
                            creatureMap.put(c, newString);
                        } else creatureMap.put(c, new String[] {color});
                        
                        final Card crd = c;
                        final Command atEOT = new Command() {
                            private static final long serialVersionUID = 8630868536866681014L;
                            
                            public void execute() {
                                //if(AllZone.GameAction.isCardInPlay(c))
                                //  c.removeExtrinsicKeyword("Protection from "+color);
                                if(AllZone.GameAction.isCardInPlay(crd)) {
                                    String[] colors = creatureMap.get(crd);
                                    for(String col:colors) {
                                        crd.removeExtrinsicKeyword("Protection from " + col);
                                    }
                                }
                            }
                        };//Command
                        AllZone.EndOfTurn.addUntil(atEOT);
                    }
                }
            };
            Input runtime = new Input() {
                private static final long serialVersionUID = -2171146532836387392L;
                
                @Override
                public void showMessage() {
                    CardList creats = new CardList(
                            AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards());
                    creats = creats.getType("Merfolk");
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(ability, creats, "Select a target Merfolk",
                            true, false));
                }
            };
            ability.setDescription("1 W: Target Merfolk you control gains protection from the color of your choice until end of turn.");
            ability.setBeforePayMana(runtime);
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Laquatus's Champion")) {
            final SpellAbility abilityComes = new Ability(card, "0") {
                @Override
                public void resolve() {
                    getTargetPlayer().loseLife(6,card);
                }//resolve()
            };
            
            final Input inputComes = new Input() {
                private static final long serialVersionUID = -2666229064706311L;
                
                @Override
                public void showMessage() {
                    stopSetNext(CardFactoryUtil.input_targetPlayer(abilityComes));
                    ButtonUtil.disableAll();//to disable the Cancel button
                }
            };
            Command commandComes = new Command() {
                private static final long serialVersionUID = -4246229185669164581L;
                
                public void execute() {
                    if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(inputComes);
                    else //computer
                    {
                        abilityComes.setTargetPlayer(AllZone.HumanPlayer);
                        AllZone.Stack.add(abilityComes);
                    }//else
                }//execute()
            };//CommandComes
            Command commandLeavesPlay = new Command() {
                
                private static final long serialVersionUID = 9172348861441804625L;
                
                public void execute() {
                    //System.out.println(abilityComes.getTargetCard().getName());
                    
                    SpellAbility ability = new Ability(card, "0") {
                        @Override
                        public void resolve() {
                            abilityComes.getTargetPlayer().gainLife(6, card);
                            
                        }//resolve()
                    };//SpellAbility
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("Laquatus's Champion - ").append(abilityComes.getTargetPlayer()).append(" regains 6 life.");
                    ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }//execute()
            };//Command
            
            card.addComesIntoPlayCommand(commandComes);
            card.addLeavesPlayCommand(commandLeavesPlay);
            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Meddling Mage")) {
            final String[] input = new String[1];
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        input[0] = JOptionPane.showInputDialog(null, "Which card?", "Pick card",
                                JOptionPane.QUESTION_MESSAGE);
                        card.setNamedCard(input[0]);
                    } else {
                        String s = "Ancestral Recall";
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
                        PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.HumanPlayer);
                        
                        CardList list = new CardList();
                        list.addAll(hand.getCards());
                        list.addAll(lib.getCards());
                        list = list.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                return !c.isLand() && !c.isUnCastable();
                            }
                        });
                        
                        if(list.size() > 0) {
                            CardList rare;
                            rare = list;
                            rare = rare.filter(new CardListFilter() {
                                public boolean addCard(Card c) {
                                    return c.getRarity().equals("Rare");
                                }
                            });
                            
                            if(rare.size() > 0) {
                                s = rare.get(CardUtil.getRandomIndex(rare)).getName();
                            } else {
                                Card c = list.get(CardUtil.getRandomIndex(list));
                                //System.out.println(c + " - " + c.getRarity());
                                s = c.getName();
                            }
                        }
                        
                        card.setNamedCard(s);
                        
                    }
                    
                }
            };
            Command comesIntoPlay = new Command() {
                private static final long serialVersionUID = 8485080996453793968L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };//Command
            ability.setStackDescription("As Meddling Mage enters the battlefield, name a nonland card.");
            card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Iona, Shield of Emeria")) {
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(card.getController().equals(AllZone.HumanPlayer)) {
                        
                        String color = "";
                        String[] colors = Constant.Color.Colors;
                        colors[colors.length - 1] = null;
                        
                        Object o = GuiUtils.getChoice("Choose color", colors);
                        color = (String) o;
                        card.setChosenColor(color);
                    } else {
                        PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.HumanPlayer);
                        PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
                        CardList list = new CardList();
                        list.addAll(lib.getCards());
                        list.addAll(hand.getCards());
                        
                        if(list.size() > 0) {
                            String color = CardFactoryUtil.getMostProminentColor(list);
                            if(!color.equals("")) card.setChosenColor(color);
                            else card.setChosenColor("black");
                        } else {
                            card.setChosenColor("black");
                        }
                    }
                }
            };
            Command comesIntoPlay = new Command() {
                private static final long serialVersionUID = 3331342605626623161L;
                
                public void execute() {
                    AllZone.Stack.add(ability);
                }
            };//Command
            ability.setStackDescription("As Iona, Shield of Emeria enters the battlefield, choose a color.");
            card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Chainer, Dementia Master")) {
            final Ability ability = new Ability(card, "B B B") {
                @Override
                public void resolve() {
                    card.getController().loseLife(3,card);
                    
                    PlayerZone hGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer);
                    PlayerZone cGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);

                    CardList creatures = new CardList();
                    creatures.addAll(hGrave.getCards());
                    creatures.addAll(cGrave.getCards());
                    

                    creatures = creatures.getType("Creature");
                    if(creatures.size() > 0) {
                        if(card.getController().equals(AllZone.HumanPlayer)) {
                            Object o = GuiUtils.getChoice("Pick creature: ", creatures.toArray());
                            if(o != null) {
                                Card c = (Card) o;
                                c.setController(card.getController());
                                c.addExtrinsicKeyword(c.getName() + " is black.");
                                c.addType("Nightmare");
                                AllZone.GameAction.moveToPlay(c, card.getController());
                            }
                        } else {
                            Card c = CardFactoryUtil.AI_getBestCreature(creatures);
                            c.setController(card.getController());
                            c.addExtrinsicKeyword(c.getName() + " is black.");
                            c.addType("Nightmare");
                            AllZone.GameAction.moveToPlay(c, card.getController());
                        }
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.ComputerPlayer.getLife() < 7) return false;
                    
                    PlayerZone hGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer);
                    PlayerZone cGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);
                    CardList creatures = new CardList();
                    creatures.addAll(hGrave.getCards());
                    creatures.addAll(cGrave.getCards());
                    creatures = creatures.getType("Creature");
                    return creatures.size() > 0;
                }
                
                @Override
                public boolean canPlay() {
                    PlayerZone hGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.HumanPlayer);
                    PlayerZone cGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);
                    CardList creatures = new CardList();
                    creatures.addAll(hGrave.getCards());
                    creatures.addAll(cGrave.getCards());
                    creatures = creatures.getType("Creature");
                    return creatures.size() > 0 && super.canPlay();
                }
            };
            
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append("B B B, Pay 3 life: Put target creature card from a graveyard onto the battlefield under your control. ");
            sbDesc.append("That creature is black and is a Nightmare in addition to its other creature types.");
            ability.setDescription(sbDesc.toString());
            
            StringBuilder sbStack = new StringBuilder();
            sbStack.append(card).append("Put target creature card from a graveyard onto the battlefield under your control. ");
            sbStack.append("That creature is black and is a Nightmare in addition to its other creature types.");
            ability.setStackDescription(sbStack.toString());
            
            card.addSpellAbility(ability);
            
            final Command leavesPlay = new Command() {
                private static final long serialVersionUID = 3367460511478891560L;
                
                public void execute() {
                    PlayerZone hPlay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
                    PlayerZone cPlay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    
                    CardList list = new CardList();
                    list.addAll(hPlay.getCards());
                    list.addAll(cPlay.getCards());
                    list = list.getType("Nightmare");
                    
                    for(Card c:list) {
                        AllZone.GameAction.exile(c);
                    }
                }
            };
            
            card.addLeavesPlayCommand(leavesPlay);
        }//*************** END ************ END **************************
               
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sprouting Phytohydra")) {
            final Card newCard = new Card() {
                @Override
                public void addDamage(HashMap<Card, Integer> map) {
                	final HashMap<Card, Integer> m = map;
                    final Ability ability = new Ability(card, "0") {
                    	@Override
                    	public void resolve() {
                    		if(getController().isHuman() &&
                    				GuiUtils.getChoice("Copy " + getSourceCard(),
                    						new String[] {"Yes", "No"}).equals("No"))
                    			return;
                    		PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, getSourceCard().getController());
                    		CardList DoublingSeasons = new CardList(play.getCards());
                    		DoublingSeasons = DoublingSeasons.getName("Doubling Season");
                    		PlayerZone_ComesIntoPlay.setSimultaneousEntry(true);      
                    		double Count = DoublingSeasons.size();
                    		Count = Math.pow(2,Count);
                    		for(int i = 0; i < Count; i++) {
                    			if(i + 1 == Count) PlayerZone_ComesIntoPlay.setSimultaneousEntry(false);                 
                    			Card Copy = AllZone.CardFactory.copyCardintoNew(getSourceCard());
                    			Copy.setToken(true);
                    			Copy.setController(getSourceCard().getController());

                    			AllZone.GameAction.moveToPlay(Copy);
                    		}
                    	}
                    };
                    ability.setStackDescription(toString() + " - you may put a token that's a copy of " + getName() + " onto the battlefield.");
                    AllZone.Stack.add(ability);
                    
                    for(Entry<Card, Integer> entry : m.entrySet()) {
                        this.addDamage(entry.getValue(), entry.getKey());
                    }
                    
                }
            };
            
            newCard.setOwner(card.getOwner());
            newCard.setController(card.getController());
            
            newCard.setManaCost(card.getManaCost());
            newCard.setName(card.getName());
            newCard.setType(card.getType());
            newCard.setText(card.getSpellText());
            newCard.setBaseAttack(card.getBaseAttack());
            newCard.setBaseDefense(card.getBaseDefense());
            
            newCard.addSpellAbility(new Spell_Permanent(newCard));
            
            newCard.setSVars(card.getSVars());
            newCard.setSets(card.getSets());
            
            return newCard;
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Thoughtcutter Agent")) {
        	Target target = new Target(card,"Select target player", new String[] {"Player"});
        	Cost abCost = new Cost("U B T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
                private static final long serialVersionUID = -3880035465617987801L;
                
                @Override
                public void resolve() {
                	Player player = getTargetPlayer();
                    CardList hand = AllZoneUtil.getPlayerHand(player);
                    player.loseLife(1, card);
                    if(player.equals(AllZone.ComputerPlayer)) {
                        GuiUtils.getChoice("Look", hand.toArray());
                    }
                    
                }
                
                @Override
                public boolean canPlayAI() {
                    //computer should play ability if this creature doesn't attack
                    Combat c = ComputerUtil.getAttackers();
                    CardList list = new CardList(c.getAttackers());
                    
                    //could this creature attack?, if attacks, do not use ability
                    return (!list.contains(card));
                }
            };//SpellAbility
            ability.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Target player loses 1 life and reveals his or her hand.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - target player loses 1 life.");
            ability.setStackDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Singe-Mind Ogre")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    Card choice = null;
                    Player opponent = card.getController().getOpponent();
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, opponent);
                    Card[] handChoices = hand.getCards();
                    if (handChoices.length > 0)
                    {
	                    choice = CardUtil.getRandom(handChoices);
	                    handChoices[0] = choice;
	                    for(int i = 1; i < handChoices.length; i++) {
	                        handChoices[i] = null;
	                    }
	                    GuiUtils.getChoice("Random card", handChoices);
	                    opponent.loseLife(CardUtil.getConvertedManaCost(choice.getManaCost()),card);
                    }
                }//resolve()
            };
            Command intoPlay = new Command() {
                
                private static final long serialVersionUID = -4833144157620224716L;
                
                public void execute() {
                    ability.setStackDescription("Singe-Mind Ogre - target player reveals a card at random from " +
                    		"his or her hand, then loses life equal to that card's converted mana cost.");
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Chronatog")) {
            
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = 6926430725410883578L;
                
                public void execute() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        card.addTempAttackBoost(-3);
                        card.addTempDefenseBoost(-3);
                    }
                }
            };
            
            Cost abCost = new Cost("0", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = -8345060615720565828L;

				@Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        card.addTempAttackBoost(3);
                        card.addTempDefenseBoost(3);
                        AllZone.EndOfTurn.addUntil(untilEOT);
                        
                        AllZone.Phase.skipTurn(card.getController());
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };
            ability.getRestrictions().setActivationLimit(1);
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" gets +3/+3 until end of turn, ");
            sb.append(card.getController()).append(" skips his/her next turn.");
            ability.setStackDescription(sb.toString());
            
            ability.setDescription("0: Chronatog gets +3/+3 until end of turn. You skip your next turn. Activate this ability only once each turn.");
            
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Kinsbaile Borderguard")) {
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounter(Counters.P1P1, countKithkin());
                    //System.out.println("all counters: " +card.sumAllCounters());
                }//resolve()
                
                public int countKithkin() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    CardList kithkin = new CardList(play.getCards());
                    kithkin = kithkin.filter(new CardListFilter() {
                        
                        public boolean addCard(Card c) {
                            return (c.getType().contains("Kithkin") || c.getKeyword().contains("Changeling"))
                                    && !c.equals(card);
                        }
                        
                    });
                    return kithkin.size();
                    
                }
            };
            Command intoPlay = new Command() {
                private static final long serialVersionUID = -7067218066522935060L;
                
                public void execute() {
                    ability.setStackDescription("Kinsbaile Borderguard enters the battlefield with a +1/+1 counter on it for each other Kithkin you control.");
                    AllZone.Stack.add(ability);
                }
            };
            
            final SpellAbility ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    for(int i = 0; i < card.sumAllCounters(); i++) {
                        makeToken();
                    }
                }//resolve()
                
                public void makeToken() {
                    CardFactoryUtil.makeToken("Kithkin Soldier", "W 1 1 Kithkin Soldier", card.getController(), "W", new String[] {
                            "Creature", "Kithkin", "Soldier"}, 1, 1, new String[] {""});
                }
            };
            
            Command destroy = new Command() {
                private static final long serialVersionUID = 304026662487997331L;
                
                public void execute() {
                    ability2.setStackDescription("When Kinsbaile Borderguard is put into a graveyard from play, put a 1/1 white " +
                    		"Kithkin Soldier creature token onto the battlefield for each counter on it.");
                    AllZone.Stack.add(ability2);
                }
            };
            
            card.addComesIntoPlayCommand(intoPlay);
            card.addDestroyCommand(destroy);
            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Arctic Nishoba")) {
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    int lifeGain = card.getCounters(Counters.AGE) * 2;
                    card.getController().gainLife(lifeGain, card);
                }
            };
            
            Command destroy = new Command() {
                private static final long serialVersionUID = 1863551466234257411L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - gain 2 life for each age counter on it.");
                	ability.setStackDescription(sb.toString());
                    
                    AllZone.Stack.add(ability);
                }
            };//command
            
            card.addDestroyCommand(destroy);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START ************************** 
        else if(cardName.equals("Kavu Titan")) {
            final SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = -1598664196463358630L;
                
                @Override
                public void resolve() {
                    card.setKicked(true);
                    AllZone.GameAction.moveToPlay(card);
                }
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && AllZone.Phase.getPlayerTurn().equals(card.getController())
                            && !AllZone.Phase.getPhase().equals("End of Turn")
                            && !AllZone.GameAction.isCardInPlay(card);
                }
                
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("3 G G");
            kicker.setAdditionalManaCost("2 G");
            kicker.setDescription("Kicker 2 G");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - Creature 5/5 (Kicked)");
            kicker.setStackDescription(sb.toString());
            
            card.addSpellAbility(kicker);
            
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounter(Counters.P1P1, 3);
                    card.addIntrinsicKeyword("Trample");

                	card.setKicked(false);
                }
            };
            
            Command commandComes = new Command() {
                private static final long serialVersionUID = -2622859088591798773L;
                
                public void execute() {
                    if(card.isKicked()) {
                            ability.setStackDescription("Kavu Titan gets 3 +1/+1 counters and gains trample.");
                            AllZone.Stack.add(ability);
                    }
                }//execute()
            };//CommandComes
            
            card.addComesIntoPlayCommand(commandComes);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START ************************** 
        else if(cardName.equals("Gatekeeper of Malakir")) {
            final SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = -1598664186463358630L;
                
                @Override
                public void resolve() {
                    card.setKicked(true);
                    AllZone.GameAction.moveToPlay(card);
                }
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && AllZone.Phase.getPlayerTurn().equals(card.getController())
                            && !AllZone.Phase.getPhase().equals("End of Turn")
                            && !AllZone.GameAction.isCardInPlay(card);
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
                    CardList cl = new CardList(play.getCards());
                    cl = cl.getType("Creature");
                    
                    return cl.size() > 0;
                }
                
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("B B B");
            kicker.setAdditionalManaCost("B");
            kicker.setDescription("Kicker B");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - Creature 2/2 (Kicked)");
            kicker.setStackDescription(sb.toString());
            
            card.addSpellAbility(kicker);
            
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                	if (card.getController().equals(AllZone.ComputerPlayer))
                		setTargetPlayer(AllZone.HumanPlayer);
                	getTargetPlayer().sacrificeCreature();

                	card.setKicked(false);
                }
            };
            
            Command commandComes = new Command() {
                private static final long serialVersionUID = -2622859088591798773L;
                
                public void execute() {
                    if(card.isKicked()) {
                        if(card.getController().equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(CardFactoryUtil.input_targetPlayer(ability));
                        else //computer
                        {
                            ability.setStackDescription("Gatekeeper of Malakir - targeting Human");
                            AllZone.Stack.add(ability);
                        }//else
                    }
                }//execute()
            };//CommandComes
            
            card.addComesIntoPlayCommand(commandComes);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Gnarlid Pack") || cardName.equals("Apex Hawks") || cardName.equals("Enclave Elite") || 
                cardName.equals("Quag Vampires") || cardName.equals("Skitter of Lizards") ||
                cardName.equals("Joraga Warcaller"))
        {
        	final Ability_Static ability = new Ability_Static(card, "0") {
                @Override
                public void resolve() {
                    card.addCounter(Counters.P1P1, card.getMultiKickerMagnitude());
                    card.setMultiKickerMagnitude(0);
                }
            };
            StringBuilder sb = new StringBuilder();
            sb.append(cardName);
            sb.append(" enters the battlefield with a +1/+1 counter on it for each time it was kicked.");
            ability.setStackDescription(sb.toString());
            
            final Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = 4245563898487609274L;

				public void execute() {
                    AllZone.Stack.add(ability);
                }
            };
            card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************

                
        
        //*************** START *********** START **************************
        else if(cardName.equals("Venerated Teacher")) {
        	/*
        	 * When Venerated Teacher enters the battlefield, put two level counters
        	 * on each creature you control with level up.
        	 */
        	final Ability ability = new Ability(card, "0") {
        		
        		@Override
        		public void resolve() {
        			PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
        			CardList level = new CardList(play.getCards());
        			level = level.filter(new CardListFilter() {
        				public boolean addCard(Card c) {
        					return c.hasLevelUp();
        				}
        			});
        			for( int i = 0; i < level.size(); i++ ) {
        				Card c = level.get(i);
        				c.addCounter(Counters.LEVEL, 2);
        			}
        		}//resolve()
        	};//Ability

        	Command addLevelCounters = new Command() {
				private static final long serialVersionUID = 1919112942772054206L;

				public void execute() {
					StringBuilder sb = new StringBuilder();
					sb.append(card.getName()).append(" - Add 2 Level counters to each creature you control with Level up.");
					ability.setStackDescription(sb.toString());
        			
        			AllZone.Stack.add(ability);
        		}
        	};
        	card.addComesIntoPlayCommand(addLevelCounters);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Tribal Forcemage")) {
        	/*
        	 * Morph: 1G
        	 * When Tribal Forcemage is turned face up, creatures of the creature
        	 * type of your choice get +2/+2 and gain trample until end of turn.
        	 */
        	final Command turnsFaceUp = new Command() {
				private static final long serialVersionUID = 2826741404979610245L;

				public void execute() {
        			final int pump = 2;
        			final Command eot = new Command() {
        				private static final long serialVersionUID = -3638246921594162776L;

        				public void execute() {
        					CardList type = AllZoneUtil.getCardsInPlay();
        					type = type.getType(card.getChosenType());

        					for(int i = 0; i < type.size(); i++) {
        						Card c = type.get(i);
        						c.addTempAttackBoost(-pump);
        						c.addTempDefenseBoost(-pump);
        						c.removeExtrinsicKeyword("Trample");
        					}
        					card.setChosenType(null);
        				}
        			};
        			final SpellAbility ability = new Ability(card, "0") {
        				@Override
        				public void resolve() {
        					String chosenType = "";
        					if(card.getController().equals(AllZone.HumanPlayer)) {
        						chosenType = JOptionPane.showInputDialog(null, "Select a card type:", card.getName(),
        								JOptionPane.QUESTION_MESSAGE);
        					}
        					else {
        						//TODO - this could probably be updated to get the most prominent type in play
        						chosenType = "Elf";
        					}
        					card.setChosenType(chosenType);
        					CardList type = AllZoneUtil.getCardsInPlay();
        					type = type.getType(chosenType);
        					for(int i = 0; i < type.size(); i++) {
        						Card c = type.get(i);
        						c.addTempAttackBoost(pump);
        						c.addTempDefenseBoost(pump);
        						c.addExtrinsicKeyword("Trample");
        					}
        					AllZone.EndOfTurn.addUntil(eot);
        				}
        			};//SpellAbility
        			
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName()).append(" - chosen type gets +2/+2 and Trample until EOT");
        			ability.setStackDescription(sb.toString());
        			
        			AllZone.Stack.add(ability);
        		}//execute
        	};//command

        	card.addTurnFaceUpCommand(turnsFaceUp);
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Storm Entity")) {
        	final SpellAbility intoPlay = new Ability(card, "0") {
        		
        		@Override
        		public boolean canPlayAI() {
    			CardList human = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
                CardListUtil.sortAttack(human);
        		 return (human.get(0).getNetAttack() < Phase.getStormCount() && Phase.getStormCount() > 1);
        		}
        		@Override
        		public void resolve() {
                    for(int i = 0; i < Phase.getStormCount() - 1; i++) {
                        card.addCounter(Counters.P1P1, 1);
        		    }  
        		}
        	};//SpellAbility
        	
        	Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = -3734151854295L;

				public void execute() {
        			AllZone.Stack.add(intoPlay);
        		}
        	};
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(cardName).append(" - enters the battlefield with a +1/+1 counter on it for each other spell played this turn.");
        	intoPlay.setStackDescription(sb.toString());
        	
        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END ************************** 
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dawnglare Invoker")) {
        	/*
        	 * 8: Tap all creatures target player controls.
        	 */
        	final SpellAbility ability = new Ability(card, "8") {
        		@Override
        		public boolean canPlayAI() {
        			CardList human = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
        			human = human.filter(AllZoneUtil.tapped);
        			return human.size() > 0 && AllZone.Phase.getPhase().equals("Main1");
        		}
        		@Override
        		public void resolve() {
        			final Player player = getTargetPlayer();
        			CardList creatures = AllZoneUtil.getCreaturesInPlay(player);
        			for(Card c:creatures) {
        				if( c.isUntapped() ) {
        					c.tap();
        				}
        			}
        		}   
        	};
        	card.addSpellAbility(ability);
        	ability.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
        	ability.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability));
        }//*************** END ************ END **************************
        
        
         //*************** START *********** START **************************
        else if(cardName.equals("Arc-Slogger")) {
        	/*
        	 * R, Exile the top ten cards of your library: Arc-Slogger deals
        	 * 2 damage to target creature or player.
        	 */
        	final SpellAbility ability = new Ability(card, "R") {
        		@Override
        		public boolean canPlayAI() {
        			PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.ComputerPlayer);
        			int life = AllZone.HumanPlayer.getLife();
        			if(lib.size() > 10 && life <=2) {
        				return true;
        			}
        			else{
        				return false;
        			}
        		}
        		@Override
        		public void resolve() {
        			int damage = 2;
        			PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
        			int max = Math.min(lib.size(), 10);
        			for(int i = 0; i < max; i++) {
        				//remove the top card 10 times
        				AllZone.GameAction.exile(lib.get(0));
        			}
        			if(getTargetCard() != null) {
        				if(AllZone.GameAction.isCardInPlay(getTargetCard())
        						&& CardFactoryUtil.canTarget(card, getTargetCard())) {
        					getTargetCard().addDamage(damage, card);
        				}
        			} else getTargetPlayer().addDamage(damage, card);

        		}   
        	};
        	card.addSpellAbility(ability);
        	ability.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(ability, true, false));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Vampire Hexmage")) {
        	/*
        	 * Sacrifice Vampire Hexmage: Remove all counters from target permanent.
        	 */
        	final SpellAbility ability = new Ability(card, "0") {

        		@Override
        		public boolean canPlayAI() {
        			
        			//Dark Depths:
        			CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer, "Dark Depths");
        			list = list.filter(new CardListFilter(){
        				public boolean addCard(Card crd)
        				{
        					return crd.getCounters(Counters.ICE) >= 3;
        				}
        			});
        			
        			if (list.size()>0)
        			{
        				setTargetCard(list.get(0));
        				return true;
        			}
        			
        			//Get rid of Planeswalkers:
        			list = AllZoneUtil.getPlayerCardsInPlay(AllZone.HumanPlayer);
        			list = list.filter(new CardListFilter(){
        				public boolean addCard(Card crd)
        				{
        					return crd.isPlaneswalker() && crd.getCounters(Counters.LOYALTY) >= 5;
        				}
        			});
        			
        			if (list.size()>0)
        			{
        				setTargetCard(list.get(0));
        				return true;
        			}
        			
        			return false;
        		}
        		
        		@Override
        		public boolean canPlay() {
        			return AllZoneUtil.isCardInPlay(card) && super.canPlay();
        		}

        		@Override
        		public void resolve() {
        			final Card c = getTargetCard();
        			for(Counters counter:Counters.values()) {
        				if(c.getCounters(counter) > 0) {
        					c.setCounter(counter, 0, false);
        				}
        			}
        			AllZone.GameAction.sacrifice(card);
        		}
        	};
        	card.addSpellAbility(ability);
        	ability.setBeforePayMana(CardFactoryUtil.input_targetPermanent(ability));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Kargan Dragonlord"))
        {
        	Cost abCost = new Cost("R", cardName, true);
        	
	        final SpellAbility ability = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = -2252408767635375616L;

				@Override
	            public boolean canPlayAI() {
	                
	                if(AllZone.Phase.getPhase().equals(Constant.Phase.Main2)) return false;
	                
	                setTargetCard(card);
	                    
	                if((card.hasSickness() && (!card.getKeyword().contains("Haste"))) ) return false;
	                else {
	                	Random r = new Random();
	                    if(r.nextFloat() <= Math.pow(.6667, card.getAbilityUsed())) return CardFactoryUtil.AI_doesCreatureAttack(card);
	                }
	                return false;
	            }
	            
	            @Override
	            public boolean canPlay() {
	                return card.getCounters(Counters.LEVEL) >= 8 && super.canPlay();
	            }
	            
	            @Override
	            public void resolve() {
	                if(AllZone.GameAction.isCardInPlay(getTargetCard())) {
	                    final Card[] creature = new Card[1];
	                    creature[0] = card;
	                    
	                    final Command EOT = new Command() {
	                        
							private static final long serialVersionUID = 3161373279207630319L;

							public void execute() {
	                            if(AllZone.GameAction.isCardInPlay(creature[0])) {
	                                creature[0].addTempAttackBoost(-1);
	                            }
	                        }
	                    };
	                    
	                    creature[0].addTempAttackBoost(1);

	                    card.setAbilityUsed(card.getAbilityUsed() + 1);
	                    AllZone.EndOfTurn.addUntil(EOT);
	                }//if (card is in play)
	            }//resolve()
	        };//SpellAbility
	        
	        ability.setDescription("R: Kargan Dragonlord gets +1/+0 until end of turn. (LEVEL 8+)");
	        //ability.setStackDescription(stDesc[0]);
	        
	        ability.setTargetCard(card);
	        card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
          else if(cardName.equals("Sutured Ghoul")) {
        	  final int[] numCreatures = new int[1];
        	  final int[] sumPower = new int[1];
        	  final int[] sumToughness = new int[1];

        	  Command intoPlay = new Command() {
        		  private static final long serialVersionUID = -75234586897814L;

        		  public void execute() {
        			  int intermSumPower,intermSumToughness;
        			  intermSumPower = intermSumToughness = 0;
        			  PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
        			  CardList creats = new CardList(grave.getCards());
        			  creats = creats.filter(new CardListFilter() {
        				  public boolean addCard(Card c) {
        					  return c.isCreature() && !c.equals(card);
        				  }
        			  });

        			  if(card.getController().equals(AllZone.HumanPlayer)) {
        				  if (creats.size() > 0)
        				  {
        					  List<Card> selection = GuiUtils.getChoicesOptional("Select creatures to sacrifice", creats.toArray());

        					  numCreatures[0] = selection.size();
        					  for(int m = 0; m < selection.size(); m++) {
        						  intermSumPower += selection.get(m).getBaseAttack();
        						  intermSumToughness += selection.get(m).getBaseDefense();
        						  AllZone.GameAction.exile(selection.get(m));
        					  }
        				  }

        			  }//human
        			  else {
        				  int count = 0;
        				  for(int i = 0; i < creats.size(); i++) {
        					  Card c = creats.get(i);
        					  if(c.getNetAttack() <= 2 && c.getNetDefense() <= 3) {
        						  intermSumPower += c.getBaseAttack();
        						  intermSumToughness += c.getBaseDefense();
        						  AllZone.GameAction.exile(c);
        						  count++;
        					  }
        					  //is this needed?
        					  AllZone.Computer_Battlefield.updateObservers();
        				  }
        				  numCreatures[0] = count;
        			  }
        			  sumPower[0] = intermSumPower;
        			  sumToughness[0] = intermSumToughness;
        			  card.setBaseAttack(sumPower[0]);
        			  card.setBaseDefense(sumToughness[0]);
        		  }
        	  };
              // Do not remove SpellAbilities created by AbilityFactory or Keywords.
        	  card.clearFirstSpellAbility();
        	  card.addComesIntoPlayCommand(intoPlay);
        	  card.addSpellAbility(new Spell_Permanent(card) {
        		  private static final long serialVersionUID = 304885517082977723L;

        		  @Override
        		  public boolean canPlayAI() {
        			  //get all creatures
        			  CardList list = AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer);
        			  list = list.filter(AllZoneUtil.creatures);
        			  return 0 < list.size();
        		  }
        	  });
          }//*************** END ************ END **************************


        //*************** START *********** START **************************
        else if(cardName.equals("Singing Tree")) {
            final String Tgts[] = {"Creature.attacking"};
            Target target = new Target(card,"Select target attacking creature.", Tgts);
          
            final Cost cost = new Cost("T", card.getName(), true);

            final SpellAbility ability = new Ability_Activated(card, cost, target) {
				private static final long serialVersionUID = 3750045284339229395L;

				@Override
                public boolean canPlayAI() {
                    Card c = getCreature();
                    if(c == null) return false;
                    else {
                        setTargetCard(c);
                        return true;
                    }
                }//canPlayAI()
                
                //may return null
                public Card getCreature() {
                    CardList attacking = AllZoneUtil.getCreaturesInPlay();
                    attacking = attacking.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isAttacking() && c != card && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    if(attacking.isEmpty()) return null;
                    
                    Card big = CardFactoryUtil.AI_getBestCreature(attacking);
                    return big;
                }
                
                @Override
                public boolean canPlay() {
                	return Phase.canPlayDuringCombat();
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        final Card[] creature = new Card[1];
                        
                        creature[0] = getTargetCard();
                        final int[] originalAttack = {creature[0].getBaseAttack()};
                        creature[0].setBaseAttack(0);
                        
                        final Command EOT = new Command() {
							private static final long serialVersionUID = -7188543458319933986L;

							public void execute() {
                        		if(AllZone.GameAction.isCardInPlay(creature[0])) {
                        			creature[0].setBaseAttack(originalAttack[0]);
                        		}
                        	}
                        };
                        AllZone.EndOfTurn.addUntil(EOT);
                    }//is card in play?
                }//resolve()
            };//SpellAbility
            
            card.addSpellAbility(ability); 
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Nameless Race")) {
        	/*
        	 * As Nameless Race enters the battlefield, pay any amount of life.
        	 * The amount you pay can't be more than the total number of white
        	 * nontoken permanents your opponents control plus the total number
        	 * of white cards in their graveyards.
        	 * Nameless Race's power and toughness are each equal to the life
        	 * paid as it entered the battlefield.
        	 */
        	final SpellAbility ability = new Ability(card, "0") {
        		@Override
        		public void resolve() {
        			Player player = card.getController();
        			Player opp = player.getOpponent();
        			int max = 0;
        			CardList play = AllZoneUtil.getPlayerCardsInPlay(opp);
        			play = play.filter(AllZoneUtil.nonToken);
        			play = play.filter(AllZoneUtil.white);
        			max += play.size();
        			
        			CardList grave = AllZoneUtil.getPlayerGraveyard(opp);
        			grave = grave.filter(AllZoneUtil.white);
        			max += grave.size();
        			
        			String[] life = new String[max+1];
        			for(int i = 0; i <= max; i++) {
        				life[i] = String.valueOf(i);
        			}
        			
        			Object o = GuiUtils.getChoice("Nameless Race - pay X life", life);
        			String answer = (String) o;
        			int loseLife = 0;
        			try {
        				loseLife = Integer.parseInt(answer.trim());
        			}
        			catch (NumberFormatException nfe) {
        				System.out.println(card.getName()+" - NumberFormatException: " + nfe.getMessage());
        			}
        			
        			card.setBaseAttack(loseLife);
        			card.setBaseDefense(loseLife);
        			
        			player.loseLife(loseLife, card);
        		}//resolve()
        	};//SpellAbility
        	
        	Command intoPlay = new Command() {
				private static final long serialVersionUID = 931101364538995898L;

				public void execute() {
        			AllZone.Stack.add(ability);
        		}
        	};
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(cardName).append(" - pay any amount of life.");
        	ability.setStackDescription(sb.toString());
        	
        	card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
       
        
        //*************** START *********** START **************************
        else if(cardName.equals("Banshee")) {
        	/*
        	 * X, Tap: Banshee deals half X damage, rounded down, to target creature or
        	 * player, and half X damage, rounded up, to you.
        	 */
        	
        	Cost abCost = new Cost("X T", cardName, true);
        	Target tgt = new Target(card,"TgtCP");
        	
        	final Ability_Activated ability = new Ability_Activated(card, abCost, tgt) {
				private static final long serialVersionUID = 2755743211116192949L;

				@Override
        		public void resolve() {
        			int x = card.getXManaCostPaid();
        			if(getTargetPlayer() == null) {
        				getTargetCard().addDamage((int)Math.floor(x/2.0), card);
        			}
        			else {
        				getTargetPlayer().addDamage((int)Math.floor(x/2.0), card);
        			}
        			card.getController().addDamage((int)Math.ceil(x/2.0), card);
        			card.setXManaCostPaid(0);
        		}//resolve()
				
				@Override
				public boolean canPlayAI() {
					return false;
				}
        		
        	};//SpellAbility
        	
        	ability.setDescription("X, tap: "+"Banshee deals half X damage, rounded down, to target creature or player, and half X damage, rounded up, to you.");
        	ability.setStackDescription(card.getName()+" - Banshee deals half X damage, rounded down, to target creature or player, and half X damage, rounded up, to you.");
        	card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Shapeshifter")) {
        	Command intoPlay = new Command() {
				private static final long serialVersionUID = 5447692676152380940L;

				public void execute() {
					if(!card.isToken()) {  //ugly hack to get around tokens created by Crib Swap
						int num = 0;
						if(card.getController().isHuman()) {
							String[] choices = new String[7];
							for(int j = 0; j < 7; j++) {
								choices[j] = ""+j;
							}
							String answer = (String)(GuiUtils.getChoiceOptional(
									card.getName()+" - Choose a number", choices));
							num = Integer.parseInt(answer);
						}
						else {
							num = 3;
						}
						card.setBaseAttack(num);
						card.setBaseDefense(7-num);
					}
				}
        	};

        	card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Metalworker")) {
        	final Cost abCost = new Cost("T", card.getName(), true);
        	
        	final SpellAbility ability = new Ability_Activated(card, abCost, null) {
        		private static final long serialVersionUID = 6661308920885136284L;
        		
        		@Override
        		public boolean canPlayAI() {
        			//compy doesn't have a manapool
        			return false;
        		}//canPlayAI()
        		
        		@Override
        		public void resolve() {
        			AllZone.InputControl.setInput(new Input() {
						private static final long serialVersionUID = 6150236529653275947L;
        				CardList revealed = new CardList();

        				@Override
        				public void showMessage() {
        					//in case hand is empty, don't do anything
        					if (AllZoneUtil.getPlayerHand(card.getController()).size() == 0) stop();

        					AllZone.Display.showMessage(card.getName()+" - Reveal an artifact.  Revealed "+revealed.size()+" so far.  Click OK when done.");
        					ButtonUtil.enableOnlyOK();
        				}

        				@Override
        				public void selectCard(Card c, PlayerZone zone) {
        					if(zone.is(Constant.Zone.Hand) && c.isArtifact() && !revealed.contains(c)) {
        						revealed.add(c);

        						//in case no more cards in hand to reveal
        						if(revealed.size() == AllZoneUtil.getPlayerHand(card.getController()).size()) done();
        						else
        							showMessage();
        					}
        				}
        				
        				@Override
        				public void selectButtonOK() {
        					done();
        				}

        				void done() {
        					StringBuilder sb = new StringBuilder();
        					for(Card reveal:revealed) sb.append(reveal.getName()+"\n");
        					JOptionPane.showMessageDialog(null, "Revealed Cards:\n"+sb.toString(), card.getName(), JOptionPane.PLAIN_MESSAGE);
        					//adding mana
        					
        					Ability_Mana abMana = new Ability_Mana(card, "0", "1", 2*revealed.size()) {
        						private static final long serialVersionUID = -2182129023960978132L;
        					};
        					abMana.setUndoable(false);
        					abMana.produceMana();

        					stop();
        				}
        			});
        		}//resolve()
        	};//SpellAbility

        	ability.setDescription(abCost+"Reveal any number of artifact cards in your hand. Add 2 to your mana pool for each card revealed this way.");
        	ability.setStackDescription(cardName+" - Reveal any number of artifact cards in your hand.");
        	card.addSpellAbility(ability); 
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sea Gate Oracle")) {
        	final Ability ability = new Ability(card, "") {
        		@Override
        		public void resolve() {
        			if(card.getController().isHuman()) {
        				PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
        				int maxCards = lib.size();
        				maxCards = Math.min(maxCards, 2);
        				if(maxCards == 0) return;
        				CardList topCards =   new CardList();
        				//show top n cards:
        				for(int j = 0; j < maxCards; j++ ) {
        					topCards.add(lib.get(j));
        				}
        				Object o = GuiUtils.getChoice("Put one card in your hand", topCards.toArray());
    					if(o != null) {
    						Card c_1 = (Card) o;
    						topCards.remove(c_1);
    						AllZone.GameAction.moveToHand(c_1);
    					}
    					for(Card c:topCards) {
    						AllZone.GameAction.moveToBottomOfLibrary(c);
    					}
        			}
        		}
        	};
        	ability.setStackDescription(cardName+" - Look at the top two cards of your library. Put one of them into your hand and the other on the bottom of your library.");
        	Command intoPlay = new Command() {
				private static final long serialVersionUID = -4300804642226899861L;

				public void execute() {
        			AllZone.Stack.add(ability);
        		}
        	};

        	card.addComesIntoPlayCommand(intoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Necratog")) {
            final Command untilEOT = new Command() {
				private static final long serialVersionUID = 6743592637334556854L;

				public void execute() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        card.addTempAttackBoost(-2);
                        card.addTempDefenseBoost(-2);
                    }
                }
            };
            
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override 
                public boolean canPlay() {
                	CardList grave = AllZoneUtil.getPlayerGraveyard(card.getController());
                	grave = grave.filter(AllZoneUtil.creatures);
                	return super.canPlay() && grave.size() > 0;
                }
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(card)) {
                        card.addTempAttackBoost(2);
                        card.addTempDefenseBoost(2);
                        AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }
            };
            
            Input runtime = new Input() {
				private static final long serialVersionUID = 63327418012595048L;
				Card topCreature = null;
            	public void showMessage() {
            		
            		PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
            		for(int i = grave.size()-1; i >=0; i--) {
            			Card c = grave.get(i);
            			if(c.isCreature()) {
            				topCreature = c;
            				break;
            			}
            		}
            		AllZone.Display.showMessage(card.getName()+" - Select OK to exile "+topCreature+".");
            		ButtonUtil.enableAll();
            	}
            	
            	public void selectButtonOK() {
            		AllZone.GameAction.exile(topCreature);
            		AllZone.Stack.add(ability);
            		stop();
            	}
            	
            	public void selectButtonCancel() {
            		stop();
            	}
            };
            
            
            ability.setDescription("Exile the top creature card of your graveyard: CARDNAME gets +2/+2 until end of turn.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" gets +2/+2 until end of turn.");
            ability.setStackDescription(sb.toString());
            ability.setBeforePayMana(runtime);
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START ************************** 
        else if(cardName.equals("Phyrexian Scuta")) {
        	Cost abCost = new Cost("3 B PayLife<3>", cardName, false);
            final SpellAbility kicker = new Spell(card, abCost, null) {
				private static final long serialVersionUID = -6420757044982294960L;

				@Override
                public void resolve() {
                    card.setKicked(true);
                    AllZone.GameAction.moveToPlay(card);
                    card.addCounterFromNonEffect(Counters.P1P1, 2);
                }
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && card.getController().getLife() >= 3;
                }
                
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("3 B");
            kicker.setDescription("Kicker - Pay 3 life.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - Creature 3/3 (Kicked)");
            kicker.setStackDescription(sb.toString());
            
            card.addSpellAbility(kicker);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Yosei, the Morning Star")) {
        	final CardList targetPerms = new CardList();
            final SpellAbility ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                	Player p = getTargetPlayer();
                	if(p.canTarget(card)) {
                		p.setSkipNextUntap(true);
                		for(Card c:targetPerms) {
                    		if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                    			c.tap();
                    		}
                    	}
                	}
                	targetPerms.clear();
                }//resolve()
            };
            
            final Input targetInput = new Input() {
                private static final long serialVersionUID = -8727869672234802473L;
                
                @Override
                public void showMessage() {
                    if(targetPerms.size() == 5) done();
                	AllZone.Display.showMessage("Select up to 5 target permanents.  Selected ("+targetPerms.size()+") so far.  Click OK when done.");
                    ButtonUtil.enableOnlyOK();
                }
                
                @Override
                public void selectButtonOK() {
                	done();
                }
                
                private void done() {
                	//here, we add the ability to the stack since it's triggered.
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - tap up to 5 permanents target player controls. Target player skips his or her next untap step.");
                	ability.setStackDescription(sb.toString());
                	AllZone.Stack.add(ability);
                	stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                	if(zone.is(Constant.Zone.Battlefield, ability.getTargetPlayer()) && !targetPerms.contains(c)) {
                		if(CardFactoryUtil.canTarget(card, c)) {
                			targetPerms.add(c);
                		}
                	}
                    showMessage();
                }
            };//Input
            
            final Input playerInput = new Input() {
				private static final long serialVersionUID = 4765535692144126496L;

				@Override
            	public void showMessage() {
            		AllZone.Display.showMessage(card.getName()+" - Select target player");
            		ButtonUtil.enableOnlyCancel();
            	}
            	
            	@Override
            	public void selectPlayer(Player p) {
            		if(p.canTarget(card)) {
            			ability.setTargetPlayer(p);
            			stopSetNext(targetInput);
            		}
            	}
            	
            	@Override
            	public void selectButtonCancel() { stop(); }
            };
            
            Command destroy = new Command() {
                private static final long serialVersionUID = -3868616119471172026L;
                
                public void execute() {
                	Player player = card.getController();
                    CardList list = CardFactoryUtil.AI_getHumanCreature(card, true);
                    
                    if(player.equals(AllZone.HumanPlayer)) AllZone.InputControl.setInput(playerInput);
                    else if(list.size() != 0) {
                        Card target = CardFactoryUtil.AI_getBestCreature(list);
                        ability.setTargetCard(target);
                        AllZone.Stack.add(ability);
                    }
                }//execute()
            };
            card.addDestroyCommand(destroy);
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Phyrexian Dreadnought")) {
        	final Player player = card.getController();
    		final CardList toSac = new CardList();
    		
        	final Ability sacOrSac = new Ability(card, "") {
        		@Override
        		public void resolve() {
        			if(player.isHuman()) {
        				Input target = new Input() {
        					private static final long serialVersionUID = 2698036349873486664L;
        					
        					@Override
        					public void showMessage() {
        						String toDisplay = cardName+" - Select any number of creatures to sacrifice.  ";
        						toDisplay += "Currently, ("+toSac.size()+") selected with a total power of: "+getTotalPower();
        						toDisplay += "  Click OK when Done.";
        						AllZone.Display.showMessage(toDisplay);
        						ButtonUtil.enableAll();
        					}
        					
        					@Override
        					public void selectButtonOK() {
        						done();
        					}
        					
        					@Override
        					public void selectButtonCancel() {
        						toSac.clear();
        						AllZone.GameAction.sacrifice(card);
        						stop();
        					}
        					
        					@Override
        					public void selectCard(Card c, PlayerZone zone) {
        						if(c.isCreature() && zone.is(Constant.Zone.Battlefield, AllZone.HumanPlayer)
        								&& !toSac.contains(c)) {
        							toSac.add(c);
        						}
        						showMessage();
        					}//selectCard()
        					
        					private void done() {
        						if(getTotalPower() >= 12) {
        							for(Card sac:toSac) AllZone.GameAction.sacrifice(sac);
        						}
        						else {
        							AllZone.GameAction.sacrifice(card);
        						}
        						toSac.clear();
        						stop();
        					}
        				};//Input
        				AllZone.InputControl.setInput(target);
        			}
        		}//end resolve
        		
        		private int getTotalPower() {
        			int sum = 0;
        			for(Card c:toSac) {
        				sum += c.getNetAttack();
        			}
        			return sum;
        		}
        	};// end sacOrSac
        	
        	final Command comesIntoPlay = new Command() {
        		private static final long serialVersionUID = 7680692311339496770L;
        		
        		public void execute() {
        			sacOrSac.setStackDescription("When "+cardName+" enters the battlefield, sacrifice it unless you sacrifice any number of creatures with total power 12 or greater.");
        			AllZone.Stack.add(sacOrSac);
        		}
        	};

        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Clone") || cardName.equals("Vesuvan Doppelganger") 
        		|| cardName.equals("Quicksilver Gargantuan")
        		|| cardName.equals("Jwari Shapeshifter")) {
        	final CardFactory cfact = cf;
        	final Card[] copyTarget = new Card[1];
        	final Card[] cloned = new Card[1];
        	
        	final SpellAbility copyBack = new Ability(card, "0") {
                @Override
                public void resolve() {
                    Card orig = cfact.getCard(card.getName(), card.getController());
                    PlayerZone dest = AllZone.getZone(card.getCurrentlyCloningCard());
                    AllZone.GameAction.moveTo(dest, orig);
                    dest.remove(card.getCurrentlyCloningCard());
                }
            };//SpellAbility
        	
        	final Command leaves = new Command() {
				private static final long serialVersionUID = 8590474793502538215L;

				public void execute() {
					StringBuilder sb = new StringBuilder();
                    sb.append(card.getName()).append(" - reverting self to "+card.getName()+".");
                    copyBack.setStackDescription(sb.toString());
                    
                    //Slight hack if the cloner copies a card with triggers
                    AllZone.TriggerHandler.removeAllFromCard(cloned[0]);
                    
                    AllZone.Stack.add(copyBack);
                }
            };
        	
        	final SpellAbility copy = new Spell(card) {
				private static final long serialVersionUID = 4496978456522751302L;

				@Override
                public void resolve() {
					if (card.getController().isComputer()) {
						CardList creatures = AllZoneUtil.getCreaturesInPlay();
						if(!creatures.isEmpty()) {
							copyTarget[0] = CardFactoryUtil.AI_getBestCreature(creatures);
						}
					}
					
					if (copyTarget[0] != null) {
						cloned[0] = CardFactory.copyStats(copyTarget[0]);
						cloned[0].setOwner(card.getController());
						cloned[0].setController(card.getController());
						cloned[0].setCloneOrigin(card);
						cloned[0].addLeavesPlayCommand(leaves);
						cloned[0].setCloneLeavesPlayCommand(leaves);
						cloned[0].setCurSetCode(copyTarget[0].getCurSetCode());
						cloned[0].setImageFilename(copyTarget[0].getImageFilename());
						if(cardName.equals("Vesuvan Doppelganger")) {
							cloned[0].addExtrinsicKeyword("At the beginning of your upkeep, you may have this creature become a copy of target creature except it doesn't copy that creature's color. If you do, this creature gains this ability.");
							cloned[0].addColor("U", cloned[0], false, true);
						}						
						else if (cardName.equals("Quicksilver Gargantuan")) {
							cloned[0].setBaseDefense(7);
							cloned[0].setBaseAttack(7);
						}
						
						//Slight hack in case the cloner copies a card with triggers
						for(Trigger t : cloned[0].getTriggers())
						{
							AllZone.TriggerHandler.registerTrigger(t);
						}
						
						AllZone.GameAction.moveToPlay(cloned[0]);
						card.setCurrentlyCloningCard(cloned[0]);
					}
                }
            };//SpellAbility
            
            Input runtime = new Input() {
				private static final long serialVersionUID = 7615038074569687330L;

				@Override
            	public void showMessage() {
            		AllZone.Display.showMessage(cardName+" - Select a creature on the battlefield");
            		ButtonUtil.enableOnlyCancel();
            	}
				
				@Override
				public void selectButtonCancel() { stop(); }
            	
            	@Override
            	public void selectCard(Card c, PlayerZone z) {
            		if( z.is(Constant.Zone.Battlefield) && c.isCreature()) {
            			if(cardName.equals("Jwari Shapeshifter") && ! c.isType("Ally"))
            			{
            				return;
            			}
            			copyTarget[0] = c;
            			stopSetNext(new Input_PayManaCost(copy));
            		}
            	}
            };
            // Do not remove SpellAbilities created by AbilityFactory or Keywords.
            card.clearFirstSpellAbility();
            card.addSpellAbility(copy);
            copy.setStackDescription(cardName+" - enters the battlefield as a copy of selected card.");
            copy.setBeforePayMana(runtime);
        }//*************** END ************ END **************************
        
        
        //*************** START ************ START **************************
        else if(cardName.equals("Nebuchadnezzar")) {
        	/*
        	 * X, T: Name a card. Target opponent reveals X cards at random from his or her hand. 
        	 * Then that player discards all cards with that name revealed this way. 
        	 * Activate this ability only during your turn.
        	 */ 
        	Cost abCost = new Cost("X T", cardName, true);
        	Target target = new Target(card,"Select target opponent", "Opponent".split(","));
        	Ability_Activated discard = new Ability_Activated(card, abCost, target) {
				private static final long serialVersionUID = 4839778470534392198L;

				@Override
        		public void resolve() {
        			//name a card
					String choice = JOptionPane.showInputDialog(null, "Name a card", cardName, JOptionPane.QUESTION_MESSAGE);
					CardList hand = AllZoneUtil.getPlayerHand(getTargetPlayer());
					int numCards = card.getXManaCostPaid();
					numCards = Math.min(hand.size(), numCards);
					
					CardList revealed = new CardList();
					for(int i = 0; i < numCards; i++) {
						Card random = CardUtil.getRandom(hand.toArray());
						revealed.add(random);
						hand.remove(random);
					}
					if(!revealed.isEmpty()) {
						GuiUtils.getChoice("Revealed at random", revealed.toArray());
					}
					else {
						GuiUtils.getChoice("Revealed at random", new String[] {"Nothing to reveal"});
					}
					
					for(Card c:revealed) {
						if(c.getName().equals(choice)) c.getController().discard(c, this);
					}
        		}
				
				@Override
				public boolean canPlayAI() {
					return false;
				}
        	};
        	
        	discard.getRestrictions().setPlayerTurn(true);
        	
        	StringBuilder sbDesc = new StringBuilder();
        	sbDesc.append(abCost).append("Name a card. Target opponent reveals X cards at random from his or her hand. ");
        	sbDesc.append("Then that player discards all cards with that name revealed this way. ");
        	sbDesc.append("Activate this ability only during your turn.");
        	discard.setDescription(sbDesc.toString());
        	
        	StringBuilder sbStack = new StringBuilder();
        	sbStack.append(cardName).append(" - name a card.");
        	discard.setStackDescription(sbStack.toString());
        	
        	card.addSpellAbility(discard);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Anurid Brushhopper")) {
        	
        	final SpellAbility toPlay = new Ability(card, "0") {
                @Override
                public void resolve() {
                    AllZone.GameAction.moveToPlay(card);
                }
            }; //ability
            StringBuilder sb = new StringBuilder();
            sb.append("Return "+card+" to the battlefield.");
            toPlay.setStackDescription(sb.toString());
            
            final Command eot = new Command() {
				private static final long serialVersionUID = 911163814565333484L;

				public void execute() {
            		AllZone.Stack.add(toPlay);
            	}
            };
        	
        	final Cost abCost = new Cost("Discard<2/Card>", cardName, true);
        	final Ability_Activated toExile = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = 7850843970664800204L;

				public void resolve() {
        			AllZone.GameAction.exile(card);
        			AllZone.EndOfTurn.addAt(eot);
        		}
        	};
        	toExile.setDescription(abCost+"Exile CARDNAME. Return it to the battlefield under its owner's control at the beginning of the next end step.");
        	toExile.setStackDescription(card+" - exile "+card+".");
        	card.addSpellAbility(toExile);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Frost Titan")) {
        	final Trigger targetedTrigger = TriggerHandler.parseTrigger("FrostTitanCounter","Mode$ SpellAbilityCast | TargetsValid$ Card.Self | ValidControllingPlayer$ Opponent | TriggerZones$ Battlefield | Execute$ TrigOverridden | TriggerDescription$ Whenever CARDNAME becomes the target of a spell or ability an opponent controls, counter that spell or ability unless its controller pays 2.", card);
        	final Ability FrostTitanCounterAbility = new Ability(card,"0")
        	{

				@Override
				public void resolve() {
					Trigger trig = card.getNamedTrigger("FrostTitanCounter");
					HashMap<String,Object> runParams = trig.getRunParams();
					final SpellAbility tgtSA = (SpellAbility)runParams.get("CastSA");
					
					Ability ability = new Ability(card, "2") {
	                    @Override
	                    public void resolve() {
	                        ;
	                    }
	                };
	                
	                final Command unpaidCommand = new Command() {
	                    private static final long serialVersionUID = 8094833091127334678L;
	                    
	                    public void execute() {
	                    	AllZone.Stack.remove(tgtSA);
	                    	if(tgtSA.isSpell())
	                    		AllZone.GameAction.moveToGraveyard(tgtSA.getSourceCard());
	                    }
	                };
	                
	                if(tgtSA.getActivatingPlayer().equals(AllZone.HumanPlayer))
	                {
	                	GameActionUtil.payManaDuringAbilityResolve(card + "\r\n", ability.getManaCost(), 
	                			Command.Blank, unpaidCommand);
	                }
	                else
	                {
	                	if(ComputerUtil.canPayCost(ability)) ComputerUtil.playNoStack(ability);
	                    else {
	                    	AllZone.Stack.remove(tgtSA);
	                    	if(tgtSA.isSpell())
	                    		AllZone.GameAction.moveToGraveyard(tgtSA.getSourceCard());
	                    }
	                }
				}
        		
        	};
        	
        	targetedTrigger.setOverridingAbility(FrostTitanCounterAbility);
        	
        	card.addTrigger(targetedTrigger);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Brass Squire")) {
        	
        	Target t2 = new Target(card, "Select target creature you control", "Creature.YouCtrl".split(","));
            final Ability_Sub sub = new Ability_Sub(card, t2) {
				private static final long serialVersionUID = -8926850792424930054L;

				@Override
            	public boolean chkAI_Drawback() {
            		return false;
            	}
				
				@Override
				public void resolve() {
					Card equipment = this.getParent().getTargetCard();
					Card creature = getTargetCard();
					if(AllZoneUtil.isCardInPlay(equipment) && AllZoneUtil.isCardInPlay(creature)) {
						if(CardFactoryUtil.canTarget(card, equipment) && CardFactoryUtil.canTarget(card, creature)) {
							if (equipment.isEquipping()) {
								Card equipped = equipment.getEquipping().get(0);
								if (!equipped.equals(creature)) {
									equipment.unEquipCard(equipped);
									equipment.equipCard(creature);
								}
							}
							else {
								equipment.equipCard(getTargetCard());
							}
						}
					}
				}
            	
            	@Override
            	public boolean doTrigger(boolean b) {
            		return false;
            	}
            };
        	
        	Cost abCost = new Cost("T", cardName, true);
        	Target t1 = new Target(card, "Select target equipment you control", "Equipment.YouCtrl".split(","));
        	final Ability_Activated ability = new Ability_Activated(card, abCost, t1) {
				private static final long serialVersionUID = 3818559481920103914L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                	sub.resolve();
                }
            };
            ability.setSubAbility(sub);
            ability.setStackDescription(cardName+" - Attach target Equipment you control to target creature you control.");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Gore Vassal")) {
        	Cost abCost = new Cost("Sac<1/CARDNAME>", cardName, true);
        	final Ability_Activated ability = new Ability_Activated(card, abCost, new Target(card, "TgtC")) {
        		private static final long serialVersionUID = 3689290210743241201L;

        		@Override
        		public boolean canPlayAI() {
        			return false;
        		}

        		@Override
        		public void resolve() {
        			final Card target = getTargetCard();

        			if(AllZoneUtil.isCardInPlay(target) && CardFactoryUtil.canTarget(card, target)) {
        				target.addCounter(Counters.M1M1, 1);
        				if(target.getNetDefense() >= 1) {
        					target.addShield();
        					AllZone.EndOfTurn.addUntil(new Command() {
        						private static final long serialVersionUID = -3332692040606224591L;

        						public void execute() {
        							target.resetShield();
        						}
        					});
        				}
        			}
        		}//resolve()
        	};//SpellAbility
            
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Put a -1/-1 counter on target creature. Then if that creature's toughness is 1 or greater, regenerate it.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" put a -1/-1 counter on target creature.");
            ability.setStackDescription(sb.toString());
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if (cardName.equals("Orcish Captain")) {
        	Cost abCost = new Cost("1", cardName, true);
        	Target target = new Target(card, "Select target Orc creature", "Creature.Orc".split(","));
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
				private static final long serialVersionUID = 6724781940648179318L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                	final Card tgt = getTargetCard();
                	final boolean[] win = new boolean[1];
                    if (AllZone.GameAction.isCardInPlay(tgt) && CardFactoryUtil.canTarget(card, tgt)) {
                    	if(GameActionUtil.flipACoin(card.getController(), card)) {
                    		tgt.addTempAttackBoost(2);
                    		win[0] = true;
                    	}
                    	else {
                    		tgt.addTempDefenseBoost(-2);
                    		win[0] = false;
                    	}
            
                        final Command EOT = new Command() {
							private static final long serialVersionUID = -7905540871887278236L;

							public void execute() {
                                if (AllZone.GameAction.isCardInPlay(tgt)) {
                                    if(win[0]) {
                                    	tgt.addTempAttackBoost(-2);
                                    }
                                    else {
                                    	tgt.addTempDefenseBoost(2);
                                    }
                                }
                            }
                        };
                        AllZone.EndOfTurn.addUntil(EOT);

                    }//if (card is in play)
                }//resolve()
            };//SpellAbility
            card.addSpellAbility(ability);
            ability.setDescription(abCost+"Flip a coin. If you win the flip, target Orc creature gets +2/+0 until end of turn. If you lose the flip, it gets -0/-2 until end of turn.");
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Orcish Spy")) {
        	Target target = new Target(card,"Select target player", new String[] {"Player"});
        	Cost abCost = new Cost("T", cardName, true);
            final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
				private static final long serialVersionUID = -7781215422160018196L;

				@Override
                public void resolve() {
                    final Player player = getTargetPlayer();
                    CardList lib = AllZoneUtil.getPlayerCardsInLibrary(player);
                    CardList toDisplay = new CardList();
                    for(int i = 0; i < 3 && i < lib.size(); i++) {
                    	toDisplay.add(lib.get(i));
                    }
                    if (lib.size() > 0) {
                        GuiUtils.getChoice("Top three cards of "+player+"'s library", toDisplay.toArray());
                    } else {
                    	StringBuilder sb = new StringBuilder();
                        sb.append(getTargetPlayer()).append("'s library is empty!");
                        javax.swing.JOptionPane.showMessageDialog(null, sb.toString(), "Target player's library", JOptionPane.INFORMATION_MESSAGE);
                    }
                }//resolve()

                @Override
                public boolean canPlayAI() {
                    return false;
                }

            };//SpellAbility

            ability.setDescription(abCost+"Look at the top three cards of target player's library.");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************  

        else if(cardName.equals("Awakener Druid"))
        {
            final long[] timeStamp = {0};

            Trigger myTrig = TriggerHandler.parseTrigger("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | TriggerDescription$ When CARDNAME enters the battlefield, target Forest becomes a 4/5 green Treefolk creature for as long as CARDNAME is on the battlefield. It's still a land.",card);
            Target myTarget = new Target(card,"Choose target forest.","Land.Forest".split(","),"1","1");
            final SpellAbility awaken = new Ability(card, "0") {
                @Override
                public void resolve() {
                    if(!AllZone.getZone(card).is("Battlefield") || getTarget().getTargetCards().size() == 0)
                        return;
                    final Card c = getTarget().getTargetCards().get(0);
                    String[] types = { "Creature", "Treefolk" };
                    String[] keywords = {  };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 4, 5, types, keywords, "G");

                    final Command onleave = new Command() {
                        private static final long serialVersionUID = -6004932214386L;
                        long stamp = timeStamp[0];
                        Card tgt = c;
                        public void execute() {
                            String[] types = { "Creature", "Treefolk" };
                            String[] keywords = { "" };
                            CardFactoryUtil.revertManland(tgt, types, keywords, "G", stamp);
                        }
                    };
                    card.addLeavesPlayCommand(onleave);
                }
            };//SpellAbility
            awaken.setTarget(myTarget);

            myTrig.setOverridingAbility(awaken);
            card.addTrigger(myTrig);
        }
        
        
        if(hasKeyword(card, "Level up") != -1 && hasKeyword(card, "maxLevel") != -1)
        {
        	int n = hasKeyword(card, "Level up");
        	int m = hasKeyword(card, "maxLevel");
        	if(n != -1) {
                String parse = card.getKeyword().get(n).toString();
                String parseMax = card.getKeyword().get(m).toString();
                
                card.removeIntrinsicKeyword(parse);
                card.removeIntrinsicKeyword(parseMax);
                
                
                String k[] = parse.split(":");
                final String manacost = k[1];
                
                String l[] = parseMax.split(":");
                final int maxLevel = Integer.parseInt(l[1]);
                
                final Ability levelUp = new Ability(card, manacost){
                	public void resolve()
                	{
                		card.addCounter(Counters.LEVEL, 1);
                	}
                	
                	public boolean canPlay()
                	{
                		//only as sorcery
                		return AllZone.getZone(card).is(Constant.Zone.Battlefield) && Phase.canCastSorcery(card.getController());
                	}
                	
                	public boolean canPlayAI()
                	{
                		return card.getCounters(Counters.LEVEL) < maxLevel;
                	}
                	
                };
                card.addSpellAbility(levelUp);
                
                StringBuilder sbDesc = new StringBuilder();
                sbDesc.append("Level up ").append(manacost).append(" (").append(manacost);
                sbDesc.append(": Put a level counter on this. Level up only as a sorcery.)");
                levelUp.setDescription(sbDesc.toString());
                
                StringBuilder sbStack = new StringBuilder();
                sbStack.append(card).append(" - put a level counter on this.");
                levelUp.setStackDescription(sbStack.toString());
                
                card.setLevelUp(true);
                
            }
        }//level up
        
        return card;
    }
}
