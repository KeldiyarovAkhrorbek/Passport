package uz.projects.passport.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import uz.projects.passport.entity.Passport
import uz.projects.passport.R
import uz.projects.passport.databinding.ItemPassportBinding

class PassportAdapter(
    var list: List<Passport>,
    private var listener: OnItemClickPassport,
    var context: Context
) : RecyclerView.Adapter<PassportAdapter.MyHolder>() {


    inner class MyHolder(var itemPassportBinding: ItemPassportBinding) :
        RecyclerView.ViewHolder(itemPassportBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            ItemPassportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.itemPassportBinding.apply {
            tvName.text = "${position + 1}. ${list[position].surname}  ${list[position].name}"
            tvPassportSeries.text = list[position].seriesNumber
            menuBtn.setOnClickListener {
                val popupMenu = PopupMenu(context, menuBtn)
                popupMenu.menuInflater.inflate(R.menu.menu_item, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { it1 ->
                    when (it1.itemId) {
                        R.id.menu_edit -> listener.onItemClickEdit(list[position], position)
                        R.id.menu_delete -> listener.onItemClickDelete(list[position], position)
                    }
                    true
                }
                popupMenu.show()

            }
        }
        holder.itemView.setOnClickListener {
            listener.onItemClickPassport(list[position], position)
        }
    }

    override fun getItemCount(): Int = list.size

    interface OnItemClickPassport {
        fun onItemClickPassport(passport: Passport, position: Int)
        fun onItemClickDelete(passport: Passport, position: Int)
        fun onItemClickEdit(passport: Passport, position: Int)
    }
}