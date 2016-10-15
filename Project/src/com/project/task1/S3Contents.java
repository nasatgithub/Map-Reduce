package com.project.task1;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3Contents {
	
	public static void getContents(String filename,ArrayList<ReviewerHelperClass> r,AmazonS3 s3Client) throws IOException{ 
		
		S3Object s3object = s3Client.getObject(new GetObjectRequest("mr-project", filename));
		BufferedReader reader = new BufferedReader(new 
        		InputStreamReader(s3object.getObjectContent()));
        String line = "";
        while((line = reader.readLine())!=null){
        	String[] row = line.split("\t");
			ReviewerHelperClass reviewer = new ReviewerHelperClass(row[2], row[1], row[0],row[3],row[4]);
			r.add(reviewer);
        }
    }
}
		
