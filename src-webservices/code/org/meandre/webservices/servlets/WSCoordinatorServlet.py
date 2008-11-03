#
# Implements the basic about services
#

__name__ = 'WSCoordinatorServlet'

requestMap = {
    'GET': { 
        'log': 'coordinator_log'
    }
}


#
# Required imports
#


#
# Services implementation
#

def coordinator_log ( request, response, format ):
    '''Returms the logs of the clustered server.'''
    if checkUserRole (request,Role.ADMIN) :
        limit, params = 0, extractRequestParamaters(request)
        if 'limit' in params : limit = params['limit']
        content, logs = [], meandre_coordinator.getLogs()
        for log_entry in logs :
            map = {}
            for k in log_entry.keySet() :
                map[k] = log_entry.get(k)
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
 
    