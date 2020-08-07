package me.lalayu.serializer;

import me.lalayu.serializer.impl.FastJsonSerializer;
import me.lalayu.serializer.impl.HessianSerializer;
import me.lalayu.serializer.impl.ProtostuffSerializer;

import java.io.IOException;

/**
 *
 **/
public class SerializerTest {
    public static void main(String[] args) throws IOException {
        TestCustomBody example = new TestCustomBody(323414, "我的名字很长", "dfsfsffneujnvenjijidfafs");
        RpcSerializer serializer1 = HessianSerializer.S;
        RpcSerializer serializer2 = FastJsonSerializer.S;
        RpcSerializer serializer3 = ProtostuffSerializer.S;

        runTest(example, serializer1);
        runTest(example, serializer2);

        runTest(example, serializer3);

        System.out.println(runTest(example, serializer1));
        System.out.println(runTest(example, serializer2));
        System.out.println(runTest(example, serializer3));
    }

    private static long runTest(TestCustomBody example, RpcSerializer serializer) throws IOException {
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            byte[] temp2 = serializer.serialize(example);
            TestCustomBody res2 = (TestCustomBody) serializer.deserialize(temp2, TestCustomBody.class);
        }
        long endTime = System.currentTimeMillis();

        return endTime - beginTime;
    }

}
