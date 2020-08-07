package me.lalayu.client;

import me.lalayu.exception.InvokeTimeoutException;
import me.lalayu.protocol.RequestMessagePacket;
import me.lalayu.protocol.ResponseMessagePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 **/
public class ResponseFuture {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFuture.class);

    private final long beginTimestamp = System.currentTimeMillis();

    private final long TIME_OUT_MILLISECONDS = 3000L;

    private ResponseMessagePacket response;
    private RequestMessagePacket request;

    private Lock lock = new ReentrantLock();
    private Condition completed = lock.newCondition();


    public ResponseFuture(RequestMessagePacket request) {
        this.request = request;
    }

    public Object start() {
        lock.lock();
        try {
            await();
            if (this.response != null) {
                if (response.getStatusCode() == 200L) {
                    return this.response.getPayload();
                } else {
                    LOGGER.error("failed to invoke the remote service");
                    return null;
                }
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

   private void await() {
        boolean timeout = false;
       try {
           timeout = completed.await(TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       if (!timeout) {
           throw new InvokeTimeoutException("timeout when invoke remote service");
       }
   }

    public void finish(ResponseMessagePacket response) {
        lock.lock();
        try {
            this.response = response;
            completed.signal();
        } finally {
            lock.unlock();
        }
    }
}
