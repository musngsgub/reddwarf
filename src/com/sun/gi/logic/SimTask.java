/* Generated by Together */

package com.sun.gi.logic;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import com.sun.gi.comm.routing.ChannelID;
import com.sun.gi.comm.routing.UserID;
import com.sun.gi.objectstore.ObjectStore;
import com.sun.gi.objectstore.Transaction;

/**
 *
 * <p>Title: SimTask</p>
 * <p>Description: This interface defines a class that encapsulates
 * the exection of a Game Logic Manager task</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Sun Microsystems, TMI</p>
 * @author Jeff Kesselman
 * @version 1.0
 */

public interface SimTask {
	public enum ACCESS_TYPE {GET,PEEK,ATTEMPT};
  /**
   * Called to transfer the calling thread of control to the execution
   * of the task.
   * 
   */
  public void execute();

  /**
   * This is a utility call used by other parts of the system.
   * It takes a Game Logic Object (GLO) ID and wraps it in a
   * SORerence.  (An SOReference is a GLO refernce, it has the SO name
   * for historical reasons.)
   *
   * @param id long the GLO id
   * @return SOReference an SO references that may be used by another GLO
   */

  public GLOReference makeReference(long id);

  /**
   * Gets the transaction associated with this SimTask.  A SimTask
   * only has a transaction associated with it during execution.
   * @return Transaction the associated transaction or NULL if the SimTask
   * is not currently executing.
   */
  public Transaction getTransaction();

  // client functions
  // All the functions from here down are used by game application code
  // to talk to its execution context and request services from it.

  /**
   * Returns the applciation ID assigned to the game to which this task belongs.
   * @return long the app ID
   */
  public long getAppID();

  /**
   * This function registers a GLO as a listener to user join/left events
   * The listening GLO must implement the SimUserListener interface.
   * @param ref SOReference A reference to the GLO to be registered.
   */
  public void addUserListener(GLOReference ref);

  /**
   * This function registers a GLO as a listener to data packet arrival
   * events.  It listens for data adressed to the UserID passed.
   *
   * The listening GLO must implement the SimUserDataListener interface.
   *
   * @param id UserID The UserID that data will be adressed to to trigger
   * this listener.
   * @param ref SOReference A reference to the GLO to be registered.
   */

  public void addUserDataListener(UserID id, GLOReference ref);
  
  /**
   * @param boot
   */
  public void addChannelListener(ChannelID id, GLOReference ref);


  /**
   * The game code can call this to send data to users by their IDs.
   * This actually maps to the send call down in the router layer by calling the
   * user manager created to handle this particular game.
   *
   * @param to UserID[]  the list of message recipients.
   * @param from UserID the sender's id (return address)
   * @param bs byte[] the data packet to send.
   */
  public void sendData(ChannelID cid, UserID[]  to, ByteBuffer data,
		  boolean reliable);

  

  /**
   * This method is called to create a GLO is the objectstore
   * (the name is historical and shoudl probably be changed.)
   *
   * @param simObject Serializable the GLO to insert into the objects store
   * @param name String an optional symbolic reference to assign to the
   * object, or null.
   * @return SOReference A SOReference that references the newly created GLO
   */
  public GLOReference createSO(Serializable simObject, String name);
  

  // data access functions
  // These functions are used by games to get data from the ObjectStore

  /**
   * Thsi method is used to rertrieve an SOReference based on the symbolic
   * name assigned to the GLO at the time of creation in the objectstore.
   *
   * As is everything else is the obejctstore, symbolic names are specific
   * to a game context.  (The Game's App ID is an implicit part of the key.)
   *
   * @param soName String The symbolic name to look up.
   * @return SOReference A reference to the GLo if found, null if not found.
   */
  public GLOReference findSO(String soName);

/**
 * This method opens a comm channel and returns an ID for it
 * @param string
 * @return
 */
	public ChannelID openChannel(String string);
	
	/**
	 * 
	 * @param ref
	 * @param delay
	 * @param repeat
	 * @return
	 */

	public long registerTimerEvent(long delay, boolean repeat, GLOReference ref);
	
	/**
	 * @param access
	 * @param l
	 * @param b
	 * @param reference
	 */
	public long registerTimerEvent(ACCESS_TYPE access, long l, boolean b, GLOReference reference);

	/**
	 * @param timer
	 * @return
	 * @throws InstantiationException 
	 */
	public GLOReference makeReference(Serializable glo) throws InstantiationException;

	public void registerGLOID(long objID,Serializable glo,ACCESS_TYPE access);

	/**
	 * 
	 * @param accessType
	 * @param target
	 * @param method
	 * @param parameters
	 */
	public void queueTask(ACCESS_TYPE accessType, GLOReference target, Method method,
			Object[] parameters);
	
	/**
	 * 
	 * @param target
	 * @param method
	 * @param parameters
	 */
	public void queueTask(GLOReference target, Method method,
			Object[] parameters);

	/**
	 * @param get
	 */
	public void access_check(ACCESS_TYPE accessType, Object glo); 
	
	
	// Hooks into the RawSocketManager, added 1/16/2006
	
	/**
	 * Requests that a socket be opened at the given host on the given port.
	 * The returned ID can be used for future communication with the socket that will
	 * be opened.  The socket ID will not be valid, and therefore should not be used 
	 * until the connection is complete.  Connection is complete once the 
	 * SimRawSocketListener.socketOpened() call back is called.
	 * 
	 * @param access			the access type (GET, PEEK, or ATTEMPT)
	 * @param ref				a reference to the GLO initiating the connection.
	 * @param host				a String representation of the remote host.
	 * @param port				the remote port.
	 * @param reliable			if true, the connection will use a reliable protocol.
	 * 
	 * @return an identifier that can be used for future communication with the socket.
	 */
	public long openSocket(ACCESS_TYPE access, GLOReference ref, String host, 
			int port, boolean reliable);

	/**
	 * Sends data on the socket mapped to the given socketID.  This method 
	 * will not return until the entire buffer has been drained.
	 * 
	 * @param socketID			the socket identifier.
	 * @param data				the data to send.  The buffer should be in a ready
	 * 							state, i.e. flipped if necessary. 
	 * 
	 * @return the number of bytes sent.
	 */
	public long sendRawSocketData(long socketID, ByteBuffer data);
	
	/**
	 * Requests that the socket matching the given socketID be closed.
	 * The socket should not be assumed to be closed, however, until the 
	 * call back SimRawSocketListener.socketClosed() is called.
	 * 
	 * @param socketID		the identifier of the socket.
	 */
	public void closeSocket(long socketID);	


	
}
