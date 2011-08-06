public class Mana_PartColor extends Mana_Part
{
  private String manaCost;

  //String manaCostToPay is either "G" or "GW" NOT "3 G"
  //ManaPartColor only needs 1 mana in order to be paid
  //GW means it will accept either G or W like Selesnya Guildmage
  public Mana_PartColor(String manaCostToPay)
  {
    char[] c = manaCostToPay.toCharArray();
    for(int i = 0; i < c.length; i++) {
    	if (i == 0 && c[i] == ' ')
    		;
    	else
    		checkSingleMana("" +c[i]);
    }

    manaCost = manaCostToPay;
  }
  public String toString()
  {
    return manaCost;
  }
  public boolean isNeeded(String mana)
  {
    //ManaPart method
    checkSingleMana(mana);

    int index = manaCost.indexOf(mana);
    return index != -1;
  }
  public void reduce(String mana)
  {
    //if mana is needed, then this mana cost is all paid up
    if(! isNeeded(mana))
      throw new RuntimeException("Mana_PartColor : reduce() error, argument mana not needed, mana - " +mana +", toString() - " +toString());

    manaCost = "";
  }
  public boolean isPaid()
  {
    return manaCost.length() == 0;
  }
}
