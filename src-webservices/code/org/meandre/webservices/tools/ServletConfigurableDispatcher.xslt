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
							padding-left: 9px;
							margin-left: 0px;
							margin-top: 0px;
							margin-bottom: 0px;
						}
						
						#navigation li {
							margin-left: 10px;
							padding-left: 0px;
						}
						
						#navigation form {
							margin-bottom: 2px;
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
		 		<script language="JAVASCRIPT" type="TEXT/JAVASCRIPT"><![CDATA[
						checkForFlow = function () {
							if ( window.location.pathname=='/services/repository/list_flows.html' ||
							     window.location.pathname=='/services/repository/search_flows.html' ||
							     window.location.pathname=='/services/repository/flows_by_tag.html' ) {
								return confirm('Are you sure you want to run this flow');
							}
							else {
								alert('Only flows are allowed for execution');
								return false;
							}
						} 
					]]></script>
		 		<div id="menu"> 
					<img src="/public/resources/system/logo-meandre.gif" />
					<div id="navigation">
			 			<p>About</p>
			 		    <ul>
			 		    	<li><a href="/services/about/installation.html">Installation</a></li>
			 		    	<li><a href="/services/about/version.html">Version</a></li>
			 		    	<li><a href="/services/about/plugins.html">Plugins</a></li>
		 		    	</ul>
		 		    	<p>Public</p>
			 		    <ul>
			 		    	<li><a href="/public/services/repository.ttl" target="_blank">Published repository</a></li>
			 		    	<li><a href="/public/services/demo_repository.ttl" target="_blank">Demo repository</a></li>
			 		    	<li><a href="/public/services/ping.html" >Ping</a></li>
		 		    	</ul>
		 		    	<p>Locations</p>
			 		    <ul>
							<li><a href="/services/locations/list.html">List</a></li>
							<li>
								Add a location<br/>
								<ul>
									<form name="al"  method="get" action="/services/locations/add.html">
										<input type="text" id="loc" name="location" value="Location..." onclick="document.al.loc.value=''"/><br/>
										<input type="text" id="desc" name="description" value="Description..." onclick="document.al.desc.value=''"/><br/>
										<input type="submit" value="Add" />
									</form> 
								</ul>
							</li>
		 		    	</ul>
		 		    	<p>Repository</p>
			 		    <ul>
							<li><a href="/services/repository/dump.ttl" target="_blank">Dump</a></li>
							<li><a href="/services/repository/regenerate.html">Regenerate</a></li>
							<li><a href="/services/repository/list_components.html">Components</a></li>
							<li><a href="/services/repository/list_flows.html">Flows</a></li>
							<li><a href="/services/repository/tags.html">Tags</a></li>
							<li>
								<form name="sfc" method="get" action="/services/repository/search_components.html">
									<input type="text" id="sc" name="q" value="Search components..." onclick="document.sfc.sc.value=''"/><br/>
								</form> 
							</li>
							<li>
								<form name="sff"  method="get" action="/services/repository/search_flows.html">
									<input type="text" id="sf" name="q" value="Search flows..." onclick="document.sff.sf.value=''"/><br/>
								</form> 
							</li>
		 		    	</ul>
		 		    	<p>Execution</p>
			 		    <ul>
							<li><a href="/services/execute/list_running_flows.html">Running flows</a></li>
		 		    	</ul>
		 		    	<p>Security</p>
			 		    <ul>
							<li><a href="/services/security/users.html">Users</a></li>
							<li><a href="/services/security/valid_roles.html">Valid roles</a></li>
							<li><a href="/services/security/current_roles.html">Current roles</a></li>
							<li>
								<form name="rou"  method="get" action="/services/security/roles_of_user.html">
									<input type="text" id="un" name="user_name" value="User roles..." onclick="document.rou.un.value=''"/><br/>
								</form> 
							</li>
							<li>
								<form name="u"  method="get" action="/services/security/user.html">
									<input type="text" id="un" name="user_name" value="User info..." onclick="document.u.un.value=''"/><br/>
								</form> 
							</li>
							<li>
								<form name="rar"  method="get" action="/services/security/revoke_all_roles.html">
									<input type="text" id="un" name="user_name" value="Revoke all roles..." onclick="document.rar.un.value=''"/><br/>
								</form> 
							</li>
		 		    	</ul>
		 		    	<p>Publish</p>
			 		    <ul>
							<li><a href="/services/publish/list_published.html">List published</a></li>
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
			          				<xsl:if test="message">
			          					<th>Message</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_uri">
			          					<th>Meandre URI</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_tag">
			          					<th>Tag</th>
			          				</xsl:if>
			          				<xsl:if test="flow_instance_uri">
			          					<th>Flow instance URI</th>
			          				</xsl:if>
			          				<xsl:if test="flow_instance_webui_uri">
			          					<th>WebUI URL</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_role_name">
			          					<th>Role name</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_role_uri">
			          					<th>Role URI</th>
			          				</xsl:if>	
			          				<xsl:if test="user_name">
			          					<th>User name</th>
			          				</xsl:if>	
			          				<xsl:if test="full_name">
			          					<th>Full name</th>
			          				</xsl:if>			   
			          				<xsl:if test="revoked">
			          					<th>Roles revoked</th>
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
						     			<a>
						     				<xsl:attribute name="href">/services/locations/remove.html?location=<xsl:value-of select="location"/></xsl:attribute> 
											(remove)
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
		          				<xsl:if test="message">
		          					<td><xsl:value-of select="message"/></td>
		          				</xsl:if>
		          				<xsl:if test="meandre_uri">
				     				<td>
						     			<a>
						     				<xsl:attribute name="href">/services/repository/describe.ttl?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute> 
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:value-of select="meandre_uri"/>
						     			</a>
						     			(<a>
						     				<xsl:attribute name="href">/services/execute/flow.txt?statistics=true&amp;uri=<xsl:value-of select="meandre_uri"/></xsl:attribute> 
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:attribute name="onclick">return checkForFlow()</xsl:attribute>
											run 
						     			</a>
						     			|						     			
						     			<a>
						     				<xsl:attribute name="href">/services/publish/publish.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute> 
											publish
						     			</a>
						     			|						     			
						     			<a>
						     				<xsl:attribute name="href">/services/publish/unpublish.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute> 
											unpublish
						     			</a>
						     			|						     			
						     			<a>
						     				<xsl:attribute name="href">/services/repository/remove.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute> 
											remove
						     			</a>)
				     				</td>
				     			</xsl:if>
				     			<xsl:if test="meandre_tag">
				     				<td>
				     					<xsl:value-of select="meandre_tag"/>
						     			(<a>
						     				<xsl:attribute name="href">/services/repository/components_by_tag.html?tag=<xsl:value-of select="meandre_tag"/></xsl:attribute> 
											<xsl:value-of select="meandre_uri"/>
											components
						     			</a> |
						     			<a>
						     				<xsl:attribute name="href">/services/repository/flows_by_tag.html?tag=<xsl:value-of select="meandre_tag"/></xsl:attribute> 
											flows
						     			</a>)
				     				</td>
				     			</xsl:if>
				     			<xsl:if test="flow_instance_uri">
		          					<td><xsl:value-of select="flow_instance_uri"/></td>
		          				</xsl:if>
		          				<xsl:if test="flow_instance_webui_uri">
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_webui_uri"/></xsl:attribute> 
											<xsl:value-of select="flow_instance_webui_uri"/>
						     			</a>
		          					</td>
		          				</xsl:if>	
		          				<xsl:if test="meandre_role_name">
		          					<td><xsl:value-of select="meandre_role_name"/></td>
		          				</xsl:if>
		          				<xsl:if test="meandre_role_uri">
		          					<td><xsl:value-of select="meandre_role_uri"/></td>
		          				</xsl:if>	
		          				<xsl:if test="user_name">
		          					<td><xsl:value-of select="user_name"/></td>
		          				</xsl:if>	
		          				<xsl:if test="full_name">
		          					<td><xsl:value-of select="full_name"/></td>
		          				</xsl:if>	
		          				<xsl:if test="revoked">
		          					<td><xsl:value-of select="revoked"/></td>
		          				</xsl:if>		
					     	</tr>
				     	</xsl:for-each>
		     		</table>
		     		<br/>
		     		<a href="javascript:javascript:history.go(-1)">Back</a> ---
		     		<a href="">Refresh</a>
	     		</div>
		 	</body>
	 	</html>
	</xsl:template>

</xsl:stylesheet>