package com.xzzpig.pigutils.web

import java.lang.reflect.Method
import java.nio.charset.Charset
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

open class BaseServlet(val methodKey: String = "method", val defaultCharset: Charset = Charsets.UTF_8, val recodeGetParam: Boolean = true, val oldCharset: Charset = Charsets.ISO_8859_1) : HttpServlet() {


    private val methodMap = mutableMapOf<String, Method>()

    /**
     * 在调用实际方法前调用
     */
    open fun doInit(method: String, req: HttpServletRequest, resp: HttpServletResponse) {}

    /**
     * 在没有方法匹配时调用
     * */
    open fun doElse(method: String, req: HttpServletRequest, resp: HttpServletResponse) {}

    open fun doDefault(req: HttpServletRequest, resp: HttpServletResponse): String? = null

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if (recodeGetParam) {
            doPost(object : HttpServletRequestWrapper(req) {
                override fun getParameter(name: String?): String? {
                    return req.getParameter(name)?.toByteArray(oldCharset)?.toString(defaultCharset)
                }
            }, resp)
        } else
            doPost(req, resp)
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = defaultCharset.name()
        resp.characterEncoding = defaultCharset.name()
        val method = req.getParameter(methodKey)?.takeIf { it.isNotBlank() } ?: "doDefault"
        doInit(method, req, resp)
        val javaMethod = try {
            methodMap.getOrPut(method) {
                this.javaClass.getMethod(method, HttpServletRequest::class.java, HttpServletResponse::class.java)
            }
        } catch (e: NoSuchMethodException) {
            doElse(method, req, resp)
            return
        }
        var result: String = javaMethod(this, req, resp)?.toString()
                ?: return
        var next = 'f'
        if (result.matches(Regex("\\w:.+"))) {
            next = result[0]
            result = result.substring(2)
        }
        when (next) {
            'r' -> resp.sendRedirect(result)
            else -> req.getRequestDispatcher(result).forward(req, resp)
        }
    }
}