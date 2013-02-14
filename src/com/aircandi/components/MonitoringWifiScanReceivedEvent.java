package com.aircandi.components;

import java.util.List;

import com.aircandi.components.ProxiManager.WifiScanResult;

@SuppressWarnings("ucd")
public class MonitoringWifiScanReceivedEvent {
	public final List<WifiScanResult>	wifiList;

	public MonitoringWifiScanReceivedEvent(List<WifiScanResult> wifiList) {
		this.wifiList = wifiList;
	}
}
