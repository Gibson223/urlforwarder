/*
    UrlForwarder makes it possible to use bookmarklets on Android
    Copyright (C) 2016 David Laurell

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daverix.urlforward

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.core.AllOf.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder
import java.util.*

@RunWith(AndroidJUnit4::class)
class LinkDialogActivityTest {
    @Test
    fun shouldStartIntentWithCorrectUri() {
        val createFilterScenario = launch(FiltersActivity::class.java)
        val filterName = "MyTestFilter-" + UUID.randomUUID()

        clickAddFilter()

        setFilterData(filterName, "http://daverix.net/test.php?url=@uri&subject=@subject", "@uri", "@subject")

        save()

        Intents.init()
        launch<Activity>(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "http://example.com")
            putExtra(Intent.EXTRA_SUBJECT, "Example")
            setClassName("net.daverix.urlforward", "net.daverix.urlforward.LinkDialogActivity")
        })

        onData(LinkFilterMatcher(filterName)).perform(click())

        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("http://daverix.net/test.php?url=" + URLEncoder.encode("http://example.com", "UTF-8") + "&subject=Example")
        ))
        Intents.release()
    }

    @Test
    fun shouldStartIntentWithCorrectUriWhenUriNotEncoded() {
        val createFilterScenario = launch(FiltersActivity::class.java)
        val filterName = "MyTestFilter-" + UUID.randomUUID()

        clickAddFilter()

        setFilterData(filterName, "http://daverix.net/test/@uri", "@uri", "")

        clickEncodeCheckbox()

        save()

        Intents.init()
        launch<Activity>(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "http://example2.com")
            setClassName("net.daverix.urlforward", "net.daverix.urlforward.LinkDialogActivity")
        })

        onData(LinkFilterMatcher(filterName)).perform(click())

        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("http://daverix.net/test/http://example2.com")
        ))
        Intents.release()
    }

    class LinkFilterMatcher(
            private val filterName: String
    ) : BoundedMatcher<Any, LinkFilter>(LinkFilter::class.java) {
        override fun matchesSafely(item: LinkFilter): Boolean = filterName == item.title

        override fun describeTo(description: Description) {
            description.appendText("with title: ").appendValue(filterName)
        }
    }
}
