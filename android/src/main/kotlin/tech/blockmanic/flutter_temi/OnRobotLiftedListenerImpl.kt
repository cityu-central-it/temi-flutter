package tech.blockmanic.flutter_temi

import com.robotemi.sdk.listeners.OnRobotLiftedListener
import io.flutter.plugin.common.EventChannel

class OnRobotLiftedListenerImpl: OnRobotLiftedListener, EventChannel.StreamHandler {
    private var eventSink: EventChannel.EventSink? = null

    companion object {
        const val STREAM_CHANNEL_NAME = "flutter_temi/on_robot_lifted_stream"
    }

    override fun onRobotLifted(isLifted: Boolean, reason: String) {
        val responseMap = HashMap<String, Any>(2)
        responseMap["isLifted"] = isLifted
        responseMap["reason"] = reason
        eventSink?.success(responseMap)
    }

    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(p0: Any?) {
        this.eventSink = null
    }

}