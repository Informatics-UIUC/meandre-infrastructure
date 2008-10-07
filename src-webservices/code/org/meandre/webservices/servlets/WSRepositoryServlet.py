#
# Implements the basic about services
#

__name__ = 'WSRepositoryServlet'

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

from com.hp.hpl.jena.rdf.model import Resource

from org.meandre.core.repository import RepositoryImpl

from org.meandre.webservices.servlets import WSRepositoryServlet

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
        sendTJXContent(response,[content],format)
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
   
   
def repository_tags_components ( request, response, format ):
    '''Returns the component tags stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        qr = meandre_store.getRepositoryStore(getMeandreUser(request))
        content = []
        for tag in qr.getComponentTags():
            content.append({'meandre_tag':tag})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
  
  
def repository_tags_flows ( request, response, format ):
    '''Returns the component tags stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        qr = meandre_store.getRepositoryStore(getMeandreUser(request))
        content = []
        for tag in qr.getFlowTags():
            content.append({'meandre_tag':tag})
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
  
def repository_components_by_tag ( request, response, format ):
    '''List the components aggregated in the user repository that match
       the provided tags stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'tag' in params :
            content = []
            qr = meandre_store.getRepositoryStore(getMeandreUser(request))
            for tag in params['tag'] :
                components = qr.getComponentsByTag(tag) 
                if components is not None :
                    for component in components :
                         content.append({
                                'meandre_uri': component.getExecutableComponent().toString()
                            })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)
        
  
def repository_flows_by_tag ( request, response, format ):
    '''List the flows aggregated in the user repository that match
       the provided tags stored in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'tag' in params :
            content = []
            qr = meandre_store.getRepositoryStore(getMeandreUser(request))
            for tag in params['tag'] :
                flows = qr.getFlowsByTag(tag)
                if flows is not None : 
                    for component in flows :
                        content.append({
                            'meandre_uri': component.getFlowComponent().toString()
                            })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)
        
    
def repository_describe_all_components ( request, response, format ):
    '''Returns all the component descriptions  aggregated for the requesting user in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        qr = meandre_store.getRepositoryStore(getMeandreUser(request))
        content = getEmptyModel()
        for component_desc in qr.getAvailableExecutableComponentDescriptions():
            content.add(component_desc.getModel())
        statusOK(response)
        sendRDFModel(response,content,format)
    else:
        errorForbidden(response)

   
def repository_describe_all_flows ( request, response, format ):
    '''Returns all the flow descriptions aggregated for the requesting user in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        qr = meandre_store.getRepositoryStore(getMeandreUser(request))
        content = getEmptyModel()
        for component_desc in qr.getAvailableFlowDescriptions():
            content.add(component_desc.getModel())
        statusOK(response)
        sendRDFModel(response,content,format)
    else:
        errorForbidden(response)
   
  
def repository_describe_component ( request, response, format ):
    '''Returns all the components aggregated for the requesting user in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            qr = meandre_store.getRepositoryStore(getMeandreUser(request))
            content = getEmptyModel()
            for component_uri in params['uri']:
                component_desc = qr.getExecutableComponentDescription(content.createResource(component_uri))
                if component_desc is not None:
                    content.add(component_desc.getModel())
            statusOK(response)
            sendRDFModel(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)

  
def repository_describe_flow ( request, response, format ):
    '''Returns all the flows aggregated for the requesting user in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            qr = meandre_store.getRepositoryStore(getMeandreUser(request))
            content = getEmptyModel()
            for flow_uri in params['uri']:
                flow_desc = qr.getFlowDescription(content.createResource(flow_uri))
                if flow_desc is not None:
                    content.add(flow_desc.getModel())
            statusOK(response)
            sendRDFModel(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)

def repository_remove ( request, response, format ):
    '''Remove the associated uri element from the aggregated repository
       for the requesting user in the current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            qr = meandre_store.getRepositoryStore(getMeandreUser(request))
            user_model = qr.getModel()
            content = []
            for uri in params['uri']:
                resource_uri = user_model.createResource(uri)
                component_desc = qr.getExecutableComponentDescription(resource_uri)
                flow_desc = qr.getFlowDescription(resource_uri)
                model_to_remove = None
                if component_desc is not None :
                    model_to_remove = component_desc.getModel()
                    content.append({'meandre_uri':uri})
                elif flow_desc is not None :
                    model_to_remove = flow_desc.getModel()
                    content.append({'meandre_uri':uri})
                else :
                    model_to_remove = getEmptyModel()
                user_model.begin()
                user_model.remove(model_to_remove)
                user_model.commit()
            qr.refreshCache()
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)  
    
    
def repository_search_components ( request, response, format ):
    '''Search the components aggregated in the user repository stored in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'q' in params:
            content, order, limit, ordered, q = [], 'name', -1, 0, params['q'][0]
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
            resources = qr.getAvailableExecutableComponents(q)
            if ordered :
                model = getEmptyModel()
                for resource in resources:
                    model.add(qr.getExecutableComponentDescription(resource).getModel())
                qrNew = RepositoryImpl(model)
                resources = qrNew.getAvailableExecutableComponentsOrderedBy(order,limit)            
            
            for resource in resources:
                content.append({'meandre_uri':resource.toString()})
            statusOK(response)
            sendTJXContent(response,content,format) 
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)

    
def repository_search_flows ( request, response, format ):
    '''Search the components aggregated in the user repository stored in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'q' in params:
            content, order, limit, ordered, q = [], 'name', -1, 0, params['q'][0]
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
            resources = qr.getAvailableFlows(q)
            if ordered :
                model = getEmptyModel()
                for resource in resources:
                    model.add(qr.getFlowDescription(resource).getModel())
                qrNew = RepositoryImpl(model)
                resources = qrNew.getAvailableFlowsOrderedBy(order,limit)            
            
            for resource in resources:
                content.append({'meandre_uri':resource.toString()})
            statusOK(response)
            sendTJXContent(response,content,format) 
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)
   
        
def repository_add_flow_descriptions ( request, response, format ):
    '''Add a flow descriptor the user repository stored in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        params = extractRequestParamaters(request)
        if 'repository' in params:
            repositories = params['repository']
            overwrites = params['overwrite']
            diff = len(repositories)-len(overwrites)
            if diff>0 :
                overwrite.append(['false' for i in range(diff)])
            user = getMeandreUser(request)
            content = []
            for repository,overwrite in zip(repositories,overwrites) :
                uris = meandre_store.addFlowsToRepository(user, repository, overwrite=='true')
                for uri in uris :
                    content.append({'meandre_uri':uri})
            statusOK(response)
            sendTJXContent(response,content,format) 
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)
  
def repository_add ( request, response, format ):
    '''Add components and flows to  the user repository stored in the 
       current Meandre server.'''
    if checkUserRole (request,Role.REPOSITORY) :
        uris = WSRepositoryServlet.addToRepository(request,meandre_store,meandre_config,format)
        content = []
        for uri in uris :
            content.append({'meandre_uri':uri})
        statusOK(response)
        sendTJXContent(response,content,format) 
    else:
        errorForbidden(response) 
    
    