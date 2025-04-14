package DBMS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import DBMS.FileManager.*;
import org.junit.Test;

public class DBApp
{
	static int dataPageSize = 2;
	
	public static void createTable(String tableName, String[] columnsNames)
	{
		
	    if (new File(FileManager.directory.getAbsolutePath()).listFiles().length == 0) {  // Auto reset 
	        FileManager.reset();
	        dataPageSize = 2;
	    }
	    
		 Table table = new Table(tableName, columnsNames);
		 
		 if (tableName == null || tableName.trim().isEmpty()) {
		     throw new IllegalArgumentException("Table name cannot be null or empty.");
		 }

		 if (columnsNames == null || columnsNames.length == 0) {
		     throw new IllegalArgumentException("Columns names cannot be null or empty.");
		 }
		 
		 FileManager.storeTable(tableName, table);
		 table.addTrace("Table created name: " + table.getTableName() + ", columnsNames:" + Arrays.toString(table.getColumnsName()) );
		 FileManager.storeTable(tableName, table);	
	}
	
	public static void insert(String tableName, String[] record) {
	    Table table = FileManager.loadTable(tableName);
	    
	    if (table == null) {
	        throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
	    }

	    if (record == null || record.length != table.getColumnsName().length) {
	        throw new IllegalArgumentException("Record data does not match table column structure.");
	    }

	    if (table.getPages() == null) {
	        throw new IllegalArgumentException("The table '" + tableName + "' has an invalid pages structure.");
	    }
	    
	    ArrayList<String> allPages = table.getPages();
	    int pageInserted;
	    long start = System.currentTimeMillis();

	    if (allPages.isEmpty()) 
	    {
	
	        Page page = new Page();
	        page.add(record);
	        FileManager.storeTablePage(tableName, 0, page);  // Store in page 0
	        table.getPages().add("0");  // Track page 0
	        pageInserted = 0;
	        
	    } 
	    else 
	    {

	        Page lastPage = FileManager.loadTablePage(tableName, allPages.size() - 1);
	        
	        if (lastPage.isFull()) 
	        {

	            Page newPage = new Page();
	            newPage.add(record);
	            FileManager.storeTablePage(tableName, allPages.size(), newPage);  
	            table.getPages().add(String.valueOf(allPages.size())); 
	            pageInserted = allPages.size()-1;
	        }
	        else 
	        {	
	            lastPage.add(record);
	            FileManager.storeTablePage(tableName, allPages.size() - 1, lastPage); 
	            pageInserted = allPages.size() - 1 ; 
	        }
	    }

	    long executionTime = System.currentTimeMillis() - start;
	    table.addTrace("Inserted:" + Arrays.toString(record) + ", at page number:" + pageInserted + ", execution time (mil):" + executionTime);
	    FileManager.storeTable(tableName, table);  
	}


	public static ArrayList<String[]> select(String tableName) 
	{
		
		 Table table = FileManager.loadTable(tableName);
		
		// Validate if table exists
		if (table == null) {
		    throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
		}

		// Validate pages
		if (table.getPages() == null || table.getPages().isEmpty()) {
		    throw new IllegalArgumentException("The table '" + tableName + "' has no pages.");
		}

	    ArrayList<String> allPagesNames = table.getPages();
	    ArrayList<String[]> allRecords = new ArrayList<>();
	    long start = System.currentTimeMillis();


	    for (int i = 0; i < allPagesNames.size(); i++)
	    {
	        Page page = FileManager.loadTablePage(tableName, i);
	        ArrayList<String[]> records = page.getRecords(); 
	        allRecords.addAll(records);
	    }
	    
	    long end = System.currentTimeMillis();
	    table.addTrace("Select all pages:" + allPagesNames.size() + ", records:" + allRecords.size() + ", execution time (mil):" + (end - start));
	    FileManager.storeTable(tableName, table);

	    return allRecords;
	}
	
	public static ArrayList<String []> select(String tableName, int pageNumber, int recordNumber)
	{
		
		long start = System.currentTimeMillis();
		 
		Table table = FileManager.loadTable(tableName);
		Page page = FileManager.loadTablePage(tableName, pageNumber);
		
		if (page.getRecords().isEmpty()) {
		    throw new IllegalArgumentException("Page " + pageNumber + " is empty.");
		}

		// Validate record number
		if (recordNumber < 0 || recordNumber >= page.getRecords().size()) {
		    throw new IllegalArgumentException("Invalid record number: " + recordNumber);
		}

		 
		  if (table == null) {
		        throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
		    }

		    // Validate page number
		    if (pageNumber < 0 || pageNumber >= table.getPages().size()) {
		        throw new IllegalArgumentException("Invalid page number: " + pageNumber);
		    }

		    // Load the specified page
		 
		    // Validate record number
		    if (recordNumber < 0 || recordNumber >= page.getRecords().size()) {
		        throw new IllegalArgumentException("Invalid record number: " + recordNumber);
		    }

		 ArrayList<String[]> records = page.getRecords();
		 ArrayList<String[]> result = new ArrayList<>();
		 
		    if (recordNumber >= 0 && recordNumber < records.size()) {
		        result.add(records.get(recordNumber));
		    }
		    
		    long end = System.currentTimeMillis();
		    table.addTrace("Select pointer page:" + pageNumber + ", record:" + recordNumber + ", total output count:" + result.size() + ", execution time (mil):" + (end - start));
			FileManager.storeTable(tableName, table);
		    
		    return result ;	 
	}
	
	public static ArrayList<String[]> select(String tableName, String[] cols, String[] vals) {
		Table table = FileManager.loadTable(tableName);
		
		// Validate if table exists
		if (table == null) {
		    throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
		}
 
		// Validate input lengths
		if (cols == null || vals == null || cols.length != vals.length) {
		    throw new IllegalArgumentException("Column names and values count mismatch.");
		}

		// Validate column existence
		String[] tableColumns = table.getColumnsName();
		for (String col : cols) {
		    if (!Arrays.asList(tableColumns).contains(col)) {
		        throw new IllegalArgumentException("Column '" + col + "' does not exist in table '" + tableName + "'.");
		    }
		}

		// Validate pages
		if (table.getPages() == null || table.getPages().isEmpty()) {
		    throw new IllegalArgumentException("The table '" + tableName + "' has no pages.");
		}
		
		
	    ArrayList<String[]> result = new ArrayList<>();
	    long start = System.currentTimeMillis();

	    ArrayList<String> pageNames = table.getPages();
	    ArrayList<ArrayList<Integer>> countPerPage = new ArrayList<>();

	    for (int i = 0; i < pageNames.size(); i++) {
	        int count = 0;
	        Page page = FileManager.loadTablePage(tableName, i);
	        for (String[] record : page.getRecords()) {
	            boolean match = true;
	            for (int j = 0; j < cols.length; j++) {
	                int colIdx = Arrays.asList(table.getColumnsName()).indexOf(cols[j]);
	                if (!record[colIdx].equals(vals[j])) {
	                    match = false;
	                    break;
	                }
	            }
	            if (match) {
	                result.add(record);
	                count++;
	            }
	        }
	        if (count > 0) countPerPage.add(new ArrayList<>(Arrays.asList(i, count)));
	    }

	    long end = System.currentTimeMillis();
	    table.addTrace("Select condition:" + Arrays.toString(cols) + "->" + Arrays.toString(vals) + ", Records per page:" + countPerPage + ", records:" + result.size() + ", execution time (mil):" + (end - start));
	    FileManager.storeTable(tableName, table);
	    return result;

	}

	
	public static String getFullTrace(String tableName)
	{
		 Table table = FileManager.loadTable(tableName);
		 
		// Validate if table exists
		 if (table == null) {
		     throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
		 }

		 // Validate trace
		 if (table.getTrace() == null || table.getTrace().isEmpty()) {
		     throw new IllegalArgumentException("The table '" + tableName + "' has no trace.");
		 }
		 
		    String fullTrace = String.join("\n", table.getTrace());
		    fullTrace += "\n" + "Pages Count: " + table.getPages().size() + ", Records Count: " + table.getRecordsNumber();
		    return fullTrace;
	}
	
	public static String getLastTrace(String tableName)
	{
		Table table = FileManager.loadTable(tableName);
		
		// Validate if table exists
		if (table == null) {
		    throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
		}

		// Validate trace
		if (table.getTrace() == null || table.getTrace().isEmpty()) {
		    throw new IllegalArgumentException("The table '" + tableName + "' has no trace.");
		}
		
		ArrayList<String> fullTrace=table.getTrace();
		
		if(fullTrace==null ||fullTrace.isEmpty())
			return "there's no trace.";
		
		String lastTrace = fullTrace.get(fullTrace.size()-1);
		return lastTrace;
	}
	
	public static void reset ()
	{
		FileManager.reset();
		dataPageSize =2 ;
	}
	
	public static void main(String []args) throws IOException
	{	   
		FileManager.reset();
		
		String[] cols = {"id","name","major","semester","gpa"}; 
		createTable("student", cols); 
	    String[] r1 = {"1", "stud1", "CS", "5", "0.9"}; 
	    insert("student", r1); 
		   
	    String[] r2 = {"2", "stud2", "BI", "7", "1.2"}; 
	    insert("student", r2); 
		   
	    String[] r3 = {"3", "stud3", "CS", "2", "2.4"}; 
        insert("student", r3); 
		   
		 String[] r4 = {"4", "stud4", "DMET", "9", "1.2"}; 
		 insert("student", r4); 
		   
	     String[] r5 = {"5", "stud5", "BI", "4", "3.5"}; 
		 insert("student", r5); 
		   
		   
         System.out.println("Output of selecting the whole table content:"); 
		 ArrayList<String[]> result1 = select("student"); 
         for (String[] array : result1) { 
         for (String str : array) { 
		                System.out.print(str + " "); 
		            } 
		            System.out.println(); 
		        } 
		         
		        System.out.println("--------------------------------"); 
		        System.out.println("Output of selecting the output by position:"); 
		  ArrayList<String[]> result2 = select("student", 1, 1); 
		        for (String[] array : result2) { 
		            for (String str : array) { 
		                System.out.print(str + " "); 
		            } 
		            System.out.println();  
		        } 
		         
		        System.out.println("--------------------------------"); 
		        System.out.println("Output of selecting the output by column condition:"); 
		  ArrayList<String[]> result3 = select("student", new String[]{"gpa"}, new 
		String[]{"1.2"}); 
		        for (String[] array : result3) { 
		 
		 
		            for (String str : array) { 
		                System.out.print(str + " "); 
		            } 
		            System.out.println();  
		        } 
		        System.out.println("--------------------------------"); 
		  System.out.println("Full Trace of the table:"); 
		  System.out.println(getFullTrace("student")); 
		  System.out.println("--------------------------------"); 
		  System.out.println("Last Trace of the table:"); 
		  System.out.println(getLastTrace("student")); 
		  System.out.println("--------------------------------"); 
		  System.out.println("The trace of the Tables Folder:"); 
		  System.out.println(FileManager.trace()); 
		  FileManager.reset(); 
		  System.out.println("--------------------------------"); 
		  System.out.println("The trace of the Tables Folder after resetting:"); 
		  System.out.println(FileManager.trace()); 
		   		
	}
	
	
	
}