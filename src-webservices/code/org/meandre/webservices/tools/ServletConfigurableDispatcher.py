#
# The needed imports
#

from javax.servlet.http import HttpServletResponse

from org.json import JSONArray,JSONObject,XML

from org.meandre.webservices.logger import WSLoggerFactory
from org.meandre.core.store import Store

from com.hp.hpl.jena.vocabulary import DC,RDF,RDFS,XSD

from javax.xml.transform import Source,Transformer,TransformerFactory
from javax.xml.transform.stream import StreamResult,StreamSource
from java.io import StringReader

from org.meandre.webservices.tools import ServletConfigurableDispatcher

#
# The basic dispatching dictionary
#
requestMap = {
}

log = WSLoggerFactory.getWSLogger()

#
# Prepare the XSLT information
#
__xsltFile = ServletConfigurableDispatcher.getSimpleName()+".xslt"
__xsltSource = StreamSource(ServletConfigurableDispatcher.getResourceAsStream(__xsltFile));
__xslTransFact = TransformerFactory.newInstance()
__xslTrans = __xslTransFact.newTransformer(__xsltSource)
#
# The main dispatch method
#
def dispatch(httpMethod,request,response,method,format):
    '''Dispatches the request using the provided map. The request map is a basic
       dictionary with the basic HTTP methods (GET, POST, etc.). Then for each of
       them another dictionary provide the translation from the method name used
       on the URL request into the python method implementing the request. This
       method is invoked by the Java servlet dispatcher implementation as and entry
       point to the service dispatching.'''
    if httpMethod not in requestMap:
        errorUnauthorized(response)
    elif method in requestMap[httpMethod]:
        user = request.getRemoteUser()
        query = request.getQueryString()
        if user is None : user="anonymous"
        if query is None : 
            query=' '
        else :
            query = ' <-- '+query
        log.info(request.getRemoteAddr()+':'+str(request.getRemotePort())+'/'+user+' --> '+httpMethod+' --> '+request.getRequestURL().toString()+query)
        eval(requestMap[httpMethod][method]+'(request,response,format)')
    else :
        errorNotFound(response)
        
#
# Status and basic error functions
#

def statusOK ( response ) :
    '''Responses with and OK code
    
       statusOK ( response )'''
    response.setStatus(HttpServletResponse.SC_OK)
   
def errorUnauthorized ( response ): 
    '''Responses with an unauthorized code
    
       errorUnauthorized ( response )'''
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
      
def errorNotFound ( response ): 
    '''Responses with a not found error code
    
       errorUnauthorized ( response )'''
    response.sendError(HttpServletResponse.SC_NOT_FOUND)
 
def errorForbidden ( response ):
    '''Responses with a forbidden error code
    
       errorForbidden ( response )'''
    response.sendError(HttpServletResponse.SC_FORBIDDEN)   

def errorBadRequest ( response ):
    '''Responses with a bad request code
    
       errorBadRequest ( response )'''
    response.sendError(HttpServletResponse.SC_BAD_REQUEST)   

def errorExpectationFail ( response ):
    '''Responses with a expectation fail code
    
       errorExpectationFail ( response )'''
    response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED)  
#
# Content response function
#
def contentTextPlain ( response ):
    '''Sets the response type to plain text
    
       contentTextPlain ( response )'''
    response.setContentType("text/plain")
     
def contentAppJSON ( response ):
    '''Sets the response type to json text
       
       contentAppJSON ( response )'''
    response.setContentType("application/json")
    
def contentAppXML ( response ):
    '''Sets the response type to xml
     
       contentAppXML ( response )'''
    response.setContentType("application/xml")
   
#   
def contentAppHTML ( response ):
    '''Sets the response type to xml
     
       contentAppHTML ( response )'''
    response.setContentType("text/html")
   
#
# Response functions
#
def sendRawContent ( response, content ):
    '''Pushes the raw content provided to the provided response
      
       sendRawContent ( response, content )'''
    response.getWriter().print(content)
    
def __content_to_JSON__ (content) :
    '''Returns the content into JSON. The method process basic types list and 
       dictionaries providing the proper JSON equivalent translation.
       
       __content_to_JSON__ (content)'''
    if isinstance(content,list) :
        ja = JSONArray()
        for c in content:
            ja.put(__content_to_JSON__(c))
        return ja
    elif isinstance(content,dict) :
        jo = JSONObject()
        for k,c in content.items():
            jo.put(k,__content_to_JSON__(c))
        return jo
    else :
        return content
    
def __content_to_TXT__(content,tab):
    '''Returns the content into plain text. The method process basic types list and 
       dictionaries providing the proper text equivalent translation. The basic 
       assumptions are:
         (1) The list are separated by a new line character
         (2) Each nesting level is indicated by a tab
         (3) Dictionaries are expressed as properties key = value (one per line)
       
       __content_to_TXT__ (content)'''
    res = ''
    if isinstance(content,list) :
        for c in content:
            res += __content_to_TXT__(c,tab+'\t')+'\n'
        return res
    elif isinstance(content,dict) :
        for k,c in content.items():
            res += tab+k+' = '+__content_to_TXT__(c,tab+'\t')+'\n'
        return res
    else :
        return content
        

#
# Load the processor for XSLs and the stylesheets
#



def sendTJXContent ( response, content, format ):
    '''Response the content with the proper format. It supports three response
       content format: txt, json, and xml. XML responses follow the following 
       syntax:
       
           <meandre_response>
               <meandre_item>
                   ....
               </meandre_item>
               ...
               <meandre_item>
                   ....
               </meandre_item>
           </meandre_response>
           
        Where each meandre_item entry can be a simple text, another set of 
        meandre_items, or tags identifying the keys of a dictionary. It also
        allows the html format, which is only an style sheet transformation of
        the XML response
        
        sendTJXContent ( response, content, format )'''
    if format=='txt':
        contentTextPlain(response)
        txt = __content_to_TXT__(content,'')
        sendRawContent(response, txt)
    elif format=='json' :
        contentAppJSON(response)
        jc = __content_to_JSON__(content)
        sendRawContent(response, jc.toString())
    elif format=='xml' :
        contentAppXML(response)
        jc = __content_to_JSON__(content)
        xmlc = XML.toString(jc,"meandre_item")
        sendRawContent(response, '<?xml version="1.0" encoding="UTF-8"?><meandre_response>')
        sendRawContent(response, xmlc)
        sendRawContent(response, '</meandre_response>')
    elif format=='html' :
        contentAppHTML(response)
        xmlc = '<?xml version="1.0" encoding="UTF-8"?><meandre_response>'
        xmlc += XML.toString(__content_to_JSON__(content),"meandre_item")
        xmlc += '</meandre_response>'
        xmlSource = StreamSource(StringReader(xmlc))
        result = StreamResult(response.getOutputStream())
        __xslTrans.transform(xmlSource, result);
    else:
        errorNotFound(response)
        
def sendRDFModel ( response, model, format ) :
    ''' Given a model, it serializes it to the response with the proper requested 
        serialization format. The current supported formats are RDF-XML, TTL, and 
        N-TRIPLE.
        
        sendRDFModel ( response, model, format )'''
    
    if format=='rdf' :
        contentAppXML(response)
        model.write(response.getOutputStream(),'RDF/XML-ABBREV')
    elif format=='ttl' :
        contentTextPlain(response)
        model.write(response.getOutputStream(),'TTL')
    elif format=='nt' :
        contentTextPlain(response)
        model.write(response.getOutputStream(),'N-TRIPLE')
    else:
        errorNotFound(response)
        
#
# Request parameters manipulation methods
#
def __dictionary_from_get_request__ ( request ):
    '''Extracts the parameters from a simple GET request into a dictionary.
    
       __dictionary_from_get_request__ ( request )'''
    dictParams = {}
    if request is not None:
        parameterNames = request.getParameterNames() 
        for name in parameterNames :
            requestValues = request.getParameterValues(name)
            values = []
            for value in requestValues:
                values.append(value)
            dictParams[name] = values
    return dictParams

def extractRequestParamaters ( request ):
    '''Given a request, it extracts the parameters and values provided 
       creating and returning a dicctionary containing them.   
       
       extractRequestParamaters ( request )'''
    method = request.getMethod()
    if method=="GET" or method=="get" or method=="POST" or method=="post" :
        return __dictionary_from_get_request__(request)
    else :
        log.warning("Request "+method+" method not support for parameter extraction")
        return {} 