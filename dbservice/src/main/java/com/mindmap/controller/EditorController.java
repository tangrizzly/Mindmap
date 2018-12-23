package com.mindmap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mindmap.token.TokenUtil;

@Controller
@RequestMapping("/editor")
public class EditorController {
	@RequestMapping(method=RequestMethod.GET)
	public String index(@RequestParam("token") String token) {
		try {
			TokenUtil.validateToken(token, "token");
		}catch (Exception e) {
			return "/";
		}
		return "c/c.html";
	}
}
