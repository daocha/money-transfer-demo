package com.rayli.service;

import java.math.BigDecimal;

/**
 * @author Ray LI
 * @date 4 Feb 2020
 * @company Prive Financial
 */
public interface BalanceService {

	BigDecimal getBalance(String userid);

}
