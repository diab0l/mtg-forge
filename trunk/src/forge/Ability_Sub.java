package forge;

abstract public class Ability_Sub extends SpellAbility implements java.io.Serializable {
	private static final long serialVersionUID = 4650634415821733134L;

	private SpellAbility parent = null;
	
	public Ability_Sub(Card sourceCard, Target tgt) {
		super(SpellAbility.Ability, sourceCard);
		setTarget(tgt);
	}

	@Override
	public boolean canPlay() {
		// this should never be on the Stack by itself
		return false;
	}

	abstract public boolean chkAI_Drawback();
	
	abstract public boolean doTrigger(boolean mandatory);
	
	public void setParent(SpellAbility parent) {
		this.parent = parent;
	}

	public SpellAbility getParent() {
		return parent;
	}
}
