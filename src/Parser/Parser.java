package Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import inter.Break;
import inter.Do;
import inter.Else;
import inter.For;
import inter.If;
import inter.Node;
import inter.While;
import lexer.Lexer;
import lexer.Tag;
import lexer.Token;
import util.Method;

public class Parser {
	private Lexer lex;
	
	private Token look;
	//踩分点1：头文件
	private ArrayList<String> header = new ArrayList<String>();
	//踩分点2：变量的声明
	private HashMap<String,Integer> variables = new HashMap<String,Integer>();
	//踩分点3：for,if/if-else,while/do-while,switch的运用
	private ArrayList<Node> statements = new ArrayList<Node>();
	private HashMap<String,Integer> stmts = new HashMap<String,Integer>();//get number of each stmt
	//踩分点4：指定函数声明，制定规则时必须指定返回值，函数名可选
	private ArrayList<Method> methods = new ArrayList<Method>();
	//踩分点5：指定方法，如scanf()等库函数的调用
	private HashMap<String,Integer> calls = new HashMap<String,Integer>();
	
	public Parser(Lexer l) throws IOException{
		lex = l;
		move();
	}
	
	void move() throws IOException{
		look = lex.scan();
	}
	
	void error(String s){
		System.out.println("near line"+lex.line+": "+s);
		//judge();
	}
	
	void match(int t) throws IOException{
		if( look.tag == t)
			move();
		else
			error("syntax error");
	}
	
	public void program() throws IOException{
		//for(int i=0;i<100;i++){
			//System.out.println(look.toString()+": "+look.tag);
			//look = lex.scan();
		//}
		
		header();
		while(look.tag == Tag.BASIC || look.tag == Tag.VOID){
			Method m = new Method();
			m.returnType = look.toString();
			match(Tag.BASIC);
			m.methodName = look.toString();
			match(Tag.ID);
			match('(');
			while(look.tag!=')'){
				move();
			}
			match(')');
			methods.add(m);
			
			if(look.tag == '{')
				block(null);
			else
				match(';');
		}
	}
	
	void header() throws IOException{
		while(look.tag == '#'){
			move();
			match(Tag.INCLUDE);
			match('<');
			StringBuffer b = new StringBuffer();
			b.append("");
			while(look.tag!='>'){
				b.append(look.toString());
				move();
			}
			header.add(b.toString());
			match('>');
		}
	}
	
	
	void block(Node prev) throws IOException{
		match('{');
		stmts(prev);
		match('}');
	}
	
	void stmts(Node prev) throws IOException{
		if(look.tag == '}')
			return;
		else{
			boolean result = stmt(prev);
			if(result)
				stmts(prev);
			else
				return;
		}
	}
	
	boolean stmt(Node prev) throws IOException{
		switch(look.tag){
		case ';':
			move();
			break;
		case Tag.IF:
			match(Tag.IF);
			If ifNode = new If(prev);
			statements.add(ifNode);
			if(stmts.get("if")!=null){
				stmts.put("if", stmts.get("if")+1);
			}else{
				stmts.put("if", 1);
			}
			match('(');
			while(look.tag!=')'){
				move();
			}
			match(')');
			stmt(ifNode);
			if(look.tag == Tag.ELSE){
				Else elseNode = new Else(prev);
				statements.add(elseNode);
				if(stmts.get("else")!=null){
					stmts.put("else", stmts.get("else")+1);
				}else{
					stmts.put("else", 1);
				}
				match(Tag.ELSE);
				stmt(elseNode);
			}
			break;
		case Tag.WHILE:
			While whileNode = new While(prev);
			statements.add(whileNode);
			if(stmts.get("while")!=null){
				stmts.put("while", stmts.get("while")+1);
			}else{
				stmts.put("while", 1);
			}
			match(Tag.WHILE);
			match('(');
			while(look.tag!=')'){
				move();
			}
			match(')');	
			stmt(whileNode);
			break;
		case Tag.DO:
			Do doNode = new Do(prev);
			statements.add(doNode);
			if(stmts.get("do")!=null){
				stmts.put("do", stmts.get("do")+1);
			}else{
				stmts.put("do", 1);
			}
			match(Tag.DO);
			stmt(doNode);
			match(Tag.WHILE);
			match('(');
			while(look.tag!=')'){
				move();
			}
			match(')');
			match(';');
			break;
		case Tag.BREAK:
			Break breakNode = new Break(prev);
			statements.add(breakNode);
			if(stmts.get("break")!=null){
				stmts.put("break", stmts.get("break")+1);
			}else{
				stmts.put("break", 1);
			}
			match(Tag.BREAK);
			match(';');
			break;
		case Tag.FOR:
			For forNode = new For(prev);
			statements.add(forNode);
			if(stmts.get("for")!=null){
				stmts.put("for", stmts.get("for")+1);
			}else{
				stmts.put("for", 1);
			}
			match(Tag.FOR);
			match('(');
			while(look.tag != ')')
				move();
			match(')');
			stmt(forNode);
			break;
		case Tag.RETURN:
			if(stmts.get("return") == null){
				stmts.put("return", 1);
			}else{
				stmts.put("return", stmts.get("return")+1);
			}
			match(Tag.RETURN);
			while(look.tag != ';')
				move();
			match(';');
			break;
		case '{':
			block(prev);
			break;
		case Tag.NUM:
			while(look.tag != ';')
				move();
			break;
		default:
			if(look.tag == Tag.BASIC){
				delc(null);
			}else{
				if(look.tag == Tag.ID){
					String id = look.toString();
					match(Tag.ID);
					if(look.tag == '='){
						assign();
					}else{
						if(look.tag == '('){
							Integer num = calls.get(id);
							if(num == null)
								calls.put(id, 1);
							else
								calls.put(id, num+1);
							match('(');
							while(look.tag != ')')
								move();
							match(')');
							match(';');
						}else{
							//operate
							while(look.tag != ';')
								move();
							match(';');
						}
					}
				}else{
					return false;
				}
			}
		}
		return true;
	}
	
	void delc(String type) throws IOException{
		if(type == null){
			type = look.toString();
			match(Tag.BASIC);
			if(look.tag == '['){
				type = type + "[]";
				match('[');
				if(look.tag == Tag.NUM)
					match(Tag.NUM);
				match(']');
			}
			match(Tag.ID);
			if(look.tag == '['){
				type = type + "[]";
				match('[');
				if(look.tag == Tag.NUM)
					match(Tag.NUM);
				match(']');
			}
		}
		
		if(variables.get(type)!=null){
			variables.put(type,variables.get(type)+1);
		}else{
			variables.put(type, 1);
		}
		
		if(look.tag == Tag.ID)
			match(Tag.ID);
		if(look.tag == '['){
			match('[');
			if(look.tag == Tag.NUM)
				match(Tag.NUM);
			match(']');
		}
		
		if(look.tag == '='){
			assign();
		}
		if(look.tag == ','){
			match(',');
			delc(type);
		}
		else{
			match(';');
		}
	}
	
	void assign() throws IOException{
		match('=');
		if(look.tag == Tag.ID){
			String id = look.toString();
			match(Tag.ID);
			if(look.tag == '['){
				match('[');
				if(look.tag == Tag.NUM)
					match(Tag.NUM);
				match(']');
			}
			if(look.tag == '('){
				Integer num = calls.get(id);
				if(num == null)
					calls.put(id, 1);
				else
					calls.put(id, num+1);
				match('(');
				while(look.tag != ')')
					move();
				match(')');
			}
		}
		
		while(look.tag != ';' && look.tag !=',')
			move();
	}
	
	public ArrayList<String> getHeader(){
		for(int i = 0;i<header.size();i++){
			System.out.println(header.get(i));
		}
		return header;
	}
	
	public HashMap<String,Integer> getVariables(){
		for(String key:variables.keySet()){
			System.out.println("type "+key+",number "+variables.get(key));
		}
		return variables;
	}
	
	public HashMap<String,Integer> getStmts(){
		for(Node n:statements){
			System.out.println(n.toString());
		}
		return stmts;
	}
	
	public ArrayList<Method> getMethods(){
		for(Method m:methods){
			System.out.println("return type:"+m.returnType+",method name:"+m.methodName);
		}
		return methods;
	}
	
	public HashMap<String,Integer> getCalls(){
		for(String key:calls.keySet()){
			System.out.println("call:"+key+",num:"+calls.get(key));
		}
		return calls;
	}
}
