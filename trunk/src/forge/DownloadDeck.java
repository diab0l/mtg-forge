package forge;

 public class DownloadDeck {
	
	 private Card DownloadCard;
	 
	public  String FoundNumberCard(String rStr)
	{
		int temp;
		int i;
		
		for(i=0; i<rStr.length();i++)
		{
			temp=rStr.codePointAt(i) ;
			if(temp>=48 && temp<=57)
			{
				break;
				
			}
					
		}
		if(rStr.codePointAt(i+1)>=48 &&rStr.codePointAt(i+1)<=57)
		{
			return rStr.substring(i,i+2);	
		}else
		{
			return rStr.substring(i,i+1);
		}		
	}
	 
	public  String FoundNameCard(String rStr)
	{
		int temp;
		int i;
		
		for(i=0; i<rStr.length();i++)
		{
			temp=rStr.codePointAt(i) ;
			if(temp>=48 && temp<=57)
			{
				break;
				
			}
					
		}
			return rStr.substring(0,i-1);		
	}
	
	
	
	public String RemoveSpace(String rStr)
	{
		int temp;
		int i;
		
		for(i=0; i<rStr.length();i++)
		{
			temp=rStr.codePointAt(i) ;
			if(temp!=32)
			{
				break;
				
			}
					
		}
		return rStr.substring(i);
	}
	public String RemoveSpaceBack(String rStr)
	{
		int temp;
		int i;
		
		for(i=rStr.length()-1; i>-1;i=i-1)
		{
			temp=rStr.codePointAt(i) ;
			if(temp!=32)
			{
				break;
				
			}
					
		}
		return rStr.substring(0,i+1);
	}
	
	public String RemoveFoundNumberCard(String rStr, String Number)
	{
		int a;
		int temp;
		a=rStr.indexOf(Number);
		temp=rStr.codePointAt(a+1) ;
		if(temp>=48 && temp<=57)
		{
		return rStr.substring(a+2);
		}else
		{
		return rStr.substring(a+1);
		}
	
	}
	
	public String RemoveFoundNameCard(String rStr, String Name)
	{
		int a;
		a=Name.length();
		return rStr.substring(a);
	
	}
	
	public boolean IsCardSupport(String CardName)
	{
		CardList all = AllZone.CardFactory.getAllCards(); 
		
		Card gCard = new Card();
		for(int i=0;i<all.size();i++)
		{
			gCard = all.getCard(i);
			if(CardName.equalsIgnoreCase(gCard.getName()))
					{
					return true;
					}
		}
		return false;
	}
	
	public Card GetCardDownload(Card c, String CardName)
	{
		CardList all = AllZone.CardFactory.getAllCards(); 
		
		
		for(int i=0;i<all.size();i++)
		{
			DownloadCard = all.getCard(i);
			
			if(CardName.equalsIgnoreCase(DownloadCard.getName()))
					{
					return DownloadCard;
					}
		}
		return DownloadCard;
		
	}
	
	}

	

