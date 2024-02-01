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

import java.net.URLEncoder

fun createUrl(linkFilter: LinkFilter, url: String?, subject: String?, other_matches: List<Pair<String, String>>?): String {
    var filteredUrl = linkFilter.filterUrl


    val replaceText = linkFilter.replaceText
    if (replaceText.isNotEmpty() && url != null) {
        val encodedUrl = if (linkFilter.encoded) URLEncoder.encode(url, "UTF-8") else url
        filteredUrl = filteredUrl.replace(replaceText, encodedUrl)
    }

    val replaceSubject = linkFilter.replaceSubject
    if (replaceSubject.isNotEmpty() && subject != null) {
        filteredUrl = filteredUrl.replace(replaceSubject, URLEncoder.encode(subject, "UTF-8"))
    }

    if (other_matches != null  && other_matches.isNotEmpty() && url != null) {
        for ((to_replace, replacement) in other_matches) {
            filteredUrl = filteredUrl.replace(to_replace, replacement)
        }
    }

    return filteredUrl
}
