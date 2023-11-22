package presentation.speech

import presentation.maps.Marker
import presentation.maps.RecentlySeenMarker
import speakTextToSpeech

fun speakMarker(
    marker: Marker,
    shouldSpeakDetails: Boolean = false,
    onSetUnspokenText: (String) -> Unit = { },
): RecentlySeenMarker {
    val currentlySpeakingMarker =
        RecentlySeenMarker(
            marker.id,
            marker.title,
        )

    if(shouldSpeakDetails) {
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

        if(finalSpeechText.length > 4000) {
            // Trim to the last word boundary before 4000 characters
            val lastWordBoundaryIndex = finalSpeechText.substring(0, 4000).lastIndexOf(" ")
            finalSpeechText = finalSpeechText.substring(0, lastWordBoundaryIndex)

            // Set the unspoken text to the rest of the text
            val restOfFinalSpeechText = finalSpeechText.substring(lastWordBoundaryIndex)
            onSetUnspokenText(restOfFinalSpeechText)
        }

        speakTextToSpeech(finalSpeechText)
    } else {
        speakTextToSpeech("Nearby marker " + marker.title) //  (present perfect tense) // https://www.thesaurus.com/e/grammar/seen-vs-saw/
    }

    return currentlySpeakingMarker
}
