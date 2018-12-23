package com.mindmap.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mindmap.model.Document;
import com.mindmap.model.Ownership;
import com.mindmap.model.Response;
import com.mindmap.model.User;
import com.mindmap.repository.DocumentRepository;
import com.mindmap.repository.OwnershipRepository;
import com.mindmap.repository.UserRepository;
import com.mindmap.token.TokenUtil;
import com.nimbusds.jose.KeyLengthException;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	@Autowired
	DocumentRepository documentRepository;
	@Autowired
	OwnershipRepository ownershipRepository;

	private final static Logger logger = LoggerFactory.getLogger(UserController.class);

	@ResponseBody
	@RequestMapping(value="/sign/in", method = RequestMethod.POST)
	public Response signIn(@RequestBody User user) {
		HashMap<String, Object> ret = new HashMap<>();
		Response res = null;
		User us = userRepository.findByName(user.getName());
		if(null != us) {
			if(us.getPassword().equals(user.getPassword())) {
				ret.put("user", us);
				String token = null;
				try {
					token = TokenUtil.createToken(us.getId());
					ret.put("token", token);
					
				} catch (KeyLengthException e) {
					e.printStackTrace();
				}
				res =  new Response("success", ret, "");
				return res;
			}
		}
		res = new Response("error", ret, "用户名或密码不正确");
		return res;
	}
	
	@ResponseBody
	@RequestMapping(value="/sign/up", method = RequestMethod.POST)
	public Response signUp(@RequestBody User user) {
		try {
			User us = userRepository.findByName(user.getName());
			if (null != us) {
				logger.debug("User name already exists.");
				return new Response("error", null, "用户名已存在");
			}
			userRepository.save(user);
			Document root = documentRepository.save(new Document("根目录", true));
			ownershipRepository.save(new Ownership(user.getId(), root.getId()));
			logger.debug("User record inserted.");
			return new Response("success", user, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Response("error", null, "服务器内部错误");
	}
	

	@RequestMapping(value="/info", method = RequestMethod.GET)
	public String userInfo(@RequestParam("token") String token) {
		try {
			TokenUtil.validateToken(token, "token");
		}catch (Exception e) {
			return "/";
		}
		return "/d.html";
	}
	
	@ResponseBody
	@RequestMapping(value="/info/detail", method = RequestMethod.GET)
	public Response getUserInfo(@RequestParam("token") String token) {
		Response res = null;
		try {
			int id = TokenUtil.validateToken(token, "token");
			User u = userRepository.findById(id).get();
			res = new Response("success", u, "");
		}catch (Exception e) {
			logger.error(e.getMessage());
			res = new Response("error", null, "此用户信息不存在");
		}
		return res;
	}