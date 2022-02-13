package com.browndwarf.bucket4jex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.browndwarf.bucket4jex.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MemberController {
	
	@Autowired
	private MemberService	memberService;

	// private Bucket bucket;

	
	@GetMapping("/idlist/name/{name}")
	public List<String> SearchMemberIDListByName(@PathVariable("name") String name) {
		
		return memberService.getFriendIDListByName(name);
	}
}
