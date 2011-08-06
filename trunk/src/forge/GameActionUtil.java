
package forge;


import java.util.ArrayList;
import java.util.HashMap;


public class GameActionUtil {
	public static void executeUpkeepEffects() {
		upkeep_removeDealtDamageToOppThisTurn();
		upkeep_UpkeepCost(); //sacrifice unless upkeep cost is paid
		upkeep_DestroyUpkeepCost(); //destroy unless upkeep cost is paid
		upkeep_DamageUpkeepCost(); //deal damage unless upkeep cost is paid
		upkeep_CumulativeUpkeepCost(); //sacrifice unless cumulative upkeep cost is paid
		upkeep_Echo();
		upkeep_TabernacleUpkeepCost();
		upkeep_MagusTabernacleUpkeepCost();
		// upkeep_CheckEmptyDeck_Lose(); //still a little buggy
		upkeep_Genesis();
		upkeep_Phyrexian_Arena();
		upkeep_Carnophage();
		upkeep_Sangrophage();
		upkeep_Honden_of_Cleansing_Fire();
		upkeep_Honden_of_Seeing_Winds();
		upkeep_Honden_of_Lifes_Web();
		upkeep_Honden_of_Nights_Reach();
		upkeep_Honden_of_Infinite_Rage();
		upkeep_Land_Tax();
		upkeep_Greener_Pastures();
		upkeep_Wort();
		upkeep_Squee();
		upkeep_Sporesower_Thallid();
		upkeep_Dragonmaster_Outcast();
		upkeep_Scute_Mob();
		upkeep_Lichenthrope();
		upkeep_Heartmender();
		upkeep_AEther_Vial();
		upkeep_Ratcatcher();
		upkeep_Nath();
		upkeep_Anowon();
		upkeep_Cunning_Lethemancer();
		upkeep_Sensation_Gorger();
		upkeep_Winnower_Patrol();
		upkeep_Nightshade_Schemers();
		upkeep_Wandering_Graybeard();
		upkeep_Wolf_Skull_Shaman();
		upkeep_Leaf_Crowned_Elder();
		upkeep_Debtors_Knell();
		upkeep_Reya();
		upkeep_Emeria();
		upkeep_Oversold_Cemetery();
		upkeep_Nether_Spirit();
		upkeep_Nettletooth_Djinn();
		upkeep_Fledgling_Djinn();
		upkeep_Juzam_Djinn();
		upkeep_Grinning_Demon();
		upkeep_Moroii();
		upkeep_Vampire_Lacerator();
		upkeep_Seizan_Perverter_of_Truth();
		upkeep_Serendib_Efreet();
		upkeep_Sleeper_Agent();
		upkeep_Cursed_Land();
		upkeep_Pillory_of_the_Sleepless();
		upkeep_Bringer_of_the_Green_Dawn();
		upkeep_Bringer_of_the_Blue_Dawn();
		upkeep_Bringer_of_the_White_Dawn();
		upkeep_Mirror_Sigil_Sergeant();
		upkeep_Dragon_Broodmother(); //put this before bitterblossom and mycoloth, so that they will resolve FIRST
		upkeep_Bitterblossom();
		upkeep_Goblin_Assault();
		upkeep_Awakening_Zone();
		upkeep_Battle_of_Wits();
		upkeep_Epic_Struggle();
		upkeep_Near_Death_Experience();
		upkeep_Helix_Pinnacle();
		upkeep_Barren_Glory();
		upkeep_Felidar_Sovereign();
		upkeep_Klass();
		upkeep_Convalescence();
		upkeep_Convalescent_Care();
		upkeep_Karma();
		upkeep_Defense_of_the_Heart();
		upkeep_Mycoloth();
		upkeep_Spore_Counters();
		upkeep_Vanishing();
		upkeep_Aven_Riftwatcher();
		upkeep_Calciderm();
		upkeep_Blastoderm();
		upkeep_Masticore();
		upkeep_Eldrazi_Monument();
		upkeep_Blaze_Counters();
		upkeep_Dark_Confidant(); // keep this one semi-last

		upkeep_Copper_Tablet();
		upkeep_The_Rack();
		upkeep_BlackVise(); 
		upkeep_Ivory_Tower();

		upkeep_Howling_Mine(); // keep this one even laster, since it would
		// cause black vise to do an extra point of
		// damage if black vise was in play
		upkeep_Font_of_Mythos();
		upkeep_Overbeing_of_Myth();

		upkeep_AI_Aluren(); // experimental, just have the AI dump his small
		// creats in play when aluren is there
	}

	public static void executeTapSideEffects(Card c) {

		/* cards with Tap side effects can be listed here, just like in
		 * the CardFactory classes
		 */
		if(c.getName().equals("City of Brass")) {
			final String player = c.getController();
			final Card crd = c;
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.addDamage(player, 1, crd);
				}
			};// Ability
			ability.setStackDescription("City of Brass deals 1 damage to " + player);
			AllZone.Stack.add(ability);
		}//end City of Brass

		if (c.getName().equals("Fallowsage")) {
			final String player = c.getController();
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.drawCard(player);
				}
			};// Ability
			ability.setStackDescription("Fallowsage - draw a card");
			AllZone.Stack.add(ability);
		}//end Fallowsage

	}


	public static void executePlayCardEffects(SpellAbility sa) {
		// experimental:
		// this method check for cards that have triggered abilities whenever a
		// card gets played
		// (called in MagicStack.java)
		Card c = sa.getSourceCard();
		
		if (c.getName().equals("Kozilek, Butcher of Truth"))
			playCard_Kozilek(c);
		else if (c.getName().equals("Ulamog, the Infinite Gyre"))
			playCard_Ulamog(c);

		playCard_Cascade(c);

		playCard_Dovescape(c); //keep this one top
		playCard_Demigod_of_Revenge(c);
		playCard_Halcyon_Glaze(c);
		playCard_Thief_of_Hope(c);
		playCard_Infernal_Kirin(c);
		playCard_Cloudhoof_Kirin(c);
		playCard_Bounteous_Kirin(c);
		playCard_Emberstrike_Duo(c);
		playCard_Gravelgill_Duo(c);
		playCard_Safehold_Duo(c);
		playCard_Tattermunge_Duo(c);
		playCard_Thistledown_Duo(c);
		playCard_Battlegate_Mimic(c);
		playCard_Nightsky_Mimic(c);
		playCard_Riverfall_Mimic(c);
		playCard_Shorecrasher_Mimic(c);
		playCard_Woodlurker_Mimic(c);
		playCard_Belligerent_Hatchling(c);
		playCard_Voracious_Hatchling(c);
		playCard_Sturdy_Hatchling(c);
		playCard_Noxious_Hatchling(c);
		playCard_Witch_Maw_Nephilim(c);
		playCard_Forced_Fruition(c);
		playCard_Gelectrode(c);
		playCard_Cinder_Pyromancer(c);
		playCard_Ballynock_Trapper(c);
		playCard_Standstill(c);
		playCard_Memory_Erosion(c);
		playCard_SolKanar(c);
		playCard_Gilt_Leaf_Archdruid(c);
		playCard_Reki(c);
		playCard_Vedalken_Archmage(c);
		playCard_Sigil_of_the_Empty_Throne(c);
		playCard_Merrow_Levitator(c);
		playCard_Primordial_Sage(c);
		playCard_Quirion_Dryad(c);
		playCard_Enchantress_Draw(c);
		playCard_Mold_Adder(c);
		playCard_Fable_of_Wolf_and_Owl(c);
		playCard_Kor_Firewalker(c);
		playCard_Curse_of_Wizardry(c);
	}

	public static void playCard_Kozilek(Card c)
	{
		final String controller = c.getController();
		final Ability ability = new Ability(c, "0")
		{
			public void resolve()
			{
				for (int i=0;i<4;i++)
					AllZone.GameAction.drawCard(controller);
			}
		};
		ability.setStackDescription("Kozilek - draw four cards.");
		AllZone.Stack.add(ability);
	}
	
	public static void playCard_Ulamog(Card c)
	{
		final Card ulamog = c;
		final String controller = c.getController();
		final Ability ability = new Ability(c, "0")
		{
			public void chooseTargetAI()
			{
				CardList list = AllZoneUtil.getPlayerCardsInPlay(Constant.Player.Human);
				list = list.filter(new CardListFilter()
				{
					public boolean addCard(Card card)
					{
						return CardFactoryUtil.canTarget(ulamog, card);
					}
				});
				
				if (list.size()>0)
				{
					CardListUtil.sortCMC(list);
					setTargetCard(list.get(0));
				}
			}
			public void resolve()
			{
				Card crd = getTargetCard();
				if (crd!=null) {
					if (CardFactoryUtil.canTarget(ulamog, crd))
						AllZone.GameAction.destroy(crd);
				}
			}
		};
		ability.setBeforePayMana(CardFactoryUtil.input_targetPermanent(ability));
		if (controller.equals(Constant.Player.Human))
			AllZone.GameAction.playSpellAbility(ability);
		else {
			ability.chooseTargetAI();
			AllZone.Stack.add(ability);
		}
	}
	
	public static void playCard_Cascade(Card c) {

		if(c.getKeyword().contains("Cascade") || c.getName().equals("Bituminous Blast")) //keyword gets cleared for Bitumonous Blast
		{
			final String controller = c.getController();
			final PlayerZone lib = AllZone.getZone(Constant.Zone.Library, controller);
			final Card cascCard = c;

			final Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {
					CardList topOfLibrary = new CardList(lib.getCards());
					CardList revealed = new CardList();

					if(topOfLibrary.size() == 0) return;

					Card cascadedCard = null;
					Card crd;
					int count = 0;
					while(cascadedCard == null) {
						crd = topOfLibrary.get(count++);
						revealed.add(crd);
						lib.remove(crd);
						if((!crd.isLand() && CardUtil.getConvertedManaCost(crd.getManaCost()) < CardUtil.getConvertedManaCost(cascCard.getManaCost()))) cascadedCard = crd;

						if(count == topOfLibrary.size()) break;

					}//while
						AllZone.Display.getChoiceOptional("Revealed cards:", revealed.toArray());

					if(cascadedCard != null && !cascadedCard.isUnCastable()) {

						if(cascadedCard.getController().equals(Constant.Player.Human)) {
							String[] choices = {"Yes", "No"};

							Object q = null;

							q = AllZone.Display.getChoiceOptional("Cast " + cascadedCard.getName() + "?", choices);
							if(q != null) {
								if(q.equals("Yes")) {
									AllZone.GameAction.playCardNoCost(cascadedCard);
									revealed.remove(cascadedCard);
								}
							}
						} else //computer
						{
							ArrayList<SpellAbility> choices = cascadedCard.getBasicSpells();

							for(SpellAbility sa:choices) {
								if(sa.canPlayAI()) {
									ComputerUtil.playStackFree(sa);
									revealed.remove(cascadedCard);
									break;
								}
							}
						}
					}
					revealed.shuffle();
					for(Card bottom:revealed) {
						lib.add(bottom);
					}
				}
			};
			ability.setStackDescription(c + " - Cascade.");
			AllZone.Stack.add(ability);

		}
	}

	public static void playCard_Emberstrike_Duo(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Emberstrike Duo");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Black)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.addTempAttackBoost(-1);
								card.addTempDefenseBoost(-1);
							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.addTempAttackBoost(1);
							card.addTempDefenseBoost(1);
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a black spell, Emberstrike Duo gets +1/+1 until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Red)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = -4569751606008597903L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(card)) {
							card.removeIntrinsicKeyword("First Strike");

						}
					}
				};

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(!card.getIntrinsicKeyword().contains("First Strike")) card.addIntrinsicKeyword("First Strike");
						AllZone.EndOfTurn.addUntil(untilEOT);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a red spell, Emberstrike Duo gains first strike until end of turn.");
				AllZone.Stack.add(ability2);
			}
		}//if


	}//Emberstrike Duo

	public static void playCard_Gravelgill_Duo(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Gravelgill Duo");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.addTempAttackBoost(-1);
								card.addTempDefenseBoost(-1);


							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.addTempAttackBoost(1);
							card.addTempDefenseBoost(1);
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a blue spell, Gravelgill Duo gets +1/+1 until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Black)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = -4569751606008597903L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(card)) {
							card.removeIntrinsicKeyword("Fear");

						}
					}
				};

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(!card.getIntrinsicKeyword().contains("Fear")) card.addIntrinsicKeyword("Fear");
						AllZone.EndOfTurn.addUntil(untilEOT);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a black spell, Emberstrike Duo gains fear until end of turn.");
				AllZone.Stack.add(ability2);
			}
		}//if


	}//Gravelgill Duo

	public static void playCard_Safehold_Duo(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Safehold Duo");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.addTempAttackBoost(-1);
								card.addTempDefenseBoost(-1);


							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.addTempAttackBoost(1);
							card.addTempDefenseBoost(1);
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a green spell, Safehold Duo gets +1/+1 until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.White)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = -4569751606008597903L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(card)) {
							card.removeIntrinsicKeyword("Vigilance");

						}
					}
				};

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(!card.getIntrinsicKeyword().contains("Vigilance")) card.addIntrinsicKeyword("Vigilance");
						AllZone.EndOfTurn.addUntil(untilEOT);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a white spell, Safehold Duo gains vigilance until end of turn.");
				AllZone.Stack.add(ability2);
			}
		}//if


	}//Safehold Duo

	public static void playCard_Tattermunge_Duo(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Tattermunge Duo");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Red)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.addTempAttackBoost(-1);
								card.addTempDefenseBoost(-1);


							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.addTempAttackBoost(1);
							card.addTempDefenseBoost(1);
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a red spell, Tattermunge Duo gets +1/+1 until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = -4569751606008597903L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(card)) {
							card.removeIntrinsicKeyword("Forestwalk");

						}
					}
				};

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(!card.getIntrinsicKeyword().contains("Forestwalk")) card.addIntrinsicKeyword("Forestwalk");
						AllZone.EndOfTurn.addUntil(untilEOT);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a green spell, Tattermunge Duo gains forestwalk until end of turn.");
				AllZone.Stack.add(ability2);
			}
		}//if


	}//Tattermunge Duo

	public static void playCard_Thistledown_Duo(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Thistledown Duo");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.White)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.addTempAttackBoost(-1);
								card.addTempDefenseBoost(-1);


							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.addTempAttackBoost(1);
							card.addTempDefenseBoost(1);
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a white spell, Thistledown Duo gets +1/+1 until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = -4569751606008597903L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(card)) {
							card.removeIntrinsicKeyword("Flying");

						}
					}
				};

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(!card.getIntrinsicKeyword().contains("Flying")) card.addIntrinsicKeyword("Flying");
						AllZone.EndOfTurn.addUntil(untilEOT);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a blue spell, Thistledown Duo gains flying until end of turn.");
				AllZone.Stack.add(ability2);
			}
		}//if


	}//Thistledown Duo

	public static void playCard_Demigod_of_Revenge(Card c) {

		if(c.getName().equals("Demigod of Revenge")) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					PlayerZone Grave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.Phase.getActivePlayer());
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, AllZone.Phase.getActivePlayer());
					CardList evildead = new CardList();
					evildead.addAll(Grave.getCards());
					evildead = evildead.filter(new CardListFilter() {
						public boolean addCard(Card card) {
							return (card.getName().contains("Demigod of Revenge"));
						}
					});
					for(int i = 0; i < evildead.size(); i++) {
						Card c = evildead.get(i);
						Grave.remove(c);
						Play.add(c);
					}
				}
			}; // ability2
			ability2.setStackDescription(c.getName()
					+ " - "
					+ c.getController()
					+ " casts Demigod of Revenge, returns all cards named Demigod of Revenge from your graveyard to the battlefield.");
			AllZone.Stack.add(ability2);

		}//if					
	}// Demigod of Revenge

	public static void playCard_Halcyon_Glaze(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Halcyon Glaze");

		if(list.size() > 0) {
			if(c.getType().contains("Creature")) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(0);
								card.setBaseDefense(0);
								card.removeType("Creature");
								card.removeType("Illusion");
								card.removeIntrinsicKeyword("Flying");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(4);
							card.setBaseDefense(4);
							if(!card.getIntrinsicKeyword().contains("Flying")) card.addIntrinsicKeyword("Flying");
							if(!card.getType().contains("Creature")) card.addType("Creature");
							if(!card.getType().contains("Illusion")) card.addType("Illusion");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a creature spell Halcyon Glaze becomes a 4/4 Illusion creature with flying until end of turn.  It's still an enchantment.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Halcyon Glaze

	public static void playCard_Thief_of_Hope(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Thief of Hope");

		if(list.size() > 0) {
			if(c.getType().contains("Spirit") || c.getType().contains("Arcane")
					|| c.getIntrinsicKeyword().contains("Changeling")) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							final String target;
							if(card.getController().contains("Human")) {
								String[] choices = {"Opponent", "Yourself"};
								Object choice = AllZone.Display.getChoice("Choose target player", choices);
								if(choice.equals("Opponent")) {
									target = "Computer"; // check for target of spell/abilities should be here
								}// if choice yes
								else target = "Human"; // check for target of spell/abilities should be here
							} else target = "Human"; // check for target of spell/abilities should be here
							AllZone.GameAction.getPlayerLife(target).subtractLife(1);
							PlayerLife life = AllZone.GameAction.getPlayerLife(card.getController());
							life.addLife(1);

						} //resolve
					}; //ability
					ability2.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
					ability2.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability2));
					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a Spirit or Arcane spell,  target opponent loses 1 life and you gain 1 life.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Thief of Hope

	public static void playCard_Infernal_Kirin(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Infernal Kirin");

		if(list.size() > 0) {
			if(c.getType().contains("Spirit") || c.getType().contains("Arcane")
					|| c.getIntrinsicKeyword().contains("Changeling")) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final int converted = CardUtil.getConvertedManaCost(c.getManaCost());
					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							final String target;
							if(card.getController().contains("Human")) {
								String[] choices = {"Opponent", "Yourself"};
								Object choice = AllZone.Display.getChoice("Choose target player", choices);
								if(choice.equals("Opponent")) {
									target = "Computer"; // check for target of spell/abilities should be here
								}// if choice yes
								else target = "Human"; // check for target of spell/abilities should be here
							} else target = "Human"; // check for target of spell/abilities should be here
							PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, target);
							CardList fullHand = new CardList(hand.getCards());
							if(fullHand.size() > 0 && target.equals(Constant.Player.Computer)) AllZone.Display.getChoice(
									"Revealing hand", fullHand.toArray());
							CardList discard = new CardList(hand.getCards());
							discard = discard.filter(new CardListFilter() {
								public boolean addCard(Card c) {
									return CardUtil.getConvertedManaCost(c.getManaCost()) == converted;
								}
							});
							for(int j = 0; j < discard.size(); j++) {
								Card choice = discard.get(j);
								AllZone.GameAction.discard(choice);
							}
						} //resolve
					}; //ability
					ability2.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
					ability2.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability2));
					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a Spirit or Arcane spell, target player reveals his or her hand and discards all cards with converted mana cost "
							+ converted + ".");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Infernal Kirin

	public static void playCard_Cloudhoof_Kirin(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Cloudhoof Kirin");

		if(list.size() > 0) {
			if(c.getType().contains("Spirit") || c.getType().contains("Arcane")
					|| c.getIntrinsicKeyword().contains("Changeling")) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final int converted = CardUtil.getConvertedManaCost(c.getManaCost());
					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							final String target;
							if(card.getController().contains("Human")) {
								String[] choices = {"Opponent", "Yourself", "None"};
								Object choice = AllZone.Display.getChoice("Choose target player", choices);
								if(choice.equals("Opponent")) {
									target = "Computer"; // check for target of spell/abilities should be here
								}// if choice yes
								else if(!choice.equals("None")) target = "Human"; // check for target of spell/abilities should be here
								else target = "none";
							} else target = "Human"; // check for target of spell/abilities should be here						
							if(!(target.contains("none"))) {
								PlayerZone lib = AllZone.getZone(Constant.Zone.Library, target);
								PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, target);
								CardList libList = new CardList(lib.getCards());

								int max = converted;

								if(libList.size() < max) max = libList.size();

								for(int i = 0; i < max; i++) {
									Card c = libList.get(i);
									lib.remove(c);
									grave.add(c);
								}
							} //if
						} //resolve
					}; //ability
					ability2.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
					ability2.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability2));
					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a Spirit or Arcane spell, target player puts the top " + converted
							+ " cards of his or her library into his or her graveyard.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Cloudhoof Kirin

	public static void playCard_Bounteous_Kirin(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Bounteous Kirin");

		if(list.size() > 0) {
			if(c.getType().contains("Spirit") || c.getType().contains("Arcane")
					|| c.getIntrinsicKeyword().contains("Changeling")) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final int converted = CardUtil.getConvertedManaCost(c.getManaCost());
					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							final String target;
							if(card.getController().contains("Human")) {
								String[] choices = {"Yourself", "Opponent", "None"};
								Object choice = AllZone.Display.getChoice("Choose target player", choices);
								if(choice.equals("Opponent")) {
									target = "Computer"; // check for target of spell/abilities should be here
								}// if choice yes
								else if(!choice.equals("None")) target = "Human"; // check for target of spell/abilities should be here
								else target = "none";
							} else target = "Computer"; // check for target of spell/abilities should be here						
							if(!target.equals("none")) AllZone.GameAction.getPlayerLife(target).addLife(converted);
						} //resolve
					}; //ability
					ability2.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
					ability2.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability2));
					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a Spirit or Arcane spell, target player may gain " + converted + " life.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Bounteous Kirin


	public static void playCard_Shorecrasher_Mimic(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Shorecrasher Mimic");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Blue)
					&& CardUtil.getColors(c).contains(Constant.Color.Green)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(2);
								card.setBaseDefense(1);
								card.removeIntrinsicKeyword("Trample");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(5);
							card.setBaseDefense(3);
							if(!card.getIntrinsicKeyword().contains("Trample")) card.addIntrinsicKeyword("Trample");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a spell that�s both green and blue, it becomes 5/3 and gains trample until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Shorecrasher Mimic

	public static void playCard_Battlegate_Mimic(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Battlegate Mimic");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Red)
					&& CardUtil.getColors(c).contains(Constant.Color.White)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(2);
								card.setBaseDefense(1);
								card.removeIntrinsicKeyword("First Strike");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(4);
							card.setBaseDefense(2);
							if(!card.getIntrinsicKeyword().contains("First Strike")) card.addIntrinsicKeyword("First Strike");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a spell that�s both red and white, it becomes 4/2 and gains first strike until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Battlegate Mimic

	public static void playCard_Nightsky_Mimic(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Nightsky Mimic");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Black)
					&& CardUtil.getColors(c).contains(Constant.Color.White)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(2);
								card.setBaseDefense(1);
								card.removeIntrinsicKeyword("Flying");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(4);
							card.setBaseDefense(4);
							if(!card.getIntrinsicKeyword().contains("Flying")) card.addIntrinsicKeyword("Flying");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a spell that�s both black and white, it becomes 4/4 and gains flying until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Nightsky Mimic

	public static void playCard_Riverfall_Mimic(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Riverfall Mimic");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Blue)
					&& CardUtil.getColors(c).contains(Constant.Color.Red)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(2);
								card.setBaseDefense(1);
								card.removeIntrinsicKeyword("Unblockable");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(3);
							card.setBaseDefense(3);
							if(!card.getIntrinsicKeyword().contains("Unblockable")) card.addIntrinsicKeyword("Unblockable");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a spell that�s both red and blue, it becomes 3/3 and is unblockable until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Riverfall Mimic

	public static void playCard_Woodlurker_Mimic(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Woodlurker Mimic");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Black)
					&& CardUtil.getColors(c).contains(Constant.Color.Green)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);
					final Command untilEOT = new Command() {
						private static final long serialVersionUID = -4569751606008597903L;

						public void execute() {
							if(AllZone.GameAction.isCardInPlay(card)) {
								card.setBaseAttack(2);
								card.setBaseDefense(1);
								card.removeIntrinsicKeyword("Wither");

							}
						}
					};

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							card.setBaseAttack(4);
							card.setBaseDefense(5);
							if(!card.getIntrinsicKeyword().contains("Wither")) card.addIntrinsicKeyword("Wither");
							AllZone.EndOfTurn.addUntil(untilEOT);
						}
					}; // ability2

					ability2.setStackDescription(card.getName()
							+ " - "
							+ c.getController()
							+ " played a spell that�s both green and black, it becomes 4/5 and gains wither until end of turn.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

	}//Woodlurker Mimic


	public static void playCard_Dovescape(Card c) {
		final Card crd1 = c;
		PlayerZone hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(hplay.getCards());
		list.addAll(cplay.getCards());
		final int cmc = CardUtil.getConvertedManaCost(c.getManaCost());
		list = list.getName("Dovescape");
		final CardList cl = list;
		if(!c.getType().contains("Creature") && list.size() > 0) {


			final Card card = list.get(0);

			Ability ability2 = new Ability(card, "0") {
				@Override
				public void resolve() {

					SpellAbility sa = AllZone.Stack.peek();

					if(sa.getSourceCard().equals(crd1)) {
						sa = AllZone.Stack.pop();

						AllZone.GameAction.moveToGraveyard(sa.getSourceCard());

						for(int j = 0; j < cl.size() * cmc; j++) {
							CardFactoryUtil.makeToken("Bird", "WU 1 1 Bird", sa.getSourceCard().getController(), "W U", new String[] {
								"Creature", "Bird"}, 1, 1, new String[] {"Flying"});
						}

						/*
                        SpellAbility sa = AllZone.Stack.peek
                        if (!sa.getSourceCard().isCreature() && sa.isSpell())
                        {

                        }
						 */
					} else //TODO 
					{
						;
					}


				}
			}; // ability2

			ability2.setStackDescription("Dovescape Ability");
			AllZone.Stack.add(ability2);


		}
	} // Dovescape


	public static void playCard_Belligerent_Hatchling(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Belligerent Hatchling");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Red)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
						}

					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a red spell, remove a -1/-1 counter from Belligerent Hatchling.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.White)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
					}

				}; // ability

				ability.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a white spell, remove a -1/-1 counter from Belligerent Hatchling.");
				AllZone.Stack.add(ability);
			}
		}//if


	}// Belligerent Hatchling

	public static void playCard_Noxious_Hatchling(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Noxious Hatchling");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Black)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
						}

					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a black spell, remove a -1/-1 counter from Noxious Hatchling.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
					}

				}; // ability

				ability.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a green spell, remove a -1/-1 counter from Noxious Hatchling.");
				AllZone.Stack.add(ability);
			}
		}//if


	}// Noxious Hatchling

	public static void playCard_Sturdy_Hatchling(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Sturdy Hatchling");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
						}

					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a blue spell, remove a -1/-1 counter from Sturdy Hatchling.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
					}

				}; // ability

				ability.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a green spell, remove a -1/-1 counter from Sturdy Hatchling.");
				AllZone.Stack.add(ability);
			}
		}//if


	}// Sturdy Hatchling

	public static void playCard_Voracious_Hatchling(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Voracious Hatchling");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Black)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
						}

					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a black spell, remove a -1/-1 counter from Voracious Hatchling.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.White)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(card.getCounters(Counters.M1M1) > 0) card.subtractCounter(Counters.M1M1, 1);
					}

				}; // ability

				ability.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a white spell, remove a -1/-1 counter from Voracious Hatchling.");
				AllZone.Stack.add(ability);
			}
		}//if


	}// Voracious Hatchling

	public static void playCard_Witch_Maw_Nephilim(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Witch-Maw Nephilim");

		if(list.size() > 0) {

			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {

						if(card.getController().equals("Human")) {
							String[] choices = {"Yes", "No"};
							Object choice = AllZone.Display.getChoice("Put two +1/+1 on Witch-Maw Nephilim?",
									choices);
							if(choice.equals("Yes")) {
								card.addCounter(Counters.P1P1, 2);
							}
						}
						if(card.getController().equals("Computer")) {
							card.addCounter(Counters.P1P1, 2);
						}
					}

				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a spell, you may put two +1/+1 counters on Witch-Maw Nephilim.");
				AllZone.Stack.add(ability2);
			}
		}
	}// Witch-Maw Nephilim

	public static void playCard_Gelectrode(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Gelectrode");

		if(list.size() > 0 && (c.getType().contains("Instant") || c.getType().contains("Sorcery"))) {

			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {

						if(card.getController().equals("Human")) {
							String[] choices = {"Yes", "No"};
							Object choice = AllZone.Display.getChoice("Untap gelectrode?", choices);
							if(choice.equals("Yes")) {
								card.untap();
							}
						}
						if(card.getController().equals("Computer")) {
							card.untap();
						}
					}

				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played an instant or sorcery spell, you may untap Gelectrode.");
				AllZone.Stack.add(ability2);
			}
		}
	}// Gelectrode

	public static void playCard_Cinder_Pyromancer(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Cinder Pyromancer");

		if(list.size() > 0 && CardUtil.getColors(c).contains(Constant.Color.Red)) {

			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {

						if(card.getController().equals("Human")) {
							String[] choices = {"Yes", "No"};
							Object choice = AllZone.Display.getChoice("Untap Cinder Pyromancer?", choices);
							if(choice.equals("Yes")) {
								card.untap();
							}
						}
						if(card.getController().equals("Computer")) {
							card.untap();
						}
					}

				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a red spell, you may untap Cinder Pyromancer.");
				AllZone.Stack.add(ability2);
			}
		}
	}// Cinder_Pyromancer

	public static void playCard_Ballynock_Trapper(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Ballynock Trapper");

		if(list.size() > 0 && CardUtil.getColors(c).contains(Constant.Color.White)) {

			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {

						if(card.getController().equals("Human")) {
							String[] choices = {"Yes", "No"};
							Object choice = AllZone.Display.getChoice("Untap Ballynock Trapper?", choices);
							if(choice.equals("Yes")) {
								card.untap();
							}
						}
						if(card.getController().equals("Computer")) {
							card.untap();
						}
					}

				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a white spell, you may untap Ballynock Trapper.");
				AllZone.Stack.add(ability2);
			}
		}
	}// Ballynock Trapper


	public static void playCard_Forced_Fruition(Card c) {
		PlayerZone hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(hplay.getCards());
		list.addAll(cplay.getCards());

		list = list.getName("Forced Fruition");

		for(int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			final String drawer = AllZone.GameAction.getOpponent(card.getController());


			Ability ability2 = new Ability(card, "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);

				}
			}; // ability2
			if(!(card.getController().equals(c.getController()))) {
				ability2.setStackDescription(card.getName() + " - " + c.getController() + " played a spell, "
						+ drawer + " draws seven cards.");
				AllZone.Stack.add(ability2);
			}
		}

	}

	public static void playCard_Standstill(Card c) {
		PlayerZone hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(hplay.getCards());
		list.addAll(cplay.getCards());

		list = list.getName("Standstill");

		for(int i = 0; i < list.size(); i++) {
			final String drawer = AllZone.GameAction.getOpponent(c.getController());
			final Card card = list.get(i);

			Ability ability2 = new Ability(card, "0") {
				@Override
				public void resolve() {
					// sac standstill
					AllZone.GameAction.sacrifice(card);
					// player who didn't play spell, draws 3 cards
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
					AllZone.GameAction.drawCard(drawer);
				}
			}; // ability2

			ability2.setStackDescription(card.getName() + " - " + c.getController() + " played a spell, " + drawer
					+ " draws three cards.");
			AllZone.Stack.add(ability2);

		}

	}

	public static void playCard_Memory_Erosion(Card c) {
		PlayerZone hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(hplay.getCards());
		list.addAll(cplay.getCards());

		list = list.getName("Memory Erosion");

		for(int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			final String drawer = AllZone.GameAction.getOpponent(card.getController());


			Ability ability2 = new Ability(card, "0") {
				@Override
				public void resolve() {
					// sac standstill
					//            AllZone.GameAction.sacrifice(card);
					// player who didn't play spell, draws 3 cards
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, drawer);
					PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, drawer);
					CardList libList = new CardList(lib.getCards());

					int max = 2;
					if(libList.size() < 2) max = libList.size();

					for(int i = 0; i < max; i++) {
						Card c = libList.get(i);
						lib.remove(c);
						grave.add(c);
					}

				}
			}; // ability2
			if(!(card.getController().equals(c.getController()))) {
				ability2.setStackDescription(card.getName() + " - " + c.getController() + " played a spell, "
						+ drawer + " puts the top two cards of his or her library into his or her graveyard.");
				AllZone.Stack.add(ability2);

			}
		}
	}

	public static void playCard_SolKanar(Card c) {
		PlayerZone hplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cplay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(hplay.getCards());
		list.addAll(cplay.getCards());

		list = list.getName("Sol'kanar the Swamp King");

		if(list.size() > 0 && CardUtil.getColors(c).contains(Constant.Color.Black)) {
			final Card card = list.get(0);
			final String controller = card.getController();

			Ability ability2 = new Ability(card, "0") {
				@Override
				public void resolve() {
					PlayerLife life = AllZone.GameAction.getPlayerLife(controller);
					life.addLife(1);
				}
			}; // ability2

			ability2.setStackDescription(card.getName() + " - " + c.getController() + " played a black spell, "
					+ card.getController() + " gains 1 life.");
			AllZone.Stack.add(ability2);
		}

	}

	public static void playCard_Enchantress_Draw(Card c) {

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.filter(new CardListFilter() {
			public boolean addCard(Card crd) {
				if(crd.getName().equals("Verduran Enchantress") || crd.getName().equals("Enchantress's Presence")
						|| crd.getName().equals("Mesa Enchantress")
						|| crd.getName().equals("Argothian Enchantress")) return true;
				else return false;
			}
		});
		//list = list.getName("Verduran Enchantress");

		if(c.isEnchantment()) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						// draws a card
						AllZone.GameAction.drawCard(card.getController());
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " plays an enchantment spell and draws a card");
				AllZone.Stack.add(ability2);

			} // for
		}// if isEnchantment()
	}

	public static void playCard_Gilt_Leaf_Archdruid(Card c) {

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Gilt-Leaf Archdruid");
		if(c.getType().contains("Druid") || c.getKeyword().contains("Changeling")) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						// draws a card
						AllZone.GameAction.drawCard(card.getController());
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " plays a Druid spell and draws a card");
				AllZone.Stack.add(ability2);

			} // for
		}// if druid
	}

	public static void playCard_Reki(Card c) {
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Reki, the History of Kamigawa");
		if(c.getType().contains("Legendary")) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						// draws a card
						AllZone.GameAction.drawCard(card.getController());
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " plays a Legendary spell and draws a card");
				AllZone.Stack.add(ability2);

			} // for
		}// if legendary
	}

	public static void playCard_Vedalken_Archmage(Card c) {

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Vedalken Archmage");
		if(c.getType().contains("Artifact")) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						// draws a card
						AllZone.GameAction.drawCard(card.getController());
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " plays an Artifact spell and draws a card");
				AllZone.Stack.add(ability2);

			} // for
		}// if artifact
	}

	public static void playCard_Sigil_of_the_Empty_Throne(Card c) {

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Sigil of the Empty Throne");
		if(c.isEnchantment()) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						CardFactoryUtil.makeToken("Angel", "W 4 4 Angel", card, "W", new String[] {
								"Creature", "Angel"}, 4, 4, new String[] {"Flying"});
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " puts a 4/4 White Angel token with flying into play.");
				AllZone.Stack.add(ability2);

			} // for
		}// if isEnchantment()
	}

	public static void playCard_Merrow_Levitator(Card c) {
		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());


		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Merrow Levitator");

		if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						if(card.isTapped()) card.untap();
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + " untaps");
				AllZone.Stack.add(ability2);

			} // for
		}// if is blue spell
	}//merrow levitator

	public static void playCard_Primordial_Sage(Card c) {

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Primordial Sage");
		if(c.getType().contains("Creature")) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(0);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						// draws a card
						AllZone.GameAction.drawCard(card.getController());
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " plays a Creature spell and draws a card");
				AllZone.Stack.add(ability2);

			} // for
		}// if Creature
	}//primordial sage

	public static void playCard_Quirion_Dryad(Card c) {
		String controller = c.getController();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Quirion Dryad");

		if(list.size() > 0
				&& (CardUtil.getColors(c).contains(Constant.Color.White)
						|| CardUtil.getColors(c).contains(Constant.Color.Blue)
						|| CardUtil.getColors(c).contains(Constant.Color.Black) || CardUtil.getColors(c).contains(
								Constant.Color.Red))) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						card.addCounter(Counters.P1P1, 1);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a white, blue, black or red spell,  " + " gets a +1/+1 counter.");
				AllZone.Stack.add(ability2);
			}
		}

	}//Quirion

	public static void playCard_Mold_Adder(Card c) {
		String opponent = AllZone.GameAction.getOpponent(c.getController());

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, opponent);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Mold Adder");

		if(list.size() > 0
				&& (CardUtil.getColors(c).contains(Constant.Color.Blue) || CardUtil.getColors(c).contains(
						Constant.Color.Black))) {
			for(int i = 0; i < list.size(); i++) {

				final Card card = list.get(i);

				Ability ability2 = new Ability(card, "0") {
					@Override
					public void resolve() {
						card.addCounter(Counters.P1P1, 1);
					}
				}; // ability2

				ability2.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a blue or black spell,  " + card.getName() + " gets a +1/+1 counter.");
				AllZone.Stack.add(ability2);

			}
		}

	}//Quirion

	public static void playCard_Fable_of_Wolf_and_Owl(Card c) {
		final String controller = c.getController();

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Fable of Wolf and Owl");

		if(list.size() > 0) {
			if(CardUtil.getColors(c).contains(Constant.Color.Blue)) {
				for(int i = 0; i < list.size(); i++) {
					final Card card = list.get(i);

					Ability ability2 = new Ability(card, "0") {
						@Override
						public void resolve() {
							CardFactoryUtil.makeToken("Bird", "U 1 1 Bird", card, "U", new String[] {
									"Creature", "Bird"}, 1, 1, new String[] {"Flying"});
						}
					}; // ability2

					ability2.setStackDescription(card.getName() + " - " + c.getController()
							+ " played a blue spell, put a 1/1 blue Bird token with flying into play.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}

		if(CardUtil.getColors(c).contains(Constant.Color.Green)) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);

				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						CardFactoryUtil.makeToken("Wolf", "G 2 2 Wolf", card, "G", new String[] {
								"Creature", "Wolf"}, 2, 2, new String[] {""});
					}
				}; // ability

				ability.setStackDescription(card.getName() + " - " + c.getController()
						+ " played a green spell, put a 2/2 green Wolf token into play.");
				AllZone.Stack.add(ability);
			}
		}//if
	}//Fable

	public static void playCard_Kor_Firewalker(Card c) {

		final PlayerZone play = AllZone.getZone(Constant.Zone.Play,
				Constant.Player.Human);
		final PlayerZone comp = AllZone.getZone(Constant.Zone.Play,
				Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(play.getCards());
		list.addAll(comp.getCards());

		list = list.getName("Kor Firewalker");

		if (list.size() > 0){
			ArrayList<String> cl=CardUtil.getColors(c);
			if (cl.contains(Constant.Color.Red))
			{
				for (int i=0;i<list.size();i++)
				{
					final Card card = list.get(i);                  
					Ability ability2 = new Ability(card, "0")
					{
						public void resolve()
						{
							AllZone.GameAction.getPlayerLife(card.getController()).addLife(1);                      
						} //resolve
					};   //ability
					ability2.setStackDescription(card.getName() + " - "
							+ c.getController() + " played a Red spell," + card.getController()+" gains 1 life.");
					AllZone.Stack.add(ability2);
				}
			}//if
		}                   

	}//Kor Firewalker
	
	public static void playCard_Curse_of_Wizardry(final Card c) {
		CardList list = AllZoneUtil.getCardsInPlay("Curse of Wizardry");

		if(list.size() > 0){
			ArrayList<String> cl=CardUtil.getColors(c);
			
				for (int i=0;i<list.size();i++) {
					final Card card = list.get(i);
					if(cl.contains(card.getChosenColor())) {
					Ability ability = new Ability(card, "0") {
						public void resolve() {
							AllZone.GameAction.getPlayerLife(c.getController()).subtractLife(1);                      
						} //resolve
					};//ability
					ability.setStackDescription(card.getName() + " - " + c.getController() + 
							" played a "+card.getChosenColor()+" spell, " + c.getController()+" loses 1 life.");
					AllZone.Stack.add(ability);
				}
			}//if
		}//if
	}//Curse of Wizardry


	public static void executeDrawCardTriggeredEffects(String player) {
		drawCardTriggered_Niv_Mizzet(player);
		drawCardTriggered_Hoofprints_of_the_Stag(player);
		drawCardTriggered_Lorescale_Coatl(player);
		drawCardTriggered_Underworld_Dreams(player);
	}

	public static void drawCardTriggered_Underworld_Dreams(String player) {
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(player));
		CardList list = new CardList(playZone.getCards());
		final String player_d = player;
		list = list.getName("Underworld Dreams");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			final Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {

					AllZone.GameAction.getPlayerLife(player_d).subtractLife(1);
				}
			};
			ability.setStackDescription(list.get(i) + " - Deals 1 damage to him or her");
			AllZone.Stack.add(ability);

		}
	}

	public static void drawCardTriggered_Lorescale_Coatl(String player) {
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());

		list = list.getName("Lorescale Coatl");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			c.addCounter(Counters.P1P1, 1);
		}
	}

	public static void drawCardTriggered_Niv_Mizzet(String player) {
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());

		final String controller = player;

		list = list.getName("Niv-Mizzet, the Firemind");

		if(list.size() > 0) {
			final Card crd = list.get(0);
			final Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void chooseTargetAI() {

					CardList cards = CardFactoryUtil.AI_getHumanCreature(1, crd, false);
					cards.shuffle();

					if(cards.isEmpty() || AllZone.Human_Life.getLife() < 5) setTargetPlayer(Constant.Player.Human);
					else setTargetCard(cards.get(0));
				}

				@Override
				public void resolve() {
					if(controller.equals("Human")) {
						String opp = AllZone.GameAction.getOpponent(controller);
						PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, opp);

						String[] choices = {"Yes", "No, target a creature instead"};

						Object q = AllZone.Display.getChoiceOptional("Select computer as target?", choices);
						if(q.equals("Yes")) AllZone.GameAction.getPlayerLife(Constant.Player.Computer).subtractLife(
								1);
						else {
							CardList cards = new CardList(oppPlay.getCards());
							CardList oppCreatures = new CardList();
							for(int i = 0; i < cards.size(); i++) {
								if(cards.get(i).isPlaneswalker() || cards.get(i).isCreature()
										&& CardFactoryUtil.canTarget(crd, cards.get(i))) {
									oppCreatures.add(cards.get(i));
								}
							}

							if(oppCreatures.size() > 0) {
								Object o = AllZone.Display.getChoiceOptional("Pick target creature",
										oppCreatures.toArray());
								Card c = (Card) o;
								c.addDamage(1, crd);
							}
						}
					}

					else {

						if(getTargetCard() != null) {
							if(AllZone.GameAction.isCardInPlay(getTargetCard())
									&& CardFactoryUtil.canTarget(crd, getTargetCard())) getTargetCard().addDamage(
											1, crd);
						} else AllZone.GameAction.getPlayerLife(getTargetPlayer()).subtractLife(1);
					}
				}//resolve()
			};//SpellAbility

			ability.setStackDescription(list.get(0) + " - Deals 1 damage to target creature or player");
			AllZone.Stack.add(ability);


		}//if

	}

	public static void drawCardTriggered_Hoofprints_of_the_Stag(String player) {
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());

		list = list.getName("Hoofprints of the Stag");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			c.addCounter(Counters.HOOFPRINT, 1);
		}
	}

	//UPKEEP CARDS:

	public static void upkeep_removeDealtDamageToOppThisTurn() {
		// resets the status of attacked/blocked this turn
		String player = AllZone.Phase.getActivePlayer();
		String opp = AllZone.GameAction.getOpponent(player);
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, opp);

		CardList list = new CardList();
		list.addAll(play.getCards());
		list = list.getType("Creature");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if(c.getDealtCombatDmgToOppThisTurn()) c.setDealtCombatDmgToOppThisTurn(false);
			if(c.getDealtDmgToOppThisTurn()) c.setDealtDmgToOppThisTurn(false);
		}
	}

	public static void upkeep_TabernacleUpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith(
							"At the beginning of your upkeep, destroy this creature unless you pay")) {
						String k[] = a.get(i).toString().split("pay ");
						k[1] = k[1].substring(0, k[1].length() - 1);
						c.setTabernacleUpkeepCost(k[1]);
						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability destroyAbility = new Ability(c, c.getTabernacleUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {

				private static final long serialVersionUID = -8737736216222268696L;

				public void execute() {
					AllZone.GameAction.destroy(c);
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -1832975152887536245L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Tabernacle Upkeep for " + c + "\r\n",
						destroyAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(destroyAbility)) ComputerUtil.playNoStack(destroyAbility);
				else AllZone.GameAction.destroy(c);
			}
		}
	}//TabernacleUpkeepCost

	public static void upkeep_MagusTabernacleUpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith(
							"At the beginning of your upkeep, sacrifice this creature unless you pay")) {
						String k[] = a.get(i).toString().split("pay ");
						k[1] = k[1].substring(0, k[1].length() - 1);
						c.setMagusTabernacleUpkeepCost(k[1]);
						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability sacrificeAbility = new Ability(c, c.getMagusTabernacleUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {
				private static final long serialVersionUID = 660060621665783254L;

				public void execute() {
					AllZone.GameAction.sacrifice(c);
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = 7896720208740364774L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Magus of the Tabernacle Upkeep for "
						+ c + "\r\n", sacrificeAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(sacrificeAbility)) ComputerUtil.playNoStack(sacrificeAbility);
				else AllZone.GameAction.sacrifice(c);
			}
		}
	}//MagusTabernacleUpkeepCost

	public static void upkeep_CumulativeUpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith("Cumulative upkeep")) {
						String k[] = a.get(i).toString().split(":");
						c.addCounter(Counters.AGE, 1);
						String upkeepCost = CardFactoryUtil.multiplyManaCost(k[1], c.getCounters(Counters.AGE));
						c.setUpkeepCost(upkeepCost);
						System.out.println("Multiplied cost: " + upkeepCost);
						//c.setUpkeepCost(k[1]);
						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability sacAbility = new Ability(c, c.getUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {

				private static final long serialVersionUID = -8737736216222268696L;

				public void execute() {
					AllZone.GameAction.sacrifice(c);
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -1832975152887536245L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Upkeep for " + c + "\r\n",
						sacAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(sacAbility)) ComputerUtil.playNoStack(sacAbility);
				else AllZone.GameAction.sacrifice(c);
			}
		}
	}//upkeepCost

	public static void upkeep_Echo() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return c.getKeyword().contains("(Echo unpaid)");
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);
			if(c.getIntrinsicKeyword().contains("(Echo unpaid)")) {

				final Ability sacAbility = new Ability(c, c.getEchoCost()) {
					@Override
					public void resolve() {
						System.out.println("Echo cost for " + c + " was paid.");
					}
				};

				final Command unpaidCommand = new Command() {
					private static final long serialVersionUID = -7354791599039157375L;

					public void execute() {
						AllZone.GameAction.sacrifice(c);
					}
				};

				final Command paidCommand = new Command() {
					private static final long serialVersionUID = 4549981408026393913L;

					public void execute() {
						;
					}
				};

				//AllZone.Stack.add(sacAbility);
				if(c.getController().equals(Constant.Player.Human)) {
					AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Echo for " + c + "\r\n",
							sacAbility.getManaCost(), paidCommand, unpaidCommand));
				} else //computer
				{
					if(ComputerUtil.canPayCost(sacAbility)) ComputerUtil.playNoStack(sacAbility);
					else AllZone.GameAction.sacrifice(c);
				}

				c.removeIntrinsicKeyword("(Echo unpaid)");
			}
		}
	}//echo


	public static void upkeep_UpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith("At the beginning of your upkeep, sacrifice " + c.getName()) ||
							a.get(i).toString().startsWith("At the beginning of your upkeep, sacrifice CARDNAME")) {
						String k[] = a.get(i).toString().split(":");
						c.setUpkeepCost(k[1]);
						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability sacAbility = new Ability(c, c.getUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {

				private static final long serialVersionUID = -6483405139208343935L;

				public void execute() {
					AllZone.GameAction.sacrifice(c);
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -8303368287601871955L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Upkeep for " + c + "\r\n",
						sacAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(sacAbility)) ComputerUtil.playNoStack(sacAbility);
				else AllZone.GameAction.sacrifice(c);
			}
		}
	}//upkeepCost

	public static void upkeep_DestroyUpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith("At the beginning of your upkeep, destroy CARDNAME")) {
						String k[] = a.get(i).toString().split(":");
						c.setUpkeepCost(k[1]);
						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability sacAbility = new Ability(c, c.getUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {
				private static final long serialVersionUID = 8942537892273123542L;

				public void execute() {
					if(c.getName().equals("Cosmic Horror")) {
						String player = c.getController();
						PlayerLife life = AllZone.GameAction.getPlayerLife(player);
						life.subtractLife(7);
					}
					AllZone.GameAction.destroy(c);
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -8462246567257483700L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Upkeep for " + c + "\r\n",
						sacAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(sacAbility)) ComputerUtil.playNoStack(sacAbility);
				else AllZone.GameAction.destroy(c);
			}
		}
	}//upkeepCost


	public static void upkeep_DamageUpkeepCost() {
		String player = AllZone.Phase.getActivePlayer();

		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList();
		list.addAll(play.getCards());
		//list = list.getType("Creature");
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				ArrayList<String> a = c.getKeyword();
				for(int i = 0; i < a.size(); i++) {
					if(a.get(i).toString().startsWith(
							"At the beginning of your upkeep, CARDNAME deals ")) {
						String k[] = a.get(i).toString().split("deals ");
						String s1 = k[1].substring(0, 2);
						s1 = s1.trim();
						c.setUpkeepDamage(Integer.parseInt(s1));
						System.out.println(k[1]);
						String l[] = k[1].split("pay:");
						System.out.println(l[1]);
						c.setUpkeepCost(l[1]);

						return true;
					}
				}
				return false;
			}
		});

		for(int i = 0; i < list.size(); i++) {
			final Card c = list.get(i);

			final Ability sacAbility = new Ability(c, c.getUpkeepCost()) {
				@Override
				public void resolve() {
					;
				}
			};

			final Command unpaidCommand = new Command() {
				private static final long serialVersionUID = 8942537892273123542L;

				public void execute() {
					//AllZone.GameAction.sacrifice(c);
					String player = c.getController();
					PlayerLife life = AllZone.GameAction.getPlayerLife(player);
					life.subtractLife(c.getUpkeepDamage());
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -8462246567257483700L;

				public void execute() {
					;
				}
			};

			//AllZone.Stack.add(sacAbility);
			if(c.getController().equals(Constant.Player.Human)) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Upkeep for " + c + "\r\n",
						sacAbility.getManaCost(), paidCommand, unpaidCommand));
			} else //computer
			{
				if(ComputerUtil.canPayCost(sacAbility)) ComputerUtil.playNoStack(sacAbility);
				else AllZone.GameAction.sacrifice(c);
			}
		}
	}//damageUpkeepCost
	
	/**
	 * runs the upkeep for Genesis
	 */
	public static void upkeep_Genesis() {
		/*
		 * At the beginning of your upkeep, if Genesis is in your graveyard,
		 * you may pay 2G. If you do, return target creature card from your 
		 * graveyard to your hand.
		 */
		final String player = AllZone.Phase.getActivePlayer();
		final CardList grave = AllZoneUtil.getPlayerGraveyard(player, "Genesis");

		for(int i = 0; i < grave.size(); i++) {
			final Card c = grave.get(i);

			final Ability ability = new Ability(c, "2 G") {
				CardListFilter creatureFilter = new CardListFilter() {
					public boolean addCard(Card c) {
						return c.isCreature();
					}
				};
				@Override
				public void resolve() {
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
					PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
					if(player.equals("Human") && grave.size() > 0) {
							CardList creatures = AllZoneUtil.getPlayerGraveyard(player);
							creatures = creatures.filter(creatureFilter);
							Object creatureChoice = AllZone.Display.getChoice("Creature to move to hand", creatures.toArray());
							Card creatureCard = (Card) creatureChoice;
	                        graveyard.remove(creatureCard);
	                        hand.add(creatureCard);
						//}//end choice="Yes"
					}
					else{ //computer resolve
						CardList compCreatures = AllZoneUtil.getPlayerGraveyard(player);
						compCreatures = compCreatures.filter(creatureFilter);
						Card target = CardFactoryUtil.AI_getBestCreature(compCreatures);
						graveyard.remove(target);
                        hand.add(target);
					}
				}
			};

			final Command unpaidCommand = new Command() {
				private static final long serialVersionUID = 8969863703446141914L;

				public void execute() {
					;
				}
			};

			final Command paidCommand = new Command() {
				private static final long serialVersionUID = -5102763277280782548L;

				public void execute() {
					ability.setStackDescription(c.getName()+" - return 1 creature from your graveyard to your hand");
					AllZone.Stack.add(ability);
				}
			};

			//AllZone.Stack.add(ability);
			if(c.getController().equals(Constant.Player.Human)) {
				String[] choices = {"Yes", "No"};
				Object choice = AllZone.Display.getChoice("Use Genesis?", choices);
				if(choice.equals("Yes")) {
				AllZone.InputControl.setInput(new Input_PayManaCost_Ability("Pay cost for " + c + "\r\n",
						ability.getManaCost(), paidCommand, unpaidCommand));
				}
			} else //computer
			{
				if(ComputerUtil.canPayCost(ability)) {
					ability.setStackDescription(c.getName()+" - return 1 creature from your graveyard to your hand");
					AllZone.Stack.add(ability);
				}
			}
		}
	}//upkeep_Genesis

	//END UPKEEP CARDS

	//START ENDOFTURN CARDS

	public static void endOfTurn_Wall_Of_Reverence()
	{
		final String player = AllZone.Phase.getActivePlayer();
		final PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());
		list = list.getName("Wall of Reverence");

		Ability ability;
		for (int i = 0; i < list.size(); i++)
		{
			final Card card = list.get(i);
			ability = new Ability(list.get(i), "0")
			{
				public void resolve()
				{
					CardList creats = new CardList(playZone.getCards());
					CardList validTargets = new CardList();
					creats = creats.getType("Creature");
					for (int i = 0; i < creats.size(); i++) {
						if (CardFactoryUtil.canTarget(card, creats.get(i))) {
							validTargets.add(creats.get(i));
						}
					}
					if (validTargets.size() == 0)
						return;

					if (player.equals(Constant.Player.Human))
					{
						Object o = AllZone.Display.getChoiceOptional("Select creature for Wall of Reverence life gain", validTargets.toArray());
						if (o != null) {
							Card c = (Card) o;
							int power=c.getNetAttack();
							PlayerLife life = AllZone.GameAction.getPlayerLife(player);
							life.addLife(power);
						}
					}
					else//computer
					{
						CardListUtil.sortAttack(validTargets);
						Card c = creats.get(0);
						if (c != null) {
							int power = c.getNetAttack();
							PlayerLife life = AllZone.GameAction.getPlayerLife(player);
							life.addLife(power);
						}
					}
				} // resolve
			}; // ability
			ability.setStackDescription("Wall of Reverence - "
					+ player + " gains life equal to target creature's power.");
			AllZone.Stack.add(ability);
		}
	}
	
	public static void endOfTurn_Lighthouse_Chronologist() 
	{
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);
		final PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, opponent);
		CardList list = new CardList(playZone.getCards());
		
		list = list.filter(new CardListFilter()
		{
			public boolean addCard(Card c)
			{
				return c.getName().equals("Lighthouse Chronologist") && c.getCounters(Counters.LEVEL) >= 7;
			}
		});
		
		Ability ability;
		for (int i = 0; i < list.size(); i++)
		{
			final Card card = list.get(i);
			ability = new Ability(list.get(i), "0")
			{
				public void resolve()
				{
					 AllZone.Phase.addExtraTurn(card.getController());
				}
			};
			ability.setStackDescription(card + " - " +card.getController() + " takes an extra turn.");
			AllZone.Stack.add(ability);
		}
	}


	//END ENDOFTURN CARDS

	public static void removeAttackedBlockedThisTurn() {
		// resets the status of attacked/blocked this turn
		String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());
		list = list.getType("Creature");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if(c.getCheckedPropagandaThisTurn()) c.setCheckedPropagandaThisTurn(false);
			if(c.getCreatureAttackedThisCombat()) c.setCreatureAttackedThisCombat(false);
			if(c.getCreatureBlockedThisCombat()) c.setCreatureBlockedThisCombat(false);

			if(c.getCreatureGotBlockedThisCombat()) c.setCreatureGotBlockedThisCombat(false);

			c.resetReceivedDamageFromThisTurn();
		}
	}


	/*
    public static void executeExaltedEffects2(Card c, Combat combats)
    {
    	boolean exalted = false;
    	//Card[] attackers = AllZone.Combat.getAttackers();
    	int attackersDeclared = AllZone.Combat.getDeclaredAttackers() + AllZone.pwCombat.getDeclaredAttackers();
    	//System.out.println("declared attackers: " + attackersDeclared);
    	int numberOfAttackers = AllZone.Combat.getAttackers().length + AllZone.pwCombat.getAttackers().length;

    	// fetch all cards in play controlled by attacking player, find card
    	// with exalted
    	String attackingPlayer = AllZone.Combat.getAttackingPlayer();

    	PlayerZone play = AllZone.getZone(Constant.Zone.Play, attackingPlayer);

    	CardList cards = new CardList(play.getCards());

    	CardList exaltedCreatures = new CardList();

    	for (int i = 0; i < cards.size(); i++)
    	{
    		Card crd = cards.get(i);
    		if (crd.getKeyword().contains("Exalted"))
    		{
    			exalted = true;
    			exaltedCreatures.add(c);
    		}
    	}
    	// scenarios:
    	// 1. creature has no exalted bonus, no exalted in play -> nothing
    	// happens
    	// 2. creature has no exalted bonus, exalted in play, only attacking
    	// creature -> give exalted
    	// 3. creature has no exalted bonus, exalted in play, more attacking
    	// creatures -> nothing happens
    	// 4. creature has exalted bonus, no exalted in play -> remove exalted
    	// 5. creature has exalted bonus, exalted in play, only attacking
    	// creature -> nothing happens
    	// 6. creature has exalted bonus, exalted in play, more attacking
    	// creatures -> remove exalted

    	//int power = c.getNetAttack();
    	//int toughness = c.getNetDefense();

    	if (c.hasExaltedBonus())
    	{
    		if (exalted == false)
    		{
    			c.setExaltedBonus(false);

    			c.addTempAttackBoost(-c.getExaltedMagnitude());
    			c.addTempDefenseBoost(-c.getExaltedMagnitude());

    			if (isRafiqInPlay(c.getController()))
    				c.removeExtrinsicKeyword("Double Strike");

    			int battleGraceAngels = getBattleGraceAngels(c.getController());
    			for (int j = 0; j < battleGraceAngels; j++)
    			{
    				c.removeExtrinsicKeyword("Lifelink");
    			}
    		} else
    		// exalted in play == true
    		{
    			if (numberOfAttackers > 1 || attackersDeclared > 1)
    			{
    				c.setExaltedBonus(false);

    				c.addTempAttackBoost(-c.getExaltedMagnitude());
    				c.addTempDefenseBoost(-c.getExaltedMagnitude());

    				if (isRafiqInPlay(c.getController()))
    					c.removeExtrinsicKeyword("Double Strike");

    				int battleGraceAngels = getBattleGraceAngels(c
    						.getController());
    				for (int j = 0; j < battleGraceAngels; j++)
    				{
    					c.removeExtrinsicKeyword("Lifelink");
    				}
    			}
    		}
    	} else
    	// no exaltedBonus on creature
    	{
    		if (exalted == true && numberOfAttackers == 1 || attackersDeclared == 1)
    		{
    			c.setExaltedBonus(true);
    			c.setExaltedMagnitude(exaltedCreatures.size());

    			c.addTempAttackBoost(c.getExaltedMagnitude());
    			c.addTempDefenseBoost(c.getExaltedMagnitude());

    			if (isRafiqInPlay(c.getController()))
    				c.addExtrinsicKeyword("Double Strike");

    			int battleGraceAngels = getBattleGraceAngels(c.getController());
    			for (int j = 0; j < battleGraceAngels; j++)
    			{
    				//System.out.println("adding instance of lifelink.");
    				c.addExtrinsicKeyword("Lifelink");
    			}
    			//System.out.println("keywordsize: " +c.getKeywordSize());
    		}
    	}

    }
	 */
	/*
    public static void removeExaltedEffects() // at EOT
    {
    	PlayerZone playerZone = AllZone.getZone(Constant.Zone.Play,
    			Constant.Player.Human);
    	PlayerZone computerZone = AllZone.getZone(Constant.Zone.Play,
    			Constant.Player.Computer);

    	CardList cards = new CardList();
    	cards.addAll(playerZone.getCards());
    	cards.addAll(computerZone.getCards());

    	for (int i = 0; i < cards.size(); i++)
    	{
    		Card crd = cards.get(i);

    		//int power = crd.getNetAttack();
    		//int toughness = crd.getNetDefense();

    		if (crd.hasExaltedBonus())
    		{
    			crd.setExaltedBonus(false);
    			crd.addTempAttackBoost(-crd.getExaltedMagnitude());
    			crd.addTempDefenseBoost(-crd.getExaltedMagnitude());

    			if (isRafiqInPlay(crd.getController()))
    				crd.removeExtrinsicKeyword("Double Strike");

    			int battleGraceAngels = getBattleGraceAngels(crd
    					.getController());
    			for (int j = 0; j < battleGraceAngels; j++)
    			{
    				crd.removeExtrinsicKeyword("Lifelink");
    			}
    		}
    	}
    }
	 */
	public static boolean isRafiqInPlay(String player) {
		PlayerZone playerZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList cards = new CardList();
		cards.addAll(playerZone.getCards());

		cards = cards.getName("Rafiq of the Many");

		if(cards.size() >= 1) // should only be 1, since Rafiq is Legendary
			return true;
		else return false;

	}

	public static int getBattleGraceAngels(String player) {
		PlayerZone playerZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList cards = new CardList();
		cards.addAll(playerZone.getCards());

		cards = cards.getName("Battlegrace Angel");

		return cards.size();
	}

	public static int countFinestHours(String controller) {
		PlayerZone playerZone = AllZone.getZone(Constant.Zone.Play, controller);

		CardList cards = new CardList();
		cards.addAll(playerZone.getCards());

		cards = cards.getName("Finest Hour");

		return cards.size();
	}


	public static void executeAllyEffects(Card c) {
		if(c.getName().equals("Kazandu Blademaster") || c.getName().equals("Makindi Shieldmate")
				|| c.getName().equals("Nimana Sell-Sword") || c.getName().equals("Oran-Rief Survivalist")
				|| c.getName().equals("Tuktuk Grunts") || c.getName().equals("Umara Raptor")
				|| c.getName().equals("Hada Freeblade") || c.getName().equals("Bojuka Brigand")
				|| c.getName().equals("Graypelt Hunter")) ally_Generic_P1P1(c);
		else if(c.getName().equals("Turntimber Ranger")) ally_Turntimber_Ranger(c);
		else if(c.getName().equals("Highland Berserker")) ally_BoostUntilEOT(c, "First Strike");
		else if(c.getName().equals("Joraga Bard")) ally_BoostUntilEOT(c, "Vigilance");
		else if(c.getName().equals("Seascape Aerialist")) ally_BoostUntilEOT(c, "Flying");
		else if(c.getName().equals("Ondu Cleric")) ally_Ondu_Cleric(c);
		else if(c.getName().equals("Kazuul Warlord")) ally_Kazuul_Warlord(c);

	}

	private static boolean showAllyDialog(Card c) {
		String[] choices = {"Yes", "No"};

		Object q = null;

		q = AllZone.Display.getChoiceOptional("Use " + c.getName() + "'s Ally ability?", choices);

		if(q == null || q.equals("No")) return false;
		else return true;
	}

	private static void ally_Generic_P1P1(Card c) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				crd.addCounter(Counters.P1P1, 1);
			}
		};
		ability.setStackDescription(c.getName() + " - Ally: gets a +1/+1 counter.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showAllyDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}

	private static void ally_Turntimber_Ranger(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardFactoryUtil.makeToken("Wolf", "G 2 2 Wolf", crd, "G", new String[] {"Creature", "Wolf"}, 2, 2,
						new String[] {""});
				crd.addCounter(Counters.P1P1, 1);
			}
		};

		ability.setStackDescription(c.getName() + " - Ally: " + c.getController()
				+ " puts a 2/2 green Wolf creature token onto the battlefield, and adds a +1/+1 on " + c.getName()
				+ ".");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showAllyDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) {

			PlayerZone cPlay = AllZone.Computer_Play;
			CardList list = new CardList();
			list.addAll(cPlay.getCards());

			CardList cl = list.filter(new CardListFilter() {
				public boolean addCard(Card crd) {
					return crd.getName().equals("Conspiracy") && crd.getChosenType().equals("Ally");
				}
			});

			list = list.getName("Wolf");

			if((list.size() > 15 && cl.size() > 0)) ;
			else AllZone.Stack.add(ability);
		}
	}

	private static void ally_BoostUntilEOT(Card c, String k) {
		final Card crd = c;
		final String keyword = k;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone play = AllZone.getZone(Constant.Zone.Play, crd.getController());
				CardList list = new CardList(play.getCards());
				list = list.getType("Ally");

				final CardList allies = list;

				final Command untilEOT = new Command() {

					private static final long serialVersionUID = -8434529949884582940L;

					public void execute() {
						for(Card creat:allies) {
							if(AllZone.GameAction.isCardInPlay(creat)) {
								creat.removeExtrinsicKeyword(keyword);
							}
						}
					}
				};//Command

				for(Card creat:allies) {
					if(AllZone.GameAction.isCardInPlay(creat)) {
						creat.addExtrinsicKeyword(keyword);
					}
				}
				AllZone.EndOfTurn.addUntil(untilEOT);

			}
		};

		ability.setStackDescription(c.getName() + " - Ally: Ally creatures you control gain " + keyword
				+ " until end of turn.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showAllyDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}

	private static void ally_Ondu_Cleric(Card c) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone play = AllZone.getZone(Constant.Zone.Play, crd.getController());
				CardList allies = new CardList(play.getCards());
				allies = allies.getType("Ally");
				PlayerLife life = AllZone.GameAction.getPlayerLife(crd.getController());
				life.addLife(allies.size());

			}
		};

		ability.setStackDescription(c.getName() + " - Ally: gain life equal to the number of allies you control.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showAllyDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}

	private static void ally_Kazuul_Warlord(Card c) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone play = AllZone.getZone(Constant.Zone.Play, crd.getController());
				CardList list = new CardList(play.getCards());
				list = list.getType("Ally");

				for(Card ally:list) {
					ally.addCounter(Counters.P1P1, 1);
				}
			}
		};
		ability.setStackDescription(c.getName()
				+ " - Ally: put a +1/+1 counter on each Ally creature you control.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showAllyDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}


	public static void executeDestroyCardEffects(Card c, Card destroyed) {
		if(destroyed.isCreature()) executeDestroyCreatureCardEffects(c, destroyed);
		if(destroyed.isLand()) executeDestroyLandCardEffects(c, destroyed);
		if(destroyed.isEnchantment()) executeDestroyEnchantmentCardEffects(c, destroyed);
	}

	private static boolean showDialog(Card c) {
		String[] choices = {"Yes", "No"};

		Object q = null;

		q = AllZone.Display.getChoiceOptional("Use " + c.getName() + " effect?", choices);

		if(q == null || q.equals("No")) return false;
		else return true;
	}

	//***CREATURES START HERE***

	public static void executeDestroyCreatureCardEffects(Card c, Card destroyed) {
		//if (AllZone.GameAction.isCardInPlay(c)){
		if(c.getName().equals("Goblin Sharpshooter")) destroyCreature_Goblin_Sharpshooter(c, destroyed);
		else if(c.getName().equals("Dingus Staff")) destroyCreature_Dingus_Staff(c, destroyed);
		else if(c.getName().equals("Dauthi Ghoul") && destroyed.getKeyword().contains("Shadow")) destroyCreature_Dauthi_Ghoul(
				c, destroyed);
		else if(c.getName().equals("Soulcatcher") && destroyed.getKeyword().contains("Flying")) destroyCreature_Soulcatcher(
				c, destroyed);
		else if(c.getName().equals("Prowess of the Fair") && destroyed.getType().contains("Elf")
				&& !destroyed.isToken() && !c.equals(destroyed)
				&& destroyed.getController().equals(c.getController())) destroyCreature_Prowess_of_the_Fair(c,
						destroyed);
		else if(c.getName().equals("Fecundity")) destroyCreature_Fecundity(c, destroyed);
		else if(c.getName().equals("Moonlit Wake")) destroyCreature_Moonlit_Wake(c, destroyed);
		else if(c.getName().equals("Proper Burial") && destroyed.getController().equals(c.getController())) destroyCreature_Proper_Burial(
				c, destroyed);
		else if(c.getName().equals("Sek'Kuar, Deathkeeper") && !destroyed.isToken()
				&& destroyed.getController().equals(c.getController()) && !destroyed.getName().equals(c.getName())) destroyCreature_SekKuar(
						c, destroyed);
		//}
	}

	//***

	private static void destroyCreature_Goblin_Sharpshooter(Card c, Card destroyed) {
		//not using stack for this one
		if(AllZone.GameAction.isCardInPlay(c) && c.isTapped()) c.untap();
	}

	private static void destroyCreature_Dingus_Staff(Card c, Card destroyed) {
		final Card crd = destroyed;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.subtractLife(2);
			}
		};
		ability.setStackDescription("Dingus Staff - Deals 2 damage to " + destroyed.getController() + ".");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Dauthi_Ghoul(Card c, Card destroyed) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				if(AllZone.GameAction.isCardInPlay(crd)) crd.addCounter(Counters.P1P1, 1);
			}
		};
		if(AllZone.GameAction.isCardInPlay(c)) ability.setStackDescription("Dauthi Ghoul - gets a +1/+1 counter.");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Soulcatcher(Card c, Card destroyed) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				if(AllZone.GameAction.isCardInPlay(crd)) crd.addCounter(Counters.P1P1, 1);
			}
		};

		ability.setStackDescription("Soulcatcher - gets a +1/+1 counter.");
		if(AllZone.GameAction.isCardInPlay(c)) AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Prowess_of_the_Fair(Card c, Card destroyed) {
		final Card crd = c;
		final Card crd2 = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				if(player.equals(Constant.Player.Human)) {
					if(showDialog(crd2)) makeToken();
				} else makeToken();
			}

			public void makeToken() {
				CardFactoryUtil.makeToken("Elf Warrior", "G 1 1 Elf Warrior", crd, "G", new String[] {
						"Creature", "Elf", "Warrior"}, 1, 1, new String[] {""});
			}
		};
		ability.setStackDescription("Prowess of the Fair - " + c.getController()
				+ " may put a 1/1 green Elf Warrior creature token onto the battlefield.");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Fecundity(Card c, Card destroyed) {
		final Card crd = destroyed;
		final Card crd2 = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				if(player.equals(Constant.Player.Human)) {
					if(showDialog(crd2)) AllZone.GameAction.drawCard(player);
				} else AllZone.GameAction.drawCard(player); //computer
			}
		};
		ability.setStackDescription("Fecundity - " + destroyed.getController() + " may draw a card.");

		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Moonlit_Wake(Card c, Card destroyed) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.addLife(1);
			}
		};
		ability.setStackDescription("Moonlit Wake - " + c.getController() + " gains 1 life.");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Proper_Burial(Card c, Card destroyed) {
		final Card crd = c;
		final Card crd2 = destroyed;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.addLife(crd2.getNetDefense());
			}
		};
		ability.setStackDescription("Proper Burial - " + c.getController() + " gains " + destroyed.getNetDefense()
				+ " life.");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_SekKuar(Card c, Card destroyed) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardFactoryUtil.makeToken("Graveborn", "BR 3 1 Graveborn", crd, "BR", new String[] {
						"Creature", "Graveborn"}, 3, 1, new String[] {"Haste"});
			}
		};
		ability.setStackDescription("Sek'Kuar, Deathkeeper - put a 3/1 black and red Graveborn creature token with haste onto the battlefield.");
		AllZone.Stack.add(ability);
	}

	//***CREATURES END HERE***

	//***LANDS START HERE***

	public static void executeDestroyLandCardEffects(Card c, Card destroyed) {
		if(c.getName().equals("Dingus Egg")) destroyLand_Dingus_Egg(c, destroyed);
	}

	//***

	private static void destroyLand_Dingus_Egg(Card c, Card destroyed) {
		final Card crd = destroyed;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.subtractLife(2);
			}
		};
		ability.setStackDescription("Dingus Egg - Deals 2 damage to " + destroyed.getController() + ".");
		AllZone.Stack.add(ability);
	}

	public static void executeGrvDestroyCardEffects(Card c, Card destroyed) {
		if(c.getName().contains("Bridge from Below") && destroyed.getController().equals(c.getController())
				&& !destroyed.isToken()) destroyCreature_Bridge_from_Below_maketoken(c, destroyed);
		if(c.getName().contains("Bridge from Below") && !destroyed.getController().equals(c.getController())) destroyCreature_Bridge_from_Below_remove(c);
	}

	private static void destroyCreature_Bridge_from_Below_maketoken(Card c, Card destroyed) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardFactoryUtil.makeToken("Zombie", "B 2 2 Zombie", crd, "B", new String[] {"Creature", "Zombie"},
						2, 2, new String[] {""});
			}
		};
		ability.setStackDescription("Bridge from Below - " + c.getController()
				+ "puts a 2/2 black Zombie creature token onto the battlefield.");
		AllZone.Stack.add(ability);
	}

	private static void destroyCreature_Bridge_from_Below_remove(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone grv = AllZone.getZone(Constant.Zone.Graveyard, crd.getController());
				PlayerZone exile = AllZone.getZone(Constant.Zone.Removed_From_Play, crd.getController());
				grv.remove(crd);
				exile.add(crd);
			}
		};
		ability.setStackDescription("Bridge from Below - " + c.getController() + " exile Bridge from Below.");
		AllZone.Stack.add(ability);
	}


	//***LANDS END HERE***

	//***ENCHANTMENTS START HERE***

	public static void executeDestroyEnchantmentCardEffects(Card c, Card destroyed) {
		if(c.getName().equals("Femeref Enchantress")) destroyEnchantment_Femeref_Enchantress(c, destroyed);
	}


	//***

	public static void destroyEnchantment_Femeref_Enchantress(Card c, Card destroyed) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = crd.getController();
				AllZone.GameAction.drawCard(player);
			}
		};
		ability.setStackDescription("Femeref Enchantress - " + c.getController() + " draws a card.");

		AllZone.Stack.add(ability);
	}

	//***ENCHANTMENTS END HERE***

	public static void executeLandfallEffects(Card c) {
		
		ArrayList<String> kws = c.getKeyword();
		for (String kw : kws){
			if (kw.equals("Landfall - Whenever a land enters the battlefield under your control, CARDNAME gets +2/+2 until end of turn."))
			landfall_Generic_P2P2_UntilEOT(c);
		}

		if(c.getName().equals("Rampaging Baloths")) landfall_Rampaging_Baloths(c);
		else if(c.getName().equals("Emeria Angel")) landfall_Emeria_Angel(c);
		else if(c.getName().equals("Ob Nixilis, the Fallen")) landfall_Ob_Nixilis(c);
		else if(c.getName().equals("Ior Ruin Expedition")
				|| c.getName().equals("Khalni Heart Expedition")) landfall_AddQuestCounter(c);
		else if(c.getName().equals("Lotus Cobra")) landfall_Lotus_Cobra(c);
		else if(c.getName().equals("Hedron Crab")) landfall_Hedron_Crab(c);
		else if(c.getName().equals("Bloodghast")) landfall_Bloodghast(c);
		else if(c.getName().equals("Avenger of Zendikar")) landfall_Avenger_of_Zendikar(c);
	}

	private static boolean showLandfallDialog(Card c) {
		String[] choices = {"Yes", "No"};

		Object q = null;

		q = AllZone.Display.getChoiceOptional("Use " + c.getName() + " Landfall?", choices);

		if(q == null || q.equals("No")) return false;
		else return true;
	}

	private static void landfall_Generic_P2P2_UntilEOT(Card c)
	{
		final Card crd = c;
		Ability ability = new Ability(c, "0")
		{
			@Override
			public void resolve()
			{
				final Command untilEOT = new Command() {
					private static final long serialVersionUID = 8919719388859986796L;

					public void execute() {
						if(AllZone.GameAction.isCardInPlay(crd)) {
							crd.addTempAttackBoost(-2);
							crd.addTempDefenseBoost(-2);
						}
					}
				};
				crd.addTempAttackBoost(2);
				crd.addTempDefenseBoost(2);

				AllZone.EndOfTurn.addUntil(untilEOT);
			}
		};
		ability.setStackDescription(c + " - Landfall: gets +2/+2 until EOT.");

		/*if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer))*/
		AllZone.Stack.add(ability);
	}

	private static void landfall_Rampaging_Baloths(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardFactoryUtil.makeToken("Beast", "G 4 4 Beast", crd, "G", new String[] {"Creature", "Beast"}, 4,
						4, new String[] {""});
			}
		};

		ability.setStackDescription(c.getName() + " - Landfall: " + c.getController()
				+ " puts a 4/4 green Beast creature token onto the battlefield.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);

	}

	private static void landfall_Emeria_Angel(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardFactoryUtil.makeToken("Bird", "W 1 1 Bird", crd, "W", new String[] {"Creature", "Bird"}, 1, 1,
						new String[] {"Flying"});
			}
		};

		ability.setStackDescription(c.getName() + " - Landfall: " + c.getController()
				+ " puts a 1/1 white Bird creature token with flying onto the battlefield.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}//landfall_Emeria_Angel

	private static void landfall_Ob_Nixilis(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerLife life = AllZone.GameAction.getPlayerLife(AllZone.GameAction.getOpponent(crd.getController()));
				life.subtractLife(3);
				crd.addCounter(Counters.P1P1, 3);
			}
		};

		ability.setStackDescription("Landfall: " + AllZone.GameAction.getOpponent(c.getController())
				+ " loses 3 life and " + c.getName() + " gets three +1/+1 counters.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		} else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}

	private static void landfall_AddQuestCounter(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				crd.addCounter(Counters.QUEST, 1);
			}
		};

		ability.setStackDescription(c.getName() + " - gets a Quest counter.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) AllZone.Stack.add(ability);
	}

	private static void landfall_Lotus_Cobra(Card c) {
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String color = "";

				Object o = AllZone.Display.getChoice("Choose mana color", Constant.Color.Colors);
				color = (String) o;

				if(color.equals("white")) color = "W";
				else if(color.equals("blue")) color = "U";
				else if(color.equals("black")) color = "B";
				else if(color.equals("red")) color = "R";
				else if(color.equals("green")) color = "G";

				//CardList list = new CardList(AllZone.getZone(Constant.Zone.Play, Constant.Player.Human).getCards());
				//list = list.getName("Mana Pool");
				Card mp = AllZone.ManaPool;//list.getCard(0);

				mp.addExtrinsicKeyword("ManaPool:" + color);
			}
		};

		ability.setStackDescription(c.getName() + " - add one mana of any color to your mana pool.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		}

	}

	private static void landfall_Hedron_Crab(Card c) {
		//final Card crd = c;
		final Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				String player = getTargetPlayer();

				PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
				PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
				CardList libList = new CardList(lib.getCards());

				int max = 3;
				if(libList.size() < 3) max = libList.size();

				for(int i = 0; i < max; i++) {
					Card c = libList.get(i);
					lib.remove(c);
					grave.add(c);
				}
			}
		};

		//ability.setStackDescription(c.getName() + " - Landfall: " + c.getController() + "  puts the top three cards of his or her library into his or her graveyard.");
		//ability.setBeforePayMana(CardFactoryUtil.input_targetPlayer(ability));

		if(c.getController().equals(Constant.Player.Human)) {
			AllZone.InputControl.setInput(CardFactoryUtil.input_targetPlayer(ability));
			//AllZone.Stack.add(ability);
		}

		else if(c.getController().equals(Constant.Player.Computer)) {
			ability.setChooseTargetAI(CardFactoryUtil.AI_targetHuman());
			AllZone.Stack.add(ability);

		}
	}//landfall_Hedron_Crab

	private static void landfall_Bloodghast(Card c) {
		PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, c.getController());
		if(!AllZone.GameAction.isCardInZone(c, grave)) return;

		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone play = AllZone.getZone(Constant.Zone.Play, crd.getController());
				PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, crd.getController());

				if(AllZone.GameAction.isCardInZone(crd, grave)) {
					grave.remove(crd);
					play.add(crd);
				}
			}
		};

		ability.setStackDescription(c + " - return Bloodghast from your graveyard to the battlefield.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		} else if(c.getController().equals(Constant.Player.Computer)) {
			AllZone.Stack.add(ability);
		}

		AllZone.GameAction.checkStateEffects();

	}//landfall_Bloodghast

	private static void landfall_Avenger_of_Zendikar(Card c) {
		final Card crd = c;
		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerZone play = AllZone.getZone(Constant.Zone.Play, crd.getController());
				CardList plants = new CardList(play.getCards());
				plants = plants.filter(new CardListFilter() {
					public boolean addCard(Card card) {
						return card.isCreature() && card.getType().contains("Plant") || 
						card.getKeyword().contains("Changeling");
					}
				});

				for(Card plant:plants)
					plant.addCounter(Counters.P1P1, 1);
			}
		};
		ability.setStackDescription(c + " - put a +1/+1 counter on each Plant creature you control.");

		if(c.getController().equals(Constant.Player.Human)) {
			if(showLandfallDialog(c)) AllZone.Stack.add(ability);
		} else if(c.getController().equals(Constant.Player.Computer)) {
			AllZone.Stack.add(ability);
		}

	}//landfall_Avenger

	public static void executeLifeLinkEffects(Card c) {
		final String player = c.getController();
		int pwr = c.getNetAttack();
		if(CombatUtil.isDoranInPlay()) pwr = c.getNetDefense();

		final int power = pwr;

		Ability ability2 = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.addLife(power);
			}
		}; // ability2

		ability2.setStackDescription(c.getName() + " (Lifelink) - " + player + " gains " + power + " life.");
		AllZone.Stack.add(ability2);
	}

	public static void executeLifeLinkEffects(Card c, int n) {
		final String player = c.getController();

		final int power = n;

		Ability ability2 = new Ability(c, "0") {
			@Override
			public void resolve() {
				PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				life.addLife(power);
			}
		}; // ability2

		ability2.setStackDescription(c.getName() + " (Lifelink) - " + player + " gains " + power + " life.");
		AllZone.Stack.add(ability2);
	}

	public static void executeGuiltyConscienceEffects(Card c, Card source) {
		int pwr = c.getNetAttack();
		if(CombatUtil.isDoranInPlay()) pwr = c.getNetDefense();
		final int damage = pwr;
		final Card src = source;

		final Card crd = c;
		Ability ability2 = new Ability(c, "0") {
			@Override
			public void resolve() {
				crd.addDamage(damage, src);
			}
		}; // ability2

		ability2.setStackDescription("Guilty Conscience deals " + damage + " damage to " + c.getName());
		AllZone.Stack.add(ability2);
	}

	public static void executeGuiltyConscienceEffects(Card c, Card source, int n) {
		final int damage = n;
		final Card crd = c;
		final Card src = source;
		Ability ability2 = new Ability(c, "0") {
			@Override
			public void resolve() {
				crd.addDamage(damage, src);
			}
		}; // ability2

		ability2.setStackDescription("Guilty Conscience deals " + n + " damage to " + c.getName());
		AllZone.Stack.add(ability2);
	}

	//this is for cards like Sengir Vampire
	public static void executeVampiricEffects(Card c) {
		ArrayList<String> a = c.getKeyword();
		for(int i = 0; i < a.size(); i++) {
			if(AllZone.GameAction.isCardInPlay(c)
					&& a.get(i).toString().startsWith(
							"Whenever a creature dealt damage by this card this turn is put into a graveyard, put")) {
				final Card thisCard = c;
				Ability ability2 = new Ability(c, "0") {
					@Override
					public void resolve() {
						if(AllZone.GameAction.isCardInPlay(thisCard)) thisCard.addCounter(Counters.P1P1, 1);
					}
				}; // ability2

				ability2.setStackDescription(c.getName() + " - gets a +1/+1 counter");
				AllZone.Stack.add(ability2);
			}

		}
	}

	public static void executePlayerCombatDamageEffects(Card c) {

		if(c.getKeyword().contains("Whenever this creature deals damage to a player, that player gets a poison counter.")) 
			playerCombatDamage_PoisonCounter(c, 1);

		if (c.getKeyword().contains("Poisonous 1"))
		{
			final Card crd = c;
			Ability ability = new Ability(c, "0")
			{
				public void resolve()
				{
					final String player = crd.getController();
					final String opponent = AllZone.GameAction.getOpponent(player);

					if(opponent.equals(Constant.Player.Human)) 
						AllZone.Human_PoisonCounter.addPoisonCounters(1);
					else
						AllZone.Computer_PoisonCounter.addPoisonCounters(1);
				}
			};

			StringBuilder sb = new StringBuilder();
			sb.append(c);
			sb.append(" - Poisonous 1: ");
			sb.append(AllZone.GameAction.getOpponent(c.getController()));
			sb.append(" gets a poison counter.");

			ability.setStackDescription(sb.toString());
			ArrayList<String> keywords = c.getKeyword();

			for (int i=0;i<keywords.size();i++)
			{
				AllZone.Stack.add(ability);
			}
		}

		if(c.getName().equals("Marsh Viper")) playerCombatDamage_PoisonCounter(c, 2);
		else if(c.getName().equals("Hypnotic Specter")) playerCombatDamage_Hypnotic_Specter(c);
		else if(c.getName().equals("Dimir Cutpurse")) playerCombatDamage_Dimir_Cutpurse(c);
		else if(c.getName().equals("Ghastlord of Fugue")) playerCombatDamage_Ghastlord_of_Fugue(c);
		else if(c.getName().equals("Garza Zol, Plague Queen")) playerCombatDamage_May_draw(c);
		else if(CardFactoryUtil.hasNumberEquipments(c, "Mask of Riddles") > 0 && c.getNetAttack() > 0) {
			for(int k = 0; k < CardFactoryUtil.hasNumberEquipments(c, "Mask of Riddles"); k++) {
				playerCombatDamage_May_draw(c);
			}
		} else if(CardFactoryUtil.hasNumberEquipments(c, "Quietus Spike") > 0 && c.getNetAttack() > 0) {
			for(int k = 0; k < CardFactoryUtil.hasNumberEquipments(c, "Quietus Spike"); k++) {
				playerCombatDamage_lose_halflife_up(c);
			}
		} else if(c.getName().equals("Scalpelexis")) playerCombatDamage_Scalpelexis(c);
		else if(c.getName().equals("Blazing Specter") || c.getName().equals("Guul Draz Specter")
				|| c.getName().equals("Chilling Apparition") || c.getName().equals("Sedraxis Specter")) playerCombatDamage_Simple_Discard(c);
		else if((c.getName().equals("Headhunter") || c.getName().equals("Riptide Pilferer")) && !c.isFaceDown()) playerCombatDamage_Simple_Discard(c);
		else if(c.getName().equals("Shadowmage Infiltrator") || c.getName().equals("Thieving Magpie")
				|| c.getName().equals("Lu Xun, Scholar General")) playerCombatDamage_Shadowmage_Infiltrator(c);
		else if(c.getName().equals("Nicol Bolas")) playerCombatDamage_Nicol_Bolas(c);
		else if(c.getName().equals("Goblin Lackey")) playerCombatDamage_Goblin_Lackey(c);
		else if(c.getName().equals("Augury Adept")) playerCombatDamage_Augury_Adept(c);
		else if(c.getName().equals("Warren Instigator")) playerCombatDamage_Warren_Instigator(c);
		else if(c.getName().equals("Spawnwrithe")) playerCombatDamage_Spawnwrithe(c);
		else if(c.getName().equals("Glint-Eye Nephilim") || c.getName().equals("Cold-Eyed Selkie")) playerCombatDamage_Glint_Eye_Nephilim(c);
		else if(c.getName().equals("Hystrodon") && !c.isFaceDown()) playerCombatDamage_Hystrodon(c);
		else if(c.getName().equals("Raven Guild Master") && !c.isFaceDown()) playerCombatDamage_Raven_Guild_Master(c);
		else if(c.getName().equals("Slith Strider") || c.getName().equals("Slith Ascendant")
				|| c.getName().equals("Slith Bloodletter") || c.getName().equals("Slith Firewalker")
				|| c.getName().equals("Slith Predator")) playerCombatDamage_Slith(c);
		else if(c.getName().equals("Whirling Dervish") || c.getName().equals("Dunerider Outlaw")) 
			playerCombatDamage_Whirling_Dervish(c);
		else if (c.getName().equals("Arcbound Slith"))
			playerCombatDamage_Arcbound_Slith(c);
		else if(c.getName().equals("Oros, the Avenger")) playerCombatDamage_Oros(c);
		else if(c.getName().equals("Rootwater Thief")) playerCombatDamage_Rootwater_Thief(c);
		else if(c.getName().equals("Treva, the Renewer")) playerCombatDamage_Treva(c);
		else if(c.getName().equals("Rith, the Awakener")) playerCombatDamage_Rith(c);
		else if(c.getName().equals("Vorosh, the Hunter")) playerCombatDamage_Vorosh(c);

		if(c.getNetAttack() > 0) c.setDealtCombatDmgToOppThisTurn(true);

	}

	/*
    public static void executePlayerCombatDmgOptionalEffects(Card[] c)
    {
    	for (int i=0;i< c.length;i++)
    	{
    		if (c[i].getName().equals("Treva, the Renewer"))
    		{
    			SpellAbility[] sa = c[i].getSpellAbility();
    			if (c[i].getController().equals(Constant.Player.Human))
    				AllZone.GameAction.playSpellAbility(sa[1]);
    			else
    				ComputerUtil.playNoStack(sa[1]);
    		}

    		else if (c[i].getName().equals("Rootwater Thief"))
    		{
    			SpellAbility[] sa = c[i].getSpellAbility();
    			if (c[i].getController().equals(Constant.Player.Human))
    				AllZone.GameAction.playSpellAbility(sa[2]); //because sa[1] is the kpump u: flying
    			else
    				ComputerUtil.playNoStack(sa[2]);
    		}


    	}//for
    }
	 */

	private static void playerCombatDamage_PoisonCounter(Card c, int n) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(opponent.equals(Constant.Player.Human)) AllZone.Human_PoisonCounter.addPoisonCounters(n);
		else AllZone.Computer_PoisonCounter.addPoisonCounters(n);
	}

	private static void playerCombatDamage_Oros(Card c) {
		SpellAbility[] sa = c.getSpellAbility();
		if(c.getController().equals(Constant.Player.Human)) AllZone.GameAction.playSpellAbility(sa[1]);
		else ComputerUtil.playNoStack(sa[1]);
	}

	private static void playerCombatDamage_Dimir_Cutpurse(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {

					AllZone.GameAction.drawCard(player);
					if(opponent.equals(Constant.Player.Human)) AllZone.InputControl.setInput(CardFactoryUtil.input_discard());
					else AllZone.GameAction.discardRandom(Constant.Player.Computer);

				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + player + " draws a card, opponent discards a card");
			AllZone.Stack.add(ability2);
		}

	}

	private static void playerCombatDamage_Ghastlord_of_Fugue(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					Card choice = null;

					//check for no cards in hand on resolve
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, opponent);
					PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, opponent);
					Card[] handChoices = removeLand(hand.getCards());

					if(handChoices.length == 0) return;

					//human chooses
					if(opponent.equals(Constant.Player.Computer)) {
						choice = AllZone.Display.getChoice("Choose", handChoices);
					} else//computer chooses
					{
						choice = CardUtil.getRandom(handChoices); // wise choice should be here
					}

					hand.remove(choice);
					exiled.add(choice);
				}//resolve()

				@Override
				public boolean canPlayAI() {
					Card[] c = removeLand(AllZone.Human_Hand.getCards());
					return 0 < c.length;
				}

				Card[] removeLand(Card[] in) {
					return in;
				}//removeLand() 
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + "opponent discards a card.");
			AllZone.Stack.add(ability2);
		}
	} //Ghastlord of Fugue


	private static void playerCombatDamage_Rootwater_Thief(Card c) {
		SpellAbility[] sa = c.getSpellAbility();
		if(c.getController().equals(Constant.Player.Human)) AllZone.GameAction.playSpellAbility(sa[2]); //because sa[1] is the kpump u: flying
		else ComputerUtil.playNoStack(sa[2]);


	}

	private static void playerCombatDamage_Treva(Card c) {
		SpellAbility[] sa = c.getSpellAbility();
		if(c.getController().equals(Constant.Player.Human)) AllZone.GameAction.playSpellAbility(sa[1]);
		else ComputerUtil.playNoStack(sa[1]);

	}

	private static void playerCombatDamage_Rith(Card c) {
		SpellAbility[] sa = c.getSpellAbility();
		if(c.getController().equals(Constant.Player.Human)) AllZone.GameAction.playSpellAbility(sa[1]);
		else ComputerUtil.playNoStack(sa[1]);
	}

	private static void playerCombatDamage_Vorosh(Card c) {
		SpellAbility[] sa = c.getSpellAbility();
		if(c.getController().equals(Constant.Player.Human)) AllZone.GameAction.playSpellAbility(sa[1]);
		else ComputerUtil.playNoStack(sa[1]);
	}

	private static void playerCombatDamage_Slith(Card c) {
		final int power = c.getNetAttack();
		final Card card = c;

		if(power > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					card.addCounter(Counters.P1P1, 1);
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - gets a +1/+1 counter.");
			AllZone.Stack.add(ability2);
		} // if
	}

	private static void playerCombatDamage_Whirling_Dervish(Card c) {
		final int power = c.getNetAttack();
		final Card card = c;

		if(power > 0) {
			final Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					card.addCounter(Counters.P1P1, 1);
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - gets a +1/+1 counter.");

			Command dealtDmg = new Command() {
				private static final long serialVersionUID = 2200679209414069339L;

				public void execute() {
					AllZone.Stack.add(ability2);
				}
			};
			AllZone.EndOfTurn.addAt(dealtDmg);

		} // if
	}

	private static void playerCombatDamage_Arcbound_Slith(Card c) {
		final int power = c.getNetAttack();
		final Card card = c;

		if(power > 0) {
			final Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					card.addCounter(Counters.P1P1, 1);
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - gets a +1/+1 counter.");

			AllZone.Stack.add(ability2);

		} // if
	}

	private static void playerCombatDamage_Raven_Guild_Master(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, opponent);
					PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, opponent);
					CardList libList = new CardList(lib.getCards());

					int max = 10;
					if(libList.size() < 10) max = libList.size();

					for(int i = 0; i < max; i++) {
						Card c = libList.get(i);
						lib.remove(c);
						exiled.add(c);
					}
				}
			};// ability

			ability.setStackDescription("Raven Guild Master - " + opponent
					+ " removes the top ten cards of his or her library from the game");
			AllZone.Stack.add(ability);
		}
	}

	private static void playerCombatDamage_May_draw(Card c) {
		final String player = c.getController();

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					if(player.equals("Human")) {
						String[] choices = {"Yes", "No"};
						Object choice = AllZone.Display.getChoice("Draw a card?", choices);
						if(choice.equals("Yes")) {
							AllZone.GameAction.drawCard(player);
						}
					}
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
					CardList libList = new CardList(lib.getCards());
					if(player.equals("Computer") && (libList.size() > 3)) AllZone.GameAction.drawCard(player);
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + player + " may draw a card.");
			AllZone.Stack.add(ability2);
		}

	}

	private static void playerCombatDamage_lose_halflife_up(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					int x = 0;
					int y = 0;
					if(player == "Human") {
						y = (AllZone.Computer_Life.getLife() % 2);
						if(!(y == 0)) y = 1;
						else y = 0;

						x = (AllZone.Computer_Life.getLife() / 2) + y;
					} else {
						y = (AllZone.Human_Life.getLife() % 2);
						if(!(y == 0)) y = 1;
						else y = 0;

						x = (AllZone.Human_Life.getLife() / 2) + y;
					}
					AllZone.GameAction.getPlayerLife(opponent).subtractLife(x);

				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + opponent
					+ " loses half his or her life, rounded up.");
			AllZone.Stack.add(ability2);
		}

	}


	private static void playerCombatDamage_Simple_Discard(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {

					if(opponent.equals(Constant.Player.Human)) AllZone.InputControl.setInput(CardFactoryUtil.input_discard());
					else AllZone.GameAction.discardRandom(Constant.Player.Computer); // Should be changed to wise discard  

				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + "opponent discards a card.");
			AllZone.Stack.add(ability2);
		}
	}

	private static void playerCombatDamage_Scalpelexis(Card c) {
		final String player = c.getController();
		final String opponent = AllZone.GameAction.getOpponent(player);

		if(c.getNetAttack() > 0) {
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {

					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, opponent);
					PlayerZone exiled = AllZone.getZone(Constant.Zone.Removed_From_Play, opponent);
					CardList libList = new CardList(lib.getCards());
					int count = 0;
					int broken = 0;
					for(int i = 0; i < libList.size(); i = i + 4) {
						Card c1 = null;
						Card c2 = null;
						Card c3 = null;
						Card c4 = null;
						if(i < libList.size()) c1 = libList.get(i);
						else broken = 1;
						if(i + 1 < libList.size()) c2 = libList.get(i + 1);
						else broken = 1;
						if(i + 2 < libList.size()) c3 = libList.get(i + 2);
						else broken = 1;
						if(i + 3 < libList.size()) c4 = libList.get(i + 3);
						else broken = 1;
						if(broken == 0) {
							if((c1.getName().contains(c2.getName()) || c1.getName().contains(c3.getName())
									|| c1.getName().contains(c4.getName()) || c2.getName().contains(c3.getName())
									|| c2.getName().contains(c4.getName()) || c3.getName().contains(c4.getName()))) {
								count = count + 1;
							} else {
								broken = 1;
							}
						}

					}
					count = (count * 4) + 4;
					int max = count;
					if(libList.size() < count) max = libList.size();

					for(int j = 0; j < max; j++) {
						Card c = libList.get(j);
						lib.remove(c);
						exiled.add(c);
					}
				}
			};// ability

			ability.setStackDescription("Scalpelexis - "
					+ opponent
					+ " removes the top four cards of his or her library from the game.  If two or more of those cards have the same name, repeat this process.");
			AllZone.Stack.add(ability);
		}
	}

	private static void playerCombatDamage_Hystrodon(Card c) {
		final String player = c.getController();
		final int power = c.getNetAttack();

		if(power > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.drawCard(player);
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + player + " draws a card.");
			AllZone.Stack.add(ability2);
		} // if

	}

	private static void playerCombatDamage_Glint_Eye_Nephilim(Card c) {
		final String player = c.getController();
		final int power = c.getNetAttack();

		if(power > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					for(int i = 0; i < power; i++) {
						AllZone.GameAction.drawCard(player);
					}
				}
			};// ability2

			ability2.setStackDescription(c.getName() + " - " + player + " draws " + power + " card(s).");
			AllZone.Stack.add(ability2);
		} // if

	}

	private static void playerCombatDamage_Spawnwrithe(Card c) {
		final String player = c.getController();
		final Card crd = c;

		Ability ability2 = new Ability(c, "0") {
			@Override
			public void resolve() {
				CardList cl = CardFactoryUtil.makeToken("Spawnwrithe", "", crd, "2 G", new String[] {
						"Creature", "Elemental"}, 2, 2, new String[] {"Trample"});

				for(Card c:cl) {
					c.setText("Whenever Spawnwrithe deals combat damage to a player, put a token into play that's a copy of Spawnwrithe.");
					c.setCopiedToken(true);
				}
			}
		};// ability2

		ability2.setStackDescription(c.getName() + " - " + player + " puts copy into play.");
		AllZone.Stack.add(ability2);
	}

	private static void playerCombatDamage_Goblin_Lackey(Card c) {
		if(c.getNetAttack() > 0) {
			final Card card = c;
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
					PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());

					CardList goblins = new CardList(hand.getCards());
					//goblins = goblins.getType("Goblin");
					goblins = goblins.filter(new CardListFilter() {

						public boolean addCard(Card c) {
							return (c.getType().contains("Goblin") || c.getKeyword().contains("Changeling"))
							&& c.isPermanent();
						}

					});

					if(goblins.size() > 0) {
						if(card.getController().equals(Constant.Player.Human)) {
							Object o = AllZone.Display.getChoiceOptional("Select a Goblin to put into play",
									goblins.toArray());

							if(o != null) {
								Card gob = (Card) o;
								hand.remove(gob);
								play.add(gob);
							}
						} else {
							Card gob = goblins.get(0);
							hand.remove(gob);
							play.add(gob);
						}
					}
				}
			};
			ability2.setStackDescription(c.getName() + " - " + c.getController()
					+ " puts a goblin into play from his or her hand.");
			AllZone.Stack.add(ability2);
		}
	}

	private static void playerCombatDamage_Warren_Instigator(Card c) {
		if(c.getNetAttack() > 0) {
			final Card card = c;
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, card.getController());
					PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());

					CardList goblins = new CardList(hand.getCards());
					//goblins = goblins.getType("Goblin");
					goblins = goblins.filter(new CardListFilter() {

						public boolean addCard(Card c) {
							return (c.getType().contains("Goblin") || c.getKeyword().contains("Changeling"))
							&& c.isCreature();
						}

					});

					if(goblins.size() > 0) {
						if(card.getController().equals(Constant.Player.Human)) {
							Object o = AllZone.Display.getChoiceOptional("Select a Goblin to put into play",
									goblins.toArray());

							if(o != null) {
								Card gob = (Card) o;
								hand.remove(gob);
								play.add(gob);
							}
						} else {
							Card gob = goblins.get(0);
							hand.remove(gob);
							play.add(gob);
						}
					}
				}
			};
			ability2.setStackDescription(c.getName() + " - " + c.getController()
					+ " puts a goblin into play from his or her hand.");
			AllZone.Stack.add(ability2);
		}
	}//warren instigator

	private static void playerCombatDamage_Nicol_Bolas(Card c) {
		final String[] opp = new String[1];
		final Card crd = c;

		if(c.getNetAttack() > 0) {
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {
					opp[0] = AllZone.GameAction.getOpponent(crd.getController());
					AllZone.GameAction.discardHand(opp[0]);
				}
			};
			opp[0] = AllZone.GameAction.getOpponent(c.getController());
			ability.setStackDescription(c.getName() + " - " + opp[0] + " discards his or her hand.");
			AllZone.Stack.add(ability);
		}
	}//nicol bolas

	private static void playerCombatDamage_Shadowmage_Infiltrator(Card c) {
		//String player = c.getController();
		final String[] player = new String[1];
		final Card crd = c;


		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					player[0] = crd.getController();
					AllZone.GameAction.drawCard(player[0]);
				}
			};// ability2

			player[0] = c.getController();
			ability2.setStackDescription(c.getName() + " - " + player[0] + " draws a card.");
			AllZone.Stack.add(ability2);
		}

	}

	private static void playerCombatDamage_Augury_Adept(Card c) {
		final String[] player = new String[1];
		final Card crd = c;

		if(c.getNetAttack() > 0) {
			Ability ability2 = new Ability(c, "0") {
				@Override
				public void resolve() {
					player[0] = crd.getController();
					PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player[0]);
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player[0]);
					if(lib.size() > 0) {
						CardList cl = new CardList();
						cl.add(lib.get(0));
						AllZone.Display.getChoiceOptional("Top card", cl.toArray());
					};
					Card top = lib.get(0);
					AllZone.GameAction.getPlayerLife(player[0]).addLife(
							CardUtil.getConvertedManaCost(top.getManaCost()));
					hand.add(top);
					lib.remove(top);

				}
			};// ability2

			player[0] = c.getController();
			ability2.setStackDescription(c.getName()
					+ " - "
					+ player[0]
					         + " reveals the top card of his library and put that card into his hand. He gain life equal to its converted mana cost.");
			AllZone.Stack.add(ability2);
		}

	}

	private static void playerCombatDamage_Hypnotic_Specter(Card c) {
		final String[] player = new String[1];
		player[0] = c.getController();
		final String[] opponent = new String[1];

		if(c.getNetAttack() > 0) {
			Ability ability = new Ability(c, "0") {
				@Override
				public void resolve() {

					opponent[0] = AllZone.GameAction.getOpponent(player[0]);
					AllZone.GameAction.discardRandom(opponent[0]);
				}
			};// ability

			opponent[0] = AllZone.GameAction.getOpponent(player[0]);
			ability.setStackDescription("Hypnotic Specter - " + opponent[0] + " discards a card at random");
			AllZone.Stack.add(ability);
		}
	}

	@SuppressWarnings("unused")
	// upkeep_CheckEmptyDeck_Lose
	private static void upkeep_CheckEmptyDeck_Lose() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone libraryZone = AllZone.getZone(Constant.Zone.Library, player);

		System.out.println("libraryZone.size: " + libraryZone.size() + " phase: " + AllZone.Phase.getPhase()
				+ "Turn: " + AllZone.Phase.getTurn());
		if(libraryZone.size() == 0 && AllZone.Phase.getPhase().equals(Constant.Phase.Untap)
				&& AllZone.Phase.getTurn() > 1) {
			PlayerLife life = AllZone.GameAction.getPlayerLife(player);
			life.setLife(0);
			// TODO display this somehow!!!
		}// if
	}// upkeep_CheckEmptyDeck_Lose

	private static void upkeep_AI_Aluren() {
		PlayerZone AIHand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Computer);
		PlayerZone AIPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList();
		list.addAll(AllZone.Human_Play.getCards());
		list.addAll(AllZone.Computer_Play.getCards());

		list = list.getName("Aluren");

		CardList creatures = new CardList();

		for(int i = 0; i < AIHand.size(); i++) {
			if(AIHand.get(i).getType().contains("Creature")
					&& CardUtil.getConvertedManaCost(AIHand.get(i).getManaCost()) <= 3) creatures.add(AIHand.get(i));
		}

		if(list.size() > 0 && creatures.size() > 0) {
			for(int i = 0; i < creatures.size(); i++) {
				Card c = creatures.getCard(i);
				AIHand.remove(c);
				AIPlay.add(c);
				c.setSickness(true);

			}
		}

	}

	private static void upkeep_Land_Tax() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Land Tax");

		PlayerZone oppPlayZone = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(player));

		CardList self = new CardList(playZone.getCards());
		CardList opp = new CardList(oppPlayZone.getCards());

		self = self.getType("Land");
		opp = opp.getType("Land");

		if(self.size() < opp.size()) {

			for(int i = 0; i < list.size(); i++) {
				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
						PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

						CardList lands = new CardList(lib.getCards());
						lands = lands.getType("Basic");

						if(player.equals("Human") && lands.size() > 0) {
							String[] choices = {"Yes", "No"};
							Object choice = AllZone.Display.getChoice("Use Land Tax?", choices);
							if(choice.equals("Yes")) {
								Object o = AllZone.Display.getChoiceOptional(
										"Pick a basic land card to put into your hand", lands.toArray());
								if(o != null) {
									Card card = (Card) o;
									lib.remove(card);
									hand.add(card);
									lands.remove(card);

									if(lands.size() > 0) {
										o = AllZone.Display.getChoiceOptional(
												"Pick a basic land card to put into your hand", lands.toArray());
										if(o != null) {
											card = (Card) o;
											lib.remove(card);
											hand.add(card);
											lands.remove(card);
											if(lands.size() > 0) {
												o = AllZone.Display.getChoiceOptional(
														"Pick a basic land card to put into your hand",
														lands.toArray());
												if(o != null) {
													card = (Card) o;
													lib.remove(card);
													hand.add(card);
													lands.remove(card);
												}
											}
										}

									}
								}
								AllZone.GameAction.shuffle("Human");
							}// if choice yes
						} // player equals human
						else if(player.equals("Computer") && lands.size() > 0) {
							Card card = lands.get(0);
							lib.remove(card);
							hand.add(card);
							lands.remove(card);

							if(lands.size() > 0) {
								card = lands.get(0);
								lib.remove(card);
								hand.add(card);
								lands.remove(card);

								if(lands.size() > 0) {
									card = lands.get(0);
									lib.remove(card);
									hand.add(card);
									lands.remove(card);
								}
							}
							AllZone.GameAction.shuffle("Computer");
						}
					}

				};// Ability
				ability.setStackDescription("Land Tax - search library for up to three basic land cards and put them into your hand");
				AllZone.Stack.add(ability);

			}// for
		}// if fewer lands than opponent

	}

	private static void upkeep_Squee() {
		final String player = AllZone.Phase.getActivePlayer();
		//PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player); //unused
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList list = new CardList(graveyard.getCards());
		list = list.getName("Squee, Goblin Nabob");

		final CardList squees = list;
		final int[] index = new int[1];
		index[0] = 0;

		for(int i = 0; i < list.size(); i++) {
			Ability ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

					if(player.equals("Human")) {
						String[] choices = {"Yes", "No"};
						Object o = AllZone.Display.getChoiceOptional(
								"Return Squee from your graveyard to your hand?", choices);
						if(o.equals("Yes")) {
							Card c = squees.get(index[0]);
							graveyard.remove(c);
							hand.add(c);
						}
					} else if(player.equals("Computer")) {
						Card c = squees.get(index[0]);
						graveyard.remove(c);
						hand.add(c);
					}
					index[0] = index[0] + 1;
				}

			};// Ability
			ability.setStackDescription("Squee gets returned from graveyard to hand.");
			AllZone.Stack.add(ability);
		} // if creatures > 0

	}


	private static void upkeep_AEther_Vial() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("AEther Vial");

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				final Card thisCard = list.get(i);
				Ability ability = new Ability(list.get(i), "") {
					@Override
					public void resolve() {
						// TODO Auto-generated method stub
						String[] choices = {"Yes", "No"};

						Object q = null;
						if(player.equals(Constant.Player.Human)) {
							q = AllZone.Display.getChoiceOptional("Put a counter on AEther Vial? ("
									+ thisCard.getCounters(Counters.CHARGE) + ")", choices);
							if(q == null || q.equals("No")) return;
							if(q.equals("Yes")) {

								thisCard.addCounter(Counters.CHARGE, 1);
							}
						} else if(player.equals(Constant.Player.Computer)) {

							thisCard.addCounter(Counters.CHARGE, 1);
						}

					}

				};
				ability.setStackDescription(list.get(i).getName() + " ("
						+ list.get(i).getCounters(Counters.CHARGE)
						+ " counters) - Put a charge counter on AEther Vial?");
				AllZone.Stack.add(ability);
			}//for
		}
	}

	private static void upkeep_Dragonmaster_Outcast() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Dragonmaster Outcast");

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				CardList lands = new CardList(playZone.getCards());
				lands = lands.getType("Land");

				if(lands.size() >= 6) {
					final Card c = list.get(i);
					Ability ability = new Ability(list.get(i), "0") {
						@Override
						public void resolve() {
							CardFactoryUtil.makeToken("Dragon", "R 5 5 Dragon", c, "R", new String[] {
									"Creature", "Dragon"}, 5, 5, new String[] {"Flying"});
						}

					};// Ability
					ability.setStackDescription("Dragonmaster Outcast - put a 5/5 red Dragon creature token with flying onto the battlefield.");
					AllZone.Stack.add(ability);
				}
			} // for
		} // if creatures > 0
	};

	private static void upkeep_Scute_Mob() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Scute Mob");

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				CardList lands = new CardList(playZone.getCards());
				lands = lands.getType("Land");

				if(lands.size() >= 5) {
					final Card c = list.get(i);
					Ability ability = new Ability(list.get(i), "0") {
						@Override
						public void resolve() {
							c.addCounter(Counters.P1P1, 4);
						}

					};// Ability
					ability.setStackDescription("Scute Mob - put four +1/+1 counters on Scute Mob.");
					AllZone.Stack.add(ability);
				}
			} // for
		} // if creatures > 0
	}//Scute Mob


	private static void upkeep_Sporesower_Thallid() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Sporesower Thallid");

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {

				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

						CardList fungi = new CardList(play.getCards());
						fungi = fungi.getType("Fungus");

						for(int j = 0; j < fungi.size(); j++) {
							Card c = fungi.get(j);
							c.addCounter(Counters.SPORE, 1);
						}
					}
				};// Ability
				ability.setStackDescription("Sporesower - put a spore counter on each fungus you control.");
				AllZone.Stack.add(ability);
			} // for
		} // if creatures > 0
	}

	private static void upkeep_Lichenthrope() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.filter(new CardListFilter()
		{
			public boolean addCard(Card c)
			{
				return c.getName().equals("Lichenthrope") && c.getCounters(Counters.M1M1) > 0;
			}
		});

		final CardList cl = list;

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {

				final int j = i;
				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						Card c = cl.get(j);
						c.subtractCounter(Counters.M1M1, 1);
					}

				};// Ability
				ability.setStackDescription("Lichenthrope - Remove a -1/-1 counter.");
				AllZone.Stack.add(ability);
			} // for
		} // if creatures > 0
	}//Lichenthrope


	private static void upkeep_Heartmender() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Heartmender");

		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {

				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

						CardList creats = new CardList(play.getCards());
						creats = creats.filter(new CardListFilter() {

							public boolean addCard(Card c) {
								return c.getCounters(Counters.M1M1) > 0;
							}

						});

						for(int j = 0; j < creats.size(); j++) {
							Card c = creats.get(j);
							if(c.getCounters(Counters.M1M1) > 0) c.addCounter(Counters.M1M1, -1);
						}

					}

				};// Ability
				ability.setStackDescription("Heartmender - Remove a -1/-1 counter from each creature you control.");
				AllZone.Stack.add(ability);
			} // for
		} // if creatures > 0
	}//heartmender

	private static void upkeep_Ratcatcher() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		//PlayerZone hand = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList creatures = new CardList(library.getCards());
		creatures = creatures.getType("Rat");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Ratcatcher");

		if(creatures.size() > 0 && list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {

				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						PlayerZone lib = AllZone.getZone(Constant.Zone.Library, player);
						PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

						CardList rats = new CardList(lib.getCards());
						rats = rats.getType("Rat");

						if(rats.size() > 0) {
							if(player.equals("Human")) {
								Object o = AllZone.Display.getChoiceOptional("Pick a Rat to put into your hand",
										rats.toArray());
								if(o != null) {
									Card card = (Card) o;
									lib.remove(card);
									hand.add(card);
								}
							} else if(player.equals("Computer")) {
								Card card = rats.get(0);
								lib.remove(card);
								hand.add(card);

							}
							AllZone.GameAction.shuffle(player);
						}
					}

				};// Ability
				ability.setStackDescription("Ratcatcher - search library for a rat and put into your hand");
				AllZone.Stack.add(ability);
			} // for
		} // if creatures > 0
	}

	private static void upkeep_Nath() {
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone zone1 = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(zone1.getCards());
		list = list.getName("Nath of the Gilt-Leaf");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					//PlayerZone hand = AllZone.getZone(Constant.Zone.Hand,Constant.Player.Human); //unused

					AllZone.GameAction.discardRandom(opponent);

				}
			}; // ability
			ability.setStackDescription("Nath of the Gilt-Leaf - " + opponent + " discards a card at random.");
			AllZone.Stack.add(ability);
		}
	}

	private static void upkeep_Anowon() {
		final String player = AllZone.Phase.getActivePlayer();
		CardList list = CardFactoryUtil.getCards("Anowon, the Ruin Sage", player);

		if(list.size() > 0) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
					PlayerZone cPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);
					CardList choices = new CardList(hPlay.getCards());

					CardListFilter filter = new CardListFilter() {
						public boolean addCard(Card c) {
							return c.isCreature() && !c.getType().contains("Vampire")
							&& !c.getKeyword().contains("Changeling");
						}
					};

					choices = choices.filter(filter);
					if(choices.size() > 0) AllZone.GameAction.sacrificePermanent(Constant.Player.Human, this,
							choices);

					CardList compCreats = new CardList(cPlay.getCards());
					compCreats = compCreats.filter(filter);

					if(compCreats.size() > 0) AllZone.GameAction.sacrificePermanent(Constant.Player.Computer,
							this, compCreats);
				}
			};
			ability.setStackDescription("At the beginning of your upkeep, each player sacrifices a non-Vampire creature.");
			AllZone.Stack.add(ability);
		}
	}

	private static void upkeep_Cunning_Lethemancer() {
		final String player = AllZone.Phase.getActivePlayer();
		//final String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone zone1 = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(zone1.getCards());
		list = list.getName("Cunning Lethemancer");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, Constant.Player.Human);

					CardList cardsInHand = new CardList(hand.getCards());

					if(cardsInHand.size() > 0) {
						Object o = AllZone.Display.getChoiceOptional("Select Card to discard",
								cardsInHand.toArray());
						Card c = (Card) o;
						AllZone.GameAction.discard(c);
					}

					AllZone.GameAction.discardRandom(Constant.Player.Computer);

				}
			}; // ability
			ability.setStackDescription("Cunning Lethemancer - Everyone discards a card.");
			AllZone.Stack.add(ability);
		}
	}

	private static void upkeep_Sensation_Gorger() {
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Sensation Gorger");

		for(int i = 0; i < list.size(); i++) {

			if(library.size() <= 0) {
				return;
			}

			if(list.get(i).getController().equals("Human")) {
				String[] choices = {"Yes", "No"};
				Object o = AllZone.Display.getChoiceOptional("Use " + list.get(i).getName() + "'s ability?",
						choices);

				if(o == null) return;
				if(o.equals("No")) return;
			}

			// System.out.println("top of deck: " + library.get(i).getName());
			//String creatureType = library.get(i).getType().toString();
			//String cardName = library.get(i).getName();

			PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

			PlayerZone oppHand = AllZone.getZone(Constant.Zone.Hand, opponent);

			String creatureType = library.get(0).getType().toString();

			if(creatureType.contains("Goblin") || creatureType.contains("Shaman")
					|| library.get(0).getKeyword().contains("Changeling")) {
				Card[] c = hand.getCards();
				for(int q = 0; q < c.length; q++)
					AllZone.GameAction.discard(c[q]);

				Card[] oc = oppHand.getCards();
				for(int j = 0; j < oc.length; j++)
					AllZone.GameAction.discard(oc[j]);

				for(int z = 0; z < 4; z++) {
					AllZone.GameAction.drawCard(Constant.Player.Computer);
					AllZone.GameAction.drawCard(Constant.Player.Human);
				}

			}
		}// for
	}// upkeep_Sensation_Gorger() 

	private static void upkeep_Winnower_Patrol() {
		final String player = AllZone.Phase.getActivePlayer();
		//final String opponent = AllZone.GameAction.getOpponent(player);
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Winnower Patrol");

		// final Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			String creatureType = library.get(0).getType().toString();
			String cardName = library.get(0).getName();


			System.out.println("CardName: " + list.get(i) + " id: " + list.get(i).getUniqueNumber());

			final int cardIndex = i;

			final Ability ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

					String creatureType = library.get(cardIndex).getType().toString();


					if(creatureType.contains("Elf") || creatureType.contains("Warrior")
							|| library.get(cardIndex).getKeyword().contains("Changeling")) {

						CardList list = new CardList(play.getCards());
						list = list.getName("Winnower Patrol");

						Card c = list.get(cardIndex); // must get same winnower
						// patrol
						System.out.println("cardIndex: " + cardIndex + " name: " + c.getName());
						int attack = c.getBaseAttack();
						int defense = c.getBaseDefense();

						attack++;
						defense++;

						c.setBaseAttack(attack);
						c.setBaseDefense(defense);

					}

				}// resolve()
			};// Ability
			if(creatureType.contains("Elf") || creatureType.contains("Warrior")) ability.setStackDescription("Winnower Patrol - "
					+ player + " reveals: " + cardName + ", and Winnower Patrol gets +1/+1.");
			else ability.setStackDescription("Winnower Patrol - " + player + " reveals top card: " + cardName
					+ ".");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Winnower_Patrol()

	private static void upkeep_Nightshade_Schemers() {
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Nightshade Schemers");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			String creatureType = library.get(0).getType().toString();
			String cardName = library.get(0).getName();

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					//PlayerZone play = AllZone.getZone(Constant.Zone.Play,player); //unused
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

					String creatureType = library.get(0).getType().toString();

					if(creatureType.contains("Faerie") || creatureType.contains("Wizard")
							|| library.get(0).getKeyword().contains("Changeling")) {
						AllZone.GameAction.getPlayerLife(opponent).subtractLife(2);
					}

				}// resolve()
			};// Ability
			if(creatureType.contains("Faerie") || creatureType.contains("Wizard")) ability.setStackDescription("Nightshade Schemers - "
					+ player + " reveals: " + cardName + ", and " + opponent + " loses 2 life.");
			else ability.setStackDescription("Nightshade Schemers - " + player + " reveals top card: " + cardName
					+ ".");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Nightshade_Schemers()

	private static void upkeep_Wandering_Graybeard() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Wandering Graybeard");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			String creatureType = library.get(0).getType().toString();
			String cardName = library.get(0).getName();

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					//PlayerZone play = AllZone.getZone(Constant.Zone.Play, player); //unused
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

					String creatureType = library.get(0).getType().toString();


					if(creatureType.contains("Giant") || creatureType.contains("Wizard")
							|| library.get(0).getKeyword().contains("Changeling")) {
						AllZone.GameAction.getPlayerLife(player).addLife(4);
					}

				}// resolve()
			};// Ability
			if(creatureType.contains("Giant") || creatureType.contains("Wizard")) ability.setStackDescription("Wandering Graybeard - "
					+ player + " reveals: " + cardName + ", and gains 4 life.");
			else ability.setStackDescription("Wandering Graybeard - " + player + " reveals top card: " + cardName
					+ ".");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Wandering_Graybeard()

	private static void upkeep_Wolf_Skull_Shaman() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Wolf-Skull Shaman");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			String creatureType = library.get(0).getType().toString();
			String cardName = library.get(0).getName();

			final Card crd = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

					String creatureType = library.get(0).getType().toString();

					if(creatureType.contains("Elf") || creatureType.contains("Shaman")
							|| library.get(0).getKeyword().contains("Changeling")) {
						CardFactoryUtil.makeToken("Wolf", "G 2 2 Wolf", crd, "G",
								new String[] {"Creature", "Wolf"}, 2, 2, new String[] {""});
					}

				}// resolve()
			};// Ability
			if(creatureType.contains("Elf") || creatureType.contains("Shaman")) ability.setStackDescription("Wolf-Skull Shaman - "
					+ player + " reveals: " + cardName + ", and puts 2/2 Wolf into play.");
			else ability.setStackDescription("Wolf-Skull Shaman - " + player + " reveals top card: " + cardName
					+ ".");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Wolf_Skull_Shaman()

	private static void upkeep_Leaf_Crowned_Elder() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Leaf-Crowned Elder");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			String creatureType = library.get(0).getType().toString();
			String cardName = library.get(0).getName();

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

					String creatureType = library.get(0).getType().toString();

					if(creatureType.contains("Treefolk") || creatureType.contains("Shaman")
							|| library.get(0).getKeyword().contains("Changeling")) {
						if(player.equals(Constant.Player.Human)) {
							String[] choices = {"Yes", "No"};
							Object q = null;

							StringBuilder sb = new StringBuilder();
							sb.append("Play ");
							sb.append(library.get(0).toString());
							sb.append(" without paying its mana cost?");

							q = AllZone.Display.getChoiceOptional(sb.toString(), choices);

							if(q == null || q.equals("No")) ;
							else {
								Card c = library.get(0);
								AllZone.GameAction.playCardNoCost(c);
								library.remove(c);
							}
						} else {
							Card c = library.get(0);
							ArrayList<SpellAbility> choices = c.getBasicSpells();

							for(SpellAbility sa:choices) {
								if(sa.canPlayAI()) {
									ComputerUtil.playStackFree(sa);
									break;
								}
							}
							library.remove(c);
							//play.add(c);
						}
					}

				}// resolve()
			};// Ability
			if(creatureType.contains("Treefolk") || creatureType.contains("Shaman")) ability.setStackDescription("Leaf-Crowned Elder - "
					+ player + " reveals: " + cardName + ".");
			else ability.setStackDescription("Leaf-Crowned Elder - " + player + " reveals top card: " + cardName
					+ ".");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Leaf_Crowned_Elder()


	private static void upkeep_Dark_Confidant() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Dark Confidant");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(library.size() <= 0) {
				return;
			}
			// System.out.println("top of deck: " + library.get(i).getName());
			final int convertedManaCost = CardUtil.getConvertedManaCost(library.get(i).getManaCost());
			String cardName = library.get(i).getName();

			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone library = AllZone.getZone(Constant.Zone.Library, player);
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
					AllZone.GameAction.getPlayerLife(player).subtractLife(convertedManaCost);

					// AllZone.GameAction.drawCard(player);
					// !!!can't just draw card, since it won't work with jpb's
					// fix!!!
					Card c = library.get(0);
					library.remove(c);
					hand.add(c);

				}// resolve()
			};// Ability
			ability.setStackDescription("Dark Confidant - " + player + " loses " + convertedManaCost
					+ " life and draws top card(" + cardName + ").");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Dark_Confidant()

	private static void upkeep_Debtors_Knell() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
		PlayerZone oppGrave = AllZone.getZone(Constant.Zone.Graveyard, AllZone.GameAction.getOpponent(player));

		CardList creatures = new CardList();
		creatures.addAll(grave.getCards());
		creatures.addAll(oppGrave.getCards());
		creatures = creatures.getType("Creature");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Debtors' Knell");

		if(creatures.size() > 0 && list.size() > 0) for(int i = 0; i < list.size(); i++) {
			{
				Ability ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						PlayerZone grave = AllZone.getZone(Constant.Zone.Graveyard, player);
						PlayerZone oppGrave = AllZone.getZone(Constant.Zone.Graveyard,
								AllZone.GameAction.getOpponent(player));
						PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

						CardList creatures = new CardList();
						creatures.addAll(grave.getCards());
						creatures.addAll(oppGrave.getCards());

						creatures = creatures.getType("Creature");

						if(player.equals("Human")) {
							Object o = AllZone.Display.getChoiceOptional("Pick a creature to put into play",
									creatures.toArray());
							if(o != null) {
								Card card = (Card) o;
								PlayerZone graveyard = AllZone.getZone(card);
								graveyard.remove(card);
								card.setController(player);
								playZone.add(card);
							}
						} else if(player.equals("Computer")) {
							Card card = creatures.get(0);
							PlayerZone graveyard = AllZone.getZone(card);
							graveyard.remove(card);
							card.setController(player);
							playZone.add(card);

						}
					}
				};// Ability
				ability.setStackDescription("Debtors' Knell returns creature from graveyard to play");
				AllZone.Stack.add(ability);
			}//for
		} // if creatures > 0

	}

	private static void upkeep_Emeria() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList creatures = new CardList(graveyard.getCards());
		creatures = creatures.getType("Creature");

		CardList land = new CardList(playZone.getCards());
		land = land.getType("Plains");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Emeria, the Sky Ruin");

		if(land.size() >= 7 && creatures.size() >= 1) {
			for(int i = 0; i < list.size(); i++) {
				Ability ability = new Ability(list.get(0), "0") {
					@Override
					public void resolve() {
						PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
						PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

						CardList creatures = new CardList(graveyard.getCards());
						creatures = creatures.getType("Creature");

						if(player.equals("Human")) {
							Object o = AllZone.Display.getChoiceOptional("Pick a creature to put into play",
									creatures.toArray());
							if(o != null) {
								Card card = (Card) o;
								graveyard.remove(card);
								playZone.add(card);
							}
						} else if(player.equals("Computer")) {
							Card card = creatures.get(0);
							graveyard.remove(card);
							playZone.add(card);

						}
					}

				};// Ability
				ability.setStackDescription("Emeria, the Sky Ruin returns creature from graveyard to the battlefield.");
				AllZone.Stack.add(ability);

			}
		}
	}

	private static void upkeep_Oversold_Cemetery() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList creatures = new CardList(graveyard.getCards());
		creatures = creatures.getType("Creature");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Oversold Cemetery");

		if(creatures.size() >= 4) {
			for(int i = 0; i < list.size(); i++) {
				Ability ability = new Ability(list.get(0), "0") {
					@Override
					public void resolve() {
						PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
						PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

						CardList creatures = new CardList(graveyard.getCards());
						creatures = creatures.getType("Creature");

						if(creatures.size() >= 4) {
							if(player.equals("Human")) {
								Object o = AllZone.Display.getChoiceOptional("Pick a creature to return to hand",
										creatures.toArray());
								if(o != null) {
									Card card = (Card) o;
									graveyard.remove(card);
									hand.add(card);
								}
							} else if(player.equals("Computer")) {
								Card card = creatures.get(0);
								graveyard.remove(card);
								hand.add(card);

							}
						}
					}

				};// Ability
				ability.setStackDescription("Oversold Cemetary returns creature from the graveyard to its owner's hand.");
				AllZone.Stack.add(ability);

			}
		}

	}//Oversold Cemetery

	private static void upkeep_Reya() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList creatures = new CardList(graveyard.getCards());
		creatures = creatures.getType("Creature");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Reya Dawnbringer");

		if(creatures.size() > 0 && list.size() > 0) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
					PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

					CardList creatures = new CardList(graveyard.getCards());
					creatures = creatures.getType("Creature");

					if(player.equals("Human")) {
						Object o = AllZone.Display.getChoiceOptional("Pick a creature to put into play",
								creatures.toArray());
						if(o != null) {
							Card card = (Card) o;
							graveyard.remove(card);
							playZone.add(card);
						}
					} else if(player.equals("Computer")) {
						Card card = creatures.get(0);
						graveyard.remove(card);
						playZone.add(card);

					}
				}

			};// Ability
			ability.setStackDescription("Reya returns creature from graveyard back to play");
			AllZone.Stack.add(ability);
		} // if creatures > 0
	} // reya

	private static void upkeep_Wort() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList creatures = new CardList(graveyard.getCards());
		creatures = creatures.getType("Goblin");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Wort, Boggart Auntie");

		if(creatures.size() > 0 && list.size() > 0) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);

					CardList creatures = new CardList(graveyard.getCards());
					creatures = creatures.getType("Goblin");

					if(player.equals("Human")) {
						Object o = AllZone.Display.getChoiceOptional("Pick a goblin to put into your hand",
								creatures.toArray());
						if(o != null) {
							Card card = (Card) o;
							graveyard.remove(card);
							hand.add(card);
						}
					} else if(player.equals("Computer")) {
						Card card = creatures.get(0);
						graveyard.remove(card);
						hand.add(card);

					}
				}

			};// Ability
			ability.setStackDescription("Wort returns creature from graveyard to " + player + "'s hand");
			AllZone.Stack.add(ability);
		} // if creatures > 0
	} // Wort

	private static void upkeep_Nether_Spirit() {
		final String player = AllZone.Phase.getActivePlayer();
		final PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
		final PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList all = new CardList(graveyard.getCards());
		all = all.getType("Creature");

		CardList list = new CardList(graveyard.getCards());
		list = list.getName("Nether Spirit");

		if(all.size() == 1 && list.size() == 1) {
			final Card nether = list.get(0);
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					graveyard.remove(nether);
					playZone.add(nether);
				}
			};

			boolean returnNether = false;

			if(player.equals(Constant.Player.Human)) {
				String[] choices = {"Yes", "No"};

				Object q = AllZone.Display.getChoiceOptional("Return Nether Spirit to play?", choices);
				if(q.equals("Yes")) returnNether = true;
			}

			if(player.equals(Constant.Player.Computer) || returnNether) {
				ability.setStackDescription("Nether Spirit returns to play.");
				AllZone.Stack.add(ability);
			}
		} //if
	}//nether spirit

	private static void upkeep_Spore_Counters() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getType("Creature");

		for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if(c.getName().equals("Deathspore Thallid") || c.getName().equals("Elvish Farmer")
					|| c.getName().equals("Feral Thallid") || c.getName().equals("Mycologist")
					|| c.getName().equals("Pallid Mycoderm") || c.getName().equals("Psychotrope Thallid")
					|| c.getName().equals("Savage Thallid") || c.getName().equals("Thallid")
					|| c.getName().equals("Thallid Devourer") || c.getName().equals("Thallid Germinator")
					|| c.getName().equals("Thallid Shell-Dweller") || c.getName().equals("Thorn Thallid")
					|| c.getName().equals("Utopia Mycon") || c.getName().equals("Vitaspore Thallid")) {
				c.addCounter(Counters.SPORE, 1);
			}
		}
	}

	private static void upkeep_Vanishing() {

		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());
		list = list.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				SpellAbility[] sas = c.getSpellAbility();
				boolean hasRegen = false;
				for(SpellAbility sa:sas) {
					if(sa.toString().contains(
							"At the beginning of your upkeep, remove a time counter from it. When the last is removed, sacrifice it.)")) //this is essentially ".getDescription()"
						hasRegen = true;
				}
				return hasRegen;
			}
		});
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				Ability ability = new Ability(card, "0") {
					@Override
					public void resolve() {
						card.setCounter(Counters.TIME, card.getCounters(Counters.TIME) - 1);
						if(card.getCounters(Counters.TIME) <= 0) {
							AllZone.GameAction.sacrifice(card);
						}
					}
				}; // ability
				ability.setStackDescription(card.getName()
						+ " - Vanishing - remove a time counter from it. When the last is removed, sacrifice it.)");
				AllZone.Stack.add(ability);

			}
		}
	}


	private static void upkeep_Aven_Riftwatcher() {
		// get all Aven Riftwatcher in play under the control of this player
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());
		list = list.getName("Aven Riftwatcher");
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);
				card.setCounter(Counters.TIME, card.getCounters(Counters.TIME) - 1);
				if(card.getCounters(Counters.TIME) <= 0) {
					AllZone.GameAction.sacrifice(card);
				}
			}
		}
	}

	private static void upkeep_Calciderm() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());
		list = list.getName("Calciderm");
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);
				card.setCounter(Counters.TIME, card.getCounters(Counters.TIME) - 1);
				if(card.getCounters(Counters.TIME) <= 0) {
					AllZone.GameAction.sacrifice(card);
				}
			}
		}
	}

	private static void upkeep_Blastoderm() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList list = new CardList(playZone.getCards());
		list = list.getName("Blastoderm");
		if(list.size() > 0) {
			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);
				if(card.getCounters(Counters.FADE) <= 0) {
					AllZone.GameAction.sacrifice(card);
				}
				card.setCounter(Counters.FADE, card.getCounters(Counters.FADE) - 1);

			}
		}
	}


	private static void upkeep_Defense_of_the_Heart() {
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		// check if opponent has 3 or more creatures in play
		PlayerZone opponentZone = AllZone.getZone(Constant.Zone.Play, opponent);
		CardList opponentList = new CardList(opponentZone.getCards());
		opponentList = opponentList.getType("Creature");

		/*
        for (int i = 0; i < opponentList.size(); i++)
        {
        	Card tmpCard = opponentList.get(i);
        	System.out.println("opponent has: " + tmpCard);
        }
		 */

		if(3 > opponentList.size()) return;

		// opponent has more than 3 creatures in play, so check if Defense of
		// the Heart is in play and sacrifice it for the effect.
		CardList list = new CardList(playZone.getCards());
		list = list.getName("Defense of the Heart");

		if(0 < list.size()) {
			// loop through the number of Defense of the Heart's that player
			// controls. They could control 1, 2, 3, or 4 of them.
			for(int i = 0; i < list.size(); i++) {
				final Card card = list.get(i);
				Ability ability = new Ability(list.get(0), "0") {
					@Override
					public void resolve() {
						PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());
						PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());

						// sacrifice Defense of the Heart
						AllZone.GameAction.sacrifice(card);
						// search library for a creature, put it into play
						Card creature1 = getCreatureFromLibrary();
						if(creature1 != null) {
							library.remove(creature1);
							play.add(creature1);
						}

						// search library for a second creature, put it into
						// play
						Card creature2 = getCreatureFromLibrary();
						if(creature2 != null) {
							// if we got this far the effect was good
							library.remove(creature2);
							play.add(creature2);
						}

						AllZone.GameAction.shuffle(card.getController());

					}

					public Card getCreatureFromLibrary() {
						PlayerZone library = AllZone.getZone(Constant.Zone.Library, card.getController());

						CardList creatureList = new CardList(library.getCards());
						creatureList = creatureList.getType("Creature");

						if(Constant.Player.Computer.equals(card.getController())) {
							return CardFactoryUtil.AI_getBestCreature(creatureList);
						} else {
							Object o = AllZone.Display.getChoiceOptional("Choose a creature card",
									creatureList.toArray());
							if(o != null) {
								Card creature = (Card) o;
								return creature;
							} else {
								return null;
							}
						}
					}// getCreatureFromLibrary
				};// Ability

				ability.setStackDescription("Defense of the Heart - "
						+ player
						+ " sacrifices Defense of the Heart to search their library for up to two creature cards and put those creatures into play. Then shuffle's their library.");
				AllZone.Stack.add(ability);
				card.addSpellAbility(ability);

			}
		}// if
	}// upkeep_Defense of the Heart

	private static void upkeep_Karma() {
		final String player = AllZone.Phase.getActivePlayer();
		String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Play, opponent);

		CardList karma = new CardList(opponentPlayZone.getCards());
		karma = karma.getName("Karma");

		PlayerZone activePlayZone = AllZone.getZone(Constant.Zone.Play, player);
		CardList swamps = new CardList(activePlayZone.getCards());
		swamps = swamps.getType("Swamp");

		// determine how much damage to deal the current player
		final int damage = swamps.size();

		// if there are 1 or more Karmas owned by the opponent of the
		// current player have each of them deal damage.
		if(0 < karma.size()) {
			for(int i = 0; i < karma.size(); i++) {
				Ability ability = new Ability(karma.get(0), "0") {
					@Override
					public void resolve() {
						if(damage>0){
							PlayerLife life = AllZone.GameAction.getPlayerLife(player);
							life.setLife(life.getLife() - damage);
						}
					}
				};// Ability
				if(damage>0){
					ability.setStackDescription("Karma deals " + damage + " damage to " + player);
					AllZone.Stack.add(ability);
				}
			}
		}// if
	}// upkeep_Karma()

	private static void upkeep_Convalescence() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		final PlayerLife pLife = AllZone.GameAction.getPlayerLife(player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Convalescence");

		for(int i = 0; i < list.size(); i++) {
			Ability ability = new Ability(list.get(i), "0") {

				@Override
				public void resolve() {
					pLife.addLife(1);
				}
			};// Ability
			ability.setStackDescription("Convalescence - " + player + " gain 1 life");

			if((pLife.getLife() + i) <= 10) {
				AllZone.Stack.add(ability);
			}
		}// for
	}// upkeep_Convalescence()

	private static void upkeep_Convalescent_Care() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerLife pLife = AllZone.GameAction.getPlayerLife(player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Convalescent Care");

		for(int i = 0; i < list.size(); i++) {
			/*
            Ability ability = new Ability(list.get(i), "0")
            {

            	public void resolve()
            	{

            		pLife.addLife(3);
            		AllZone.GameAction.drawCard(player);
            	}
            };// Ability
            ability.setStackDescription("Convalescent Care - " + player
            		+ " gains 3 life and draws a card");

            if ((pLife.getLife() + i) <= 5)
            {
            	AllZone.Stack.add(ability);
            }
			 */
			if((pLife.getLife()) <= 5) {
				pLife.addLife(3);
				AllZone.GameAction.drawCard(player);
			}

		}// for
	}// upkeep_Convalescence()

	public static void upkeep_Ivory_Tower() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play,player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Ivory Tower");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				public void resolve() {
					PlayerZone hand = AllZone.getZone(Constant.Zone.Hand, player);
					int numCards = hand.getCards().length;
					if( numCards > 4 ) {
						AllZone.GameAction.getPlayerLife(player).addLife(numCards-4);
					}
				}
			};//Ability
			ability.setStackDescription("Ivory Tower - " +player+ " gains 1 life for each card > 4");

			AllZone.Stack.add(ability);
		}//for
	}//upkeep_Ivory Tower()

	//Forge doesn't distinguish between beginning and end of upkeep
	//so, we'll put The Rack next to Black Vise
	private static void upkeep_The_Rack() {
		// sanity check. If a player has >= 3 cards The Rack does nothing.
		final String player = AllZone.Phase.getActivePlayer();
		final int playerHandSize = AllZone.getZone(Constant.Zone.Hand, player).size();

		if(playerHandSize >= 3) {
			return;
		}

		// if a player has 2 or fewer cards The Rack does damage
		// so, check if opponent of the current player has The Rack
		String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Play, opponent);

		CardList theRack = new CardList(opponentPlayZone.getCards());
		theRack = theRack.getName("The Rack");

		// determine how much damage to deal the current player
		final int damage = 3 - playerHandSize;

		// if there are 1 or more The Racks owned by the opponent of the
		// current player have each of them deal damage.
		if(0 < theRack.size()) {
			for(int i = 0; i < theRack.size(); i++) {
				Ability ability = new Ability(theRack.get(0), "0") {
					@Override
					public void resolve() {
						PlayerLife life = AllZone.GameAction.getPlayerLife(player);
						life.setLife(life.getLife() - damage);
					}
				};// Ability

				ability.setStackDescription("The Rack -  deals " + damage + " damage to " + player);
				AllZone.Stack.add(ability);
			}
		}// if
	}// upkeep_The_Rack

	// Currently we don't determine the difference between beginning and end of
	// upkeep in MTG forge.
	// So Black Vise's effects happen at the beginning of the upkeep instead of
	// at the end.
	private static void upkeep_BlackVise() {
		// sanity check. If a player has <= 4 cards black vise does nothing.
		final String player = AllZone.Phase.getActivePlayer();
		final int playerHandSize = AllZone.getZone(Constant.Zone.Hand, player).size();

		if(playerHandSize <= 4) {
			return;
		}

		// if a player has 5 or more cards black vise does damage
		// so, check if opponent of the current player has Black Vise
		String opponent = AllZone.GameAction.getOpponent(player);

		PlayerZone opponentPlayZone = AllZone.getZone(Constant.Zone.Play, opponent);

		CardList blackVice = new CardList(opponentPlayZone.getCards());
		blackVice = blackVice.getName("Black Vise");

		// determine how much damage to deal the current player
		final int damage = playerHandSize - 4;

		// if there are 1 or more black vises owned by the opponent of the
		// current player have each of them deal damage.
		if(0 < blackVice.size()) {
			for(int i = 0; i < blackVice.size(); i++) {
				Ability ability = new Ability(blackVice.get(0), "0") {
					@Override
					public void resolve() {
						PlayerLife life = AllZone.GameAction.getPlayerLife(player);
						life.setLife(life.getLife() - damage);
					}
				};// Ability

				ability.setStackDescription("Black Vise deals " + damage + " to " + player);
				AllZone.Stack.add(ability);
			}
		}// if
	}// upkeep_BlackVice

	private static void upkeep_Copper_Tablet() {
		/*
		 * At the beginning of each player's upkeep, Copper Tablet deals 1 damage to that player.
		 */
		final String player = AllZone.Phase.getActivePlayer();
		CardList list = AllZoneUtil.getCardsInPlay("Copper Tablet");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			};// Ability
			ability.setStackDescription("Copper Tablet - deals 1 damage to " + player);

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Copper_Tablet()

	private static void upkeep_Klass() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList elf = new CardList(playZone.getCards());
		elf = elf.getType("Elf");

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Klaas, Elf Friend");

		if(0 < list.size() && 10 <= elf.size()) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife life = AllZone.GameAction.getPlayerLife(opponent);
					life.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Klaas, Elf Friend - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Klass

	private static void upkeep_Felidar_Sovereign() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		PlayerLife plife = AllZone.GameAction.getPlayerLife(player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Felidar Sovereign");

		if(0 < list.size() && plife.getLife() >= 40) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife life = AllZone.GameAction.getPlayerLife(opponent);

					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Felidar Sovereign");
					life.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Felidar Sovereign - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Felidar_Sovereign

	private static void upkeep_Battle_of_Wits() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone libraryZone = AllZone.getZone(Constant.Zone.Library, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Battle of Wits");

		if(0 < list.size() && 200 <= libraryZone.size()) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife life = AllZone.GameAction.getPlayerLife(opponent);

					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Battle of Wits");

					life.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Battle of Wits - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Battle_of_Wits

	private static void upkeep_Epic_Struggle() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Epic Struggle");

		CardList creats = new CardList(playZone.getCards());
		creats = creats.getType("Creature");

		if(0 < list.size() && creats.size() >= 20) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife life = AllZone.GameAction.getPlayerLife(opponent);

					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Epic Struggle");

					life.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Epic Struggle - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Epic_Struggle

	private static void upkeep_Helix_Pinnacle() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Helix Pinnacle");

		for(Card c : list) {
			if (c.getCounters(Counters.TOWER) < 100) continue;
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() 
				{
					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Helix Pinnacle");
					AllZone.GameAction.getPlayerLife(AllZone.GameAction.getOpponent(player))
					.setLife(0);
				}
			};

			ability.setStackDescription("Helix Pinnacle - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Helix_Pinnacle

	private static void upkeep_Near_Death_Experience() {
		/*
		 * At the beginning of your upkeep, if you have exactly 1 life, you win the game.
		 */
		final String player = AllZone.Phase.getActivePlayer();
		PlayerLife life = AllZone.GameAction.getPlayerLife(player);
		
		CardList list = AllZoneUtil.getPlayerCardsInPlay(player, "Near-Death Experience");

		if(0 < list.size() && life.getLife() == 1) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife oppLife = AllZone.GameAction.getPlayerLife(opponent);

					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Near-Death Experience");

					oppLife.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Near-Death Experience - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Near_Death_Experience


	private static void upkeep_Barren_Glory() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone handZone = AllZone.getZone(Constant.Zone.Hand, player);

		CardList list = new CardList(playZone.getCards());
		CardList playList = new CardList(playZone.getCards());
		playList = playList.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return !c.getName().equals("Mana Pool");
			}
		});

		list = list.getName("Barren Glory");

		if(playList.size() == 1 && list.size() == 1 && handZone.size() == 0) {
			Ability ability = new Ability(list.get(0), "0") {
				@Override
				public void resolve() {
					String opponent = AllZone.GameAction.getOpponent(player);
					PlayerLife life = AllZone.GameAction.getPlayerLife(opponent);

					int gameNumber = 0;
					if (Constant.Runtime.WinLose.getWin()==1)
						gameNumber = 1;
					Constant.Runtime.WinLose.setWinMethod(gameNumber,"Barren Glory");

					life.setLife(0);
				}
			};// Ability

			ability.setStackDescription("Barren Glory - " + player + " wins the game");
			AllZone.Stack.add(ability);
		}// if
	}// upkeep_Barren_Glory

	private static void upkeep_Sleeper_Agent() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Sleeper Agent");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(2);
				}
			};

			ability.setStackDescription("Sleeper Agent deals 2 damage to its controller.");

			AllZone.Stack.add(ability);
		}
	}

	private static void upkeep_Cursed_Land() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		//list = list.getName("Cursed Land");
		list = list.filter(new CardListFilter() {

			public boolean addCard(Card c) {
				return c.isLand() && c.isEnchanted();
			}
		});

		if(list.size() > 0) {
			ArrayList<Card> enchants;
			Ability ability;
			for(int i = 0; i < list.size(); i++) {
				enchants = list.get(i).getEnchantedBy();
				for(Card enchant:enchants) {
					if(enchant.getName().equals("Cursed Land")) {
						//final Card c = enchant;
						ability = new Ability(enchant, "0") {

							@Override
							public void resolve() {
								//if (c.getController().equals(player))
								AllZone.GameAction.getPlayerLife(player).subtractLife(1);
							}
						};

						ability.setStackDescription("Cursed Land deals one damage to enchanted land's controller.");

						AllZone.Stack.add(ability);


					}
				}
			}

		}//list > 0
	}//cursed land

	private static void upkeep_Pillory_of_the_Sleepless() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		//list = list.getName("Cursed Land");
		list = list.filter(new CardListFilter() {

			public boolean addCard(Card c) {
				return c.isCreature() && c.isEnchanted();
			}
		});

		if(list.size() > 0) {
			ArrayList<Card> enchants;
			Ability ability;
			for(int i = 0; i < list.size(); i++) {
				enchants = list.get(i).getEnchantedBy();
				for(Card enchant:enchants) {
					if(enchant.getName().equals("Pillory of the Sleepless")) {
						//final Card c = enchant;
						ability = new Ability(enchant, "0") {
							@Override
							public void resolve() {
								//if (c.getController().equals(player))
								AllZone.GameAction.getPlayerLife(player).subtractLife(1);
							}
						};
						ability.setStackDescription("Pillory of the Sleepless deals one damage to enchanted creature's controller.");

						AllZone.Stack.add(ability);
					}
				}
			}

		}//list > 0
	}//cursed land


	private static void upkeep_Greener_Pastures() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone oppPlayZone = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(player));

		CardList self = new CardList(playZone.getCards());
		CardList opp = new CardList(oppPlayZone.getCards());

		self = self.getType("Land");
		opp = opp.getType("Land");

		if((self.size() == opp.size()) || opp.size() > self.size()) return;
		else //active player has more lands
		{
			String mostLandsPlayer = "";
			if(self.size() > opp.size()) mostLandsPlayer = player;

			final String mostLands = mostLandsPlayer;

			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Greener Pastures");

			Ability ability;

			for(int i = 0; i < list.size(); i++) {
				//final Card crd = list.get(i);
				ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", mostLands, "G", new String[] {
								"Creature", "Saproling"}, 1, 1, new String[] {""});
					}// resolve()
				};// Ability
				ability.setStackDescription("Greener Pastures - " + mostLands
						+ " puts a 1/1 green Saproling token into play.");

				AllZone.Stack.add(ability);
			}// for

		}//else
	}// upkeep_Greener_Pastures()

	private static void upkeep_Bitterblossom() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Bitterblossom");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
					CardFactoryUtil.makeToken("Faerie Rogue", "B 1 1 Faerie Rogue", crd, "B", new String[] {
							"Creature", "Faerie", "Rogue"}, 1, 1, new String[] {"Flying"});
				}// resolve()
			};// Ability
			ability.setStackDescription("Bitterblossom - deals 1 damage to " + player
					+ " and put a 1/1 token into play.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Bitterblossom()

	private static void upkeep_Goblin_Assault() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Goblin Assault");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					CardFactoryUtil.makeToken("Goblin", "R 1 1 Goblin", crd, "R", new String[] {
							"Creature", "Goblin"}, 1, 1, new String[] {"Haste"});
				}// resolve()
			};// Ability
			ability.setStackDescription("Goblin Assault - " + player +
			" puts a 1/1 red Goblin creature token with haste onto the battlefield.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Goblin_Assault()
	
	private static void upkeep_Awakening_Zone() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Awakening Zone");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					CardList cl = CardFactoryUtil.makeToken("Eldrazi Spawn", "C 0 1 Eldrazi Spawn", crd, "C", new String[] {
							"Creature", "Eldrazi", "Spawn"}, 0, 1, new String[] {"Sacrifice CARDNAME: Add 1 to your mana pool."});
					for (Card c:cl)
						c.addSpellAbility(CardFactoryUtil.getEldraziSpawnAbility(c));
				}// resolve()
			};// Ability
			ability.setStackDescription("Awakening Zone - " + player +
			" puts a 0/1 colorless Eldrazi Spawn creature token onto the battlefield.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Awakening_Zone()

	private static void upkeep_Masticore() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Masticore");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);

			final Input discard = new Input() {
				private static final long serialVersionUID = 2252076866782738069L;

				@Override
				public void showMessage() {
					AllZone.Display.showMessage(crd + " - Discard a card from your hand");
					ButtonUtil.enableOnlyCancel();
				}

				@Override
				public void selectCard(Card c, PlayerZone zone) {
					if(zone.is(Constant.Zone.Hand)) {
						AllZone.GameAction.discard(c);
						stop();
					}
				}

				@Override
				public void selectButtonCancel() {
					AllZone.GameAction.sacrifice(crd);
					stop();
				}
			};//Input

			ability = new Ability(crd, "0") {
				@Override
				public void resolve() {
					if(crd.getController().equals(Constant.Player.Human)) {
						if(AllZone.Human_Hand.getCards().length == 0) AllZone.GameAction.sacrifice(crd);
						else AllZone.InputControl.setInput(discard);
					} else //comp
					{
						CardList list = new CardList(AllZone.Computer_Hand.getCards());

						if(list.size() != 0) AllZone.GameAction.discard(list.get(0));
						else AllZone.GameAction.sacrifice(crd);
					}//else
				}//resolve()
			};//Ability
			ability.setStackDescription(crd + " - sacrifice Masticore unless you discard a card.");
			AllZone.Stack.add(ability);
		}// for
	}//upkeep_Masticore


	private static void upkeep_Eldrazi_Monument() {
		final String player = AllZone.Phase.getActivePlayer();
		final PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Eldrazi Monument");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					CardList creats = new CardList(playZone.getCards());
					creats = creats.getType("Creature");

					if(creats.size() < 1) {
						AllZone.GameAction.sacrifice(card);
						return;
					}

					if(player.equals(Constant.Player.Human)) {
						Object o = AllZone.Display.getChoiceOptional("Select creature to sacrifice",
								creats.toArray());
						Card sac = (Card) o;
						if(sac == null) {
							creats.shuffle();
							sac = creats.get(0);
						}
						AllZone.GameAction.sacrifice(sac);
					} else//computer
					{
						CardListUtil.sortAttackLowFirst(creats);
						AllZone.GameAction.sacrifice(creats.get(0));
					}
				}
			};
			ability.setStackDescription("Eldrazi Monument - " + player + " sacrifices a creature.");
			AllZone.Stack.add(ability);
		}

	}//upkeep_Eldrazi_Monument

	private static void upkeep_Blaze_Counters() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList blaze = new CardList(playZone.getCards());
		blaze = blaze.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return c.isLand() && c.getCounters(Counters.BLAZE) > 0;
			}
		});

		if(blaze.size() > 0) {
			final int lands = blaze.size();
			Ability ability = new Ability(blaze.get(0), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(lands);
				}
			};
			ability.setStackDescription("Obsidian Fireheart - " + player + " gets dealt " + lands + " damage.");
			AllZone.Stack.add(ability);

		}

	}

	private static void upkeep_Mycoloth() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		final int[] number = new int[1];

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Mycoloth");


		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					number[0] = card.getNetPTCounters();

					for(int j = 0; j < number[0]; j++) {
						makeToken();
					}

				}// resolve()

				public void makeToken() {
					CardFactoryUtil.makeToken("Saproling", "G 1 1 Saproling", card, "G", new String[] {
							"Creature", "Saproling"}, 1, 1, new String[] {""});
				}
			};// Ability
			ability.setStackDescription("Mycoloth - " + player
					+ " puts a 1/1 green Saproling into play for each +1/+1 counter on Mycoloth.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Mycoloth()

	private static void upkeep_Dragon_Broodmother() {
		//final String player = AllZone.Phase.getActivePlayer();
		PlayerZone hPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Human);
		PlayerZone cPlay = AllZone.getZone(Constant.Zone.Play, Constant.Player.Computer);

		CardList list = new CardList(hPlay.getCards());
		list.addAll(cPlay.getCards());
		list = list.getName("Dragon Broodmother");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			ability = new Ability(card, "0") {
				@Override
				public void resolve() {
					int multiplier = 1;
					int doublingSeasons = CardFactoryUtil.getCards("Doubling Season", card.getController()).size();
					if(doublingSeasons > 0) multiplier = (int) Math.pow(2, doublingSeasons);
					for(int i = 0; i < multiplier; i++)
						makeToken();

				}// resolve()

				public void makeToken() {
					//CardList cl = CardFactoryUtil.makeToken("Dragon", "RG 1 1 Dragon", card, "RG", new String[] {"Creature", "Dragon"}, 1, 1, new String[] {"Flying"} );


					final Card c = new Card();

					c.setOwner(card.getController());
					c.setController(card.getController());

					c.setName("Dragon");
					c.setImageName("RG 1 1 Dragon");
					c.setManaCost("RG");
					c.setToken(true);

					c.addType("Creature");
					c.addType("Dragon");

					c.addIntrinsicKeyword("Flying");

					c.setBaseAttack(1);
					c.setBaseDefense(1);

					//final String player = card.getController();
					final int[] numCreatures = new int[1];

					final SpellAbility devour = new Spell(card) {

						private static final long serialVersionUID = 4158780345303896275L;

						@Override
						public void resolve() {
							int totalCounters = numCreatures[0] * 2;
							c.addCounter(Counters.P1P1, totalCounters);

						}

						@Override
						public boolean canPlay() {
							return AllZone.Phase.getActivePlayer().equals(card.getController())
							&& card.isFaceDown() && !AllZone.Phase.getPhase().equals("End of Turn")
							&& AllZone.GameAction.isCardInPlay(card);
						}

					};//devour

					Command intoPlay = new Command() {

						private static final long serialVersionUID = -9220268793346809216L;

						public void execute() {

							PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
							CardList creatsToSac = new CardList();
							CardList creats = new CardList(play.getCards());
							creats = creats.filter(new CardListFilter() {
								public boolean addCard(Card crd) {
									return crd.isCreature() && !crd.equals(c);
								}
							});

							//System.out.println("Creats size: " + creats.size());

							if(card.getController().equals(Constant.Player.Human)) {
								Object o = null;
								int creatsSize = creats.size();

								for(int k = 0; k < creatsSize; k++) {
									o = AllZone.Display.getChoiceOptional("Select creature to sacrifice",
											creats.toArray());

									if(o == null) break;

									Card crd = (Card) o;
									creatsToSac.add(crd);
									creats.remove(crd);
								}

								numCreatures[0] = creatsToSac.size();
								for(int m = 0; m < creatsToSac.size(); m++) {
									AllZone.GameAction.sacrifice(creatsToSac.get(m));
								}

							}//human
							else {
								int count = 0;
								for(int i = 0; i < creats.size(); i++) {
									Card crd = creats.get(i);
									if(crd.getNetAttack() <= 1 && crd.getNetDefense() <= 2) {
										AllZone.GameAction.sacrifice(crd);
										count++;
									}
								}
								numCreatures[0] = count;
							}
							AllZone.Stack.add(devour);
						}
					};

					devour.setStackDescription(c.getName() + " - gets 2 +1/+1 counter(s) per devoured creature.");
					devour.setDescription("Devour 2");
					c.addSpellAbility(devour);
					c.addComesIntoPlayCommand(intoPlay);

					PlayerZone play = AllZone.getZone(Constant.Zone.Play, card.getController());
					play.add(c);

				}
			};// Ability
			ability.setStackDescription("Dragon Broodmother - put a 1/1 red and green Dragon token into play.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Dragon_Broodmother()

	private static void upkeep_Bringer_of_the_Green_Dawn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Bringer of the Green Dawn");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					String[] choices = {"Yes", "No"};

					Object q = null;
					if(player.equals(Constant.Player.Human)) {
						q = AllZone.Display.getChoiceOptional("Use Bringer of the Green Dawn?", choices);

						if(q == null || q.equals("No")) return;
						if(q.equals("Yes")) {
							CardFactoryUtil.makeToken("Beast", "G 3 3 Beast", crd, "G", new String[] {
									"Creature", "Beast"}, 3, 3, new String[] {""});
						}
					} else if(player.equals(Constant.Player.Computer)) {
						CardFactoryUtil.makeToken("Beast", "G 3 3 Beast", crd, "G", new String[] {
								"Creature", "Beast"}, 3, 3, new String[] {""});
					}
				}// resolve()
			};// Ability
			ability.setStackDescription("Bringer of the Green Dawn - " + player
					+ " puts a 3/3 Green Beast token creature into play.");

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Bringer_of_the_Green_Dawn()

	private static void upkeep_Bringer_of_the_Blue_Dawn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Bringer of the Blue Dawn");

		for(int i = 0; i < list.size(); i++) {
			String[] choices = {"Yes", "No"};
			Object q = null;
			if(player.equals(Constant.Player.Human)) {
				q = AllZone.Display.getChoiceOptional("Use Bringer of the Blue Dawn?", choices);

				if(q == null || q.equals("No")) return;
			}
			if(player.equals(Constant.Player.Computer)) {
				AllZone.GameAction.drawCard(player);
				AllZone.GameAction.drawCard(player);
			} else if(q.equals("Yes")) {
				AllZone.GameAction.drawCard(player);
				AllZone.GameAction.drawCard(player);
			}
		}// for
	}// upkeep_Bringer_of_the_Blue_Dawn()

	private static void upkeep_Bringer_of_the_White_Dawn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);
		PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Bringer of the White Dawn");

		CardList artifacts = new CardList(graveyard.getCards());
		artifacts = artifacts.getType("Artifact");

		if(artifacts.size() > 0 && list.size() > 0) {
			Ability ability;
			for(int i = 0; i < list.size(); i++) {
				ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						String[] choices = {"Yes", "No"};

						Object q = null;
						if(player.equals(Constant.Player.Human)) {
							q = AllZone.Display.getChoiceOptional("Use Bringer of the White Dawn?", choices);
							if(q == null || q.equals("No")) return;
							if(q.equals("Yes")) {
								PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
								PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

								CardList arts = new CardList(graveyard.getCards());
								arts = arts.getType("Artifact");

								Object o = AllZone.Display.getChoiceOptional("Pick an artifact to put into play",
										arts.toArray());
								if(o != null) {
									Card card = (Card) o;
									graveyard.remove(card);
									playZone.add(card);
								}

							}
						}

						else if(player.equals(Constant.Player.Computer)) {
							PlayerZone graveyard = AllZone.getZone(Constant.Zone.Graveyard, player);
							PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

							CardList arts = new CardList(graveyard.getCards());
							arts = arts.getType("Artifact");

							Card card = arts.get(0);
							graveyard.remove(card);
							playZone.add(card);
						}

					}// resolve()
				};// Ability
				ability.setStackDescription("Bringer of the White Dawn - " + player
						+ " returns an artifact to play.");


				AllZone.Stack.add(ability);
			}// for
		}//if
	}// upkeep_Bringer_of_the_White_Dawn()


	private static void upkeep_Serendib_Efreet() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Serendib Efreet");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			};// Ability
			ability.setStackDescription("Serendib Efreet - deals 1 damage to " + player);

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Serendib_Efreet()

	private static void upkeep_Nettletooth_Djinn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Nettletooth Djinn");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			};// Ability
			ability.setStackDescription("Nettletooth Djinn - deals 1 damage to " + player);

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Nettletooth_Djinn()

	private static void upkeep_Howling_Mine() {
		final String player = AllZone.Phase.getActivePlayer();

		CardList list = new CardList();
		list.addAll(AllZone.Human_Play.getCards());
		list.addAll(AllZone.Computer_Play.getCards());
		list = list.getName("Howling Mine");

		for(int i = 0; i < list.size(); i++){
			if( list.getCard(i).isUntapped() ) {
				AllZone.GameAction.drawCard(player);
			}
		}
	}// upkeep_Howling_Mine()

	private static void upkeep_Font_of_Mythos() {
		final String player = AllZone.Phase.getActivePlayer();

		CardList list = new CardList();
		list.addAll(AllZone.Human_Play.getCards());
		list.addAll(AllZone.Computer_Play.getCards());
		list = list.getName("Font of Mythos");

		for(int i = 0; i < list.size(); i++) {
			AllZone.GameAction.drawCard(player);
			AllZone.GameAction.drawCard(player);
		}
	}// upkeep_Font_of_Mythos()

	private static void upkeep_Overbeing_of_Myth() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();

		list.addAll(play.getCards());
		list = list.getName("Overbeing of Myth");

		for(int i = 0; i < list.size(); i++)
			AllZone.GameAction.drawCard(player);
	}// upkeep_Overbeing_of_Myth()

	private static void upkeep_Carnophage() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Carnophage");
		if(player == "Human") {
			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				String[] choices = {"Yes", "No"};
				Object choice = AllZone.Display.getChoice("Pay Carnophage's upkeep?", choices);
				if(choice.equals("Yes")) AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				else c.tap();
			}
		}
		if(player == "Computer") for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if(AllZone.Computer_Life.getLife() > 1) AllZone.GameAction.getPlayerLife(player).subtractLife(1);
			else c.tap();
		}
	}// upkeep_Carnophage

	private static void upkeep_Sangrophage() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Sangrophage");
		if(player == "Human") {
			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				String[] choices = {"Yes", "No"};
				Object choice = AllZone.Display.getChoice("Pay Sangrophage's upkeep?", choices);
				if(choice.equals("Yes")) AllZone.GameAction.getPlayerLife(player).subtractLife(2);
				else c.tap();
			}
		}
		if(player == "Computer") for(int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if(AllZone.Computer_Life.getLife() > 2) AllZone.GameAction.getPlayerLife(player).subtractLife(2);
			else c.tap();
		}
	}// upkeep_Carnophage

	private static void upkeep_Phyrexian_Arena() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Phyrexian Arena");

		for(int i = 0; i < list.size(); i++) {
			AllZone.GameAction.drawCard(player);
			AllZone.GameAction.getPlayerLife(player).subtractLife(1);

			AllZone.GameAction.checkStateEffects();
		}
	}// upkeep_Phyrexian_Arena

	private static void upkeep_Honden_of_Seeing_Winds() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Honden of Seeing Winds");

		for(int i = 0; i < list.size(); i++) {
			//			final Ability ability2 = new Ability(list.get(i), "0")
			//	    {   
			//	public void resolve() {
			PlayerZone Play = AllZone.getZone(Constant.Zone.Play, player);
			CardList hondlist = new CardList();
			hondlist.addAll(Play.getCards());
			hondlist = hondlist.getType("Shrine");
			for(int j = 0; j < hondlist.size(); j++) {
				AllZone.GameAction.drawCard(player);
			}//}
		//  };
			//    ability2.setStackDescription(list.get(i)+" - " + list.get(i).getController() + " draws a card for each Shrine he controls.");
			//	    AllZone.Stack.add(ability2);	
		}

	}// upkeep_Honden_of_Seeing_Winds

	private static void upkeep_Honden_of_Cleansing_Fire() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Honden of Cleansing Fire");

		for(int i = 0; i < list.size(); i++) {
			final Ability ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, player);
					CardList hondlist = new CardList();
					hondlist.addAll(Play.getCards());
					hondlist = hondlist.getType("Shrine");
					for(int j = 0; j < hondlist.size(); j++) {
						AllZone.GameAction.getPlayerLife(player).addLife(2);
					}
				}
			};
			ability.setStackDescription(list.get(i) + " - " + list.get(i).getController()
					+ " gains 2 life for each Shrine he controls.");
			AllZone.Stack.add(ability);
		}

	}// upkeep_Honden_of_Cleansing_Fire

	private static void upkeep_Honden_of_Nights_Reach() {
		final String player = AllZone.Phase.getActivePlayer();
		final String opponent = AllZone.GameAction.getOpponent(player);
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Honden of Night's Reach");

		for(int i = 0; i < list.size(); i++) {
			final Ability ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, player);
					CardList hondlist = new CardList();
					hondlist.addAll(Play.getCards());
					hondlist = hondlist.getType("Shrine");

					for(int j = 0; j < hondlist.size(); j++) {
						if(opponent.equals(Constant.Player.Human)) AllZone.InputControl.setInput(CardFactoryUtil.input_discard());
						else {
							AllZone.GameAction.discardRandom(Constant.Player.Computer);
						}
					}
				}
			};
			ability.setStackDescription(list.get(i) + " - "
					+ AllZone.GameAction.getOpponent(list.get(i).getController())
					+ " discards a card for each Shrine " + list.get(i).getController() + " controls.");
			AllZone.Stack.add(ability);
		}
	}

	// upkeep_Honden_of_Nights_Reach()

	private static void upkeep_Honden_of_Infinite_Rage() {
		final String controller = AllZone.Phase.getActivePlayer();
		//final String opponent = AllZone.GameAction.getOpponent(player);
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, controller);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Honden of Infinite Rage");
		PlayerZone Play = AllZone.getZone(Constant.Zone.Play, controller);
		CardList hondlist = new CardList();
		hondlist.addAll(Play.getCards());
		hondlist = hondlist.getType("Shrine");
		for(int i = 0; i < list.size(); i++) {

			final Card card = list.get(i);
			final Ability ability = new Ability(list.get(i), "0") {


				@Override
				public void resolve() {
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, controller);
					CardList hondlist = new CardList();
					hondlist.addAll(Play.getCards());
					hondlist = hondlist.getType("Shrine");
					if(controller.equals("Human")) {
						String opp = AllZone.GameAction.getOpponent(controller);
						PlayerZone oppPlay = AllZone.getZone(Constant.Zone.Play, opp);

						String[] choices = {"Yes", "No, target a creature instead"};

						Object q = AllZone.Display.getChoiceOptional("Select computer as target?", choices);
						if(q.equals("Yes")) AllZone.GameAction.getPlayerLife(Constant.Player.Computer).subtractLife(
								hondlist.size());
						else {
							CardList cards = new CardList(oppPlay.getCards());
							CardList oppCreatures = new CardList();
							for(int i = 0; i < cards.size(); i++) {
								if(cards.get(i).isPlaneswalker() || cards.get(i).isCreature()) {
									oppCreatures.add(cards.get(i));
								}
							}

							if(oppCreatures.size() > 0) {

								Object o = AllZone.Display.getChoiceOptional("Pick target creature",
										oppCreatures.toArray());
								Card c = (Card) o;
								c.addDamage(hondlist.size(), card);
							}
						}
					}

					else {
						Card targetc = null;
						CardList flying = CardFactoryUtil.AI_getHumanCreature("Flying", card, true);
						if(AllZone.Human_Life.getLife() > hondlist.size() * 2) {
							for(int i = 0; i < flying.size(); i++) {
								if(flying.get(i).getNetDefense() <= hondlist.size()) {
									targetc = flying.get(i);
								}

							}
						}
						if(targetc != null) {
							if(AllZone.GameAction.isCardInPlay(targetc)) targetc.addDamage(hondlist.size(), card);
						} else AllZone.GameAction.getPlayerLife(Constant.Player.Human).subtractLife(
								hondlist.size());
					}
				}//resolve()
			};//SpellAbility

			ability.setStackDescription(list.get(i) + " - Deals " + hondlist.size()
					+ " damage to target creature or player");
			AllZone.Stack.add(ability);


		}

	}// upkeep_Honden_of_Infinite_Rage


	private static void upkeep_Honden_of_Lifes_Web() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Honden of Life's Web");

		for(int i = 0; i < list.size(); i++) {
			final Card crd = list.get(i);
			final Ability ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					PlayerZone Play = AllZone.getZone(Constant.Zone.Play, player);
					CardList hondlist = new CardList();
					hondlist.addAll(Play.getCards());
					hondlist = hondlist.getType("Shrine");
					for(int j = 0; j < hondlist.size(); j++) {
						CardFactoryUtil.makeToken("Spirit", "C 1 1 Spirit", crd, "", new String[] {
								"Creature", "Spirit"}, 1, 1, new String[] {""});
					}
				}
			};
			ability.setStackDescription(list.get(i) + " - " + list.get(i).getController()
					+ " puts a 1/1 colorless Spirit creature token into play for each Shrine he controls.");
			AllZone.Stack.add(ability);
		}

	}// upkeep_Honden_of_Lifes_Web

	private static void upkeep_Seizan_Perverter_of_Truth() {
		final String player = AllZone.Phase.getActivePlayer();

		// get all creatures
		CardList list = new CardList();
		list.addAll(AllZone.Human_Play.getCards());
		list.addAll(AllZone.Computer_Play.getCards());

		list = list.getName("Seizan, Perverter of Truth");

		if(list.size() == 0) return;

		Ability ability = new Ability(list.get(0), "0") {
			@Override
			public void resolve() {
				AllZone.GameAction.getPlayerLife(player).subtractLife(2);
			}
		};
		ability.setStackDescription("Seizan, Perverter of Truth - " + player + " loses 2 life and draws 2 cards");

		AllZone.Stack.add(ability);

		//drawing cards doesn't seem to work during upkeep if it's in an ability
		AllZone.GameAction.drawCard(player);
		AllZone.GameAction.drawCard(player);
	}// upkeep_Seizan_Perverter_of_Truth()

	private static void upkeep_Moroii() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Moroii");

		for(int i = 0; i < list.size(); i++) {
			AllZone.GameAction.getPlayerLife(player).subtractLife(1);
		}
	}// upkeep_Moroii

	private static void upkeep_Vampire_Lacerator() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone play = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList();
		list.addAll(play.getCards());

		list = list.getName("Vampire Lacerator");

		for(int i = 0; i < list.size(); i++) {
			if(player == "Human" && AllZone.Computer_Life.getLife() > 10) {
				AllZone.GameAction.getPlayerLife(player).subtractLife(1);
			} else {
				if(player == "Computer" && AllZone.Human_Life.getLife() > 10) {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			}
		}
	}// upkeep_Vampire_Lacerator

	private static void upkeep_Grinning_Demon() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Grinning Demon");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			if(!list.get(i).isFaceDown()) {
				ability = new Ability(list.get(i), "0") {
					@Override
					public void resolve() {
						AllZone.GameAction.getPlayerLife(player).subtractLife(2);
					}
				};// Ability
				ability.setStackDescription("Grinning Demon - " + player + " loses 2 life");

				AllZone.Stack.add(ability);
			}
		}// for
	}// upkeep_Grinning_Demon()

	private static void upkeep_Juzam_Djinn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Juzam Djinn");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			};// Ability
			ability.setStackDescription("Juzam Djinn - deals 1 damage to " + player);

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Juzam_Djinn()

	private static void upkeep_Fledgling_Djinn() {
		final String player = AllZone.Phase.getActivePlayer();
		PlayerZone playZone = AllZone.getZone(Constant.Zone.Play, player);

		CardList list = new CardList(playZone.getCards());
		list = list.getName("Fledgling Djinn");

		Ability ability;
		for(int i = 0; i < list.size(); i++) {
			ability = new Ability(list.get(i), "0") {
				@Override
				public void resolve() {
					AllZone.GameAction.getPlayerLife(player).subtractLife(1);
				}
			};// Ability
			ability.setStackDescription("Fledgling Djinn - deals 1 damage to " + player);

			AllZone.Stack.add(ability);
		}// for
	}// upkeep_Fledgling_Djinn()

	private static void upkeep_Mirror_Sigil_Sergeant()
	{
		final String player = AllZone.Phase.getActivePlayer();
		CardList list = AllZoneUtil.getPlayerCardsInPlay(player);

		list = list.getName("Mirror-Sigil Sergeant");

		Ability ability;
		for (int i = 0; i < list.size(); i++) {
			final Card card = list.get(i);
			ability = new Ability(card, "0") {
				public void resolve() {
					CardList list = AllZoneUtil.getPlayerCardsInPlay(player);
					CardList blueList = list.getColor("U");
					if (!blueList.isEmpty()) {
						CardFactoryUtil.makeToken("Mirror-Sigil Sergeant","W 4 4 Mirror Sigil Sergeant", card, "5 W",
								new String[]{"Creature","Rhino","Soldier"}, 4, 4, new String[]{"Trample",
								"At the beginning of your upkeep, if you control a blue permanent, you may put a token that's a copy of Mirror-Sigil Sergeant onto the battlefield."});
					}
				};

			}; // ability

			ability.setStackDescription("Mirror-Sigil Sergeant - put a token into play that's a copy of Mirror-Sigil Sergeant.");
			AllZone.Stack.add(ability);
		} // for
	} //upkeep_Mirror_Sigil_Sergeant

	public static void executeCardStateEffects() {
		Wonder.execute();
		Anger.execute();
		Valor.execute();
		Brawn.execute();

		Baru.execute();
		Reach_of_Branches.execute();

		Essence_Warden.execute();
		Soul_Warden.execute();
		Souls_Attendant.execute();
		Wirewood_Hivemaster.execute();

		Sacrifice_NoIslands.execute();
		
		topCardReveal_Update.execute();
		//Angelic_Chorus.execute();
	}// executeCardStateEffects()

	public static Command Conspiracy                  = new Command() {
		private static final long serialVersionUID   = -752798545956593342L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			//String keyword = "Defender";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				//System.out.println("prev type: " +c.getPrevType());
				c.setType(c.getPrevType());
			}

			list.clear();

			PlayerZone[] zone = new PlayerZone[4];

			CardList cl = new CardList();
			cl.addAll(AllZone.Human_Play.getCards());
			cl.addAll(AllZone.Computer_Play.getCards());
			cl = cl.getName("Conspiracy");

			for(int i = 0; i < cl.size(); i++) {
				Card card = cl.get(i);
				String player = card.getController();
				zone[0] = AllZone.getZone(Constant.Zone.Hand,
						player);
				zone[1] = AllZone.getZone(Constant.Zone.Library,
						player);
				zone[2] = AllZone.getZone(
						Constant.Zone.Graveyard, player);
				zone[3] = AllZone.getZone(Constant.Zone.Play,
						player);

				for(int outer = 0; outer < zone.length; outer++) {
					CardList creature = new CardList(
							zone[outer].getCards());
					creature = creature.getType("Creature");

					//System.out.println("zone[" + outer + "] = " + creature.size());

					for(int j = 0; j < creature.size(); j++) {
						boolean art = false;
						boolean ench = false;

						c = creature.get(j);

						if(c.isArtifact()) art = true;
						if(c.isEnchantment()) ench = true;

						if(c.getPrevType().size() == 0) c.setPrevType(c.getType());
						c.setType(new ArrayList<String>());
						c.addType("Creature");
						if(art) c.addType("Artifact");
						if(ench) c.addType("Enchantment");
						c.addType(card.getChosenType());

						gloriousAnthemList.add(c);
					}
				}
			}// for inner
		}// execute()
	}; //Conspiracy

	public static Command Engineered_Plague           = new Command() {
		private static final long serialVersionUID   = -7941528835392424702L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(1);
				c.addSemiPermanentDefenseBoost(1);
			}

			list.clear();
			CardList cards = new CardList();
			cards.addAll(AllZone.Human_Play.getCards());
			cards.addAll(AllZone.Computer_Play.getCards());
			cards = cards.getName("Engineered Plague");

			for(int outer = 0; outer < cards.size(); outer++) {
				Card card = cards.get(outer);

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType(card.getChosenType());

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.getType().contains(card.getChosenType())
							|| c.getKeyword().contains(
									"Changeling")) {
						c.addSemiPermanentAttackBoost(-1);
						c.addSemiPermanentDefenseBoost(-1);

						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; //Engineered Plague

	public static Command Night_of_Souls_Betrayal     = new Command() {
		private static final long serialVersionUID   = 867116049464930958L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(1);
				c.addSemiPermanentDefenseBoost(1);
			}

			list.clear();
			CardList cards = new CardList();
			cards.addAll(AllZone.Human_Play.getCards());
			cards.addAll(AllZone.Computer_Play.getCards());
			cards = cards.getName("Night of Souls' Betrayal");

			for(int outer = 0; outer < cards.size(); outer++) {
				//Card card = cards.get(outer); //unused

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(-1);
					c.addSemiPermanentDefenseBoost(-1);

					gloriousAnthemList.add(c);

				}// for inner
			}// for outer
		}// execute()
	}; //Night of Souls' Betrayal


	public static Command Rolling_Stones              = new Command() {
		private static final long serialVersionUID   = -3317318747868440229L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Defender";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addIntrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Rolling Stones");

			for(int outer = 0; outer < zone.length; outer++) {
				//CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.getKeyword().contains(keyword)
							&& c.getType().contains("Wall")) {
						c.removeIntrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //Rolling Stones

	public static Command Kobold_Overlord             = new Command() {
		private static final long serialVersionUID   = 4620370378774187573L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "First Strike";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Kobold Overlord");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Kobold");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains("First Strike")) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};
	
	public static Command Mul_Daya_Channelers          = new Command() {
		private static final long serialVersionUID   = -2543659953307485051L;

		CardList                  landList = new CardList();
		CardList				  creatList = new CardList();

		String[]                  keyword            = {
				"tap: add B B", "tap: add W W", "tap: add G G", "tap: add U U", "tap: add R R" };

		final void addMana(Card c) {
			for(int i = 0; i < keyword.length; i++) {
				//don't add an extrinsic mana ability if the land can already has the same intrinsic mana ability
				//eg. "tap: add G"
				if(!c.getIntrinsicManaAbilitiesDescriptions().contains(
						keyword[i])) {
					//c.addExtrinsicKeyword(keyword[i]);
					SpellAbility mana = new Ability_Mana(c,
							keyword[i]) {
						private static final long serialVersionUID = 2384540533244132975L;
					};

					mana.setType("Extrinsic");
					c.addSpellAbility(mana);
				}
			}
		}
		final void removeMana(Card c) {
			c.removeAllExtrinsicManaAbilities();
		}

		public void execute() {
			CardList list1 = landList;
			CardList list2 = creatList;
			
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list1.size(); i++) {
				c = list1.get(i);
				removeMana(c);
			}
			
			for(int i = 0; i < list2.size(); i++) {
				c = list2.get(i);
				c.addSemiPermanentAttackBoost(-3);
				c.addSemiPermanentDefenseBoost(-3);
			}


			list1.clear();
			list2.clear();
			CardList cl = AllZoneUtil.getCardsInPlay();
			cl = cl.getName("Mul Daya Channelers");

			for (Card crd:cl)
			{
				if (CardFactoryUtil.getTopCard(crd)!= null)
				{
					Card topCard = CardFactoryUtil.getTopCard(crd);
					if (topCard.isLand()) {
						addMana(crd);
						landList.add(crd);
					}
					else if(topCard.isCreature())
					{
						crd.addSemiPermanentAttackBoost(3);
						crd.addSemiPermanentDefenseBoost(3);
						creatList.add(crd);
					}
						

				}
			}// for outer
		}// execute()
	}; // Mul Daya
	

	//moved to Card.addExtrinsicAbilities

	public static Command Joiner_Adept                = new Command() {
		private static final long serialVersionUID   = -2543659953307485051L;

		CardList                  gloriousAnthemList = new CardList();

		String[]                  keyword            = {
				"tap: add B", "tap: add W", "tap: add G", "tap: add U", "tap: add R"                       };

		final void addMana(Card c) {
			for(int i = 0; i < keyword.length; i++) {
				//don't add an extrinsic mana ability if the land can already has the same intrinsic mana ability
				//eg. "tap: add G"
				if(!c.getIntrinsicManaAbilitiesDescriptions().contains(
						keyword[i])) {
					//c.addExtrinsicKeyword(keyword[i]);
					SpellAbility mana = new Ability_Mana(c,
							keyword[i]) {
						private static final long serialVersionUID = 2384540533244132975L;
					};

					mana.setType("Extrinsic");
					c.addSpellAbility(mana);
				}
			}
		}

		final void removeMana(Card c) {

			/*
                                                              for (int i = 0; i < keyword.length; i++)
                                                              	c.removeExtrinsicKeyword(keyword[i]);
			 */
			c.removeAllExtrinsicManaAbilities();
		}

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				removeMana(c);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Joiner Adept");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length && outer < 1; outer++) // 1
				// is
				// a
				// cheat
			{
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Land");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					addMana(c);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Battering_Sliver            = new Command() {
		private static final long serialVersionUID   = -2214824705109236342L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Trample";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Battering Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Blade Sliver

	public static Command Marrow_Gnawer               = new Command() {
		private static final long serialVersionUID   = -2500490393763095527L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Fear";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Marrow-Gnawer");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Rat");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Marrow-Gnawer

	public static Command Blade_Sliver                = new Command() {
		private static final long serialVersionUID   = 5059367392983499740L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 1;
			int pumpDefense = 0;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Blade Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Gemhide_Sliver              = new Command() {
		private static final long serialVersionUID   = -2941784982910968772L;

		CardList                  gloriousAnthemList = new CardList();

		String[]                  keyword            = {
				"tap: add B", "tap: add W", "tap: add G", "tap: add U", "tap: add R"                       };

		final void addMana(Card c) {

			for(int i = 0; i < keyword.length; i++) {
				if(!c.getIntrinsicManaAbilitiesDescriptions().contains(
						keyword[i])) {
					//c.addExtrinsicKeyword(keyword[i]);
					SpellAbility mana = new Ability_Mana(c,
							keyword[i]) {
						private static final long serialVersionUID = -8909660504657778172L;
					};
					mana.setType("Extrinsic");
					c.addSpellAbility(mana);
				}
			}
		}

		final void removeMana(Card c) {
			/*
                                                              for (int i = 0; i < keyword.length; i++)
                                                              	c.removeExtrinsicKeyword(keyword[i]);
			 */
			c.removeAllExtrinsicManaAbilities();
		}

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				removeMana(c);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Gemhide Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length && outer < 1; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					addMana(c);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Heart_Sliver                = new Command() {
		private static final long serialVersionUID   = -3213253353499680447L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Heart Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Heart_Sliver

	public static Command Reflex_Sliver               = new Command() {
		private static final long serialVersionUID   = -6606809422365563893L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Reflex Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Reflex_Sliver

	public static Command Horned_Sliver               = new Command() {
		private static final long serialVersionUID   = 4789073152424705372L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Trample";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Horned Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Sinew_Sliver                = new Command() {
		private static final long serialVersionUID   = -4633694634393704728L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 1;
			int pumpDefense = 1;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Sinew Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Winged_Sliver               = new Command() {
		private static final long serialVersionUID   = -1840399835079335499L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Flying";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Winged Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Knighthood                  = new Command() {

		private static final long serialVersionUID   = -6904191523315339355L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "First Strike";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Knighthood");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Darksteel_Forge 			  = new Command() {

		private static final long serialVersionUID = 7212793187625704417L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Indesctructible";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Darksteel Forge");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList arts = new CardList(
						zone[outer].getCards());
				arts = arts.getType("Artifact");

				for(int i = 0; i < arts.size(); i++) {
					c = arts.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()

	};

	public static Command Levitation                  = new Command() {

		private static final long serialVersionUID   = -6707183535529395830L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Flying";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Levitation");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	/**
	 * stores the Command to execute the "Legends don't untap during your untap step"
	 */
	public static Command Arena_of_the_Ancients = new Command() {
		private static final long serialVersionUID = -3233715310427996429L;
		
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "CARDNAME doesn't untap during your untap step.";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}
			list.clear();

			if(AllZoneUtil.isCardInPlay("Arena of the Ancients")) {
				CardList legends = AllZoneUtil.getTypeInPlay("Legendary");
				legends = legends.filter(AllZoneUtil.creatures);
				for(int i = 0; i < legends.size(); i++) {
					c = legends.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}//for
			}//if
		}// execute()
	};

	public static Command Absolute_Grace              = new Command() {
		private static final long serialVersionUID   = -6904191523315339355L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Protection from black";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Absolute Grace");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};


	public static Command Absolute_Law                = new Command() {

		private static final long serialVersionUID   = -6707183535529395830L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Protection from red";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Absolute Law");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Tabernacle                  = new Command() {
		private static final long serialVersionUID   = -3233715310427996429L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "At the beginning of your upkeep, destroy this creature unless you pay";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				ArrayList<String> a = c.getKeyword();
				for(String s:a) {
					if(s.startsWith(keyword)) c.removeExtrinsicKeyword(s);
				}
			}

			list.clear();
			PlayerZone cPlay = AllZone.Computer_Play;
			PlayerZone hPlay = AllZone.Human_Play;
			CardList clist = new CardList();
			clist.addAll(cPlay.getCards());
			clist.addAll(hPlay.getCards());
			clist = clist.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"The Tabernacle at Pendrell Vale"); /*|| c.getName().equals("Magus of the Tabernacle");*/
				}
			});

			int number = clist.size();
			//System.out.println("Tabernacle Number:" + number);
			if(number > 0) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword + " " + number
							+ ".");
					gloriousAnthemList.add(c);
				}// for inner
			}
		}// execute()
	};

	public static Command Magus_of_the_Tabernacle     = new Command() {
		private static final long serialVersionUID   = -249708982895077034L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "At the beginning of your upkeep, sacrifice this creature unless you pay";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				ArrayList<String> a = c.getKeyword();
				for(String s:a) {
					if(s.startsWith(keyword)) c.removeExtrinsicKeyword(s);
				}
			}

			list.clear();
			PlayerZone cPlay = AllZone.Computer_Play;
			PlayerZone hPlay = AllZone.Human_Play;
			CardList clist = new CardList();
			clist.addAll(cPlay.getCards());
			clist.addAll(hPlay.getCards());
			clist = clist.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"Magus of the Tabernacle");
				}
			});

			int number = clist.size();
			//System.out.println("Tabernacle Number:" + number);
			if(number > 0) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword + " " + number
							+ ".");
					gloriousAnthemList.add(c);
				}// for inner
			}
		}// execute()
	};


	public static Command Serras_Blessing             = new Command() {
		private static final long serialVersionUID   = -6904191523315339355L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Vigilance";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Serra's Blessing");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Cover_of_Darkness           = new Command() {
		private static final long serialVersionUID   = -6707183535529395830L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword("Fear");
			}

			list.clear();
			CardList cards = new CardList();
			cards.addAll(AllZone.Human_Play.getCards());
			cards.addAll(AllZone.Computer_Play.getCards());
			cards = cards.getName("Cover of Darkness");

			for(int outer = 0; outer < cards.size(); outer++) {
				Card card = cards.get(outer);

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType(card.getChosenType());

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.getType().contains(card.getChosenType())
							|| c.getKeyword().contains(
									"Changeling")) {
						c.addExtrinsicKeyword("Fear");
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; //Cover of Darkness

	public static Command Steely_Resolve              = new Command() {

		private static final long serialVersionUID   = 2005579284163773044L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword("Shroud");
			}

			list.clear();
			CardList cards = new CardList();
			cards.addAll(AllZone.Human_Play.getCards());
			cards.addAll(AllZone.Computer_Play.getCards());
			cards = cards.getName("Steely Resolve");

			for(int outer = 0; outer < cards.size(); outer++) {
				Card card = cards.get(outer);

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType(card.getChosenType());

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.getType().contains(card.getChosenType())
							|| c.getKeyword().contains(
									"Changeling")) {
						c.addExtrinsicKeyword("Shroud");
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; //Steely Resolve

	public static Command Goblin_Assault                = new Command() {

		private static final long serialVersionUID = 5138624295158786103L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "CARDNAME attacks each turn if able.";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Goblin Assault");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Goblin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //Goblin Assault               
	

	public static Command Mobilization                = new Command() {
		private static final long serialVersionUID   = 2005579284163773044L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Vigilance";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Mobilization");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //mobilization

	public static Command That_Which_Was_Taken        = new Command() {
		private static final long serialVersionUID   = -4142514935709694293L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Indestructible";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("That Which Was Taken");

			if(zone.length > 0) {
				CardList cards = new CardList();
				cards.addAll(AllZone.Human_Play.getCards());
				cards.addAll(AllZone.Computer_Play.getCards());

				for(int i = 0; i < cards.size(); i++) {
					c = cards.get(i);
					if(!c.getKeyword().contains(keyword)
							&& c.getCounters(Counters.DIVINITY) > 0) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};


	public static Command Concordant_Crossroads       = new Command() {
		private static final long serialVersionUID   = -6811663469245799727L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Concordant Crossroads");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Mass_Hysteria               = new Command() {
		private static final long serialVersionUID   = 8171915479339460306L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Mass Hysteria");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Fervor                      = new Command() {
		private static final long serialVersionUID   = -826876381048543684L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Fervor");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Madrush_Cyclops             = new Command() {

		private static final long serialVersionUID   = -2379786355503597363L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Haste";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Madrush Cyclops");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //Madrush Cyclops


	public static Command Sun_Quan                    = new Command() {

		private static final long serialVersionUID   = -2379786355503597363L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Horsemanship";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Sun Quan, Lord of Wu");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getKeyword().contains(keyword)) {
						c.addExtrinsicKeyword(keyword);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //Sun Quan

	public static Command Kinsbaile_Cavalier          = new Command() {
		private static final long serialVersionUID   = -4124745123035715658L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Double Strike";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Kinsbaile Cavalier");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Knight");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Wrens_Run_Packmaster        = new Command() {

		private static final long serialVersionUID   = 6089293045852070662L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Deathtouch";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Wren's Run Packmaster");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Wolf");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};


	public static Command Fury_Sliver                 = new Command() {
		private static final long serialVersionUID   = -2379786355503597363L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Double Strike";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Fury Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Talon_Sliver                 = new Command() {

		private static final long serialVersionUID = -7392607614574103064L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "First Strike";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Talon Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};    

	public static Command Crystalline_Sliver          = new Command() {

		private static final long serialVersionUID   = 6089293045852070662L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Shroud";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Crystalline Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Sidewinder_Sliver           = new Command() {

		private static final long serialVersionUID   = 4336346186741907749L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Flanking";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Sidewinder Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Virulent_Sliver           = new Command() {
		private static final long serialVersionUID = 2755343097020369210L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Poisonous 1";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Virulent Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addStackingExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Essence_Sliver              = new Command() {
		private static final long serialVersionUID   = 6089293045852070662L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Lifelink";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Essence Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Synchronous_Sliver          = new Command() {
		private static final long serialVersionUID   = 4336346186741907749L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			String keyword = "Vigilance";

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword(keyword);
			}

			list.clear();
			PlayerZone[] zone = getZone("Synchronous Sliver");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword(keyword);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	};

	public static Command Bonesplitter_Sliver         = new Command() {
		private static final long serialVersionUID   = -3463429634177142721L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 2;
			int pumpDefense = 0;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Bonesplitter Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Might_Sliver                = new Command() {
		private static final long serialVersionUID   = 1618762378975019557L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 2;
			int pumpDefense = 2;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Might Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver

	public static Command Plated_Sliver               = new Command() {
		private static final long serialVersionUID   = 7670935990022098909L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 0;
			int pumpDefense = 1;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			list.clear();
			PlayerZone[] zone = getZone("Plated Sliver");

			// for each zone found add +0/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Watcher_Sliver

	public static Command Watcher_Sliver              = new Command() {
		private static final long serialVersionUID   = -3148897786330400205L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			int pumpAttack = 0;
			int pumpDefense = 2;

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pumpAttack);
				c.addSemiPermanentDefenseBoost(-pumpDefense);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Watcher Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(pumpAttack);
					c.addSemiPermanentDefenseBoost(pumpDefense);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Watcher_Sliver

	public static Command Muscle_Sliver               = new Command() {
		private static final long serialVersionUID   = -2791476542570951362L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Muscle Sliver");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Sliver");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Muscles_Sliver
	
	public static Command Sliver_Legion               = new Command() {
		private static final long serialVersionUID   = -4564640511791858445L;

		CardList                  gloriousAnthemList = new CardList();
		int                       pump               = 0;

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-pump);
				c.addSemiPermanentDefenseBoost(-pump);
			}

			// add +pump/+pump to cards
			list.clear();
			//PlayerZone[] zone = getZone("Sliver Legion");

			// get all Slivers
			CardList all = new CardList();
			all.addAll(AllZone.Human_Play.getCards());
			all.addAll(AllZone.Computer_Play.getCards());

			CardList allSliver = all.getType("Sliver");
			pump = allSliver.size() - 1;  //it's for each *other* Sliver in play
			
			//slapshot5 - outer loop not needed.  This applies to *all* Slivers
			// for each zone found add +pump/+pump to each card
			/*
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Sliver");
				*/

				for(int i = 0; i < allSliver.size(); i++) {
					c = allSliver.get(i);
					c.addSemiPermanentAttackBoost(pump);
					c.addSemiPermanentDefenseBoost(pump);

					gloriousAnthemList.add(c);
				}// for inner
			//}// for outer
		}// execute()
	}; // Sliver_Legion

	public static Command Serra_Avatar                = new Command() {
		private static final long serialVersionUID = -7560281839252561370L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Serra Avatar");

			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);
				int n = AllZone.GameAction.getPlayerLife(
						card.getController()).getLife();
				card.setBaseAttack(n);
				card.setBaseDefense(n);
			}// for
		}// execute
	}; // Serra Avatar

	public static Command Ajani_Avatar_Token          = new Command() {
		private static final long serialVersionUID = 3027329837165436727L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());

			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals("Avatar")
					&& c.getImageName().equals(
							"W N N Avatar");
				}
			});
			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);
				int n = AllZone.GameAction.getPlayerLife(
						card.getController()).getLife();
				card.setBaseAttack(n);
				card.setBaseDefense(n);
			}// for
		}// execute
	}; // Serra Avatar

	public static Command Windwright_Mage             = new Command() {
		private static final long serialVersionUID = 7208941897570511298L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Windwright Mage");

			// Cardlist artifacts = new CardList()

			for(int i = 0; i < list.size(); i++) {
				Card card = list.get(i);

				String player = card.getController();
				PlayerZone graveyard = AllZone.getZone(
						Constant.Zone.Graveyard, player);

				CardList artifacts = new CardList(
						graveyard.getCards());
				artifacts = artifacts.getType("Artifact");

				if(artifacts.size() > 0) {
					if(!card.getKeyword().contains("Flying")) {
						card.addExtrinsicKeyword("Flying");
					}
				} else {
					// this is tricky, could happen that flying is wrongfully
					// removed... not sure?
							card.removeExtrinsicKeyword("Flying");
				}

			}// for
		}// execute
	}; // Windwright Mage

	// Reach of Branches
	public static Command Reach_of_Branches           = new Command() {
		private static final long serialVersionUID = 9191592685635589492L;

		CardList                  oldForest        = new CardList();

		public void execute() {
			// count card "Reach of Branches" in graveyard
			final String player = AllZone.Phase.getActivePlayer();
			final PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, player);
			CardList tempList = new CardList(grave.getCards());
			final CardList nCard = tempList.getName("Reach of Branches");

			// get all Forest that player has
			final PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList newForest = new CardList(play.getCards());
			newForest = newForest.getType("Forest");

			// if "Reach of Branches" is in graveyard and played a Forest
			if(0 < nCard.size()
					&& newForest(oldForest, newForest)) {
				SpellAbility ability = new Ability(new Card(),
						"0") {
					@Override
					public void resolve() {
						// return all Reach of Branches to hand
						PlayerZone hand = AllZone.getZone(
								Constant.Zone.Hand, player);
						for(int i = 0; i < nCard.size(); i++) {
							grave.remove(nCard.get(i));
							hand.add(nCard.get(i));
						}
					}// resolve()
				};// SpellAbility
				ability.setStackDescription("Reach of Branches - return card to "
						+ player + "'s hand");
				AllZone.Stack.add(ability);
			}// if

			// potential problem: if a Forest is bounced to your hand
			// "Reach Branches"
			// won't trigger when you play that Forest
			oldForest.addAll(newForest.toArray());
		}// execute

		// check if newList has anything that oldList doesn't have
		boolean newForest(CardList oldList, CardList newList) {
			// check if a Forest came into play under your control
			for(int i = 0; i < newList.size(); i++)
				if(!oldList.contains(newList.get(i))) return true;

			return false;
		}// newForest()
	}; // Reach of Branches

	public static Command Mad_Auntie                  = new Command() {
		private static final long serialVersionUID   = 7969640438477308299L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Mad Auntie");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Goblin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute
	}; // Mad_Auntie()

	public static Command Imperious_Perfect           = new Command() {
		private static final long serialVersionUID   = 5835056455026735693L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Imperious Perfect");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Elf");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute
	}; // Imperious_Perfect()

	public static Command Baru                        = new Command() {
		private static final long serialVersionUID = 7535910275326543185L;

		CardList                  old              = new CardList();

		public void execute() {
			// get all Forests
			CardList all = new CardList();
			all.addAll(AllZone.Human_Play.getCards());
			all.addAll(AllZone.Computer_Play.getCards());
			CardList current = all.getType("Forest");

			for(int outer = 0; outer < current.size(); outer++) {
				if(old.contains(current.get(outer))) continue;

				final CardList test = all.getName("Baru, Fist of Krosa");
				SpellAbility ability = new Ability(new Card(),
						"0") {
					@Override
					public void resolve() {
						Card c = test.get(0);

						CardList all = new CardList(
								AllZone.getZone(
										Constant.Zone.Play,
										c.getController()).getCards());

						all = all.filter(new CardListFilter() {
							public boolean addCard(Card c) {
								return c.isCreature()
								&& CardUtil.getColors(c).contains(
										Constant.Color.Green);
							}
						});

						for(int i = 0; i < all.size(); i++) {
							all.get(i).addTempAttackBoost(1);
							all.get(i).addTempDefenseBoost(1);
							all.get(i).addExtrinsicKeyword(
									"Trample");

							final Card c1 = all.get(i);
							AllZone.EndOfTurn.addUntil(new Command() {
								private static final long serialVersionUID = 3659932873866606966L;

								public void execute() {
									c1.addTempAttackBoost(-1);
									c1.addTempDefenseBoost(-1);
									c1.removeExtrinsicKeyword("Trample");
								}
							});
						}// for
					}
				};
				ability.setStackDescription("Baru, Fist of Krosa - creatures get +1/+1 until end of turn.");

				if(!all.getName("Baru, Fist of Krosa").isEmpty()) AllZone.Stack.push(ability);
			}// outer for

			old = current;
		}// execute()
	}; // Baru
	public static Command Essence_Warden              = new Command() {
		private static final long serialVersionUID = 6515549135916060107L;

		// Hold old creatures
		CardList                  old              = new CardList();      // Hold old Essence Wardens
		CardList                  essence          = new CardList();

		public void execute() {
			// get all creatures
			CardList current = new CardList();
			current.addAll(AllZone.Human_Play.getCards());
			current.addAll(AllZone.Computer_Play.getCards());

			final ArrayList<String> list = CardFactoryUtil.getCreatureLandNames();

			current = current.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature()
					&& !list.contains(c.getName());
				}
			});


			// Holds Essence Warden's in play
			CardList wardenList = current.getName("Essence Warden");

			// Holds Essence Warden's that are new to play
			CardList newWarden = new CardList();

			// Go through the list of Essence Warden's in play
			for(int i = 0; i < wardenList.size(); i++) {
				Card c = wardenList.get(i);

				// Check to see which Essence Warden's in play are new
				if(!essence.contains(c)) {
					newWarden.add(c);
					wardenList.remove(c);
					i -= 1; // Must do as a card was just removed
				}

				current.remove(c);
			}

			for(int outer = 0; outer < wardenList.size(); outer++) {
				// Gain life for new creatures in play - excluding any new
				// Essence Wardens
				final int[] n = new int[1];
				for(int i = 0; i < current.size(); i++) {
					if(!old.contains(current.getCard(i))) {
						n[0]++;
					}
				}

				// Gain life for new Essence Wardens
				n[0] += newWarden.size();

				final PlayerLife life = AllZone.GameAction.getPlayerLife(wardenList.get(
						outer).getController());
				SpellAbility ability = new Ability(new Card(),
						"0") {
					@Override
					public void resolve() {
						life.addLife(n[0]);
					}
				};
				ability.setStackDescription(wardenList.get(outer).getName()
						+ " - "
						+ wardenList.get(outer).getController()
						+ " gains " + n[0] + " life");

				if(n[0] != 0) {
					AllZone.Stack.push(ability);
				}
			}// outer for

			essence = wardenList;
			essence.addAll(newWarden.toArray());
			old = current;
		}// execute()
	}; // essence warden
	public static Command Soul_Warden                 = new Command() {
		private static final long serialVersionUID = 5099736949744748496L;

		// Hold old creatures
		CardList                  old              = new CardList();      // Hold old Soul Wardens
		CardList                  soul             = new CardList();

		public void execute() {
			// get all creatures
			CardList current = new CardList();
			current.addAll(AllZone.Human_Play.getCards());
			current.addAll(AllZone.Computer_Play.getCards());
			//current = current.getType("Creature");

			final ArrayList<String> list = CardFactoryUtil.getCreatureLandNames();

			current = current.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature()
					&& !list.contains(c.getName());
				}
			});

			// Holds Soul Warden's in play
			CardList wardenList = current.getName("Soul Warden");

			// Holds Soul Warden's that are new to play
			CardList newWarden = new CardList();

			// Go through the list of Soul Warden's in play
			for(int i = 0; i < wardenList.size(); i++) {
				Card c = wardenList.get(i);

				// Check to see which Soul Warden's in play are new
				if(!soul.contains(c)) {
					newWarden.add(c);
					wardenList.remove(c);
					i -= 1; // Must do as a card was just removed
				}

				current.remove(c);
			}

			for(int outer = 0; outer < wardenList.size(); outer++) {
				// Gain life for new creatures in play - excluding any new Soul
				// Wardens
				final int[] n = new int[1];
				for(int i = 0; i < current.size(); i++) {
					if(!old.contains(current.getCard(i))) {
						n[0]++;
					}
				}

				// Gain life for new Soul Wardens
				n[0] += newWarden.size();

				final PlayerLife life = AllZone.GameAction.getPlayerLife(wardenList.get(
						outer).getController());
				SpellAbility ability = new Ability(new Card(),
						"0") {

					@Override
					public void resolve() {
						life.addLife(n[0]);
					}
				};
				ability.setStackDescription(wardenList.get(outer).getName()
						+ " - "
						+ wardenList.get(outer).getController()
						+ " gains " + n[0] + " life");

				if(n[0] != 0) {
					AllZone.Stack.push(ability);
				}
			}// outer for

			soul = wardenList;
			soul.addAll(newWarden.toArray());
			old = current;
		}// execute()
	}; // soul warden
	
	public static Command Souls_Attendant                 = new Command() {
		private static final long serialVersionUID = -472504539729742971L;
		// Hold old creatures
		CardList                  old              = new CardList();      // Hold old Soul Wardens
		CardList                  soul             = new CardList();

		public void execute() {
			// get all creatures
			CardList current = new CardList();
			current.addAll(AllZone.Human_Play.getCards());
			current.addAll(AllZone.Computer_Play.getCards());
			//current = current.getType("Creature");

			final ArrayList<String> list = CardFactoryUtil.getCreatureLandNames();

			current = current.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature()
					&& !list.contains(c.getName());
				}
			});

			// Holds Soul Warden's in play
			CardList wardenList = current.getName("Soul's Attendant");

			// Holds Soul Warden's that are new to play
			CardList newWarden = new CardList();

			// Go through the list of Soul Warden's in play
			for(int i = 0; i < wardenList.size(); i++) {
				Card c = wardenList.get(i);

				// Check to see which Soul Warden's in play are new
				if(!soul.contains(c)) {
					newWarden.add(c);
					wardenList.remove(c);
					i -= 1; // Must do as a card was just removed
				}

				current.remove(c);
			}

			for(int outer = 0; outer < wardenList.size(); outer++) {
				// Gain life for new creatures in play - excluding any new Soul
				// Wardens
				final int[] n = new int[1];
				for(int i = 0; i < current.size(); i++) {
					if(!old.contains(current.getCard(i))) {
						n[0]++;
					}
				}

				// Gain life for new Soul's Attendants
				n[0] += newWarden.size();

				final PlayerLife life = AllZone.GameAction.getPlayerLife(wardenList.get(
						outer).getController());
				SpellAbility ability = new Ability(new Card(),
						"0") {

					@Override
					public void resolve() {
						life.addLife(n[0]);
					}
				};
				ability.setStackDescription(wardenList.get(outer).getName()
						+ " - "
						+ wardenList.get(outer).getController()
						+ " gains " + n[0] + " life");

				if(n[0] != 0) {
					AllZone.Stack.push(ability);
				}
			}// outer for

			soul = wardenList;
			soul.addAll(newWarden.toArray());
			old = current;
		}// execute()
	}; // soul's Attendant

	public static Command Wirewood_Hivemaster         = new Command() {
		private static final long serialVersionUID = -6440532066018273862L;

		// Hold old creatures
		CardList                  old              = new CardList();       // Hold old Wirewood Hivemasters
		CardList                  wirewood         = new CardList();

		public void execute() {
			// get all creatures
			CardList current = new CardList();
			current.addAll(AllZone.Human_Play.getCards());
			current.addAll(AllZone.Computer_Play.getCards());
			current = current.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return !c.isToken()
					&& (c.getType().contains("Elf") || c.getKeyword().contains(
							"Changeling"));
				}

			});

			// Holds Soul Warden's in play
			CardList hivemasterList = current.getName("Wirewood Hivemaster");

			// Holds Soul Warden's that are new to play
			CardList newHivemaster = new CardList();

			// Go through the list of Soul Warden's in play
			for(int i = 0; i < hivemasterList.size(); i++) {
				Card c = hivemasterList.get(i);

				// Check to see which Soul Warden's in play are new
				if(!wirewood.contains(c)) {
					newHivemaster.add(c);
					hivemasterList.remove(c);
					i -= 1; // Must do as a card was just removed
				}

				current.remove(c);
			}

			for(int outer = 0; outer < hivemasterList.size(); outer++) {

				final int[] n = new int[1];
				for(int i = 0; i < current.size(); i++) {
					if(!old.contains(current.getCard(i))) {
						n[0]++;
					}
				}

				// Gain life for new Soul Wardens
				n[0] += newHivemaster.size();
				final Card crd = hivemasterList.get(outer);

				SpellAbility ability = new Ability(new Card(),
						"0") {

					@Override
					public void resolve() {
						for(int i = 0; i < n[0]; i++) {
							CardFactoryUtil.makeToken("Insect",
									"G 1 1 Insect", crd, "G",
									new String[] {
									"Creature", "Insect"                                                      }, 1, 1, new String[] {""});
						}
					}
				};
				ability.setStackDescription(hivemasterList.get(
						outer).getName()
						+ " - "
						+ hivemasterList.get(outer).getController()
						+ " puts "
						+ n[0]
						    + " insect tokens into play.");

				if(n[0] != 0) {
					AllZone.Stack.push(ability);
				}
			}// outer for

			wirewood = hivemasterList;
			wirewood.addAll(newHivemaster.toArray());
			old = current;
		}// execute()
	}; // soul warden


	public static Command Angelic_Chorus              = new Command() {
		private static final long serialVersionUID = 296710856999966577L;

		CardList                  old              = new CardList();

		public void execute() {
			// Set up current player
			final String player = AllZone.Phase.getActivePlayer();
			PlayerZone playZone = AllZone.getZone(
					Constant.Zone.Play, player);

			// List of all cards
			CardList playerCards = new CardList(
					playZone.getCards());

			// List of Angelic Chorus
			CardList angelicChorus = playerCards.getName("Angelic Chorus");

			// List of player creature cards
			playerCards = playerCards.getType("Creature");

			// Holds new cards
			CardList newCards = new CardList();

			for(int i = 0; i < angelicChorus.size(); i++) {
				final int[] lifeGain = new int[1];
				for(int j = 0; j < playerCards.size(); j++) {
					Card c = playerCards.get(j);
					if(!old.contains(c)) {
						lifeGain[0] += c.getNetDefense();
						if(!newCards.contains(c)) {
							newCards.add(c);
						}
					}
				}

				// Ability
				final PlayerLife life = AllZone.GameAction.getPlayerLife(player);
				SpellAbility ability = new Ability(new Card(),
						"0") {

					@Override
					public void resolve() {
						life.addLife(lifeGain[0]);
					}
				};
				ability.setStackDescription(angelicChorus.get(i).getName()
						+ " - "
						+ angelicChorus.get(i).getController()
						+ " gains " + lifeGain[0] + " life");

				// Only run if something new to add
				if(lifeGain[0] != 0) {
					AllZone.Stack.push(ability);
				}
			}// outer for

			// Add new cards to old list
			old.addAll(newCards.toArray());
		}// execute()
	};
	
	public static Command Lighthouse_Chronologist  = new Command() {
		
		private static final long serialVersionUID = 2627513737024865169L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Lighthouse Chronologist");
			
			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 4)
				{
					c.setBaseAttack(1);
					c.setBaseDefense(3);
				}
				else if ( lcs >=4 && lcs < 7 )
				{
					c.setBaseAttack(2);
					c.setBaseDefense(4);
				}
				else
				{
					c.setBaseAttack(3);
					c.setBaseDefense(5);
				}
			}
		}
	};
	
	public static Command Skywatcher_Adept  = new Command() {
		private static final long serialVersionUID = -7568530551652446195L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Skywatcher Adept");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(1);
					c.setBaseDefense(1);
					c.removeIntrinsicKeyword("Flying");
				}
				else if ( lcs >=1 && lcs < 3 )
				{
					c.setBaseAttack(2);
					c.setBaseDefense(2);
					c.addNonStackingIntrinsicKeyword("Flying");
				}
				else
				{
					c.setBaseAttack(4);
					c.setBaseDefense(2);
					c.addNonStackingIntrinsicKeyword("Flying");
				}
			}
		}
	};
	
	public static Command Caravan_Escort  = new Command() {
		private static final long serialVersionUID = -6996623102170747897L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Caravan Escort");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
				else if ( lcs >=1 && lcs < 5 )
				{
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
				else
				{
					c.setBaseAttack(5);
					c.setBaseDefense(5);
					c.addNonStackingIntrinsicKeyword("First Strike");
				}
			}
		}
	};
	
	public static Command Ikiral_Outrider  = new Command() {
		private static final long serialVersionUID = 7835884582225439851L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Ikiral Outrider");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(1);
					c.setBaseDefense(2);
				}
				else if ( lcs >=1 && lcs < 4 )  //levels 1-3
				{
					c.setBaseAttack(2);
					c.setBaseDefense(6);
					c.addNonStackingIntrinsicKeyword("Vigilance");
				}
				else
				{
					c.setBaseAttack(3);
					c.setBaseDefense(10);
					c.addNonStackingIntrinsicKeyword("Vigilance");
				}
			}
		}
	};
	
	public static Command Knight_of_Cliffhaven  = new Command() {
		private static final long serialVersionUID = 3624165284236103054L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Knight of Cliffhaven");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
				else if ( lcs >=1 && lcs < 4 )  //levels 1-3
				{
					c.setBaseAttack(2);
					c.setBaseDefense(3);
					c.addNonStackingIntrinsicKeyword("Flying");
				}
				else
				{
					c.setBaseAttack(4);
					c.setBaseDefense(4);
					c.addNonStackingIntrinsicKeyword("Flying");
					c.addNonStackingIntrinsicKeyword("Vigilance");
				}
			}
		}
	};
	
	public static Command Beastbreaker_of_Bala_Ged  = new Command() {
		private static final long serialVersionUID = -8692202913296782937L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Beastbreaker of Bala Ged");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
				else if ( lcs >=1 && lcs < 4 )  //levels 1-3
				{
					c.setBaseAttack(4);
					c.setBaseDefense(4);
				}
				else
				{
					c.setBaseAttack(6);
					c.setBaseDefense(6);
					c.addNonStackingIntrinsicKeyword("Trample");
				}
			}
		}
	};
	
	public static Command Hada_Spy_Patrol  = new Command() {
		private static final long serialVersionUID = 2343715852240209999L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Hada Spy Patrol");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
				else if ( lcs >=1 && lcs < 3 )  //levels 1-2
				{
					c.setBaseAttack(2);
					c.setBaseDefense(2);
					c.addNonStackingIntrinsicKeyword("Unblockable");
				}
				else
				{
					c.setBaseAttack(4);
					c.setBaseDefense(4);
					c.addNonStackingIntrinsicKeyword("Unblockable");
					c.addNonStackingIntrinsicKeyword("Shroud");
				}
			}
		}
	};
	
	public static Command Halimar_Wavewatch  = new Command() {
		private static final long serialVersionUID = 117755207922239944L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Halimar Wavewatch");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(0);
					c.setBaseDefense(3);
				}
				else if ( lcs >=1 && lcs < 5 )  //levels 1-4
				{
					c.setBaseAttack(0);
					c.setBaseDefense(6);
				}
				else
				{
					c.setBaseAttack(6);
					c.setBaseDefense(6);
					c.addNonStackingIntrinsicKeyword("Islandwalk");
				}
			}
		}
	};
	
	/*
	 * Level up 2 B
	 * LEVEL 1-2 4/3 Deathtouch
	 * LEVEL 3+ 5/4 First strike, deathtouch
	 */
	public static Command Nirkana_Cutthroat  = new Command() {
		private static final long serialVersionUID = 3804539422363462063L;

		public void execute()
		{
			/* CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Nirkana Cutthroat"); */
			CardList list = AllZoneUtil.getCardsInPlay("Nirkana Cutthroat");

			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1)
				{
					c.setBaseAttack(3);
					c.setBaseDefense(2);
				}
				else if ( lcs >=1 && lcs < 3 )  //levels 1-2
				{
					c.setBaseAttack(4);
					c.setBaseDefense(3);
					c.addNonStackingIntrinsicKeyword("Deathtouch");
				}
				else
				{
					c.setBaseAttack(5);
					c.setBaseDefense(4);
					c.addNonStackingIntrinsicKeyword("Deathtouch");
					c.addNonStackingIntrinsicKeyword("First strike");
				}
			}
		}
	};
	
	/*
	 * Level up 4
	 * LEVEL 1-2 3/3
	 * LEVEL 3+ 5/5 CARDNAME can't be blocked except by black creatures.
	 */
	public static Command Zulaport_Enforcer  = new Command() {
		private static final long serialVersionUID = -679141054963080569L;

		public void execute(){
			CardList list = AllZoneUtil.getCardsInPlay("Zulaport Enforcer");

			for (Card c:list) {
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 1) {
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
				else if ( lcs >=1 && lcs < 3 ) {  //levels 1-2
					c.setBaseAttack(3);
					c.setBaseDefense(3);
				}
				else {
					c.setBaseAttack(5);
					c.setBaseDefense(5);
					c.addNonStackingIntrinsicKeyword("CARDNAME can't be blocked except by black creatures.");
				}
			}
		}
	};
	
	public static Command Student_of_Warfare 		  = new Command() {
		private static final long serialVersionUID = 2627513737024865169L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Student of Warfare");
			
			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 2)
				{
					c.setBaseDefense(1);
					c.setBaseAttack(1);
					c.removeIntrinsicKeyword("First Strike");
					c.removeIntrinsicKeyword("Double Strike");
				}
				else if ( lcs >=2 && lcs < 7 )
				{
					c.setBaseDefense(3);
					c.setBaseAttack(3);
					c.addNonStackingIntrinsicKeyword("First Strike");
				}
				else
				{
					c.setBaseDefense(4);
					c.setBaseAttack(4);
					c.removeIntrinsicKeyword("First Strike");
					c.addNonStackingIntrinsicKeyword("Double Strike");
				}
			}
		}
	};
	
	public static Command Transcendent_Master		  = new Command() {
		private static final long serialVersionUID = -7568530551652446195L;

		public void execute()
		{
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Transcendent Master");
			
			for (Card c:list)
			{
				int lcs = c.getCounters(Counters.LEVEL);
				if ( lcs < 6)
				{
					c.setBaseDefense(3);
					c.setBaseAttack(3);
					c.removeIntrinsicKeyword("Lifelink");
					c.removeIntrinsicKeyword("Indestructible");
				}
				else if ( lcs >=6)
				{
					c.setBaseDefense(6);
					c.setBaseAttack(6);
					c.addNonStackingIntrinsicKeyword("Lifelink");
				
					if (lcs >=12) {
						c.setBaseDefense(9);
						c.setBaseAttack(9);
						c.addNonStackingIntrinsicKeyword("Indestructible");
					}
				}
			}
		}
	};

	
	
	
	public static Command Dakkon                      = new Command() {

		private static final long serialVersionUID = 6863244333398587274L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Dakkon Blackblade");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countLands(c));
				c.setBaseDefense(c.getBaseAttack());
			}
		}// execute()

		private int countLands(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList lands = new CardList(play.getCards());
			lands = lands.getType("Land");
			return lands.size();
		}
	};

	public static Command Korlash                     = new Command() {
		private static final long serialVersionUID = 1791221644995716398L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Korlash, Heir to Blackblade");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countSwamps(c));
				c.setBaseDefense(c.getBaseAttack());
			}
		}// execute()

		private int countSwamps(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList swamps = new CardList(play.getCards());
			swamps = swamps.getType("Swamp");
			return swamps.size();
		}
	};
	
	public static Command Vampire_Nocturnus                       = new Command() {

		private static final long serialVersionUID = 666334034902503917L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-2);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Flying");
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Vampire Nocturnus");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Vampire");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if (CardFactoryUtil.getTopCard(c) != null)
					{
						if(CardUtil.getColors(CardFactoryUtil.getTopCard(c)).contains(
								Constant.Color.Black)) {
							c.addSemiPermanentAttackBoost(2);
							c.addSemiPermanentDefenseBoost(1);
							c.addExtrinsicKeyword("Flying");
							gloriousAnthemList.add(c);
						}
					}//topCard!=null
				}// for inner
			}// for outer
		}// execute()
	}; // Vampire Nocturnus
	

	public static Command Dauntless_Dourbark          = new Command() {

		private static final long serialVersionUID = -8843070116088984774L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Dauntless Dourbark");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);

				PlayerZone pz = AllZone.getZone(c);
				CardList cl = new CardList(pz.getCards());
				cl = cl.getName("Dauntless Dourbark");
				int dourbarksControlled = cl.size();

				if(hasTreefolk(c) || dourbarksControlled > 1) {
					//may be problematic, should be fine though
					c.setIntrinsicKeyword(new ArrayList<String>());
					c.addIntrinsicKeyword("Trample");
				} else {
					c.removeIntrinsicKeyword("Trample");
				}

				c.setBaseAttack(countTreeForests(c));
				c.setBaseDefense(c.getBaseAttack());
			}
		}// execute()

		private boolean hasTreefolk(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList tree = new CardList();
			tree.addAll(play.getCards());

			tree = tree.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return (c.getKeyword().contains("Changeling") || c.getType().contains(
							"Treefolk"))
							&& !c.getName().equals(
									"Dauntless Dourbark");
				}
			});
			if(tree.size() > 0) return true;
			else return false;
		}

		private int countTreeForests(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList list = new CardList(play.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getType().contains("Treefolk")
					|| c.getKeyword().contains(
							"Changeling")
							|| c.getType().contains("Forest");
				}
			});

			return list.size();
		}
	};

	public static Command Guul_Draz_Vampire           = new Command() {
		private static final long serialVersionUID = -4252257530318024113L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Guul Draz Vampire");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(oppLess10Life(c)) {
					if(!c.getIntrinsicKeyword().contains(
							"Intimidate")) c.addIntrinsicKeyword("Intimidate");
					c.setBaseAttack(3);
					c.setBaseDefense(2);
				} else {
					c.removeIntrinsicKeyword("Haste");
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
			}
		}// execute()

		//does opponent have 10 or less life?
		private boolean oppLess10Life(Card c) {
			String opp = AllZone.GameAction.getOpponent(c.getController());
			return AllZone.GameAction.getPlayerLife(opp).getLife() <= 10;
		}
	};

	public static Command Ruthless_Cullblade          = new Command() {
		private static final long serialVersionUID = 2627513737024865169L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Ruthless Cullblade");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(oppLess10Life(c)) {
					c.setBaseAttack(4);
					c.setBaseDefense(2);
				} else {
					c.setBaseAttack(2);
					c.setBaseDefense(1);
				}
			}
		}// execute()

		//does opponent have 10 or less life?
		private boolean oppLess10Life(Card c) {
			String opp = AllZone.GameAction.getOpponent(c.getController());
			return AllZone.GameAction.getPlayerLife(opp).getLife() <= 10;
		}
	};

	public static Command Bloodghast                  = new Command() {
		private static final long serialVersionUID = -4252257530318024113L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Bloodghast");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(oppLess10Life(c)
						&& !c.getIntrinsicKeyword().contains(
								"Haste")) c.addIntrinsicKeyword("Haste");
				else c.removeIntrinsicKeyword("Haste");
			}
		}// execute()

		//does opponent have 10 or less life?
		private boolean oppLess10Life(Card c) {
			String opp = AllZone.GameAction.getOpponent(c.getController());
			return AllZone.GameAction.getPlayerLife(opp).getLife() <= 10;
		}
	};

	public static Command Sejiri_Merfolk                   = new Command() {

		private static final long serialVersionUID = 3624165284236103054L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Sejiri Merfolk");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasPlains(c)) {
					if (!c.getIntrinsicKeyword().contains("First Strike"))
						c.addIntrinsicKeyword("First Strike");
					if (!c.getIntrinsicKeyword().contains("Lifelink"))
						c.addIntrinsicKeyword("Lifelink");
				} else {
					c.removeIntrinsicKeyword("Lifelink");
					c.removeIntrinsicKeyword("First Strike");
				}
			}
		}// execute()

		private boolean hasPlains(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Plains");
			if(land.size() > 0) return true;
			else return false;
		}
	};


	public static Command Gaeas_Avenger                   = new Command() {
		private static final long serialVersionUID = 1987511098173387864L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Gaea's Avenger");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countOppArtifacts(c)+1);
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countOppArtifacts(Card c) {
			PlayerZone play = AllZone.getZone(Constant.Zone.Play, AllZone.GameAction.getOpponent(c.getController()));
			CardList artifacts = new CardList(play.getCards());
			artifacts = artifacts.getType("Artifact");
			return artifacts.size();
		}
	};

	public static Command People_of_the_Woods                   = new Command() {
		private static final long serialVersionUID = 1987554325573387864L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("People of the Woods");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(1);
				c.setBaseDefense(countForests(c));
			}

		}// execute()

		private int countForests(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList forests = new CardList(play.getCards());
			forests = forests.getType("Forest");
			return forests.size();
		}
	};
	
	public static Command Serpent_of_the_Endless_Sea = new Command() {
		private static final long serialVersionUID = 8263339065128877297L;

		public void execute() {
			CardList list = AllZoneUtil.getCardsInPlay("Serpent of the Endless Sea");
			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int pt = AllZoneUtil.getPlayerTypeInPlay(c.getController(), "Island").size();
				c.setBaseAttack(pt);
				c.setBaseDefense(pt);
			}
		}// execute()
	};
	
	public static Command Heedless_One = new Command() {
		private static final long serialVersionUID = -220650457326100804L;

		public void execute() {
			CardList list = AllZoneUtil.getCardsInPlay("Heedless One");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countElves(c));
				c.setBaseDefense(countElves(c));
			}
		}// execute()

		private int countElves(Card c) {
			return AllZoneUtil.getTypeInPlay("Elf").size();
		}
	}; 


	public static Command Kird_Ape                    = new Command() {
		private static final long serialVersionUID = 3448725650293971110L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Kird Ape");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasForest(c)) {
					c.setBaseAttack(2);
					c.setBaseDefense(3);
				} else {
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
			}
		}// execute()

		private boolean hasForest(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Forest");
			if(land.size() > 0) return true;
			else return false;
		}
	};

	public static Command Loam_Lion                   = new Command() {
		private static final long serialVersionUID = -6996623102170747897L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Loam Lion");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasForest(c)) {
					c.setBaseAttack(2);
					c.setBaseDefense(3);
				} else {
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
			}
		}// execute()

		private boolean hasForest(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Forest");
			if(land.size() > 0) return true;
			else return false;
		}
	};

	public static Command Vexing_Beetle               = new Command() {

		private static final long serialVersionUID = 4599996155083227853L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Vexing Beetle");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(!oppHasCreature(c)) {
					c.setBaseAttack(6);
					c.setBaseDefense(6);
				} else {
					c.setBaseAttack(3);
					c.setBaseDefense(3);
				}
			}
		}// execute()

		private boolean oppHasCreature(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play,
					AllZone.GameAction.getOpponent(c.getController()));

			CardList creats = new CardList();
			creats.addAll(play.getCards());

			creats = creats.getType("Creature");
			if(creats.size() > 0) return true;
			else return false;
		}
	};

	public static Command Sedge_Troll                 = new Command() {

		private static final long serialVersionUID = -6021569379906767611L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Sedge Troll");


			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasSwamp(c)) {
					c.setBaseAttack(3);
					c.setBaseDefense(3);
				} else {
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
			}

		}// execute()

		private boolean hasSwamp(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Swamp");
			if(land.size() > 0) return true;
			else return false;
		}
	};

	public static Command Hedge_Troll                 = new Command() {

		private static final long serialVersionUID = -8843070116088984774L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Hedge Troll");


			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasPlains(c)) {
					c.setBaseAttack(3);
					c.setBaseDefense(3);
				} else {
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
			}

		}// execute()

		private boolean hasPlains(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Plains");
			if(land.size() > 0) return true;
			else return false;
		}
	};
	
	public static Command Champions_Drake = new Command() {
		private static final long serialVersionUID = 8076177362922156784L;

		public void execute() {
			CardList list = AllZoneUtil.getCardsInPlay("Champion's Drake");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hasThreeLevels(c)) {
					c.setBaseAttack(4);
					c.setBaseDefense(4);
				} else {
					c.setBaseAttack(1);
					c.setBaseDefense(1);
				}
			}
		}// execute()

		private boolean hasThreeLevels(Card c) {
			CardList levels = AllZoneUtil.getPlayerCardsInPlay(c.getController());
			levels = levels.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature() && c.getCounters(Counters.LEVEL) >= 3;
				}
			});
			return levels.size() > 0;
		}
	};


	public static Command Wild_Nacatl                 = new Command() {
		private static final long serialVersionUID = 6863244333398587274L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Wild Nacatl");

			if(list.size() > 0) {
				//Card crd = list.get(0);


				for(int i = 0; i < list.size(); i++) {
					int pt = 1;
					Card c = list.get(i);
					if(hasPlains(c)) pt++;
					if(hasMountain(c)) pt++;


					c.setBaseAttack(pt);
					c.setBaseDefense(pt);

				}
			}
		}// execute()

		private boolean hasPlains(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Plains");
			if(land.size() > 0) return true;
			else return false;
		}

		private boolean hasMountain(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList land = new CardList();
			land.addAll(play.getCards());

			land = land.getType("Mountain");
			if(land.size() > 0) return true;
			else return false;
		}

	};

	public static Command Liu_Bei                     = new Command() {

		private static final long serialVersionUID = 4235093010715735727L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Liu Bei, Lord of Shu");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(getsBonus(c)) {
						c.setBaseAttack(4);
						c.setBaseDefense(6);
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(4);
					}

				}
			}
		}// execute()

		private boolean getsBonus(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());

			CardList list = new CardList();
			list.addAll(play.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"Guan Yu, Sainted Warrior")
							|| c.getName().equals(
									"Zhang Fei, Fierce Warrior");
				}

			});

			return list.size() > 0;
		}

	}; //Liu_Bei

	public static Command Nimble_Mongoose             = new Command() {
		private static final long serialVersionUID = -8155356899650795833L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Nimble Mongoose");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(hasThreshold(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(3);
					} else {
						c.setBaseAttack(1);
						c.setBaseDefense(1);
					}

				}
			}
		}// execute()

		private boolean hasThreshold(Card c) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, c.getController());

			CardList gy = new CardList();
			gy.addAll(grave.getCards());

			if(gy.size() >= 7) return true;
			else return false;
		}

	}; //Nimble_Mongoose

	public static Command Mystic_Enforcer             = new Command() {

		private static final long serialVersionUID = 4569052031336290843L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Mystic Enforcer");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(hasThreshold(c)) {
						c.setBaseAttack(6);
						c.setBaseDefense(6);
						if(!c.getIntrinsicKeyword().contains(
								"Flying")) c.addIntrinsicKeyword("Flying");
					} else {
						c.setBaseAttack(3);
						c.setBaseDefense(3);
						if(c.getIntrinsicKeyword().contains(
								"Flying")) c.removeIntrinsicKeyword("Flying");
					}

				}
			}
		}// execute()

		private boolean hasThreshold(Card c) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, c.getController());

			CardList gy = new CardList();
			gy.addAll(grave.getCards());

			if(gy.size() >= 7) return true;
			else return false;
		}

	}; //Mystic_Enforcer

	public static Command Bant_Sureblade              = new Command() {

		private static final long serialVersionUID = 1987511205573387864L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Bant Sureblade");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherMulticoloredPermanent(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(2);
						if(!c.getIntrinsicKeyword().contains(
								"First Strike")) c.addIntrinsicKeyword("First Strike");
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(1);
						if(c.getIntrinsicKeyword().contains(
								"First Strike")) c.removeIntrinsicKeyword("First Strike");
					}

				}
			}
		}// execute()

	}; //Bant_Sureblade

	public static Command Esper_Stormblade            = new Command() {

		private static final long serialVersionUID = 1799759665613654307L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Esper Stormblade");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherMulticoloredPermanent(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(2);
						if(!c.getIntrinsicKeyword().contains(
								"Flying")) c.addIntrinsicKeyword("Flying");
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(1);
						if(c.getIntrinsicKeyword().contains(
								"Flying")) c.removeIntrinsicKeyword("Flying");
					}

				}
			}
		}// execute()

	}; //Esper_Stormblade

	public static Command Grixis_Grimblade            = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Grixis Grimblade");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherMulticoloredPermanent(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(2);
						if(!c.getIntrinsicKeyword().contains(
								"Deathtouch")) c.addIntrinsicKeyword("Deathtouch");
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(1);
						if(c.getIntrinsicKeyword().contains(
								"Deathtouch")) c.removeIntrinsicKeyword("Deathtouch");
					}

				}
			}
		}// execute()

	}; //Grixis_Grimblade

	public static Command Jund_Hackblade              = new Command() {

		private static final long serialVersionUID = -4386978825641506610L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Jund Hackblade");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherMulticoloredPermanent(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(2);
						if(!c.getIntrinsicKeyword().contains(
								"Haste")) c.addIntrinsicKeyword("Haste");
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(1);
						if(c.getIntrinsicKeyword().contains(
								"Haste")) c.removeIntrinsicKeyword("Haste");
					}

				}
			}
		}// execute()

	}; //Jund_Hackblade

	public static Command Naya_Hushblade              = new Command() {
		private static final long serialVersionUID = 3953482302338689497L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Naya Hushblade");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherMulticoloredPermanent(c)) {
						c.setBaseAttack(3);
						c.setBaseDefense(2);
						if(!c.getIntrinsicKeyword().contains(
								"Shroud")) c.addIntrinsicKeyword("Shroud");
					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(1);
						if(c.getIntrinsicKeyword().contains(
								"Shroud")) c.removeIntrinsicKeyword("Shroud");
					}

				}
			}
		}// execute()

	}; //Naya_Hushblade

	public static Command Ballynock_Cohort            = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Ballynock Cohort");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherColoredCreature(
							c, "white")) {
						c.setBaseAttack(3);
						c.setBaseDefense(3);

					} else {
						c.setBaseAttack(2);
						c.setBaseDefense(2);

					}

				}
			}
		}// execute()

	}; //Ballynock Cohort

	public static Command Ashenmoor_Cohort            = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Ashenmoor Cohort");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherColoredCreature(
							c, "black")) {
						c.setBaseAttack(5);
						c.setBaseDefense(4);

					} else {
						c.setBaseAttack(4);
						c.setBaseDefense(3);

					}

				}
			}
		}// execute()

	}; //Ashenmoor Cohort

	public static Command Briarberry_Cohort           = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Briarberry Cohort");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherColoredCreature(
							c, "blue")) {
						c.setBaseAttack(2);
						c.setBaseDefense(2);

					} else {
						c.setBaseAttack(1);
						c.setBaseDefense(1);

					}

				}
			}
		}// execute()

	}; //Briarberry Cohort

	public static Command Crabapple_Cohort            = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Crabapple Cohort");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherColoredCreature(
							c, "green")) {
						c.setBaseAttack(5);
						c.setBaseDefense(5);

					} else {
						c.setBaseAttack(4);
						c.setBaseDefense(4);

					}

				}
			}
		}// execute()

	}; //Crabapple Cohort

	public static Command Mudbrawler_Cohort           = new Command() {

		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Mudbrawler Cohort");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(CardFactoryUtil.controlsAnotherColoredCreature(
							c, "red")) {
						c.setBaseAttack(2);
						c.setBaseDefense(2);

					} else {
						c.setBaseAttack(1);
						c.setBaseDefense(1);

					}

				}
			}
		}// execute()

	}; //Mudbrawler Cohort


	public static Command Werebear                    = new Command() {

		private static final long serialVersionUID = 4599996155083227853L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Werebear");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(hasThreshold(c)) {
						c.setBaseAttack(4);
						c.setBaseDefense(4);
					} else {
						c.setBaseAttack(1);
						c.setBaseDefense(1);
					}

				}
			}
		}// execute()

		private boolean hasThreshold(Card c) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, c.getController());

			CardList gy = new CardList();
			gy.addAll(grave.getCards());

			if(gy.size() >= 7) return true;
			else return false;
		}

	};

	public static Command Divinity_of_Pride           = new Command() {
		private static final long serialVersionUID = -8809209646381095625L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Divinity of Pride");

			if(list.size() > 0) {
				//Card crd = list.get(0); //unused

				for(int i = 0; i < list.size(); i++) {

					Card c = list.get(i);
					if(moreThan25Life(c)) {
						c.setBaseAttack(8);
						c.setBaseDefense(8);
					} else {
						c.setBaseAttack(4);
						c.setBaseDefense(4);
					}

				}
			}
		}// execute()

		private boolean moreThan25Life(Card c) {
			PlayerLife life = AllZone.GameAction.getPlayerLife(c.getController());

			if(life.getLife() >= 25) return true;
			else return false;
		}

	};
	
	public static Command Aura_Gnarlid       = new Command() {
		private static final long serialVersionUID = 7072465568184131512L;

		public void execute() {
              // get all creatures
              CardList list = new CardList();
              list.addAll(AllZone.Human_Play.getCards());
              list.addAll(AllZone.Computer_Play.getCards());
              list = list.getName("Aura Gnarlid");
              
              for(int i = 0; i < list.size(); i++) {
                  Card c = list.get(i);
                  c.setBaseAttack(2 + countAuras());
                  c.setBaseDefense(2 + countAuras());
              }
          }// execute()
          
          private int countAuras() {
              PlayerZone cplay = AllZone.getZone(
                      Constant.Zone.Play, Constant.Player.Computer);
              PlayerZone hplay = AllZone.getZone(
                      Constant.Zone.Play, Constant.Player.Human);
              
              CardList auras = new CardList();
              auras.addAll(hplay.getCards());
              auras.addAll(cplay.getCards());
              
              auras = auras.getType("Aura");
              return auras.size();
          }
      };

	public static Command Yavimaya_Enchantress        = new Command() {
		private static final long serialVersionUID = -5650088477640877743L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Yavimaya Enchantress");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(2 + countEnchantments());
				c.setBaseDefense(2 + countEnchantments());
			}
		}// execute()

		private int countEnchantments() {
			PlayerZone cplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			PlayerZone hplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);

			CardList ench = new CardList();
			ench.addAll(hplay.getCards());
			ench.addAll(cplay.getCards());

			ench = ench.getType("Enchantment");
			return ench.size();
		}
	};

	public static Command Knight_of_the_Reliquary     = new Command() {
		private static final long serialVersionUID = -8511276284636573216L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Knight of the Reliquary");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(2 + countLands(c));
				c.setBaseDefense(2 + countLands(c));
			}
		}// execute()

		private int countLands(Card c) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, c.getController());

			CardList land = new CardList();
			land.addAll(grave.getCards());

			land = land.getType("Land");
			return land.size();
		}
	};

	public static Command Relentless_Rats_Other       = new Command() {
		private static final long serialVersionUID = -7731719556755491679L;

		int                       otherRats        = 0;

		private int countOtherRats(Card c) {
			PlayerZone hplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList rats = new CardList(hplay.getCards());
			rats.addAll(cplay.getCards());
			rats = rats.getName("Relentless Rats");
			return rats.size() - 1;
		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Relentless Rats");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherRats = countOtherRats(c);
				c.setOtherAttackBoost(otherRats);
				c.setOtherDefenseBoost(otherRats);

			}// for inner
		}// execute()

	};

	public static Command Privileged_Position         = new Command() {
		private static final long serialVersionUID   = -6677858046910868126L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.removeExtrinsicKeyword("This card can't be the target of spells or abilities your opponents control.");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Privileged Position");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList perms = new CardList(
						zone[outer].getCards());

				for(int i = 0; i < perms.size(); i++) {
					c = perms.get(i);
					if(c.isPermanent()
							&& !c.getName().equals(
									"Privileged Position")
									&& !c.getExtrinsicKeyword().contains(
									"This card can't be the target of spells or abilities your opponents control.")) {
						c.addExtrinsicKeyword("This card can't be the target of spells or abilities your opponents control.");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Priviliged_Position

	public static Command Privileged_Position_Other   = new Command() {
		private static final long serialVersionUID = -220264241686906985L;
		int                       otherPPs         = 0;

		private int countOtherPPs(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList pps = new CardList(play.getCards());
			pps = pps.getName("Privileged Position");
			return pps.size() - 1;

		}

		public void execute() {


			CardList pp = new CardList();
			pp.addAll(AllZone.Human_Play.getCards());
			pp.addAll(AllZone.Computer_Play.getCards());

			pp = pp.getName("Privileged Position");

			for(int i = 0; i < pp.size(); i++) {
				Card c = pp.get(i);
				otherPPs = countOtherPPs(c);
				if(otherPPs > 0) c.addExtrinsicKeyword("This card can't be the target of spells or abilities your opponents control.");
				//else if
			}// for inner
		}// execute()

	}; //Privileged_Position_Other


	public static Command Elvish_Archdruid_Pump       = new Command() {

		private static final long serialVersionUID   = -4549774958203921994L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Elvish Archdruid");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Elf");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Elvish Archdruid")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Elvish_Archdruid_Pump


	public static Command Elvish_Archdruid_Other      = new Command() {

		private static final long serialVersionUID = -8097280193598506523L;
		int                       otherDruids      = 0;

		private int countOtherDruids(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList druids = new CardList(play.getCards());
			druids = druids.getName("Elvish Archdruid");
			return druids.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Elvish Archdruid");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherDruids = countOtherDruids(c);
				c.setOtherAttackBoost(otherDruids);
				c.setOtherDefenseBoost(otherDruids);

			}// for inner
		}// execute()

	}; //Elvish_Archdruid_Other

	public static Command Wizened_Cenn_Pump           = new Command() {
		private static final long serialVersionUID   = 542524781150091105L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Wizened Cenn");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Kithkin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Wizened Cenn")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Wizened_Cenn_Pump

	public static Command Wizened_Cenn_Other          = new Command() {
		private static final long serialVersionUID = -7242601069504800797L;

		int                       otherCenns       = 0;

		private int countOtherCenns(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList cenns = new CardList(play.getCards());
			cenns = cenns.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals("Wizened Cenn")
					&& (c.getType().contains("Kithkin") || c.getKeyword().contains(
							"Changeling"));
				}
			});
			return cenns.size() - 1;

		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Wizened Cenn");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherCenns = countOtherCenns(c);
				c.setOtherAttackBoost(otherCenns);
				c.setOtherDefenseBoost(otherCenns);

			}// for inner
		}// execute()

	}; //Wizened Cenn Other

	public static Command Captain_of_the_Watch_Pump   = new Command() {
		private static final long serialVersionUID   = 542524781150091105L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeIntrinsicKeyword("Vigilance");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Captain of the Watch");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Captain of the Watch")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						if(!c.getIntrinsicKeyword().contains(
								"Vigilance")) c.addIntrinsicKeyword("Vigilance");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Captain_of_the_Watch_Pump

	public static Command Captain_of_the_Watch_Other  = new Command() {
		private static final long serialVersionUID = -7242601069504800797L;

		int                       otherCenns       = 0;

		private int countOtherCenns(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList cenns = new CardList(play.getCards());
			cenns = cenns.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"Captain of the Watch")
							&& (c.getType().contains("Soldier") || c.getKeyword().contains(
									"Changeling"));
				}
			});
			return cenns.size() - 1;

		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Captain of the Watch");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherCenns = countOtherCenns(c);
				c.setOtherAttackBoost(otherCenns);
				c.setOtherDefenseBoost(otherCenns);
				if(!c.getIntrinsicKeyword().contains("Vigilance")) c.addIntrinsicKeyword("Vigilance");

			}// for inner
		}// execute()

	}; //Captain of the Watch Other

	public static Command Veteran_Swordsmith_Pump     = new Command() {
		private static final long serialVersionUID   = 542524781150091105L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);

			}
			cList.clear();
			PlayerZone[] zone = getZone("Veteran Swordsmith");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Veteran Swordsmith")) {
						c.addSemiPermanentAttackBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Veteran Swordsmith_Pump

	public static Command Veteran_Swordsmith_Other    = new Command() {
		private static final long serialVersionUID = -7242601069504800797L;

		int                       otherCenns       = 0;

		private int countOtherCenns(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList cenns = new CardList(play.getCards());
			cenns = cenns.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"Veteran Swordsmith")
							&& (c.getType().contains("Soldier") || c.getKeyword().contains(
									"Changeling"));
				}
			});
			return cenns.size() - 1;

		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Veteran Swordsmith");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherCenns = countOtherCenns(c);
				c.setOtherAttackBoost(otherCenns);


			}// for inner
		}// execute()

	}; //Veteran Swordsmith Other

	public static Command Veteran_Armorsmith_Pump     = new Command() {
		private static final long serialVersionUID   = 542524781150091105L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Veteran Armorsmith");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Veteran Armorsmith")) {
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Veteran_Armorsmith_Pump

	public static Command Veteran_Armorsmith_Other    = new Command() {
		private static final long serialVersionUID = -7242601069504800797L;

		int                       otherCenns       = 0;

		private int countOtherCenns(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList cenns = new CardList(play.getCards());
			cenns = cenns.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(
							"Veteran Armorsmith")
							&& (c.getType().contains("Soldier") || c.getKeyword().contains(
									"Changeling"));
				}
			});
			return cenns.size() - 1;

		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Veteran Armorsmith");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherCenns = countOtherCenns(c);
				c.setOtherDefenseBoost(otherCenns);


			}// for inner
		}// execute()

	}; //Veteran Armorsmith Other	

	public static Command Elvish_Champion_Pump        = new Command() {

		private static final long serialVersionUID   = -2128898623878576243L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Forestwalk");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Elvish Champion");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Elf");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Elvish Champion")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Forestwalk");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Elvish_Champion_Pump 

	public static Command Elvish_Champion_Other       = new Command() {

		private static final long serialVersionUID = -8294068492084097409L;
		int                       otherLords       = 0;

		private int countOtherLords() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList lords = new CardList();
			lords.addAll(hPlay.getCards());
			lords.addAll(cPlay.getCards());
			lords = lords.getName("Elvish Champion");
			return lords.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Elvish Champion");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherLords();
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
				if(!c.getExtrinsicKeyword().contains(
						"Forestwalk")
						&& otherLords > 0) c.addExtrinsicKeyword("Forestwalk");

			}// for inner
		}// execute()

	}; //Elvish_Champion_Other

	public static Command Timber_Protector_Pump       = new Command() {

		private static final long serialVersionUID   = 395882142255572162L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				if(c.isCreature()) {
					c.addSemiPermanentAttackBoost(-1);
					c.addSemiPermanentDefenseBoost(-1);
					c.removeExtrinsicKeyword("Indestructible");
				} else //forest
				{
					c.removeExtrinsicKeyword("Indestructible");
				}
			}
			cList.clear();
			PlayerZone[] zone = getZone("Timber Protector");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());

				creature = creature.filter(new CardListFilter() {
					public boolean addCard(Card c) {
						return c.getType().contains("Treefolk")
						|| c.getType().contains("Forest")
						|| c.getKeyword().contains(
								"Changeling");
					}
				});

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
							"Timber Protector")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Indestructible");
						gloriousAnthemList.add(c);
					} else if(c.getType().contains("Forest")) {
						c.addExtrinsicKeyword("Indestructible");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Timber_Protector_Pump

	public static Command Timber_Protector_Other      = new Command() {
		private static final long serialVersionUID = -3107498901233064819L;
		int                       otherLords       = 0;

		private int countOtherLords() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList lords = new CardList();
			lords.addAll(hPlay.getCards());
			lords.addAll(cPlay.getCards());
			lords = lords.getName("Timber Protector");
			return lords.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Timber Protector");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherLords();
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
				if(!c.getExtrinsicKeyword().contains(
						"Indestructible")
						&& otherLords > 0) c.addExtrinsicKeyword("Indestructible");
				//else if (c.getExtrinsicKeyword().contains("Mountainwalk") && otherLords == 0 )


			}// for inner
		}// execute()

	}; //Timber_Protector_Other


	public static Command Goblin_Chieftain_Pump       = new Command() {

		private static final long serialVersionUID   = 395882142255572162L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Haste");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Goblin Chieftain");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				//creature.addAll(AllZone.Human_Play.getCards());
				//creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Goblin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Goblin Chieftain")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Haste");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Goblin_Chieftain_Pump

	public static Command Goblin_Chieftain_Other      = new Command() {

		private static final long serialVersionUID = -3107498901233064819L;
		int                       otherLords       = 0;

		private int countOtherLords() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList lords = new CardList();
			lords.addAll(hPlay.getCards());
			lords.addAll(cPlay.getCards());
			lords = lords.getName("Goblin Chieftain");
			return lords.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Goblin Chieftain");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherLords();
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
				if(!c.getExtrinsicKeyword().contains("Haste")
						&& otherLords > 0) c.addExtrinsicKeyword("Haste");
				//else if (c.getExtrinsicKeyword().contains("Mountainwalk") && otherLords == 0 )


			}// for inner
		}// execute()

	}; //Goblin_Chieftain_Other


	public static Command Goblin_King_Pump            = new Command() {

		private static final long serialVersionUID   = -2128898623878576243L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Mountainwalk");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Goblin King");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Goblin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals("Goblin King")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Mountainwalk");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Goblin_King_Pump

	public static Command Goblin_King_Other           = new Command() {

		private static final long serialVersionUID = -8294068492084097409L;
		int                       otherLords       = 0;

		private int countOtherLords() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList lords = new CardList();
			lords.addAll(hPlay.getCards());
			lords.addAll(cPlay.getCards());
			lords = lords.getName("Goblin King");
			return lords.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Goblin King");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherLords();
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
				if(!c.getExtrinsicKeyword().contains(
						"Mountainwalk")
						&& otherLords > 0) c.addExtrinsicKeyword("Mountainwalk");
				//else if (c.getExtrinsicKeyword().contains("Mountainwalk") && otherLords == 0 )


			}// for inner
		}// execute()

	}; //Goblin_King_Other

	public static Command Merfolk_Sovereign_Pump      = new Command() {

		private static final long serialVersionUID   = -8250416279767429585L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Merfolk Sovereign");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				//creature.addAll(AllZone.Human_Play.getCards());
				//creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Merfolk");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Merfolk Sovereign")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Merfolk_Sovereign_Pump 

	public static Command Merfolk_Sovereign_Other     = new Command() {
		private static final long serialVersionUID = -179394803961615332L;
		int                       otherLords       = 0;

		private int countOtherSovereigns(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList sovs = new CardList(play.getCards());
			sovs = sovs.getName("Merfolk Sovereign");
			return sovs.size() - 1;
		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Merfolk Sovereign");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherSovereigns(c);
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
			}// for inner
		}// execute()

	}; //Merfolk_Sovereign_Other

	public static Command Lord_of_Atlantis_Pump       = new Command() {

		private static final long serialVersionUID   = -2128898623878576243L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Islandwalk");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Lord of Atlantis");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Merfolk");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Lord of Atlantis")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Islandwalk");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Lord_of_Atlantis_Pump 

	public static Command Lord_of_Atlantis_Other      = new Command() {

		private static final long serialVersionUID = -8294068492084097409L;
		int                       otherLords       = 0;

		private int countOtherLords() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList lords = new CardList();
			lords.addAll(hPlay.getCards());
			lords.addAll(cPlay.getCards());
			lords = lords.getName("Lord of Atlantis");
			return lords.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Lord of Atlantis");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherLords = countOtherLords();
				c.setOtherAttackBoost(otherLords);
				c.setOtherDefenseBoost(otherLords);
				if(!c.getExtrinsicKeyword().contains(
						"Islandwalk")
						&& otherLords > 0) c.addExtrinsicKeyword("Islandwalk");

			}// for inner
		}// execute()

	}; //Lord_of_Atlantis_Other


	public static Command Field_Marshal_Pump          = new Command() {
		private static final long serialVersionUID   = -2429608928111507712L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("First Strike");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Field Marshal");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Field Marshal")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("First Strike");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Field Marshal Pump

	public static Command Field_Marshal_Other         = new Command() {
		private static final long serialVersionUID = 8252431904723630691L;

		int                       otherMarshals    = 0;

		private int countOtherMarshals() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList marshals = new CardList();
			marshals.addAll(hPlay.getCards());
			marshals.addAll(cPlay.getCards());
			marshals = marshals.getName("Field Marshal");
			return marshals.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Field Marshal");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherMarshals = countOtherMarshals();
				c.setOtherAttackBoost(otherMarshals);
				c.setOtherDefenseBoost(otherMarshals);
				if(!c.getExtrinsicKeyword().contains(
						"First Strike")
						&& otherMarshals > 0) c.addExtrinsicKeyword("First Strike");

			}// for inner
		}// execute()

	}; //Field Marshal Other

	public static Command Aven_Brigadier_Soldier_Pump = new Command() {
		private static final long serialVersionUID   = -2052700621466065388L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Aven Brigadier");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Soldier");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Aven Brigadier")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Aven Brigadier Soldier Pump

	public static Command Aven_Brigadier_Bird_Pump    = new Command() {
		private static final long serialVersionUID   = 69906668683163765L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Aven Brigadier");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Bird");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Aven Brigadier")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Aven Brigadier Bird Pump

	public static Command Aven_Brigadier_Other        = new Command() {
		private static final long serialVersionUID = 3214384167995760060L;

		int                       otherBrigadiers  = 0;

		private int countOtherBrigadiers() {
			PlayerZone hPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cPlay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList brigadiers = new CardList();

			brigadiers.addAll(hPlay.getCards());
			brigadiers.addAll(cPlay.getCards());
			brigadiers = brigadiers.getName("Aven Brigadier");
			return brigadiers.size() - 1;
		}

		public void execute() {

			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Aven Brigadier");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherBrigadiers = countOtherBrigadiers();
				int boost = 0;
				if(c.getType().contains("Bird")) boost++;
				if(c.getType().contains("Soldier")) boost++;
				c.setOtherAttackBoost(boost * otherBrigadiers);
				c.setOtherDefenseBoost(boost * otherBrigadiers);
			}// for inner
		}// execute()

	}; //brigadiers other

	public static Command Scion_of_Oona_Pump          = new Command() {
		private static final long serialVersionUID   = 8659017444482040867L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
				c.removeExtrinsicKeyword("Shroud");
			}
			cList.clear();
			PlayerZone[] zone = getZone("Scion of Oona");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Faerie");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Scion of Oona")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						c.addExtrinsicKeyword("Shroud");
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()

	}; //Scion of Oona Pump


	public static Command Scion_of_Oona_Other         = new Command() {
		private static final long serialVersionUID = -2317464426622768435L;

		int                       otherScions      = 0;

		private int countOtherScions(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList scions = new CardList(play.getCards());
			scions = scions.getName("Scion of Oona");
			return scions.size() - 1;
		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Scion of Oona");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherScions = countOtherScions(c);
				if(c.getType().contains("Faerie")
						|| c.getKeyword().contains("Changeling")) {
					c.setOtherAttackBoost(otherScions);
					c.setOtherDefenseBoost(otherScions);
					if(!c.getExtrinsicKeyword().contains(
							"Shroud")
							&& otherScions > 0) c.addExtrinsicKeyword("Shroud");
				} else {
					c.setOtherAttackBoost(0);
					c.setOtherDefenseBoost(0);
					c.removeExtrinsicKeyword("Shroud");
				}

			}// for inner
		}// execute()
	}; //Scion of Oona other


	public static Command Covetous_Dragon             = new Command() {
		private static final long serialVersionUID = -8898010588711890705L;

		int                       artifacts        = 0;

		public void execute() {
			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Covetous Dragon");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				artifacts = countArtifacts(c);
				if(artifacts == 0) {
					AllZone.GameAction.sacrifice(c);
				}
			}

		}//execute()

		private int countArtifacts(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList artifacts = new CardList(play.getCards());
			artifacts = artifacts.getType("Artifact");
			return artifacts.size();
		}


	};

	public static Command Tethered_Griffin            = new Command() {

		private static final long serialVersionUID = 572286202401670996L;
		int                       enchantments     = 0;

		public void execute() {
			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Tethered Griffin");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				enchantments = countEnchantments(c);
				if(enchantments == 0) {
					AllZone.GameAction.sacrifice(c);
				}
			}

		}//execute()

		private int countEnchantments(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList enchantments = new CardList(play.getCards());
			enchantments = enchantments.getType("Enchantment");
			return enchantments.size();
		}


	};
	
	public static Command topCardReveal_Update      = new Command() {

		private static final long serialVersionUID = 8669404698350637963L;

		public void execute() {
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());

			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getKeyword().contains(
							"Play with the top card of your library revealed.");
				}
			});

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if (CardFactoryUtil.getTopCard(c)!= null)
					c.setTopCardName(CardFactoryUtil.getTopCard(c).getName());
			}

		}//execute()
	};

	public static Command Sacrifice_NoIslands         = new Command() {

		private static final long serialVersionUID = 8064452222949253952L;
		int                       islands          = 0;

		public void execute() {
			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getKeyword().contains(
							"When you control no Islands, sacrifice this creature");
				}
			});

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				islands = countIslands(c);
				if(islands == 0) {
					AllZone.GameAction.sacrifice(c);
				}
			}

		}//execute()

		private int countIslands(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList islands = new CardList(play.getCards());
			islands = islands.getType("Island");
			return islands.size();
		}

	};

	public static Command Zuberi                      = new Command() {
		private static final long serialVersionUID   = -6283266522827930762L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}
			cList.clear();
			PlayerZone[] zone = getZone("Zuberi, Golden Feather");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Griffin");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
									"Zuberi, Golden Feather")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

		}// execute()


	};

	public static Command Master_of_Etherium_Other    = new Command() {
		private static final long serialVersionUID = -3325892185484133742L;

		int                       otherMasters     = 0;

		private int countOtherMasters(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList masters = new CardList(play.getCards());
			masters = masters.getName("Master of Etherium");
			return masters.size() - 1;

		}

		public void execute() {


			CardList creature = new CardList();
			creature.addAll(AllZone.Human_Play.getCards());
			creature.addAll(AllZone.Computer_Play.getCards());

			creature = creature.getName("Master of Etherium");

			for(int i = 0; i < creature.size(); i++) {
				Card c = creature.get(i);
				otherMasters = countOtherMasters(c);
				c.setOtherAttackBoost(otherMasters);
				c.setOtherDefenseBoost(otherMasters);

			}// for inner
		}// execute()
	};

	public static Command Master_of_Etherium          = new Command() {
		private static final long serialVersionUID   = -5406532269375480827L;

		@SuppressWarnings("unused")
		// gloriousAnthemList
		CardList                  gloriousAnthemList = new CardList();

		@SuppressWarnings("unused")
		// otherMasters
		int                       otherMasters       = 0;

		public void execute() {
			// get all cards

			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Master of Etherium");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);

				c.setBaseAttack(countArtifacts(c));
				c.setBaseDefense(c.getBaseAttack());


			}

		}// execute()

		private int countArtifacts(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList artifacts = new CardList(play.getCards());
			artifacts = artifacts.getType("Artifact");
			return artifacts.size();
		}


	}; // Master of etherium

	public static Command Master_of_Etherium_Pump     = new Command() {
		private static final long serialVersionUID   = -1736492817816019320L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			// now, apply the +1/+1 to all other artifacts controlled:

				CardList cList = gloriousAnthemList;
			Card c;

			for(int i = 0; i < cList.size(); i++) {
				c = cList.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			cList.clear();
			PlayerZone[] zone = getZone("Master of Etherium");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Artifact");

				for(int i = 0; i < creature.size(); i++) {

					c = creature.get(i);
					if(c.isCreature()
							&& !c.getName().equals(
							"Master of Etherium")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}

				} // for
			} // for

			/*
			 * CardList masters = new CardList();
			 * masters.addAll(AllZone.Human_Play.getCards());
			 * masters.addAll(AllZone.Computer_Play.getCards()); masters =
			 * masters.getName("Master of Etherium");
			 * 
			 * for (int i=0; i < masters.size(); i++) { c = masters.get(i); int
			 * otherMasters = countOtherMasters(c);
			 * System.out.println("otherMasters: " +otherMasters); if
			 * (otherMasters > 0) { for (int j=0; j < otherMasters; j++) {
			 * System.out.println("j: " + j + " for card ");
			 * c.setAttack(c.getAttack() + 1); c.setDefense(c.getDefense() + 1);
			 * gloriousAnthemList.add(c); } } }
			 */

		}// execute()

		@SuppressWarnings("unused")
		// countArtifacts
		private int countArtifacts(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList artifacts = new CardList(play.getCards());
			artifacts = artifacts.getType("Artifact");
			return artifacts.size();
		}

		@SuppressWarnings("unused")
		// countOtherMasters
		private int countOtherMasters(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList masters = new CardList(play.getCards());
			masters = masters.getName("Master of Etherium");
			return masters.size() - 1;

		}
	}; // Master of etherium pump


	public static Command Loxodon_Punisher            = new Command() {

		private static final long serialVersionUID = -7746134566580289667L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Loxodon Punisher");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(2 + countEquipment(c) * 2);
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countEquipment(Card c) {
			CardList equipment = new CardList(
					c.getEquippedBy().toArray());
			return equipment.size();
		}
	};

	public static Command Rabid_Wombat                = new Command() {

		private static final long serialVersionUID = -7746134566580289667L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Rabid Wombat");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countAuras(c) * 2);
				c.setBaseDefense(c.getBaseAttack() + 1);
			}

		}// execute()

		private int countAuras(Card c) {
			CardList auras = new CardList(
					c.getEnchantedBy().toArray());
			return auras.size();
		}
	};

	public static Command Uril                        = new Command() {
		private static final long serialVersionUID = 8168928048322850517L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Uril, the Miststalker");

			if(list.size() > 0) {
				Card c = list.get(0);
				c.setBaseAttack(5 + (countAuras(c) * 2));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countAuras(Card c) {
			CardList auras = new CardList(
					c.getEnchantedBy().toArray());
			return auras.size();
		}
	};


	public static Command Kithkin_Rabble              = new Command() {
		private static final long serialVersionUID = 6686690505949642328L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Kithkin Rabble");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countWhitePermanents(c));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countWhitePermanents(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList whitePermanents = new CardList(
					play.getCards());
			whitePermanents = whitePermanents.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return CardUtil.getColors(c).contains(
							Constant.Color.White);
				}

			});
			return whitePermanents.size();
		}
	};

	public static Command Crowd_of_Cinders            = new Command() {
		private static final long serialVersionUID = 6686690505949642328L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Crowd of Cinders");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countWhitePermanents(c));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countWhitePermanents(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList whitePermanents = new CardList(
					play.getCards());
			whitePermanents = whitePermanents.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return CardUtil.getColors(c).contains(
							Constant.Color.Black);
				}

			});
			return whitePermanents.size();
		}
	}; // Crowd of Cinders

	public static Command Faerie_Swarm                = new Command() {
		private static final long serialVersionUID = 6686690505949642328L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Faerie Swarm");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countWhitePermanents(c));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countWhitePermanents(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList whitePermanents = new CardList(
					play.getCards());
			whitePermanents = whitePermanents.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return CardUtil.getColors(c).contains(
							Constant.Color.Blue);
				}

			});
			return whitePermanents.size();
		}
	}; // Faerie Swarm

	public static Command Drove_of_Elves              = new Command() {
		private static final long serialVersionUID = 6686690505949642328L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Drove of Elves");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countWhitePermanents(c));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countWhitePermanents(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList whitePermanents = new CardList(
					play.getCards());
			whitePermanents = whitePermanents.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return CardUtil.getColors(c).contains(
							Constant.Color.Green);
				}

			});
			return whitePermanents.size();
		}
	}; // Drove of Elves


	public static Command Multani_Maro_Sorcerer       = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Multani, Maro-Sorcerer");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countHands());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countHands() {
			PlayerZone compHand = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Computer);
			PlayerZone humHand = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compHand.getCards());
			list.addAll(humHand.getCards());
			return list.size();
		}

	}; //Multani, Maro-Sorcerer

	public static Command Molimo_Maro_Sorcerer        = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Molimo, Maro-Sorcerer");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int k = 0;
				if(c.getController().equals(
						Constant.Player.Human)) {
					k = countLands_Human();
				} else k = countLands_Computer();
				c.setBaseAttack(k);
				c.setBaseDefense(k);

			}
		}

		private int countLands_Human() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isLand();
				}
			});
			return list.size();
		}

		private int countLands_Computer() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isLand();
				}
			});
			return list.size();
		}


	}; //Molimo, Maro-Sorcerer

	public static Command Maro                        = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Maro");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int k = 0;
				if(c.getController().equals(
						Constant.Player.Human)) {
					k = countHand_Human();
				} else k = countHand_Computer();
				c.setBaseAttack(k);
				c.setBaseDefense(k);
			}
		}

		private int countHand_Human() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

		private int countHand_Computer() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Computer);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

	}; //Maro

	public static Command Overbeing_of_Myth           = new Command() {
		private static final long serialVersionUID = -2250795040532050455L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Overbeing of Myth");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int k = 0;
				if(c.getController().equals(
						Constant.Player.Human)) {
					k = countHand_Human();
				} else k = countHand_Computer();
				c.setBaseAttack(k);
				c.setBaseDefense(k);
			}
		}

		private int countHand_Human() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

		private int countHand_Computer() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Computer);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

	}; //overbeing of myth

	public static Command Guul_Draz_Specter           = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Guul Draz Specter");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int k = 0;
				if(c.getController().equals(
						Constant.Player.Computer)) {
					k = countHand_Human();
				} else k = countHand_Computer();
				if(k == 0) {
					c.setBaseAttack(5);
					c.setBaseDefense(5);
				} else {
					c.setBaseAttack(2);
					c.setBaseDefense(2);
				}
			}
		}

		private int countHand_Human() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

		private int countHand_Computer() {
			PlayerZone Play = AllZone.getZone(
					Constant.Zone.Hand, Constant.Player.Computer);
			CardList list = new CardList();
			list.addAll(Play.getCards());
			return list.size();
		}

	}; //Guul Draz Specter

	public static Command Mortivore                   = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Mortivore");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCreatures());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countCreatures() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature();
				}
			});
			return list.size();
		}

	}; //Mortivore

	public static Command Cognivore                   = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Cognivore");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCreatures());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countCreatures() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isInstant();
				}
			});
			return list.size();
		}

	}; //Cognivore

	public static Command Cantivore                   = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Cantivore");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCreatures());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countCreatures() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isEnchantment();
				}
			});
			return list.size();
		}

	}; //Cantivore

	public static Command Lhurgoyf                    = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Lhurgoyf");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCreatures());
				c.setBaseDefense(c.getBaseAttack() + 1);
			}
		}

		private int countCreatures() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature();
				}
			});
			return list.size();
		}

	}; //Lhurgoyf

	public static Command Svogthos_the_Restless_Tomb  = new Command() {
		private static final long serialVersionUID = -8778902687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Svogthos, the Restless Tomb");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int x = 0;
				if(c.getController() == "Human") x = countCreatures_Hum();
				else x = countCreatures_Comp();
				if(c.isCreature()) {
					c.setBaseAttack(x);
					c.setBaseDefense(x);
				}
			}
		}

		private int countCreatures_Comp() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature();
				}
			});
			return list.size();
		}

		private int countCreatures_Hum() {
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(humGrave.getCards());
			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature();
				}
			});
			return list.size();
		}

	}; //Svogthos, the Restless Tomb

	public static Command Deaths_Shadow               = new Command() {
		private static final long serialVersionUID = 6025078590277639849L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Death's Shadow");

			for(Card c:list) {
				int n = 13 - AllZone.GameAction.getPlayerLife(
						c.getController()).getLife();
				c.setBaseAttack(n);
				c.setBaseDefense(n);
			}

		}// execute()
	};

	public static Command Nightmare                   = new Command() {
		private static final long serialVersionUID = 1987511205573387864L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Nightmare");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countSwamps(c));
				c.setBaseDefense(c.getBaseAttack());
			}

		}// execute()

		private int countSwamps(Card c) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, c.getController());
			CardList swamps = new CardList(play.getCards());
			swamps = swamps.getType("Swamp");
			return swamps.size();
		}
	};

	public static Command Aven_Trailblazer            = new Command() {
		private static final long serialVersionUID = 2731050781896531776L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Aven Trailblazer");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseDefense(countLandTypes(c));
			}

		}// execute()

		int countLandTypes(Card card) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, card.getController());
			CardList land = new CardList(play.getCards());

			String basic[] = {
					"Forest", "Plains", "Mountain", "Island", "Swamp" };
			int count = 0;

			for(int i = 0; i < basic.length; i++) {
				CardList c = land.getType(basic[i]);
				if(!c.isEmpty()) count++;
			}
			return count;
		}
	};

	public static Command Rakdos_Pit_Dragon           = new Command() {
		private static final long serialVersionUID = -8778900687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Rakdos Pit Dragon");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(hellbent(c.getController())
						&& !c.getIntrinsicKeyword().contains(
								"Double Strike")) c.addIntrinsicKeyword("Double Strike");
				else if(!hellbent(c.getController())
						&& c.getIntrinsicKeyword().contains(
								"Double Strike")) c.removeIntrinsicKeyword("Double Strike");
			}
		}

		private boolean hellbent(String player) {
			PlayerZone hand = AllZone.getZone(
					Constant.Zone.Hand, player);

			CardList list = new CardList();
			list.addAll(hand.getCards());

			return list.size() == 0;
		}

	}; //Rakdos Pit Dragon

	public static Command Nyxathid                    = new Command() {

		private static final long serialVersionUID = -8778900687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Nyxathid");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				int pt = 7 - countCards(AllZone.GameAction.getOpponent(c.getController()));
				c.setBaseAttack(pt);
				c.setBaseDefense(pt);
			}
		}

		private int countCards(String player) {
			PlayerZone hand = AllZone.getZone(
					Constant.Zone.Hand, player);

			CardList list = new CardList();
			list.addAll(hand.getCards());

			return list.size();
		}

	}; //Nyxathid


	public static Command Lord_of_Extinction          = new Command() {
		private static final long serialVersionUID = -8778900687347191964L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Lord of Extinction");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCards());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countCards() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());

			return list.size();
		}

	}; //Lord of Extinction


	public static Command Terravore                   = new Command() {
		private static final long serialVersionUID = -7848248012651247059L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Terravore");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countLands());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countLands() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());

			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isLand();
				}
			});

			return list.size();
		}

	}; //terravore

	public static Command Magnivore                   = new Command() {

		private static final long serialVersionUID = 6569701555927133445L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Magnivore");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countSorcs());
				c.setBaseDefense(c.getBaseAttack());
			}
		}

		private int countSorcs() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());

			list = list.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isSorcery();
				}
			});

			return list.size();
		}

	}; //magnivore

	public static Command Tarmogoyf                   = new Command() {
		private static final long serialVersionUID = 5895665460018262987L;

		public void execute() {
			// get all creatures
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName("Tarmogoyf");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countDiffTypes());
				c.setBaseDefense(c.getBaseAttack() + 1);
			}

		}// execute()

		private int countDiffTypes() {
			PlayerZone compGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Computer);
			PlayerZone humGrave = AllZone.getZone(
					Constant.Zone.Graveyard,
					Constant.Player.Human);
			CardList list = new CardList();
			list.addAll(compGrave.getCards());
			list.addAll(humGrave.getCards());

			int count = 0;
			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isCreature()) {
					count++;
					break;
				}
			}
			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isSorcery()) {
					count++;
					break;
				}
			}
			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isInstant()) {
					count++;
					break;
				}
			}
			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isArtifact()) {
					count++;
					break;
				}
			}

			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isEnchantment()) {
					count++;
					break;
				}
			}

			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isLand()) {
					count++;
					break;
				}
			}

			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isPlaneswalker()) {
					count++;
					break;
				}
			}

			for(int q = 0; q < list.size(); q++) {
				if(list.get(q).isTribal()) {
					count++;
					break;
				}
			}
			return count;
		}
	};

	public static Command Castle                      = new Command() {
		private static final long serialVersionUID = 4779036066493452237L;

		CardList                  old              = new CardList();

		public void execute() {
			Card c;
			// reset all previous cards stats
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentDefenseBoost(-2);
			}
			old.clear();

			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());

			CardList creatures = list.getType("Creature");

			list = list.getName("Castle");

			// Each Castle
			for(int i = 0; i < list.size(); i++) {
				// Each creature in play
				Card castleCard = list.get(i);
				for(int j = 0; j < creatures.size(); j++) {
					c = creatures.get(j);

					if(c.isUntapped()) {
						if(c.getController().equals(
								castleCard.getController())) // Only
								// apply
								// benefit
								// to
								// controlled
								// cards
								{
							c.addSemiPermanentDefenseBoost(2);
							old.add(c);
								}
					}
				}
			}
		}
	}; // Castle

	public static Command Castle_Raptors              = new Command() {
		private static final long serialVersionUID = 8774172452544866232L;

		CardList                  old              = new CardList();
		int                       pump             = 2;

		public void execute() {
			Card c;
			// reset all previous cards stats
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentDefenseBoost(-pump);
			}
			old.clear();

			CardList list = getCard("Castle Raptors");
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				// only add boost if card is untapped
				if(c.isUntapped()) {
					c.addSemiPermanentDefenseBoost(pump);
					old.add(c);
				}
			}// for
		}// execute()

		CardList getCard(String name) {
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName(name);
			return list;
		}// getRaptor()
	}; // Castle Raptors

	public static Command Giant_Tortoise = new Command() {
		private static final long serialVersionUID = -8191148876633239167L;

		CardList old = new CardList();
		int pump = 3;

		public void execute() {
			Card c;
			// reset all previous cards stats
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentDefenseBoost(-pump);
			}
			old.clear();

			CardList list = getCard("Giant Tortoise");
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				// only add boost if card is untapped
				if(c.isUntapped()) {
					c.addSemiPermanentDefenseBoost(pump);
					old.add(c);
				}
			}// for
		}// execute()

		CardList getCard(String name) {
			CardList list = new CardList();
			list.addAll(AllZone.Human_Play.getCards());
			list.addAll(AllZone.Computer_Play.getCards());
			list = list.getName(name);
			return list;
		}// getCard()
	}; // Giant_Tortoise


	public static Command Radiant_Archangel           = new Command() {
		private static final long serialVersionUID = -7086544305058527889L;

		CardList                  old              = new CardList();
		int                       pump             = 0;

		public void execute() {
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentAttackBoost(-pump);
				c.addSemiPermanentDefenseBoost(-pump);
			}

			old.clear();

			// get all cards names Radiant, Archangel
			CardList angel = getAngel();
			pump = countFlying();

			for(int i = 0; i < angel.size(); i++) {
				c = angel.get(i);
				c.addSemiPermanentAttackBoost(pump);
				c.addSemiPermanentDefenseBoost(pump);
			}
			old = angel;
		}// execute()

		CardList getAngel() {
			CardList angel = new CardList();
			angel.addAll(AllZone.Human_Play.getCards());
			angel.addAll(AllZone.Computer_Play.getCards());
			angel = angel.getName("Radiant, Archangel");
			return angel;
		}// getAngel()

		int countFlying() {
			// count number of creatures with flying
			CardList flying = new CardList();
			flying.addAll(AllZone.Human_Play.getCards());
			flying.addAll(AllZone.Computer_Play.getCards());
			flying = flying.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature()
					&& c.getKeyword().contains("Flying")
					&& !c.getName().equals(
							"Radiant, Archangel");
				}
			});
			return flying.size();
		}
	}; // Radiant, Archangel

	public static Command Veteran_Armorer             = new Command() {
		private static final long serialVersionUID = 6081997041540911467L;

		CardList                  old              = new CardList();

		public void execute() {
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			old.clear();
			PlayerZone[] zone = getZone("Veteran Armorer");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentDefenseBoost(1);

					old.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Veteran_Armorer

	public static Command Kongming					 = new Command() {

		private static final long serialVersionUID = 5376204832608673379L;
		CardList                  old              = new CardList();

		public void execute() {
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < old.size(); i++) {
				c = old.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			old.clear();
			PlayerZone[] zone = getZone("Kongming, \"Sleeping Dragon\"");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.filter(new CardListFilter(){
					public boolean addCard(Card c)
					{
						return c.isCreature() && !c.getName().equals("Kongming, \"Sleeping Dragon\"");
					}
				});

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					old.add(c);
				}// for inner
			}// for outer
		}// execute()

	};

	public static Command Valor                       = new Command() {
		private static final long serialVersionUID = 1664342157638418864L;

		CardList                  old              = new CardList();

		public void execute() {
			// reset creatures
			removeFirstStrike(old);

			if(isInGrave(Constant.Player.Computer)) addFirstStrike(Constant.Player.Computer);

			if(isInGrave(Constant.Player.Human)) addFirstStrike(Constant.Player.Human);
		}// execute()

		void addFirstStrike(String player) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList list = new CardList(play.getCards());
			list = list.getType("Creature");

			// add creatures to "old" or previous list of creatures
			old.addAll(list.toArray());

			addFirstStrike(list);
		}

		boolean isInGrave(String player) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, player);
			CardList list = new CardList(grave.getCards());

			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList lands = new CardList(play.getCards());
			lands = lands.getType("Plains");


			if(!list.containsName("Valor") || lands.size() == 0) return false;
			else return true;
		}

		void removeFirstStrike(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).removeExtrinsicKeyword(
						"First Strike");
		}

		void addFirstStrike(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).addExtrinsicKeyword("First Strike");
		}
	}; // Valor

	public static Command Anger                       = new Command() {
		private static final long serialVersionUID = -8436803135298267370L;

		CardList                  old              = new CardList();

		public void execute() {
			// reset creatures
			removeHaste(old);

			if(isInGrave(Constant.Player.Computer)) addHaste(Constant.Player.Computer);

			if(isInGrave(Constant.Player.Human)) addHaste(Constant.Player.Human);
		}// execute()

		void addHaste(String player) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList list = new CardList(play.getCards());
			list = list.getType("Creature");

			// add creatures to "old" or previous list of creatures
			old.addAll(list.toArray());

			addHaste(list);
		}

		boolean isInGrave(String player) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, player);
			CardList list = new CardList(grave.getCards());

			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList lands = new CardList(play.getCards());
			lands = lands.getType("Mountain");


			if(!list.containsName("Anger") || lands.size() == 0) return false;
			else return true;
		}

		void removeHaste(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).removeExtrinsicKeyword("Haste");
		}

		void addHaste(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).addExtrinsicKeyword("Haste");
		}
	}; // Anger

	public static Command Wonder                      = new Command() {
		private static final long serialVersionUID = 8346741995447241353L;

		CardList                  old              = new CardList();

		public void execute() {
			// reset creatures
			removeFlying(old);

			if(isInGrave(Constant.Player.Computer)) addFlying(Constant.Player.Computer);

			if(isInGrave(Constant.Player.Human)) addFlying(Constant.Player.Human);
		}// execute()

		void addFlying(String player) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList list = new CardList(play.getCards());
			list = list.getType("Creature");

			// add creatures to "old" or previous list of creatures
			old.addAll(list.toArray());

			addFlying(list);
		}

		boolean isInGrave(String player) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, player);
			CardList list = new CardList(grave.getCards());

			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList lands = new CardList(play.getCards());
			lands = lands.getType("Island");


			if(!list.containsName("Wonder") || lands.size() == 0) return false;
			else return true;
		}

		void removeFlying(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).removeExtrinsicKeyword("Flying");
		}

		void addFlying(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).addExtrinsicKeyword("Flying");
		}
	}; // Wonder

	public static Command Brawn                       = new Command() {
		private static final long serialVersionUID = -8467814700545847505L;

		CardList                  old              = new CardList();

		public void execute() {
			// reset creatures
			removeTrample(old);

			if(isInGrave(Constant.Player.Computer)) addTrample(Constant.Player.Computer);

			if(isInGrave(Constant.Player.Human)) addTrample(Constant.Player.Human);
		}// execute()

		void addTrample(String player) {
			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList list = new CardList(play.getCards());
			list = list.getType("Creature");

			// add creatures to "old" or previous list of creatures
			old.addAll(list.toArray());

			addTrample(list);
		}

		boolean isInGrave(String player) {
			PlayerZone grave = AllZone.getZone(
					Constant.Zone.Graveyard, player);
			CardList list = new CardList(grave.getCards());

			PlayerZone play = AllZone.getZone(
					Constant.Zone.Play, player);
			CardList lands = new CardList(play.getCards());
			lands = lands.getType("Forest");


			if(!list.containsName("Brawn") || lands.size() == 0) return false;
			else return true;
		}

		void removeTrample(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).removeExtrinsicKeyword("Trample");
		}

		void addTrample(CardList list) {
			for(int i = 0; i < list.size(); i++)
				list.get(i).addExtrinsicKeyword("Trample");
		}
	}; // Brawn

	public static Command Crucible_of_Fire            = new Command() {
		private static final long serialVersionUID   = 3620025773676030026L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-3);
				c.addSemiPermanentDefenseBoost(-3);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Crucible of Fire");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Dragon");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(3);
					c.addSemiPermanentDefenseBoost(3);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Crucible of Fire

	public static Command Glorious_Anthem             = new Command() {
		private static final long serialVersionUID   = 3686349742274071761L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Glorious Anthem");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Glorious Anthem

	public static Command Gaeas_Anthem                = new Command() {
		private static final long serialVersionUID   = -7379505886788323042L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Gaea's Anthem");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Gaea's Anthem

	public static Command Jacques                     = new Command() {

		private static final long serialVersionUID   = -4568356486065355565L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentDefenseBoost(-2);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Jacques le Vert");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.Green)) {
						c.addSemiPermanentDefenseBoost(2);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Jacques

	public static Command Kaysa                       = new Command() {
		private static final long serialVersionUID   = -4252908395616478212L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Kaysa");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.Green)) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Kaysa

	public static Command Meng_Huo                    = new Command() {
		private static final long serialVersionUID   = 786540623554314365L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);

			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Meng Huo, Barbarian King");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.Green)
							&& !c.getName().equals(
									"Meng Huo, Barbarian King")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Meng_Huo

	public static Command Eladamri                    = new Command() {
		private static final long serialVersionUID   = -6406997129429105950L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword("Shroud");
				c.removeExtrinsicKeyword("Forestwalk");
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Eladamri, Lord of Leaves");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Elf");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getName().equals(
							"Eladamri, Lord of Leaves")) {
						c.addExtrinsicKeyword("Shroud");
						c.addExtrinsicKeyword("Forestwalk");
						gloriousAnthemList.add(c);
					}

				}// for inner
			}// for outer
		}// execute()
	}; // Eladamri

	public static Command Tolsimir                    = new Command() {
		private static final long serialVersionUID   = -4522657609875269555L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				if(CardUtil.getColors(c).contains(
						Constant.Color.Green)) {
					c.addSemiPermanentAttackBoost(-1);
					c.addSemiPermanentDefenseBoost(-1);
				}
				if(CardUtil.getColors(c).contains(
						Constant.Color.White)) {
					c.addSemiPermanentAttackBoost(-1);
					c.addSemiPermanentDefenseBoost(-1);
				}
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Tolsimir Wolfblood");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(!c.getName().equals("Tolsimir Wolfblood")
							&& !c.isFaceDown()) {
						if(CardUtil.getColors(c).contains(
								Constant.Color.Green)) {
							c.addSemiPermanentAttackBoost(1);
							c.addSemiPermanentDefenseBoost(1);
						}
						if(CardUtil.getColors(c).contains(
								Constant.Color.White)) {
							c.addSemiPermanentAttackBoost(1);
							c.addSemiPermanentDefenseBoost(1);
						}
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; // Tolsimir

	public static Command Bad_Moon                    = new Command() {
		private static final long serialVersionUID   = 7137954808896324237L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);

			}

			// add +1/+1 to black cards
			list.clear();
			PlayerZone[] zone = getZone("Bad Moon");

			// for each zone found add +1/+1 to each black card
			for(int outer = 0; outer < zone.length; outer++) {
				// CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.Black)
							&& !c.isFaceDown()) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);

						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Bad Moon

	public static Command Crusade                     = new Command() {
		private static final long serialVersionUID   = 880902091818475216L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);

			}

			// add +1/+1 to white cards
			list.clear();
			PlayerZone[] zone = getZone("Crusade");

			// for each zone found add +1/+1 to each white card
			for(int outer = 0; outer < zone.length; outer++) {
				// CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.White)
							&& !c.isFaceDown()) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; // Crusade

	public static Command Honor_of_the_Pure           = new Command() {

		private static final long serialVersionUID   = -2784700121894495478L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Honor of the Pure");

			// for each zone found add +1/+1 to each card
			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(CardUtil.getColors(c).contains(
							Constant.Color.White)
							&& !c.isFaceDown()) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Honor of the Pure

	public static Command Beastmaster_Ascension       = new Command() {

		private static final long serialVersionUID   = -3455855754974451348L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-5);
				c.addSemiPermanentDefenseBoost(-5);
			}

			// add +1/+1 to cards
			list.clear();

			PlayerZone hplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Human);
			PlayerZone cplay = AllZone.getZone(
					Constant.Zone.Play, Constant.Player.Computer);

			CardList cl = new CardList();
			cl.addAll(hplay.getCards());
			cl.addAll(cplay.getCards());
			cl = cl.getName("Beastmaster Ascension");

			for(int i = 0; i < cl.size(); i++) {
				String player = cl.get(i).getController();
				PlayerZone play = AllZone.getZone(
						Constant.Zone.Play, player);

				CardList creature = new CardList(play.getCards());
				creature = creature.getType("Creature");

				for(int j = 0; j < creature.size(); j++) {
					if(cl.get(i).getCounters(Counters.QUEST) >= 7) {
						c = creature.get(j);
						c.addSemiPermanentAttackBoost(5);
						c.addSemiPermanentDefenseBoost(5);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Beastmaster Ascension

	public static Command Spidersilk_Armor            = new Command() {

		private static final long serialVersionUID   = -1151510755451414602L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword("Reach");
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Spidersilk Armor");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword("Reach");
					c.addSemiPermanentDefenseBoost(1);
					gloriousAnthemList.add(c);


				}// for inner
			}// for outer
		}// execute()
	}; // Spidersilk Armor

	public static Command Chainer                     = new Command() {
		private static final long serialVersionUID   = -5404417712966524986L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Chainer, Dementia Master");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Nightmare");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);
					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; //Chainer

	public static Command Eldrazi_Monument            = new Command() {

		private static final long serialVersionUID   = -3591110487441151195L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.removeExtrinsicKeyword("Flying");
				c.removeExtrinsicKeyword("Indestructible");
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			// add +1/+1 to cards
			list.clear();
			PlayerZone[] zone = getZone("Eldrazi Monument");

			for(int outer = 0; outer < zone.length; outer++) {
				CardList creature = new CardList(
						zone[outer].getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addExtrinsicKeyword("Flying");
					c.addExtrinsicKeyword("Indestructible");
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);
					gloriousAnthemList.add(c);


				}// for inner
			}// for outer
		}// execute()
	}; // Eldrazi_Monument


	public static Command Shared_Triumph              = new Command() {
		private static final long serialVersionUID   = -6427402366896716659L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {

			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);
			}

			list.clear();
			CardList cards = new CardList();
			cards.addAll(AllZone.Human_Play.getCards());
			cards.addAll(AllZone.Computer_Play.getCards());
			cards = cards.getName("Shared Triumph");

			for(int outer = 0; outer < cards.size(); outer++) {
				Card card = cards.get(outer);

				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType(card.getChosenType());

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if(c.getType().contains(card.getChosenType())
							|| c.getKeyword().contains(
									"Changeling")) {
						c.addSemiPermanentAttackBoost(1);
						c.addSemiPermanentDefenseBoost(1);
						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; //Shared Triumph


	public static Command Thelonite_Hermit            = new Command() {
		private static final long serialVersionUID   = 1876182498187900500L;

		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);

			}

			// add +1/+1 to black cards
			list.clear();
			PlayerZone[] zone = getZone("Thelonite Hermit");

			// for each zone found add +1/+1 to each black card
			for(int outer = 0; outer < zone.length; outer++) {
				// CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.filter(new CardListFilter() {
					public boolean addCard(Card c) {
						return c.getType().contains("Saproling")
						|| c.getKeyword().contains(
								"Changeling");
					}
				});

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Thelonite Hermit

	public static Command Deranged_Hermit             = new Command() {
		private static final long serialVersionUID   = -6105987998040015344L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-1);
				c.addSemiPermanentDefenseBoost(-1);

			}

			// add +1/+1 to black cards
			list.clear();
			PlayerZone[] zone = getZone("Deranged Hermit");

			for(int outer = 0; outer < zone.length; outer++) {
				// CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.filter(new CardListFilter() {
					public boolean addCard(Card c) {
						return c.getType().contains("Squirrel")
						|| c.getKeyword().contains(
								"Changeling");
					}
				});

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					c.addSemiPermanentAttackBoost(1);
					c.addSemiPermanentDefenseBoost(1);

					gloriousAnthemList.add(c);
				}// for inner
			}// for outer
		}// execute()
	}; // Deranged Hermit


	public static Command Muraganda_Petroglyphs       = new Command() {
		private static final long serialVersionUID   = -6715848091817213517L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				c.addSemiPermanentAttackBoost(-2);
				c.addSemiPermanentDefenseBoost(-2);
			}

			// add +2/+2 to vanilla cards
			list.clear();
			PlayerZone[] zone = getZone("Muraganda Petroglyphs");

			// for each zone found add +2/+2 to each vanilla card
			for(int outer = 0; outer < zone.length; outer++) {
				// CardList creature = new CardList(zone[outer].getCards());
				CardList creature = new CardList();
				creature.addAll(AllZone.Human_Play.getCards());
				creature.addAll(AllZone.Computer_Play.getCards());
				creature = creature.getType("Creature");

				for(int i = 0; i < creature.size(); i++) {
					c = creature.get(i);
					if((( c.getText().trim().equals("") || c.isFaceDown()) && c.getKeyword().size() == 0)) {
						c.addSemiPermanentAttackBoost(2);
						c.addSemiPermanentDefenseBoost(2);

						gloriousAnthemList.add(c);
					}


				}// for inner
			}// for outer
		}// execute()
	}; // Muraganda_Petroglyphs

	public static Command Meddling_Mage               = new Command() {
		private static final long serialVersionUID   = 738264163993370439L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				//c.removeIntrinsicKeyword("This card can't be cast");
				c.setUnCastable(false);
			}

			list.clear();

			PlayerZone cplay = AllZone.Computer_Play;
			PlayerZone hplay = AllZone.Human_Play;

			CardList cl = new CardList();
			cl.addAll(cplay.getCards());
			cl.addAll(hplay.getCards());
			cl = cl.getName("Meddling Mage");

			for(int i = 0; i < cl.size(); i++) {
				final Card crd = cl.get(i);

				CardList spells = new CardList();
				spells.addAll(AllZone.Human_Graveyard.getCards());
				spells.addAll(AllZone.Human_Hand.getCards());
				spells.addAll(AllZone.Computer_Hand.getCards());
				spells.addAll(AllZone.Computer_Graveyard.getCards());
				spells = spells.filter(new CardListFilter() {
					public boolean addCard(Card c) {
						return !c.isLand()
						&& c.getName().equals(
								crd.getNamedCard());
					}
				});

				for(int j = 0; j < spells.size(); j++) {
					c = spells.get(j);
					if(!c.isLand()) {
						//c.addIntrinsicKeyword("This card can't be cast");
						c.setUnCastable(true);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; // Meddling_Mage

	public static Command Gaddock_Teeg                = new Command() {
		private static final long serialVersionUID   = -479252814191086571L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				//c.removeIntrinsicKeyword("This card can't be cast");
				c.setUnCastable(false);
			}

			list.clear();

			PlayerZone cplay = AllZone.Computer_Play;
			PlayerZone hplay = AllZone.Human_Play;

			CardList cl = new CardList();
			cl.addAll(cplay.getCards());
			cl.addAll(hplay.getCards());
			cl = cl.getName("Gaddock Teeg");

			for(int i = 0; i < cl.size(); i++) {
				CardList spells = new CardList();
				spells.addAll(AllZone.Human_Graveyard.getCards());
				spells.addAll(AllZone.Human_Hand.getCards());
				spells.addAll(AllZone.Computer_Hand.getCards());
				spells.addAll(AllZone.Computer_Graveyard.getCards());


				spells = spells.filter(new CardListFilter() {
					public boolean addCard(Card c) {

						boolean isXNonCreature = false;
						if (c.getSpellAbility().length > 0)
						{
							if (c.getSpellAbility()[0].isXCost())
								isXNonCreature = true;
						}

						return !c.isLand()
						&& !c.isCreature()
						&& (CardUtil.getConvertedManaCost(c.getManaCost()) >= 4 || isXNonCreature);
					}
				});

				for(int j = 0; j < spells.size(); j++) {
					c = spells.get(j);
					if(!c.isLand()) {
						c.setUnCastable(true);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //

	public static Command Iona_Shield_of_Emeria       = new Command() {
		private static final long serialVersionUID   = 7349652597673216545L;
		CardList                  gloriousAnthemList = new CardList();

		public void execute() {
			CardList list = gloriousAnthemList;
			Card c;
			// reset all cards in list - aka "old" cards
			for(int i = 0; i < list.size(); i++) {
				c = list.get(i);
				//c.removeIntrinsicKeyword("This card can't be cast");
				c.setUnCastable(false);
			}

			list.clear();

			PlayerZone cplay = AllZone.Computer_Play;
			PlayerZone hplay = AllZone.Human_Play;

			CardList cl = new CardList();
			cl.addAll(cplay.getCards());
			cl.addAll(hplay.getCards());
			cl = cl.getName("Iona, Shield of Emeria");

			for(int i = 0; i < cl.size(); i++) {
				final Card crd = cl.get(i);
				String controller = cl.get(i).getController();
				String opp = AllZone.GameAction.getOpponent(controller);

				CardList spells = new CardList();
				PlayerZone grave = AllZone.getZone(
						Constant.Zone.Graveyard, opp);
				PlayerZone hand = AllZone.getZone(
						Constant.Zone.Hand, opp);

				spells.addAll(grave.getCards());
				spells.addAll(hand.getCards());

				spells = spells.filter(new CardListFilter() {
					public boolean addCard(Card c) {
						return !c.isLand()
						&& CardUtil.getColors(c).contains(
								crd.getChosenColor());
					}
				});

				for(int j = 0; j < spells.size(); j++) {
					c = spells.get(j);
					if(!c.isLand()) {
						c.setUnCastable(true);
						gloriousAnthemList.add(c);
					}
				}// for inner
			}// for outer
		}// execute()
	}; //
	public static Command Kor_Duelist           = new Command() {
		private static final long serialVersionUID = -8050975750696096661L;

		/*
		 * As long as Kor Duelist is equipped, it has double strike.
		 */
		public void execute() {
			CardList list = AllZoneUtil.getCardsInPlay("Kor Duelist");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				if(c.isEquipped()
						&& !c.getIntrinsicKeyword().contains(
								"Double Strike")) c.addIntrinsicKeyword("Double Strike");
				else if(!c.isEquipped()
						&& c.getIntrinsicKeyword().contains(
								"Double Strike")) c.removeIntrinsicKeyword("Double Strike");
			}
		}
	}; //Kor Duelist

	public static Command Keldon_Warlord                   = new Command() {
		private static final long serialVersionUID = 3804539422363462063L;
		
		/*
		 * Keldon Warlord's power and toughness are each equal to the number
		 * of non-Wall creatures you control.
		 */
		public void execute() {
			// get all creatures
			CardList list = AllZoneUtil.getCardsInPlay("Keldon Warlord");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				c.setBaseAttack(countCreatures(c));
				c.setBaseDefense(c.getNetAttack());
			}

		}// execute()

		private int countCreatures(Card c) {
			PlayerZone play = AllZone.getZone(Constant.Zone.Play, c.getController());
			CardList creatures = new CardList(play.getCards());
			creatures = creatures.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.isCreature() && !c.isWall();
				}
			});
			return creatures.size();
		}
	};
	
	public static Command Soulsurge_Elemental                   = new Command() {
		private static final long serialVersionUID = 8607200838396348507L;

		public void execute() {
			CardList list = AllZoneUtil.getCardsInPlay("Soulsurge Elemental");

			for(int i = 0; i < list.size(); i++) {
				Card c = list.get(i);
				//c.setBaseAttack(countCreatures(c));
				c.setBaseAttack(AllZoneUtil.getCreaturesInPlay(c.getController()).size());
				c.setBaseDefense(1);
			}
		}// execute()
	};
	
	public static void Elvish_Vanguard(Card c) {
		final Card crd = c;

		Ability ability = new Ability(c, "0") {
			@Override
			public void resolve() {
				crd.addCounter(Counters.P1P1, 1);
			}
		};
		ability.setStackDescription(c.getName() + " - gets a +1/+1 counter.");
		AllZone.Stack.add(ability);
	}


	// returns all PlayerZones that has at least 1 Glorious Anthem
	// if Computer has 2 Glorious Anthems, AllZone.Computer_Play will be
	// returned twice
	private static PlayerZone[] getZone(String cardName) {
		CardList all = new CardList();
		all.addAll(AllZone.Human_Play.getCards());
		all.addAll(AllZone.Computer_Play.getCards());

		ArrayList<PlayerZone> zone = new ArrayList<PlayerZone>();
		for(int i = 0; i < all.size(); i++)
			if(all.get(i).getName().equals(cardName) && !all.get(i).isFaceDown()) zone.add(AllZone.getZone(all.get(i)));

		PlayerZone[] z = new PlayerZone[zone.size()];
		zone.toArray(z);
		return z;
	}

	public static HashMap<String, Command> commands = new HashMap<String, Command>();
	static {
		commands.put("Conspiracy", Conspiracy);
		commands.put("Serra_Avatar", Serra_Avatar);
		commands.put("Ajani_Avatar_Token", Ajani_Avatar_Token);
		commands.put("Windwright_Mage", Windwright_Mage);

		//commands.put("Baru", Baru);
		//commands.put("Reach_of_Branches", Reach_of_Branches);

		//commands.put("Essence_Warden", Essence_Warden);
		//commands.put("Soul_Warden", Soul_Warden);
		//commands.put("Wirewood_Hivemaster", Wirewood_Hivemaster);
		//commands.put("Angelic_Chorus", Angelic_Chorus);

		commands.put("Uril", Uril);
		commands.put("Rabid_Wombat", Rabid_Wombat);
		commands.put("Kithkin_Rabble", Kithkin_Rabble);
		commands.put("Crowd_of_Cinders", Crowd_of_Cinders);
		commands.put("Faerie_Swarm", Faerie_Swarm);
		commands.put("Drove_of_Elves", Drove_of_Elves);
		commands.put("Svogthos_the_Restless_Tomb", Svogthos_the_Restless_Tomb);
		commands.put("Lhurgoyf", Lhurgoyf);
		commands.put("Deaths_Shadow", Deaths_Shadow);
		commands.put("Nightmare", Nightmare);
		commands.put("Aven_Trailblazer", Aven_Trailblazer);
		commands.put("Rakdos_Pit_Dragon", Rakdos_Pit_Dragon);
		commands.put("Nyxathid", Nyxathid);
		commands.put("Lord_of_Extinction", Lord_of_Extinction);
		commands.put("Cantivore", Cantivore);
		commands.put("Cognivore", Cognivore);
		commands.put("Mortivore", Mortivore);
		commands.put("Terravore", Terravore);
		commands.put("Magnivore", Magnivore);
		commands.put("Tarmogoyf", Tarmogoyf);
		commands.put("Multani_Maro_Sorcerer", Multani_Maro_Sorcerer);
		commands.put("Molimo_Maro_Sorcerer", Molimo_Maro_Sorcerer);
		commands.put("Maro", Maro);
		commands.put("Overbeing_of_Myth", Overbeing_of_Myth);
		commands.put("Guul_Draz_Specter", Guul_Draz_Specter);
		commands.put("Dakkon", Dakkon);
		commands.put("Korlash", Korlash);

		commands.put("Student_of_Warfare", Student_of_Warfare);
		commands.put("Transcendent_Master", Transcendent_Master);
		commands.put("Lighthouse_Chronologist", Lighthouse_Chronologist);
		commands.put("Skywatcher_Adept", Skywatcher_Adept);
		commands.put("Caravan_Escort", Caravan_Escort);
		commands.put("Ikiral_Outrider", Ikiral_Outrider);
		commands.put("Knight_of_Cliffhaven", Knight_of_Cliffhaven);
		commands.put("Beastbreaker_of_Bala_Ged", Beastbreaker_of_Bala_Ged);
		commands.put("Hada_Spy_Patrol", Hada_Spy_Patrol);
		commands.put("Halimar_Wavewatch", Halimar_Wavewatch);
		commands.put("Nirkana_Cutthroat", Nirkana_Cutthroat);
		commands.put("Zulaport_Enforcer", Zulaport_Enforcer);
		commands.put("Soulsurge_Elemental", Soulsurge_Elemental);
		commands.put("Champions_Drake", Champions_Drake);
		
		commands.put("Vampire_Nocturnus", Vampire_Nocturnus);
		commands.put("Dauntless_Dourbark", Dauntless_Dourbark);
		commands.put("People_of_the_Woods", People_of_the_Woods);
		commands.put("Serpent_of_the_Endless_Sea", Serpent_of_the_Endless_Sea);
		commands.put("Gaeas_Avenger", Gaeas_Avenger);
		commands.put("Vexing_Beetle", Vexing_Beetle);
		commands.put("Sejiri_Merfolk", Sejiri_Merfolk);
		commands.put("Kird_Ape", Kird_Ape);
		commands.put("Loam_Lion", Loam_Lion);
		commands.put("Sedge_Troll", Sedge_Troll);
		commands.put("Hedge_Troll", Hedge_Troll);
		commands.put("Wild_Nacatl", Wild_Nacatl);
		commands.put("Liu_Bei", Liu_Bei);
		commands.put("Mystic_Enforcer", Mystic_Enforcer);
		commands.put("Guul_Draz_Vampire", Guul_Draz_Vampire);
		commands.put("Ruthless_Cullblade", Ruthless_Cullblade);
		commands.put("Bloodghast", Bloodghast);
		commands.put("Bant_Sureblade", Bant_Sureblade);
		commands.put("Esper_Stormblade", Esper_Stormblade);
		commands.put("Grixis_Grimblade", Grixis_Grimblade);
		commands.put("Jund_Hackblade", Jund_Hackblade);
		commands.put("Naya_Hushblade", Naya_Hushblade);
		commands.put("Ballynock_Cohort", Ballynock_Cohort);
		commands.put("Ashenmoor_Cohort", Ashenmoor_Cohort);
		commands.put("Briarberry_Cohort", Briarberry_Cohort);
		commands.put("Crabapple_Cohort", Crabapple_Cohort);
		commands.put("Mudbrawler_Cohort", Mudbrawler_Cohort);
		commands.put("Nimble_Mongoose", Nimble_Mongoose);
		commands.put("Werebear", Werebear);
		commands.put("Divinity_of_Pride", Divinity_of_Pride);
		commands.put("Yavimaya_Enchantress", Yavimaya_Enchantress);
		commands.put("Aura_Gnarlid", Aura_Gnarlid);
		commands.put("Knight_of_the_Reliquary", Knight_of_the_Reliquary);
		commands.put("Zuberi", Zuberi);
		commands.put("Loxodon_Punisher", Loxodon_Punisher);
		commands.put("Master_of_Etherium", Master_of_Etherium);
		commands.put("Master_of_Etherium_Pump", Master_of_Etherium_Pump);
		commands.put("Master_of_Etherium_Other", Master_of_Etherium_Other);
		commands.put("Relentless_Rats_Other", Relentless_Rats_Other);
		commands.put("Privileged_Position", Privileged_Position);
		commands.put("Privileged_Position_Other", Privileged_Position_Other);
		commands.put("Elvish_Archdruid_Pump", Elvish_Archdruid_Pump);
		commands.put("Elvish_Archdruid_Other", Elvish_Archdruid_Other);
		commands.put("Elvish_Champion_Pump", Elvish_Champion_Pump);
		commands.put("Elvish_Champion_Other", Elvish_Champion_Other);
		commands.put("Wizened_Cenn_Pump", Wizened_Cenn_Pump);
		commands.put("Wizened_Cenn_Other", Wizened_Cenn_Other);
		commands.put("Captain_of_the_Watch_Pump", Captain_of_the_Watch_Pump);
		commands.put("Captain_of_the_Watch_Other", Captain_of_the_Watch_Other);
		commands.put("Veteran_Swordsmith_Pump", Veteran_Swordsmith_Pump);
		commands.put("Veteran_Swordsmith_Other", Veteran_Swordsmith_Other);
		commands.put("Veteran_Armorsmith_Pump", Veteran_Armorsmith_Pump);
		commands.put("Veteran_Armorsmith_Other", Veteran_Armorsmith_Other);
		commands.put("Merfolk_Sovereign_Pump", Merfolk_Sovereign_Pump);
		commands.put("Merfolk_Sovereign_Other", Merfolk_Sovereign_Other);
		commands.put("Lord_of_Atlantis_Pump", Lord_of_Atlantis_Pump);
		commands.put("Lord_of_Atlantis_Other", Lord_of_Atlantis_Other);
		commands.put("Timber_Protector_Pump", Timber_Protector_Pump);
		commands.put("Timber_Protector_Other", Timber_Protector_Other);
		commands.put("Goblin_Chieftain_Pump", Goblin_Chieftain_Pump);
		commands.put("Goblin_Chieftain_Other", Goblin_Chieftain_Other);
		commands.put("Goblin_King_Pump", Goblin_King_Pump);
		commands.put("Goblin_King_Other", Goblin_King_Other);
		commands.put("Field_Marshal_Pump", Field_Marshal_Pump);
		commands.put("Field_Marshal_Other", Field_Marshal_Other);
		commands.put("Aven_Brigadier_Soldier_Pump", Aven_Brigadier_Soldier_Pump);
		commands.put("Aven_Brigadier_Bird_Pump", Aven_Brigadier_Bird_Pump);
		commands.put("Aven_Brigadier_Other", Aven_Brigadier_Other);
		commands.put("Scion_of_Oona_Pump", Scion_of_Oona_Pump);
		commands.put("Scion_of_Oona_Other", Scion_of_Oona_Other);

		commands.put("Covetous_Dragon", Covetous_Dragon);
		commands.put("Tethered_Griffin", Tethered_Griffin);

		commands.put("Shared_Triumph", Shared_Triumph);
		commands.put("Crucible_of_Fire", Crucible_of_Fire);
		commands.put("Glorious_Anthem", Glorious_Anthem);
		commands.put("Gaeas_Anthem", Gaeas_Anthem);
		commands.put("Bad_Moon", Bad_Moon);
		commands.put("Crusade", Crusade);
		commands.put("Honor_of_the_Pure", Honor_of_the_Pure);
		commands.put("Beastmaster_Ascension", Beastmaster_Ascension);
		commands.put("Spidersilk_Armor", Spidersilk_Armor);
		commands.put("Chainer", Chainer);
		commands.put("Eldrazi_Monument", Eldrazi_Monument);
		commands.put("Muraganda_Petroglyphs", Muraganda_Petroglyphs);

		commands.put("Engineered_Plague", Engineered_Plague);
		commands.put("Night_of_Souls_Betrayal", Night_of_Souls_Betrayal);

		commands.put("Thelonite_Hermit", Thelonite_Hermit);
		commands.put("Deranged_Hermit", Deranged_Hermit);
		commands.put("Jacques", Jacques);
		commands.put("Kaysa", Kaysa);
		commands.put("Meng_Huo", Meng_Huo);
		commands.put("Eladamri", Eladamri);
		commands.put("Tolsimir", Tolsimir);
		commands.put("Imperious_Perfect", Imperious_Perfect);
		commands.put("Mad_Auntie", Mad_Auntie);

		commands.put("Veteran_Armorer", Veteran_Armorer);
		commands.put("Kongming", Kongming);
		commands.put("Radiant_Archangel", Radiant_Archangel);
		commands.put("Castle", Castle);
		commands.put("Castle_Raptors", Castle_Raptors);
		commands.put("Giant_Tortoise", Giant_Tortoise);

		commands.put("Darksteel_Forge", Darksteel_Forge);
		commands.put("Levitation", Levitation);
		commands.put("Knighthood", Knighthood);
		commands.put("Absolute_Law", Absolute_Law);
		commands.put("Absolute_Grace", Absolute_Grace);
		commands.put("Tabernacle", Tabernacle);
		commands.put("Magus_of_the_Tabernacle", Magus_of_the_Tabernacle);
		commands.put("Mobilization", Mobilization);
		commands.put("Serras_Blessing", Serras_Blessing);
		commands.put("Cover_of_Darkness", Cover_of_Darkness);
		commands.put("Steely_Resolve", Steely_Resolve);
		commands.put("Goblin_Assault", Goblin_Assault);
		commands.put("Concordant_Crossroads", Concordant_Crossroads);
		commands.put("Mass_Hysteria", Mass_Hysteria);
		commands.put("Fervor", Fervor);
		commands.put("Madrush_Cyclops", Madrush_Cyclops);

		commands.put("Sun_Quan", Sun_Quan);

		commands.put("Rolling_Stones", Rolling_Stones);
		commands.put("Kobold_Overlord", Kobold_Overlord);
		commands.put("Kinsbaile_Cavalier", Kinsbaile_Cavalier);
		commands.put("Wrens_Run_Packmaster", Wrens_Run_Packmaster);

		commands.put("Sliver_Legion", Sliver_Legion);
		commands.put("Muscle_Sliver", Muscle_Sliver);

		commands.put("Bonesplitter_Sliver", Bonesplitter_Sliver);
		commands.put("Might_Sliver", Might_Sliver);
		commands.put("Watcher_Sliver", Watcher_Sliver);

		commands.put("Winged_Sliver", Winged_Sliver);
		commands.put("Synchronous_Sliver", Synchronous_Sliver);
		commands.put("Fury_Sliver", Fury_Sliver);
		commands.put("Talon_Sliver", Talon_Sliver);
		commands.put("Plated_Sliver", Plated_Sliver);
		commands.put("Crystalline_Sliver", Crystalline_Sliver);
		commands.put("Virulent_Sliver", Virulent_Sliver);
		commands.put("Sidewinder_Sliver", Sidewinder_Sliver);
		commands.put("Essence_Sliver", Essence_Sliver);
		commands.put("Sinew_Sliver", Sinew_Sliver);
		commands.put("Horned_Sliver", Horned_Sliver);

		commands.put("Heart_Sliver", Heart_Sliver);
		commands.put("Reflex_Sliver", Reflex_Sliver);
		commands.put("Gemhide_Sliver", Gemhide_Sliver);

		commands.put("Blade_Sliver", Blade_Sliver);
		commands.put("Battering_Sliver", Battering_Sliver);

		commands.put("Marrow_Gnawer", Marrow_Gnawer);

		commands.put("Mul_Daya_Channelers", Mul_Daya_Channelers);
		commands.put("Joiner_Adept", Joiner_Adept);
		commands.put("Meddling_Mage", Meddling_Mage);
		commands.put("Gaddock_Teeg", Gaddock_Teeg);
		commands.put("Iona_Shield_of_Emeria", Iona_Shield_of_Emeria);
		commands.put("Kor_Duelist", Kor_Duelist);
		commands.put("Keldon_Warlord", Keldon_Warlord);
		commands.put("Heedless_One", Heedless_One);
		commands.put("Arena_of_the_Ancients", Arena_of_the_Ancients);
		
		//System.out.println("size of commands: " + commands.size());

	}

}
