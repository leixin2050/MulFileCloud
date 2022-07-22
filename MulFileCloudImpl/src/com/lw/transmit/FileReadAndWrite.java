package com.lw.transmit;

import com.lw.file.FileSection;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author leiWei
 * 对文件的读写，发送端的读，以及接收端的写
 */
public class FileReadAndWrite {

    public FileReadAndWrite() {
    }

    /**
     * 发送端从文件片段读操作
     * @param raf
     * @param fileSection
     */
    public static void read(RandomAccessFile raf, FileSection fileSection) throws IOException {
        byte[] buffer = new byte[(int) fileSection.getLength()];
        raf.seek(fileSection.getOffset());
        raf.read(buffer);

        fileSection.setContext(buffer);
    }

    /**
     * 接收端对文件进行写操作
     * 使用raf作为锁来使得多线程下写文件不混乱
     * @param raf
     * @param fileSection
     */
    public static void write(RandomAccessFile raf, FileSection fileSection) throws IOException {
        synchronized(raf) {
            raf.seek(fileSection.getOffset());
            raf.write(fileSection.getContext());
        }
    }
}
