@file:OptIn(ExperimentalContracts::class)

package de.selfmade4u.tucanplus

import android.content.Context
import androidx.room.Room
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Attribute
import com.fleeksoft.ksoup.nodes.Comment
import com.fleeksoft.ksoup.nodes.DataNode
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.util.toMap
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


// https://kotlinlang.org/docs/type-safe-builders.html#how-it-works
// https://kotlinlang.org/docs/ksp-overview.html

// https://github.com/fleeksoft/ksoup
// https://github.com/skrapeit/skrape.it - no multiplatform yet
// https://github.com/MohamedRejeb/Ksoup https://github.com/MohamedRejeb/Ksoup/issues/26 seems like it only has a event based api

fun String.hashedWithSha256() =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .toHexString()

@DslMarker
annotation class HtmlTagMarker

@HtmlTagMarker
abstract class HtmlTag(val children: MutableList<Node>, val attributes: MutableList<Attribute>) {
    fun attribute(key: String, value: String?) {
        val attribute = attributes.removeAt(0)
        if (attribute.key == "class") {
            attribute.setValue(attribute.value.trim())
        }
        if (value == null) {
            check(attribute == Attribute(key, null)) {
                "actual   ${attribute}\nexpected ${
                    Attribute(
                        key,
                        null
                    )
                }"
            }
        } else {
            check(
                attribute == Attribute.createFromEncoded(
                    key,
                    value
                )
            ) { "actual   ${attribute}\nexpected ${Attribute.createFromEncoded(key, value)}" }
        }
    }

    fun attributeValue(key: String): String {
        val attribute = attributes.removeAt(0)
        if (attribute.key == "class") {
            attribute.setValue(attribute.value.trim())
        }
        check(attribute.key == key) { "actual   ${attribute.key}\nexpected $key" }
        return attribute.value
    }

    fun extractText(): String {
        check(attributes.isEmpty()) { attributes.removeAt(0) }
        val next = this.children.removeAt(0)
        check(next is TextNode) { next }
        return next.text().trim()
    }

    fun text(text: String) {
        check(text.trim().isNotEmpty()) { "expected text cannot be empty" }
        check(attributes.isEmpty()) { attributes.removeAt(0) }
        val next = this.children.removeAt(0)
        check(next is TextNode) { next }
        check(next.text().trim() == text) { "actual   $${next.text().trim()}$\nexpected $${text}$" }
    }

    fun dataHash(hash: String) {
        check(attributes.isEmpty()) { attributes.removeAt(0) }
        val next = this.children.removeAt(0)
        check(next is DataNode) { next }
        check(next.getWholeData().hashedWithSha256() == hash) {
            "${
                next.getWholeData().hashedWithSha256()
            } $${next.getWholeData()}$"
        }
    }

    fun extractData(): String {
        check(attributes.isEmpty()) { attributes.removeAt(0) }
        val next = this.children.removeAt(0)
        check(next is DataNode) { next }
        return next.getWholeData()
    }
}

class Root(nodeList: MutableList<Node>) : HtmlTag(nodeList, mutableListOf())
class Doctype(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Html(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Head(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Body(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Title(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Meta(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Link(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

class Script(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) :
    HtmlTag(nodeList, attributes)

fun shouldIgnore(node: Node): Boolean =
    node is Comment || (node is TextNode && node.text().replace("\\r\\n", "").trim().isEmpty())

class Response(
    val response: HttpResponse,
    var headers: MutableMap<String, List<String>>,
    var checkedStatus: Boolean = false
) {
    fun status(status: HttpStatusCode) {
        check(response.status == status) { "actual   ${response.status} expected $status" }
        checkedStatus = true
    }

    fun maybeHeader(key: String, values: List<String>) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key.lowercase())
        check(actualValue == null || actualValue == values) { "actual   $actualValue\nexpected $values" }
    }

    fun header(key: String, values: List<String>) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key.lowercase())
        check(actualValue == values) { "actual   $actualValue\nexpected $values\nremaining $headers" }
    }

    fun header(key: String, values: String) {
        header(key, listOf(values))
    }

    fun ignoreHeader(key: String) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key.lowercase())
        check(actualValue != null) { "expected header with key $key\nremaining $headers" }
    }

    fun hasHeader(key: String): Boolean {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        return headers.containsKey(key.lowercase())
    }

    fun extractHeader(key: String): List<String> {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        return headers.remove(key.lowercase())!!
    }

    suspend fun <T> root(init: suspend Root.() -> T): T {
        check(headers.isEmpty()) { "unparsed headers $headers" }
        val document = Ksoup.parse(response.bodyAsText())
        check(document.nameIs("#root")) { document.normalName() }
        check(document.attributesSize() == 0) { document.attributes() }
        val node = Root(
            document.childNodes()
                .filterNot(::shouldIgnore)
                .toMutableList()
        )
        return node.init()
    }

}

@OptIn(ExperimentalUuidApi::class)
suspend fun <T> response(
    context: Context? = null,
    response: HttpResponse,
    init: suspend Response.() -> T
): T {
    val db = context?.let {
        MyDatabase.getDatabase(context)
    }
    try {
        val result = Response(response, response.headers.toMap().toMutableMap()).init()
        db?.cacheDao()?.insertAll(
            CacheEntry(
                0,
                response.request.url.toString(),
                response.request.url.toString(), // TODO FIXME at least remove session id
                response.bodyAsText(),
                LocalDateTime.now(Clock.systemUTC()),
                null
            )
        )
        return result
    } catch (e: IllegalStateException) {
        // cd app/src/test/resources
        // adb pull /data/data/de.selfmade4u.tucanplus/files/parsingerrors/
        if (context != null) {
            val dir = File(context.filesDir, "parsingerrors")
            dir.mkdirs()
            val fileOutputStream = FileOutputStream(File(dir, "error${Uuid.random()}.html"))
            fileOutputStream.use {
                it.write(response.bodyAsText().toByteArray())
            }
            db?.cacheDao()?.insertAll(
                CacheEntry(
                    0,
                    response.request.url.toString(),
                    response.request.url.toString(),
                    response.bodyAsText(),
                    LocalDateTime.now(Clock.systemUTC()),
                    e.toString(),
                )
            )
        }
        throw e
    }
}

fun <T> root(document: Document, init: Root.() -> T): T {
    check(document.nameIs("#root")) { document.normalName() }
    check(document.attributesSize() == 0) { document.attributes() }
    val node = Root(
        document.childNodes()
            .filterNot(::shouldIgnore)
            .toMutableList()
    )
    return node.init()
}

suspend fun <T> Root.doctype(init: suspend Doctype.() -> T): T = initTag("#doctype", ::Doctype, init)
suspend fun <R> Root.html(init: suspend Html.() -> R): R = initTag("html", ::Html, init)
suspend fun <R> Html.head(init: suspend Head.() -> R): R = initTag("head", ::Head, init)
suspend fun <R> Html.body(init: suspend Body.() -> R): R = initTag("body", ::Body, init)
suspend fun <R> Head.title(init: suspend Title.() -> R): R = initTag("title", ::Title, init)
suspend fun <R> Head.meta(init: suspend Meta.() -> R): R = initTag("meta", ::Meta, init)
suspend fun <R> Head.link(init: suspend Link.() -> R): R = initTag("link", ::Link, init)
suspend fun <R> Head.script(init: suspend Script.() -> R): R = initTag("script", ::Script, init)
suspend fun <R> Head.style(init: suspend Head.() -> R): R = initTag("style", ::Head, init)

suspend fun <R> Body.script(init: suspend Script.() -> R): R = initTag("script", ::Script, init)
suspend fun <R> Body.style(init: suspend Body.() -> R): R = initTag("style", ::Body, init)
suspend fun <R> Body.a(init: suspend Body.() -> R): R {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return initTag("a", ::Body, init)
}

suspend fun <R> Body.div(init: suspend Body.() -> R): R = initTag("div", ::Body, init)
suspend fun <R> Body.form(init: suspend Body.() -> R): R = initTag("form", ::Body, init)
suspend fun <R> Body.fieldset(init: suspend Body.() -> R): R = initTag("fieldset", ::Body, init)
suspend fun <R> Body.img(init: suspend Body.() -> R): R = initTag("img", ::Body, init)
suspend fun <R> Body.legend(init: suspend Body.() -> R): R = initTag("legend", ::Body, init)
suspend fun <R> Body.label(init: suspend Body.() -> R): R = initTag("label", ::Body, init)
suspend fun <R> Body.h1(init: suspend Body.() -> R): R = initTag("h1", ::Body, init)
suspend fun <R> Body.p(init: suspend Body.() -> R): R = initTag("p", ::Body, init)
suspend fun <R> Body.ul(init: suspend Body.() -> R): R = initTag("ul", ::Body, init)
suspend fun <R> Body.li(init: suspend Body.() -> R): R = initTag("li", ::Body, init)
suspend fun <R> Body.header(init: suspend Body.() -> R): R = initTag("header", ::Body, init)
suspend fun <R> Body.span(init: suspend Body.() -> R): R = initTag("span", ::Body, init)
suspend fun <R> Body.b(init: suspend Body.() -> R): R = initTag("b", ::Body, init)
suspend fun <R> Body.br(init: suspend Body.() -> R): R = initTag("br", ::Body, init)
suspend fun <R> Body.option(init: suspend Body.() -> R): R {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }; return initTag(
        "option",
        ::Body,
        init
    )
}

suspend fun <R> Body.input(init: suspend Body.() -> R): R = initTag("input", ::Body, init)
suspend fun <R> Body.select(init: suspend Body.() -> R): R = initTag("select", ::Body, init)
suspend fun <R> Body.table(init: suspend Body.() -> R): R = initTag("table", ::Body, init)
suspend fun <R> Body.thead(init: suspend Body.() -> R): R = initTag("thead", ::Body, init)
suspend fun <R> Body.tbody(init: suspend Body.() -> R): R = initTag("tbody", ::Body, init)

@OptIn(ExperimentalContracts::class)
suspend fun <R> Body.tr(init: suspend Body.() -> R): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return initTag("tr", ::Body, init)
}

@OptIn(ExperimentalContracts::class)
suspend fun <R> Body.td(init: suspend Body.() -> R): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return initTag("td", ::Body, init)
}

suspend fun <R> Body.th(init: suspend Body.() -> R): R = initTag("th", ::Body, init)

fun HtmlTag.peek(): Node? {
    return this.children.firstOrNull()
}

fun HtmlTag.peekAttribute(): Attribute? {
    return this.attributes.firstOrNull()
}

@OptIn(ExperimentalContracts::class)
suspend fun <P : HtmlTag, C, R> P.initTag(
    tag: String,
    createTag: (iterator: MutableList<Node>, attributes: MutableList<Attribute>) -> C,
    init: suspend  C.() -> R
): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    if (this.children.isEmpty()) {
        throw IllegalStateException("actual no children, expected at least one")
    }
    val next = this.children.removeAt(0)
    check(next.nameIs(tag)) { "actual   ${next.normalName()} expected $tag" }
    val attributes = next.attributes().toMutableList()
    val childIterator = next.childNodes().filterNot(::shouldIgnore).toMutableList()
    val node = createTag(childIterator, attributes)
    val ret = node.init()
    check(attributes.isEmpty()) { "${next.normalName()} unparsed attributes ${attributes.removeAt(0)}" }
    check(childIterator.isEmpty()) { "unparsed children in $tag ${childIterator}" }
    return ret
}
