<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet"
	href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" />
<link rel="stylesheet"
	href="//netdna.bootstrapcdn.com/font-awesome/4.0.0/css/font-awesome.css" />
<title>Insert title here</title>
<style type="text/css">
#wrapper {
	background-color: white;
	margin: 20px auto 20px auto;
	border: 1px solid white;
	width: 940px;
}
</style>
</head>
<body>
	<div id="wrapper" class="table-responsive">
	<button type="button" class="btn btn-default btn-lg"
			onclick="goHome()">
			<span class="glyphicon glyphicon-home"></span>
		</button>
	<h2>Top 10 Movies By ${requestScope.reqType} </h2>
	<h3> 
	For Genres : 
	<c:forEach items="${requestScope.genres}" var="genre">
	 ${genre} |
	 </c:forEach>
	 </h3>
		<br><br>
		<table class="table table-bordered table-striped">
			<thead>
				<tr>
					<th style="text-align: center;vertical-align: middle;">Title</th>
					<th style="text-align: center;vertical-align: middle;">Genre</th>
					<th style="text-align: center;vertical-align: middle;">Amazon Score</th>
					<th style="text-align: center;vertical-align: middle;">Awards</th>
					<th style="text-align: center;vertical-align: middle;">Poster</th>
					<th style="text-align: center;vertical-align: middle;">IMDB Rating</th>
					<th style="text-align: center;vertical-align: middle;">Language</th>
					<th style="text-align: center;vertical-align: middle;">Year</th>
				</tr>
			<thead>
				<c:forEach items="${requestScope.listOpt}" var="obj">
					<tbody>
						<tr>
							<td align="center">${obj.title}</td>
							<td align="center">${obj.genre}</td>
							<td align="center">${obj.amazonscore}</td>
							<td align="center">${obj.awards}</td>
							<td>
							<c:choose>
							<c:when test="${obj.poster!='N/A'}">
							<img src="${obj.poster}" width="200" height="200"></img>
							</c:when>
							<c:otherwise>
							<img src="No_Poster.png" width="200" height="200"></img>
							</c:otherwise>
							</c:choose>
							</td>
							<td align="center">${obj.imdbrating}</td>
							<td align="center">${obj.language}</td>
							<td align="center">${obj.year}</td>
						</tr>
					</tbody>
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