package de.selfmade4u.tucanplus.connector

import co.touchlab.stately.concurrency.value
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.fillDefaults
import io.ktor.client.plugins.cookies.matches
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.min

@OptIn(ExperimentalAtomicApi::class)
public class PersistentCookiesStorage(private val clock: () -> Long = { getTimeMillis() }) :
    CookiesStorage {

    @Serializable
    private data class CookieWithTimestamp(val cookie: Cookie, val createdAt: Long)

    private val container: MutableList<CookieWithTimestamp> = let {
        val text = File("cookies.log").readText()
        val value: MutableList<CookieWithTimestamp> = Json.decodeFromString(text)
        value
    }
    private val oldestCookie: AtomicLong = AtomicLong(0L)
    private val mutex = Mutex()

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = clock()
        if (now >= oldestCookie.load()) cleanup(now)

        val cookies = container.filter { it.cookie.matches(requestUrl) }.map { it.cookie }
        return@withLock cookies
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        with(cookie) {
            if (name.isBlank()) return
        }

        mutex.withLock {
            container.removeAll { (existingCookie, _) ->
                existingCookie.name == cookie.name && existingCookie.matches(requestUrl)
            }
            val createdAt = clock()
            container.add(CookieWithTimestamp(cookie.fillDefaults(requestUrl), createdAt))

            val json = Json.encodeToString(container)
            File("cookies.log").writeText(json)

            cookie.maxAgeOrExpires(createdAt)?.let {
                if (oldestCookie.load() > it) {
                    oldestCookie.store(it)
                }
            }
        }
    }

    override fun close() {
    }

    private fun cleanup(timestamp: Long) {
        container.removeAll { (cookie, createdAt) ->
            val expires = cookie.maxAgeOrExpires(createdAt) ?: return@removeAll false
            expires < timestamp
        }

        val newOldest = container.fold(Long.MAX_VALUE) { acc, (cookie, createdAt) ->
            cookie.maxAgeOrExpires(createdAt)?.let { min(acc, it) } ?: acc
        }

        oldestCookie.store(newOldest)
    }

    private fun Cookie.maxAgeOrExpires(createdAt: Long): Long? =
        maxAge?.let { createdAt + it * 1000L } ?: expires?.timestamp
}