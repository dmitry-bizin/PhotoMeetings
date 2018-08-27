package com.photomeetings.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.photomeetings.R;
import com.photomeetings.activities.MainActivity;
import com.photomeetings.model.vk.VKPhoto;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class InfoFragment extends Fragment {

    private LinearLayout infoLayout;
    private VKPhoto vkPhoto;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        vkPhoto = (VKPhoto) args.getSerializable("vkPhoto");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_fragment, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        infoLayout = view.findViewById(R.id.infoLayout);
        prepareTextView(mainActivity);
        prepareAddressTextView(mainActivity);
        return view;
    }

    private void prepareTextView(AppCompatActivity activity) {
        TextView textView = new TextView(activity);
        if (vkPhoto.getUserId() != null) {
            if (Integer.valueOf(100).equals(vkPhoto.getUserId())) {// от имени сообщества
                textView.append("Сообщество:\nhttps://vk.com/club" + (-vkPhoto.getOwnerId()) + "\n");
            } else {// от имени пользователя в сообществе
                textView.append("Пользователь:\nhttps://vk.com/id" + vkPhoto.getUserId() + "\n");
                textView.append("Сообщество:\nhttps://vk.com/club" + (-vkPhoto.getOwnerId()) + "\n");
            }
        } else {
            textView.append("Пользователь:\nhttps://vk.com/id" + vkPhoto.getOwnerId() + "\n");
        }
        if (!"".equals(vkPhoto.getText())) {
            textView.append("Текст к фотографии:\n" + vkPhoto.getText() + "\n");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMMM yyyy, HH:mm:ss", Locale.getDefault());
        textView.append("Дата загрузки фото:\n" + simpleDateFormat.format(vkPhoto.getDate() * 1_000) + "\n");
        textView.append("Адрес:");
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextSize(16);
        textView.setTextIsSelectable(true);
        Intent test = new Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru"));
        if (activity.getPackageManager().resolveActivity(test, 0) != null) {
            Linkify.addLinks(textView, Linkify.WEB_URLS);
        }
        infoLayout.addView(textView);
    }

    private void prepareAddressTextView(AppCompatActivity activity) {
        if (vkPhoto.getAddress() != null) {
            TextView addressTextView = new TextView(activity);
            addressTextView.setTypeface(Typeface.DEFAULT_BOLD);
            addressTextView.setTextSize(16);
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f", vkPhoto.getLat(), vkPhoto.getLng());
            Intent test = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (activity.getPackageManager().resolveActivity(test, 0) != null) {
                Pattern pattern = Pattern.compile(".*", Pattern.DOTALL);
                addressTextView.append(vkPhoto.getAddress());
                Linkify.addLinks(addressTextView, pattern,
                        String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f", vkPhoto.getLat(), vkPhoto.getLng()));
                infoLayout.addView(addressTextView);
            } else {
                addressTextView.append(vkPhoto.getAddress());
                addressTextView.setTextColor(Color.BLACK);
                addressTextView.setTextIsSelectable(true);
                infoLayout.addView(addressTextView);
            }
        }
    }


}
