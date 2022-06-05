package com.browndwarf.bucket4jex.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;


import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.browndwarf.bucket4jex.model.MemberDto;
import com.browndwarf.bucket4jex.service.MemberService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@TestMethodOrder(OrderAnnotation.class)
@WebMvcTest(MemberController.class)
class MemberControllerTest {
	
	private final static int	MAX_BANDWIDTH = 10;
	private final static int	FAKE_MEMBER_COUNT = 10;
	
	private static final int GREEDY_TOKEN_REFILL_DURATION_MINUTES = 1;
	private static final int GREEDY_TOKEN_REFILL_COUNT = 5;
	private static final String	COMMON_NAME = "Kim";
	
	private static final int INTERVAL_TOKEN_REFILL_DURATION_SECONDS = 20;
	private static final int INTERVAL_TOKEN_REFILL_COUNT = 3;
	private static final String	TEST_ID = "ID001";
	
    @Autowired
    private MockMvc mockMvc;

	@MockBean
	private MemberService	memberService;
	
	private	List<MemberDto>	testMemberList = new ArrayList<MemberDto>();
	
	@BeforeEach
	void setup() {
		testMemberList.add(new MemberDto("ID001", "Kim", "010-1111-1234", "Seoul", 1));
		testMemberList.add(new MemberDto("ID005", "Kim", "010-9876-2580", "Seoul", 3));
		testMemberList.add(new MemberDto("ID006", "Kim", "010-8998-4523", "Mokpo", 1));
	}
	
	@Test
	@Order(1)
	@DisplayName("Case 1. Use SimpleBucket. After N times API calls, and then N+1th call to empty bucket")
	void testGetTotalMemberCount_WithSimpleBucket() {
		
		log.info("Test Case 1");
		
		// Given
		when(memberService.getAllFriendsCount()).thenReturn(FAKE_MEMBER_COUNT);
		
		// When & Then		
		try {
			
			log.info("--- Call API with remained bucket");
			for(int i=0;i < MAX_BANDWIDTH;i++) {
				mockMvc.perform(get("/member/count"))		
				.andExpect(status().isOk())
				.andExpect(content().string(String.valueOf(FAKE_MEMBER_COUNT)));				
			}

			log.info("--- Call API with exausted bucket");
			mockMvc.perform(get("/member/count"))		
			.andExpect(status().isTooManyRequests());	
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	@Test
	@Order(2)
	@DisplayName("Case 2. Use SimpleBucket. After N times API calls, and then N+1th call to refilled bucket")
	void testGetTotalMemberCount_WithRefilledSimpleBucket() {
		
		log.info("Test Case 2");
		
		// Given
		when(memberService.getAllFriendsCount()).thenReturn(FAKE_MEMBER_COUNT);
		
		// When & Then		
		try {
			
			log.info("--- Call API again after refill 1 token");
			Thread.sleep(60 / MAX_BANDWIDTH * 1000 * 1);
			mockMvc.perform(get("/member/count"))		
			.andExpect(status().isOk())
			.andExpect(content().string(String.valueOf(FAKE_MEMBER_COUNT)));	
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	
	@Test
	@Order(3)
	@DisplayName("Case 3. Use Complex Bucket which refill token greedly.")
	void testGetMemberListByName_WithComplexBucket_RefillGrdeedly() {
		
		log.info("Test Case 3");		
		
		// Given
		when(memberService.getFriendInfoListByName(COMMON_NAME)).thenReturn(testMemberList);
		String	targetURL = new StringBuilder("/member/name/").append(COMMON_NAME).toString();
		
		// When & Then		
		try {
			
			log.info("--- Call API with Complex bucket - refill a token gradually");
			for(int i=0;i < MAX_BANDWIDTH;i++) {
				mockMvc.perform(get(targetURL))		
				.andExpect(status().isOk());
				// .andExpect(jsonPath("$.errors", hasSize(3)))				
			}

			log.info("--- Call API with exausted bucket");
			mockMvc.perform(get(targetURL))		
			.andExpect(status().isTooManyRequests());	
			
			log.info("--- Call API again after refill 1 token");
			Thread.sleep(( GREEDY_TOKEN_REFILL_DURATION_MINUTES * 60 ) / GREEDY_TOKEN_REFILL_COUNT * 1000 * 1);
			mockMvc.perform(get(targetURL))		
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$.[0].memberName", is(COMMON_NAME)));

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

	@Test
	@Order(4)
	@DisplayName("Case 4. Use Complex Bucket which refill token per every interval.")
	void testgetMemberById_WithComplexBucket_RefillIntervally() {
		
		log.info("Test Case 4");
		
		// Given
		when(memberService.getFriendInfoById(TEST_ID)).thenReturn(testMemberList.get(0));
		String	targetURL = new StringBuilder("/member/id/").append(TEST_ID).toString();
		
		// When & Then		
		try {
			
			log.info("--- Call API with Complex bucket - Refilling a token at regular intervals");
			for(int i=0;i < MAX_BANDWIDTH;i++) {
				mockMvc.perform(get(targetURL))		
				.andExpect(status().isOk());
				// .andExpect(jsonPath("$.errors", hasSize(3)))				
			}

			log.info("--- Call API with exausted bucket");
			mockMvc.perform(get(targetURL))		
			.andExpect(status().isTooManyRequests());	
			
			log.info("--- Call API again after partial interval period");
			Thread.sleep(INTERVAL_TOKEN_REFILL_DURATION_SECONDS / INTERVAL_TOKEN_REFILL_COUNT * 1000);
			mockMvc.perform(get(targetURL))		
			.andExpect(status().isTooManyRequests());
			
			log.info("--- Call API again after full interval period");
			Thread.sleep(INTERVAL_TOKEN_REFILL_DURATION_SECONDS * 1000);
			mockMvc.perform(get(targetURL))		
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId", is(TEST_ID)))
			.andExpect(jsonPath("$.memberName", is(testMemberList.get(0).getMemberName())))	
			.andExpect(jsonPath("$.phoneNo", is(testMemberList.get(0).getPhoneNo())));

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
}


