package org.meandre.core.engine;

import java.security.Permission;

/**This Security manager traps System.exit calls. We don't want components calling System.exit
 * 
 * @author Amit Kumar
 * @since 1.1
 *
 */
public final class MeandreSecurityManager extends SecurityManager{
	
	/** Check the requested permission.
	 * 
	 * @param permission The permision to check
	 */
	public void checkPermission( Permission permission ) {
		System.out.println("======"+permission.getName());
		if( "exitVM".equals( permission.getName() ) ) {
          System.out.println("EXIT VM is Privileged.");
          throw new SecurityException("Meandre infrastructure does not allow directly calling exit");
        }
		
      }

	
}
