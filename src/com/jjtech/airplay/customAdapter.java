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
		
		if(m_img.get(position) != null)
			holder.img.setImageBitmap(m_img.get(position));		
		holder.img_name.setText(fileName.substring(0, fileName.lastIndexOf('.')));
		holder.img_size.setText("유형 : "+ fileName.substring(fileName.lastIndexOf('.')+1));
		holder.img_size.append(m_detail.get(position));
		
		convertView.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					v.setBackgroundColor(color.holo_blue_light);
				return false;
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

	public void refresh(){
		m_list.clear();
		m_img.clear();
		m_detail.clear();
		notifyDataSetChanged();
	}
	
	public void imageSelect(int position){
		if(airplay.isConnected())
			airplay.putImage(m_list.get(position));
	}
	
	public void add(final File file){	
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		int fileSize = (int)(file.length()/1024);
		String detail = ("\n크기 : "+ options.outWidth+" * "+  options.outHeight+" ("+fileSize +"KB)");
		
		BitmapFactory.Options roptions = new BitmapFactory.Options();
		roptions.inSampleSize = calculateInSampleSize(options, 200, 200);
		bm=BitmapFactory.decodeFile(file.getAbsolutePath(), roptions);  
		
		m_img.add(bm);		
		m_detail.add(detail);		
		m_list.add(file);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	    return inSampleSize;
	}
}