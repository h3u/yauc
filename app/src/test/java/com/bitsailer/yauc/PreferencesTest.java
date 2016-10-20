package com.bitsailer.yauc;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link Preferences} storage.
 */
abstract class PreferencesTest {

    static final String KEY_ACCESS_TOKEN = "key_access_token";
    static final String KEY_ACCESS_TOKEN_CREATED_AT = "key_access_token_created_at";
    static final String KEY_REFRESH_TOKEN = "key_refresh_token";
    static final String KEY_USER_ID = "key_user_id";
    static final String KEY_USER_NAME = "key_user_name";
    static final String KEY_USER_USERNAME = "key_user_username";
    static final String KEY_USER_AVATAR = "key_user_avatar";

    Preferences mPreferences;

    @Mock
    Context mMockContext;

    @Mock
    Context mMockApplicationContext;

    @Mock
    SharedPreferences mMockSharedPreferences;

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }

    void mockContext() {
        when(mMockApplicationContext.getSharedPreferences("yauc_prefs", Context.MODE_PRIVATE))
                .thenReturn(mMockSharedPreferences);
        when(mMockContext.getApplicationContext()).thenReturn(mMockApplicationContext);
    }
}
