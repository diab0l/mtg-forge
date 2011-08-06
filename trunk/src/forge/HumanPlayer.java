
package forge;


public class HumanPlayer extends Player{
	
	public HumanPlayer(String myName) {
		this(myName, 20, 0);
	}
	
	public HumanPlayer(String myName, int myLife, int myPoisonCounters) {
		super(myName, myLife, myPoisonCounters);
	}
	
	public Player getOpponent() {
		return AllZone.ComputerPlayer;
	}
	
	////////////////
	///
	/// Methods to ease transition to Abstract Player class
	///
	///////////////
	
	public boolean isHuman() { return true; }
	public boolean isComputer() { return false; }
	public boolean isPlayer(Player p1) {
		return p1.getName().equals(this.name);
	}
	
	protected Card getPlayerCard() {
		return AllZone.CardFactory.HumanNullCard;
	}
	
	///////////////
	///
	/// End transition methods
	///
	///////////////
	
	public void mayDrawCards(int n) {
		String[] choices = {"Yes", "No"};
		Object choice = AllZone.Display.getChoice("Draw "+n+" cards?", choices);
		if(choice.equals("Yes")) 
			drawCards(n);
	}
	
	public void mayDrawCard() {
		mayDrawCards(1);
	}
	
	public boolean dredge() {
		boolean dredged = false;
		String choices[] = {"Yes", "No"};
		Object o = AllZone.Display.getChoice("Do you want to dredge?", choices);
		if(o.equals("Yes")) {
			Card c = (Card) AllZone.Display.getChoice("Select card to dredge", getDredge().toArray());
			//rule 702.49a
			if(getDredgeNumber(c) <= AllZone.Human_Library.size()) {

				//might have to make this more sophisticated
				//dredge library, put card in hand
				AllZone.GameAction.moveToHand(c);

				for(int i = 0; i < getDredgeNumber(c); i++) {
					Card c2 = AllZone.Human_Library.get(0);
					AllZone.GameAction.moveToGraveyard(c2);
				}
				dredged = true;
			}
			else {
				dredged = false;
			}
		}
		return dredged;
	}
	
	public CardList discard(final int num, final SpellAbility sa, boolean duringResolution) {
		AllZone.InputControl.setInput(CardFactoryUtil.input_discard(num, sa), duringResolution);
		
		// why is CardList returned?
		return new CardList();	
	}
	
	public void discardUnless(int num, String uType, SpellAbility sa) {
		AllZone.InputControl.setInput(CardFactoryUtil.input_discardNumUnless(num, uType, sa));
	}
	
	public void handToLibrary(final int numToLibrary, String libPos) {
		if(libPos.equals("Top") || libPos.equals("Bottom")) libPos = libPos.toLowerCase();
        else {
            String s = "card";
            if(numToLibrary > 1) s += "s";
            
            Object o = AllZone.Display.getChoice("Do you want to put the " + s
                    + " on the top or bottom of your library?", new Object[] {"top", "bottom"});
            libPos = o.toString();
        }
        AllZone.InputControl.setInput(CardFactoryUtil.input_putFromHandToLibrary(libPos, numToLibrary));
	}
	
	protected void doScry(final CardList topN, final int N) {
		int num = N;
		for(int i = 0; i < num; i++) {
            Object o;
            o = AllZone.Display.getChoiceOptional("Put on bottom of library.",topN.toArray());
            if(o != null) {
                Card c = (Card) o;
                topN.remove(c);
                AllZone.Human_Library.add(c);
            } else // no card chosen for the bottom
            break;
        }
        num = topN.size();
        if(num > 0) for(int i = 0; i < num; i++) {
            Object o;
            o = AllZone.Display.getChoice("Put on top of library.", topN.toArray());
            if(o != null) {
                Card c = (Card) o;
                topN.remove(c);
                AllZone.Human_Library.add(c, 0);
            }
            // no else - a card must have been chosen
        }
	}
	
	public void sacrificePermanent(String prompt, CardList choices) {
		Input in = CardFactoryUtil.input_sacrificePermanent(choices, prompt);
        AllZone.InputControl.setInput(in);
	}
	
}//end HumanPlayer class