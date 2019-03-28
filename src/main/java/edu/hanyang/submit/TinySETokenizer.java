package edu.hanyang.submit;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.hanyang.indexer.Tokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.PorterStemmer;


public class TinySETokenizer implements Tokenizer {

	public void setup() {
	}

	public List<String> split(String text) {
		List<String> result = new ArrayList<String>();
		Analyzer analyzer = new SimpleAnalyzer();
		TokenStream tokenStream = analyzer.tokenStream("test", text);
		PorterStemmer PS = new PorterStemmer();
		CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
		try {
			tokenStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while(tokenStream.incrementToken()) {
				PS.setCurrent(attr.toString());
				PS.stem();
                result.add(PS.getCurrent());
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void clean() {
	}

//	public static void main(String args[]){
//		TinySETokenizer obj = new TinySETokenizer();
//		List<String> result = obj.split("Where will the next Olympics be held?");
//		System.out.println(result);
//	}

}