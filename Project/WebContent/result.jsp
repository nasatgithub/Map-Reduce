<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" />
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/4.0.0/css/font-awesome.css" />
<style type="text/css">
#wrapper{
background-color: white;
margin: 20px auto 20px auto;
border: 1px solid white;
width:940px;
}
</style>
<title>Top Results</title>
</head>
<body>
<div id="wrapper">
<button type="button" class="btn btn-default btn-lg" onclick="goHome()">
  <span class="glyphicon glyphicon-home" ></span>
</button>

<br><br>
<table class="table table-condensed table-bordered table-striped">
<tr>
<th style="text-align: center;">Rank</th>
<th style="text-align: center;">Reviewer Name</th>
<th style="text-align: center;">Review Counts</th>
<th style="text-align: center;">Review Helpfulness</th>
</tr>
<%int i=1; %>
<c:forEach items="${requestScope.listTopR}" var="obj">
<tr>
<td align="center"><%=i++%></td>
<td align="center">${obj.name}</td>
<td align="center">${obj.numOfReviews}</td>
<td align="center">${obj.numden}</td>
</tr>
</c:forEach>
</table>
</div>
<script type="text/javascript">
function goHome(){
	window.location.replace("frontUI.html");
} 
</script>
</body>
</html>