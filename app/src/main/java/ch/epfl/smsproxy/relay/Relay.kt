package ch.epfl.smsproxy.relay

/**
 * Interface of a Relay, that is, a object that can relay a message via some particular mean
 * (e.g., email, slack, whatsapp...)
 */
fun interface Relay {

    /**
     * Sends the text to the configured remote.
     */
    fun relay(text: String)
}
