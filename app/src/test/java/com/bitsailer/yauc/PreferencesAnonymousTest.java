package com.bitsailer.yauc;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link Preferences} storage in anonymous state.
 */

@SmallTest
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class PreferencesAnonymousTest extends PreferencesTest {

    @Before
    public void setupAnonymousUser() {
        when(mMockSharedPreferences.getString(KEY_ACCESS_TOKEN, null))
                .thenReturn(null);
        when(mMockSharedPreferences.getString(KEY_USER_USERNAME, null))
                .thenReturn(null);
        when(mMockSharedPreferences.getString(KEY_USER_NAME, null))
                .thenReturn(null);
        mockContext();
        mPreferences = Preferences.get(mMockContext);
    }

    @Test
    public void NotAuthenticatedUser() {
        assertThat("Calling isAuthenticated is false with anonymous user",
                mPreferences.isAuthenticated(), is(false));
    }
}
