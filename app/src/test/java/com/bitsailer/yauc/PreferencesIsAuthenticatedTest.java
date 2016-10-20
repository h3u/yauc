package com.bitsailer.yauc;

import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test for the {@link Preferences} storage in authenticated state.
 */

@SmallTest
@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class PreferencesIsAuthenticatedTest extends PreferencesAuthenticatedTest {

    @Test
    public void userIsAuthenticated() {
        assertThat(mPreferences.getUserUsername(), is(TEST_USER_USERNAME));
        assertThat(mPreferences.getUserName(), is(TEST_USER_NAME));
        assertThat(mPreferences.getAccessToken(), is(TEST_ACCESS_TOKEN));
        assertThat("Calling isAuthenticated is true with authenticated user",
                mPreferences.isAuthenticated(), is(true));
    }
}
