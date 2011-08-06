
package forge;


import java.util.HashMap;

import com.esotericsoftware.minlog.Log;

import forge.gui.GuiUtils;


class CardFactory_Planeswalkers {
    public static Card getCard(final Card card, String cardName, Player owner) {
    	// All Planeswalkers set their loyality in the beginning
    	if (card.getBaseLoyalty() > 0)
    		card.addComesIntoPlayCommand(CardFactoryUtil.entersBattleFieldWithCounters(card, Counters.LOYALTY, card.getBaseLoyalty()));
    	
        //*************** START *********** START **************************
        if(cardName.equals("Elspeth, Knight-Errant")) {
            //computer only plays ability 1 and 3, put 1/1 Soldier in play and make everything indestructible
            final int turn[] = new int[1];
            turn[0] = -1;
            
            //ability2: target creature gets +3/+3 and flying until EOT
            final SpellAbility ability2 = new Ability(card, "0") {
                
                @Override
                public void resolve() {
                    
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    
                    turn[0] = AllZone.Phase.getTurn();
                    
                    final Command eot = new Command() {
                        private static final long serialVersionUID = 94488363210770877L;
                        
                        public void execute() {
                            Card c = getTargetCard();
                            if(AllZone.GameAction.isCardInPlay(c)) {
                                c.addTempAttackBoost(-3);
                                c.addTempDefenseBoost(-3);
                                c.removeExtrinsicKeyword("Flying");
                            }
                        }//execute()
                    };//Command
                    
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        c.addTempAttackBoost(3);
                        c.addTempDefenseBoost(3);
                        c.addExtrinsicKeyword("Flying");
                        
                        AllZone.EndOfTurn.addUntil(eot);
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                    

                }//canPlay()
            };//SpellAbility ability2
            
            ability2.setBeforePayMana(new Input() {
                private static final long serialVersionUID = 9062830120519820799L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        
                        AllZone.Stack.add(ability2);
                    }
                    stop();
                }//showMessage()
            });
            

            //ability3
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 8);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Card emblem = new Card();
                    //should we even name this permanent?
                    //emblem.setName("Elspeth Emblem");
                    emblem.addIntrinsicKeyword("Indestructible");
                    emblem.addIntrinsicKeyword("Shroud");
                    emblem.addIntrinsicKeyword("Artifacts, creatures, enchantments, and lands you control are indestructible.");
                    emblem.setImmutable(true);
                    emblem.addType("Emblem");
                    emblem.setController(card.getController());
                    emblem.setOwner(card.getOwner());
                    
                    AllZone.GameAction.moveToPlay(emblem);
                    
                    //AllZone.GameAction.checkStateEffects();
                    AllZone.StaticEffects.rePopulateStateBasedList();
                    for(String effect:AllZone.StaticEffects.getStateBasedMap().keySet()) {
                        Command com = GameActionUtil.commands.get(effect);
                        com.execute();
                    }  
                }
                
                @Override
                public boolean canPlay() {
                    return 8 <= card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                    CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    list = list.filter(new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isEmblem() && c.getKeyword().contains("Artifacts, creatures, enchantments, and lands you control are indestructible.");
                    	}
                    });
                	return list.size() == 0 && card.getCounters(Counters.LOYALTY) > 8;
                }
            };
            ability3.setBeforePayMana(new Input() {
                private static final long serialVersionUID = -2054686425541429389L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability3);
                    }
                    stop();
                }//showMessage()
            });
            
            //ability 1: create white 1/1 token
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    CardFactoryUtil.makeToken("Soldier", "W 1 1 Soldier", card.getController(), "W", new String[] {
                            "Creature", "Soldier"}, 1, 1, new String[] {""});
                }
                
                @Override
                public boolean canPlayAI() {
                    if(ability3.canPlay() && ability3.canPlayAI()) {
                        return false;
                    } else {
                        return true;
                    }
                }
                
                @Override
                public boolean canPlay() {
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };//SpellAbility ability1
            
            ability1.setBeforePayMana(new Input() {
                private static final long serialVersionUID = -7892114885686285881L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability1);
                    }
                    stop();
                }//showMessage()
            });
            
            ability1.setDescription("+1: Put a 1/1 white Soldier creature token onto the battlefield.");
            ability1.setStackDescription("Elspeth, Knight-Errant - put 1/1 token onto the battlefield.");
            card.addSpellAbility(ability1);
            
            ability2.setDescription("+1: Target creature gets +3/+3 and gains flying until end of turn.");
            ability2.setStackDescription("Elspeth, Knight-Errant - creature gets +3/+3 and Flying until EOT.");
            ability2.setBeforePayMana(CardFactoryUtil.input_targetCreature(ability2));
            
            card.addSpellAbility(ability2);
            
            ability3.setDescription("-8: You get an emblem with \"Artifacts, creatures, enchantments, and lands you control are indestructible.\"");
            ability3.setStackDescription("Elspeth, Knight-Errant - You get an emblem with \"Artifacts, creatures, enchantments, and lands you control are indestructible.\"");
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }
        //*************** END ************ END **************************
        
  
        //*************** START *********** START **************************
        else if(cardName.equals("Ajani Goldmane")) {
            //computer only plays ability 1 and 3, gain life and put X/X token onto battlefield
            final int turn[] = new int[1];
            turn[0] = -1;
            
            //ability2: all controller's creatures get +1\+1 and vigilance until EOT
            final SpellAbility ability2 = new Ability(card, "0") {
                final Command untilEOT = new Command() {
                	private static final long serialVersionUID = -5436621445704076988L;
                	
                	public void execute() {
                		Player player = card.getController();
                		CardList creatures;
                		if(player.equals(AllZone.HumanPlayer)) {
                			creatures = new CardList(AllZone.Human_Battlefield.getCards());
                		} else {
                			creatures = new CardList(AllZone.Computer_Battlefield.getCards());
                		}
                		
                		creatures = creatures.getType("Creature");
                		
                		for(int i = 0; i < creatures.size(); i++) {
                			Card card = creatures.get(i);
                			//card.setAttack(card.getAttack() - 1);
                			//card.setDefense(card.getDefense() - 1);
                			card.removeExtrinsicKeyword("Vigilance");
                		}
                	}
                };
                
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Player player = card.getController();
                    CardList creatures;
                    if(player.equals(AllZone.HumanPlayer)) {
                        creatures = new CardList(AllZone.Human_Battlefield.getCards());
                    } else {
                        creatures = new CardList(AllZone.Computer_Battlefield.getCards());
                    }
                    
                    creatures = creatures.getType("Creature");
                    
                    for(int i = 0; i < creatures.size(); i++) {
                        Card card = creatures.get(i);
                        card.addCounter(Counters.P1P1, 1);
                        card.addExtrinsicKeyword("Vigilance");
                    }
                    
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                    
                }//canPlay()
            };//SpellAbility ability2
            
            ability2.setBeforePayMana(new Input() {
                private static final long serialVersionUID = 6373573398967821630L;
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability2);
                    }
                    stop();
                }//showMessage()
            });
            
            //ability3
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 6);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    //Create token
                    int n = card.getController().getLife();
                    CardFactoryUtil.makeToken("Avatar", "W N N Avatar", card.getController(), "W", new String[] {
                            "Creature", "Avatar"}, n, n,
                            new String[] {"This creature's power and toughness are each equal to your life total"});
                }
                
                @Override
                public boolean canPlay() {
                    return 6 <= card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                    // may be it's better to put only if you have less than 5 life
                    return true;
                }
            };
            ability3.setBeforePayMana(new Input() {
                private static final long serialVersionUID = 7530960428366291386L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability3);
                    }
                    stop();
                }//showMessage()
            });
            
            //ability 1: gain 2 life
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    

                    card.getController().gainLife(2, card);
                    Log.debug("Ajani Goldmane", "current phase: " + AllZone.Phase.getPhase());
                }
                
                @Override
                public boolean canPlayAI() {
                    if(ability3.canPlay() && ability3.canPlayAI()) {
                        return false;
                    } else {
                        return true;
                    }
                }
                
                @Override
                public boolean canPlay() {
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };//SpellAbility ability1
            
            ability1.setBeforePayMana(new Input() {
                private static final long serialVersionUID = -7969603493514210825L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability1);
                    }
                    stop();
                }//showMessage()
            });
            
            ability1.setDescription("+1: You gain 2 life.");
            StringBuilder stack1 = new StringBuilder();
            stack1.append("Ajani Goldmane - ").append(card.getController()).append(" gains 2 life");
            ability1.setStackDescription(stack1.toString());
            // ability1.setStackDescription("Ajani Goldmane - " + card.getController() + " gains 2 life");
            card.addSpellAbility(ability1);
            
            ability2.setDescription("-1: Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn.");
            ability2.setStackDescription("Ajani Goldmane - Put a +1/+1 counter on each creature you control. They get vigilance.");
            card.addSpellAbility(ability2);
            
            ability3.setDescription("-6: Put a white Avatar creature token onto the battlefield. It has \"This creature's power and toughness are each equal to your life total.\"");
            ability3.setStackDescription("Ajani Goldmane - Put a X/X white Avatar creature token onto the battlefield.");
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }
        //*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Chandra Nalaar")) {
            //computer only plays ability 1 and 3, discard and return creature from graveyard to play
            final int turn[] = new int[1];
            turn[0] = -1;
            
            //ability 1
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    if(getTargetCard() != null) {
                        if(AllZone.GameAction.isCardInPlay(getTargetCard())
                                && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            Card c = getTargetCard();
                            c.addDamage(1, card);
                        }
                    }

                    else {
                    	getTargetPlayer().addDamage(1, card);
                    }
                }
                
                @Override
                public boolean canPlay() {
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }
                
                @Override
                public boolean canPlayAI() {
                    setTargetPlayer(AllZone.HumanPlayer);
                    setStackDescription("Chandra Nalaar - deals 1 damage to " + AllZone.HumanPlayer);
                    return card.getCounters(Counters.LOYALTY) < 8;
                }
            };//SpellAbility ability1
            
            Input target1 = new Input() {
                private static final long serialVersionUID = 5263705146686766284L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target Player or Planeswalker");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(card.isPlaneswalker() && zone.is(Constant.Zone.Battlefield) &&
                       CardFactoryUtil.canTarget(card, card)) {
                        ability1.setTargetCard(card);
                        //stopSetNext(new Input_PayManaCost(ability1));
                        AllZone.Stack.add(ability1);
                        stop();
                    }
                }//selectCard()
                
                @Override
                public void selectPlayer(Player player) {
                    ability1.setTargetPlayer(player);
                    //stopSetNext(new Input_PayManaCost(ability1));
                    AllZone.Stack.add(ability1);
                    stop();
                }
            };
            ability1.setBeforePayMana(target1);
            ability1.setDescription("+1: Chandra Nalaar deals 1 damage to target player.");
            card.addSpellAbility(ability1);
            //end ability1
            
            //ability 2
            final int damage2[] = new int[1];
            
            final SpellAbility ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    
                    card.subtractCounter(Counters.LOYALTY, damage2[0]);
                    getTargetCard().addDamage(damage2[0], card);
                    
                    damage2[0] = 0;
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility ability2
            
            Input target2 = new Input() {
                private static final long serialVersionUID = -2160464080456452897L;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select target creature");
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(!CardFactoryUtil.canTarget(card, c)) {
                        AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                    } else if(c.isCreature()) {
                        turn[0] = AllZone.Phase.getTurn();
                        

                        damage2[0] = getDamage();
                        
                        ability2.setTargetCard(c);
                        ability2.setStackDescription("Chandra Nalaar - deals damage to " + c);
                        
                        AllZone.Stack.add(ability2);
                        stop();
                    }
                }//selectCard()
                
                int getDamage() {
                    int size = card.getCounters(Counters.LOYALTY);
                    Object choice[] = new Object[size];
                    
                    for(int i = 0; i < choice.length; i++)
                        choice[i] = Integer.valueOf(i + 1);
                    
                    Integer damage = (Integer) GuiUtils.getChoice("Select X", choice);
                    return damage.intValue();
                }
            };//Input target
            ability2.setBeforePayMana(target2);
            ability2.setDescription("-X: Chandra Nalaar deals X damage to target creature.");
            card.addSpellAbility(ability2);
            //end ability2
            

            //ability 3
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 8);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    getTargetPlayer().addDamage(10, card);
                    
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, getTargetPlayer());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Creature");
                    
                    for(int i = 0; i < list.size(); i++) 
                    	list.get(i).addDamage(10, card);
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && 7 < card.getCounters(Counters.LOYALTY)
                            && Phase.canCastSorcery(card.getController());
                }
                
                @Override
                public boolean canPlayAI() {
                    setTargetPlayer(AllZone.HumanPlayer);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Chandra Nalaar - deals 10 damage to ").append(AllZone.HumanPlayer);
                    sb.append(" and each creature he or she controls.");
                    setStackDescription(sb.toString());
                    //setStackDescription("Chandra Nalaar - deals 10 damage to " + AllZone.HumanPlayer
                    //        + " and each creature he or she controls.");
                    return true;
                }
            };//SpellAbility ability3
            
            Input target3 = new Input() {
                private static final long serialVersionUID = -3014450919506364666L;
                
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
                public void selectPlayer(Player player) {
                    turn[0] = AllZone.Phase.getTurn();
                    
                    ability3.setTargetPlayer(player);
                    
                    StringBuilder stack3 = new StringBuilder();
                    stack3.append("Chandra Nalaar - deals 10 damage to ").append(player);
                    stack3.append(" and each creature he or she controls.");
                    ability3.setStackDescription(stack3.toString());
                    //ability3.setStackDescription("Chandra Nalaar - deals 10 damage to " + player
                    //        + " and each creature he or she controls.");
                    
                    AllZone.Stack.add(ability3);
                    stop();
                }
            };//Input target
            ability3.setBeforePayMana(target3);
            ability3.setDescription("-8: Chandra Nalaar deals 10 damage to target player and each creature he or she controls.");
            card.addSpellAbility(ability3);
            //end ability3
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }
        //*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Ajani Vengeant")) {

            //ability3
        	Ability_Cost cost = new Ability_Cost("SubCounter<7/LOYALTY>", cardName, true);
        	final SpellAbility ability3 = new Ability_Activated(card, cost, new Target(card,"P")){
				private static final long serialVersionUID = -1200172251117224702L;

				@Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    CardList land = AllZoneUtil.getPlayerCardsInPlay(player);
                    land = land.getType("Land");
                    
                    for(Card c:land) {
                        AllZone.GameAction.destroy(c);
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	Player player = AllZone.HumanPlayer;
                    CardList land = AllZoneUtil.getPlayerCardsInPlay(player);
                    land = land.getType("Land");
                    
                    setTargetPlayer(player);
                    return card.getCounters(Counters.LOYALTY) >= 8 && land.size() >= 3;
                }
            };
            ability3.setDescription("-7: Destroy all lands target player controls.");
            ability3.getRestrictions().setPlaneswalker(true);
            
            card.addSpellAbility(ability3);
            
            return card;
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Tezzeret the Seeker")) {
            final int turn[] = new int[1];
            turn[0] = -1;
            
            //ability1
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    
                    turn[0] = AllZone.Phase.getTurn();
                    
                    //only computer uses the stack
                    CardList tapped = new CardList(AllZone.Computer_Battlefield.getCards());
                    tapped = tapped.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() && c.isTapped() && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    
                    for(int i = 0; i < 2 && i < tapped.size(); i++)
                        tapped.get(i).untap();
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return card.getCounters(Counters.LOYALTY) <= 6
                            && AllZone.Phase.getPhase().equals(Constant.Phase.Main2);
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            final Input targetArtifact = new Input() {
                
                private static final long serialVersionUID = -7915255038817192835L;
                private int               count;
                
                @Override
                public void showMessage() {
                    AllZone.Display.showMessage("Select an artifact to untap");
                    ButtonUtil.disableAll();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(c.isArtifact() && zone.is(Constant.Zone.Battlefield) && CardFactoryUtil.canTarget(card, c)) {
                        count++;
                        c.untap();
                    }
                    
                    //doesn't use the stack, its just easier this way
                    if(count == 2) {
                        count = 0;
                        turn[0] = AllZone.Phase.getTurn();
                        card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                        stop();
                    }
                }//selectCard()
            };//Input
            
            Input runtime1 = new Input() {
                private static final long serialVersionUID = 871304623687370615L;
                
                @Override
                public void showMessage() {
                    stopSetNext(targetArtifact);
                }
            };//Input
            ability1.setDescription("+1: Untap up to two target artifacts.");
            ability1.setStackDescription("Tezzeret the Seeker - Untap two target artifacts.");
            
            ability1.setBeforePayMana(runtime1);
            card.addSpellAbility(ability1);
            //end ability 1
            

            //ability 2
            final SpellAbility ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    
                    int size = card.getCounters(Counters.LOYALTY) + 1;
                    Object choice[] = new Object[size];
                    
                    for(int i = 0; i < choice.length; i++)
                        choice[i] = Integer.valueOf(i);
                    
                    Integer damage = (Integer) GuiUtils.getChoice("Select X", choice);
                    final int dam = damage.intValue();
                    
                    card.subtractCounter(Counters.LOYALTY, dam);
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());

                    CardList list = new CardList(lib.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact() && CardUtil.getConvertedManaCost(c.getManaCost()) <= dam;
                        }
                    });
                    
                    if(list.size() > 0) {
                        Object o = GuiUtils.getChoiceOptional("Select artifact",
                                AllZone.Human_Library.getCards());
                        if(o != null) {
                            Card c = (Card) o;
                            if(list.contains(c)) {
                                AllZone.GameAction.moveToPlay(c);
                            }
                        }
                    }
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility ability2
            ability2.setDescription("-X: Search your library for an artifact card with converted mana cost X or less and put it onto the battlefield. Then shuffle your library.");
            StringBuilder stack2 = new StringBuilder();
            stack2.append(card.getName());
            stack2.append(" - Search your library for an artifact card with converted mana cost X or less and put it onto the battlefield. Then shuffle your library.");
            ability2.setStackDescription(stack2.toString());
            card.addSpellAbility(ability2);
            

            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    
                    card.subtractCounter(Counters.LOYALTY, 5);
                    
                    turn[0] = AllZone.Phase.getTurn();
                    
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    CardList list = new CardList(play.getCards());
                    list = list.getType("Artifact");
                    CardList creatures = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature();
                        }
                    });
                    
                    //final Card[] tempCards = new Card[creatures.size()];
                    final HashMap<Integer, Card> tempCardMap = new HashMap<Integer, Card>();
                    
                    for(Card creatureCard:creatures) {
                        Card crd = copyStats(creatureCard);
                        tempCardMap.put(creatureCard.getUniqueNumber(), crd);
                        //System.out.println("Just added:" + crd);
                    }
                    
                    for(Card c:list) {
                        final Card[] art = new Card[1];
                        art[0] = c;
                        if(AllZone.GameAction.isCardInPlay(art[0])) {
                            if(c.isCreature()) {
                                //Card crd = copyStats(art[0]);
                                //tempCards[c.getUniqueNumber()] = crd;
                                
                                final Command creatureUntilEOT = new Command() {
                                    private static final long serialVersionUID = 5063161656920609389L;
                                    
                                    public void execute() {
                                        final int id = art[0].getUniqueNumber();
                                        
                                        Card tempCard = tempCardMap.get(id);
                                        art[0].setBaseAttack(tempCard.getBaseAttack());
                                        art[0].setBaseDefense(tempCard.getBaseDefense());
                                        
                                    }
                                };//Command
                                
                                art[0].setBaseAttack(5);
                                art[0].setBaseDefense(5);
                                
                                AllZone.EndOfTurn.addUntil(creatureUntilEOT);
                            } else {
                                final Command nonCreatureUntilEOT = new Command() {
                                    private static final long serialVersionUID = 248122386218960073L;
                                    
                                    public void execute() {
                                        art[0].removeType("Creature");
                                        art[0].setBaseAttack(0);
                                        art[0].setBaseDefense(0);
                                    }
                                };//Command
                                
                                if(art[0].isEquipment() && art[0].isEquipping()) {
                                    Card equippedCreature = art[0].getEquipping().get(0);
                                    art[0].unEquipCard(equippedCreature);
                                }
                                art[0].addType("Creature");
                                art[0].setBaseAttack(5);
                                art[0].setBaseDefense(5);
                                
                                AllZone.EndOfTurn.addUntil(nonCreatureUntilEOT);
                            }//noncreature artifact
                            
                        }
                    }//for
                }//resolve
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && card.getCounters(Counters.LOYALTY) >= 5
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    CardList list = new CardList(play.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isArtifact()
                                    && (!c.isCreature() || (c.isCreature() && c.getBaseAttack() < 4))
                                    && !c.hasSickness();
                        }
                    });
                    return list.size() > 4 && AllZone.Phase.getPhase().equals("Main1")
                            && card.getCounters(Counters.LOYALTY) > 5;
                }
            };
            ability3.setDescription("-5: Artifacts you control become 5/5 artifact creatures until end of turn.");
            StringBuilder stack3 = new StringBuilder();
            stack3.append(card.getName()).append(" - Artifacts you control become 5/5 artifact creatures until end of turn.");
            ability3.setStackDescription(stack3.toString());
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if (cardName.equals("Jace, the Mind Sculptor")) {
            final int turn[] = new int[1];
            turn[0] = -1;
             
            final Ability ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    card.addCounterFromNonEffect(Counters.LOYALTY, 2);
                    Player targetPlayer = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, targetPlayer);
                    
                    if (lib.size() == 0) return;
                    
                    Card c = lib.get(0);
                    
                    if (card.getController().equals(AllZone.HumanPlayer)) {
                        StringBuilder question = new StringBuilder();
                        question.append("Put the card ").append(c).append(" on the bottom of the ");
                        question.append(c.getController()).append("'s library?");
                        
                        if (GameActionUtil.showYesNoDialog(card, question.toString())) {
                            AllZone.GameAction.moveToBottomOfLibrary(c);
                        }
                        
                    } else //compy
                    {
                        PlayerZone humanPlay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
                        CardList land = new CardList(humanPlay.getCards());
                        land = land.getType("Land");
                        
                        //TODO: improve this:
                        if(land.size() > 4 && c.isLand()) ;
                        else {
                        	AllZone.GameAction.moveToBottomOfLibrary(c);
                        }
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    return card.getCounters(Counters.LOYALTY) < 13 && AllZone.Human_Library.size() > 2;
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability1.setDescription("+2: Look at the top card of target player's library. You may put that card on the bottom of that player's library.");
            StringBuilder stack1 = new StringBuilder();
            stack1.append(card.getName()).append(" - Look at the top card of target player's library. You may put that card on the bottom of that player's library.");
            ability1.setStackDescription(stack1.toString());
            
            ability1.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability1));
            ability1.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.addSpellAbility(ability1);
            
            final Ability ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    card.getController().drawCards(3);
                    
                    Player player = card.getController();
                    if(player.equals(AllZone.HumanPlayer)) humanResolve();
                    //else
                    //  computerResolve();
                }
                
                public void humanResolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
                    
                    CardList putOnTop = new CardList(hand.getCards());
                    
                    Object o = GuiUtils.getChoiceOptional("First card to put on top: ", putOnTop.toArray());
                    if(o != null) {
                        Card c1 = (Card) o;
                        putOnTop.remove(c1);
                        AllZone.GameAction.moveToLibrary(c1);
                    }
                    o = GuiUtils.getChoiceOptional("Second card to put on top: ", putOnTop.toArray());
                    if(o != null) {
                        Card c2 = (Card) o;
                        putOnTop.remove(c2);
                        AllZone.GameAction.moveToLibrary(c2);
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability2.setDescription("0: Draw three cards, then put two cards from your hand on top of your library in any order.");
            StringBuilder stack2 = new StringBuilder();
            stack2.append(card.getName()).append(" - Draw three cards, then put two cards from your hand on top of your library in any order.");
            ability2.setStackDescription(stack2.toString());
            card.addSpellAbility(ability2);
            
            final Ability ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    card.subtractCounter(Counters.LOYALTY, 1);
                    
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                            AllZone.GameAction.moveToHand(getTargetCard());
                    }//if
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return card.getCounters(Counters.LOYALTY) >= 1
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }
            };
            ability3.setDescription("-1: Return target creature to its owner's hand.");
            StringBuilder stack3 = new StringBuilder();
            stack3.append(card.getName()).append(" - Return target creature to its owner's hand.");
            ability3.setStackDescription(stack3.toString());
            
            ability3.setBeforePayMana(CardFactoryUtil.input_targetCreature(ability3));
            card.addSpellAbility(ability3);
            
            final Ability ability4 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    card.subtractCounter(Counters.LOYALTY, 12);
                    
                    Player player = getTargetPlayer();
                    
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    
                    CardList libList = new CardList(lib.getCards());
                    CardList handList = new CardList(hand.getCards());
                    
                    for(Card c:libList)
                        AllZone.GameAction.exile(c);
                    
                    for(Card c:handList) {
                        AllZone.GameAction.moveToLibrary(c);
                    }
                    player.shuffle();
                }
                
                @Override
                public boolean canPlayAI() {
                    int libSize = AllZone.Human_Library.size();
                    int handSize = AllZone.Human_Hand.size();
                    return libSize > 10 && (libSize > handSize);
                }
                
                @Override
                public boolean canPlay() {
                    return card.getCounters(Counters.LOYALTY) >= 12
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }
            };
            ability4.setDescription("-12: Exile all cards from target player's library, then that player shuffles his or her hand into his or her library.");
            StringBuilder stack4 = new StringBuilder();
            stack4.append(card.getName()).append(" - Exile all cards from target player's library, then that player shuffles his or her hand into his or her library.");
            ability4.setStackDescription(stack4.toString());
            
            ability4.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability4));
            ability4.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.addSpellAbility(ability4);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Sarkhan the Mad")) {

        	//Planeswalker book-keeping
        	final int turn[] = new int[1];
        	turn[0] = -1;
                  
            //ability1
            /*
             * 0: Reveal the top card of your library and put it into your hand. Sarkhan
             * the Mad deals damage to himself equal to that card's converted mana cost.
             */
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 0);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    final Player player = card.getController();
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);

                    Card topCard = lib.get(0);
                    int convertedManaTopCard = CardUtil.getConvertedManaCost(topCard.getManaCost());
                    CardList showTop = new CardList();
                    showTop.add(topCard);
                    GuiUtils.getChoiceOptional("Revealed top card: ", showTop.toArray());
                    
                    //now, move it to player's hand                
                    AllZone.GameAction.moveToHand(topCard);
                    
                    //now, do X damage to Sarkhan
                    card.addDamage(convertedManaTopCard, card);
                    
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	//the computer isn't really smart enough to play this effectively, and it doesn't really
                	//help unless there are no cards in his hand
                	return false;
                }
                
                @Override
                public boolean canPlay() {
                	//looks like standard Planeswalker stuff...
                	//maybe should check if library is empty, or 1 card?
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability1.setDescription("0: Reveal the top card of your library and put it into your hand. Sarkhan the Mad deals damage to himself equal to that card's converted mana cost.");
            StringBuilder stack1 = new StringBuilder();
            stack1.append(card.getName()).append(" - Reveal top card and do damage.");
            ability1.setStackDescription(stack1.toString());
            
            //ability2
            /*
             * -2: Target creature's controller sacrifices it, then that player puts a 5/5 red Dragon
             * creature token with flying onto the battlefield.
             */
            final SpellAbility ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 2);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Card target = getTargetCard();
                    AllZone.GameAction.sacrifice(target);
                    //in makeToken, use target for source, so it goes into the correct Zone
                    CardFactoryUtil.makeToken("Dragon", "R 5 5 Dragon", target.getController(), "", new String[] {"Creature", "Dragon"}, 5, 5, new String[] {"Flying"});
                    
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    CardList creatures = new CardList(play.getCards());
                    creatures = creatures.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isCreature();
                    	}
                    });
                	return creatures.size() >= 1;
                }
                
                @Override
                public void chooseTargetAI() {
                	PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    CardList cards = new CardList(play.getCards());
                    //avoid targeting the dragon tokens we just put in play...
                    cards = cards.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return !(c.isToken() && c.getType().contains("Dragon"));
                    	}
                    });
                	setTargetCard(CardFactoryUtil.AI_getCheapestCreature(cards, card, true));
                	Log.debug("Sarkhan the Mad", "Sarkhan the Mad caused sacrifice of: "+
                			CardFactoryUtil.AI_getCheapestCreature(cards, card, true).getName());
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && card.getCounters(Counters.LOYALTY) >= 2
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability2.setDescription("-2: Target creature's controller sacrifices it, then that player puts a 5/5 red Dragon creature token with flying onto the battlefield.");
            ability2.setBeforePayMana(CardFactoryUtil.input_targetCreature(ability2));
            
            //ability3
            /*
             * -4: Each Dragon creature you control deals damage equal to its
             * power to target player.
             */
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 4);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    final Player target = getTargetPlayer();
                    final Player player = card.getController();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, player);
                    CardList dragons = new CardList(play.getCards());
                    dragons = dragons.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isType("Dragon");
                    	}
                    });
                    for(int i = 0; i < dragons.size(); i++) {
                    	Card dragon = dragons.get(i);
                    	int damage = dragon.getNetAttack();
                    	target.addDamage(damage, dragon);
                    }
                    
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    setTargetPlayer(AllZone.HumanPlayer);
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
                    CardList dragons = new CardList(play.getCards());
                    dragons = dragons.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return c.isType("Dragon");
                    	}
                    });
                    return card.getCounters(Counters.LOYALTY) >= 4 && dragons.size() >= 1;
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && card.getCounters(Counters.LOYALTY) >= 4
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability3.setDescription("-4: Each Dragon creature you control deals damage equal to its power to target player.");
            ability3.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability3));
            
            card.addSpellAbility(ability1);
            card.addSpellAbility(ability2);
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Koth of the Hammer")) {
            //computer only plays ability 1 and 3, put 1/1 Soldier in play and make everything indestructible
            final int turn[] = new int[1];
            turn[0] = -1;
            
            //ability2: add R for each mountain
            final SpellAbility ability2 = new Ability(card, "0") {
                
                @Override
                public void resolve() {
                    
                    card.subtractCounter(Counters.LOYALTY, 2);
                    
                    turn[0] = AllZone.Phase.getTurn();
                    
                    CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.HumanPlayer);
                    list = list.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card crd)
                    	{
                    		return crd.getType().contains("Mountain");
                    	}
                    });
                    
                    Ability_Mana abMana = new Ability_Mana(card, "0", "R", list.size()) {
                    	private static final long serialVersionUID = -2182129023960978132L;
                    };
                    abMana.undoable = false;
                    abMana.produceMana();
                    
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                    
                }//canPlay()
            };//SpellAbility ability2
            
            //ability3
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 5);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Card emblem = new Card();

                    emblem.addIntrinsicKeyword("Indestructible");
                    emblem.addIntrinsicKeyword("Shroud");
                    emblem.addIntrinsicKeyword("Mountains you control have 'tap: This land deals 1 damage to target creature or player.'");
                    emblem.setImmutable(true);
                    emblem.addType("Emblem");
                    emblem.setController(card.getController());
                    emblem.setOwner(card.getOwner());
                    
                    // todo: Emblems live in the command zone
                    AllZone.GameAction.moveToPlay(emblem);
                    
                    //AllZone.GameAction.checkStateEffects();
                    AllZone.StaticEffects.rePopulateStateBasedList();
                    for(String effect:AllZone.StaticEffects.getStateBasedMap().keySet()) {
                        Command com = GameActionUtil.commands.get(effect);
                        com.execute();
                    }  
                }
                
                @Override
                public boolean canPlay() {
                    return 5 <= card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                    CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    list = list.filter(new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isEmblem() && c.getKeyword().contains("Mountains you control have 'tap: This land deals 1 damage to target creature or player.'");
                    	}
                    });
                	return list.size() == 0 && card.getCounters(Counters.LOYALTY) > 5;
                }
            };
            ability3.setBeforePayMana(new Input() {
                private static final long serialVersionUID = -2054686425541429389L;
                
                int                       check            = -1;
                
                @Override
                public void showMessage() {
                    if(check != AllZone.Phase.getTurn()) {
                        check = AllZone.Phase.getTurn();
                        turn[0] = AllZone.Phase.getTurn();
                        AllZone.Stack.add(ability3);
                    }
                    stop();
                }//showMessage()
            });
            
            //ability 1: make 4/4 out of moutain
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.addCounterFromNonEffect(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    final Card card[] = new Card[1];
                    card[0] = getTargetCard();
                    
                    final int[] oldAttack = new int[1];
                    final int[] oldDefense = new int[1];
                    
                    oldAttack[0] = card[0].getBaseAttack();
                    oldDefense[0] = card[0].getBaseDefense();
                   
                    if (card[0].getType().contains("Mountain"))
                    {
                    	card[0].untap();
                    	
	                    card[0].setBaseAttack(4);
	                    card[0].setBaseDefense(4);
	                    card[0].addType("Creature");
	                    card[0].addType("Elemental");
	                    
	                    //EOT
	                    final Command untilEOT = new Command() {
	
							private static final long serialVersionUID = 6426615528873039915L;
	
							public void execute() {
	                            card[0].setBaseAttack(oldAttack[0]);
	                            card[0].setBaseDefense(oldDefense[0]);
	                            
	                            card[0].removeType("Creature");
	                            card[0].removeType("Elemental");
	                        }
	                    };
	                    AllZone.EndOfTurn.addUntil(untilEOT);
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                	CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                	list = list.filter(new CardListFilter()
                	{
                		public boolean addCard(Card crd)
                		{
                			return crd.isEmblem() && crd.getKeyword().contains("Mountains you control have 'tap: This land deals 1 damage to target creature or player.'");
                		}
                	});
                	
                	CardList mountains = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                	mountains = mountains.filter(new CardListFilter()
                	{
                		public boolean addCard(Card crd)
                		{
                			return crd.getType().contains("Mountain") && CardFactoryUtil.canTarget(card, crd);
                		}
                	});
                	CardListUtil.sortByTapped(mountains);
                	
                	if (mountains.size() == 0)
                		return false;
                	
                    if(ability3.canPlay() && ability3.canPlayAI() && list.size() == 0) {
                        return false;
                    } else {
                    	setTargetCard(mountains.get(0));
                        return true;
                    }
                }
                
                @Override
                public boolean canPlay() {
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };//SpellAbility ability1
            
            Input runtime = new Input() {
                private static final long serialVersionUID = -7823269301012427007L;
                
                @Override
                public void showMessage() {
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    
                    CardList lands = new CardList();
                    lands.addAll(play.getCards());
                    lands = lands.filter(new CardListFilter() {
                    	public boolean addCard(Card crd)
                    	{
                    		return crd.getType().contains("Mountain");
                    	}
                    });
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(ability1, lands, "Select target Mountain",
                            true, false));
                }//showMessage()
            };//Input
            
            ability1.setBeforePayMana(runtime);
            
            ability1.setDescription("+1: Untap target Mountain. It becomes a 4/4 red Elemental creature until end of turn. It's still a land.");
            //ability1.setStackDescription("");
            card.addSpellAbility(ability1);
            
            ability2.setDescription("-2: Add R to your mana pool for each Mountain you control.");
            ability2.setStackDescription("Koth of the Hammer - Add R to your mana pool for each Mountain you control.");
            card.addSpellAbility(ability2);
            
            ability3.setDescription("-5: You get an emblem with \"Mountains you control have 'tap: This land deals 1 damage to target creature or player.'\"");
            ability3.setStackDescription("Koth of the Hammer - You get an emblem with \"Mountains you control have �tap: This land deals 1 damage to target creature or player.'\"");
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }
        //*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Venser, the Sojourner")) {
            
            final int turn[] = new int[1];
            turn[0] = -1;
            
            final SpellAbility ability1 = new Ability(card, "0") {
                @Override
                public void resolve() {
                	final Card c = getTargetCard();
                	                	
                	if (c != null && AllZone.GameAction.isCardInPlay(c)) 
                	{		
                		 final Command eot = new Command() {

							private static final long serialVersionUID = -947355314271308770L;

							public void execute() {
                                 if(AllZone.GameAction.isCardExiled(c)) {
                                     PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, c.getOwner());
                                	 AllZone.GameAction.moveTo(play, AllZoneUtil.getCardState(c));
                                 }
                             }//execute()
                         };//Command
                		
	                    card.addCounterFromNonEffect(Counters.LOYALTY, 2);
	                    turn[0] = AllZone.Phase.getTurn();
	                    
	                    AllZone.GameAction.exile(c);
	                    AllZone.EndOfTurn.addAt(eot);
                	}
                	
                }
                
                @Override
                public boolean canPlayAI() {
                	CardList list = AllZoneUtil.getCardsInPlay();
                	list = list.filter(new CardListFilter()
                	{
                		public boolean addCard(Card c)
                		{
                			return CardFactoryUtil.canTarget(card, c) && c.getOwner().equals(AllZone.ComputerPlayer) &&
                				   !c.equals(card);
                		}
                	});
                	if (list.size() > 0) {
                		
                		CardList bestCards = list.filter(new CardListFilter()
                		{
                			public boolean addCard(Card c)
                			{
                				return c.getKeyword().contains("When CARDNAME enters the battlefield, draw a card.") ||
                					   c.getName().equals("Venerated Teacher") || c.getName().equals("Stoneforge Mystic") || c.getName().equals("Sun Titan") ||
                					   c.getType().contains("Ally");
                			}
                		});
                		
                		if (bestCards.size()>0) {
                			bestCards.shuffle();
                			setTargetCard(bestCards.get(0));
                		}
                		setTargetCard(list.get(0));
                	}
                	
                    return card.getCounters(Counters.LOYALTY) < 8 && list.size() > 0 &&
                    	   AllZone.Phase.getPhase().equals("Main2");
                }
                
                @Override
                public boolean canPlay() {
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    return 0 < card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
            };//SpellAbility ability1
            
            Input runtime = new Input() {
                private static final long serialVersionUID = 8609211991425118222L;
                
                @Override
                public void showMessage() {
                    CardList list = new CardList();
                    list.addAll(AllZone.Human_Battlefield.getCards());
                    list.addAll(AllZone.Computer_Battlefield.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isPermanent() && c.getOwner().equals(AllZone.HumanPlayer) 
                            	   && CardFactoryUtil.canTarget(card, c);
                        }
                    });
                    
                    stopSetNext(CardFactoryUtil.input_targetSpecific(ability1, list,
                            "Select target permanent you own", true, false));
                }//showMessage()
            };//Input
            
            
            final SpellAbility ability2 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 1);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    CardList list = AllZoneUtil.getCardsInPlay();
                    list = list.getType("Creature");
                    
                    for(int i = 0; i < list.size(); i++) {
                        final Card[] target = new Card[1];
                        target[0] = list.get(i);
                        
                        final Command untilEOT = new Command() {
							private static final long serialVersionUID = -7291011871465745495L;

							public void execute() {
                                if(AllZone.GameAction.isCardInPlay(target[0])) {
                                    target[0].removeExtrinsicKeyword("Unblockable");
                                }
                            }
                        };//Command
                        
                        if(AllZone.GameAction.isCardInPlay(target[0])) {
                            target[0].addExtrinsicKeyword("Unblockable");
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }//if
                    }//for
                    
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && card.getCounters(Counters.LOYALTY) >= 1
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                    
                	CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    list = list.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card crd)
                    	{
                    		return crd.isEmblem() && crd.getKeyword().contains("Whenever you cast a spell, exile target permanent.");
                    	}
                    });
                	
                    CardList creatList = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    creatList = creatList.filter(new CardListFilter()
                    {
                    	public boolean addCard(Card crd)
                    	{
                    		return CombatUtil.canAttack(crd);
                    	}
                    });
                    
                    return list.size() >= 1 && card.getCounters(Counters.LOYALTY) > 2 && creatList.size() >= 3 && AllZone.Phase.getPhase().equals("Main1");
                    
                }
            };//SpellAbility ability2
            
            
            //ability3
            final SpellAbility ability3 = new Ability(card, "0") {
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.LOYALTY, 8);
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Card emblem = new Card();
                    //should we even name this permanent?
                    //emblem.setName("Elspeth Emblem");
                    emblem.addIntrinsicKeyword("Indestructible");
                    emblem.addIntrinsicKeyword("Shroud");
                    emblem.addIntrinsicKeyword("Whenever you cast a spell, exile target permanent.");
                    emblem.setImmutable(true);
                    emblem.addType("Emblem");
                    emblem.setController(card.getController());
                    emblem.setOwner(card.getOwner());
                    
                    AllZone.GameAction.moveToPlay(emblem);        
                }
                
                @Override
                public boolean canPlay() {
                    return 8 <= card.getCounters(Counters.LOYALTY)
                            && AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && turn[0] != AllZone.Phase.getTurn()
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
                
                @Override
                public boolean canPlayAI() {
                	//multiple venser emblems are NOT redundant
                	/*
                    CardList list = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                    list = list.filter(new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isEmblem() && c.getKeyword().contains("Whenever you cast a spell, exile target permanent.");
                    	}
                    });
                    */
                	return card.getCounters(Counters.LOYALTY) > 8;
                }
            };
            
            ability1.setBeforePayMana(runtime);
            ability1.setDescription("+2: Exile target permanent you own. Return it to the battlefield under your control at the beginning of the next end step.");
            card.addSpellAbility(ability1);
            
            ability2.setDescription("-1: Creatures are unblockable this turn.");
            ability2.setStackDescription("Creatures are unblockable this turn.");
            card.addSpellAbility(ability2);
            
            ability3.setDescription("-8: You get an emblem with \"Whenever you cast a spell, exile target permanent.\"");
            StringBuilder stack3 = new StringBuilder();
            stack3.append(card.getName()).append("You get an emblem with \"Whenever you cast a spell, exile target permanent.\"");
            ability3.setStackDescription(stack3.toString());
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Tezzeret, Agent of Bolas")) {

        	//Planeswalker book-keeping
        	final int turn[] = new int[1];
        	turn[0] = -1;
                  
            //ability1
            /*
             * +1: Look at the top five cards of your library. You may reveal an artifact card 
             * from among them and put it into your hand. Put the rest on the bottom of your 
             * library in any order.
             */
        	Ability_Cost abCost1 = new Ability_Cost("AddCounter<1/LOYALTY>", cardName, true);
            final SpellAbility ability1 = new Ability_Activated(card, abCost1, null) {
				private static final long serialVersionUID = 3817068914199871827L;

				@Override
                public void resolve() {
                    turn[0] = AllZone.Phase.getTurn();
                    
                    Player player = card.getController();
                    if(player.equals(AllZone.HumanPlayer)) humanResolve();
                    else computerResolve();
                }//resolve()
                
                private void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList();
                    for(int i = 0; i < 5 && i < library.getCards().length; i++)
                        list.add(library.get(i));
                    
                    //optional, select an artifact
                    Object o = GuiUtils.getChoiceOptional("Select an artifact", list.toArray());
                    if(o != null && ((Card) o).isArtifact()) {
                        list.remove((Card) o);
                        AllZone.GameAction.moveToHand((Card) o);
                    }
                    
                    //put remaining cards on the bottom of the library
                    for(int i = 0; i < list.size(); i++)
                    	AllZone.GameAction.moveToBottomOfLibrary(list.get(i));

                }//resolve()
                
                private void computerResolve() {
                    //get top 5 cards of library
                    CardList top = new CardList();
                    int limit = AllZone.Computer_Library.getCards().length;
                    
                    for(int i = 0; i < 5 && i < limit; i++) {
                        top.add(AllZone.Computer_Library.get(0));
                    }
                    
                    //put artifact card in hand, if there is one
                    CardList art = top.getType("Artifact");
                    if(art.size() > 0) {
                    	Card best = CardFactoryUtil.AI_getBestArtifact(art);
                        top.remove(best);
                    	AllZone.GameAction.moveToHand(best);
                    }
                    
                    //put cards on bottom of library
                    for(int i = 0; i < top.size(); i++)
                    	AllZone.GameAction.moveToBottomOfLibrary(top.get(i));
                    
                }//computerResolve()
                
                @Override
                public boolean canPlayAI() {
                	return false;
                }
                
                @Override
                public boolean canPlay() {
                	//looks like standard Planeswalker stuff...
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability1.setDescription("+1: Look at the top five cards of your library. You may reveal an artifact card from among them and put it into your hand. Put the rest on the bottom of your library in any order.");
            StringBuilder stack1 = new StringBuilder();
            stack1.append(card.getName()).append(" - look at the top 5 cards of you library");
            ability1.setStackDescription(stack1.toString());
            
            //ability2
            /*
             * -1: Target artifact becomes a 5/5 artifact creature.
             */
            Ability_Cost abCost2 = new Ability_Cost("SubCounter<1/LOYALTY>", cardName, true);
            Target target = new Target(card, "Select target artifact", "Artifact".split(","));
            final SpellAbility ability2 = new Ability_Activated(card, abCost2, target) {
				private static final long serialVersionUID = -669957056327870077L;

				@Override
                public void resolve() {
					Card target = getTargetCard();
                	if(CardFactoryUtil.canTarget(card, target) && AllZoneUtil.isCardInPlay(target)) {
                		if(!target.isCreature()) {
                			target.addType("Creature");
                		}
                		target.setBaseAttack(5);
                		target.setBaseDefense(5);
                	}
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && card.getCounters(Counters.LOYALTY) >= 1
                            /* && turn[0] != AllZone.Phase.getTurn() */
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability2.setDescription("-1: Target artifact becomes a 5/5 artifact creature.");
            
            //ability3
            /*
             * Target player loses X life and you gain X life, where X is twice the number of artifacts you control.
             */
            Ability_Cost abCost3 = new Ability_Cost("SubCounter<4/LOYALTY>", cardName, true);
            Target target2 = new Target(card, "Select target player", "Player".split(","));
            final SpellAbility ability3 = new Ability_Activated(card, abCost3, target2) {
				private static final long serialVersionUID = 4407137815355315502L;

				@Override
                public void resolve() {
                	int numArts = AllZoneUtil.getPlayerTypeInPlay(card.getController(), "Artifact").size();
                    Player p = getTargetPlayer();
                    p.loseLife(2 * numArts, card);
                    card.getController().gainLife(2 * numArts, card);
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public boolean canPlay() {
                    return AllZone.getZone(card).is(Constant.Zone.Battlefield)
                            && card.getCounters(Counters.LOYALTY) >= 4
                            /* && turn[0] != AllZone.Phase.getTurn() */
                            && Phase.canCastSorcery(card.getController());
                }//canPlay()
            };
            ability3.setDescription("-4: Target player loses X life and you gain X life, where X is twice the number of artifacts you control.");
            
            card.addSpellAbility(ability1);
            card.addSpellAbility(ability2);
            card.addSpellAbility(ability3);
            
            card.setSVars(card.getSVars());
            card.setSets(card.getSets());
            
            return card;
        }//*************** END ************ END **************************
        
        
        return card;
    }
    
    
    // copies stats like attack, defense, etc..
    private static Card copyStats(Object o) {
        Card sim = (Card) o;
        Card c = new Card();
        
        c.setBaseAttack(sim.getBaseAttack());
        c.setBaseDefense(sim.getBaseDefense());
        c.setIntrinsicKeyword(sim.getKeyword());
        c.setName(sim.getName());
        c.setType(sim.getType());
        c.setText(sim.getSpellText());
        c.setManaCost(sim.getManaCost());
        
        return c;
    }// copyStats()
}
