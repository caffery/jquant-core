package org.jquant.portfolio;




public class StatisticsHelper {

    
    /**
     * Get the biggest draw down in the serie 
     * @param serie Equity value serie of a Fund/Portfolio
     * @return a {@link DrawDownData} transfer object containing the biggest Draw down informations 
     */
    public static DrawDownData getDrawDownsData(double[] serie) {
		double biggestDrawDown = 0.0;
		int indiceSmallest = 0;
		int fromIndex = 0;
		int toIndex = 0;
		boolean bFound = false;
		int periodsInMaxDD = 0;
		int timeToRecover = 0;

		if (serie != null && (serie.length >0) ) {			
			double[] theDrawDownsData = new double[serie.length];
			


			// for every month, we get the draw down
			for (int i = 0; i < serie.length; i++) {
				// we are looking for the lowest Nav after the current one
				double min = serie[i];
				indiceSmallest = i;
				for (int j = (i + 1); j < serie.length; j++) {
					if (serie[j] < min) {
						min = serie[j];
						indiceSmallest = j;
					}
				}

				// setting up the draw down data
				theDrawDownsData[i] = (min / serie[i]) - 1.0;

				// checking for the number of months in max draw down
				if (theDrawDownsData[i] < biggestDrawDown) {
					periodsInMaxDD = indiceSmallest - i;
					biggestDrawDown = theDrawDownsData[i];
					fromIndex = i;
					toIndex = i + periodsInMaxDD;
				}
			}
			

			// we have the biggest draw down
			// and the number of months in it so we can get the recovery info
			if (periodsInMaxDD > 0) {
				timeToRecover = -1;

				int i = toIndex + 1;
				int mtsRecov = 1;
				while (!bFound && (i < serie.length)) {
					if (serie[i] >= serie[fromIndex]) {
						bFound = true;
						timeToRecover = mtsRecov;
					}

					mtsRecov++;
					i++;
				}
			}
			
			
		}

		return new DrawDownData(biggestDrawDown, periodsInMaxDD, timeToRecover, fromIndex, toIndex);
	}

	
    
//    public double[] getWorstDrawDownValue() {
//        if (this.hasValues && (topDrawDowns == null)) {
//            // getting the draw downs
//            double[] allDrawDowns = getDrawDownsData();
//            allDrawDowns = (double[]) allDrawDowns.clone();
//
//            if (allDrawDowns != null) {
//                // put the values in an array to sort them
//                Arrays.sort(allDrawDowns);
//
//                // keeping only the KEEP_DRAWDOWNS top drowdowns
//                int nbDrawDowns = Math.min(StatisticsConst.KEEP_DRAWDOWNS, allDrawDowns.length);
//                double[] topDraws = new double[StatisticsConst.KEEP_DRAWDOWNS];
//                for (int i = 0; i < nbDrawDowns; i++) {
//                    topDraws[i] = allDrawDowns[i];
//                }
//
//                topDrawDowns = topDraws;
//            }
//        }
//
//        return topDrawDowns;
//    }
    
    
    	
    
	
}
