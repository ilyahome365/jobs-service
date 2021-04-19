package com.home365.jobservice.utils;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import org.modelmapper.ModelMapper;

public abstract class Converters {
    public static final ModelMapper modelMapper = new ModelMapper();

    private Converters(){

    }

    public static Transactions fromTransactionsWithProjectedBalanceToTransactions(TransactionsWithProjectedBalance transactionsWithProjectedBalance) {
        return modelMapper.map(transactionsWithProjectedBalance, Transactions.class);
    }



}
