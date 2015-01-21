package com.jjtech.airplay;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
		holder.img_name.setText(fileName.substring(0, fileName.lastIndexOf('.')));
		holder.img_size.setText("유형 : "+ fileName.substring(fileName.lastIndexOf('.')+1));
		holder.img_size.append(m_detail.get(position));
		holder.img.setImageResource(R.drawable.icon512);
		
		new ImageResizeTask(holder.img, position).execute(m_list.get(position));
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
		
		m_img.add(null);
		m_detail.add(detail);		
		m_list.add(file);
	}
	
	public class ImageResizeTask extends AsyncTask<File, Void, Bitmap>{
		private File file;
		private int position;
		private final WeakReference<ImageView> imageViewReference;
		
		public ImageResizeTask(ImageView imageView, int position){
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.position = position;
		}
		
		@Override
		protected Bitmap doInBackground(File... params) {
			// TODO Auto-generated method stub
			if(m_img.get(position) == null){
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = getSampleSize(params[0]);
				Bitmap bm=BitmapFactory.decodeFile(params[0].getAbsolutePath(), options);
				m_img.set(position, bm);
				return bm;
			}else{
				return m_img.get(position);
			}			
		}
		
		@Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (imageViewReference != null) {
	        	ImageView imageView = imageViewReference.get();
	        	if (imageView != null) {
	        		imageView.setImageBitmap(bitmap);
	        	}
	        }
	    }				
	}
	
	public int getSampleSize(File file){
		int filesize = (int)(file.length()/1024);
		if(filesize > 3200) return 16;
		else if(filesize > 1600) return 8;
		else if(filesize > 800) return 4;
		else return 2;
	}
}