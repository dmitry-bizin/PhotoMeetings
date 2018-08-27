package com.photomeetings.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.photomeetings.R;
import com.photomeetings.fragments.GridViewFragment;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Serializable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorizationIfNeeded();
        setContentView(R.layout.main_activity);
    }

    private void startFragment() {
        Fragment gridViewFragment = new GridViewFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.fragment, gridViewFragment, "grid");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void authorizationIfNeeded() {
        if (VKAccessToken.currentToken() == null || VKAccessToken.currentToken().isExpired()) {
            VKSdk.login(this, VKScope.PHOTOS);
        } else {
            startFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {

            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                // Пользователь успешно авторизовался
                vkAccessToken.save();
                startFragment();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                if (error.errorCode == VKError.VK_CANCELED) {
                    Toast.makeText(getBaseContext(), "Для работы приложения необходима авторизация ВКонтакте!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
                authorizationIfNeeded();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && containsGrid(fragments)) {
            getSupportFragmentManager().popBackStack();
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private boolean containsGrid(List<Fragment> fragments) {
        for (Fragment fragment : fragments) {
            if ("grid".equals(fragment.getTag())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // если не очищать bundle, в нём будут хранится все фрагменты из адаптера (в них фото), это много по памяти и приводит к краху
        // возможно, это приведёт к краху при возврате в приложение (когда текущие фрагменты выместятся из памяти)...
        outState.clear();
    }

}
