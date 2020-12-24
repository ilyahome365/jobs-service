package com.home365.jobservice.utils;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.model.mail.MailDetails;
import org.modelmapper.ModelMapper;

public abstract class Converters {
    public static  ModelMapper modelMapper = new ModelMapper();



    public static Transactions fromTransactionsWithProjectedBalanceToTransactions(TransactionsWithProjectedBalance transactionsWithProjectedBalance) {
        return modelMapper.map(transactionsWithProjectedBalance, Transactions.class);
    }



}
