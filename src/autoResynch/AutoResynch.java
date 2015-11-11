package autoResynch;

import java.io.FileInputStream;
import java.io.IOException;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

public class AutoResynch {
	
	public static void main(String[] args) throws IOException {
        
        Configuration configuration = new Configuration();
        
        // Set path to acoustic model.
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        // Set path to dictionary.
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        // Set language model.
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        
        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        recognizer.startRecognition(new FileInputStream("/home/claudio/workspace/autoResynch/speech.wav"));
        SpeechResult result; //= recognizer.getResult();
        
        while ((result = recognizer.getResult()) != null) {

            System.out.println("List of recognized words and their times:");
            for (WordResult r : result.getWords()) {
                System.out.println(r);
            }


        }
        recognizer.stopRecognition();

    }

}
