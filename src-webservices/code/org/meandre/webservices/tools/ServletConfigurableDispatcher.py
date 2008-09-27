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
    if httpMethod not in requestMap:
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    else :
        eval(requestMap[httpMethod][method]+'(request,response,format)')
        
#
# Status functions
#

def statusOK ( response ) :
    response.setStatus(HttpServletResponse.SC_OK)
    
#
# Content response function
#
def contentTextPlain ( response ):
    response.setContentType("text/plain")
     
def contentAppJSON ( response ):
    response.setContentType("application/json")
    
def contentAppXML ( response ):
    response.setContentType("application/xml")
   
#
# Response functions
#
def sendRawContent ( response, content ):
    response.getWriter().print(content)
    
def __content_to_JSON__ (content) :
    if isinstance(content,list) :
        ja = JSONArray()
        for c in content:
            ja.put(__content_to_JSON__(c))
        return ja
    elif isinstance(content,dict) :
        jo = JSONObject()
        for k,c in content:
            jo.put(k,__content_to_JSON__(c))
        return jo
    else :
        return content
    
def sendTJXContent ( response, content, format ):
    if format=='txt':
        contentTextPlain(response)
        for c in content :
            sendRawContent(response,c)
            sendRawContent(response,'\n')
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
        
def sendRDFModel ( response, model, format ) :
    model.setNsPrefix('meandre', Store.MEANDRE_ONTOLOGY_BASE_URL )
    model.setNsPrefix('xsd', XSD.getURI())
    model.setNsPrefix('rdf', RDF.getURI())
    model.setNsPrefix('rdfs',RDFS.getURI())
    model.setNsPrefix('dc',DC.getURI())
    if format=='rdf' :
        contentAppXML(response)
        model.write(response.getOutputStream(),'RDF/XML-ABBREV')
    elif format=='ttl' :
        contentTextPlain(response)
        model.write(response.getOutputStream(),'TTL')
    else:
        contentTextPlain(response)
        model.write(response.getOutputStream(),'N-TRIPLE')
        
    