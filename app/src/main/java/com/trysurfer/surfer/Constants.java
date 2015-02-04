package com.trysurfer.surfer;

/**
 * Created by pro on 25.11.2014.
 */
public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.trysurfer.surfer.screenlockservice.action.main";
        public static String PREV_ACTION = "com.trysurfer.surfer.screenlockservice.action.prev";
        public static String PLAY_ACTION = "com.trysurfer.surfer.screenlockservice.action.play";
        public static String NEXT_ACTION = "com.trysurfer.surfer.screenlockservice.action.next";
        public static String STARTFOREGROUND_ACTION = "com.trysurfer.surfer.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.trysurfer.surfer.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public interface SHAREDPREFS {
        public static final String APP_PREFS = "app_prefs";
        public static final String USER_ID = "user_id";
        public static final String USER_FB_ID = "user_fb_id";
        public static final String USER_EMAIL = "user_email";
        public static final String USER_AUTH_TOKEN = "user_auth_token";
        public static final String USER_BIRTHDAY = "user_birthday";
        public static final String USER_GENDER = "user_gender";
        public static final String USER_POINTS = "user_points";
        public static final String USER_INTERESTS_PICKED = "user_interests_picked";
        public static final String USER_INTERESTS_LIST = "user_interests_list";
        public static final String USER_LOCATION = "user_location";
        public static final String USER_STORED = "user_stored";
        public static final String INTRO_SHOWN = "intro_shown";
        public static final String COMMERCIAL_COOLDOWN = "commercial_cooldown";
        public static final String TRACKERS_STORED = "trackers_stored";
        public static final String TRACKERS_LIST = "trackers_list";
    }
}
