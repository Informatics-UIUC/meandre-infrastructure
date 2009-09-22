#
# Implements the basic about services
#

__name__ = 'WSJobServlet'

requestMap = {
    'GET': { 
        'list_jobs_statuses': 'job_list_jobs_statuses',
        'job_console': 'job_job_console'   
    }
}

#
# Required imports
#

from org.meandre.core.security import Role

#
# Services implementation
#

def job_list_jobs_statuses ( request, response, format ):
    '''List the current job statuses in the Meandre Server.''' 
    content = []
    remote_user = getMeandreUser(request)
    jiba = meandre_store.getJobInformation()
    for job in jiba.getJobStatuses():
        job_user_id = job.get('user_id')
        print '<',job_user_id,'>'
        if checkUserRole (request,Role.ADMIN) or job_user_id==remote_user :
            map = {}
            for key in job.keySet() :
                map[key] = job.get(key)
            content.append(map)
    statusOK(response)
    sendTJXContent(response,content,format,remote_user)
        
def job_job_console ( request, response, format ):
    '''List the current job statuses in the Meandre Server.''' 
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            content = []
            remote_user = request.getRemoteUser()
            jiba = meandre_store.getJobInformation()
            for uri in params['uri'] :
                job_user_id = jiba.getJobOwner(uri)
                if job_user_id and (checkUserRole (request,Role.ADMIN) or job_user_id==remote_user) :
                    console = jiba.getConsole(uri)
                    content.append( {'job_id':uri, 'console':console} )
                else:
                    errorForbidden(response)
                    return
            statusOK(response)
            sendTJXContent(response,content,format,getMeandreUser(request))
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)
        
