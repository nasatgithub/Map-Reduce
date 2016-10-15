package com.project.task1;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Top5List {

	@SuppressWarnings("unchecked")
	public static void getTopFive(List<ReviewerHelperClass> listTopR, int top) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<ReviewerHelperClass> reviewerList = new ArrayList<ReviewerHelperClass>();
		
		ReadS3files.getList(reviewerList);
		Collections.sort(reviewerList);
		
		int cnt = 0;
		
		for(ReviewerHelperClass reviewer : reviewerList){
			if(cnt >= top) break;
			cnt++;
			listTopR.add(reviewer);
		}

	}

}
