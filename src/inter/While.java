package inter;

import lexer.Tag;

public class While extends Node{
	public While(Node prev){
		super(prev,"while");
	}
	public String toString(){
		return "while";
	}
}
