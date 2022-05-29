package com.browndwarf.bucket4jex.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@EqualsAndHashCode
public class MemberDto {

	private String	memberId;
	
	private String 	memberName;
	
	private String	phoneNo;
	
	private String	address;
	
	private int	grade;
	
	
}
