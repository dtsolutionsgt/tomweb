package com.dts.rfid;

import com.zebra.rfid.api3.MEMORY_BANK;

public class AccessOperationController {


    protected AccessOperationController() {
    }

    public static MEMORY_BANK getAccessRWMemoryBank(String bankItem) {
        if ("RESV".equalsIgnoreCase(bankItem) || bankItem.contains("PASSWORD"))
            return MEMORY_BANK.MEMORY_BANK_RESERVED;
        else if ("EPC".equalsIgnoreCase(bankItem) || bankItem.contains("PC"))
            return MEMORY_BANK.MEMORY_BANK_EPC;
        else if ("TID".equalsIgnoreCase(bankItem))
            return MEMORY_BANK.MEMORY_BANK_TID;
        else if ("USER".equalsIgnoreCase(bankItem))
            return MEMORY_BANK.MEMORY_BANK_USER;
        return MEMORY_BANK.MEMORY_BANK_EPC;
    }

}
