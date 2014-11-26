package com.example.wifi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectToAP("12345", "12345");

        WifiConfiguration wifiConf = null;
        WifiManager wifiManager = (WifiManager) getSystemService(MainActivity.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            String BACKSLASH = "\"";
            if (conf.SSID.equals(BACKSLASH + "12345" + BACKSLASH)) {
                // if (conf.SSID.equals("12345")) {
                wifiConf = conf;
                setWifiProxySettings(wifiConf);
                try {
                    setIpAssignment("STATIC", wifiConf);
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    setIpAssignment("STATIC", wifiConf); // or "DHCP" for
                                                         // dynamic setting
                    setIpAddress(InetAddress.getByName("192.168.0.100"), 24,
                            wifiConf);
                    setGateway(InetAddress.getByName("4.4.4.4"), wifiConf);
                    setDNS(InetAddress.getByName("4.4.4.4"), wifiConf);
                    wifiManager.updateNetwork(wifiConf); // apply the setting
                    wifiManager.saveConfiguration(); // Save it
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }

    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static void setIpAddress(InetAddress addr, int prefixLength,
            WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException,
            ClassNotFoundException, InstantiationException,
            InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(new Class[] {
                InetAddress.class, int.class });
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties,
                "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setGateway(InetAddress gateway,
            WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException,
            InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass
                .getConstructor(new Class[] { InetAddress.class });
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties,
                "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(
                linkProperties, "mDnses");
        mDnses.clear(); // or add a new dns address , here I just want to
                        // replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    String TAG = "wifi";
    WifiManager wifiManager;

    public void connectToAP(String ssid, String passkey) {
        Log.i(TAG, "* connectToAP");
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        String networkSSID = ssid;
        String networkPass = passkey;

        Log.d(TAG, "# password " + networkPass);

        // for (ScanResult result : scanResultList) {
        // if (result.SSID.equals(networkSSID)) {
        if (true) {
            // String securityMode = getScanResultSecurity(result);
            String securityMode = "WEP";
            if (securityMode.equalsIgnoreCase("OPEN")) {

                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "# add Network returned " + res);

                boolean b = wifiManager.enableNetwork(res, true);
                Log.d(TAG, "# enableNetwork returned " + b);

                wifiManager.setWifiEnabled(true);

            } else if (securityMode.equalsIgnoreCase("WEP")) {

                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                wifiConfiguration.wepTxKeyIndex = 0;
                wifiConfiguration.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.WEP40);
                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "### 1 ### add Network returned " + res);

                boolean b = wifiManager.enableNetwork(res, true);
                Log.d(TAG, "# enableNetwork returned " + b);

                wifiManager.setWifiEnabled(true);
            }

            wifiConfiguration.SSID = "\"" + networkSSID + "\"";
            wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols
                    .set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols
                    .set(WifiConfiguration.Protocol.WPA);

            int res = wifiManager.addNetwork(wifiConfiguration);
            Log.d(TAG, "### 2 ### add Network returned " + res);

            wifiManager.enableNetwork(res, true);

            boolean changeHappen = wifiManager.saveConfiguration();

            if (res != -1 && changeHappen) {
                Log.d(TAG, "### Change happen");

                // AppStaticVar.connectedSsidName = networkSSID;

            } else {
                Log.d(TAG, "*** Change NOT happen");
            }

            wifiManager.setWifiEnabled(true);
        }
        // }
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    public static void setProxySettings(String assign,
            WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }

    WifiConfiguration GetCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List<WifiConfiguration> configurationList = manager
                .getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    void setWifiProxySettings(WifiConfiguration config) {
        // get the current wifi configuration
        // WifiManager manager = (WifiManager)
        // getSystemService(Context.WIFI_SERVICE);
        // WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            // get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            // get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class
                    .forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",
                    setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            // get ProxyProperties constructor
            Class[] proxyPropertiesCtorParamTypes = new Class[3];
            proxyPropertiesCtorParamTypes[0] = String.class;
            proxyPropertiesCtorParamTypes[1] = int.class;
            proxyPropertiesCtorParamTypes[2] = String.class;

            Constructor proxyPropertiesCtor = proxyPropertiesClass
                    .getConstructor(proxyPropertiesCtorParamTypes);

            // create the parameters for the constructor
            Object[] proxyPropertiesCtorParams = new Object[3];
            proxyPropertiesCtorParams[0] = "127.0.0.1";
            proxyPropertiesCtorParams[1] = 8118;
            proxyPropertiesCtorParams[2] = "example.com";

            // create a new object using the params
            Object proxySettings = proxyPropertiesCtor
                    .newInstance(proxyPropertiesCtorParams);

            // pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("STATIC", config);

            // // save the settings
            // manager.updateNetwork(config);
            // manager.disconnect();
            // manager.reconnect();
        } catch (Exception e) {
        }
    }

    void unsetWifiProxySettings() {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            // get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            // get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class
                    .forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy",
                    setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            // pass null as the proxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("NONE", config);

            // save the config
            manager.updateNetwork(config);
            manager.disconnect();
            manager.reconnect();
        } catch (Exception e) {
        }
    }

}
