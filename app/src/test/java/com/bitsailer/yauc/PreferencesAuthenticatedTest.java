package com.bitsailer.yauc;

import android.content.SharedPreferences;

import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link Preferences} storage in authenticated state.
 */
abstract class PreferencesAuthenticatedTest extends PreferencesTest {

    static final String TEST_ACCESS_TOKEN = "asdfmuqpz23rhe";
    static final String TEST_REFRESH_TOKEN = "rasdfmuqpz23rh";
    static final int TEST_CREATED_AT = 1436544465;
    static final String TEST_USER_USERNAME = "king-lui";
    static final String TEST_USER_NAME = "Lui";
    static final String TEST_USER_ID = "lui4711";
    static final String TEST_USER_AVATAR = "https://source.unsplash.com/HRZUzoX1e6w/100x100";

    @Mock
    SharedPreferences.Editor mMockEditor;

    @Before
    public void setupAuthenticatedUser() {
        when(mMockSharedPreferences.getString(KEY_ACCESS_TOKEN, null))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(mMockSharedPreferences.getString(KEY_USER_USERNAME, null))
                .thenReturn(TEST_USER_USERNAME);
        when(mMockSharedPreferences.getString(KEY_USER_NAME, null))
                .thenReturn(TEST_USER_NAME);
        when(mMockSharedPreferences.edit())
                .thenReturn(mMockEditor);
        mockContext();
        mPreferences = Preferences.get(mMockContext);
    }
}
