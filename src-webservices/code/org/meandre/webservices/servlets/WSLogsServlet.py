#
# Implements the basic about services
#

__name__ = 'WSLogsServlet'

requestMap = {
    'GET': { 
        'webservices': 'log_webservices',
        'kernel': 'log_kernel',
        'coordinator': 'log_coordinator',
        'plugins': 'log_plugins',
        'probes': 'log_probes'
    }
}

#
# Required imports
#

from java.io import File
from org.meandre.core.utils import Constants

#
# Constants
#
__base_ws_logger = '.'+File.separator+'log'+File.separator+'meandre-webservices.log.0'
__base_kr_logger = '.'+File.separator+'log'+File.separator+'meandre-kernel.log.0'
__base_co_logger = '.'+File.separator+'log'+File.separator+'meandre-coordinator.log.0'
__base_pg_logger = '.'+File.separator+'log'+File.separator+'meandre-plugins.log.0'
__base_pr_logger = '.'+File.separator+'log'+File.separator+'meandre-probes.log.0'

#
# Services implementation
#

def __read_log ( log_file, offset ):
    '''Returns the read log'''
    f=open(log_file,'rb')
    s,i = '',0
    while 1:
        line = f.readline()
        if not line:
            break
        if i>=offset :
            s += line
        i += 1
    f.close()
    return s

def __dump_log ( request, response, format, log ):
    '''Dumps the requested log'''
    
    if checkUserRole (request,Role.ADMIN) :
        params = extractRequestParamaters(request)
        offset = 0
        if 'offset' in params : offset = int(params['offset'][0])
        content = []
        statusOK(response)
        content.append({'log':__read_log(log,offset)})
        sendTJXContent(response,content,format)
    else:
        errorForbidden(response)
    
def log_webservices ( request, response, format ):
    '''Returns the webservices log'''
    __dump_log ( request, response, format, __base_ws_logger )    
        
def log_kernel ( request, response, format ):
    '''Returns the kernel log'''
    __dump_log ( request, response, format, __base_kr_logger )
        
def log_coordinator ( request, response, format ):
    '''Returns the coordinator log'''
    __dump_log ( request, response, format, __base_co_logger )
        
def log_plugins ( request, response, format ):
    '''Returns the plugins log'''
    __dump_log ( request, response, format, __base_pg_logger )
    
def log_probes ( request, response, format ):
    '''Returns the probes log'''
    __dump_log ( request, response, format, __base_pr_logger )