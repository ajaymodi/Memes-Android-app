package com.funmeme.jobaka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class ImageExtractor {

	public static String FROM_REPLACEMENT = "FROM_NUMBER";
	public static String PHOTOS_URI =  FROM_REPLACEMENT;
	public static String IMAGES = "images";
	public static String IMAGE_POSITION = "image_position";
	public static String PREVIEW_IMAGES = "preview_images";
	public static String ORIGINAL_IMAGES = "original_images";
	public static String DOWNLOADS = "downloads";
	public static String FAV = "fav";
	public static String ID= "id";
	private static final String TAG_SUCCESS = "success";
	JSONArray wallpapers = null;
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ImageExtractor(String url) {
		setUrl(url);
	}

	public int getCount()
	{
		int count = 10000;

		return count;
	}


	public ArrayList<HashMap<String, String>> getImages() throws IOException {

		String previewJPGURL = null;
		String viewJPGURL = null;

		String response = null;
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

		try{
			response = CustomHttpClient.executeHttpGet(getUrl());

			String result = response.toString();

			JSONObject json = new JSONObject(result);

			try {

				// Checking for sucess
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// Getting Array of Wallpapers
					wallpapers = json.getJSONArray("wallpapers");

					// looping through All wallpapers
					for(int i = 0; i < wallpapers.length(); i++){
						JSONObject json_data = wallpapers.getJSONObject(i);
						String id =  String.valueOf(json_data.getInt("id"));
						String download = String.valueOf(json_data.getInt("downloads"));
						String fav = String.valueOf(json_data.getInt("fav"));

						viewJPGURL = json_data.getString("orig_url");

						previewJPGURL = json_data.getString("orig_url");

						// creating new HashMap
						HashMap<String, String> jpgs = new HashMap<String, String>();

						// adding each child node to HashMap key =>
						// value
						jpgs.put(PREVIEW_IMAGES, previewJPGURL);
						jpgs.put(ORIGINAL_IMAGES, viewJPGURL);
						jpgs.put("id", id);
						jpgs.put("downloads", download);
						jpgs.put("fav", fav);

						// adding HashList to ArrayList


						data.add(jpgs);
						Log.i("hash",
								"images," + data);

					}
				} else {
					data=null;

				}

			}
			catch(JSONException e){
				Log.e("log_tag", "Error parsing data "+e.toString());
			}
		}
		catch (Exception e) {
			Log.e("log_tag","Error in http connection!!" + e.toString());
		}
		return data;
	}

}