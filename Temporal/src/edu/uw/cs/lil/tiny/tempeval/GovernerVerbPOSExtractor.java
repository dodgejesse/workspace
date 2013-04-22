package edu.uw.cs.lil.tiny.tempeval;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.uw.cs.utils.composites.Pair;

public class GovernerVerbPOSExtractor {
	// @Param: a String[] that contains world knowledge about a given pharse, including the sentence, the dependency parse,
	// 		   and the character number that the phrase starts on. 
	public static Pair<String, String> getGovVerbTag(String[] dataItem) {
		String depParse = dataItem[5];
		int charNum = Integer.parseInt(dataItem[4]);
		String sentence = dataItem[1];
		// tokenNum is the number of the starting token of the temporal phrase. starts at 0.
		int tokenNum = getTokenNumFromCharNum(charNum, sentence);
		
		// split the dependency parse into a string[], splitting on new lines.
		String[] depParseArray = depParse.split("\n");
		Pair<String, String> verb = findVerb(depParseArray, tokenNum);
				
		return verb;
	}
	
	private static Pair<String,String> findVerb(String[] depParseArray, int tokenNum){
		int curTokenNum = tokenNum;
		// while loop that traverses the dependency parse to find the governer verb.
		String curPOS = getPOS(depParseArray[curTokenNum]);
		while (!curPOS.startsWith("VB")){
			curTokenNum = getDep(depParseArray[curTokenNum]);
			// in case there is no verb in the 'sentence'.
			if (curTokenNum == -1){
				curPOS = "noVerb";
				break;
			}
			curPOS = getPOS(depParseArray[curTokenNum]);
		}
		String mod = "";
		if (curTokenNum != -1)
			mod += findMod(depParseArray, curTokenNum);
		return Pair.of(mod, curPOS);
	}
	
	private static String findMod(String[] depParseArray, int tokenNum){
		return "";
	}
	
	private static String getPOS(String s){
		String partOfLineEndingWithPOS = regexResult(s, "^[0-9]+\t[^\t]+\t_\t[^\t]+");
		String POS = regexResult(partOfLineEndingWithPOS, "_\t[^\t]+$");
		String result = POS.substring(1).trim();
		return result;
	}
	
	private static int getDep(String s){
		String partOfLineEndingWithDepNum = regexResult(s, "^[0-9]+\t[^\t]+\t_\t[^\t]+\t[^\t]+\t_\t[0-9]+");
		String depNum = regexResult(partOfLineEndingWithDepNum, "[0-9]+$");
		
		return Integer.parseInt(depNum) - 1;
	}
	
	// given a regex and a string, returns the result of the regex applied to the string.
	private static String regexResult (String s, String regex){
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(s);
		String val = "";
		if (m.find())
			val = m.group();
		if (val.equals(""))
			throw new IllegalArgumentException("Problem getting info from regexResult! Probably used on the dependency parse");
		return val;
	}

	private static int getTokenNumFromCharNum(int charNum, String sentence) {
	    PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(sentence),
	              new CoreLabelTokenFactory(), "");
	    int tokenCounter = 0;
	    // weirdest loop ever. Thanks chris manning.
	    for (CoreLabel label ; ptbt.hasNext(); ){
	    	label = ptbt.next();
	    	if (Integer.parseInt(getCharOffsetEndAnnotationNum(label.toString())) > charNum)
	    		break;
	    	tokenCounter++;
	    }
	    return tokenCounter;
	}
	
	private static String getCharOffsetEndAnnotationNum(String s){
		String charOffset = regexResult(s, "CharacterOffsetEndAnnotation=[0-9]+");
		return regexResult(charOffset, "[0-9]+");
	}

}