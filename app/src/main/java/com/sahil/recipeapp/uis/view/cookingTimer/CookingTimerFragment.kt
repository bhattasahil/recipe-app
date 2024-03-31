package com.sahil.recipeapp.uis.view.cookingTimer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.sahil.recipeapp.R
import com.sahil.recipeapp.databinding.FragmentCookingTimerBinding
import com.sahil.recipeapp.uis.view.RecipeDetailActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CookingTimerFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var mPreference: SharedPreferences

    private lateinit var mBinding: FragmentCookingTimerBinding
    private var timeInMinutes: Int? = 40
    private var recipeName = "Test"
    private var isAlreadyLessThanHour = false
    private var countDownTimer: CountDownTimer? = null

    private val notificationId = System.currentTimeMillis().toInt()

    private var startTime: Long = 0
    private var firstTime = false
    private var totalMillisRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = arguments?.getLong("start_time", 0) ?: 0

        if (startTime == 0.toLong()) {
            firstTime = true
            startTime = System.currentTimeMillis()
        }

        timeInMinutes = arguments?.getInt("ready_in_minutes", 40)
        recipeName = arguments?.getString("recipe_name") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentCookingTimerBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.parent as View).setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.transparent
            )
        )
        initView()
    }

    /**
     * Initializes view
     */
    private fun initView() {
        mBinding.close.setOnClickListener {
            dismiss()
        }

        mBinding.btnCancelCooking.visibility = View.GONE

        if (firstTime) {
            if (timeInMinutes != null) {
                startAlarm()
                val hours = timeInMinutes!! / 60
                if (hours <= 0) {
                    isAlreadyLessThanHour = true
                    mBinding.llHour.visibility = View.GONE
                }
                val progressMaxLimit = timeInMinutes!! * 60
                mBinding.cpvTimer.max = 100
                val timeInMilliSeconds = (timeInMinutes!! * 60 * 1000).toLong()
                var countDownInterval = 1
                countDownTimer =
                    object : CountDownTimer(timeInMilliSeconds, countDownInterval * 1000L) {
                        override fun onTick(p0: Long) {
                            totalMillisRemaining = p0
                            countDownInterval += 1
                            val totalSecondsRemaining = p0 / 1000
                            val totalHoursRemaining = totalSecondsRemaining / (60 * 60)
                            val totalMinutesRemaining = if (totalHoursRemaining > 0) {
                                (totalSecondsRemaining / 60) - (totalHoursRemaining * 60)
                            } else {
                                totalSecondsRemaining / 60
                            }

                            val secondsRemainingToDisplay =
                                if (totalHoursRemaining > 0) {
                                    totalSecondsRemaining - (totalHoursRemaining * 60 * 60) - (totalMinutesRemaining * 60)
                                } else {
                                    totalSecondsRemaining - (totalMinutesRemaining * 60)
                                }

                            val progressPercentage = (100 * countDownInterval) / progressMaxLimit
                            mBinding.cpvTimer.progress = progressPercentage
                            Log.e("$countDownInterval Progress", " $progressPercentage%")
                            Log.e(
                                "Remaining ",
                                " Minutes: $totalMinutesRemaining Seconds: $totalSecondsRemaining"
                            )
                            if (!isAlreadyLessThanHour) {
                                if (totalHoursRemaining <= 0) {
                                    mBinding.tvHourTenth.text =
                                        getString(R.string.label_default_hour)
                                    mBinding.tvHourZeroth.text =
                                        getString(R.string.label_default_hour)
                                } else {
                                    val tenthHr = totalHoursRemaining / 10
                                    val zerothHr = totalHoursRemaining % 10
                                    mBinding.tvHourTenth.text = tenthHr.toString()
                                    mBinding.tvHourZeroth.text = zerothHr.toString()
                                }
                            }

                            if (totalMinutesRemaining <= 0) {
                                mBinding.tvMinuteTenth.text = getString(R.string.label_default_hour)
                                mBinding.tvMinuteZeroth.text =
                                    getString(R.string.label_default_hour)
                            } else {
                                val tenthMin = totalMinutesRemaining / 10
                                val zerothMin = totalMinutesRemaining % 10
                                mBinding.tvMinuteTenth.text = tenthMin.toString()
                                mBinding.tvMinuteZeroth.text = zerothMin.toString()
                            }

                            if (secondsRemainingToDisplay <= 0) {
                                mBinding.tvSecondTenth.text = getString(R.string.label_default_hour)
                                mBinding.tvSecondZeroth.text =
                                    getString(R.string.label_default_hour)
                            } else {
                                val tenthSec = secondsRemainingToDisplay / 10
                                val zerothSec = secondsRemainingToDisplay % 10
                                mBinding.tvSecondTenth.text = tenthSec.toString()
                                mBinding.tvSecondZeroth.text = zerothSec.toString()
                            }
                        }

                        override fun onFinish() {
                            mBinding.tvTimerFinished.visibility = View.VISIBLE
                            mBinding.tvSecondTenth.text = getString(R.string.label_default_hour)
                            mBinding.tvSecondZeroth.text = getString(R.string.label_default_hour)
                            mBinding.cpvTimer.progress = 100
                        }

                    }.start()

                mBinding.btnCancelCooking.setOnClickListener {
                    cancelAlarm()
                    dismiss()
                }
            }
        } else {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime

            val timerInMillis: Long = (timeInMinutes!! * 60 * 1000).toLong()
            if (elapsedTime < timerInMillis) {
                val newTimeMills = timerInMillis - elapsedTime
                val hours = newTimeMills / (1000 * 60 * 60)
                if (hours <= 0) {
                    isAlreadyLessThanHour = true
                    mBinding.llHour.visibility = View.GONE
                }
                val progressMaxLimit = newTimeMills / 1000
                mBinding.cpvTimer.max = 100
                var countDownInterval = 1
                countDownTimer =
                    object : CountDownTimer(newTimeMills, countDownInterval * 1000L) {
                        override fun onTick(p0: Long) {
                            totalMillisRemaining = p0
                            countDownInterval += 1
                            val totalSecondsRemaining = p0 / 1000
                            val totalHoursRemaining = totalSecondsRemaining / (60 * 60)
                            val totalMinutesRemaining = if (totalHoursRemaining > 0) {
                                (totalSecondsRemaining / 60) - (totalHoursRemaining * 60)
                            } else {
                                totalSecondsRemaining / 60
                            }

                            val secondsRemainingToDisplay =
                                if (totalHoursRemaining > 0) {
                                    totalSecondsRemaining - (totalHoursRemaining * 60 * 60) - (totalMinutesRemaining * 60)
                                } else {
                                    totalSecondsRemaining - (totalMinutesRemaining * 60)
                                }

                            val progressPercentage = (100 * countDownInterval) / progressMaxLimit
                            mBinding.cpvTimer.progress = progressPercentage.toInt()
                            Log.e("$countDownInterval Progress", " $progressPercentage%")
                            Log.e(
                                "Remaining ",
                                " Minutes: $totalMinutesRemaining Seconds: $totalSecondsRemaining"
                            )
                            if (!isAlreadyLessThanHour) {
                                if (totalHoursRemaining <= 0) {
                                    mBinding.tvHourTenth.text =
                                        getString(R.string.label_default_hour)
                                    mBinding.tvHourZeroth.text =
                                        getString(R.string.label_default_hour)
                                } else {
                                    val tenthHr = totalHoursRemaining / 10
                                    val zerothHr = totalHoursRemaining % 10
                                    mBinding.tvHourTenth.text = tenthHr.toString()
                                    mBinding.tvHourZeroth.text = zerothHr.toString()
                                }
                            }

                            if (totalMinutesRemaining <= 0) {
                                mBinding.tvMinuteTenth.text = getString(R.string.label_default_hour)
                                mBinding.tvMinuteZeroth.text =
                                    getString(R.string.label_default_hour)
                            } else {
                                val tenthMin = totalMinutesRemaining / 10
                                val zerothMin = totalMinutesRemaining % 10
                                mBinding.tvMinuteTenth.text = tenthMin.toString()
                                mBinding.tvMinuteZeroth.text = zerothMin.toString()
                            }

                            if (secondsRemainingToDisplay <= 0) {
                                mBinding.tvSecondTenth.text = getString(R.string.label_default_hour)
                                mBinding.tvSecondZeroth.text =
                                    getString(R.string.label_default_hour)
                            } else {
                                val tenthSec = secondsRemainingToDisplay / 10
                                val zerothSec = secondsRemainingToDisplay % 10
                                mBinding.tvSecondTenth.text = tenthSec.toString()
                                mBinding.tvSecondZeroth.text = zerothSec.toString()
                            }
                        }

                        override fun onFinish() {
                            mBinding.tvSecondTenth.text = getString(R.string.label_default_hour)
                            mBinding.tvSecondZeroth.text = getString(R.string.label_default_hour)
                            mBinding.cpvTimer.progress = 100
                        }

                    }.start()
            }
        }
    }

    /**
     * Sends call to AlarmReceiver to play sound
     */
    private fun startAlarm() {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val timeWhenAlarmIsToBeSet = System.currentTimeMillis() + (timeInMinutes!! * 60 * 1000)
        intent.putExtra("timeWhenAlarmIsToBeSet", timeWhenAlarmIsToBeSet)
        intent.putExtra("recipeName", recipeName)
        intent.putExtra("notificationId", notificationId)
        intent.action = "startAlarm"
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager =
            requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeWhenAlarmIsToBeSet, pendingIntent)
    }


    /**
     * Cancels alarm
     */
    private fun cancelAlarm() {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val timeWhenAlarmIsToBeSet = System.currentTimeMillis() + (timeInMinutes!! * 60 * 1000)
        intent.putExtra("timeWhenAlarmIsToBeSet", timeWhenAlarmIsToBeSet)
        intent.putExtra("recipeName", recipeName)
        intent.putExtra("notificationId", notificationId)
        intent.action = "startAlarm"
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager =
            requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancel alarm and timer if already present
     */
    override fun onStop() {
        super.onStop()
        cancelAlarm()
        setFragmentResult(
            RecipeDetailActivity.STARTED_TIMER_KEY,
            bundleOf(
                RecipeDetailActivity.STARTED_TIMER_RESULT to startTime,
                RecipeDetailActivity.STARTED_TIMER_RESULT_REM to totalMillisRemaining
            )
        )
        countDownTimer?.cancel()
    }

    companion object {
        const val ALARM_REQUEST_CODE = 123

        fun newInstance(duration: Int, startTime: Long, recipeName: String): CookingTimerFragment {
            val fragment = CookingTimerFragment()
            val args = Bundle()
            args.putInt("ready_in_minutes", duration)
            args.putLong("start_time", startTime)
            args.putString("recipe_name", recipeName)
            fragment.arguments = args
            return fragment
        }
    }

}