package com.project.task1;


import java.io.IOException;
import java.util.ArrayList;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class ReadS3files {

	public static void getList(ArrayList<ReviewerHelperClass> reviewerList) throws IOException{

		String prefix = "Output-task1";

		AWSCredentials credentials = new BasicAWSCredentials("AKIAIS6HZ576JP2DQVRA", "KxOkDMA1V+CFfnqCnVisQuHekyEQ2EM7SvqWlQiM");
		AmazonS3 s3client = new AmazonS3Client(credentials);

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName("mr-project")
				.withPrefix(prefix);
		//			.withDelimiter("/");
		ObjectListing objectListing;

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : 
				objectListing.getObjectSummaries()) {
				if(objectSummary.getKey().contains("part-r")){
					
					S3Contents.getContents(objectSummary.getKey(),reviewerList,s3client);
				}
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

	}
}
