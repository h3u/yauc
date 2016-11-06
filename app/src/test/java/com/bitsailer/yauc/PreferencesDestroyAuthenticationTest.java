package com.bitsailer.yauc;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

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
public class PreferencesDestroyAuthenticationTest extends PreferencesAuthenticatedTest {

    @Test
    public void destroyAuthentication() {
        mPreferences.destroyAuthorization();
        verify(mMockEditor).remove(KEY_ACCESS_TOKEN);
        verify(mMockEditor).remove(KEY_ACCESS_TOKEN_CREATED_AT);
        verify(mMockEditor).remove(KEY_REFRESH_TOKEN);
        verify(mMockEditor).remove(KEY_USER_AVATAR);
        verify(mMockEditor).remove(KEY_USER_NAME);
        verify(mMockEditor).remove(KEY_USER_USERNAME);
    }
}
