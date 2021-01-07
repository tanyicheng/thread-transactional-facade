package com.barrett.transactional.util.service;


import com.barrett.transactional.util.threadTransaction.TransactionInfo;

public interface ChildTask {

    <T> void execute(TransactionInfo transactionInfo, T t) throws Exception;
}
