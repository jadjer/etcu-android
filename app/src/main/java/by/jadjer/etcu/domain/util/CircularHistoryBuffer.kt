package by.jadjer.etcu.domain.util

import by.jadjer.etcu.domain.model.telemetry.HistoryRecord
import by.jadjer.etcu.domain.model.telemetry.TelemetryConstants

class CircularHistoryBuffer<T> : List<HistoryRecord<T>>, RandomAccess {
    private val deque = ArrayDeque<HistoryRecord<T>>()

    @Synchronized
    fun add(data: T, timestamp: Long) {
        val cutoff = timestamp - TelemetryConstants.HISTORY_DURATION_MS
        
        // Remove old records
        while (deque.isNotEmpty() && deque.first().timestamp < cutoff) {
            deque.removeFirst()
        }
        
        deque.addLast(HistoryRecord(data, timestamp))
    }

    override val size: Int @Synchronized get() = deque.size

    @Synchronized
    override fun get(index: Int): HistoryRecord<T> = deque[index]

    @Synchronized
    override fun isEmpty(): Boolean = deque.isEmpty()

    @Synchronized
    override fun iterator(): Iterator<HistoryRecord<T>> = ArrayList(deque).iterator()

    @Synchronized
    override fun listIterator(): ListIterator<HistoryRecord<T>> = ArrayList(deque).listIterator()

    @Synchronized
    override fun listIterator(index: Int): ListIterator<HistoryRecord<T>> = ArrayList(deque).listIterator(index)

    @Synchronized
    override fun subList(fromIndex: Int, toIndex: Int): List<HistoryRecord<T>> = ArrayList(deque).subList(fromIndex, toIndex)

    @Synchronized
    override fun lastIndexOf(element: HistoryRecord<T>): Int = deque.lastIndexOf(element)

    @Synchronized
    override fun indexOf(element: HistoryRecord<T>): Int = deque.indexOf(element)

    @Synchronized
    override fun containsAll(elements: Collection<HistoryRecord<T>>): Boolean = deque.containsAll(elements)

    @Synchronized
    override fun contains(element: HistoryRecord<T>): Boolean = deque.contains(element)
}
