package com.adsnative.android.sdk.story;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.adsnative.android.sdk.Constants;
import com.adsnative.android.sdk.device.DeviceInfo;
import com.adsnative.android.sdk.device.GetAdvertisingId;
import com.adsnative.android.sdk.request.AdRequest;
import com.adsnative.android.sdk.request.GetSponsoredStoryRequest;
import com.adsnative.android.sdk.request.GetSponsoredStoryResponse;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class handles fetching sponsored story and stores all data of sponsored story, IDs and device info
 */
public class SponsoredStory {

    private String uuid;
    private SponsoredStoryData sponsoredStoryData;
    private AdRequest adRequest;
    private Context context;
    private DeviceInfo deviceInfo;
    private OnSponsoredStoryListener onSponsoredStoryListener;

    /**
     * Constructor
     *
     * @param adRequest with AdUnitId
     * @param context
     */
    public SponsoredStory(AdRequest adRequest, Context context) {
        this.adRequest = adRequest;
        this.context = context;
        this.deviceInfo = new DeviceInfo(context);
    }

    /**
     * Fetches AdvertisingId from device and after that starts task that fetches sponsored story
     */
    public void loadRequest() {
        if (Build.BRAND.equalsIgnoreCase("generic")) {
            //Allows lib to be run on emulator
            setUuid("");
            new GetSponsoredStoryTask(adRequest, deviceInfo).execute();
        } else {
            new GetAdvertisingId(context) {
                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s != null) {
                        setUuid(s);
                        new GetSponsoredStoryTask(adRequest, deviceInfo).execute();
                    } else {
                        setUuid("");
                    }
                }
            }.execute();
        }
    }

    /**
     * Uuid setter
     *
     * @param uuid
     */
    private void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Sponsored story data setter
     *
     * @param sponsoredStoryData
     */
    private void setSponsoredStoryData(SponsoredStoryData sponsoredStoryData) {
        this.sponsoredStoryData = sponsoredStoryData;
    }

    /**
     * Sponsored story data getter
     *
     * @return this sponsoredStoryData object
     */
    public SponsoredStoryData getSponsoredStoryData() {
        return this.sponsoredStoryData;
    }

    /**
     * Sets SponsoredStoryListener to know when the story is completely fetched and parsed
     *
     * @param onSponsoredStoryListener
     */
    public void setOnSponsoredStoryListener(OnSponsoredStoryListener onSponsoredStoryListener) {
        this.onSponsoredStoryListener = onSponsoredStoryListener;
    }

    /**
     * Fetching SponsoredStory task.
     * Triggers onSponsoredStoryData from {@link com.adsnative.android.sdk.story.OnSponsoredStoryListener}
     * if SponsoredStory is completely fetched and data is correctly parsed. If SponsoredStoryData is null
     * nth is going to be triggered.
     */
    private class GetSponsoredStoryTask extends AsyncTask<String, Void, SponsoredStoryData> {

        private AdRequest adRequest;
        private DeviceInfo deviceInfo;

        public GetSponsoredStoryTask(AdRequest adRequest, DeviceInfo deviceInfo) {
            this.adRequest = adRequest;
            this.deviceInfo = deviceInfo;
        }

        @Override
        protected SponsoredStoryData doInBackground(String... params) {
            GetSponsoredStoryRequest getSponsoredStoryRequest =
                    new GetSponsoredStoryRequest(adRequest, uuid, deviceInfo);
            String json = getSponsoredStoryRequest.get();
            if (json != null) {
                SponsoredStoryData sponsoredStoryData = null;
                try {
                    sponsoredStoryData = new GetSponsoredStoryResponse(json).parseJson();
                    sponsoredStoryData.setThumbnailBitmap(BitmapFactory.decodeStream(new URL(sponsoredStoryData.getThumbnailUrl()).openConnection().getInputStream()));
                } catch (JSONException e) {
                    Log.e(Constants.ERROR_TAG, e.getMessage());
                    return null;
                } catch (MalformedURLException e) {
                    Log.e(Constants.ERROR_TAG, e.getMessage());
                    return null;
                } catch (IOException e) {
                    Log.e(Constants.ERROR_TAG, e.getMessage());
                    return null;
                }
                return sponsoredStoryData;
            }
            return null;

        }

        @Override
        protected void onPostExecute(SponsoredStoryData sponsoredStoryData) {
            super.onPostExecute(sponsoredStoryData);
            if (sponsoredStoryData != null) {
                setSponsoredStoryData(sponsoredStoryData);
                onSponsoredStoryListener.onSponsoredStoryData(sponsoredStoryData);
            }
        }
    }
}

