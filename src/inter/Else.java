package inter;

import lexer.Tag;

public class Else extends Node{
	public Else(Node prev){
		super(prev,"else");
	}
	public String toString(){
		return "else";
	}
}
