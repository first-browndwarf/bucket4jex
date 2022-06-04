package com.browndwarf.bucket4jex.controller;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.browndwarf.bucket4jex.model.MemberDto;
import com.browndwarf.bucket4jex.service.MemberService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucketBuilder;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MemberController {
	
	private static final int MAX_BANDWIDTH = 10;
	
	private static final int TOKEN_REFILL_INTERVAL_SECONDS = 20;
	private static final int TOKEN_REFILL_COUNT_AT_ONCE = 3;
	
	private static final int TOKEN_REFILL_DURATION_MINUTES = 1;
	private static final int TOKEN_REFILL_COUNT = 5;
	
	private static final int TOKEN_CONSUME_COUNT = 1;
	
	@Autowired
	private MemberService	memberService;

	private Bucket	simpleBucket;
	private Bucket	complexGreedyRefillBucket;
	private Bucket	complexIntervalRefillBucket;
	
	public MemberController() {
		simpleBucket = generateSimpleBucket();
		complexGreedyRefillBucket = generateComplexBucket(Arrays.asList(getClassicBandwidth(getGreedyRefill())));
		complexIntervalRefillBucket = generateComplexBucket(Arrays.asList(getClassicBandwidth(getIntervalRefill()))); 
	}
	
	
	@GetMapping("/member/count")
	public	ResponseEntity<Integer>	getTotalMemberCount() {
		
		if (simpleBucket.tryConsume(TOKEN_CONSUME_COUNT)) {
			log.info(">>> Remain bucket Count : {}", simpleBucket.getAvailableTokens()); 
			return	ResponseEntity.ok(memberService.getAllFriendsCount());
		}
		
		log.warn(">>> Exhausted Limit in Simple Bucket");

		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
	}
	
	@GetMapping("/member/name/{name}")
	public ResponseEntity<List<MemberDto>> getMemberListByName(@PathVariable("name") String name) {
		
		if (complexGreedyRefillBucket.tryConsume(TOKEN_CONSUME_COUNT)) {
			log.info(">>> Remain bucket Count : {}", complexGreedyRefillBucket.getAvailableTokens()); 
			return	ResponseEntity.ok(memberService.getFriendInfoListByName(name));			
		}
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
	}
	
	@GetMapping("/member/id/{id}")
	public ResponseEntity<MemberDto> getMemberById(@PathVariable("id") String id) {
		if (complexIntervalRefillBucket.tryConsume(TOKEN_CONSUME_COUNT)) {
			log.info(">>> Remain bucket Count : {}", complexIntervalRefillBucket.getAvailableTokens()); 
			return	ResponseEntity.ok(memberService.getFriendInfoById(id));			
		}
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
	}
	
	// Create Interval Refill Instance
	private Refill getIntervalRefill() {
		return Refill.intervally(TOKEN_REFILL_COUNT_AT_ONCE, Duration.ofSeconds(TOKEN_REFILL_INTERVAL_SECONDS));
	}
	
	// Create Greedy Refill Instance
	private Refill getGreedyRefill() {
		return Refill.greedy(TOKEN_REFILL_COUNT, Duration.ofMinutes(TOKEN_REFILL_DURATION_MINUTES));
	}
	
	// Create Simple Bandwidth Instance
	private Bandwidth getSimpleBandwidth() {
		return Bandwidth.simple(MAX_BANDWIDTH, Duration.ofMinutes(TOKEN_REFILL_DURATION_MINUTES));
	}
	
	// Create Classic Bandwidth Instance
	private Bandwidth getClassicBandwidth(Refill refill) {
		return Bandwidth.classic(MAX_BANDWIDTH, refill);
	}
	
	private Bucket generateSimpleBucket() {
		return Bucket.builder()
				.addLimit(getSimpleBandwidth())
				.build();
	}
	
	private Bucket generateComplexBucket(List<Bandwidth> bandwidthList) {
		LocalBucketBuilder bucketBuilder = Bucket.builder();
		
		for(Bandwidth bandwidth : bandwidthList) {
			bucketBuilder.addLimit(bandwidth);
		}

		return bucketBuilder.build();
	}
}
