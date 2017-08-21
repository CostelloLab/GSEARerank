<%--
  Created by IntelliJ IDEA.
  User: pielk
  Date: 8/21/2017
  Time: 4:16 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GSEA Rerank</title>
</head>
<body>
    <div style="text-align: center;">
        <h2>Using JavaBeans in JSP</h2>
        <jsp:useBean id="test" class="edu.ucdenver.gsearerank.Test"/>
        <jsp:setProperty name="test" property="message" value="Hello JSP..."/>

        <p>got message....</p>
        <jsp:getProperty name="test" property="message"/>
    </div>
</body>
</html>
