package com.github.xzzpig.pigutils.pack.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.xzzpig.pigutils.annoiation.NotNull;
import com.github.xzzpig.pigutils.pack.Package;

public abstract class PackageSocketServer implements Runnable {

	private List<PackageSocket> packageSockets;

	private Map<PackageSocket, Thread> packThreads;
	private final int port;
	ServerSocket ss;
	private AtomicBoolean started;
	private Thread thread;

	/**
	 * 允许的最大连续错误数<br/>
	 * 当接收Package时连续报错数目超过maxErrorCount时自动与客户端断开连接
	 */
	public int maxErrorCount = 10;

	public PackageSocketServer(@NotNull int port) {
		this.port = port;
		started = new AtomicBoolean(false);
		packageSockets = new Vector<>();
		packThreads = new Hashtable<>();
	}

	public synchronized boolean isStarted() {
		return started.get();
	}

	public abstract void onClose(PackageSocket socket);

	public abstract void onError(PackageSocket socket, Exception exception);

	/**
	 * @return 是否与socket断开连接
	 */
	public boolean onGetPackageError(PackageSocket socket, Exception exception) {
		return false;
	}

	public abstract void onOpen(PackageSocket socket);

	public abstract void onPackage(PackageSocket socket, Package pack);

	private void onPSClose(PackageSocket socket) {
		packageSockets.remove(socket);
		packThreads.remove(socket);
		onClose(socket);
	}

	private void onPSOpen(PackageSocket socket) {
		if (socket == null)
			return;
		packageSockets.add(socket);
		onOpen(socket);
		Thread t = new Thread() {
			@Override
			public void run() {
				int errorCounter = 0;
				while (!isInterrupted() && !socket.getSocket().isClosed()) {
					try {
						Package pack = Package.read(socket.getSocket().getInputStream());
						onPackage(socket, pack);
						errorCounter = 0;
					} catch (IOException e) {
						if (e.getMessage().contains("Connection reset"))
							break;
						onError(socket, e);
						if (onGetPackageError(socket, e))
							break;
						errorCounter++;
						if (errorCounter > maxErrorCount)
							break;
					} catch (NegativeArraySizeException e) {
						break;
					}
				}
				onPSClose(socket);
			}
		};
		packThreads.put(socket, t);
		t.start();
	}

	@Override
	public final void run() {
		try {
			ss = new ServerSocket(port);
			started.set(true);
		} catch (IOException e) {
			onError(null, e);
			return;
		}
		while (!thread.isInterrupted()) {
			PackageSocket socket = null;
			try {
				socket = new PackageSocket(ss.accept());
			} catch (IOException e) {
				onError(socket, e);
			}
			onPSOpen(socket);
		}
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		for (Entry<PackageSocket, Thread> entry : packThreads.entrySet()) {
			try {
				entry.getKey().getSocket().close();
			} catch (IOException e) {
				onError(entry.getKey(), e);
			}
			try {
				entry.getValue().interrupt();
			} catch (Exception e) {
			}
			// onPSClose(entry.getKey());
		}
		try {
			ss.close();
		} catch (IOException e) {
			onError(null, e);
		}
		thread.interrupt();
		started.set(false);
		onClose(null);
	}
}
