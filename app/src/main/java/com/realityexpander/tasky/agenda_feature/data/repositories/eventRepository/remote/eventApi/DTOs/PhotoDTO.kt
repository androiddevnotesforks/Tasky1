@file:Suppress("PackageName")
package com.realityexpander.tasky.agenda_feature.data.repositories.eventRepository.remote.eventApi.DTOs

import android.net.Uri
import com.realityexpander.tasky.agenda_feature.domain.PhotoId
import com.realityexpander.tasky.agenda_feature.domain.UrlStr
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

sealed interface PhotoDTO {

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)  // for @JsonNames
    data class Remote(
        @JsonNames("key")   // input from json
        val id: PhotoId,
        val url: UrlStr,      // url of the photo on server
    ) : PhotoDTO

    data class Local(         // Only used for uploading. NOT sent from/to server.
        val id: PhotoId,
        val uri: Uri,         // path to the photo on device
    ) : PhotoDTO
}
