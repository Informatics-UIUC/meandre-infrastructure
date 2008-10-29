#
# Implements the basic about services
#

__name__ = 'WSAuxiliarServlet'

requestMap = {
    'GET': { 
        'add_to_repository': 'auxiliar_add_to_repository'
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
                    
                    </style>
            </head>
             <body>
             <img src="/public/resources/system/logo-meandre.gif" /><br/>
    """
    
__footer = """
             </body>
    </html>
    """

__add_form = """
        <form enctype="multipart/form-data" method="post" action="/services/repository/add.ttl" >
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
#
# Services implementation
#

def auxiliar_add_to_repository ( request, response, format ):
    '''Generates for to upload to a repository.''' 
    if format=='html' :
        statusOK(response)
        contentAppHTML(response)
        sendRawContent(response,__header)
        sendRawContent(response,__add_form)
        sendRawContent(response,__footer)
    else :
        errorNotFound(response)
    