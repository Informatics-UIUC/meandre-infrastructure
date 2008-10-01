#
# Implements the basic about services
#

__name__ = 'WSAboutServlet'

requestMap = {
    'GET': { 
        'flow': 'execute_flow',    
        'list_running_flows': 'execute_list_running_flows' ,
        'url': 'execute_url',
        'web_component_url': 'execute_web_component_url',
        'uri_flow': 'execute_uri_flow'
    }
}

#
# The token junk
#
executionTokenMap = {
}

#
# Required imports
#

from java.lang import System
from java.lang import Boolean

from org.meandre.core.security import Role
from org.meandre.core.engine.execution import InteractiveExecution
from org.meandre.webui import WebUIFactory
from org.meandre.webservices.beans import JobDetail

#
# Services implementation
#

def execute_flow ( request, response, format ):
    '''Executes a flow interactively on the Server.'''
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            uris,tokens,stats = params['uri'],[], []
            if 'token' not in params:
                tokens = [str(System.currentTimeMillis()) for i in range(len(uris))]
            if 'statistics' not in params:
                stats = [Boolean('false') for i in range(len(uris))]
            content = []
            for flow_uri, stat, token in zip(uris,stats,tokens): 
                statusOK(response)
                qr = meandre_store.getRepositoryStore(getMeandreUser(request))
                job = JobDetail()
                executionTokenMap[token] = job
                if format == 'txt' :
                    InteractiveExecution.executeVerboseFlowURI(qr,flow_uri,response.getOutputStream(),meandre_config,stat,token,job)
                elif format == 'silent': 
                    InteractiveExecution.executeSilentFlowURI(qr,flow_uri,response.getOutputStream(),meandre_config,token,job)
                else :
                    errorNotFound(response)
                del executionTokenMap[token]
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)

 
def execute_list_running_flows ( request, response, format ):
    '''Returns the list of interactive flow currently being run on the Server.'''
    if checkUserRole (request,Role.EXECUTION) :
        content = []
        for flow_uri in WebUIFactory.getFlows() :
            webui = WebUIFactory.getExistingWebUI(flow_uri)
            if ( webui is not None ) :
                content.append( {
                        'flow_instance_uri': flow_uri,
                        'flow_instance_webui_uri': 'http://'+getHostName()+':'+str(webui.getPort())+'/'
                    })
        statusOK(response)
        sendTJXContent(response,[content],format)
    else:
        errorForbidden(response)
 
def execute_url ( request, response, format ):
    '''Returns the webUI URL for an interactive flow currently being run on the 
       Server.'''
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            uris = params['uri']
            content = []
            for flow_uri in uris: 
                webui = WebUIFactory.getExistingWebUI(flow_uri)
                if webui is not None :
                    content.append( {
                            'flow_instance_uri': flow_uri,
                            'flow_instance_webui_uri': 'http://'+getHostName()+':'+str(webui.getPort())+'/'
                        })
            statusOK(response)
            sendTJXContent(response,[content],format)
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)
 
 
def execute_web_component_url ( request, response, format ):
    '''Returns the webfragments url for andinteractive flow currently being run on the 
       Server.'''
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            uris = params['uri']
            content = []
            for flow_uri in uris: 
                webui = WebUIFactory.getExistingWebUI(flow_uri)
                if webui is not None :
                    host_url = 'http://'+getHostName()+':'+str(webui.getPort())+'/'
                    fragments = webuiTmp.getWebUIDispatcher().getLstHandlers()
                    for fragment in fragments:
                        content.append( {
                            'flow_instance_uri': flow_uri,
                            'flow_instance_fragment_url': host_url+wfrag.getFragmentID()
                        })
            statusOK(response)
            sendTJXContent(response,[content],format)
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)
 
  
def execute_uri_flow ( request, response, format ):
    '''Returns the webUI URL for an interactive flow currently which has been
       assigned to a given token being run on the Server.'''
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'token' in params :
            tokens = params['token']
            content = []
            for token in tokens: 
                if token in executionTokenMap :
                    job = executionTokenMap[token]
                    job_info = {
                            'port': job.getPort(),
                            'hostname': job.getHostname(),
                            'token': job.getToken(),
                            'uri': job.getFlowInstanceId()
                        }
                    content.append(job_info)
            statusOK(response)
            sendTJXContent(response,[content],format)
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)
 
    