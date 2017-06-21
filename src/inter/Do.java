package inter;

import lexer.Tag;

public class Do extends Node{
	public Do(Node prev){
		super(prev,"do");
	}
	
	public String toString(){
		return "do";
	}
}
