<%@ page contentType="application/x-java-jnlp-file" %>
<%@ page session="true" %>
<% 
	response.setDateHeader("Expires", 0);
	
	final String FILE_PARAM = "file";
	final String LTV_PARAM = "ltv";
	String paramFile = request.getParameter(FILE_PARAM);
	String paramLTV = request.getParameter(LTV_PARAM);
%>


<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<jnlp codebase="http://moerbius.no-ip.org:8080/cptsigner" href="index.jsp?<%=FILE_PARAM + "=" + paramFile%>&<%=LTV_PARAM + "=" + paramLTV%>" spec="1.0+">
    <information>
        <title>CPTsigner</title>
        <vendor>Canon Portugal</vendor>
        <homepage href=""/>
        <description>CPTsigner</description>
        <description kind="short">CPTsigner</description>
    </information>
    <update check="always"/>
    <security>
<all-permissions/>
</security>
    <resources>
        <j2se version="1.8+"/>
        <jar href="CPTsigner.jar" main="true"/>
    <jar href="lib/commons-logging-1.2.jar"/>
<jar href="lib/fontbox-2.0.4.jar"/>
<jar href="lib/pdfbox-2.0.4.jar"/>
<jar href="lib/poreid-1.49.jar"/>
<jar href="lib/itextpdf-5.5.10.jar"/>
<jar href="lib/itext-pdfa-5.5.10.jar"/>
<jar href="lib/itext-xtra-5.5.10.jar"/>
<jar href="lib/xmlworker-5.5.10.jar"/>
<extension href="jnlpcomponent1.jnlp"/>
</resources>
    <application-desc main-class="canonportugal.CPTsigner">
	<argument><%=paramFile%></argument>
	<argument><%=paramLTV%></argument>
    </application-desc>
</jnlp>