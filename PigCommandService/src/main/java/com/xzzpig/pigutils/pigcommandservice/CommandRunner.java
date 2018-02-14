package com.xzzpig.pigutils.pigcommandservice;

import com.xzzpig.pigutils.json.JSONObject;

public interface CommandRunner {
	/**
	 * 执行命令
	 * 
	 * @param cmd
	 * @param args
	 * @return 异常(null未无异常)
	 */
    JSONObject run(String cmd, JSONObject args);
}
