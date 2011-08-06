package forge;

import java.util.ArrayList;

abstract public class Ability_Mana extends Ability_Activated implements java.io.Serializable {
	private static final long serialVersionUID = -6816356991224950520L;

    private String		origProduced;
    private int			amount = 1;
    protected boolean	reflected = false;
    protected boolean 	undoable = true;
    protected boolean	canceled = false;
    
    public Ability_Mana(Card sourceCard, String parse, String produced) {
    	this(sourceCard, parse, produced, 1);
    }
    
    public Ability_Mana(Card sourceCard, String parse, String produced, int num) {
    	this(sourceCard, new Ability_Cost(parse, sourceCard.getName(), true), produced, num);
    }
    
    public Ability_Mana(Card sourceCard, Ability_Cost cost, String produced) {
        this(sourceCard, cost, produced, 1);
    }
    
    public Ability_Mana(Card sourceCard, Ability_Cost cost, String produced, int num) {
        super(sourceCard, cost, null);

        origProduced = produced;
        amount = num;
    }

    @Override
    public boolean canPlayAI() {
        return false;
    }    

	@Override
	public void resolve() {
		produceMana();
	}
	
	public void produceMana(){
		produceMana(origProduced);
	}
	
	public void produceMana(String produced){
		final Card source = this.getSourceCard();
		// change this, once ManaPool moves to the Player
		// this.getActivatingPlayer().ManaPool.addManaToFloating(origProduced, getSourceCard());
		AllZone.ManaPool.addManaToFloating(produced, source);

		// TODO: all of the following would be better as trigger events "tapped for mana"
		
		if (source.getType().contains("Swamp")){
			// If Nirkana Revenant triggers, make mana undoable

			CardList nirkanas = AllZoneUtil.getPlayerCardsInPlay(getActivatingPlayer(), "Nirkana Revenant");
			int size = nirkanas.size();
			for(int i = 0; i < size; i++){
				this.undoable = false;
				AllZone.ManaPool.addManaToFloating("B", nirkanas.get(i));
			}
		}
		if (source.getType().contains("Island")){
			// If High Tide triggers, make mana undoable
			
			int size = Phase.HighTides.size();
			for(int i = 0; i < size; i++){
				this.undoable = false;
				AllZone.ManaPool.addManaToFloating("U", Phase.HighTides.get(i));
			}
		}
		
		if (source.isLand()){
			CardList manaBarbs = AllZoneUtil.getCardsInPlay("Manabarbs");

			for(final Card c : manaBarbs){
				this.undoable = false;
        		SpellAbility ability = new Ability(c, "") {
        			@Override
        			public void resolve() {
        				source.getController().addDamage(1, c);
        			}
        		};
        		
        		StringBuilder sb = new StringBuilder();
        		sb.append(c.getName()).append(" - deal 1 damage to ").append(source.getController());
        		ability.setStackDescription(sb.toString());
        		
        		AllZone.Stack.add(ability);
			}
		}
		
        if(source.getName().equals("Rainbow Vale")) {
        	this.undoable = false;
        	source.addExtrinsicKeyword("An opponent gains control of CARDNAME at the beginning of the next end step.");
        }
        
        if (source.getName().equals("Undiscovered Paradise")) {
        	this.undoable = false;
        	// Probably best to conver this to an Extrinsic Ability
        	source.setBounceAtUntap(true);
        }
        
        if (source.getName().equals("Forbidden Orchard")) {
        	this.undoable = false;
        	AllZone.Stack.add(CardFactoryUtil.getForbiddenOrchardAbility(source, getActivatingPlayer().getOpponent()));
        }
        
        if(source.getType().contains("Mountain") && AllZoneUtil.isCardInPlay("Gauntlet of Might")) {
        	CardList list = AllZoneUtil.getCardsInPlay("Gauntlet of Might");
        	for(int i = 0; i < list.size(); i++) {
        		AllZone.ManaPool.addManaToFloating("R", list.get(i));
        	}
        }
        
        ArrayList<Card> auras = source.getEnchantedBy();
        for(Card c : auras){
        	if (c.getName().equals("Wild Growth")){
        		this.undoable = false;
				AllZone.ManaPool.addManaToFloating("G", c);
			}
        	if (c.getName().equals("Overgrowth")){
				this.undoable = false;
				AllZone.ManaPool.addManaToFloating("G", c);
				AllZone.ManaPool.addManaToFloating("G", c);
			}
        }
	}
	
	public String mana() { return origProduced; }
	public void setMana(String s) { origProduced = s; }
	public void setReflectedMana(boolean bReflect) { reflected = bReflect; }
	
	public boolean isSnow() { return this.getSourceCard().isSnow(); }
	public boolean isSacrifice() { return this.getPayCosts().getSacCost(); }
	public boolean isReflectedMana() { return reflected; }
	
	public boolean canProduce(String s) { return origProduced.contains(s); }
	
	public boolean isBasic(){
		if (origProduced.length() != 1)
			return false;
		
		if (amount > 1)
			return false;
		
		return true;
	}

	public boolean isUndoable() { return getPayCosts().isUndoable() && AllZoneUtil.isCardInPlay(getSourceCard()); }
	
	public void setCanceled(boolean bCancel) { canceled = bCancel; }
	public boolean getCanceled() { return canceled; }
	
	public void undo(){
		if (isUndoable()){
			getPayCosts().refundPaidCost(getSourceCard());
		}
	}
	
    @Override
    public boolean equals(Object o)
    {
    	//Mana abilities with same Descriptions are "equal"
    	return  o.toString().equals(this.toString());
    }
}

