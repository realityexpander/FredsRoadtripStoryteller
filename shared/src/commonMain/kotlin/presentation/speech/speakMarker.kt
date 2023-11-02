package presentation.speech

import maps.Marker
import maps.RecentlySeenMarker
import speakTextToSpeech

fun speakMarker(
    marker: Marker,
    includeDetails: Boolean = false
): RecentlySeenMarker {
    val currentlySpeakingMarker =
        RecentlySeenMarker(
            marker.id,
            marker.title,
        )

    if(includeDetails) {
        // Speak the marker title and inscription
        val title = marker.title
        val subtitle = marker.subtitle
        val inscription = marker.inscription
        val englishInscription = marker.englishInscription
        val spanishInscription = marker.spanishInscription

        var finalSpeechText = "Marker details"
        if (title.isNotEmpty()) {
            finalSpeechText += " for $title"
        }
        if (subtitle.isNotEmpty()) {
            finalSpeechText += ", with subtitle of $subtitle"
        }
        val inscriptionPrefix = " has inscription reading"
        finalSpeechText += if (englishInscription.isNotEmpty()) {
            " $inscriptionPrefix $englishInscription"
        } else if (inscription.isNotEmpty()) {
            " $inscriptionPrefix $inscription"
        } else if (spanishInscription.isNotEmpty()) {
            " tiene inscripción que dice $spanishInscription"
        } else {
            " and there is no inscription available."
        }

        speakTextToSpeech(finalSpeechText)
    } else {
        speakTextToSpeech(marker.title)
    }

    return currentlySpeakingMarker
}
