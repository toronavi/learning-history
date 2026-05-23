data class SaData(val value: String)

class SaRepository {
    private val _notificationSaData = MutableSharedFlow<SaData>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val notificationSaData = _notificationSaData.asSharedFlow()

    suspend fun emitSaData(data: SaData) {
        _notificationSaData.emit(SaData)
    }
}