#
# The basic dispatching dictionary
#

__name__ = 'SimpleServlet'

requestMap = {
    'GET': { 
        'ping': 'ping_pong', 
        'array': 'get_array_info',
        'dictionary': 'get_dictionary_info',
        'get_rdf': 'demo_rdf_repository',
        'request_echo': 'reposonse_request_parameters'
    }
}

#
# The ping pong method
#
def ping_pong ( request, response, format ):
    '''Given a request, just response "pong".'''
    content = [ 'pong' ]
    statusOK(response)
    sendTJXContent(response,content,format,request.getRemoteUser())

#
# Returns an array of information 
#
def get_array_info ( request, response, format ):
    '''Given a request, returns and array of ten entries of the for valueX,
       where X is a number in the range from 0 to 9 (inclusive).'''
    content = [ 'value'+str(i) for i in range(10) ]
    statusOK(response)
    sendTJXContent(response,content,format,request.getRemoteUser())

#
# Returns a dictionary of information
#     
def get_dictionary_info ( request, response, format ):
    '''Returns an example dictionary with two keys: "name" and "method".'''
    content = {
            "name": __name__,
            "method": "get_dictionary_info"
        }
    statusOK(response)
    sendTJXContent(response,[content],format,request.getRemoteUser())
    
#
# The demo rdf repository
#
from org.meandre.demo.repository import DemoRepositoryGenerator

def demo_rdf_repository ( request, response, format ):
    '''Response with a hello world repository'''
    content = DemoRepositoryGenerator.getTestHelloWorldRepository()
    statusOK(response)
    sendRDFModel(response,content,format)
    
#
# The echo request
#
def reposonse_request_parameters ( request, response, format ):
    '''Given a get request, returns a dictionary with the parameters and
       values provided as parameters to the request call.'''
    content = extractRequestParamaters(request)
    statusOK(response)
    sendTJXContent(response,[content],format,request.getRemoteUser())
