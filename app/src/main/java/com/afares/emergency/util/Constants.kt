package com.afares.emergency.util

object Constants {
    const val RC_SIGN_IN = 123

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val PAGE_SIZE = 10

    const val COLLECTION_USERS = "users"
    const val COLLECTION_REQUESTS = "Requests"
    const val COLLECTION_MEDICAL_HISTORY = "Medical History"
    const val COLLECTION_HOSPITAL = "Hospital"
    const val COLLECTION_CIVIL_DEFENSE = "Civil Defense Center"

    const val AMBULANCE = "اسعاف"
    const val FIRE_FIGHTER = "دفاع مدني"

    const val PREFERENCES_NAME = "login_preferences"
    const val PREFERENCES_LOGIN_STATUS = "login_status"
    const val PREFERENCES_USER_TYPE = "user_type"
    const val DEFAULT_USER_TYPE = "none"

    // request status
    const val REQUESTED = "تم الطلب"
    const val LOADING = "تم الاستلام"
    const val FINISHED = "تم الانتهاء"

    // user type
    const val USER = "مستخدم"
    const val PARAMEDIC = "اسعاف"
    const val CIVIL_DEFENSE = "دفاع مدني"

    const val PICK_IMAGE_REQUEST = 1
    const val PHOTO_URL = "photoUrl"

}