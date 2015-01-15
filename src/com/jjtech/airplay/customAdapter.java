package com.jjtech.airplay;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class customAdapter extends BaseAdapter {
	private ArrayList<File> m_list=new ArrayList<File>();
	private ArrayList<Bitmap> m_img=new ArrayList<Bitmap>();
	private Context mContext = null;
	
	public customAdapter(Context mContext){
		super();
		this.mContext = mContext; 
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ImageView		img	= null;
		TextView		img_name = null;
		TextView		img_size = null;
		CustomHolder	holder = null;
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list, null);
			img = (ImageView) convertView.findViewById(R.id.list_img);
			img_name = (TextView) convertView.findViewById(R.id.list_img_name);
			img_size = (TextView) convertView.findViewById(R.id.list_img_size);
			holder = new CustomHolder(img, img_name, img_size);
			convertView.setTag(holder);
		}else{
			holder = (CustomHolder) convertView.getTag();
			img = holder.img;
			img_name = holder.img_name;
			img_size = holder.img_size;
		}
		
		
		img.setImageBitmap(m_img.get(position));
		img_name.setText(m_list.get(position).getName());
		img_size.setText("size: "+m_list.get(position).length());
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
	
	public void add(File file){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		Bitmap bm=BitmapFactory.decodeFile(file.getPath(), options);     
		Bitmap rbm = Bitmap.createScaledBitmap(bm, 100, 100, true ); 
		m_img.add(rbm);
		m_list.add(file);
	}	
}