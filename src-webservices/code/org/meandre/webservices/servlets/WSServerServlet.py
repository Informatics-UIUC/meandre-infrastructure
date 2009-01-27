#
# Implements the basic server services
#

__name__ = 'WSServerServlet'

requestMap = {
    'GET': { 
        'shutdown': 'server_shutdown'       
    }
}

#
# Required imports
#

from org.meandre.core.security import Role

#
# Services implementation
#

def server_shutdown ( request, response, format ):
    '''Shuts down the current Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        content = [{'message':'Server shutting down now!!!'}]    
        statusOK(response)
        sendTJXContent(response,content,format)
        meandre_server.delayedStop(1000)
    else:
        errorForbidden(response)

