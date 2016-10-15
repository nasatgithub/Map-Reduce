package com.project.mr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class AwardScore {

	@SuppressWarnings("resource")
	public static void awardsScore(HttpServletRequest request) throws IOException {
		FileReader fr = new FileReader(request.getServletContext().getInitParameter("nasirRoot")+"/result.txt");
		BufferedReader br = new BufferedReader(fr);

		FileWriter fw = new FileWriter(request.getServletContext().getInitParameter("nasirRoot")+"/score.txt");
		BufferedWriter bw = new BufferedWriter(fw);
         System.out.println("############ CAME ");
		String line ;
		while ((line = br.readLine()) != null) {

			String award = line.split("\t")[4];

			if(award.contentEquals("N/A")) {
				bw.write(0+"\n");
			}
			else
			{
				String award_string = award.toLowerCase();
				Pattern p = Pattern.compile("-?\\d+");
				Matcher m = p.matcher(award_string);

				if (award_string.contains("won") 
						&& award_string.contains("win")
						&& award_string.contains("nomination")) 
				{
					while (m.find()) {
						bw.write(m.group()+"\t");
					}
					bw.write("\n");
				}
				else if (award_string.contains("nominated") 
						&& award_string.contains("win")
						&& award_string.contains("nomination")) 
				{
					List<Integer> data = new ArrayList<>();
					while (m.find()) {
						int n = Integer.parseInt(m.group());
						data.add(n);
					}
					bw.write("0\t"+data.get(1).toString());
					Integer s = data.get(0)+data.get(2);
					bw.write("\t"+s.toString());
					bw.write("\n");
				}
				else if (award_string.contains("nominated") 
						&& award_string.contains("nomination")) 
				{
					List<Integer> data = new ArrayList<>();
					while (m.find()) {
						int n = Integer.parseInt(m.group());
						data.add(n);
					}
					bw.write("0\t0\t");
					Integer s = data.get(0)+data.get(1);
					bw.write(s.toString());
					bw.write("\n");
				}
				else if (award_string.contains("nominated") 
						&& award_string.contains("win")) 
				{
					List<Integer> data = new ArrayList<>();
					while (m.find()) {
						int n = Integer.parseInt(m.group());
						data.add(n);
					}
					bw.write("0\t"+data.get(0).toString()+ "\t"+data.get(1).toString());
					bw.write("\n");
				}
				else if (award_string.contains("won")  
						&& (award_string.contains("win")))
				{
					while (m.find()) {
						bw.write(m.group()+"\t");
					}
					bw.write(0+"\t\n");
				}
				else if (award_string.contains("won") 
						&& (award_string.contains("nomination")))
				{
					int count = 0;
					while (m.find()) {
						bw.write(m.group()+"\t");
						while(count<1)
						{
							bw.write(0+"\t");
							count+=1;
						}
					}
					bw.write("\n");
				}	
				else if (award_string.contains("win") 
						&& award_string.contains("nomination"))
				{
					bw.write(0+"\t");
					while (m.find()) {
						bw.write(m.group()+"\t");
					}
					bw.write("\n");
				}
				else if (award_string.contains("win"))
				{
					bw.write(0+"\t");
					while (m.find()) {
						bw.write(m.group()+"\t");
					}
					bw.write(0+"\t\n");
				}
				else if (award_string.contains("nomination"))
				{
					bw.write(0+"\t"+0+"\t");
					while (m.find()) {
						bw.write(m.group()+"\t");
					}
					bw.write("\n");
				}
			}
		}
		bw.close();
		FileReader fr1 = new FileReader(request.getServletContext().getInitParameter("nasirRoot")+"/score.txt");
		BufferedReader br1 = new BufferedReader(fr1);
		FileReader fr2 = new FileReader(request.getServletContext().getInitParameter("nasirRoot")+"/result.txt");
		BufferedReader br2 = new BufferedReader(fr2);
		fw = new FileWriter(request.getServletContext().getInitParameter("nasirRoot")+"/rating.txt");
		bw = new BufferedWriter(fw);
		String scorelist;
		while ((scorelist = br1.readLine()) != null) {
			bw.write(br2.readLine() + "\t"+scorelist+"\n");
		}
		
		bw.close();
	}
}
