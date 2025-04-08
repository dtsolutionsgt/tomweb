package com.dts.rfid;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.ReaderDevice;
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
