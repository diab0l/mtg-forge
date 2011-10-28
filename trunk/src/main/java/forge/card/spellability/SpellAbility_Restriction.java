package forge.card.spellability;

import java.util.ArrayList;
import java.util.HashMap;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.Constant.Zone;
import forge.Phase;
import forge.Player;
import forge.PlayerZone;
import forge.card.abilityFactory.AbilityFactory;
import forge.card.cardFactory.CardFactoryUtil;

/**
 * <p>
 * SpellAbility_Restriction class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class SpellAbility_Restriction extends SpellAbility_Variables {
    // A class for handling SpellAbility Restrictions. These restrictions
    // include:
    // Zone, Phase, OwnTurn, Speed (instant/sorcery), Amount per Turn, Player,
    // Threshold, Metalcraft, LevelRange, etc
    // Each value will have a default, that can be overridden (mostly by
    // AbilityFactory)
    // The canPlay function will use these values to determine if the current
    // game state is ok with these restrictions

    /**
     * <p>
     * Constructor for SpellAbility_Restriction.
     * </p>
     */
    public SpellAbility_Restriction() {
    }

    /**
     * <p>
     * setRestrictions.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @since 1.0.15
     */
    public final void setRestrictions(final HashMap<String, String> params) {
        if (params.containsKey("Activation")) {
            String value = params.get("Activation");
            if (value.equals("Threshold")) {
                setThreshold(true);
            }
            if (value.equals("Metalcraft")) {
                setMetalcraft(true);
            }
            if (value.equals("Hellbent")) {
                setHellbent(true);
            }
            if (value.startsWith("Prowl")) {
                ArrayList<String> prowlTypes = new ArrayList<String>();
                prowlTypes.add("Rogue");
                if (value.split("Prowl").length > 1) {
                    prowlTypes.add(value.split("Prowl")[1]);
                }
                setProwl(prowlTypes);
            }
        }

        if (params.containsKey("ActivationZone")) {
            setZone(Zone.smartValueOf(params.get("ActivationZone")));
        }

        if (params.containsKey("Flashback")) {
            setZone(Zone.Graveyard);
        }

        if (params.containsKey("SorcerySpeed")) {
            setSorcerySpeed(true);
        }

        if (params.containsKey("PlayerTurn")) {
            setPlayerTurn(true);
        }

        if (params.containsKey("OpponentTurn")) {
            setOpponentTurn(true);
        }

        if (params.containsKey("AnyPlayer")) {
            setAnyPlayer(true);
        }

        if (params.containsKey("ActivationLimit")) {
            setActivationLimit(Integer.parseInt(params.get("ActivationLimit")));
        }

        if (params.containsKey("ActivationNumberSacrifice")) {
            setActivationNumberSacrifice(Integer.parseInt(params.get("ActivationNumberSacrifice")));
        }

        if (params.containsKey("ActivationPhases")) {
            String phases = params.get("ActivationPhases");

            if (phases.contains("->")) {
                // If phases lists a Range, split and Build Activate String
                // Combat_Begin->Combat_End (During Combat)
                // Draw-> (After Upkeep)
                // Upkeep->Combat_Begin (Before Declare Attackers)

                String[] split = phases.split("->", 2);
                phases = AllZone.getPhase().buildActivateString(split[0], split[1]);
            }

            setPhases(phases);
        }

        if (params.containsKey("ActivationCardsInHand")) {
            setActivateCardsInHand(Integer.parseInt(params.get("ActivationCardsInHand")));
        }

        if (params.containsKey("Planeswalker")) {
            setPlaneswalker(true);
        }

        if (params.containsKey("IsPresent")) {
            setIsPresent(params.get("IsPresent"));
            if (params.containsKey("PresentCompare")) {
                setPresentCompare(params.get("PresentCompare"));
            }
            if (params.containsKey("PresentZone")) {
                setPresentZone(Zone.smartValueOf(params.get("PresentZone")));
            }
        }

        if (params.containsKey("IsNotPresent")) {
            setIsPresent(params.get("IsNotPresent"));
            setPresentCompare("EQ0");
        }

        // basically PresentCompare for life totals:
        if (params.containsKey("ActivationLifeTotal")) {
            setLifeTotal(params.get("ActivationLifeTotal"));
            if (params.containsKey("ActivationLifeAmount")) {
                setLifeAmount(params.get("ActivationLifeAmount"));
            }
        }

        if (params.containsKey("CheckSVar")) {
            setSvarToCheck(params.get("CheckSVar"));
        }
        if (params.containsKey("SVarCompare")) {
            setSvarOperator(params.get("SVarCompare").substring(0, 2));
            setSvarOperand(params.get("SVarCompare").substring(2));
        }
    } // end setRestrictions()

    /**
     * <p>
     * canPlay.
     * </p>
     * 
     * @param c
     *            a {@link forge.Card} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public final boolean canPlay(final Card c, final SpellAbility sa) {
        if (c.isPhasedOut()) {
            return false;
        }

        PlayerZone cardZone = AllZone.getZoneOf(c);
        if (!cardZone.is(getZone())) {
            // If Card is not in the default activating zone, do some additional
            // checks
            // Not a Spell, or on Battlefield, return false
            if (!sa.isSpell() || cardZone.is(Zone.Battlefield)) {
                return false;
            } else if (!c.hasStartOfKeyword("May be played") || !getZone().equals(Zone.Hand)) {
                return false;
            }
        }

        Player activator = sa.getActivatingPlayer();
        if (activator == null) {
            activator = c.getController();
            System.out.println(c.getName() + " Did not have activator set in SpellAbility_Restriction.canPlay()");
        }

        if (isSorcerySpeed() && !Phase.canCastSorcery(activator)) {
            return false;
        }

        if (isPlayerTurn() && !AllZone.getPhase().isPlayerTurn(activator)) {
            return false;
        }

        if (isOpponentTurn() && AllZone.getPhase().isPlayerTurn(activator)) {
            return false;
        }

        if (!isAnyPlayer() && !activator.equals(c.getController())) {
            return false;
        }

        if (getActivationLimit() != -1 && getNumberTurnActivations() >= getActivationLimit()) {
            return false;
        }

        if (getPhases().size() > 0) {
            boolean isPhase = false;
            String currPhase = AllZone.getPhase().getPhase();
            for (String s : getPhases()) {
                if (s.equals(currPhase)) {
                    isPhase = true;
                    break;
                }
            }

            if (!isPhase) {
                return false;
            }
        }

        if (getCardsInHand() != -1) {
            if (activator.getCardsIn(Zone.Hand).size() != getCardsInHand()) {
                return false;
            }
        }
        if (isHellbent()) {
            if (!activator.hasHellbent()) {
                return false;
            }
        }
        if (isThreshold()) {
            if (!activator.hasThreshold()) {
                return false;
            }
        }
        if (isMetalcraft()) {
            if (!activator.hasMetalcraft()) {
                return false;
            }
        }
        if (getProwl() != null) {
            // only true if the activating player has damaged the opponent with
            // one of the specified types
            boolean prowlFlag = false;
            for (String type : getProwl()) {
                if (activator.hasProwl(type)) {
                    prowlFlag = true;
                }
            }
            if (!prowlFlag) {
                return false;
            }
        }
        if (getIsPresent() != null) {
            CardList list = AllZoneUtil.getCardsIn(getPresentZone());

            list = list.getValidCards(getIsPresent().split(","), activator, c);

            int right = 1;
            String rightString = getPresentCompare().substring(2);
            if (rightString.equals("X")) {
                right = CardFactoryUtil.xCount(c, c.getSVar("X"));
            } else {
                right = Integer.parseInt(getPresentCompare().substring(2));
            }
            int left = list.size();

            if (!AllZoneUtil.compare(left, getPresentCompare(), right)) {
                return false;
            }
        }

        if (getLifeTotal() != null) {
            int life = 1;
            if (getLifeTotal().equals("You")) {
                life = activator.getLife();
            }
            if (getLifeTotal().equals("Opponent")) {
                life = activator.getOpponent().getLife();
            }

            int right = 1;
            String rightString = getLifeAmount().substring(2);
            if (rightString.equals("X")) {
                right = CardFactoryUtil.xCount(sa.getSourceCard(), sa.getSourceCard().getSVar("X"));
            } else {
                right = Integer.parseInt(getLifeAmount().substring(2));
            }

            if (!AllZoneUtil.compare(life, getLifeAmount(), right)) {
                return false;
            }
        }

        if (isPwAbility()) {
            // Planeswalker abilities can only be activated as Sorceries
            if (!Phase.canCastSorcery(activator)) {
                return false;
            }

            for (SpellAbility pwAbs : c.getAllSpellAbilities()) {
                // check all abilities on card that have their planeswalker
                // restriction set to confirm they haven't been activated
                SpellAbility_Restriction restrict = pwAbs.getRestrictions();
                if (restrict.getPlaneswalker() && restrict.getNumberTurnActivations() > 0) {
                    return false;
                }
            }
        }

        if (getsVarToCheck() != null) {
            int svarValue = AbilityFactory.calculateAmount(c, getsVarToCheck(), sa);
            int operandValue = AbilityFactory.calculateAmount(c, getsVarOperand(), sa);

            if (!AllZoneUtil.compare(svarValue, getsVarOperator(), operandValue)) {
                return false;
            }

        }

        return true;
    } // canPlay()

} // end class SpellAbility_Restriction
