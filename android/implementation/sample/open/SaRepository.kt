data class SaData(val value: String)

class SaRepository {
    private val _notificationSaData = MutableSharedFlow<SaData>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val notificationSaData = _notificationSaData.asSharedFlow()

    fun tryEmitSaData(data: SaData) {
        _notificationSaData.tryEmit(SaData)
    }
}