package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AutoResynch {
	
	static int sampling = 16;
	static int windowSize = sampling*500;
	
	public static void main(String[] args) throws IOException {
				
		System.out.print("Loading subtitle file...");
		SRTParser srt1 = new SRTParser("test/sample.srt", sampling);
		
		SRTParser srt2 = new SRTParser();
		
		try {
			srt2 = (SRTParser ) srt1.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // this line will return you an independent

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
		
		for(int i = 0; i < nLines; i++){
			
			
			currentSampleSet = w1.getNSamples(srt1.getPeak(i), windowSize);

			delayedPeak = w2.compareNSamplesWithAllSamplesBound(currentSampleSet, srt2.getPeak(i), 48000);//, w1.getAverageAmplitudeNumber(i));
				
			delay = delayedPeak - srt2.getPeak(i);
			
			srt1.applyDelay(delay);
			
			srt2.print(i);

		}
		
		
	}
	

	


}

