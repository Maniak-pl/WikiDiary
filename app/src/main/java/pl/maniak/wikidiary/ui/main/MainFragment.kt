package pl.maniak.wikidiary.ui.main


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.fragment.app.Fragment
import pl.maniak.wikidiary.R
import pl.maniak.wikidiary.data.Tag
import pl.maniak.wikidiary.data.WikiNote
import pl.maniak.wikidiary.databinding.FragmentMainBinding
import pl.maniak.wikidiary.domain.WikiNoteRepository
import pl.maniak.wikidiary.domain.WikiNoteRepositoryImpl
import pl.maniak.wikidiary.utils.helpers.DateHelper
import pl.maniak.wikidiary.utils.views.FlowLayout.LayoutParams
import java.util.*


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val repository: WikiNoteRepository = WikiNoteRepositoryImpl()
    private var tag = arrayListOf<Tag>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater)
        setUpView()
        return binding.root
    }

    private fun setUpView() {
        with(binding) {
            numberOfDay.text = DateHelper.getOnlyDayFromDate(Date())
            numberOfDay.setOnClickListener { showDatePicker() }
        }
        initTagContainer()
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.HOUR_OF_DAY, dayOfMonth)
            binding.numberOfDay.text = DateHelper.getOnlyDayFromDate(year, month, dayOfMonth)
        }

        DatePickerDialog(requireContext(), listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun initTagContainer() {
        binding.containerTags.removeAllViews()
        tag = arrayListOf(Tag(1, "Today"), Tag(2, "Work"))
        for (i in tag.indices) {
            val tv = requireActivity().layoutInflater.inflate(R.layout.tag_item, null) as TextView
            tv.text = tag[i].tag
            val params = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            params.setMargins(10, 10, 10, 10)
            tv.layoutParams = params
            tv.setOnClickListener { addWikiNote(tv.text.toString())}
            binding.containerTags.addView(tv)
        }
        binding.containerTags.invalidate()
    }

    private fun addWikiNote(tag: String) {
        if(tag.isNotEmpty()) {
            repository.saveNote(WikiNote(1, tag, binding.editText.text.toString(),Date(), false))
            binding.editText.text.clear()
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}