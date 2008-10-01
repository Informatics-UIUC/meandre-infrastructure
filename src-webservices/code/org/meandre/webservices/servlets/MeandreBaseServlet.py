#
# Machinery for the Meandre servlets
#
from org.meandre.core.security import Role

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