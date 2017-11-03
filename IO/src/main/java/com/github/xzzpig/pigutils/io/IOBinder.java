package com.github.xzzpig.pigutils.io;

import com.github.xzzpig.pigutils.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;

public class IOBinder<I extends InputStream, O extends OutputStream> extends Thread {

    public static int BYTE_ARRAY_SIZE = 1024;
    private I inputStream;
    private O outputStream;
    private IOSolver solver;
    private Predicate<IOException> predicate;

    public IOBinder(I in, O out) {
        this.inputStream = in;
        this.outputStream = out;
        this.solver = new DefaultIOSolver();
    }

    public IOBinder(I in, O out, IOSolver solver) {
        this(in, out);
        this.solver = solver;
    }

    public @Nullable Predicate<IOException> getIOExceptionWatcher() {
        return predicate;
    }

    /**
     * @param predicate if test() return true then stop the while
     */
    public IOBinder setIOExceptionWatcher(@Nullable Predicate<IOException> predicate) {
        this.predicate = predicate;
        return this;
    }

    public I getInputStream() {
        return inputStream;
    }

    public O getOutputStream() {
        return outputStream;
    }

    @Override
    public void run() {
        solver.solve(this);
    }

    private class DefaultIOSolver implements IOSolver {
        @Override
        public void solve(IOBinder<?, ?> binder) {
            byte[] bytes = new byte[BYTE_ARRAY_SIZE];
            int len, count = 0;
            while (!binder.isInterrupted()) {
                try {
                    len = inputStream.read(bytes);
                    if (len == -1)
                        break;
                    outputStream.write(bytes, 0, len);
                    count = 0;
                } catch (IOException e) {
                    if (predicate != null && predicate.test(e)) {
                        break;
                    }
                }
            }
        }
    }
}
