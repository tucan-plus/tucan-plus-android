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
            check(attribute == Attribute(key, null)) { "actual   ${attribute}\nexpected ${Attribute(key, null)}" }
        } else {
            check(attribute == Attribute.createFromEncoded(key, value)) { "actual   ${attribute}\nexpected ${Attribute.createFromEncoded(key, value)}" }
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
        check(next.getWholeData().hashedWithSha256() == hash) { "${next.getWholeData().hashedWithSha256()} $${next.getWholeData()}$" }
    }
    fun extractData(): String {
        check(attributes.isEmpty()) { attributes.removeAt(0) }
        val next = this.children.removeAt(0)
        check(next is DataNode) { next }
        return next.getWholeData()
    }
}

class Root(nodeList: MutableList<Node>) : HtmlTag(nodeList, mutableListOf())
class Doctype(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Html(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Head(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Body(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Title(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Meta(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Link(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)
class Script(nodeList: MutableList<Node>, attributes: MutableList<Attribute>) : HtmlTag(nodeList, attributes)

fun shouldIgnore(node: Node): Boolean =
    node is Comment || (node is TextNode && node.text().replace("\\r\\n", "").trim().isEmpty())

class Response(val response: HttpResponse, var headers: MutableMap<String, List<String>>, var checkedStatus: Boolean = false) {
    fun status(status: HttpStatusCode) {
        check(response.status == status) { "actual   ${response.status} expected $status" }
        checkedStatus = true
    }

    fun maybeHeader(key: String, values: List<String>) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key);
        check(actualValue == null || actualValue == values) { "actual   $actualValue\nexpected $values"}
    }

    fun header(key: String, values: List<String>) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key);
        check(actualValue == values) { "actual   $actualValue\nexpected $values"}
    }

    fun header(key: String, values: String) {
        header(key, listOf(values))
    }

    fun ignoreHeader(key: String) {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        val actualValue = headers.remove(key)
        check(actualValue != null) { "expected header with key $key" }
    }

    fun hasHeader(key: String): Boolean {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        return headers.containsKey(key)
    }

    fun extractHeader(key: String): List<String> {
        check(checkedStatus) { "you need to check the status before checking the headers" }
        return headers.remove(key)!!
    }

    suspend fun <T> root(init: Root.() -> T): T {
        check(headers.isEmpty()) { "unparsed headers $headers" }
        val document =  Ksoup.parse(response.bodyAsText())
        check(document.nameIs("#root")) { document.normalName() }
        check(document.attributesSize() == 0) { document.attributes() }
        val node = Root( document.childNodes()
            .filterNot(::shouldIgnore)
            .toMutableList())
        return node.init()
    }

}

@OptIn(ExperimentalUuidApi::class)
suspend fun <T> response(context: Context? = null, response: HttpResponse, init: suspend Response.() -> T): T {
    try {
        return Response(response, response.headers.toMap().toMutableMap()).init()
    } catch (e: IllegalStateException) {
        // cd app/src/test/resources
        // adb pull /data/data/de.selfmade4u.tucanplus/files/parsingerrors/
        if (context != null) {
            val dir = File(context.filesDir, "parsingerrors")
            dir.mkdirs()
            val fileOutputStream = FileOutputStream(File(dir,  "error${Uuid.random()}.html"))
            fileOutputStream.use {
                it.write( response.bodyAsText().toByteArray())
            }
            val db = Room.databaseBuilder(
                context,
                ParsingErrorsDatabase::class.java, "parsing-errors"
            ).fallbackToDestructiveMigration(true).build()
            db.parsingErrorDao().insertAll(
                ParsingError(
                    0,
                    response.request.url.toString(),
                    e.toString(),
                    response.bodyAsText()
                )
            )
        }
        throw e
    }
}

fun <T> root(document: Document, init: Root.() -> T): T {
    check(document.nameIs("#root")) { document.normalName() }
    check(document.attributesSize() == 0) { document.attributes() }
    val node = Root( document.childNodes()
        .filterNot(::shouldIgnore)
        .toMutableList())
    return node.init()
}

fun <T> Root.doctype(init: Doctype.() -> T): T = initTag("#doctype", ::Doctype, init)
fun <R> Root.html(init: Html.() -> R): R = initTag("html", ::Html, init)
fun <R> Html.head(init: Head.() -> R): R = initTag("head", ::Head, init)
fun <R> Html.body(init: Body.() -> R): R = initTag("body", ::Body, init)
fun <R> Head.title(init: Title.() -> R): R = initTag("title", ::Title, init)
fun <R> Head.meta(init: Meta.() -> R): R = initTag("meta", ::Meta, init)
fun <R> Head.link(init: Link.() -> R): R = initTag("link", ::Link, init)
fun <R> Head.script(init: Script.() -> R): R = initTag("script", ::Script, init)
fun <R> Head.style(init: Head.() -> R): R = initTag("style", ::Head, init)

fun <R> Body.script(init: Script.() -> R): R = initTag("script", ::Script, init)
fun <R> Body.style(init: Body.() -> R): R = initTag("style", ::Body, init)
fun <R> Body.a(init: Body.() -> R): R {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return initTag("a", ::Body, init)
}

fun <R> Body.div(init: Body.() -> R): R = initTag("div", ::Body, init)
fun <R> Body.form(init: Body.() -> R): R = initTag("form", ::Body, init)
fun <R> Body.fieldset(init: Body.() -> R): R = initTag("fieldset", ::Body, init)
fun <R> Body.img(init: Body.() -> R): R = initTag("img", ::Body, init)
fun <R> Body.legend(init: Body.() -> R): R = initTag("legend", ::Body, init)
fun <R> Body.label(init: Body.() -> R): R = initTag("label", ::Body, init)
fun <R> Body.h1(init: Body.() -> R): R = initTag("h1", ::Body, init)
fun <R> Body.p(init: Body.() -> R): R = initTag("p", ::Body, init)
fun <R> Body.ul(init: Body.() -> R): R = initTag("ul", ::Body, init)
fun <R> Body.li(init: Body.() -> R): R = initTag("li", ::Body, init)
fun <R> Body.header(init: Body.() -> R): R = initTag("header", ::Body, init)
fun <R> Body.span(init: Body.() -> R): R = initTag("span", ::Body, init)
fun <R> Body.b(init: Body.() -> R): R = initTag("b", ::Body, init)
fun <R> Body.br(init: Body.() -> R): R = initTag("br", ::Body, init)
fun <R> Body.option(init: Body.() -> R): R { contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }; return initTag("option", ::Body, init) }
fun <R> Body.input(init: Body.() -> R): R = initTag("input", ::Body, init)
fun <R> Body.select(init: Body.() -> R): R = initTag("select", ::Body, init)
fun <R> Body.table(init: Body.() -> R): R = initTag("table", ::Body, init)
fun <R> Body.thead(init: Body.() -> R): R = initTag("thead", ::Body, init)
fun <R> Body.tbody(init: Body.() -> R): R = initTag("tbody", ::Body, init)

@OptIn(ExperimentalContracts::class)
fun <R> Body.tr(init: Body.() -> R): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return initTag("tr", ::Body, init)
}

@OptIn(ExperimentalContracts::class)
fun <R> Body.td(init: Body.() -> R): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return initTag("td", ::Body, init)
}
fun <R> Body.th(init: Body.() -> R): R = initTag("th", ::Body, init)

fun HtmlTag.peek(): Node? {
    return this.children.firstOrNull()
}

fun HtmlTag.peekAttribute(): Attribute? {
    return this.attributes.firstOrNull()
}

@OptIn(ExperimentalContracts::class)
fun <P: HtmlTag, C, R> P.initTag(tag: String, createTag: (iterator: MutableList<Node>, attributes: MutableList<Attribute>) -> C, init: C.() -> R): R {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    if (this.children.isEmpty()) {
        throw IllegalStateException("actual no children, expected at least one")
    }
    val next = this.children.removeAt(0)
    check(next.nameIs(tag)) { "actual   ${next.normalName()} expected $tag" }
    val attributes = next.attributes().toMutableList()
    val childIterator = next.childNodes().filterNot(::shouldIgnore).toMutableList();
    val node = createTag(childIterator, attributes)
    val ret = node.init()
    check(attributes.isEmpty()) { "${next.normalName()} unparsed attributes ${attributes.removeAt(0)}" }
    check(childIterator.isEmpty()) { "unparsed children in $tag ${childIterator}" }
    return ret
}
