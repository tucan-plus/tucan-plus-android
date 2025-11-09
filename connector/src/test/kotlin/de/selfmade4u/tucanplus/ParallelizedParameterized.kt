package de.selfmade4u.tucanplus

import org.junit.runners.Parameterized
import org.junit.runners.model.RunnerScheduler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class ParallelizedParameterized(klass: Class<*>?) : Parameterized(klass) {
    private class ThreadPoolScheduler : RunnerScheduler {
        private val executor: ExecutorService

        init {
            val threads = System.getProperty("junit.parallel.threads", "16")
            val numThreads = threads.toInt()
            executor = Executors.newFixedThreadPool(numThreads)
        }

        override fun finished() {
            executor.shutdown()
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES)
            } catch (exc: InterruptedException) {
                throw RuntimeException(exc)
            }
        }

        override fun schedule(childStatement: Runnable) {
            executor.submit(childStatement)
        }
    }

    init {
        setScheduler(ThreadPoolScheduler())
    }
}