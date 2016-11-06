package com.bitsailer.yauc;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import com.bitsailer.yauc.api.model.AccessToken;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;

/**
 * Unit test for the {@link Preferences} storage in authenticated state.
 */

@SmallTest
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class PreferencesSetAccessTokenTest extends PreferencesAuthenticatedTest {

    @Test
    public void setAccessToken() {
        AccessToken response = new AccessToken();
        response.setAccessToken(TEST_ACCESS_TOKEN);
        response.setRefreshToken(TEST_REFRESH_TOKEN);
        response.setCreatedAt(1436544465);
        mPreferences.setAccessToken(response);
        verify(mMockEditor).putString(KEY_ACCESS_TOKEN, TEST_ACCESS_TOKEN);
        verify(mMockEditor).putString(KEY_REFRESH_TOKEN, TEST_REFRESH_TOKEN);
        verify(mMockEditor).putInt(KEY_ACCESS_TOKEN_CREATED_AT, TEST_CREATED_AT);
    }
}
