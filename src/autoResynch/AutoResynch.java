package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AutoResynch {
	
	static int sampling = 16;
	static int windowSize = sampling*500;
	static double threshold = 0.06;
	
	public static void main(String[] args) throws IOException {
				
		System.out.print("Loading subtitle file...");
		//SRTParser srt1 = new SRTParser("test/Greys.Anatomy.s12e05.italiansubs.srt", sampling);
		SRTParser srt1 = new SRTParser("test/The.Walking.Dead.s06e07.italiansubs.srt", sampling);
		//SRTParser srt1 = new SRTParser("test/sample.srt", sampling);
		SRTParser srt2 = new SRTParser();
		
		srt2 = (SRTParser) DeepCopy.copy(srt1);
		
		System.out.println(" done.");
		
		System.out.print("Loading waveform 1...");
		WaveformParser w1 = new WaveformParser("test/1.bin", sampling);
		System.out.print(" loading waveform 2...");
		WaveformParser w2 = new WaveformParser("test/2.bin", sampling);
		System.out.println(" done.");

		/*System.out.print("Setting waveform 1 and 2 mobile average...");
		w1.setMobileAverage(windowSize);
		w2.setMobileAverage(windowSize);
		System.out.println(" done.");*/

		int nLines = srt1.getNumberLines();

		int[] currentSampleSet;
		int delayedPeak = 0;
		int delay = 0;
		int[] delays = new int[nLines];
		
		/*double avg1 = w1.getAverageAmplitude();
		double avg2 = w2.getAverageAmplitude();
		System.out.println(avg1 + " " + avg2);
		double ratio = avg2/avg1;
		*/
		//w1.normaliseBy(ratio);
		double avgDelay = 0;
		int searchWindow = 480000;
		for(int i = 0; i < nLines; i++){
			
			
			currentSampleSet = w1.getNSamples(srt1.getPeak(i), windowSize);

			delayedPeak = w2.compareNSamplesWithAllSamplesBound(currentSampleSet, srt2.getPeak(i), searchWindow);//, w1.getAverageAmplitudeNumber(i));
				
			delay = delayedPeak - srt2.getPeak(i);
			
			if(delay > threshold){
				
				searchWindow = searchWindow + Math.abs(delay);
				
			}else{
				
				searchWindow = 48000;
				
			}
			
			srt2.applyDelayFrom(i, delay);
			if(i>0){
				System.out.println(delay + " " +(double)delay/(srt1.getPeak(i)-srt1.getPeak(i-1)));
			}else{
				System.out.println(delay);
			}
			delays[i] = delay;
			avgDelay += delay;


		}
		avgDelay /= nLines;
		System.out.println("Average delay: " + avgDelay);
		System.out.println("Search delays for errors...");
		
		int countErrors = 0;
		int sumErrors = 0;

		for (int j = 0; j < delays.length; j++){
			if((j > 0 && Math.abs((double)delays[j]/(srt1.getPeak(j)-srt1.getPeak(j-1))) > threshold) || (j+1 < delays.length && countErrors > 0 && Math.abs((double)delays[j+1]/(srt1.getPeak(j+1)-srt1.getPeak(j))) > threshold)) {
				countErrors++;
				
				if(countErrors > 1) {
					sumErrors += delays[j-1];
				}
			}
			else {
				if(countErrors > 1) {
					sumErrors += delays[j-1];
					
					for(int k = 0; k < countErrors; k++) {
						delays[j-k-1] = Math.round(sumErrors/countErrors);
					}
				}				
				sumErrors = 0;
				countErrors = 0;
			}
			 
		}
		srt2 = new SRTParser();
		
		srt2 = (SRTParser) DeepCopy.copy(srt1);
		int count = 0;
		for(int d : delays) {
			//System.out.println(d);	
			srt2.applyDelayFrom(count, d);
			srt2.print(count);
			count++;
		}				
	}
}

