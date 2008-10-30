#
# Implements the basic about services
#

__name__ = 'WSAuxiliarServlet'

requestMap = {
    'GET': { 
        'add_to_repository': 'auxiliar_add_to_repository',
        'create_user': 'auxiliar_create_user',
        'roles_map': 'auxiliar_roles_map'
    }
}

#
# Required imports
#

__header = """
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
                <title>Meandre Server</title>
                <meta http-equiv="content-type" content="text/html; charset=utf8" />
                <meta http-equiv="content-language" content="EN" />
                <meta name="ROBOTS" content="NOINDEX,NOFOLLOW"/>
                <meta name="description" content="Meandre Server"/>
                <style type="text/css">
                        a, a:visited {
                            color: #aa6000;
                            text-decoration: none;
                            font-style: italic;
                        }
                        
                        a:hover {
                            color: #cc6000;
                            text-decoration: underline;
                            font-style: italic;
                        }
                        
                        
                        body {
                           color: #444; 
                           background: white; 
                           font-family: Helvetica, Arial, Verdana; 
                           font-size: 11px; 
                        }   
                        
                        form {
                            margin-top:15px;
                            margin-left:15px;
                        } 
                    
                        #main table {
                            width: 100%;
                            margin-left:auto;
                            margin-right:auto;
                            border: 1px solid gray;
                            border-collapse: collapse;
                            font-size: 12px;
                        }
                        
                        #main th {
                            color: white;
                            background: orange;
                        }    
                        
                        #main th,td {    
                            border: 1px solid gray;
                            padding-left:6px;
                            padding-top:3px;
                            padding-bottom:3px;
                            text-align:center;
                        }
                    </style>
            </head>
             <body>
             <img src="/public/resources/system/logo-meandre.gif" /><br/>
    """
    
__footer = """
             <br/>
             <a href="javascript:javascript:history.go(-1)">Back</a>
             </body>
    </html>
    """

__add_to_repository_form = """
        <form enctype="multipart/form-data" method="POST" action="/services/repository/add.html" >
        <fieldset>
                <table>
                    <tbody>
                        <tr>
                            <td>
                                <label for="repository">Descriptor:</label>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <input type="file" name="repository" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <label for="context">Resource contexts:</label>
                            </td>
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td >
                                <input type="file" name="context" />
                            </td> 
                        </tr>
                        <tr>
                            <td>
                                <label for="embed">Embedded? </label><input type="checkbox" name="embed" value="true" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="overwrite">Overwrite? </label><input type="checkbox" name="overwrite" value="true" />
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="submit" class="submit" value="Upload" />
                                <input type="reset" class="reset" value="Clear data" />
                            </td>
                        </tr>
                    </tbody>                
                </table>
            </fieldset>
        </form>
    """
    
__add_user_form = """
        <form method="get" action="/services/security/create_users.html" >
            <fieldset>
                <label for="username">Username:</label><br/>
                <input type="text" name="user_name"/><br/>
                <label for="fullname">Full name:</label><br/>
                <input type="text" name="user_full_name"/><br/>
                <label for="password">Password:</label><br/>
                <input type="password" name="password"/><br/>
                <input type="submit" class="submit" value="Add user" />
                <input type="reset" class="reset" value="Clear data" />
            </fieldset>
        </form>
    """
    
#
# Services implementation
#

def auxiliar_add_to_repository ( request, response, format ):
    '''Generates for to upload to a repository.''' 
    if checkUserRole (request,Role.REPOSITORY) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header)
            sendRawContent(response,__add_to_repository_form)
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)


def auxiliar_create_user ( request, response, format ):
    '''Generates for to add a user for this server.''' 
    if checkUserRole (request,Role.ADMIN) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header)
            sendRawContent(response,__add_user_form)
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)
        
def auxiliar_roles_map ( request, response, format ):
    '''Generates map of the current users and roles.''' 
    if checkUserRole (request,Role.ADMIN) :
        if format=='html' :
            statusOK(response)
            contentAppHTML(response)
            sendRawContent(response,__header)
            sendRawContent(response,'<br/><div id="main"><table><tr><th>User</th><th>Full name</th>')
            roles = Role.getStandardRoles()
            for role in roles :
                sendRawContent(response,'<th>'+role.getShortName()+'</th>')
            sendRawContent(response,'</tr>')
            for user in meandre_security.getUsers():
                user_nick_name = user.getNickName()
                sendRawContent(response,'<tr><td>'+user_nick_name+'</td>')
                sendRawContent(response,'<td>'+user.getName()+'</td>')
                user_roles = meandre_security.getRolesOfUser(user)
                for role in roles:
                    if role in user_roles :
                        sendRawContent(response,'<td><a href="/services/security/revoke_roles.html?user_name='+user_nick_name+'&role_name='+role.getUrl()+'">revoke</a></td>')
                    else:
                        sendRawContent(response,'<td><a href="/services/security/grant_roles.html?user_name='+user_nick_name+'&role_name='+role.getUrl()+'">grant</a></td>')
                sendRawContent(response,'</tr>')
            sendRawContent(response,'</table></div>')
            sendRawContent(response,__footer)
        else :
            errorNotFound(response)
    else:
        errorForbidden(response)
    