package autoResynch;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AutoResynch {
	
	static int sampling = 16;
	static int windowSize = sampling*500;
	static double threshold = 0.1;
	
	public static void main(String[] args) throws IOException {
				
		System.out.print("Loading subtitle file...");
		SRTParser srt1 = new SRTParser("test/Unforgettable.s04e01.italiansubs.srt", sampling);
		SRTParser srt2 = new SRTParser();
		
		srt2 = (SRTParser) DeepCopy.copy(srt1);
		
		System.out.println(" done.");
		
		System.out.print("Loading waveform 1...");
		WaveformParser w1 = new WaveformParser("test/1.bin", sampling);
		System.out.print(" loading waveform 2...");
		WaveformParser w2 = new WaveformParser("test/2.bin", sampling);
		System.out.println(" done.");

		System.out.println("Number of breaks in waveform 1...:" + w1.getNumberOfBreaks());
		System.out.println("Number of breaks in waveform 2...:" + w2.getNumberOfBreaks());
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
		int searchWindow = 48000;
		int errorCount = 0;
		
		int numberOfSamples1 = w1.getNumberOfSamples();
		boolean[] silence = w1.getSilentSamples();
		
		int i = 0;
		boolean comingFromABreak = false;
		
		int currentPeak = srt1.getPeak(i);
		
		
		for(int j = 0; j < numberOfSamples1; j++){
			
			if(silence[j]){//commercial break?
				comingFromABreak = true;
				System.out.println("Break found at second: " + (double)j/16000.0);
			}
			
			if(j != currentPeak){
				continue;
			}
			
			if(comingFromABreak || i == 0){//wider window search
				comingFromABreak = false;
				searchWindow = 160000;
			}else if(i > 0){
				searchWindow = (int) (threshold * (srt1.getPeak(i)-srt1.getPeak(i-1)));//last delay, multiplied by two int)(threshold * (currentPeak - srt1.getPeak(i-1))); 
				System.out.println("New search window: " + searchWindow);
			}
			
			currentSampleSet = w1.getNSamples(srt1.getPeak(i), windowSize);

			delayedPeak = w2.compareNSamplesWithAllSamplesBound(currentSampleSet, srt2.getPeak(i), searchWindow);//, w1.getAverageAmplitudeNumber(i));
			delay = delayedPeak - srt2.getPeak(i);
			
			delay += (16 - (delay % 16));
			
			if(i > 0 && Math.abs((double)delays[i]/(srt1.getPeak(i)-srt1.getPeak(i-1))) > threshold){
				
				searchWindow = searchWindow + Math.abs(delay);
				
			}else{
				
				searchWindow = 48000;
				
			}
			
			
			if(i>0){
				srt2.applyDelayFrom(i, delay, (double)delay/(srt1.getPeak(i)-srt1.getPeak(i-1)));
			}else{
				srt2.applyDelayFrom(i, delay);
			}
			//srt2.applyDelayAt(i, delay);
			if(i>0){
				System.out.println(delay + " " +(double)delay/(srt1.getPeak(i)-srt1.getPeak(i-1)));
			}else{
				System.out.println(delay);
			}
			delays[i] = delay;
			avgDelay += delay;

			
			if(i> 0 && Math.abs((double)delays[i]/(srt1.getPeak(i)-srt1.getPeak(i-1))) > threshold){
				
				errorCount++;
				
				if(errorCount > 10){
					
					System.out.println("Something's wrong... trying to fix it");
					//go back to the first possible error
					//i -= 10;
					errorCount = 0;
				}
				
			}else{
				errorCount=0;
			}

			i++;//onto the next line
			
			if(i < srt1.getNumberLines()){
				currentPeak = srt1.getPeak(i);
			}
		}
		avgDelay /= nLines;
		System.out.println("Average delay: " + avgDelay);
		System.out.println("Search delays for errors...");
		
		int countErrors = 0;
		int sumErrors = 0;
		double sumErrorsPerFrame = 0;
		
		//should we ever consider the first delay a possible error?
		for (int j = 1; j < delays.length; j++){
			if((j > 0 && Math.abs((double)delays[j]/(srt1.getPeak(j)-srt1.getPeak(j-1))) > threshold) || (j+1 < delays.length && countErrors > 0 && Math.abs((double)delays[j+1]/(srt1.getPeak(j+1)-srt1.getPeak(j))) > threshold)) {
				countErrors++;
				System.out.println("Possible error at line " + j + ", will attempt to fix");
				if(countErrors > 1) {
					sumErrors += delays[j-1];
				}
			}
			else {
				if(countErrors > 1) {
					sumErrors += delays[j-1];
					
					//normalising the sum of the errors by the number of frames between the last correct line and the last "wrong" line
					sumErrorsPerFrame = ((double)sumErrors)/(srt1.getPeak(j-1) - srt1.getPeak(j-countErrors-1));
					System.out.print("Delay totale: " + sumErrors);
					for(int k = 0; k < countErrors; k++) {
						if(j-k-1 > 0){
							delays[j-k-1] = (int) Math.round(sumErrorsPerFrame * (srt1.getPeak(j-k-1) - srt1.getPeak(j-k-2)));//.round(sumErrors/countErrors);////
							System.out.print(" " + delays[j-k-1]);
						}
					}
					System.out.println("");
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
			if(count > 0){
				
				srt2.applyDelayFrom(count, d, (double)d/(srt1.getPeak(count)-srt1.getPeak(count-1)));

			}else{

				srt2.applyDelayFrom(count, d);

			}
			srt2.print(count);
			count++;
		}				
	}
}

