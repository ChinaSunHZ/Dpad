package com.spencer_dev.dpad

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class IntervalRefreshActivity : AppCompatActivity(R.layout.activity_interval_refresh) {
    private val adapter = IntervalRefreshAdapter()

    private lateinit var rvList: DpadRecyclerView

    private val dataSource = MutableSharedFlow<MutableList<String>>(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rvList = findViewById(R.id.rv_list)
        rvList.adapter = adapter

        // ----------> START
        // This will cause position confusion problems
        rvList.addItemDecoration(
            DpadLinearSpacingDecoration.create(
                resources.getDimensionPixelSize(
                    R.dimen.dp_10,
                ),
                0,
                0,
            ),
        )
        rvList.addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelected(parent, child, position, subPosition)
                Log.i("Selection", "Position: $position")
                adapter.selectedItem = adapter.getItemOrNull(position)
            }
        })
        // ----------> END

        lifecycleScope.launch {
            dataSource.collect {
                setNewItemsAndKeepSelectedPositionForCurrentItem(it)
            }
        }

        startIntervalRefresh()
    }

    private fun setNewItemsAndKeepSelectedPositionForCurrentItem(items: MutableList<String>) {
        val currentSelectedPosition = rvList.getSelectedPosition()
        val targetSelectedItem = adapter.getItemOrNull(currentSelectedPosition)
        val targetSelectedItemsPosition = items.indexOfFirst { targetSelectedItem == it }

        adapter.setNewInstance(items)

        if (targetSelectedItemsPosition == -1) {
            adapter.selectedItem = adapter.getItemOrNull(0)
            rvList.setSelectedPosition(0)
        } else {
            adapter.selectedItem = targetSelectedItem
            rvList.setSelectedPosition(targetSelectedItemsPosition)
        }
    }

    private fun startIntervalRefresh() {
        lifecycleScope.launch(Dispatchers.IO) {
            var refreshCount = 0
            while (isActive) {
                dataSource.emit(
                    (0..19)
                        .map { it.toString() }
                        .toMutableList()
                        .apply {
                            if (refreshCount == 5) add("100")
                            refreshCount++
                        }.shuffled()
                        .toMutableList(),
                )
                delay(5_000L)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean = true
}

class IntervalRefreshAdapter : BaseQuickAdapter<String, IntervalRefreshHolder>(0) {

    var selectedItem: String? = null

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): IntervalRefreshHolder =
        IntervalRefreshHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_interval_refresh, parent, false),
        )

    override fun convert(
        holder: IntervalRefreshHolder,
        item: String,
    ) {
        holder.onBind(item, item == selectedItem)
    }
}

class IntervalRefreshHolder(
    itemView: View,
) : BaseViewHolder(itemView),
    DpadViewHolder {
    private var resources = itemView.context.resources

    private var llContainer: LinearLayoutCompat = itemView.findViewById(R.id.ll_container)
    private var ivImage: AppCompatImageView = itemView.findViewById(R.id.iv_image)
    private var tvText: AppCompatTextView = itemView.findViewById(R.id.tv_text)

    init {
        onViewHolderDeselected()
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        llContainer.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.dp_60)
        }
        llContainer.updatePadding(
            resources.getDimensionPixelSize(R.dimen.dp_10),
            0,
            resources.getDimensionPixelSize(R.dimen.dp_10),
            0,
        )
        llContainer.setBackgroundColor(Color.DKGRAY)

        ivImage.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.dp_30)
            width = resources.getDimensionPixelSize(R.dimen.dp_30)
        }

        tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
        tvText.setTextColor(Color.WHITE)
    }

    override fun onViewHolderDeselected() {
        llContainer.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.dp_50)
        }
        llContainer.updatePadding(0, 0, 0, 0)
        llContainer.setBackgroundColor(Color.WHITE)

        ivImage.updateLayoutParams<ViewGroup.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.dp_20)
            width = resources.getDimensionPixelSize(R.dimen.dp_20)
        }

        tvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        tvText.setTextColor(Color.BLACK)

        super.onViewHolderDeselected()
    }

    fun onBind(item: String, isSelected: Boolean) {
        tvText.text = item
        if (isSelected) {
            onViewHolderSelected()
        } else {
            onViewHolderDeselected()
        }
    }
}
