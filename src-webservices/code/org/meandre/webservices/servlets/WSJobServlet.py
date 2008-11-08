#
# Implements the basic about services
#

__name__ = 'WSJobServlet'

requestMap = {
    'GET': { 
        'list_jobs_statuses': 'job_list_jobs_statuses'     
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
