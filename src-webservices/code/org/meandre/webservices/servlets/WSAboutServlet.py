#
# Implements the basic about services
#

__name__ = 'WSAboutServlet'

requestMap = {
    'GET': { 
        'version': 'about_version',    
        'plugins': 'about_plugins' ,
        'installation': 'about_installation',
        'valid_roles': 'about_valid_roles',
        'user_roles': 'about_user_roles'
    }
}

#
# Required imports
#

from java.lang import Class
from java.util import Date
from org.meandre.core.utils import Constants

#
# Services implementation
#

def about_version ( request, response, format ):
    '''Returns information about the current Meandre version.'''
    
    content = { 'version': Constants.MEANDRE_VERSION }
    statusOK(response)
    sendTJXContent(response,content,format)
    
    
def about_plugins ( request, response, format ):
    '''Returns the list of installed global plugins on the Meander server.'''
    content = []
    map = meandre_plugins.getPropPluginFactoryConfig()
    for key in map.keySet() :
        value = map.get(key)
        plugin = Class.forName(value).newInstance()
        pluginInfo = { 
                      'key': key,
                      'className': value,
                     'isServlet': 'false',
                      'alias': meandre_config.getAppContext()+plugin.getAlias()
             }
        if plugin.isServlet() :
            pluginInfo['isServlet'] = 'true'
        content.append(pluginInfo)
    statusOK(response)
    sendTJXContent(response,content,format)


def __get_installation_information__(request):
    '''Returns all the relevant information about the installation.'''
    install_info = {
            'MEANDRE_VERSION': Constants.MEANDRE_VERSION,
            'MEANDRE_RIGHTS': 'All rights reserved by DITA, NCSA, UofI (2007-2008). THIS SOFTWARE IS PROVIDED UNDER University of Illinois/NCSA OPEN SOURCE LICENSE.',
            'CURRENT_USER_LOGGED_IN': request.getRemoteUser(),
            'CURRENT_SESSION_ID': request.getSession().getId(),
            'CURRENT_TIME': Date().toString()  
        }
    properties = meandre_store.getAllProperties()
    for k in properties.keySet():
        install_info[k] = properties.get(k)
    return install_info


def about_installation ( request, response, format ):
    '''Returns related information about the system installation.'''
    if checkUserRole(request,Role.ADMIN) :
        content = __get_installation_information__(request)
        statusOK(response)
        sendTJXContent(response,[content],format)
    else:
        errorUnauthorized(response)
 
 
def about_valid_roles ( request, response, format ):
    '''Returns the list of valid roles recognize by this Meandre engine.'''
    content = []
    for role in Role.getStandardRoles() :
        role = { 
                'meandre_role_uri': role.getUrl(),
                'meandre_role_name': role.getShortName() 
            }
        content.append(role)
    statusOK(response)
    sendTJXContent(response,content,format)
   
   
def about_user_roles ( request, response, format ):
    '''Returns the list of valid roles recognize by this Meandre engine.'''
    content = []
    user = meandre_security.getUser(request.getRemoteUser())
    for role in meandre_security.getRolesOfUser(user) :
        role = { 
                'meandre_role_uri': role.getUrl(),
                'meandre_role_name': role.getShortName() 
            }
        content.append(role)
    statusOK(response)
    sendTJXContent(response,content,format)
    
 