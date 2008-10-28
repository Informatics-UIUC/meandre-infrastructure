<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<xsl:template match="/meandre_response">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
			<head>
				<title>Meandre Server</title>
				<meta http-equiv="content-type" content="text/html; charset=utf8" />
				<meta http-equiv="content-language" content="EN" />
				<meta name="ROBOTS" content="NOINDEX,NOFOLLOW"/>
				<meta name="description" content="Meandre Server"/>
				<style type="text/css">
						a {
							color: #444;
							text-decoration: none;
						}
						
						a:hover {
							color: orange;
							text-decoration: underline;
						}
						
						
						body {
						   color: #444; 
						   background: white; 
						   font-family: Helvetica, Arial, Verdana; 
						   font-size: 11px; 
						}	
						#menu {
							width: 200px;
							float: left;
						}
						
						#menu img {
							width: 95%;
							margin-left:auto;
							margin-right: auto;
							border: 0px none white; 
							margin-bottom: 6px;
						}	
						
						#navigation {
							margin-top: 10px;
							background: orange;
							border: 1px solid gray;
							padding-left: 0px;
							margin-left: 0px;
						}
						
						#navigation p {
							padding-left: 6px;
							padding-top: 3px;
							padding-bottom: 3px;
							margin-top: 2px;
							margin-bottom: 2px;
							color: white;
							font-weight: bolder;
							font-size:11px;
						}
						
						#navigation ul {
							background: white;
							list-style-type : none;
							padding-top: 6px;
							padding-bottom: 6px;
							padding-left: 0px;
							margin-left: 0px;
							margin-top: 0px;
							margin-bottom: 0px;
						}
						
						#navigation li {
							margin-left: 10px;
							padding-left: 0px;
						}
						
						#main {
							min-width : 440px;
							padding-left : 210px;
							margin-right:10px;
						    font-size: 10px; 
						}
						
						#main table {
							width: 100%;
							margin-left:auto;
							margin-right:auto;
							border: 1px solid gray;
							border-collapse: collapse;
							font-size: 12px;
						}
						
						#main th {
							color: white;
							background: orange;
						}	
						
						#main th,td {	
							border: 1px solid gray;
						}
						
						
					</style>
			</head>
		 	<body>
		 		<div id="menu"> 
					<img src="/public/resources/system/logo-meandre.gif" />
					<div id="navigation">
			 			<p>Locations</p>
			 		    <ul>
			 		    	<li><a href="/services/locations/list.html">List</a></li>
			 		    	<li><a href="/services/locations/list.html">List</a></li>
		 		    	</ul>
		 		    	<p>Locations</p>
			 		    <ul>
			 		    	<li><a href="/services/locations/list.html">List</a></li>
			 		    	<li><a href="/services/locations/list.html">List</a></li>
		 		    	</ul>
		 		    </div>
				</div>	
				<div id="main">
					<table>
						<xsl:for-each select="meandre_item">
							<xsl:if test="position() = 1">
								<tr>
									<xsl:if test="location">
			          					<th>Location</th>
			          				</xsl:if>
			          				<xsl:if test="description">
			          					<th>Description</th>
			          				</xsl:if>
		          				</tr>
		          			</xsl:if>
				     		<tr>
				     			<td>
					     			<a>
					     				<xsl:attribute name="href"><xsl:value-of select="location"/></xsl:attribute> 
										<xsl:attribute name="target">_blank</xsl:attribute>
										<xsl:value-of select="location"/>
					     			</a>
				     			</td>
					     		<td><xsl:value-of select="description"/></td>
					     	</tr>
				     	</xsl:for-each>
		     		</table>
	     		</div>
		 	</body>
	 	</html>
	</xsl:template>

</xsl:stylesheet>