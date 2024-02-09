package com.sokol.pizzadream.ui.favorites

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.FavoriteAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.R
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class FavoritesFragment : Fragment() {
    private lateinit var layoutEmptyFav: LinearLayout
    private lateinit var recyclerCart: RecyclerView
    private var recyclerViewState: Parcelable? = null
    private lateinit var favoritesViewModel: FavoritesViewModel
    private var adapter: FavoriteAdapter? = null
    private lateinit var favorite: FavoriteInterface
    private lateinit var btnGoToMenu: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        favoritesViewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)
        favoritesViewModel.initFavoriteInterface(requireContext())
        val root: View = inflater.inflate(R.layout.fragment_favorites, container, false)
        initView(root)
        favoritesViewModel.getMutableLiveDataFavoriteItems().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recyclerCart.visibility = View.GONE
                layoutEmptyFav.visibility = View.VISIBLE
            } else {
                recyclerCart.visibility = View.VISIBLE
                layoutEmptyFav.visibility = View.GONE
                if (recyclerCart.adapter == null) {
                    adapter = FavoriteAdapter(it, requireContext())
                    recyclerCart.adapter = adapter
                    recyclerCart.layoutManager?.onRestoreInstanceState(recyclerViewState)
                }
            }
        })
        return root
    }

    private fun initView(root: View) {
        favorite = FavoriteRepository(PizzaDatabase.getInstance(requireContext()).getFavoriteDAO())
        recyclerCart = root.findViewById(R.id.favorites_recycler)
        recyclerCart.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerCart.layoutManager = layoutManager
        recyclerCart.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        layoutEmptyFav = root.findViewById(R.id.empty_favorites)
        btnGoToMenu = root.findViewById(R.id.btn_go_to_menu)
        btnGoToMenu.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(MenuClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}