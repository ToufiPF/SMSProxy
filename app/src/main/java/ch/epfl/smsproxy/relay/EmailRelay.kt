package ch.epfl.smsproxy.relay

class EmailRelay(
    private val emailService: EmailService,
    private val destinationAddress: String,
) : Relay {

    override fun relay(text: String) {
        emailService.send(arrayOf(destinationAddress), arrayOf(), arrayOf(), "Forwarded SMS", text)
    }
}
