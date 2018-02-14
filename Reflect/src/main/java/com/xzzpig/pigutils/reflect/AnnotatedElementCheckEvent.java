package com.xzzpig.pigutils.reflect;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;
import com.xzzpig.pigutils.event.Event;

import java.lang.reflect.AnnotatedElement;

/**
 *	当 {@link MethodUtils#checkArgs(Object...)}调用时触发
 */
public class AnnotatedElementCheckEvent extends Event {

    public interface AnnotatedElementChecker {

		/**
		 * @param element
		 * @param arg
		 * @return 是否检查成功
		 */
		boolean check(AnnotatedElement element, Object arg);

	}

	public static void regAnnotatedElementChecker(AnnotatedElementChecker checker) {
		Event.regRunner((AnnotatedElementCheckEvent e) -> {
			if (!checker.check(e.element, e.getArg())) {
				e.setCheckFailed(true);
				e.setCanceled(true);
			}
		});
	}

	private Object arg;
	private AnnotatedElement element;
	private boolean fail;

	public AnnotatedElementCheckEvent(@NotNull AnnotatedElement element, @Nullable Object arg) {
		this.arg = arg;
		this.element = element;
	}

	public AnnotatedElement getAnnotatedElement() {
		return element;
	}

	public Object getArg() {
		return arg;
	}

	public boolean isFailed() {
		return fail;
	}

	public AnnotatedElementCheckEvent setCheckFailed(boolean bool) {
		this.fail = bool;
		return this;
	}
}
