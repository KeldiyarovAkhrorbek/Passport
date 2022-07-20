package uz.projects.passport.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import uz.projects.passport.R
import uz.projects.passport.database.MyDatabase
import uz.projects.passport.databinding.FragmentAboutBinding
import uz.projects.passport.entity.Passport

class AboutFragment : Fragment(R.layout.fragment_about) {
    private val binding by viewBinding(FragmentAboutBinding::bind)
    private lateinit var myDatabase: MyDatabase
    private val args: AboutFragmentArgs by navArgs()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val id = args.id
        myDatabase = MyDatabase.getInstance(requireContext())
        val passport: Passport = myDatabase.passportDao().searchCitizen(id)
        binding.apply {
            image.setImageURI(Uri.parse(passport.imagePath))
            tvFullname.text = passport.name + " " + passport.surname + " " + passport.fio
            address.text = "Yashash manzili: " + passport.address
            givenDate.text = "Berilgan vaqti: " + passport.date
            lifetime.text = "Tugash vaqti: " + passport.lifetime
            gender.text = "Jinsi: " + passport.gender
            tvPassportSeries.text = "Seriya va raqami: " + passport.seriesNumber
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}