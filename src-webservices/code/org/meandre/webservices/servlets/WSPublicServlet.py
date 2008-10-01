#
# Implements the basic about services
#

__name__ = 'WSPublicServlet'

requestMap = {
    'GET': { 
        'repository': 'public_repository',    
        'demo_repository': 'public_demo_repository'
    }
}

#
# Required imports
#

from org.meandre.demo.repository import DemoRepositoryGenerator

#
# Services implementation
#

def public_repository ( request, response, format ):
    '''Returns the user repository stored in this instance of the 
       Meandre Server.''' 
    content = meandre_store.getPublicRepositoryStore()
    statusOK(response)
    sendRDFModel(response,content,format)
    

def public_demo_repository ( request, response, format ):
    '''Returns a simple hello world demo repository available in this 
       instance of the Meandre Server.''' 
    content = DemoRepositoryGenerator.getTestHelloWorldMoreHetereogenousRepository()
    statusOK(response)
    sendRDFModel(response,content,format)
    
    