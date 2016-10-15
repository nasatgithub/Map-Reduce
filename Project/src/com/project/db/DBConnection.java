package com.project.db;
import java.sql.*;
public class DBConnection {
Connection con;
Statement st;
ResultSet r;
String url,schemaname,driver;
public DBConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException	
{
	 url="jdbc:mysql://localhost:3306/"; 	
	 schemaname="mr_project";
	 driver = "com.mysql.jdbc.Driver";
	 Class.forName(driver).newInstance();
	 con= DriverManager.getConnection(url+schemaname, "root", "adminpassword");
	 System.out.println("created Connection at DB Successully");
}
public Statement getDBStatement() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{

	    Statement st=con.createStatement();
		return st;
	
}
public Connection getDBConnection()
{
	return con;
}
public void closeDBConnection() throws SQLException
{
	con.close();
}

}
