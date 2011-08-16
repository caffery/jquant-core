package org.jquant.data.reader;

import org.joda.time.DateTime;
import org.jquant.exception.MarketDataReaderException;
import org.jquant.model.Quote;
import org.jquant.serie.QuoteSerie;


public interface IQuoteReader {

	/**
	 * Lit une série de quotations 
	 * @param instrument
	 * @return une {@link QuoteSerie}
	 * @throws MarketDataReaderException
	 */
	public QuoteSerie fetchAllQuote(String instrumentId) throws MarketDataReaderException;
	
	/**
	 * Lit une s�rie de quotations entre une date de début et une date de fin
	 * @param instrument
	 * @return une {@link QuoteSerie}
	 * @throws MarketDataReaderException
	 */
	public QuoteSerie fetchAllQuote(String instrumentId,DateTime debut, DateTime fin) throws MarketDataReaderException;
	
	/**
	 * Lit une unique quotations 
	 * @param instrument
	 * @param date
	 * @return une {@link Quote}
	 * @throws MarketDataReaderException
	 */
	public Quote fetchQuote(String instrumentId,DateTime date) throws MarketDataReaderException;
	
	
}
