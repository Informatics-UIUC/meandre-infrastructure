#
# Implements the basic about services
#

__name__ = 'WSSecurityServlet'

requestMap = {
    'GET': { 
        'roles_of_user': 'security_roles_of_user',    
        'users': 'security_users',    
        'user': 'security_user',    
        'valid_roles': 'security_valid_roles',    
        'grant_roles': 'security_grant_roles',    
        'revoke_roles': 'security_revoke_roles',    
        'create_users': 'security_create_users',    
        'remove_users': 'security_remove_users',    
        'update_users': 'security_update_users',    
        'revoke_all_roles': 'security_revoke_all_roles'       
    }
}

#
# Required imports
#

from org.meandre.core.security import Role

#
# Services implementation
#

def security_roles_of_user ( request, response, format ):
    '''List the roles of the user registered by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if 'user_name' in params :
            content = []
            for user_name in params['user_name'] :
                user = meandre_security.getUser(user_name)
                roles = meandre_security.getRolesOfUser(user)
                for role in roles :
                    content.append( {
                            'meandre_role_uri': role.getUrl(),
                            'meandre_role_name': role.getShortName() 
                        })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)

 
def security_users ( request, response, format ):
    '''List the users registered by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        content = []
        users = meandre_security.getUsers()
        for user in users:
            content.append({
                    'Nickname': user.getNickName(),
                    'Name': user.getName()
                })
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
 
 
def security_user ( request, response, format ):
    '''List the user information registered by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if 'user_name' in params :
            content = []
            for user_name in params['user_name'] :
                user = meandre_security.getUser(user_name)
                content.append({
                    'Nickname': user.getNickName(),
                    'Name': user.getName()
                })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_valid_roles ( request, response, format ):
    '''List the valid roles for this instance of the Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        content = []
        roles = Role.getStandardRoles()
        for role in roles :
            content.append( {
                    'meandre_role_uri': role.getUrl(),
                    'meandre_role_name': role.getShortName() 
                })
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)


def security_revoke_all_roles ( request, response, format ):
    '''Revokes all the roles assign to this user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if 'user_name' in params :
            content = []
            for user_name in params['user_name'] :
                user = meandre_security.getUser(user_name)
                meandre_security.revokeAllRoles(user)
                content.append({
                    'Nickname': user.getNickName(),
                    'Name': user.getName(),
                    'Revoked': 'all'
                })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_revoke_roles ( request, response, format ):
    '''Revokes the roles assign to the indicated user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if ('user_name' in params) and ('role_name' in params):
            content = []
            for user_name in params['user_name'] :
                user = meandre_security.getUser(user_name)
                for role_url in params['role_name'] :
                    role = Role.fromUrl(role_url)
                    meandre_security.revokeRole(user,role)
                    content.append({
                        'Nickname': user.getNickName(),
                        'Name': user.getName(),
                        'Revoked': role_url
                    })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_grant_roles ( request, response, format ):
    '''Grants the roles assign to the indicated user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if ('user_name' in params) and ('role_name' in params):
            content = []
            for user_name in params['user_name'] :
                user = meandre_security.getUser(user_name)
                for role_url in params['role_name'] :
                    role = Role.fromUrl(role_url)
                    meandre_security.grantRole(user,role)
                    content.append({
                        'Nickname': user.getNickName(),
                        'Name': user.getName(),
                        'Revoked': role_url
                    })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_create_users ( request, response, format ):
    '''Creates the indicated user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if ('user_name' in params) and ('user_full_name' in params) and ('password' in params):
            content = []
            for user_name, user_full_name, password in zip(params['user_name'],params['user_full_name'],params['password']) :
                meandre_security.createUser(user_name,user_full_name,password)
                content.append({
                    'Nickname': user_name,
                    'Name': user_full_name
                })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_remove_users ( request, response, format ):
    '''Removes the indicated user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if 'user_name' in params :
            content = []
            for user_name in params['user_name']:
                user = meandre_security.getUser(user_name)
                meandre_security.removeUser(user)
                content.append({
                    'Nickname': user.getNickName(),
                    'Name': user.getName()
                })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)


def security_update_users ( request, response, format ):
    '''Creates the indicated user by this instance of the 
       Meandre Server.''' 
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        if ('user_name' in params) and ('user_full_name' in params) and ('password' in params):
            content = []
            for user_name, user_full_name, password in zip(params['user_name'],params['user_full_name'],params['password']) :
                user = meandre_security.getUser(user_name)
                meandre_security.removeUser(user)
                meandre_security.createUser(user_name,user_full_name,password)
                content.append({
                    'Nickname': user_name,
                    'Name': user_full_name
                })
            statusOK(response)
            sendTJXContent(response,content,format)
        else:
            errorExpectationFail(response)
    else:
        errorForbidden(response)

