package demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static util.ByteBufferUtil.debugAll;
import static util.ByteBufferUtil.debugRead;

public class TestByteBuffer {
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        //                     11            24
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you?\nhaha!\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip(); // 切换为读模式
        int oldLimit = source.limit(); //保护原始数据
        for (int i = 0; i < oldLimit; i++) {
            if (source.get(i) == '\n') { //get(i)逐个访问，但不移动position
                System.out.println(i);
                ByteBuffer target = ByteBuffer.allocate(i + 1 - source.position());
                // 0 ~ limit
                source.limit(i + 1);
                target.put(source); // 从source 读，向 target 写
                debugAll(target);
                source.limit(oldLimit);
            }
        }
        source.compact(); // 压缩原始数据
    }
}