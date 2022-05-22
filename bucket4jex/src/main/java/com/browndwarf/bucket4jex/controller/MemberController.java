package com.browndwarf.bucket4jex.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.browndwarf.bucket4jex.service.MemberService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.local.LocalBucketBuilder;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MemberController {
	
	private static final int MAX_BANDWIDTH = 20;
	
	private static final int TOKEN_REFILL_INTERVAL_SECONDS = 20;
	private static final int TOKEN_REFILL_COUNT_AT_ONCE = 1;
	
	private static final int TOKEN_REFILL_DURATION_MINUTES = 1;
	private static final int TOKEN_REFILL_COUNT = 5;
	
	@Autowired
	private MemberService	memberService;

	// private Bucket bucket;
	

	
	@GetMapping("/idlist/name/{name}")
	public List<String> SearchMemberIDListByName(@PathVariable("name") String name) {
		
		return memberService.getFriendIDListByName(name);
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