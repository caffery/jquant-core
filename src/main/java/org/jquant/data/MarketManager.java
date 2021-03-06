package org.jquant.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jquant.exception.MarketDataReaderException;
import org.jquant.instrument.GenericFuture;
import org.jquant.model.InstrumentId;
import org.jquant.model.MarketDataPrecision;
import org.jquant.model.StitchingMethod;
import org.jquant.serie.Candle;
import org.jquant.serie.CandleSerie;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * InstrumentId Repository for simulation and backtesting
 * <p> The MarketManager holds the instruments that are used during a simulation or at Runtime. 
 
 *  The StrategyRunner read the series and dispatch Quotes and Candles to the strategies. 
 * @author pat
 *@see MarketDataPrecision
 *TODO: Chargement from To  
 *TODO: Gestion du cache avec une clé composite (InstrumentId:from:to)
 */
@Component
public class MarketManager implements IMarketManager, InitializingBean, ApplicationContextAware {

	/** logger */
	private static final Logger logger = Logger.getLogger(MarketManager.class);
	

	private ApplicationContext applicationContext;
	
	private Map<String, IMarketDataProviderMapping> mappings;
	
	private Map<String, IMarketDataProviderAdapter> adapters;
	
	/**
	 * CSMap CandleSerie Map
	 * Map of growing TimeSeries (view of the TimeSeries in the MarketManager)
	 * TODO: see ConcurrentHashMap to enable multi threading capabilities  
	 */
	private final Map<InstrumentId,CandleSerie> csMap = new HashMap<InstrumentId, CandleSerie>();
	
	
	
	/**
	 * 
	 * @param timestamp a {@link DateTime}
	 * @return The Collection of Candles in the market at a precise time 
	 */
	public Map<InstrumentId,Candle> getMarketSlice(DateTime timestamp){
		Map<InstrumentId,Candle> slice = new HashMap<InstrumentId, Candle>();
		for (CandleSerie cs : csMap.values()){
			if (cs.getValue(timestamp)!= null){
				slice.put(cs.getSymbol(), cs.getValue(timestamp));
			}
			
		}
		return slice;
	}
	
	
	public Candle getCandle(InstrumentId symbol, DateTime timestamp) {
		CandleSerie cs =  csMap.get(symbol);
		if (cs != null){
			return cs.getValue(timestamp);
		}
		return null;
	}

	/**
	 * used by the simulation stragy runner to determine 
	 * which date is the oldest candle and which date is the youngest candle in the historical market 
	 * @return First and Last DateTime for the current Market
	 */
	public ImmutablePair<DateTime, DateTime> getFirstLast(){
		
		if (csMap != null && csMap.size()>0){
			
			/*
			 * Put old/recent enough arbitrary date to init watermarking variables  
			 */
			DateTime min = new DateTime("3000-12-31");
			DateTime max = new DateTime("0-12-25");
			
			for (CandleSerie cs : csMap.values()){
				if (cs.getFirstDate().isBefore(min))
					min = cs.getFirstDate();
				if (cs.getLastDate().isAfter(max))
					max= cs.getLastDate();
			}
		
			return new ImmutablePair<DateTime, DateTime>(min, max);
		}
		
		return null;
		
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
//		candleSeries = new ArrayList<CandleSerie>();
		initMarketDataProviderReaderMappings();
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	private void initMarketDataProviderReaderMappings(){
		mappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, IMarketDataProviderMapping.class, true, false);
		adapters = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, IMarketDataProviderAdapter.class, true, false);
	}
	
	/**
	 * Return the adapter for the reader 
	 * @param reader Legacy/Proprietary MarketDataReader
	 * @return the adapter {@link IMarketDataProviderAdapter}
	 */
	private IMarketDataProviderAdapter findReaderAdapter(Object reader) {
		if (adapters != null){
			// Parse all adapters 
			for (IMarketDataProviderAdapter adapter :adapters.values()){
				if (adapter.supports(reader)){
					return adapter;
				}
			}
		}
		return null;
	}

	/**
	 * return the legacy/proprietary Market Data Provider  
	 * @param provider
	 * @return
	 */
	private Object findMarketDataReader(JQuantDataProvider provider){
		if (mappings != null){
			// Parse all mappings 
			for (IMarketDataProviderMapping mapping : mappings.values()){
				if (mapping.getReader(provider)!= null){
					return mapping.getReader(provider);
				}
			}
		}
		return null;
	}

	
	/**
	 *  Add an InstrumentId in the simulation scope
	 *  <ul>
	 *  <li>Load all Historical Data</li>
	 *  <li>Subscribe for new Market Data Events concerning this instrument</li>
	 *  </ul>
	 * @param symbol the {@link InstrumentId} of the InstrumentId you want to add to the simulation  
	 * @param from begining of the historical data  
	 * @param to end of the historical data  
	 * @throws MarketDataReaderException 
	 */
	@Override
	public void addInstrument(InstrumentId symbol, DateTime from, DateTime to) throws MarketDataReaderException {
		Object reader = findMarketDataReader(symbol.getProvider());

		if (reader == null ) throw new MarketDataReaderException("No MarketData reader for provider " + symbol.getProvider());

		IMarketDataProviderAdapter adapter = findReaderAdapter(reader);

		if (adapter == null ) throw new MarketDataReaderException("No MarketData adapter for provider " + symbol.getProvider());


		// Read historical market data
		CandleSerie serie = adapter.readCandleSerie(symbol,from, to, reader);
		if (serie != null && serie.size()>0) {
			serie.setSymbol(symbol);

			// TODO: Manage Implied Volatility If Any

			// FIXME  : Cache with key (symbol,from,to)
			//				candleSeries.add(serie);
			csMap.put(symbol, serie);
		}else {
			logger.warn("No market-data for InstrumentId"+ symbol.toString());
		}



		}
		
	

	@Override
	public void addGenericFuture(InstrumentId future, DateTime from, DateTime end, StitchingMethod method) throws MarketDataReaderException {
		Object reader = findMarketDataReader(future.getProvider());
		if (reader == null ) throw new MarketDataReaderException("No MarketData reader for provider " + future.getProvider());

		IMarketDataProviderAdapter adapter = findReaderAdapter(reader);

		if (adapter == null ) throw new MarketDataReaderException("No MarketData adapter for provider " + future.getProvider());
		
		GenericFuture gf = adapter.readGenericFuture(future, from, end, reader);
		CandleSerie serie = gf.getSerie();
		if (serie != null && serie.size()>0) {
			serie.setSymbol(gf.getId());

			// TODO: Manage Implied Volatility If Any

			// FIXME  : Cache with key (symbol,from,to)
			//				candleSeries.add(serie);
			csMap.put(gf.getId(), serie);
		}else {
			logger.warn("No market-data for InstrumentId"+ future.toString());
		}

	}

	@Override
	public void addFuture(InstrumentId future, Integer deliveryMonth, Integer deliveryYear) throws MarketDataReaderException {
		// TODO Auto-generated method stub
		
	}


}
