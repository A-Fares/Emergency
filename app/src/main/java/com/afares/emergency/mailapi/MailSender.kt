package com.afares.emergency.mailapi

import javax.mail.internet.InternetAddress

import javax.activation.DataHandler

import javax.mail.internet.MimeMessage

import java.security.Security
import java.util.*
import javax.mail.*


class MailSender(private val user: String, private val password: String) : Authenticator() {
    private val mailHost =
        "smtp.gmail.com" //Hostname of the SMTP mail server which you want to connect for sending emails.
    private val session: Session

    companion object {
        init {
            Security.addProvider(JSSEProvider())
        }
    }

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(user, password)
    }

    @Synchronized
    @Throws(Exception::class)
    fun sendMail(
        subject: String?, body: String,
        sender: String?, recipients: String
    ) {
        val message = MimeMessage(session)
        val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
        message.sender = InternetAddress(sender)
        message.subject = subject
        message.dataHandler = handler
        if (recipients.indexOf(',') > 0) message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(recipients)
        ) else message.setRecipient(Message.RecipientType.TO, InternetAddress(recipients))
        Transport.send(message)
    }

    init {
        //Your SMTP username. In case of GMail SMTP this is going to be your GMail email address.
        //Your SMTP password. In case of GMail SMTP this is going to be your GMail password.
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.host", mailHost)
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }
}