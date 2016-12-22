import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class HexGenerator {
	
	
	public static void main(String[] args) {
		int MIN_WORD_LENGTH = 4;
		int MAX_WORD_LENGTH = 8;
		int MAX_WORDS = 256;
		boolean debug = true;
		boolean repeat = true;
		boolean random = false;
		String hex_file = "C://Users//grodriguez//Desktop//eepromfile.hex";
		String hex_file_debug = "C://Users//grodriguez//Desktop//eepromfile_debug.hex";
		Random rnd = new Random();
		
		if ( args == null ) { 
			System.out.println("[INFO] El siguiente programa tiene los siguientes parametros:");
			System.out.println(">> hexgenerator words.txt minWordLength=4 maxWordLength=8 repeat=yes|no\n");
			System.exit(-1);
		}
		
		System.out.println("Args[0] == "+args[0]);
		/*if ( args.length < 4 ) {
			System.out.println("[ERROR] ");
			System.exit(-1);
		}*/
		
		File file = new File(args[0]);
		if ( file.exists() == false || file.isDirectory() ) {
			System.out.println("[ERROR] El fichero indicado no existe o es un directorio\n");
			System.exit(-2);
		}
		/*
		 * BLOQUE COPIADO DE PALABRAS DEL FICHERO A MEMORIA RAM
		 */		
		int wordsAdded = 0;
		int totalWords = 0;
		ArrayList<String> words = new ArrayList<String>();
		Hashtable<String,String> wordsLoaded = new Hashtable<String,String>();
		BufferedReader reader = null;
		System.out.println("-> Filtrando palabras del fichero "+args[0]);
		long initTime = System.currentTimeMillis();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			
			while ((line = reader.readLine()) != null ) {
				line = line.trim().replaceAll(" +"," ");	// Eliminamos los espacios de inicio - fin y reducimos los espacios entre palabras a 1
				String[] wordsLine = line.split(" ");
				int wordsInLine = wordsLine.length;
				for (int i=0; i<wordsInLine; i++) {
					String wordToAdd = wordsLine[i].toUpperCase();
					if ( wordToAdd.matches("[A-Za-z]{"+MIN_WORD_LENGTH+","+MAX_WORD_LENGTH+"}") == false ) { 
						continue; 
					}
					if ( wordsLoaded.get(wordToAdd) == null ) {						
						words.add(wordToAdd);
						wordsLoaded.put(wordToAdd,"");
						wordsAdded++;
					} 
				}
				totalWords++;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("-> Se han cargado "+wordsAdded+" palabras en memoria de "+totalWords+" que hay en fichero en "+(stopTime-initTime)+" ms.\n-> Se han excluido palabras que no cumplen con la regex [A-Za-z]{"+MIN_WORD_LENGTH+","+MAX_WORD_LENGTH+"}, excluye palabras con accentos, ñ, l·l");
		if ( debug ) { try { System.out.println("-> Esperando 10 segs para continuar\n\n");Thread.sleep(10000); } catch (Exception e){} }
		
		
		/*
		 * BLOQUE DE SELECCION DE PALABRA ALEATORIA + CONVERSION A HEX PARA MOSTRAR
		 */
		wordsLoaded = new Hashtable<String,String>();
		String[] wordsEEprom = new String[MAX_WORDS];
		String selectedWord;
		if ( repeat ) {
			if ( debug ) {
				System.out.println("-> Seleccionando palabras aleatoriamente (con repeticion)\n\n");
				System.out.println("-------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------");
				System.out.println(String.format("%-5s %-10s %-30s %-16s %-5s","--","PALABRA","ASCII (+128 LAST CHAR)","HEX (ASCII +128)","--"));
				System.out.println("-------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------");
			}
			for ( int i = 0; i<MAX_WORDS; i++) {
				if ( random ) { selectedWord = words.get(rnd.nextInt(wordsAdded-1)); }
				else { selectedWord = words.get(i%wordsAdded); }
				String selectedWordAscii = getASCIIForEEprom(selectedWord,MAX_WORD_LENGTH);
				if ( debug ) { System.out.println(String.format("%-5s %-10s %-30s %-16s %-5s", "--",selectedWord,selectedWordAscii,getHexaFromAscii(selectedWordAscii,MAX_WORD_LENGTH),"--")); }
				wordsEEprom[i] = selectedWord;
			}
		} else {
			
			int numWordsEEprom = 0;
			if (debug) {
				System.out.println("-> Seleccionando palabras aleatoriamente (sin repeticion)\n\n");
				System.out.println("-------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------");
				System.out.println(String.format("%-5s %-10s %-30s %-16s %-5s","--","PALABRA","ASCII (+128 LAST CHAR)","HEX ASCII 128","--"));
				System.out.println("-------------------------------------------------------------------");
				System.out.println("-------------------------------------------------------------------");
			}
			while (numWordsEEprom < MAX_WORDS) {
				if ( random ) { selectedWord = words.get(rnd.nextInt(wordsAdded-1)); }
				else { selectedWord = words.get(numWordsEEprom%MAX_WORDS); }
				if ( wordsLoaded.get(selectedWord) == null ) {
					String selectedWordAscii = getASCIIForEEprom(selectedWord,MAX_WORD_LENGTH);
					if (debug) { System.out.println(String.format("%-5s %-10s %-30s %-16s %-5s", "--",selectedWord,selectedWordAscii,getHexaFromAscii(selectedWordAscii,MAX_WORD_LENGTH),"--")); }
					wordsEEprom[numWordsEEprom] = selectedWord;
					numWordsEEprom++;
				}
			}
		}
		System.out.println("-------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------");
		
		if ( debug ) { try { System.out.println("-> Esperando 10 segs para continuar\n\n");Thread.sleep(10000); } catch (Exception e){} }
		
		if ( debug ) {
			System.out.println(String.format("%-20s %-20s %-20s %-20s","16SHV","16SLV","16SHO","16SLO"));
			for ( int i = 0; i<MAX_WORDS; i++ ) {
				String word1 = wordsEEprom[i];
				//String word2 = wordsEEprom[i+1];
				
				String word1_16SH_visible = get16Segments(word1,true,false, MAX_WORD_LENGTH); 	String word1_16SL_visible = get16Segments(word1,false,false, MAX_WORD_LENGTH); 			
				String word1_16SH_oculto = get16Segments(word1,true,true, MAX_WORD_LENGTH); 	String word1_16SL_oculto = get16Segments(word1,false,true, MAX_WORD_LENGTH); 			
				/*
				String word2_16SH_visible = get16Segments(word1,true,false); 	String word2_16SL_visible = get16Segments(word1,false,false); 
				String word2_16SH_oculto = get16Segments(word1,true,false); 	String word2_16SL_oculto = get16Segments(word1,true,false); 
				*/
				System.out.println( String.format("%-20s %-20s %-20s %-20s",word1_16SH_visible,word1_16SL_visible,word1_16SH_oculto,word1_16SL_oculto));
				
			}
		}
		
		/*
		 * BLOQUE DE ESCRITURA EN EL FICHERO HEX Y EN FICHERO DUMP FILE (POR TEMA DE BUFFERS)
		 */
		if ( debug ) { try { System.out.println("-> Esperando 10 segs para continuar\n\n");Thread.sleep(10000); } catch (Exception e){} }
		StringBuilder eepromAsciiLines = new StringBuilder("");
		if ( debug ) { System.out.println(String.format("%-8s %-8s %-6s %-20s %-20s %-6s %-64s","OPCODE","ADDRESS","TYPE","HEX WORD1","HEX WORD2","CHKSUM","SAVED IN HEX FILE")); }
		
		int i = 0; int j = 0;
		for ( i = 0; i<MAX_WORDS; i+=2) {
			String word1 = wordsEEprom[i];
			String word2 = wordsEEprom[i+1];
			String word1_hexa = getHexaFromAscii(getASCIIForEEprom(word1,MAX_WORD_LENGTH),MAX_WORD_LENGTH).toUpperCase();
			String word2_hexa = getHexaFromAscii(getASCIIForEEprom(word2,MAX_WORD_LENGTH),MAX_WORD_LENGTH).toUpperCase();
			
			eepromAsciiLines.append(getLineForEEProm(word1_hexa,word2_hexa,(j*16), debug)+"\n");
			j++;
		}
		
		/* 
		 * BLOQUE DE GENERACION DE 16SEGMENTS 
		 */ 
		String eeprom16SegmentsLowVisible = getLinesForEEProm16S(wordsEEprom, false, false, MAX_WORD_LENGTH, 4096, debug);	// El 5º param es la direccion de memoria en DECIMAL por la que comenzará a rellenar 4096 --> Bit 16S (2^12) activado  
		String eeprom16SegmentsHighVisible= getLinesForEEProm16S(wordsEEprom, true, false, MAX_WORD_LENGTH, 6144, debug);	// Bit 12 (16S) + 11 (High) activados
		String eeprom16SegmentsLowHidden  = getLinesForEEProm16S(wordsEEprom, false, true, MAX_WORD_LENGTH, 8192, debug);	// Bit 13 (Guiones) activado
		String eeprom16SegmentsHighHidden = getLinesForEEProm16S(wordsEEprom, true, true, MAX_WORD_LENGTH, 10240, debug);	// Bit 13 + 11 (High) activados
		String eeprom16SegmentsLowDebug	  = getLinesForEEProm16S(wordsEEprom, false, false, MAX_WORD_LENGTH, 8192, false);
		String eeprom16SegmentsHighDebug  = getLinesForEEProm16S(wordsEEprom, true, false, MAX_WORD_LENGTH, 10240, false);
		
		// GENERAMOS FICHERO DE DEBUG! --> NO OCULTARA LAS LETRAS, ASI PODREMOS VER COMO LAS PINTA POR PANTALLA
		StringBuilder eepromDebug = new StringBuilder(eepromAsciiLines.toString());
		eepromDebug.append(eeprom16SegmentsLowVisible);
		eepromDebug.append(eeprom16SegmentsHighVisible);
		eepromDebug.append(eeprom16SegmentsLowDebug);
		eepromDebug.append(eeprom16SegmentsHighDebug);
		
		
		eepromAsciiLines.append(eeprom16SegmentsLowVisible);
		eepromAsciiLines.append(eeprom16SegmentsHighVisible);
		eepromAsciiLines.append(eeprom16SegmentsLowHidden);
		eepromAsciiLines.append(eeprom16SegmentsHighHidden);
		if ( debug ) { System.out.println(String.format("%-8s %-8s %-6s %-20s %-20s %-6s %-64s","OPCODE","ADDRESS","TYPE","HEX WORD1","HEX WORD2","CHKSUM","SAVED IN HEX FILE")); }
		
		/*
		 * BLOQUE DE ESCRITURA EN FICHERO
		 */
		generateHEXFile(eepromAsciiLines,hex_file);
		generateHEXFile(eepromDebug,hex_file_debug);
		
		System.out.println("\nFichero hex guardado en "+hex_file);
	}
	
	
	/* STATIC FUNCTIONS FOR UTILITIES */
	public static String getLinesForEEProm16S(String[] words, boolean high, boolean guiones, int max_word_length, int startAddress, boolean debug) {
		int size = words.length;
		
		StringBuilder sb = new StringBuilder("");
		for (int i=0;i < size; i+=2) {
			
			String word1_16s = get16Segments( words[i], high, guiones, max_word_length );
			String word2_16s = get16Segments( words[i+1], high, guiones, max_word_length );
			sb.append( getLineForEEProm(word1_16s,word2_16s, startAddress, debug ) + "\n" );
			startAddress = startAddress + 16;
		}
		return sb.toString();
	}
	public static String get16Segments(String asciiWord, boolean high, boolean guiones, int max_word_length) {
		StringBuilder sb = new StringBuilder("");
		char[] charsInWord = asciiWord.toCharArray();
		int size = charsInWord.length;
		
		if ( guiones ) {
			if ( high ) {
				for ( int i = 0; i<size; i++) {
					switch(charsInWord[i]) {
						case 'A': sb.append(getHexaFromNumber(0));break;		case 'J': sb.append(getHexaFromNumber(0));break;		case 'S': sb.append(getHexaFromNumber(0)); break;
						case 'B': sb.append(getHexaFromNumber(0));break;		case 'K': sb.append(getHexaFromNumber(0));break;		case 'T': sb.append(getHexaFromNumber(0)); break;
						case 'C': sb.append(getHexaFromNumber(0));break;		case 'L': sb.append(getHexaFromNumber(0));break;		case 'U': sb.append(getHexaFromNumber(0)); break;
						case 'D': sb.append(getHexaFromNumber(0));break;		case 'M': sb.append(getHexaFromNumber(0));break;		case 'V': sb.append(getHexaFromNumber(0)); break;
						case 'E': sb.append(getHexaFromNumber(0));break;		case 'N': sb.append(getHexaFromNumber(0));break;		case 'W': sb.append(getHexaFromNumber(0)); break;
						case 'F': sb.append(getHexaFromNumber(0));break;		case 'O': sb.append(getHexaFromNumber(0));break;		case 'X': sb.append(getHexaFromNumber(0)); break;
						case 'G': sb.append(getHexaFromNumber(0));break;		case 'P': sb.append(getHexaFromNumber(0));break;		case 'Y': sb.append(getHexaFromNumber(0)); break;
						case 'H': sb.append(getHexaFromNumber(0));break;		case 'Q': sb.append(getHexaFromNumber(0));break;		case 'Z': sb.append(getHexaFromNumber(0)); break;
						case 'I': sb.append(getHexaFromNumber(0));break;		case 'R': sb.append(getHexaFromNumber(0));break;		default:  sb.append(getHexaFromNumber(0)); break;
					}
				}	
			} else {
				for ( int i = 0; i<size; i++) {
					switch(charsInWord[i]) {
						case 'A': sb.append(getHexaFromNumber(192));break;		case 'J': sb.append(getHexaFromNumber(192));break;		case 'S': sb.append(getHexaFromNumber(192)); break;
						case 'B': sb.append(getHexaFromNumber(192));break;		case 'K': sb.append(getHexaFromNumber(192));break;		case 'T': sb.append(getHexaFromNumber(192)); break;
						case 'C': sb.append(getHexaFromNumber(192));break;		case 'L': sb.append(getHexaFromNumber(192));break;		case 'U': sb.append(getHexaFromNumber(192)); break;
						case 'D': sb.append(getHexaFromNumber(192));break;		case 'M': sb.append(getHexaFromNumber(192));break;		case 'V': sb.append(getHexaFromNumber(192)); break;
						case 'E': sb.append(getHexaFromNumber(192));break;		case 'N': sb.append(getHexaFromNumber(192));break;		case 'W': sb.append(getHexaFromNumber(192)); break;
						case 'F': sb.append(getHexaFromNumber(192));break;		case 'O': sb.append(getHexaFromNumber(192));break;		case 'X': sb.append(getHexaFromNumber(192)); break;
						case 'G': sb.append(getHexaFromNumber(192));break;		case 'P': sb.append(getHexaFromNumber(192));break;		case 'Y': sb.append(getHexaFromNumber(192)); break;
						case 'H': sb.append(getHexaFromNumber(192));break;		case 'Q': sb.append(getHexaFromNumber(192));break;		case 'Z': sb.append(getHexaFromNumber(192)); break;
						case 'I': sb.append(getHexaFromNumber(192));break;		case 'R': sb.append(getHexaFromNumber(192));break;		default:  sb.append(getHexaFromNumber(0)); break;
					}
				}	
			}
		} else {
			if ( high ) {
				for ( int i = 0; i<size; i++) {
					switch(charsInWord[i]) {
						case 'A': sb.append(getHexaFromNumber(199));break;		case 'J': sb.append(getHexaFromNumber(19));break;		case 'S': sb.append(getHexaFromNumber(135)); break;
						case 'B': sb.append(getHexaFromNumber(83));break;		case 'K': sb.append(getHexaFromNumber(164));break;		case 'T': sb.append(getHexaFromNumber(19)); break;
						case 'C': sb.append(getHexaFromNumber(7));break;		case 'L': sb.append(getHexaFromNumber(4));break;		case 'U': sb.append(getHexaFromNumber(68)); break;
						case 'D': sb.append(getHexaFromNumber(83));break;		case 'M': sb.append(getHexaFromNumber(108));break;		case 'V': sb.append(getHexaFromNumber(36)); break;
						case 'E': sb.append(getHexaFromNumber(135));break;		case 'N': sb.append(getHexaFromNumber(76));break;		case 'W': sb.append(getHexaFromNumber(68)); break;
						case 'F': sb.append(getHexaFromNumber(135));break;		case 'O': sb.append(getHexaFromNumber(71));break;		case 'X': sb.append(getHexaFromNumber(40)); break;
						case 'G': sb.append(getHexaFromNumber(7));break;		case 'P': sb.append(getHexaFromNumber(199));break;		case 'Y': sb.append(getHexaFromNumber(40)); break;
						case 'H': sb.append(getHexaFromNumber(196));break;		case 'Q': sb.append(getHexaFromNumber(71));break;		case 'Z': sb.append(getHexaFromNumber(163)); break;
						case 'I': sb.append(getHexaFromNumber(19));break;		case 'R': sb.append(getHexaFromNumber(199));break;		default:  sb.append(getHexaFromNumber(0)); break;
					}
				}	
			} else {
				for ( int i = 0; i<size; i++) {
					switch(charsInWord[i]) {
						case 'A': sb.append(getHexaFromNumber(35));break;		case 'J': sb.append(getHexaFromNumber(74));break;		case 'S': sb.append(getHexaFromNumber(225)); break;
						case 'B': sb.append(getHexaFromNumber(233));break;		case 'K': sb.append(getHexaFromNumber(18));break;		case 'T': sb.append(getHexaFromNumber(8)); break;
						case 'C': sb.append(getHexaFromNumber(194));break;		case 'L': sb.append(getHexaFromNumber(194));break;		case 'U': sb.append(getHexaFromNumber(226)); break;
						case 'D': sb.append(getHexaFromNumber(232));break;		case 'M': sb.append(getHexaFromNumber(34));break;		case 'V': sb.append(getHexaFromNumber(6)); break;
						case 'E': sb.append(getHexaFromNumber(195));break;		case 'N': sb.append(getHexaFromNumber(50));break;		case 'W': sb.append(getHexaFromNumber(234)); break;
						case 'F': sb.append(getHexaFromNumber(3));break;		case 'O': sb.append(getHexaFromNumber(226));break;		case 'X': sb.append(getHexaFromNumber(20)); break;
						case 'G': sb.append(getHexaFromNumber(227));break;		case 'P': sb.append(getHexaFromNumber(3));break;		case 'Y': sb.append(getHexaFromNumber(8)); break;
						case 'H': sb.append(getHexaFromNumber(35));break;		case 'Q': sb.append(getHexaFromNumber(242));break;		case 'Z': sb.append(getHexaFromNumber(197)); break;
						case 'I': sb.append(getHexaFromNumber(200));break;		case 'R': sb.append(getHexaFromNumber(19));break;		default:  sb.append(getHexaFromNumber(0)); break;
					}
				}	
			}
		}
		// Añadimos padding
		for ( int i=sb.length(); i < (max_word_length*2) ;i+=2) {
			sb.append("00");
		}

		return sb.toString();
	}
	public static String getASCIIForEEprom(String word,int max_word_length) {
		byte[] wordBytes = word.getBytes();
		byte[] eepromWord = new byte[max_word_length];	//Arrays.fill(eepromWord, (byte) 0); // No es necesario --> se inicializa a 0
		
		for ( int i=0; i<wordBytes.length; i++) {
			if ( (i+1) == wordBytes.length ) { 
				eepromWord[i]=(byte) (((byte) 128) + wordBytes[i]);	// Si es última letra activamos el bit MSB!
			} else {
				eepromWord[i]=wordBytes[i];
			}
		}
		return new String(eepromWord);
	}
	public static String getHexaFromNumber(int number){
		return String.format("%02X",number);
	}
	public static String getHexaFromAscii(String asciiWord, int max_word_length) {
		StringBuilder sb = new StringBuilder("");
		byte[] wordBytes = asciiWord.getBytes();
		int size = wordBytes.length;
		
		for (int i=0; i<size; i++) {
			sb.append( new String(Integer.toHexString(wordBytes[i] & 0xFF) ));
			
		}
		size = sb.length();
		for ( int i=size; i<(max_word_length*2); i++) {
			sb.append("0");
		}
		return sb.toString();
	}
	public static String getLineForEEProm(String word_1_hex,String word_2_hex, int decimalAddress, boolean debug) {
		String address = String.format("%04X",decimalAddress);

		String data = "10"+address+"00"+word_1_hex+word_2_hex;
		String checksum = calculateChecksum(data);
		String eepromLine = ":"+data+checksum;
		if ( debug ) { System.out.println(String.format("%-8s %-8s %-6s %-20s %-20s %-6s %-64s",":10",address,"00",word_1_hex.toUpperCase(),word_2_hex.toUpperCase(),checksum,eepromLine)); }	

		return eepromLine.toUpperCase();
	}
	private static String calculateChecksum(String data) {
		int checksum = 0, result = 0;
		int i = 0;
		String checksumString;
		while (i < data.length()) {
			checksum += Integer.parseInt(data.substring(i, i + 2), 16);
			i += 2;
		}
		result = ~checksum + 1;

		checksumString = String.format("%02X", result);

		return checksumString.substring(checksumString.length() - 2).toUpperCase();
	}
	public static void generateHEXFile(StringBuilder sb,String hex_filename) {
		File hexfile = new File(hex_filename);
		BufferedWriter bwriter = null;
		try {
			bwriter = new BufferedWriter(new FileWriter(hexfile));
			bwriter.write(sb.toString());
			//Guardamos el ultimo codigo
			bwriter.write(":00000001FF\n");
		} catch (Exception e) {
			System.out.println("Se ha producido un problema al guardar en el fichero.\nRevisa que la ruta exista o tengas permisos de escritura");
			e.printStackTrace();
		} finally {
			try {
				bwriter.close();
			} catch (Exception e_fileClose) {}
		}
		
	}
}
