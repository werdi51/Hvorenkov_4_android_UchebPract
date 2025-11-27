package com.bignerdranch.android.criminalintent
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment() {
    interface DatePickerListener {
        fun onDateSelected(date: Date)
    }
    private var listener: DatePickerListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePicker(requireContext())
        datePicker.init(year, month, day, null)

        return AlertDialog.Builder(requireContext())
            .setView(datePicker)
            .setTitle(getString(R.string.choose_date_title))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, datePicker.year)
                    set(Calendar.MONTH, datePicker.month)
                    set(Calendar.DAY_OF_MONTH, datePicker.dayOfMonth)
                }
                listener?.onDateSelected(selectedDate.time)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }

    fun setDatePickerListener(listener: DatePickerListener) {
        this.listener = listener
    }
}