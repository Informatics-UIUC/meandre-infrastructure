#
# Implements the basic about services
#

__name__ = 'WSPublishServlet'

requestMap = {
    'GET': { 
        'publish': 'publish_publish',
        'list_published': 'publish_list_published',    
        'unpublish': 'publish_unpublish'
        }
}

#
# Required imports
#

from org.meandre.core.security import Role

#
# Services implementation
#

def publish_publish ( request, response, format ):
    '''Publish a component/flow stored in this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.PUBLISH) :
        params = extractRequestParamaters(request)
        if 'uri' not in params:
            errorExpectationFail(response)
        else :
            content = []
            for uri in params['uri']:
                if meandre_store.publishURI(uri,getMeandreUser(request)) :
                    content.append({'meandre_uri':uri})
            statusOK(response)
            sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    

def publish_unpublish ( request, response, format ):
    '''Unpublish a component/flow available in this 
       instance of the Meandre Server.''' 
    if checkUserRole (request,Role.PUBLISH) :
        params = extractRequestParamaters(request)
        if 'uri' not in params:
            errorExpectationFail(response)
        else :
            content = []
            for uri in params['uri']:
                if meandre_store.unpublishURI(uri,getMeandreUser(request)) :
                    content.append({'meandre_uri':uri})
            statusOK(response)
            sendTJXContent(response,content,format)
    else:
        errorForbidden(response)        
    
    
def publish_list_published ( request, response, format ):
    '''List the uro of published components/flows in this 
       instance of the Meandre Server.''' 
    if checkUserRole (request,Role.PUBLISH) :
        content = []
        for uri in meandre_store.getPublishedComponentsAndFlows() :
            content.append({'meandre_uri':uri})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)        
    
    