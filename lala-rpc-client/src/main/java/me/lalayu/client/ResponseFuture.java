package me.lalayu.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.lalayu.protocol.ResponseMessagePacket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 **/
@ToString
public class ResponseFuture {

    private final long beginTimestamp = System.currentTimeMillis();

    @Getter
    private final long timeoutMilliseconds;

    @Getter
    private final String requestId;

    @Getter
    @Setter
    private volatile boolean sendRequestSucceed = false;

    @Getter
    @Setter
    private volatile Throwable cause;

    @Getter
    private volatile ResponseMessagePacket response;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public ResponseFuture(String requestId, long timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
        this.requestId = requestId;
    }

    public boolean timeout() {
        return System.currentTimeMillis() - timeoutMilliseconds > timeoutMilliseconds;
    }

    public ResponseMessagePacket waitResponse(final long timeoutMilliseconds) throws InterruptedException {
        countDownLatch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        return response;
    }

    public void putResponse(ResponseMessagePacket response) throws InterruptedException {
        this.response = response;
        countDownLatch.countDown();
    }
}
