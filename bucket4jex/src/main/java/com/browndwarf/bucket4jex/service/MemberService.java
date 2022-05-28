package com.browndwarf.bucket4jex.service;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.browndwarf.bucket4jex.controller.MemberController;
import com.browndwarf.bucket4jex.model.MemberDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MemberService {

	private	List<MemberDto> memberData;
	
	@PostConstruct
	private void init() {
		memberData = new ArrayList<MemberDto>();
		
		memberData.add(new MemberDto("ID001", "Kim", "010-1111-1234", "Seoul", 1));
		memberData.add(new MemberDto("ID002", "Lee", "010-4040-4887", "Busan", 1));
		memberData.add(new MemberDto("ID003", "Park", "010-6123-5678", "Inchon", 2));
		memberData.add(new MemberDto("ID004", "Choi", "010-2345-9012", "Seoul", 2));
		memberData.add(new MemberDto("ID005", "Kim", "010-9876-2580", "Seoul", 3));
		memberData.add(new MemberDto("ID006", "Kim", "010-8998-4523", "Mokpo", 1));
		memberData.add(new MemberDto("ID007", "Oh", "010-9056-4466", "Kangneung", 2));
		memberData.add(new MemberDto("ID008", "Woo", "010-3412-5634", "Wonjoo", 3));
		memberData.add(new MemberDto("ID009", "Lee", "010-8001-5888", "Busan", 3));
		memberData.add(new MemberDto("ID010", "Coi", "010-4377-6653", "Daejeon", 3));
	}
	
	
	public List<String> getFriendIDListByName(String name) {
		
		log.info(">>> Enter getFriendIDListByName({})", name);
		
		return memberData.stream()
			.filter(member->member.getMemberName().toLowerCase().contains(name.toLowerCase()))
			.map(member->member.getMemberId())
			.collect(Collectors.toList());
	}
	
	public List<String> getAllFriendsName() {
		
		log.info(">>> Enter getAllFriendsName()");
		
		return memberData.stream()
			.map(MemberDto::getMemberName)
			.collect(Collectors.toList());
	}
	
	public int getAllFriendsCount() {
		
		log.info(">>> Enter getAllFriendsCount()");
		
		return memberData.size();
	}

}
