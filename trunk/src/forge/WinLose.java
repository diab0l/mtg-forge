package forge;
public class WinLose
{
  //the way wins were achieved:
  //Damage
  //Poison Counters	
  //Battle of Wits
  //Milled
  //Felidar Sovereign
  //...
  //
  private String[] winMethods = new String[2];
  
  private int win;
  private int lose;
  private boolean winRecently;

  public void reset()  {win = 0; lose = 0; winMethods = new String[2];}
  public void addWin() {win++;  winRecently = true;}
  public void addLose(){lose++; winRecently = false;}
  
  public int getWin()       {return win;}
  public int getLose()      {return lose;}
  public int countWinLose() {return win + lose;}
  
  public void setWinMethod(int gameNumber, String method)
  {
	  winMethods[gameNumber] = method;
  }
  
  public String[] getWinMethods()
  {
	  return winMethods;
  }
  

  public boolean didWinRecently() {return winRecently;}
}