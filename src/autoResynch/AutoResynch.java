package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AutoResynch {
	
	static int sampling = 16;
	static int windowSize = sampling*750;
	
	public static void main(String[] args) throws IOException {
				
		System.out.print("Loading subtitle file...");
		SRTParser srt1 = new SRTParser("test/Greys.Anatomy.s12e05.italiansubs.srt", sampling);
		
		SRTParser srt2 = new SRTParser();
		
		srt2 = (SRTParser) DeepCopy.copy(srt1);
		
		System.out.println(" done.");
		
		System.out.print("Loading waveform 1...");
		WaveformParser w1 = new WaveformParser("test/1.bin", sampling);
		System.out.print(" loading waveform 2...");
		WaveformParser w2 = new WaveformParser("test/2.bin", sampling);
		System.out.println(" done.");

		System.out.print("Setting waveform 1 and 2 mobile average...");
		w1.setMobileAverage(windowSize);
		w2.setMobileAverage(windowSize);
		System.out.println(" done.");

		int nLines = srt1.getNumberLines();

		int[] currentSampleSet;
		int delayedPeak = 0;
		int delay = 0;
		
		double avg1 = w1.getAverageAmplitude();
		double avg2 = w2.getAverageAmplitude();
		System.out.println(avg1 + " " + avg2);
		double ratio = avg2/avg1;
		
		//w1.normaliseBy(ratio);
		
		for(int i = 0; i < nLines; i++){
			
			
			currentSampleSet = w1.getNSamples(srt1.getPeak(i), windowSize);

			delayedPeak = w2.compareNSamplesWithAllSamplesBound(currentSampleSet, srt2.getPeak(i), 48000 + delay/2);//, w1.getAverageAmplitudeNumber(i));
				
			delay = delayedPeak - srt2.getPeak(i);
			System.out.println(delay);
			srt2.applyDelayFrom(i, delay);
			
			//srt2.print(i);

		}
		
		
	}
	

	


}

