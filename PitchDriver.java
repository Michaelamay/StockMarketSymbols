import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PitchDriver {

	//public static String textPath = "/Users/michaeljonathanamay/Desktop/pitch_example_data";
	//public static String textPath = "/Users/michaeljonathanamay/Desktop/feed_pitch_data";
	public static String textPath;

	public static Map<String,Integer> computedMap = new HashMap<>();

	public static Map<String,Integer> pMap = new HashMap<>();

	public static Map<String, Integer> eMap = new HashMap<>();

	public static ArrayList<String> eArray = new ArrayList<>();

	public static ArrayList<String> pArray = new ArrayList<>();

	public PitchDriver(String textPath){
		this.textPath = textPath;
	}

	public static String getSymbolFromAddOrder(String orderID) {
		/* This method finds the symbol for messages useful for message types 'E' which does NOT come with symbols.*/

		String addOrderType = "a";
		String symbol = "NO SYMBOL FOUND";

		try {
			List<String> allLines = Files.readAllLines(Paths.get(textPath));

			for(String textLine: allLines) {

				//MATCH THE SPECFIC ORDER ID WE ARE CURRENT LOOKING
				if(textLine.substring(10,22).equals(orderID)) {

					//IT MUST BE OF MESSAGE TPYE ADD ORDER 'A'
					if(textLine.substring(9, 10).toLowerCase().equals(addOrderType)) {

						//FINALLY, GRAB THAT SYMBOL
						symbol = textLine.substring(29, 35);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return symbol;
	}

	public static void arrayListInsertAndPopulate(String line) {
		/* This method filters messages of type e and p into two separate arrays and populates the arrays: eArray and pArray.
		 * It saves the order id in even index and symbols in odd index of the arrays.*/

		String lineMessageType = line.substring(9, 10).toLowerCase();//Universal message type offset for all message orders is 9 and 10.
		String orderExecutedType = "e";
		String tradeType = "p";
		String orderID = line.substring(10,22);//Universal order id offset for all message orders is 10 and 22.

		if((lineMessageType.equals(orderExecutedType))) {

			//WHATS THE SYMBOL FOR THIS ORDERID?
			String symbol = getSymbolFromAddOrder(orderID);

			eArray.add(orderID);
			eArray.add(symbol);

		} else if (lineMessageType.equals(tradeType)) {

			//GRAB SYMBOL FIRST
			String symbol = line.substring(29, 35);

			pArray.add(orderID);
		    pArray.add(symbol);

		}
	}

	public static void hashSymbols() {
		/* This method takes the arrays containing messages of type e and p, and hashes them.*/

		//HASH ARRAY OF MESSAGE TYPE 'P'
		//ODDS ARE SYMBOLS, EVEN ARE ORDER IDs
		for(int i = 0; i < pArray.size(); i+=2) {

			String pSymbol = pArray.get(i+1);

			boolean doSymbolExist = pMap.containsKey(pSymbol);

			if(doSymbolExist) {

				  //Add one more to the count on that related symbol
				  int currentCount = pMap.get(pSymbol);

				  currentCount++;

				  //INSERT UPDATE COUNT
				  pMap.put(pSymbol, currentCount);

			} else {
				  int firstEntry = 1;

				  //there does not exist a record of this symbol, insert record into hash map for the first time.
				  pMap.put(pSymbol, firstEntry);
			}
		}

		//HASH ARRAY OF MESSAGE TYPE 'E'
		for(int k = 0; k < eArray.size(); k+=2) {

			String eSymbol = eArray.get(k+1);

			boolean doSymbolExist = eMap.containsKey(eSymbol);

			if(doSymbolExist) {

				  //Add one more to the count on that related symbol
				  int currentCount = eMap.get(eSymbol);

				  currentCount++;

				  //INSERT UPDATE COUNT
				  eMap.put(eSymbol, currentCount);

			} else {
				  int firstEntry = 1;

				  //there does not exist a record of this symbol, insert record into hash map for the first time.
				  eMap.put(eSymbol, firstEntry);
			}
		}

		//System.out.println("Message type P hash map= " + pMap);
		//System.out.println("Message type E hash map= " + eMap);

	}

	public static void readTextFile() {

		try {
			List<String> allLines = Files.readAllLines(Paths.get(textPath));

			int count = 1;

			for(String line: allLines) {
				arrayListInsertAndPopulate(line);
				count++;
			}

			System.out.println(count + " lines were scanned in total from the document using the readTextFile() method.");

			System.out.println("The size of the array holding lines with the message type E is: " + eArray.size()/2);

			System.out.println("The size of the array holding lines with the message type P is: " + pArray.size()/2);
			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
	    return map.entrySet()
	      .stream()
	      .sorted(Map.Entry.<K, V>comparingByValue().reversed())
	      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static void sortHashMap() {

		pMap = sortMapByValueDescending(pMap);
		eMap = sortMapByValueDescending(eMap);

		System.out.println("Array of message type 'P' in hash map form is: " + pMap);
		System.out.println("Array of message type 'E' in hash map form is: " + eMap);
	}

	public static void computeVolume() {

	for(Map.Entry<String, Integer> pEntry : pMap.entrySet()) {

			String pSymbol = pEntry.getKey();
			Integer pValue = pEntry.getValue();

			//System.out.println("Key: " + pSymbol + ", Value: " + pValue);

			for(Map.Entry<String, Integer> eEntry: eMap.entrySet()) {

				String eSymbol = eEntry.getKey();
				Integer eValue = eEntry.getValue();

				if(pSymbol.equals(eSymbol)) {

					//System.out.println("pKey: " + pSymbol + " equals eKey: "+ eSymbol);

					int sum = pValue + eValue;

					//INSERT UPDATE COUNT
					computedMap.put(pSymbol, sum);

					break;
				}
			}

			if(!computedMap.containsKey(pSymbol)) {
				computedMap.put(pSymbol, pValue);
			}

		}

		computedMap = sortMapByValueDescending(computedMap);

		//System.out.println(computedMap);
	}

	public static void printMessageTypeEArray() {

		System.out.println("How many messages of type E does the document contain? " + eArray.size()/2);

		int counter = 0;
		for(int i = 0; i < eArray.size();i+=2) {
			System.out.println("Counter: " +counter+ " " + eArray.get(i) + " Symbol: "+ eArray.get(i+1));
			counter++;
		}
	}

	public static void printMessageTypePArray() {
		System.out.println("How many messages of type P does the document contain? " + pArray.size()/2);

		int counter = 0;
		for(int i = 0; i < pArray.size();i+=2) {
			System.out.println("Counter: " +counter+ " " + pArray.get(i) + " Symbol: "+ pArray.get(i+1));
			counter++;
		}
	}

	public static void printHashMapVolumeDescending() {

		System.out.println("SYMBOL   VOLUME" );
		System.out.println("------   ------" );
		for(Map.Entry<String, Integer> computedEntry : computedMap.entrySet()) {

			String computedSymbol = computedEntry.getKey();
			Integer computedValue = computedEntry.getValue();

			System.out.println(computedSymbol + "   Volume define as (E + P) for this symbol is: " + computedValue);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = System.getProperty("user.dir")+"/pitch_feed_data";

		PitchDriver obj = new PitchDriver(filePath);
		readTextFile();

		//printMessageTypeEArray();
		//printMessageTypePArray();

		hashSymbols();
		sortHashMap();

		computeVolume();
		printHashMapVolumeDescending();

		//System.out.println(System.getProperty("user.dir")+"/feed_pitch_data");




	}

}
