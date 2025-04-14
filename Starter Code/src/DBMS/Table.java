package DBMS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Table implements Serializable
{
	private String tableName;
	private String[] columnsName;
	private ArrayList<String> pagesNames;
	private ArrayList<String> trace;
	
	public Table(String table,String[] columnsName)
	{
		this.tableName=table;
		this.columnsName=columnsName;
		this.pagesNames = new ArrayList<>();
		this.trace = new ArrayList<>() ;
	}
	public ArrayList<String> getTrace()
	{		
		return trace;
	}
	public String getTableName()
	{
		return tableName;
	}
	public String[] getColumnsName()
	{
		return columnsName;
	}
	public ArrayList<String> getPages()
	{
		return pagesNames ;
	}
	public ArrayList<String> addTrace(String message)
	{
		FileManager.trace();
		trace.add(message);
		return trace;
	}
	
	public int getRecordsNumber() {
		int result = 0;
		
		for(int i=0; i<getPages().size(); i++) {
			Page page = FileManager.loadTablePage(tableName, i);
			result += page.getRecords().size();
		
		}
		
		return result; 
		
	}
}