
package forge;


import java.util.ArrayList;


public class PlayerZone_ComesIntoPlay extends DefaultPlayerZone {
    private static final long serialVersionUID = 5750837078903423978L;
    
    private boolean           trigger          = true;
    private boolean           leavesTrigger    = true;
	static boolean SimultaneousEntry = false; // For Cards with Multiple Token Entry. Only Affects Allies at the moment.
	static int SimultaneousEntryCounter = 1; // For Cards with Multiple Token Entry. Only Affects Allies at the moment.
    
    public PlayerZone_ComesIntoPlay(String zone, String player) {
        super(zone, player);
    }
    
    @Override
    public void add(Object o) {
        if(o == null) throw new RuntimeException("PlayerZone_ComesInto Play : add() object is null");
        
        super.add(o);
        
        Card c = (Card) o;
        final String player = c.getController();
        
        if(trigger && ((CardFactoryUtil.oppHasKismet(c.getController()) && (c.isLand() || c.isCreature() || c.isArtifact()))
        		|| (AllZoneUtil.isCardInPlay("Root Maze") && (c.isLand() || c.isArtifact()))
        		|| (AllZoneUtil.isCardInPlay("Orb of Dreams") && c.isPermanent()))) c.tap();
        
        //cannot use addComesIntoPlayCommand - trigger might be set to false;
        // Keep track of max lands can play per turn
        int addMax = 0;
        boolean isHuman = c.getController().equals(Constant.Player.Human);
        boolean adjustLandPlays = false;
        boolean eachPlayer = false;
        
        if(c.getName().equals("Exploration") || c.getName().equals("Oracle of Mul Daya")) {
        	addMax = 1;
        	adjustLandPlays = true;
        } 
        else if(c.getName().equals("Azusa, Lost but Seeking")) {
        	addMax = 2;
        	adjustLandPlays = true;
        }
        else if (c.getName().equals("Storm Cauldron") || c.getName().equals("Rites of Flourishing")){
        	// these two aren't in yet, but will just need the other part of the card to work with more lands
        	adjustLandPlays = true;
        	eachPlayer = true;
        	addMax = 1;
        }
        // 7/13: fastbond code removed, fastbond should be unlimited and will be handled elsewhere.
        
        if (adjustLandPlays){
        	if (eachPlayer){
        		AllZone.GameInfo.addHumanMaxPlayNumberOfLands(addMax);
        		AllZone.GameInfo.addComputerMaxPlayNumberOfLands(addMax);
        	}
        	else if (isHuman)
        		AllZone.GameInfo.addHumanMaxPlayNumberOfLands(addMax);
        	else
        		AllZone.GameInfo.addComputerMaxPlayNumberOfLands(addMax);
        }
        
        if(trigger) {
            c.setSickness(true);// summoning sickness
            c.comesIntoPlay();
            AllZone.GameAction.CheckWheneverKeyword(c,"EntersBattleField",null);
            
            PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
            PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, c.getController());
            
            //Amulet of Vigor
            if(c.isTapped()) {
                final Card untapCrd = c;
                Ability ability = new Ability(c, "0") {
                    @Override
                    public void resolve() {
                        untapCrd.untap();
                    }
                };
                ability.setStackDescription("Amulet of Vigor - Untap " + c);
                for(int i = 0; i < CardFactoryUtil.getCards("Amulet of Vigor", c.getController()).size(); i++)
                    AllZone.Stack.add(ability);
            }
            
            if(c.isCreature() && (c.getType().contains("Elf")) || c.getKeyword().contains("Changeling")) {
            	CardList list = AllZoneUtil.getPlayerCardsInPlay(c.getController(), "Elvish Vanguard");
                
            	//not for the Elvish Vanguard coming into play now
            	list.remove(c);
                for(Card var:list) {
                    GameActionUtil.Elvish_Vanguard(var);
                }
            }
            
            if(c.isLand()) {
                //System.out.println("A land just came into play: " + c.getName());
                
                CardList list = new CardList(play.getCards());
                CardList graveList = new CardList(grave.getCards());
                
                CardList listValakut = list.filter(new CardListFilter() {
                	public boolean addCard(Card c) {
                		return c.getName().contains("Valakut, the Molten Pinnacle");
                	}
                });
                
                list = list.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return c.getKeyword().contains("Landfall") || 
                        	   c.getKeyword().contains("Landfall - Whenever a land enters the battlefield under your control, CARDNAME gets +2/+2 until end of turn.");
                    }
                });
                
                graveList = graveList.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return c.getName().equals("Bloodghast");
                    }
                });
                
                for(Card crd:graveList) {
                    list.add(crd);
                }
                
                for(int i = 0; i < list.size(); i++) {
                    GameActionUtil.executeLandfallEffects(list.get(i));
                }
                
                // Check for a mountain
                if (!listValakut.isEmpty() && c.getType().contains("Mountain") ) {
                	for (int i = 0; i < listValakut.size(); i++) {
                		boolean b = GameActionUtil.executeValakutEffect(listValakut.get(i),c);
                		if (!b) {
                			// Not enough mountains to activate Valakut -- stop the loop
                			break;
                		}
                	}
                }
                
                CardList ankhs = AllZoneUtil.getCardsInPlay("Ankh of Mishra");
                final Card ankhLand = c;
                for(Card ankh:ankhs) {
                	final Card source = ankh;
                	SpellAbility ability = new Ability(source, "") {
                		@Override
                		public void resolve() {
                			AllZone.GameAction.addDamage(ankhLand.getController(), source, 2);
                		}
                	};
                	ability.setStackDescription(source+" - deals 2 damage to "+ankhLand.getController());
                	AllZone.Stack.add(ability);
                }
                
                CardList seeds = AllZoneUtil.getCardsInPlay("Seed the Land");
                final Card seedLand = c;
                for(Card seed:seeds) {
                	final Card source = seed;
                	SpellAbility ability = new Ability(source, "") {
                		@Override
                		public void resolve() {
                			CardFactoryUtil.makeToken("Snake", "G 1 1 Snake", seedLand.getController(),
                					"G", new String[] {"Creature", "Snake"}, 1, 1, new String[] {});
                		}
                	};
                	ability.setStackDescription(source+" - "+seedLand.getController()+" puts a 1/1 green Snake token in play");
                	AllZone.Stack.add(ability);
                }
                
                //Tectonic Instability
                CardList tis = AllZoneUtil.getCardsInPlay("Tectonic Instability");
                final Card tisLand = c;
                for(Card ti:tis) {
                	final Card source = ti;
                	SpellAbility ability = new Ability(source, "") {
                		@Override
                		public void resolve() {
                			CardList lands = AllZoneUtil.getPlayerCardsInPlay(tisLand.getController());
                			lands = lands.filter(AllZoneUtil.lands);
                			for(Card land:lands) land.tap();
                		}
                	};
                	ability.setStackDescription(source+" - tap all lands "+tisLand.getController()+"controls.");
                	AllZone.Stack.add(ability);
                }
            }//isLand()
            
            //hack to make tokens trigger ally effects:
            CardList clist = new CardList(play.getCards());
            clist = clist.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return c.getName().equals("Conspiracy") && c.getChosenType().equals("Ally");
                }
            });
            
            String[] allyNames = {
                    "Umara Raptor", "Tuktuk Grunts", "Oran-Rief Survivalist", "Nimana Sell-Sword",
                    "Makindi Shieldmate", "Kazandu Blademaster", "Turntimber Ranger", "Highland Berserker",
                    "Joraga Bard", "Bojuka Brigand", "Graypelt Hunter", "Kazuul Warlord"};
            final ArrayList<String> allyNamesList = new ArrayList<String>();
            
            for(int i = 0; i < allyNames.length; i++) {
                allyNamesList.add(allyNames[i]);
            }
            
            if(SimultaneousEntry == false) { // For Cards with Multiple Token Entry. Only Affects Allies at the moment.
            	for(int i = 0; i < SimultaneousEntryCounter; i++) {
            if(c.getType().contains("Ally") || (c.getKeyword().contains("Changeling") && c.isCreature())
                    || (clist.size() > 0 && (c.getType().contains("Creature") || c.getKeyword().contains(
                            "Changeling"))) || allyNamesList.contains(c.getName())) {
                CardList list = new CardList(play.getCards());
                list = list.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return c.getType().contains("Ally") || c.getKeyword().contains("Changeling")
                                || allyNamesList.contains(c.getName());
                    }
                });
                
                for(Card var:list) {
                    GameActionUtil.executeAllyEffects(var);
                }
            }
            	}
            	SimultaneousEntryCounter = 1;
        } else SimultaneousEntryCounter = SimultaneousEntryCounter + 1;
        }
        
        if(AllZone.StaticEffects.getCardToEffectsList().containsKey(c.getName())) {
            String[] effects = AllZone.StaticEffects.getCardToEffectsList().get(c.getName());
            for(String effect:effects) {
                AllZone.StaticEffects.addStateBasedEffect(effect);
            }
        }
        
        PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, c.getController());
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
        CardList meek = new CardList(grave.getCards());
        
        meek = meek.getName("Sword of the Meek");
        
        if(meek.size() > 0 && c.isCreature() && c.getNetAttack() == 1 && c.getNetDefense() == 1) {
            for(int i = 0; i < meek.size(); i++) {
                final Card crd = meek.get(i);
                final Card creat = c;
                final PlayerZone graveZone = grave;
                final PlayerZone playZone = play;
                Ability ability = new Ability(meek.get(i), "0") {
                    @Override
                    public void resolve() {
                        if(crd.getController().equals(Constant.Player.Human)) {
                            String[] choices = {"Yes", "No"};
                            
                            Object q = null;
                            
                            q = AllZone.Display.getChoiceOptional("Attach " + crd + " to " + creat + "?", choices);
                            if(q == null || q.equals("No")) ;
                            else if(AllZone.GameAction.isCardInZone(crd, graveZone)
                                    && AllZone.GameAction.isCardInPlay(creat) && creat.isCreature()
                                    && creat.getNetAttack() == 1 && creat.getNetDefense() == 1) {
                                graveZone.remove(crd);
                                playZone.add(crd);
                                
                                crd.equipCard(creat);
                            }
                            
                        } else //computer
                        {
                            if(AllZone.GameAction.isCardInZone(crd, graveZone)
                                    && AllZone.GameAction.isCardInPlay(creat) && creat.isCreature()
                                    && creat.getNetAttack() == 1 && creat.getNetDefense() == 1) {
                                graveZone.remove(crd);
                                playZone.add(crd);
                                
                                crd.equipCard(creat);
                            }
                        }
                    }
                };
                
                ability.setStackDescription("Sword of the Meek - Whenever a 1/1 creature enters the battlefield under your control, you may return Sword of the Meek from your graveyard to the battlefield, then attach it to that creature.");
                AllZone.Stack.add(ability);
            }
        }
        
        /*
        for (String effect : AllZone.StateBasedEffects.getStateBasedMap().keySet() ) {
        	Command com = GameActionUtil.commands.get(effect);
        	com.execute();
        }
        */

        //System.out.println("Size: " + AllZone.StateBasedEffects.getStateBasedMap().size());
    }
    
    @Override
    public void remove(Object o) {
        
        super.remove(o);
        
        Card c = (Card) o;
        
        // Keep track of max lands can play per turn
        int addMax = 0;
        boolean isHuman = c.getController().equals(Constant.Player.Human);
        boolean adjustLandPlays = false;
        boolean eachPlayer = false;
        
        if(c.getName().equals("Exploration") || c.getName().equals("Oracle of Mul Daya")) {
        	addMax = -1;
        	adjustLandPlays = true;
        } else if(c.getName().equals("Azusa, Lost but Seeking")) {
        	addMax = -2;
        	adjustLandPlays = true;
        } 
        else if (c.getName().equals("Storm Cauldron") || c.getName().equals("Rites of Flourishing")){
        	// once their second half of their abilities are programmed these two can be added in
        	adjustLandPlays = true;
        	eachPlayer = true;
        	addMax = -1;
        }
        // 7/12: fastbond code removed, fastbond should be unlimited and will be handled elsewhere.
        
        if (adjustLandPlays){
        	if (eachPlayer){
        		AllZone.GameInfo.addHumanMaxPlayNumberOfLands(addMax);
        		AllZone.GameInfo.addComputerMaxPlayNumberOfLands(addMax);
        	}
        	else if (isHuman)
        		AllZone.GameInfo.addHumanMaxPlayNumberOfLands(addMax);
        	else
        		AllZone.GameInfo.addComputerMaxPlayNumberOfLands(addMax);
        }
        

        if(leavesTrigger) {
        	AllZone.GameAction.CheckWheneverKeyword(c,"LeavesBattleField",null);
        	c.leavesPlay();
        }
        
        if(AllZone.StaticEffects.getCardToEffectsList().containsKey(c.getName())) {
            String[] effects = AllZone.StaticEffects.getCardToEffectsList().get(c.getName());
            String tempEffect = "";
            for(String effect:effects) {
                tempEffect = effect;
                AllZone.StaticEffects.removeStateBasedEffect(effect);
                Command comm = GameActionUtil.commands.get(tempEffect); //this is to make sure cards reset correctly
                comm.execute();
            }
            
        }
        for(String effect:AllZone.StaticEffects.getStateBasedMap().keySet()) {
            Command com = GameActionUtil.commands.get(effect);
            com.execute();
        }
        
    }
    
    public void setTrigger(boolean b) {
        trigger = b;
    }
    
    public void setLeavesTrigger(boolean b) {
        leavesTrigger = b;
    }
    
    public void setTriggers(boolean b) {
        trigger = b;
        leavesTrigger = b;
    }
}
