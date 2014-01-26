package forge.game.ability;


import java.util.Map;
import java.util.TreeMap;

import forge.ai.SpellAbilityAi;
import forge.ai.ability.*;
import forge.game.ability.effects.*;
import forge.util.ReflectionUtil;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public enum ApiType {

    Abandon (AbandonEffect.class, AlwaysPlayAi.class),
    AddOrRemoveCounter (CountersPutOrRemoveEffect.class, CountersPutOrRemoveAi.class),
    AddPhase (AddPhaseEffect.class, AddPhaseAi.class),
    AddTurn (AddTurnEffect.class, AddTurnAi.class),
    Animate (AnimateEffect.class, AnimateAi.class),
    AnimateAll (AnimateAllEffect.class, AnimateAllAi.class),
    Attach (AttachEffect.class, AttachAi.class),
    Balance (BalanceEffect.class, BalanceAi.class),
    BecomesBlocked (BecomesBlockedEffect.class, BecomesBlockedAi.class),
    Bond (BondEffect.class, BondAi.class),
    ChangeTargets(ChangeTargetsEffect.class, ChangeTargetsAi.class),
    ChangeZone(ChangeZoneEffect.class, ChangeZoneAi.class),
    ChangeZoneAll(ChangeZoneAllEffect.class, ChangeZoneAllAi.class),
    /** This is <b>Modal</b>, like 'choose one - ' or 'choose two - '. <br> Might be great to rename this api and update all scripts.*/
    Charm(CharmEffect.class, CharmAi.class),
    ChooseCard (ChooseCardEffect.class, ChooseCardAi.class),
    ChooseColor (ChooseColorEffect.class, ChooseColorAi.class),
    ChooseNumber (ChooseNumberEffect.class, CannotPlayAi.class),
    ChoosePlayer (ChoosePlayerEffect.class, ChoosePlayerAi.class),
    ChooseSource (ChooseSourceEffect.class, ChooseSourceAi.class),
    ChooseType (ChooseTypeEffect.class, ChooseTypeAi.class),
    Clash (ClashEffect.class, ClashAi.class),
    Cleanup (CleanUpEffect.class, AlwaysPlayAi.class),
    Clone (CloneEffect.class, CloneAi.class),
    CopyPermanent (CopyPermanentEffect.class, CopyPermanentAi.class),
    CopySpellAbility (CopySpellAbilityEffect.class, CanPlayAsDrawbackAi.class),
    ControlPlayer(ControlPlayerEffect.class, CannotPlayAi.class),
    Counter (CounterEffect.class, CounterAi.class),
    DamageAll (DamageAllEffect.class, DamageAllAi.class),
    DealDamage (DamageDealEffect.class, DamageDealAi.class),
    Debuff (DebuffEffect.class, DebuffAi.class),
    DebuffAll (DebuffAllEffect.class, DebuffAllAi.class),
    DeclareCombatants(DeclareCombatantsEffect.class, CannotPlayAi.class),
    DelayedTrigger (DelayedTriggerEffect.class, DelayedTriggerAi.class),
    Destroy (DestroyEffect.class, DestroyAi.class),
    DestroyAll (DestroyAllEffect.class, DestroyAllAi.class),
    Dig (DigEffect.class, DigAi.class),
    DigUntil (DigUntilEffect.class, DigUntilAi.class),
    Discard (DiscardEffect.class, DiscardAi.class),
    DrainMana (DrainManaEffect.class, DrainManaAi.class),
    Draw (DrawEffect.class, DrawAi.class),
    EachDamage (DamageEachEffect.class, DamageEachAi.class),
    Effect (EffectEffect.class, EffectAi.class),
    Encode (EncodeEffect.class, EncodeAi.class),
    EndTurn (EndTurnEffect.class, EndTurnAi.class),
    ExchangeLife (LifeExchangeEffect.class, LifeExchangeAi.class),
    ExchangeControl (ControlExchangeEffect.class, ControlExchangeAi.class),
    ExchangePower (PowerExchangeEffect.class, PowerExchangeAi.class),
    ExchangeZone (ZoneExchangeEffect.class, ZoneExchangeAi.class),
    Fight (FightEffect.class, FightAi.class),
    FlipACoin (FlipCoinEffect.class, FlipACoinAi.class),
    Fog (FogEffect.class, FogAi.class),
    GainControl (ControlGainEffect.class, ControlGainAi.class),
    GainLife (LifeGainEffect.class, LifeGainAi.class),
    GainOwnership(OwnershipGainEffect.class, CannotPlayAi.class),
    GenericChoice (ChooseGenericEffect.class, ChooseGenericEffectAi.class),
    LoseLife (LifeLoseEffect.class, LifeLoseAi.class),
    LosesGame (GameLossEffect.class, GameLossAi.class),
    Mana (ManaEffect.class, ManaEffectAi.class),
    ManaReflected (ManaReflectedEffect.class, CannotPlayAi.class),
    Mill (MillEffect.class, MillAi.class),
    MoveCounter (CountersMoveEffect.class, CountersMoveAi.class),
    MultiplePiles (MultiplePilesEffect.class, CannotPlayAi.class),
    MustAttack (MustAttackEffect.class, MustAttackAi.class),
    MustBlock (MustBlockEffect.class, MustBlockAi.class),
    NameCard (ChooseCardNameEffect.class, ChooseCardNameAi.class),
    PeekAndReveal (PeekAndRevealEffect.class, PeekAndRevealAi.class),
    PermanentCreature (PermanentCreatureEffect.class, PermanentCreatureAi.class),
    PermanentNoncreature (PermanentNoncreatureEffect.class, PermanentNoncreatureAi.class),
    Phases (PhasesEffect.class, PhasesAi.class),
    Planeswalk(PlaneswalkEffect.class, AlwaysPlayAi.class),
    Play (PlayEffect.class, PlayAi.class),
    Poison (PoisonEffect.class, PoisonAi.class),
    PreventDamage (DamagePreventEffect.class, DamagePreventAi.class),
    PreventDamageAll (DamagePreventAllEffect.class, DamagePreventAllAi.class),
    Proliferate (CountersProliferateEffect.class, CountersProliferateAi.class),
    Protection (ProtectEffect.class, ProtectAi.class),
    ProtectionAll (ProtectAllEffect.class, ProtectAllAi.class),
    Pump (PumpEffect.class, PumpAi.class),
    PumpAll (PumpAllEffect.class, PumpAllAi.class),
    PutCounter (CountersPutEffect.class, CountersPutAi.class),
    PutCounterAll (CountersPutAllEffect.class, CountersPutAllAi.class),
    RearrangeTopOfLibrary (RearrangeTopOfLibraryEffect.class, RearrangeTopOfLibraryAi.class),
    Regenerate (RegenerateEffect.class, RegenerateAi.class),
    RegenerateAll (RegenerateAllEffect.class, RegenerateAllAi.class),
    RemoveCounter (CountersRemoveEffect.class, CountersRemoveAi.class),
    RemoveCounterAll (CountersRemoveAllEffect.class, CannotPlayAi.class),
    RemoveFromCombat (RemoveFromCombatEffect.class, RemoveFromCombatAi.class),
    ReorderZone (ReorderZoneEffect.class, AlwaysPlayAi.class),
    Repeat (RepeatEffect.class, RepeatAi.class),
    RepeatEach (RepeatEachEffect.class, RepeatEachAi.class),
    RestartGame (RestartGameEffect.class, RestartGameAi.class),
    Reveal (RevealEffect.class, RevealAi.class),
    RevealHand (RevealHandEffect.class, RevealHandAi.class),
    RollPlanarDice (RollPlanarDiceEffect.class, RollPlanarDiceAi.class),
    RunSVarAbility (RunSVarAbilityEffect.class, AlwaysPlayAi.class),
    Sacrifice (SacrificeEffect.class, SacrificeAi.class),
    SacrificeAll (SacrificeAllEffect.class, SacrificeAllAi.class),
    Scry (ScryEffect.class, ScryAi.class),
    SetInMotion (SetInMotionEffect.class, AlwaysPlayAi.class),
    SetLife (LifeSetEffect.class, LifeSetAi.class),
    SetState (SetStateEffect.class, SetStateAi.class),
    Shuffle (ShuffleEffect.class, ShuffleAi.class),
    SkipTurn (SkipTurnEffect.class, CannotPlayAi.class),
    StoreSVar (StoreSVarEffect.class, StoreSVarAi.class),
    Tap (TapEffect.class, TapAi.class),
    TapAll (TapAllEffect.class, TapAllAi.class),
    TapOrUntap (TapOrUntapEffect.class, TapOrUntapAi.class),
    TapOrUntapAll (TapOrUntapAllEffect.class, TapOrUntapAllAi.class),
    Token (TokenEffect.class, TokenAi.class, false), // Token AI and effect classes have state, they have to be re-created on each request 
    TwoPiles (TwoPilesEffect.class, TwoPilesAi.class),
    UnattachAll (UnattachAllEffect.class, UnattachAllAi.class),
    Untap (UntapEffect.class, UntapAi.class),
    UntapAll (UntapAllEffect.class, UntapAllAi.class),
    WinsGame (GameWinEffect.class, GameWinAi.class), 
    
    
    InternalEtbReplacement(ETBReplacementEffect.class, CanPlayAsDrawbackAi.class),
    InternalLegendaryRule(CharmEffect.class, LegendaryRuleAi.class), // Charm has empty resolve blocks, may act as a dummy
    InternalHaunt(CharmEffect.class, HauntAi.class); 

    private final Class<? extends SpellAbilityEffect> clsEffect;
    private final Class<? extends SpellAbilityAi> clsAi;
    
    private final SpellAbilityEffect instanceEffect;
    private final SpellAbilityAi instanceAi;

    
    
    private static final Map<String, ApiType> allValues = new TreeMap<String, ApiType>(String.CASE_INSENSITIVE_ORDER);

    ApiType(Class<? extends SpellAbilityEffect> clsEf, Class<? extends SpellAbilityAi> clsAi) {
        this(clsEf, clsAi, true);
    }
    
    ApiType(Class<? extends SpellAbilityEffect> clsEf, Class<? extends SpellAbilityAi> clsAI, final boolean isStateLess) {
        clsEffect = clsEf;
        clsAi = clsAI;
        
        instanceAi = isStateLess ? ReflectionUtil.makeDefaultInstanceOf(clsAi) : null;
        instanceEffect = isStateLess ? ReflectionUtil.makeDefaultInstanceOf(clsEf) : null; 
        
    }

    public static ApiType smartValueOf(String value) {
        if (allValues.isEmpty())
            for(ApiType c : ApiType.values())
                allValues.put(c.toString(), c);
        
        ApiType v = allValues.get(value);
        if ( v == null )
            throw new RuntimeException("Element " + value + " not found in ApiType enum");
        return v;
    }

    public SpellAbilityEffect getSpellEffect() {
        return instanceEffect != null ? instanceEffect : ReflectionUtil.makeDefaultInstanceOf(clsEffect);
    }

    public SpellAbilityAi getAi() {
        return instanceAi != null ? instanceAi : ReflectionUtil.makeDefaultInstanceOf(clsAi);
    }

}
