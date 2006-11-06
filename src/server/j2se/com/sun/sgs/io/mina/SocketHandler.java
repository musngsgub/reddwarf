package com.sun.sgs.io.mina;

import java.nio.ByteBuffer;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;

import com.sun.sgs.io.IOHandle;
import com.sun.sgs.io.IOHandler;

/**
 *  This is an adapter between an Apache Mina {@link IoHandler} and the SGS
 *  IO framework {@link IOHandler}.  This is used on the client side to 
 *  pass messages from Mina onto the associated {@code IOHandler}.
 * 
 * @author      Sten Anderson
 * @version     1.0
 */
public class SocketHandler implements IoHandler {
    
    private IOHandler handler;
    
    /**
     * Constructs a new {@code SocketHandler}.  As callbacks get fired from
     * the Apache Mina framework, they are forwarded on to the given
     * {@code IOHandler}.
     * 
     * @param handler           the {@code IOHandler} that will receive 
     *                          callbacks.
     */
    public SocketHandler(IOHandler handler) {
        this.handler = handler;
    }

    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("SocketHandler sessionCreated ");
        
    }

    public void sessionOpened(IoSession arg0) throws Exception {

    }

    public void sessionClosed(IoSession session) throws Exception {
        handler.disconnected(getHandle(session));
    }

    public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {

    }

    public void exceptionCaught(IoSession session, Throwable exception)
            throws Exception {
        
        handler.exceptionThrown(exception, getHandle(session));
        
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        org.apache.mina.common.ByteBuffer buffer = 
                (org.apache.mina.common.ByteBuffer) message;
        handler.messageReceived(buffer.buf(), getHandle(session));
        
    }

    public void messageSent(IoSession arg0, Object arg1) throws Exception {

    }
    
    private IOHandle getHandle(IoSession session) {
        return (IOHandle) session.getAttachment();
    }

}
