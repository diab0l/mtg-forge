
package forge;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import com.esotericsoftware.minlog.Log;

import forge.Constant.Zone;


public class CardFactoryUtil {
    private static Random random = new Random();
    
    // who uses this function?
    public final static String getPumpString(int n) {
        if(0 <= n) return "+" + n;
        else return "" + n;
    }
    
    public static Card AI_getMostExpensivePermanent(CardList list, final Card spell, boolean targeted) {
        CardList all = list;
        if(targeted) {
            all = all.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardFactoryUtil.canTarget(spell, c);
                }
            });
        }
        if(all.size() == 0) return null;
        
        //get biggest Permanent
        Card biggest = null;
        biggest = all.get(0);
        
        for(int i = 0; i < all.size(); i++) {
            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) >= CardUtil.getConvertedManaCost(biggest.getManaCost())) {
                biggest = all.get(i);
            }
        }
        
        return biggest;
        
    }
    
  //for Sarkhan the Mad
    public static Card AI_getCheapestCreature(CardList list, final Card spell, boolean targeted) {
    	list = list.filter(new CardListFilter() {
    		public boolean addCard(Card c) {
    			return c.isCreature();
    		}
    	});
    	return AI_getCheapestPermanent(list, spell, targeted);
    }
    
    public static Card AI_getCheapestPermanent(CardList list, final Card spell, boolean targeted) {
        CardList all = list;
        if(targeted) {
            all = all.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardFactoryUtil.canTarget(spell, c);
                }
            });
        }
        if(all.size() == 0) return null;
        
        //get cheapest card:
        Card cheapest = null;
        cheapest = all.get(0);
        
        for(int i = 0; i < all.size(); i++) {
            if(CardUtil.getConvertedManaCost(cheapest.getManaCost()) <= CardUtil.getConvertedManaCost(cheapest.getManaCost())) {
                cheapest = all.get(i);
            }
        }
        
        return cheapest;
        
    }
    
    public static Card AI_getBestLand(CardList list) {
        CardList land = list.getType("Land");
        if(!(land.size() > 0)) return null;
        
        CardList nbLand = land.filter(new CardListFilter() // prefer to target non basic lands
        {
            public boolean addCard(Card c) {
                return (!c.getType().contains("Basic"));
            }
        });
        
        if(nbLand.size() > 0) {
            //TODO: Rank non basics?
            
            Random r = new Random();
            return nbLand.get(r.nextInt(nbLand.size()));
        }
        
        // if no non-basic lands, target the least represented basic land type
        String names[] = {"Plains", "Island", "Swamp", "Mountain", "Forest"};
        String sminBL = "";
        int iminBL = 20000; // hopefully no one will ever have more than 20000 lands of one type....
        int n = 0;
        for(int i = 0; i < 5; i++) {
            n = land.getType(names[i]).size();
            if(n < iminBL && n > 0) // if two or more are tied, only the first one checked will be used
            {
                iminBL = n;
                sminBL = names[i];
            }
        }
        if(iminBL == 20000) return null; // no basic land was a minimum
        
        CardList BLand = land.getType(sminBL);
        for(int i = 0; i < BLand.size(); i++)
            if(!BLand.get(i).isTapped()) // prefer untapped lands
            return BLand.get(i);
        
        Random r = new Random();
        return BLand.get(r.nextInt(BLand.size())); // random tapped land of least represented type
    }
    
    
//The AI doesn't really pick the best enchantment, just the most expensive.
    public static Card AI_getBestEnchantment(CardList list, final Card spell, boolean targeted) {
        CardList all = list;
        all = all.getType("Enchantment");
        if (targeted)
        {
        	all = all.filter(new CardListFilter() {
            
	            public boolean addCard(Card c) {
	                return CardFactoryUtil.canTarget(spell, c);
	            }            
        	});
        }
        if(all.size() == 0) {
            return null;
        }
        
        //get biggest Enchantment
        Card biggest = null;
        biggest = all.get(0);
        
        for(int i = 0; i < all.size(); i++) {
            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) >= CardUtil.getConvertedManaCost(biggest.getManaCost())) {
                biggest = all.get(i);
            }
        }
        
        return biggest;
    }
    
    
//The AI doesn't really pick the best artifact, just the most expensive.
    public static Card AI_getBestArtifact(CardList list) {
        CardList all = list;
        all = all.getType("Artifact");
        if(all.size() == 0) {
            return null;
        }
        
        //get biggest Artifact
        Card biggest = null;
        biggest = all.get(0);
        
        for(int i = 0; i < all.size(); i++) {
            if(CardUtil.getConvertedManaCost(biggest.getManaCost()) >= CardUtil.getConvertedManaCost(biggest.getManaCost())) {
                biggest = all.get(i);
            }
        }
        
        return biggest;
    }
    
    public static CardList AI_getHumanArtifact(final Card spell, boolean targeted) {
        CardList artifact = new CardList(AllZone.Human_Play.getCards());
        artifact = artifact.getType("Artifact");
        if(targeted) {
            artifact = artifact.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return canTarget(spell, c);
                }
            });
        }
        return artifact;
    }
    
    public static CardList AI_getHumanEnchantment(final Card spell, boolean targeted) {
        CardList enchantment = new CardList(AllZone.Human_Play.getCards());
        enchantment = enchantment.getType("Enchantment");
        if(targeted) {
            enchantment = enchantment.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return canTarget(spell, c);
                }
            });
        }
        return enchantment;
    }
    
    
//yes this is more hacky code
    //Object[0] is the cardname
    //Object[1] is the max number of times it can be used per turn
    //Object[1] has to be an Object like Integer and not just an int
    private static Object[][] AbilityLimits = {
            {"Azimaet Drake", Integer.valueOf(1)}, {"Drake Hatchling", Integer.valueOf(1)},
            {"Fire Drake", Integer.valueOf(1)}, {"Plated Rootwalla", Integer.valueOf(1)},
            {"Rootwalla", Integer.valueOf(1)}, {"Spitting Drake", Integer.valueOf(1)},
            {"Ghor-Clan Bloodscale", Integer.valueOf(1)}, {"Wild Aesthir", Integer.valueOf(1)},
            {"Viashino Slaughtermaster", Integer.valueOf(1)}, {"Twinblade Slasher", Integer.valueOf(1)},
            {"Boreal Centaur", Integer.valueOf(1)}, {"Knight of the Skyward Eye", Integer.valueOf(1)},
            {"Chronatog", Integer.valueOf(1)}, {"Putrid Leech", Integer.valueOf(1)},

            {"Phyrexian Battleflies", Integer.valueOf(2)}, {"Pit Imp", Integer.valueOf(2)},
            {"Roterothopter", Integer.valueOf(2)}, {"Vampire Bats", Integer.valueOf(2)},
            {"Fire-Belly Changeling", Integer.valueOf(2)}, {"Azusa, Lost but Seeking", Integer.valueOf(2)},
            {"Oracle of Mul Daya", Integer.valueOf(1)}}; 
    
    public static boolean canUseAbility(Card card) {
        int found = -1;
        
        //try to find card name in AbilityLimits[][]
        for(int i = 0; i < AbilityLimits.length; i++)
            if(AbilityLimits[i][0].equals(card.getName())) found = i;
        
        if(found == -1) return true;
        
        //card was found
        if(card.getAbilityTurnUsed() != AllZone.Phase.getTurn()) {
            card.setAbilityTurnUsed(AllZone.Phase.getTurn());
            card.setAbilityUsed(0);
        }
        SpellAbility sa;
        //this is a hack, check the stack to see if this card has an ability on the stack
        //if so, we can't use the ability: this is to prevent using a limited ability too many times
        for(int i = 0; i < AllZone.Stack.size(); i++) {
            sa = AllZone.Stack.peek(i);
            if(sa.getSourceCard().equals(card)) return false;
        }
        
        Integer check = (Integer) AbilityLimits[found][1];
        return card.getAbilityUsed() < check.intValue();
    }//canUseAbility(Card card)
    

    public static boolean AI_doesCreatureAttack(Card card) {
        Combat combat = ComputerUtil.getAttackers();
        Card[] att = combat.getAttackers();
        for(int i = 0; i < att.length; i++)
            if(att[i].equals(card)) return true;
        
        return false;
    }
    
    public static Card AI_getBestCreature(CardList list, Card c) {
        final Card crd = c;
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return CardFactoryUtil.canTarget(crd, c);
            }
        });
        
        return AI_getBestCreature(list);
        
    }
    
    //returns null if list.size() == 0
    public static Card AI_getBestCreature(CardList list) {
        CardList all = list;
        all = all.getType("Creature");
        
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
        return biggest;
    }
    
  //returns null if list.size() == 0
    public static Card AI_getWorstCreature(CardList list) {
        CardList all = list;
        all = all.getType("Creature");
        //get smallest creature
        Card smallest = null;
        
        if(all.size() != 0) {
            smallest = all.get(0);
            
            for(int i = 0; i < all.size(); i++)
                if(smallest.getNetAttack() > all.get(i).getNetAttack()) smallest = all.get(i);
        }
        return smallest;
    }
    
    public static Input input_targetCreaturePlayer(final SpellAbility spell, boolean targeted, boolean free) {
        return input_targetCreaturePlayer(spell, Command.Blank, targeted, free);
    }
    
    public static Input input_targetCreaturePlayer(final SpellAbility spell, final Command paid, final boolean targeted, final boolean free) {
        Input target = new Input() {
            private static final long serialVersionUID = 2781418414287281005L;
            
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
            public void selectCard(Card card, PlayerZone zone) {
                if((card.isCreature() || card.isPlaneswalker()) && zone.is(Constant.Zone.Play)
                        && (!targeted || canTarget(spell, card))) {
                    spell.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(String player) {
                spell.setTargetPlayer(player);
                done();
            }
            
            void done() {
                paid.execute();
                
                if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                        (Ability_Tap) spell));
                else if(spell.getManaCost().equals("0") || this.isFree()) {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetCreaturePlayer()
    
    public static Input input_Spell(final SpellAbility spell, final CardList choices, final boolean free) {
        Input target = new Input() {
            private static final long serialVersionUID = 2781418414287281005L;
            
            @Override
            public void showMessage() {
            	if(choices.size() == 0) stop();
            	if(spell.getTargetCard() != null) stop();
                AllZone.Display.showMessage("Select target Spell: ");
            	Card choice = AllZone.Display.getChoiceOptional("Choose a Spell", choices.toArray());
                if(choice != null) {
                	spell.setTargetCard(choice);
                	done();
                }
                else stop();
                
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
           
            void done() {
            	choices.clear();
                if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                        (Ability_Tap) spell));
                else if(spell.getManaCost().equals("0") || this.isFree()) {
                	if(spell.getTargetCard() != null) AllZone.Stack.add(spell);               	
                	stop();
                }
                 else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetSpell()
    
    public static Input input_targetNonCreaturePermanent(final SpellAbility spell, final Command paid) {
        Input target = new Input() {
            private static final long serialVersionUID = 8796813407167561318L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target noncreature permanent");
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(!card.isCreature() && zone.is(Constant.Zone.Play)) {
                    spell.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            void done() {
                paid.execute();
                
                if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                        (Ability_Tap) spell));
                else if(spell.getManaCost().equals("0") || this.isFree()) {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetNonCreaturePermanent()
    
    public static Input input_targetPermanent(final SpellAbility spell) {
        Input target = new Input() {
            private static final long serialVersionUID = -7635051691776562901L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target permanent");
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(card.isPermanent() && zone.is(Constant.Zone.Play)) {
                    spell.setTargetCard(card);
                    done();
                }
            }//selectCard()
            
            void done() {
                
                if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                        (Ability_Tap) spell));
                else if(spell.getManaCost().equals("0") || this.isFree()) {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetPermanent()
    

    //CardList choices are the only cards the user can successful select
    //sacrifices one of the CardList choices
    public static Input input_sacrifice(final SpellAbility spell, final CardList choices, final String message) {
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(choices.contains(card)) {
                    AllZone.getZone(card).remove(card);
                    AllZone.GameAction.moveToGraveyard(card);
                    
                    if(spell.getManaCost().equals("0") || this.isFree()) {
                        this.setFree(false);
                        AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                }
            }
        };
        return target;
    }//input_sacrifice()
    
    public static Input Wheneverinput_sacrifice(final SpellAbility spell, final CardList choices, final String message, final Command Paid) {
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(choices.contains(card)) {
                    AllZone.getZone(card).remove(card);
                    AllZone.GameAction.moveToGraveyard(card);
                    Paid.execute();
                    stop();
                }
            }
        };
        return target;
    }//Wheneverinput_sacrifice()
    
    public static Input input_sacrifice(final SpellAbility spell, final CardList choices, final String message, final boolean free) {
        Input target = new Input() {

			private static final long serialVersionUID = 3391527854483291332L;

			@Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(choices.contains(card)) {
                    AllZone.getZone(card).remove(card);
                    AllZone.GameAction.moveToGraveyard(card);
                    
                    if (free)
                    	this.setFree(true);
                    
                    if(spell.getManaCost().equals("0") || this.isFree()) {
                        this.setFree(false);
                        AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                }
            }
        };
        return target;
    }//input_sacrifice()
    
    //this one is used for Phyrexian War Beast:
    public static Input input_sacrificePermanent(final SpellAbility spell, final CardList choices, final String message) {
        Input target = new Input() {
			private static final long serialVersionUID = 5927821262821559665L;

			@Override
            public void showMessage() {
            	if (choices.size()==0) {
            		stop();
            	}
            	else
            	{
	                AllZone.Display.showMessage(message);
	                ButtonUtil.disableAll();
            	}
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(choices.contains(card)) {
                    //AllZone.getZone(card).remove(card);
                    AllZone.GameAction.sacrifice(card);
                    stop();
                    
                    if(spell.getManaCost().equals("0")) {
                        AllZone.Stack.add(spell);
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                }
            }
        };
        return target;
    }//input_sacrifice()
    
    
    public static Input input_sacrificePermanent(final CardList choices, final String message) {
        Input target = new Input() {
            private static final long serialVersionUID = 2685832214519141903L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(choices.contains(card)) {
                    //AllZone.getZone(card).remove(card);
                    AllZone.GameAction.sacrifice(card);
                    stop();
                }
            }
        };
        return target;
    }//input_sacrifice()
    
    public static Input input_sacrificePermanents(final int nCards) {
    	Input target = new Input() {
			private static final long serialVersionUID = -8149416676562317629L;
			int                       n                = 0;
            
            @Override
            public void showMessage() {
            	CardList list = new CardList(AllZone.Human_Play.getCards());
                list = list.filter(new CardListFilter(){
                	public boolean addCard(Card c)
                	{
                		return c.isPermanent() && !c.getName().equals("Mana Pool");
                	}
                });
                if(n == nCards || list.size() == 0) stop();
                
                AllZone.Display.showMessage("Select a permanent to sacrifice (" +(nCards-n) +" left)");
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.equals(AllZone.Human_Play) && !card.getName().equals("Mana Pool")) {
                    AllZone.GameAction.sacrifice(card);
                    n++;
                    
                    //in case no more {type}s in play
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    list = list.filter(new CardListFilter(){
                    	public boolean addCard(Card c)
                    	{
                    		return c.isPermanent() && !c.getName().equals("Mana Pool");
                    	}
                    });
                    if(n == nCards || list.size() == 0) stop();
                    else
                    	showMessage();
                }
            }
        };
        return target;
    }//input_sacrificePermanents()
    
    public static Input input_sacrificePermanents(final int nCards, final String type) {
    	Input target = new Input() {
			private static final long serialVersionUID = 1981791992623774490L;
			int                       n                = 0;
            
            @Override
            public void showMessage() {
            	//in case no more {type}s in play
                CardList list = new CardList(AllZone.Human_Play.getCards());
                list = list.getType(type);
                if(n == nCards || list.size() == 0) stop();
            	
                AllZone.Display.showMessage("Select a " +type +" to sacrifice (" +(nCards-n) +" left)");
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.equals(AllZone.Human_Play) && card.getType().contains(type)) {
                    AllZone.GameAction.sacrifice(card);
                    n++;
                    
                    //in case no more {type}s in play
                    CardList list = new CardList(AllZone.Human_Play.getCards());
                    list = list.getType(type);
                    if(n == nCards || list.size() == 0) stop();
                    else
                    	showMessage();
                }
            }
        };
        return target;
    }//input_sacrificePermanents()
    
    
    public static Input input_putFromHandToLibrary(final String TopOrBottom, final int num) {
        Input target = new Input() {
            private static final long serialVersionUID = 5178077952030689103L;
            public int                n                = 0;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select a card to put on the " + TopOrBottom + " of your library.");
                ButtonUtil.disableAll();
                
                if(n == num || AllZone.Human_Hand.getCards().length == 0) stop();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand)) {
                    AllZone.Human_Hand.remove(card);
                    
                    if(TopOrBottom.equals("top")) AllZone.Human_Library.add(card, 0);
                    else if(TopOrBottom.equals("bottom")) AllZone.Human_Library.add(card);
                    
                    n++;
                    if(n == num) stop();
                    
                    showMessage();
                }
            }
        };
        return target;
    }
    
    public static Input input_discardNumUnless(final int nCards, final String uType) {
        Input target = new Input() {
            private static final long serialVersionUID = 8822292413831640944L;
            
            int                       n                = 0;
            
            @Override
            public void showMessage() {
            	if (AllZone.Human_Hand.getCards().length == 0) stop();
                AllZone.Display.showMessage("Select " + (nCards - n) + " cards to discard, unless you discard a "
                        + uType + ".");
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand)) {
                    AllZone.GameAction.discard(card);
                    n++;
                    
                    if(card.getType().contains(uType.toString())) stop();
                    
                    else {
	                    if(n == nCards || AllZone.Human_Hand.getCards().length == 0) stop();
	                    else
	                    	showMessage();
                    }
                }
            }
        };
        
        return target;
    }//input_discardNumUnless
    

    public static SpellAbility ability_Untap(final Card sourceCard, String cost) {
        final SpellAbility a1 = new Ability(sourceCard, cost) {
            @Override
            public boolean canPlay() {
                return sourceCard.isTapped() && super.canPlay();
            }
            
            @Override
            public void resolve() {
                sourceCard.untap();
            }
        };//SpellAbility
        //sourceCard.addSpellAbility(a1);
        a1.setDescription(cost + ": Untap " + sourceCard.getName() + ".");
        a1.setStackDescription("Untap " + sourceCard.getName());
        
        a1.setBeforePayMana(new Input_PayManaCost(a1));
        return a1;
    }
    
    public static SpellAbility ability_Flashback(final Card sourceCard, String manaCost, String lifeloss) {
        final int loss = Integer.parseInt(lifeloss);
        final SpellAbility flashback = new Spell(sourceCard) {
            
            private static final long serialVersionUID = -4196027546564209412L;
            
            @Override
            public void resolve() {
                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, sourceCard.getController());
                PlayerZone removed = AllZone.getZone(Constant.Zone.Removed_From_Play, sourceCard.getController());
                
                SpellAbility[] sa = sourceCard.getSpellAbility();
                
                AllZone.Stack.add(sa[0]);
                
                grave.remove(sourceCard);
                removed.add(sourceCard);
                
                AllZone.GameAction.getPlayerLife(sourceCard.getController()).subtractLife(loss,sourceCard);
                
            }
            
            @Override
            public boolean canPlayAI() {
                PlayerLife compLife = AllZone.GameAction.getPlayerLife("Computer");
                int life = compLife.getLife();
                
                return (life > (loss + 2));
            }
            
            @Override
            public boolean canPlay() {
                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, sourceCard.getController());
                String phase = AllZone.Phase.getPhase();
                String activePlayer = AllZone.Phase.getActivePlayer();
                
                ArrayList<Card> spellsOnStack = AllZone.Stack.getSourceCards();
                Card sourceCard = this.getSourceCard();
                
                return AllZone.GameAction.isCardInZone(sourceCard, grave) && !spellsOnStack.contains(sourceCard)
                        && (sourceCard.isInstant() || (phase.equals(Constant.Phase.Main1) || phase.equals(Constant.Phase.Main2))
                                && sourceCard.getController().equals(activePlayer) && AllZone.Stack.size() == 0);
                
            }
            
        };
        
        String lifecost = "";
        if(loss != 0) lifecost = ", pay " + lifeloss + " life";
        
        flashback.setFlashBackAbility(true);
        flashback.setManaCost(manaCost);
        flashback.setDescription("Flashback: " + manaCost + lifecost);
        flashback.setStackDescription("Flashback: " + sourceCard.getName());
        
        return flashback;
        
    }//ability_Flashback()
    
    public static Ability ability_Unearth(final Card sourceCard, String manaCost) {

        final Ability unearth = new Ability(sourceCard, manaCost) {
            
			private static final long serialVersionUID = -5633945565395478009L;


			@Override
            public void resolve() {
                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, sourceCard.getController());
                //PlayerZone removed = AllZone.getZone(Constant.Zone.Removed_From_Play, sourceCard.getController());
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, sourceCard.getController());
                
                grave.remove(sourceCard);
                play.add(sourceCard);
                
                sourceCard.addIntrinsicKeyword("At the beginning of the end step, sacrifice CARDNAME.");
                sourceCard.addIntrinsicKeyword("Haste");
                sourceCard.setUnearthed(true);
                /*
                final Command entersPlay = new Command()
                {
                	public void execute()
                	{
                		sourceCard.setUnearthed(true);
                	}
                };
                sourceCard.addComesIntoPlayCommand(entersPlay);
                
                
                final Command leavesPlay = new Command()
                {
					private static final long serialVersionUID = -8640915882354670864L;

					public void execute()
                	{
                		AllZone.GameAction.removeUnearth(sourceCard);
                	}
                };
                sourceCard.addLeavesPlayCommand(leavesPlay);
                */
                
            }
            
            
            @Override
            public boolean canPlay() {
                PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, sourceCard.getController());
                String phase = AllZone.Phase.getPhase();
                String activePlayer = AllZone.Phase.getActivePlayer();
                
                return AllZone.GameAction.isCardInZone(sourceCard, grave)
                        && ((phase.equals(Constant.Phase.Main1) || phase.equals(Constant.Phase.Main2))
                                && sourceCard.getController().equals(activePlayer) && AllZone.Stack.size() == 0);
                
            }
            
        };
        
        unearth.setFlashBackAbility(true);
        //unearth.setManaCost(manaCost);
        //unearth.setDescription("Unearth: " + manaCost);
        unearth.setStackDescription("Unearth: " + sourceCard.getName());
        
        return unearth;
        
    }//ability_Unearth()
    
    public static SpellAbility ability_Spore_Saproling(final Card sourceCard) {
        final SpellAbility ability = new Ability(sourceCard, "0") {
            @Override
            public boolean canPlay() {
                SpellAbility sa;
                for(int i = 0; i < AllZone.Stack.size(); i++) {
                    sa = AllZone.Stack.peek(i);
                    if(sa.getSourceCard().equals(sourceCard) && super.canPlay()) return false;
                }
                
                if(sourceCard.getCounters(Counters.SPORE) >= 3 && AllZone.GameAction.isCardInPlay(sourceCard)) return true;
                else return false;
            }
            
            @Override
            public boolean canPlayAI() {
                return true;
            }
            
            @Override
            public void resolve() {
                sourceCard.subtractCounter(Counters.SPORE, 3);
                
                makeToken("Saproling", "G 1 1 Saproling", sourceCard, "G", new String[] {
                        "Creature", "Saproling"}, 1, 1, new String[] {""});
            }
        };
        ability.setDescription("Remove three spore counters from CARDNAME: Put a 1/1 green Saproling creature token onto the battlefield.");
//      ability.setDescription("Remove three spore counters from " + sourceCard.getName()
//              + ": Put a 1/1 green Saproling creature token onto the battlefield.");
        ability.setStackDescription(sourceCard.getName()
                + " - put a 1/1 green Saproling creature token onto the battlefield.");
        return ability;
    }//ability_Spore_Saproling()
    
    public static SpellAbility ability_Morph_Down(final Card sourceCard) {
        final String player = sourceCard.getController();
        final SpellAbility morph_down = new Spell(sourceCard) {
            private static final long serialVersionUID = -1438810964807867610L;
            
            @Override
            public void resolve() {
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
                
                //card.setName("Morph");
                sourceCard.setIsFaceDown(true);
                sourceCard.setManaCost("");
                sourceCard.setBaseAttack(2);
                sourceCard.setBaseDefense(2);
                sourceCard.comesIntoPlay();
                sourceCard.setIntrinsicKeyword(new ArrayList<String>()); //remove all keywords
                sourceCard.setType(new ArrayList<String>()); //remove all types
                sourceCard.addType("Creature");
                
                hand.remove(sourceCard);
                play.add(sourceCard);
            }
            
            @Override
            public boolean canPlay() {
                return AllZone.Phase.getActivePlayer().equals(sourceCard.getController())
                        && (AllZone.Phase.getPhase().equals(Constant.Phase.Main1) || AllZone.Phase.getPhase().equals(
                                Constant.Phase.Main2)) && !AllZone.Phase.getPhase().equals("End of Turn")
                        && !AllZone.GameAction.isCardInPlay(sourceCard);
            }
            
        };
        
        morph_down.setManaCost("3");
        morph_down.setDescription("You may play this face down as a 2/2 creature for 3. Turn it face up any time for its morph cost.");
        morph_down.setStackDescription("Morph - Creature 2/2");
        
        return morph_down;
    }
    
    public static SpellAbility ability_Spellbomb(final Card sourceCard) {
        final SpellAbility ability = new Ability(sourceCard, "1") {
            @Override
            public boolean canPlay() {
                return AllZone.GameAction.isCardInPlay(sourceCard)
                        && !AllZone.Stack.getSourceCards().contains(sourceCard);//in play and not already activated(Sac cost problems)
            }
            
            @Override
            public boolean canPlayAI() {
                return (AllZone.Computer_Hand.size() < 4) && (AllZone.Computer_Library.size() > 0)
                        && MyRandom.random.nextBoolean();
            }
            
            @Override
            public void resolve() {
                AllZone.GameAction.drawCard(sourceCard.getController());
                AllZone.GameAction.sacrifice(getSourceCard());
            }
        };
        ability.setDescription("1, Sacrifice " + sourceCard.getName() + ": Draw a card.");
        ability.setStackDescription(sourceCard.getName() + " - Draw a card.");
        return ability;
    }
    
    public static Ability ability_Morph_Up(final Card sourceCard, String cost, String orgManaCost, int a, int d) {
        //final String player = sourceCard.getController();
        //final String manaCost = cost;
        final int attack = a;
        final int defense = d;
        final String origManaCost = orgManaCost;
        final Ability morph_up = new Ability(sourceCard, cost) {
            private static final long serialVersionUID = -7892773658629724785L;
            
            @Override
            public void resolve() {
                //PlayerZone hand = AllZone.getZone(Constant.Zone.Hand ,player);
                //PlayerZone play = AllZone.getZone(Constant.Zone.Play ,player);
                
                //card.setName("Morph");
                sourceCard.setIsFaceDown(false);
                sourceCard.setManaCost(origManaCost);
                sourceCard.setBaseAttack(attack);
                sourceCard.setBaseDefense(defense);
                sourceCard.setIntrinsicKeyword(sourceCard.getPrevIntrinsicKeyword());
                sourceCard.setType(sourceCard.getPrevType());
                sourceCard.turnFaceUp();
            }
            
            @Override
            public boolean canPlay() {
        		CardList Silence = AllZoneUtil.getPlayerCardsInPlay(AllZone.GameAction.getOpponent(getSourceCard().getController()));
        		Silence = Silence.getName("Linvala, Keeper of Silence");
                return sourceCard.isFaceDown() && AllZone.GameAction.isCardInPlay(sourceCard) && Silence.size() == 0;
            }
            
        };//morph_up
        
        morph_up.setManaCost(cost);
        morph_up.setDescription(cost + " - turn this card face up.");
        morph_up.setStackDescription(sourceCard.getName() + " - turn this card face up.");
        
        return morph_up;
        
    }
    
    /*
      public static SpellAbility spellability_spDamageP(final Card sourceCard, final String dmg)
      {
          final int damage = Integer.parseInt(dmg);
          
          final SpellAbility spDamageP = new Spell(sourceCard)
          {
    		private static final long serialVersionUID = -1263171535312610675L;
    		
    		@SuppressWarnings("unused")  // check
    		Card check;
             
             public boolean canPlayAI()
             {
                     return false;
             }
             
             public void chooseTargetAI()
             {
                  CardFactoryUtil.AI_targetHuman();
                   return;
             }
             
             public void resolve()
             {
                      AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage);
             }
          };
          spDamageP.setDescription(sourceCard.getName() + " deals " + damage + " damage to target player.");
          spDamageP.setStackDescription(sourceCard.getName() +" deals " + damage + " damage.");
          spDamageP.setBeforePayMana(CardFactoryUtil.input_targetPlayer(spDamageP));
          return spDamageP;
      }//Spellability_spDamageP
      
      public static SpellAbility spellability_spDamageCP(final Card sourceCard, final String dmg)
      {
       final int damage = Integer.parseInt(dmg); // converting string dmg -> int

       final SpellAbility DamageCP =  new Spell(sourceCard)
       {
    	private static final long serialVersionUID = 7239608350643325111L;
    	
    	Card check;
          //Shock's code here atm
          public boolean canPlayAI()
          {
              if(AllZone.Human_Life.getLife() <= damage)
                return true;
                
              PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
              CardList hand = new CardList(compHand.getCards());
                   
               if (hand.size() >= 8)
                return true;
             
              check = getFlying();
              return check != null;
          }
              
          public void chooseTargetAI()
          {
              if(AllZone.Human_Life.getLife() <= damage)
              {
                setTargetPlayer(Constant.Player.Human);
                return;
              }
                
              PlayerZone compHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
               CardList hand = new CardList(compHand.getCards());
                
              if(getFlying() == null && hand.size() >= 7 ) //not 8, since it becomes 7 when getting cast
               {
                  setTargetPlayer(Constant.Player.Human);
                  return;
               }
             
              Card c = getFlying();
              
              if (check == null &&  c != null)
            	  System.out.println("Check equals null");
              else if((c == null) || (! check.equals(c)))
                throw new RuntimeException(sourceCard +" error in chooseTargetAI() - Card c is " +c +",  Card check is " +check);
             
              if (c != null)
            	  setTargetCard(c);
              else
            	  setTargetPlayer(Constant.Player.Human);
          }//chooseTargetAI()
             
             //uses "damage" variable
          Card getFlying()
          {
        	  CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", sourceCard, true);
              for(int i = 0; i < flying.size(); i++)
            	  if(flying.get(i).getNetDefense() <= damage){
            		  System.out.println("getFlying() returns " + flying.get(i).getName());
            		  return flying.get(i);
            	  }
              
            System.out.println("getFlying() returned null");
            return null;
          }
          public void resolve()
          {
                  
              if(getTargetCard() != null)
              {
                if(AllZone.GameAction.isCardInPlay(getTargetCard()) && canTarget(sourceCard, getTargetCard()))
                {
                    Card c = getTargetCard();
                    //c.addDamage(damage);
                    AllZone.GameAction.addDamage(c, damage);
                }
              }
              else
                AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(damage);
              //resolve()
          }
       }; //spellAbility
       DamageCP.setDescription(sourceCard.getName() + " deals " + damage + " damage to target creature or player.");
       DamageCP.setStackDescription(sourceCard.getName() +" deals " + damage + " damage.");
       DamageCP.setBeforePayMana(CardFactoryUtil.input_targetCreaturePlayer(DamageCP, true));
       return DamageCP;
      }//spellability_DamageCP
     */

    public static SpellAbility ability_Merc_Search(final Card sourceCard, String cost) {
        final int intCost = Integer.parseInt(cost);
        //final String player = sourceCard.getController();
        
        final SpellAbility ability = new Ability_Tap(sourceCard, cost) {
            private static final long serialVersionUID = 4988299801575232348L;
            
            @Override
            public boolean canPlay() {
                SpellAbility sa;
                for(int i = 0; i < AllZone.Stack.size(); i++) {
                    sa = AllZone.Stack.peek(i);
                    if(sa.getSourceCard().equals(sourceCard)) return false;
                }
                
                if(AllZone.GameAction.isCardInPlay(sourceCard) && !sourceCard.hasSickness()
                        && !sourceCard.isTapped() && super.canPlay()) return true;
                else return false;
            }
            
            @Override
            public boolean canPlayAI() {
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                CardList mercs = new CardList();
                CardList list = new CardList(lib.getCards());
                list = list.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return ((c.getType().contains("Mercenary") || c.getKeyword().contains("Changeling")))
                                && c.isPermanent();
                    }
                });
                

                if(list.size() == 0) return false;
                
                for(int i = 0; i < list.size(); i++) {
                    if(CardUtil.getConvertedManaCost(list.get(i).getManaCost()) <= intCost) {
                        mercs.add(list.get(i));
                    }
                }
                
                if(AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && mercs.size() > 0) return true;
                else return false;
            }
            
            @Override
            public void resolve() {
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, sourceCard.getController());
                
                CardList mercs = new CardList();
                CardList list = new CardList(lib.getCards());
                list = list.getType("Mercenary");
                
                if(list.size() == 0) return;
                
                for(int i = 0; i < list.size(); i++) {
                    if(CardUtil.getConvertedManaCost(list.get(i).getManaCost()) <= intCost) {
                        mercs.add(list.get(i));
                    }
                }
                if(mercs.size() == 0) return;
                
                if(sourceCard.getController().equals(Constant.Player.Computer)) {
                    Card merc = AI_getBestCreature(mercs);
                    lib.remove(merc);
                    play.add(merc);
                } else //human
                {
                    Object o = AllZone.Display.getChoiceOptional("Select target Mercenary", mercs.toArray());
                    if(o != null) {
                        Card merc = (Card) o;
                        lib.remove(merc);
                        play.add(merc);
                    }
                }
                AllZone.GameAction.shuffle(sourceCard.getController());
            }
        };
        ability.setDescription(cost
                + ", tap: Search your library for a Mercenary permanent card with converted mana cost " + cost
                + " or less and put it into play. Then shuffle your library.");
        ability.setStackDescription(sourceCard.getName() + " - search for a Mercenary and put it into play.");
        return ability;
    }
    
    public static SpellAbility ability_Rebel_Search(final Card sourceCard, String cost) {
        String costMinusOne = "";
        int a = Integer.parseInt(cost);
        a--;
        costMinusOne = Integer.toString(a);
        final int converted = a;
        //final String player = sourceCard.getController();
        
        final SpellAbility ability = new Ability_Tap(sourceCard, cost) {
            private static final long serialVersionUID = 7219065355049285681L;
            
            @Override
            public boolean canPlay() {
                SpellAbility sa;
                for(int i = 0; i < AllZone.Stack.size(); i++) {
                    sa = AllZone.Stack.peek(i);
                    if(sa.getSourceCard().equals(sourceCard)) return false;
                }
                
                if(AllZone.GameAction.isCardInPlay(sourceCard) && !sourceCard.hasSickness()
                        && !sourceCard.isTapped() && super.canPlay()) return true;
                else return false;
            }
            
            @Override
            public boolean canPlayAI() {
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                
                CardList rebels = new CardList();
                CardList list = new CardList(lib.getCards());
                list = list.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return ((c.getType().contains("Rebel") || c.getKeyword().contains("Changeling")))
                                && c.isPermanent();
                    }
                });
                
                if(list.size() == 0) return false;
                
                for(int i = 0; i < list.size(); i++) {
                    if(CardUtil.getConvertedManaCost(list.get(i).getManaCost()) <= converted) {
                        rebels.add(list.get(i));
                    }
                }
                
                if(AllZone.Phase.getPhase().equals(Constant.Phase.Main2) && rebels.size() > 0) return true;
                else return false;
                
            }
            
            
            @Override
            public void resolve() {
                
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, sourceCard.getController());
                
                CardList rebels = new CardList();
                CardList list = new CardList(lib.getCards());
                list = list.getType("Rebel");
                
                if(list.size() == 0) return;
                
                for(int i = 0; i < list.size(); i++) {
                    if(CardUtil.getConvertedManaCost(list.get(i).getManaCost()) <= converted) {
                        rebels.add(list.get(i));
                    }
                }
                if(rebels.size() == 0) return;
                
                if(sourceCard.getController().equals(Constant.Player.Computer)) {
                    Card rebel = AI_getBestCreature(rebels);
                    lib.remove(rebel);
                    play.add(rebel);
                } else //human
                {
                    Object o = AllZone.Display.getChoiceOptional("Select target Rebel", rebels.toArray());
                    if(o != null) {
                        Card rebel = (Card) o;
                        lib.remove(rebel);
                        play.add(rebel);
                        if(rebel.isAura()) {
                            Object obj = null;
                            if(rebel.getKeyword().contains("Enchant creature")) {
                                PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
                                CardList creats = new CardList(play.getCards());
                                creats.addAll(oppPlay.getCards());
                                creats = creats.getType("Creature");
                                obj = AllZone.Display.getChoiceOptional("Pick a creature to attach "
                                        + rebel.getName() + " to", creats.toArray());
                            }
                            if(obj != null) {
                                Card target = (Card) obj;
                                if(AllZone.GameAction.isCardInPlay(target)) {
                                    rebel.enchantCard(target);
                                }
                            }
                            
                        }
                    }
                }
                AllZone.GameAction.shuffle(sourceCard.getController());
            }
        };
        ability.setDescription(cost
                + ", tap: Search your library for a Rebel permanent card with converted mana cost " + costMinusOne
                + " or less and put it into play. Then shuffle your library.");
        ability.setStackDescription(sourceCard.getName() + " - search for a Rebel and put it into play.");
        return ability;
    }
    
    public static SpellAbility ability_cycle(final Card sourceCard, final String cycleCost) {
        final SpellAbility cycle = new Ability_Hand(sourceCard, cycleCost) {
            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public boolean canPlayAI() {
                return false;
            }
            
            @Override
            public void resolve() {
                AllZone.GameAction.discard(sourceCard);
                AllZone.GameAction.drawCard(sourceCard.getController());
                sourceCard.cycle();
            }
        };
        cycle.setDescription("Cycling " + cycleCost + " (" + cycleCost + ", Discard this card: Draw a card.)");
        cycle.setStackDescription(sourceCard + " Cycling: Draw a card");
        return cycle;
    }//ability_cycle()
    
    public static SpellAbility ability_typecycle(final Card sourceCard, final String cycleCost, final String type) {
        String description;
        final SpellAbility cycle = new Ability_Hand(sourceCard, cycleCost) {
            

            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public boolean canPlayAI() {
                return false;
            }
            
            // some AI code could be added (certain colored mana needs analyze method maybe)
            
            @Override
            public boolean canPlay() {
                if(super.canPlay()) return true;
                else return false;
            }
            
            @Override
            public void resolve() {
                
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, sourceCard.getController());
                

                CardList cards = new CardList(lib.getCards());
                CardList sameType = new CardList();
                
                for(int i = 0; i < cards.size(); i++) {
                    if(cards.get(i).getType().contains(type)) {
                        sameType.add(cards.get(i));
                    }
                }
                

                if(sameType.size() == 0) {
                	AllZone.GameAction.discard(sourceCard);
                	return;
                }
                

                Object o = AllZone.Display.getChoiceOptional("Select a card", sameType.toArray());
                if(o != null) {
                    //ability.setTargetCard((Card)o);
                    //AllZone.Stack.add(ability);
                    AllZone.GameAction.discard(sourceCard);
                    Card c1 = (Card) o;
                    lib.remove(c1);
                    hand.add(c1);
                    

                }
                AllZone.GameAction.shuffle(sourceCard.getController());
                

            }
        };
        if(type.contains("Basic")) description = "basic land";
        else description = type;
        cycle.setDescription(description + "cycling " + cycleCost + " (" + cycleCost
                + ", Discard this card:  Search your library for a " + description
                + " card, reveal it, and put it into your hand. Then shuffle your library.");
        cycle.setStackDescription(sourceCard + " " + description + "cycling: Search your library for a "
                + description + " card.");
        return cycle;
    }//ability_typecycle()
    
    
    public static SpellAbility ability_transmute(final Card sourceCard, final String transmuteCost) {
        final SpellAbility transmute = new Ability_Hand(sourceCard, transmuteCost) {
            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public boolean canPlayAI() {
                return false;
            }
            
            @Override
            public boolean canPlay() {
                if((AllZone.Phase.getPhase().equals(Constant.Phase.Main2)
                        && AllZone.Phase.getActivePlayer() == sourceCard.getController() && super.canPlay())
                        || (AllZone.Phase.getPhase().equals(Constant.Phase.Main1)
                                && AllZone.Phase.getActivePlayer() == sourceCard.getController() && super.canPlay())) return true;
                else return false;
            }
            
            
            @Override
            public void resolve() {
                PlayerZone lib = AllZone.getZone(Constant.Zone.Library, sourceCard.getController());
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, sourceCard.getController());
                

                CardList cards = new CardList(lib.getCards());
                CardList sameCost = new CardList();
                
                for(int i = 0; i < cards.size(); i++) {
                    if(CardUtil.getConvertedManaCost(cards.get(i).getManaCost()) == CardUtil.getConvertedManaCost(sourceCard.getManaCost())) {
                        sameCost.add(cards.get(i));
                    }
                }
                

                if(sameCost.size() == 0) return;
                

                Object o = AllZone.Display.getChoiceOptional("Select a card", sameCost.toArray());
                if(o != null) {
                    //ability.setTargetCard((Card)o);
                    //AllZone.Stack.add(ability);
                    AllZone.GameAction.discard(sourceCard);
                    Card c1 = (Card) o;
                    lib.remove(c1);
                    hand.add(c1);
                    

                }
                AllZone.GameAction.shuffle(sourceCard.getController());
            }
            
        };
        transmute.setDescription("Transmute "
                + transmuteCost
                + " ("
                + transmuteCost
                + ", Discard this card: Search your library for a card with the same converted mana cost as the discarded card, reveal that card, and put it into your hand. Then shuffle your library. Play this ability only any time you could play a sorcery.)");
        transmute.setStackDescription(sourceCard
                + " Transmute: Search your library for a card with the same converted mana cost.");
        return transmute;
    }//ability_transmute()
    
    public static SpellAbility ability_suspend(final Card sourceCard, final String suspendCost, final int suspendCounters) {
        final SpellAbility suspend = new Ability_Hand(sourceCard, suspendCost) {
			private static final long serialVersionUID = 21625903128384507L;

			@Override
			public boolean canPlay(){
				// if not in hand can't suspend
				if (!AllZone.GameAction.isCardInZone(sourceCard, AllZone.getZone(Zone.Hand, sourceCard.getOwner())))
					return false;
				
				if (sourceCard.isInstant())
					return true;
				return Phase.canCastSorcery(sourceCard.getOwner());
			}
			
			@Override
            public boolean canPlayAI() {
                return false;
            }
            
            @Override
            public void resolve() {
            	AllZone.GameAction.removeFromGame(sourceCard);
            	sourceCard.addCounter(Counters.TIME, suspendCounters);
            }
        };
        suspend.setDescription("Suspend " +suspendCounters + ": "+ suspendCost);
        suspend.setStackDescription(sourceCard + " suspending for " + suspendCounters + " turns.)");
        return suspend;
    }//ability_cycle()
    
    public static SpellAbility eqPump_Equip(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, final String Manacost) {
        final Ability equip = new Ability(sourceCard, Manacost) {
            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public void resolve() {
                if (AllZone.GameAction.isCardInPlay(getTargetCard()) && 
                       CardFactoryUtil.canTarget(sourceCard, getTargetCard())) {
                    if (sourceCard.isEquipping()) {
                        Card crd = sourceCard.getEquipping().get(0);
                        if (crd.equals(getTargetCard())) return;
                        
                        sourceCard.unEquipCard(crd);
                    }
                    sourceCard.equipCard(getTargetCard());
                }
            }
            
            // An animated artifact equipmemt can't equip a creature
            @Override
            public boolean canPlay() {
                return AllZone.getZone(sourceCard).is(Constant.Zone.Play) && 
                       AllZone.Phase.getActivePlayer().equals(sourceCard.getController()) && 
                       !sourceCard.isCreature() && 
                       (AllZone.Phase.getPhase().equals("Main1") || 
                       AllZone.Phase.getPhase().equals("Main2"));
            }
            
            @Override
            public boolean canPlayAI() {
                return getCreature().size() != 0 && 
                !sourceCard.isEquipping();
            }
            
            
            @Override
            public void chooseTargetAI() {
                Card target = CardFactoryUtil.AI_getBestCreature(getCreature());
                setTargetCard(target);
            }
            
            CardList getCreature() {    // build list and do some pruning
                CardList list = new CardList(AllZone.Computer_Play.getCards());
                list = list.filter(new CardListFilter() {
                    public boolean addCard(Card c) {
                        return c.isCreature() && (!CardFactoryUtil.AI_doesCreatureAttack(c)) && 
                               CardFactoryUtil.canTarget(sourceCard, c) && 
                               (!c.getKeyword().contains("Defender")) && 
                               (c.getNetDefense() + Tough > 0);
                    }
                });
                // list.remove(card);      // if mana-only cost, allow self-target
                
                // is there at least 1 Loxodon Punisher to target
                
                CardList equipMagnetList = list.getName("Loxodon Punisher");
                if (equipMagnetList.size() != 0 && Tough >= -1) {    // we want Loxodon Punisher to gain at least +1 toughness
                	return equipMagnetList;
                }
                
                if (Power == 0 && Tough == 0) {    // This aura is keyword only
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c){
                            ArrayList<String> extKeywords = new ArrayList<String>(Arrays.asList(extrinsicKeywords));
                            for (String s:extKeywords) {
                                if (!c.getKeyword().contains(s))
                                    return true;    // We want to give a new keyword
                            }
                                //no new keywords:
                                return false;
                        }
                    });
                }
                
                return list;
            }//getCreature()
            
        };//equip ability
        
        Input runtime = new Input() {
            private static final long serialVersionUID = -6785656229070523470L;
            
            @Override
            public void showMessage() {
                //get all creatures you control
                CardList list = new CardList();
                list.addAll(AllZone.Human_Play.getCards());
                list = list.getType("Creature");
                
                stopSetNext(CardFactoryUtil.input_targetSpecific(equip, list, "Select target creature to equip",
                        true, false));
            }
        };//Input
        
        equip.setBeforePayMana(runtime);
        
        equip.setDescription("Equip: " + Manacost);
        return equip;
    }//eqPump_Equip() ( was vanila_equip() )
    
    public static Command eqPump_onEquip(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, final String Manacost) {
        
        Command onEquip = new Command() {
            
            private static final long serialVersionUID = 8130682765214560887L;
            
            public void execute() {
                if (sourceCard.isEquipping()) {
                    Card crd = sourceCard.getEquipping().get(0);
                    
                    for(int i = 0; i < extrinsicKeywords.length; i ++)
                    {
                    	if (! (extrinsicKeywords[i].equals ("none")) && (! crd.getKeyword().contains(extrinsicKeywords[i])))    // prevent Flying, Flying
                    		   crd.addExtrinsicKeyword(extrinsicKeywords[i]);
                    }
                    
                    crd.addSemiPermanentAttackBoost(Power);
                    crd.addSemiPermanentDefenseBoost(Tough);
                }
            }//execute()
        };//Command
        

        return onEquip;
    }//eqPump_onEquip ( was vanila_onequip() )
    
    public static Command eqPump_unEquip(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, final String Manacost) {
        
        Command onUnEquip = new Command() {
            
            private static final long serialVersionUID = 5783423127748320501L;
            
            public void execute() {
                if(sourceCard.isEquipping()) {
                    Card crd = sourceCard.getEquipping().get(0);
                    
                    for (int i = 0; i < extrinsicKeywords.length; i ++)
                    {
                    	crd.removeExtrinsicKeyword(extrinsicKeywords[i]);
                    }
                    
                    crd.addSemiPermanentAttackBoost(-1 * Power);
                    crd.addSemiPermanentDefenseBoost(-1 * Tough);
                    
                }
                
            }//execute()
        };//Command
        
        return onUnEquip;
    }//eqPump_unEquip ( was vanila_unequip() )
    
    public static SpellAbility enPump_Enchant(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, 
    		final String[] spellDescription, final String[] stackDescription) {
    	
        final SpellAbility enchant = new Spell(sourceCard) {
			private static final long serialVersionUID = -8259560434384053776L;
			
			/*
			 *  for flash, which is not working through the keyword for some reason
			 *  if not flash then limit to main 1 and 2 on controller's turn and card in hand
			 */
            @Override
            public boolean canPlay() {
                return (sourceCard.getKeyword().contains("Flash") && (AllZone.GameAction.isCardInZone(sourceCard, AllZone.Human_Hand) || 
                        AllZone.GameAction.isCardInZone(sourceCard, AllZone.Computer_Hand)) 
                            || 
                       (! sourceCard.getKeyword().contains("Flash") && (sourceCard.getController().equals(AllZone.Phase.getActivePlayer()) &&
                       (AllZone.GameAction.isCardInZone(sourceCard, AllZone.Human_Hand) || AllZone.GameAction.isCardInZone(sourceCard, AllZone.Computer_Hand)) && 
                       (AllZone.Phase.getPhase().equals(Constant.Phase.Main1) || AllZone.Phase.getPhase().equals(Constant.Phase.Main2)))));
            }// CanPlay (for auras with Flash)
			
            public boolean canPlayAI() {
                CardList list = new CardList(AllZone.Computer_Play.getCards());
                list = list.getType("Creature");
                
                if (list.isEmpty()) return false;
                
                //else (is there a Rabid Wombat or a Uril, the Miststalker to target?)
                
                if (Tough >= -1) {    // we want Rabid Wombat or a Uril, the Miststalker to gain at least +1 toughness
                    CardList auraMagnetList = new CardList(AllZone.Computer_Play.getCards());
                    auraMagnetList = auraMagnetList.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                	        return c.isCreature() && (c.getName().equals("Rabid Wombat") || c.getName().equals("Uril, the Miststalker"));
                	    }
                    });
                    if (! auraMagnetList.isEmpty()) {    // AI has a special target creature(s) to enchant
                        auraMagnetList.shuffle();
                        for (int i = 0; i < auraMagnetList.size(); i++) {
                            if (CardFactoryUtil.canTarget(sourceCard, auraMagnetList.get(i))) {
                                setTargetCard(auraMagnetList.get(i));    // Target only Rabid Wombat or Uril, the Miststalker
                	            return true;
                	    	}
                	    }
                    }
                }
                
                /*
                 *  else (if aura is keyword only)
                 *  Do not duplicate keyword or enchant card with Defender or enchant card already enchanted
                 */
                if (Power == 0 && Tough == 0) {
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c){
                            ArrayList<String> extKeywords = new ArrayList<String>(Arrays.asList(extrinsicKeywords));
                            for (String s:extKeywords) {
                                if (!c.getKeyword().contains(s) && !c.getKeyword().contains("Defender") && !c.isEnchanted())
                                    return true;
                            }
                                // no new keywords:
                                return false;
                        }
                    });
                }
                
                /*
                 *  else aura is power/toughness boost and may have keyword(s)
                 *  Do not reduce power to <= zero or kill by reducing toughness to <= zero
                 *  Do not enchant card with Defender or enchant card already enchanted
                 */
                CardListUtil.sortAttack(list);
                CardListUtil.sortFlying(list);
                
                for (int i = 0; i < list.size(); i++) {
                    if (CardFactoryUtil.canTarget(sourceCard, list.get(i)) && 
                            list.get(i).getNetAttack() + Power > 0 && list.get(i).getNetDefense() + Tough > 0 && 
                            !list.get(i).getKeyword().contains("Defender") && !list.get(i).isEnchanted()) {
                        setTargetCard(list.get(i));
                        return true;
                    }
                }
                return false;
            }//canPlayAI()

			public void resolve() {
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, sourceCard.getController());
                play.add(sourceCard);
                
                Card c = getTargetCard();
                
                if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(sourceCard, c)) {
                	sourceCard.enchantCard(c);
                    //System.out.println("Enchanted: " +getTargetCard());
                }
            }//resolve()
        };//enchant ability
        enchant.setBeforePayMana(CardFactoryUtil.input_targetCreature(enchant));
        enchant.setDescription(spellDescription[0]);
        enchant.setStackDescription(stackDescription[0]);
        
		return enchant;
    }//enPump_Enchant()
    
    public static Command enPump_onEnchant(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, 
    		final String[] spellDescription, final String[] stackDescription) {
    	
        Command onEnchant = new Command() {
            
			private static final long serialVersionUID = -357890638647936585L;

			public void execute() {
                if (sourceCard.isEnchanting()) {
                    Card crd = sourceCard.getEnchanting().get(0);
                    
                    for(int i = 0; i < extrinsicKeywords.length; i ++) {
                    	if (! (extrinsicKeywords[i].equals ("none")) && (! crd.getKeyword().contains(extrinsicKeywords[i])))    // prevent Flying, Flying
                    		   crd.addExtrinsicKeyword(extrinsicKeywords[i]);
                    }
                    
                    crd.addSemiPermanentAttackBoost(Power);
                    crd.addSemiPermanentDefenseBoost(Tough);
                }
            }//execute()
        };//Command
        
        return onEnchant;
    }//enPump_onEnchant
    
    public static Command enPump_unEnchant(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, 
    		final String[] spellDescription, final String[] stackDescription) {
        
    	Command onUnEnchant = new Command() {
            
			private static final long serialVersionUID = -7121856650546173401L;

			public void execute() {
                if (sourceCard.isEnchanting()) {
                    Card crd = sourceCard.getEnchanting().get(0);
                    
                    for (int i = 0; i < extrinsicKeywords.length; i ++) {
                    	crd.removeExtrinsicKeyword(extrinsicKeywords[i]);
                    }
                    
                    crd.addSemiPermanentAttackBoost(-1 * Power);
                    crd.addSemiPermanentDefenseBoost(-1 * Tough);
                }
            }//execute()
        };//Command
        
        return onUnEnchant;
    }//enPump_unEnchant
    
    public static Command enPump_LeavesPlay(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, 
    		final String[] spellDescription, final String[] stackDescription) {
    	
    	Command onLeavesPlay = new Command() {
    		
			private static final long serialVersionUID = -924212760053167271L;

			public void execute() {
                if(sourceCard.isEnchanting()) {
                    Card crd = sourceCard.getEnchanting().get(0);
                    sourceCard.unEnchantCard(crd);
                }
            }//execute()
    	};//Command
    	
    	return onLeavesPlay;
    }//enPump_LeavesPlay

    public static Ability_Reflected_Mana getReflectedManaAbility(final Card card, String colorOrType, String who) {
    	
    	String whoString;
    	if (who.startsWith("Opp")) {
    		whoString = "an opponent controls";
    	} else
    		whoString = "you control";
    	
    	String abString = "tap: add to your mana pool one mana of any " + colorOrType.toLowerCase() +
    		" that a land " + whoString + " could produce.";

    	Ability_Reflected_Mana theAbility = new Ability_Reflected_Mana(card, abString, colorOrType, who);
 
    	return theAbility;
    	//((ReflectedManaInfo)theAbility.choices_made[0]).colorChosen = new String("0");
    	//((ReflectedManaInfo)theAbility.choices_made[0]).colorOrType = new String(colorOrType);
    	//((ReflectedManaInfo)theAbility.choices_made[0]).who = new String(who);
    } // End getReflectedManaAbility
    
    public static Ability getForbiddenOrchardAbility(final Card card, String player)
    {
    	final String opp = player;
    	final Ability ability = new Ability(card,"0")
    	{
    		public void resolve()
    		{
    			makeToken("Spirit", "C 1 1 Spirit", opp, "C", new String[] {
                        "Creature", "Spirit"}, 1, 1, new String[] {""});
    		}
    	};
    	ability.setStackDescription(card + " - put a 1/1 colorless Spirit creature token onto the battlefield under target opponent's control.");
    	
    	return ability;
    }
    
    public static SpellAbility enPumpCurse_Enchant(final Card sourceCard, final int Power, final int Tough, final String[] extrinsicKeywords, 
    		final String[] spellDescription, final String[] stackDescription) {
    	
        final SpellAbility enchant = new Spell(sourceCard) {
			private static final long serialVersionUID = -4021229901439299033L;

			/*
			 *  for flash, which is not working through the keyword for some reason
			 *  if not flash then limit to main 1 and 2 on controller's turn and card in hand
			 */
			@Override
            public boolean canPlay() {
                return (sourceCard.getKeyword().contains("Flash") && (AllZone.GameAction.isCardInZone(sourceCard, AllZone.Human_Hand) || 
                        AllZone.GameAction.isCardInZone(sourceCard, AllZone.Computer_Hand))
                            || 
                       (! sourceCard.getKeyword().contains("Flash") && (sourceCard.getController().equals(AllZone.Phase.getActivePlayer()) &&
                       (AllZone.GameAction.isCardInZone(sourceCard, AllZone.Human_Hand) || AllZone.GameAction.isCardInZone(sourceCard, AllZone.Computer_Hand)) && 
                       (AllZone.Phase.getPhase().equals(Constant.Phase.Main1) || AllZone.Phase.getPhase().equals(Constant.Phase.Main2)))));
            }
			
            public boolean canPlayAI() {
                CardList list = new CardList(AllZone.Human_Play.getCards());    // Target human creature
                list = list.getType("Creature");
                
                if (list.isEmpty()) return false;
                
                //else we may need to filter the list and remove inappropriate targets

                /* If extrinsicKeywords contains "CARDNAME can't attack." or "CARDNAME can't attack or block."
                 *     then remove creatures with Defender from the list and remove creatures that have one
                 *     or more of these keywords to start with
                 */
                final ArrayList<String> extKeywords = new ArrayList<String>(Arrays.asList(extrinsicKeywords));
                
                if (extKeywords.contains("CARDNAME can't attack.") || extKeywords.contains("CARDNAME can't attack or block.")) {
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                        	return c.isCreature() && !c.getKeyword().contains("Defender") && 
                        	!c.getKeyword().contains("CARDNAME can't attack.") && !c.getKeyword().contains("CARDNAME can't attack or block.");
                        }
                    });
                }
                
                /* If extrinsicKeywords contains "CARDNAME doesn't untap during your untap step."
                 *     then remove creatures with Vigilance from the list
                */
                if (extKeywords.contains("CARDNAME doesn't untap during your untap step.")) {
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c) {
                            return c.isCreature() && 
                                  (c.getKeyword().contains("Vigilance") && c.isTapped()) ||
                                (! c.getKeyword().contains("Vigilance") && 
                               ((! c.isTapped() && Power < 1) || c.isTapped()));
                	    }
                	});
                }
                                
                //else (if aura is keyword only or is Cagemail)
                
                if (Power >= 0 && Tough >= 0) {    // This aura is keyword only or is Cagemail
                    list = list.filter(new CardListFilter() {
                        public boolean addCard(Card c){
                            ArrayList<String> extKeywords = new ArrayList<String>(Arrays.asList(extrinsicKeywords));
                            for (String s:extKeywords) {
                                if (!c.getKeyword().contains(s))
                                    return true;
                            }
                                //no new keywords:
                                return false;
                        }
                    });
                    
                }
                
                //else aura is power/toughness decrease and may have keyword(s)
                
                CardListUtil.sortAttack(list);
                CardListUtil.sortFlying(list);
                
                for (int i = 0; i < list.size(); i++) {
                    if (CardFactoryUtil.canTarget(sourceCard, list.get(i))) {
                        setTargetCard(list.get(i));
                        return true;
                    }
                }
                return false;
            }//canPlayAI()

			public void resolve() {
                PlayerZone play = AllZone.getZone(Constant.Zone.Play, sourceCard.getController());
                play.add(sourceCard);
                
                Card c = getTargetCard();
                
                if(AllZone.GameAction.isCardInPlay(c) && CardFactoryUtil.canTarget(sourceCard, c)) {
                	sourceCard.enchantCard(c);
                    //System.out.println("Enchanted: " +getTargetCard());
                }
            }//resolve()
        };//enchant ability
        enchant.setBeforePayMana(CardFactoryUtil.input_targetCreature(enchant));
        enchant.setDescription(spellDescription[0]);
        enchant.setStackDescription(stackDescription[0]);
        
		return enchant;
    }//enPumpCurse_Enchant()
    
    public static Ability_Mana getEldraziSpawnAbility(final Card c)
    {
    	SpellAbility mana = new Ability_Mana(c, "Sacrifice CARDNAME: Add 1 to your mana pool.") {
			private static final long serialVersionUID = 2384540533244132975L;
		};
		
		return (Ability_Mana)mana;
    }
    
    
    public static Command entersBattleFieldWithCounters(final Card c, final Counters type, final int n) {
        Command addCounters = new Command() {
            private static final long serialVersionUID = 4825430555490333062L;
            
            public void execute() {
                c.addCounter(type, n);
            }
        };
        return addCounters;
    }
    
    public static Command vanishing(final Card sourceCard, final int Power) {
        Command age = new Command() {
            private static final long serialVersionUID = 431920157968451817L;
            public boolean            firstTime        = true;
            
            public void execute() {
                
                //testAndSet - only needed when comes into play.
                if(firstTime) {
                    sourceCard.addCounter(Counters.TIME, Power);
                }
                firstTime = false;
            }
        };
        return age;
    } // vanishing
    
    public static SpellAbility vanish_desc(final Card sourceCard, final int power) {
        final SpellAbility desc = new Ability_Hand(sourceCard, "0") {
            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public boolean canPlay() {
                return false;
            }
            
            @Override
            public void resolve() {}
        };
        // Be carefull changing this description cause it's crucial for ability to work (see GameActionUtil - vanishing for it)
        desc.setDescription("Vanishing "
                + power
                + " (This permanent enters the battlefield with "
                + power
                + " time counters on it. At the beginning of your upkeep, remove a time counter from it. When the last is removed, sacrifice it.)");
        return desc;
    }//vanish_desc()
    
    public static Command ability_Soulshift(final Card sourceCard, final String Manacost) {
        final Command Soulshift = new Command() {
            private static final long serialVersionUID = -4960704261761785512L;
            
            public void execute() {
                AllZone.Stack.add(soul_desc(sourceCard, Manacost));
            }
            
        };
        
        return Soulshift;
    }//ability_Soulshift()
    
    public static SpellAbility soul_desc(final Card sourceCard, final String Manacost) {
        final SpellAbility desc = new Ability_Hand(sourceCard, "0") {
            private static final long serialVersionUID = -4960704261761785512L;
            
            @Override
            public boolean canPlay() {
                return false;
            }
            
            @Override
            public void resolve() {
                PlayerZone lib = AllZone.getZone(Constant.Zone.Graveyard, sourceCard.getController());
                PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, sourceCard.getController());
                

                CardList cards = new CardList(lib.getCards());
                CardList sameCost = new CardList();
                int Cost = CardUtil.getConvertedManaCost(Manacost);
                for(int i = 0; i < cards.size(); i++) {
                    if((CardUtil.getConvertedManaCost(cards.get(i).getManaCost()) <= Cost)
                            && (cards.get(i).getType().contains("Spirit") || cards.get(i).getType().contains(
                                    "Changeling"))) {
                        sameCost.add(cards.get(i));
                    }
                }
                

                if(sameCost.size() == 0) return;
                
                if(sourceCard.getController().equals(Constant.Player.Human)) {
                    String[] choices = {"Yes", "No"};
                    Object choice = AllZone.Display.getChoice(sourceCard + " - Soulshift " + Cost + "?", choices);
                    if(choice.equals("Yes")) {
                        Object o = AllZone.Display.getChoiceOptional("Select a card", sameCost.toArray());
                        if(o != null) {
                            //ability.setTargetCard((Card)o);
                            //AllZone.Stack.add(ability);
                            
                            Card c1 = (Card) o;
                            lib.remove(c1);
                            hand.add(c1);
                            

                        }
                    }
                } else //Wiser choice should be here
                {
                    Card choice = null;
                    sameCost.shuffle();
                    choice = sameCost.getCard(0);
                    if(!(choice == null)) {
                        lib.remove(choice);
                        hand.add(choice);
                    }
                }
            }
        };
        desc.setDescription("Soulshift "
                + Manacost
                + " - When this permanent is put into a graveyard from play, you may return target Spirit card with converted mana cost "
                + Manacost + "or less from your graveyard to your hand.");
        desc.setStackDescription(sourceCard.getName() + " - Soulshift " + Manacost);
        return desc;
    }//soul_desc()
    
    public static Input input_targetValid(final SpellAbility sa, final String[] Tgts, final String message)
    {
    	return new Input() {
        private static final long serialVersionUID = -142142142142L;
        
        @Override
        public void showMessage() {
            CardList allCards = new CardList();
            allCards.addAll(AllZone.Human_Play.getCards());
            allCards.addAll(AllZone.Computer_Play.getCards());
            
            CardList choices = allCards.getValidCards(Tgts);
            boolean free = false;
            if(this.isFree()) free = true;
            stopSetNext(CardFactoryUtil.input_targetSpecific(sa, choices, message, true, free));
        }
       };
    }//input_targetValid
    //CardList choices are the only cards the user can successful select
    public static Input input_targetSpecific(final SpellAbility spell, final CardList choices, final String message, final boolean targeted, final boolean free) {
        return input_targetSpecific(spell, choices, message, Command.Blank, targeted, free);
    }
    
    //CardList choices are the only cards the user can successful select
    public static Input input_targetSpecific(final SpellAbility spell, final CardList choices, final String message, final Command paid, final boolean targeted, final boolean free) {
        Input target = new Input() {
            private static final long serialVersionUID = -1779224307654698954L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(targeted && !canTarget(spell, card)) {
                    AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                } else if(choices.contains(card)) {
                    spell.setTargetCard(card);
                    if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                            (Ability_Tap) spell));
                    else if(spell.getManaCost().equals("0") || free) {
                        this.setFree(false);
                        AllZone.Stack.add(spell);
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                    
                    paid.execute();
                }
            }//selectCard()
        };
        return target;
    }//input_targetSpecific()
    
  //CardList choices are the only cards the user can successful select
    public static Input input_targetChampionSac(final Card crd, final SpellAbility spell, final CardList choices, final String message, final boolean targeted, final boolean free) {
        Input target = new Input() {
			private static final long serialVersionUID = -3320425330743678663L;

			@Override
            public void showMessage() {
                AllZone.Display.showMessage(message);
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	AllZone.GameAction.sacrifice(crd);
            	stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(targeted && !canTarget(spell, card)) {
                    AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                } else if(choices.contains(card)) {
                    spell.setTargetCard(card);
                    if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                            (Ability_Tap) spell));
                    else if(spell.getManaCost().equals("0") || free) {
                        this.setFree(false);
                        AllZone.Stack.add(spell);
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                }
            }//selectCard()
        };
        return target;
    }//input_targetSpecific()
    
    public static Input input_equipCreature(final SpellAbility equip) {
        Input runtime = new Input() {	
			private static final long serialVersionUID = 2029801495067540196L;

			@Override
	        public void showMessage() {
	            //get all creatures you control
	            CardList list = new CardList();
	            list.addAll(AllZone.Human_Play.getCards());
	            list = list.getType("Creature");
	            
	            stopSetNext(input_targetSpecific(equip, list,
	                    "Select target creature to equip", true, false));
	        }
	    };//Input
	    return runtime;
    }
    
    public static Input input_discard(final SpellAbility spell, final int nCards) {
        Input target = new Input() {
            private static final long serialVersionUID = 5101772642421944050L;
            
            int                       n                = 0;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select a card to discard");
                ButtonUtil.disableAll();
                
                if(n == nCards || AllZone.Human_Hand.getCards().length == 0) stop();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand)) {
                    AllZone.GameAction.discard(card);
                    n++;
                    if(spell.getManaCost().equals("0")) {
                        AllZone.Stack.add(spell);
                        stop();
                    } else stopSetNext(new Input_PayManaCost(spell));
                    
                    //showMessage();
                } // if
                

            }//selectCard
            
        };
        return target;
        
    }
    
    public static Input input_discard() {
        return input_discard(1);
    }
    
    public static Input input_discard(final int nCards) {
        Input target = new Input() {
            private static final long serialVersionUID = -329993322080934435L;
            
            int                       n                = 0;
            
            @Override
            public void showMessage() {
            	if (AllZone.Human_Hand.getCards().length == 0) stop();
            	
                AllZone.Display.showMessage("Select a card to discard");
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand)) {
                    AllZone.GameAction.discard(card);
                    n++;
                    
                    //in case no more cards in hand
                    if(n == nCards || AllZone.Human_Hand.getCards().length == 0) stop();
                    else
                    	showMessage();
                }
            }
        };
        return target;
    }//input_discard()
    
    /**
     * custom input method only for use in Recall
     * 
     * @param numCards
     * @param recall
     * @return
     */
    public static Input input_discardRecall(final int numCards, final Card recall) {
        Input target = new Input() {
			private static final long serialVersionUID = 1942999595292561944L;
			int n = 0;
            
            @Override
            public void showMessage() {
            	if (AllZone.Human_Hand.getCards().length == 0) stop();
            	
                AllZone.Display.showMessage("Select a card to discard");
                ButtonUtil.disableAll();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(zone.is(Constant.Zone.Hand)) {
                    AllZone.GameAction.discard(card);
                    n++;
                    
                    //in case no more cards in hand
                    if(n == numCards || AllZone.Human_Hand.getCards().length == 0) done();
                    else
                    	showMessage();
                }
            }
            
            void done() {
            	AllZone.Display.showMessage("Returning cards to hand.");
            	AllZone.GameAction.exile(recall);
            	CardList grave = AllZoneUtil.getPlayerGraveyard(Constant.Player.Human);
            	for(int i = 1; i <= n; i++) {
            		String title = "Return card from grave to hand";
            		Object o = AllZone.Display.getChoice(title, grave.toArray());
            		if(o == null) break;
            		Card toHand = (Card) o;
            		grave.remove(toHand);
            		AllZone.GameAction.moveToHand(toHand);
            	}
            	stop();
            }
        };
        return target;
    }//input_discardRecall()
    

    /*
    //cardType is like "Creature", "Land", "Artifact", "Goblin", "Legendary"
    //cardType can also be "All", which will allow any permanent to be selected
    public static Input input_targetType(final SpellAbility spell, final String cardType) {
        Input target = new Input() {
            private static final long serialVersionUID = 4944828318048780429L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target " + cardType);
                
                if(cardType.equals("All")) AllZone.Display.showMessage("Select target permanent");
                
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(((card.getType().contains(cardType) || card.getKeyword().contains("Changeling")) || cardType.equals("All"))
                        && zone.is(Constant.Zone.Play) && canTarget(spell, card)) {
                    spell.setTargetCard(card);
                    stopSetNext(new Input_PayManaCost(spell));
                }
            }
        };
        return target;
    }//input_targetType()
    */
    
    //****************copied from input_targetType*****************
    //cardType is like "Creature", "Land", "Artifact", "Goblin", "Legendary", ";"-delimited
    //cardType can also be "All", which will allow any permanent to be selected
    public static Input input_targetType(final SpellAbility spell, final String cardTypeList) {
       Input target = new Input() {
		private static final long serialVersionUID = 6443658187985259709L;
		public void showMessage() {
             StringTokenizer st = new StringTokenizer(cardTypeList, ";");
             if(cardTypeList.equals("All")) {
                AllZone.Display.showMessage("Select target permanent");
             }
             else {
                StringBuffer toDisplay = new StringBuffer();
                toDisplay.append("Select target ");
                while( st.hasMoreTokens() ) {
                   toDisplay.append(st.nextToken());
                   if( st.hasMoreTokens() ) {
                      toDisplay.append(" or ");
                   }
                }
                AllZone.Display.showMessage( toDisplay.toString() );
             }
             ButtonUtil.enableOnlyCancel();
          }
          public void selectButtonCancel() {stop();}
          public void selectCard(Card card, PlayerZone zone) {
             boolean foundCardType = false;
             StringTokenizer st = new StringTokenizer(cardTypeList, ";");
             if( cardTypeList.equals("All") ) {
                foundCardType = true;
             } else {
                while( st.hasMoreTokens() ) {
                   if( card.getType().contains( st.nextToken() )) {
                      foundCardType = true;
                   }
                }
             }
             if( foundCardType && zone.is(Constant.Zone.Play)) {
                spell.setTargetCard(card);
                if(spell.getManaCost().equals("0") || this.isFree())//for "sacrifice this card" abilities
                {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                }
                else
                	stopSetNext(new Input_PayManaCost(spell));
             }
          }
       };
       return target;
    }//input_targetType()
    //***************end copy******************


    public static Input input_targetCreature(final SpellAbility spell) {
        return input_targetCreature(spell, Command.Blank);
    }
    
    public static Input input_targetCreature(final SpellAbility spell, final Command paid) {
        Input target = new Input() {
            private static final long serialVersionUID = 141164423096887945L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target creature for " + spell.getSourceCard());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(!canTarget(spell, card)) {
                    AllZone.Display.showMessage("Cannot target this card (Shroud? Protection?).");
                } else if(card.isCreature() && zone.is(Constant.Zone.Play)) {
                    spell.setTargetCard(card);
                    done();
                }
            }
            
            void done() {
                if(spell instanceof Ability_Tap && spell.getManaCost().equals("0")) stopSetNext(new Input_NoCost_TapAbility(
                        (Ability_Tap) spell));
                else if(spell.getManaCost().equals("0") || this.isFree())//for "sacrifice this card" abilities
                {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
                
                paid.execute();
            }
        };
        return target;
    }//input_targetCreature()
    
    public static Input input_targetCreature_NoCost_TapAbility(final Ability_Tap spell) {
        Input target = new Input() {
            private static final long serialVersionUID = 6027194502614341779L;
            
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
            public void selectCard(Card card, PlayerZone zone) {
                if(card.isCreature() && zone.is(Constant.Zone.Play) && canTarget(spell, card)) {
                    spell.setTargetCard(card);
                    spell.getSourceCard().tap();
                    AllZone.Stack.push(spell);
                    stop();
                }
            }
        };
        return target;
    }//input_targetCreature()
    
    public static Input MasteroftheWildHunt_input_targetCreature(final SpellAbility spell, final CardList choices, final Command paid) {
        Input target = new Input() {
            private static final long serialVersionUID = -1779224307654698954L;
            
            @Override
            public void showMessage() {
            	AllZone.Display.showMessage("Select target wolf to damage for " + spell.getSourceCard());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
            	if(choices.size() == 0) stop();
            		if(choices.contains(card)) {
                    spell.setTargetCard(card);
                    paid.execute();
                        stop();                  
                }
            }//selectCard()
        };
        return target;
    }//input_MasteroftheWildHunt_input_targetCreature()
    
    public static Input input_MultitargetCreatureOrPlayer(final SpellAbility spell, final int i, final int i_max,final Command paid) {
        Input target = new Input() {
        	
            private static final long serialVersionUID = -1779224307654698954L;
            
            @Override
            public void showMessage() {
            	if(GameAction.MultiTarget_Cancelled == true) stop();
            	if(i == 0) AllZone.Display.showMessage("Select target creature or player: " + (i_max) + " more damage to deal");
            	else AllZone.Display.showMessage("Select target creature or player: " + (i) + " more damage to deal");
            }
            
            @Override
            public void selectButtonCancel() {
            	GameAction.MultiTarget_Cancelled = true;
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if((card.isCreature() || card.isPlaneswalker()) && zone.is(Constant.Zone.Play)
                        && (canTarget(spell, card))) {
                    spell.setTargetCard(card);
                    paid.execute();
                    stop();  
                }
            }//selectCard()
            
            @Override
            public void selectPlayer(String player) {
                spell.setTargetPlayer(player);
                paid.execute();
                stop();  
            }

        };
        return target;
    }//input_MultitargetCreatureOrPlayer()
    
    public static Input Lorthos_input_targetPermanent(final SpellAbility spell, final CardList choices, final int i, final Command paid) {
        Input target = new Input() {
        	
            private static final long serialVersionUID = -1779224307654698954L;
            
            @Override
            public void showMessage() {
            	if(CombatUtil.Lorthos_Cancelled == true) stop();
            	AllZone.Display.showMessage("Select target Permanents for Lorthos: " + (i + 1) + " more to pick");
            	ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
            	CombatUtil.Lorthos_Cancelled = true;
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
            	if(choices.size() == 0) stop();
                if(card.isPermanent() && zone.is(Constant.Zone.Play) && canTarget(spell, card) && choices.contains(card)) {
                    spell.setTargetCard(card);
                    paid.execute();
                        stop();                  
                }
            }//selectCard()
        };
        return target;
    }//input_Lorthos_input_targetPermanent()
    
    public static Input input_targetCreature_NoCost_TapAbility_NoTargetSelf(final Ability_Tap spell) {
        Input target = new Input() {
            private static final long serialVersionUID = -6310420275914649718L;
            
            @Override
            public void showMessage() {
                AllZone.Display.showMessage("Select target creature other than " + spell.getSourceCard().getName());
                ButtonUtil.enableOnlyCancel();
            }
            
            @Override
            public void selectButtonCancel() {
                stop();
            }
            
            @Override
            public void selectCard(Card card, PlayerZone zone) {
                if(card == spell.getSourceCard()) {
                    AllZone.Display.showMessage("You must select a target creature other than "
                            + spell.getSourceCard().getName());
                } else if(card.isCreature() && zone.is(Constant.Zone.Play)
                        && !card.getKeyword().contains("Shroud")) {
                    spell.setTargetCard(card);
                    spell.getSourceCard().tap();
                    AllZone.Stack.push(spell);
                    stop();
                }
            }
        };
        return target;
    }//input_targetCreature_NoCost_TapAbility_NoTargetSelf
    

    public static Input input_targetPlayer(final SpellAbility spell) {
        Input target = new Input() {
            private static final long serialVersionUID = 8736682807625129068L;
            
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
                spell.setTargetPlayer(player);
                if(spell.getManaCost().equals("0") || this.isFree()) {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    if(spell.isTapAbility()) spell.getSourceCard().tap();
                    if(spell.isUntapAbility()) spell.getSourceCard().untap();
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetPlayer()
    
    public static Input input_targetPlayer(final SpellAbility spell, final Command command) {
        Input target = new Input() {
            private static final long serialVersionUID = 8736682807625129068L;
            
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
                command.execute();
                
                spell.setTargetPlayer(player);
                if(spell.getManaCost().equals("0") || this.isFree()) {
                    this.setFree(false);
                    AllZone.Stack.add(spell, spell.getSourceCard().getManaCost().contains("X"));
                    stop();
                } else stopSetNext(new Input_PayManaCost(spell));
            }
        };
        return target;
    }//input_targetPlayer()
    
    public static CardList AI_getHumanCreature(final Card spell, boolean targeted) {
        CardList creature = new CardList(AllZone.Human_Play.getCards());
        creature = creature.getType("Creature");
        if(targeted) {
            creature = creature.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return canTarget(spell, c);
                }
            });
        }
        return creature;
    }
    
    public static CardList AI_getHumanCreature(final String keyword, final Card spell, final boolean targeted) {
        CardList creature = new CardList(AllZone.Human_Play.getCards());
        creature = creature.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                if(targeted) return c.isCreature() && c.getKeyword().contains(keyword) && canTarget(spell, c);
                else return c.isCreature() && c.getKeyword().contains(keyword);
            }
        });
        return creature;
    }//AI_getHumanCreature()
    
    public static CardList AI_getHumanCreature(final int toughness, final Card spell, final boolean targeted) {
        CardList creature = new CardList(AllZone.Human_Play.getCards());
        creature = creature.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                if(targeted) return c.isCreature() && (c.getNetDefense() <= toughness) && canTarget(spell, c);
                else return c.isCreature() && (c.getNetDefense() <= toughness);
            }
        });
        return creature;
    }//AI_getHumanCreature()
    
    public static CardList AI_getHumanCreature(final boolean lower, final int manaCost, final Card spell, final boolean targeted) {
        CardList creature = new CardList(AllZone.Human_Play.getCards());
        creature = creature.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                if(targeted && lower) return c.isCreature()
                        && (CardUtil.getConvertedManaCost(c.getManaCost()) <= 3) && canTarget(spell, c);
                else if(lower) return c.isCreature() && (CardUtil.getConvertedManaCost(c.getManaCost()) <= 3);
                
                else if(targeted && !lower) return c.isCreature()
                        && (CardUtil.getConvertedManaCost(c.getManaCost()) >= 3) && canTarget(spell, c);
                else //if !targeted && !lower
                return c.isCreature() && (CardUtil.getConvertedManaCost(c.getManaCost()) >= 3);
            }
        });
        return creature;
    }//AI_getHumanCreature()
    
    public static CommandArgs AI_targetHumanCreatureOrPlayer() {
        return new CommandArgs() {
            private static final long serialVersionUID = 1530080942899792553L;
            
            public void execute(Object o) {
                SpellAbility sa = (SpellAbility) o;
                
                CardList creature = new CardList(AllZone.Human_Play.getCards());
                creature = creature.getType("Creature");
                Card c = getRandomCard(creature);
                
                if((c == null) || random.nextBoolean()) {
                    sa.setTargetPlayer(Constant.Player.Human);
                } else {
                    sa.setTargetCard(c);
                }
            }
        };//CommandArgs
    }//human_creatureOrPlayer()
    
    public static CommandArgs AI_targetHuman() {
        return new CommandArgs() {
            private static final long serialVersionUID = 8406907523134006697L;
            
            public void execute(Object o) {
                SpellAbility sa = (SpellAbility) o;
                sa.setTargetPlayer(Constant.Player.Human);
            }
        };
    }//targetHuman()
    
    //is it the computer's main phase before attacking?
    public static boolean AI_isMainPhase() {
        return AllZone.Phase.getPhase().equals(Constant.Phase.Main1)
                && AllZone.Phase.getActivePlayer().equals(Constant.Player.Computer);
    }
    
    public static CommandArgs AI_targetComputer() {
        return new CommandArgs() {
            private static final long serialVersionUID = -445231553588926627L;
            
            public void execute(Object o) {
                SpellAbility sa = (SpellAbility) o;
                sa.setTargetPlayer(Constant.Player.Computer);
            }
        };
    }//targetComputer()
    
    //type can also be "All"
    public static CommandArgs AI_targetType(final String type, final PlayerZone zone) {
        return new CommandArgs() {
            private static final long serialVersionUID = 6475810798098105603L;
            
            public void execute(Object o) {
                CardList filter = new CardList(zone.getCards());
                
                if(!type.equals("All")) filter = filter.getType(type);
                
                Card c = getRandomCard(filter);
                if(c != null) {
                    SpellAbility sa = (SpellAbility) o;
                    sa.setTargetCard(c);
                    
                    //doesn't work for some reason
//          if(shouldAttack && CombatUtil.canAttack(c))
//            AllZone.Combat.addAttacker(c);
                }
            }//execute()
        };
    }//targetInPlay()
    
    public static int getNumberOfPermanentsByColor(String color) {
        CardList cards = new CardList();
        cards.addAll(AllZone.Human_Play.getCards());
        cards.addAll(AllZone.Computer_Play.getCards());
        
        CardList coloredPerms = new CardList();
        
        for(int i = 0; i < cards.size(); i++) {
            if(CardUtil.getColors(cards.get(i)).contains(color)) coloredPerms.add(cards.get(i));
        }
        return coloredPerms.size();
    }
    
    public static boolean multipleControlled(Card c) {
        PlayerZone play = AllZone.getZone(c);
        CardList list = new CardList(play.getCards());
        list.remove(c);
        
        return list.containsName(c.getName());
    }
    
    public static boolean controlsAnotherMulticoloredPermanent(Card c) {
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
        
        final Card crd = c;
        
        CardList list = new CardList(play.getCards());
        list = list.filter(new CardListFilter() {
            
            public boolean addCard(Card c) {
                return !c.equals(crd) && CardUtil.getColors(c).size() >= 2;
            }
            
        });
        
        return list.size() >= 1;
        
    }
    
    public static boolean controlsAnotherColoredCreature(Card c, String color) {
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
        
        final Card crd = c;
        final String col = color;
        CardList list = new CardList(play.getCards());
        list = list.filter(new CardListFilter() {
            
            public boolean addCard(Card c) {
                return !c.equals(crd) && c.isCreature() && CardUtil.getColors(c).contains(col);
            }
            
        });
        
        return list.size() >= 1;
        
    }
    
    public static boolean oppHasKismet(String player) {
        String opp = AllZone.GameAction.getOpponent(player);
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, opp);
        CardList list = new CardList(play.getCards());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getName().equals("Kismet") || c.getName().equals("Frozen AEther")
                        || c.getName().equals("Loxodon Gatekeeper");
            }
        });
        return list.size() > 0;
    }
    
    public static int getNumberOfManaSymbolsControlledByColor(String colorAbb, String player) {
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
        
        CardList cards = new CardList();
        cards.addAll(play.getCards());
        
        int count = 0;
        for(int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if(!c.isToken()) {
                String manaCost = c.getManaCost();
                manaCost = manaCost.trim();
                count += countOccurrences(manaCost, colorAbb);
            }
        }
        return count;
    }
    
    public static String sumManaCost(String manacost1, String manacost2)
    {
    	String tokenized1[] = manacost1.split("\\s");
    	String tokenized2[] = manacost2.split("\\s");
    	
    	StringBuilder sb = new StringBuilder();
    	
    	int totalNumberCost = 0;
    	if (Character.isDigit(tokenized1[0].charAt(0)) && Character.isDigit(tokenized2[0].charAt(0)))
    	{
    		int cost1 = Integer.parseInt(tokenized1[0]);
    		int cost2 = Integer.parseInt(tokenized2[0]);
    		totalNumberCost = cost1 + cost2;
    	}
    	else if (Character.isDigit(tokenized1[0].charAt(0)))
    		totalNumberCost = Integer.parseInt(tokenized1[0]);
    	else if (Character.isDigit(tokenized2[0].charAt(0)))
    	    totalNumberCost = Integer.parseInt(tokenized2[0]);
    	
    	if (totalNumberCost != 0) {
    		sb.append(totalNumberCost);
    	}
    	
    	for (int i=1;i<tokenized1.length;i++)
    	{
    		sb.append(" ");
    		sb.append(tokenized1[i]);
    	}
    	
    	for (int i=1;i<tokenized2.length;i++)
    	{
    		sb.append(" ");
    		sb.append(tokenized2[i]);
    	}
    	
    	//TODO: resort mana symbol order?
    	return sb.toString().trim();
    }
    
    public static String multiplyManaCost(String manacost, int multiplier) {
        if(multiplier == 0) return "";
        if(multiplier == 1) return manacost;
        
        String tokenized[] = manacost.split("\\s");
        StringBuilder sb = new StringBuilder();
        
        if(Character.isDigit(tokenized[0].charAt(0))) //manacost starts with "colorless" number cost
        {
            int cost = Integer.parseInt(tokenized[0]);
            cost = multiplier * cost;
            tokenized[0] = "" + cost;
            sb.append(tokenized[0]);
        } else {
            for(int i = 0; i < multiplier; i++) {
                //tokenized[0] = tokenized[0] + " " + tokenized[0];
                sb.append((" "));
                sb.append(tokenized[0]);
            }
        }
        
        for(int i = 1; i < tokenized.length; i++) {
            for(int j = 0; j < multiplier; j++) {
                //tokenized[i] = tokenized[i] + " " + tokenized[i];
                sb.append((" "));
                sb.append(tokenized[i]);
                
            }
        }
        
        String result = sb.toString();
        System.out.println("result: " + result);
        result = result.trim();
        return result;
    }
    
    public static boolean canTarget(SpellAbility ability, Card target) {
        return canTarget(ability.getSourceCard(), target);
    }
    
    public static boolean isColored(Card c)
    {
    	return CardUtil.getColors(c).contains(Constant.Color.White) || CardUtil.getColors(c).contains(Constant.Color.Blue) ||
    		   CardUtil.getColors(c).contains(Constant.Color.Black) || CardUtil.getColors(c).contains(Constant.Color.Red) ||
    		   CardUtil.getColors(c).contains(Constant.Color.Green);
    }
    
    public static boolean canTarget(Card spell, Card target) {
        if(target == null) return true;
        //System.out.println("Target:" + target);
        if(target.getKeyword() != null) {
            ArrayList<String> list = target.getKeyword();
            
            String kw = "";
            for(int i = 0; i < list.size(); i++) {
                kw = list.get(i);
                if(kw.equals("Shroud")) return false;
                
                if(kw.equals("CARDNAME can't be the target of spells or abilities your opponents control.")) {
                    if(!spell.getController().equals(target.getController())) return false;
                }
                
                if(kw.equals("CARDNAME can't be the target of Aura spells.")) {
                    if(spell.isAura()) return false;
                }
                
                if(kw.equals("Protection from white") && CardUtil.getColors(spell).contains(Constant.Color.White)) return false;
                if(kw.equals("Protection from blue") && CardUtil.getColors(spell).contains(Constant.Color.Blue)) return false;
                if(kw.equals("Protection from black") && CardUtil.getColors(spell).contains(Constant.Color.Black)) return false;
                if(kw.equals("Protection from red") && CardUtil.getColors(spell).contains(Constant.Color.Red)) return false;
                if(kw.equals("Protection from green") && CardUtil.getColors(spell).contains(Constant.Color.Green)) return false;
                
                if(kw.equals("Protection from creatures") && spell.isCreature()) return false;
                
                if(kw.equals("Protection from artifacts") && spell.isArtifact()) return false;
                
                if(kw.equals("Protection from Dragons")
                        && (spell.getType().contains("Dragon") || spell.getKeyword().contains("Changeling"))) return false;
                if(kw.equals("Protection from Demons")
                        && (spell.getType().contains("Demon") || spell.getKeyword().contains("Changeling"))) return false;
                if(kw.equals("Protection from Goblins")
                        && (spell.getType().contains("Goblin") || spell.getKeyword().contains("Changeling"))) return false;
                if(kw.equals("Protection from Clerics")
                        && (spell.getType().contains("Cleric") || spell.getKeyword().contains("Changeling"))) return false;
                
                if(kw.equals("Protection from enchantments") && spell.getType().contains("Enchantment")) return false;
                
                if(kw.equals("Protection from everything")) return false;
                
                if(kw.equals("Protection from colored spells") && (spell.isInstant() || spell.isSorcery() || spell.isAura() ) &&
                   isColored(spell)) return false;
            }
        }
        return true;
    }
    
    //does "target" have protection from "card"?
    public static boolean hasProtectionFrom(Card card, Card target) {
        if(target == null) return false;
        
        if(target.getKeyword() != null) {
            ArrayList<String> list = target.getKeyword();
            
            String kw = "";
            for(int i = 0; i < list.size(); i++) {
                kw = list.get(i);
                

                if(kw.equals("Protection from white") && CardUtil.getColors(card).contains(Constant.Color.White) && 
                		!card.getName().contains("White Ward")) return true;
                if(kw.equals("Protection from blue") && CardUtil.getColors(card).contains(Constant.Color.Blue) && 
                		!card.getName().contains("Blue Ward")) return true;
                if(kw.equals("Protection from black") && CardUtil.getColors(card).contains(Constant.Color.Black) && 
                		!card.getName().contains("Black Ward")) return true;
                if(kw.equals("Protection from red") && CardUtil.getColors(card).contains(Constant.Color.Red) && 
                		!card.getName().contains("Red Ward")) return true;
                if(kw.equals("Protection from green") && CardUtil.getColors(card).contains(Constant.Color.Green) && 
                		!card.getName().contains("Green Ward")) return true;
                
                if(kw.equals("Protection from creatures") && card.isCreature()) return true;
                
                if(kw.equals("Protection from artifacts") && card.isArtifact() && 
                		!card.getName().contains("Artifact Ward")) return true;
                
                if(kw.equals("Protection from everything")) return true;
                
                if(kw.equals("Protection from Dragons")
                        && (card.getType().contains("Dragon") || card.getKeyword().contains("Changeling"))) return true;
                if(kw.equals("Protection from Demons")
                        && (card.getType().contains("Demon") || card.getKeyword().contains("Changeling"))) return true;
                if(kw.equals("Protection from Goblins")
                        && (card.getType().contains("Goblin") || card.getKeyword().contains("Changeling"))) return true;
                if(kw.equals("Protection from Clerics")
                        && (card.getType().contains("Cleric") || card.getKeyword().contains("Changeling"))) return false;
                
                if(kw.equals("Protection from enchantments") && card.getType().contains("Enchantment") && 
                		!card.getName().contains("Tattoo Ward")) return true;
            }
        }
        return false;
    }
    
    public static boolean canDamage(Card spell, Card receiver) {
        //this is for untargeted damage spells, such as Pestilence, Pyroclasm, Tremor, etc. 
        //and also combat damage?
        ArrayList<String> list = receiver.getKeyword();
        
        String kw = "";
        for(int i = 0; i < list.size(); i++) {
            kw = list.get(i);
            
            if(kw.equals("Prevent all damage that would be dealt to CARDNAME by artifact creatures.") 
            		&& spell.isCreature() && spell.isArtifact()) return false;
            if(kw.equals("Protection from white") && CardUtil.getColors(spell).contains(Constant.Color.White)) return false;
            if(kw.equals("Protection from blue") && CardUtil.getColors(spell).contains(Constant.Color.Blue)) return false;
            if(kw.equals("Protection from black") && CardUtil.getColors(spell).contains(Constant.Color.Black)) return false;
            if(kw.equals("Protection from red") && CardUtil.getColors(spell).contains(Constant.Color.Red)) return false;
            if(kw.equals("Protection from green") && CardUtil.getColors(spell).contains(Constant.Color.Green)) return false;
            
            if(kw.equals("Protection from creatures") && spell.isCreature()) return false;
            
            if(kw.equals("Protection from artifacts") && spell.isArtifact()) return false;
            
            if(kw.equals("Protection from Dragons")
                    && (spell.getType().contains("Dragon") || spell.getKeyword().contains("Changeling"))) return false;
            if(kw.equals("Protection from Demons")
                    && (spell.getType().contains("Demon") || spell.getKeyword().contains("Changeling"))) return false;
            if(kw.equals("Protection from Goblins")
                    && (spell.getType().contains("Goblin") || spell.getKeyword().contains("Changeling"))) return false;
            if(kw.equals("Protection from Clerics")
                    && (spell.getType().contains("Cleric") || spell.getKeyword().contains("Changeling"))) return false;
            
            if(kw.equals("Protection from enchantments") && spell.getType().contains("Enchantment")) return false;
            
            if(kw.equals("Protection from everything")) return false;
            
            if(kw.equals("Protection from colored spells") && !spell.isPermanent() && isColored(spell) ) return false;
        }
        
        return true;
        
    }
    
    public static boolean isCounterable(Card c) {
        if(!c.getKeyword().contains("CARDNAME can't be countered.")) return true;
        else return false;
    }
    
    //returns the number of enchantments named "e" card c is enchanted by
    public static int hasNumberEnchantments(Card c, String e) {
        if(!c.isEnchanted()) return 0;
        
        final String enchantmentName = e;
        CardList list = new CardList(c.getEnchantedBy().toArray());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getName().equals(enchantmentName);
            }
            
        });
        
        return list.size();
        
    }
    
    //returns a CardList of all auras named e enchanting Card c
    public static CardList getAurasEnchanting(Card c, String e) {
        CardList list = new CardList();
        if(!c.isEnchanted()) return list;
        
        final String enchantmentName = e;
        CardList cl = new CardList(c.getEnchantedBy().toArray());
        cl = cl.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getName().equals(enchantmentName);
            }
            
        });
        
        return cl;
        
    }
    
    //returns the number of equipments named "e" card c is equipped by
    public static int hasNumberEquipments(Card c, String e) {
        if(!c.isEquipped()) return 0;
        
        final String equipmentName = e;
        CardList list = new CardList(c.getEquippedBy().toArray());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getName().equals(equipmentName);
            }
            
        });
        
        return list.size();
        
    }
    
    /*public static CardList getValuableCreatures() 
    {
      
    }
    */

    public static CardList getFlashbackCards(String player) {
        PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
        CardList cl = new CardList(grave.getCards());
        cl = cl.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.hasFlashback();
            }
        });
        return cl;
    }

    public static CardList getFlashbackUnearthCards(String player) {
    	final CardList crucible = AllZoneUtil.getPlayerCardsInPlay(player, "Crucible of Worlds");
    	
        PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
        CardList cl = new CardList(grave.getCards());
        cl = cl.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.hasFlashback() || c.hasUnearth() || (c.isLand() && crucible.size() > 0);
            }
        });
        return cl;
    }
    
    public static int countOccurrences(String arg1, String arg2) {
        
        int count = 0;
        int index = 0;
        while((index = arg1.indexOf(arg2, index)) != -1) {
            ++index;
            ++count;
        }
        return count;
    }
    
    //parser for non-mana X variables
    public static int xCount(Card c, String s) {
        int n = 0;
        
        String cardController = c.getController();
        String oppController = AllZone.GameAction.getOpponent(cardController);
        
        PlayerZone myField = AllZone.getZone(Constant.Zone.Play, cardController);
        PlayerZone opField = AllZone.getZone(Constant.Zone.Play, oppController);
        
        PlayerZone myYard = AllZone.getZone(Constant.Zone.Graveyard, cardController);
        PlayerZone opYard = AllZone.getZone(Constant.Zone.Graveyard, oppController);
        
        PlayerZone myHand = AllZone.getZone(Constant.Zone.Hand, cardController);
        PlayerZone opHand = AllZone.getZone(Constant.Zone.Hand, oppController);
        
        final String[] l;
        l = s.split("/"); // separate the specification from any math
        final String m[] = {"none"};
        if(l.length > 1) m[0] = l[1];
        final String[] sq;
        sq = l[0].split("\\.");
        
        if(sq[0].contains("xPaid")) {
        	if (c.getController().equals(Constant.Player.Human)) {
        		return c.getXManaCostPaid();
        	}
        	else {
        		int dam = ComputerUtil.getAvailableMana().size()- CardUtil.getConvertedManaCost(c);
        		if (dam < 0) dam = 0;
        		return dam;
        	}
        }
        
        CardList someCards = new CardList();
        
        //Complex counting methods
        
        
        
        // Count$Domain
        if(sq[0].contains("Domain")) {
            someCards.addAll(myField.getCards());
            String basic[] = {"Forest", "Plains", "Mountain", "Island", "Swamp"};
            
            for(int i = 0; i < basic.length; i++)
                if(!someCards.getType(basic[i]).isEmpty()) n++;
            
            return doXMath(n, m);
        }
        
        // Count$YourLifeTotal
        if(sq[0].contains("YourLifeTotal")) {
            if(cardController.equals(Constant.Player.Computer)) return doXMath(AllZone.Computer_Life.getLife(), m);
            else if(cardController.equals(Constant.Player.Human)) return doXMath(AllZone.Human_Life.getLife(), m);
            
            return 0;
        }
        
        // Count$OppLifeTotal
        if(sq[0].contains("OppLifeTotal")) {
            if(oppController.equals(Constant.Player.Computer)) return doXMath(AllZone.Computer_Life.getLife(), m);
            else if(oppController.equals(Constant.Player.Human)) return doXMath(AllZone.Human_Life.getLife(), m);
            
            return 0;
        }
        
        // Count$Chroma.<mana letter>
        if(sq[0].contains("Chroma")) return doXMath(
                getNumberOfManaSymbolsControlledByColor(sq[1], cardController), m);
        
        // Count$Hellbent.<numHB>.<numNotHB>
        if(sq[0].contains("Hellbent")) if(myHand.size() <= 1) return doXMath(Integer.parseInt(sq[1]), m); // Hellbent
        else return doXMath(Integer.parseInt(sq[2]), m); // not Hellbent
        
        // Count$CardPower
        if(sq[0].contains("CardPower")) return doXMath(c.getNetAttack(), m);
        // Count$CardToughness
        if(sq[0].contains("CardToughness")) return doXMath(c.getNetDefense(), m);
        // Count$CardManaCost
        if(sq[0].contains("CardManaCost")) return doXMath(CardUtil.getConvertedManaCost(c), m);
        
        //Count$IfMainPhase.<numMain>.<numNotMain> // 7/10
        if (sq[0].contains("IfMainPhase"))
        {
        	String cPhase = AllZone.Phase.getPhase();
        	if ((cPhase.equals(Constant.Phase.Main1) ||
        		 cPhase.equals(Constant.Phase.Main2)) && 
        		 AllZone.Phase.getActivePlayer().equals(cardController))
        		return doXMath(Integer.parseInt(sq[1]), m);
        	else
        		return doXMath(Integer.parseInt(sq[2]), m); // not Main Phase
        }
        
        //Generic Zone-based counting
        // Count$QualityAndZones.Subquality
        
        // build a list of cards in each possible specified zone
        
        // if a card was ever written to count two different zones,
        // make sure they don't get added twice.
        boolean MF = false, MY = false, MH = false;
        boolean OF = false, OY = false, OH = false;
        
        if(sq[0].contains("YouCtrl")) if(MF == false) {
            someCards.addAll(myField.getCards());
            MF = true;
        }
        
        if(sq[0].contains("InYourYard")) if(MY == false) {
            someCards.addAll(myYard.getCards());
            MY = true;
        }
        
        if(sq[0].contains("InYourHand")) if(MH == false) {
            someCards.addAll(myHand.getCards());
            MH = true;
        }
        
        if(sq[0].contains("OppCtrl")) if(OF == false) {
            someCards.addAll(opField.getCards());
            OF = true;
        }
        
        if(sq[0].contains("InOppYard")) if(OY == false) {
            someCards.addAll(opYard.getCards());
            OY = true;
        }
        
        if(sq[0].contains("InOppHand")) if(OH == false) {
            someCards.addAll(opHand.getCards());
            OH = true;
        }
        
        if(sq[0].contains("OnBattlefield")) {
            if(MF == false) someCards.addAll(myField.getCards());
            if(OF == false) someCards.addAll(opField.getCards());
        }
        
        if(sq[0].contains("InAllYards")) {
            if(MY == false) someCards.addAll(myYard.getCards());
            if(OY == false) someCards.addAll(opYard.getCards());
        }
        
        if(sq[0].contains("InAllHands")) {
            if(MH == false) someCards.addAll(myHand.getCards());
            if(OH == false) someCards.addAll(opHand.getCards());
        }
        
        // filter lists based on the specified quality
        
        // "Clerics you control" - Count$TypeYouCtrl.Cleric
        if(sq[0].contains("Type")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    if(c.getType().contains(sq[1]) || c.getKeyword().contains("Changeling")) return true;
                    
                    return false;
                }
            });
        }
        
        // "Named <CARDNAME> in all graveyards" - Count$NamedAllYards.<CARDNAME>
        
        if(sq[0].contains("Named")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    if(c.getName().equals(sq[1])) return true;
                    
                    return false;
                }
            });
        }
        
        // Refined qualities
        
        // "Untapped Lands" - Count$UntappedTypeYouCtrl.Land
        if(sq[0].contains("Untapped")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return !c.isTapped();
                }
            });
        }
        
        if(sq[0].contains("Tapped")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return c.isTapped();
                }
            });
        }
        
        // "White Creatures" - Count$WhiteTypeYouCtrl.Creature
        if(sq[0].contains("White")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardUtil.getColor(c) == Constant.Color.White;
                }
            });
        }
        
        if(sq[0].contains("Blue")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardUtil.getColor(c) == Constant.Color.Blue;
                }
            });
        }
        
        if(sq[0].contains("Black")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardUtil.getColor(c) == Constant.Color.Black;
                }
            });
        }
        
        if(sq[0].contains("Red")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardUtil.getColor(c) == Constant.Color.Red;
                }
            });
        }
        
        if(sq[0].contains("Green")) {
            someCards = someCards.filter(new CardListFilter() {
                public boolean addCard(Card c) {
                    return CardUtil.getColor(c) == Constant.Color.Green;
                }
            });
        }
        
        if(sq[0].contains("Multicolor")) someCards = someCards.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return (CardUtil.getColors(c).size() > 1);
            }
        });
        
        if(sq[0].contains("Monocolor")) someCards = someCards.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return (CardUtil.getColors(c).size() == 1);
            }
        });
        
        // 1/10 - Count$MaxCMCYouCtrl
        if(sq[0].contains("MaxCMC")) {
            int mmc = 0;
            int cmc = 0;
            for(int i = 0; i < someCards.size(); i++) {
                cmc = CardUtil.getConvertedManaCost(someCards.getCard(i).getManaCost());
                if(cmc > mmc) mmc = cmc;
            }
            
            return doXMath(mmc, m);
        }
        
        n = someCards.size();
        
        return doXMath(n, m);
    }
    
    private static int doXMath(int num, String[] m) {
        if(m[0].equals("none")) return num;
        
        String[] s = m[0].split("\\.");
        
        if(s[0].contains("Plus")) return num + Integer.parseInt(s[1]);
        else if(s[0].contains("NMinus")) return Integer.parseInt(s[1]) - num;
        else if(s[0].contains("Minus")) return num - Integer.parseInt(s[1]);
        else if(s[0].contains("Twice")) return num * 2;
        else if(s[0].contains("HalfUp")) return (int) (Math.ceil(num / 2.0));
        else if(s[0].contains("HalfDown")) return (int) (Math.floor(num / 2.0));
        else if(s[0].contains("Negative")) return num * -1;
        else if(s[0].contains("Times")) return num * Integer.parseInt(s[1]);
        else return num;
    }
    
    
    public static void doDrawBack(String DB, int nDB, String cardController, String Opp, String TgtP, Card Src, Card TgtC) {
        // Drawbacks may be any simple additional effect a spell or ability may have
        // not just the negative ones
        
        String d[] = DB.split("/");
        int X = 0;
        if(d.length > 1) if(d[1].matches("dX")) // 2/10
        {
            String dX = Src.getSVar(d[1]);
            if(dX.startsWith("Count$")) {
                String dd[] = dX.split("\\$");
                X = xCount(Src, dd[1]);
            }
        } else if(d[1].matches("X")) {
            X = nDB;
            if(d[1].contains(".")) {
                String dd[] = d[1].split("\\.");
                ArrayList<String> ddd = new ArrayList<String>();
                for(int i = 1; i < dd.length; i++)
                    ddd.add(dd[i]);
                
                X = doXMath(X, ddd.toArray(new String[3]));
            }
        } else if(d[1].matches("[0-9][0-9]?")) X = Integer.parseInt(d[1]);
        
        String dbPlayer = "";
        if(d[0].contains("You")) dbPlayer = cardController;
        else if(d[0].contains("Opp")) dbPlayer = Opp;
        else if(d[0].contains("Tgt")) dbPlayer = TgtP;
        
        // 1/10
        if(d[0].contains("DamageTgtC")) AllZone.GameAction.addDamage(TgtC, Src, X);
        else if(d[0].contains("DamageSelf")) AllZone.GameAction.addDamage(Src, Src, X); // 2/10
        else if(d[0].contains("Damage")) AllZone.GameAction.addDamage(dbPlayer, X, Src);
        
        

        if(d[0].contains("GainLife")) AllZone.GameAction.getPlayerLife(dbPlayer).addLife(X);

        if(d[0].contains("LoseLifeTgtCtrlr")) //2/10
        AllZone.GameAction.getPlayerLife(TgtC.getController()).subtractLife(X,Src);
        else if(d[0].contains("LoseLife")) AllZone.GameAction.getPlayerLife(dbPlayer).subtractLife(X,Src);
        
        if(d[0].contains("Discard")) {
            if(d.length > 2) {
                if(d[2].contains("UnlessDiscardType")) {
                    String dd[] = d[2].split("\\.");
                    AllZone.GameAction.discardUnless(dbPlayer, X, dd[1]);
                }
                if(d[2].contains("AtRandom")) AllZone.GameAction.discardRandom(dbPlayer, X);
            } else AllZone.GameAction.discard(dbPlayer, X);
        }
        
        if(d[0].contains("HandToLibrary")) AllZone.GameAction.handToLibrary(dbPlayer, X, d[2]);
        
        if(d[0].contains("Draw")) for(int i = 0; i < X; i++)
            AllZone.GameAction.drawCard(dbPlayer);
        
        if(d[0].contains("UntapTgt")) TgtC.untap();
        
        if(d[0].contains("UntapAll")) // 6/10
        {
        	CardList ut = new CardList();
        	if (d[0].contains("YouCtrl"))
        		ut.addAll(AllZone.getZone(Constant.Zone.Play, dbPlayer).getCards());
        	else if (d[0].contains("OppCtrl"))
        		ut.addAll(AllZone.getZone(Constant.Zone.Play, Opp).getCards());
        	else
        	{
        		ut.addAll(AllZone.getZone(Constant.Zone.Play, dbPlayer).getCards());
        		ut.addAll(AllZone.getZone(Constant.Zone.Play, Opp).getCards());
        	}
        	if (d[0].contains("Type"))
        	{
        		String dd[] = d[0].split("\\.");
        		ut = ut.getValidCards(dd);
        	}
        	
        	for (int i=0; i<ut.size(); i++)
        		ut.get(i).untap();
        }
        
        if(d[0].contains("TapTgt")) // 2/10
        TgtC.tap();
        
        if(d[0].contains("GenToken")) // placeholder for effect
        X = X + 0;
        
        if(d[0].contains("ReturnFromYard")) // placeholder for effect
        X = X + 0;
        
        if(d[0].contains("Sacrifice")) // placeholder for effect
        X = X + 0;
    }
    
    public static int getNumberOfMostProminentCreatureType(CardList list, String type) {
        list = list.getType(type);
        return list.size();
    }
    
    public static String getMostProminentCreatureType(CardList list) {
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        for(Card c : list) {
            ArrayList<String> typeList = c.getType();
            
            for(String var:typeList) {
                if(var.equals("Creature") || var.equals("Artifact") || var.equals("Land") || var.equals("Tribal")
                        || var.equals("Enchantment") || var.equals("Legendary")) ;
                else if(!map.containsKey(var)) map.put(var, 1);
                else {
                    map.put(var, map.get(var) + 1);
                }
            }
        }//for
        
        int max = 0;
        String maxType = "";
        
        for(Entry<String, Integer> entry : map.entrySet()){
            String type = entry.getKey();
            Log.debug(type + " - " + entry.getValue());
            
            if(max < entry.getValue()) {
                max = entry.getValue();
                maxType = type;
            }
        }
        
        return maxType;
    }
    
    public static String getMostProminentColor(CardList list) {
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        
        for(Card c : list) {
            ArrayList<String> colorList = CardUtil.getColors(c);
            
            for(String color:colorList) {
                if(color.equals("colorless")) ;
                else if(!map.containsKey(color)) map.put(color, 1);
                else {
                    map.put(color, map.get(color) + 1);
                }
            }
        }//for
        
        int max = 0;
        String maxColor = "";

        for(Entry<String, Integer> entry : map.entrySet()){
            String color = entry.getKey();
            Log.debug(color + " - " + entry.getValue());
            
            if(max < entry.getValue()) {
                max = entry.getValue();
                maxColor = color;
            }
        }

        return maxColor;
    }
    
    
    public static String chooseCreatureTypeAI(Card c) {
        String s = "";
        //TODO, take into account what human has
        
        PlayerZone humanPlayZone = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
        PlayerZone humanLibZone = AllZone.getZone(Constant.Zone.Library, Constant.Player.Human);
        
        CardList humanPlay = new CardList(humanPlayZone.getCards());
        CardList humanLib = new CardList(humanLibZone.getCards());
        
        PlayerZone compPlayZone = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
        PlayerZone compLibZone = AllZone.getZone(Constant.Zone.Library, Constant.Player.Computer);
        PlayerZone compHandZone = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
        
        CardList compPlay = new CardList(compPlayZone.getCards());
        CardList compLib = new CardList(compLibZone.getCards());
        CardList compHand = new CardList(compHandZone.getCards());
        
        humanPlay = humanPlay.getType("Creature");
        humanLib = humanLib.getType("Creature");
        
        compPlay = compPlay.getType("Creature");
        compLib = compLib.getType("Creature");
        
        //Buffs
        if(c.getName().equals("Conspiracy") || c.getName().equals("Cover of Darkness")
                || c.getName().equals("Belbe's Portal") || c.getName().equals("Steely Resolve")
                || c.getName().equals("Shared Triumph")) {
            
            String type = "";
            int number = 0;
            if((c.getName().equals("Shared Triumph") || c.getName().equals("Cover of Darkness") || c.getName().equals(
                    "Steely Resolve"))
                    && compPlay.size() > 7) {
                type = getMostProminentCreatureType(compPlay);
                number = getNumberOfMostProminentCreatureType(compPlay, type);
                
            }
            
            if(number >= 3) s = type;
            else {
                type = getMostProminentCreatureType(compLib);
                number = getNumberOfMostProminentCreatureType(compLib, type);
                if(number >= 5) s = type;
                
            }
            
            CardList turnTimber = new CardList();
            turnTimber.addAll(compPlay.toArray());
            turnTimber.addAll(compLib.toArray());
            turnTimber.addAll(compHand.toArray());
            
            turnTimber = turnTimber.getName("Turntimber Ranger");
            
            if(c.getName().equals("Conspiracy") && turnTimber.size() > 0) s = "Ally";
            
        }
        //Debuffs
        else if(c.getName().equals("Engineered Plague")) {
            String type = "";
            int number = 0;
            if(c.getName().equals("Engineered Plague") && humanPlay.size() > 6) {
                type = getMostProminentCreatureType(humanPlay);
                number = getNumberOfMostProminentCreatureType(humanPlay, type);
                if(number >= 3) s = type;
                else if(humanLib.size() > 0) {
                    type = getMostProminentCreatureType(humanLib);
                    number = getNumberOfMostProminentCreatureType(humanLib, type);
                    if(number >= 5) s = type;
                }
            }
        }
        return s;
    }

    public static CardList getCards(String cardName)
    {
    	CardList list = new CardList();
    	list.addAll(AllZone.Human_Play.getCards());
    	list.addAll(AllZone.Computer_Play.getCards());
    	list = list.getName(cardName);
    	return list;
    }
    
    public static CardList getCards(String cardName, String player) {
    	PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
        CardList list = new CardList(play.getCards());
        list = list.getName(cardName);
        return list;
    }
    
    public static int countBasicLandTypes(String player) {
        String basic[] = {"Forest", "Plains", "Mountain", "Island", "Swamp"};
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
        CardList list = new CardList(play.getCards());
        int count = 0;
        
        for(int i = 0; i < basic.length; i++)
            if(!list.getType(basic[i]).isEmpty()) count++;
        
        return count;
    }
    
    //total cost to pay for an attacker c, cards like Propaganda, Ghostly Prison, Collective Restraint, ...
    public static String getPropagandaCost(Card c) {
        String s = "";
        
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(c.getController()));
        CardList list = new CardList(play.getCards());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getName().equals("Propaganda") || c.getName().equals("Windborn Muse")
                        || c.getName().equals("Ghostly Prison");
            }
        });
        int cost = list.size() * 2;
        
        list = new CardList(play.getCards());
        list = list.getName("Collective Restraint");
        
        int domain = countBasicLandTypes(AllZone.GameAction.getOpponent(c.getController()));
        
        cost += domain * list.size();
        
        s = Integer.toString(cost);
        
        return s;
    }
    
    public static int getUsableManaSources(String player) {
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
        CardList list = new CardList(play.getCards());
        list = list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                for(Ability_Mana am:c.getAIPlayableMana())
                    if(am.canPlay()) return true;
                return false;
            }
        });
        
        return list.size();
    }
    
    //do card1 and card2 share any colors?
    public static boolean sharesColorWith(Card card1, Card card2) {
        ArrayList<String> card1Colors = CardUtil.getOnlyColors(card1);
        ArrayList<String> card2Colors = CardUtil.getOnlyColors(card2);
        
        for(String color:card1Colors) {
            if(card2Colors.contains(color)) return true;
        }
        
        return false;
    }
    
    public static ArrayList<String> getCreatureLandNames() {
        String[] creatureLands = {
                "Faerie Conclave", "Forbidding Watchtower", "Treetop Village", "Ghitu Encampment",
                "Blinkmoth Nexus", "Mishra's Factory", "Mutavault"};
        final ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < creatureLands.length; i++)
            list.add(creatureLands[i]);
        return list;
    }
    
    public static Card getTopCard(Card c)
    {
    	PlayerZone lib = AllZone.getZone(Constant.Zone.Library, c.getController());
    	if (lib.size() > 0)
    		return lib.get(0);
    	else
    		return null;
    }
    
    public static CardList makeToken(String name, String imageName, Card source, String manaCost, String[] types, int baseAttack, int baseDefense, String[] intrinsicKeywords) {
    	// todo(sol) this function shouldn't be called, better to call makeToken with String controller as third paramter
    	CardList list = new CardList();
        Card c = new Card();
        c.setName(name);
        c.setImageName(imageName);
        
        //c.setController(source.getController());
        //c.setOwner(source.getOwner());
        
        c.setManaCost(manaCost);
        c.setToken(true);
        
        for(String t:types)
            c.addType(t);
        
        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);
        
        for(String kw:intrinsicKeywords)
            c.addIntrinsicKeyword(kw);
        
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, source.getController());
        
        int multiplier = 1;
        int doublingSeasons = CardFactoryUtil.getCards("Doubling Season", source.getController()).size();
        if(doublingSeasons > 0) multiplier = (int) Math.pow(2, doublingSeasons);
        
        for(int i = 0; i < multiplier; i++) {
            Card temp = CardFactory.copyStats(c);
            temp.setToken(true);
            temp.setController(source.getController());
            temp.setOwner(source.getController());
            play.add(temp);
            list.add(temp);
        }
        return list;
    }
    
    public static CardList makeToken(String name, String imageName, String controller, String manaCost, String[] types, int baseAttack, int baseDefense, String[] intrinsicKeywords) {
        CardList list = new CardList();
        Card c = new Card();
        c.setName(name);
        c.setImageName(imageName);
        
        //c.setController(controller);
        //c.setOwner(controller);
        
        c.setManaCost(manaCost);
        c.setToken(true);
        
        for(String t:types)
            c.addType(t);
        
        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);
        
        for(String kw:intrinsicKeywords)
            c.addIntrinsicKeyword(kw);
        
        PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);
        
        int multiplier = 1;
        int doublingSeasons = CardFactoryUtil.getCards("Doubling Season", controller).size();
        if(doublingSeasons > 0) multiplier = (int) Math.pow(2, doublingSeasons);
        
        for(int i = 0; i < multiplier; i++) {
            Card temp = CardFactory.copyStats(c);
            temp.setController(controller);
            temp.setOwner(controller);
            temp.setToken(true);
            play.add(temp);
            list.add(temp);
        }
        return list;
    }
    
    public static int getTotalBushidoMagnitude(Card c) {
        int count = 0;
        ArrayList<String> keywords = c.getKeyword();
        for(String kw:keywords) {
            if(kw.contains("Bushido")) {
                String[] parse = kw.split(" ");
                String s = parse[1];
                count += Integer.parseInt(s);
            }
        }
        return count;
    }
    
    public static ArrayList<Ability> getBushidoEffects(Card c) {
        ArrayList<String> keywords = c.getKeyword();
        ArrayList<Ability> list = new ArrayList<Ability>();
        
        final Card crd = c;
        
        for(String kw:keywords) {
            if(kw.contains("Bushido")) {
                String[] parse = kw.split(" ");
                String s = parse[1];
                final int magnitude = Integer.parseInt(s);
                

                Ability ability = new Ability(c, "0") {
                    @Override
                    public void resolve() {
                        final Command untilEOT = new Command() {
                            
                            private static final long serialVersionUID = 3014846051064254493L;
                            
                            public void execute() {
                                if(AllZone.GameAction.isCardInPlay(crd)) {
                                    crd.addTempAttackBoost(-1 * magnitude);
                                    crd.addTempDefenseBoost(-1 * magnitude);
                                }
                            }
                        };
                        
                        AllZone.EndOfTurn.addUntil(untilEOT);
                        
                        crd.addTempAttackBoost(magnitude);
                        crd.addTempDefenseBoost(magnitude);
                    }
                };
                StringBuilder sb = new StringBuilder();
                sb.append(c);
                sb.append(" - (Bushido) gets +");
                sb.append(magnitude);
                sb.append("/+");
                sb.append(magnitude);
                sb.append(" until end of turn.");
                ability.setStackDescription(sb.toString());
                
                list.add(ability);
            }
        }
        return list;
    }
    
    static public int getNeededXDamage(SpellAbility ability)
    {
    	//when targeting a creature, make sure the AI won't overkill on X damage
		Card target = ability.getTargetCard();
		int neededDamage = -1;
		
		Card c = ability.getSourceCard();
		
		if (target != null && c.getText().contains("deals X damage to target") && !c.getName().equals("Death Grasp"))
			neededDamage = target.getNetDefense() - target.getDamage();
		
		return neededDamage;
    }
    /*
    public static void checkEquipmentOnControllerChange(PlayerZone from, PlayerZone to, Card c)
    {
    	if (c.isEquipped() && !from.equals(to))
    		c.unEquipAllCards();
    }
    */
    
    
    /**
     * getWorstLand(String)
     *
     * This function finds the worst land a player has in play based on:
     * worst
     * 1. tapped, basic land
     * 2. tapped, non-basic land
     * 3. untapped, basic land
     * 4. untapped, non-basic land
     * best
     *
     * This is useful when the AI needs to find one of its lands to sacrifice
     *
     * @param player - Constant.Player.Human or Constant.Player.Computer
     * @return the worst land found based on the description above
     */
    public static Card getWorstLand(String player) {
    	Card worstLand = null;
    	CardList lands = CardFactoryUtil.getLandsInPlay(player);
    	//first, check for tapped, basic lands
    	for( int i = 0; i < lands.size(); i++ ) {
    		Card tmp = lands.get(i);
    		if(tmp.isTapped() && tmp.isBasicLand()) {
    			worstLand = tmp;
    		}
    	}
    	//next, check for tapped, non-basic lands
    	if(worstLand == null) {
    		for( int i = 0; i < lands.size(); i++ ) {
    			Card tmp = lands.get(i);
    			if(tmp.isTapped()) {
    				worstLand = tmp;
    			}
    		}
    	}
    	//next, untapped, basic lands
    	if(worstLand == null) {
    		for( int i = 0; i < lands.size(); i++ ) {
    			Card tmp = lands.get(i);
    			if(tmp.isUntapped() && tmp.isBasicLand()) {
    				worstLand = tmp;
    			}
    		}
    	}
    	//next, untapped, non-basic lands
    	if(worstLand == null) {
    		for( int i = 0; i < lands.size(); i++ ) {
    			Card tmp = lands.get(i);
    			if(tmp.isUntapped()) {
    				worstLand = tmp;
    			}
    		}
    	}
    	return worstLand;
    }//end getWorstLand

    /**
     * getLandsInPlay(String)
     *
     * This function returns a CardList of all lands that the given
     * player has in Constant.Zone.Play
     *
     * @param player - Constant.Player.Human or Constant.Player.Computer
     * @return a CardList of that players lands
     */
    public static CardList getLandsInPlay(String player) {
    	PlayerZone compBattlezone = AllZone.getZone(Constant.Zone.Play, player);
    	CardList list = new CardList(compBattlezone.getCards());
    	list = list.filter(new CardListFilter() {
    		public boolean addCard(Card c) {
    			if(c.isLand()) return true;
    			else return false;
    		}
    	});
    	return list;
    }

    
    //may return null
    static public Card getRandomCard(CardList list) {
        if(list.size() == 0) return null;
        
        int index = random.nextInt(list.size());
        return list.get(index);
    }
    
    //may return null
    static public Card getRandomCard(PlayerZone zone) {
        return getRandomCard(new CardList(zone.getCards()));
    }
    
    
    public static void revertManland(Card c, String[] removeTypes, String[] removeKeywords) {
        c.setBaseAttack(0);
        c.setBaseDefense(0);
        for(String r : removeTypes)
        	c.removeType(r);

        for(String k : removeKeywords)
        	c.removeIntrinsicKeyword(k);
        
        c.setManaCost("");
        c.unEquipAllCards();
    }
    
    public static void activateManland(Card c, int attack, int defense, String[] addTypes, String[] addKeywords, String cost) {
        c.setBaseAttack(attack);
        c.setBaseDefense(defense);
        
        for(String r : addTypes)
        {
        	// if the card doesn't have that type, add it
        	if (!c.getType().contains(r))
        		c.addType(r);
        }
        for(String k : addKeywords)
        {
        	// if the card doesn't have that keyword, add it (careful about stackable keywords)
        	if(!c.getIntrinsicKeyword().contains(k))
        		c.addIntrinsicKeyword(k);	
        }
        
        c.setManaCost(cost);
    }
    
    public static boolean canHumanPlayLand(){
    	// LandsToPlay Left or Fastbond in play, Human's turn, Stack is Empty, In Main Phase
    	return (AllZone.GameInfo.humanNumberLandPlaysLeft() > 0 || CardFactoryUtil.getCards("Fastbond", "Human").size() > 0) &&
    		AllZone.GameAction.getLastPlayerToDraw().equals("Human") && (AllZone.Stack.size() == 0) &&
    		(AllZone.Phase.getPhase().equals(Constant.Phase.Main1) || AllZone.Phase.getPhase().equals(Constant.Phase.Main2));
    }
    
    public static boolean canComputerPlayLand(){
    	// LandsToPlay Left or Fastbond in play, Computer's turn, Stack is Empty, In Main Phase
    	return (AllZone.GameInfo.computerNumberLandPlaysLeft() > 0 || CardFactoryUtil.getCards("Fastbond", "Computer").size() > 0) &&
			AllZone.GameAction.getLastPlayerToDraw().equals("Computer") && (AllZone.Stack.size() == 0) &&
			(AllZone.Phase.getPhase().equals(Constant.Phase.Main1) || AllZone.Phase.getPhase().equals(Constant.Phase.Main2));
    }
    
    public static void playLandEffects(Card c){
    	final String player = c.getController();
    	boolean extraLand;
    	if (player.equals("Human")){
    		extraLand = AllZone.GameInfo.humanPlayedFirstLandThisTurn();
    	}
    	else{
    		extraLand = AllZone.GameInfo.computerPlayedFirstLandThisTurn();
    	}
    	
		if(extraLand) {
			CardList fastbonds = CardFactoryUtil.getCards("Fastbond", player);
	        for(final Card f : fastbonds){
	            SpellAbility ability = new Ability(f, "0") {
	                @Override
	                public void resolve() {
	                    AllZone.GameAction.getPlayerLife(f.getController()).subtractLife(1,f);
	                }
	            };
	            ability.setStackDescription("Fastbond - Deals 1 damage to you.");
	            AllZone.Stack.add(ability);
	        }
        }
		
		CardList greedy = CardFactoryUtil.getCards("Horn of Greed");
		if (!greedy.isEmpty()){
			for(final Card g : greedy){
	            SpellAbility ability = new Ability(g, "0") {
	                @Override
	                public void resolve() {
	                	AllZone.GameAction.drawCard(player);
	                }
	            };
	            ability.setStackDescription("Horn of Greed - " + player + " draws a card.");
	            AllZone.Stack.add(ability);
			}
		}
    }

    public static void main(String[] args) {
        
        CardList in = AllZone.CardFactory.getAllCards();
        
        CardList list = new CardList();
        list.addAll(CardListUtil.getColor(in, "black").toArray());
        list = list.getType("Creature");
        
        System.out.println("Most prominent creature type: " + getMostProminentCreatureType(list));
        

        String manacost = "3 GW W W R B S";
        String multipliedTwice = multiplyManaCost(manacost, 2);
        String multipliedThrice = multiplyManaCost(manacost, 3);
        
        System.out.println(manacost + " times 2 = " + multipliedTwice);
        System.out.println(manacost + " times 3 = " + multipliedThrice);
        

    }
    
}
