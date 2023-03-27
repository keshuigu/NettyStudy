# NIO基础

## 1. 三大组件

### 1.1 Channel和Buffer

-   Channel：数据读写**双向通道**

-   Buffer：缓冲读写数据

-   ```mermaid
    graph LR
    channel --> buffer
    buffer --> channel
    ```

-   可以从channel写数据到buffer，也可以从buffer读数据到channel

-   常见channel：

    -   FileChannel：文件
    -   DatagramChannel：UDP
    -   SocketChannel：TCP
    -   ServerSocketChannel：TCP（Server专用）

-   常见Buffer：

    -   ByteBuffer（abstract）：
        -   MappedByteBuffer
        -   DirectByteBuffer
        -   HeapByteBuffer
    -   ShortBuffer、IntBuffer、......（不常用）

### 1.2 Selector

`selector` 的作用是配合一个`Thread`来管理多个 `channel`，获取这些 `channel` 上发生的事件，这些 `channel` 工作在非阻塞模式下，不会让`Thread`吊死在一个 `channel` 上。适合连接数特别多，但流量低的场景
调用`selector`的`selcet()`方法会阻塞直到`channel`发生读写就绪事件，发生后返回这些事件给`Thread`处理。

```mermaid
graph TD
subgraph selector版
thread --> selector
selector --> c1(channel)
selector --> c2(channel)
selector --> c3(channel)
end
```

## 2. ByteBuffer

### 2.1 ByteBuffer常规使用

1. 向`buffer`写入数据，如调用`channel.read(buffer)`
2. 调用`filp()`切换至读模式
3. 从`buffer`读取数据，如调用`buffer.get()`
4. 调用`clear()`或`compact()`切换至写模式
5. 重复上述步骤

### 2.2 ByteBuffer结构

ByteBuffer重要属性：

- capacity
- position
- limit

1. 初始状态下

   ![](../.guide/讲义/img/0021.png)
   
2. 处于写模式，position为写入位置，limit等于容量，在写入4个字节后

   ![](../.guide/讲义/img/0018.png)

3. flip动作发生后，position切换为读取位置，limit切换为读取限制

   ![](../.guide/讲义/img/0019.png)

4. 读取4个字节后，position=limit

   ![](../.guide/讲义/img/0020.png)

5. clear动作发生后

   ![](../.guide/讲义/img/0021.png)

6. compact方法，是将未读完的部分向前压缩，然后切换至写模式

   ![](../.guide/讲义/img/0022.png)

### 2.3 ByteBuffer 常见方法

1. 分配空间： allocate

   ```java
   Bytebuffer buf = ByteBuffer.allocate(16);
   ```

2. 写入数据：channel.read, buffer.put

   ```java
   int readBytes = channel.read(buf)
   buf.put((byte)127)
   ```

3. 读取数据：channel.write, buffer.get

   ```java
   int writeBytes = channel.write(buf)
   byte b = buf.get(); // position 发生移动，使用rewind方法将position重新置0
   byte b = buf.get(i);// 获取索引i的内容，但不移动position
   ```

4. mark和reset

   mark 是在读取时，做一个标记，即使 position 改变，只要调用 reset 就能回到 mark 的位置

   > rewind 和 flip 都会清除 mark 位置

5. 分散读取

   ```java
   ByteBuffer a = ByteBuffer.allocate(3);
   ByteBuffer b = ByteBuffer.allocate(3);
   ByteBuffer c = ByteBuffer.allocate(5);
   channel.read(new ByteBuffer[]{a, b, c}); //将数据填充至a,b,c
   ```

6. 聚合写入

   ```java
   ByteBuffer d = ByteBuffer.allocate(4);
   ByteBuffer e = ByteBuffer.allocate(4);
   channel.write(new ByteBuffer[]{d, e});	//将d,e数据写入channel
   ```

   

### 2.4 黏包、半包

```java
public static void main(String[] args) {
    ByteBuffer source = ByteBuffer.allocate(32);
    source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
    split(source);
    source.put("w are you?\nhaha!\n".getBytes());
    split(source);
}

private static void split(ByteBuffer source) {
    source.flip(); // 切换为读模式
    int oldLimit = source.limit(); //保护原始数据
    for (int i = 0; i < oldLimit; i++) { //执行flip后，position=0
        if (source.get(i) == '\n') { //get(i)逐个访问，但不移动position
            System.out.println(i);
            ByteBuffer target = ByteBuffer.allocate(i + 1 - source.position());
            // 0 ~ limit，设置长度
            source.limit(i + 1); //移动limit读取对应长度数据
            target.put(source); // 从source 读，向 target 写，此处移动position
            debugAll(target);
            source.limit(oldLimit);//还原limit，继续读入
        }
    }
    source.compact();
}
```

