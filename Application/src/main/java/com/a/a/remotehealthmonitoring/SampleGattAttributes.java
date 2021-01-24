package com.a.a.remotehealthmonitoring;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HM_10 = "0000ffe1-0000-1000-8000-00805f9b34fb"; // - My HM_10
    public static String HM_10_bluno = "0000dfb1-0000-1000-8000-00805f9b34fb"; // - My HM_10  //-first Unknown characteristic
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
//        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM-10 Service");   //- My HM_10     //Unknown service
        attributes.put("0000dfb0-0000-1000-8000-00805f9b34fb", "HM-10 Service");   //- My HM_10_bluno //Unknown service
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
//        attributes.put(HM_10, "HM-10 Module");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
