package com.rayli.service;

import java.math.BigDecimal;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company Prive Financial
 */
public class BalanceServiceImpl implements BalanceService {

	@Override
	public BigDecimal getBalance(String userid) {
		return BigDecimal.valueOf(1000000.0d);
	}

}
