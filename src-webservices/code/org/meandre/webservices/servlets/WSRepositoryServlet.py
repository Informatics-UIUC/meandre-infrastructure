#
# Implements the basic about services
#

__name__ = 'WSAboutServlet'

requestMap = {
    'GET': { 
        'dump': 'repository_dump',    
        'regenerate': 'repository_regenerate',    
        'list_components': 'repository_list_components',    
        'list_flows': 'repository_list_flows',    
        'tags': 'repository_tags',    
        'tags_components': 'repository_tags_components',    
        'tags_flows': 'repository_tags_flows',    
        'components_by_tag': 'repository_components_by_tag',    
        'flows_by_tag': 'repository_flows_by_tag',    
        'describe_component': 'repository_describe_component',    
        'describe_all_components': 'repository_describe_all_components',    
        'describe_flow': 'repository_describe_flow',    
        'describe_all_flows': 'repository_describe_all_flows',    
        'search_components': 'repository_search_components',    
        'search_flows': 'repository_search_flows',    
        'remove': 'repository_remove'
    },
    'POST': { 
        'add': 'repository_add',    
        'add_flow_description': 'repository_add_flow_descriptions' 
    }
}

#
# Required imports
#



#
# Services implementation
#

def repository_dump ( request, response, format ):
    '''Returns the user repository stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        content = meandre_store.getRepositoryStore(getMeandreUser(request)).getModel()
        statusOK(response)
        sendRDFModel(response,content,format)
    else:
        errorForbidden(response)
        

def repository_regenerate ( request, response, format ):
    '''Regenerates the user repository stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        meandre_store.regenerateRepository(getMeandreUser(request))
        content = {'message':'Repository successfully regenerated'}
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    
    
def repository_list_components ( request, response, format ):
    '''List the components aggregated in the user repository stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        content, order, limit, ordered = [], 'name', -1, 0
        if 'order' in params :
            ordered = 1
            if params['order'][0]=='date' :
                order = 'date'
        if 'limit' in params :
            try:
                ordered = 1
                limit = int(params['limit'][0])
            except :
                log.warning('Wrong limit format '+str(params['limit'][0])+'. Defaulting to unlimited')
        qr, resources = meandre_store.getRepositoryStore(getMeandreUser(request)), None
        if ordered :
            resources = qr.getAvailableExecutableComponentsOrderedBy(order,limit)            
        else:
            resources = qr.getAvailableExecutableComponents()
        for resource in resources:
            content.append({'meandre_uri':resource.toString()})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    
     
def repository_list_flows ( request, response, format ):
    '''List the flows aggregated in the user repository stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        content, order, limit, ordered = [], 'name', -1, 0
        if 'order' in params :
            ordered = 1
            if params['order'][0]=='date' :
                order = 'date'
        if 'limit' in params :
            try:
                ordered = 1
                limit = int(params['limit'][0])
            except :
                log.warning('Wrong limit format '+str(params['limit'][0])+'. Defaulting to unlimited')
        qr, resources = meandre_store.getRepositoryStore(getMeandreUser(request)), None
        if ordered :
            resources = qr.getAvailableFlowsOrderedBy(order,limit)            
        else:
            resources = qr.getAvailableFlows()
        for resource in resources:
            content.append({'meandre_uri':resource.toString()})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    
def repository_tags ( request, response, format ):
    '''Returns the tags stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        qr = meandre_store.getRepositoryStore(getMeandreUser(request))
        content = []
        for tag in qr.getTags():
            content.append({'meandre_tag':tag})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
  