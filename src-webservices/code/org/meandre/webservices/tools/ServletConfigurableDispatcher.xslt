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
							padding-top: 0px;
							padding-bottom: 0px;
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
			 			<p>About</p>
			 		    <ul>
			 		    	<li><a href="/services/about/installation.html">Installation</a></li>
			 		    	<li><a href="/services/about/version.html">Version</a></li>
			 		    	<li><a href="/services/about/plugins.html">Plugins</a></li>
		 		    	</ul>
		 		    	<p>Locations</p>
			 		    <ul>
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
			          				<xsl:if test="key">
			          					<th>Key</th>
			          				</xsl:if>
			          				<xsl:if test="alias">
			          					<th>Alias</th>
			          				</xsl:if>
			          				<xsl:if test="className">
			          					<th>Class name</th>
			          				</xsl:if>
			          				<xsl:if test="isServlet">
			          					<th>Servlet flag</th>
			          				</xsl:if>
			          				<xsl:if test="version">
			          					<th>Version</th>
			          				</xsl:if>
			          				<xsl:if test="DB_PASSWD">
			          					<th>DB password</th>
			          				</xsl:if>
			          				<xsl:if test="MEANDRE_STORE_CONFIG_FILE">
			          					<th>Store config</th>
			          				</xsl:if>
			          				<xsl:if test="DB_USER">
			          					<th>DB user</th>
			          				</xsl:if>
			          				<xsl:if test="CURRENT_USER_LOGGED_IN">
			          					<th>Logged user</th>
			          				</xsl:if>
			          				<xsl:if test="DB">
			          					<th>DB</th>
			          				</xsl:if>
			          				<xsl:if test="MEANDRE_AUTHENTICATION_REALM_FILENAME">
			          					<th>Real file</th>
			          				</xsl:if>
			          				<xsl:if test="MEANDRE_VERSION">
			          					<th>Version</th>
			          				</xsl:if>
			          				<xsl:if test="CURRENT_SESSION_ID">
			          					<th>Session</th>
			          				</xsl:if>
			          				<xsl:if test="CURRENT_TIME">
			          					<th>Time</th>
			          				</xsl:if>
			          				<xsl:if test="MEANDRE_ADMIN_USER">
			          					<th>Admin user</th>
			          				</xsl:if>
			          				<xsl:if test="DB_URL">
			          					<th>DB URL</th>
			          				</xsl:if>
			          				<xsl:if test="MEANDRE_RIGHTS">
			          					<th>Rights</th>
			          				</xsl:if>
			          				<xsl:if test="DB_DRIVER_CLASS">
			          					<th>DB Driver</th>
			          				</xsl:if>
		          				</tr>
		          			</xsl:if>
				     		<tr>
				     			<xsl:if test="location">
				     				<td>
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="location"/></xsl:attribute> 
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:value-of select="location"/>
						     			</a>
				     				</td>
				     			</xsl:if>
					     		<xsl:if test="description">
					     			<td><xsl:value-of select="description"/></td>
					     		</xsl:if>
		          				<xsl:if test="key">
		          					<td><xsl:value-of select="key"/></td>
		          				</xsl:if>
		          				<xsl:if test="alias">
		          					<td><xsl:value-of select="alias"/></td>
		          				</xsl:if>
		          				<xsl:if test="className">
		          					<td><xsl:value-of select="className"/></td>
		          				</xsl:if>
		          				<xsl:if test="isServlet">
		          					<td><xsl:value-of select="isServlet"/></td>
		          				</xsl:if>
					     		
					     		<xsl:if test="version">
		          					<td><xsl:value-of select="version"/></td>
		          				</xsl:if>
		          				
		          				<xsl:if test="DB_PASSWD">
		          					<td><xsl:value-of select="DB_PASSWD"/></td>
		          				</xsl:if>
		          				<xsl:if test="MEANDRE_STORE_CONFIG_FILE">
		          					<td><xsl:value-of select="MEANDRE_STORE_CONFIG_FILE"/></td>
		          				</xsl:if>
		          				<xsl:if test="DB_USER">
		          					<td><xsl:value-of select="DB_USER"/></td>
		          				</xsl:if>
		          				<xsl:if test="CURRENT_USER_LOGGED_IN">
		          					<td><xsl:value-of select="CURRENT_USER_LOGGED_IN"/></td>
		          				</xsl:if>
		          				<xsl:if test="DB">
		          					<td><xsl:value-of select="DB"/></td>
		          				</xsl:if>
		          				<xsl:if test="MEANDRE_AUTHENTICATION_REALM_FILENAME">
		          					<td><xsl:value-of select="MEANDRE_AUTHENTICATION_REALM_FILENAME"/></td>
		          				</xsl:if>
		          				<xsl:if test="MEANDRE_VERSION">
		          					<td><xsl:value-of select="MEANDRE_VERSION"/></td>
		          				</xsl:if>
		          				<xsl:if test="CURRENT_SESSION_ID">
		          					<td><xsl:value-of select="CURRENT_SESSION_ID"/>n</td>
		          				</xsl:if>
		          				<xsl:if test="CURRENT_TIME">
		          					<td><xsl:value-of select="CURRENT_TIME"/></td>
		          				</xsl:if>
		          				<xsl:if test="MEANDRE_ADMIN_USER">
		          					<td><xsl:value-of select="MEANDRE_ADMIN_USER"/></td>
		          				</xsl:if>
		          				<xsl:if test="DB_URL">
		          					<td><xsl:value-of select="DB_URL"/></td>
		          				</xsl:if>
		          				<xsl:if test="MEANDRE_RIGHTS">
		          					<td><xsl:value-of select="MEANDRE_RIGHTS"/></td>
		          				</xsl:if>
		          				<xsl:if test="DB_DRIVER_CLASS">
		          					<td><xsl:value-of select="DB_DRIVER_CLASS"/></td>
		          				</xsl:if>
					     	</tr>
				     	</xsl:for-each>
		     		</table>
	     		</div>
		 	</body>
	 	</html>
	</xsl:template>

</xsl:stylesheet>