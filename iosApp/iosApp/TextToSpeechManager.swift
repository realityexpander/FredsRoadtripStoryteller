//
//  TextToSpeechManager.swift
//  iosApp
//
//  Created by Chris Athanas on 12/9/23.
//  Copyright © 2023 Chris Athanas. All rights reserved.
//
import shared
import Speech
import NaturalLanguage
import Foundation


// Listens to and sends speech state updates to the commonSpeech object using CommonFlow
// Uses AVSpeechSynthesizer to speak text
class TextToSpeechManager : NSObject, AVSpeechSynthesizerDelegate {
    private var synthesis: AVSpeechSynthesizer = AVSpeechSynthesizer()
    private var utterance: AVSpeechUtterance = AVSpeechUtterance(string: "")
    var commonSpeech: CommonSpeech

    init(commonSpeech: CommonSpeech) {
        self.commonSpeech = commonSpeech
        super.init()
            
        synthesis.delegate = self
        AVSpeechSynthesisVoice.speechVoices() // fetches dependencies (supposedly fixes runtime warning: [catalog] Unable to list folder)
    }
    
    func speakText(text: String) {
        if(text.isEmpty || synthesis.isSpeaking) {
            synthesis.stopSpeaking(at: AVSpeechBoundary.immediate)
            commonSpeech.updateSpeechState(speechState: CommonSpeech.SpeechStateNotSpeaking())
        }

        if(text.isEmpty) {
            return
        }
        
        //let utterance = AVSpeechUtterance(string: text)
        utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "en-US")
        
        // Set voice for language of the text (if possible)
        if let language = self.detectLanguageOf(text: text) {
            utterance.voice = AVSpeechSynthesisVoice(language: language.rawValue)
        }
        
        synthesis.speak(utterance)
        commonSpeech.updateSpeechState(speechState: CommonSpeech.SpeechStateSpeaking())
    }

    func speechSynthesizer(
        _ synthesizer: AVSpeechSynthesizer,
        didFinish utterance: AVSpeechUtterance
    ) {
        commonSpeech.updateSpeechState(speechState: CommonSpeech.SpeechStateNotSpeaking())
    }
    
    private func detectLanguageOf(text: String) -> NLLanguage? {
        let recognizer = NLLanguageRecognizer()
        recognizer.processString(text)
        
        guard let language = recognizer.dominantLanguage else {
            return nil
        }
        
        return language
    }
}
