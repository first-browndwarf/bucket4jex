package com.browndwarf.bucket4jex.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.browndwarf.bucket4jex.service.MemberService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@WebMvcTest(MemberController.class)
class MemberControllerTest {
	
	private final static int	MAX_BANDWIDTH = 10;
	private final static int	FAKE_MEMBER_COUNT = 10;
	
    @Autowired
    private MockMvc mockMvc;

	@MockBean
	private MemberService	memberService;
	

	@Test
	@Order(1)
	@DisplayName("Case 1. Use SimpleBucket. After N times API calls, and then N+1th call to empty bucket")
	void testGetTotalMemberCount_WithSimpleBucket() {
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

}


