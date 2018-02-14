package com.xzzpig.pigutils.pigsimpleweb.event;

import com.xzzpig.pigutils.event.Event;
import com.xzzpig.pigutils.pigsimpleweb.PigSimpleWebServer;

public abstract class PigSWSEvent extends Event {
	private PigSimpleWebServer psws;

	protected PigSWSEvent(PigSimpleWebServer psws) {
		this.psws = psws;

	}

	public PigSimpleWebServer getPigSimpleWebServer() {
		return psws;
	}
}
