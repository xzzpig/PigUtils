@file:JvmName("MailUtils")

package com.xzzpig.pigutils.mail

import java.io.File
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.*

var MAILDEBUG = false

fun sendMail(host: String, from: String, password: String? = null, block: MailPropertiesBuilder.() -> Unit, block1: MimeMessageBuilder.() -> Unit) {
    val session = Session.getInstance(MailPropertiesBuilder(host).apply(block).props)
    session.debug = MAILDEBUG
    val transport = session.transport
    transport.connect(from, password)
    try {
        MimeMessageBuilder(session, from).apply(block1).mimeMessage.let {
            transport.sendMessage(it, it.allRecipients)
        }
    } catch (e: Exception) {
        throw e
    } finally {
        transport.close()
    }
}

class MailPropertiesBuilder(host: String) {
    val props = Properties().apply { setProperty("mail.smtp.host", host);setProperty("mail.transport.protocol", "smtp") }

    fun enableAuth() {
        props.setProperty("mail.smtp.auth", "true")
    }

    private var smtpPort: Int = 25

    fun port(smtpPort: Int) {
        this.smtpPort = smtpPort
        props.setProperty("mail.smtp.port", smtpPort.toString())
    }

    fun enableSSL() {
        props["mail.smtp.ssl.enable"] = "true"
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        props.setProperty("mail.smtp.socketFactory.fallback", "false")
        props.setProperty("mail.smtp.socketFactory.port", smtpPort.toString())
    }
}

class MimeMessageBuilder(session: Session, private val sender: String) {
    val mimeMessage = MimeMessage(session)

    init {
        mimeMessage.setFrom(sender)
    }

    fun from(name: String) {
        mimeMessage.setFrom(InternetAddress(sender, name, "UTF-8"))
    }

    fun target(mail: String, name: String, type: Message.RecipientType) {
        mimeMessage.addRecipients(type, arrayOf(InternetAddress(mail, name, "UTF-8")))

    }

    fun target(mail: String, name: String) {
        target(mail, name, Message.RecipientType.TO)
    }

    fun bcc(mail: String, name: String) {
        target(mail, name, Message.RecipientType.BCC)
    }

    fun cc(mail: String, name: String) {
        target(mail, name, Message.RecipientType.CC)
    }

    fun subject(str: String) {
        mimeMessage.setSubject(str, "UTF-8")
    }

    inline fun content(block: MimeMultipartBuilder.() -> Unit) {
        mimeMessage.setContent(MimeMultipartBuilder().apply(block).part)
    }
}

class MimeMultipartBuilder(val subType: String = "mixed") {

    val part = MimeMultipart(subType).apply { setSubType(subType) }

    inline fun mixed(block: MimeMultipartBuilder.() -> Unit) {
        part.addBodyPart(MimeBodyPart().apply { setContent(MimeMultipartBuilder().apply(block).build()) })
    }

    inline fun related(block: MimeMultipartBuilder.() -> Unit) {
        part.addBodyPart(MimeBodyPart().apply { setContent(MimeMultipartBuilder("related").apply(block).build()) })
    }

    fun text(str: String) {
        part.addBodyPart(MimeBodyPart().apply { setContent(str, "text/html;charset=UTF-8") })
    }

    fun image(source: DataHandler, contentID: String) {
        part.addBodyPart(MimeBodyPart().apply { dataHandler = source;this.contentID = contentID })
    }

    fun file(source: DataHandler, fileName: String = source.name) {
        part.addBodyPart(MimeBodyPart().apply { dataHandler = source;this.fileName = MimeUtility.encodeText(fileName) })
    }

    operator fun String.unaryPlus() {
        text(this)
    }

    fun String.toDataHandler(): DataHandler = DataHandler(FileDataSource(this))
    fun File.toDataHandler(): DataHandler = DataHandler(FileDataSource(this))

    inline fun build(block: MimeMultipartBuilder.() -> Unit = {}) = part.apply { setSubType(subType) }
}