package DBMS;

import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable
{
	private ArrayList<String[]> records;
	static final int maxRecords =DBApp.dataPageSize ;
	
	public Page ()
	{
		this.records = new ArrayList<>();
	}
	
	public boolean isFull()
	{
		if(records.size()==maxRecords)
			return true ;
		return false ;		
	}
	public void add(String[] record)
	{
		records.add(record);
		
	}
	public ArrayList<String[]> getRecords()
	{
		return this.records;
	}
}