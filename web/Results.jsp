<%@ page import="java.util.Objects" %>
<%@ page %>

<html>
<head>
    <title>GSEARerank</title>
</head>
<body>
<h1>GSEARerank</h1>
<h2>Results</h2>

<jsp:useBean id="helper" scope="session" class="edu.ucdenver.gsearerank.WebHelper" />
<%
    String rnk = request.getParameter("rnk");
    String gmx = request.getParameter("gmt");
    String filt = request.getParameter("filt");
    String anno = request.getParameter("anno");

    if (!Objects.equals(anno, "")) {
        helper.setAnno(anno);
    }
    if (!Objects.equals(filt, "")) {
        helper.setFilt(filt);
    }
    if (!Objects.equals(gmx, "")) {
        helper.setGmx(gmx);
    }
    if (!Objects.equals(rnk, "")) {
        helper.setRnk(rnk);
    }

    out.println(helper.run());
%>

<jsp:getProperty name="helper" property="args" />
<a href="file:///C:\Users\pielk\Google Drive\Costello Lab\GSEARerank\Data\Test\Output\my_analysis.GseaPreranked_Rerank.1503189496603"> GSEA results </a>
</body>
</html>