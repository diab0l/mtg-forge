package forge.trackable;

import org.w3c.dom.Element;

import forge.card.CardRarity;
import forge.game.zone.ZoneType;
import forge.trackable.TrackableTypes;
import forge.trackable.TrackableTypes.TrackableType;

public enum TrackableProperty {
    //Shared
    Text(TrackableTypes.StringType),
    PreventNextDamage(TrackableTypes.IntegerType),
    EnchantedBy(TrackableTypes.CardViewCollectionType),

    //Card 
    Owner(TrackableTypes.PlayerViewType),
    Controller(TrackableTypes.PlayerViewType),
    Zone(TrackableTypes.EnumType(ZoneType.class)),
    Cloned(TrackableTypes.BooleanType),
    FaceDown(TrackableTypes.BooleanType),
    FlipCard(TrackableTypes.BooleanType),
    Flipped(TrackableTypes.BooleanType),
    SplitCard(TrackableTypes.BooleanType),
    Transformed(TrackableTypes.BooleanType),
    SetCode(TrackableTypes.StringType),
    Rarity(TrackableTypes.EnumType(CardRarity.class)),
    Attacking(TrackableTypes.BooleanType),
    Blocking(TrackableTypes.BooleanType),
    PhasedOut(TrackableTypes.BooleanType),
    Sickness(TrackableTypes.BooleanType),
    Tapped(TrackableTypes.BooleanType),
    Token(TrackableTypes.BooleanType),
    Counters(TrackableTypes.CounterMapType),
    Damage(TrackableTypes.IntegerType),
    AssignedDamage(TrackableTypes.IntegerType),
    ShieldCount(TrackableTypes.IntegerType),
    ChosenType(TrackableTypes.StringType),
    ChosenColors(TrackableTypes.CardViewCollectionType),
    ChosenPlayer(TrackableTypes.PlayerViewType),
    NamedCard(TrackableTypes.StringType),
    Equipping(TrackableTypes.CardViewType),
    EquippedBy(TrackableTypes.CardViewCollectionType),
    Enchanting(TrackableTypes.GameEntityViewType),
    Fortifying(TrackableTypes.CardViewType),
    FortifiedBy(TrackableTypes.CardViewCollectionType),
    GainControlTargets(TrackableTypes.CardViewCollectionType),
    CloneOrigin(TrackableTypes.CardViewType),
    ImprintedCards(TrackableTypes.CardViewCollectionType),
    HauntedBy(TrackableTypes.CardViewCollectionType),
    Haunting(TrackableTypes.CardViewType),
    MustBlockCards(TrackableTypes.CardViewCollectionType),
    PairedWith(TrackableTypes.CardViewType),
    Original(TrackableTypes.CardStateViewType),
    Alternate(TrackableTypes.CardStateViewType),

    //Card State
    Name(TrackableTypes.StringType),
    Colors(TrackableTypes.ColorSetType),
    ImageKey(TrackableTypes.StringType),
    Type(TrackableTypes.StringSetType),
    ManaCost(TrackableTypes.ManaCostType),
    Power(TrackableTypes.IntegerType),
    Toughness(TrackableTypes.IntegerType),
    Loyalty(TrackableTypes.IntegerType),
    ChangedColorWords(TrackableTypes.StringMapType),
    ChangedTypes(TrackableTypes.StringMapType),
    HasDeathtouch(TrackableTypes.BooleanType),
    HasHaste(TrackableTypes.BooleanType),
    HasInfect(TrackableTypes.BooleanType),
    HasStorm(TrackableTypes.BooleanType),
    HasTrample(TrackableTypes.BooleanType),
    FoilIndex(TrackableTypes.IntegerType),

    //Player
    LobbyPlayer(TrackableTypes.PlayerViewType),
    Opponents(TrackableTypes.PlayerViewCollectionType),
    Life(TrackableTypes.IntegerType),
    PoisonCounters(TrackableTypes.IntegerType),
    MaxHandSize(TrackableTypes.IntegerType),
    HasUnlimitedHandSize(TrackableTypes.BooleanType),
    NumDrawnThisTurn(TrackableTypes.IntegerType),
    Keywords(TrackableTypes.BooleanType),
    CommanderInfo(TrackableTypes.StringType),
    Ante(TrackableTypes.CardViewCollectionType),
    Battlefield(TrackableTypes.CardViewCollectionType),
    Command(TrackableTypes.CardViewCollectionType),
    Exile(TrackableTypes.CardViewCollectionType),
    Flashback(TrackableTypes.CardViewCollectionType),
    Graveyard(TrackableTypes.CardViewCollectionType),
    Hand(TrackableTypes.CardViewCollectionType),
    Library(TrackableTypes.CardViewCollectionType),
    Mana(TrackableTypes.ManaMapType),

    //SpellAbility
    HostCard(TrackableTypes.CardViewType),
    Description(TrackableTypes.StringType),
    CanPlay(TrackableTypes.BooleanType),
    PromptIfOnlyPossibleAbility(TrackableTypes.BooleanType),

    //StackItem
    Key(TrackableTypes.StringType),
    SourceTrigger(TrackableTypes.IntegerType),
    SourceCard(TrackableTypes.CardViewType),
    Activator(TrackableTypes.PlayerViewType),
    TargetCards(TrackableTypes.CardViewCollectionType),
    TargetPlayers(TrackableTypes.PlayerViewCollectionType),
    SubInstance(TrackableTypes.StackItemViewType),
    Ability(TrackableTypes.BooleanType),
    OptionalTrigger(TrackableTypes.BooleanType),

    //Combat
    AttackersWithDefenders(null), //TODO
    AttackersWithBlockers(null),
    BandsWithDefenders(null),
    BandsWithBlockers(null),
    AttackersWithPlannedBlockers(null),
    BandsWithPlannedBlockers(null);

    private final TrackableType<?> type;

    private TrackableProperty(TrackableType<?> type0) {
        type = type0;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue() {
        return ((TrackableType<T>)type).getDefaultValue();
    }
    @SuppressWarnings("unchecked")
    public <T> T loadFromXml(Element el, String name, T oldValue) {
        return ((TrackableType<T>)type).loadFromXml(el, name, oldValue);
    }
    @SuppressWarnings("unchecked")
    public <T> void saveToXml(Element el, String name, T value) {
        ((TrackableType<T>)type).saveToXml(el, name, value);
    }
}
