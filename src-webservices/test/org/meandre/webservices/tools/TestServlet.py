#
# The basic dispatching dictionary
#
requestMap = {
    'GET': { 'ping': 'ping' }
}

#
# The needed imports
#
from javax.servlet.http import HttpServletResponse

#
# The ping method
#
def ping ( request, response, format ):
    response.setStatus(HttpServletResponse.SC_OK)
    response.setContentType("text/plain")
    response.getWriter().print("pong")
    
