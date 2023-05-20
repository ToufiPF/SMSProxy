package ch.epfl.smsproxy.sms

interface MessageSender {
    /**
     * Sends the given message to all configured relays
     */
    fun broadcast(text: String)
}
