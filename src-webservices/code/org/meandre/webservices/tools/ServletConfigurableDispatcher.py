#
# The basic dispatching dictionary
#
requestMap = {
}

#
# The needed imports
#
from javax.servlet.http import HttpServletResponse

#
# The main dispatch method
#
def dispatch(httpMethod,request,response,method,format):
    if httpMethod not in requestMap:
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    else :
        eval(requestMap[httpMethod][method]+'(request,response,format)')