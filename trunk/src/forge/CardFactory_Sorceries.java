package forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.esotericsoftware.minlog.Log;

import forge.gui.GuiUtils;

public class CardFactory_Sorceries {
    
    public static Card getCard(final Card card, final String cardName, Player owner) 
    {
    
    	 
        //*************** START *********** START **************************
        if(cardName.equals("Molten Rain")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8855786097956610090L;
                
                @Override
                public void resolve() {
                    Card c = getTargetCard();
                    if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(card, c)) {
                        if(!c.getType().contains("Basic")) c.getController().addDamage(2, card);
                        AllZone.GameAction.destroy(c);
                    }
                    
                }// resolve()
                
            };// Spell
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetType("Land", AllZone.Human_Battlefield));
            spell.setBeforePayMana(CardFactoryUtil.input_targetType(spell, "Land"));
        }// *************** END ************ END **************************
        

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
                    	Player p0 = crd0.getController();
                    	Player p1 = crd1.getController();
                    	AllZone.GameAction.changeController(new CardList(crd0), p0, p1);
                    	AllZone.GameAction.changeController(new CardList(crd1), p1, p0);
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
                    

                    if(c.isLand() && zone.is(Constant.Zone.Battlefield) && CardFactoryUtil.canTarget(card, c)) {
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
        else if(cardName.equals("Do or Die")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 8241241003478388362L;
                
                @Override
                public boolean canPlayAI() {
                    return 4 <= CardFactoryUtil.AI_getHumanCreature(card, true).size();
                }
                
                @Override
                public void resolve() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(getTargetPlayer());
                    
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
        else if(cardName.equals("Insurrection")) {
        	/*
        	 * Untap all creatures and gain control of them until end of
        	 * turn. They gain haste until end of turn.
        	 */
            final ArrayList<PlayerZone> orig = new ArrayList<PlayerZone>();
            final PlayerZone[] newZone = new PlayerZone[1];
            final ArrayList<Player> controllerEOT = new ArrayList<Player>();
            final ArrayList<Card> targets = new ArrayList<Card>();
            
            final Command untilEOT = new Command() {
				private static final long serialVersionUID = -5809548350739536763L;

				public void execute() {
                	int i = 0;
                	for(Card target:targets) {
                		//if card isn't in play, do nothing
                		if(!AllZone.GameAction.isCardInPlay(target)) continue;

                		AllZone.GameAction.changeController(new CardList(target), card.getController(), controllerEOT.get(i));

                		target.removeExtrinsicKeyword("Haste");

                		i++;
                	}
                }//execute()
            };//Command
            
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -532862769235091780L;

				@Override
                public void resolve() {
                	CardList creatures = AllZoneUtil.getCreaturesInPlay();
                	newZone[0] = AllZone.getZone(Constant.Zone.Battlefield, card.getController());;
                	int i = 0;
                	for(Card target:creatures) {
                		if(AllZone.GameAction.isCardInPlay(target)) {
                			orig.add(i, AllZone.getZone(target));
                			controllerEOT.add(i, target.getController());
                			targets.add(i, target);

                			AllZone.GameAction.changeController(new CardList(target), target.getController(), card.getController());

                			target.untap();
                			target.addExtrinsicKeyword("Haste");
                		}//is card in play?
                	}//end for
                	AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	CardList creatures = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
                    return creatures.size() > 0 && AllZone.Phase.getPhase().equals(Constant.Phase.Main1);
                }//canPlayAI()
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.setSVar("PlayMain1", "TRUE");
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
                    Player player = getTargetPlayer();
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    Card[] handChoices = hand.getCards();
                    if (handChoices.length > 0)
                    {
                        choice = CardUtil.getRandom(handChoices);
                        handChoices[0] = choice;
                        for(int i = 1; i < handChoices.length; i++) {
                            handChoices[i] = null;
                        }
                        GuiUtils.getChoice("Random card", handChoices);
                        player.addDamage(CardUtil.getConvertedManaCost(choice.getManaCost()), card);
                    }                                   
                }//resolve()
            };
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END ************************** 
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Cruel Ultimatum")) {
        	Ability_Cost abCost = new Ability_Cost("U U B B B R R", cardName, false);
        	Target tgt = new Target(card,"Select target opponent", "Opponent".split(","));
            final SpellAbility spell = new Spell(card,abCost, tgt) {

				private static final long serialVersionUID = -6598023699468746L;

				@Override
                public void resolve() {
					Player player = getTargetPlayer();
					Player you = card.getController();
					
					// target opponent Sacrifices a Creature
                    player.sacrificeCreature();
                                 
					// target Opponent Discards 3 Cards
                    player.discard(3, this, true);
                    
					// Opponent Loses 5 Life
			        player.loseLife(5, card);

					// Player Returns Creature Card from Graveyard to Hand
			        if(player.isHuman()) {                 	
			        	AllZone.Display.showMessage("Return a creature from your graveyard to your hand: ");
			        	ButtonUtil.enableOnlyCancel();
			        }
                    
                    CardList creature = AllZoneUtil.getPlayerGraveyard(you);
                    creature = creature.filter(AllZoneUtil.creatures);

                    Card c2 = null;
                    if(player.isHuman()){
                    	Card[] Target = new Card[creature.size()];
                    	for(int i = 0; i < creature.size(); i++) {
                    		Card crd = creature.get(i);
                    		Target[i] = crd;
                    	}
                    	Object check = GuiUtils.getChoiceOptional("Select creature", Target);
                    	if(check != null) {
                    		c2 = (Card) check;
                    	} 
                    } else {
                    	if(creature.size() > 0) {
                    		Card biggest = creature.get(0);
                    		for(int i = 0; i < creature.size(); i++)
                    			if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);                         
                    		c2 = biggest;
                    	}
                    }
                    PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, card.getController()); 	                        
                    if(AllZone.GameAction.isCardInZone(c2, grave)) {
                        AllZone.GameAction.moveToHand(c2);
                    }
					// Player Draws 3 Cards
                    you.drawCards(3);
                    
					// Player Gains 5 Life
                    you.gainLife(5, card);
			     
				} // Resolve

				public boolean canPlayAI() {
                    Player opponent = card.getController().getOpponent();	
                    PlayerZone Lib = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList Deck = new CardList();
                    Deck.addAll(Lib.getCards()); 
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, opponent);                   
                    CardList creature = new CardList();
                    creature.addAll(play.getCards());
                    creature = creature.getType("Creature"); 
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Graveyard, card.getController());
                    CardList creature2 = new CardList();
                    creature2.addAll(zone.getCards());
                    creature2 = creature2.getType("Creature");
                    return (Deck.size() > 2 && (opponent.getLife() <= 5 || (creature.size() > 0 && creature2.size() > 0)));
				}
            };//SpellAbility
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            //spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
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
                    Player player = card.getController();
					PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, player);
			        PlayerZone RFG = AllZone.getZone(Constant.Zone.Exile, player);
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
                Player player = card.getController();
                if(player.isHuman()){
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
                	check = GuiUtils.getChoiceOptional("Select Card to play for free", Choices);                   	
	                if(check != null) {
	                   target = ((Card) check);
	                }
	                if(target != null) c = AllZone.CardFactory.copyCard(target);
	                
					if(c != null) {
						if(c.isLand()) {
		   					if(player.canPlayLand()) {
		   						player.playLand(c);
		   					} else {
		   					JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "", JOptionPane.INFORMATION_MESSAGE);
		   					}
						} else if(c.isPermanent() == true && c.isAura() == false) {
							c.removeIntrinsicKeyword("Flash"); // Stops the player from re-casting the flash spell.
							
							StringBuilder sb = new StringBuilder();
							sb.append(c.getName()).append(" - Copied from Mind's Desire");
							PlayCreature.setStackDescription(sb.toString());
							
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
            		Player player = AllZone.Phase.getPlayerTurn();
            		PlayerZone Play = AllZone.getZone(Constant.Zone.Battlefield, player);
            		Card Minds_D = card;
            		if(player.isHuman()) card.getController().shuffle();
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
            			PlayerZone RFG = AllZone.getZone(Constant.Zone.Exile, player);
            			AllZone.GameAction.moveTo(RFG, c);
            			Minds_D.attachCard(c); 
            		}
            		final Card Minds = card;  
            		//	AllZone.GameAction.exile(Minds);   
            		Minds.setImmutable(true);
            		Command untilEOT = new Command() {
            			private static final long serialVersionUID = -28032591440730370L;

            			public void execute() {
            				Player player = AllZone.Phase.getPlayerTurn();
            				PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, player);
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
                	Player Player = card.getController();
                	GraveandLibrary.add(new CardList(AllZone.getZone(Constant.Zone.Library, Player).getCards()));
                	GraveandLibrary.add(new CardList(AllZone.getZone(Constant.Zone.Graveyard, Player).getCards()));
                	CardList NewLibrary = new CardList();
                	int Count = 5;
                	if(GraveandLibrary.size() < 5) Count = GraveandLibrary.size();
                	
                	for(int i = 0; i < Count; i++) {   
                	Card[] Search = GraveandLibrary.toArray();
                    AllZone.Display.showMessage("Select a card to put " + i + " from the top of the new library: "  + (Count - i) + " Choices to go.");
                    ButtonUtil.enableOnlyCancel();
                    Object check = GuiUtils.getChoice("Select a card: ", Search);   
                    NewLibrary.add((Card) check);
                    GraveandLibrary.remove((Card) check);
                    
                	}
                	
			        PlayerZone RFG = AllZone.getZone(Constant.Zone.Exile, Player);   
			        PlayerZone Library = AllZone.getZone(Constant.Zone.Library, Player);  
                    for(int i = 0; i < GraveandLibrary.size(); i++) AllZone.GameAction.moveTo(RFG,GraveandLibrary.get(i));
                    for(int i = 0; i < NewLibrary.size(); i++) AllZone.GameAction.moveTo(Library,NewLibrary.get(i));

                    //lose half life
                    Player player = AllZone.HumanPlayer;
                    player.loseLife(player.getLife() / 2,card);
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
                    PlayerZone Exile = AllZone.getZone(Constant.Zone.Exile, card.getController());
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
                   

                        GuiUtils.getChoice("Revealing top " + Count + " cards of library: ", cards.toArray());
                        //Human chooses
                        if(card.getController().equals(AllZone.ComputerPlayer)) {
                        	for(int i = 0; i < Count; i++) {
                        		if(stop == false) {
                        			choice = GuiUtils.getChoiceOptional("Choose cards to put into the first pile: ", cards.toArray());
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
                        
                    } 
                    else{//Computer chooses (It picks the highest converted mana cost card and 1 random card.)
                        Card biggest = Exiled.get(0);
                        
                        for(Card c : Exiled)
                            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) < CardUtil.getConvertedManaCost(c.getManaCost()))
                                biggest = c;

                        Pile1.add(biggest);
                        cards.remove(biggest);
                        if(cards.size() > 2) { 
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

			        	CardList chosen;	
			        	if (q.equals(0))
			        		chosen = Pile1;
			        	else
			        		chosen = Pile2;
			        	
			        	int numChosen = chosen.size();
                	  for( int i = 0; i < numChosen; i++) {
            			  Object check = GuiUtils.getChoiceOptional("Select spells to play in reverse order: ", chosen.toArray());
            			  if (check == null)
            				  break;
            			  
        				  Card playing = (Card)check;
        				  if(playing.isLand()) {
        					  if(card.getController().canPlayLand()) {
        						  card.getController().playLand(playing);
        					  } else {
        						  JOptionPane.showMessageDialog(null, "You can't play any more lands this turn.", "", JOptionPane.INFORMATION_MESSAGE);
        					  }
        				  } else {
        					  AllZone.GameAction.playCardNoCost(playing);
        				  }
        				  chosen.remove(playing);
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
        else if(cardName.equals("Feudkiller's Verdict")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5532477141899236266L;
                
                @Override
                public void resolve() {
                	Player player = card.getController();
                    player.gainLife(10, card);
                    
                    Player opponent = card.getController().getOpponent();
                    
                    if(opponent.getLife() < player.getLife()) makeToken();
                }//resolve()
                
                void makeToken() {
                    CardFactoryUtil.makeToken("Giant Warrior", "W 5 5 Giant Warrior", card.getController(), "W", new String[] {
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
                    if (AllZone.ComputerPlayer.getLife() <= 3)
                    	return false;
                    
                    if (AllZone.HumanPlayer.getLife() <= 3)
                    	return true;
                    
                    CardListFilter filter = new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isCreature() && (c.getNetDefense() - c.getDamage())< 4;
                    	}
                    };
                    
                    CardList humCreats = new CardList(AllZone.Human_Battlefield.getCards());
                    humCreats = humCreats.filter(filter);
                    
                    CardList compCreats = new CardList(AllZone.Computer_Battlefield.getCards());
                    compCreats = compCreats.filter(filter);
                    
                    return humCreats.size() > compCreats.size();
                    
                }
                
                @Override
                public void resolve() {
                    CardList all = AllZoneUtil.getCreaturesInPlay();
                    
                    for(int i = 0; i < all.size(); i++)
                        if(!all.get(i).getKeyword().contains("Flying")) {
                                all.get(i).setShield(0);
                                all.get(i).addDamage(3, card);
                        }
                    AllZone.HumanPlayer.addDamage(3, card);
                    AllZone.ComputerPlayer.addDamage(3, card);
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
                public void resolve() {
                    Player target = getTargetPlayer();
                    String choice = null;
                    
                    //human chooses
                    if(card.getController().isHuman()) {
                        choice = JOptionPane.showInputDialog(null, "Name a nonland card", cardName, JOptionPane.QUESTION_MESSAGE);
                        
                        CardList showLibrary = AllZoneUtil.getPlayerCardsInLibrary(target);
                        GuiUtils.getChoiceOptional("Target Player's Library", showLibrary.toArray());
                        
                        CardList showHand = AllZoneUtil.getPlayerHand(target);
                        GuiUtils.getChoiceOptional("Target Player's Hand", showHand.toArray());
                    }//if
                    else  //computer chooses
                    {
                        //the computer cheats by choosing a creature in the human players library or hand
                        CardList all = AllZoneUtil.getPlayerHand(target);
                        all.add(AllZoneUtil.getPlayerCardsInLibrary(target));
                        
                        CardList four = all.filter(new CardListFilter() {
                            public boolean addCard(Card c) {
                                if(c.isLand()) return false;
                                
                                return 3 < CardUtil.getConvertedManaCost(c.getManaCost());
                            }
                        });
                        if(!four.isEmpty()) choice = CardUtil.getRandom(four.toArray()).getName();
                        else choice = CardUtil.getRandom(all.toArray()).getName();
                        
                    }//else
                    remove(choice, target);
                    target.shuffle();
                }//resolve()
                
                void remove(String name, Player player) {
                    CardList all = AllZoneUtil.getPlayerHand(player);
                    all.add(AllZoneUtil.getPlayerGraveyard(player));
                    all.add(AllZoneUtil.getPlayerCardsInLibrary(player));
                    
                    for(int i = 0; i < all.size(); i++) {
                        if(all.get(i).getName().equals(name)) {
                            if(!all.get(i).isLand()) AllZone.GameAction.exile(all.get(i));
                        }
                    }
                }//remove()
                
                @Override
                public boolean canPlayAI() {
                    CardList c = AllZoneUtil.getPlayerCardsInLibrary(AllZone.HumanPlayer);
                    c = c.filter(AllZoneUtil.nonlands);
                    return c.size() > 0;
                }
            };//SpellAbility spell
            
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
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
                            CardList list = AllZoneUtil.getCardsInPlay();
                            
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
                    AllZone.Display.showMessage("Select target nonland permanent for " + spell.getSourceCard());
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card card, PlayerZone zone) {
                    if(zone.is(Constant.Zone.Battlefield) && !card.isLand()) {
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
        }//*************** END ************ END ***************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Erratic Explosion")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -6003403347798646257L;
                
                int                       damage           = 3;
                Card                      check;
                
                @Override
                public boolean canPlayAI() {
                    if(AllZone.HumanPlayer.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.HumanPlayer.getLife() <= damage) {
                        setTargetPlayer(AllZone.HumanPlayer);
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
                        getTargetPlayer().addDamage(damage, card);
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
                    change(AllZone.Human_Battlefield, card.getController());
                    change(AllZone.Computer_Battlefield, card.getController());
                }
                
                public void change(PlayerZone play, Player owner) {
                    Card[] c = play.getCards();
                    for(int i = 0; i < c.length; i++) {
                        if(c[i].isCreature() && !c[i].hasKeyword("Indestructible")) {
                            AllZone.GameAction.destroyNoRegeneration(c[i]);
                            CardFactoryUtil.makeToken("Spirit", "W 1 1 Spirit", c[i].getController(), "W", new String[] {
                                    "Creature", "Spirit"}, 1, 1, new String[] {"Flying"});
                        }
                    }
                }//change()
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Phyrexian Rebirth")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -9183346394081027286L;

				@Override
                public void resolve() {
                	int count = 0;
                	CardList play = AllZoneUtil.getCreaturesInPlay();
                	for(Card c:play) {
                		AllZone.GameAction.destroy(c);
                		if(!c.hasKeyword("Indestructible")) count++;
                	}
                	CardFactoryUtil.makeToken("Horror", "C X X Horror", card.getController(), "", new String[] {
                        "Artifact", "Creature", "Horror"}, count, count, new String[] {});
                }
                
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
                    CardList all = AllZoneUtil.getCardsInPlay();
                	int Soldiers = card.getXManaCostPaid();
                	for(int i = 0; i < Soldiers; i++) {
                    CardFactoryUtil.makeToken("Soldier", "W 1 1 Soldier", card.getController(), "W", new String[] {
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
                    CardList human = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
                    CardList computer = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
                    
                    // the computer will at least destroy 2 more human creatures
                    return (computer.size() < human.size() - 1
                            || (AllZone.ComputerPlayer.getLife() < 7 && !human.isEmpty())) && ComputerUtil.getAvailableMana().size() >= 7;
                }
            };// SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }// *************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Incendiary Command")) {
            //not sure what to call variables, so I just made up something
            final Player[] m_player = new Player[1];
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
                	//System.out.println(userChoice);
                	//System.out.println(m_land[0]);
                	//System.out.println(m_player[0]);
                	
                    //"Incendiary Command deals 4 damage to target player",
        			for(int i = 0; i <card.getChoices().size(); i++) {
        				if(card.getChoice(i).equals(cardChoice[0])) {
        					if(card.getChoiceTarget(0).equals(AllZone.HumanPlayer.getName())) {
        						setTargetPlayer(AllZone.HumanPlayer); 
        					}
        					else {
        						setTargetPlayer(AllZone.ComputerPlayer);
        					}
        					getTargetPlayer().addDamage(4, card);
        				}
        			}

                    //"Incendiary Command deals 2 damage to each creature",
                    if(userChoice.contains(cardChoice[1]) || card.getChoices().contains(cardChoice[1])) {
                        //get all creatures
                        CardList list = AllZoneUtil.getCreaturesInPlay();
                        

                        for(int i = 0; i < list.size(); i++) {
                            list.get(i).addDamage(2, card);
                        }
                    }
                    
                    //"Destroy target nonbasic land",
        			for(int i = 0; i <card.getChoices().size(); i++) {
        				if(card.getChoice(i).equals(cardChoice[2])) {
        			        PlayerZone Hplay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
        			        PlayerZone Cplay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);
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
                        discardDraw(AllZone.ComputerPlayer);
                        discardDraw(AllZone.HumanPlayer);
                    }
                }//resolve()
                
                void discardDraw(Player player) {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                    int n = hand.size();
                    
                    //technically should let the user discard one card at a time
                    //in case graveyard order matters
                    player.discard(n, this, true);
                    
                    player.drawCards(n);
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
                    if(c.isLand() && zone.is(Constant.Zone.Battlefield) && !c.getType().contains("Basic")) {
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
                public void selectPlayer(Player player) {
                	if(card.isCopiedSpell()) card.getChoiceTargets().remove(0);
                    m_player[0] = player;
                    spell.setTargetPlayer(player);
                    card.setSpellChoiceTarget(player.toString());
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
                    list.addAll(AllZone.Human_Battlefield.getCards());
                    list.addAll(AllZone.Computer_Battlefield.getCards());
                    
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
                    Object o = GuiUtils.getChoiceOptional("Choose Two", choices.toArray());
                    if(o == null) return null;
                    
                    out.add((String) o);
                    card.addSpellChoice((String) o);
                    choices.remove(out.get(0));
                    o = GuiUtils.getChoiceOptional("Choose Two", choices.toArray());
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
        else if(cardName.equals("Overwhelming Forces")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7165356050118574287L;
                
                @Override
                public void resolve() {
                    Player opponent = card.getController().getOpponent();
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, opponent);
                    
                    CardList all = new CardList(play.getCards());
                    all = all.getType("Creature");
                    
                    for(int i = 0; i < all.size(); i++) {
                        Card c = all.get(i);
                        if(c.isCreature()) AllZone.GameAction.destroy(c);
                        card.getController().drawCard();
                    }
                }//resolve()

                @Override
                public boolean canPlayAI() {
                    CardList human = new CardList(AllZone.Human_Battlefield.getCards());
                    
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
        else if(cardName.equals("Amnesia")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -5456164079438881319L;
                
                @Override
                public void resolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, getTargetPlayer());
                    Card[] c = hand.getCards();
                    
                    for(int i = 0; i < c.length; i++)
                        if(!c[i].isLand()) c[i].getController().discard(c[i], this);
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
                    card.getController().drawCards(2);
                }
                
                @Override
                public boolean canPlayAI() {
                    return AllZone.Computer_Hand.getCards().length <= 6;
                }
            };
            spell.setDescription("Target player draws two cards.");
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - ").append(card.getController()).append(" draws two cards.");
            spell.setStackDescription(sb.toString());
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "1 U PayLife<3>"));
            card.setFlashback(true);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Pulse of the Tangle")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 523613120207836692L;
                
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Beast", "G 3 3 Beast", card.getController(), "G",
                            new String[] {"Creature", "Beast"}, 3, 3, new String[] {""});
                    
                    //return card to hand if necessary
                    Player opponent = card.getController().getOpponent();
                    PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Battlefield, opponent);
                    PlayerZone myPlay = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    
                    CardList oppList = new CardList(oppPlay.getCards());
                    CardList myList = new CardList(myPlay.getCards());
                    
                    oppList = oppList.getType("Creature");
                    myList = myList.getType("Creature");
                    
                    //if true, return card to hand
                    if(myList.size() < oppList.size()) 
                    	AllZone.GameAction.moveToHand(card);

                }//resolve()
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Parallel Evolution")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3456160935845779623L;
                
                @Override
                public boolean canPlayAI() {
                    CardList humTokenCreats = new CardList();
                    humTokenCreats.addAll(AllZone.Human_Battlefield.getCards());
                    humTokenCreats = getTokenCreats(humTokenCreats);
                    
                    CardList compTokenCreats = new CardList();
                    compTokenCreats.addAll(AllZone.Computer_Battlefield.getCards());
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
                    AllTokenCreatures.addAll(AllZone.Human_Battlefield.getCards());
                    AllTokenCreatures.addAll(AllZone.Computer_Battlefield.getCards());
                    
                    AllTokenCreatures = getTokenCreats(AllTokenCreatures);
                    
                    CardFactoryUtil.copyTokens(AllTokenCreatures);
                
                }//resolve()
            };//SpellAbility
            
            spell.setDescription("For each creature token on the battlefield, its controller puts a token that's a copy of that creature onto the battlefield.");
            spell.setStackDescription("Parallel Evolution - For each creature token on the battlefield, its controller puts a token that's a copy of that creature onto the battlefield.");
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "4 G G G"));
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
                    CardFactoryUtil.makeToken("Bear", "G 2 2 Bear", card.getController(), "G", new String[] {"Creature", "Bear"},
                            2, 2, new String[] {""});
                }//resolve()
            };//SpellAbility
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getController()).append(" Puts 2/2 green Bear tokens onto the battlefield.");
            spell.setStackDescription(sb.toString());
            
            card.setFlashback(true);
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(CardFactoryUtil.ability_Flashback(card, "5 G G"));            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Delirium Skeins")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 7901561313373975648L;
                
                @Override
                public void resolve() {
                	AllZone.ComputerPlayer.discard(3, this, false);
                	AllZone.HumanPlayer.discard(3, this, false);
                }//resolve()
            };
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
                    } else if(card.isCreature() && zone.is(Constant.Zone.Battlefield)) {
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
            
            card.setSVar("PlayMain1", "TRUE");
            
            spell.setBeforePayMana(target);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Commune with Nature")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -7652317332073733242L;
                
                @Override
                public void resolve() {
                    Player player = card.getController();
                    if(player.equals(AllZone.HumanPlayer)) humanResolve();
                    else computerResolve();
                }
                
                public void computerResolve() {
                    //get top 5 cards of library
                    CardList top = new CardList();
                    int limit = AllZone.Computer_Library.getCards().length;
                    
                    for(int i = 0; i < 5 && i < limit; i++) {
                        top.add(AllZone.Computer_Library.get(0));
                    }
                    
                    //put creature card in hand, if there is one
                    CardList creature = top.getType("Creature");
                    if(creature.size() > 0) {
                    	Card best = CardFactoryUtil.AI_getBestCreature(creature);
                        top.remove(best);
                    	AllZone.GameAction.moveToHand(best);
                    }
                    
                    //put cards on bottom of library
                    for(int i = 0; i < top.size(); i++)
                    	AllZone.GameAction.moveToBottomOfLibrary(top.get(i));
                    
                }//computerResolve()
                
                public void humanResolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    CardList list = new CardList();
                    for(int i = 0; i < 5 && i < library.getCards().length; i++)
                        list.add(library.get(i));
                    
                    //optional, select a creature
                    Object o = GuiUtils.getChoiceOptional("Select a creature", list.toArray());
                    if(o != null && ((Card) o).isCreature()) {
                        list.remove((Card) o);
                        AllZone.GameAction.moveToHand((Card) o);
                    }
                    
                    //put remaining cards on the bottom of the library
                    for(int i = 0; i < list.size(); i++)
                    	AllZone.GameAction.moveToBottomOfLibrary(list.get(i));

                }//resolve()
            };
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
                    CardList land = new CardList(AllZone.Computer_Battlefield.getCards()).getType("Land");
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
                    if(c.isLand() && zone.is(Constant.Zone.Battlefield)
                            && c.getController().equals(AllZone.HumanPlayer)
                            /*&& c.getName().equals(humanBasic.get(count))*/
                            && c.getType().contains(humanBasic.get(count)) 
                            /*&& !saveList.contains(c) */) {
                        //get all other basic[count] lands human player controls and add them to target
                        PlayerZone humanPlay = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
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
                    CardList land = new CardList(AllZone.Human_Battlefield.getCards());
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
                    PlayerZone humanHand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
                    if(humanHand.size() >= 2) return true;
                    else return false;
                }
                
                @Override
                public void resolve() {
                    Player player = card.getController();
                    if(player.equals(AllZone.HumanPlayer)) humanResolve();
                    else computerResolve();
                }
                
                public void humanResolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer);
                    CardList list = new CardList(hand.getCards());
                    list.shuffle();
                    
                    if(list.size() == 0) return;
                    
                    Card c1 = list.get(0);
                    list.remove(c1);
                    c1.getController().discard(c1, null);
                    
                    if(list.size() == 0) return;
                    
                    Card c2 = list.get(0);
                    list.remove(c2);
                    
                    c2.getController().discard(c2, null);
                    
                    if(c1.getType().contains("Land")) {
                    	AllZone.HumanPlayer.gainLife(3, card);
                    }
                    
                    if(c2.getType().contains("Land")) {
                    	AllZone.HumanPlayer.gainLife(3, card);
                    }
                    

                }//resolve()
                
                public void computerResolve() {
                    PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer);
                    CardList list = new CardList(hand.getCards());
                    
                    if(list.size() > 0) {
                        
                        Object o = GuiUtils.getChoiceOptional("First card to discard", list.toArray());
                        
                        Card c = (Card) o;
                        list.remove(c);
                        
                        c.getController().discard(c, null);
                        
                        if(c.getType().contains("Land")) {
                        	AllZone.ComputerPlayer.gainLife(3, card);
                        }
                        
                        if(list.size() > 0) {
                            Object o2 = GuiUtils.getChoiceOptional("Second card to discard", list.toArray());
                            
                            Card c2 = (Card) o2;
                            list.remove(c2);
                            
                            c2.getController().discard(c2, null);
                            
                            if(c2.getType().contains("Land")) {
                            	AllZone.ComputerPlayer.gainLife(3, card);
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
                    Player player = getTargetPlayer();
                    // Move graveyard into library
                    CardList grave = AllZoneUtil.getPlayerGraveyard(player);
                    
                    for(Card c : grave){
                    	AllZone.GameAction.moveToLibrary(c);
                    }

                    // Shuffle library
                    player.shuffle();;
                }
                
                @Override
                public boolean canPlayAI()//97% of the time shuffling your grave into your library is a good thing
                {						 // ^--- over 2/3rd of the statistics in the world are made up
                    setTargetPlayer(AllZone.ComputerPlayer);
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
                    CardList lib = AllZoneUtil.getPlayerCardsInLibrary(card.getController());
                    
                    if (lib.size() == 0)
                    	return;
                    
                    if (lib.size() == 1){
                    	AllZone.GameAction.moveToHand(lib.get(0));
                    	return;
                    }
                    
                    CardList topTwo = new CardList();
                    topTwo.add(lib.get(0));
                    topTwo.add(lib.get(1));
                    
                    Card toHand = null;
                    if(card.getController().isHuman()) {
                        Object o = GuiUtils.getChoice("Select card to put in hand: ",
                                topTwo.toArray());
                        
                        toHand = (Card) o;
                    } 
                    else{
                    	toHand = CardUtil.getRandom(topTwo.toArray());
                    }    
                    topTwo.remove(toHand);
                    AllZone.GameAction.moveToHand(toHand);
                    
                    for(Card c : topTwo)	// Unnecessary for Sleight of Hand, but will be useful for other things
                    	AllZone.GameAction.moveToBottomOfLibrary(c);
                }
            };
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);           
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Invincible Hymn")) {
            final Player player = card.getController();
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -827136493013927725L;
                
                @Override
                public void resolve() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList libCards = new CardList(library.getCards());
                    int lifeGain = libCards.size();
                    
                    Log.debug("Invincible Hymn", "lifeGain: " + lifeGain);
                    
                    player.setLife(lifeGain, card);
                    
                    Log.debug("Invincible Hymn", "life.getLife(): " + player.getLife());
                }
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    CardList libCards = new CardList(library.getCards());
                    int lifeGain = libCards.size();
                    
                    if(lifeGain > AllZone.ComputerPlayer.getLife()) return true;
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
                    Player oppPlayer = card.getController().getOpponent();
                    CardList self = AllZoneUtil.getPlayerLandsInPlay(card.getController());
                    CardList opp = AllZoneUtil.getPlayerLandsInPlay(oppPlayer);
                    
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
        

        //*************** START *********** START **************************
        else if(cardName.equals("Innocent Blood")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 3915880400376059369L;
                
                @Override
                public void resolve() {
                    AllZone.HumanPlayer.sacrificeCreature();
                    AllZone.ComputerPlayer.sacrificeCreature();
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList hList = AllZoneUtil.getPlayerCardsInPlay(AllZone.HumanPlayer);
                    CardList cList = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
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
        else if(cardName.equals("Rite of Replication")) {
            SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -2902112019334177L;
                @Override
                public boolean canPlayAI() {
                    Card biggest = null;
                    CardList creature = AllZoneUtil.getCreaturesInPlay(card.getController());
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
                    
                    return biggest.getNetAttack() > 4;
                }
                
                @Override
                public void chooseTargetAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                    if(zone != null) {
                    CardList creature = new CardList();
                    creature.addAll(zone.getCards());
                    creature.addAll(AllZone.getZone(Constant.Zone.Battlefield, card.getController().getOpponent()).getCards());
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
                		PlayerZone_ComesIntoPlay.SimultaneousEntry = true;      
                		double Count = AllZoneUtil.getDoublingSeasonMagnitude(card.getController());
                		for(int i = 0; i < Count; i++) {       
                			if(i + 1 == Count) PlayerZone_ComesIntoPlay.SimultaneousEntry = false;
                			final Card Copy = AllZone.CardFactory.copyCardintoNew(getTargetCard());
                			
                			//Slight hack for copying stuff that has triggered abilities
                			for(Trigger t : Copy.getTriggers())
                			{
                				AllZone.TriggerHandler.registerTrigger(t);
                			}
                			Copy.addLeavesPlayCommand(new Command() {

								/**
								 * 
								 */
								private static final long serialVersionUID = 1988240749380718859L;

								public void execute() {
									AllZone.TriggerHandler.removeAllFromCard(Copy);
								}
                				
                			});
                			
                			Copy.setToken(true);
                			Copy.setController(card.getController());
                			AllZone.GameAction.moveToPlay(Copy, card.getController());
                		}
                	}             
                }//resolve()
            };
            
            spell.setDescription("Put a token onto the battlefield that's a copy of target creature.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - ").append(card.getController());
            sb.append(" puts a token onto the battlefield that's a copy of target creature.");
            spell.setStackDescription(sb.toString());
            
            SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = 13762512058673590L;
                @Override
                public boolean canPlayAI() {
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
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
                    PlayerZone zone = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
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
                		PlayerZone_ComesIntoPlay.SimultaneousEntry = true;
                		int Count = 5 * AllZoneUtil.getDoublingSeasonMagnitude(card.getController());
                		for(int i = 0; i < Count; i++) {
                			if(i + 1 == Count) PlayerZone_ComesIntoPlay.SimultaneousEntry = false;   
                			final Card Copy = AllZone.CardFactory.copyCardintoNew(getTargetCard());
                			
                			//Slight hack for copying stuff that has triggered abilities
                			for(Trigger t : Copy.getTriggers())
                			{
                				AllZone.TriggerHandler.registerTrigger(t);
                			}
                			Copy.addLeavesPlayCommand(new Command() {

								/**
								 * 
								 */
								private static final long serialVersionUID = -3703289691606291059L;

								public void execute() {
									AllZone.TriggerHandler.removeAllFromCard(Copy);
								}
                				
                			});
                			
                			Copy.setToken(true);
                			Copy.setController(card.getController());
                			AllZone.GameAction.moveToPlay(Copy);
                		}    
                	}            
                }//resolve()
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("7 U U");
            kicker.setAdditionalManaCost("5");
            kicker.setDescription("Kicker 5: If Rite of Replication was kicked, put five of those tokens onto the battlefield instead.");
            
            StringBuilder sbKick = new StringBuilder();
            sbKick.append(card.getName()).append(" - ").append(card.getController());
            sbKick.append(" puts five tokens onto the battlefield that's a copy of target creature.");
            kicker.setStackDescription(sbKick.toString());
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
                        CardFactoryUtil.makeToken("Kor Soldier", "W 1 1 Kor Soldier", card.getController(), "W", new String[] {
                                "Creature", "Kor", "Soldier"}, 1, 1, new String[] {""});
                    }//for
                }//resolve()
            };
            
            spell.setDescription("Put six 1/1 white Kor Soldier creature tokens onto the battlefield.");
            
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - ").append(card.getController());
            sb.append(" puts six 1/1 white Kor Soldier creature tokens onto the battlefield.");
            spell.setStackDescription(sb.toString());
            
            SpellAbility kicker = new Spell(card) {
                private static final long serialVersionUID = 1376255732058673590L;
                
                @Override
                public void resolve() {
                    card.setKicked(true);
                    for(int i = 0; i < 12; i++) {
                        CardFactoryUtil.makeToken("Kor Soldier", "W 1 1 Kor Soldier", card.getController(), "W", new String[] {
                                "Creature", "Kor", "Soldier"}, 1, 1, new String[] {""});
                    }//for
                }//resolve()
            };
            kicker.setKickerAbility(true);
            kicker.setManaCost("8 W W W");
            kicker.setAdditionalManaCost("6");
            kicker.setDescription("Kicker 6: If Conqueror's Pledge was kicked, put twelve of those tokens onto the battlefield instead.");
            
            StringBuilder sbkick = new StringBuilder();
            sbkick.append(card.getName()).append(" - ").append(card.getController());
            sbkick.append(" puts twelve 1/1 white Kor Soldier creature tokens onto the battlefield.");
            kicker.setStackDescription(sbkick.toString());
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.addSpellAbility(kicker);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Stitch in Time")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8869467398554803600L;

				@Override
                public void resolve() {
                	if(GameActionUtil.flipACoin(card.getController(), card)) {
                		AllZone.Phase.addExtraTurn(card.getController());
                	}
                }
            };
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************        
        

        //*************** START *********** START **************************
        else if(cardName.equals("Traumatize")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.HumanPlayer);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    
                    int max = AllZoneUtil.getPlayerCardsInLibrary(player).size() / 2;
                    
                    player.mill(max);
                }
            };//SpellAbility
            
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Mind Funeral")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                    Player player = AllZone.HumanPlayer;
                    setTargetPlayer(player);
                    PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
                    CardList libList = new CardList(lib.getCards());
                    return libList.size() > 0;
                }
                
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    
                    CardList libList = AllZoneUtil.getPlayerCardsInLibrary(player);
                    
                    int numLands = libList.getType("Land").size();
                    
                    int total = 0;
                    if (numLands > 3){	// if only 3 or less lands in the deck everything is going
	                    int landCount = 0;
	                    
	                    for(Card c : libList){
	                    	total++;
	                    	if (c.isLand()){
	                    		landCount++;
	                    		if (landCount == 4)
	                    			break;
	                    	}
	                    }
                    }
                    else{
                    	total = libList.size();
                    }
                    player.mill(total);
                }
            };//SpellAbility
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Haunting Echoes")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                	// Haunting Echoes shouldn't be cast if only basic land in graveyard or library is empty
                	CardList graveyard = AllZoneUtil.getPlayerGraveyard(AllZone.HumanPlayer);
                	CardList library = AllZoneUtil.getPlayerCardsInLibrary(AllZone.HumanPlayer);
                	int graveCount =  graveyard.size();
            		graveyard = graveyard.filter(new CardListFilter() {
        				public boolean addCard(Card c) {
        					return c.isBasicLand();
        				}
        			});
            		
            		setTargetPlayer(AllZone.HumanPlayer);
            		
                    return ((graveCount - graveyard.size() > 0) && library.size() > 0);
                }
                
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    
                    CardList grave = AllZoneUtil.getPlayerGraveyard(player);
                    grave = grave.getNotType("Basic");
                    
                    CardList lib = AllZoneUtil.getPlayerCardsInLibrary(player);

                    for(Card c : grave){
                    	CardList remLib = lib.getName(c.getName());
                    	for(Card rem : remLib){
                    		AllZone.GameAction.exile(rem);
                    		lib.remove(rem);
                    	}
                    	AllZone.GameAction.exile(c);
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
                    Player player = getTargetPlayer();
                    
                    //check for no cards in hand on resolve
                    CardList handList = AllZoneUtil.getPlayerHand(player);
                    
                    if (handList.size() == 0)
                    	return;
                    
                    if(card.getController().isHuman()) 
                    	GuiUtils.getChoice("Revealing hand", handList.toArray());
                    
                    CardList choices = handList.getNotType("Basic");
                    
                    if (choices.size() == 0)
                    	return;
                    
                    if(card.getController().isHuman()) 
                        choice = GuiUtils.getChoice("Choose", choices.toArray());
                    else //computer chooses
                    	choice = CardUtil.getRandom(choices.toArray());
                    
                    String name = choice.getName();
                    
                    CardList remove = AllZoneUtil.getPlayerCardsInLibrary(player);
                    remove.add(AllZoneUtil.getPlayerHand(player));
                    remove.add(AllZoneUtil.getPlayerGraveyard(player));
                    remove = remove.getName(name);
                    
                    for(Card c : remove)
                    	AllZone.GameAction.exile(c);
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	setTargetPlayer(AllZone.HumanPlayer);
                	CardList handList = AllZoneUtil.getPlayerHand(AllZone.HumanPlayer);
                    return 0 < handList.size();
                }
            };//SpellAbility spell
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        }//*************** END ************ END **************************          
        

        //*************** START *********** START **************************
        else if(cardName.equals("Identity Crisis")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = 42470566751344693L;
                
                @Override
                public boolean canPlayAI() {
                	CardList exiling = AllZoneUtil.getPlayerHand(AllZone.HumanPlayer);
                    return exiling.size() > 1;
                }
                
                @Override
                public void resolve() {
                    Player player = getTargetPlayer();
                    
                    CardList exiling = AllZoneUtil.getPlayerHand(player);
                    exiling.add(AllZoneUtil.getPlayerGraveyard(player));
                    
                    for(Card c : exiling)
                    	AllZone.GameAction.exile(c);
                }
            };//SpellAbility
            spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
            spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
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
                    	// Donate should target both the player and the creature
                        if(!c.isAura()) {
                        	AllZone.GameAction.changeController(new CardList(c), c.getController(), c.getController().getOpponent());

                        } else //Aura
                        {
                            c.setController(card.getController().getOpponent());
                        }
                    }
                }
                
                @Override
                public boolean canPlayAI() {
                    CardList list = new CardList(AllZone.Computer_Battlefield.getCards());
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
                    PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
                    
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
                    CardFactoryUtil.makeToken("Snake", "G 1 1 Snake", card.getController(), "G",
                            new String[] {"Creature", "Snake"}, 1, 1, new String[] {""});
                    CardFactoryUtil.makeToken("Wolf", "G 2 2 Wolf", card.getController(), "G", new String[] {"Creature", "Wolf"},
                            2, 2, new String[] {""});
                    CardFactoryUtil.makeToken("Elephant", "G 3 3 Elephant", card.getController(), "G", new String[] {
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
                 human_play.addAll(AllZone.Human_Battlefield.getCards());
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
                 computer_play.addAll(AllZone.Computer_Battlefield.getCards());
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
                    AllZone.GameAction.moveTo(AllZone.Human_Battlefield,c);
                    AllZone.GameAction.moveTo(AllZone.Human_Graveyard,c);
                 }
                 
                 it = human_graveyard.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Human_Graveyard,c);
                    AllZone.GameAction.moveTo(AllZone.Human_Battlefield,c);
                 }
                 
                 it = computer_play.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Computer_Battlefield,c);
                    AllZone.GameAction.moveTo(AllZone.Computer_Graveyard,c);
                 }
                 
                 it = computer_graveyard.iterator();
                 while(it.hasNext())
                 {
                    c = it.next();
                    AllZone.GameAction.moveTo(AllZone.Computer_Graveyard,c);
                    AllZone.GameAction.moveTo(AllZone.Computer_Battlefield,c);
                 }
                 
              }//resolve
           };//spellability
           card.clearSpellAbility();
            card.addSpellAbility(spell);
         }//*********************END**********END***********************
        
        
      //*************** START *********** START **************************
      else if(cardName.equals("Exhume"))
      {
    	  // Can this be converted to SP$ChangeZone | Hidden$ True | Defined$ Each ?
    	  final SpellAbility spell = new Spell(card)
    	  {
    		  private static final long serialVersionUID = 8073863864604364654L;

    		  public void resolve()
    		  {
    			  CardList humanList = AllZoneUtil.getPlayerGraveyard(AllZone.HumanPlayer);
    			  humanList = humanList.getType("Creature");
    			  CardList computerList = AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer);
    			  computerList = computerList.getType("Creature");

    			  Card c;
    			  if (humanList.size() > 0)
    			  {
    				  Object check = GuiUtils.getChoiceOptional("Select creature to Exhume", humanList.toArray());
    				  if (check!=null)
    				  {
    					  c = (Card)check;
    					  AllZone.GameAction.moveToPlay(c);
    				  }

    			  }

    			  if (computerList.size() > 0)
    			  {
    				  c = CardFactoryUtil.AI_getBestCreature(computerList);
    				  if (c != null)
    					  c = computerList.get(0);

    				  AllZone.GameAction.moveToPlay(c);
    			  }
    		  }

    		  public boolean canPlayAI()
    		  {   
    			  CardList humanList = AllZoneUtil.getPlayerGraveyard(AllZone.HumanPlayer);
    			  humanList = humanList.getType("Creature");
    			  CardList computerList = AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer);
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
    				  AllZone.HumanPlayer.drawCard();
    				  AllZone.ComputerPlayer.drawCard();
    			  }
    			  card.setXManaCostPaid(0);
    		  }
    		  public boolean canPlayAI()
    		  {
    			  return AllZone.Computer_Hand.size() < 5 && ComputerUtil.canPayCost("3 U");
    		  }
    	  };
    	  spell.setDescription("Each player draws X cards.");
    	  StringBuilder sb = new StringBuilder();
    	  sb.append(card).append(" - Each player draws X cards.");
    	  spell.setStackDescription(sb.toString());

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
                  all.addAll(AllZone.Human_Battlefield.getCards());
                  all.addAll(AllZone.Computer_Battlefield.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && c.getKeyword().contains("Flying");
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.HumanPlayer.addDamage(damage, card);
                  AllZone.ComputerPlayer.addDamage(damage, card);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - 1;
  				
  				if (AllZone.HumanPlayer.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && c.getKeyword().contains("Flying") && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList humanFliers = new CardList(AllZone.Human_Battlefield.getCards());
  			    humanFliers = humanFliers.filter(filter);
  			    
  			    CardList compFliers = new CardList(AllZone.Computer_Battlefield.getCards());
  			    compFliers = compFliers.filter(filter);
  			    
  			    return humanFliers.size() > (compFliers.size() + 2) && AllZone.ComputerPlayer.getLife() > maxX + 3;
  			}
      	  };
      	  StringBuilder sbDesc = new StringBuilder();
      	  sbDesc.append(cardName).append(" deals X damage to each creature with flying and each player.");
      	  spell.setDescription(sbDesc.toString());
      	  
      	  StringBuilder sbStack = new StringBuilder();
      	  sbStack.append(card).append(" - deals X damage to each creature with flying and each player.");
      	  spell.setStackDescription(sbStack.toString());
      	  
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
                  all.addAll(AllZone.Human_Battlefield.getCards());
                  all.addAll(AllZone.Computer_Battlefield.getCards());
                  all = all.filter(new CardListFilter()
                  {
                  	public boolean addCard(Card c)
                  	{
                  		return c.isCreature() && !c.getKeyword().contains(keyword[0]);
                  	}
                  });
                  
                  for(int i = 0; i < all.size(); i++)
                      	all.get(i).addDamage(card.getXManaCostPaid(), card);
                  
                  AllZone.HumanPlayer.addDamage(damage, card);
                  AllZone.ComputerPlayer.addDamage(damage, card);
                  
      			card.setXManaCostPaid(0);
      		}
  			public boolean canPlayAI()
  			{
  				final int maxX = ComputerUtil.getAvailableMana().size() - CardUtil.getConvertedManaCost(card);
  				
  				if (AllZone.HumanPlayer.getLife() <= maxX)
  					return true;
  				
  				CardListFilter filter = new CardListFilter(){
  					public boolean addCard(Card c)
  					{
  						return c.isCreature() && !c.getKeyword().contains(keyword) && maxX >= (c.getNetDefense() + c.getDamage());
  					}
  				};
  				
  				CardList human = new CardList(AllZone.Human_Battlefield.getCards());
  			    human = human.filter(filter);
  			    
  			    CardList comp = new CardList(AllZone.Computer_Battlefield.getCards());
  			    comp = comp.filter(filter);
  			    
  			    return human.size() > (comp.size() + 2) && AllZone.ComputerPlayer.getLife() > maxX + 3;
  			}
      	  };
      	  StringBuilder sbDesc = new StringBuilder();
      	  sbDesc.append(cardName).append(" deals X damage to each creature without ");
      	  sbDesc.append(keyword[0]).append(" and each player.");
      	  spell.setDescription(sbDesc.toString());
      	  
      	  StringBuilder sbStack = new StringBuilder();
      	  sbStack.append(card).append(" - deals X damage to each creature without ");
      	  sbStack.append(keyword[0]).append(" and each player.");
      	  spell.setStackDescription(sbStack.toString());
      	  
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
        			all.addAll(AllZone.Human_Battlefield.getCards());
        			all.addAll(AllZone.Computer_Battlefield.getCards());
        			all = all.filter(new CardListFilter()
        			{
        				public boolean addCard(Card c)
        				{
        					return c.isCreature();
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
        					return c.isCreature() && maxX >= (c.getNetDefense() + c.getDamage());
        				}
        			};

        			CardList humanAll = new CardList(AllZone.Human_Battlefield.getCards());
        			humanAll = humanAll.filter(filter);

        			CardList compAll = new CardList(AllZone.Computer_Battlefield.getCards());
        			compAll = compAll.filter(filter);

        			return humanAll.size() > (compAll.size() + 2);
        		}
        	};
        	StringBuilder sbDesc = new StringBuilder();
        	sbDesc.append(cardName).append(" deals X damage to each creature.");
        	spell.setDescription(sbDesc.toString());

        	StringBuilder sbStack = new StringBuilder();
        	sbStack.append(cardName).append(" - deals X damage to each creature.");
        	spell.setStackDescription(sbStack.toString());

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
        			getTargetPlayer().gainLife(card.getXManaCostPaid(), card);
        			card.setXManaCostPaid(0);
        		}

        		public boolean canPlayAI()
        		{
        			int humanLife = AllZone.HumanPlayer.getLife();
        			int computerLife = AllZone.ComputerPlayer.getLife();

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
        
        
        //*************** START *********** START **************************
        else if (cardName.equals("Lavalanche"))
        {
        	final SpellAbility spell = new Spell(card)
        	{
        		private static final long serialVersionUID = 3571646571415945308L;
        		public void resolve()
        		{
        			int damage = card.getXManaCostPaid();

        			Player player = getTargetPlayer();
        			PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, player);
        			CardList list = new CardList(play.getCards());

        			list = list.filter(new CardListFilter()
        			{
        				public boolean addCard(Card c)
        				{
        					return c.isCreature();
        				}
        			});

        			for(int i = 0; i < list.size(); i++) {
        				list.get(i).addDamage(card.getXManaCostPaid(), card);
        			}

        			player.addDamage(damage, card);
        			card.setXManaCostPaid(0);
        		}
        		public boolean canPlayAI()
        		{
        			final int maxX = ComputerUtil.getAvailableMana().size() - 3;

        			if (AllZone.HumanPlayer.getLife() <= maxX)
        				return true;

        			CardListFilter filter = new CardListFilter(){
        				public boolean addCard(Card c)
        				{
        					return c.isCreature() && maxX >= (c.getNetDefense() + c.getDamage());
        				}
        			};

        			CardList killableCreatures = new CardList(AllZone.Human_Battlefield.getCards());
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
        }//*************** END ************ END **************************
                 
        
        //*************** START *********** START **************************
        else if (cardName.equals("Psychic Drain"))
        {
        	final SpellAbility spell = new Spell(card){
        		private static final long serialVersionUID = -5739635875246083152L;

        		public void resolve()
        		{
        			getTargetPlayer().mill(card.getXManaCostPaid());
        			
        			card.getController().gainLife(card.getXManaCostPaid(), card);
        			
        			card.setXManaCostPaid(0);
        		}
      		  
        		public boolean canPlayAI()
        		{
        			PlayerZone lib = AllZone.getZone(Constant.Zone.Library, AllZone.HumanPlayer);
        			CardList libList = new CardList(lib.getCards());
      			  
        			int humanLife = AllZone.HumanPlayer.getLife();
        			int computerLife = AllZone.ComputerPlayer.getLife();
      			  
        			final int maxX = ComputerUtil.getAvailableMana().size() - 2;
        			return (maxX >= 3) && (humanLife >= computerLife) && (libList.size() > 0);
        		}
        	};
        	spell.setDescription("Target player puts the top X cards of his or her library into his or her graveyard and you gain X life.");
        	spell.setStackDescription("Psychic Drain - Target player puts the top X cards of his or her library into his or her graveyard and you gain X life.");
        	spell.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
        	spell.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spell));
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Balance"))
        {
        	final SpellAbility spell = new Spell(card)
        	{
				private static final long serialVersionUID = -5941893280103164961L;

				public void resolve()
        		{
					//Lands:
					CardList humLand = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
        			CardList compLand = AllZoneUtil.getPlayerLandsInPlay(AllZone.ComputerPlayer);
        			
        			if (compLand.size() > humLand.size())
        			{
        				compLand.shuffle();
        				for (int i=0; i < compLand.size()-humLand.size();i++)
        					AllZone.GameAction.sacrifice(compLand.get(i));
        			}
        			else if (humLand.size() > compLand.size())
        			{
        				int diff = humLand.size() - compLand.size();
        				AllZone.InputControl.setInput(CardFactoryUtil.input_sacrificePermanents(diff, "Land"));
        			}
        			
        			//Hand
        			CardList humHand = AllZoneUtil.getPlayerHand(AllZone.HumanPlayer);
        			CardList compHand = AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer);
        			int handDiff = Math.abs(humHand.size() - compHand.size());
        			
        			if (compHand.size() > humHand.size())
        			{
        				AllZone.ComputerPlayer.discard(handDiff, this, false);
        			}
        			else if (humHand.size() > compHand.size())
        			{
        				AllZone.HumanPlayer.discard(handDiff, this, false);
        			}
        			
        			//Creatures:
        			CardList humCreats = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
        			CardList compCreats = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
        				
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
        				AllZone.InputControl.setInput(CardFactoryUtil.input_sacrificePermanents(diff, "Creature"));
        			}
        		}
        		
        		public boolean canPlayAI()
        		{
        			int diff = 0;
        			CardList humLand = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
        			CardList compLand = AllZoneUtil.getPlayerLandsInPlay(AllZone.ComputerPlayer);
        			diff += humLand.size() - compLand.size();
        			
        			CardList humCreats = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
        			CardList compCreats = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
        			compCreats = compCreats.getType("Creature");
        			diff += 1.5 * (humCreats.size() - compCreats.size());
        			
        			CardList humHand = AllZoneUtil.getPlayerHand(AllZone.HumanPlayer);
        			CardList compHand = AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer);
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
            
            StringBuilder sb = new StringBuilder();
            sb.append(cardName).append(" adds W U B R G to your mana pool");
            spell.setStackDescription(sb.toString());
           
            card.clearSpellAbility();
            card.addSpellAbility(spell);
           
            return card;
        }//*************** END ************ END **************************
        
        /*
        //*************** START *********** START **************************
        else if(cardName.equals("Riding the Dilu Horse"))
        {
        	SpellAbility spell = new Spell(card)
        	{
        		private static final long serialVersionUID = -620930445462994580L;


        		public boolean canPlayAI()
        		{
        			PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);

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
		*/
        
        //*************** START *********** START **************************
        else if(cardName.equals("Summer Bloom"))
        {
       	final SpellAbility spell = new Spell(card) {
			private static final long serialVersionUID = 5559004016728325736L;

			public boolean canPlayAI() {
   				// The computer should only play this card if it has at least 
   				// one land in its hand. Because of the way the computer turn
   				// is structured, it will already have played land to it's limit
   				
   				CardList hand = AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer);
   				hand = hand.getType("Land");
   				return hand.size() > 0;
   			}
   			
   			public void resolve() {
   				final Player thePlayer = card.getController();
   				thePlayer.addMaxLandsToPlay(3);
   				
   				Command untilEOT = new Command()
   				{
					private static final long serialVersionUID = 1665720009691293263L;

					public void execute(){
						thePlayer.addMaxLandsToPlay(-3);
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
        					AllZone.ComputerPlayer);

        			CardList list = new CardList(hand.getCards());

        			list = list.getType("Land");
        			if (list.size() > 0)
        				return true;
        			else
        				return false;
        		}

        		public void resolve() {
        			final Player thePlayer = card.getController();
        			thePlayer.addMaxLandsToPlay(1);

        			Command untilEOT = new Command()
        			{

        				private static final long serialVersionUID = -2618916698575607634L;

        				public void execute(){
        					thePlayer.addMaxLandsToPlay(-1);
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
        else if(cardName.equals("Hellion Eruption")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 5820870438419741058L;

				@Override
				public boolean canPlayAI() {
            		return AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer).size() > 0;
            	}
				
                @Override
                public void resolve() {
                	CardList cards = AllZoneUtil.getCreaturesInPlay(card.getController());
                	for(Card creature:cards) {
                            AllZone.GameAction.sacrifice(creature);
                            CardFactoryUtil.makeToken("Hellion", "R 4 4 hellion", creature.getController(), "R", new String[] {
                                    "Creature", "Hellion"}, 4, 4, new String[] {""});
                    }
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
        			return AllZone.HumanPlayer.getLife() > AllZone.ComputerPlayer.getLife();
        		}

        		@Override
        		public void resolve() {
        			int humanLife = AllZone.HumanPlayer.getLife();
        			int compLife = AllZone.ComputerPlayer.getLife();
        			if( humanLife > compLife ) {
        				AllZone.HumanPlayer.setLife(compLife, card);
        			}
        			else if( compLife > humanLife ) {
        				AllZone.ComputerPlayer.setLife(humanLife, card);
        			}
        			else {
        				//they must be equal, so nothing to do
        			}
        		}
        	};//SpellAbility
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(card.getName()).append(" - Each player's life total becomes the lowest life total among all players.");
        	spell.setStackDescription(sb.toString());
        	
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
					Player player = card.getController();
					Player tPlayer = getTargetPlayer();
					PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
					CardList graveList = new CardList(grave.getCards());
					
					graveList = graveList.getType("Creature");
					
					int size = graveList.size();
					int damage = 0;
					
					if( player.equals(AllZone.HumanPlayer)) {
						for(int i = 0; i < size; i++) {
							Object o = GuiUtils.getChoice("Remove from game", graveList.toArray());
							if(o == null) break;
							damage++;	// tally up how many cards removed
							Card c_1 = (Card) o;
							graveList.remove(c_1); //remove from the display list
							AllZone.GameAction.exile(c_1);
						}
					}
					else { //Computer
						// it would be nice if the computer chose vanilla creatures over 
						for(int j = 0; j < size; j++) {
							AllZone.GameAction.exile(graveList.get(j));
						}
					}
					tPlayer.addDamage(damage, card);
				}
				
				@Override
        		public void chooseTargetAI() {
        			setTargetPlayer(AllZone.HumanPlayer);
        		}//chooseTargetAI()
				
				@Override
        		public boolean canPlayAI() {
					Player player = getTargetPlayer();
        			PlayerZone grave = AllZone.getZone(Constant.Zone.Library, player);
        			CardList graveList = new CardList(grave.getCards());
        			graveList = graveList.getType("Creature");
        			int humanLife = AllZone.HumanPlayer.getLife();

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
        			Player player = card.getController();
        			Player target = player.getOpponent();

        			CardList handList = AllZoneUtil.getPlayerHand(target);
        			if(handList.size() == 0) 
        				return;

        			//choose one card from it
        			Card perish = null;
        			if(player.equals(AllZone.HumanPlayer)){ 
        				Object o = GuiUtils.getChoice("Put into library", handList.toArray());
        				//if(o == null) break;
        				perish = (Card) o;
        			}
        			else  //computer
        				perish = CardUtil.getRandom(handList.toArray());

        			if (perish == null)
        				return;
        			
        			AllZone.GameAction.moveToLibrary(perish);
        			target.shuffle();
        		}

        		@Override
        		public boolean canPlayAI() {
        			return AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer).size() > 0;
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
        			AllZone.ComputerPlayer.drawCards(3);
        			AllZone.HumanPlayer.drawCards(3);
        			
        			//now, each player discards 3 cards at random
        			AllZone.ComputerPlayer.discardRandom(3, this);
        			AllZone.HumanPlayer.discardRandom(3, this);
        		}

        		@Override
        		public boolean canPlayAI() {
        			return AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer).size() > 0;
        		}
        	};
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(cardName).append(" - each player draws 3 cards, then discards 3 cards at random.");
        	spell.setStackDescription(sb.toString());
        	
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
        			if (AllZoneUtil.getPlayerCardsInPlay(card.getController(), "Eldrazi Spawn").size() > 0)
        				times = 3;
        			for (int i=0;i<times;i++)
        			{
	        			cl = CardFactoryUtil.makeToken("Eldrazi Spawn", "C 0 1 Eldrazi Spawn", card.getController(), "", new String[] {
								"Creature", "Eldrazi", "Spawn"}, 0, 1, new String[] {});
	        			for (Card crd:cl)
	        				crd.addSpellAbility(CardFactoryUtil.getEldraziSpawnAbility(crd));
        			}
        		}
        	};
        	StringBuilder sb = new StringBuilder();
        	sb.append(cardName).append(" - ").append(card.getController());
        	sb.append(" puts one or three 0/1 Eldrazi Spawn creature tokens onto the battlefield.");
        	spell.setStackDescription(sb.toString());
        	
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        else if(cardName.equals("All Is Dust")) {
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
        			CardList human = AllZoneUtil.getPlayerCardsInPlay(AllZone.HumanPlayer);
        			human = human.filter(colorless);
        			human = human.getNotKeyword("Indestructible");
        			CardList computer = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
        			computer = computer.filter(colorless);
        			computer = computer.getNotKeyword("Indestructible");

        			Log.debug("All Is Dust", "Current phase:" + AllZone.Phase.getPhase());
        			// the computer will at least destroy 2 more human permanents
        			return  AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && 
        				(computer.size() < human.size() - 1
        				|| (AllZone.ComputerPlayer.getLife() < 7 && !human.isEmpty()));
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
        			CardList humanLands = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
        			CardList compLands = AllZoneUtil.getPlayerLandsInPlay(AllZone.ComputerPlayer);
        			
        			AllZone.ComputerPlayer.addDamage(compLands.size(), card);
        			AllZone.HumanPlayer.addDamage(humanLands.size(), card);
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			CardList human = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
        			CardList comp = AllZoneUtil.getPlayerLandsInPlay(AllZone.ComputerPlayer);
        			
        			if(AllZone.HumanPlayer.getLife() <= human.size() ) {
        				return true;
        			}
        			
        			if( AllZone.ComputerPlayer.getLife() >= comp.size() && human.size() > comp.size()+2 ) {
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
                    if(AllZone.HumanPlayer.getLife() <= damage) return true;
                    
                    check = getFlying();
                    return check != null;
                }
                
                @Override
                public void chooseTargetAI() {
                    if(AllZone.HumanPlayer.getLife() <= damage) {
                        setTargetPlayer(AllZone.HumanPlayer);
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
                        getTargetPlayer().addDamage(damage, card);
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
                    	GuiUtils.getChoice("Revealed cards:", revealed.toArray());
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
        else if(cardName.equals("Strategic Planning")) {
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -1481868510981621671L;
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    CardList top = new CardList();
                    PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
                    
                    int j = 3;
                    
                    if(library.size() < j) j = library.size();
                    for(int i = 0; i < j; i++) {
                        top.add(library.get(i));
                    }
                    
                    if(top.size() > 0) {
                        //let user get choice
                        Card chosen = GuiUtils.getChoice("Choose a card to put into your hand",
                                top.toArray());
                        top.remove(chosen);
                        
                        //put card in hand
                        AllZone.GameAction.moveToHand(chosen);
                        
                        //add cards to bottom of library
                        for(int i = 0; i < top.size(); i++)
                        	AllZone.GameAction.moveToGraveyard(top.get(i));
                    }
                }//resolve()
            };//SpellAbility
            
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
        	final CardList targets = new CardList();
        	final ArrayList<Player> targetPlayers = new ArrayList<Player>();

        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -6293612568525319357L;

				@Override
        		public boolean canPlayAI() {
					final int maxX = ComputerUtil.getAvailableMana().size() - 1;
					int humanLife = AllZone.HumanPlayer.getLife();
					if(maxX >= humanLife) {
						targetPlayers.add(AllZone.HumanPlayer);
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
        			if(card.getController().equals(AllZone.ComputerPlayer)) {
        				StringBuilder sb = new StringBuilder();
        				sb.append(cardName+" - Computer causes "+damage+" to:\n\n");
        				for(int i = 0; i < targets.size(); i++) {
        					Card target = targets.get(i);
            				if(AllZone.GameAction.isCardInPlay(target) && CardFactoryUtil.canTarget(card, target)) {
            					sb.append(target+"\n");
            				}
            			}
            			for(int i = 0; i < targetPlayers.size(); i++) {
            				Player p = targetPlayers.get(i);
            				if( p.canTarget(card) ) {
            					sb.append(p+"\n");
            				}
            			}
        				javax.swing.JOptionPane.showMessageDialog(null, sb.toString());
        			}
        			for(int i = 0; i < targets.size(); i++) {
        				Card target = targets.get(i);
        				if(AllZone.GameAction.isCardInPlay(target) && CardFactoryUtil.canTarget(card, target)) {
        					//DEBUG
        					Log.debug("Fireball", "Fireball does "+damage+" to: "+target);
        					target.addDamage(damage, card);
        				}
        			}
        			for(int i = 0; i < targetPlayers.size(); i++) {
        				Player p = targetPlayers.get(i);
        				if( p.canTarget(card) ) {
        					//DEBUG
        					Log.debug("Fireball", "Fireball does "+damage+" to: "+p);
        					p.addDamage(damage, card);
        				}
        			}
        		}//resolve()
        		
        		//DEBUG
        		private void printCardTargets() {
        			StringBuilder sb = new StringBuilder("[");
        			for(Card target:targets) {
        				sb.append(target).append(",");
        			}
        			sb.append("]");
        			Log.debug("Fireball", sb.toString());
        		}
        		//DEBUG
        		private void printPlayerTargets() {
        			StringBuilder sb = new StringBuilder("[");
        			for(Player p:targetPlayers) {
        				sb.append(p).append(",");
        			}
        			sb.append("]");
        			Log.debug("Fireball", sb.toString());
        		}
        		
        		private int getNumTargets() {
        			int numTargets = 0;
        			numTargets += targets.size();
        			numTargets += targetPlayers.size();
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
        			numTargets += targets.size();
        			numTargets += targetPlayers.size();
        			//DEBUG
        			Log.debug("Fireball", "Fireball - numTargets = "+numTargets);
        			return numTargets;
        		}

				@Override
				public void selectButtonCancel() {
					targets.clear();
					targetPlayers.clear();
					stop(); 
				}

        		@Override
        		public void selectButtonOK() {
        			spell.setStackDescription(cardName+" deals X damage to "+getNumTargets()+" target(s).");
					stopSetNext(new Input_PayManaCost(spell));
        		}

        		@Override
        		public void selectCard(Card c, PlayerZone zone) {
        			if( !CardFactoryUtil.canTarget(card, c)) {
        				AllZone.Display.showMessage("Cannot target this card.");
    					return; //cannot target
        			}
        			if(targets.contains(c)) {
        				AllZone.Display.showMessage("You have already selected this target.");
        				return; //cannot target the same creature twice.
        			}

        			if(c.isCreature() && zone.is(Constant.Zone.Battlefield)) {
        				targets.add(c);
        				showMessage();
        			}
        		}//selectCard()
        		
        		@Override
                public void selectPlayer(Player player) {
        			if( !player.canTarget(card) ) {
        				AllZone.Display.showMessage("Cannot target this card.");
    					return; //cannot target
        			}
        			if( targetPlayers.contains(player) ) {
        				AllZone.Display.showMessage("You have already selected this player.");
        				return; //cannot target the same player twice.
        			}
                    targetPlayers.add(player);
                    showMessage();
                }
        	};//Input

        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        	spell.setBeforePayMana(input);
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Energy Tap")) {
        	/*
        	 * Tap target untapped creature you control. If you do, add X to
        	 * your mana pool, where X is that creature's converted mana cost.
        	 */
        	Ability_Cost cost = new Ability_Cost("U", cardName, false);
        	Target tgt = new Target(card,"Select an untapped creature you control", "Creature.untapped+YouCtrl".split(","));
        	final SpellAbility spell = new Spell(card, cost, tgt) {

				private static final long serialVersionUID = 8883585452278041848L;

				@Override
        		public void resolve() {
        			Card target = getTargetCard();
        			if(null != target && target.isUntapped()) {
        				int cmc = CardUtil.getConvertedManaCost(target);
        				target.tap();
        				Ability_Mana abMana = new Ability_Mana(card, "0", "1", cmc) {
        					private static final long serialVersionUID = -2182129023960978132L;
        				};
        				abMana.produceMana();
        			}
        		}

        		@Override
        		public boolean canPlayAI() {
        			return false;
        		}
        	};

        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
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
                        PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                        AllZone.GameAction.moveTo(play, c);
                        c.setController(card.getController());
                    }
                    c.getController().loseLife(cmc,card);
                }//resolve()
                
                @Override
                public boolean canPlay() {
                    return super.canPlay() && getCreatures().size() > 0;
                }
                
                public CardList getCreatures() {
                    CardList creatures = AllZoneUtil.getCardsInGraveyard();
                    creatures = creatures.getType("Creature");
                    creatures = creatures.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                        	if (c.getCMC() >= AllZone.ComputerPlayer.getLife()) return false;
                            if (c.getKeyword().contains("At the beginning of the end step, sacrifice CARDNAME.")) return false;
                            return true;
                        }
                    });
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
                    Object check = GuiUtils.getChoiceOptional("Select creature", getCreatures());
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
        			final Player player = card.getController();
        			int maxCards = AllZoneUtil.getPlayerHand(player).size();
        			if(numCards != 0) {
        				numCards = Math.min(numCards, maxCards);
        			if(player.equals(AllZone.HumanPlayer)) {
        				AllZone.InputControl.setInput(CardFactoryUtil.input_discardRecall(numCards, card, this));
        			}
           			}
        			/*else { //computer
        				card.getControler().discardRandom(numCards);
        				AllZone.GameAction.exile(card);
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
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(card.getName()).append(" - discard X cards and return X cards to your hand.");
        	spell.setStackDescription(sb.toString());
        	
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
        			CardList Hhand = new CardList(AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer).getCards());
        			CardList Chand = new CardList(AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer).getCards());
        			return Chand.size() < Hhand.size();
        		}

        		@Override
        		public void resolve() {
        			discardDraw(AllZone.HumanPlayer);
        			discardDraw(AllZone.ComputerPlayer);
        		}//resolve()

        		void discardDraw(Player player) {
        			PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
        			CardList Hhand = new CardList(AllZone.getZone(Constant.Zone.Hand, AllZone.HumanPlayer).getCards());
        			CardList Chand = new CardList(AllZone.getZone(Constant.Zone.Hand, AllZone.ComputerPlayer).getCards());
        			int draw;
        			if(Hhand.size() >= Chand.size()) {
        				draw = Hhand.size();
        			} else {
        				draw = Chand.size();
        			}
        			Card[] c = hand.getCards();
        			for(int i = 0; i < c.length; i++)
        				c[i].getController().discard(c[i], null);

        			player.drawCards(draw);
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
                            PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
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
                    Object check = GuiUtils.getChoiceOptional("Select creature", getCreatures());
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
        else if(cardName.equals("Patriarch's Bidding")) {
            final String[] input = new String[2];
            
            final SpellAbility spell = new Spell(card) {
                private static final long serialVersionUID = -2182173662547136798L;

                @Override
                public void resolve() {
                		input[0] = "";
                		while(input[0] == "") {
                			input[0] = JOptionPane.showInputDialog(null, "Which creature type?", "Pick type",
                                JOptionPane.QUESTION_MESSAGE);
                			if(input[0] == null) break;
                        	if(!CardUtil.isACreatureType(input[0])) input[0] = "";
                        	//TODO: some more input validation, case-sensitivity, etc.
                        
                        	input[0] = input[0].trim(); //this is to prevent "cheating", and selecting multiple creature types,eg "Goblin Soldier"
                		}
                		
                		if(input[0] == null) input[0] = "";

                        PlayerZone aiGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);
                        HashMap<String,Integer> countInGraveyard = new HashMap<String,Integer>();
                        CardList allGrave = new CardList(aiGrave.getCards());
                        allGrave.getType("Creature");
                        for(Card c:allGrave)
                        {
                            for(String type:c.getType())
                            {
                                if(CardUtil.isACreatureType(type))
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
                        for(Entry<String, Integer> entry:countInGraveyard.entrySet())
                        {
                            if(entry.getValue() > maxCount)
                            {
                                maxKey = entry.getKey();
                                maxCount = entry.getValue();
                            }
                        }
                        if(!maxKey.equals("")) input[1] = maxKey;
                        else input[1] = "Sliver";

                        //Actually put everything  on the battlefield 
                        CardList bidded = AllZoneUtil.getCardsInGraveyard();
                        bidded = bidded.getType("Creature");
                        for(Card c : bidded){
                        	if (c.isType(input[1]) || (!input[0].equals("") && c.isType(input[0])))
                        		AllZone.GameAction.moveToPlay(c);
                        }
                }//resolve()
            };//SpellAbility
            
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
                    return 2 <= getNonArtifact().size() && 5 < AllZone.ComputerPlayer.getLife();
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
                        	AllZone.GameAction.exile(c);
                    }
                    
                    card.getController().addDamage(5, card);
                }//resolve()
            };//SpellAbility
            

            final Input input = new Input() {
                private static final long serialVersionUID = -4114782677700487264L;
                
                @Override
                public void showMessage() {
                    if(index[0] == 0) AllZone.Display.showMessage("Select 1st target nonartifact creature to exile");
                    else AllZone.Display.showMessage("Select 2nd target nonartifact creature to exile");
                    
                    ButtonUtil.enableOnlyCancel();
                }
                
                @Override
                public void selectButtonCancel() {
                    stop();
                }
                
                @Override
                public void selectCard(Card c, PlayerZone zone) {
                    if(!c.isArtifact() && c.isCreature() && zone.is(Constant.Zone.Battlefield)) {
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
        else if(cardName.equals("Leeches")) {
        	/*
        	 * Target player loses all poison counters.
        	 * Leeches deals that much damage to that player.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 8555498267738686288L;

				@Override
        		public void resolve() {
        			int counters = getTargetPlayer().getPoisonCounters();
        			getTargetPlayer().addDamage(counters, card);
        			getTargetPlayer().subtractPoisonCounters(counters);
        		}// resolve()

        		@Override
        		public boolean canPlayAI() {
        			int humanPoison = AllZone.HumanPlayer.getPoisonCounters();
        			int compPoison = AllZone.ComputerPlayer.getPoisonCounters();
        			
        			if(AllZone.HumanPlayer.getLife() <= humanPoison ) {
        				setTargetPlayer(AllZone.HumanPlayer);
        				return true;
        			}
        			
        			if( (2*(11 - compPoison) < AllZone.ComputerPlayer.getLife() || compPoison > 7) && compPoison < AllZone.ComputerPlayer.getLife() - 2) {
        				setTargetPlayer(AllZone.ComputerPlayer);
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
					Player player = card.getController();
					Player opponent = player.getOpponent();
					CardList lib = AllZoneUtil.getPlayerCardsInLibrary(opponent);
					if(lib.size() > 0) {
						final Card topCard = lib.get(0);
						int damage = CardUtil.getConvertedManaCost(topCard);
						
						GuiUtils.getChoiceOptional(card+" - Revealed card", new Card[] {topCard});

						//deal damage to player
						opponent.addDamage(damage, card);

						//deal damage to all opponent's creatures
						CardList creatures = AllZoneUtil.getCreaturesInPlay(opponent);
						for(Card creature:creatures) {
							creature.addDamage(damage, card);
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
					return AllZoneUtil.getPlayerCardsInLibrary(AllZone.HumanPlayer).size() > 0;
				}
				
        	};// SpellAbility
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }// *************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if(cardName.equals("Sanity Grinding")) {
        	/*
        	 * Chroma - Reveal the top ten cards of your library. For each blue
        	 * mana symbol in the mana costs of the revealed cards, target opponent
        	 * puts the top card of his or her library into his or her graveyard.
        	 * Then put the cards you revealed this way on the bottom of your
        	 * library in any order.
        	 */
        	SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 4475834103787262421L;

				@Override
				public void resolve() {
					Player player = card.getController();
					Player opp = player.getOpponent();
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, card.getController());
					int maxCards = lib.size();
					maxCards = Math.min(maxCards, 10);
					if(maxCards == 0) return;
					CardList topCards = new CardList();
					//show top n cards:
					for(int j = 0; j < maxCards; j++ ) {
						topCards.add(lib.get(j));
					}
					final int num = CardFactoryUtil.getNumberOfManaSymbolsByColor("U", topCards);
					GuiUtils.getChoiceOptional("Revealed cards - "+num+" U mana symbols", topCards.toArray());
					maxCards = Math.min(maxCards, num);
					
					//opponent moves this many cards to graveyard
					opp.mill(maxCards);
					
					//then, move revealed cards to bottom of library
					for(Card c:topCards) {
						AllZone.GameAction.moveToBottomOfLibrary(c);
					}
				}// resolve()
				
				@Override
				public boolean canPlayAI() {
					return AllZoneUtil.getPlayerCardsInLibrary(AllZone.ComputerPlayer).size() > 0;
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
                	Player player = card.getController();
                	Player opp = player.getOpponent();
                    int numSwamps = AllZoneUtil.getPlayerTypeInPlay(player, "Swamp").size();
                    int numMountains = AllZoneUtil.getPlayerTypeInPlay(player, "Mountain").size();
                    int numForests = AllZoneUtil.getPlayerTypeInPlay(player, "Forest").size();
                    int numPlains = AllZoneUtil.getPlayerTypeInPlay(player, "Plains").size();
                    int numIslands = AllZoneUtil.getPlayerTypeInPlay(player, "Island").size();
                    
                    //swamps
                    opp.loseLife(2*numSwamps, card);
                    
                    //mountain
                    getTargetCard().addDamage(numMountains, card);
                    
                    //forest
                    for(int i = 0; i < numForests; i++)
                    	CardFactoryUtil.makeTokenSaproling(player);
                    
                    //plains
                    player.gainLife(2*numPlains, card);
                    
                    //islands
                    int max = Math.min(numIslands, AllZoneUtil.getPlayerCardsInLibrary(player).size());
                    if(max > 0) {
                    	player.drawCards(max);
                    	if(player.equals(AllZone.HumanPlayer)) {
                    		AllZone.InputControl.setInput(CardFactoryUtil.input_discard(max, this));
                    	}
                    	else {
                    		AllZone.ComputerPlayer.discardRandom(max, this);
                    	}
                    }
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(CardFactoryUtil.input_targetCreature(spell));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Overwhelming Stampede")) {
        	/*
        	 * Until end of turn, creatures you control gain trample and get
        	 * +X/+X, where X is the greatest power among creatures you control.
        	 */
        	final int[] x = new int[1];
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -3676506382832498840L;

				@Override
                public boolean canPlayAI() {
                    CardList list = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
                    return list.size() > 2;
                }//canPlayAI()
                
                @Override
                public void resolve() {
                	Player player = card.getController();
                    CardList list = AllZoneUtil.getCreaturesInPlay(player);
                    
                    x[0] = findHighestPower(list);    
                    
                    for(Card creature:list) {
                        final Card c = creature;
                        
                        final Command untilEOT = new Command() {
							private static final long serialVersionUID = -2712661762676783458L;

							public void execute() {
                                if(AllZone.GameAction.isCardInPlay(c)) {
                                    c.addTempAttackBoost(-x[0]);
                                    c.addTempDefenseBoost(-x[0]);
                                    c.removeExtrinsicKeyword("Trample");
                                }
                            }
                        };//Command
                        
                        if(AllZone.GameAction.isCardInPlay(c)) {
                            c.addTempAttackBoost(x[0]);
                            c.addTempDefenseBoost(x[0]);
                            c.addExtrinsicKeyword("Trample");
                            
                            AllZone.EndOfTurn.addUntil(untilEOT);
                        }//if
                    }//for
                }//resolve()
                
                private int findHighestPower(CardList list) {
                	int highest = 0;
                	for(Card c:list) {
                		if( c.getNetAttack() > highest ) highest = c.getNetAttack();
                	}
                	return highest;
                }
            };
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            card.setSVar("PlayMain1", "TRUE");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Winds of Change")) {
        	/*
        	 * Each player shuffles the cards from his or her hand into
        	 * his or her library, then draws that many cards.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 1137557863607126794L;

				@Override
                public void resolve() {
                    discardDrawX(AllZone.HumanPlayer);
                    discardDrawX(AllZone.ComputerPlayer);
                }//resolve()
                
                void discardDrawX(Player player) {
                	CardList hand = AllZoneUtil.getPlayerHand(player);

                    for(Card c : hand)
                    	AllZone.GameAction.moveToLibrary(c);
                    
                    // Shuffle library
                    player.shuffle();
                    
                    player.drawCards(hand.size());
                }
                
                // Simple, If computer has two or less playable cards remaining in hand play Winds of Change
                @Override
                public boolean canPlayAI() {
                	CardList c = AllZoneUtil.getPlayerCardsInPlay(AllZone.ComputerPlayer);
                	c = c.filter(AllZoneUtil.nonlands);
                    return 2 >= c.size();
                }
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Nature's Resurgence")) {
        	/*
        	 * Each player draws a card for each creature card in his
        	 * or her graveyard.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 5736340966381828725L;

				@Override
                public void resolve() {
                    int human = AllZoneUtil.getPlayerGraveyard(AllZone.HumanPlayer).filter(AllZoneUtil.creatures).size();
                    int comp = AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer).filter(AllZoneUtil.creatures).size();
                    AllZone.HumanPlayer.drawCards(human);
                    AllZone.ComputerPlayer.drawCards(comp);
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                	return AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer).filter(AllZoneUtil.creatures).size() > 1;
                }
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("All Hallow's Eve")) {
        	/*
        	 * Exile All Hallow's Eve with 2 scream counters on it.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 2756905332132706863L;

				@Override
				public void resolve() {
					card.addReplaceMoveToGraveyardCommand(new Command() {
						private static final long serialVersionUID = -1840315433449918025L;

						public void execute() {
							//when this is in exile, and on the stack, this must get called again...
							if(!AllZone.GameAction.isCardExiled(card)) {
								AllZone.GameAction.exile(card);
								card.addCounter(Counters.SCREAM, 2);
							}
						}
					});
				}//resolve()
				
				public boolean canPlayAI() {
					CardList compGrave = AllZoneUtil.getPlayerGraveyard(AllZone.ComputerPlayer);
					compGrave = compGrave.filter(AllZoneUtil.creatures);
					CardList humanGrave = AllZoneUtil.getPlayerGraveyard(AllZone.HumanPlayer);
					humanGrave = humanGrave.filter(AllZoneUtil.creatures);
					if(compGrave.size() > humanGrave.size()) return true;
					else return false;
				}
                
            };//SpellAbility
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Molten Psyche")) {
        	/*
        	 * Each player shuffles the cards from his or her hand into his
        	 * or her library, then draws that many cards.
        	 * Metalcraft - If you control three or more artifacts, Molten
        	 * Psyche deals damage to each opponent equal to the number of
        	 * cards that player has drawn this turn.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -1276674329039279896L;

				@Override
                public void resolve() {
                	Player player = card.getController();
                	Player opp = player.getOpponent();
                    discardDraw(AllZone.HumanPlayer);
                    discardDraw(AllZone.ComputerPlayer);
                    
                    if(player.hasMetalcraft()) {
                    	opp.addDamage(opp.getNumDrawnThisTurn(), card);
                    }
                }//resolve()
                
                void discardDraw(Player player) {
                    CardList hand = AllZoneUtil.getPlayerHand(player);
                    int numDraw = hand.size();
                    
                    //move hand to library
                    for(Card c:hand) {
                    	AllZone.GameAction.moveToLibrary(c);
                    }
                    
                    // Shuffle library
                    player.shuffle();
                    
                    // Draw X cards
                    player.drawCards(numDraw);
                }
                
                // Simple, If computer has two or less playable cards remaining in hand play CARDNAME
                @Override
                public boolean canPlayAI() {
                    Card[] c = removeLand(AllZone.Computer_Hand.getCards());
                    return 2 >= c.length || 
                    	(AllZone.ComputerPlayer.hasMetalcraft() && AllZone.HumanPlayer.getLife() <= 3);
                }
                
                private Card[] removeLand(Card[] in) {
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
        else if(cardName.equals("Channel")) {
        	/*
        	 * Until end of turn, any time you could activate a mana ability, you
        	 * may pay 1 life. If you do, add 1 to your mana pool.
        	 */
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 4113684767236269830L;

				@Override
                public boolean canPlayAI() {
					//AI currently has no mana pool
                    return false;
                }
                
                @Override
                public void resolve() {
                	getActivatingPlayer().setChannelCard(card);
                	final Command untilEOT = new Command() {
						private static final long serialVersionUID = 6608218813784831252L;

						public void execute() {
                            getActivatingPlayer().setChannelCard(null);
                        }
                    };//Command
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Choking Sands")) {
            Ability_Cost abCost = new Ability_Cost("1 B B", cardName, false);
            Target target = new Target(card,"Select target non-Swamp land.", new String[]{"Land.nonSwamp"});
            final SpellAbility spell = new Spell(card, abCost, target) {
				private static final long serialVersionUID = 6499378648382900112L;

				@Override
                public boolean canPlayAI() {
					CardList land = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
                    land = land.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return !c.isType("Swamp");
                    	}
                    });
                    return land.size() > 0;
                }
                
                @Override
                public void chooseTargetAI() {
                    CardList land = AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer);
                    land = land.filter(new CardListFilter() {
                    	public boolean addCard(Card c) {
                    		return !c.isType("Swamp");
                    	}
                    });
                    CardList nonBasic = land.filter(AllZoneUtil.nonBasicLand);
                    if(nonBasic.size() > 0) {
                    	setTargetCard(nonBasic.get(0));
                    }
                    else {
                    	setTargetCard(land.get(0));
                    }
                }//chooseTargetAI()
                
                @Override
                public void resolve() {
                    if(AllZone.GameAction.isCardInPlay(getTargetCard())
                            && CardFactoryUtil.canTarget(card, getTargetCard())) {
                        AllZone.GameAction.destroy(getTargetCard());
                        
                        if(!getTargetCard().isBasicLand()) getTargetCard().getController().addDamage(2, card);
                    }
                }//resolve()
            };//SpellAbility
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setDescription(abCost+"Destroy target non-Swamp land. If that land was nonbasic, Choking Sands deals 2 damage to the land's controller.");
        }//*************** END ************ END **************************

        
        //*************** START *********** START **************************
        else if (cardName.equals("Decree of Justice")) { 
        	/*
        	 *   When you cycle Decree of Justice, you may pay X. If you do, put X 1/1 
        	 *   white Soldier creature tokens onto the battlefield.
        	 */
        	Ability_Cost abCost = new Ability_Cost("X", cardName, false);
        	final SpellAbility ability = new Ability_Activated(card, abCost, null) {
        		private static final long serialVersionUID = -7995897172138409120L;

        		@Override
        		public void resolve() {
        			for(int i = 0; i < card.getXManaCostPaid(); i++)
        				CardFactoryUtil.makeToken11WSoldier(card.getController());
        			card.setXManaCostPaid(0);
        		}

        	};
        	ability.setStackDescription(cardName+" - put X 1/1 white Soldier creature tokens onto the battlefield.");
        	card.addCycleCommand(new Command() {
        		private static final long serialVersionUID = 7699412574052780825L;

        		public void execute() {
        			AllZone.InputControl.setInput(new Input() {
        				private static final long serialVersionUID = 7823602729552197455L;

        				public void showMessage() {
        					String s = JOptionPane.showInputDialog(cardName+" - What would you like X to be?");
        					try {
        						int x = Integer.parseInt(s);
        						ability.setManaCost(s);
        						stopSetNext(new Input_PayManaCost(ability));
        						card.setXManaCostPaid(x);
        					}
        					catch(NumberFormatException e){
        						AllZone.Display.showMessage("\"" + s + "\" is not a number.");
        						showMessage();
        					}
        				}
        			});
        		}
        	});
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Research the Deep")) {
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -3031317284608505865L;

				@Override
                public void resolve() {
                    card.getController().drawCard();
                    if(card.getController().clashWithOpponent(card)) {
                    	win();
                    }
                }//resolve()
                
                void win() {
                    AllZone.GameAction.moveToHand(card);
                }
            };
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Biorhythm")) {
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -6042020870286943301L;

				@Override
        		public boolean canPlayAI() {
					CardList human = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
					CardList comp = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
					int hLife = AllZone.HumanPlayer.getLife();
					int cLife = AllZone.ComputerPlayer.getLife();
        			return comp.size() > 0 && human.size() < hLife && ((cLife < 4 && hLife > 4) || (hLife - cLife > 12));
        		}

        		@Override
        		public void resolve() {
        			CardList human = AllZoneUtil.getCreaturesInPlay(AllZone.HumanPlayer);
					CardList comp = AllZoneUtil.getCreaturesInPlay(AllZone.ComputerPlayer);
					AllZone.HumanPlayer.setLife(human.size(), card);
					AllZone.ComputerPlayer.setLife(comp.size(), card);
        		}
        	};//SpellAbility
        	
        	StringBuilder sb = new StringBuilder();
        	sb.append(card.getName()).append(" - Each player's life total becomes the lowest life total among all players.");
        	spell.setStackDescription(sb.toString());
        	
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Austere Command")) {
            final ArrayList<String> userChoice = new ArrayList<String>();
            
            final String[] cardChoices = {
                    "Destroy all artifacts",
                    "Destroy all enchantments",
                    "Destroy all creatures with converted mana cost 3 or less",
                    "Destroy all creatures with converted mana cost 4 or more"
            };
            
            final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = -8501457363981482513L;

				@Override
                public void resolve() {
                	
                	//"Destroy all artifacts",
                	if(userChoice.contains(cardChoices[0])) {
                		CardList cards = AllZoneUtil.getCardsInPlay().filter(AllZoneUtil.artifacts);
                		for(Card c:cards) AllZone.GameAction.destroy(c);
                	}

                    //"Destroy all enchantments",
                    if(userChoice.contains(cardChoices[1])) {
                    	CardList cards = AllZoneUtil.getCardsInPlay().filter(AllZoneUtil.enchantments);
                		for(Card c:cards) AllZone.GameAction.destroy(c);
                    }
                    
                    //"Destroy all creatures with converted mana cost 3 or less",
                    if(userChoice.contains(cardChoices[2])) {
                    	CardList cards = AllZoneUtil.getCreaturesInPlay();
                    	cards = cards.filter(new CardListFilter() {
                    		public boolean addCard(Card c) {
                    			return CardUtil.getConvertedManaCost(c) <= 3;
                    		}
                    	});
                		for(Card c:cards) AllZone.GameAction.destroy(c);
                    }

                    //"Destroy all creatures with converted mana cost 4 or more"};
                    if(userChoice.contains(cardChoices[3])) {
                    	CardList cards = AllZoneUtil.getCreaturesInPlay();
                    	cards = cards.filter(new CardListFilter() {
                    		public boolean addCard(Card c) {
                    			return CardUtil.getConvertedManaCost(c) >= 4;
                    		}
                    	});
                		for(Card c:cards) AllZone.GameAction.destroy(c);
                    }
                }//resolve()
                
                @Override
                public boolean canPlayAI() {
                    return false;
                }
            };//SpellAbility
            
            final Command setStackDescription = new Command() {
				private static final long serialVersionUID = -635710110379729475L;

				public void execute() {
                    ArrayList<String> a = new ArrayList<String>();
                    if(userChoice.contains(cardChoices[0])) a.add("destroy all artifacts");
                    if(userChoice.contains(cardChoices[1])) a.add("destroy all enchantments");
                    if(userChoice.contains(cardChoices[2])) a.add("destroy all creatures with CMC <= 3");
                    if(userChoice.contains(cardChoices[3])) a.add("destroy all creatures with CMC >= 4");
                    
                    String s = a.get(0) + ", " + a.get(1);
                    spell.setStackDescription(card.getName() + " - " + s);
                }
            };//Command

            Input chooseTwoInput = new Input() {
				private static final long serialVersionUID = 2352497236500922820L;

				@Override
                public void showMessage() {
					if(card.isCopiedSpell()) {
						setStackDescription.execute();
						stopSetNext(new Input_PayManaCost(spell));
					}
                	else {
                		//reset variables
                		userChoice.clear();

                		ArrayList<String> display = new ArrayList<String>(Arrays.asList(cardChoices));

                		ArrayList<String> a = chooseTwo(display);
                		//everything stops here if user cancelled
                		if(a == null) {
                			stop();
                			return;
                		}

                		userChoice.addAll(a);

                		setStackDescription.execute();
                		stopSetNext(new Input_PayManaCost(spell));
                	}
                }//showMessage()
                
                ArrayList<String> chooseTwo(ArrayList<String> choices) {
                    ArrayList<String> out = new ArrayList<String>();
                    Object o = GuiUtils.getChoiceOptional("Choose Two", choices.toArray());
                    if(o == null) return null;
                    
                    out.add((String) o);
                    choices.remove(out.get(0));
                    o = GuiUtils.getChoiceOptional("Choose Two", choices.toArray());
                    if(o == null) return null;
                    
                    out.add((String) o);
                    
                    return out;
                }//chooseTwo()
            };//Input chooseTwoInput
            
            card.clearSpellAbility();
            card.addSpellAbility(spell);
            spell.setBeforePayMana(chooseTwoInput);
        }//*************** END ************ END **************************
        
      
        //*************** START *********** START **************************
        else if(cardName.equals("Praetor's Counsel")) {
        	final SpellAbility spell = new Spell(card) {
				private static final long serialVersionUID = 2208683667850222369L;

				@Override
        		public boolean canPlayAI() {
					return false;
        		}

				@Override
				public void resolve() {
					Player player = card.getController();
					CardList grave = AllZoneUtil.getPlayerGraveyard(player);
					for(Card c:grave) AllZone.GameAction.moveToHand(c);

					AllZone.GameAction.exile(card);

					card.setSVar("HSStamp","" + Player.getHandSizeStamp());
					player.addHandSizeOperation(new HandSizeOp("=", -1, Integer.parseInt(card.getSVar("HSStamp"))));
				}
        	};//SpellAbility
        	
        	/*StringBuilder sb = new StringBuilder();
        	sb.append(card.getName()).append(" - Each player's life total becomes the lowest life total among all players.");
        	spell.setStackDescription(sb.toString()); */
        	
        	card.clearSpellAbility();
        	card.addSpellAbility(spell);
        }//*************** END ************ END **************************
        
        
    	return card;
    }//getCard
}
