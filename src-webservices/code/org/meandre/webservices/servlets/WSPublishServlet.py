#
# Implements the basic about services
#

__name__ = 'WSAboutServlet'

requestMap = {
    'GET': { 
        'publish': 'publish_publish',    
        'unpublish': 'publish_unpublish'
    }
}

#
# Required imports
#

from org.meandre.core.security import Role

#
# Provides information about the servers version
#

def public_publish ( request, response, format ):
    '''Publish a component/flow stored in this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.PUBLISH) :
        params = extractRequestParamaters(request)
        if 'uri' not in params:
            errorBadRequest(response)
        else :
            content = []
            for uri in params['uri']:
                if meandre_store.publishURI(uri,request.getRemoteUser()) :
                    content.append(uri)
            statusOK(response)
            sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    

def public_unpublish ( request, response, format ):
    '''Unpublish a component/flow available in this 
       instance of the Meandre Server.''' 
    if checkUserRole (request,Role.PUBLISH) :
        params = extractRequestParamaters(request)
        if 'uri' not in params:
            errorBadRequest(response)
        else :
            content = []
            for uri in params['uri']:
                if meandre_store.unpublishURI(uri,request.getRemoteUser()) :
                    content.append(uri)
            statusOK(response)
            sendTJXContent(response,content,format)
    else:
        errorForbidden(response)        
    
    