package com.xzzpig.pigutils.io;

import org.junit.Test;

import java.io.*;

public class IOBinderTest {

    @Test
    public void test() throws IOException, InterruptedException {
        File source = new File(this.getClass().getResource("IOBinderTest.class").getFile());
        File target = new File(source.getParentFile(), "_" + source.getName());
        System.out.println(target.getCanonicalPath());
        FileInputStream in = new FileInputStream(source);
        target.createNewFile();
        FileOutputStream out = new FileOutputStream(target);
        IOBinder<InputStream, OutputStream> binder = new IOBinder<>(in, out);
        binder.start();
        binder.join();
        assert (target.exists());
        System.out.println(source.length());
        System.out.println(target.length());
        assert (target.length() == source.length());
    }

}