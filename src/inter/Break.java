package inter;

import lexer.Tag;

public class Break extends Node{
	public Break(Node prev){
		super(prev,"break");
	}
	public String toString(){
		return "break";
	}
}
