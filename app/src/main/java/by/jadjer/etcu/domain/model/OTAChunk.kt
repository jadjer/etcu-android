package by.jadjer.etcu.domain.model

data class OTAChunk(
    val firmwareSize: Long,
    val totalChunks: Int,
    val chunkNumber: Int,
    val chunkSize: Int,
    val data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OTAChunk

        if (firmwareSize != other.firmwareSize) return false
        if (totalChunks != other.totalChunks) return false
        if (chunkNumber != other.chunkNumber) return false
        if (chunkSize != other.chunkSize) return false
        if (!data.contentEquals(other.data)) return false

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
