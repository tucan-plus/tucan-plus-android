package de.selfmade4u.tucanplus

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import de.selfmade4u.tucanplus.connector.LoginTest
import de.selfmade4u.tucanplus.connector.ModuleResultsTest
import de.selfmade4u.tucanplus.connector.TucanLogin
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginFailure
import de.selfmade4u.tucanplus.connector.TucanLogin.parseLoginSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Categories
import org.junit.experimental.categories.Categories.ExcludeCategory
import org.junit.experimental.categories.Categories.IncludeCategory
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

// https://github.com/junit-team/junit4/wiki/Categories
interface AccessesTucan {

}

@RunWith(Categories::class)
@ExcludeCategory(AccessesTucan::class)
@SuiteClasses(LoginTest::class, ModuleResultsTest::class)
class NoTucanAccess
