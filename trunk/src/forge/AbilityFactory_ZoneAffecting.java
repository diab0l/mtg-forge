package forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AbilityFactory_ZoneAffecting {
	public static SpellAbility createAbilityDraw(final AbilityFactory AF){
		final SpellAbility abDraw = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = 5445572699000471299L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
			// when getStackDesc is called, just build exactly what is happening
				return drawStackDescription(af, this);
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();		
			}
			
			public boolean canPlayAI()
			{
				return drawCanPlayAI(af,this);
			}
			
			@Override
			public void resolve() {
				drawResolve(af, this);
			}
			
		};
		return abDraw;
	}
	
	public static SpellAbility createSpellDraw(final AbilityFactory AF){
		final SpellAbility spDraw = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = -4990932993654533449L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
				// when getStackDesc is called, just build exactly what is happening
				return drawStackDescription(af, this);
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				return drawCanPlayAI(af, this);
			}
			
			@Override
			public void resolve() {
				drawResolve(af, this);
			}
			
		};
		return spDraw;
	}
	
	public static SpellAbility createDrawbackDraw(final AbilityFactory AF){
		final SpellAbility dbDraw = new Ability_Sub(AF.getHostCard(), AF.getAbTgt()){
			private static final long serialVersionUID = -4990932993654533449L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
				// when getStackDesc is called, just build exactly what is happening
				return drawStackDescription(af, this);
			}

			@Override
			public void resolve() {
				drawResolve(af, this);
			}

			@Override
			public boolean chkAI_Drawback() {
				return drawTargetAI(af, this);
			}
			
		};
		return dbDraw;
	}
	
	public static String drawStackDescription(AbilityFactory af, SpellAbility sa){
		Player player = af.getAbTgt() == null ? sa.getActivatingPlayer() : sa.getTargetPlayer(); 
		StringBuilder sb = new StringBuilder();
		
		if (!(sa instanceof Ability_Sub))
			sb.append(sa.getSourceCard().getName()).append(" - ");
		else
			sb.append(" ");
		
		sb.append(player.toString());
		sb.append(" draws (");
		sb.append(AbilityFactory.calculateAmount(sa.getSourceCard(), af.getMapParams().get("NumCards"), sa));
		sb.append(").");
		
		Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null){
        	abSub.setParent(sa);
        	sb.append(abSub.getStackDescription());
        }
		
		return sb.toString();
	}
	
	public static boolean drawCanPlayAI(final AbilityFactory af, SpellAbility sa){
		// AI cannot use this properly until he can use SAs during Humans turn
		if (!ComputerUtil.canPayCost(sa))
			return false;
		
		Target tgt = af.getAbTgt();
		Card source = sa.getSourceCard();
		Ability_Cost abCost = af.getAbCost();
		
		if (abCost != null){
			// AI currently disabled for these costs
			if (abCost.getSacCost()){
				return false;
			}
			if (abCost.getLifeCost()){
				if (AllZone.ComputerPlayer.getLife() - abCost.getLifeAmount() < 4)
					return false;
			}
			if (abCost.getDiscardCost()) 	return false;
			
			if (abCost.getSubCounter()) {
				if (abCost.getCounterType().equals(Counters.P1P1)) return false; // Other counters should be used 
			}
			
		}
			
		boolean bFlag = drawTargetAI(af, sa);
		
		if (!bFlag)
			return false;
		
		if (tgt != null){
			ArrayList<Player> players = tgt.getTargetPlayers();
			if (players.size() > 0 && players.get(0).equals(AllZone.HumanPlayer))
				return true;
		}
		
		Random r = new Random();
		boolean randomReturn = r.nextFloat() <= Math.pow(.6667, source.getAbilityUsed());
		
		// some other variables here, like handsize vs. maxHandSize

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
        	randomReturn &= subAb.chkAI_Drawback();
		return randomReturn;
	}
	
    public static boolean drawTargetAI(AbilityFactory af, SpellAbility sa) {
        Target tgt = af.getAbTgt();
        HashMap<String,String> params = af.getMapParams();
        
        int computerHandSize = AllZoneUtil.getCardsInZone(Constant.Zone.Hand, AllZone.ComputerPlayer).size();
        int humanLibrarySize = AllZoneUtil.getCardsInZone(Constant.Zone.Library, AllZone.HumanPlayer).size();
        int computerLibrarySize = AllZoneUtil.getCardsInZone(Constant.Zone.Library, AllZone.ComputerPlayer).size();
        int computerMaxHandSize = AllZone.ComputerPlayer.getMaxHandSize();
        
        // todo: handle deciding what X would be around here for Braingeyser type cards
        int numCards = 1;
        if (params.containsKey("NumCards"))
        	numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
        	
        
        if (tgt != null) {
            // ability is targeted
            tgt.resetTargets();
            
            if (!AllZone.HumanPlayer.cantLose() && numCards >= humanLibrarySize) {
                // Deck the Human? DO IT!
                tgt.addTarget(AllZone.HumanPlayer);
                return true;
            }
            
            if (numCards >= computerLibrarySize) {
                // Don't deck your self
                return false;
            }
            
            if (computerHandSize + numCards > computerMaxHandSize) {
                // Don't draw too many cards and then risk discarding cards at EOT
                return false;
            }
            
            tgt.addTarget(AllZone.ComputerPlayer);
        }
        else {
            // ability is not targeted
            if (numCards >= computerLibrarySize) {
                // Don't deck yourself
                return false;
            }
            if (computerHandSize + numCards > computerMaxHandSize) {
                // Don't draw too many cards and then risk discarding cards at EOT
                return false;
            }
        }
        return true;
    }// drawTargetAI()
	
	public static void drawResolve(final AbilityFactory af, final SpellAbility sa){
		HashMap<String,String> params = af.getMapParams();
		
		Card source = sa.getSourceCard();
		int numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
		
		ArrayList<Player> tgtPlayers;

		Target tgt = af.getAbTgt();
		if (tgt != null)
			tgtPlayers = tgt.getTargetPlayers();
		else{
			tgtPlayers = new ArrayList<Player>();
			tgtPlayers.add(sa.getActivatingPlayer());
		}
		
		for(Player p : tgtPlayers)
			if (tgt == null || p.canTarget(af.getHostCard())){
				if (params.containsKey("NextUpkeep"))
					for(int i = 0; i < numCards; i++)
						p.addSlowtripList(source);
				else
					p.drawCards(numCards);		
				
			}

		if (af.hasSubAbility()){
			Ability_Sub abSub = sa.getSubAbility();
			if (abSub != null){
	     	   if (abSub.getParent() == null)
	     		  abSub.setParent(sa);
	     	   abSub.resolve();
	        }
	        else{
				String DrawBack = params.get("SubAbility");
				if (af.hasSubAbility())
					 CardFactoryUtil.doDrawBack(DrawBack, 0, source.getController(), source.getController().getOpponent(), tgtPlayers.get(0), source, null, sa);
	        }
		}
	}
	
	
	// ******************** MILL ****************************
	
	public static SpellAbility createAbilityMill(final AbilityFactory AF){
		final SpellAbility abMill = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = 5445572699000471299L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
				return millStackDescription(this, af);
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();		
			}
			
			public boolean canPlayAI()
			{
				return millCanPlayAI(af,this);
			}
			
			@Override
			public void resolve() {
				millResolve(af, this);
			}
			
		};
		return abMill;
	}
	
	public static SpellAbility createSpellMill(final AbilityFactory AF){
		final SpellAbility spMill = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()){
			private static final long serialVersionUID = -4990932993654533449L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
				return millStackDescription(this, af);
			}
			
			public boolean canPlay(){
				// super takes care of AdditionalCosts
				return super.canPlay();	
			}
			
			public boolean canPlayAI()
			{
				return millCanPlayAI(af, this);
			}
			
			@Override
			public void resolve() {
				millResolve(af, this);
			}
			
		};
		return spMill;
	}
	
	public static SpellAbility createDrawbackMill(final AbilityFactory AF){
		final SpellAbility dbMill = new Ability_Sub(AF.getHostCard(), AF.getAbTgt()){
			private static final long serialVersionUID = -4990932993654533449L;
			
			final AbilityFactory af = AF;
			
			@Override
			public String getStackDescription(){
				return millStackDescription(this, af);
			}
			
			public boolean canPlayAI()
			{
				return millCanPlayAI(af, this);
			}
			
			@Override
			public void resolve() {
				millResolve(af, this);
			}

			@Override
			public boolean chkAI_Drawback() {
				return millTargetAI(af, this);
			}
			
		};
		return dbMill;
	}
	
	public static String millStackDescription(SpellAbility sa, AbilityFactory af){
		// when getStackDesc is called, just build exactly what is happening
		Player player = af.getAbTgt() == null ? sa.getActivatingPlayer() : sa.getTargetPlayer(); 
		StringBuilder sb = new StringBuilder();
		
		if (!(sa instanceof Ability_Sub))
			sb.append(sa.getSourceCard().getName()).append(" - ");
		else
			sb.append(" ");
		
		sb.append("Mills ");
		int numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), af.getMapParams().get("NumCards"), sa);
		sb.append(numCards);
		sb.append(" Card(s) from ");
		sb.append(player.toString());
		sb.append("'s library.");
		
		Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null){
        	abSub.setParent(sa);
        	sb.append(abSub.getStackDescription());
        }
		
		return sb.toString();
	}
	
	public static boolean millCanPlayAI(final AbilityFactory af, SpellAbility sa){
		// AI cannot use this properly until he can use SAs during Humans turn
		if (!ComputerUtil.canPayCost(sa))
			return false;
		

		Card source = sa.getSourceCard();
		Ability_Cost abCost = af.getAbCost();
		
		if (abCost != null){
			// AI currently disabled for these costs
			if (abCost.getSacCost()){
				return false;
			}
			if (abCost.getLifeCost()){
				if (AllZone.ComputerPlayer.getLife() - abCost.getLifeAmount() < 4)
					return false;
			}
			if (abCost.getDiscardCost()) 	return false;
			
			if (abCost.getSubCounter()) {
				if (abCost.getCounterType().equals(Counters.P1P1)) return false; // Other counters should be used 
			}
			
		}
		
		boolean bFlag = millTargetAI(af, sa);
		if (!bFlag)
			return false;
		
		Random r = new Random();
		boolean randomReturn = r.nextFloat() <= Math.pow(.6667, source.getAbilityUsed());
		
		// some other variables here, like deck size, and phase and other fun stuff

		return randomReturn;
	}
	
	public static boolean millTargetAI(AbilityFactory af, SpellAbility sa){
		Target tgt = af.getAbTgt();
		HashMap<String,String> params = af.getMapParams();
		
		if (tgt != null){
			tgt.resetTargets();
			
			int numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
			
			CardList pLibrary = AllZoneUtil.getCardsInZone(Constant.Zone.Library, AllZone.HumanPlayer);
			
			if (pLibrary.size() == 0)	// deck already empty, no need to mill
				return false;
			
			if (numCards >= pLibrary.size()){
				// Can Mill out Human's deck? Do it!
				tgt.addTarget(AllZone.HumanPlayer);
				return true;
			}
			
			// Obscure case when you know what your top card is so you might? want to mill yourself here
			// if (AI wants to mill self)
			// tgt.addTarget(AllZone.ComputerPlayer);
			// else
			tgt.addTarget(AllZone.HumanPlayer);
		}
		return true;
	}
	
	public static void millResolve(final AbilityFactory af, final SpellAbility sa){
		HashMap<String,String> params = af.getMapParams();
		
		Card source = sa.getSourceCard();

		int numCards = AbilityFactory.calculateAmount(sa.getSourceCard(), params.get("NumCards"), sa);
		
		ArrayList<Player> tgtPlayers;

		Target tgt = af.getAbTgt();
		if (tgt != null)
			tgtPlayers = tgt.getTargetPlayers();
		else{
			tgtPlayers = new ArrayList<Player>();
			tgtPlayers.add(sa.getActivatingPlayer());
		}
		
		for(Player p : tgtPlayers)
			if (tgt == null || p.canTarget(af.getHostCard()))
				p.mill(numCards);	

		if (af.hasSubAbility()){
			Ability_Sub abSub = sa.getSubAbility();
			if (abSub != null){
	     	   if (abSub.getParent() == null)
	     		  abSub.setParent(sa);
	     	   abSub.resolve();
	        }
			else{
				String DrawBack = params.get("SubAbility");
				if (af.hasSubAbility())
					 CardFactoryUtil.doDrawBack(DrawBack, 0, source.getController(), source.getController().getOpponent(), tgtPlayers.get(0), source, null, sa);
			}
		}
	}	
}
