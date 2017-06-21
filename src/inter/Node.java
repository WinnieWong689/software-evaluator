package inter;

import lexer.Lexer;
import lexer.Tag;

public class Node {
	private int tag;
	private Node prev;
	private String name;
	
	public Node(Node prev,String name){
		switch(name){
		case "break":
			tag = Tag.BREAK;
			break;
		case "do":
			tag = Tag.DO;
			break;
		case "else":
			tag = Tag.ELSE;
			break;
		case "for":
			tag = Tag.FOR;
			break;
		case "if":
			tag = Tag.IF;
			break;
		case "while":
			tag = Tag.WHILE;
			break;
		default:
			tag = Tag.ID;	
		}
		this.prev = prev;
		this.name = name;
	}
	
	public int getTag(){
		return tag;
	}
	
	public Node getPrev(){
		return prev;
	}
	
	public String toString(){
		return name;
	}
}
