package com.dts.rfid;

import android.os.AsyncTask;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;

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
