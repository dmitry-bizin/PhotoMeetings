package com.photomeetings.services;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photomeetings.R;
import com.photomeetings.adapters.GridViewAdapter;
import com.photomeetings.adapters.PhotoPagerAdapter;
import com.photomeetings.model.Point;
import com.photomeetings.model.vk.VKPhoto;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchPhotosService implements Serializable {

    private static final int COUNT = 100;// 100-1000
    private int offset;
    private Point fullAddress;
    private String radius;
    private transient ProgressBar progressBarGridView;
    private GridViewAdapter gridViewAdapter;
    private TextView nothingFoundTextView;

    public SearchPhotosService(Point fullAddress, String radius, GridViewAdapter gridViewAdapter) {
        this.fullAddress = fullAddress;
        this.offset = 0;
        this.radius = radius;
        this.gridViewAdapter = gridViewAdapter;
    }

    public void vkPhotos(@Nullable final PhotoPagerAdapter photoPagerAdapter,
                         @Nullable final SwipeRefreshLayout swipeRefreshLayout,
                         final boolean isClearAdapter) {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        if (vkAccessToken != null && !vkAccessToken.isExpired()) {
            VKRequest request = new VKRequest("photos.search",
                    VKParameters.from(
                            VKApiConst.LAT, fullAddress.getLat(),
                            VKApiConst.LONG, fullAddress.getLng(),
                            VKApiConst.COUNT, COUNT,
                            VKApiConst.OFFSET, offset,
                            "radius", radius,
                            VKApiConst.ACCESS_TOKEN, vkAccessToken.accessToken,
                            VKApiConst.VERSION, "5.80"
                    ));

            if (swipeRefreshLayout == null) {
                progressBarGridView.setVisibility(View.VISIBLE);
            }
            gridViewAdapter.setLoading(true);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject responseObject = response.json.getJSONObject("response");
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<VKPhoto>>() {
                        }.getType();
                        List<VKPhoto> vkPhotos = gson.fromJson(responseObject.getString("items"), type);
                        if (isClearAdapter) {
                            gridViewAdapter.clear();
                            gridViewAdapter.notifyDataSetChanged();
                        }
                        if (vkPhotos == null || vkPhotos.isEmpty()) {
                            if (gridViewAdapter.isEmpty()) {
                                nothingFoundTextView.setVisibility(View.VISIBLE);
                            } else {
                                //todo: https://vk.com/bugtracker?act=show&id=72826
                                //todo: https://vk.com/bugtracker?act=show&id=72828
                                //todo: https://vk.com/bugtracker?act=show&id=72830

                                //fixme: баг в VK API: при определенных оффсетах возвращает пустой список, притом что фотографии ещё имеются (их можно получить при других оффсетах и count)
                                //fixme: т.о. не все фотографии выгружаются...
                                //fixme: справедливо для сочетания полей offset и count при фиксированных других полях
                                gridViewAdapter.setAllDownloaded(true);
                            }
                        } else {
                            nothingFoundTextView.setVisibility(View.GONE);
                            offset += vkPhotos.size();
                            //fixme: баг VK API: возвращаются дублированные фото (при определенных сочетаниях полей offset и count)
                            //fixme: возможно, связан с багом выше
                            for (VKPhoto vkPhoto : vkPhotos) {
                                if (!gridViewAdapter.contains(vkPhoto)) {
                                    gridViewAdapter.add(vkPhoto);
                                }
                            }
                            gridViewAdapter.notifyDataSetChanged();
                            if (photoPagerAdapter != null) {
                                photoPagerAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        if (swipeRefreshLayout == null) {
                            progressBarGridView.setVisibility(View.GONE);
                        }
                        gridViewAdapter.setLoading(false);
                    }
                }

                @Override
                public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                    super.attemptFailed(request, attemptNumber, totalAttempts);
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    progressBarGridView.setVisibility(View.GONE);
                    gridViewAdapter.setLoading(false);
                    if (error.errorCode == VKError.VK_API_ERROR) {
                        if (error.apiError.errorCode == 6) {
                            Toast.makeText(gridViewAdapter.getContext(), R.string.frequent_updates, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(gridViewAdapter.getContext(), error.apiError.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                    super.onProgress(progressType, bytesLoaded, bytesTotal);
                }
            });
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Не забывать вызвать после конструктора!
     */
    public void setViews(ProgressBar progressBarGridView, TextView nothingFoundTextView) {
        this.progressBarGridView = progressBarGridView;
        this.nothingFoundTextView = nothingFoundTextView;
    }

    public void update(Point fullAddress, String radius) {
        this.fullAddress = fullAddress;
        this.radius = radius;
        this.offset = 0;
    }

}
