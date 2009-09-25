#
# Implements the basic about services
#

__name__ = 'WSAuxiliarServlet'

requestMap = {
    'GET': { 
        'add_to_repository': 'auxiliar_add_to_repository',
        'create_user': 'auxiliar_create_user',
        'roles_map': 'auxiliar_roles_map',
        'execute_repository': 'auxiliar_execute_repository',
        'tune_flow': 'auxiliar_tune_flow',
        'show': 'auxiliar_show'
    },
    'POST': {
        'run_tuned_flow': 'auxiliar_run_tuned_flow'
    }
}

#
# Required imports
#

from java.io import ByteArrayOutputStream

from org.meandre.webservices.tools import ServletConfigurableDispatcher
from org.meandre.core.repository import ExecutableComponentInstanceDescription
from org.meandre.core.repository import RepositoryImpl

__header = """
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
                            font-style: italic;
                        }
                        
                        a:hover {
                            color: #cc6000;
                            text-decoration: underline;
                            font-style: italic;
                        }
                        
                        
                        body {
                           color: #444; 
                           background: white; 
                           font-family: Helvetica, Arial, Verdana; 
                           font-size: 11px; 
                        }   
                        
                        form {
                            margin-top:15px;
                            margin-left:15px;
                        } 
                    
                        #main table {
                            width: 100%% ;
                            margin-left:auto;
                            margin-right:auto;
                            border: 1px solid gray;
                            border-collapse: separate;
                            font-size: 12px;
                        }
                        
                        #main th {
                            color: white;
                            background: orange;
                        }    
                        
                        #main th,td {    
                            border: 0px solid gray;
                            padding-left:6px;
                            padding-top:3px;
                            padding-bottom:3px;
                            text-align:left;
                            font-size:11px;
                        }
                    </style>
            </head>
             <body>
             <div id="main">
             <img src="%s/public/resources/system/logo-meandre.gif" /><br/>
    """  
            
    
__footer = """
             </div>
             <br/>
             <center><a href="javascript:javascript:history.go(-1)">Back</a></center>
             </body>
    </html>
    """

__add_to_repository_form = """
        <form enctype="multipart/form-data" method="POST" action="%s/services/repository/add.html" >
        <fieldset>
                <table>
                    <tbody>
                        <tr>
                            <td>
                                <label for="repository">Descriptor:</label>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <label for="context">Resource contexts:</label>
                            </td>
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <label for="embed">Embedded? </label><input type="checkbox" name="embed" value="true" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="overwrite">Overwrite? </label><input type="checkbox" name="overwrite" value="true" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="submit" class="submit" value="Upload" />
                                <input type="reset" class="reset" value="Clear data" />
                            </td>
                        </tr>
                    </tbody>                
                </table>
            </fieldset>
        </form>
    """
    
__add_user_form = """
        <form method="get" action="%s/services/security/create_users.html" >
            <fieldset>
                <table>
                    <tr><td>
                        <label for="username">Username:</label><br/>
                        <input type="text" name="user_name"/><br/>
                        <label for="fullname">Full name:</label><br/>
                        <input type="text" name="user_full_name"/><br/>
                        <label for="password">Password:</label><br/>
                        <input type="password" name="password"/><br/>
                        <input type="submit" class="submit" value="Add user" />
                        <input type="reset" class="reset" value="Clear data" />
                    </td></tr>
                </table>
            </fieldset>
        </form>
    """

__run_repository_form = """
        <form enctype="multipart/form-data" method="POST" action="%s/services/execute/repository.txt" >
        <fieldset>
                <table>
                    <tbody>
                        <tr>
                            <td>
                                <label for="repository">Repository:</label>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <input type="submit" class="submit" value="Execute" />
                                <input type="reset" class="reset" value="Clear data" />
                            </td>
                        </tr>
                    </tbody>                
                </table>
            </fieldset>
        </form>
    """
#
# Services implementation
#

def auxiliar_add_to_repository ( request, response, format ):
    '''Generates for to upload to a repository.''' 
    if checkUserRole (request,Role.REPOSITORY) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header % (meandre_config.appContext) )
            sendRawContent(response,__add_to_repository_form % (meandre_config.appContext))
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)


def auxiliar_create_user ( request, response, format ):
    '''Generates for to add a user for this server.''' 
    if checkUserRole (request,Role.ADMIN) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header % (meandre_config.appContext))
            sendRawContent(response,__add_user_form % (meandre_config.appContext))
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)
        
def auxiliar_roles_map ( request, response, format ):
    '''Generates map of the current users and roles.''' 
    if checkUserRole (request,Role.ADMIN) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header % (meandre_config.appContext))
            sendRawContent(response,'<br/><div id="main"><table><tr><th>User</th><th>Full name</th>')
            roles = Role.getStandardRoles()
            for role in roles :
                sendRawContent(response,'<th>'+role.getShortName()+'</th>')
            sendRawContent(response,'</tr>')
            for user in meandre_security.getUsers():
                user_nick_name = user.getNickName()
                sendRawContent(response,'<tr><td>'+user_nick_name+'</td>')
                sendRawContent(response,'<td>'+user.getName()+'</td>')
                user_roles = meandre_security.getRolesOfUser(user)
                for role in roles:
                    if role in user_roles :
                        sendRawContent(response,('<td><a href="%s/services/security/revoke_roles.html?user_name=' % (meandre_config.appContext))+user_nick_name+'&role_name='+role.getUrl()+'">revoke</a></td>')
                    else:
                        sendRawContent(response,('<td><a href="%s/services/security/grant_roles.html?user_name=' % (meandre_config.appContext))+user_nick_name+'&role_name='+role.getUrl()+'">grant</a></td>')
                sendRawContent(response,'</tr>')
            sendRawContent(response,'</table></div>')
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)
        

def auxiliar_execute_repository ( request, response, format ):
    '''Generates for to upload to a repository.''' 
    if checkUserRole (request,Role.EXECUTION) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header % (meandre_config.appContext))
            sendRawContent(response,__run_repository_form % (meandre_config.appContext))
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)

def __render_basic_metadata ( desc ):
    '''Renders the basic metadata share by components and flows'''
    html  = ''
    html += '<tr><th valign="top"><strong>Name</strong>:</th><td>'+desc.getName()+'</td></tr>'
    html += '<tr><th valign="top"><strong>Description</strong>:</th><td>'+desc.getDescription()+'</td></tr>'
    html += '<tr><th valign="top"><strong>Creator</strong>:</th><td>'+desc.getCreator()+'</td></tr>'
    html += '<tr><th valign="top"><strong>Creation date</strong>:</th><td>'+desc.getCreationDate().toString()+'</td></tr>'
    html += '<tr><th valign="top"><strong>Rights</strong>:</th><td>'+desc.getRights()+'</td></tr>'
    html += '<tr><th valign="top"><strong>Tags</strong>:</th><td>'+desc.getTags().toString()+'</td></tr>'
    return html

def __render_descriptor_download ( uri ):
        '''Displays the download row for the supported formats'''
        html  = '<tr><th>Download</th><td>'
        html += uri+' ('
        html += ('<a href="%s/services/repository/describe.rdf?uri=' % (meandre_config.appContext))+uri+'" target="_blank" title="Get RDF/XML">RDF/XML</a>, '
        html += ('<a href="%s/services/repository/describe.ttl?uri=' % (meandre_config.appContext))+uri+'" target="_blank" title="Get TTL">TTL</a>, '
        html += ('<a href="%s/services/repository/describe.nt?uri=' % (meandre_config.appContext))+uri+'" target="_blank" title="Get N-TRIPLE">N-TRIPLE</a>)'
        html += '</td></tr>'
        return html
    
def __render_flow ( flow_desc, edit, qr ):
        '''Given a flow description it renders the info in html'''
        
        def render_component_instance ( ecid ):
            '''Renders a component instance'''
            html  = '<tr><th valign="top"><strong>URI</strong>:</th><td>'+ecid.getExecutableComponentInstance().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Name</strong>:</th><td>'+ecid.getName()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Description</strong>:</th><td>'+ecid.getDescription()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Component</strong>:</th><td><a href="/services/auxiliar/show.html?uri='+ecid.getExecutableComponent().toString()+'">'+ecid.getExecutableComponent().toString()+'</a></td></tr>'
            props = None
            ecd = qr.getAvailableExecutableComponentDescriptionsMap().get(ecid.getExecutableComponent().getURI())
            props =  ecd.getProperties()
            html += '<tr><th valign="top"><strong>Set properties</strong>:</th><td>'
            html += '<table>'
            for key in props.getKeys() :
                if key.find('wb_')<0 :
                    if edit is False:
                        val = ecid.getProperties().getValue(key)
                        if val is None :
                            html += '<tr><td style="text-align: right;">'+key+' = </td><td>'+props.getValue(key)+'</td></tr>'
                        else :
                            html += '<tr><td style="text-align: right;">'+key+' = </td><td>'+val+'</td></tr>'
                    else:
                        html += '<input type="hidden" name="flow_uri" value="'+flow_desc.getFlowComponent().toString()+'" />'
                        html += '<input type="hidden" name="flow_component_instance" value="'+ecid.getExecutableComponentInstance().toString()+'" />'
                        html += '<input type="hidden" name="property_name" value="'+key+'" />'
                        val = ecid.getProperties().getValue(key)
                        html += '<tr><td style="text-align: right;">'+key+' = </td><td>'
                        if val is None :
                            html += '<input type="text" name="property_value" value="'+ecd.getProperties().getValue(key)+'" size="60" />'
                        else :
                            html += '<input type="text" name="property_value" value="'+val+'" size="60" />'
                        html += '</td></tr>'
            html += '</table>' 
            html += '</td></tr>'            
            return html
        
        def render_component_instance_connector ( cd ):
            '''Renders a connector'''
            html  = '<tr><th valign="top"><strong>URI</strong>:</th><td>'+cd.getConnector().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Source</strong>:</th><td>'+cd.getSourceInstance().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Source port</strong>:</th><td>'+cd.getSourceInstanceDataPort().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Target</strong>:</th><td>'+cd.getTargetInstance().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Target port</strong>:</th><td>'+cd.getTargetInstanceDataPort().toString()+'</td></tr>'  
            return html
        
        html = ''
        if edit is True:
            html += '<form method="POST" action="%s/services/auxiliar/run_tuned_flow.txt">' % (meandre_config.appContext)
            html += '<br/><table><tr><td colspan="2" align="center"><input type="submit" value="Run!" /><input type="reset" value="Reset" /></td></tr>'
        else:
            html += '<br/><table>'
        html += __render_basic_metadata(flow_desc)
        html += __render_descriptor_download(flow_desc.getFlowComponentAsString())
        html += '<tr><th valign="top"><strong>Instances:</strong></th><td>'
        for ecid in flow_desc.getExecutableComponentInstancesOrderedByName() :
            html += '<table>'+render_component_instance(ecid)+'</table><br/>'
        html += '</td></tr>'
        if edit is False :
            html += '<tr><th valign="top"><strong>Connectors:</strong></th><td>'
            for cd in flow_desc.getConnectorDescriptions() :
                html += '<table>'+render_component_instance_connector(cd)+'</table><br/>'
            html += '</td></tr>'
        if edit is True:
            html += '<tr><td colspan="2" align="center"><input type="submit" value="Run!" /><input type="reset" value="Reset" /></td></tr>'
            html += '</table>'
            html += '</form>'
        else:
            html += '</table>'
        return html
           
def auxiliar_tune_flow ( request, response, format ):
    '''Generates for to upload to a repository.''' 
    if checkUserRole (request,Role.EXECUTION) :
        if format=='html' :
            params = extractRequestParamaters(request)
            if 'uri' in params :
                content, flow_uri = getEmptyModel(), params['uri'][0]
                qr = meandre_store.getRepositoryStore(getMeandreUser(request))
                flow_desc = qr.getFlowDescription(content.createResource(flow_uri))
                body = '<br><center>No flow description found for flow uri <code>'+flow_uri+'</code></center>'
                if flow_desc is not None:
                    body  = '<br/><table>'        
                    body += __render_flow(flow_desc,True,qr)  
                    body += '</table>'
                statusOK(response)
                contentAppHTML(response)
                sendRawContent(response,__header % (meandre_config.appContext))
                sendRawContent(response,body)
                sendRawContent(response,__footer)
            else :
                errorExpectationFail(response)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)


def auxiliar_run_tuned_flow ( request, response, format ):
    '''Runs a tuned flow'''
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'flow_uri' in params and 'flow_component_instance' in params and'property_name' in params and 'property_value' in params :
            flow_uri = params['flow_uri'][0]
            model,qr = getEmptyModel(),meandre_store.getRepositoryStore(getMeandreUser(request))
            flow_desc_orig = qr.getFlowDescription(model.createResource(flow_uri))
            flow_desc = RepositoryImpl(flow_desc_orig.getModel()).getAvailableFlowDescriptions().iterator().next()
            for ecidr, key, value in zip(params['flow_component_instance'],params['property_name'],params['property_value']) :
                print ecidr, key, value
                ecid = ExecutableComponentInstanceDescription(flow_desc.getExecutableComponentInstanceDescription(ecidr))
                ecid.getProperties().add(key,value)
                flow_desc.removeExecutableComponentInstance(model.createResource(ecidr))
                flow_desc.addExecutableComponentInstance(ecid)
                print ecid.getProperties()   
            model.add(flow_desc.getModel())
            for ecid in flow_desc.getExecutableComponentInstances() :
                ecd = qr.getExecutableComponentDescription(ecid.getExecutableComponent())
                model.add(ecd.getModel())
            request.setAttribute('repository',model)
            rd = request.getRequestDispatcher('%s/services/execute/repository.txt' % (meandre_config.appContext))
            rd.forward(request, response)
    else:
        errorForbidden(response)
        
    
def auxiliar_show ( request, response, format ):
    '''Renders a flow or a component information.''' 

    def render_component ( comp_desc ):
        '''Given a component description it renders the info in html'''
        
        def render_component_data_port ( dpd ):
            '''Renders a component data port'''
            html  = '<tr><th valign="top"><strong>URI</strong>:</th><td>'+dpd.getResource().toString()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Name</strong>:</th><td>'+dpd.getName()+'</td></tr>'
            html += '<tr><th valign="top"><strong>Description</strong>:</th><td>'+dpd.getDescription()+'</td></tr>'
            return html
         
        def render_property ( prop ):
            '''Renders a component property'''
            html = ''
            for key in  prop.getKeys() :
                html += '<table>'
                html += '<tr><th valign="top"><strong>Name</strong>:</th><td>'+key+'</td></tr>'
                html += '<tr><th valign="top"><strong>Description</strong>:</th><td>'+prop.getDescription(key)+'</td></tr>'
                html += '<tr><th valign="top"><strong>Default</strong>:</th><td>'+prop.getValue(key)+'</td></tr>'
                html += '</table><br/>'
            return html
            
        html  = '<br/><table>'
        html += __render_basic_metadata(comp_desc)
        html += __render_descriptor_download(comp_desc.getExecutableComponentAsString())
        html += '<tr><th valign="top"><strong>Runnable</strong>:</th><td>'+comp_desc.getRunnable()+'</td></tr>'
        html += '<tr><th valign="top"><strong>Format</strong>:</th><td>'+comp_desc.getFormat()+'</td></tr>'
        html += '<tr><th valign="top"><strong>Firing policy</strong>:</th><td>'+comp_desc.getFiringPolicy()+'</td></tr>'
        html += '<tr><th valign="top"><strong>Context</strong>:</th><td>'
        for context in comp_desc.getContext() :
            html += context.toString()+'<br/>'
        html += '</td></tr>' 
        html += '<tr><th valign="top"><strong>Properties:</strong></th><td>'+render_property(comp_desc.getProperties())+'</td></tr>'
        html += '<tr><th valign="top"><strong>Inputs:</strong></th><td>'
        for dpd in comp_desc.getInputs() :
            html += '<table>'+render_component_data_port(dpd)+'</table><br/>'
        html += '</td></tr>'
        html += '<tr><th valign="top"><strong>Outputs:</strong></th><td>'
        for dpd in comp_desc.getOutputs() :
            html += '<table>'+render_component_data_port(dpd)+'</table><br/>'
        html += '</td></tr>'
        html += '</table>'  
        return html
      
    
    if checkUserRole (request,Role.REPOSITORY) or checkUserRole (request,Role.FLOW) or checkUserRole (request,Role.COMPONENT) :
        if format=='html' :
            params = extractRequestParamaters(request)
            if 'uri' in params :
                statusOK(response)
                contentAppHTML(response)
                sendRawContent(response,__header % (meandre_config.appContext))
                for uri in params['uri']:
                    content = getEmptyModel()
                    qr = meandre_store.getRepositoryStore(getMeandreUser(request))
                    comp_desc = qr.getExecutableComponentDescription(content.createResource(uri))
                    flow_desc = qr.getFlowDescription(content.createResource(uri))
                    if comp_desc is not None:
                        sendRawContent(response,render_component(comp_desc))
                    if flow_desc is not None:
                        sendRawContent(response,__render_flow(flow_desc,False,qr))
                sendRawContent(response,__footer)
            else :
                errorExpectationFail(response)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)



    