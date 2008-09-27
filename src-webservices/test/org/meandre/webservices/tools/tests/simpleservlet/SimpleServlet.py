#
# The basic dispatching dictionary
#
requestMap = {
    'GET': { 
        'ping': 'ping_pong', 
        'array': 'get_array_info',
        'get_rdf': 'demo_rdf_repository'
    }
}

#
# The ping pong method
#
def ping_pong ( request, response, format ):
    content = [ 'pong' ]
    statusOK(response)
    sendTJXContent(response,content,format)

#
# The ping pong method
#
def get_array_info ( request, response, format ):
    content = [ 'value'+str(i) for i in range(10) ]
    statusOK(response)
    sendTJXContent(response,content,format)
     
#
# The demo rdf repository
#
from org.meandre.demo.repository import DemoRepositoryGenerator

def demo_rdf_repository ( request, response, format ):
    content = DemoRepositoryGenerator.getTestHelloWorldRepository()
    statusOK(response)
    sendRDFModel(response,content,format)
    
    
