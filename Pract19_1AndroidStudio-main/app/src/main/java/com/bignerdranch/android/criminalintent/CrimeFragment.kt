package com.bignerdranch.android.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bignerdranch.android.criminalintent.database.CrimeRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateTextView: TextView
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var fabDate: FloatingActionButton
    private lateinit var suspectButton: AppCompatButton
    private lateinit var reportButton: AppCompatButton

    private val REQUEST_CONTACT = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coroutineScope.launch {
            val crimes = CrimeRepository.get().getCrimes()
            crime = if (crimes.isNotEmpty()) crimes[0] else Crime(title = "Новое преступление", suspect = "")
            CrimeRepository.get().updateCrime(crime)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        dateTextView = view.findViewById(R.id.crime_date)
        fabDate = view.findViewById(R.id.fab_date)
        suspectButton = view.findViewById(R.id.ChooseSuspect)
        reportButton = view.findViewById(R.id.SendCrimeReport)

        coroutineScope.launch {
            val crimes = CrimeRepository.get().getCrimes()
            crime = if (crimes.isNotEmpty()) crimes[0] else Crime(title = "Новое преступление", suspect = "")

            titleField.setText(crime.title)
            solvedCheckBox.isChecked = crime.isSolved

            val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.format(crime.date)
            updateUI()
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
                val TextNull = s?.toString()?.trim()?.isNotEmpty() == true

                if (TextNull) {
                    solvedCheckBox.isEnabled = true
                    solvedCheckBox.isChecked = true
                    crime.isSolved = true
                    fabDate.show()
                } else {
                    solvedCheckBox.isEnabled = false
                    solvedCheckBox.isChecked = false
                    crime.isSolved = false
                    fabDate.hide()
                }

                coroutineScope.launch {
                    CrimeRepository.get().updateCrime(crime)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
            coroutineScope.launch {
                CrimeRepository.get().updateCrime(crime)
            }
        }

        fabDate.setOnClickListener {
            showDateSnackbar()
        }

        reportButton.setOnClickListener {
            val reportIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, "CriminalIntent Report")
            }
            val chooserIntent = Intent.createChooser(reportIntent, "Send crime report via:")
            startActivity(chooserIntent)
        }

        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

        suspectButton.setOnClickListener {
            startActivityForResult(pickContactIntent, REQUEST_CONTACT)
        }

        fabDate.hide()
    }

    private fun updateUI() {
        suspectButton.text = if (crime.suspect.isNotBlank()) crime.suspect else "Выбери виновника"
        solvedCheckBox.isChecked = crime.isSolved
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            "преступление совершено"
        } else {
            "преступление не  совершено"
        }
        val dateString = DateFormat.format("EEE, MMM, dd", crime.date).toString()

        val suspect = if (crime.suspect.isBlank()) {
            "виновника нет"
        } else {
            "виновник ${crime.suspect}"
        }

        return "${crime.title}. преступление совершено ${dateString}. ${solvedString}, и ${suspect}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            resultCode != android.app.Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                val cursor = contactUri?.let {
                    requireActivity().contentResolver.query(it, queryFields, null, null, null)
                }

                cursor?.use {
                    if (it.count > 0) {
                        it.moveToFirst()
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        coroutineScope.launch {
                            CrimeRepository.get().updateCrime(crime)
                            updateUI()
                        }
                    }
                }
            }
        }
    }

    private fun showDateSnackbar() {
        val dateFormat = SimpleDateFormat("EEEE, d MMM yyyy 'at' HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(crime.date)
        val message = "дата: $formattedDate"

        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}