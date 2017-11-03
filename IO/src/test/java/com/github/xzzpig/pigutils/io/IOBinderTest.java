package com.github.xzzpig.pigutils.io;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOBinderTest {
    @Test
    @Ignore
    public void test() throws IOException, InterruptedException {
        FileInputStream in = new FileInputStream("E:/aaa.txt");
        //        File bbb = new File("E:/bbb.txt");
        //        bbb.createNewFile();
        FileOutputStream out = new FileOutputStream("E:/bbb.txt");
        IOBinder binder = new IOBinder(in, out);
        binder.start();
        binder.join();
    }

}