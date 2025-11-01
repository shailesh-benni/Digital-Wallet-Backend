package com.example.demo.mapper;

import com.example.demo.dto.*;
import com.example.demo.model.Wallet;

public class WalletMapper {

    private WalletMapper() {}

    public static LoadMoneyResponse toLoadMoneyResponse(Wallet wallet, String message) {
        return new LoadMoneyResponse(message, wallet.getBalance());
    }

    public static TransferResponse toTransferResponse(Wallet senderWallet, String message) {
        return new TransferResponse(message, senderWallet.getBalance());
    }
}
