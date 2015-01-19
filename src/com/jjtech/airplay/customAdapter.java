package com.jjtech.airplay;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class customAdapter extends BaseAdapter {
	private ArrayList<File> m_list=new ArrayList<File>();
	private ArrayList<Bitmap> m_img=new ArrayList<Bitmap>();
	private ArrayList<String> m_detail = new ArrayList<String>();
	private Context mContext = null;
	
	private ImageView img = null;
	private TextView img_name = null;
	private TextView img_size = null;
	private CustomHolder holder = null;
	
	private AirplayService airplay;
	
	public customAdapter(Context mContext, AirplayService airplay){
		super();
		this.mContext = mContext; 
		this.airplay = airplay;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return m_list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return m_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list, null);
			convertView.setBackgroundColor(color.background_light);
			img = (ImageView) convertView.findViewById(R.id.list_img);
			img_name = (TextView) convertView.findViewById(R.id.list_img_name);
			img_size = (TextView) convertView.findViewById(R.id.list_img_size);
			holder = new CustomHolder(img, img_name, img_size);
			convertView.setTag(holder);
		}else{
			holder = (CustomHolder) convertView.getTag();
		}
		
		String fileName = m_list.get(position).getName();
		
		
		holder.img.setImageBitmap(m_img.get(position));		
		holder.img_name.setText(fileName.substring(0, fileName.lastIndexOf('.')));
		holder.img_size.setText("유형 : "+ fileName.substring(fileName.lastIndexOf('.')+1));
		holder.img_size.append(m_detail.get(position));
		
		convertView.setOnClickListener(new OnClickListener(){ 
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(airplay.isConnected())
					airplay.putImage(m_list.get(position));	
			}
		});
				
		return convertView;
	}
	
	private class CustomHolder{
		ImageView	img;
		TextView	img_name;
		TextView	img_size;
		public CustomHolder(ImageView img, TextView img_name, TextView img_size){
			this.img = img;
			this.img_name = img_name;
			this.img_size = img_size;
		}
		
	}

	public void add(final File file){				
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bm=BitmapFactory.decodeFile(file.getPath(), options);     
		Bitmap rbm = Bitmap.createScaledBitmap(bm, 100, 100, true ); 		
		
		int fileSize = (int)(file.length()/1024);
		int width = bm.getWidth();
		int height = bm.getHeight();
		String detail = ("\n크기 : "+width+" * "+height+" ("+fileSize +"KB)");
		
		m_detail.add(detail);
		m_img.add(rbm);
		m_list.add(file);
	}
}