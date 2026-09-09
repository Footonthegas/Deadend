package com.sih26168.app.sensors

/**
 * A time-ordered, bounded circular buffer for sensor samples.
 */
class SensorBuffer<T>(private val capacity: Int) {
    private val buffer = ArrayList<T>(capacity)

    @Synchronized
    fun add(sample: T) {
        if (buffer.size >= capacity) {
            buffer.removeAt(0) // Simple eviction of oldest
        }
        buffer.add(sample)
    }

    @Synchronized
    fun getLatest(): T? {
        return buffer.lastOrNull()
    }
    
    @Synchronized
    fun getAll(): List<T> {
        return buffer.toList()
    }
    
    @Synchronized
    fun clear() {
        buffer.clear()
    }
}
