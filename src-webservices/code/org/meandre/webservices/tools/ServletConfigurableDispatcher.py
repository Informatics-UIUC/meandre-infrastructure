#
# The needed imports
#
from javax.servlet.http import HttpServletResponse

from org.json import JSONArray
from org.json import JSONObject
from org.json import XML

from org.meandre.webservices.logger import WSLoggerFactory

from org.meandre.core.store import Store

from com.hp.hpl.jena.vocabulary import DC
from com.hp.hpl.jena.vocabulary import RDF
from com.hp.hpl.jena.vocabulary import RDFS
from com.hp.hpl.jena.vocabulary import XSD

#
# The basic dispatching dictionary
#
requestMap = {
}

log = WSLoggerFactory.getWSLogger()

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
# Response functions
#
def sendRawContent ( response, content ):
    '''Pushes the raw content provided to the privided response
      
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
        
def sendTJXContent ( response, content, format ):
    '''Response the content with the proper format. It supports three response
       content format: txt, json, and xml.  XML responses follow the following 
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
        meandre_items, or tags identifying the keys of a dictionary.
        
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
        # TODO 
        # Need to add the style sheet transformation to make it look nice
        sendRawContent(response, xmlc)
        sendRawContent(response, '</meandre_response>')
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
    if method=="GET" or method=="get" :
        return __dictionary_from_get_request__(request)
    else :
        log.warning("Request "+method+" method not support for parameter extraction") 