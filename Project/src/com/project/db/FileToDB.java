package com.project.db;

import java.sql.Connection;
import java.sql.Statement;



public class FileToDB {


	public static boolean loadFileToDB(String filepath)  {
		try{
		System.out.println("In FileToDB");
		DBConnection db=new DBConnection();
		Connection con=db.getDBConnection();
		String query = "LOAD DATA LOCAL INFILE '"+filepath+"' INTO TABLE finalMR  FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n'";
		Statement s=con.createStatement();
		s.executeQuery(query);
		
		System.out.println("DATA LOADED INTO DB SUCCESSFULLY !!! ");
		
		
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}

				return true;
	}
}
