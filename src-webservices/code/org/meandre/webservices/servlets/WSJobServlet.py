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
    if checkUserRole (request,Role.ADMIN) :
        content = []
        jiba = meandre_store.getJobInformation()
        for job in jiba.getJobStatuses():
            map = {}
            for key in job.keySet() :
                map[key] = job.get(key)
            content.append(map)
        statusOK(response)
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
        
def job_job_console ( request, response, format ):
    '''List the current job statuses in the Meandre Server.''' 
    if checkUserRole (request,Role.EXECUTION) :
        params = extractRequestParamaters(request)
        if 'uri' in params :
            content = []
            jiba = meandre_store.getJobInformation()
            for uri in params['uri'] :
                console = jiba.getConsole(uri)
                content.append( {'job_id':uri, 'console':console} )
            statusOK(response)
            sendTJXContent(response,content,format)
        else :
            errorExpectationFail(response)
    else:
        errorForbidden(response)
        
