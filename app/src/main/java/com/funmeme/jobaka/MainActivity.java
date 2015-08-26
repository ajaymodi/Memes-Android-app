package com.funmeme.jobaka;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ActionBarActivity {

	private List<String> imageUrls;
	private List<String> previewImageUrls;
	private List<String> downloads;
	private List<String> fav;
	private List<String> ids;
	private TextView noConnectTextView;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	private GridView gridView;
	private TextView results;
	private DownloadPreviewImagesTask downloadPreviewImagesTask;
	private int page = 2;
	int count=0;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private String gallery_url="http://jobaka-123.herokuapp.com/page/";
	ConnectionDetector cd;
	Boolean isInternetPresent = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		noConnectTextView = (TextView) findViewById(R.id.no_connect_message);

		cd = new ConnectionDetector(this);

		results = (TextView) findViewById(R.id.tresults);

		isInternetPresent = cd.isConnectingToInternet();

		// check for Internet status
		if (isInternetPresent) {
			imageUrls = new ArrayList<String>();
			previewImageUrls = new ArrayList<String>();
			downloads = new ArrayList<String>();
			fav = new ArrayList<String>();
			ids = new ArrayList<String>();

			//initializing gridview
			initGridView();

			//executing async task for getting data
			downloadPreviewImagesTask = getDownloadPreviewImagesTask(R.id.preview_img_loading, "http://jobaka-123.herokuapp.com/page/1");
			if (downloadPreviewImagesTask != null && downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
				downloadPreviewImagesTask.attachActivity(this);
				downloadPreviewImagesTask.setSpinnerVisible();

			} else {
				Log.d("downloadPreviewImagesTask", "downloadPreviewImagesTask has executed already");
			}
		}
		else
		{

			showAlertDialog(MainActivity.this, "No Internet Connection",
					"You don't have internet connection!", false);
		}

	}

	@SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message, Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}



	public String geturl(){

		//You can set various URLs for different types of query here!
		return gallery_url;
	}

	private DownloadPreviewImagesTask getDownloadPreviewImagesTask(int spinnerID, String url) {
		downloadPreviewImagesTask = new DownloadPreviewImagesTask();
		downloadPreviewImagesTask.setSpinner(spinnerID);
		downloadPreviewImagesTask.attachActivity(this);
		downloadPreviewImagesTask.execute(url); // show spinner
		return downloadPreviewImagesTask;
	}


	class EndlessListListener implements OnScrollListener {


		private int visibleThreshold = 5;  //setting visible threshold for loading new data
		private int previousTotal = 0;
		private boolean loading = true;

		public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {

			if (loading) {
				if (totalCount > previousTotal) {
					loading = false;
					previousTotal = totalCount;
				}
			}

			//Log.i("check", String.valueOf(totalCount) + " " + String.valueOf(visibleCount) + " " + String.valueOf(firstVisible));
			if (!loading && (totalCount - visibleCount) <= (firstVisible + visibleThreshold)) {

				Log.d("loadData", "new data loaded");
				String photosUri = ImageExtractor.PHOTOS_URI;

				String url = geturl() + photosUri.replace(ImageExtractor.FROM_REPLACEMENT, String.valueOf(page));;


				if (downloadPreviewImagesTask != null && downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
					Log.d("downloadPreviewImagesTask", "downloadPreviewImagesTask is running");
				}
				else  if(page<(count/21)) //checking if there is more data
				{
					downloadPreviewImagesTask = getDownloadPreviewImagesTask(R.id.new_photos_loading, url);
					Log.i("url", url);
					page = page+1;
				}
				loading = true;
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

	}

	//Async Task for downloading new data
	class DownloadPreviewImagesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

		private Exception exc = null;
		private ProgressBar spinner;
		private MainActivity imageGridActivity = null;
		private int spinnerId;

		public AsyncTask<String, Void, ArrayList<HashMap<String, String>>> setSpinner(int spinnerId) {
			this.spinnerId = spinnerId;
			return this;
		}

		public void attachActivity(MainActivity imageGridActivity) {
			this.imageGridActivity = imageGridActivity;
		}

		public void detachActivity() {
			this.imageGridActivity = null;
		}

		public ProgressBar getSpinner() {
			return spinner;
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... params) {

			String url = "";
			if( params.length > 0 ){
				url = params[0];
			}

			ImageExtractor imageExtractor = new ImageExtractor(url);
			ArrayList<HashMap<String, String>> images = null;
			try {
				images = imageExtractor.getImages();
				count = imageExtractor.getCount();

			} catch (IOException e) {
				exc = e;
				Log.e("catched_error", e.getMessage(), e);
			}
			return images;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSpinnerVisible();
		}

		public void setSpinnerVisible() {
			spinner = (ProgressBar) imageGridActivity.findViewById(spinnerId);
			spinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> images) {
			super.onPostExecute(images);
			spinner.setVisibility(View.GONE);
			if (exc != null) {
				imageGridActivity.showError(exc);
			} else if (images !=null) {
				imageGridActivity.initOriginalAndPreviewUrls(images);

				if (gridView == null && imageAdapter == null) {
					initGridView();
				} else
					imageGridActivity.getImageAdapter().notifyDataSetChanged();
			}
			else
			{
				results.setText("Sorry! No Results Found!");
			}
		}
	}

	public ImageAdapter getImageAdapter() {
		return imageAdapter;
	}

	private void showError(Exception exc) {
		noConnectTextView.setText(exc.getMessage());
		noConnectTextView.setVisibility(View.VISIBLE);
	}

	private void initOriginalAndPreviewUrls(ArrayList<HashMap<String, String>> images) {

		for(int i=0;i<=images.size()-1;i++){

			String vurls=  images.get(i).get(ImageExtractor.ORIGINAL_IMAGES).toString();
			String purls=  images.get(i).get(ImageExtractor.PREVIEW_IMAGES).toString();
			String download=  images.get(i).get("downloads").toString();
			String ifav=  images.get(i).get("fav").toString();
			String ide=  images.get(i).get("id").toString();
			imageUrls.add(vurls);
			previewImageUrls.add(purls);
			downloads.add(download);
			fav.add(ifav);
			ids.add(ide);
		}

	}


	private void startImageGalleryActivity(int position) {
		Intent intent = new Intent(this, ImageDetails.class);
		intent.putExtra(ImageExtractor.IMAGES, (Serializable)imageUrls);
		intent.putExtra(ImageExtractor.PREVIEW_IMAGES, (Serializable)previewImageUrls);
		intent.putExtra(ImageExtractor.DOWNLOADS,(Serializable) downloads);
		intent.putExtra(ImageExtractor.FAV, (Serializable) fav);
		intent.putExtra(ImageExtractor.IMAGE_POSITION, position);
		intent.putExtra(ImageExtractor.ID, (Serializable) ids);


		startActivity(intent);
	}

	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridview);
		imageAdapter = new ImageAdapter();
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startImageGalleryActivity(position);
			}
		});
		gridView.setOnScrollListener(new EndlessListListener());
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.stub_image)
				.showImageForEmptyUri(R.drawable.image_for_empty_url)
				.showImageOnFail(R.drawable.image_for_empty_url)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public class ImageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}

			imageLoader.displayImage(previewImageUrls.get(position), imageView, options);

			return imageView;
		}
	}

}