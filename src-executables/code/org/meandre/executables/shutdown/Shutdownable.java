package org.meandre.executables.shutdown;

/**
 * A Shutdownable object is typically some kind of service or thread that
 * has 'stop' command to end it's run. This java Interface is originally to
 * provide a consistent interface for a ShutdownControlThread to 
 * stop a generic application.
 */
public interface Shutdownable{

    /** when called, the Shutdownable object should end it's run, shutdown,
     * stop, kill it's thread, kill it's process, or whatever it does.
     */
    public void stop();

}
