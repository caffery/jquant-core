package org.jquant.strategy;

import org.jquant.model.InstrumentId;
import org.jquant.serie.CandleSerie;

/**
 * One Strategy class instance per instruments in the market 
 * @author patrick.merheb
 *
 */
public abstract class MonoAssetStrategy extends AbstractStrategy {

	private CandleSerie serie;
	
	private InstrumentId instrument;
	
	

	/**
	 * Returns the growing CandleSerie of the MonoAssetStrategy instance's instrument 
	 * @return {@link CandleSerie}
	 */
	public CandleSerie getSerie() {
		return serie;
	}

	public void setSerie(CandleSerie serie) {
		this.serie = serie;
	}

	/**
	 * return the instrument of the {@link MonoAssetStrategy} instance 
	 * @return {@link InstrumentId}
	 */
	public InstrumentId getInstrument() {
		return instrument;
	}

	public void setInstrument(InstrumentId instrument) {
		this.instrument = instrument;
	}

	
	/**
	 * Is there an open position in the globalportfolio for this instance intrument 
	 * @return
	 */
	protected boolean hasPosition() {
		return super.hasPosition(instrument);
	}
	
	
	
}
