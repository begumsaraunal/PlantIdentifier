package com.example.plantidentifier.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plantidentifier.R

data class OnboardingPage(
    val iconRes: Int,
    val titleRes: Int,
    val descriptionRes: Int
)

class OnboardingAdapter :
    RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    private val pages = listOf(
        OnboardingPage(
            iconRes = R.drawable.plant,
            titleRes = R.string.onboarding_title_1,
            descriptionRes = R.string.onboarding_description_1
        ),
        OnboardingPage(
            iconRes = R.drawable.camera,
            titleRes = R.string.onboarding_title_2,
            descriptionRes = R.string.onboarding_description_2
        ),
        OnboardingPage(
            iconRes = R.drawable.result,
            titleRes = R.string.onboarding_title_3,
            descriptionRes = R.string.onboarding_description_3
        )
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_onboarding,
                parent,
                false
            )

        return PageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PageViewHolder,
        position: Int
    ) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class PageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val icon =
            itemView.findViewById<ImageView>(R.id.imgIcon)

        private val title =
            itemView.findViewById<TextView>(R.id.tvTitle)

        private val description =
            itemView.findViewById<TextView>(R.id.tvDescription)

        fun bind(page: OnboardingPage) {
            icon.setImageResource(page.iconRes)
            title.setText(page.titleRes)
            description.setText(page.descriptionRes)
        }
    }
}
