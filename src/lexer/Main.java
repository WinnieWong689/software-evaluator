package lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import Parser.Parser;
import inter.Node;
import util.Log;
import util.Method;
import util.Student;

public class Main{
	static Runtime runtime = Runtime.getRuntime();
	private ArrayList<Student> studentList = new ArrayList<Student>();
	public static String rootPath = "";
	public static String logPath = "";
	
	//踩分点1：头文件
	ArrayList<String> header = new ArrayList<String>();
	//踩分点2：变量的声明
	HashMap<String,Integer> variables = new HashMap<String,Integer>();
	//踩分点3：for,if/if-else,while/do-while,switch的运用
	HashMap<String,Integer> statements = new HashMap<String,Integer>();
	//踩分点4：指定函数声明，制定规则时必须指定返回值，函数名可选
	ArrayList<Method> methods = new ArrayList<Method>();
	//踩分点5：指定方法，如scanf()等库函数的调用
	HashMap<String,Integer> calls = new HashMap<String,Integer>();
			
	HashMap<String,Integer> scoreTable = new HashMap<String,Integer>();
			
	public static void main(String[] args){
		Main main = new Main();
		main.start();
	}
	
	private void start(){
		Scanner scan = new Scanner(System.in);
		System.out.println("please enter the root directory:");
		rootPath = scan.nextLine();
		String codePath = rootPath + "/code";
		if(!(new File(rootPath)).exists()){
			System.out.println("root path not found");
			scan.close();
			return;
		}
		logPath = rootPath + "/log/log"+System.currentTimeMillis()+".txt";
		initRule();
		File codeFiles = new File(codePath);
		File[] codeFileList = codeFiles.listFiles();
		for(File file:codeFileList){
			if(file.isDirectory()){
				Student s = new Student();
				s.setStudentID(file.getName());
				File[] files = file.listFiles();
				Arrays.sort(files,new Comparator<File>(){
					@Override
					public int compare(File file1, File file2) {
						int result = (int)(file2.lastModified()-file1.lastModified());
						return result;
					}
				});
				String regex = ".*\\.c";
				String codeFilePath = "";
				String parentDir = file.getAbsolutePath();
				for(File f:files){
					if(f.getName().matches(regex)){
						codeFilePath = f.getAbsolutePath();
						break;
					}
				}
				if(codeFilePath.equals("")){
					Log.log("did not find code file in directory "+file.getAbsolutePath());
					System.out.println("did not find code file in directory "+file.getAbsolutePath());
					s.setErrorMessage("code not found");
					studentList.add(s);
				}else{
					unitTest(parentDir,s,codeFilePath);
				}
			}
		}		
		scan.close();
		System.out.println("end");
		writeReport();
	}
	
	private void unitTest(String parentDir,Student student,String codeFilePath){
		if(compile(parentDir,codeFilePath)){
			//run all the test case
			student = runTestCase(parentDir,student);
			//get test case score and decide wither the student need content analysis
			getScore(codeFilePath,student);
			student.setTotalScore(student.getTestCaseScore()+student.getContentScore());
		}else{
			student.setErrorMessage("compileFail");
		}
		studentList.add(student);
	}
	
	private boolean compile(String parentDir,String codeFilePath){
		String cmd = "c:/MinGW/bin/gcc "+ codeFilePath +" -o " + parentDir+"/result.exe";
		Process process = null;
		System.out.println("compile "+ codeFilePath);
		try {
			process = runtime.exec("cmd /c"+cmd);
			boolean compileResult = process.waitFor(2,TimeUnit.MINUTES);
			if(!compileResult){
				String errorMsg = codeFilePath + " compileTimeout";
				System.out.println(errorMsg);
				Log.log(errorMsg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File file = new File(parentDir + "/result.exe");
		if(file.exists())
			return true;
		else
			return false;
	}
	
	private Student runTestCase(String path,Student student){
		String cmd = path+"\\result.exe"; 
		Process process = null;
		String parameters;
		String expectedResult = null;
		String actualResult = null;
		Log.log("Student "+student.getStudentID());
		try {
			File file = new File(rootPath + "/input/test.txt");
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			
			int i = 0;
			while((parameters = bufferedReader.readLine()) != null){
				i++;
				if(i%3!=0){
				parameters = parameters.trim();
				expectedResult = bufferedReader.readLine().trim();
				i++;
				}else{
						parameters = bufferedReader.readLine().trim();
						i++;
						expectedResult = bufferedReader.readLine().trim();
						i++;
				}
				Log.log("parameters:"+parameters+" expectedResult:"+expectedResult);
			    process = runtime.exec("cmd /c"+cmd);
			    OutputStream output = process.getOutputStream();
				parameters+="\n";
				output.write(parameters.getBytes());
				output.flush();
				actualResult = getOutput(process);
				LogError(process);
				Log.log("actualResult:"+actualResult);
				Boolean testResult = judge(expectedResult,actualResult);
				process.waitFor();
				if(testResult)
					student.setPass(student.getPass()+1);
				else
					student.setFail(student.getFail()+1);
			}
			student.setTotalTestCase(i/3+1);
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return student;
	}
	
	//get output of result.exe
		private String getOutput(Process process){
			String result = "";
			
			InputStream input = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			try {
				result = br.readLine();
				//only read the first line of output
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				boolean isInTime = process.waitFor(2,TimeUnit.SECONDS);
				if(!isInTime){
					Log.log("test case timeout");
					System.out.println("test case timeout");
					return null;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return result;	
		}
		
		private void LogError(Process process){
			InputStream error = process.getErrorStream();
			BufferedReader errorBR = new BufferedReader(new InputStreamReader(error));
			String tmp = "";
			try {
				while((tmp = errorBR.readLine())!=null){
					Log.log(tmp);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private boolean judge(String expectedResult,String actualResult){
			if(expectedResult.equals(actualResult)){
				return true;
			}else{
				return false;
			}
		}
		
		private Student getScore(String codeFilePath,Student student){
			double accuracy = (double)student.getPass()/student.getTotalTestCase();
			if(accuracy > 0.8){
				student.setTestCaseScore((int)Math.rint(accuracy*100));
			}else{
				student.setTestCaseScore(0);
				contentAnalyse(codeFilePath,student);
			}
			return student;
		}
		
	private void initRule(){
		String rulePath = rootPath + "/input/rule.txt";
		File ruleFile = new File(rulePath);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(ruleFile));
			String tmp = "";
			while((tmp = bufferedReader.readLine())!=null){
				String[] ruleEntry = tmp.split(";");
				if(ruleEntry.length != 4 && ruleEntry.length != 5){
					System.out.println("ERROR:Rule pattern is wrong!");
					return;
				}else{
					String ruleType = ruleEntry[0];
					switch(ruleType){
					case "header":
						header.add(ruleEntry[1]);
						scoreTable.put(ruleEntry[1], Integer.valueOf(ruleEntry[3]));
						break;
					case "delc":
						variables.put(ruleEntry[1], Integer.valueOf(ruleEntry[2]));
						scoreTable.put(ruleEntry[1], Integer.valueOf(ruleEntry[3]));
						break;
					case "statement":
						statements.put(ruleEntry[1], Integer.valueOf(ruleEntry[2]));
						scoreTable.put(ruleEntry[1], Integer.valueOf(ruleEntry[3]));
						break;
					case "method":
						Method m = new Method();
						m.returnType = ruleEntry[1];
						m.methodName = ruleEntry[2];
						methods.add(m);
						scoreTable.put(m.methodName, Integer.valueOf(ruleEntry[4]));
						break;
					case "call":
						calls.put(ruleEntry[1], Integer.valueOf(ruleEntry[2]));
						scoreTable.put(ruleEntry[1], Integer.valueOf(ruleEntry[3]));
						break;
					default:
						System.out.println("ERROR:Rule pattern is wrong!");
						return;
					}
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void contentAnalyse(String codeFilePath,Student student){
		ArrayList<String> actualHeader = new ArrayList<String>();
		HashMap<String,Integer> actualVar = new HashMap<String,Integer>();
		HashMap<String,Integer> actualStmts = new HashMap<String,Integer>();
		ArrayList<Method> actualMethods = new ArrayList<Method>();
		HashMap<String,Integer> actualCalls = new HashMap<String,Integer>();
		
		
		try {
			Lexer lex = new Lexer(codeFilePath);
			Parser parse = new Parser(lex);
			parse.program();
			
			actualHeader = parse.getHeader();
			actualVar = parse.getVariables();
			actualMethods = parse.getMethods();
			actualStmts = parse.getStmts();
			actualCalls = parse.getCalls();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//get content score
		int contentScore = 0;
		int percentage = 0;// percentage geted by the code
		for(String h: header){
			//System.out.println("header:"+h);
			if(actualHeader.contains(h)){
				percentage += scoreTable.get(h);
			}
		}
		Set<String> callsKeySet = calls.keySet();
		for(String key: callsKeySet){
			Integer expectedNumber = calls.get(key);
			Integer actualNumber = actualCalls.get(key);
			//System.out.println("call "+"expNum:"+expectedNumber+"actNum:"+actualNumber);
			if(actualNumber != null){
				int scorePercentage = scoreTable.get(key);
				if(actualNumber>=expectedNumber){
					percentage = percentage + expectedNumber*scorePercentage;
				}else{
					percentage = percentage + actualNumber*scorePercentage;
				}
			}
		}
		
		Set<String> varsKeySet = variables.keySet();
		for(String key:varsKeySet){
			Integer expectedNumber = variables.get(key);
			Integer actualNumber = actualVar.get(key);
			//System.out.println("var "+"expNum:"+expectedNumber+"actNum:"+actualNumber);
			if(actualNumber != null){
				int scorePercentage = scoreTable.get(key);
				if(actualNumber>=expectedNumber){
					percentage = percentage + expectedNumber*scorePercentage;
				}else{
					percentage = percentage + actualNumber*scorePercentage;
				}
			}
		}
		Set<String> stmtKeySet = statements.keySet();
		for(String key:stmtKeySet){
			Integer expectedNumber = statements.get(key);
			Integer actualNumber = actualStmts.get(key);
			//System.out.println("stmt "+"expNum:"+expectedNumber+"actNum:"+actualNumber);
			if(actualNumber != null){
				int scorePercentage = scoreTable.get(key);
				if(actualNumber>=expectedNumber){
					percentage = percentage + expectedNumber*scorePercentage;
				}else{
					percentage = percentage + actualNumber*scorePercentage;
				}
			}
		}
		for(Method method:methods){
			String expectedReturnType = method.returnType;
			String expectedMethodName = method.methodName;
			for(Method mm:actualMethods){
				if(mm.methodName.equals(expectedMethodName) && mm.returnType.equals(expectedReturnType)){
					percentage = percentage + scoreTable.get(expectedMethodName);
					//System.out.println("method "+ expectedMethodName);
					break;
				}
					
			}
		}
		contentScore = (int)Math.rint(((double)percentage/100)*80);
		student.setContentScore(contentScore);
		System.out.println("contentScore of "+student.getStudentID() +": "+ contentScore);
	}
	
	private void writeReport(){
		String filePath = rootPath+"/report/report"+String.valueOf(System.currentTimeMillis())+".xls";
		System.out.println(filePath);
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
			Workbook wb = new HSSFWorkbook();
			Sheet s = wb.createSheet();
			wb.setSheetName(0, "scores");
			//Font font = wb.createFont();
			Row firstRow = s.createRow(0);
			firstRow.createCell(0).setCellValue("StudentID");
			firstRow.createCell(1).setCellValue("Name");
			firstRow.createCell(2).setCellValue("TotalTestCase");
			firstRow.createCell(3).setCellValue("Pass");
			firstRow.createCell(4).setCellValue("fail");
			firstRow.createCell(5).setCellValue("TestCaseScore");
			firstRow.createCell(6).setCellValue("ContentScore");
			firstRow.createCell(7).setCellValue("TotalScore");
			firstRow.createCell(8).setCellValue("errorMessage");
			int rowNumber = 1;
			for(Student student : studentList){
				Row tmp = s.createRow(rowNumber);
				rowNumber++;
				tmp.createCell(0).setCellValue(student.getStudentID());
				tmp.createCell(2).setCellValue(student.getTotalTestCase());
				tmp.createCell(3).setCellValue(student.getPass());
				tmp.createCell(4).setCellValue(student.getFail());
				tmp.createCell(5).setCellValue(student.getTestCaseScore());
				tmp.createCell(6).setCellValue(student.getContentScore());
				tmp.createCell(7).setCellValue(student.getTotalScore());
				tmp.createCell(8).setCellValue(student.getErrorMessage());
			}
			wb.write(fileOutputStream);
			wb.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
