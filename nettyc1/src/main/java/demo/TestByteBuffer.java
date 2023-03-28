package demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;

import static util.ByteBufferUtil.debugAll;
import static util.ByteBufferUtil.debugRead;

public class TestByteBuffer {
    public static void main(String[] args) {
        String from = "data.txt";
        String to = "to.txt";
        long start = System.nanoTime();
        try (FileChannel fromChannel = new FileInputStream(from).getChannel();
            FileChannel toChannel = new FileOutputStream(to).getChannel()
        ) {
            System.out.println(fromChannel.size());
            fromChannel.transferTo(0,fromChannel.size(),toChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();
        System.out.println("time" + (end-start)/100_0000.0);

    }
}