package com.jjtech.airplay;

import java.io.File;
import java.io.FilenameFilter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {
	private AirplayService airplay; 
	private InetAddress deviceAddress;
	private customAdapter m_adapter;
	private ListView m_list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().setTitle("Air Play Serivce");
		getActionBar().setSubtitle("Not connected");
		getActionBar().setHomeButtonEnabled(true);
		
		airplay = new AirplayService();
		
		m_adapter = new customAdapter(this, airplay);
		m_list = (ListView) findViewById(R.id.listview);
		m_list.setAdapter(m_adapter);
		getImageFile("/sdcard/Pictures/");		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
			case android.R.id.home:
				ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if(wifi.isConnected()){
					SearchDialog("장치를 검색 중입니다.", (long)2);
				}
				else
					AlertDialog("WIFI가 연결되어 있지 않습니다.", "apple TV가 연결되어 있는 네트워크에 접속해주시길 바랍니다.");
				return true;
			case R.id.option_stop:
				airplay.stopImage(); break;
			case R.id.option_trans:
				TransitionDialog(); break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getImageFile(String path){		
		FilenameFilter fileFilter = new FilenameFilter(){
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				if(filename.endsWith(".bmp")||filename.endsWith(".jpg")||
				   filename.endsWith(".gif")||filename.endsWith(".png")||
				   filename.endsWith(".jpeg"))
					return true;
				else
					return false;
			}			
		};
		File file = new File(path);
		File[] files = file.listFiles(fileFilter);	

		if(files != null){
			for(int i=0; i<files.length; i++){
				m_adapter.add(files[i]);
			}
		}
	}
	
	private void SearchDialog(String message, long time){
		deviceAddress = getWifiInetAddress();
		airplay.connect(deviceAddress);
		final ProgressDialog mProgressDialog = ProgressDialog.show(this,"",message,true);
		Handler mHandler = new Handler();		
		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mProgressDialog.dismiss();
				DeviceDialog();
			}
        }, 1000*time);
	}
	
	private void DeviceDialog(){
		final ArrayList<ServiceInfo> list = airplay.getServices();
		int size = list.size();
		String[] items = new String[size];
		for(int i=0; i<size; i++){
			items[i] = list.get(i).getName();
		}
		
		AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);		
		builder.setTitle("Choose device");	
		builder.setNegativeButton("재시도", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	SearchDialog("장치를 검색 중입니다.",2);
		    }
		});	
		builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    }
		});		
		if(size > 0){
			builder.setItems(items, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int index){
					selectService(list.get(index));
				}
			});			
		}else{
			builder.setMessage("장치가 존재하지 않습니다. \n네트워크를 확인해주세요.");
		}
		
		AlertDialog dialog = builder.create();
		dialog.show();   
	}
	
	private void AlertDialog(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("확인",
		new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
	
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}	
	
	private void TransitionDialog(){
		final String[] items = {"Dissolve", "SlideLeft", "SlideRight"};
		AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);		
		builder.setTitle("Transition");	
		builder.setItems(items, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int index){
				airplay.setTransition(items[index]);			
			}
		});		
		builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void selectService(ServiceInfo service){
		airplay.setCurrentService(service);
		getActionBar().setSubtitle(service.getName());		
	}
	
	private InetAddress getWifiInetAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                	return (inetAddress);
	                }
	            }
	        }
	    } catch (Exception e) {
	        return (null);
	    }
	    return (null);
	}
}

