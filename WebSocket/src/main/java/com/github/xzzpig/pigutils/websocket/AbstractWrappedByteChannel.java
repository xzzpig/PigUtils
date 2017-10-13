package com.github.xzzpig.pigutils.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLException;

public class AbstractWrappedByteChannel implements WrappedByteChannel {

	private final ByteChannel channel;

	public AbstractWrappedByteChannel(ByteChannel towrap) {
		this.channel = towrap;
	}

	public AbstractWrappedByteChannel(WrappedByteChannel towrap) {
		this.channel = towrap;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public boolean isBlocking() {
		if (channel instanceof SocketChannel)
			return ((SocketChannel) channel).isBlocking();
		else if (channel instanceof WrappedByteChannel)
			return ((WrappedByteChannel) channel).isBlocking();
		return false;
	}

	@Override
	public boolean isNeedRead() {
        return channel instanceof WrappedByteChannel && ((WrappedByteChannel) channel).isNeedRead();

	}

	@Override
	public boolean isNeedWrite() {
        return channel instanceof WrappedByteChannel && ((WrappedByteChannel) channel).isNeedWrite();
    }

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	@Override
	public int readMore(ByteBuffer dst) throws SSLException {
		return channel instanceof WrappedByteChannel ? ((WrappedByteChannel) channel).readMore(dst) : 0;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return channel.write(src);
	}

	@Override
	public void writeMore() throws IOException {
		if (channel instanceof WrappedByteChannel)
			((WrappedByteChannel) channel).writeMore();

	}

}
