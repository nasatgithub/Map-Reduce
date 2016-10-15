package com.project.task2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class FormResultFile {

	public static void generateResult(HttpServletRequest request) throws IOException {
		// TODO Auto-generated method stub
		
		String prefix = "output-spark";
		

		AWSCredentials credentials = new BasicAWSCredentials("AKIAIS6HZ576JP2DQVRA", "KxOkDMA1V+CFfnqCnVisQuHekyEQ2EM7SvqWlQiM");
		AmazonS3 s3client = new AmazonS3Client(credentials);

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName("mr-project")
				.withPrefix(prefix);
		//			.withDelimiter("/");
		ObjectListing objectListing;

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			JSONObject jb = null;
			FileWriter fw = new FileWriter(request.getServletContext().getInitParameter("nasirRoot")+"/result.txt");
			BufferedWriter bw = new BufferedWriter(fw);
			int cnt = 1;
			for (S3ObjectSummary objectSummary : 
				objectListing.getObjectSummaries()) {
				if(objectSummary.getKey().contains("part-")){
//					System.out.println(objectSummary.getKey());
					S3Object s3object = s3client.getObject(new GetObjectRequest("mr-project", objectSummary.getKey()));
					BufferedReader reader = new BufferedReader(new 
			        		InputStreamReader(s3object.getObjectContent()));
			        String line = "";
			        try {
						while((line = reader.readLine())!=null){
								jb = new JSONObject(line);
								bw.write(cnt+"\t"+jb.get("Title")+"\t"+jb.get("Genre")+"\t"
								+jb.get("Amazon_Score")+"\t"+jb.get("Awards")+"\t"+
							     jb.get("Poster")+"\t"+jb.get("imdbRating")+"\t"+jb.get("Language")
							     +"\t"+jb.get("Year")+"\n");
								cnt++;
							 }
							 
							 
						}
					 catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
			bw.close();
		} while (objectListing.isTruncated());
		
		System.out.println("Read files from S3!");
	}

}
