#
# Implements the basic about services
#

__name__ = 'WSLocationsServlet'

requestMap = {
    'GET': { 
        'list': 'locations_list',    
        'add': 'locations_add',    
        'remove': 'locations_remove'
    }
}

#
# Required imports
#

from org.meandre.core.security import Role
from org.meandre.core.store.system import SystemStore

#
# Services implementation
#

def locations_list ( request, response, format ):
    '''List the list of locations used by the user of this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.REPOSITORY) :
        system = meandre_store.getSystemStore(meandre_config,getMeandreUser(request))
        locations = system.getProperty(SystemStore.REPOSITORY_LOCATION)
        content = []
        for location in locations:
            location_info = {
                    'location': location.get("value"),
                    'description': location.get("description")
                }
            content.append(location_info)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)       
    

def locations_add ( request, response, format ):
    '''Add a location to the list of locations used by the user of this 
       instance of the Meandre Server.''' 
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if ('location' in  params) and ('description' in params ) :
            uris = params['location']
            descs = params['description']
            content = []
            for uri,dsc in zip(uris,descs) :
                if ( meandre_store.addLocation(getMeandreUser(request), uri, dsc, meandre_config) ) :
                    content.append({
                            'location': uri,
                            'description': dsc
                        })
                else:
                    content.append({'message':'ERROR: Could not add location '+uri})
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)        


def locations_remove ( request, response, format ):
    '''Remove a location form the list of locations used by the user of 
       this instance of the Meandre Server.''' 
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'location' in  params :
            uris = params['location']
            content = []
            for uri in uris: 
                if ( meandre_store.removeLocation(getMeandreUser(request), uri, meandre_config) ) :
                    content.append({'location': uri})
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)        
    
    