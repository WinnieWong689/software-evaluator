package nju.edu.evaluator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/home")
	public String getHome() {
		System.out.println("hello");
		return "homePage";
	}
}
