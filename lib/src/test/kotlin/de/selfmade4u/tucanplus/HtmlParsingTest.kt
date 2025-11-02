package de.selfmade4u.tucanplus

import de.selfmade4u.tucanplus.connector.LoginTest
import de.selfmade4u.tucanplus.connector.ModuleResultsTest
import org.junit.experimental.categories.Categories
import org.junit.experimental.categories.Categories.ExcludeCategory
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses

// https://github.com/junit-team/junit4/wiki/Categories
interface AccessesTucan

@RunWith(Categories::class)
@ExcludeCategory(AccessesTucan::class)
@SuiteClasses(LoginTest::class, ModuleResultsTest::class)
class NoTucanAccess
