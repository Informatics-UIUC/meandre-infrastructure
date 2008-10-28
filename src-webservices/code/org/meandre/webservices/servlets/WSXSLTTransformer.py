#
# Implements the basic about services
#

__name__ = 'WSXSLTTransformer'

requestMap = {
    'GET': { 
        'process': 'xslttransformer_process'
    }
}

#
# Required imports
#

#
# Services implementation
#

def xslttransformer_process ( request, response, format ):
    '''Transforms the requested URI''' 
    params = extractRequestParamaters(request)
    if 'target' in params and format=='xml' :
        disp = request.getRequestDispatcher(params['target'])
        pass
    else:
        errorExpectationFail(response)