package com.andromeda.kunalbhatia.demo.hungamaplayer.data

import android.Manifest

class Constants {
    companion object{
        val UN_FOLLOWED= "UNFOLLOWED"
        val LIST_LIMIT = "15"
        val FOLLOWER = "accepted"
        val NOT_FOLLOWING = "not_following"
        val PENDING = "pending"
        val REJECTED = "rejected"
        val ACCEPTED = "accepted"
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        val ATHLETE_GROUP_STATUS_TYPE = "group_join"
        val NOT_MEMBER = "not_member"
        val JOINED = "joined"
        val REQUESTED = "requested"
        const val INVITATIONS = "Invitations"
        const val FROM_KEY = "from"
        const val IS_EDIT_KEY = "isEdit"
        const val GROUP_ID_KEY_ = "group_id"
        const val GROUP_ID_KEY = "groupId"
        const val POST_ID_KEY = "postId"
        const val POST_NOTIFICATION_TYPE_KEY = "postNotificationTypeKey"
        const val FROM_NOTIFICATION_KEY = "fromNotification"
        const val IS_ADMIN_KEY = "isAdmin"
        const val GROUP_STATUS = "groupStatus"
        const val FOLLOWING = "following"
        const val MALE = 1
        const val FEMALE = 2
        const val GROUP_TYPE_JOINED = "joined"
        const val GROUP_TYPE_CREATED = "created"
        const val POST_DETAILS = "post_details"

        val EXTRA_CHANNEL = "extra_channel"
        val EXTRA_CHANNEL_SID = "channel_sid"

        val POST = "post"
        val COMMENT = "comment"
        val GROUP = "group"
        val USER = "user"
    }


}