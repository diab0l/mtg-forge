package forge.game.card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.ImageKeys;
import forge.card.CardStateName;
import forge.card.CardEdition;
import forge.card.CardRarity;
import forge.card.CardRules;
import forge.card.CardTypeView;
import forge.card.ColorSet;
import forge.card.mana.ManaCost;
import forge.game.Direction;
import forge.game.GameEntityView;
import forge.game.combat.Combat;
import forge.game.player.Player;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.item.IPaperCard;
import forge.trackable.TrackableCollection;
import forge.trackable.TrackableObject;
import forge.trackable.TrackableProperty;
import forge.util.FCollectionView;


public class CardView extends GameEntityView {
    public static CardView get(Card c) {
        return c == null ? null : c.getView();
    }
    public static CardStateView getState(Card c, CardStateName state) {
        if (c == null) { return null; }
        CardState s = c.getState(state);
        return s == null ? null : s.getView();
    }

    public static CardView getCardForUi(IPaperCard pc) {
        return Card.getCardForUi(pc).getView();
    }

    public static TrackableCollection<CardView> getCollection(Iterable<Card> cards) {
        if (cards == null) {
            return null;
        }
        TrackableCollection<CardView> collection = new TrackableCollection<CardView>();
        for (Card c : cards) {
            if (c.getCardForUi() == c) { //only add cards that match their card for UI
                collection.add(c.getView());
            }
        }
        return collection;
    }

    public static boolean mayViewAny(Iterable<CardView> cards, PlayerView viewer) {
        if (cards == null) { return false; }

        for (CardView cv : cards) {
            if (cv.canBeShownTo(viewer)) {
                return true;
            }
        }
        return false;
    }

    public CardView(int id0) {
        super(id0);
        set(TrackableProperty.CurrentState, new CardStateView(id0, CardStateName.Original));
    }
    public CardView(int id0, String name0) {
        this(id0);
        getCurrentState().setName(name0);
        set(TrackableProperty.Name, name0);
        set(TrackableProperty.ChangedColorWords, new HashMap<String, String>());
        set(TrackableProperty.ChangedTypes, new HashMap<String, String>());
        set(TrackableProperty.Sickness, true);
    }
    public CardView(int id0, String name0, PlayerView ownerAndController, String imageKey) {
        this(id0, name0);
        set(TrackableProperty.Owner, ownerAndController);
        set(TrackableProperty.Controller, ownerAndController);
    }

    public PlayerView getOwner() {
        return get(TrackableProperty.Owner);
    }
    void updateOwner(Card c) {
        set(TrackableProperty.Owner, PlayerView.get(c.getOwner()));
    }

    public PlayerView getController() {
        return get(TrackableProperty.Controller);
    }
    void updateController(Card c) {
        set(TrackableProperty.Controller, PlayerView.get(c.getController()));
    }

    public ZoneType getZone() {
        return get(TrackableProperty.Zone);
    }
    void updateZone(Card c) {
        set(TrackableProperty.Zone, c.getZone() == null ? null : c.getZone().getZoneType());
    }

    public boolean isCloned() {
        return get(TrackableProperty.Cloned);
    }

    public boolean isFaceDown() {
        return getCurrentState().getState() == CardStateName.FaceDown;
    }

    public boolean isFlipCard() {
        return get(TrackableProperty.FlipCard);
    }

    public boolean isFlipped() {
        return getCurrentState().getState() == CardStateName.Flipped;
    }

    public boolean isSplitCard() {
        return get(TrackableProperty.SplitCard);
    }

    public boolean isTransformed() {
        return getCurrentState().getState() == CardStateName.Transformed;
    }

    public boolean isAttacking() {
        return get(TrackableProperty.Attacking);
    }
    void updateAttacking(Card c) {
        Combat combat = c.getGame().getCombat();
        set(TrackableProperty.Attacking, combat == null ? false : combat.isAttacking(c));
    }

    public boolean isBlocking() {
        return get(TrackableProperty.Blocking);
    }
    void updateBlocking(Card c) {
        Combat combat = c.getGame().getCombat();
        set(TrackableProperty.Blocking, combat == null ? false : combat.isBlocking(c));
    }

    public boolean isPhasedOut() {
        return get(TrackableProperty.PhasedOut);
    }
    void updatePhasedOut(Card c) {
        set(TrackableProperty.PhasedOut, c.isPhasedOut());
    }

    public boolean isFirstTurnControlled() {
        return get(TrackableProperty.Sickness);
    }
    public boolean hasSickness() {
        return isFirstTurnControlled() && !getCurrentState().hasHaste();
    }
    public boolean isSick() {
        return getZone() == ZoneType.Battlefield && getCurrentState().isCreature() && hasSickness();
    }
    void updateSickness(Card c) {
        set(TrackableProperty.Sickness, c.isFirstTurnControlled());
    }

    public boolean isTapped() {
        return get(TrackableProperty.Tapped);
    }
    void updateTapped(Card c) {
        set(TrackableProperty.Tapped, c.isTapped());
    }

    public boolean isToken() {
        return get(TrackableProperty.Token);
    }
    void updateToken(Card c) {
        set(TrackableProperty.Token, c.isToken());
    }

    public boolean isCommander() {
        return get(TrackableProperty.IsCommander);
    }
    void updateCommander(Card c) {
        set(TrackableProperty.IsCommander, c.isCommander());
    }

    public Map<CounterType, Integer> getCounters() {
        return get(TrackableProperty.Counters);
    }
    public boolean hasSameCounters(CardView otherCard) {
        Map<CounterType, Integer> counters = getCounters();
        if (counters == null) {
            return otherCard.getCounters() == null;
        }
        return counters.equals(otherCard.getCounters());
    }
    void updateCounters(Card c) {
        set(TrackableProperty.Counters, c.getCounters());
        CardStateView state = getCurrentState();
        state.updatePower(c);
        state.updateToughness(c);
        state.updateLoyalty(c);
    }

    public int getDamage() {
        return get(TrackableProperty.Damage);
    }
    void updateDamage(Card c) {
        set(TrackableProperty.Damage, c.getDamage());
    }

    public int getAssignedDamage() {
        return get(TrackableProperty.AssignedDamage);
    }
    void updateAssignedDamage(Card c) {
        set(TrackableProperty.AssignedDamage, c.getTotalAssignedDamage());
    }

    public int getLethalDamage() {
        return getCurrentState().getToughness() - getDamage() - getAssignedDamage();
    }

    public int getShieldCount() {
        return get(TrackableProperty.ShieldCount);
    }
    void updateShieldCount(Card c) {
        set(TrackableProperty.ShieldCount, c.getShieldCount());
    }

    public String getChosenType() {
        return get(TrackableProperty.ChosenType);
    }
    void updateChosenType(Card c) {
        set(TrackableProperty.ChosenType, c.getChosenType());
    }

    public List<String> getChosenColors() {
        return get(TrackableProperty.ChosenColors);
    }
    void updateChosenColors(Card c) {
        set(TrackableProperty.ChosenColors, c.getChosenColors());
    }

    public PlayerView getChosenPlayer() {
        return get(TrackableProperty.ChosenPlayer);
    }
    void updateChosenPlayer(Card c) {
        set(TrackableProperty.ChosenPlayer, PlayerView.get(c.getChosenPlayer()));
    }

    public Direction getChosenDirection() {
        return get(TrackableProperty.ChosenDirection);
    }
    void updateChosenDirection(Card c) {
        set(TrackableProperty.ChosenDirection, c.getChosenDirection());
    }

    private String getRemembered() {
        return get(TrackableProperty.Remembered);
    }
    void updateRemembered(Card c) {
        if (c.getRemembered() == null) {
            set(TrackableProperty.Remembered, null);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\r\nRemembered: \r\n");
        for (final Object o : c.getRemembered()) {
            if (o instanceof Card) {
                final Card card = (Card) o;
                if (card.isFaceDown()) {
                    sb.append("Face Down");
                    // face-down cards don't show unique number to avoid cheating
                }
                else {
                    sb.append(card.getName());
                    sb.append(" (");
                    sb.append(card.getId());
                    sb.append(")");
                }
            }
            else if (o != null) {
                sb.append(o.toString());
            }
            sb.append("\r\n");
        }
        set(TrackableProperty.Remembered, sb.toString());
    }

    public String getNamedCard() {
        return get(TrackableProperty.NamedCard);
    }
    void updateNamedCard(Card c) {
        set(TrackableProperty.NamedCard, c.getNamedCard());
    }

    public boolean mayPlayerLook(PlayerView pv) {
        TrackableCollection<PlayerView> col = get(TrackableProperty.PlayerMayLook);
        if (col != null && col.contains(pv)) {
            return true;
        }
        col = get(TrackableProperty.PlayerMayLookTemp);
        if (col != null && col.contains(pv)) {
            return true;
        }
        return false;
    }
    void setPlayerMayLook(Player p, boolean mayLook, boolean temp) {
        TrackableProperty prop = temp ? TrackableProperty.PlayerMayLookTemp : TrackableProperty.PlayerMayLook;
        TrackableCollection<PlayerView> col = get(prop);
        if (mayLook) {
            if (col == null) {
                col = new TrackableCollection<PlayerView>(p.getView());
                set(prop, col);
            }
            else if (col.add(p.getView())) {
                flagAsChanged(prop);
            }
        }
        else if (col != null) {
            if (col.remove(p.getView())) {
                if (col.isEmpty()) {
                    set(prop, null);
                }
                else {
                    flagAsChanged(prop);
                }
            }
        }
    }
    public boolean canBeShownTo(final PlayerView viewer) {
        if (viewer == null) { return false; }

        ZoneType zone = getZone();
        if (zone == null) { return true; } //cards outside any zone are visible to all

        final PlayerView controller = getController();
        switch (zone) {
        case Ante:
        case Command:
        case Exile:
        case Battlefield:
        case Graveyard:
        case Flashback:
        case Stack:
            //cards in these zones are visible to all
            return true;
        case Hand:
            if (controller.hasKeyword("Play with your hand revealed.")) {
                return true;
            }
            //fall through
        case Sideboard:
            //face-up cards in these zones are hidden to opponents unless they specify otherwise
            if (controller.isOpponentOf(viewer) && !getCurrentState().getOpponentMayLook()) {
                break;
            }
            return true;
        case Library:
        case PlanarDeck:
            //cards in these zones are hidden to all unless they specify otherwise
            if (controller == viewer && getCurrentState().getYouMayLook()) {
                return true;
            }
            if (controller.isOpponentOf(viewer) && getCurrentState().getOpponentMayLook()) {
                return true;
            }
            break;
        case SchemeDeck:
            // true for now, to actually see the Scheme cards (can't see deck anyway)
            return true;
        }

        // special viewing permissions for viewer
        if (mayPlayerLook(viewer)) {
            return true;
        }

        //if viewer is controlled by another player, also check if card can be shown to that player
        PlayerView mindSlaveMaster = controller.getMindSlaveMaster();
        if (mindSlaveMaster != null && mindSlaveMaster == viewer) {
            return canBeShownTo(controller);
        }
        return false;
    }
    public boolean canFaceDownBeShownTo(final PlayerView viewer) {
        if (!isFaceDown()) {
            return true;
        }
        if (viewer.hasKeyword("CanSeeOpponentsFaceDownCards")) {
            return true;
        }

        // special viewing permissions for viewer
        if (mayPlayerLook(viewer)) {
            return true;
        }

        //if viewer is controlled by another player, also check if face can be shown to that player
        PlayerView mindSlaveMaster = viewer.getMindSlaveMaster();
        if (mindSlaveMaster != null && canFaceDownBeShownTo(mindSlaveMaster)) {
            return true;
        }
        return !getController().isOpponentOf(viewer) || getCurrentState().getOpponentMayLook();
    }

    public CardView getEquipping() {
        return get(TrackableProperty.Equipping);
    }

    public FCollectionView<CardView> getEquippedBy() {
        return get(TrackableProperty.EquippedBy);
    }

    public boolean isEquipped() {
        return getEquippedBy() != null; //isEmpty check not needed since we won't keep an empty collection around
    }

    public GameEntityView getEnchanting() {
        return get(TrackableProperty.Enchanting);
    }
    void updateEnchanting(Card c) {
        set(TrackableProperty.Enchanting, GameEntityView.get(c.getEnchanting()));
    }

    public CardView getEnchantingCard() {
        GameEntityView enchanting = getEnchanting();
        if (enchanting instanceof CardView) {
            return (CardView) enchanting;
        }
        return null;
    }
    public PlayerView getEnchantingPlayer() {
        GameEntityView enchanting = getEnchanting();
        if (enchanting instanceof PlayerView) {
            return (PlayerView) enchanting;
        }
        return null;
    }

    public CardView getFortifying() {
        return get(TrackableProperty.Fortifying);
    }

    public FCollectionView<CardView> getFortifiedBy() {
        return get(TrackableProperty.FortifiedBy);
    }

    public boolean isFortified() {
        return getFortifiedBy() != null;
    }

    public FCollectionView<CardView> getGainControlTargets() {
        return get(TrackableProperty.GainControlTargets);
    }

    public CardView getCloneOrigin() {
        return get(TrackableProperty.CloneOrigin);
    }

    public FCollectionView<CardView> getImprintedCards() {
        return get(TrackableProperty.ImprintedCards);
    }

    public FCollectionView<CardView> getHauntedBy() {
        return get(TrackableProperty.HauntedBy);
    }

    public CardView getHaunting() {
        return get(TrackableProperty.Haunting);
    }

    public FCollectionView<CardView> getMustBlockCards() {
        return get(TrackableProperty.MustBlockCards);
    }

    public CardView getPairedWith() {
        return get(TrackableProperty.PairedWith);
    }

    public Map<String, String> getChangedColorWords() {
        return get(TrackableProperty.ChangedColorWords);
    }
    void updateChangedColorWords(Card c) {
        set(TrackableProperty.ChangedColorWords, c.getChangedTextColorWords());
    }

    public Map<String, String> getChangedTypes() {
        return get(TrackableProperty.ChangedTypes);
    }
    void updateChangedTypes(Card c) {
        set(TrackableProperty.ChangedTypes, c.getChangedTextTypeWords());
    }

    void updateRulesText(CardRules rules, CardTypeView type) {
        String rulesText = null;

        if (type.isVanguard()) {
            rulesText = "Hand Modifier: " + rules.getHand() + 
                    "\r\nLife Modifier: " + rules.getLife();
        }
        set(TrackableProperty.RulesText, rulesText);
    }

    void updateNonAbilityText(Card c) {
        set(TrackableProperty.NonAbilityText, c.getNonAbilityText());
    }

    public String getText() {
        return getText(getCurrentState());
    }
    public String getText(CardStateView state) {
        if (getId() < 0) {
            return getOracleText();
        }

        final StringBuilder sb = new StringBuilder();

        String rulesText = get(TrackableProperty.RulesText);
        if (!rulesText.isEmpty()) {
            sb.append(rulesText).append("\r\n\r\n");
        }
        if (isCommander()) {
            sb.append(getOwner()).append("'s Commander\r\n");
            sb.append(CardFactoryUtil.getCommanderInfo(Player.get(getOwner()))).append("\r\n");
        }
        sb.append(state.getAbilityText());

        if (state.getState() == CardStateName.LeftSplit && getZone() != ZoneType.Stack) {
            //ensure ability text for right half of split card is included unless spell is on stack
            sb.append("\r\n\r\n").append(getAlternateState().getAbilityText());
        }

        String nonAbilityText = get(TrackableProperty.NonAbilityText);
        int blockAdditional = state.getBlockAdditional();
        if (blockAdditional > 1) {
            final StringBuilder ab = new StringBuilder();
            ab.append("CARDNAME can block an additional ");
            ab.append(blockAdditional);
            ab.append(" creatures.");
            nonAbilityText = nonAbilityText.replaceFirst("CARDNAME can block an additional creature.", ab.toString());
            nonAbilityText = nonAbilityText.replaceAll("CARDNAME can block an additional creature.", "");
            nonAbilityText = nonAbilityText.replaceAll("\r\n\r\n\r\n", "");
        }
        if (!nonAbilityText.isEmpty()) {
            sb.append("\r\n \r\nNon ability features: \r\n");
            sb.append(nonAbilityText.replaceAll("CARDNAME", getName()));
        }

        sb.append(getRemembered());

        PlayerView chosenPlayer = getChosenPlayer();
        if (chosenPlayer != null) {
            sb.append("\r\n[Chosen player: ");
            sb.append(chosenPlayer);
            sb.append("]\r\n");
        }

        Direction chosenDirection = getChosenDirection();
        if (chosenDirection != null) {
            sb.append("\r\n[Chosen direction: ");
            sb.append(chosenDirection);
            sb.append("]\r\n");
        }

        Iterable<CardView> hauntedBy = getHauntedBy();
        if (hauntedBy != null) {
            sb.append("Haunted by: ");
            boolean needDelim = false;
            for (final CardView c : hauntedBy) {
                if (needDelim) {
                    sb.append(",");
                }
                else { needDelim = false; }
                sb.append(c);
            }
            sb.append("\r\n");
        }

        CardView haunting = getHaunting();
        if (haunting != null) {
            sb.append("Haunting: ").append(haunting);
            sb.append("\r\n");
        }

        CardView pairedWith = getPairedWith();
        if (pairedWith != null) {
            sb.append("\r\n \r\nPaired With: ").append(pairedWith);
            sb.append("\r\n");
        }

        String cloner = get(TrackableProperty.Cloner);
        if (!cloner.isEmpty()) {
            sb.append("\r\nCloned by: ").append(cloner);
        }

        return sb.toString().trim();
    }

    public String getOracleText() {
        return get(TrackableProperty.OracleText);
    }
    void updateOracleText(Card c) {
        set(TrackableProperty.OracleText, c.getOracleText().replace("\\n", "\r\n").trim());
    }

    public CardStateView getCurrentState() {
        return get(TrackableProperty.CurrentState);
    }

    public boolean hasAlternateState() {
        return getAlternateState() != null;
    }
    public CardStateView getAlternateState() {
        return get(TrackableProperty.AlternateState);
    }
    CardStateView createAlternateState(CardStateName state0) {
        return new CardStateView(getId(), state0);
    }

    public CardStateView getState(final boolean alternate0) {
        return alternate0 ? getAlternateState() : getCurrentState();
    }
    void updateState(Card c) {
        updateName(c);

        boolean isSplitCard = c.isSplitCard();
        set(TrackableProperty.Cloned, c.isCloned());
        set(TrackableProperty.SplitCard, isSplitCard);
        set(TrackableProperty.FlipCard, c.isFlipCard());

        CardStateView cloner = CardView.getState(c, CardStateName.Cloner);
        set(TrackableProperty.Cloner, cloner == null ? null : cloner.getName() + " (" + cloner.getId() + ")");

        CardState currentState = isSplitCard ? c.getState(CardStateName.LeftSplit) : c.getCurrentState();
        CardStateView currentStateView = currentState.getView();
        if (getCurrentState() != currentStateView) {
            set(TrackableProperty.CurrentState, currentStateView);
            currentStateView.updatePower(c); //ensure power, toughness, and loyalty updated when current state changes
            currentStateView.updateToughness(c);
            currentStateView.updateLoyalty(c);
        }
        currentState.getView().updateKeywords(c, currentState); //update keywords even if state doesn't change

        CardState alternateState = isSplitCard ? c.getState(CardStateName.RightSplit) : c.getAlternateState();
        if (alternateState == null) {
            set(TrackableProperty.AlternateState, null);
        }
        else {
            CardStateView alternateStateView = alternateState.getView();
            if (getAlternateState() != alternateStateView) {
                set(TrackableProperty.AlternateState, alternateStateView);
                alternateStateView.updatePower(c); //ensure power, toughness, and loyalty updated when current state changes
                alternateStateView.updateToughness(c);
                alternateStateView.updateLoyalty(c);
            }
            alternateState.getView().updateKeywords(c, alternateState);
        }
    }

    @Override
    public String toString() {
        String name = getName();
        if (getId() <= 0) { //if fake card, just return name
            return name;
        }

        if (name.isEmpty()) {
            CardStateView alternate = getAlternateState();
            if (alternate != null) {
                return "Face-down card (" + getAlternateState().getName() + ")";
            }
            return "(" + getId() + ")";
        }
        return name + " (" + getId() + ")";
    }

    public class CardStateView extends TrackableObject {
        private final CardStateName state;

        public CardStateView(int id0, CardStateName state0) {
            super(id0);
            state = state0;
        }

        @Override
        public String toString() {
            return getName() + " (" + getId() + ")";
        }

        public CardView getCard() {
            return CardView.this;
        }

        public CardStateName getState() {
            return state;
        }

        public String getName() {
            return get(TrackableProperty.Name);
        }
        void updateName(CardState c) {
            setName(c.getName());

            if (CardView.this.getCurrentState() == this) {
                Card card = Card.get(CardView.this);
                if (card != null) {
                    CardView.this.updateName(card);
                }
            }
        }
        private void setName(String name0) {
            set(TrackableProperty.Name, name0);
        }

        public ColorSet getColors() {
            return get(TrackableProperty.Colors);
        }
        void updateColors(Card c) {
            set(TrackableProperty.Colors, c.determineColor());
        }
        void updateColors(CardState c) {
            set(TrackableProperty.Colors, c.determineColor());
        }

        public String getImageKey(PlayerView viewer) {
            if (viewer == null || canBeShownTo(viewer)) {
                return get(TrackableProperty.ImageKey);
            }
            return ImageKeys.HIDDEN_CARD;
        }
        void updateImageKey(Card c) {
            set(TrackableProperty.ImageKey, c.getImageKey());
        }
        void updateImageKey(CardState c) {
            set(TrackableProperty.ImageKey, c.getImageKey());
        }

        public CardTypeView getType() {
            return get(TrackableProperty.Type);
        }
        void updateType(CardState c) {
            CardTypeView type = c.getType();
            if (CardView.this.getCurrentState() == this) {
                Card card = Card.get(CardView.this);
                if (card != null) {
                    type = type.getTypeWithChanges(card.getChangedCardTypes()); //TODO: find a better way to do this
                    CardView.this.updateRulesText(card.getRules(), type);
                }
            }
            set(TrackableProperty.Type, type);
        }

        public ManaCost getManaCost() {
            return get(TrackableProperty.ManaCost);
        }
        void updateManaCost(CardState c) {
            set(TrackableProperty.ManaCost, c.getManaCost());
        }

        public int getPower() {
            return get(TrackableProperty.Power);
        }
        void updatePower(Card c) {
            set(TrackableProperty.Power, c.getNetPower());
        }
        void updatePower(CardState c) {
            if (CardView.this.getCurrentState() == this) {
                Card card = Card.get(CardView.this);
                if (card != null) {
                    updatePower(card); //TODO: find a better way to do this
                    return;
                }
            }
            set(TrackableProperty.Power, c.getBasePower());
        }

        public int getToughness() {
            return get(TrackableProperty.Toughness);
        }
        void updateToughness(Card c) {
            set(TrackableProperty.Toughness, c.getNetToughness());
        }
        void updateToughness(CardState c) {
            if (CardView.this.getCurrentState() == this) {
                Card card = Card.get(CardView.this);
                if (card != null) {
                    updateToughness(card); //TODO: find a better way to do this
                    return;
                }
            }
            set(TrackableProperty.Toughness, c.getBaseToughness());
        }

        public int getLoyalty() {
            return get(TrackableProperty.Loyalty);
        }
        void updateLoyalty(Card c) {
            set(TrackableProperty.Loyalty, c.getCurrentLoyalty());
        }
        void updateLoyalty(CardState c) {
            if (CardView.this.getCurrentState() == this) {
                Card card = Card.get(CardView.this);
                if (card != null) {
                    updateLoyalty(card); //TODO: find a better way to do this
                    return;
                }
            }
            set(TrackableProperty.Loyalty, 0); //alternates don't need loyalty
        }

        public String getSetCode() {
            return get(TrackableProperty.SetCode);
        }
        void updateSetCode(CardState c) {
            set(TrackableProperty.SetCode, c.getSetCode());
        }

        public CardRarity getRarity() {
            return get(TrackableProperty.Rarity);
        }
        void updateRarity(CardState c) {
            set(TrackableProperty.Rarity, c.getRarity());
        }

        private int foilIndexOverride = -1;
        public int getFoilIndex() {
            if (foilIndexOverride >= 0) {
                return foilIndexOverride;
            }
            return get(TrackableProperty.FoilIndex);
        }
        void updateFoilIndex(Card c) {
            updateFoilIndex(c.getCurrentState());
        }
        void updateFoilIndex(CardState c) {
            set(TrackableProperty.FoilIndex, c.getFoil());
        }
        public void setFoilIndexOverride(int index0) {
            if (index0 < 0) {
                index0 = CardEdition.getRandomFoil(getSetCode());
            }
            foilIndexOverride = index0;
        }

        public boolean hasDeathtouch() {
            return get(TrackableProperty.HasDeathtouch);
        }
        public boolean hasHaste() {
            return get(TrackableProperty.HasHaste);
        }
        public boolean hasInfect() {
            return get(TrackableProperty.HasInfect);
        }
        public boolean hasStorm() {
            return get(TrackableProperty.HasStorm);
        }
        public boolean hasTrample() {
            return get(TrackableProperty.HasTrample);
        }
        public boolean getYouMayLook() {
            return get(TrackableProperty.YouMayLook);
        }
        public boolean getOpponentMayLook() {
            return get(TrackableProperty.OpponentMayLook);
        }
        public int getBlockAdditional() {
            return get(TrackableProperty.BlockAdditional);
        }
        public String getAbilityText() {
            return get(TrackableProperty.AbilityText);
        }
        void updateAbilityText(Card c, CardState state) {
            set(TrackableProperty.AbilityText, c.getAbilityText(state));
        }
        void updateKeywords(Card c, CardState state) {
            set(TrackableProperty.HasDeathtouch, c.hasKeyword("Deathtouch", state));
            set(TrackableProperty.HasHaste, c.hasKeyword("Haste", state));
            set(TrackableProperty.HasInfect, c.hasKeyword("Infect", state));
            set(TrackableProperty.HasStorm, c.hasKeyword("Storm", state));
            set(TrackableProperty.HasTrample, c.hasKeyword("Trample", state));
            set(TrackableProperty.YouMayLook, c.hasKeyword("You may look at this card."));
            set(TrackableProperty.OpponentMayLook, c.hasKeyword("Your opponent may look at this card."));
            set(TrackableProperty.BlockAdditional, c.getAmountOfKeyword("CARDNAME can block an additional creature.", state));
            updateAbilityText(c, state);
        }

        public boolean isBasicLand() {
            return getType().isBasicLand();
        }
        public boolean isCreature() {
            return getType().isCreature();
        }
        public boolean isLand() {
            return getType().isLand();
        }
        public boolean isPlane() {
            return getType().isPlane();
        }
        public boolean isPhenomenon() {
            return getType().isPhenomenon();
        }
        public boolean isPlaneswalker() {
            return getType().isPlaneswalker();
        }
    }

    //special methods for updating card and player properties as needed and returning the new collection
    Card setCard(Card oldCard, Card newCard, TrackableProperty key) {
        if (newCard != oldCard) {
            set(key, CardView.get(newCard));
        }
        return newCard;
    }
    CardCollection setCards(CardCollection oldCards, CardCollection newCards, TrackableProperty key) {
        if (newCards == null || newCards.isEmpty()) { //avoid storing empty collections
            set(key, null);
            return null;
        }
        set(key, CardView.getCollection(newCards)); //TODO prevent overwriting list if not necessary
        return newCards;
    }
    CardCollection setCards(CardCollection oldCards, Iterable<Card> newCards, TrackableProperty key) {
        if (newCards == null) {
            set(key, null);
            return null;
        }
        return setCards(oldCards, new CardCollection(newCards), key);
    }
    CardCollection addCard(CardCollection oldCards, Card cardToAdd, TrackableProperty key) {
        if (cardToAdd == null) { return oldCards; }

        if (oldCards == null) {
            oldCards = new CardCollection();
        }
        if (oldCards.add(cardToAdd)) {
            TrackableCollection<CardView> views = get(key);
            if (views == null) {
                views = new TrackableCollection<CardView>();
                views.add(cardToAdd.getView());;
                set(key, views);
            }
            else if (views.add(cardToAdd.getView())) {
                flagAsChanged(key);
            }
        }
        return oldCards;
    }
    CardCollection addCards(CardCollection oldCards, Iterable<Card> cardsToAdd, TrackableProperty key) {
        if (cardsToAdd == null) { return oldCards; }

        TrackableCollection<CardView> views = get(key);
        if (oldCards == null) {
            oldCards = new CardCollection();
        }
        boolean needFlagAsChanged = false;
        for (Card c : cardsToAdd) {
            if (oldCards.add(c)) {
                if (views == null) {
                    views = new TrackableCollection<CardView>();
                    views.add(c.getView());
                    set(key, views);
                }
                else if (views.add(c.getView())) {
                    needFlagAsChanged = true;
                }
            }
        }
        if (needFlagAsChanged) {
            flagAsChanged(key);
        }
        return oldCards;
    }
    CardCollection removeCard(CardCollection oldCards, Card cardToRemove, TrackableProperty key) {
        if (cardToRemove == null || oldCards == null) { return oldCards; }

        if (oldCards.remove(cardToRemove)) {
            TrackableCollection<CardView> views = get(key);
            if (views != null && views.remove(cardToRemove.getView())) {
                if (views.isEmpty()) {
                    set(key, null); //avoid keeping around an empty collection
                }
                else {
                    flagAsChanged(key);
                }
            }
            if (oldCards.isEmpty()) {
                oldCards = null; //avoid keeping around an empty collection
            }
        }
        return oldCards;
    }
    CardCollection removeCards(CardCollection oldCards, Iterable<Card> cardsToRemove, TrackableProperty key) {
        if (cardsToRemove == null || oldCards == null) { return oldCards; }

        TrackableCollection<CardView> views = get(key);
        boolean needFlagAsChanged = false;
        for (Card c : cardsToRemove) {
            if (oldCards.remove(c)) {
                if (views != null && views.remove(c.getView())) {
                    if (views.isEmpty()) {
                        views = null;
                        set(key, null); //avoid keeping around an empty collection
                        needFlagAsChanged = false; //doesn't need to be flagged a second time
                    }
                    else {
                        needFlagAsChanged = true;
                    }
                }
                if (oldCards.isEmpty()) {
                    oldCards = null; //avoid keeping around an empty collection
                    break;
                }
            }
        }
        if (needFlagAsChanged) {
            flagAsChanged(key);
        }
        return oldCards;
    }
    CardCollection clearCards(CardCollection oldCards, TrackableProperty key) {
        if (oldCards != null) {
            set(key, null);
        }
        return null;
    }
}
