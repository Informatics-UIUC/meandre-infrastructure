<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<xsl:param name="date"></xsl:param>
	<xsl:param name="context"></xsl:param>
	<xsl:param name="host"></xsl:param>
	<xsl:param name="port"></xsl:param>
	<xsl:param name="user"></xsl:param>

	<xsl:template match="/meandre_response">
		<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
			<head>
				<title>Meandre Server</title>
				<meta http-equiv="content-type" content="text/html; charset=utf8" />
				<meta http-equiv="content-language" content="EN" />
				<meta name="ROBOTS" content="NOINDEX,NOFOLLOW"/>
				<meta name="description" content="Meandre Server"/>
				<style type="text/css">
						a, a:visited {
							color: #aa6000;
							text-decoration: none;
						}

						a:hover {
							color: #cc6000;
							text-decoration: underline;
							font-style: italic;
						}


						body {
						   color: #444;
						   background: white;
						   font-family: Verdana, Arial, Helvetica ;
						   font-size: 11px;
						}
						#menu {
							width: 200px;
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
							margin-top: 1px;
							margin-bottom: 2px;
							font-size: 11px;
						}

						#main {
							min-width : 440px;
							margin-right:10px;
						    font-size: 10px;
						}

						#main table {
							width: 98%;
							margin-left:auto;
							margin-right:auto;
							border: 1px solid gray;
							border-collapse: collapse;
							font-size: 11px;
						}

						#main th {
							color: white;
							background: orange;
						}

						#main th,td {
							border: 1px solid gray;
							padding-left:6px;
							padding-top:3px;
							padding-bottom:3px;
						}

						#separator {
							border:none;
							font-size:11px;
						}

					</style>
			</head>
		 	<body>
		 		<script language="JAVASCRIPT" type="TEXT/JAVASCRIPT"><![CDATA[
						checkForFlow = function () {
						    var pn = window.location.pathname
						    var pn1 = pn.indexOf('/services/repository/list_flows.html')!=-1
						    var pn2 = pn.indexOf('/services/repository/search_flows.html')!=-1
						    var pn3 = pn.indexOf('/services/repository/flows_by_tag.html')!=-1
						    var pn4 = pn.indexOf('/services/publish/list_published.html')!=-1
							if ( pn1 ) {
								return confirm('Are you sure you want to run this flow');
							}
							else if ( pn2 ) {
								return confirm('Are you sure you want to run this flow');
							}
							else if ( pn3 ) {
								return confirm('Are you sure you want to run this flow');
							}
							else if ( pn4 ) {
								alert('Flows cannot be run from this menu. Please run them from the flow list');
								return false;
							}
							else {
								alert('Only flows are allowed for execution');
								return false;
							}
						}
					]]></script>
				<table id="separator"><tr><td id="separator" valign="top" style="border:none;" width="200px">
		 		<div id="menu">
					<img>
						<xsl:attribute name="src"><xsl:value-of select="$context"/>/public/resources/system/logo-meandre.gif</xsl:attribute>
					</img>
					<div id="navigation">
			 			<p>Repository</p>
			 		    <ul>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/add_to_repository.html</xsl:attribute>Add</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/dump.ttl</xsl:attribute><xsl:attribute name="target">_blank</xsl:attribute>Dump</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/list_components.html?order=name</xsl:attribute>Components</a> (<a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/list_components.html?order=date</xsl:attribute>by date</a>)</li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/list_flows.html?order=name</xsl:attribute>Flows</a> (<a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/list_flows.html?order=date</xsl:attribute>by date</a>)</li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/tags.html</xsl:attribute>Tags</a></li>
							<li>
								<form name="sfc" method="get">
								 	<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/repository/search_components.html</xsl:attribute>
									<input type="text" id="sc" name="q" value="Search components..." onclick="document.sfc.sc.value=''"/><br/>
								</form>
							</li>
							<li>
								<form name="sff"  method="get">
									<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/repository/search_flows.html</xsl:attribute>
									<input type="text" id="sf" name="q" value="Search flows..." onclick="document.sff.sf.value=''"/><br/>
								</form>
							</li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/clear.html</xsl:attribute><xsl:attribute name="onclick">return confirm('Are you sure you want to remove all the components and flow from the repository?');</xsl:attribute>Clear</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/regenerate.html</xsl:attribute><xsl:attribute name="onclick">return confirm('Are you sure you want to to regenerate the repository?');</xsl:attribute>Regenerate</a></li>
		 		    	</ul>
		 		    	<p>Publish</p>
			 		    <ul>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/publish/list_published.html</xsl:attribute>List published</a></li>
							<li><a onclick="return confirm('Are you sure you want to unpublish all components and flows?');"><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/publish/unpublish_all.html</xsl:attribute>Unpublish all</a></li>
		 		    		<li><a onclick="return confirm('Are you sure you want to publish all components and flows?');"><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/publish/publish_all.html</xsl:attribute>Publish all</a></li>
		 		    	</ul>
		 		    	<p>Execution</p>
			 		    <ul>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/execute/list_running_flows.html</xsl:attribute>Running flows</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/execute_repository.html</xsl:attribute>Execute repository</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/jobs/list_jobs_statuses.html</xsl:attribute>List job statuses</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/execute/clean_uri_flow.html</xsl:attribute>Clean data structures</a></li>
		 		    	</ul>
		 		    	<p>Cluster</p>
			 		    <ul>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/coordinator/log.html</xsl:attribute>Cluster log</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/coordinator/status.html</xsl:attribute>Servers' statuses</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/coordinator/info.html</xsl:attribute>Servers' information</a></li>
		 		    		<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/coordinator/property.html</xsl:attribute>Servers' properties</a></li>
		 		    		<li><a onclick="return confirm('Are you sure you want to shutdown this server?');"><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/server/shutdown.html</xsl:attribute>Shutdown this server</a></li>
		 		    	</ul>
		 		    	<p>Locations</p>
			 		    <ul>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/locations/list.html</xsl:attribute>List</a></li>
							<li>
								Add a location<br/>
								<ul>
									<form name="al"  method="get">
								 		<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/locations/add.html</xsl:attribute>
										<input type="text" id="loc" name="location" value="Location..." onclick="document.al.loc.value=''"/><br/>
										<input type="text" id="desc" name="description" value="Description..." onclick="document.al.desc.value=''"/><br/>
										<input type="submit" value="Add" />
									</form>
								</ul>
							</li>
		 		    	</ul>
		 		    	<p>Security</p>
			 		    <ul>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/create_user.html</xsl:attribute>Create user</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/security/users.html</xsl:attribute>Users</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/security/valid_roles.html</xsl:attribute>Valid roles</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/security/current_roles.html</xsl:attribute>Current user roles</a></li>
							<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/roles_map.html</xsl:attribute>Role map</a></li>
							<li>
								<form name="rou"  method="get">
								 	<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/security/roles_of_user.html</xsl:attribute>
									<input type="text" id="un" name="user_name" value="User roles..." onclick="document.rou.un.value=''"/><br/>
								</form>
							</li>
							<li>
								<form name="u"  method="get">
							 		<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/security/user.html</xsl:attribute>
									<input type="text" id="un" name="user_name" value="User info..." onclick="document.u.un.value=''"/><br/>
								</form>
							</li>
							<li>
								<form name="rar"  method="get">
							 		<xsl:attribute name="action"><xsl:value-of select="$context"/>/services/security/revoke_all_roles.html</xsl:attribute>
									<input type="text" id="un" name="user_name" value="Revoke all roles..." onclick="document.rar.un.value=''"/><br/>
								</form>
							</li>
		 		    	</ul>
		 		    	<p>Server Logs</p>
			 		    <ul>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/logs/webservices.html</xsl:attribute>Webservices</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/logs/kernel.html</xsl:attribute>Kernel</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/logs/coordinator.html</xsl:attribute>Coordinator</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/logs/plugins.html</xsl:attribute>Plugins</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/logs/probes.html</xsl:attribute>Probes</a></li>
		 		    	</ul>
		 		    	<p>Public</p>
			 		    <ul>
			 		    	<li><a target="_blank"><xsl:attribute name="href"><xsl:value-of select="$context"/>/public/services/repository.ttl</xsl:attribute>Published repository</a></li>
			 		    	<li><a target="_blank"><xsl:attribute name="href"><xsl:value-of select="$context"/>/public/services/demo_repository.ttl</xsl:attribute>Demo repository</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/public/services/ping.html</xsl:attribute>Ping</a></li>
		 		    	</ul>
		 		    	<p>About</p>
			 		    <ul>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/about/installation.html</xsl:attribute>Installation</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/about/version.html</xsl:attribute>Version</a></li>
			 		    	<li><a><xsl:attribute name="href"><xsl:value-of select="$context"/>/services/about/plugins.html</xsl:attribute>Plugins</a></li>
		 		    	</ul>
		 		    </div>
		 		    <br/>All rights reserved by<br/>DITA, NCSA, and UofI, 2007-2011.
				</div>
				</td><td id="separator" valign="top" width="100%">
				<div id="main">
					<p style="text-align:right;width: 98%;"><strong><xsl:value-of select="$date"/></strong>. User <strong><xsl:value-of select="$user"/></strong> on host <strong><xsl:value-of select="$host"/></strong> at port <strong><xsl:value-of select="$port"/></strong>.</p>

					<table>
						<xsl:for-each select="meandre_item">
							<xsl:if test="position() = 1">
								<tr>
									<xsl:if test="location">
			          					<th colspan='2'>Location</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_uri_name">
			          					<th>Name</th>
		          					</xsl:if>
		          					<xsl:if test="description and not(meandre_uri)">
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
			          					<th>Actions</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_tag">
			          					<th>Tag</th>
			          					<th>Actions</th>
			          				</xsl:if>
			          				<xsl:if test="flow_instance_uri">
			          					<th>Job ID</th>
			          					<th>Console</th>
			          				</xsl:if>
     							    <xsl:if test="flow_instance_proxy_webui_relative">
			          					<th>Proxy UI</th>
			          				</xsl:if>
			          				<xsl:if test="flow_instance_webui_uri">
			          					<th>WebUI URL</th>
			          				</xsl:if>
			          				<xsl:if test="flow_instance_proxy_webui_uri">
			          					<th>Proxy WebUI URL</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_role_name">
			          					<th>Role name</th>
			          				</xsl:if>
			          				<xsl:if test="meandre_role_uri">
			          					<th>Role URI</th>
			          				</xsl:if>
			          				<xsl:if test="user_name">
			          					<th>User name</th>
			          					<th>Actions</th>
			          				</xsl:if>
			          				<xsl:if test="full_name">
			          					<th>Full name</th>
			          				</xsl:if>
			          				<xsl:if test="revoked">
			          					<th>Roles revoked</th>
			          				</xsl:if>
			          				<xsl:if test="granted">
			          					<th>Roles granted</th>
			          				</xsl:if>
			          				<xsl:if test="id">
			          					<th>ID</th>
			          				</xsl:if>
			          				<xsl:if test="server_id">
			          					<th>Server</th>
			          				</xsl:if>
			          				<xsl:if test="status">
			          					<th>Status</th>
			          				</xsl:if>
			          				<xsl:if test="user_id">
			          					<th>User</th>
			          				</xsl:if>
			          				<xsl:if test="uptime">
			          					<th>Uptime</th>
			          				</xsl:if>
			          				<xsl:if test="failed_updates">
			          					<th>Failed updates</th>
			          				</xsl:if>
			          				<xsl:if test="ip">
			          					<th>IP</th>
			          				</xsl:if>
			          				<xsl:if test="server_name">
			          					<th>Name</th>
			          				</xsl:if>
			          				<xsl:if test="port">
			          					<th>Port</th>
			          				</xsl:if>
			          				<xsl:if test="memory_available">
			          					<th>Memory available</th>
			          				</xsl:if>
			          				<xsl:if test="number_processors">
			          					<th>Processors</th>
			          				</xsl:if>
			          				<xsl:if test="os_name">
			          					<th>OS</th>
			          				</xsl:if>
			          				<xsl:if test="os_arch">
			          					<th>OS arch</th>
			          				</xsl:if>
			          				<xsl:if test="os_version">
			          					<th>OS version</th>
			          				</xsl:if>
			          				<xsl:if test="java_version">
			          					<th>Java Version</th>
			          				</xsl:if>
			          				<xsl:if test="java_vm_version">
			          					<th>Java VM version</th>
			          				</xsl:if>
			          				<xsl:if test="java_vm_vendor">
			          					<th>Java Vendor</th>
			          				</xsl:if>
			          				<xsl:if test="local_user_name">
			          					<th>Local user</th>
			          				</xsl:if>
			          				<xsl:if test="user_home">
			          					<th>User home</th>
			          				</xsl:if>
			          				<xsl:if test="server_description">
			          					<th>Description</th>
			          				</xsl:if>
			          				<xsl:if test="ts">
			          					<th>Time stamp</th>
			          				</xsl:if>
			          				<xsl:if test="previous_updated">
			          					<th>Last update</th>
			          				</xsl:if>
			          				<xsl:if test="free_memory">
			          					<th>Free memory</th>
			          				</xsl:if>
			          				<xsl:if test="property_key">
			          					<th>Key</th>
			          				</xsl:if>
			          				<xsl:if test="property_value">
			          					<th>Value</th>
			          				</xsl:if>
			          				<xsl:if test="job_id and not(console)">
			          					<th>Job ID</th>
			          				</xsl:if>
			          				<xsl:if test="console">
			          					<th>Console output</th>
			          				</xsl:if>
			          				<xsl:if test="token">
			          					<th>Token cleaned</th>
			          				</xsl:if>
			          				<xsl:if test="log">
			          					<th>Log</th>
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
						     		<td>
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/locations/remove.html?location=<xsl:value-of select="location"/></xsl:attribute>
											remove
						     			</a>
				     				</td>
				     			</xsl:if>
				     			<xsl:if test="meandre_uri_name">
		          					<td><xsl:value-of select="meandre_uri_name"/></td>
	          					</xsl:if>
		          				<xsl:if test="description and not(meandre_uri)">
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
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/show.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											<xsl:value-of select="meandre_uri"/>
						     			</a>
						     		</td>
						     		<td>
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/execute/flow.txt?statistics=true&amp;uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:attribute name="onclick">return checkForFlow()</xsl:attribute>
											run
						     			</a>
						     			-
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/auxiliar/tune_flow.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											<xsl:attribute name="target">_blank</xsl:attribute>
											tune&amp;run
						     			</a>
						     			-
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/publish/publish.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											publish
						     			</a>
						     			-
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/publish/unpublish.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											unpublish
						     			</a>
						     			-
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/remove.html?uri=<xsl:value-of select="meandre_uri"/></xsl:attribute>
											remove
						     			</a>
				     				</td>
				     			</xsl:if>
				     			<xsl:if test="meandre_tag">
				     				<td>
				     					<xsl:value-of select="meandre_tag"/>
						     		</td>
						     		<td>
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/components_by_tag.html?tag=<xsl:value-of select="meandre_tag"/></xsl:attribute>
											<xsl:value-of select="meandre_uri"/>
											show components
						     			</a>
						     			-
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/repository/flows_by_tag.html?tag=<xsl:value-of select="meandre_tag"/></xsl:attribute>
											show flows
						     			</a>
				     				</td>
				     			</xsl:if>
				     			<xsl:if test="flow_instance_uri">
		          					<td><xsl:value-of select="flow_instance_uri"/></td>
		          					<td>
		          						<a>
		          							<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/jobs/job_console.html?uri=<xsl:value-of select="flow_instance_uri"/></xsl:attribute>
											view output
		          						</a>
		          					</td>
		          				</xsl:if>
		          				<xsl:if test="flow_instance_proxy_webui_relative">
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_proxy_webui_relative"/></xsl:attribute>
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:value-of select="flow_instance_proxy_webui_relative"/>
						     			</a>
		          					</td>
		          				</xsl:if>
		          				<xsl:if test="flow_instance_webui_uri">
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_webui_uri"/></xsl:attribute>
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:value-of select="flow_instance_webui_uri"/>
						     			</a> -
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_webui_uri"/>admin/abort.txt</xsl:attribute>
						     				<xsl:attribute name="onclick">return confirm('Are you sure you want to abort the flow execution?');</xsl:attribute>
											abort
						     			</a>
		          					</td>
		          				</xsl:if>
		          				<xsl:if test="flow_instance_proxy_webui_uri">
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_proxy_webui_uri"/></xsl:attribute>
											<xsl:attribute name="target">_blank</xsl:attribute>
											<xsl:value-of select="flow_instance_proxy_webui_uri"/>
						     			</a>
						     			<a>
						     				<xsl:attribute name="href"><xsl:value-of select="flow_instance_proxy_webui_uri"/>admin/abort.txt</xsl:attribute>
						     				<xsl:attribute name="onclick">return confirm('Are you sure you want to abort the flow execution?');</xsl:attribute>
											abort
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
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/security/remove_users.html?user_name=<xsl:value-of select="user_name"/></xsl:attribute>
											<xsl:attribute name="onclick">return confirm('Are you sure you want to delete this user?');</xsl:attribute>
											delete user
						     			</a>
		          					</td>
		          				</xsl:if>
		          				<xsl:if test="full_name">
		          					<td><xsl:value-of select="full_name"/></td>
		          				</xsl:if>
		          				<xsl:if test="revoked">
		          					<td><xsl:value-of select="revoked"/></td>
		          				</xsl:if>
		          				<xsl:if test="granted">
		          					<td><xsl:value-of select="granted"/></td>
		          				</xsl:if>
		          				<xsl:if test="id">
		          					<td><xsl:value-of select="id"/></td>
		          				</xsl:if>
		          				<xsl:if test="server_id">
		          					<td><xsl:value-of select="server_id"/></td>
		          				</xsl:if>
		          				<xsl:if test="status">
		          					<td><xsl:value-of select="status"/></td>
		          				</xsl:if>
		          				<xsl:if test="user_id">
		          					<td>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/security/user.html?user_name=<xsl:value-of select="user_id"/></xsl:attribute>
											<xsl:value-of select="user_id"/>
					     				</a>
				     				</td>
		          				</xsl:if>
		          				<xsl:if test="uptime">
		          					<td><xsl:value-of select="uptime"/></td>
		          				</xsl:if>
		          				<xsl:if test="failed_updates">
		          					<td><xsl:value-of select="failed_updates"/></td>
		          				</xsl:if>
		          				<xsl:if test="ip">
		          					<td><xsl:value-of select="ip"/></td>
		          				</xsl:if>
		          				<xsl:if test="server_name">
		          					<td><xsl:value-of select="server_name"/></td>
		          				</xsl:if>
		          				<xsl:if test="port">
		          					<td><xsl:value-of select="port"/></td>
		          				</xsl:if>
		          				<xsl:if test="memory_available">
		          					<td><xsl:value-of select="memory_available"/></td>
		          				</xsl:if>
		          				<xsl:if test="number_processors">
		          					<td><xsl:value-of select="number_processors"/></td>
		          				</xsl:if>
		          				<xsl:if test="os_name">
		          					<td><xsl:value-of select="os_name"/></td>
		          				</xsl:if>
		          				<xsl:if test="os_arch">
		          					<td><xsl:value-of select="os_arch"/></td>
		          				</xsl:if>
		          				<xsl:if test="os_version">
		          					<td><xsl:value-of select="os_version"/></td>
		          				</xsl:if>
		          				<xsl:if test="java_version">
		          					<td><xsl:value-of select="java_version"/></td>
		          				</xsl:if>
		          				<xsl:if test="java_vm_version">
		          					<td><xsl:value-of select="java_vm_version"/></td>
		          				</xsl:if>
		          				<xsl:if test="java_vm_vendor">
		          					<td><xsl:value-of select="java_vm_vendor"/></td>
		          				</xsl:if>
		          				<xsl:if test="local_user_name">
		          					<td><xsl:value-of select="local_user_name"/></td>
		          				</xsl:if>
		          				<xsl:if test="user_home">
		          					<td><xsl:value-of select="user_home"/></td>
		          				</xsl:if>
		          				<xsl:if test="server_description">
		          					<td><xsl:value-of select="server_description"/></td>
		          				</xsl:if>
		          				<xsl:if test="ts">
		          					<td><xsl:value-of select="ts"/></td>
		          				</xsl:if>
		          				<xsl:if test="previous_updated">
		          					<td><xsl:value-of select="previous_updated"/></td>
		          				</xsl:if>
		          				<xsl:if test="free_memory">
		          					<td><xsl:value-of select="free_memory"/></td>
		          				</xsl:if>
		          				<xsl:if test="property_key">
		          					<td><xsl:value-of select="property_key"/></td>
		          				</xsl:if>
		          				<xsl:if test="property_value">
		          					<td><xsl:value-of select="property_value"/></td>
		          				</xsl:if>
		          				<xsl:if test="job_id and not(console)">
		          					<td>
		          						<xsl:value-of select="job_id"/>
		          						<a>
						     				<xsl:attribute name="href"><xsl:value-of select="$context"/>/services/jobs/job_console.html?uri=<xsl:value-of select="job_id"/></xsl:attribute>
											Console
						     			</a>
					     			</td>
		          				</xsl:if>
		          				<xsl:if test="console">
		          					<td>
		          						<pre>
		          							<xsl:value-of select="console"/>
		          						</pre>
		          					</td>
		          				</xsl:if>
		          				<xsl:if test="token">
		          					<td><xsl:value-of select="token"/></td>
		          				</xsl:if>
		          				<xsl:if test="log">
		          					<td>
		          						<pre>
		          							<xsl:value-of select="log"/>
		          						</pre>
		          					</td>
		          				</xsl:if>
					     	</tr>
				     	</xsl:for-each>
		     		</table>
		     		<br/>
		     		<center>
		     		<a href="javascript:javascript:history.go(-1)">Back</a> ---
		     		<a href="javascript:this.location.reload();">Refresh</a>
		     		</center>
	     		</div>
	     		</td></tr></table>
		 	</body>
	 	</html>
	</xsl:template>

</xsl:stylesheet>