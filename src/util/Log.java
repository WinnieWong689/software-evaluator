package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lexer.Main;

public class Log {
	public static void log(String log){
		File file = new File(Main.logPath);
		try {
			FileWriter writer = new FileWriter(file,true);
			PrintWriter printer = new PrintWriter(writer);
			printer.println(log);
			printer.flush();
			writer.flush();
			printer.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
