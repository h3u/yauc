package com.bitsailer.yauc;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import com.bitsailer.yauc.api.model.ProfileImage;
import com.bitsailer.yauc.api.model.User;

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
public class PreferencesSetUserTest extends PreferencesAuthenticatedTest {

    @Test
    public void setUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setName(TEST_USER_NAME);
        user.setUsername(TEST_USER_USERNAME);
        ProfileImage image = new ProfileImage();
        image.setSmall(TEST_USER_AVATAR);
        user.setProfileImage(image);
        mPreferences.setUser(user);
        verify(mMockEditor).putString(KEY_USER_ID, TEST_USER_ID);
        verify(mMockEditor).putString(KEY_USER_NAME, TEST_USER_NAME);
        verify(mMockEditor).putString(KEY_USER_USERNAME, TEST_USER_USERNAME);
        verify(mMockEditor).putString(KEY_USER_AVATAR, TEST_USER_AVATAR);
    }
}
