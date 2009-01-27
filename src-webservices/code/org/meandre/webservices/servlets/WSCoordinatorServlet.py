#
# Implements the basic about services
#

__name__ = 'WSCoordinatorServlet'

requestMap = {
    'GET': { 
        'log': 'coordinator_log',
        'status': 'coordinator_status',
        'info': 'coordinator_info',
        'property': 'coordinator_property'
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
        if 'limit' in params : limit = int(params['limit'][0])
        content, logs = [], meandre_coordinator.getLogs(limit)
        for log_entry in logs :
            map = {}
            for k in log_entry.keySet() :
                map[k] = log_entry.get(k)
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
 
    
def coordinator_status ( request, response, format ):
    '''Returms the status of the clustered server.'''
    if checkUserRole (request,Role.ADMIN) :
        content, statuses = [], meandre_coordinator.getStatuses()
        for status_entry in statuses :
            map = {}
            for k in status_entry.keySet() :
                map[k] = status_entry.get(k)
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
 
   
def coordinator_info ( request, response, format ):
    '''Returns the info of the clustered server.'''
    if checkUserRole (request,Role.ADMIN) :
        content, infos = [], meandre_coordinator.getInfos()
        for info_entry in infos :
            map = {}
            for k in info_entry.keySet() :
                map[k] = info_entry.get(k)
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)

  
def coordinator_property ( request, response, format ):
    '''Returns the properties for a server of the clustered server.'''
    if checkUserRole (request,Role.ADMIN) :
        content, properties = [], meandre_coordinator.getServerProperties()
        for prop_key in properties.keySet() :
            map = {'property_key':prop_key, 'property_value':properties.get(prop_key)}
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
 
    