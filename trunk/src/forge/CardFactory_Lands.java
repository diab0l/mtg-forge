
package forge;

import java.util.HashMap;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import forge.gui.GuiUtils;

class CardFactory_Lands {

    public static Card getCard(final Card card, final String cardName, Player owner) {
        
//	    computer plays 2 land of these type instead of just 1 per turn
        

        //*************** START *********** START **************************
        if(cardName.equals("Oran-Rief, the Vastwood")) {
            card.clearSpellKeepManaAbility();
            
            final CardListFilter targets = new CardListFilter() {
                
                public boolean addCard(Card c) {
                    return AllZone.GameAction.isCardInPlay(c) && c.isCreature()
                            && c.getTurnInZone() == AllZone.Phase.getTurn()
                            && c.isGreen();
                }
                
            };
            Ability_Cost abCost = new Ability_Cost("T", card.getName(), true);
            final SpellAbility ability = new Ability_Activated(card, abCost, null){                
                private static final long serialVersionUID = 1416258136308898492L;
                
                CardList                  inPlay           = new CardList();
                
                @Override
                public boolean canPlayAI() {
                    if(!(AllZone.Phase.getPhase().equals(Constant.Phase.Main1) && AllZone.Phase.getPlayerTurn().equals(
                            AllZone.ComputerPlayer))) return false;
                    inPlay.clear();
                    inPlay.addAll(AllZone.Computer_Battlefield.getCards());
                    return (inPlay.filter(targets).size() > 1) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    inPlay.clear();
                    inPlay.addAll(AllZone.Human_Battlefield.getCards());
                    inPlay.addAll(AllZone.Computer_Battlefield.getCards());
                    for(Card targ:inPlay.filter(targets))
                        targ.addCounter(Counters.P1P1, 1);
                }
            };
            ability.setDescription(abCost+"Put a +1/+1 counter on each green creature that entered the battlefield this turn.");
            ability.setStackDescription("Put a +1/+1 counter on each green creature that entered the battlefield this turn.");
            card.addSpellAbility(ability);
        }
        //*************** END ************ END **************************        


        //*************** START *********** START **************************
        //Ravinca Dual Lands
        else if (  cardName.equals("Blood Crypt")    || cardName.equals("Breeding Pool") 
                || cardName.equals("Godless Shrine") || cardName.equals("Hallowed Fountain") 
                || cardName.equals("Overgrown Tomb") || cardName.equals("Sacred Foundry") 
                || cardName.equals("Steam Vents")    || cardName.equals("Stomping Ground") 
                || cardName.equals("Temple Garden")  || cardName.equals("Watery Grave")) {
            //if this isn't done, computer plays more than 1 copy
            //card.clearSpellAbility();
            card.clearSpellKeepManaAbility();
            
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 7352127748114888255L;
                
                public void execute() {
                    if (card.getController().equals(AllZone.HumanPlayer)) humanExecute();
                    else computerExecute();
                }
                
                public void computerExecute() {
                    boolean pay = false;
                    
                    if(AllZone.ComputerPlayer.getLife() > 9) pay = MyRandom.random.nextBoolean();
                    
                    if(pay) AllZone.ComputerPlayer.loseLife(2, card);
                    else card.tap();
                }
                
                public void humanExecute() {
                    int life = card.getController().getLife();
                    if (2 < life) {
                        
                        StringBuilder question = new StringBuilder();
                        question.append("Pay 2 life? If you don't, ").append(card.getName());
                        question.append(" enters the battlefield tapped.");
                        
                        if (GameActionUtil.showYesNoDialog(card, question.toString())) {
                            AllZone.HumanPlayer.loseLife(2, card);
                        } else tapCard();
                        
                    }//if
                    else tapCard();
                }//execute()
                
                private void tapCard() {
                    card.tap();
                }
            });
        }//*************** END ************ END **************************
        
        
                //*************** START *********** START **************************
        else if(cardName.equals("Sejiri Steppe")) {
        	final HashMap<Card, String[]> creatureMap = new HashMap<Card, String[]>();
        	final SpellAbility[] a = new SpellAbility[1];
        	final Command eot1 = new Command() {
        		private static final long serialVersionUID = 5106629534549783845L;

        		public void execute() {
        			Card c = a[0].getTargetCard();
        			if(AllZone.GameAction.isCardInPlay(c)) {
        				String[] colors = creatureMap.get(c);
        				for(String col:colors) {
        					c.removeExtrinsicKeyword("Protection from " + col);
        				}
        			}
        		};
        	};
        	a[0] = new Ability(card, "0") {
        		@Override
        		public void resolve() {
        			String Color = "";

        			if(card.getController() == AllZone.HumanPlayer){
        				if(AllZone.GameAction.isCardInPlay(getTargetCard()) && CardFactoryUtil.canTarget(card, getTargetCard())) {                     
        					Object o = GuiUtils.getChoice("Choose mana color", Constant.Color.onlyColors);
        					Color = (String) o;
        				}

        			} else {
        				CardList creature = new CardList();
        				PlayerZone zone = AllZone.getZone(Constant.Zone.Battlefield, AllZone.ComputerPlayer);             
        				if(zone != null) {
        					creature.addAll(zone.getCards());
        					creature = creature.getType("Creature"); 
        					creature = creature.filter(new CardListFilter()
        					{
        						public boolean addCard(Card c)
        						{
        							return (AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(a[0], c) && !c.hasKeyword("Defender"));
        						}
        					});
        					Card biggest = null;
        					if(creature.size() > 0) {
        						biggest = creature.get(0);

        						for(int i = 0; i < creature.size(); i++) {
        							if(biggest.getNetAttack() < creature.get(i).getNetAttack()) biggest = creature.get(i);   
        						}
        						setTargetCard(biggest);

        					}
        				}
        				PlayerZone Hzone = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);  
        				if(zone != null) {
        					CardList creature2 = new CardList();
        					creature2.addAll(Hzone.getCards());
        					creature2 = creature2.getType("Creature"); 
        					creature2 = creature2.filter(new CardListFilter()
        					{
        						public boolean addCard(Card c)
        						{
        							return (!c.isTapped() && !CardUtil.getColors(c).contains(Constant.Color.Colorless));
        						}
        					});
        					Card biggest2 = null;
        					if(creature2.size() > 0) {
        						biggest2 = creature2.get(0);
        						for(int i = 0; i < creature2.size(); i++) {
        							if(biggest2.getNetAttack() < creature2.get(i).getNetAttack()) biggest2 = creature2.get(i);   
        						}
        						if(biggest2 != null) {  
        							if(biggest2.isGreen()) Color = "green";
        							if(biggest2.isBlue()) Color = "blue";
        							if(biggest2.isWhite()) Color = "white";
        							if(biggest2.isRed()) Color = "red";
        							if(biggest2.isBlack()) Color = "black";
        						} else {
        							Color = "black";          			
        						}

        					} else {
        						Color = "black"; 
        					}
        				}
        			}
        			Card Target = getTargetCard();
        			if(Color != "" && Target != null) Target.addExtrinsicKeyword("Protection from " + Color);;
        			if(creatureMap.containsKey(Target)) {
        				int size = creatureMap.get(Target).length;
        				String[] newString = new String[size + 1];

        				for(int i = 0; i < size; i++) {
        					newString[i] = creatureMap.get(Target)[i];
        				}
        				newString[size] = Color;
        				creatureMap.put(Target, newString);
        			} else creatureMap.put(Target, new String[] {Color});
        			AllZone.EndOfTurn.addUntil(eot1);
        		}
        	};

        	Command intoPlay = new Command() {
        		private static final long serialVersionUID = 5055232386220487221L;

        		public void execute() {
        			CardList creats = new CardList(
        					AllZone.getZone(Constant.Zone.Battlefield, card.getController()).getCards());
        			creats = creats.getType("Creature");
        			StringBuilder sb = new StringBuilder();
        			sb.append(card.getName()).append(" - target creature you control gains protection from the color of your choice until end of turn");
        			a[0].setStackDescription(sb.toString());
        			if(card.getController() == AllZone.HumanPlayer) {
        				AllZone.InputControl.setInput(CardFactoryUtil.input_targetSpecific(a[0], creats, "Select target creature you control", false, false));
        			} else {
        				AllZone.Stack.add(a[0]);  		
        			}
        		}
        	};         
        	card.addComesIntoPlayCommand(intoPlay);

        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Faerie Conclave")) {
        	final long[] timeStamp = new long[1];
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 2792041290726604698L;
                
                public void execute() {
                    card.tap();
                }
            });
            
            final SpellAbility a1 = new Ability(card, "1 U") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Faerie" };
                    String[] keywords = { "Flying" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 2, 1, types, keywords, "U");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = 5106629534549783845L;
                        
                        public void execute() {
                        	long stamp = timeStamp[0];
                            Card c = card;
                            String[] types = { "Creature", "Faerie" };
                            String[] keywords = { "Flying" };
                            CardFactoryUtil.revertManland(c, types, keywords, "U", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            a1.setDescription("1 U: Faerie Conclave becomes a 2/1 blue Faerie creature with flying until end of turn. It's still a land.");
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 2/1 creature with flying until EOT");
            a1.setStackDescription(sb.toString());
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -601119544294387668L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Forbidding Watchtower")) {
        	final long[] timeStamp = new long[1];
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = 5212793782060828409L;
                
                public void execute() {
                    card.tap();
                }
            });
            
            final SpellAbility a1 = new Ability(card, "1 W") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Soldier" };
                    String[] keywords = {  };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 1, 5, types, keywords, "W");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = 8806880921707550181L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Soldier" };
                            String[] keywords = {  };

                            CardFactoryUtil.revertManland(c, types, keywords, "W", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            a1.setDescription("1 W: Forbidding Watchtower becomes a 1/5 white Soldier creature until end of turn. It's still a land.");
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 1/5 creature until EOT");
            a1.setStackDescription(sb.toString());
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -7211256926392695778L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Treetop Village")) {
        	final long[] timeStamp = new long[1];
            card.addComesIntoPlayCommand(new Command() {
                private static final long serialVersionUID = -2246560994818997231L;
                
                public void execute() {
                    card.tap();
                }
            });
            
            final SpellAbility a1 = new Ability(card, "1 G") {
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Creature")  && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Ape" };
                    String[] keywords = { "Trample" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 3, 3, types, keywords, "G");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -8535770979347971863L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            
                            String[] removeTypes = { "Creature", "Ape" };
                            String[] removeKeywords = { "Trample" };
                            CardFactoryUtil.revertManland(c, removeTypes, removeKeywords, "G", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 3/3 creature with trample until EOT");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1 G: Treetop Village becomes a 3/3 green Ape creature with trample until end of turn. It's still a land.");
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -6800983290478844750L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Blinkmoth Nexus") || cardName.equals("Inkmoth Nexus")) {
        	final long[] timeStamp = new long[1];
        	Ability_Cost abCost = new Ability_Cost("1", cardName, true);
            final SpellAbility a1 = new Ability_Activated(card, abCost, null) {
				private static final long serialVersionUID = -8834858776517935070L;

				@Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    final String[] types = { "Artifact", "Creature", "Blinkmoth" };
                    final ArrayList<String> keywords = new ArrayList<String>();
                    keywords.add("Flying");
                    if(cardName.equals("Inkmoth Nexus")) keywords.add("Infect");
                    final String[] kwArray = new String[keywords.size()];
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 1, 1, types, keywords.toArray(kwArray), "0");
                    
                    final Command eot1 = new Command() {
                    	private static final long serialVersionUID = 3564161001279001235L;
                    	long stamp = timeStamp[0];
                    	public void execute() {
                    		CardFactoryUtil.revertManland(card, types, keywords.toArray(kwArray), "", stamp);
                    	}
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            card.addSpellAbility(a1);
            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(abCost).append(cardName).append(" becomes a 1/1 Blinkmoth artifact creature with flying ");
            if(cardName.equals("Inkmoth Nexus")) sbDesc.append("and infect ");
            sbDesc.append("until end of turn. It's still a land.");
            a1.setDescription(sbDesc.toString());
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 1/1 creature with flying ");
            if(cardName.equals("Inkmoth Nexus")) sb.append("and infect ");
            sb.append("until EOT");
            a1.setStackDescription(sb.toString());
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Mishra's Factory")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "1") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Artifact", "Creature", "Assembly-Worker" };
                    String[] keywords = { };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 2, 2, types, keywords, "0");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -956566640027406078L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            
                            String[] types = { "Artifact", "Creature", "Assembly-Worker" };
                            String[] keywords = { };
                            CardFactoryUtil.revertManland(c, types, keywords, "", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            card.addSpellAbility(a1);
            a1.setDescription("1: Mishra's Factory becomes a 2/2 Assembly-Worker artifact creature until end of turn. It's still a land.");
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - becomes a 2/2 creature until EOT");
            a1.setStackDescription(sb.toString());
            
            // is this even needed?
            Command paid1 = new Command() {
                private static final long serialVersionUID = -6767109002136516590L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dark Depths")) {
                        
            card.addComesIntoPlayCommand(CardFactoryUtil.entersBattleFieldWithCounters(card, Counters.ICE , 10));
            
            final SpellAbility ability = new Ability(card, "3") {
                @Override
                public boolean canPlay() {
                    SpellAbility sa;
                    for(int i = 0; i < AllZone.Stack.size(); i++) {
                        sa = AllZone.Stack.peek(i);
                        if(sa.getSourceCard().equals(card)) return false;
                    }
                    
                    if(card.getCounters(Counters.ICE) > 0 && AllZone.GameAction.isCardInPlay(card) && super.canPlay()) return true;
                    else return false;
                }
                
                @Override
                public boolean canPlayAI() {
                    String phase = AllZone.Phase.getPhase();
                    return phase.equals(Constant.Phase.Main2) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    card.subtractCounter(Counters.ICE, 1);
                    
                    if(card.getCounters(Counters.ICE) == 0) 
                    {CardFactoryUtil.makeToken("Marit Lage",
                            "B 20 20 Marit Lage", card.getController(), "B", new String[] {"Legendary", "Creature", "Avatar"}, 20,
                            20, new String[] {"Flying", "Indestructible"});
                    	AllZone.GameAction.sacrifice(card);
                    }
                }
            };
            final SpellAbility sacrifice = new Ability(card, "0") {
            	//TODO - this should probably be a state effect
                @Override
                public boolean canPlay() {
                    return card.getCounters(Counters.ICE) == 0 && AllZone.GameAction.isCardInPlay(card) && super.canPlay();
                }
                
                @Override
                public boolean canPlayAI() {
                    return canPlay() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    if(card.getCounters(Counters.ICE) == 0) {
                    CardFactoryUtil.makeToken("Marit Lage",
                            "B 20 20 Marit Lage", card.getController(), "B", new String[] {"Legendary", "Creature", "Avatar"}, 20,
                            20, new String[] {"Flying", "Indestructible"});
                    }
                    AllZone.GameAction.sacrifice(card);  
                }
            };
            //ability.setDescription("Dark Depths enters the battlefield with ten ice counters on it.\r\n\r\n3: Remove an ice counter from Dark Depths.\r\n\r\nWhen Dark Depths has no ice counters on it, sacrifice it. If you do, put an indestructible legendary 20/20 black Avatar creature token with flying named Marit Lage onto the battlefield.");
            ability.setDescription("3: remove an Ice Counter.");
            StringBuilder sb = new StringBuilder();
            sb.append(card.getName()).append(" - remove an ice counter.");
            ability.setStackDescription(sb.toString());
            
            card.addSpellAbility(ability);
            sacrifice.setStackDescription("Sacrifice "+card.getName());
            card.addSpellAbility(sacrifice);
            
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Gods' Eye, Gate to the Reikai")) {
            
            final Ability ability = new Ability(card, "0") {
                @Override
                public void resolve() {
                    CardFactoryUtil.makeToken("Spirit", "C 1 1 Spirit", card.getController(), "", new String[] {
                            "Creature", "Spirit"}, 1, 1, new String[] {""});
                }//resolve()
            };//Ability
            
            Command makeToken = new Command() {
                private static final long serialVersionUID = 2339209292936869322L;
                
                public void execute() {
                	StringBuilder sb = new StringBuilder();
                	sb.append(card.getName()).append(" - put a 1/1 Spirit creature token onto the battlefield");
                	ability.setStackDescription(sb.toString());
                    AllZone.Stack.add(ability);
                }
            };
            
            card.addDestroyCommand(makeToken);
        }//*************** END ************ END **************************
        

        
        //*************** START *********** START **************************
        else if(cardName.equals("Mutavault")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "1") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature" };
                    String[] keywords = { "Changeling" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 2, 2, types, keywords, "0");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = 5106629534549783845L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;

                            String[] types = { "Creature" };
                            String[] keywords = { "Changeling" };
                            CardFactoryUtil.revertManland(c, types, keywords, "", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            a1.setDescription("1: Mutavault becomes a 2/2 creature with all creature types until end of turn. It's still a land.");
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 2/2 creature with changeling until EOT");
            a1.setStackDescription(sb.toString());
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -601119544294387668L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
            
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Spawning Pool")) {
        	final long[] timeStamp = new long[1];
            
            final Command untilEOT = new Command() {
                private static final long serialVersionUID = -451839437837081897L;
                
                public void execute() {
                    card.setShield(0);
                }
            };
            
            final SpellAbility a2 = new Ability(card, "B") {
                @Override
                public boolean canPlayAI() {
                    return false;
                }
                
                @Override
                public void resolve() {
                    card.addShield();
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }
            };//SpellAbility
            a2.setType("Extrinsic"); // Required for Spreading Seas
            a2.setDescription("B: Regenerate Spawning Pool.");
            a2.setStackDescription("Regenerate Spawning Pool");
            
            a2.setBeforePayMana(new Input_PayManaCost(a2));
            
            final SpellAbility a1 = new Ability(card, "1 B") {
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Creature") && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Skeleton" };
                    String[] keywords = {  };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 1, 1, types, keywords, "B");
                    
                    // Don't stack Regen ability
                    boolean hasRegen = false;
                    SpellAbility[] sas = card.getSpellAbility();
                    for(SpellAbility sa:sas) {
                        if(sa.toString().equals("B: Regenerate Spawning Pool.")) //this is essentially ".getDescription()"
                        hasRegen = true;
                    }
                    if(!hasRegen) {
                        card.addSpellAbility(a2);
                    }
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -8535770979347971863L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Skeleton" };
                            String[] keywords = {  };
                            CardFactoryUtil.revertManland(c, types, keywords, "B", stamp);
                            c.removeSpellAbility(a2);
                        }
                    };

                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 1/1 skeleton creature with B: regenerate this creature until EOT");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1B: Spawning Pool becomes a 1/1 skeleton creature with B: regenerate this creature until end of the turn. It's still a land.");
            Command paid1 = new Command() {
                private static final long serialVersionUID = -6800983290478844750L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Novijen, Heart of Progress")) {
            card.clearSpellKeepManaAbility();
            
            final CardListFilter targets = new CardListFilter() {
                
                public boolean addCard(Card c) {
                    return AllZone.GameAction.isCardInPlay(c) && c.isCreature()
                            && c.getTurnInZone() == AllZone.Phase.getTurn();
                }
            };
            
            Ability_Cost abCost = new Ability_Cost("G U T", cardName, true);
            Ability_Activated ability = new Ability_Activated(card, abCost, null) {
                private static final long serialVersionUID = 1416258136308898492L;
                
                CardList                  inPlay           = new CardList();
                
                @Override
                public boolean canPlayAI() {
                    if(!(AllZone.Phase.getPhase().equals(Constant.Phase.Main1) && AllZone.Phase.getPlayerTurn().equals(
                            AllZone.ComputerPlayer))) return false;
                    inPlay.clear();
                    inPlay.addAll(AllZone.Computer_Battlefield.getCards());
                    return (inPlay.filter(targets).size() > 1) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    inPlay.clear();
                    inPlay.addAll(AllZone.Human_Battlefield.getCards());
                    inPlay.addAll(AllZone.Computer_Battlefield.getCards());
                    for(Card targ:inPlay.filter(targets))
                        targ.addCounter(Counters.P1P1, 1);
                }
            };
            ability.setDescription(abCost+"Put a +1/+1 counter on each creature that entered the battlefield this turn.");
            ability.setStackDescription(cardName+" - Put a +1/+1 counter on each creature that entered the battlefield this turn.");
            card.addSpellAbility(ability);
        }
        //*************** END ************ END **************************

       
        //*************** START *********** START **************************
        else if(cardName.equals("Svogthos, the Restless Tomb")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "3 B G") {
                @Override
                public boolean canPlayAI() {
                    PlayerZone compGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.ComputerPlayer);
                    CardList list = new CardList();
                    list.addAll(compGrave.getCards());
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature();
                        }
                    });
                    return ((list.size() > 0) & !card.getType().contains("Creature")) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Zombie", "Plant" };
                    String[] keywords = {  };

                    timeStamp[0] = CardFactoryUtil.activateManland(c, 1, 1, types, keywords, "B G");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -8535770979347971863L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Zombie", "Plant" };
                            String[] keywords = {  };

                            CardFactoryUtil.revertManland(c, types, keywords, "B G", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a black and green Plant Zombie creature with power and toughness each equal to the number of creature cards in your graveyard until EOT");
            a1.setStackDescription(sb.toString());            
            a1.setDescription("3 B G: Until end of turn, Svogthos, the Restless Tomb becomes a black and green Plant Zombie creature with This creature's power and toughness are each equal to the number of creature cards in your graveyard. It's still a land.");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Ghitu Encampment")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "1 R") {
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Creature") && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Warrior" };
                    String[] keywords = { "First Strike" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 2, 1, types, keywords, "R");

                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -8535770979347971863L;
                        
                        public void execute() {
                        	long stamp = timeStamp[0];
                            Card c = card;
                            String[] types = { "Creature", "Warrior" };
                            String[] keywords = { "First Strike" };

                            CardFactoryUtil.revertManland(c, types, keywords, "R", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 2/1 creature with first strike until EOT");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1 R: Ghitu Encampment becomes a 2/1 red Warrior creature with first strike until end of turn. It's still a land.");
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -6800983290478844750L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Stalking Stones")) {
            
            final SpellAbility a1 = new Ability(card, "6") {
                @Override
                public boolean canPlayAI() {
                    return !card.getType().contains("Creature") && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Artifact", "Creature", "Elemental" };
                    String[] keywords = {  };
                    
                    CardFactoryUtil.activateManland(c, 3, 3, types, keywords, "0");
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" becomes a 3/3 Elemental artifact creature that's still a land.");
            a1.setStackDescription(sb.toString());            
            a1.setDescription("6: Stalking Stones becomes a 3/3 Elemental artifact creature that's still a land. (This effect lasts indefinitely.)");
            
            Command paid1 = new Command() {
                private static final long serialVersionUID = -6800983290478844750L;
                
                public void execute() {
                    AllZone.Stack.add(a1);
                }
            };
            a1.setBeforePayMana(new Input_PayManaCost_Ability(a1.getManaCost(), paid1));
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Celestial Colonnade")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "3 W U") {
                @Override
                public boolean canPlayAI() {
                    return !card.hasSickness() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    
                    String[] types = { "Creature", "Elemental" };
                    String[] keywords = { "Vigilance", "Flying" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 4, 4, types, keywords, "W U");
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = 7377356496869217420L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Elemental" };
                            String[] keywords = { "Vigilance", "Flying" };
                            CardFactoryUtil.revertManland(c, types, keywords, "W U", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - until end of turn, Celestial Colonnade becomes a 4/4 white and blue Elemental creature with flying and vigilance.");
            a1.setStackDescription(sb.toString());
            a1.setDescription("3 W U: Until end of turn, Celestial Colonnade becomes a 4/4 white and blue Elemental creature with flying and vigilance. It's still a land.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Lavaclaw Reaches")) {
        	final long[] timeStamp = new long[1];
            final SpellAbility X_ability = new Ability(card, "0") {
                @Override
                public boolean canPlayAI() {
					PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Battlefield, AllZone.HumanPlayer);
			        CardList opponentCreatureList = new CardList(opponentPlayZone.getCards());
			        opponentCreatureList = opponentCreatureList.getType("Creature");
      			  int n = ComputerUtil.getAvailableMana().size() - 1;
      			  if(n > 0) setManaCost(n + "");
                    return (n > 0 && opponentCreatureList.size() == 0) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    final Card c = card;
                    for(int i = 0; i < Integer.parseInt(getManaCost()); i++) {
                        c.addTempAttackBoost(1);   	
                    }                  
                    c.updateObservers();
                    
                    Command untilEOT = new Command() {
                        private static final long serialVersionUID = -28032591440730370L;
                        
                        public void execute() {
                            for(int i = 0; i < Integer.parseInt(getManaCost()); i++) {
                            c.addTempAttackBoost(-1);
                            }
                        }
                    };
                    AllZone.EndOfTurn.addUntil(untilEOT);
                }//resolve()
            };//SpellAbility 
            
            X_ability.setType("Extrinsic"); // Required for Spreading Seas
            
          	  X_ability.setBeforePayMana(new Input()
        	  {
        		private static final long serialVersionUID = 437814522686732L;

    			public void showMessage()
        		 {
        			 String s = JOptionPane.showInputDialog("What would you like X to be?");
        	  		 try {
        	  			     Integer.parseInt(s);
        	  				 X_ability.setManaCost(s);
        	  				 stopSetNext(new Input_PayManaCost(X_ability));
        	  			 }
        	  			 catch(NumberFormatException e){
        	  				 AllZone.Display.showMessage("\"" + s + "\" is not a number.");
        	  				 showMessage();
        	  			 }
        		 }
        	  });
              
            final SpellAbility a1 = new Ability(card, "1 B R") {
                @Override
                public boolean canPlayAI() {
                    return (!card.hasSickness() && !card.getType().contains("Creature")) && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
	                Card c = card;
	                String[] types = { "Creature", "Elemental" };
	                String[] keywords = {  };
	                timeStamp[0] = CardFactoryUtil.activateManland(c, 2, 2, types, keywords, "B R");
                    
					card.removeSpellAbility(X_ability);
					X_ability.setDescription("X: This creature gets +X/+0 until end of turn.");
					X_ability.setStackDescription("X: This creature gets +X/+0 until end of turn.");
					card.addSpellAbility(X_ability);
                    
		              final Command eot1 = new Command() {
		                  private static final long serialVersionUID = -132950142223575L;
		                  long stamp = timeStamp[0];
		                  public void execute() {
		                      Card c = card;
		                      String[] types = { "Creature", "Elemental" };
		                      String[] keywords = {  };
		                      CardFactoryUtil.revertManland(c, types, keywords, "B R", stamp);
		                      c.removeSpellAbility(X_ability);
		                  }
		              };
					
                    AllZone.EndOfTurn.addUntil(eot1);
                }

            };//SpellAbility
            
            final Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = 4245563898487609274L;

				public void execute() {
					// Comes into tapped Keyword gets removed, so this this command does the tapping. Keyword is still required for things like Amulet of Vigor (Not tested)
					card.tap();
                }
            };

            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            card.addComesIntoPlayCommand(comesIntoPlay);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - until end of turn, Lavaclaw Reaches becomes a 2/2 black and red Elemental creature with \"X: This creature gets +X/+0 until end of turn.\"");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1 B R: Until end of turn, Lavaclaw Reaches becomes a 2/2 black and red Elemental creature with \"X: This creature gets +X/+0 until end of turn.\" It's still a land.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Stirring Wildwood")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "1 G W") {
                @Override
                public boolean canPlayAI() {
                    return !card.hasSickness() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Elemental" };
                    String[] keywords = { "Reach" };
                    
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 3, 4, types, keywords, "G W");
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -1329533520874994575L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Elemental" };
                            String[] keywords = { "Reach" };
                            CardFactoryUtil.revertManland(c, types, keywords, "G W", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - until end of turn, Stirring Wildwood becomes a 3/4 green and white Elemental creature with reach.");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1 G W: Until end of turn, Stirring Wildwood becomes a 3/4 green and white Elemental creature with reach. It's still a land.");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Creeping Tar Pit")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "1 U B") {
                @Override
                public boolean canPlayAI() {
                    return !card.hasSickness() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Elemental" };
                    String[] keywords = { "Unblockable" };
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 3, 2, types, keywords, "U B");
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -6004932967127014386L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            String[] types = { "Creature", "Elemental" };
                            String[] keywords = { "Unblockable" };
                            CardFactoryUtil.revertManland(c, types, keywords, "U B", stamp);	
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - Until end of turn, Creeping Tar Pit becomes a 3/2 blue and black Elemental creature and is unblockable.");
            a1.setStackDescription(sb.toString());
            a1.setDescription("1 U B: Until end of turn, Creeping Tar Pit becomes a 3/2 blue and black Elemental creature and is unblockable. It's still a land.");
        }//*************** END ************ END **************************
        

        //*************** START *********** START **************************
        else if(cardName.equals("Raging Ravine")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "2 R G") {
                @Override
                public boolean canPlayAI() {
                    return !card.hasSickness() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Creature", "Elemental"};
                    String[] keywords = { };
                    
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 3, 3, types, keywords, "R G");
                    
                    // this keyword stacks, so we can't put it through the activate
                    c.addIntrinsicKeyword("Whenever this creature attacks, put a +1/+1 counter on it.");
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -2632172918887247003L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            
                            String[] types = { "Creature", "Elemental"};
                            String[] keywords = { "Whenever this creature attacks, put a +1/+1 counter on it." };
                            CardFactoryUtil.revertManland(c, types, keywords, "R G", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - until end of turn, Raging Ravine becomes a 3/3 red and green Elemental creature with \"Whenever this creature attacks, put a +1/+1 counter on it.\"");
            a1.setStackDescription(sb.toString());
            a1.setDescription("2 R G: Until end of turn, Raging Ravine becomes a 3/3 red and green Elemental creature with \"Whenever this creature attacks, put a +1/+1 counter on it.\" It's still a land.");
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Dread Statuary")) {
        	final long[] timeStamp = new long[1];
            
            final SpellAbility a1 = new Ability(card, "4") {
                @Override
                public boolean canPlayAI() {
                    return !card.hasSickness() && super.canPlayAI();
                }
                
                @Override
                public void resolve() {
                    Card c = card;
                    String[] types = { "Artifact", "Creature", "Golem"};
                    String[] keywords = { };
                    
                    timeStamp[0] = CardFactoryUtil.activateManland(c, 4, 2, types, keywords, "0");
                    
                    final Command eot1 = new Command() {
                        private static final long serialVersionUID = -2632172918887247003L;
                        long stamp = timeStamp[0];
                        public void execute() {
                            Card c = card;
                            
                            String[] types = { "Artifact", "Creature", "Golem"};
                            String[] keywords = {  };
                            CardFactoryUtil.revertManland(c, types, keywords, "", stamp);
                        }
                    };
                    
                    AllZone.EndOfTurn.addUntil(eot1);
                }
            };//SpellAbility
            
            card.clearSpellKeepManaAbility();
            card.addSpellAbility(a1);
            StringBuilder sb = new StringBuilder();
            sb.append(card).append(" - until end of turn, becomes a 4/2 Golem artifact creature until end of turn.");
            a1.setStackDescription(sb.toString());
            a1.setDescription("4: Until end of turn, becomes a 4/2 Golem artifact creature until end of turn. It's still a land.");
        }//*************** END ************ END **************************
 
        //*************** START *********** START **************************
        else if(cardName.equals("Lotus Vale")) {
        	/*
        	 * If Lotus Vale would enter the battlefield, sacrifice two untapped
        	 * lands instead. If you do, put Lotus Vale onto the battlefield.
        	 * If you don't, put it into its owner's graveyard.
        	 */
        	final Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = -194247993330560188L;
				
				final Player player = card.getController();
        		public void execute() {
        			if(player.isHuman()) {
        				final int[] paid = {0};

        				Input target = new Input() {
							private static final long serialVersionUID = -7835834281866473546L;
							public void showMessage() {
        						AllZone.Display.showMessage(cardName+" - Select an untapped land to sacrifice");
        						ButtonUtil.enableOnlyCancel();
        					}
        					public void selectButtonCancel() {
        						AllZone.GameAction.sacrifice(card);
        						stop();
        					}
        					public void selectCard(Card c, PlayerZone zone) {
        						if(c.isLand() && zone.is(Constant.Zone.Battlefield) && c.isUntapped()) {
        							AllZone.GameAction.sacrifice(c);
        							if(paid[0] < 1) {
        								paid[0]++;
        								AllZone.Display.showMessage(cardName+" - Select an untapped land to sacrifice");
        							}
        							else stop();
        						}
        					}//selectCard()
        				};//Input
        				if ((AllZoneUtil.getPlayerLandsInPlay(AllZone.HumanPlayer).filter(AllZoneUtil.untapped).size() < 2)) {
        					AllZone.GameAction.sacrifice(card);
        					return;
        				}
        				else AllZone.InputControl.setInput(target);
        			}
        			else {
        				//compy can't play this card because it has no mana pool
        			}
        		}
        	};

        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************


        //*************** START *********** START **************************
        else if(cardName.equals("Kjeldoran Outpost") || cardName.equals("Balduvian Trading Post")
        		|| cardName.equals("Heart of Yavimaya") || cardName.equals("Lake of the Dead")) {
        	
        	final String[] type = new String[1];
        	if(cardName.equals("Kjeldoran Outpost")) type[0] = "Plains";
        	else if(cardName.equals("Balduvian Trading Post")) type[0] = "Mountain.untapped";
        	else if(cardName.equals("Heart of Yavimaya")) type[0] = "Forest";
        	else if(cardName.equals("Lake of the Dead")) type[0] = "Swamp";
        	else if(cardName.equals("Soldevi Excavations")) type[0] = "Island.untapped";
        	
        	final Command comesIntoPlay = new Command() {
        		private static final long serialVersionUID = 6175830918425915833L;
        		final Player player = card.getController();
        		public void execute() {
        			final CardList land = AllZoneUtil.getPlayerCardsInPlay(player).getValidCards(type[0], player, card);

        			if( player.equals(AllZone.ComputerPlayer)) {
        				if( land.size() > 0 ) {
        					CardList tappedLand = new CardList(land.toArray());
        					tappedLand = tappedLand.filter(AllZoneUtil.tapped);
        					//if any are tapped, sacrifice it
        					//else sacrifice random
        					if( tappedLand.size() > 0 ) {
        						AllZone.GameAction.sacrifice(tappedLand.get(0));
        					}
        					else {
        						AllZone.GameAction.sacrifice(land.get(0));
        					}
        				}
        				else {
        					AllZone.GameAction.sacrifice(card);
        				}
        			}
        			else { //this is the human resolution
        				Input target = new Input() {
        					private static final long serialVersionUID = 6653677835621129465L;
        					public void showMessage() {
        						AllZone.Display.showMessage(cardName+" - Select one "+type[0]+" to sacrifice");
        						ButtonUtil.enableOnlyCancel();
        					}
        					public void selectButtonCancel() {
        						AllZone.GameAction.sacrifice(card);
        						stop();
        					}
        					public void selectCard(Card c, PlayerZone zone) {
        						if(c.isLand() && zone.is(Constant.Zone.Battlefield) && land.contains(c)) {
        							AllZone.GameAction.sacrifice(c);
        							stop();
        						}
        					}//selectCard()
        				};//Input
        				AllZone.InputControl.setInput(target);
        			}
        		}
        	};

        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        //*************** START *********** START **************************
        else if(cardName.equals("Sheltered Valley")) {
        	
        	final Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = 685604326470832887L;

				public void execute() {
					final Player player = card.getController();
					CardList land = AllZoneUtil.getPlayerCardsInPlay(player, "Sheltered Valley");
					land.remove(card);

					if( land.size() > 0 ) {
						for(Card c:land) AllZone.GameAction.sacrifice(c);
					}
				}
        	};

        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Scorched Ruins")) {
            final Command comesIntoPlay = new Command() {
               private static final long serialVersionUID = 6175830918425915833L;
               final Player player = card.getController();
               public void execute() {
                  PlayerZone play = AllZone.getZone(Constant.Zone.Battlefield, card.getController());
                  CardList plains = new CardList(play.getCards());
                  plains = plains.getType("Land");
                  plains = plains.getTapState("Untapped");

                  if( player.equals(AllZone.ComputerPlayer)) {
                     if( plains.size() > 1 ) {
                        CardList tappedPlains = new CardList(plains.toArray());
                        tappedPlains = tappedPlains.getType("Basic");
                        for(Card c : tappedPlains)
                        	   AllZone.GameAction.sacrifice(c);
                        for(int i = 0; i < tappedPlains.size(); i++){
                           AllZone.GameAction.sacrifice(plains.get(i));
                        }
                        //if any are tapped, sacrifice it
                        //else sacrifice random
                     }
                     else {
                        AllZone.GameAction.sacrifice(card);
                     }
                  }
                  else { //this is the human resolution
                     final int[] paid = {0};
                     if ((new CardList(AllZone.Human_Battlefield.getCards())
                     .getType("Land").getTapState("Untapped").size() < 2))
                    	{
                    	 AllZone.GameAction.sacrifice(card);
                    	 return;
                    	}
                     Input target = new Input() {
                        private static final long serialVersionUID = 6653677835621129465L;
                        public void showMessage() {
                           AllZone.Display.showMessage("Scorched Ruins - Select an untapped land to sacrifice");
                           ButtonUtil.enableOnlyCancel();
                        }
                        public void selectButtonCancel() {
                     	   AllZone.GameAction.sacrifice(card);
                     	   stop();
                        }
                        public void selectCard(Card c, PlayerZone zone) {
                           if(c.isLand() && zone.is(Constant.Zone.Battlefield) && c.isUntapped()) {
                              AllZone.GameAction.sacrifice(c);
                              if(paid[0] < 1){
                            	  paid[0]++;
                            	  AllZone.Display.showMessage("Scorched Ruins - Select an untapped land to sacrifice");
                              }
                              else stop();
                           }
                        }//selectCard()
                     };//Input
                     AllZone.InputControl.setInput(target);
                  }
               }
            };

            card.addComesIntoPlayCommand(comesIntoPlay);
         }//*************** END ************ END **************************

        //*************** START *********** START **************************
        else if(cardName.equals("Island of Wak-Wak")) {
        	/*
        	 * Tap: The power of target creature with flying becomes 0 until end of turn.
        	 */
        	Ability_Cost abCost = new Ability_Cost("T", cardName, true);
        	final String Tgts[] = {"Creature.withFlying"};
        	Target target = new Target(card,"Select target creature with flying.", Tgts);
        	final Ability_Activated ability = new Ability_Activated(card, abCost, target) {
        		private static final long serialVersionUID = -2090435946748184314L;

        		@Override
        		public boolean canPlayAI() {
        			Card c = getCreature();
                    if(c == null) return false;
                    else {
                        setTargetCard(c);
                        return super.canPlayAI();
                    }
                }//canPlayAI()
                
                //may return null
                private Card getCreature() {
                    CardList untapped = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
                    untapped = untapped.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isUntapped() && 0 < c.getNetDefense();
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
                        creature[0].setBaseAttack(0);
                        
                        final Command EOT = new Command() {
							private static final long serialVersionUID = 3502589085738502851L;

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
            ability.setDescription(abCost+"The power of target creature with flying becomes 0 until end of turn.");
            ability.setStackDescription(cardName+" - target creature's power becomes 0.");
            card.addSpellAbility(ability);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Magosi, the Waterveil")) {
        	/*
        	 * Magosi, the Waterveil enters the battlefield tapped.
        	 * Tap: Add Blue to your mana pool.
        	 * Blue, Tap: Put an eon counter on Magosi, the Waterveil. Skip your next turn.
        	 * Tap, Remove an eon counter from Magosi, the Waterveil and return it to its 
        	 * owner's hand: Take an extra turn after this one.
        	 */

        	Ability_Cost skipCost = new Ability_Cost("U T", card.getName(), true);
        	final SpellAbility skipTurn = new Ability_Activated(card, skipCost, null){
				private static final long serialVersionUID = -2404286785963486611L;

				@Override
        		public void resolve() {
					Player player = card.getController();
        			card.addCounter(Counters.EON, 1);
        			AllZone.Phase.addExtraTurn(player.getOpponent());                 
        		}
        	};//skipTurn
        	
        	Ability_Cost extraCost = new Ability_Cost("T SubCounter<1/EON> Return<1/CARDNAME>", card.getName(), true);
        	final SpellAbility extraTurn = new Ability_Activated(card, extraCost, null){
				private static final long serialVersionUID = -2599252144246080154L;

				@Override
        		public void resolve() {
        			AllZone.Phase.addExtraTurn(getActivatingPlayer());
        		}
        	};//extraTurn
        	
        	StringBuilder sbDesc = new StringBuilder();
        	sbDesc.append("U, tap: Put an eon counter on ").append(card.getName()).append(". Skip your next turn.");
        	skipTurn.setDescription(sbDesc.toString());
        	StringBuilder sbStack = new StringBuilder();
        	sbStack.append(card.getName()).append(" - add an Eon counter and skip you next turn.");
        	skipTurn.setStackDescription(sbStack.toString());
        	card.addSpellAbility(skipTurn);
        	
        	StringBuilder sbDesc2 = new StringBuilder();
        	sbDesc2.append(extraCost.toString());
        	sbDesc2.append("Take an extra turn after this one.");
        	extraTurn.setDescription(sbDesc2.toString());
        	StringBuilder sb = new StringBuilder();
        	sb.append(card.getName()).append(" - Take an extra turn after this one.");
        	extraTurn.setStackDescription(sb.toString());
        	card.addSpellAbility(extraTurn);
        }//*************** END ************ END **************************
        
        
        //*************** START ************ START **************************
        else if(cardName.equals("Bottomless Vault") || cardName.equals("Dwarven Hold")
        		|| cardName.equals("Hollow Trees") || cardName.equals("Icatian Store")
        		|| cardName.equals("Sand Silos")) {
        	final int[] num = new int[1];
        	String shortTemp = "";
        	if(cardName.equals("Bottomless Vault")) shortTemp = "B";
        	if(cardName.equals("Dwarven Hold")) shortTemp = "R";
        	if(cardName.equals("Hollow Trees")) shortTemp = "G";
        	if(cardName.equals("Icatian Store")) shortTemp = "W";
        	if(cardName.equals("Sand Silos")) shortTemp = "U";
        	
        	final String shortString = shortTemp;
        	StringBuilder desc = new StringBuilder();
        	desc.append("tap, Remove any number of storage counters from ").append(cardName);
        	desc.append(": Add ").append(shortString);
        	desc.append(" to your mana pool for each charge counter removed this way.");
            
        	final Ability_Mana abMana = new Ability_Mana(card, "0", shortString){
				private static final long serialVersionUID = -4506828762302357781L;
        		
                @Override
                public boolean canPlay(){
                	return false;
                }
        	};
        	abMana.undoable = false;
        	
            final Ability addMana = new Ability(card, "0", desc.toString()) {
				private static final long serialVersionUID = -7805885635696245285L;

              //@Override
                public String mana() {
                	StringBuilder mana = new StringBuilder();
                	if(num[0] == 0) mana.append("0");
                	else {
                		for(int i = 0; i < num[0]; i++) {
                			mana.append(shortString).append(" ");
                		}
                	}
                    return mana.toString().trim();
                }
                
                @Override
                public boolean canPlayAI(){
                	return false;
                }
                
                @Override
                public void resolve() {
                	abMana.produceMana(mana());
                }
            };
            
            Input runtime = new Input() {
				private static final long serialVersionUID = -4990369861806627183L;

				@Override
                public void showMessage() {
					num[0] = card.getCounters(Counters.STORAGE);
                	String[] choices = new String[num[0]+1];
                	for(int j=0;j<=num[0];j++) {
                		choices[j] = ""+j;
                	}
                    String answer = (String)(GuiUtils.getChoiceOptional("Storage counters to remove", choices));
                    if(null != answer && !answer.equals("")) {
	                    num[0] = Integer.parseInt(answer);
	                    card.tap();
	                    card.subtractCounter(Counters.STORAGE, num[0]);
	                    stop();
	                    AllZone.Stack.add(addMana);
	                    return;
                    }
                    stop();
                }
            };
            
            addMana.setDescription(desc.toString());
            addMana.setBeforePayMana(runtime);
            card.addSpellAbility(addMana);
            card.addSpellAbility(abMana);
        }//*************** END ************ END **************************

        //*************** START *********** START **************************
        //Lorwyn Dual Lands, and a couple Morningtide...
        else if(cardName.equals("Ancient Amphitheater") || cardName.equals("Auntie's Hovel")
                || cardName.equals("Gilt-Leaf Palace") || cardName.equals("Secluded Glen")
                || cardName.equals("Wanderwine Hub")
                || cardName.equals("Rustic Clachan") || cardName.equals("Murmuring Bosk")) {
        	
        	String shortTemp = "";
        	if(cardName.equals("Ancient Amphitheater")) shortTemp = "Giant";
        	if(cardName.equals("Auntie's Hovel")) shortTemp = "Goblin";
        	if(cardName.equals("Gilt-Leaf Palace")) shortTemp = "Elf";
        	if(cardName.equals("Secluded Glen")) shortTemp = "Faerie";
        	if(cardName.equals("Wanderwine Hub")) shortTemp = "Merfolk";
        	if(cardName.equals("Rustic Clachan")) shortTemp = "Kithkin";
        	if(cardName.equals("Murmuring Bosk")) shortTemp = "Treefolk";
        	
        	final String type = shortTemp;
        	
        	
            card.addComesIntoPlayCommand(new Command() {
				private static final long serialVersionUID = -5646344170306812481L;

				public void execute() {
                    if(card.getController().isHuman()) humanExecute();
                    else computerExecute();
                }
                
                public void computerExecute() {
                    CardList hand = AllZoneUtil.getPlayerHand(AllZone.ComputerPlayer);
                    hand = hand.filter(AllZoneUtil.getTypeFilter(type));
                    if(hand.size() > 0) revealCard(hand.get(0));
                    else card.tap();
                }
                
                public void humanExecute() {
                	AllZone.InputControl.setInput(new Input() {
						private static final long serialVersionUID = -2774066137824255680L;

						@Override
        				public void showMessage() {
        					AllZone.Display.showMessage(card.getName()+" - Reveal a card.");
        					ButtonUtil.enableOnlyCancel();
        				}

        				@Override
        				public void selectCard(Card c, PlayerZone zone) {
        					if(zone.is(Constant.Zone.Hand) && c.isType(type)) {
            					JOptionPane.showMessageDialog(null, "Revealed card: "+c.getName(), card.getName(), JOptionPane.PLAIN_MESSAGE);
            					stop();
        					}
        				}
        				
        				@Override
        				public void selectButtonCancel() {
        					card.tap();
        					stop();
        				}
        			});
                }//execute()
                
                private void revealCard(Card c) {
                	JOptionPane.showMessageDialog(null, c.getController()+" reveals "+c.getName(), card.getName(), JOptionPane.PLAIN_MESSAGE);
                }
            });
        }//*************** END ************ END **************************
        

        //*************** START ************ START **************************

        else if(cardName.equals("Calciform Pools") || cardName.equals("Dreadship Reef") ||
        		cardName.equals("Fungal Reaches")  || cardName.equals("Molten Slagheap") ||
        		cardName.equals("Saltcrusted Steppe")) {
        	/*
        	 * tap, Remove X storage counters from Calciform Pools: Add X mana in any combination of W and/or U to your mana pool.
        	 */
        	final int[] num = new int[1];
        	final int[] split = new int[1];
        	
        	String pTemp = "";
        	String sTemp = "";
        	if(cardName.equals("Calciform Pools")) { pTemp = "W"; sTemp = "U"; }
        	if(cardName.equals("Dreadship Reef")) { pTemp = "U"; sTemp = "B"; }
        	if(cardName.equals("Fungal Reaches")) { pTemp = "R"; sTemp = "G"; }
        	if(cardName.equals("Molten Slagheap")) { pTemp = "B"; sTemp = "R"; }
        	if(cardName.equals("Saltcrusted Steppe")) { pTemp = "G"; sTemp = "W"; }
        	
        	final String primary = pTemp;
        	final String secondary = sTemp;
        	
        	StringBuilder description = new StringBuilder();
        	description.append("1, Remove X storage counters from ").append(cardName);
        	description.append(": Add X mana in any combination of ").append(primary);
        	description.append(" and/or ").append(secondary).append(" to your mana pool.");
        	
        	// This dummy AbMana is for Reflecting and for having an abMana produce mana
        	final Ability_Mana abMana = new Ability_Mana(card, "0", primary+" "+secondary){
				private static final long serialVersionUID = -4506828762302357781L;
        		
                @Override
                public boolean canPlay(){
                	return false;
                }
        	};
        	abMana.undoable = false;
        	
        	final Ability addMana = new Ability(card, "1", description.toString()) {
				private static final long serialVersionUID = 7177960799748450242L;

				//@Override
                public String mana() {
                	StringBuilder mana = new StringBuilder();
                	for(int i = 0; i < split[0]; i++) {
                		mana.append(primary).append(" ");
                	}
                	for(int j = 0; j < num[0] - split[0]; j++) {
                		mana.append(secondary).append(" ");
                	}
                    return mana.toString().trim();
                }
                
                
                @Override
                public boolean canPlayAI(){
                	return false;
                }
                
                @Override
                public void resolve() {
                	abMana.undoable = false;
                	abMana.produceMana(mana());
                }
            };
            
            Input runtime = new Input() {
				private static final long serialVersionUID = -8808673510875540608L;

				@Override
                public void showMessage() {
					num[0] = card.getCounters(Counters.STORAGE);
                	String[] choices = new String[num[0]+1];
                	for(int j=0;j<=num[0];j++) {
                		choices[j] = ""+j;
                	}
                    String answer = (String)(GuiUtils.getChoiceOptional(
                            "Storage counters to remove", choices));
                    if (answer == null){
                    	stop();
                    	return;
                    }
                    	
                    num[0] = Integer.parseInt(answer);
                    
                    String splitNum = (String)(GuiUtils.getChoiceOptional(
                            "Number of "+primary+" to add", choices));
                    if (splitNum == null){
                    	stop();
                    	return;
                    }
                    
                    split[0] = Integer.parseInt(splitNum);
                    if(num[0] > 0 || split[0] > 0) {
                    	card.subtractCounter(Counters.STORAGE, num[0]);
                    	stop();
                    	AllZone.Stack.add(addMana);
                    	return;
                    }
                    stop();
                }
            };
            addMana.setDescription(description.toString());
            addMana.setAfterPayMana(runtime);
            card.addSpellAbility(addMana);
            card.addSpellAbility(abMana);
        }//*************** END ************ END **************************
        
        
        //*************** START *********** START **************************
        else if(cardName.equals("Crosis's Catacombs") || cardName.equals("Darigaaz's Caldera") ||
        		cardName.equals("Dromar's Cavern")    || cardName.equals("Rith's Grove") ||
        		cardName.equals("Trava's Ruins")) {
        	final Command comesIntoPlay = new Command() {
				private static final long serialVersionUID = 7813334062721799674L;

				public void execute() {
        			final Player player = card.getController();
        			CardList land = AllZoneUtil.getPlayerLandsInPlay(player);
        			land = land.getNotType("Lair");

        			if( player.isComputer()) {
        				if( land.size() > 0 ) {
        					CardList tappedLand = new CardList(land.toArray());
        					tappedLand = tappedLand.filter(AllZoneUtil.tapped);
        					if( tappedLand.size() > 0 ) {
        						AllZone.GameAction.moveToHand(CardFactoryUtil.getWorstLand(tappedLand));
        					}
        					else {
        						AllZone.GameAction.moveToHand(CardFactoryUtil.getWorstLand(land));
        					}
        				}
        				else {
        					AllZone.GameAction.sacrifice(card);
        				}
        			}
        			else { //this is the human resolution
        				Input target = new Input() {
							private static final long serialVersionUID = 7944127258985401036L;
							public void showMessage() {
        						AllZone.Display.showMessage(cardName+" - Select one non-Lair land to return to your hand");
        						ButtonUtil.enableOnlyCancel();
        					}
        					public void selectButtonCancel() {
        						AllZone.GameAction.sacrifice(card);
        						stop();
        					}
        					public void selectCard(Card c, PlayerZone zone) {
        						if(c.isLand() && zone.is(Constant.Zone.Battlefield, AllZone.HumanPlayer) && !c.getType().contains("Lair")) {
        							AllZone.GameAction.moveToHand(c);
        							stop();
        						}
        					}//selectCard()
        				};//Input
        				AllZone.InputControl.setInput(target);
        			}
        		}
        	};

        	card.addComesIntoPlayCommand(comesIntoPlay);
        }//*************** END ************ END **************************
        
        return card;
    }
    
}
