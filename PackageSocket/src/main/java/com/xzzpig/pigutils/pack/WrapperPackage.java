package com.xzzpig.pigutils.pack;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.reflect.ClassUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class WrapperPackage extends Package {

    protected Package pack;

    protected WrapperPackage() {
    }

    public WrapperPackage(@NotNull Package pack) {
        ClassUtils.checkThisConstructorArgs(pack);
        this.pack = pack;
    }

    public WrapperPackage(@Nullable String type, @Nullable byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        unWrapPackage(type, bin);
        try {
            bin.close();
        } catch (IOException e) {
        }
    }

    @Override
    public byte[] getData() {
        return pack.getData();
    }

    @Override
    public String getType() {
        return pack.getType();
    }

    public Package getWrappedPackage() {
        return pack;
    }

    protected void unWrapPackage(String type, ByteArrayInputStream data) {
        byte[] bs = new byte[data.available()];
        try {
            data.read(bs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pack = new Package(type, bs);
    }
}
