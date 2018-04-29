package app.kuluna.jp.auth2

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import app.kuluna.jp.auth2.commons.DataBindingAdapter
import app.kuluna.jp.auth2.databinding.ActivityMainBinding
import app.kuluna.jp.auth2.databinding.ListTotpBinding
import app.kuluna.jp.auth2.models.Totp

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

    }
}

class TotpAdapter(context: Context) : DataBindingAdapter<Totp, ListTotpBinding>(context, R.layout.list_totp) {
    override fun bind(holder: DataBindingViewHolder<ListTotpBinding>, item: Totp) {
        holder.binding.totp = item
    }
}
