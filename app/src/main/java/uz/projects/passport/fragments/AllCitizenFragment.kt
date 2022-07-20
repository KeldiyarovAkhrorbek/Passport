package uz.projects.passport.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import uz.projects.passport.R
import uz.projects.passport.adapters.PassportAdapter
import uz.projects.passport.database.MyDatabase
import uz.projects.passport.databinding.FragmentAllCitizenBinding
import uz.projects.passport.entity.Passport


class AllCitizenFragment : Fragment(R.layout.fragment_all_citizen) {
    private val binding by viewBinding(FragmentAllCitizenBinding::bind)
    lateinit var myDatabase: MyDatabase
    lateinit var list: ArrayList<Passport>
    lateinit var passportAdapter: PassportAdapter
    private var list2 = ArrayList<Passport>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myDatabase = MyDatabase.getInstance(requireContext())
        list = ArrayList(myDatabase.passportDao().getPassportList())
        setHasOptionsMenu(true)
        passportAdapter = PassportAdapter(list, object : PassportAdapter.OnItemClickPassport {
            override fun onItemClickPassport(passport: Passport, position: Int) {
                val action =
                    AllCitizenFragmentDirections.actionAllCitizenFragmentToAboutFragment(
                        passport.surname + " " + passport.name, passport.id
                    )
                findNavController().navigate(action)
            }

            override fun onItemClickDelete(passport: Passport, position: Int) {
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                list.removeAt(position)
                myDatabase.passportDao().deletePassport(passport)
                passportAdapter.notifyItemRemoved(position)
                passportAdapter.notifyItemRangeChanged(position, list.size)
            }

            override fun onItemClickEdit(passport: Passport, position: Int) {
                val action =
                    AllCitizenFragmentDirections.actionAllCitizenFragmentToEditFragment(passport.id)
                findNavController().navigate(action)
            }
        }, requireContext())


        binding.rv.adapter = passportAdapter

    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchDatabase(query: String) {
        val searchQuery = "%$query%"
        list2 = ArrayList(myDatabase.passportDao().searchDatabase(searchQuery))
        passportAdapter.list = list2
        passportAdapter.notifyDataSetChanged()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_sch, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Ism yoki familiya yozing..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchDatabase(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchDatabase(newText ?: "")
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }


}