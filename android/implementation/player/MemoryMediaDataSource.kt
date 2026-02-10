import android.media.MediaDataSource

class MemoryMediaDataSource(private val data: ByteArray) : MediaDataSource() {
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= data.size) return -1
        val length = if (position + size > data.size) (data.size - position).toInt() else size
        System.arraycopy(data, position.toInt(), buffer, offset, length)
        return length
    }
    override fun getSize(): Long = data.size.toLong()
    override fun close() {}
}