package util;

public class Student {
	//student ID
	private String studentID;
	//number of test cases
	private int totalTestCase;
	//number of test cases passed
	private int pass;
	//number of test cases failed
	private int fail;
	//score of test cases
	private int testCaseScore;
	//score of content analysis
	private int contentScore;
	//total score
	private int totalScore;
	//error message
	private String errorMessage;
	
	public Student(){
		studentID = "id";
		totalTestCase = 0;
		pass = 0;
		fail = 0;
		testCaseScore = 0;
		contentScore = 0;
		totalScore = 0;
		errorMessage = "";
	}
	
	public String getStudentID(){
		return studentID;
	}
	
	public int getTotalTestCase(){
		return totalTestCase;
	}
	
	public int getPass(){
		return pass;
	}
	
	public int getFail(){
		return fail;
	}
	
	public int getTestCaseScore(){
		return testCaseScore;
	}
	
	public int getContentScore(){
		return contentScore;
	}
	
	public int getTotalScore(){
		return totalScore;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	public void setStudentID(String studentID){
		this.studentID = studentID;
	}
	
	public void setTotalTestCase(int totalTestCase){
		this.totalTestCase = totalTestCase;
	}
	
	public void setPass(int pass){
		this.pass = pass;
	}
	
	public void setFail(int fail){
		this.fail = fail;
	}
	
	public void setTestCaseScore(int testCaseScore){
		this.testCaseScore = testCaseScore;
	}
	
	public void setContentScore(int contentScore){
		this.contentScore  = contentScore;
	}
	
	public void setTotalScore(int totalScore){
		this.totalScore = totalScore;
	}
	
	public void setErrorMessage(String errorMessage){
		this.errorMessage = errorMessage;
	}
}
