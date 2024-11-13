package com.ccs.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmpData {
	private Integer empNumber;
	
	private String empName;
	
	private Date empJoinDt;
	
	private String empAddr;
}
