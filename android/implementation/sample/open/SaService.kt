package com.tororontyo.playground.mock.playground

class MessagingService () {

    private val saRepository = SaRepository.getSharedRepository()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val saData = remoteMessage.data["SaData"] ?: return

        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        applicationScope.launch {
            saRepository.emitSaData(saData)
        }
    }
}