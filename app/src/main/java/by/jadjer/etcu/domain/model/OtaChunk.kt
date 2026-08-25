package by.jadjer.etcu.domain.model

data class OtaChunk(
    val data: ByteArray,
    val chunkNumber: Int,
    val totalChunks: Int,
    val firmwareSize: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OtaChunk
        if (!data.contentEquals(other.data)) return false
        if (chunkNumber != other.chunkNumber) return false
        if (totalChunks != other.totalChunks) return false
        if (firmwareSize != other.firmwareSize) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + chunkNumber
        result = 31 * result + totalChunks
        result = 31 * result + firmwareSize.hashCode()
        return result
    }
}
