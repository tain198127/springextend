package org.springframework.boot.loader;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileTools {

    public static final long ONE_KB = 1024;

    public static final long ONE_MB = ONE_KB * ONE_KB;

    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    public static final long ONE_GB = ONE_KB * ONE_MB;

    public static final long ONE_TB = ONE_KB * ONE_GB;

    public static final long ONE_PB = ONE_KB * ONE_TB;

    public static final long ONE_EB = ONE_KB * ONE_PB;

    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            closeQuietly(output);
            closeQuietly(fos);
            closeQuietly(input);
            closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    public static void deleteDirectoy(File dir) throws Exception{
        File[] files = dir.listFiles();
        if(files !=null){
            for(File file: files){
                if(!file.delete()){
                    throw new RuntimeException("删除文件失败!");
                }
                System.out.println("------------删除文件:"+file.getAbsolutePath());
            }
            dir.delete();
            System.out.println("------------删除目录:"+dir.getAbsolutePath());
        }
    }
}