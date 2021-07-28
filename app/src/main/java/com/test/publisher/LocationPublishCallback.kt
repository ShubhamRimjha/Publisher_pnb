package com.test.publisher

import android.util.Log
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import java.util.*

/**
 * @Author: Shubham Rimjha
 * @Date: 22-07-2021
 */
class LocationPublishCallback(
    private val locationMapAdapter: LocationSubscribeMapAdapter,
    private val watchChannel: String
) :
    SubscribeCallback() {

    override fun status(pubnub: PubNub, pnStatus: PNStatus) {
        println("Status category: ${pnStatus.category}")
        // PNConnectedCategory, PNReconnectedCategory, PNDisconnectedCategory
        println("Status operation: ${pnStatus.operation}")
        // PNSubscribeOperation, PNHeartbeatOperation
        println("Status error: ${pnStatus.error}")
        // true or false
    }

    override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
        if (pnMessageResult.channel != watchChannel) {
            return
        }
        try {
            Log.d(TAG, "message: $pnMessageResult")
            locationMapAdapter.locationUpdated(
                JsonUtil.fromJson(
                    pnMessageResult.message.toString(),
                    LinkedHashMap::class.java
                )
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
        if (pnPresenceEventResult.channel != watchChannel) {
            return
        }
        Log.d(TAG, "presence: $pnPresenceEventResult")
    }

    companion object {
        private val TAG = LocationPublishCallback::class.java.name
    }

}