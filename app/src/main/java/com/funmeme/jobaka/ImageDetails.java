package com.funmeme.jobaka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


public class ImageDetails extends Activity {

	private ViewPager pager;

	private DisplayImageOptions options;
	 protected ImageLoader imageLoader = ImageLoader.getInstance();

	    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);
		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);

		//getting serialized data from MainActivity
		Bundle bundle = getIntent().getExtras();
		final List<String> imageUrls = bundle.getStringArrayList(ImageExtractor.IMAGES);
		final List<String> downloads = bundle.getStringArrayList(ImageExtractor.DOWNLOADS);
		final List<String> favs = bundle.getStringArrayList(ImageExtractor.FAV);
		final List<String> ids = bundle.getStringArrayList(ImageExtractor.ID);
		
		int pagerPosition = bundle.getInt(ImageExtractor.IMAGE_POSITION, 0);

		//Setting DisplayImageOptions for UIL
	      options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.stub_image) //setting image to show on loading
				.showImageForEmptyUri(R.drawable.image_for_empty_url)
				.showImageOnFail(R.drawable.image_for_empty_url)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build(); 

	    //initializing viewpager  
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(imageUrls,downloads,favs, ids));
		pager.setCurrentItem(pagerPosition);

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private class SendfeedbackJob extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {

				//------------------>>
				HttpGet httppost = new HttpGet(params[0]);
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httppost);

				// StatusLine stat = response.getStatusLine();
				int status = response.getStatusLine().getStatusCode();

				if (status == 200) {
					HttpEntity entity = response.getEntity();
					String data = EntityUtils.toString(entity);

				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

	}

	//Adapter for Pager
	private class ImagePagerAdapter extends PagerAdapter {
		
		private List<String> images;
		private LayoutInflater inflater;
		private List<String> downloads;
		private List<String> favs;
		private List<String> ids;

		ImagePagerAdapter(List<String> images,List<String> d,List<String> f,List<String> i) {
			this.images = images;
			this.downloads=d;
			this.favs=f;
			this.ids=i;
			inflater = LayoutInflater.from(ImageDetails.this);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		private void scanFile(String path) {

			MediaScannerConnection.scanFile(getApplicationContext(),
					new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {

						public void onScanCompleted(String path, Uri uri) {
							Log.i("TAG", "Finished scanning " + path);
						}
					});
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			final View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);

		final TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			 final TextView down = (TextView) imageLayout.findViewById(R.id.downloads);
			 final TextView fav = (TextView) imageLayout.findViewById(R.id.Fav);

			final ImageView imgDown = (ImageView) imageLayout.findViewById(R.id.imageViewdown);
			imgDown.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					imgDown.setImageResource(R.drawable.ic_action_download_green);
					String s = String.valueOf(Integer.parseInt(downloads.get(position)) + 1);
					down.setText(Integer.parseInt(downloads.get(position)) + 1 + " share");
					downloads.set(position, s);
					String https_url = "http://jobaka-123.herokuapp.com/update_down/" + ids.get(position);
					new SendfeedbackJob().execute(https_url);

					BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
					Bitmap bitmap = drawable.getBitmap();

					File sdCardDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "MemesImages");
					sdCardDirectory.mkdirs();

					File image = new File(sdCardDirectory, System.currentTimeMillis() + ".jpg");
					boolean success = false;

					// Encode the file as a PNG image.
					FileOutputStream outStream;
					try {

						outStream = new FileOutputStream(image);
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        /* 100 to keep full quality of the image */

						outStream.flush();
						outStream.close();
						success = true;

						scanFile(image.getAbsolutePath());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					if (success) {
						Toast.makeText(getApplicationContext(), "Image saved with success",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(),
								"Error during image saving", Toast.LENGTH_LONG).show();
					}
				}
			});



			final ImageView imgFav = (ImageView) imageLayout.findViewById(R.id.imageViewfav);
			imgFav.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					imgFav.setImageResource(R.drawable.ic_action_favorite_red);
					String s  = String.valueOf(Integer.parseInt(favs.get(position)) + 1);
					fav.setText(Integer.parseInt(favs.get(position)) + 1 + " liked");
					String https_url = "http://jobaka-123.herokuapp.com/update_fav/"+ids.get(position);
					favs.set(position,s);
					new SendfeedbackJob().execute(https_url);
				}
			});


			final ImageView imgShare = (ImageView) imageLayout.findViewById(R.id.imageViewshare);
			imgShare.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String https_url = "http://jobaka-123.herokuapp.com/update_down/" + ids.get(position);
					FileOutputStream outStream;
					try {
						BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

						FileOutputStream out = getApplicationContext().openFileOutput("pic", Context.MODE_WORLD_READABLE);
						Bitmap bm = ((BitmapDrawable) drawable ).getBitmap();
						bm.compress(Bitmap.CompressFormat.JPEG,100, out);
						out.flush();
						out.close();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}


					File f = getApplicationContext().getFileStreamPath("pic");
					Uri u = Uri.fromFile(f);

					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("image/jpg");
					i.setPackage("com.whatsapp");
					i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					i.putExtra(Intent.EXTRA_STREAM, u);

					try {
						startActivity(Intent.createChooser(i, "Share with friends"));
					} catch (android.content.ActivityNotFoundException ex) {
						Toast.makeText(getApplicationContext(), "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
					}
				}
			});


			down.setText(downloads.get(position) + " share");
			fav.setText(favs.get(position) + " liked");
			
			//displaying image using UIL
			imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				spinner.setVisibility(View.VISIBLE);
				}
				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
				case IO_ERROR:
				message = "Input/Output error";
				break;
				case DECODING_ERROR:
				message = "Image can't be decoded";
				break;
				case NETWORK_DENIED:
				message = "Downloads are denied";
				break;
				case OUT_OF_MEMORY:
				message = "Out Of Memory error";
				break;
				case UNKNOWN:
				message = "Unknown error";
				break;
				}
				Toast.makeText(ImageDetails.this, message, Toast.LENGTH_SHORT).show();
				spinner.setVisibility(View.GONE);
				}
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				
					spinner.setVisibility(View.GONE);
				
				}
				});
			view.addView(imageLayout, 0);
				return imageLayout;
				
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

	}
}