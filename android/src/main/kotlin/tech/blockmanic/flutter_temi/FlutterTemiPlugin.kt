package tech.blockmanic.flutter_temi

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.PluginRegistry
import com.robotemi.sdk.*

//Method Call Handler can do V1 V2,
//FlutterPlugin Give me most of the things except Activity
//ActivityAware give me Activity

class FlutterTemiPlugin : MethodCallHandler, FlutterPlugin, ActivityAware {

    public lateinit var plugin: FlutterTemiPlugin

    public lateinit var application_context: Context
    public lateinit var activity: Activity

    private lateinit var onRobotLiftedEventChannel: EventChannel
    private lateinit var onBatteryStatusChangedListenerEventChannel: EventChannel

    private val robot: Robot = Robot.getInstance()
    private val goToLocationStatusChangedImpl: GoToLocationStatusChangedImpl = GoToLocationStatusChangedImpl()
    private val onBeWithMeStatusChangedImpl: OnBeWithMeStatusChangedImpl = OnBeWithMeStatusChangedImpl()
    private val onLocationsUpdatedImpl: OnLocationsUpdatedImpl = OnLocationsUpdatedImpl()
    private val nlpImpl: NlpImpl = NlpImpl()
    private val onUserInteractionChangedImpl: OnUserInteractionChangedImpl = OnUserInteractionChangedImpl()
    private val ttsListenerImpl: TtsListenerImpl = TtsListenerImpl()
    private val asrListenerImpl: ASRListenerImpl = ASRListenerImpl()

    private val wakeupWordListenerImpl: WakeupWordListenerImpl = WakeupWordListenerImpl()
    private val onConstraintBeWithStatusListenerImpl: OnConstraintBeWithStatusListenerImpl = OnConstraintBeWithStatusListenerImpl()

    //private val onTelepresenceStatusChangedListenerImpl : OnTelepresenceStatusChangedListenerImpl = OnTelepresenceStatusChangedListenerImpl()
    //private val onUsersUpdatedListenerImpl : OnUsersUpdatedListenerImpl = OnUsersUpdatedListenerImpl()
    private val onPrivacyModeChangedListenerImpl: OnPrivacyModeChangedListenerImpl = OnPrivacyModeChangedListenerImpl()
    private val onBatteryStatusChangedListenerImpl: OnBatteryStatusChangedListenerImpl = OnBatteryStatusChangedListenerImpl()
    private val onDetectionStateChangedListenerImpl: OnDetectionStateChangedListenerImpl = OnDetectionStateChangedListenerImpl()
    private val onRobotReadyListenerImpl: OnRobotReadyListenerImpl = OnRobotReadyListenerImpl()
    private val onRobotLiftedListenerImpl: OnRobotLiftedListenerImpl = OnRobotLiftedListenerImpl();


    companion object {

        private lateinit var channel: MethodChannel

        /// 保留旧版本的兼容
        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            channel = MethodChannel(registrar.messenger(), "flutter_temi")
            val plugin = FlutterTemiPlugin()
            channel.setMethodCallHandler(plugin)

            //added
            plugin.application_context = registrar.activity().getApplication()
            plugin.activity = registrar.activity()


            val onBeWithMeEventChannel = EventChannel(registrar.messenger(), OnBeWithMeStatusChangedImpl.STREAM_CHANNEL_NAME)
            onBeWithMeEventChannel.setStreamHandler(plugin.onBeWithMeStatusChangedImpl)

            val onLocationStatusChangeEventChannel = EventChannel(registrar.messenger(), GoToLocationStatusChangedImpl.STREAM_CHANNEL_NAME)
            onLocationStatusChangeEventChannel.setStreamHandler(plugin.goToLocationStatusChangedImpl)

            val onLocationsUpdatedEventChannel = EventChannel(registrar.messenger(), OnLocationsUpdatedImpl.STREAM_CHANNEL_NAME)
            onLocationsUpdatedEventChannel.setStreamHandler(plugin.onLocationsUpdatedImpl)

            val onNlpEventChannel = EventChannel(registrar.messenger(), NlpImpl.STREAM_CHANNEL_NAME)
            onNlpEventChannel.setStreamHandler(plugin.nlpImpl)

            val ttsListenerEventChannel = EventChannel(registrar.messenger(), TtsListenerImpl.STREAM_CHANNEL_NAME)
            ttsListenerEventChannel.setStreamHandler(plugin.ttsListenerImpl)

            val asrListenerEventChannel = EventChannel(registrar.messenger(), ASRListenerImpl.STREAM_CHANNEL_NAME)
            asrListenerEventChannel.setStreamHandler(plugin.asrListenerImpl)

            val wakeupWordListenerEventChannel = EventChannel(registrar.messenger(), WakeupWordListenerImpl.STREAM_CHANNEL_NAME)
            wakeupWordListenerEventChannel.setStreamHandler(plugin.wakeupWordListenerImpl)

            val onConstraintBeWithStatusListenerEventChannel = EventChannel(registrar.messenger(), OnConstraintBeWithStatusListenerImpl.STREAM_CHANNEL_NAME)
            onConstraintBeWithStatusListenerEventChannel.setStreamHandler(plugin.onConstraintBeWithStatusListenerImpl)

            //val onTelepresenceStatusChangedListenerEventChannel = EventChannel(registrar.messenger(), OnTelepresenceStatusChangedListenerImpl.STREAM_CHANNEL_NAME)
            //onTelepresenceStatusChangedListenerEventChannel.setStreamHandler(plugin.onTelepresenceStatusChangedListenerImpl)

            //val onUsersUpdatedListenerEventChannel = EventChannel(registrar.messenger(), OnUsersUpdatedListenerImpl.STREAM_CHANNEL_NAME)
            //onUsersUpdatedListenerEventChannel.setStreamHandler(plugin.onUsersUpdatedListenerImpl)

            val onPrivacyModeChangedListenerEventChannel = EventChannel(registrar.messenger(), OnPrivacyModeChangedListenerImpl.STREAM_CHANNEL_NAME)
            onPrivacyModeChangedListenerEventChannel.setStreamHandler(plugin.onPrivacyModeChangedListenerImpl)

            val onDetectionStateChangedEventChannel = EventChannel(registrar.messenger(), OnDetectionStateChangedListenerImpl.STREAM_CHANNEL_NAME)
            onDetectionStateChangedEventChannel.setStreamHandler(plugin.onDetectionStateChangedListenerImpl)

            val onRobotReadyEventChannel = EventChannel(registrar.messenger(), OnRobotReadyListenerImpl.STREAM_CHANNEL_NAME)
            onRobotReadyEventChannel.setStreamHandler(plugin.onRobotReadyListenerImpl)
        }
    }

    init {
        robot.addOnGoToLocationStatusChangedListener(this.goToLocationStatusChangedImpl)
        robot.addOnBeWithMeStatusChangedListener(this.onBeWithMeStatusChangedImpl)
        robot.addOnLocationsUpdatedListener(this.onLocationsUpdatedImpl)
        robot.addNlpListener(this.nlpImpl)
        robot.addOnUserInteractionChangedListener(this.onUserInteractionChangedImpl)
        robot.addTtsListener(this.ttsListenerImpl)
        robot.addAsrListener(this.asrListenerImpl) //asr
        robot.addWakeupWordListener(this.wakeupWordListenerImpl)
        robot.addOnConstraintBeWithStatusChangedListener(this.onConstraintBeWithStatusListenerImpl)
        //robot.addOnTelepresenceStatusChangedListener(this.onTelepresenceStatusChangedListenerImpl)
        //robot.addOnUsersUpdatedListener(this.onUsersUpdatedListenerImpl)
        robot.addOnPrivacyModeStateChangedListener(this.onPrivacyModeChangedListenerImpl)
        robot.addOnDetectionStateChangedListener(this.onDetectionStateChangedListenerImpl)
        robot.addOnRobotReadyListener(this.onRobotReadyListenerImpl)
        robot.addOnRobotLiftedListener(this.onRobotLiftedListenerImpl)
        robot.addOnBatteryStatusChangedListener(this.onBatteryStatusChangedListenerImpl)
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "temi_serial_number" -> {
                result.success(robot.serialNumber)
            }
            "temi_privacy_mode" -> {
                result.success(robot.privacyMode)
            }
            "temi_set_privacy_mode" -> {
                val privacyMode = call.arguments<Boolean>()
                robot.privacyMode = privacyMode
                result.success(privacyMode)
            }
            "temi_battery_data" -> {
                result.success(OnBatteryStatusChangedListenerImpl.batteryToMap(robot.batteryData!!))
            }
            "temi_set_hard_buttons_disabled" -> {
                val disabled = call.arguments<Boolean>()
                robot.isHardButtonsDisabled = disabled
                result.success(disabled)
            }
            "temi_is_hard_buttons_disabled" -> {
                result.success(robot.isHardButtonsDisabled)
                result.success(true)
            }
            "temi_show_top_bar" -> {
                robot.showTopBar()
                result.success(true)
            }
            "temi_hide_top_bar" -> {
                robot.hideTopBar()
                result.success(true)
            }
            "temi_speak" -> {
                val speech = call.arguments<String>()
                val request = TtsRequest.create(speech, true)
                robot.speak(request)
                result.success(true)
            }
            "temi_speak_force" -> {
                val speech = call.arguments<String>()
                val request = TtsRequest.create(speech, false)
                robot.speak(request)
                result.success(true)
            }
            "temi_finishe_conversation" -> {
                robot.finishConversation()
                result.success(true)
            }
            "temi_goto" -> {
                val location = call.arguments<String>()
                robot.goTo(location)
                result.success(true)
            }
            "temi_save_location" -> {
                val location = call.arguments<String>()
                result.success(robot.saveLocation(location))
            }
            "temi_get_locations" -> {
                result.success(robot.locations)
            }
            "temi_delete_location" -> {
                val location = call.arguments<String>()
                result.success(robot.deleteLocation(location))
            }
            "temi_be_with_me" -> {
                robot.beWithMe()
                result.success(true)
            }
            "temi_constraint_be_with" -> {
                robot.constraintBeWith()
                result.success(true)
            }
            "temi_follow_me" -> {
                robot.beWithMe()
                result.success(true)
            }
            "temi_stop_movement" -> {
                robot.stopMovement()
                result.success(true)
            }
            "temi_skid_joy" -> {
                val values = call.arguments<List<Double>>()
                robot.skidJoy(values[0].toFloat(), values[1].toFloat())
                result.success(true)
            }
            "temi_tilt_angle" -> {
                val tiltAngle = call.arguments<Int>()
                robot.tiltAngle(tiltAngle)
                result.success(true)
            }
            "temi_turn_by" -> {
                val turnByAngle = call.arguments<Int>()
                robot.turnBy(turnByAngle)
                result.success(true)
            }
            "temi_tilt_by" -> {
                val degrees = call.arguments<Int>()
                robot.tiltBy(degrees)
                result.success(true)
            }
            "temi_start_telepresence" -> {
                val arguments = call.arguments<List<String>>()
                result.success(robot.startTelepresence(arguments[0], arguments[1]))
            }
            "temi_user_info" -> {
                val userInfo = robot.adminInfo!!
                result.success(OnUsersUpdatedListenerImpl.contactToMap(userInfo))
            }
            "temi_get_contacts" -> {
                val contacts = robot.allContact
                val maps = contacts.map { contact -> OnUsersUpdatedListenerImpl.contactToMap(contact) }
                result.success(maps)
            }
            "temi_get_recent_calls" -> {
                val recentCalls = robot.recentCalls
                val recentCallMaps = recentCalls.map { call ->
                    val callMap = HashMap<String, Any?>(4)
                    callMap["callType"] = call.callType
                    callMap["sessionId"] = call.sessionId
                    callMap["timestamp"] = call.timestamp
                    callMap["userId"] = call.userId
                    callMap
                }
                result.success(recentCallMaps)
            }
            "temi_wakeup" -> {
                robot.wakeup()
                result.success(true)
            }
            "temi_showAppList" -> {
                robot.showAppList()
                result.success(true)
            }

            "temi_toggle_wakeup" -> {
                val disable = call.arguments<Boolean>()
                robot.toggleWakeup(disable)
                result.success(true)
            }
            "temi_toggle_navigation_billboard" -> {
                val hide = call.arguments<Boolean>()
                robot.toggleNavigationBillboard(hide)
                result.success(true)
            }
            "temi_turnKoiskMode" -> {
                val activityInfo = application_context.getPackageManager()
                        .getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA)
                robot.onStart(activityInfo)
                result.success(true)
            }
            "temi_repose" -> {
                robot.repose()
                result.success(true)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        val channel = MethodChannel(binding.binaryMessenger, "flutter_temi")
        plugin = FlutterTemiPlugin()
        application_context = binding.applicationContext
        channel.setMethodCallHandler(this)


        val onBeWithMeEventChannel = EventChannel(binding.binaryMessenger, OnBeWithMeStatusChangedImpl.STREAM_CHANNEL_NAME)
        onBeWithMeEventChannel.setStreamHandler(plugin.onBeWithMeStatusChangedImpl)

        val onLocationStatusChangeEventChannel = EventChannel(binding.binaryMessenger, GoToLocationStatusChangedImpl.STREAM_CHANNEL_NAME)
        onLocationStatusChangeEventChannel.setStreamHandler(plugin.goToLocationStatusChangedImpl)

        val onLocationsUpdatedEventChannel = EventChannel(binding.binaryMessenger, OnLocationsUpdatedImpl.STREAM_CHANNEL_NAME)
        onLocationsUpdatedEventChannel.setStreamHandler(plugin.onLocationsUpdatedImpl)

        val onNlpEventChannel = EventChannel(binding.binaryMessenger, NlpImpl.STREAM_CHANNEL_NAME)
        onNlpEventChannel.setStreamHandler(plugin.nlpImpl)

        val ttsListenerEventChannel = EventChannel(binding.binaryMessenger, TtsListenerImpl.STREAM_CHANNEL_NAME)
        ttsListenerEventChannel.setStreamHandler(plugin.ttsListenerImpl)

        val asrListenerEventChannel = EventChannel(binding.binaryMessenger, ASRListenerImpl.STREAM_CHANNEL_NAME)
        asrListenerEventChannel.setStreamHandler(plugin.asrListenerImpl)

        val wakeupWordListenerEventChannel = EventChannel(binding.binaryMessenger, WakeupWordListenerImpl.STREAM_CHANNEL_NAME)
        wakeupWordListenerEventChannel.setStreamHandler(plugin.wakeupWordListenerImpl)

        val onConstraintBeWithStatusListenerEventChannel = EventChannel(binding.binaryMessenger, OnConstraintBeWithStatusListenerImpl.STREAM_CHANNEL_NAME)
        onConstraintBeWithStatusListenerEventChannel.setStreamHandler(plugin.onConstraintBeWithStatusListenerImpl)

        //val onTelepresenceStatusChangedListenerEventChannel = EventChannel(registrar.messenger(), OnTelepresenceStatusChangedListenerImpl.STREAM_CHANNEL_NAME)
        //onTelepresenceStatusChangedListenerEventChannel.setStreamHandler(plugin.onTelepresenceStatusChangedListenerImpl)

        //val onUsersUpdatedListenerEventChannel = EventChannel(registrar.messenger(), OnUsersUpdatedListenerImpl.STREAM_CHANNEL_NAME)
        //onUsersUpdatedListenerEventChannel.setStreamHandler(plugin.onUsersUpdatedListenerImpl)

        val onPrivacyModeChangedListenerEventChannel = EventChannel(binding.binaryMessenger, OnPrivacyModeChangedListenerImpl.STREAM_CHANNEL_NAME)
        onPrivacyModeChangedListenerEventChannel.setStreamHandler(plugin.onPrivacyModeChangedListenerImpl)

        val onBatteryStatusChangedListenerEventChannel = EventChannel(binding.binaryMessenger, OnBatteryStatusChangedListenerImpl.STREAM_CHANNEL_NAME)
        onBatteryStatusChangedListenerEventChannel.setStreamHandler(plugin.onBatteryStatusChangedListenerImpl)

        val onDetectionStateChangedEventChannel = EventChannel(binding.binaryMessenger, OnDetectionStateChangedListenerImpl.STREAM_CHANNEL_NAME)
        onDetectionStateChangedEventChannel.setStreamHandler(plugin.onDetectionStateChangedListenerImpl)

        val onRobotReadyEventChannel = EventChannel(binding.binaryMessenger, OnRobotReadyListenerImpl.STREAM_CHANNEL_NAME)
        onRobotReadyEventChannel.setStreamHandler(plugin.onRobotReadyListenerImpl)

        val onRobotLiftedEventChannel = EventChannel(binding.binaryMessenger, OnRobotLiftedListenerImpl.STREAM_CHANNEL_NAME)
        onRobotLiftedEventChannel.setStreamHandler(plugin.onRobotLiftedListenerImpl)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {

    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

    }

    override fun onDetachedFromActivityForConfigChanges() {
        
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {

    }
}
