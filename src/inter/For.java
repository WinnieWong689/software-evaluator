package inter;

import lexer.Tag;

public class For extends Node{
	public For(Node prev){
		super(prev,"for");
	}
	
	public String toString(){
		return "for";
	}
}
