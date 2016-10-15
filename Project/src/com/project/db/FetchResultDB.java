package com.project.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.project.mr.Options;

public class FetchResultDB {
	public static boolean fetch(String reqType,String[] genres, HttpServletRequest request){
		List<Options> listOpt;
		try{
			DBConnection db=new DBConnection();
			Connection con=db.getDBConnection();
			Statement stmt = con.createStatement();
			stmt.executeQuery("SET sql_mode = ''");
			stmt.close();
			PreparedStatement ps;
			StringBuffer query =new StringBuffer
					("select distinct(title),genre, round(amazon_avg_score,1), awards, "
							+ "imagelink, imdbrating, lang, yr from finalMR "  
							+ "where lang = 'English' and  genre not like '%Music%' "
							+ "and genre not like '%Short%' and genre not like '%Documentary%' "
							+ "and genre not like '%Biography%'");
			if(genres!=null){
				for(int i=0;i<genres.length;i++){
					query.append(" and genre like ?");
				}
			}
			if(!reqType.equals("award_score"))
				query.append(" group by title order by "+reqType+" desc limit 10;");
			else
				query.append(" group by title order by oscars desc,wins desc,nomin desc limit 10;");	
			ps=con.prepareStatement(new String(query));

			System.out.println("\nQUERY IS :\n"+query+"\n");
			int i;
			if(genres!=null){
				for(i=0;i<genres.length;i++){
					ps.setString(i+1, "%"+genres[i]+"%");
				}
			}
			ResultSet r=ps.executeQuery();
			Options opt;
			listOpt=new ArrayList<Options>();
			while(r.next()){
				//System.out.println(r.getString(1) + "\t"+r.getDouble(2)+"\t"+r.getString(7) + "\t"+r.getDouble(8));
				opt=new Options(r.getString(1), r.getString(2), 
						r.getDouble(3), r.getString(4),
						r.getString(5), r.getDouble(6), 
						r.getString(7), r.getString(8));
				listOpt.add(opt);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			listOpt=null;
			return false;
		}
		request.setAttribute("listOpt", listOpt);
		return true;
	}
}
