package com.example.meetupmate

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class RecyclerViewHexagonAdapter(private val mData: List<User>, private val clickListener: OnHexagonClickListener) :
    RecyclerView.Adapter<RecyclerViewHexagonAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    interface OnHexagonClickListener {
        fun onHexagonClick(item: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hexagon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = mData[position]

        DatabaseManager.loadImageIntoImageView(item.profileImage, holder.ivHexagon)
        holder.tvHexagonUsername.text = item.username

        val isSelected = position == selectedPosition

        if (isSelected) {
            holder.ivHexagon.strokeColor = ColorStateList.valueOf(Color.parseColor("#a63d35"))
            holder.tvHexagonUsername.setTypeface(null, Typeface.BOLD)
            holder.ivHexagon.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(ShapeAppearanceModel.PILL)
                .build()
            holder.ivHexagon.strokeWidth = 8f
            holder.ivHexagon.layoutParams.width = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.profile_on_select)
            holder.ivHexagon.layoutParams.height = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.profile_on_select)
            (holder.llHexagon.layoutParams as ViewGroup.MarginLayoutParams).marginStart = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.ll_hexagon_margin_on_select)
        } else {
            holder.ivHexagon.strokeColor = ColorStateList.valueOf(Color.BLACK)
            holder.tvHexagonUsername.setTypeface(null, Typeface.NORMAL)
            holder.ivHexagon.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(ShapeAppearanceModel.PILL)
                .build()
            holder.ivHexagon.strokeWidth = 4f
            holder.ivHexagon.layoutParams.width = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.profile_default)
            holder.ivHexagon.layoutParams.height = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.profile_default)
            (holder.llHexagon.layoutParams as ViewGroup.MarginLayoutParams).marginStart = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.ll_hexagon_margin_default)
        }

        holder.ivHexagon.setOnClickListener {
            if (isSelected) {
                selectedPosition = RecyclerView.NO_POSITION
            } else {
                val previouslySelectedPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previouslySelectedPosition)
            }

            notifyItemChanged(position)

            if (selectedPosition == RecyclerView.NO_POSITION) {
                clickListener.onHexagonClick(User())
            } else {
                clickListener.onHexagonClick(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHexagon: ShapeableImageView = itemView.findViewById(R.id.ivHexagon)
        val tvHexagonUsername: TextView = itemView.findViewById(R.id.tvHexagonUsername)
        val llHexagon: LinearLayout = itemView.findViewById(R.id.llHexagon)
    }
}
