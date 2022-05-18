package com.dts.rfid;

import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.media.ToneGenerator;
import android.os.AsyncTask;

import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFModeTable;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.StartTrigger;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.UNIQUE_TAG_REPORT_SETTING;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class RFIDController {

    //public  static RFIDReader mConnectedReader;
    //public static ReaderDevice mConnectedDevice;

    //Boolean to keep track of whether the inventory is running or not
    public static volatile boolean mIsInventoryRunning;
    public static volatile boolean mInventoryStartPending;
    public static int inventoryMode = 0;
    public static Boolean isBatchModeInventoryRunning = false;
    public static String accessControlTag;
    //Variable to maintain the RR started time to maintain the read rate
    public static volatile long mRRStartedTime;
    public static PreFilters[] preFilters = null;
    public static boolean isAccessCriteriaRead = false;
    public static int preFilterIndex = -1;
    //For Notification
    public static volatile int INTENT_ID = 100;

    public static boolean tagListMatchAutoStop = true;
    public static boolean tagListMatchNotice = false;

    public static boolean isGettingTags;
    public static boolean EXPORT_DATA;

    public static BluetoothDevice BTDevice;
    public static boolean isLocatingTag;
    public static String currentLocatingTag;

    public static String importFileName = "";

    public static StartTrigger settings_startTrigger;
    public static StopTrigger settings_stopTrigger;
    public static short TagProximityPercent = -1;
    public static TagStorageSettings tagStorageSettings;
    public static int batchMode;
    public static Events.BatteryData BatteryData = null;
    public static DYNAMIC_POWER_OPTIMIZATION dynamicPowerSettings;
    public static boolean is_disconnection_requested;
    public static boolean is_connection_requested;
    //RFIDController Settings
    public static volatile boolean AUTO_DETECT_READERS;
    public static volatile boolean AUTO_RECONNECT_READERS;
    public static volatile boolean NOTIFY_READER_AVAILABLE;
    public static volatile boolean NOTIFY_READER_CONNECTION;
    public static volatile boolean NOTIFY_BATTERY_STATUS;
    public static String LAST_CONNECTED_READER = "";
    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public static BEEPER_VOLUME sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    // Singulation control
    public static Antennas.SingulationControl singulationControl;
    // regulatory
    public static RegulatoryConfig regulatory;
    public static Boolean regionNotSet = false;
    // antenna
    public static RFModeTable rfModeTable;
    public static Antennas.AntennaRfConfig antennaRfConfig;
    public static int[] antennaPowerLevel;
    public static Readers readers;

    public static boolean settingsactivityResumed;
    public static ReaderDevice mReaderDisappeared;
    public static ToneGenerator toneGenerator;
    //public static Activity contextSettingDetails = null;
    public static String currentFragment = "";   //for MTC export data, when curr frag is rr it should export as previously.
    public static boolean SHOW_CSV_TAG_NAMES = false;
    public static boolean asciiMode = false;
    //public static ProfileContent.ProfilesItem ActiveProfile;
    public static String PreFilterTagID;
    public static boolean NON_MATCHING = false;


    public static UNIQUE_TAG_REPORT_SETTING reportUniquetags = null;
    public static boolean ledState = false;
    public static int beeperspinner_status;
    public static String TAG = "RFIDDEMO";


    public static boolean brandidcheckenabled = false;
    //   public static boolean iLogo1, iLogo2, iLogo3, iLogo4, iIgnore = false;
    public static String strCurrentImage = "";
    // public static Bitmap logoBitmap1, logoBitmap2, logoBitmap3, logoBitmap4;
    public static boolean bFound = false;
    //From MainActivity

    public static Boolean isInventoryAborted;
    public static Boolean isTriggerRepeat;
    public static boolean pc = false;
    public static boolean rssi = false;
    public static boolean phase = false;
    public static boolean channelIndex = false;
    public static boolean tagSeenCount = false;
    public static boolean isLocationingAborted;
    //public static boolean isPreFilterSimpleEnabled;
    //public static boolean isPreFilterAdvanceEnabled;
    public static AsyncTask<Void, Void, Boolean> autoConnectDeviceTask;


    private static volatile RFIDController instance;
    private static Object mutex = new Object();

    private AccessOperationController accessOperationController = new AccessOperationController();
    private InventoryController inventoryController = new InventoryController();
    //private ConnectionController connectionController = new ConnectionController();
    //private LocationingController locationingController = new LocationingController();




    public void updateTagIDs() {
        //inventoryController.updateTagIDs();
    }


    public static void getTagReportingFields() {
        pc = false;
        phase = false;
        channelIndex = false;
        rssi = false;
        if (tagStorageSettings != null) {
            TAG_FIELD[] tag_field = tagStorageSettings.getTagFields();
            for (int idx = 0; idx < tag_field.length; idx++) {
                if (tag_field[idx] == TAG_FIELD.PEAK_RSSI)
                    rssi = true;
                if (tag_field[idx] == TAG_FIELD.PHASE_INFO)
                    phase = true;
                if (tag_field[idx] == TAG_FIELD.PC)
                    pc = true;
                if (tag_field[idx] == TAG_FIELD.CHANNEL_INDEX)
                    channelIndex = true;
                if (tag_field[idx] == TAG_FIELD.TAG_SEEN_COUNT)
                    tagSeenCount = true;
            }
        }
    }

    public static boolean getRepeatTriggers() {
        if ((settings_startTrigger != null && (settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD || settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC))
                || (isTriggerRepeat != null && isTriggerRepeat))
            return true;
        else
            return false;
    }


    /**
     * method to start a timer task to get device battery status per every 60 seconds
     */
    private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> taskHandle;

    /**
     * method to stop timer
     */
    public static void stopTimer() {
        if (taskHandle != null) {
            taskHandle.cancel(true);
            scheduler.shutdown();
        }
        taskHandle = null;
        scheduler = null;
    }


}
