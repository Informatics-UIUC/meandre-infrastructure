#
# Machinery for the Meandre servlets
#

#
# Basic imports
#

from com.hp.hpl.jena.rdf.model import Model
from com.hp.hpl.jena.rdf.model import ModelFactory

from com.hp.hpl.jena.vocabulary import DC
from com.hp.hpl.jena.vocabulary import RDF
from com.hp.hpl.jena.vocabulary import RDFS
from com.hp.hpl.jena.vocabulary import XSD

from org.meandre.core.security import Role
from org.meandre.core.utils import NetworkTools

#
# Support methods
#
def checkUserRole ( request, role ):
    '''Checks if the provided user making the resquest has been granted 
       the role requested. If so, return true, false otherwise.
       
       checkUserRole ( request, role )'''
    user = meandre_security.getUser(request.getRemoteUser());
    return meandre_security.hasRoleGranted(user, role);

def getMeandreUser ( request ):
    '''Returns the user that made the request.
    
    getMeandreUser ( request )'''
    return request.getRemoteUser()

def getHostName () :
    '''Return the host name.
    
       getHostName()'''
    return NetworkTools.getLocalHostName()

def getEmptyModel() :
    '''Creates an empty blank model.
    
       getEmptykModel() '''
    model = ModelFactory.createDefaultModel();
    model.setNsPrefix('meandre', Store.MEANDRE_ONTOLOGY_BASE_URL )
    model.setNsPrefix('xsd', XSD.getURI())
    model.setNsPrefix('rdf', RDF.getURI())
    model.setNsPrefix('rdfs',RDFS.getURI())
    model.setNsPrefix('dc',DC.getURI())
    return model
