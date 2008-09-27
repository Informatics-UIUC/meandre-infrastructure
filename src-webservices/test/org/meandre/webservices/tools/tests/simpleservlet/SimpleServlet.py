#
# The basic dispatching dictionary
#
requestMap = {
    'GET': { 
        'ping': 'ping_pong', 
        'demo': 'demo_rdf'
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
# The demo rdf repository
#
from org.meandre.demo.repository import DemoRepositoryGenerator

def demo_rdf ( request, response, format ):
    content = DemoRepositoryGenerator.getTestHelloWorldRepository()
    statusOK(response)
    sendRDFModel(response,content,format)