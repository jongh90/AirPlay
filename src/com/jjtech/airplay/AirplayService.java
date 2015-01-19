package com.jjtech.airplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.util.Log;

public class AirplayService implements ServiceListener {
	private static final String SERVICE_TYPE = "_airplay._tcp.local.";
	private JmDNS jmdns;
	private ExecutorService es;
	private Map<String, ServiceInfo> servicesMap;
	private ServiceInfo CurrentService;	
	private String transition;
	
	public AirplayService(){
		es = Executors.newSingleThreadExecutor();
		servicesMap = new HashMap<String, ServiceInfo>();
		CurrentService = null;
		transition = "Dissolve";
	}
	
	/* apple TV 기기들을 인식하고 그 목록을 Map<String, ServiceInfo>services에 저장한다. */
	public void connect(InetAddress deviceAddress){
		es.submit(new connectTask(deviceAddress));
	}
	
	public void refresh(InetAddress deviceAddress){
		servicesMap.clear();
		es.submit(new connectTask(deviceAddress));
	}
	
	public boolean isConnected(){
		if(CurrentService != null) 
			return true;
		else	
			return false;
	}
	
	/* JmDNS를 종료한다. 반드시 호출하지 않아도 된다. */
	public void stop() {
		es.submit(new stopTask());
	}
	
	/* 3. Photos */
	/* 3.1. HTTP requests */
	/* PUT photo */
	public void putImage(File file, String transition){
		es.submit(new PutImageTask(CurrentService, file, transition));
	}
	
	public void putImage(File file){
		es.submit(new PutImageTask(CurrentService, file, this.transition));
	}
	
	/* POST stop */
	public void stopImage(){
		es.submit(new stopImageTask(CurrentService));
	}
	
	/* Cache photo (PUT) */
	public void putCacheImage(String key, File file){
		es.submit(new putCacheImageTask(CurrentService, key, file));
	}
	
	/* Cache photo (SHOW) */
	public void showCacheImage(String key, String transition){
		es.submit(new showCacheImageTask(CurrentService, key, transition));
	}
	
	public void setTransition(String transition){
		this.transition = transition;
	}
	
	public ArrayList<ServiceInfo> getServices(){
		ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
		Set<String> key = servicesMap.keySet();
		for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
             String keyName = (String) iterator.next();
             ServiceInfo service = servicesMap.get(keyName);
             services.add(service);
		}
		return services;
	}
	
	public void setCurrentService(ServiceInfo CurrentService){
		this.CurrentService = CurrentService;
	}
	
	public ServiceInfo getCurrentService(){
		return this.CurrentService;
	}
	
	private class connectTask implements Runnable{
		
		private InetAddress deviceAddress;
		public connectTask(InetAddress deviceAddress) {
			this.deviceAddress = deviceAddress;
		}
		
		@Override
		public void run() {
        	try {
				jmdns = JmDNS.create(deviceAddress);
				jmdns.addServiceListener(SERVICE_TYPE, AirplayService.this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
		}		
	}
	
	private class stopTask implements Runnable{				
		@Override
		public void run() {
			if (jmdns != null) {
				try {
				    jmdns.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}           
		}		
	}
		
	private class PutImageTask implements Runnable {

		private File file;		
		private ServiceInfo serviceInfo;
		private String transition;
		
		public PutImageTask(ServiceInfo serviceInfo, File file, String transition) {
			this.file = file;
			this.serviceInfo = serviceInfo;
			this.transition = transition;
		}
		
		@Override
		public void run() {
			try {
				URL url = new URL(serviceInfo.getURL() + "/photo");
				Log.d("url", ""+serviceInfo.getURL());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setConnectTimeout(15 * 1000);
				conn.setReadTimeout(15 * 1000);
				conn.setRequestMethod("PUT");
				conn.setRequestProperty("X-Apple-AssetKey", UUID.randomUUID().toString());
				conn.setRequestProperty("X-Apple-Transition", transition);
				conn.setRequestProperty("Content-Length", "" + file.length());
				conn.setRequestProperty("User-Agent", "MediaControl/1.0");
				conn.setRequestProperty("X-Apple-Session-ID", UUID.randomUUID().toString());				
				
				BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				byte[] buffer = new byte[32 * 1024];
				int i;
				while ((i = in.read(buffer)) != -1) {
					out.write(buffer, 0, i);
				}
				in.close();
				out.close();
				
				int status = conn.getResponseCode();
				if (status == 200) {
					Log.d("Status", "200");
				} else {
					Log.d("Status", ""+status);
				}
				
			} catch (Exception e) {
				Log.d("ERROR", "exception");
			}
		}		
	}
	
	private class stopImageTask implements Runnable {

		private ServiceInfo serviceInfo;
		
		public stopImageTask(ServiceInfo serviceInfo) {
			this.serviceInfo = serviceInfo;
		}
		
		@Override
		public void run() {
			try {
				URL url = new URL(serviceInfo.getURL() + "/stop");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15 * 1000);
				conn.setReadTimeout(15 * 1000);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Length", "0");
				conn.setRequestProperty("User-Agent", "MediaControl/1.0");
				conn.setRequestProperty("X-Apple-Session-ID", UUID.randomUUID().toString());
				int status = conn.getResponseCode();
				if (status == 200) {
					Log.d("Status", "200");
				} else {
					Log.d("Status", ""+status);
				}
			} catch (Exception e) {
				Log.d("ERROR", "exception");
			}
		}		
	}	
	
	private class putCacheImageTask implements Runnable {
		private String key;
		private File file;		
		private ServiceInfo serviceInfo;
		
		public putCacheImageTask(ServiceInfo serviceInfo, String key, File file) {
			this.key = key;
			this.file = file;
			this.serviceInfo = serviceInfo;
		}
		
		@Override
		public void run() {
			try {
				URL url = new URL(serviceInfo.getURL() + "/photo");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setConnectTimeout(15 * 1000);
				conn.setReadTimeout(15 * 1000);
				conn.setRequestMethod("PUT");
				conn.setRequestProperty("X-Apple-AssetAction", "cacheOnly");
				conn.setRequestProperty("X-Apple-AssetKey", key);
				conn.setRequestProperty("Content-Length", "" + file.length());
				conn.setRequestProperty("User-Agent", "MediaControl/1.0");
				conn.setRequestProperty("X-Apple-Session-ID", UUID.randomUUID().toString());				
				BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				byte[] buffer = new byte[32 * 1024];
				int i;
				while ((i = in.read(buffer)) != -1) {
					out.write(buffer, 0, i);
				}
				in.close();
				out.close();
				int status = conn.getResponseCode();
				if (status == 200) {
					Log.d("Status", "200");
				} else {
					Log.d("Status", ""+status);
				}
			} catch (Exception e) {
				Log.d("ERROR", "exception");
			}
		}		
	}
	
	private class showCacheImageTask implements Runnable {

		private ServiceInfo serviceInfo;
		private String key;
		private String transition;
		
		public showCacheImageTask(ServiceInfo serviceInfo, String key, String transition) {
			this.serviceInfo = serviceInfo;
			this.key= key;
			this.transition = transition;
		}
		
		@Override
		public void run() {
			try {
				URL url = new URL(serviceInfo.getURL() + "/photo");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(15 * 1000);
				conn.setReadTimeout(15 * 1000);
				conn.setRequestMethod("PUT");
				conn.setRequestProperty("X-Apple-AssetAction", "displayCached");
				conn.setRequestProperty("X-Apple-AssetKey", key);
				conn.setRequestProperty("X-Apple-Transition", transition);
				conn.setRequestProperty("Content-Length", "0");
				conn.setRequestProperty("User-Agent", "MediaControl/1.0");
				conn.setRequestProperty("X-Apple-Session-ID", UUID.randomUUID().toString());
				int status = conn.getResponseCode();
				if (status == 200) {
					Log.d("Status", "200");
				} else {
					Log.d("Status", ""+status);
				}
			} catch (Exception e) {
				Log.d("ERROR", "exception");
			}
		}
		
	}
	
	/* 복수의 기기가 동시에 인식될시 resolved가 drop되는 문제가 생겨 handler가 아닌 멀티스레드로 구현함 */
	private class serviceTask implements Runnable {
		private String type;
		private String name;
		private int time;
		
		serviceTask(String type, String name, int time){
			this.type = type;
			this.name = name;
			this.time = time;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			jmdns.requestServiceInfo(type, name, time);
		}
	}
	
	
		
	@Override
	public void serviceAdded(final ServiceEvent event) {
		// TODO Auto-generated method stub
		Log.d("ServiceAdded", "service : "+event.getName());
		es.submit(new serviceTask(event.getType(), event.getName(), 1000));
	}
	
	@Override
	public void serviceResolved(ServiceEvent event) {
		// TODO Auto-generated method stub
		servicesMap.put(event.getInfo().getKey(), event.getInfo());
		Log.d("ServiceResolved", "service : "+event.getName());
	}

	@Override
	public void serviceRemoved(ServiceEvent event) {
		// TODO Auto-generated method stub
		Log.d("jmDNS", "Removed AirPlay service : "+event.getName());
	}		
}

