package ch.epfl.smsproxy.sms

interface MessageSender {
    /**
     * Sends the given message to all configured relays
     */
    suspend fun broadcast(text: String)
}
