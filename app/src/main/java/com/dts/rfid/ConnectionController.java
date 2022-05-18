package com.dts.rfid;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.VersionInfo;

import java.util.ArrayList;

public class ConnectionController {


    protected ConnectionController() {
    }


    public ReaderDevice getConnectedDeviceFromRFIDReaderList(String deviceName) throws InvalidUsageException {
        ArrayList<ReaderDevice> readersListArray = RFIDController.readers.GetAvailableRFIDReaderList();
        if (readersListArray.size() == 1) {
            return readersListArray.get(0);
        } else {
            for (int prevreader = readersListArray.size() - 1; prevreader >= 0; prevreader--) {
                if (readersListArray.get(prevreader).getName().equals(deviceName)) {
                    return readersListArray.get(prevreader);
                }
            }
        }
        return null;
    }

}
