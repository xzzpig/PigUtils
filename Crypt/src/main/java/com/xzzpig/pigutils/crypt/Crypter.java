package com.xzzpig.pigutils.crypt;

import com.xzzpig.pigutils.core.TransformManager;
import com.xzzpig.pigutils.core.TransformManager.Transformer;
import com.xzzpig.pigutils.crypt.md5.FileMD5Crypter;
import com.xzzpig.pigutils.crypt.md5.MD5Crypter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * 加密器
 */
public abstract class Crypter {

    static {
        regCrypter(new MD5Crypter());
        regCrypter(new FileMD5Crypter());
    }

    /**
     * 获取type类型的 {@link Crypter} 并取得 objs 的加密结果
     */
    public static Cryptable crypt(String type, Object... objs) {
        Crypter crypter = TransformManager.getDefaultManager().transform(Crypter.class, "crypt." + type, null, null);
        return crypter == null ? null : crypter.crypt(objs);
    }

    /**
     * 获取type类型的 {@link Crypter} 并取得 objs 的加密结果
     */
    public static Decryptable decrypt(String type, Object... objs) {
        Crypter crypter = TransformManager.getDefaultManager().transform(Crypter.class, "crypt." + type, null, null);
        return crypter == null ? null : crypter.decrypt(objs);
    }

    /**
     * 注册加密器
     */
    public static void regCrypter(Crypter crypter) {
        TransformManager.getDefaultManager().addTransformer(new CrypterTransformr(crypter));
    }

    /**
     * 解除注册加密器
     */
    public static void unregCrypter(Crypter crypter) {
        TransformManager.getDefaultManager().getTransformers().removeIf(((Predicate<Transformer<?, ?>>) (CrypterTransformr.class::isInstance))
                .and((t)->((CrypterTransformr) t).crypter.getCryptType().equalsIgnoreCase(crypter.getCryptType())));
    }

    protected abstract Cryptable crypt(Object... objs);

    protected abstract Decryptable decrypt(Object... objs);

    /**
     * @return 加密类型
     */
    public abstract String getCryptType();

    private static class CrypterTransformr implements Transformer<String, Crypter> {
        private Crypter crypter;

        public CrypterTransformr(Crypter crypter) {
            this.crypter = crypter;
        }

        @Override
        public String toString() {
            return "crypt." + crypter.getCryptType();
        }


        @Override public Crypter transform(String s, @Nullable Map<Object, ?> extras, @NotNull Class<?> targetClass) {
            if (s.equals("crypt." + crypter.getCryptType()))
                return crypter;
            else
                return null;
        }

        @Override public boolean accept(@NotNull Object o) {
            return o instanceof String;
        }

        @Nullable @Override public String mark() {
            return null;
        }

        @Override public void onError(@NotNull Exception error) {

        }

        @NotNull @Override public String useFor() {
            return "Default";
        }
    }
}
