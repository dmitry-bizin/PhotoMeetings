package com.photomeetings.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.activities.MainActivity;
import com.photomeetings.model.vk.VKPhoto;
import com.photomeetings.tasks.AsyncReverseGeocodingTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DetailsFragment extends Fragment {

    private TextView dateTextView;
    private TextView addressTextView;
    private ProgressBar progressBarDetailsView;
    private VKPhoto vkPhoto;
    private PhotoView photoView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        vkPhoto = (VKPhoto) args.getSerializable("vkPhoto");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final MainActivity mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.details_view_fragment, container, false);
        progressBarDetailsView = view.findViewById(R.id.progressBarDetailsView);
        prepareDateTextView(view);
        prepareAddressTextView(view);
        preparePhotoView(view);
        prepareFab(view);
        loadPhoto(mainActivity, photoView);
        new AsyncReverseGeocodingTask(addressTextView, vkPhoto).execute();
        return view;
    }

    private void preparePhotoView(final View view) {
        photoView = view.findViewById(R.id.gridItemImage);
        photoView.setMaximumScale(4f);
        photoView.setMediumScale(2f);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (dateTextView.getVisibility() == View.VISIBLE) {
                    dateTextView.animate().alpha(0.0f).setDuration(250);
                    dateTextView.setVisibility(View.GONE);
                } else {
                    dateTextView.animate().alpha(1.0f).setDuration(250);
                    dateTextView.setVisibility(View.VISIBLE);
                }
                if (addressTextView.getVisibility() == View.VISIBLE) {
                    addressTextView.animate().alpha(0.0f).setDuration(250);
                    addressTextView.setVisibility(View.GONE);
                } else {
                    addressTextView.animate().alpha(1.0f).setDuration(250);
                    addressTextView.setVisibility(View.VISIBLE);
                }
            }

        });
    }

    private void prepareDateTextView(View view) {
        dateTextView = view.findViewById(R.id.dateTextView);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMMM yyyy, HH:mm:ss", Locale.getDefault());
        dateTextView.append(simpleDateFormat.format(vkPhoto.getDate() * 1_000));
        dateTextView.setVisibility(View.GONE);
    }

    private void prepareAddressTextView(View view) {
        addressTextView = view.findViewById(R.id.addressTextView);
        addressTextView.setVisibility(View.GONE);
    }

    private void prepareFab(View view) {
        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setRippleColor(Color.BLUE);
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getUri()));
        if (getActivity().getPackageManager().resolveActivity(intent, 0) == null) {
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().startActivity(intent);
            }

        });
    }

    private String getUri() {
        String uri;
        if (vkPhoto.getUserId() != null) {
            if (Integer.valueOf(100).equals(vkPhoto.getUserId())) {// от имени сообщества
                uri = "https://vk.com/club" + (-vkPhoto.getOwnerId());// ссылка на сообщество
            } else {
                uri = "https://vk.com/id" + vkPhoto.getUserId();// от имени пользователя в сообществе
            }
        } else {
            uri = "https://vk.com/id" + vkPhoto.getOwnerId();
        }
        return uri;
    }

    private void loadPhoto(final Context context, PhotoView photoView) {
        progressBarDetailsView.setVisibility(View.VISIBLE);
        Picasso.get().load(vkPhoto.getPhotoForDetails()).into(photoView, new Callback() {

            @Override
            public void onSuccess() {
                progressBarDetailsView.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressBarDetailsView.setVisibility(View.GONE);
                Toast.makeText(context, "Произошла ошибка при загрузке фотографии!", Toast.LENGTH_LONG).show();
            }

        });
    }

    private void startFragment(VKPhoto vkPhoto) {
        Fragment infoFragment = new InfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("vkPhoto", vkPhoto);
        infoFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment, infoFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
