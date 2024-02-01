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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.daverix.urlforward.db.DefaultFilterDao
import net.daverix.urlforward.db.FilterDao
import net.daverix.urlforward.ui.LinkDialogScreen
import net.daverix.urlforward.ui.UrlForwarderTheme

@AndroidEntryPoint
class LinkDialogActivity : ComponentActivity() {
    private val filterDao: DefaultFilterDao = DefaultFilterDao(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("LinkDialogActivity", "startup")
        val intent = intent
        if (intent == null) {
            Toast.makeText(this, "Invalid intent!", Toast.LENGTH_SHORT).show()
            Log.e("LinkDialogActivity", "Intent empty")
            finish()
            return
        }

        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "No url found in shared data!", Toast.LENGTH_SHORT).show()
            Log.e("LinkDialogActivity", "No StringExtra with url in intent")
            finish()
            return
        }

        filterDao.queryAllRegexFilters().forEach { filter ->
            if (url.matches(Regex(filter.regexPattern))) {
                val group_values = Regex(filter.regexPattern).matchEntire(url)!!.groupValues
                val group_values_without_full_match = group_values.subList(1, group_values.size)
                // List<Pair<\1,first_group_value>>
                val endless_regex_group_strings: List<String> = generateSequence(1) { it + 1}
                    .map { "\\${it}"}
                    .take(group_values_without_full_match.size).toList()

                val regex_group_with_group_values: List<Pair<String,String>> = group_values_without_full_match.zip(endless_regex_group_strings)

                createUrl(filter, url, subject, regex_group_with_group_values)
                Log.e("LinkDialogActivity", filter.regexPattern)
            }
        }

        setContent {
            UrlForwarderTheme {
                LinkDialogScreen(
                    url = url,
                    subject = subject,
                    onItemClick = this::startActivityFromUrl
                )
            }
        }
    }


//        # this
//        val res = "https://archive.is/newest/$url"
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(Intent(Intent.ACTION_VIEW, res.toUri()))
//        finish()

    private fun startActivityFromUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            finish()
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, "No app found matching url $url", Toast.LENGTH_SHORT).show()
            Log.e("LinkDialogActivity", "activity not found for $url", ex)
        } catch (ex: Exception) {
            Toast.makeText(this, "Error forwarding url $url: ${ex.message}", Toast.LENGTH_SHORT)
                .show()
            Log.e(
                "LinkDialogActivity",
                "error launching intent with url $url",
                ex
            )
        }
    }
}