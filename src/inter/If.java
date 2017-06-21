package inter;

import lexer.Tag;

public class If extends Node{
	public If(Node prev){
		super(prev,"if");
	}
	public String toString(){
		return "if";
	}
}
