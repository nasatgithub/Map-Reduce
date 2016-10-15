package com.project.mr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.project.db.FetchResultDB;
import com.project.db.FileToDB;
import com.project.task1.LaunchCluster;
import com.project.task1.ReviewerHelperClass;
import com.project.task1.Top5List;
import com.project.task2.FormResultFile;

/**
 * Servlet implementation class RecieveRequest
 */
@WebServlet("/RecieveRequest")
public class ReceiveRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ReceiveRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		String taskNo=request.getParameter("taskNo");
		System.out.println("Received Task : : : " +  taskNo);
		if(taskNo.equals("1")){
			RequestDispatcher rd=request.getRequestDispatcher("result.jsp");
			int top = Integer.parseInt(request.getParameter("rowNum"));
			
//			LaunchCluster.main(null);
			List<ReviewerHelperClass> listTopR=new ArrayList<>();
			
			Top5List.getTopFive(listTopR, top);
			
			request.setAttribute("listTopR", listTopR);
			rd.forward(request, response);
		}
		else if(taskNo.equals("2")){
			
//			LaunchClusterSpark.main(null);
			if(request.getParameter("task2step").equals("1"))
			 FormResultFile.generateResult(request);
			else if(request.getParameter("task2step").equals("2")){
			 AwardScore.awardsScore(request);
			 try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			else if(request.getParameter("task2step").equals("3"))
			 FileToDB.loadFileToDB(request.getServletContext().getInitParameter("nasirRoot")+"/rating.txt");
			PrintWriter out=response.getWriter();
			JSONObject json=new JSONObject();
			json.put("status", true);
			out.println(json);
		}
		else if(taskNo.equals("3")){
			String requestType=request.getParameter("optradio");
			String[] genres=request.getParameterValues("lstGenre");
			System.out.println("Receievd req Type : "+requestType);
			if(genres!=null){
				for(String s : genres)
				 System.out.println("Genres : "+ s );
			}
			else{
				System.out.println("No Genre selected");
			}
			
			String topType;
			System.out.println("Fetch Status : "+FetchResultDB.fetch(requestType, genres, request));
			RequestDispatcher rd=request.getRequestDispatcher("DisplayOptionResult.jsp");
			if(requestType.equals("imdbrating"))
				topType="IMDB RATING";
			else if(requestType.equals("award_score"))
				topType="Oscars, Wins and Nominations";
			else
				topType="Amazon Review Score";
			request.setAttribute("reqType", topType);
			if(genres!=null)
				request.setAttribute("genres", genres);
			else
				request.setAttribute("genres", new String[]{"ALL"});
			rd.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

