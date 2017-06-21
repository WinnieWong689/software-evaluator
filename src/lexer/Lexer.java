package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class Lexer {
	public static int line = 1;
	//the variable peek is used to store the next char read in
	char peek = ' ';
	Hashtable words  = new Hashtable();
	File file;
	FileReader fileReader;
	
	void reserve(Word w){
		words.put(w.lexeme, w);
	}
	
	public Lexer(String path) throws FileNotFoundException{
		line = 1;
		System.out.println(path);
		reserve(new Word("return",Tag.RETURN));
		reserve(new Word("for",Tag.FOR));
		reserve(new Word("switch",Tag.SWITCH));
		reserve(new Word("void",Tag.VOID));
		reserve(new Word("include",Tag.INCLUDE));
		reserve(new Word("if",Tag.IF));
		reserve(new Word("else",Tag.ELSE));
		reserve(new Word("while",Tag.WHILE));
		reserve(new Word("do",Tag.DO));
		reserve(new Word("break",Tag.BREAK));
		reserve(Word.True);
		reserve(Word.False);
		reserve(new Word("int",Tag.BASIC));
		reserve(new Word("float",Tag.BASIC));
		reserve(new Word("char",Tag.BASIC));
		reserve(new Word("bool",Tag.BASIC));
		file = new File(path);
		fileReader = new FileReader(file);
	}
	
	void readch() throws IOException{
		peek = (char)fileReader.read();
	}
	
	boolean readch(char c) throws IOException{
		readch();
		if(peek != c) return false;
		peek = ' ';
		return true;
	}
	
	public Token scan() throws IOException{
		for( ; ; readch()){
			if(peek == ' ' || peek == '\t' || peek == '\r'){continue;}
			else{ 
				if (peek == '\n' ) {line = line + 1;}
				else{
					if(peek == '/'){
						readch();
						if(peek == '/'){
							while(peek != '\n')
								readch();
							continue;
						}
						if(peek == '*'){
							while(true){
								readch();
								if(peek == '*'){
									readch();
									if(peek == '/'){
											peek = ' ';
											break;
										}
								}
							}
							continue;
						}
						Token tok = new Token('/');
						peek = ' ';
						return tok;
					}else{
						break;
					}
					}
				}
			
		}
		
		switch(peek){
		case '&':
			if(readch('&')) return Word.and; else return new Token('&');
		case '|':
			if(readch('|')) return Word.or; else return new Token('|');
		case '=':
			if(readch('=')) return Word.eq; else return new Token('=');
		case '!':
			if(readch('=')) return Word.ne; else return new Token('!');
		case '<':	
			if(readch('=')) return Word.le; else return new Token('<');
		case '>':
			if(readch('=')) return Word.ge; else return new Token('>');
		case '+':
			if(readch('+')) return Word.incr; else return new Token('+');
		case '-':
			if(readch('-')) return Word.decr; else return new Token('-');
		}
		if(Character.isDigit(peek)){
			int v = 0;
			do{
				v = 10*v +Character.digit(peek, 10);
				readch();
			}while(Character.isDigit(peek));
			if(peek != '.')
				return new Num(v);
			float x = v;
			float d = 10;
			for(;;){
				readch();
				if(!Character.isDigit(peek)) break;
				x = x +Character.digit(peek, 10)/d;
				d = d * 10;
			}
			return new Real(x);
		}
		if(Character.isLetter(peek)){
			StringBuffer b = new StringBuffer();
			do{
				b.append(peek);
				readch();
			}while(Character.isLetterOrDigit(peek));
			String s = b.toString();
			Word w = (Word)words.get(s);
			if(w != null) return w;
			w = new Word(s,Tag.ID);
			words.put(s, w);
			return w;
		}
		
		Token tok = new Token(peek);
		peek = ' ';
		return tok;
	}
}
