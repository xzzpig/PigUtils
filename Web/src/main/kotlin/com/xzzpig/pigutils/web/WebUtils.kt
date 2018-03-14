@file:JvmName("WebUtils")

package com.xzzpig.pigutils.web

import com.xzzpig.pigutils.annotation.NotTested
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

class RequestAttributes(val request: ServletRequest) {
    operator fun get(name: String): Any? = request.getAttribute(name)
    operator fun set(name: String, value: Any?) {
        request.setAttribute(name, value)
    }
}

class RequestParameters(val request: ServletRequest) {
    operator fun get(name: String): String? = request.getParameter(name)
}

val ServletRequest.attributes: RequestAttributes
    get() = RequestAttributes(this)

val ServletRequest.parmeters: RequestParameters
    get() = RequestParameters(this)

@NotTested
val HttpServletRequest.realIP: String
    get() =
        if (this.getHeader("x-forwarded-for") == null) this.remoteAddr
        else this.getHeader("x-forwarded-for")

@NotTested
val HttpServletRequest.realHost: String
    get() {
        var ip: String? = getHeader("x-forwarded-for")
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = remoteAddr
        }
        return if (ip == "0:0:0:0:0:0:0:1") "127.0.0.1" else ip ?: ""
    }